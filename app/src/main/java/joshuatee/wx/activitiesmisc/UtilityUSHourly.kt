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

import joshuatee.wx.MyApplication
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityTime
import joshuatee.wx.util.Utility

import java.util.Calendar

import joshuatee.wx.Extensions.*

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
            val time = translateTime(startTimes[it])
            val temperature = Utility.safeGet(temperatures, it)
            val windSpeed = Utility.safeGet(windSpeeds, it).replace(" to ", "-")
            val windDirection = Utility.safeGet(windDirections, it)
            val shortForecast = Utility.safeGet(shortForecasts, it)
            timeData += time + MyApplication.newline
            tempData += temperature + MyApplication.newline
            windSpeedData += windSpeed + MyApplication.newline
            windDirData += windDirection + MyApplication.newline
            conditionData += shortenConditions(shortForecast) + MyApplication.newline
        }
        return ObjectHourly(
                timeData,
                tempData,
                windSpeedData,
                windDirData,
                conditionData
        )
    }

    private fun shortenConditions(str: String) = str.replace("Showers And Thunderstorms", "Sh/Tst")
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

    fun getString(locNum: Int): List<String> {
        val html = UtilityDownloadNws.getHourlyData(Location.getLatLon(locNum))
        val header = String.format("%-16s", "Time") + " " + String.format(
                "%-10s",
                "Temp"
        ) + String.format("%-10s", "WindSpd") + String.format(
                "%-8s",
                "WindDir"
        ) + MyApplication.newline
        return listOf(header + parse(html), html)
    }

    private fun parse(html: String): String {
        val startTime = html.parseColumn("\"startTime\": \"(.*?)\",")
        val temperature = html.parseColumn("\"temperature\": (.*?),")
        val windSpeed = html.parseColumn("\"windSpeed\": \"(.*?)\"")
        val windDirection = html.parseColumn("\"windDirection\": \"(.*?)\"")
        val shortForecast = html.parseColumn("\"shortForecast\": \"(.*?)\"")
        var content = ""
        val year = UtilityTime.year()
        val yearStr = year.toString()
        startTime.indices.forEach {
            content +=
                    String.format(
                            "%-16s", startTime[it].replace("-0[0-9]:00".toRegex(), "")
                            .replace(("$yearStr-"), "").replace(":00:00", "").replace("T", " ")

                    )
            if (temperature.size > it) {
                content += String.format("%-12s", temperature[it])
            }
            if (windSpeed.size > it) {
                content += String.format("%-12s", windSpeed[it])
            }
            if (windDirection.size > it) {
                content += String.format("%-8s", windDirection[it])
            }
            if (shortForecast.size > it) {
                content += String.format("%-12s", shortenConditions(shortForecast[it]))
            }
            content += MyApplication.newline
        }
        return content
    }

    private fun translateTime(originalTime: String): String {
        val originalTimeComponents = originalTime.replace("T", "-").split("-")
        val year = originalTimeComponents[0].toIntOrNull() ?: 0
        val month = originalTimeComponents[1].toIntOrNull() ?: 0
        val day = originalTimeComponents[2].toIntOrNull() ?: 0
        val hour = originalTimeComponents[3].replace(":00:00", "").toIntOrNull() ?: 0
        val hourString = hour.toString()
        val c = Calendar.getInstance()
        c.set(year - 1900, month - 1, day, 0, 0)
        val dayOfTheWeek = when (c.get(Calendar.DAY_OF_WEEK)) {
            6 -> "Mon"
            7 -> "Tue"
            1 -> "Wed"
            2 -> "Thu"
            3 -> "Fri"
            4 -> "Sat"
            5 -> "Sun"
            else -> ""
        }
        return ("$dayOfTheWeek $hourString")
    }
}

