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
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.notifications.*

object UtilityUS {

    var obsClosestClass: String = ""
    private val OBS_CODE_TO_LOCATION = mutableMapOf<String, String>()

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
        var locationName = ""
        try {
            val xmlFileInputStream = context.resources.openRawResource(R.raw.stations_us4)
            val text = UtilityIO.readTextFile(xmlFileInputStream)
            val lines = text.split("\n").dropLastWhile { it.isEmpty() }
            val tmpArr: List<String>
            val tmp = lines.lastOrNull { it.contains(",$obsShortCode") } ?: ""
            tmpArr = tmp.split(",")
            if (tmpArr.size > 2) {
                locationName = tmpArr[0] + ", " + tmpArr[1]
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return locationName
    }

    fun checkForNotifications(
            context: Context,
            currentLoc: Int,
            inBlackout: Boolean,
            tornadoWarningString: String
    ): String {
        var html = ObjectForecastPackageHazards.getHazardsHtml(Location.getLatLon(currentLoc))
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
                    val objectPendingIntents = ObjectPendingIntents(
                            context,
                            USAlertsDetailActivity::class.java,
                            USAlertsDetailActivity.URL,
                            arrayOf(url, ""),
                            arrayOf(url, "sound")
                    )
                    val tornadoWarningPresent = title.contains(tornadoWarningString)
                    if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                                    context,
                                    url
                            ))
                    ) {
                        val sound = MyApplication.locations[currentLoc].sound
                                && !inBlackout
                                || MyApplication.locations[currentLoc].sound
                                && tornadoWarningPresent
                                && MyApplication.alertBlackoutTornado
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
}
