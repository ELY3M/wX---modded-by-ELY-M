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
//modded by ELY M. 

package joshuatee.wx.objects

import android.content.Context
import android.graphics.Color
import joshuatee.wx.radar.WXGLPolygonWarnings
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadNws

class ObjectPolygonWarning(val context: Context, val type: PolygonWarningType) {

    var color = 0
    val storage = DataStorage(prefTokenStorage)
    var isEnabled = false
    val timer: DownloadTimer

    init {
        storage.update(context)
        update()
        timer = DownloadTimer("WARNINGS_" + getTypeName())
    }

    fun download() {
        if (timer.isRefreshNeeded(context)) {
            val html = UtilityDownloadNws.getStringFromUrlNoAcceptHeader(getUrl())
            if (html != "") {
                storage.valueSet(context, html)
            }
        }
    }

    fun getData(): String {
        return storage.value
    }

    fun getUrlToken() = longName[type] ?: ""

    private fun getUrl() = baseUrl + getUrlToken()

    private fun getTypeName() = type.toString().replace("PolygonType.", "")

    val name get() = longName[type]!!.replace("%20", " ")

    val prefTokenEnabled get() = "RADAR_SHOW_" + namesByEnumId[type]!!

    val prefTokenColor get() = "RADAR_COLOR_" + namesByEnumId[type]!!

    private val prefTokenStorage get() = "SEVERE_DASHBOARD_" + namesByEnumId[type]!!

    private fun update() {
        color = Utility.readPrefInt(context, prefTokenColor, defaultColors[type]!!)
        isEnabled = Utility.readPref(context, prefTokenEnabled, "false").startsWith("t")
        if (type == PolygonWarningType.TornadoWarning || type == PolygonWarningType.FlashFloodWarning || type == PolygonWarningType.ThunderstormWarning) {
            isEnabled = RadarPreferences.warnings
        }
    }

    companion object {

        var polygonDataByType = mutableMapOf<PolygonWarningType, ObjectPolygonWarning>()

        // NWS default colors: https://www.weather.gov/help-map
        private val defaultColors = mapOf(
                PolygonWarningType.TornadoWarning to Color.RED,
                PolygonWarningType.ThunderstormWarning to Color.YELLOW,
                PolygonWarningType.FlashFloodWarning to Color.GREEN,
                PolygonWarningType.SpecialMarineWarning to Color.CYAN,
                PolygonWarningType.SnowSquallWarning to Color.rgb(199, 21, 133),
                PolygonWarningType.DustStormWarning to Color.rgb(255, 228, 196),
                PolygonWarningType.SpecialWeatherStatement to Color.rgb(255, 228, 181)
        )

        val namesByEnumId = mapOf(
                PolygonWarningType.ThunderstormWarning to "TSTORM",
                PolygonWarningType.TornadoWarning to "TOR",
                PolygonWarningType.FlashFloodWarning to "FFW",
                PolygonWarningType.SnowSquallWarning to "SQW",
                PolygonWarningType.DustStormWarning to "DSW",
                PolygonWarningType.SpecialWeatherStatement to "SPS",
                PolygonWarningType.SpecialMarineWarning to "SMW"
        )

        val polygonList = listOf(
            PolygonWarningType.SpecialMarineWarning,
            PolygonWarningType.SnowSquallWarning,
            PolygonWarningType.DustStormWarning,
            PolygonWarningType.SpecialWeatherStatement,
            PolygonWarningType.TornadoWarning,
            PolygonWarningType.ThunderstormWarning,
            PolygonWarningType.FlashFloodWarning,
        )

        val longName = mapOf(
            PolygonWarningType.SpecialMarineWarning to "Special%20Marine%20Warning",
            PolygonWarningType.SnowSquallWarning to "Snow%20Squall%20Warning",
            PolygonWarningType.DustStormWarning to "Dust%20Storm%20Warning",
            PolygonWarningType.SpecialWeatherStatement to "Special%20Weather%20Statement",
            PolygonWarningType.TornadoWarning to "Tornado%20Warning",
            PolygonWarningType.ThunderstormWarning to "Severe%20Thunderstorm%20Warning",
            PolygonWarningType.FlashFloodWarning to "Flash%20Flood%20Warning",
        )

        const val baseUrl = "https://api.weather.gov/alerts/active?event="

        fun areAnyEnabled(): Boolean {
            var anyEnabled = false
            polygonList.forEach {
                if (polygonDataByType[it]!!.isEnabled) {
                    anyEnabled = true
                }
            }
            return anyEnabled
        }

        fun load(context: Context) {
            polygonList.forEach {
                if (!polygonDataByType.containsKey(it)) {
                    polygonDataByType[it] = ObjectPolygonWarning(context, it)
                } else {
                    polygonDataByType[it]!!.update()
                }
            }
        }

        fun isCountNonZero(): Boolean {
            var count = 0
            polygonList.forEach {
                if (polygonDataByType[it]!!.isEnabled) {
                    count += WXGLPolygonWarnings.getCount(it)
                }
            }
            return count > 0
        }
    }
}
