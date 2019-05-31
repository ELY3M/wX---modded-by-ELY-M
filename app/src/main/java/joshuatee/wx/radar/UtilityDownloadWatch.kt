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
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

internal object UtilityDownloadWatch {

    private var initialized = false
    private var lastRefresh = 0.toLong()
    const val type = "WATCH"

    fun get(context: Context) {
        val refreshInterval = maxOf(Utility.readPref(context, "RADAR_REFRESH_INTERVAL", 3), 6)
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        // FIXME make this be something like maximum of refresh int or 5-7 min
        val refreshIntervalSec = (refreshInterval * 60).toLong()
        UtilityLog.d("wx", "RADAR DOWNLOAD CHECK: $type")
        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
            // download data
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
            UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED:$type")
            getWatch(context)
        }
    }

    fun getWatch(context: Context): WatchData {
        val html =  "${MyApplication.nwsSPCwebsitePrefix}/products/watch/".getHtml()
        if (html != "" ) {
            MyApplication.severeDashboardWat.valueSet(context, html)
        }

        val numberList = getListOfNumbers(context)
        val htmlList = mutableListOf<String>()

        //var watchNoList = ""
        var watchLatLonList = ""
        var watchLatlon = ""
        var watchLatlonTor = ""

        numberList.forEach {
            val watchHtml = UtilityDownload.getTextProduct(context, "SPCWAT$it")
            htmlList.add(watchHtml)
            val latLonHtml = getLatLon(it)
            watchLatLonList += UtilityNotification.storeWatMcdLatLon(latLonHtml)
            if (!watchHtml.contains("Tornado Watch")) {
                watchLatlon += UtilityNotification.storeWatMcdLatLon(latLonHtml)
            } else {
                watchLatlonTor += UtilityNotification.storeWatMcdLatLon(latLonHtml)
            }
        }
        if (PolygonType.MCD.pref) {
            UtilityLog.d("wx","RADAR DOWNLOAD SET WATCH: " + watchLatLonList)
            MyApplication.watchLatlonList.valueSet(context, watchLatLonList)
            MyApplication.watchLatlon.valueSet(context, watchLatlon)
            MyApplication.watchLatlonTor.valueSet(context, watchLatlonTor)
        }
        return WatchData(numberList, htmlList)
    }

    fun getListOfNumbers(context: Context): List<String> {
        val listOriginal = UtilityString.parseColumn(MyApplication.severeDashboardWat.value, RegExp.watchPattern)
        val list = listOriginal.map { String.format("%4s", it).replace(' ', '0') }
        UtilityLog.d("wx", "RADAR DOWNLOAD $type:$list")
        var watchNoList = ""
        list.forEach {
            watchNoList = "$watchNoList$it:"
        }
        if (PolygonType.MCD.pref) {
            MyApplication.watchNoList.valueSet(context, watchNoList)
        }
        UtilityLog.d("wx", "RADAR DOWNLOAD NO LIST $type:$watchNoList")
        return list
    }

    fun getLatLon(number: String): String {
        val html = UtilityString.getHtmlAndParseLastMatch("${MyApplication.nwsSPCwebsitePrefix}/products/watch/wou$number.html", RegExp.pre2Pattern)
        return html
    }
}
