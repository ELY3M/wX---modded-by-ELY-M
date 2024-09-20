/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.graphics.Color
import joshuatee.wx.radar.Warnings
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadNws

class PolygonWarning(val context: Context, val type: PolygonWarningType) {

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
        if (timer.isRefreshNeeded()) {
            val html = UtilityDownloadNws.getStringFromUrlBaseNoHeader1(getUrl())
            if (html != "") {
                storage.valueSet(context, html)
            }
        }
    }

    fun getData(): String = storage.value

    private fun getUrlToken(): String = longName[type] ?: ""

    private fun getUrl(): String = BASE_URL + getUrlToken()

    private fun getTypeName(): String = type.toString().replace("PolygonType.", "")

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

        var byType = mutableMapOf<PolygonWarningType, PolygonWarning>()

        // NWS default colors: https://www.weather.gov/help-map
        private val defaultColors = mapOf(
                PolygonWarningType.TornadoWarning to Color.rgb(243, 85, 243),
                PolygonWarningType.ThunderstormWarning to Color.rgb(255, 255, 0),
                PolygonWarningType.FlashFloodWarning to Color.rgb(0, 255, 0),
                PolygonWarningType.SpecialMarineWarning to Color.rgb(255, 165, 0),
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

        const val BASE_URL = "https://api.weather.gov/alerts/active?event="

        fun areAnyEnabled(): Boolean {
            var anyEnabled = false
            polygonList.forEach {
                if (byType[it]!!.isEnabled) {
                    anyEnabled = true
                }
            }
            return anyEnabled
        }

        fun load(context: Context) {
            polygonList.forEach {
                if (!byType.containsKey(it)) {
                    byType[it] = PolygonWarning(context, it)
                } else {
                    byType[it]!!.update()
                }
            }
        }

        fun isCountNonZero(): Boolean {
            var count = 0
            polygonList.forEach {
                if (byType[it]!!.isEnabled) {
                    count += Warnings.getCount(it)
                }
            }
            return count > 0
        }
    }
}
