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
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.parseColumn
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.settings.UIPreferences

internal object NotificationTornado {

    fun send(context: Context): String {
        var notificationUrls = ""
        if (NotificationPreferences.alertTornadoNotification || UIPreferences.checktor) {
            try {
                PolygonWarning.byType[PolygonWarningType.FlashFloodWarning]!!.download()
                PolygonWarning.byType[PolygonWarningType.TornadoWarning]!!.download()
                PolygonWarning.byType[PolygonWarningType.ThunderstormWarning]!!.download()
                if (NotificationPreferences.alertTornadoNotification) {
                    notificationUrls += checkAndSend(context, PolygonWarning.byType[PolygonWarningType.TornadoWarning]!!.getData())
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        return notificationUrls
    }

    private fun checkAndSend(context: Context, html: String): String {
        var notificationUrls = ""
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        try {
            notificationUrls += checkForNotifications(context, html, inBlackout)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return notificationUrls
    }

    private fun checkForNotifications(context: Context, html: String, inBlackout: Boolean): String {
        var notificationUrls = ""
        val locationLabel = "(" + "CONUS Tornado alert" + ") "
        val idAl = html.parseColumn("\"@id\": \"(.*?)\"")
        val hazardTitles = html.parseColumn("\"event\": \"(.*?)\"")
        hazardTitles.forEachIndexed { i, title ->
            if (idAl.size > i) {
                val url = idAl[i]
                val capAlert = CapAlert.createFromUrl(url)
                val isAlertCurrent = ObjectDateTime.isVtecCurrent(capAlert.vtec)
                if (isAlertCurrent && UtilityNotificationTools.nwsLocalAlertNotFiltered(context, title)) {
                    val label = locationLabel + title
                    val text = title + ": " + capAlert.area + " " + capAlert.summary
                    val objectPendingIntents = ObjectPendingIntents(
                            context,
                            USAlertsDetailActivity::class.java,
                            USAlertsDetailActivity.URL,
                            arrayOf(url, ""),
                            arrayOf(url, "sound")
                    )
                    if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, url))) {
                        val sound = NotificationPreferences.alertNotificationSoundTornadoCurrent && !inBlackout
                        val objectNotification = ObjectNotification(
                                context,
                                sound,
                                label,
                                text,
                                objectPendingIntents,
                                GlobalVariables.ICON_TORNADO,
                                GlobalVariables.ICON_ACTION,
                                context.resources.getString(R.string.read_aloud)
                        )
                        objectNotification.send(url)
                    }
                    notificationUrls += url + NotificationPreferences.notificationStrSep
                }
            }
        }
        return notificationUrls
    }
}
