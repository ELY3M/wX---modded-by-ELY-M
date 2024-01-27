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

import android.content.Context
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.widgets.WidgetFile
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox
import joshuatee.wx.ui.NumberPicker
import joshuatee.wx.util.Utility

class SettingsWidgetsActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_widgets, null, false)
        setTitle("Widgets", GlobalVariables.preferencesHelpTitle)
        box = VBox.fromResource(this)
        addTopSwitch()
        addSwitch()
        addSpinner()
        addNumberPicker()
    }

    private fun addTopSwitch() {
        val abSwitch: SwitchCompat = findViewById(R.id.abSwitch)
        abSwitch.setOnCheckedChangeListener(this)
        abSwitch.isChecked = Utility.readPref(this, "WIDGETS_ENABLED", "false").startsWith("t")
    }

    private fun addSwitch() {
        val configs = listOf(
                Switch(this, "Do not show 7day in CC widget", "WIDGET_CC_DONOTSHOW_7_DAY", R.string.cc_widget_show_seven_day),
                Switch(this, "Download AFD", WidgetFile.AFD.prefString, R.string.loc1_txt_label),
                Switch(this, "Download HWO", WidgetFile.HWO.prefString, R.string.loc1_txt_hwo_label),
                Switch(this, "Download mosaics", WidgetFile.VIS.prefString, R.string.loc1_mosaics_label),
                Switch(this, "Download nexrad radar", WidgetFile.NEXRAD_RADAR.prefString, R.string.loc1_radar_label),
                Switch(this, "Download radar mosaic", WidgetFile.MOSAIC_RADAR.prefString, R.string.loc1_mosaics_rad_label),
        )
        configs.forEach {
            box.addWidget(it)
        }
    }

    private fun addSpinner() {
        val sectors = listOf("regional", "usa")
        val locations = (1..Location.numLocations).map {
            "$it: " + Utility.readPref(this, "LOC" + it + "_LABEL", "").take(20)
        }
        val nexradCenterList = listOf("Center", "NW", "NE", "SW", "SE", "N", "E", "S", "W")
        val spinners = listOf(
                ObjectSpinner(this, "Radar mosaic level", "WIDGET_RADAR_LEVEL", "1km", R.string.widget_nexrad_size_label, sectors),
                ObjectSpinner(this, "Location", "WIDGET_LOCATION", "", R.string.spinner_location_label, locations),
                ObjectSpinner(this, "Nexrad centered at:", "WIDGET_NEXRAD_CENTER", "", R.string.nexrad_center_label, nexradCenterList),
        )
        spinners.forEach {
            box.addWidget(it)
        }
    }

    private fun addNumberPicker() {
        val numberPickers = listOf(
                NumberPicker(this, "Widget check interval in minutes", "CC_NOTIFICATION_INTERVAL", R.string.cc_interval_np_label, 30, 1, 120),
                NumberPicker(this, "Widget nexrad size", "WIDGET_NEXRAD_SIZE", R.string.widget_nexrad_size_label, 10, 1, 15)
        )
        numberPickers.forEach {
            box.addWidget(it)
        }
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
