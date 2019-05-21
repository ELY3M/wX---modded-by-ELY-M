/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.graphics.Color
import androidx.core.app.NotificationCompat

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.CAPAlert
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityLog

import joshuatee.wx.Extensions.*

// FIXME share more code with severe dashboard or nexrad radar
internal object UtilityNotificationTornado {

    // the fun with support 23.2.0 continues
    // https://code.google.com/p/android/issues/detail?id=201958
    // Issue 201958: 	NotificationCompat does not appear to support vector drawables (wrapped in another drawable or not)

    fun checkAndSendTornadoNotification(context: Context, html: String): String {
        var notifUrls = ""
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        try {
            notifUrls += checkForNotifications(context, html, inBlackout)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return notifUrls
    }

    private fun checkForNotifications(
            context: Context,
            htmlF: String,
            inBlackout: Boolean
    ): String {
        var html = htmlF
        var notifUrls = ""
        val locLabelStr = "(" + "CONUS Tornado alert" + ") "
        val idAl = html.parseColumn("\"@id\": \"(.*?)\"")
        val hazardTitles = html.parseColumn("\"event\": \"(.*?)\"")
        var i = 0
        for (title in hazardTitles) {
            if (idAl.size > i) {
                val url = idAl[i]
                val ca = CAPAlert.createFromUrl(url)
                if (UtilityNotificationTools.nwsLocalAlertNotFiltered(
                                context,
                                title
                        )
                ) { // placeholder for WFO filter check
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
                    if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(
                                    context,
                                    url
                            ))
                    ) {
                        val sound =
                                MyApplication.alertNotificationSoundTornadoCurrent && !inBlackout
                        val notifObj = ObjectNotification(
                                context,
                                sound,
                                noMain,
                                noBody,
                                objPI.resultPendingIntent,
                                MyApplication.ICON_TORNADO,
                                noSummary,
                                NotificationCompat.PRIORITY_HIGH,
                                Color.RED,
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
            } // end size check of URL list
            i += 1
        }  // end for loop over hazard list
        return notifUrls
    }
}
