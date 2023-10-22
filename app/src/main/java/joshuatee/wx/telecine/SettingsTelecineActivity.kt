/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.os.Bundle
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox

class SettingsTelecineActivity : BaseActivity() {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        box = VBox.fromResource(this)
        addSpinner()
        addSwitch()
    }

    private fun addSpinner() {
        val vidSize = listOf("100", "75", "50")
        val vidSpinner = ObjectSpinner(this, "Video Size", "video-size", "100", R.string.widget_nexrad_size_label, vidSize)
        box.addWidget(vidSpinner)
    }

    private fun addSwitch() {
        val configs = listOf(
                Switch(this, "Three Second Countdown", "show-countdown", R.string.loc1_radar_label),
                Switch(this, "Recording Notification", "recording-notification", R.string.loc1_radar_label),
        )
        configs.forEach {
            box.addWidget(it)
        }
    }

    override fun onStop() {
        super.onStop()
        MyApplication.initPreferences(this)
    }
}
