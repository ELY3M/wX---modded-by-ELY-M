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
//modded by ELY M. 

package joshuatee.wx.notifications

import java.util.regex.Matcher

import android.graphics.Color
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import joshuatee.wx.Extensions.getHtml

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.RegExp
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SPCMCDWShowActivity

import joshuatee.wx.objects.PolygonType.MCD
import joshuatee.wx.objects.PolygonType.MPD
import joshuatee.wx.objects.PolygonType.WATCH
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class BackgroundFetch(val context: Context) {

    // This is the main code that handles notifications ( formerly in AlertReciever )

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    private fun doNotifs() {
        var notifUrls = ""
        var watchNoList = ""
        var watchLatlonList = ""
        var watchLatlon = ""
        var watchLatlonTor = ""
        var mcdLatlon = ""
        var mcdNoList = ""
        var mpdLatlon = ""
        var mpdNoList = ""
        var cancelStr: String
        var noMain: String
        var noBody: String
        var noSummary: String
        var dataAsString: String
        var m: Matcher
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locationNeedsMcd = UtilityNotificationSpc.locationNeedsMcd()
        val locationNeedsSwo = UtilityNotificationSpc.locationNeedsSwo()
        val locationNeedsSpcfw = UtilityNotificationSpcFireWeather.locationNeedsSpcFireWeather()
        val locationNeedsWpcmpd = UtilityNotificationWpc.locationNeedsMpd()
        var requestID: Int
        (1..Location.numLocations).forEach {
            requestID = System.currentTimeMillis().toInt()
            notifUrls += UtilityNotification.sendNotif(context, it.toString(), requestID + 1)
        }
        MyApplication.radarWarningPolygons.forEach {
            if (it.isEnabled) {
                //UtilityLog.d("wx", "Download: " + it.type.urlToken)
                it.storage.valueSet(context, UtilityDownloadRadar.getVtecByType(it.type))
            } else {
                //UtilityLog.d("wx", "DO NOT Download: " + it.type.urlToken)
                it.storage.valueSet(context, "")
            }
        }
        if (MyApplication.alertTornadoNotificationCurrent || MyApplication.checktor || PolygonType.SVR.pref) {
            try {
                // store data for use by severe dashboard and cod warnings
                UtilityDownloadRadar.getPolygonVtec(context)
                if (MyApplication.alertTornadoNotificationCurrent) {
                    notifUrls += UtilityNotificationTornado.checkAndSendTornadoNotification(
                            context,
                            MyApplication.severeDashboardTor.valueGet()
                    )
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardTor.valueSet(context, "")
            MyApplication.severeDashboardSvr.valueSet(context, "")
            MyApplication.severeDashboardEww.valueSet(context, "")
            MyApplication.severeDashboardFfw.valueSet(context, "")
            MyApplication.severeDashboardSmw.valueSet(context, "")
            MyApplication.severeDashboardSvs.valueSet(context, "")
            MyApplication.severeDashboardSps.valueSet(context, "")
        }
        if (MyApplication.alertSpcmcdNotificationCurrent || MyApplication.checkspc || PolygonType.MCD.pref || locationNeedsMcd) {
            try {
                dataAsString = "${MyApplication.nwsSPCwebsitePrefix}/products/md/".getHtml()
                MyApplication.severeDashboardMcd.valueSet(context, dataAsString)
                if (MyApplication.alertSpcmcdNotificationCurrent || PolygonType.MCD.pref || locationNeedsMcd) {
                    // FIXME matcher
                    m = RegExp.mcdPatternAlertr.matcher(dataAsString)
                    var mdNo: String
                    while (m.find()) {
                        mdNo = m.group(1)
                        var mcdPre = UtilityDownload.getTextProduct(context, "SPCMCD$mdNo")
                        if (PolygonType.MCD.pref || locationNeedsMcd) {
                            mcdNoList = "$mcdNoList$mdNo:"
                            mcdLatlon += UtilityNotification.storeWatMcdLatLon(mcdPre)
                        }
                        if (MyApplication.alertSpcmcdNotificationCurrent) {
                            noMain = "SPC MCD #$mdNo"
                            mcdPre = mcdPre.replace("<.*?>".toRegex(), " ")
                            noBody = mcdPre
                            noSummary = mcdPre
                            val polygonType = MCD
                            val objPI = ObjectPendingIntents(
                                    context,
                                    SPCMCDWShowActivity::class.java,
                                    SPCMCDWShowActivity.NO,
                                    arrayOf(mdNo, "", polygonType.toString()),
                                    arrayOf(mdNo, "sound", polygonType.toString())
                            )
                            cancelStr = "usspcmcd$mdNo"
                            if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(
                                            context,
                                            cancelStr
                                    ))
                            ) {
                                val sound =
                                        MyApplication.alertNotificationSoundSpcmcd && !inBlackout
                                val notifObj = ObjectNotification(
                                        context,
                                        sound,
                                        noMain,
                                        noBody,
                                        objPI.resultPendingIntent,
                                        MyApplication.ICON_MCD,
                                        noSummary,
                                        NotificationCompat.PRIORITY_HIGH,
                                        Color.YELLOW,
                                        MyApplication.ICON_ACTION,
                                        objPI.resultPendingIntent2,
                                        context.resources.getString(R.string.read_aloud)
                                )
                                val noti =
                                        UtilityNotification.createNotifBigTextWithAction(notifObj)
                                notifObj.sendNotification(context, cancelStr, 1, noti)
                            }
                            notifUrls += cancelStr + MyApplication.notificationStrSep
                        }
                    } // end while find
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardMcd.valueSet(context, "")
            // end of if to test if alerts_spcmcd are enabled
        }
        if (MyApplication.alertWpcmpdNotificationCurrent || MyApplication.checkwpc || PolygonType.MPD.pref || locationNeedsWpcmpd) {
            try {
                dataAsString =
                        "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
                MyApplication.severeDashboardMpd.valueSet(context, dataAsString)
                if (MyApplication.alertWpcmpdNotificationCurrent || PolygonType.MPD.pref || locationNeedsWpcmpd) {
                    // FIXME matcher
                    m = RegExp.mpdPattern.matcher(dataAsString)
                    var mdNo: String
                    while (m.find()) {
                        mdNo = m.group(1)
                        var mcdPre = UtilityDownload.getTextProduct(context, "WPCMPD$mdNo")
                        if (PolygonType.MPD.pref || locationNeedsWpcmpd) {
                            mpdNoList = "$mpdNoList$mdNo:"
                            mpdLatlon += UtilityNotification.storeWatMcdLatLon(mcdPre)
                        }
                        if (MyApplication.alertWpcmpdNotificationCurrent) {
                            noMain = "WPC MPD #$mdNo"
                            mcdPre = mcdPre.replace("<.*?>".toRegex(), " ")
                            noBody = mcdPre
                            noSummary = mcdPre
                            val polygonType = MPD
                            val objPI = ObjectPendingIntents(
                                    context,
                                    SPCMCDWShowActivity::class.java,
                                    SPCMCDWShowActivity.NO,
                                    arrayOf(mdNo, "", polygonType.toString()),
                                    arrayOf(mdNo, "sound", polygonType.toString())
                            )
                            cancelStr = "uswpcmpd$mdNo"
                            if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(
                                            context,
                                            cancelStr
                                    ))
                            ) {
                                val sound =
                                        MyApplication.alertNotificationSoundWpcmpd && !inBlackout
                                val notifObj = ObjectNotification(
                                        context,
                                        sound,
                                        noMain,
                                        noBody,
                                        objPI.resultPendingIntent,
                                        MyApplication.ICON_MPD,
                                        noSummary,
                                        NotificationCompat.PRIORITY_HIGH,
                                        Color.GREEN,
                                        MyApplication.ICON_ACTION,
                                        objPI.resultPendingIntent2,
                                        context.resources.getString(R.string.read_aloud)
                                )
                                val noti =
                                        UtilityNotification.createNotifBigTextWithAction(notifObj)
                                notifObj.sendNotification(context, cancelStr, 1, noti)
                                //notifier.notify(cancelStr, 1, noti)
                            }
                            notifUrls += cancelStr + MyApplication.notificationStrSep
                        }

                    } // end while find
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        } else {
            MyApplication.severeDashboardMpd.valueSet(context, "")
            // end of if to test if alerts_wpcmpd are enabled
        }
        if (MyApplication.alertSpcwatNotificationCurrent || MyApplication.checkspc || PolygonType.MCD.pref) {
            try {
                dataAsString = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/".getHtml()
                MyApplication.severeDashboardWat.valueSet(context, dataAsString)
                if (MyApplication.alertSpcwatNotificationCurrent || PolygonType.MCD.pref) {
                    // FIXME matcher
                    m = RegExp.watchPattern.matcher(dataAsString)
                    var mdNo: String
                    while (m.find()) {
                        mdNo = m.group(1)
                        mdNo = String.format("%4s", mdNo).replace(' ', '0')
                        var mcdPre = UtilityDownload.getTextProduct(context, "SPCWAT$mdNo")
                        watchNoList = "$watchNoList$mdNo:"
                        val mcdPre2 = UtilityString.getHtmlAndParseLastMatch(
                                "${MyApplication.nwsSPCwebsitePrefix}/products/watch/wou$mdNo.html",
                                RegExp.pre2Pattern
                        )
                        watchLatlonList += UtilityNotification.storeWatMcdLatLon(mcdPre2)
                        if (PolygonType.MCD.pref) {
                            if (mcdPre.contains("Severe Thunderstorm Watch")) {
                                watchLatlon += UtilityNotification.storeWatMcdLatLon(mcdPre2)
                            } else {
                                watchLatlonTor += UtilityNotification.storeWatMcdLatLon(mcdPre2)
                            }
                        }
                        if (MyApplication.alertSpcwatNotificationCurrent) {
                            noMain = "SPC Watch #$mdNo"
                            mcdPre = mcdPre.replace("<.*?>".toRegex(), " ")
                            noBody = mcdPre
                            noSummary = mcdPre
                            val polygonType = WATCH
                            val objPI = ObjectPendingIntents(
                                    context,
                                    SPCMCDWShowActivity::class.java,
                                    SPCMCDWShowActivity.NO,
                                    arrayOf(mdNo, "", polygonType.toString()),
                                    arrayOf(mdNo, "sound", polygonType.toString())
                            )
                            cancelStr = "usspcwat$mdNo"
                            if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(
                                            context,
                                            cancelStr
                                    ))
                            ) {
                                val sound =
                                        MyApplication.alertNotificationSoundSpcwat && !inBlackout
                                val notifObj = ObjectNotification(
                                        context,
                                        sound,
                                        noMain,
                                        noBody,
                                        objPI.resultPendingIntent,
                                        MyApplication.ICON_ALERT_2,
                                        noSummary,
                                        NotificationCompat.PRIORITY_HIGH,
                                        Color.YELLOW,
                                        MyApplication.ICON_ACTION,
                                        objPI.resultPendingIntent2,
                                        context.resources.getString(R.string.read_aloud)
                                )
                                val noti =
                                        UtilityNotification.createNotifBigTextWithAction(notifObj)
                                notifObj.sendNotification(context, cancelStr, 1, noti)
                                //notifier.notify(cancelStr, 1, noti)
                            }
                            notifUrls += cancelStr + MyApplication.notificationStrSep
                        }
                    } // end while find
                }
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
            notifUrls += UtilityNotificationNhc.sendNhcNotifications(
                    context,
                    MyApplication.alertNhcEpacNotificationCurrent,
                    MyApplication.alertNhcAtlNotificationCurrent
            )

        // send 7day and current conditions notifications for locations
        (1..Location.numLocations).forEach {
            requestID = System.currentTimeMillis().toInt()
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
        if (locationNeedsSpcfw) {
            notifUrls += UtilityNotificationSpcFireWeather.sendSpcFireWeatherD12LocationNotifications(context)
        }
        if (locationNeedsWpcmpd) {
            notifUrls += UtilityNotificationWpc.sendMpdLocationNotifications(context)
        }
        if (PolygonType.MCD.pref || locationNeedsMcd) {
            MyApplication.watchNoList.valueSet(context, watchNoList)
            MyApplication.watchLatlonList.valueSet(context, watchLatlonList)
            MyApplication.watchLatlon.valueSet(context, watchLatlon)
            MyApplication.watchLatlonTor.valueSet(context, watchLatlonTor)
            MyApplication.mcdLatlon.valueSet(context, mcdLatlon)
            MyApplication.mcdNoList.valueSet(context, mcdNoList)
        }
        if (PolygonType.MPD.pref || locationNeedsWpcmpd) {
            MyApplication.mpdLatlon.valueSet(context, mpdLatlon)
            MyApplication.mpdNoList.valueSet(context, mpdNoList)
        }
        cancelOldNotifs(notifUrls)
    }

    private fun cancelOldNotifs(notifStr: String) {
        val oldNotifStr = Utility.readPref(context, "NOTIF_STR", "")
        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifArr = MyApplication.comma.split(oldNotifStr)
        notifArr
                .filterNot { notifStr.contains(it) }
                .forEach { notifier.cancel(it, 1) }
        Utility.writePref(context, "NOTIF_STR_OLD", oldNotifStr)
        Utility.writePref(context, "NOTIF_STR", notifStr)
    }

    fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) { doNotifs() }
    }
}
