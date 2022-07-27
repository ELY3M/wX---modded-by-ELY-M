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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityNotificationWpc
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.ObjectPolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityString

internal object UtilityDownloadMpd {

    val type = PolygonType.MPD
    val timer = DownloadTimer("MPD")

    fun get(context: Context) {
        if (timer.isRefreshNeeded(context)) {
            getMpd(context)
        }
    }

    fun getMpd(context: Context): WatchData {
//        val html = "${GlobalVariables.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
        val html = ObjectPolygonWatch.polygonDataByType[type]!!.getUrl().getHtml()
        if (html != "") {
            ObjectPolygonWatch.polygonDataByType[type]!!.storage.valueSet(context, html)
        }
        val numberList = getListOfNumbers(context)
        val htmlList = mutableListOf<String>()
        var latLonString = ""
        numberList.forEach {
            val data = getLatLon(context, it)
            htmlList.add(data[0])
            latLonString += data[1]
        }
        if (type.pref || UtilityNotificationWpc.locationNeedsMpd()) {
            ObjectPolygonWatch.polygonDataByType[type]!!.latLonList.valueSet(context, latLonString)
        }
        return WatchData(numberList, htmlList)
    }

    private fun getListOfNumbers(context: Context): List<String> {
        val list = UtilityString.parseColumn(
                ObjectPolygonWatch.polygonDataByType[type]!!.storage.value,
                ObjectPolygonWatch.regex[type]!!)
        var mpdNoList = ""
        list.forEach {
            mpdNoList += "$it:"
        }
        if (type.pref || UtilityNotificationWpc.locationNeedsMpd()) {
            ObjectPolygonWatch.polygonDataByType[type]!!.numberList.valueSet(context, mpdNoList)
        }
        return list
    }

    // return the raw MPD text and the lat/lon as a list
    private fun getLatLon(context: Context, number: String): List<String> {
        val html = UtilityDownload.getTextProduct(context, ObjectPolygonWatch.textPrefix[type] + number)
        return listOf(html, UtilityNotification.storeWatchMcdLatLon(html))
    }
}
