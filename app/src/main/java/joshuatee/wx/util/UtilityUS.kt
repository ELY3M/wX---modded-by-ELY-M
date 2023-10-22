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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import java.util.regex.Matcher

object UtilityUS {

    private val obsCodeToLocation = mutableMapOf<String, String>()

    internal fun getStatusViaMetar(context: Context, conditionsTimeStr: String, obsSite: String): String {
        var locationName: String? = obsCodeToLocation[obsSite]
        if (locationName == null) {
            locationName = findObsName(context, obsSite)
            if (locationName != "" && obsSite != "") {
                obsCodeToLocation[obsSite] = locationName
            }
        }
        return conditionsTimeStr + " " + UtilityString.capitalizeString(locationName).trim { it <= ' ' } + " (" + obsSite + ") "
    }

    private fun findObsName(context: Context, obsShortCode: String): String {
        val text = UtilityIO.readTextFileFromRaw(context.resources, R.raw.stations_us4)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val list = lines.lastOrNull { it.contains(",$obsShortCode") } ?: ""
        val items = list.split(",")
        return if (items.size > 2) {
            items[0] + ", " + items[1]
        } else {
            ""
        }
    }

    //
    // Legacy forecast support
    //
    fun getLocationHtml(x: String, y: String): String =
            "https://forecast.weather.gov/MapClick.php?lat=$x&lon=$y&unit=0&lg=english&FcstType=dwml".getHtmlWithNewLine()

    fun getSevenDayLegacy(html: String): SevenDayDataLegacy {
        val rawData = UtilityString.parseXmlExt(regexpList, html)
        return SevenDayDataLegacy(rawData[10], get7Day(rawData), get7DayExt(rawData))
    }

    private fun get7DayExt(rawData: List<String>): String {
        val forecast = UtilityString.parseXml(rawData[11], "text")
        val timeP12n13List = listOf("") + UtilityString.parseColumn(rawData[15], GlobalVariables.utilUSPeriodNamePattern)
        return (1 until forecast.size).joinToString("") {
            timeP12n13List[it].replace("\"", "") + ": " + forecast[it]
        }
    }

