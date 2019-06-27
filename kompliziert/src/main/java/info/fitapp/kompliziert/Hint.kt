package info.fitapp.kompliziert

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
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

    var hintStyle = HintStyle()
    var isShowing = false
    var customAnchor: AnchorCoordinates? = null

    private var hintOverlay: RelativeLayout? = null

    override fun onGlobalLayout() {
        hintOverlay?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

        /*
         * Calculate the screen position.
         * If there is no anchorView we will center the hint on the screen.
         */
        val anchorCoordinates = when {
            customAnchor != null -> customAnchor!!
            anchorView != null -> {
                val anchorViewPosition = IntArray(2)
                anchorView.getLocationOnScreen(anchorViewPosition)

                AnchorCoordinates(
                    anchorViewPosition[0] + anchorView.measuredWidth / 2,
                    anchorViewPosition[1] + anchorView.measuredHeight / 2
                )
            }
            else -> AnchorCoordinates(
                hintOverlay!!.measuredWidth / 2,
                hintOverlay!!.measuredHeight / 2
            )
        }

        val totalHeight = hintOverlay!!.measuredHeight
        val totalWidth = hintOverlay!!.measuredWidth

        val middlePosition: Int = totalHeight / 2
        val attachToBottom = anchorCoordinates.y <= middlePosition

        /*
         * Place the hint icon on the screen.
         * If an icon is available, the result is the containing layout.
         */
        val hintIconContainer = placeIconOnScreen(anchorCoordinates, attachToBottom)
        val bubbleContainer: LinearLayout?

        bubbleContainer = if (hintIconContainer != null) {
            placeBubbleRelativeToIcon(hintIconContainer, attachToBottom)
        } else {
            placeBubbleRelativeToCoordinates(anchorCoordinates, totalHeight, attachToBottom)
        }

        bubbleContainer?.let {
            placeBubbleTip(anchorCoordinates, it, totalWidth, attachToBottom)
            applyBubbleTexts(it)
        }


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

    /*
     * Position the hint icon.
     * If the bubble will be attached to the bottom, the icon needs to be aligned to the top and positioned with
     * a top margin. Otherwise the icon needs to be aligned to the parent's bottom and positioned with a bottom
     * margin.
     */
    private fun placeIconOnScreen(coordinates: AnchorCoordinates, attachToBottom: Boolean): RelativeLayout? {
        if (iconResource == null) return null

        hintOverlay?.let {
            val iconDimensPx = activity.resources.getDimensionPixelSize(R.dimen.hint_icon_dimen).toFloat()

            LayoutInflater.from(activity).inflate(R.layout.hint_icon, it, true)
            val hintIconContainer = it.findViewById<RelativeLayout>(R.id.rlHintIconContainer)
            val hintIconContainerLayoutParams = hintIconContainer.layoutParams as RelativeLayout.LayoutParams

            // Calculate both margins.
            val topMargin = (coordinates.y - (iconDimensPx / 2)).roundToInt()
            val bottomMargin = it.measuredHeight - coordinates.y - iconDimensPx / 2

            // Position vertically.
            if (attachToBottom) {
                hintIconContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                hintIconContainerLayoutParams.topMargin = topMargin
            } else {
                hintIconContainerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                hintIconContainerLayoutParams.bottomMargin = bottomMargin.roundToInt()
            }

            // Position horizontally.
            hintIconContainerLayoutParams.leftMargin = (coordinates.x - iconDimensPx / 2).toInt()

            // Set the icon resource.
            val hintIcon = it.findViewById<ImageView>(R.id.ivHintIcon)
            hintIcon.setImageResource(iconResource)

            // Customize the background.
            it.findViewById<ImageView>(R.id.ivHintIconBackground).setImageResource(hintStyle.iconBackground)

            // Pulsate
            if (pulsate) {
                pulsateView(hintIcon)
            }

            return hintIconContainer
        }
        return null
    }

    private fun pulsateView(view: View) {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        scaleDown.duration = 700
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatMode = ObjectAnimator.REVERSE
        scaleDown.interpolator = FastOutSlowInInterpolator()
        scaleDown.start()
    }

    private fun placeBubbleTip(
        coordinates: AnchorCoordinates,
        bubbleContainer: LinearLayout,
        totalWidth: Int,
        attachToBottom: Boolean
    ) {

        val bubblePointerWidth = activity.resources.getDimensionPixelSize(R.dimen.hint_bubble_tip_width).toFloat()
        val minMargin = bubblePointerWidth.toInt()
        val maxMargin = (totalWidth - 2 * bubblePointerWidth).toInt()
        val suggestedMargin = (coordinates.x - (bubblePointerWidth / 2)).toInt()
        val margin = Math.min(maxMargin, Math.max(minMargin, suggestedMargin))

        val pointerLayout =
            bubbleContainer.findViewById<ImageView>(if (attachToBottom) R.id.ivBubbleTipTop else R.id.ivBubbleTipBottom)
        val pointerLayoutParams = pointerLayout.layoutParams as LinearLayout.LayoutParams
        pointerLayoutParams.marginStart = margin

        // Hide the pointer that is on the opposite side of the icon.
        bubbleContainer.findViewById<ImageView>(R.id.ivBubbleTipBottom).visibility =
            if (attachToBottom) View.GONE else View.VISIBLE
        bubbleContainer.findViewById<ImageView>(R.id.ivBubbleTipTop).visibility =
            if (attachToBottom) View.VISIBLE else View.GONE

    }

    private fun placeBubbleRelativeToIcon(
        iconContainer: RelativeLayout,
        attachToBottom: Boolean
    ): LinearLayout? {
        hintOverlay?.let {

            LayoutInflater.from(activity).inflate(R.layout.hint_bubble, hintOverlay, true)
            val bubbleLayout = it.findViewById<LinearLayout>(R.id.llBubble)
            val bubbleLayoutParams = bubbleLayout.layoutParams as RelativeLayout.LayoutParams

            if (attachToBottom) {
                bubbleLayoutParams.addRule(RelativeLayout.BELOW, iconContainer.id)
            } else {
                bubbleLayoutParams.addRule(RelativeLayout.ABOVE, iconContainer.id)
            }

            return bubbleLayout
        }

        return null
    }

    private fun placeBubbleRelativeToCoordinates(
        coordinates: AnchorCoordinates,
        totalHeight: Int,
        attachToBottom: Boolean
    ): LinearLayout? {
        hintOverlay?.let {

            LayoutInflater.from(activity).inflate(R.layout.hint_bubble, hintOverlay, true)
            val bubbleLayout = it.findViewById<LinearLayout>(R.id.llBubble)
            val bubbleLayoutParams = bubbleLayout.layoutParams as RelativeLayout.LayoutParams

            if (attachToBottom) {
                bubbleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                bubbleLayoutParams.topMargin = coordinates.y
            } else {
                bubbleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                bubbleLayoutParams.bottomMargin = totalHeight - coordinates.y
            }

            return bubbleLayout
        }

        return null
    }

    private fun applyBubbleTexts(container: View) {
        container.findViewById<TextView>(R.id.tvHintTitle).text = title
        container.findViewById<TextView>(R.id.tvHintMessage).text = message
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