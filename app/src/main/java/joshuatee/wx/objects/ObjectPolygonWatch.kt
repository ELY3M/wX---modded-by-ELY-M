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

package joshuatee.wx.objects

import android.content.Context
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityNotificationSpc
import joshuatee.wx.notifications.UtilityNotificationWpc
import joshuatee.wx.radar.WatchData
import joshuatee.wx.util.*

class ObjectPolygonWatch(val context: Context, val type: PolygonType) {

    val storage: DataStorage
    val latLonList: DataStorage
    val numberList: DataStorage
    var isEnabled: Boolean
    val timer: DownloadTimer
//    var colorInt: Int

    init {
        isEnabled = Utility.readPref(context, prefTokenEnabled(), "false").startsWith("t")
        storage = DataStorage(prefTokenStorage())
        storage.update(context)
        latLonList = DataStorage(prefTokenLatLon())
        latLonList.update(context)
        numberList = DataStorage(prefTokenNumberList())
        numberList.update(context)
        timer = DownloadTimer("WATCH_" + getTypeName())
//        colorInt = Utility.readPrefInt(colorPrefByType[type]!!, colorDefaultByType[type]!!)
    }

    fun get(context: Context) {
        if (timer.isRefreshNeeded(context)) {
            getImmediate(context)
        }
    }

    fun getImmediate(context: Context): WatchData {
//        val html = "${GlobalVariables.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
        val html = getUrl().getHtml()
        if (html != "") {
            storage.valueSet(context, html)
        }
        val numberList = if (type == PolygonType.WATCH) {
            getListOfNumbersWatch(context)
        } else {
            getListOfNumbers(context)
        }
        val htmlList = mutableListOf<String>()
        if (type == PolygonType.WATCH) {
            var watchLatLonList = ""
            var watchLatLon = ""
            var watchLatLonTor = ""
            numberList.forEach {
                val watchHtml = UtilityDownload.getTextProduct(context, "SPCWAT$it")
                htmlList.add(watchHtml)
                val latLonHtml = getLatLonWatch(it)
                watchLatLonList += UtilityNotification.storeWatchMcdLatLon(latLonHtml)
                if (!watchHtml.contains("Tornado Watch")) {
                    watchLatLon += UtilityNotification.storeWatchMcdLatLon(latLonHtml)
                } else {
                    watchLatLonTor += UtilityNotification.storeWatchMcdLatLon(latLonHtml)
                }
            }
            if (PolygonType.MCD.pref) {
                watchLatlonCombined.valueSet(context, watchLatLonList)
//                polygonDataByType[UtilityDownloadWatch.type]!!.latLonList.valueSet(context, watchLatLon)
                UtilityLog.d("wxjosh", watchLatLon)
                polygonDataByType[PolygonType.WATCH]!!.latLonList.valueSet(context, watchLatLon)
                polygonDataByType[PolygonType.WATCH_TORNADO]!!.latLonList.valueSet(context, watchLatLonTor)
            }
        } else {
            var latLonString = ""
            numberList.forEach {
                val data = getLatLon(context, it)
                htmlList.add(data[0])
                latLonString += data[1]
            }
            if (type.pref || locationNotification()) {
                latLonList.valueSet(context, latLonString)
            }
        }
        return WatchData(numberList, htmlList)
    }

    private fun getListOfNumbers(context: Context): List<String> {
        val listOfNumbers = UtilityString.parseColumn(
                storage.value,
                regex[type]!!)
        var numbersAsString = ""
        listOfNumbers.forEach {
            numbersAsString += "$it:"
        }
        if (type.pref || locationNotification()) {
            numberList.valueSet(context, numbersAsString)
        }
        return listOfNumbers
    }

    private fun getListOfNumbersWatch(context: Context): List<String> {
        val listOriginal = UtilityString.parseColumn(
                polygonDataByType[type]!!.storage.value,
                regex[type]!!)
        val listOfNumbers = listOriginal.map { String.format("%4s", it).replace(' ', '0') }
        var numbersAsString = ""
        listOfNumbers.forEach {
            numbersAsString += "$it:"
        }
        if (PolygonType.MCD.pref) {
            numberList.valueSet(context, numbersAsString)
        }
        return listOfNumbers
    }

    // return the raw MPD text and the lat/lon as a list
    private fun getLatLon(context: Context, number: String): List<String> {
        val html = UtilityDownload.getTextProduct(context, textPrefix[type] + number)
        return listOf(html, UtilityNotification.storeWatchMcdLatLon(html))
    }

