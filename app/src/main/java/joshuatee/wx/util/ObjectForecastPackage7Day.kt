/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

class ObjectForecastPackage7Day internal constructor(locNum: Int, html: String) {

    var iconstr: String = ""
        private set
    var sevenDayExtStr: String = ""
        private set
    var sevenDayShort: String = ""
        private set
    var iconAl: List<String> = listOf()
        private set
    private var detailedForecastAl = mutableListOf<String>()

    init {
        if (locNum == -1 || Location.isUS(locNum)) {
            iconstr = getIcons7Day(html)
            sevenDayExtStr = get7DayExt(html)
            sevenDayShort = get7DayShort(html)
        } else {
            sevenDayExtStr = UtilityCanada.get7Day(html)
            iconstr = UtilityCanada.getIcons7Day(sevenDayExtStr)
            iconAl = UtilityCanada.getIcons7DayAl(sevenDayExtStr)
            convertExt7DaytoList()
        }
    }

    val fcstList: List<String>
        get() = detailedForecastAl

    private fun convertExt7DaytoList() {
        detailedForecastAl = sevenDayExtStr.split(MyApplication.newline + MyApplication.newline)
            .dropLastWhile { it.isEmpty() }.toMutableList()
    }

    private fun getIcons7Day(html: String): String {
        val iconAl = html.parseColumn("\"icon\": \"(.*?)\",")
        val sb = StringBuilder(500)
        iconAl.forEach {
            sb.append(it)
            sb.append("!")
        }
        return sb.toString()
    }

    private fun get7DayShort(html: String): String {
        val nameAl = html.parseColumn("\"name\": \"(.*?)\",")
        val temperatureAl = html.parseColumn("\"temperature\": (.*?),")
        val shortForecastAl = html.parseColumn("\"shortForecast\": \"(.*?)\",")
        val detailedForecastAl = html.parseColumn("\"detailedForecast\": \"(.*?)\"")
        if ((nameAl.size == temperatureAl.size) && (temperatureAl.size == shortForecastAl.size) && (shortForecastAl.size == detailedForecastAl.size)) {
            val forecastAl = (0 until nameAl.size).mapTo(mutableListOf()) {
                ObjectForecast(
                    nameAl[it],
                    temperatureAl[it],
                    shortForecastAl[it],
                    detailedForecastAl[it]
                )
            }
            val sb = StringBuilder(200)
            sb.append(MyApplication.newline)
            sb.append(MyApplication.newline)
            forecastAl.forEach {
                sb.append(it.name)
                sb.append("(")
                sb.append(it.temperature)
                sb.append("): ")
                sb.append(it.shortForecast)
                sb.append(MyApplication.newline)
                sb.append(MyApplication.newline)
            }
            return sb.toString()
        } else {
            return ""
        }
    }

    private fun get7DayExt(html: String): String {
        val forecastAl = mutableListOf<ObjectForecast>()
        val nameAl = html.parseColumn("\"name\": \"(.*?)\",")
        val temperatureAl = html.parseColumn("\"temperature\": (.*?),")
        this.iconAl = html.parseColumn("\"icon\": \"(.*?)\",")
        val shortForecastAl = html.parseColumn("\"shortForecast\": \"(.*?)\",")
        val detailedForecastAlLocal = html.parseColumn("\"detailedForecast\": \"(.*?)\"")
        if (nameAl.size == temperatureAl.size && temperatureAl.size == shortForecastAl.size && shortForecastAl.size == detailedForecastAlLocal.size) {
            (0 until nameAl.size).mapTo(forecastAl) {
                ObjectForecast(
                    nameAl[it],
                    temperatureAl[it],
                    shortForecastAl[it],
                    detailedForecastAlLocal[it]
                )
            }
        }
        val sb = StringBuilder(200)
        sb.append(MyApplication.newline)
        sb.append(MyApplication.newline)
        forecastAl.forEach {
            sb.append(it.name)
            sb.append(": ")
            sb.append(it.detailedForecast)
            sb.append(MyApplication.newline)
            sb.append(MyApplication.newline)
            detailedForecastAl.add(it.name + ": " + it.detailedForecast)
        }
        return sb.toString()
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
 
 
 */


