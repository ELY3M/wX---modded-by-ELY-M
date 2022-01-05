/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019 joshua.tee@gmail.com

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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.Extensions.parseColumn
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.settings.Location
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityTime

object UtilityUSHourly {

    internal fun getStringForActivity(html: String): ObjectHourly {
        val startTimes = html.parseColumn("\"startTime\": \"(.*?)\",")
        val temperatures = html.parseColumn("\"temperature\": (.*?),")
        val windSpeeds = html.parseColumn("\"windSpeed\": \"(.*?)\"")
        val windDirections = html.parseColumn("\"windDirection\": \"(.*?)\"")
        val shortForecasts = html.parseColumn("\"shortForecast\": \"(.*?)\"")
        var timeData = "Time" + MyApplication.newline
        var tempData = "Temp" + MyApplication.newline
        var windSpeedData = "WindSpd" + MyApplication.newline
        var windDirData = "WindDir" + MyApplication.newline
        var conditionData = "Condition" + MyApplication.newline
        startTimes.indices.forEach {
            val time = UtilityTime.translateTimeForHourly(startTimes[it])
            val temperature = Utility.safeGet(temperatures, it).replace("\"","")
            val windSpeed = Utility.safeGet(windSpeeds, it).replace(" to ", "-")
            val windDirection = Utility.safeGet(windDirections, it)
            val shortForecast = Utility.safeGet(shortForecasts, it)
            timeData += time + MyApplication.newline
            tempData += temperature + MyApplication.newline
            windSpeedData += windSpeed + MyApplication.newline
            windDirData += windDirection + MyApplication.newline
            conditionData += shortenConditions(shortForecast) + MyApplication.newline
        }
        return ObjectHourly(timeData, tempData, windSpeedData, windDirData, conditionData)
    }

    internal fun getStringForActivityFromOldApi(html: String): ObjectHourly {
        var timeData = "Time" + MyApplication.newline
        var tempData = "Temp" + MyApplication.newline
        var windSpeedData = "Dew" + MyApplication.newline
        var windDirData = "Precip%" + MyApplication.newline
        var conditionData = "Cloud%" + MyApplication.newline
        html.split(MyApplication.newline).forEach {
            val items = it.split("\\s+".toRegex())
            if (items.size > 6) {
                timeData += items[0] + " " + items[1] + " " + items[2] + MyApplication.newline
                tempData += items[3] + MyApplication.newline
                windSpeedData += items[4] + MyApplication.newline
                windDirData += items[5] + MyApplication.newline
                conditionData += items[6] + MyApplication.newline
            }
        }
        return ObjectHourly(timeData, tempData, windSpeedData, windDirData, conditionData)
    }

    private fun shortenConditions(string: String) = string.replace("Showers And Thunderstorms", "Sh/Tst")
            .replace("Chance", "Chc")
            .replace("Slight", "Slt")
            .replace("Light", "Lgt")
            .replace("Scattered", "Sct")
            .replace("Rain", "Rn")
            .replace("Showers", "Shwr")
            .replace("Snow", "Sn")
            .replace("Rn And Sn", "Rn/Sn")
            .replace("Freezing", "Frz")
            .replace("T-storms", "Tst")

    fun get(locationNumber: Int): List<String> {
        if (UIPreferences.useNwsApiForHourly) {
            return getString(locationNumber)
        }
        val data = UtilityHourlyOldApi.getHourlyString(locationNumber)
        return listOf(data, data)
    }

    private fun getString(locationNumber: Int): List<String> {
        val html = UtilityDownloadNws.getHourlyData(Location.getLatLon(locationNumber))
        // val header = String.format("%-7s", "Time") + " " + String.format("%-5s", "Temp") + String.format("%-9s", "WindSpd") + String.format("%-8s", "WindDir") + MyApplication.newline
        val header = To.stringPadLeft("Time", 7) + " " +
                To.stringPadLeft("Temp", 5) +
                To.stringPadLeft("WindSpd", 9) +
                To.stringPadLeft("WindDir", 8) +
                MyApplication.newline
        return listOf(header + parse(html), html)
    }

    private fun parse(html: String): String {
        val startTime = html.parseColumn("\"startTime\": \"(.*?)\",")
        val temperature = html.parseColumn("\"temperature\": (.*?),")
        val windSpeed = html.parseColumn("\"windSpeed\": \"(.*?)\"")
        val windDirection = html.parseColumn("\"windDirection\": \"(.*?)\"")
        val shortForecast = html.parseColumn("\"shortForecast\": \"(.*?)\"")
        var content = ""
        startTime.indices.forEach {
            val time = UtilityTime.translateTimeForHourly(startTime[it].replace(Regex("-0[0-9]:00"), ""))
            // content += String.format("%-8s", time)
            content += To.stringPadLeft(time, 8)
            if (temperature.size > it) {
                // content += String.format("%-5s", temperature[it].replace("\"",""))
                content += To.stringPadLeft(temperature[it].replace("\"",""), 5)
            }
            if (windSpeed.size > it) {
                // content += String.format("%-9s", windSpeed[it])
                content += To.stringPadLeft(windSpeed[it], 9)
            }
            if (windDirection.size > it) {
                // content += String.format("%-7s", windDirection[it])
                content += To.stringPadLeft(windDirection[it], 7)
            }
            if (shortForecast.size > it) {
                // content += String.format("%-12s", shortenConditions(shortForecast[it]))
                content += To.stringPadLeft(shortenConditions(shortForecast[it]), 12)
            }
            content += MyApplication.newline
        }
        return content
    }
}

