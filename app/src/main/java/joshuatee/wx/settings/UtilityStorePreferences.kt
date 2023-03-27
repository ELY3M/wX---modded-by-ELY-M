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
//modded by ELY M.

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
            editor.putString("ALERT_ONLYONCE", "true")
            editor.putString("ALERT_AUTOCANCEL", "true")
            editor.putString("LOCK_TOOLBARS", "true")
            editor.putString("RADAR_SHOW_COUNTY", "true")
            editor.putString("ALERT1_NOTIFICATION", "false")
            editor.putString("ALERT_CC1_NOTIFICATION", "false")
            editor.putString("ALERT_7DAY_1_NOTIFICATION", "false")
            editor.putString("ALERT_NOTIFICATION_RADAR1", "false")
            editor.putString("ALERT_NOTIFICATION_SOUND1", "false")
            editor.putInt("ALERT_NOTIFICATION_INTERVAL", 12)
            editor.putString("ALERT_BLACKOUT", "false")
            editor.putInt("ALERT_BLACKOUT_AM", 7)
            editor.putInt("ALERT_BLACKOUT_PM", 22)
            editor.putString("ALERT_BLACKOUT_TORNADO", "true")
            editor.putInt("LOC_NUM_INT", 1)
            editor.putString("LOC1_X", "35.231")
            editor.putString("LOC1_Y", "-97.451")
            editor.putString("LOC1_NWS", "OUN")
            editor.putString("LOC1_LABEL", "home")
            editor.putString("COUNTY1", "Cleveland")
            editor.putString("ZONE1", "OKC027") // TODO FIXME not used by still managed in Location code
            editor.putString("ALERTS1", "true") // TODO FIXME does not do anything anymore?
            editor.putString("CURRENT1", "true") // TODO FIXME does not do anything anymore?
            editor.putString("STATE", "Oklahoma")
            editor.putString("STATE_CODE", "OK")
            editor.putString("NWS1", "OUN")
            editor.putString("RID1", "TLX")
            editor.putString("NWS1_STATE", "OK")
            editor.putString("THEME_BLUE", "BlackAqua")
            editor.putString("NWS_RADAR_BG_BLACK", "true")
            // NCEP default to GFS
            editor.putInt("MODEL_NCEP1_INDEX", 4)
            editor.putInt("MODEL_NCEP2_INDEX", 4)
            editor.apply()
        }
    }
}
