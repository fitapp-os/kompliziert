package info.fitapp.kompliziert

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import kotlin.math.roundToInt


class Hint(
    private val activity: Activity,
    private val anchorView: View?,
    private val title: String?,
    private val message: String?,
    private val iconResource: Int?,
    private val pulsate: Boolean = false
) : ViewTreeObserver.OnGlobalLayoutListener {

    companion object {

        fun withData(
            activity: Activity,
            anchorView: View?,
            title: String,
            message: String,
            iconResource: Int?,
            pulsate: Boolean = false
        ): Hint {
            return Hint(activity, anchorView, title, message, iconResource, pulsate)
        }

        fun withData(
            activity: Activity,
            anchorView: View?,
            title: Int,
            message: Int,
            iconResource: Int?,
            pulsate: Boolean = false
        ): Hint {
            with(activity) {
                return Hint(this, anchorView, getString(title), getString(message), iconResource, pulsate)
            }
        }
    }

    var isShowing = false
    private var hintOverlay: RelativeLayout? = null

    override fun onGlobalLayout() {
        hintOverlay?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

        /*
         * Calculate the screen position.
         * If there is no anchorView we will center the hint on the screen.
         */

        val centerTop: Int
        val centerRight: Int

        if (anchorView != null) {
            val anchorViewPosition = IntArray(2)
            anchorView.getLocationOnScreen(anchorViewPosition)

            centerRight = anchorViewPosition[0] + anchorView.measuredWidth / 2
            centerTop = anchorViewPosition[1] + anchorView.measuredHeight / 2
        } else {
            centerTop = hintOverlay!!.measuredHeight / 2
            centerRight = hintOverlay!!.measuredWidth / 2
        }

        val middlePosition: Int = hintOverlay!!.measuredHeight / 2
        val attachToBottom = centerTop <= middlePosition
        val iconDimensPx = activity.resources.getDimensionPixelSize(R.dimen.hint_icon_dimen).toFloat()

        /*
         * Position the hint icon.
         * If the bubble will be attached to the bottom, the icon needs to be aligned to the top and positioned with
         * a top margin. Otherwise the icon needs to be aligned to the parent's bottom and positioned with a bottom
         * margin.
         */

        LayoutInflater.from(activity).inflate(R.layout.hint_icon, hintOverlay, true)
        val hintIconContainer = hintOverlay!!.findViewById<RelativeLayout>(R.id.rlHintIconContainer)
        val hintIconContainerLayoutParams = hintIconContainer.layoutParams as RelativeLayout.LayoutParams

        // Calculate both margins.
        val topMargin = (centerTop - (iconDimensPx / 2)).roundToInt()
        val bottomMargin = hintOverlay!!.measuredHeight - centerTop - iconDimensPx / 2

        // Position vertically.
        if (attachToBottom) {
            hintIconContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            hintIconContainerLayoutParams.topMargin = topMargin
        } else {
            hintIconContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            hintIconContainerLayoutParams.bottomMargin = bottomMargin.roundToInt()
        }

        // Position horizontally.
        hintIconContainerLayoutParams.leftMargin = (centerRight - iconDimensPx / 2).toInt()

        // Set the icon resource.
        val hintIcon = hintOverlay!!.findViewById<ImageView>(R.id.ivHintIcon)
        hintIcon.setImageResource(iconResource!!)

        // Pulsate
        if (pulsate) {
            val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                hintIcon,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f)
            )
            scaleDown.duration = 700
            scaleDown.repeatCount = ObjectAnimator.INFINITE
            scaleDown.repeatMode = ObjectAnimator.REVERSE
            scaleDown.interpolator = FastOutSlowInInterpolator()
            scaleDown.start()
        }

        /*
         * Position the speech bubble either above or below the icon using RelativeLayout parameters.
         */

        LayoutInflater.from(activity).inflate(R.layout.hint_bubble, hintOverlay, true)
        val bubbleLayout = hintOverlay!!.findViewById<LinearLayout>(R.id.llBubble)
        val bubbleLayoutParams = bubbleLayout.layoutParams as RelativeLayout.LayoutParams

        if (attachToBottom) {
            bubbleLayoutParams.addRule(RelativeLayout.BELOW, hintIconContainer.id)
        } else {
            bubbleLayoutParams.addRule(RelativeLayout.ABOVE, hintIconContainer.id)
        }

        // Offset the speech bubble pointer.
        val bubblePointerWidth =
            activity.resources.getDimensionPixelSize(R.dimen.hint_bubble_tip_width).toFloat()
        val pointerLayout =
            hintOverlay!!.findViewById<ImageView>(if (attachToBottom) R.id.ivBubbleTipTop else R.id.ivBubbleTipBottom)
        val pointerLayoutParams = pointerLayout.layoutParams as LinearLayout.LayoutParams
        pointerLayoutParams.marginStart = (centerRight - (bubblePointerWidth / 2)).toInt()

        // Hide the pointer that is on the opposite side of the icon.
        hintOverlay!!.findViewById<ImageView>(R.id.ivBubbleTipBottom).visibility =
                if (attachToBottom) View.GONE else View.VISIBLE
        hintOverlay!!.findViewById<ImageView>(R.id.ivBubbleTipTop).visibility =
                if (attachToBottom) View.VISIBLE else View.GONE

        // Apply texts.
        bubbleLayout.findViewById<TextView>(R.id.tvHintTitle).text = title
        bubbleLayout.findViewById<TextView>(R.id.tvHintMessage).text = message

        // Actually show the UI.
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.startOffset = 0
        fadeIn.duration = 400

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                hintOverlay?.visibility = View.VISIBLE
            }

        })

        hintOverlay?.animation = fadeIn
        hintOverlay?.startAnimation(fadeIn)
    }

    fun show() {
        with(activity) {
            val rootLayout = findViewById<FrameLayout>(android.R.id.content)
            hintOverlay = LayoutInflater.from(this).inflate(R.layout.hint_overlay, rootLayout, false) as RelativeLayout?
            rootLayout.addView(hintOverlay)
            hintOverlay?.setOnClickListener { hide() }
            isShowing = true

            hintOverlay!!.viewTreeObserver.addOnGlobalLayoutListener(this@Hint)
        }
    }

    fun hide() {
        with(activity) {
            isShowing = false
            val rootLayout = findViewById<FrameLayout>(android.R.id.content)
            val hintOverlay: View = rootLayout.findViewById(R.id.hintOverlay)

            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.interpolator = AccelerateInterpolator()
            fadeOut.startOffset = 0
            fadeOut.duration = 250

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    rootLayout.removeView(hintOverlay)
                }

                override fun onAnimationStart(animation: Animation?) {
                }

            })

            hintOverlay.animation = fadeOut
            hintOverlay.startAnimation(fadeOut)
        }
    }

}