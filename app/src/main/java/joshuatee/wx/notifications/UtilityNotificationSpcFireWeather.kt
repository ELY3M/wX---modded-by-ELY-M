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

package joshuatee.wx.notifications

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.spc.SpcFireOutlookSummaryActivity
import joshuatee.wx.util.UtilityString
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.NotificationPreferences

internal object UtilityNotificationSpcFireWeather {

    fun locationNeedsSpcFireWeather() = (0 until Location.numLocations).any {
            MyApplication.locations.getOrNull(it)?.notificationSpcFw ?: false
        }

    private fun sendSpcFireWeatherNotification(context: Context, locNum: String, day: Int, threatLevel: String, validTime: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locLabelStr = "(" + Location.getName(locNumInt) + ") "
        val dayStr = "SPC FWDY$day"
        val noMain = "$locLabelStr$dayStr $threatLevel"
        var detailRaw = threatLevel.replace("<.*?>".toRegex(), " ")
        detailRaw = detailRaw.replace("&nbsp".toRegex(), " ")
        val noBody = detailRaw
        val objectPendingIntents = ObjectPendingIntents(context, SpcFireOutlookSummaryActivity::class.java)
        val cancelString = "spcfwloc$day$locNum$threatLevel$validTime"
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = MyApplication.locations[locNumInt].sound && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    noMain,
                    noBody,
                    objectPendingIntents.resultPendingIntent,
                    GlobalVariables.ICON_ALERT,
                    noBody,
                    NotificationCompat.PRIORITY_HIGH, // was Notification.PRIORITY_DEFAULT
                    Color.YELLOW,
                    GlobalVariables.ICON_ACTION,
                    objectPendingIntents.resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelString, 1, notification)
        }
        return cancelString + NotificationPreferences.notificationStrSep
    }

    fun sendSpcFireWeatherD12LocationNotifications(context: Context): String {
        var notifUrls = ""
        val threatList = listOf("EXTR", "CRIT", "ELEV", "SDRT", "IDRT")
        (1..2).forEach { day ->
            val urlLocal = "${GlobalVariables.nwsSPCwebsitePrefix}/products/fire_wx/fwdy" + day.toString() + ".html"
            var urlBlob = UtilityString.getHtmlAndParse(urlLocal, "CLICK FOR <a href=.(.*?txt).>DAY [12] FIREWX AREAL OUTLINE PRODUCT .KWNSPFWFD[12].</a>")
            urlBlob = "${GlobalVariables.nwsSPCwebsitePrefix}$urlBlob"
            var html = urlBlob.getHtmlSep()
            val validTime = html.parse("VALID TIME ([0-9]{6}Z - [0-9]{6}Z)")
            html = html.replace("<br>", " ")
            val htmlBlob = html.parse("FIRE WEATHER OUTLOOK POINTS DAY $day(.*?&)&") // was (.*?)&&
            threatList.forEach { threat ->
                var string = ""
                val htmlList = htmlBlob.parseColumn(threat.substring(1) + "(.*?)[A-Z&]")
                htmlList.forEach {
                    string += UtilityNotification.storeWatchMcdLatLon(it)
                    string = string.replace(" 99.99 99.99 ", " ") // need for the way SPC ConvO seperates on 8 's
                } // end looping over polygons of one threat level
                val items = RegExp.colon.split(string)
                items.forEach {
                    val latLons = LatLon.parseStringToLatLons(it, -1.0, false)
                    // inject bounding box coords if first doesn't equal last
                    // focus on east coast for now
                    //
                    // 52,-130               52,-62
                    // 21,-130                21,-62
                    //
                    if (latLons.isNotEmpty()) {
                        val polygonFrame = ExternalPolygon.Builder()
                        latLons.forEach { latLon -> polygonFrame.addVertex(ExternalPoint(latLon)) }
                        val polygonShape = polygonFrame.build()
                        (1..Location.numLocations).forEach { n ->
                            val locNum = n.toString()
                            if (MyApplication.locations.getOrNull(n - 1)?.notificationSpcFw == true) {
                                // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                                // call secondary method to send notif if required
                                if (polygonShape.contains(Location.getLatLon(n - 1).asPoint())) {
                                    if (!notifUrls.contains("spcfwloc$day$locNum")) notifUrls += sendSpcFireWeatherNotification(context, locNum, day, threat, validTime)
                                }
                            }
                        }
                    }
                } // end loop over polygons for specific day
            } // end loop over treat level
        } // end loop of day 1-3
        return notifUrls
    }
}
