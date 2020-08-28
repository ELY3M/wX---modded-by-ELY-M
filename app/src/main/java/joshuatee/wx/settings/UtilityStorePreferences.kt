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

import android.content.Context
import androidx.preference.PreferenceManager

object UtilityStorePreferences {

    fun setDefaults(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val value = preferences.getString("ALERT1_NOTIFICATION", null)
        if (value == null) {
            // Define the default location
            val stateDefault = "Oklahoma"
            val themeBlueDefault = "whiteNew"
            val locNumIntDefault = 1
            val loc1LabelDefault = "home"
            val alerts1Default = "true"
            val alertNotificationDefault = "false"
            val alertCcNotificationDefault = "false"
            val alert7Day1NotificationDefault = "false"
            val alertNotificationSoundDefault = "false"
            val alertNotificationRadarDefault = "false"
            val alertNotificationIntervalDefault = 12
            val alertBlackoutDefault = "false"
            val alertBlackoutAmDefault = 7
            val alertBlackoutPmDefault = 22
            val alertBlackoutTornadoDefault = "true"
            val current1Default = "true"
            val county1Default = "Cleveland"
            val zone1Default = "OKC027"
            val stateCodeDefault = "OK"
            val loc1XDefault = "35.231"
            val loc1YDefault = "-97.451"
            val loc1NwsDefault = "OUN"
            val wfoDefault = "OUN"
            val radarSiteDefault = "TLX"
            val nws1DefaultState = "OK"
            val nwsRadarBgBlack = "true"
            editor.putString("ALERT_ONLYONCE", "true")
            editor.putString("ALERT_AUTOCANCEL", "true")
            editor.putString("LOCK_TOOLBARS", "true")
            editor.putString("RADAR_SHOW_COUNTY", "true")
            editor.putString("ALERT1_NOTIFICATION", alertNotificationDefault)
            editor.putString("ALERT_CC1_NOTIFICATION", alertCcNotificationDefault)
            editor.putString("ALERT_7DAY_1_NOTIFICATION", alert7Day1NotificationDefault)
            editor.putString("ALERT_NOTIFICATION_RADAR1", alertNotificationRadarDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND1", alertNotificationSoundDefault)
            editor.putInt("ALERT_NOTIFICATION_INTERVAL", alertNotificationIntervalDefault)
            editor.putString("ALERT_BLACKOUT", alertBlackoutDefault)
            editor.putInt("ALERT_BLACKOUT_AM", alertBlackoutAmDefault)
            editor.putInt("ALERT_BLACKOUT_PM", alertBlackoutPmDefault)
            editor.putString("ALERT_BLACKOUT_TORNADO", alertBlackoutTornadoDefault)
            editor.putInt("LOC_NUM_INT", locNumIntDefault)
            editor.putString("LOC1_X", loc1XDefault)
            editor.putString("LOC1_Y", loc1YDefault)
            editor.putString("LOC1_NWS", loc1NwsDefault)
            editor.putString("LOC1_LABEL", loc1LabelDefault)
            editor.putString("COUNTY1", county1Default)
            editor.putString("ZONE1", zone1Default)
            editor.putString("ALERTS1", alerts1Default)
            editor.putString("CURRENT1", current1Default)
            editor.putString("STATE", stateDefault)
            editor.putString("STATE_CODE", stateCodeDefault)
            editor.putString("NWS1", wfoDefault)
            editor.putString("RID1", radarSiteDefault)
            editor.putString("NWS1_STATE", nws1DefaultState)
            editor.putString("THEME_BLUE", themeBlueDefault)
            editor.putString("NWS_RADAR_BG_BLACK", nwsRadarBgBlack)
            // NCEP default to GFS
            editor.putInt("MODEL_NCEP1_INDEX", 4)
            editor.putInt("MODEL_NCEP2_INDEX", 4)
            editor.apply()
        }
    }
}
