/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.ObjectSettingsCheckBox
import joshuatee.wx.settings.ObjectSettingsSpinner
import joshuatee.wx.ui.BaseActivity

class SettingsTelecineActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
        val vidSize = listOf("100", "75", "50")
        val vidSpinner = ObjectSettingsSpinner(
            this,
            "Video Size",
            "video-size",
            "100",
            R.string.widget_nexrad_size_label,
            vidSize
        )
        linearLayout.addView(vidSpinner.card)
        linearLayout.addView(
            ObjectSettingsCheckBox(
                this,
                "Three Second Countdown",
                "show-countdown",
                R.string.loc1_radar_label
            ).card
        )
        linearLayout.addView(
            ObjectSettingsCheckBox(
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
