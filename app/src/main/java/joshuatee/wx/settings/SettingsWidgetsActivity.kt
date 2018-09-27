/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import android.widget.CompoundButton
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.Utility

class SettingsWidgetsActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {

    private val zoomStrArr = listOf("regional", "usa")
    private val nexradCenterArr = listOf("Center", "NW", "NE", "SW", "SE", "N", "E", "S", "W")

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_widgets, null, false)
        val locNumIntCurrent = Location.numLocations
        val locTruncateLen = 20
        val locationAl = (1 until locNumIntCurrent + 1).mapTo(mutableListOf()) { it.toString() + ": " + UtilityStringExternal.truncate(Utility.readPref(this, "LOC" + it + "_LABEL", ""), locTruncateLen) }
        val ll: LinearLayout = findViewById(R.id.ll)
        ll.addView(ObjectSettingsCheckBox(this, this, "Warnings in radar mosaic", "WIDGET_MOSAIC_WARNINGS", R.string.loc1_radar_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Do not show 7day in CC widget", "WIDGET_CC_DONOTSHOW_7_DAY", R.string.cc_widget_show_sevenday).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Download nexrad radar", WidgetFile.NEXRAD_RADAR.prefString, R.string.loc1_radar_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Download mosaics", WidgetFile.VIS.prefString, R.string.loc1_mosaics_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Download radar mosaic", WidgetFile.MOSAIC_RADAR.prefString, R.string.loc1_mosaics_rad_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Download AFD", WidgetFile.AFD.prefString, R.string.loc1_txt_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Download HWO", WidgetFile.HWO.prefString, R.string.loc1_txt_hwo_label).card)
        ll.addView(ObjectSettingsSpinner(this, this, "Radar mosaic level", "WIDGET_RADAR_LEVEL", "1km", R.string.widget_nexrad_size_label, zoomStrArr).card)
        ll.addView(ObjectSettingsSpinner(this, this, "Location", "WIDGET_LOCATION", "", R.string.spinner_location_label, locationAl).card)
        ll.addView(ObjectSettingsSpinner(this, this, "Nexrad centered at:", "WIDGET_NEXRAD_CENTER", "", R.string.nexrad_center_label, nexradCenterArr).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Widget check interval(m)", "CC_NOTIFICATION_INTERVAL", R.string.cc_interval_np_label, 60, 1, 120).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Widget nexrad size", "WIDGET_NEXRAD_SIZE", R.string.widget_nexrad_size_label, 10, 1, 15).card)
        val abSw: SwitchCompat = findViewById(R.id.ab_switch)
        abSw.setOnCheckedChangeListener(this)
        abSw.isChecked = Utility.readPref(this, "WIDGETS_ENABLED", "false").startsWith("t")
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.ab_switch -> {
                if (buttonView.isChecked) {
                    Utility.writePref(this, "WIDGETS_ENABLED", "true")
                } else {
                    Utility.writePref(this, "WIDGETS_ENABLED", "false")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        MyApplication.initPreferences(this)
        restartNotifs(this)
    }

    private fun restartNotifs(context: Context) {
        val restartNotif = Utility.readPref(context, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(context, "RESTART_NOTIF", "false")
        }
    }
}

