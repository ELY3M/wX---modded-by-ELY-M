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
//Modded by ELY M.

package joshuatee.wx.objects

import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.UtilityLog

//
// Used as an object to determine when data should be downloaded again
// particularly in nexrad radar
//

class DownloadTimer(private val identifier: String, private var refreshDataInMinutes: Int = 3) {

    private var initialized = false
    private var lastRefresh = 0.toLong()

    fun isRefreshNeeded(): Boolean {
        //var refreshDataInMinutes: Int = maxOf(Utility.readPrefInt(context, "RADAR_REFRESH_INTERVAL", 3), 6)
        if (identifier.contains("WARNINGS")) {
            refreshDataInMinutes = 3
        }
        if (identifier == "NOTIFICATIONS_MAIN") {
            refreshDataInMinutes = 1
        }
        if (identifier == "HOMESCREEN") {
            refreshDataInMinutes = UIPreferences.refreshLocMin
        }
        //elys mod --- 5 mins for now for SN Auto Report
        if (identifier == "SpotterNetworkPositionReport") {
            refreshDataInMinutes = 5
        }
        var refreshNeeded = false
        val currentTime = ObjectDateTime.currentTimeMillis()
        val currentTimeSeconds = currentTime / 1000
        val refreshIntervalSeconds = refreshDataInMinutes * 60
        if ((currentTimeSeconds > (lastRefresh + refreshIntervalSeconds)) || !initialized) {
            refreshNeeded = true
            initialized = true
            lastRefresh = currentTime / 1000
        }
        UtilityLog.d("WXRADAR", "TIMER: $identifier $refreshNeeded min: $refreshDataInMinutes")
        return refreshNeeded
    }

    fun resetTimer() {
        lastRefresh = 0
    }
}
