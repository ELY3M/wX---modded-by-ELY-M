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

import android.content.Context
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.util.To
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.UtilityLog

internal object NotificationMpd {

    fun locationNeedsMpd() = (0 until Location.numLocations).any { Location.locations.getOrNull(it)?.notificationWpcMpd ?: false }

    fun send(context: Context): String {
        var notificationUrls = ""
        if (NotificationPreferences.alertWpcMpdNotification || UIPreferences.checkwpc || locationNeedsMpd()) {
            try {
                val mpdData = PolygonWatch.byType[PolygonType.MPD]!!.getImmediate(context)
                mpdData.numberList.forEachIndexed { index, s ->
                    if (NotificationPreferences.alertWpcMpdNotification) {
                        notificationUrls += sendMpd(context, "CONUS", s, mpdData.htmlList[index].replace("<.*?>".toRegex(), " "))
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        return notificationUrls
    }

    fun sendLocation(context: Context): String {
        val textMcd = PolygonWatch.byType[PolygonType.MPD]!!.latLonList.value
        val textMcdNoList = PolygonWatch.byType[PolygonType.MPD]!!.numberList.value
        var notificationUrls = ""
        val items = RegExp.colon.split(textMcd)
        val mpdNumbers = RegExp.colon.split(textMcdNoList)
        items.indices.forEach { z ->
            val latLons = LatLon.parseStringToLatLons(items[z], -1.0, false)
            if (latLons.isNotEmpty()) {
                val poly2 = ExternalPolygon.Builder()
                latLons.forEach {
                    latLon -> poly2.addVertex(ExternalPoint(latLon))
                }
                val polygon2 = poly2.build()
                (1..Location.numLocations).forEach { n ->
                    val locNum = n.toString()
                    if (Location.locations[n - 1].notificationWpcMpd) {
                        // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                        // call secondary method to send notification if required
                        val contains = polygon2.contains(Location.getLatLon(n - 1).asPoint())
                        if (contains) {
                            notificationUrls += sendMpd(context, locNum, mpdNumbers.safeGet(z))
                        }
                    }
                }
            }
        }
        return notificationUrls
    }

    private fun sendMpd(context: Context, locNum: String, mdNo: String, bodyText: String = ""): String {
        val locationIndex = To.int(locNum) - 1
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locationLabel = if (locNum == "CONUS") {
            "(CONUS) "
        } else {
            "(" + Location.getName(locationIndex) + ") "
        }
        val iconAlert = if (locNum == "CONUS") {
            GlobalVariables.ICON_MPD
        } else {
            GlobalVariables.ICON_ALERT
        }
        val text = if (bodyText == "") {
            DownloadText.byProduct(context, "WPCMPD$mdNo").replace("<.*?>".toRegex(), " ")
        } else {
            bodyText
        }
        val label = "$locationLabel WPC MPD #$mdNo"
        val polygonType = PolygonType.MPD
        val objectPendingIntents = ObjectPendingIntents(
                context,
                SpcMcdWatchShowActivity::class.java,
                SpcMcdWatchShowActivity.NUMBER,
                arrayOf(mdNo, polygonType.toString(), ""),
                arrayOf(mdNo, polygonType.toString(), "sound")
        )
        val cancelString = "wpcmpdloc$mdNo$locNum"
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = if (locNum == "CONUS") {
                NotificationPreferences.alertNotificationSoundWpcmpd && !inBlackout
            } else {
                Location.locations[locationIndex].sound && !inBlackout
            }
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    label,
                    text,
                    objectPendingIntents,
                    iconAlert,
                    GlobalVariables.ICON_ACTION,
                    context.resources.getString(R.string.read_aloud)
            )
            objectNotification.send(cancelString)
        }
        return cancelString + NotificationPreferences.notificationStrSep
    }
}
