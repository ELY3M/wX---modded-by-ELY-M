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
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityString

internal object UtilityDownloadWatch {

    const val type = "WATCH"
    var timer = DownloadTimer(type)

    fun get(context: Context) {
        if (timer.isRefreshNeeded(context)) {
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
        var watchLatLonList = ""
        var watchLatLon = ""
        var watchLatLonTor = ""
        numberList.forEach {
            val watchHtml = UtilityDownload.getTextProduct(context, "SPCWAT$it")
            htmlList.add(watchHtml)
            val latLonHtml = getLatLon(it)
            watchLatLonList += UtilityNotification.storeWatMcdLatLon(latLonHtml)
            if (!watchHtml.contains("Tornado Watch")) {
                watchLatLon += UtilityNotification.storeWatMcdLatLon(latLonHtml)
            } else {
                watchLatLonTor += UtilityNotification.storeWatMcdLatLon(latLonHtml)
            }
        }
        if (PolygonType.MCD.pref) {
            MyApplication.watchLatLonList.valueSet(context, watchLatLonList)
            MyApplication.watchLatLon.valueSet(context, watchLatLon)
            MyApplication.watchLatLonTor.valueSet(context, watchLatLonTor)
        }
        return WatchData(numberList, htmlList)
    }

    private fun getListOfNumbers(context: Context): List<String> {
        val listOriginal = UtilityString.parseColumn(MyApplication.severeDashboardWat.value, RegExp.watchPattern)
        val list = listOriginal.map { String.format("%4s", it).replace(' ', '0') }
        var watchNoList = ""
        list.forEach {
            watchNoList = "$watchNoList$it:"
        }
        if (PolygonType.MCD.pref) {
            MyApplication.watchNoList.valueSet(context, watchNoList)
        }
        return list
    }

    fun getLatLon(number: String): String {
        return UtilityString.getHtmlAndParseLastMatch("${MyApplication.nwsSPCwebsitePrefix}/products/watch/wou$number.html", RegExp.pre2Pattern)
    }
}
