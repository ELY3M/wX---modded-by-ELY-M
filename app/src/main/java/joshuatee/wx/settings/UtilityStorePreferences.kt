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
import androidx.preference.PreferenceManager

object UtilityStorePreferences {

    fun setDefaults(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val value = preferences.getString("ALERT1_NOTIFICATION", null)
        if (value == null) {
            // Define the default location
            editor.putString("ALERT1_NOTIFICATION", "false")
            editor.putString("ALERT_CC1_NOTIFICATION", "false")
            editor.putString("ALERT_7DAY_1_NOTIFICATION", "false")
            editor.putString("ALERT_NOTIFICATION_RADAR1", "false")
            editor.putString("ALERT_NOTIFICATION_SOUND1", "false")
            editor.putInt("ALERT_NOTIFICATION_INTERVAL", 12)
            editor.putInt("LOC_NUM_INT", 1)
            editor.putString("LOC1_X", "35.231")
            editor.putString("LOC1_Y", "-97.451")
            editor.putString("LOC1_LABEL", "home")
            editor.putString("NWS1", "OUN")
            editor.putString("RID1", "TLX")
            // NCEP default to GFS
            editor.putInt("MODEL_NCEP1_INDEX", 4)
            editor.putInt("MODEL_NCEP2_INDEX", 4)
            editor.apply()
        }
    }
}
