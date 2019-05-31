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

import android.graphics.Color
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SpcMcdWatchShowActivity

import joshuatee.wx.objects.PolygonType.MCD
import joshuatee.wx.objects.PolygonType.MPD
import joshuatee.wx.objects.PolygonType.WATCH
import joshuatee.wx.radar.UtilityDownloadMcd
import joshuatee.wx.radar.UtilityDownloadMpd
import joshuatee.wx.radar.UtilityDownloadWarnings
import joshuatee.wx.radar.UtilityDownloadWatch
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class BackgroundFetch(val context: Context) {

    // This is the main code that handles notifications ( formerly in AlertReciever )

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    private fun getNotifications() {
        var notifUrls = ""
        //var watchNoList = ""
        //var watchLatLonList = ""
        //var watchLatlon = ""
        //var watchLatlonTor = ""
        var cancelStr: String
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locationNeedsMcd = UtilityNotificationSpc.locationNeedsMcd()
        val locationNeedsSwo = UtilityNotificationSpc.locationNeedsSwo()
        val locationNeedsSpcFw = UtilityNotificationSpcFireWeather.locationNeedsSpcFireWeather()
        val locationNeedsWpcMpd = UtilityNotificationWpc.locationNeedsMpd()
        (1..Location.numLocations).forEach {
            val requestID = System.currentTimeMillis().toInt()
            notifUrls += UtilityNotification.send(context, it.toString(), requestID + 1)
        }
        MyApplication.radarWarningPolygons.forEach {
            if (it.isEnabled) {
                it.storage.valueSet(context, UtilityDownloadWarnings.getVtecByType(it.type))
            } else {
                it.storage.valueSet(context, "")
            }
        }
        if (MyApplication.alertTornadoNotificationCurrent || MyApplication.checktor || PolygonType.TST.pref) {
            try {
                UtilityDownloadWarnings.getForNotification(context)
                if (MyApplication.alertTornadoNotificationCurrent) {
                    notifUrls += UtilityNotificationTornado.checkAndSend(context, MyApplication.severeDashboardTor.value)
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardTor.valueSet(context, "")
            MyApplication.severeDashboardTst.valueSet(context, "")
            MyApplication.severeDashboardFfw.valueSet(context, "")
        }
        if (MyApplication.alertSpcmcdNotificationCurrent || MyApplication.checkspc || PolygonType.MCD.pref || locationNeedsMcd) {
            try {
                val mcdData = UtilityDownloadMcd.getMcd(context)
                mcdData.numberList.forEachIndexed { index, mcdNumber ->
                    if (MyApplication.alertSpcmcdNotificationCurrent) {
                        val noMain = "SPC MCD #$mcdNumber"
                        val mcdPreModified = mcdData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val body = mcdPreModified
                        val summary = mcdPreModified
                        val polygonType = MCD
                        val objPI = ObjectPendingIntents(
                                context,
                                SpcMcdWatchShowActivity::class.java,
                                SpcMcdWatchShowActivity.NO,
                                arrayOf(mcdNumber, "", polygonType.toString()),
                                arrayOf(mcdNumber, "sound", polygonType.toString())
                        )
                        cancelStr = "usspcmcd$mcdNumber"
                        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                                        context,
                                        cancelStr
                                ))
                        ) {
                            val sound = MyApplication.alertNotificationSoundSpcmcd && !inBlackout
                            val notificationObj = ObjectNotification(
                                    context,
                                    sound,
                                    noMain,
                                    body,
                                    objPI.resultPendingIntent,
                                    MyApplication.ICON_MCD,
                                    summary,
                                    NotificationCompat.PRIORITY_HIGH,
                                    Color.YELLOW,
                                    MyApplication.ICON_ACTION,
                                    objPI.resultPendingIntent2,
                                    context.resources.getString(R.string.read_aloud)
                            )
                            val notification = UtilityNotification.createNotifBigTextWithAction(notificationObj)
                            notificationObj.sendNotification(context, cancelStr, 1, notification)
                        }
                        notifUrls += cancelStr + MyApplication.notificationStrSep
                    }
                } // end while find
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardMcd.valueSet(context, "")
            // end of if to test if alerts_spcmcd are enabled
        }
        if (MyApplication.alertWpcmpdNotificationCurrent || MyApplication.checkwpc || PolygonType.MPD.pref || locationNeedsWpcMpd) {
            try {
                val mpdData = UtilityDownloadMpd.getMpd(context)
                mpdData.numberList.forEachIndexed { index, mpdNumber ->
                    if (MyApplication.alertWpcmpdNotificationCurrent) {
                        val noMain = "WPC MPD #$mpdNumber"
                        val mcdPreModified = mpdData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val body = mcdPreModified
                        val summary = mcdPreModified
                        val polygonType = MPD
                        val objPI = ObjectPendingIntents(
                                context,
                                SpcMcdWatchShowActivity::class.java,
                                SpcMcdWatchShowActivity.NO,
                                arrayOf(mpdNumber, "", polygonType.toString()),
                                arrayOf(mpdNumber, "sound", polygonType.toString())
                        )
                        cancelStr = "uswpcmpd$mpdNumber"
                        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                                        context,
                                        cancelStr
                                ))
                        ) {
                            val sound = MyApplication.alertNotificationSoundWpcmpd && !inBlackout
                            val notificationObj = ObjectNotification(
                                    context,
                                    sound,
                                    noMain,
                                    body,
                                    objPI.resultPendingIntent,
                                    MyApplication.ICON_MPD,
                                    summary,
                                    NotificationCompat.PRIORITY_HIGH,
                                    Color.GREEN,
                                    MyApplication.ICON_ACTION,
                                    objPI.resultPendingIntent2,
                                    context.resources.getString(R.string.read_aloud)
                            )
                            val notification = UtilityNotification.createNotifBigTextWithAction(notificationObj)
                            notificationObj.sendNotification(context, cancelStr, 1, notification)
                        }
                        notifUrls += cancelStr + MyApplication.notificationStrSep
                    }

                } // end forEach
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardMpd.valueSet(context, "")
            // end of if to test if alerts_wpcmpd are enabled
        }
        if (MyApplication.alertSpcwatNotificationCurrent || MyApplication.checkspc || PolygonType.MCD.pref) {
            try {
                val watchData = UtilityDownloadWatch.getWatch(context)
                watchData.numberList.forEachIndexed { index, watchNumber ->
                    if (MyApplication.alertSpcwatNotificationCurrent) {
                        val noMain = "SPC Watch #$watchNumber"
                        val mcdPreModified = watchData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val body = mcdPreModified
                        val summary = mcdPreModified
                        val polygonType = WATCH
                        val objPI = ObjectPendingIntents(
                                context,
                                SpcMcdWatchShowActivity::class.java,
                                SpcMcdWatchShowActivity.NO,
                                arrayOf(watchNumber, "", polygonType.toString()),
                                arrayOf(watchNumber, "sound", polygonType.toString())
                        )
                        cancelStr = "usspcwat$watchNumber"
                        if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                                        context,
                                        cancelStr
                                ))
                        ) {
                            val sound = MyApplication.alertNotificationSoundSpcwat && !inBlackout
                            val notificationObj = ObjectNotification(
                                    context,
                                    sound,
                                    noMain,
                                    body,
                                    objPI.resultPendingIntent,
                                    MyApplication.ICON_ALERT_2,
                                    summary,
                                    NotificationCompat.PRIORITY_HIGH,
                                    Color.YELLOW,
                                    MyApplication.ICON_ACTION,
                                    objPI.resultPendingIntent2,
                                    context.resources.getString(R.string.read_aloud)
                            )
                            val notification = UtilityNotification.createNotifBigTextWithAction(notificationObj)
                            notificationObj.sendNotification(context, cancelStr, 1, notification)
                        }
                        notifUrls += cancelStr + MyApplication.notificationStrSep
                    }
                } // end forEach
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardWat.valueSet(context, "")
            // end of if to test if alerts_spcwat are enabled
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("notifran"))
        notifUrls += UtilityNotificationSpc.sendSwoNotifications(context, inBlackout)
        if (MyApplication.alertNhcEpacNotificationCurrent || MyApplication.alertNhcAtlNotificationCurrent)
            notifUrls += UtilityNotificationNhc.send(
                    context,
                    MyApplication.alertNhcEpacNotificationCurrent,
                    MyApplication.alertNhcAtlNotificationCurrent
            )

        // send 7day and current conditions notifications for locations
        (1..Location.numLocations).forEach {
            val requestID = System.currentTimeMillis().toInt()
            notifUrls += UtilityNotification.sendNotifCC(
                    context,
                    it.toString(),
                    requestID,
                    requestID + 1
            )
        }
        // check of any text prod notifs
        UtilityNotificationTextProduct.notifyOnAll(context)
        if (locationNeedsMcd) {
            notifUrls += UtilityNotificationSpc.sendMcdLocationNotifications(context)
        }
        if (locationNeedsSwo) {
            notifUrls += UtilityNotificationSpc.sendSwoLocationNotifications(context)
            notifUrls += UtilityNotificationSpc.sendSwoD48LocationNotifications(context)
        }
        if (locationNeedsSpcFw) {
            notifUrls += UtilityNotificationSpcFireWeather.sendSpcFireWeatherD12LocationNotifications(context)
        }
        if (locationNeedsWpcMpd) {
            notifUrls += UtilityNotificationWpc.sendMpdLocationNotifications(context)
        }
        cancelOldNotifications(notifUrls)
    }

    private fun cancelOldNotifications(notificationString: String) {
        val oldNotificationString = Utility.readPref(context, "NOTIF_STR", "")
        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationList = MyApplication.comma.split(oldNotificationString)
        notificationList
                .filterNot { notificationString.contains(it) }
                .forEach { notifier.cancel(it, 1) }
        Utility.writePref(context, "NOTIF_STR_OLD", oldNotificationString)
        Utility.writePref(context, "NOTIF_STR", notificationString)
    }

    fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) { getNotifications() }
    }
}
