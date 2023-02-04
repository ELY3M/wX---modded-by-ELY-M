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

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.widgets.UtilityWidgetDownload
import kotlinx.coroutines.*

class BackgroundFetch(val context: Context) {

    // This is the main code that handles notifications (formerly in AlertReceiver)

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    val timer = DownloadTimer("NOTIFICATIONS_MAIN")

    private fun getNotifications() {
        var notificationUrls = ""
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locationNeedsMcd = NotificationMcd.locationNeedsMcd()
        val locationNeedsSwo = NotificationSwo.locationNeedsSwo()
        val locationNeedsSpcFw = NotificationSpcFireWeather.locationNeedsSpcFireWeather()
        val locationNeedsWpcMpd = NotificationMpd.locationNeedsMpd()
        //
        // iterate over locations and if enabled check for alerts
        //
        (1..Location.numLocations).forEach {
            val requestID = ObjectDateTime.currentTimeMillis().toInt()
            notificationUrls += NotificationLocal.send(context, it.toString(), requestID + 1)
        }
        //
        // Check for CONUS Tornado alerts
        // This code also runs if users has enabled tab headers for warnings
        //
        notificationUrls += NotificationTornado.send(context)
        //
        // Check for CONUS wide MCD or MCD specific to a location if configured
        //
        notificationUrls += NotificationMcd.send(context)
        //
        // Check for CONUS wide MPD or MCD specific to a location if configured
        //
        notificationUrls += NotificationMpd.send(context)
        //
        // Check for CONUS wide Watch
        //
        notificationUrls += NotificationWatch.send(context)
        //
        // Let the main program know to update tab headers
        //
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("notifran"))
        //
        // SPC SWO outlook notification check
        //
        notificationUrls += NotificationSwo.send(context, inBlackout)
        //
        // NHC
        //
        if (NotificationPreferences.alertNhcEpacNotification || NotificationPreferences.alertNhcAtlNotification) {
            notificationUrls += NotificationNhc.send(context)
        }
        // send 7day and current conditions notifications for locations
        (1..Location.numLocations).forEach {
            val requestID = ObjectDateTime.currentTimeMillis().toInt()
            notificationUrls += NotificationLocal.sendCurrentConditionsAnd7Day(context, it.toString(), requestID)
        }
        // check for any text prod notifications
        NotificationTextProduct.notifyOnAll(context)
        //
        // Check for location specific mcd/swo/spcfw/mpd alerts
        //
        if (locationNeedsMcd) {
            notificationUrls += NotificationMcd.sendLocation(context)
        }
        if (locationNeedsSwo) {
            notificationUrls += NotificationSwo.sendLocation(context)
            notificationUrls += NotificationSwo.sendD48Location(context)
        }
        if (locationNeedsSpcFw) {
            notificationUrls += NotificationSpcFireWeather.sendD12Location(context)
        }
        if (locationNeedsWpcMpd) {
            notificationUrls += NotificationMpd.sendLocation(context)
        }
        cancelOldNotifications(notificationUrls)
    }

    private fun cancelOldNotifications(notificationString: String) {
        val oldNotificationString = Utility.readPref(context, "NOTIF_STR", "")
        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationList = RegExp.comma.split(oldNotificationString)
        notificationList.filterNot { notificationString.contains(it) }.forEach {
            notifier.cancel(it, 1)
        }
        Utility.writePref(context, "NOTIF_STR_OLD", oldNotificationString)
        Utility.writePref(context, "NOTIF_STR", notificationString)
    }

    @Synchronized fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            if (timer.isRefreshNeeded(context)) {
                // https://developer.android.com/develop/ui/views/notifications/notification-permission
                val notificationManagerCompat = NotificationManagerCompat.from(context)
                val areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled()
                UtilityLog.d("WXRADAR", "notifications enabled: $areNotificationsEnabled")
                if (areNotificationsEnabled || UIPreferences.checkspc) {
                    UtilityLog.d("WXRADAR", "checking for notifications to send")
                    getNotifications()
                }
                UtilityWidgetDownload.getWidgetData(context)
            }
        }
    }
}
