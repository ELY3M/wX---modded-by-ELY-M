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

package joshuatee.wx.util

import joshuatee.wx.settings.Location
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.LatLon
import joshuatee.wx.parseColumn

class SevenDay {

    // UtilityWidget.kt writes pref "7DAY_ICONS_WIDGET", used by WeatherWidgetService.kt
    // separated by "!"
    var iconsAsString = ""
        private set

    // used in NotificationLocal.kt, UtilityWidget.kt, and LocationFragment.kt (which writes pref "FCST" used in UtilityVoiceCommand.kt)
    // used in ObjectWidgetCC.kt, ObjectWidgetCCLegacy.kt, and WeatherDataProvider.kt via pref "7DAY_EXT_WIDGET"
    var sevenDayLong = ""
        private set

    // used in ObjectWidgetCCLegacy.kt via pref "7DAY_WIDGET"
    // used in NotificationLocal.kt
    var sevenDayShort = ""
        private set
    var icons = mutableListOf<String>()
        private set
    private var detailedForecasts = mutableListOf<String>()

    // used in LocationFragment.kt, ForecastActivity.kt
    val forecastList
        get() = detailedForecasts

    constructor()

    constructor(locationNumber: Int) {
        if (Location.isUS(locationNumber)) {
            val html = UtilityDownloadNws.get7DayData(Location.getLatLon(locationNumber))
            iconsAsString = getIcons7Day(html)
            sevenDayLong = get7DayExtended(html)
            sevenDayShort = get7DayShort(html)
        }
    }

    constructor(latLon: LatLon) {
        val html = UtilityDownloadNws.get7DayData(latLon)
        iconsAsString = getIcons7Day(html)
        sevenDayLong = get7DayExtended(html)
        sevenDayShort = get7DayShort(html)
    }

    private fun getIcons7Day(html: String): String = if (UIPreferences.useNwsApi) {
        val icons = html.parseColumn("\"icon\": \"(.*?)\",")
        icons.joinToString("!")
    } else {
        val sevenDayDataLegacy = UtilityUS.getSevenDayLegacy(html)
        val iconString = sevenDayDataLegacy.iconString
        val icons = UtilityString.parseColumn(iconString, "<icon-link>(.*?)</icon-link>")
        icons.joinToString("!")
    }

    private fun get7DayShort(html: String): String = if (UIPreferences.useNwsApi) {
        val names = html.parseColumn("\"name\": \"(.*?)\",")
        val temperatures = html.parseColumn("\"temperature\": (.*?),")
        val shortForecasts = html.parseColumn("\"shortForecast\": \"(.*?)\",")
        val detailedForecasts = html.parseColumn("\"detailedForecast\": \"(.*?)\"")
        if ((names.size == temperatures.size) && (temperatures.size == shortForecasts.size) && (shortForecasts.size == detailedForecasts.size)) {
            val objectForecasts = names.indices.map { Forecast(names[it], temperatures[it], shortForecasts[it], detailedForecasts[it]) }
            var forecasts = GlobalVariables.newline + GlobalVariables.newline
            objectForecasts.forEach {
                forecasts += it.name + "(" + it.temperature + "): " + it.shortForecast
                forecasts += GlobalVariables.newline + GlobalVariables.newline
            }
            forecasts
        } else {
            ""
        }
    } else {
        val sevenDayDataLegacy = UtilityUS.getSevenDayLegacy(html)
        val forecastString = sevenDayDataLegacy.sevenDayShort
        val forecasts = forecastString.split("\n").dropLastWhile { it.isEmpty() }
        var forecast = GlobalVariables.newline + GlobalVariables.newline
        forecasts.forEach { s ->
            if (s != "") {
                forecast += s.trim() + GlobalVariables.newline
            }
        }
        forecast
    }

    private fun get7DayExtended(html: String): String = if (UIPreferences.useNwsApi) {
        val names = html.parseColumn("\"name\": \"(.*?)\",")
        val temperatures = html.parseColumn("\"temperature\": (.*?),")
        this.icons = html.parseColumn("\"icon\": \"(.*?)\",").toMutableList()
        val shortForecasts = html.parseColumn("\"shortForecast\": \"(.*?)\",")
        val detailedForecastsLocal = html.parseColumn("\"detailedForecast\": \"(.*?)\"")
        var forecast = GlobalVariables.newline + GlobalVariables.newline
        if (names.size == temperatures.size && temperatures.size == shortForecasts.size && shortForecasts.size == detailedForecastsLocal.size) {
            val forecasts = names.indices.map { Forecast(names[it], temperatures[it], shortForecasts[it], detailedForecastsLocal[it]) }
            forecasts.forEach {
                forecast += it.name + ": " + it.detailedForecast
                forecast += GlobalVariables.newline + GlobalVariables.newline
                detailedForecasts.add(it.name + ": " + it.detailedForecast)
            }
        }
        forecast
    } else {
        icons.clear()
        detailedForecasts.clear()
        val sevenDayDataLegacy = UtilityUS.getSevenDayLegacy(html)
        val forecastString = sevenDayDataLegacy.sevenDayExtended
        val iconString = sevenDayDataLegacy.iconString
        val forecasts = forecastString.split("\n").dropLastWhile { it.isEmpty() }
        val iconList = UtilityString.parseColumn(iconString, "<icon-link>(.*?)</icon-link>")
        var forecast = GlobalVariables.newline + GlobalVariables.newline
        forecasts.forEachIndexed { index, s ->
            if (s != "") {
                detailedForecasts.add(s.trim())
                forecast += s.trim()
                forecast += GlobalVariables.newline + GlobalVariables.newline
                if (iconList.size > index) {
                    icons.add(iconList[index])
                }
            }
        }
        forecast
    }
}

/*
 {
 "number": 14,
 "name": "Tuesday Night",
 "startTime": "2016-12-27T18:00:00-05:00",
 "endTime": "2016-12-28T06:00:00-05:00",
 "isDaytime": false,
 "temperature": 50,
 "windSpeed": "7 mph",
 "windDirection": "NW",
 "icon": "https://api-v1.weather.gov/icons/land/night/rain_showers,30?size=medium",
 "shortForecast": "Chance Rain Showers",
 "detailedForecast": "A chance of rain showers. Mostly cloudy, with a low around 50. Chance of precipitation is 30%."
 }

 --------
 sevenDayShort
 ------

  Tuesday Night(49): Slight Chance Rain then Chance T-storms

    Wednesday(75): Chance T-storms

    Wednesday Night(56): Partly Cloudy

-----------
sevenDayLong
-----------

Tuesday Night: A slight chance of rain before 8pm, then a chance of thunderstorms and a chance of rain. Mostly cloudy, with a low around 49.

    Wednesday: A chance of thunderstorms and a chance of rain before 2pm. Partly sunny, with a high near 75.

    Wednesday Night: Partly cloudy, with a low around 56.

 */
