package info.fitapp.kompliziert

import android.app.Activity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout


class Hint(private val activity: Activity) : ViewTreeObserver.OnGlobalLayoutListener {

    override fun onGlobalLayout() {
        hintOverlay?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

        // Now add an icon and center it exactly
        val centerTop: Int = hintOverlay!!.measuredHeight / 2
        val centerRight: Int = hintOverlay!!.measuredWidth / 2

        //     return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        val iconDimensPx = 50f * activity.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT

        LayoutInflater.from(activity).inflate(R.layout.hint_icon, hintOverlay, true)
        val hintIcon = hintOverlay!!.findViewById<ImageView>(R.id.ivHintIcon)
        val hintIconLayoutParams = hintIcon.layoutParams as RelativeLayout.LayoutParams
        hintIconLayoutParams.topMargin = (centerTop - (iconDimensPx / 2)).toInt()
        hintIconLayoutParams.leftMargin = (centerRight - iconDimensPx / 2).toInt()

        // Also add the speech bubble.
        LayoutInflater.from(activity).inflate(R.layout.hint_bubble, hintOverlay, true)
        val bubbleLayout = hintOverlay!!.findViewById<LinearLayout>(R.id.llBubble)
        val bubbleLayoutParams = bubbleLayout.layoutParams as RelativeLayout.LayoutParams
        bubbleLayoutParams.addRule(RelativeLayout.BELOW, hintIcon.id)

        // Offset the speech bubble pointer.
        val bubblePointerWidth = 28f * activity.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
        val pointerLayout = hintOverlay!!.findViewById<ImageView>(R.id.ivBubbleTipTop)
        val pointerLayoutParams = pointerLayout.layoutParams as LinearLayout.LayoutParams
        pointerLayoutParams.marginStart = (centerRight - (bubblePointerWidth / 2)).toInt()
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