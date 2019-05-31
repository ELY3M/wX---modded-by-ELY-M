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
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityNotificationWpc
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

internal object UtilityDownloadMpd {

    private var initialized = false
    private var lastRefresh = 0.toLong()
    const val type = "MPD"

    fun get(context: Context) {
        //val refreshInterval = Utility.readPref(context, "RADAR_REFRESH_INTERVAL", 3)
        val refreshInterval = maxOf(Utility.readPref(context, "RADAR_REFRESH_INTERVAL", 3), 6)
        //val refreshInterval = 0
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (refreshInterval * 60).toLong()
        UtilityLog.d("wx", "RADAR DOWNLOAD CHECK: " + type)
        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
            // download data
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
            UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED:" + type)
            getMpd(context)
        }
    }

    fun getMpd(context: Context): WatchData {
        val html = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
        if (html != "") {
            MyApplication.severeDashboardMpd.valueSet(context, html)
        }
        //UtilityLog.d("wx", "RADAR DOWNLOAD " + html)
        val numberList = getListOfNumbers(context)
        val htmlList = mutableListOf<String>()
        var latLonString = ""
        numberList.forEach {
            val mcdData = getLatLon(context, it)
            htmlList.add(mcdData[0])
            latLonString += mcdData[1]
        }
        val locationNeedsMpd = UtilityNotificationWpc.locationNeedsMpd()
        if (PolygonType.MPD.pref || locationNeedsMpd) {
            UtilityLog.d("wx","RADAR DOWNLOAD SET MPD: " + latLonString)
            MyApplication.mpdLatlon.valueSet(context, latLonString)
        }
        return WatchData(numberList, htmlList)
    }

    fun getListOfNumbers(context: Context): List<String> {
        val list = UtilityString.parseColumn(MyApplication.severeDashboardMpd.value, RegExp.mpdPattern)
        UtilityLog.d("wx", "RADAR DOWNLOAD $type:$list")
        var mpdNoList = ""
        list.forEach {
            mpdNoList = "$mpdNoList$it:"
        }
        val locationNeedsMpd = UtilityNotificationWpc.locationNeedsMpd()
        if (PolygonType.MPD.pref || locationNeedsMpd) {
            MyApplication.mpdNoList.valueSet(context, mpdNoList)
        }
        return list
    }

    // return the raw MPD text and the lat/lon as a list
    fun getLatLon(context: Context, number: String): List<String> {
        val html = UtilityDownload.getTextProduct(context, "WPCMPD$number")
        val list = listOf(html, UtilityNotification.storeWatMcdLatLon(html))
        //UtilityLog.d("wx", "RADAR DOWNLOAD MPD OBJECT: " + list[1].toString())
        return  list
    }
}
