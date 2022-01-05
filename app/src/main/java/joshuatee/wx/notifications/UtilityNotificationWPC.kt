/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.objects.PolygonType.MPD
import joshuatee.wx.radar.LatLon

internal object UtilityNotificationWpc {

    fun locationNeedsMpd() = (0 until Location.numLocations).any { MyApplication.locations.getOrNull(it)?.notificationWpcMpd ?: false }

    fun sendMpdLocationNotifications(context: Context): String {
        val textMcd = MyApplication.mpdLatLon.value
        val textMcdNoList = MyApplication.mpdNoList.value
        var notifUrls = ""
        val items = MyApplication.colon.split(textMcd)
        val mpdNumbers = MyApplication.colon.split(textMcdNoList)
        items.indices.forEach { z ->
            val latLons = LatLon.parseStringToLatLons(items[z], -1.0, false)
            if (latLons.isNotEmpty()) {
                val poly2 = ExternalPolygon.Builder()
                latLons.forEach { latLon -> poly2.addVertex(ExternalPoint(latLon)) }
                val polygon2 = poly2.build()
                (1..Location.numLocations).forEach { n ->
                    val locNum = n.toString()
                    if (MyApplication.locations[n - 1].notificationWpcMpd) {
                        // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                        // call secondary method to send notification if required
                        val contains = polygon2.contains(Location.getLatLon(n - 1).asPoint())
                        if (contains) notifUrls += sendMpdNotification(context, locNum, mpdNumbers.safeGet(z))
                    }
                }
            }
        }
        return notifUrls
    }

    private fun sendMpdNotification(context: Context, locNum: String, mdNo: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locLabelStr = "(" + Location.getName(locNumInt) + ") "
        val mcdPre = UtilityDownload.getTextProduct(context, "WPCMPD$mdNo").replace("<.*?>".toRegex(), " ")
        val noMain = "$locLabelStr WPC MPD #$mdNo"
        val polygonType = MPD
        val objectPendingIntents = ObjectPendingIntents(
                context,
                SpcMcdWatchShowActivity::class.java,
                SpcMcdWatchShowActivity.NUMBER,
                arrayOf(mdNo, "", polygonType.toString()),
                arrayOf(mdNo, "sound", polygonType.toString())
        )
        val cancelStr = "wpcmpdloc$mdNo$locNum"
        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
            val sound = MyApplication.locations[locNumInt].sound && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    noMain,
                    mcdPre,
                    objectPendingIntents.resultPendingIntent,
                    MyApplication.ICON_ALERT,
                    mcdPre,
                    NotificationCompat.PRIORITY_HIGH, // was Notification.PRIORITY_DEFAULT
                    Color.YELLOW,
                    MyApplication.ICON_ACTION,
                    objectPendingIntents.resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelStr, 1, notification)
        }
        return cancelStr + MyApplication.notificationStrSep
    }
}
