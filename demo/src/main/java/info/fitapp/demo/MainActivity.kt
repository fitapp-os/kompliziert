package info.fitapp.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import info.fitapp.kompliziert.AnchorCoordinates
import info.fitapp.kompliziert.Hint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var hint: Hint? = null
    private var showIcon = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val anchor = AnchorCoordinates(event.x.toInt(), event.y.toInt())
                hint = getFreshHint()
                hint?.customAnchor = anchor
                hint?.show()
            }
            return@setOnTouchListener false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_toggle_star) {
            showIcon = !showIcon
            item.setIcon(if (showIcon) R.drawable.ic_action_no_star else R.drawable.ic_action_star)

            if (hint?.isShowing == true) {
                val lastAnchor = hint!!.customAnchor
                hint!!.hide()

                hint = getFreshHint()
                hint?.customAnchor = lastAnchor
                hint?.show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFreshHint(): Hint {
        val icon = if (showIcon) R.drawable.ic_star_white_38dp else null
        return Hint.withData(this, null, R.string.hint_title, R.string.hint_message, icon, true)
    }
}
