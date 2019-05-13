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

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.CAPAlert
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.notifications.*

// FIXME rename
object UtilityUSv2 {

    var obsClosestClass: String = ""
    private val OBS_CODE_TO_LOCATION = mutableMapOf<String, String>()

    /*internal fun getStatus(context: Context, conditionsTimeStrF: String): String {
        var conditionsTimeStr = conditionsTimeStrF
        var locationName: String? = OBS_CODE_TO_LOCATION[obsClosestClass]
        if (locationName == null) {
            locationName = findObsName(context, obsClosestClass)
            if (locationName != "" && obsClosestClass != "") {
                OBS_CODE_TO_LOCATION[obsClosestClass] = locationName
            }
        }
        conditionsTimeStr =
            UtilityTime.convertFromUTC(UtilityString.shortenTimeV2(conditionsTimeStr))
        return conditionsTimeStr.replace(":00 ", " ") + " " + UtilityString.capitalizeString(
            locationName
        ).trim { it <= ' ' } + " (" + obsClosestClass + ") "  // strip off seconds that is always 00, need to do this here
    }*/

    internal fun getStatusViaMetar(context: Context, conditionsTimeStr: String): String {
        var locationName: String? = OBS_CODE_TO_LOCATION[obsClosestClass]
        if (locationName == null) {
            locationName = findObsName(context, obsClosestClass)
            if (locationName != "" && obsClosestClass != "") {
                OBS_CODE_TO_LOCATION[obsClosestClass] = locationName
            }
        }
        return conditionsTimeStr + " " + UtilityString.capitalizeString(locationName).trim { it <= ' ' } + " (" + obsClosestClass + ") "
    }

    private fun findObsName(context: Context, obsShortCode: String): String {
        var locatioName = ""
        try {
            val xmlFileInputStream = context.resources.openRawResource(R.raw.stations_us4)
            val text = UtilityIO.readTextFile(xmlFileInputStream)
            val lines = text.split("\n").dropLastWhile { it.isEmpty() }
            val tmpArr: List<String>
            val tmp = lines.lastOrNull { it.contains(",$obsShortCode") } ?: ""
            tmpArr = tmp.split(",")
            if (tmpArr.size > 2) {
                locatioName = tmpArr[0] + ", " + tmpArr[1]
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return locatioName
    }

    /*internal fun getObsFromLatLon(context: Context, location: LatLon): String {
        var x = location.latString
        var y = location.lonString
        x = UtilityMath.latLonFix(x)
        y = UtilityMath.latLonFix(y)
        val key = "LLTOOBS$x,$y"
        var obsClosest: String = Utility.readPref(key, "")
        if (obsClosest == "") {
            val obsHtml =
                UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/points/$x,$y/stations")
            obsClosest = obsHtml.parse("gov/stations/(.*?)\"")
            obsClosestClass = obsClosest
            if (key != "" && obsClosest != "") {
                Utility.writePref(context, key, obsClosest)
            }
        }
        obsClosestClass = obsClosest
        return obsClosest
    }*/

    /*


	"number": 1,
                "name": "This Afternoon",
                "startTime": "2016-12-21T14:00:00-05:00",
                "endTime": "2016-12-21T18:00:00-05:00",
                "isDaytime": true,
                "temperature": 58,
                "windSpeed": "3 mph",
                "windDirection": "WSW",
                "icon": "https://api-v1.weather.gov/icons/land/day/few?size=medium",
                "shortForecast": "Sunny",
                "detailedForecast": "Sunny, with a high near 58. West southwest wind around 3 mph."


	 */

    fun checkForNotifications(
            context: Context,
            currentLoc: Int,
            inBlackout: Boolean,
            tornadoWarningString: String
    ): String {
        var html = ObjectForecastPackageHazards.getHazardsHtml(Location.getLatLon(currentLoc))
        var notifUrls = ""
        val locLabelStr = "(" + Location.getName(currentLoc) + ") "
        val idAl = html.parseColumn("\"@id\": \"(.*?)\"")
        val hazardTitles = html.parseColumn("\"event\": \"(.*?)\"")
        var i = 0
        hazardTitles.forEach { title ->
            if (idAl.size > i) {
                val url = idAl[i]
                val ca = CAPAlert.createFromURL(url)
                if (UtilityNotificationTools.nwsLocalAlertNotFiltered(context, title)) {
                    html = "$html<b>$title</b><br>"
                    html = html + "<b>Counties: " + ca.area + "</b><br>"
                    html = html + ca.summary + "<br><br><br>"
                    val noMain = locLabelStr + title
                    val noBody = title + " " + ca.area + " " + ca.summary
                    val noSummary = title + ": " + ca.area + " " + ca.summary
                    val objPI = ObjectPendingIntents(
                            context,
                            USAlertsDetailActivity::class.java,
                            USAlertsDetailActivity.URL,
                            arrayOf(url, ""),
                            arrayOf(url, "sound")
                    )
                    val tornadoWarningPresent = title.contains(tornadoWarningString)
                    if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(
                                    context,
                                    url
                            ))
                    ) {
                        val sound =
                                MyApplication.locations[currentLoc].sound && !inBlackout || MyApplication.locations[currentLoc].sound && tornadoWarningPresent && MyApplication.alertBlackoutTornadoCurrent
                        val notifObj = ObjectNotification(
                                context,
                                sound,
                                noMain,
                                noBody,
                                objPI.resultPendingIntent,
                                MyApplication.ICON_ALERT,
                                noSummary,
                                NotificationCompat.PRIORITY_HIGH,
                                Color.BLUE,
                                MyApplication.ICON_ACTION,
                                objPI.resultPendingIntent2,
                                context.resources.getString(R.string.read_aloud)
                        )
                        val noti = UtilityNotification.createNotifBigTextWithAction(notifObj)
                        notifObj.sendNotification(context, url, 1, noti)
                        //notifier.notify(url, 1, noti)
                    }
                    notifUrls += url + MyApplication.notificationStrSep
                }
            }
            i += 1
        }
        return notifUrls
    }
}
