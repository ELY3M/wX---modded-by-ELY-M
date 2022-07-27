/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.annotation.SuppressLint
import android.os.Bundle
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectSwitch
import joshuatee.wx.ui.VBox

class SettingsTelecineActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val box = VBox.fromResource(this)
        val vidSize = listOf("100", "75", "50")
        val vidSpinner = ObjectSpinner(
            this,
            "Video Size",
            "video-size",
            "100",
            R.string.widget_nexrad_size_label,
            vidSize
        )
        box.addWidget(vidSpinner.get())
        box.addWidget(
                ObjectSwitch(
                this,
                "Three Second Countdown",
                "show-countdown",
                R.string.loc1_radar_label
            ).get()
        )
        box.addWidget(
                ObjectSwitch(
                this,
                "Recording Notification",
                "recording-notification",
                R.string.loc1_radar_label
            ).get()
        )
    }

    override fun onStop() {
        super.onStop()
        MyApplication.initPreferences(this)
    }
}
