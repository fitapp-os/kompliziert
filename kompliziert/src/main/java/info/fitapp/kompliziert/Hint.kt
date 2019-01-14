package info.fitapp.kompliziert

import android.app.Activity
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import java.util.*
import kotlin.math.roundToInt


class Hint(
    private val activity: Activity,
    private val anchorView: View?,
    private val title: String?,
    private val message: String?,
    private val iconResource: Int?
) : ViewTreeObserver.OnGlobalLayoutListener {

    companion object {
        const val TAG = "Hint"

        fun withData(activity: Activity, anchorView: View?, title: String, message: String, iconResource: Int?): Hint {
            return Hint(activity, anchorView, title, message, iconResource)
        }

        fun withData(activity: Activity, anchorView: View?, title: Int, message: Int, iconResource: Int?): Hint {
            with(activity) {
                return Hint(this, anchorView, getString(title), getString(message), iconResource)
            }
        }
    }

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

            Log.d(TAG, "Positioning hint with anchor view: " + Arrays.toString(anchorViewPosition))
            Log.d(TAG, "Anchor measured dimens: " + anchorView.measuredWidth + "x" + anchorView.measuredHeight)
            Log.d(TAG, "Anchor dimens: " + anchorView.width + "x" + anchorView.height)

        } else {
            Log.d(TAG, "Centering the hint because there's no anchor view.")
            centerTop = hintOverlay!!.measuredHeight / 2
            centerRight = hintOverlay!!.measuredWidth / 2
        }

        val middlePosition: Int = hintOverlay!!.measuredHeight / 2

        val attachToBottom = centerTop <= middlePosition

        val iconDimensPx = 50f * activity.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT

        /*
         * Position the hint icon.
         * If the bubble will be attached to the bottom, the icon needs to be aligned to the top and positioned with
         * a top margin. Otherwise the icon needs to be aligned to the parent's bottom and positioned with a bottom
         * margin.
         */

        LayoutInflater.from(activity).inflate(R.layout.hint_icon, hintOverlay, true)
        val hintIcon = hintOverlay!!.findViewById<ImageView>(R.id.ivHintIcon)
        val hintIconLayoutParams = hintIcon.layoutParams as RelativeLayout.LayoutParams

        // Set the icon resource.
        hintIcon.setImageResource(iconResource!!)

        // Calculate both margins.
        val topMargin = (centerTop - (iconDimensPx / 2)).roundToInt()
        val bottomMargin = hintOverlay!!.measuredHeight - centerTop - iconDimensPx / 2

        // Position vertically.
        if (attachToBottom) {
            hintIconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            hintIconLayoutParams.topMargin = topMargin
        } else {
            hintIconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            hintIconLayoutParams.bottomMargin = bottomMargin.roundToInt()
        }

        // Position horizontally.
        hintIconLayoutParams.leftMargin = (centerRight - iconDimensPx / 2).toInt()

        /*
         * Position the speech bubble either above or below the icon using RelativeLayout parameters.
         */

        LayoutInflater.from(activity).inflate(R.layout.hint_bubble, hintOverlay, true)
        val bubbleLayout = hintOverlay!!.findViewById<LinearLayout>(R.id.llBubble)
        val bubbleLayoutParams = bubbleLayout.layoutParams as RelativeLayout.LayoutParams

        if (attachToBottom) {
            bubbleLayoutParams.addRule(RelativeLayout.BELOW, hintIcon.id)
        } else {
            bubbleLayoutParams.addRule(RelativeLayout.ABOVE, hintIcon.id)
        }

        // Offset the speech bubble pointer.
        val bubblePointerWidth = 28f * activity.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
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
    }

    private var isShowing = false
    private var hintOverlay: RelativeLayout? = null

    // TODO: Create overlay before call to show()?


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
            rootLayout.removeView(rootLayout.findViewById(R.id.hintOverlay))
        }
    }

}