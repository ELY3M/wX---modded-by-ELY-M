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

import joshuatee.wx.objects.PolygonType.MPD
import joshuatee.wx.util.Utility

internal object UtilityNotificationWpc {

    fun locationNeedsMpd(): Boolean {
        return (0 until Location.numLocations).any {
            MyApplication.locations.getOrNull(it)?.notificationWpcMpd ?: false
        }
    }

    fun sendMpdLocationNotifications(context: Context): String {
        val textMcd = MyApplication.mpdLatLon.value
        val textMcdNoList = MyApplication.mpdNoList.value
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()
        var locNum: String
        var locXDbl: Double
        var locYDbl: Double
        var notifUrls = ""
        var i: Int
        val tmpArr = MyApplication.colon.split(textMcd)
        val mcdNoArr = MyApplication.colon.split(textMcdNoList)
        var z = 0
        var test: Array<String>
        while (z < tmpArr.size) {
            test = MyApplication.space.split(tmpArr[z])
            x.clear()
            y.clear()
            i = 0
            while (i < test.size) {
                if (i and 1 == 0) {
                    x.add(test[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((test[i].toDoubleOrNull() ?: 0.0) * -1)
                }
                i += 1
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                for (j in x.indices) {
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                for (n in 1..Location.numLocations) {
                    locNum = n.toString()
                    if (MyApplication.locations[n - 1].notificationWpcMpd) {
                        // if location is watching for MCDs pull ib lat/lon and interate over polygons
                        // call secondary method to send notification if required
                        locXDbl = MyApplication.locations[n - 1].x.toDoubleOrNull() ?: 0.0
                        locYDbl = MyApplication.locations[n - 1].y.toDoubleOrNull() ?: 0.0
                        val contains =
                                polygon2.contains(ExternalPoint(locXDbl.toFloat(), locYDbl.toFloat()))
                        if (contains) {
                            notifUrls += sendMpdNotification(context, locNum, Utility.safeGet(mcdNoArr, z))
                        }
                    }
                }
            }
            z += 1
        }
        return notifUrls
    }

    private fun sendMpdNotification(context: Context, locNum: String, mdNo: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        var notifUrls = ""
        val noMain: String
        val noBody: String
        val noSummary: String
        val locLabelStr: String
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        locLabelStr = "(" + Location.getName(locNumInt) + ") "
        var mcdPre = UtilityDownload.getTextProduct(context, "WPCMPD$mdNo")
        noMain = "$locLabelStr WPC MPD #$mdNo"
        mcdPre = mcdPre.replace("<.*?>".toRegex(), " ")
        noBody = mcdPre
        noSummary = mcdPre
        val polygonType = MPD
        val objectPendingIntents = ObjectPendingIntents(
                context,
                SpcMcdWatchShowActivity::class.java,
                SpcMcdWatchShowActivity.NO,
                arrayOf(mdNo, "", polygonType.toString()),
                arrayOf(mdNo, "sound", polygonType.toString())
        )
        val cancelStr = "wpcmpdloc$mdNo$locNum"
        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                        context,
                        cancelStr
                ))
        ) {
            val sound = MyApplication.locations[locNumInt].sound && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    noMain,
                    noBody,
                    objectPendingIntents.resultPendingIntent,
                    MyApplication.ICON_ALERT,
                    noSummary,
                    NotificationCompat.PRIORITY_HIGH, // was Notification.PRIORITY_DEFAULT
                    Color.YELLOW,
                    MyApplication.ICON_ACTION,
                    objectPendingIntents.resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelStr, 1, notification)
        }
        notifUrls += cancelStr + MyApplication.notificationStrSep
        return notifUrls
    }
}




