package info.fitapp.kompliziert

import android.app.Activity
import android.view.LayoutInflater
import android.widget.FrameLayout


class Hint(private val activity: Activity) {

    fun show() {
        with(activity) {
            val rootLayout = findViewById<FrameLayout>(android.R.id.content)
            val hintOverlay = LayoutInflater.from(this).inflate(R.layout.hint_overlay, rootLayout, false)
            rootLayout.addView(hintOverlay)
            hintOverlay.setOnClickListener { hide() }
        }
    }

    fun hide() {
        with(activity) {
            val rootLayout = findViewById<FrameLayout>(android.R.id.content)
            rootLayout.removeView(rootLayout.findViewById(R.id.rlHintOverlay))
        }
    }

}