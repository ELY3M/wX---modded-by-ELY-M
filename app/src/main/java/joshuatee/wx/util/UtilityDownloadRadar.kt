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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonWarningType

object UtilityDownloadRadar {

    private const val baseUrl = "https://api.weather.gov/alerts/active?event="
    private const val tstormURl = baseUrl + "Severe%20Thunderstorm%20Warning"
    private const val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    // Below is for testing
    //val ffwUrl = baseUrl + "Flood%20Warning"
    private const val tornadoUrl = baseUrl + "Tornado%20Warning"

    fun getPolygonVtec(context: Context) {
        MyApplication.severeDashboardTst.valueSet(
                context,
                UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tstormURl)
        )
        MyApplication.severeDashboardFfw.valueSet(
                context,
                UtilityDownloadNws.getStringFromUrlNoAcceptHeader(ffwUrl)
        )
        MyApplication.severeDashboardTor.valueSet(
                context,
                UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tornadoUrl)
        )
    }

    fun getVtecTstorm(): String {
        return UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tstormURl)
    }

    fun getVtecTor(): String {
        return UtilityDownloadNws.getStringFromUrlNoAcceptHeader(tornadoUrl)
    }

    fun getVtecFfw(): String {
        return UtilityDownloadNws.getStringFromUrlNoAcceptHeader(ffwUrl)
    }

    fun getVtecByType(type: PolygonWarningType): String {
        return UtilityDownloadNws.getStringFromUrlNoAcceptHeader(baseUrl + type.urlToken)
    }

    fun getMcd(): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/products/md/".getHtml()
    }

    fun getMpd(): String {
        return "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
    }

    fun getWatch(): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/products/watch/".getHtml()
    }
}
