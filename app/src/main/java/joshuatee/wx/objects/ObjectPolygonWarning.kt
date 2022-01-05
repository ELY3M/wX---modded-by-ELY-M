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

package joshuatee.wx.objects

import android.content.Context
import joshuatee.wx.DataStorage
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityVtec

class ObjectPolygonWarning(val context: Context, val type: PolygonWarningType) {

    var color = 0
    val storage = DataStorage(prefTokenStorage)
    var isEnabled = false
    val timer: DownloadTimer

    init {
        storage.update(context)
        color = Utility.readPref(context, prefTokenColor, type.initialColor)
        isEnabled = Utility.readPref(context, prefTokenEnabled, "false").startsWith("t")
        timer = DownloadTimer("WARNINGS_" + getTypeName())
    }

    // Not currently used
    fun download() {
        if (timer.isRefreshNeeded(context)) {
            val html = UtilityIO.getHtml(getUrl())
            if (html != "") {
                storage.valueSet(context, html)
            }
        }
    }

    // Not currently used
//    fun getData(): String {
//        return storage.value
//    }

    fun getUrlToken(): String {
        return longName[type] ?: ""
    }

    private fun getUrl(): String {
        return baseUrl + getUrlToken()
    }

    private fun getTypeName(): String {
        return type.toString().replace("PolygonType.", "")
    }

    val name get() = type.urlToken.replace("%20", " ")

    val prefTokenEnabled get() = "RADAR_SHOW_" + type.productCode

    val prefTokenColor get() = "RADAR_COLOR_" + type.productCode

    private val prefTokenStorage get() = "SEVERE_DASHBOARD_" + type.productCode

    companion object {
        private var polygonDataByType = mutableMapOf<PolygonWarningType, ObjectPolygonWarning>()

        private val polygonList = listOf(
            PolygonWarningType.SpecialMarineWarning,
            PolygonWarningType.SnowSquallWarning,
            PolygonWarningType.DustStormWarning,
            PolygonWarningType.SpecialWeatherStatement
        )

        val longName = mapOf(
            PolygonWarningType.SpecialMarineWarning to "Special%20Marine%20Warning",
            PolygonWarningType.SnowSquallWarning to "Snow%20Squall%20Warning",
            PolygonWarningType.DustStormWarning to "Dust%20Storm%20Warning",
            PolygonWarningType.SpecialWeatherStatement to "Special%20Weather%20Statement",
//            PolygonType.tor: "Tornado%20Warning",
//            PolygonType.tst: "Severe%20Thunderstorm%20Warning",
//            PolygonType.ffw: "Flash%20Flood%20Warning",
        )

        const val baseUrl: String = "https://api.weather.gov/alerts/active?event="

        fun areAnyEnabled(): Boolean {
            var anyEnabled = false
            polygonList.forEach { if (polygonDataByType[it]!!.isEnabled) anyEnabled = true }
            return anyEnabled
        }

        fun load(context: Context) {
            polygonList.forEach { polygonDataByType[it] = ObjectPolygonWarning(context, it) }
        }

        fun isCountNonZero(): Boolean {
            val tstCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTst.value)
            val torCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTor.value)
            val ffwCount = UtilityVtec.getStormCount(MyApplication.severeDashboardFfw.value)
            var count = tstCount + torCount + ffwCount
            polygonList.forEach {
                if (polygonDataByType[it]!!.isEnabled) {
                    count += UtilityVtec.getStormCountGeneric(polygonDataByType[it]!!.storage.value)
                }
            }
            return count > 0
        }
    }
}

