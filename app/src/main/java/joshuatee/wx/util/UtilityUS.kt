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

package joshuatee.wx.util

import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.notifications.*
import joshuatee.wx.settings.Location
import java.util.*
import java.util.regex.Matcher
import kotlin.collections.ArrayList


object UtilityUS {

    var obsClosestClass: String = ""
    private val OBS_CODE_TO_LOCATION = mutableMapOf<String, String>()

    internal fun getStatusViaMetar(context: Context, conditionsTimeStr: String): String {
        var locationName: String? = OBS_CODE_TO_LOCATION[obsClosestClass]
        if (locationName == null) {
            locationName = findObsName(context, obsClosestClass)
            if (locationName != "" && obsClosestClass != "") OBS_CODE_TO_LOCATION[obsClosestClass] = locationName
        }
        return conditionsTimeStr + " " + UtilityString.capitalizeString(locationName).trim { it <= ' ' } + " (" + obsClosestClass + ") "
    }

    private fun findObsName(context: Context, obsShortCode: String): String {
        val text = UtilityIO.readTextFileFromRaw(context.resources, R.raw.stations_us4)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val list = lines.lastOrNull { it.contains(",$obsShortCode") } ?: ""
        val items = list.split(",")
        return if (items.size > 2) items[0] + ", " + items[1] else ""
    }

