/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility

class SettingsDeveloperActivity : BaseActivity() {

    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Developer/Advanced Settings", "Settings might/will be deprecated in the future.")
        box = VBox.fromResource(this)
        addSwitch()
    }

    private fun addSwitch() {
        val configs = listOf(
            Switch(this, "Alert only once", "ALERT_ONLYONCE", R.string.alert_onlyonce_label),
            Switch(this, "Auto cancel notifs", "ALERT_AUTOCANCEL", R.string.alert_autocancel_label),
            Switch(
                this,
                "NWS Text: remove line breaks",
                "NWS_TEXT_REMOVELINEBREAKS",
                R.string.nws_text_remove_linebreak_label
            ),
            Switch(this, "Show VR button on main screen", "VR_BUTTON", R.string.vr_button_label),
            Switch(this, "Lock toolbars", "LOCK_TOOLBARS", R.string.lock_toolbars_label),
            Switch(this, "Use JNI for radar (beta)", "RADAR_USE_JNI", R.string.radar_use_jni_label),
            Switch(
                this,
                "Multipurpose radar icons",
                "WXOGL_ICONS_LEVEL2",
                R.string.radar_icons_level2_label
            ),
            Switch(
                this,
                "Counties use high resolution data",
                "RADAR_COUNTY_HIRES",
                R.string.county_hires_label
            ),
            Switch(
                this,
                "States use high resolution data",
                "RADAR_STATE_HIRES",
                R.string.state_hires_label
            ),
            Switch(this, "Black background", "NWS_RADAR_BG_BLACK", R.string.nws_black_bg_label),
            Switch(
                this,
                "Show graph in hourly activity",
                "HOURLY_SHOW_GRAPH",
                R.string.hourly_show_graph
            ),
            Switch(
                this,
                "Main screen scroll to top on restart",
                "MAIN_SCREEN_SCROLL_TOP_RESTART",
                R.string.main_screen_scroll_top_restart
            ),
        )
        configs.forEach {
            box.addWidget(it)
        }
    }

    // formerly onStop
    override fun onPause() {
        Utility.commitPref(this)
        MyApplication.initPreferences(this)
        val restartNotif = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
        }
        super.onPause()
    }
}
