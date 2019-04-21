/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.ObjectSettingsCheckBox
import joshuatee.wx.settings.ObjectSettingsSpinner
import joshuatee.wx.ui.BaseActivity

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SettingsTelecineActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val vidSize = listOf("100", "75", "50")
        val vidSpinner = ObjectSettingsSpinner(
            this as Context,
            this as Activity,
            "Video Size",
            "video-size",
            "100",
            R.string.widget_nexrad_size_label,
            vidSize
        )
        ll.addView(vidSpinner.card)
        ll.addView(
            ObjectSettingsCheckBox(
                this,
                this,
                "Three Second Countdown",
                "show-countdown",
                R.string.loc1_radar_label
            ).card
        )
        ll.addView(
            ObjectSettingsCheckBox(
                this,
                this,
                "Recording Notification",
                "recording-notification",
                R.string.loc1_radar_label
            ).card
        )
    }

    override fun onStop() {
        super.onStop()
        MyApplication.initPreferences(this)
    }
}