    fun checkForNotifications(context: Context, currentLoc: Int, inBlackout: Boolean, tornadoWarningString: String): String {
        var html = ObjectHazards.getHazardsHtml(Location.getLatLon(currentLoc))
        var notificationUrls = ""
        val locationLabelString = "(" + Location.getName(currentLoc) + ") "
        val ids = html.parseColumn("\"@id\": \"(.*?)\"")
        val hazardTitles = html.parseColumn("\"event\": \"(.*?)\"")
        var i = 0
        hazardTitles.forEach { title ->
            if (ids.size > i) {
                val url = ids[i]
                val ca = CapAlert.createFromUrl(url)
                if (UtilityNotificationTools.nwsLocalAlertNotFiltered(context, title)) {
                    html = "$html<b>$title</b><br>"
                    html = html + "<b>Counties: " + ca.area + "</b><br>"
                    html = html + ca.summary + "<br><br><br>"
                    val noMain = locationLabelString + title
                    val noBody = title + " " + ca.area + " " + ca.summary
                    val noSummary = title + ": " + ca.area + " " + ca.summary
                    val objectPendingIntents = ObjectPendingIntents(context, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, arrayOf(url, ""), arrayOf(url, "sound"))
                    val tornadoWarningPresent = title.contains(tornadoWarningString)
                    if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, url))) {
                        val sound = MyApplication.locations[currentLoc].sound && !inBlackout || MyApplication.locations[currentLoc].sound
                                && tornadoWarningPresent && MyApplication.alertBlackoutTornado
                        val objectNotification = ObjectNotification(
                                context,
                                sound,
                                noMain,
                                noBody,
                                objectPendingIntents.resultPendingIntent,
                                MyApplication.ICON_ALERT,
                                noSummary,
                                NotificationCompat.PRIORITY_HIGH,
                                Color.BLUE,
                                MyApplication.ICON_ACTION,
                                objectPendingIntents.resultPendingIntent2,
                                context.resources.getString(R.string.read_aloud)
                        )
                        val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
                        objectNotification.sendNotification(context, url, 1, notification)
                    }
                    notificationUrls += url + MyApplication.notificationStrSep
                }
            }
            i += 1
        }
        return notificationUrls
    }

    //
    // Legacy forecast support
    //
    fun getLocationHtml(x: String, y: String): String {
        return UtilityNetworkIO.getStringFromUrlWithNewLine("https://forecast.weather.gov/MapClick.php?lat=$x&lon=$y&unit=0&lg=english&FcstType=dwml")
    }

    fun getCurrentConditionsUS(html: String): Array<String> {
        val result = Array(6) {""}
        val regexpList = arrayOf(
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
                "<conditions-icon type=.forecast-NWS. time-layout=.k-p12h-n1[0-9]-1..*?>(.*?)</conditions-icon>",
                "<wordedForecast time-layout=.k-p12h-n1[0-9]-1..*?>(.*?)</wordedForecast>",
                "<data type=.current observations.>.*?<area-description>(.*)</area-description>.*?</location>",
                "<moreWeatherInformation applicable-location=.point1.>http://www.nws.noaa.gov/data/obhistory/(.*).html</moreWeatherInformation>",
                "<data type=.current observations.>.*?<start-valid-time period-name=.current.>(.*)</start-valid-time>",
                "<time-layout time-coordinate=.local. summarization=.12hourly.>.*?<layout-key>k-p12h-n1[0-9]-1</layout-key>(.*?)</time-layout>",
                "<time-layout time-coordinate=.local. summarization=.12hourly.>.*?<layout-key>k-p24h-n[678]-1</layout-key>(.*?)</time-layout>",
                "<time-layout time-coordinate=.local. summarization=.12hourly.>.*?<layout-key>k-p24h-n[678]-2</layout-key>(.*?)</time-layout>",
                "<weather time-layout=.k-p12h-n1[0-9]-1.>.*?<name>.*?</name>(.*)</weather>", // 3 to [0-9] 3 places
                "<hazards time-layout.*?>(.*)</hazards>.*?<wordedF",
                "<data type=.forecast.>.*?<area-description>(.*?)</area-description>",
                "<humidity type=.relative..*?>(.*?)</humidity>"
        )
        val rawData = UtilityString.parseXmlExt(regexpList, html)
        result[0] = rawData[10]
        result[2] = get7Day(rawData)
        result[3] = get7DayExt(rawData)
        return result
    }

    private fun get7DayExt(raw_data: Array<String>): String {
        val timeP12n13List: MutableList<String> = ArrayList(14)
        val weatherSummaryList: MutableList<String> = ArrayList(14)
        val forecast: Array<String> = UtilityString.parseXml(raw_data[11], "text")
        // val seven_day_site_str = raw_data[20]
        // var m: Matcher
        try {
            //p = Pattern.compile(".*?weather-summary=(.*?)/>.*?");
            val m = MyApplication.utilUS_weather_summary_pattern.matcher(raw_data[18])
            weatherSummaryList.add("")
            while (m.find()) {
                weatherSummaryList.add(m.group(1).replace("\"", ""))
            }
        } catch (e: Exception) {
        }
        try {
            //p = Pattern.compile(".*?period-name=(.*?)>.*?");
            val m = MyApplication.utilUS_period_name_pattern.matcher(raw_data[15])
            timeP12n13List.add("")
            while (m.find()) {
                timeP12n13List.add(m.group(1).replace("\"", ""))
            }
        } catch (e: Exception) {
        }
        val sb = StringBuilder(300)
        // sb.append(seven_day_site_str);
        // sb.append(" ");
        // sb.append(GlobalVariables.newline);
        // sb.append(GlobalVariables.newline);
        for (j in 1 until forecast.size) {
            sb.append(timeP12n13List[j])
            sb.append(": ")
            sb.append(forecast[j])
        }
        return sb.toString()
    }

    private fun get7Day(raw_data: Array<String>): String {
        val dayHash = Hashtable<String, String>()
        dayHash["Sun"] = "Sat"
        dayHash["Mon"] = "Sun"
        dayHash["Tue"] = "Mon"
        dayHash["Wed"] = "Tue"
        dayHash["Thu"] = "Wed"
        dayHash["Fri"] = "Thu"
        dayHash["Sat"] = "Fri"

        val sb = StringBuilder(250)
        var k = 1
        val sumCnt: Int
        var maxCnt: Int
        val timeP12n13List = ArrayList<String>(14)
        val timeP24n7List = ArrayList<String>(8)
        val weatherSummaryList = ArrayList<String>(14)
        val maxTemp = UtilityString.parseXmlValue(raw_data[8])
        val minTemp = UtilityString.parseXmlValue(raw_data[9])
        var m: Matcher
        //p = Pattern.compile(".*?weather-summary=(.*?)/>.*?");
        try {
            m = MyApplication.utilUS_weather_summary_pattern.matcher(raw_data[18])
            weatherSummaryList.add("")
            while (m.find()) {
                weatherSummaryList.add(m.group(1).replace("\"",""))
            }
        } catch (e: Exception) {
        }
        //p = Pattern.compile(".*?period-name=(.*?)>.*?");
        try {
            m = MyApplication.utilUS_period_name_pattern.matcher(raw_data[15])
            timeP12n13List.add("")
            while (m.find()) {
                timeP12n13List.add(m.group(1).replace("\"",""))
            }
        } catch (e: Exception) {
        }
        try {
            m = MyApplication.utilUS_period_name_pattern.matcher(raw_data[16])
            timeP24n7List.add("")
            while (m.find()) {
                timeP24n7List.add(m.group(1).replace("\"",""))
            }
        } catch (e: Exception) {
        }
        if (timeP24n7List.size > 1 && timeP24n7List[1].contains("night")) {
            minTemp[1] = minTemp[1].replace("\\s*".toRegex(),"")
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
            sb.append(MyApplication.newline)
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
            minTemp[j] = minTemp[j].replace(" ","")
            if (sumCnt == j) {
                if (timeP24n7List.size > sumCnt + 2) {
                    sb.append(dayHash[timeP24n7List[j + 1].substring(0, 3)]) // short_time
                }
            }
            else {
                val tmpString = Utility.safeGet(timeP24n7List, j)
                if (tmpString.length > 3) {
                    sb.append(tmpString.substring(0, 3))
                } else {
                    sb.append("")
                }
            }
            sb.append(": ")
            sb.append(UtilityMath.unitsTemp(Utility.safeGet(maxTemp, maxCnt)))
            sb.append("/")
            sb.append(UtilityMath.unitsTemp(minTemp[j]))
            sb.append(" (")
            // sb.append(weatherSummaryList[k])
            sb.append(Utility.safeGet(weatherSummaryList, k))
            k += 1
            sb.append(" / ")
            // sb.append(weatherSummaryList[k])
            sb.append(Utility.safeGet(weatherSummaryList, k))
            k += 1
            sb.append(")")
            sb.append(MyApplication.newline)
            maxCnt++
        }
        if (timeP12n13List.size > 3) {
            sb.append(timeP12n13List[timeP12n13List.size - 1].substring(0, 3)) // last_short_time
        }
        sb.append(": ")
        sb.append(UtilityMath.unitsTemp(maxTemp[maxTemp.size - 1].replace("\\s*".toRegex(), ""))) // last_max
        sb.append(" (" )
        if (weatherSummaryList.size > 2) {
            sb.append(weatherSummaryList[weatherSummaryList.size - 2])
        }
        sb.append(")")
        return sb.toString().replace("Chance","Chc").replace("Thunderstorms","Tstorms")
    }
}
