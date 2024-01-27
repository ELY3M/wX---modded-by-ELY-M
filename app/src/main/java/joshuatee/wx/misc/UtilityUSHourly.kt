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

package joshuatee.wx.misc

import joshuatee.wx.parseColumn
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.Location
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadNws

object UtilityUSHourly {

    internal fun getStringForActivity(html: String): Hourly {
        val startTimes = html.parseColumn("\"startTime\": \"(.*?)\",")
        val temperatures = html.parseColumn("\"temperature\": (.*?),")
        val windSpeeds = html.parseColumn("\"windSpeed\": \"(.*?)\"")
        val windDirections = html.parseColumn("\"windDirection\": \"(.*?)\"")
        val shortForecasts = html.parseColumn("\"shortForecast\": \"(.*?)\"")
        var timeData = "Time" + GlobalVariables.newline
        var tempData = "Temp" + GlobalVariables.newline
        var windSpeedData = "WindSpd" + GlobalVariables.newline
        var windDirData = "WindDir" + GlobalVariables.newline
        var conditionData = "Condition" + GlobalVariables.newline
        startTimes.indices.forEach {
            val time = ObjectDateTime.translateTimeForHourly(startTimes[it])
            val temperature = Utility.safeGet(temperatures, it).replace("\"", "")
            val windSpeed = Utility.safeGet(windSpeeds, it).replace(" to ", "-")
            val windDirection = Utility.safeGet(windDirections, it)
            val shortForecast = Utility.safeGet(shortForecasts, it)
            timeData += time + GlobalVariables.newline
            tempData += temperature + GlobalVariables.newline
            windSpeedData += windSpeed + GlobalVariables.newline
            windDirData += windDirection + GlobalVariables.newline
            conditionData += shortenConditions(shortForecast) + GlobalVariables.newline
        }
        return Hourly(timeData, tempData, windSpeedData, windDirData, conditionData)
    }

    internal fun getStringForActivityFromOldApi(html: String): Hourly {
        var timeData = "Time" + GlobalVariables.newline
        var tempData = "Temp" + GlobalVariables.newline
        var windSpeedData = "Dew" + GlobalVariables.newline
        var windDirData = "Precip%" + GlobalVariables.newline
        var conditionData = "Cloud%" + GlobalVariables.newline
        html.split(GlobalVariables.newline).drop(2).forEach {
            val items = it.split("\\s+".toRegex())
            if (items.size > 5) {
                timeData += items[0] + " " + " " + items[1] + GlobalVariables.newline
                tempData += items[2] + GlobalVariables.newline
                windSpeedData += items[3] + GlobalVariables.newline
                windDirData += items[4] + GlobalVariables.newline
                conditionData += items[5] + GlobalVariables.newline
            }
        }
        return Hourly(timeData, tempData, windSpeedData, windDirData, conditionData)
    }

    private fun shortenConditions(s: String) =
            s.replace("Showers And Thunderstorms", "Sh/Tst")
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

    fun get(locationNumber: Int): List<String> = if (UIPreferences.useNwsApiForHourly) {
        getString(locationNumber)
    } else {
        val data = UtilityHourlyOldApi.getHourlyString(locationNumber)
        listOf(data, data)
    }

    private fun getString(locationNumber: Int): List<String> {
        var html = UtilityDownloadNws.getHourlyData(Location.getLatLon(locationNumber))
        if (html.length < 300) {
            html = UtilityDownloadNws.getHourlyData(Location.getLatLon(locationNumber))
        }
        val header = To.stringPadLeft("Time", 7) + " " +
                To.stringPadLeft("Temp", 5) +
                To.stringPadLeft("WindSpd", 9) +
                To.stringPadLeft("WindDir", 8) +
                GlobalVariables.newline
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
            val time = ObjectDateTime.translateTimeForHourly(startTime[it]) //.replace(Regex("-0[0-9]:00"), ""))
            content += To.stringPadLeft(time, 8)
            if (temperature.size > it) content += To.stringPadLeft(temperature[it].replace("\"", ""), 5)
            if (windSpeed.size > it) content += To.stringPadLeft(windSpeed[it], 9)
            if (windDirection.size > it) content += To.stringPadLeft(windDirection[it], 7)
            if (shortForecast.size > it) content += To.stringPadLeft(shortenConditions(shortForecast[it]), 12)
            content += GlobalVariables.newline
        }
        return content
    }
}
