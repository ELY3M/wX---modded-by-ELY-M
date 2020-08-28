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
import joshuatee.wx.notifications.UtilityNotificationWpc
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityString

internal object UtilityDownloadMpd {

    const val type = "MPD"
    val typeEnum = PolygonType.MPD
    val timer = DownloadTimer(type)

    fun get(context: Context) { if (timer.isRefreshNeeded(context)) getMpd(context) }

    fun getMpd(context: Context): WatchData {
        val html = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
        if (html != "") MyApplication.severeDashboardMpd.valueSet(context, html)
        val numberList = getListOfNumbers(context)
        val htmlList = mutableListOf<String>()
        var latLonString = ""
        numberList.forEach {
            val mcdData = getLatLon(context, it)
            htmlList.add(mcdData[0])
            latLonString += mcdData[1]
        }
        if (PolygonType.MPD.pref || UtilityNotificationWpc.locationNeedsMpd()) MyApplication.mpdLatLon.valueSet(context, latLonString)
        return WatchData(numberList, htmlList)
    }

    private fun getListOfNumbers(context: Context): List<String> {
        val list = UtilityString.parseColumn(MyApplication.severeDashboardMpd.value, RegExp.mpdPattern)
        var mpdNoList = ""
        list.forEach { mpdNoList += "$it:" }
        if (PolygonType.MPD.pref || UtilityNotificationWpc.locationNeedsMpd()) MyApplication.mpdNoList.valueSet(context, mpdNoList)
        return list
    }

    // return the raw MPD text and the lat/lon as a list
    fun getLatLon(context: Context, number: String): List<String> {
        val html = UtilityDownload.getTextProduct(context, "WPCMPD$number")
        return listOf(html, UtilityNotification.storeWatchMcdLatLon(html))
    }
}
