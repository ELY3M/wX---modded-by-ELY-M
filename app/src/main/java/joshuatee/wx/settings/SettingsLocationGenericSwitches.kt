/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
import android.app.Activity
import android.view.View
import android.widget.EditText
import joshuatee.wx.R
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility

@SuppressLint("SetTextI18n")
class SettingsLocationGenericSwitches(context: Activity, box: VBox, locationNumber: String, editTextLabel: EditText) {

    private var alertRadar1Sw: Switch
    private var alertSoundSw: Switch
    private var alert7Day1Sw: Switch
    private var alertCcSw: Switch
    private var alertSw: Switch
    private var alertMcdSw: Switch
    private var alertSwoSw: Switch
    private var alertSpcfwSw: Switch
    private var alertWpcmpdSw: Switch

    init {
        var alertNotificationCurrent = Utility.readPref(context, "ALERT" + locationNumber + "_NOTIFICATION", "false")
        var alertNotificationRadarCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_RADAR$locationNumber", "false")
        var alertCcNotificationCurrent = Utility.readPref(context, "ALERT_CC" + locationNumber + "_NOTIFICATION", "false")
        var alert7Day1NotificationCurrent = Utility.readPref(context, "ALERT_7DAY_" + locationNumber + "_NOTIFICATION", "false")
        var alertNotificationSoundCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SOUND$locationNumber", "false")
        var alertNotificationMcdCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_MCD$locationNumber", "false")
        var alertNotificationSwoCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SWO$locationNumber", "false")
        var alertNotificationSpcfwCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SPCFW$locationNumber", "false")
        var alertNotificationWpcmpdCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_WPCMPD$locationNumber", "false")
        // If this this is a new location
        if (To.int(locationNumber) == Location.numLocations + 1) {
            editTextLabel.setText("Location $locationNumber")
            editTextLabel.setSelection(editTextLabel.length())
            alertNotificationCurrent = "false"
            alertNotificationRadarCurrent = "false"
            alertCcNotificationCurrent = "false"
            alert7Day1NotificationCurrent = "false"
            alertNotificationSoundCurrent = "false"
            alertNotificationMcdCurrent = "false"
            alertNotificationSwoCurrent = "false"
            alertNotificationSpcfwCurrent = "false"
            alertNotificationWpcmpdCurrent = "false"
        }
        // FIXME TODO refactor this, use a map based off key
        alertSw = Switch(context,
                "Alert",
                "ALERT" + locationNumber + "_NOTIFICATION",
                R.string.alert_switch_text)
        alertSw.isChecked(alertNotificationCurrent == "true")
        alertCcSw = Switch(context,
                "Current Conditions",
                "ALERT_CC" + locationNumber + "_NOTIFICATION",
                R.string.alert_cc_switch_text)
        alertCcSw.isChecked(alertCcNotificationCurrent == "true")
        alert7Day1Sw = Switch(context,
                "7day",
                "ALERT_7DAY_" + locationNumber + "_NOTIFICATION",
                R.string.alert_7day_1_switch_text)
        alert7Day1Sw.isChecked(alert7Day1NotificationCurrent == "true")
        alertSoundSw = Switch(context,
                "Play sound for alert notification",
                "ALERT_NOTIFICATION_SOUND$locationNumber",
                R.string.alert_sound_switch_text)
        alertSoundSw.isChecked(alertNotificationSoundCurrent == "true")
        alertRadar1Sw = Switch(context,
                "Radar image with alert",
                "ALERT_NOTIFICATION_RADAR$locationNumber",
                R.string.alert_radar1_switch_text)
        alertRadar1Sw.isChecked(alertNotificationRadarCurrent == "true")
        alertMcdSw = Switch(context,
                "SPC MCD",
                "ALERT_NOTIFICATION_MCD$locationNumber",
                R.string.alert_mcd_switch_text)
        alertMcdSw.isChecked(alertNotificationMcdCurrent == "true")
        alertSwoSw = Switch(context,
                "SPC SWO",
                "ALERT_NOTIFICATION_SWO$locationNumber",
                R.string.alert_swo_switch_text)
        alertSwoSw.isChecked(alertNotificationSwoCurrent == "true")
        alertSpcfwSw = Switch(context,
                "SPC FW",
                "ALERT_NOTIFICATION_SPCFW$locationNumber",
                R.string.alert_spcfw_switch_text)
        alertSpcfwSw.isChecked(alertNotificationSpcfwCurrent == "true")
        alertWpcmpdSw = Switch(context,
                "WPC MPD",
                "ALERT_NOTIFICATION_WPCMPD$locationNumber",
                R.string.alert_wpcmpd_switch_text
        )
        alertWpcmpdSw.isChecked(alertNotificationWpcmpdCurrent == "true")
        listOf(
                alertSw,
                alertSoundSw,
                alertRadar1Sw,
                alertCcSw,
                alert7Day1Sw,
                alertMcdSw,
                alertSwoSw,
                alertSpcfwSw,
                alertWpcmpdSw
        ).forEach {
            box.addWidget(it)
        }
    }

    fun notificationsCanada(hide: Boolean) {
        val visibility = if (hide) {
            View.GONE
        } else {
            View.VISIBLE
        }
        listOf(alertMcdSw,
                alertSwoSw,
                alertSpcfwSw,
                alertWpcmpdSw
        ).forEach {
            it.visibility = visibility
        }
    }
}
