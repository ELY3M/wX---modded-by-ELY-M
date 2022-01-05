package joshuatee.wx.fingerdraw

/*
Credit for this file goes to
FingerDraw https://github.com/ChrisLizon/FingerDraw (Chris Lizon)
 */

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.RelativeLayout
import joshuatee.wx.R

/**
 * FingerDrawActivity is an activity showcases using motion
 * events to create a simple finger painting experience.

 * @author Chris Lizon
 */

class FingerDrawActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.fingerdraw_layout)
        val rl: RelativeLayout = findViewById(R.id.rl)
        rl.setBackgroundColor(Color.TRANSPARENT)
        val drawView: DrawView = this.findViewById(R.id.drawing_screen_drawview)
        drawView.requestFocus()
    }
}