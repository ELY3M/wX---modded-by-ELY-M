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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.util.UtilityDownloadNws

internal object UtilityDownloadWarnings {

    const val type = "WARNINGS"
    val timer = DownloadTimer(type)
    val timerSevereDashboard = DownloadTimer("WARNINGS_SEVERE_DASHBOARD")

    private const val baseUrl = "https://api.weather.gov/alerts/active?event="
    private const val tStormUrl = baseUrl + "Severe%20Thunderstorm%20Warning"
    private const val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    private const val tornadoUrl = baseUrl + "Tornado%20Warning"
    //val ffwUrl = baseUrl + "Flood%20Warning"

    fun get(context: Context) {
        if (timer.isRefreshNeeded(context)) {
            if (PolygonType.TST.pref) getPolygonVtec(context)
            MyApplication.radarWarningPolygons.forEach {
                if (it.isEnabled) it.storage.valueSet(context, getVtecByType(it.type)) else it.storage.valueSet(context, "")
            }
        }
    }

    fun getForSevereDashboard(context: Context) { if (timerSevereDashboard.isRefreshNeeded(context)) getPolygonVtec(context) }

    // The only difference from the get method above is the absence of any preference check
    // ie - if you call this you are going to download regardless
    fun getForNotification(context: Context) { if (timer.isRefreshNeeded(context)) getPolygonVtec(context) }

    private fun getPolygonVtec(context: Context) {
        // FIXME improve structure
        val tstData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tStormUrl)
        if (tstData != "") MyApplication.severeDashboardTst.valueSet(context, tstData)
        val ffwData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(ffwUrl)
        if (ffwData != "") MyApplication.severeDashboardFfw.valueSet(context, ffwData)
        val torData = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tornadoUrl)
        if (torData != "") MyApplication.severeDashboardTor.valueSet(context, torData)
    }

    fun getVtecByType(type: PolygonWarningType) = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(baseUrl + type.urlToken)
}
