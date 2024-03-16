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

package joshuatee.wx.notifications

import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.spc.SpcFireOutlookSummaryActivity
import android.content.Context
import joshuatee.wx.getHtml
import joshuatee.wx.parse
import joshuatee.wx.parseColumn
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.To

@Suppress("SpellCheckingInspection")
internal object NotificationSpcFireWeather {

    fun locationNeedsSpcFireWeather() = (0 until Location.numLocations).any {
        Location.locations.getOrNull(it)?.notificationSpcFw ?: false
    }

    private fun sendNotification(context: Context, locNum: String, day: Int, threatLevel: String, validTime: String): String {
        val locationIndex = To.int(locNum) - 1
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locationLabel = "(" + Location.getName(locationIndex) + ") "
        val dayString = "SPC FWDY$day"
        val label = "$locationLabel$dayString $threatLevel"
        val text = threatLevel.replace("<.*?>".toRegex(), " ").replace("&nbsp".toRegex(), " ")
        val objectPendingIntents = ObjectPendingIntents(context, SpcFireOutlookSummaryActivity::class.java)
        val cancelString = "spcfwloc$day$locNum$threatLevel$validTime"
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = Location.locations[locationIndex].sound && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    label,
                    text,
                    objectPendingIntents,
                    GlobalVariables.ICON_ALERT,
                    GlobalVariables.ICON_ACTION,
                    context.resources.getString(R.string.read_aloud)
            )
            objectNotification.send(cancelString)
        }
        return cancelString + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
    }

    fun sendD12Location(context: Context): String {
        var notificationUrls = ""
        val threatList = listOf("EXTR", "CRIT", "ELEV", "SDRT", "IDRT")
        (1..2).forEach { day ->
            val urlLocal = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/fire_wx/fwdy" + day.toString() + ".html"
            var urlBlob = urlLocal.getHtml().parse("CLICK FOR <a href=.(.*?txt).>DAY [12] FIREWX AREAL OUTLINE PRODUCT .KWNSPFWFD[12].</a>")
            urlBlob = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}$urlBlob"
            var html = urlBlob.getHtml()
            val validTime = html.parse("VALID TIME ([0-9]{6}Z - [0-9]{6}Z)")
            html = html.replace(GlobalVariables.newline, " ")
            val htmlBlob = html.parse("FIRE WEATHER OUTLOOK POINTS DAY $day(.*?&)&") // was (.*?)&&
            threatList.forEach { threat ->
                var string = ""
                val htmlList = htmlBlob.parseColumn(threat.substring(1) + "(.*?)[A-Z&]")
                htmlList.forEach {
                    string += LatLon.storeWatchMcdLatLon(it)
                    string = string.replace(" 99.99 99.99 ", " ") // need for the way SPC ConvO separates on 8 's
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
                        latLons.forEach { latLon ->
                            polygonFrame.addVertex(ExternalPoint(latLon))
                        }
                        val polygonShape = polygonFrame.build()
                        (1..Location.numLocations).forEach { n ->
                            val locNum = n.toString()
                            if (Location.locations.getOrNull(n - 1)?.notificationSpcFw == true) {
                                // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                                // call secondary method to send notif if required
                                if (polygonShape.contains(Location.getLatLon(n - 1).asPoint())) {
                                    if (!notificationUrls.contains("spcfwloc$day$locNum")) {
                                        notificationUrls += sendNotification(context, locNum, day, threat, validTime)
                                    }
                                }
                            }
                        }
                    }
                } // end loop over polygons for specific day
            } // end loop over treat level
        } // end loop of day 1-3
        return notificationUrls
    }
}