    // TODO FIXME it would be nice to get rid of this
    // used in the legacy cc widget and 7 day notification
    private fun get7Day(rawData: List<String>): String {
        val dayHash = mapOf(
                "Sun" to "Sat",
                "Mon" to "Sun",
                "Tue" to "Mon",
                "Wed" to "Tue",
                "Thu" to "Wed",
                "Fri" to "Thu",
                "Sat" to "Fri",
        )
        val sb = StringBuilder(250)
        var k = 1
        val sumCnt: Int
        var maxCnt: Int
        val timeP12n13List = ArrayList<String>(14)
        val timeP24n7List = ArrayList<String>(8)
        val weatherSummaryList = ArrayList<String>(14)
        val maxTemp = UtilityString.parseXmlValue(rawData[8]).toTypedArray()
        val minTemp = UtilityString.parseXmlValue(rawData[9]).toTypedArray()
        var m: Matcher
        try {
            m = GlobalVariables.utilUSWeatherSummaryPattern.matcher(rawData[18])
            weatherSummaryList.add("")
            while (m.find()) {
                weatherSummaryList.add((m.group(1) ?: "").replace("\"", ""))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        try {
            m = GlobalVariables.utilUSPeriodNamePattern.matcher(rawData[15])
            timeP12n13List.add("")
            while (m.find()) {
                timeP12n13List.add((m.group(1) ?: "").replace("\"", ""))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        try {
            m = GlobalVariables.utilUSPeriodNamePattern.matcher(rawData[16])
            timeP24n7List.add("")
            while (m.find()) {
                timeP24n7List.add((m.group(1) ?: "").replace("\"", ""))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        if (timeP24n7List.size > 1 && timeP24n7List[1].contains("night")) {
            minTemp[1] = minTemp[1].replace("\\s*".toRegex(), "")
            if (timeP24n7List.size > 2) {
                sb.append(dayHash[timeP24n7List[2].substring(0, 3)]) // short_time
            } else {
                sb.append(timeP24n7List[1].substring(0, 3))
            }
            sb.append(": ")
            sb.append(UtilityMath.unitsTemp(minTemp[1]))
            sb.append(" (")
            sb.append(weatherSummaryList[1])
            sb.append(")")
            sb.append(GlobalVariables.newline)
            sumCnt = 2
            maxCnt = 1
            k++
        } else {
            sumCnt = 1
            maxCnt = 1
        }
        for (j in sumCnt until minTemp.size) {
            if (maxCnt < maxTemp.size) {
                maxTemp[maxCnt] = maxTemp[maxCnt].replace(" ", "")
            }
            minTemp[j] = minTemp[j].replace(" ", "")
            if (sumCnt == j) {
                if (timeP24n7List.size > sumCnt + 2) {
                    sb.append(dayHash[timeP24n7List[j + 1].substring(0, 3)]) // short_time
                }
            } else {
                val tmpString = Utility.safeGet(timeP24n7List, j)
                if (tmpString.length > 3) {
                    sb.append(tmpString.substring(0, 3))
                } else {
                    sb.append("")
                }
            }
            sb.append(": ")
            sb.append(UtilityMath.unitsTemp(Utility.safeGet(maxTemp, maxCnt).trim()))
            sb.append("/")
            sb.append(UtilityMath.unitsTemp(minTemp[j]).trim())
            sb.append(" (")
            sb.append(Utility.safeGet(weatherSummaryList, k).trim())
            k += 1
            sb.append(" / ")
            sb.append(Utility.safeGet(weatherSummaryList, k).trim())
            k += 1
            sb.append(")")
            sb.append(GlobalVariables.newline)
            maxCnt++
        }
        if (timeP12n13List.size > 3) {
            sb.append(timeP12n13List[timeP12n13List.size - 1].substring(0, 3)) // last_short_time
        }
        sb.append(": ")
        sb.append(UtilityMath.unitsTemp(maxTemp[maxTemp.size - 1].replace("\\s*".toRegex(), ""))) // last_max
        sb.append(" (")
        if (weatherSummaryList.size > 2) {
            sb.append(weatherSummaryList[weatherSummaryList.size - 2])
        }
        sb.append(")")
        return sb.toString().replace("Chance", "Chc").replace("Thunderstorms", "Tstorms")
    }

    private val regexpList = listOf(
            "<temperature type=.apparent. units=.Fahrenheit..*?>(.*?)</temperature>",
            "<temperature type=.dew point. units=.Fahrenheit..*?>(.*?)</temperature>",
            "<direction type=.wind.*?>(.*?)</direction>",
            "<wind-speed type=.gust.*?>(.*?)</wind-speed>",
            "<wind-speed type=.sustained.*?>(.*?)</wind-speed>",
            "<pressure type=.barometer.*?>(.*?)</pressure>",
            "<visibility units=.*?>(.*?)</visibility>",
            "<weather-conditions weather-summary=.(.*?)./>.*?<weather-conditions>",
            "<temperature type=.maximum..*?>(.*?)</temperature>",
            "<temperature type=.minimum..*?>(.*?)</temperature>",
            "<conditions-icon type=.forecast-NWS. time-layout=.k-p12h-n1[0-9]-1..*?>(.*?)</conditions-icon>", // index 10
            "<wordedForecast time-layout=.k-p12h-n1[0-9]-1..*?>(.*?)</wordedForecast>",                       // index 11
            "<data type=.current observations.>.*?<area-description>(.*)</area-description>.*?</location>",
            "<moreWeatherInformation applicable-location=.point1.>http://www.nws.noaa.gov/data/obhistory/(.*).html</moreWeatherInformation>",
            "<data type=.current observations.>.*?<start-valid-time period-name=.current.>(.*)</start-valid-time>",
            "<time-layout time-coordinate=.local. summarization=.12hourly.>.*?<layout-key>k-p12h-n1[0-9]-1</layout-key>(.*?)</time-layout>", // index 15
            "<time-layout time-coordinate=.local. summarization=.12hourly.>.*?<layout-key>k-p24h-n[678]-1</layout-key>(.*?)</time-layout>",
            "<time-layout time-coordinate=.local. summarization=.12hourly.>.*?<layout-key>k-p24h-n[678]-2</layout-key>(.*?)</time-layout>",
            "<weather time-layout=.k-p12h-n1[0-9]-1.>.*?<name>.*?</name>(.*)</weather>", // 3 to [0-9] 3 places
            "<hazards time-layout.*?>(.*)</hazards>.*?<wordedF",
            "<data type=.forecast.>.*?<area-description>(.*?)</area-description>",
            "<humidity type=.relative..*?>(.*?)</humidity>"
    )
}
