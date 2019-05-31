/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityLog

internal object UtilityDownloadWarnings {

    private var initialized = false
    private var lastRefresh = 0.toLong()

    const val type = "WARNINGS"

    fun get(context: Context) {
        val refreshInterval = Utility.readPref(context, "RADAR_REFRESH_INTERVAL", 3)
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (refreshInterval * 60).toLong()

        UtilityLog.d("wx", "RADAR DOWNLOAD CHECK: $type")

        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
            // download data
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
            if (PolygonType.TST.pref) {
                UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED: $type")
                getPolygonVtec(context)
            } else {
                //UtilityDownloadRadar.clearPolygonVtec()
                UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED BUT PREF IS OFF - NO DOWNLOAD: $type")
            }
        }
    }

    // The only difference from the get method above is the absence of any preference check
    // ie - if you call this you are going to download regardless
    fun getForNotification(context: Context) {
        val refreshInterval = Utility.readPref(context, "RADAR_REFRESH_INTERVAL", 3)
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (refreshInterval * 60).toLong()
        UtilityLog.d("wx", "RADAR DOWNLOAD CHECK via NOTIFICATION: $type")
        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
            // download data
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
            UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED via NOTIFICATION: $type")
            getPolygonVtec(context)
        }
    }

    private const val baseUrl = "https://api.weather.gov/alerts/active?event="
    private const val tstormURl = baseUrl + "Severe%20Thunderstorm%20Warning"
    private const val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    // Below is for testing
    //val ffwUrl = baseUrl + "Flood%20Warning"
    private const val tornadoUrl = baseUrl + "Tornado%20Warning"

    private fun getPolygonVtec(context: Context) {
        val tstData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tstormURl)
        if (tstData != "") {
            MyApplication.severeDashboardTst.valueSet(context, tstData)
        }
        val ffwData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(ffwUrl)
        if (ffwData != "") {
            MyApplication.severeDashboardFfw.valueSet(context, ffwData)
        }
        val torData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tornadoUrl)
        if (torData != "") {
            MyApplication.severeDashboardTor.valueSet(context, torData)
        }
    }

    fun getVtecByType(type: PolygonWarningType): String {
        return UtilityDownloadNws.getStringFromUrlNoAcceptHeader(baseUrl + type.urlToken)
    }
}
