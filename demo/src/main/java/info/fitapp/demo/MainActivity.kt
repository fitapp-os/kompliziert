package info.fitapp.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import info.fitapp.kompliziert.AnchorCoordinates
import info.fitapp.kompliziert.Hint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var hint: Hint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d("MainActivity", "Touchdown detected.")
                val anchor = AnchorCoordinates(event.x.toInt(), event.y.toInt())
                hint = getFreshHint()
                hint?.customAnchor = anchor
                hint?.show()
            }
            return@setOnTouchListener false
        }
    }

    private fun getFreshHint(): Hint {
        return Hint.withData(this, null, R.string.hint_title, R.string.hint_message, null, true)
    }
}
