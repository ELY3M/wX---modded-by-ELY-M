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
import joshuatee.wx.notifications.UtilityNotificationSpc
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.*

internal object UtilityDownloadMcd {

    const val type = "MCD"
    val typeEnum = PolygonType.MCD
    val timer = DownloadTimer(type)

    fun get(context: Context) { if (timer.isRefreshNeeded(context)) getMcd(context) }

    fun getMcd(context: Context): WatchData {
        val html = "${MyApplication.nwsSPCwebsitePrefix}/products/md/".getHtml()
        if (html != "" ) MyApplication.severeDashboardMcd.valueSet(context, html)
        val numberList = getListOfNumbers(context)
        val htmlList = mutableListOf<String>()
        var latLonString = ""
        numberList.forEach {
            val mcdData = getLatLon(context, it)
            htmlList.add(mcdData[0])
            latLonString += mcdData[1]
        }
        if (PolygonType.MCD.pref || UtilityNotificationSpc.locationNeedsMcd()) MyApplication.mcdLatLon.valueSet(context, latLonString)
        return WatchData(numberList, htmlList)
    }

    private fun getListOfNumbers(context: Context): List<String> {
        val list = UtilityString.parseColumn(MyApplication.severeDashboardMcd.value, RegExp.mcdPatternAlertr)
        var mcdNoList = ""
        list.forEach { mcdNoList += "$it:" }
        if (PolygonType.MCD.pref || UtilityNotificationSpc.locationNeedsMcd()) MyApplication.mcdNoList.valueSet(context, mcdNoList)
        return list
    }

    // return the raw MCD text and the lat/lon as a list
    fun getLatLon(context: Context, number: String): List<String> {
        val html = UtilityDownload.getTextProduct(context, "SPCMCD$number")
        return listOf(html, UtilityNotification.storeWatchMcdLatLon(html))
    }
}
