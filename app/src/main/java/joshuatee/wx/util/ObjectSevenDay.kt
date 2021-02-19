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

package joshuatee.wx.util

import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.LatLon

class ObjectSevenDay {

    // separated by "!"
    var iconsAsString = ""
        private set
    var sevenDayLong = ""
        private set
    var sevenDayShort = ""
        private set
    var icons = mutableListOf<String>()
        private set
    private var detailedForecasts = mutableListOf<String>()

    constructor()

    constructor(locationNumber: Int) {
        if (Location.isUS(locationNumber)) {
            val html = UtilityDownloadNws.get7DayData(Location.getLatLon(locationNumber))
            iconsAsString = getIcons7Day(html)
            sevenDayLong = get7DayExtended(html)
            sevenDayShort = get7DayShort(html)
        } else {
            val html = UtilityCanada.getLocationHtml(Location.getLatLon(locationNumber))
            sevenDayLong = UtilityCanada.get7Day(html)
            iconsAsString = UtilityCanada.getIcons7Day(sevenDayLong)
            icons = UtilityCanada.getIcons7DayAsList(sevenDayLong).toMutableList()
            convertExt7DayToList()
        }
    }

    constructor(latLon: LatLon) {
        val html = UtilityDownloadNws.get7DayData(latLon)
        iconsAsString = getIcons7Day(html)
        sevenDayLong = get7DayExtended(html)
        sevenDayShort = get7DayShort(html)
    }

    val forecastList
        get() = detailedForecasts

    private fun convertExt7DayToList() {
        detailedForecasts = sevenDayLong.split(MyApplication.newline + MyApplication.newline).dropLastWhile { it.isEmpty() }.toMutableList()
    }

    private fun getIcons7Day(html: String): String {
        if (UIPreferences.useNwsApi) {
            val icons = html.parseColumn("\"icon\": \"(.*?)\",")
            var iconList = ""
            icons.forEach { iconList += "$it!" }
            return iconList
        } else {
            return ""
        }
    }

    private fun get7DayShort(html: String): String {
        if (UIPreferences.useNwsApi) {
            val names = html.parseColumn("\"name\": \"(.*?)\",")
            val temperatures = html.parseColumn("\"temperature\": (.*?),")
            val shortForecasts = html.parseColumn("\"shortForecast\": \"(.*?)\",")
            val detailedForecasts = html.parseColumn("\"detailedForecast\": \"(.*?)\"")
            return if ((names.size == temperatures.size) && (temperatures.size == shortForecasts.size) && (shortForecasts.size == detailedForecasts.size)) {
                val objectForecasts = (names.indices).map { ObjectForecast(names[it], temperatures[it], shortForecasts[it], detailedForecasts[it]) }
                var forecasts = MyApplication.newline + MyApplication.newline
                objectForecasts.forEach {
                    forecasts += it.name + "(" + it.temperature + "): " + it.shortForecast
                    forecasts += MyApplication.newline + MyApplication.newline
                }
                forecasts
            } else {
                ""
            }
        } else {
            return ""
        }
    }

    private fun get7DayExtended(html: String): String {
        if (UIPreferences.useNwsApi) {
            val names = html.parseColumn("\"name\": \"(.*?)\",")
            val temperatures = html.parseColumn("\"temperature\": (.*?),")
            this.icons = html.parseColumn("\"icon\": \"(.*?)\",").toMutableList()
            val shortForecasts = html.parseColumn("\"shortForecast\": \"(.*?)\",")
            val detailedForecastsLocal = html.parseColumn("\"detailedForecast\": \"(.*?)\"")
            var forecast = MyApplication.newline + MyApplication.newline
            if (names.size == temperatures.size && temperatures.size == shortForecasts.size && shortForecasts.size == detailedForecastsLocal.size) {
                val forecasts = (names.indices).map { ObjectForecast(names[it], temperatures[it], shortForecasts[it], detailedForecastsLocal[it]) }
                forecasts.forEach {
                    forecast += it.name + ": " + it.detailedForecast
                    forecast += MyApplication.newline + MyApplication.newline
                    detailedForecasts.add(it.name + ": " + it.detailedForecast)
                }
            }
            return forecast
        } else {
            icons.clear()
            detailedForecasts.clear()
            val forecastStringList = UtilityUS.getCurrentConditionsUS(html)
            val forecastString = forecastStringList[3]
            val iconString = forecastStringList[0]
            val forecasts = forecastString.split("\n").dropLastWhile { it.isEmpty() }
            val iconList = UtilityString.parseColumn(iconString, "<icon-link>(.*?)</icon-link>")
            var forecast = MyApplication.newline + MyApplication.newline
            forecasts.forEachIndexed { index, s ->
                if (s != "") {
                    detailedForecasts.add(s.trim())
                    forecast += s.trim()
                    forecast += MyApplication.newline + MyApplication.newline
                    // UtilityLog.d("wx", s.trim())
                    if (iconList.size > index) {
                        icons.add(iconList[index])
                    }
                }
            }
            return forecast
        }
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


