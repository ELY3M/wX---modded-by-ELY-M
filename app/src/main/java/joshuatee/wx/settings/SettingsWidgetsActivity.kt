/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.Utility

class SettingsWidgetsActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {

    private val sectors = listOf("regional", "usa")
    private val nexradCenterList = listOf("Center", "NW", "NE", "SW", "SE", "N", "E", "S", "W")
    private lateinit var linearLayout: LinearLayout
    private lateinit var abSwitch: SwitchCompat

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_widgets, null, false)
        linearLayout = findViewById(R.id.linearLayout)
        abSwitch = findViewById(R.id.abSwitch)
        toolbar.subtitle = "Please tap on text for additional help."
        val locationNameShortLength = 20
        val locations = (1 until Location.numLocations + 1).map { "$it: " + UtilityStringExternal.truncate(Utility.readPref(this, "LOC" + it + "_LABEL", ""), locationNameShortLength) }
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Warnings in radar mosaic",
                        "WIDGET_MOSAIC_WARNINGS",
                        R.string.loc1_radar_warnings_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Do not show 7day in CC widget",
                        "WIDGET_CC_DONOTSHOW_7_DAY",
                        R.string.cc_widget_show_sevenday
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Download nexrad radar",
                        WidgetFile.NEXRAD_RADAR.prefString,
                        R.string.loc1_radar_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Download mosaics",
                        WidgetFile.VIS.prefString,
                        R.string.loc1_mosaics_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Download radar mosaic",
                        WidgetFile.MOSAIC_RADAR.prefString,
                        R.string.loc1_mosaics_rad_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Download AFD",
                        WidgetFile.AFD.prefString,
                        R.string.loc1_txt_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Download HWO",
                        WidgetFile.HWO.prefString,
                        R.string.loc1_txt_hwo_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSpinner(
                        this,
                        "Radar mosaic level",
                        "WIDGET_RADAR_LEVEL",
                        "1km",
                        R.string.widget_nexrad_size_label,
                        sectors
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSpinner(
                        this,
                        "Location",
                        "WIDGET_LOCATION",
                        "",
                        R.string.spinner_location_label,
                        locations
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSpinner(
                        this,
                        "Nexrad centered at:",
                        "WIDGET_NEXRAD_CENTER",
                        "",
                        R.string.nexrad_center_label,
                        nexradCenterList
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Widget check interval in minutes",
                        "CC_NOTIFICATION_INTERVAL",
                        R.string.cc_interval_np_label,
                        30,
                        1,
                        120
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Widget nexrad size",
                        "WIDGET_NEXRAD_SIZE",
                        R.string.widget_nexrad_size_label,
                        10,
                        1,
                        15
                ).card
        )
        abSwitch.setOnCheckedChangeListener(this)
        abSwitch.isChecked = Utility.readPref(this, "WIDGETS_ENABLED", "false").startsWith("t")
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.abSwitch -> {
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
        restartNotifications(this)
    }

    private fun restartNotifications(context: Context) {
        if (Utility.readPref(context, "RESTART_NOTIF", "false") == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(context, "RESTART_NOTIF", "false")
        }
    }
}

