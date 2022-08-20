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

import android.graphics.Color
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.*
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.objects.PolygonType.MCD
import joshuatee.wx.objects.PolygonType.MPD
import joshuatee.wx.objects.PolygonType.WATCH
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class BackgroundFetch(val context: Context) {

    // This is the main code that handles notifications (formerly in AlertReceiver)

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    private fun getNotifications() {
        var notificationUrls = ""
        var cancelStr: String
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locationNeedsMcd = UtilityNotificationSpc.locationNeedsMcd()
        val locationNeedsSwo = UtilityNotificationSpc.locationNeedsSwo()
        val locationNeedsSpcFw = UtilityNotificationSpcFireWeather.locationNeedsSpcFireWeather()
        val locationNeedsWpcMpd = UtilityNotificationWpc.locationNeedsMpd()
        (1..Location.numLocations).forEach {
            val requestID = ObjectDateTime.currentTimeMillis().toInt()
            notificationUrls += UtilityNotification.send(context, it.toString(), requestID + 1)
        }
        // Think this is no longer needed
//        RadarPreferences.radarWarningPolygons.forEach {
//            if (it.isEnabled) {
//                it.storage.valueSet(context, UtilityDownloadWarnings.getVtecByType(it.type))
//            } else {
//                it.storage.valueSet(context, "")
//            }
//        }
        if (NotificationPreferences.alertTornadoNotification || UIPreferences.checktor || PolygonType.TST.pref) {
            try {
//                UtilityDownloadWarnings.getForNotification(context)
                ObjectPolygonWarning.polygonDataByType[PolygonWarningType.FlashFloodWarning]!!.download()
                ObjectPolygonWarning.polygonDataByType[PolygonWarningType.TornadoWarning]!!.download()
                ObjectPolygonWarning.polygonDataByType[PolygonWarningType.ThunderstormWarning]!!.download()
                if (NotificationPreferences.alertTornadoNotification) {
//                    notificationUrls += UtilityNotificationTornado.checkAndSend(context, ObjectPolygonWarning.severeDashboardTor.value)
                    notificationUrls += UtilityNotificationTornado.checkAndSend(context, ObjectPolygonWarning.polygonDataByType[PolygonWarningType.TornadoWarning]!!.getData())
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
//            ObjectPolygonWarning.severeDashboardTor.valueSet(context, "")
//            ObjectPolygonWarning.severeDashboardTst.valueSet(context, "")
//            ObjectPolygonWarning.severeDashboardFfw.valueSet(context, "")
        }
        if (NotificationPreferences.alertSpcMcdNotification || UIPreferences.checkspc || MCD.pref || locationNeedsMcd) {
            try {
//                val mcdData = UtilityDownloadMcd.getMcd(context)
                val mcdData = ObjectPolygonWatch.polygonDataByType[MCD]!!.getImmediate(context)
                mcdData.numberList.forEachIndexed { index, mcdNumber ->
                    if (NotificationPreferences.alertSpcMcdNotification) {
                        val noMain = "SPC MCD #$mcdNumber"
                        val mcdPreModified = mcdData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val polygonType = MCD
                        val objectPendingIntents = ObjectPendingIntents(
                                context,
                                SpcMcdWatchShowActivity::class.java,
                                SpcMcdWatchShowActivity.NUMBER,
                                arrayOf(mcdNumber, "", polygonType.toString()),
                                arrayOf(mcdNumber, "sound", polygonType.toString())
                        )
                        cancelStr = "usspcmcd$mcdNumber"
                        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
                            val sound = NotificationPreferences.alertNotificationSoundSpcmcd && !inBlackout
                            val notificationObj = ObjectNotification(
                                    context,
                                    sound,
                                    noMain,
                                    mcdPreModified,
                                    objectPendingIntents.resultPendingIntent,
                                    GlobalVariables.ICON_MCD,
                                    mcdPreModified,
                                    NotificationCompat.PRIORITY_HIGH,
                                    Color.YELLOW,
                                    GlobalVariables.ICON_ACTION,
                                    objectPendingIntents.resultPendingIntent2,
                                    context.resources.getString(R.string.read_aloud)
                            )
                            val notification = UtilityNotification.createNotificationBigTextWithAction(notificationObj)
                            notificationObj.sendNotification(context, cancelStr, 1, notification)
                        }
                        notificationUrls += cancelStr + NotificationPreferences.notificationStrSep
                    }
                } // end while find
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            ObjectPolygonWatch.polygonDataByType[MCD]!!.storage.valueSet(context, "")
            // end of if to test if alerts_spcmcd are enabled
        }
        if (NotificationPreferences.alertWpcMpdNotification || UIPreferences.checkwpc || MPD.pref || locationNeedsWpcMpd) {
            try {
                val mpdData = ObjectPolygonWatch.polygonDataByType[MPD]!!.getImmediate(context)
                mpdData.numberList.forEachIndexed { index, mpdNumber ->
                    if (NotificationPreferences.alertWpcMpdNotification) {
                        val noMain = "WPC MPD #$mpdNumber"
                        val mcdPreModified = mpdData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val polygonType = MPD
                        val objectPendingIntents = ObjectPendingIntents(
                                context,
                                SpcMcdWatchShowActivity::class.java,
                                SpcMcdWatchShowActivity.NUMBER,
                                arrayOf(mpdNumber, "", polygonType.toString()),
                                arrayOf(mpdNumber, "sound", polygonType.toString())
                        )
                        cancelStr = "uswpcmpd$mpdNumber"
                        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
                            val sound = NotificationPreferences.alertNotificationSoundWpcmpd && !inBlackout
                            val notificationObj = ObjectNotification(
                                    context,
                                    sound,
                                    noMain,
                                    mcdPreModified,
                                    objectPendingIntents.resultPendingIntent,
                                    GlobalVariables.ICON_MPD,
                                    mcdPreModified,
                                    NotificationCompat.PRIORITY_HIGH,
                                    Color.GREEN,
                                    GlobalVariables.ICON_ACTION,
                                    objectPendingIntents.resultPendingIntent2,
                                    context.resources.getString(R.string.read_aloud)
                            )
                            val notification = UtilityNotification.createNotificationBigTextWithAction(notificationObj)
                            notificationObj.sendNotification(context, cancelStr, 1, notification)
                        }
                        notificationUrls += cancelStr + NotificationPreferences.notificationStrSep
                    }

                } // end forEach
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            ObjectPolygonWatch.polygonDataByType[MPD]!!.storage.valueSet(context, "")
            // end of if to test if alerts_wpcmpd are enabled
        }
        if (NotificationPreferences.alertSpcWatchNotification || UIPreferences.checkspc || MCD.pref) {
            try {
//                val watchData = UtilityDownloadWatch.getWatch(context)
                val watchData = ObjectPolygonWatch.polygonDataByType[WATCH]!!.getImmediate(context)
                watchData.numberList.forEachIndexed { index, watchNumber ->
                    if (NotificationPreferences.alertSpcWatchNotification) {
                        val noMain = "SPC Watch #$watchNumber"
                        val mcdPreModified = watchData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val polygonType = WATCH
                        val objectPendingIntents = ObjectPendingIntents(
                                context,
                                SpcMcdWatchShowActivity::class.java,
                                SpcMcdWatchShowActivity.NUMBER,
                                arrayOf(watchNumber, "", polygonType.toString()),
                                arrayOf(watchNumber, "sound", polygonType.toString())
                        )
                        cancelStr = "usspcwat$watchNumber"
                        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
                            val sound = NotificationPreferences.alertNotificationSoundSpcwat && !inBlackout
                            val notificationObj = ObjectNotification(
                                    context,
                                    sound,
                                    noMain,
                                    mcdPreModified,
                                    objectPendingIntents.resultPendingIntent,
                                    GlobalVariables.ICON_ALERT_2,
                                    mcdPreModified,
                                    NotificationCompat.PRIORITY_HIGH,
                                    Color.YELLOW,
                                    GlobalVariables.ICON_ACTION,
                                    objectPendingIntents.resultPendingIntent2,
                                    context.resources.getString(R.string.read_aloud)
                            )
                            val notification = UtilityNotification.createNotificationBigTextWithAction(notificationObj)
                            notificationObj.sendNotification(context, cancelStr, 1, notification)
                        }
                        notificationUrls += cancelStr + NotificationPreferences.notificationStrSep
                    }
                } // end forEach
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            ObjectPolygonWatch.polygonDataByType[WATCH]!!.storage.valueSet(context, "")
            // end of if to test if alerts_spcwat are enabled
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("notifran"))
        notificationUrls += UtilityNotificationSpc.sendSwoNotifications(context, inBlackout)
        if (NotificationPreferences.alertNhcEpacNotification || NotificationPreferences.alertNhcAtlNotification) {
            notificationUrls += UtilityNotificationNhc.send(
                    context,
                    NotificationPreferences.alertNhcEpacNotification,
                    NotificationPreferences.alertNhcAtlNotification)
        }
        // send 7day and current conditions notifications for locations
        (1..Location.numLocations).forEach {
            val requestID = ObjectDateTime.currentTimeMillis().toInt()
            notificationUrls += UtilityNotification.sendNotificationCurrentConditions(context, it.toString(), requestID, requestID + 1)
        }
        // check for any text prod notifications
        UtilityNotificationTextProduct.notifyOnAll(context)
        if (locationNeedsMcd) notificationUrls += UtilityNotificationSpc.sendMcdLocationNotifications(context)
        if (locationNeedsSwo) {
            notificationUrls += UtilityNotificationSpc.sendSwoLocationNotifications(context)
            notificationUrls += UtilityNotificationSpc.sendSwoD48LocationNotifications(context)
        }
        if (locationNeedsSpcFw) notificationUrls += UtilityNotificationSpcFireWeather.sendSpcFireWeatherD12LocationNotifications(context)
        if (locationNeedsWpcMpd) notificationUrls += UtilityNotificationWpc.sendMpdLocationNotifications(context)
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

    fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) { getNotifications() }
    }
}