    private fun locationNotification(): Boolean {
        return when(type) {
            PolygonType.MCD -> UtilityNotificationSpc.locationNeedsMcd()
            PolygonType.MPD -> UtilityNotificationWpc.locationNeedsMpd()
            else -> false
        }
    }

//    private fun download(ignoreTimer: Boolean = false) {
//        if (ignoreTimer || timer.isRefreshNeeded(context)) {
//            val html = UtilityIO.getHtml(getUrl())
//            storage.valueSet(context, html)
//            when (type) {
//                PolygonType.MPD -> {
//
//                }
//                PolygonType.MCD -> {
//
//                }
//                PolygonType.WATCH -> {
//
//                }
//                else -> {}
//            }
//        }
//    }

    private fun prefTokenEnabled() = "RADAR_SHOW_" + getTypeName().uppercase()

    private fun prefTokenLatLon() = getTypeName() + "LATLON"

    private fun prefTokenNumberList() = getTypeName() + "NOLIST"

    private fun prefTokenStorage() = "SEVEREDASHBOARD_" + getTypeName()

    private fun getTypeName() = type.toString().replace("PolygonType.", "")

    fun getUrl() = when (type) {
        PolygonType.MCD -> GlobalVariables.nwsSPCwebsitePrefix + "/products/md/"
        PolygonType.WATCH -> GlobalVariables.nwsSPCwebsitePrefix + "/products/watch/"
        PolygonType.WATCH_TORNADO -> GlobalVariables.nwsSPCwebsitePrefix + "/products/watch/"
        PolygonType.MPD -> GlobalVariables.nwsWPCwebsitePrefix + "/metwatch/metwatch_mpd.php"
        else -> ""
    }

    private fun update() {
        isEnabled = Utility.readPref(context, prefTokenEnabled(), "false").startsWith("t")
//        colorInt = Utility.readPrefInt(colorPrefByType[type]!!, colorDefaultByType[type]!!)
    }

    companion object {

        private val polygonList = listOf(
                PolygonType.WATCH,
                PolygonType.WATCH_TORNADO,
                PolygonType.MCD,
                PolygonType.MPD
        )
        val polygonDataByType = mutableMapOf<PolygonType, ObjectPolygonWatch>()

        var watchLatlonCombined = DataStorage("WATCH_LATLON_COMBINED")

//        val colorDefaultByType = mapOf(
//                PolygonType.Mcd to Color.rgb(153, 51, 255),
//                PolygonType.Mpd to Color.rgb(0, 255, 0),
//                PolygonType.Watch to Color.rgb(255, 187, 0),
//                PolygonType.WatchTornado to Color.rgb(255, 0, 0),
//        )
//        val colorPrefByType = mapOf(
//                PolygonType.Mcd to "RADAR_COLOR_MCD",
//                PolygonType.Mpd to "RADAR_COLOR_MPD",
//                PolygonType.Watch to "RADAR_COLOR_TSTORM_WATCH",
//                PolygonType.WatchTornado to "RADAR_COLOR_TOR_WATCH",
//        )

        val textPrefix = mapOf(
            PolygonType.MCD to "SPCMCD",
            PolygonType.MPD to "WPCMPD",
//            PolygonType.WATCH to "RADAR_COLOR_TSTORM_WATCH",
//            PolygonType.WatchTornado to "RADAR_COLOR_TOR_WATCH",
        )

        val regex = mapOf(
                PolygonType.MCD to RegExp.mcdPatternAlerts,
                PolygonType.MPD to RegExp.mpdPattern,
                PolygonType.WATCH to RegExp.watchPattern,
//            PolygonType.WatchTornado to "RADAR_COLOR_TOR_WATCH",
        )

        fun load(context: Context) {
            polygonList.forEach {
                if (!polygonDataByType.containsKey(it)) {
                    polygonDataByType[it] = ObjectPolygonWatch(context, it)
                } else {
                    polygonDataByType[it]!!.update()
                }
            }
            polygonDataByType[PolygonType.WATCH_TORNADO]!!.isEnabled = polygonDataByType[PolygonType.WATCH]!!.isEnabled
        }

        fun getLatLonWatch(number: String) = UtilityString.getHtmlAndParseLastMatch("${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/wou$number.html", RegExp.pre2Pattern)

//        fun getLatLon(number: String): String {
//            val html = UtilityIO.getHtml(GlobalVariables.nwsSPCwebsitePrefix + "/products/watch/wou" + number + ".html")
//            return UtilityString.parseLastMatch(html, GlobalVariables.pre2Pattern)
//        }
    }
}
