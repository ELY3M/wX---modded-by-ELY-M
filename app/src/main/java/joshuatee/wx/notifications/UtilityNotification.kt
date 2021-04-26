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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle

import joshuatee.wx.activitiesmisc.HourlyActivity
import joshuatee.wx.audio.AudioServiceBack
import joshuatee.wx.audio.AudioServiceForward
import joshuatee.wx.audio.AudioServiceToggleState
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.fragments.UtilityNws
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.audio.VoiceCommandActivity
import joshuatee.wx.canada.CanadaHourlyActivity
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.canada.UtilityCanadaImg
import joshuatee.wx.settings.Location
import joshuatee.wx.util.*
import android.app.NotificationChannel
import android.os.Build
import joshuatee.wx.*

import joshuatee.wx.Extensions.*
import android.media.AudioAttributes
import androidx.core.app.NotificationManagerCompat

object UtilityNotification {

    // FIXME lots of variable naming improvement opportunities

    private var notificationChannelInitialized = false

    internal fun send(context: Context, locNum: String, y: Int): String {
        val locNumInt = (locNum.toIntOrNull() ?: 1) - 1
        var notifUrls = ""
        val oldNotifStr = Utility.readPref(context, "NOTIF_STR", "")
        val noSummary = ""
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val tornadoWarningString = "Tornado Warning"
        if (MyApplication.locations.size > locNumInt && MyApplication.locations[locNumInt].notification) {
            //if (Location.getName(locNumInt).contains("ROAMING"))
            //    UtilityLocation.checkRoamingLocation(context, locNum, Location.getX(locNumInt), Location.getY(locNumInt))
            var locLabelStr = "(" + Location.getName(locNumInt) + ") "
            var alertPresent = false
            if (Location.isUS(locNumInt)) {
                val oldnotifUrls = notifUrls
                notifUrls += UtilityUS.checkForNotifications(context, locNumInt, inBlackout, tornadoWarningString)
                if (oldnotifUrls != notifUrls) alertPresent = true
            } else {
                val html = UtilityCanada.getLocationHtml(Location.getLatLon(locNumInt))
                val hazArr = UtilityCanada.getHazards(html)
                val hazSum = Utility.fromHtml(hazArr[0])
                val hazUrls = hazArr[1]
                val noMain = locLabelStr + hazSum
                if (hazSum != "" && !hazSum.contains("No watches or warnings in effect")) {
                    alertPresent = true
                    val objectPendingIntents = ObjectPendingIntents(
                            context, TextScreenActivity::class.java, TextScreenActivity.URL,
                            arrayOf(hazUrls, hazSum),
                            arrayOf(hazUrls, hazSum, "sound")
                    )
                    val cancelStr = Location.getY(locNumInt) + hazUrls.parse("(</h2> <p>.*?</strong> </p>)").replace(",".toRegex(), "").replace(" ".toRegex(), "")
                    if (!(MyApplication.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
                        val sound = MyApplication.locations[locNumInt].sound && !inBlackout || MyApplication.locations[locNumInt].sound && MyApplication.alertBlackoutTornado
                        val notifObj = ObjectNotification(
                                context,
                                sound,
                                noMain,
                                Utility.fromHtml(hazUrls),
                                objectPendingIntents.resultPendingIntent,
                                MyApplication.ICON_ALERT,
                                noSummary,
                                NotificationCompat.PRIORITY_HIGH,
                                Color.BLUE,
                                MyApplication.ICON_ACTION,
                                objectPendingIntents.resultPendingIntent2,
                                context.resources.getString(R.string.read_aloud)
                        )
                        val notification = createNotificationBigTextWithAction(notifObj)
                        notifObj.sendNotification(context, cancelStr, 1, notification)
                    }
                    notifUrls += cancelStr + MyApplication.notificationStrSep
                }
            }
            if (alertPresent && MyApplication.locations[locNumInt].notificationRadar) {
                val url2: String
                var nws1StateCurrent = ""
                if (Location.isUS(locNumInt)) {
                    val nwsLocation = Utility.getWfoSiteName(MyApplication.locations[locNumInt].wfo)
                    val nwsLocationArr = MyApplication.comma.split(nwsLocation)
                    nws1StateCurrent = nwsLocationArr[0]
                }
                val bitmap: Bitmap
                if (Location.isUS(locNumInt)) {
                    url2 = Location.getRid(locNumInt) + "US"
                    bitmap = UtilityUSImg.getPreferredLayeredImg(context, Location.getRid(locNumInt), false)
                } else {
                    url2 = Location.getRid(locNumInt) + "CA"
                    bitmap = UtilityCanadaImg.getRadarBitmapOptionsApplied(context, Location.getRid(locNumInt), "")
                }
                locLabelStr = "(" + Location.getName(locNumInt) + ") " + Location.getRid(locNumInt) + " Radar"
                val noMain = locLabelStr
                val notifier2 = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val noti2: Notification
                val resultIntent2: Intent
                if (Location.isUS(locNumInt)) {
                    resultIntent2 = Intent(context, WXGLRadarActivity::class.java)
                    resultIntent2.putExtra(WXGLRadarActivity.RID, arrayOf(Location.getRid(locNumInt), nws1StateCurrent))
                } else {
                    resultIntent2 = Intent(context, CanadaRadarActivity::class.java)
                    resultIntent2.putExtra(CanadaRadarActivity.RID, arrayOf(Location.getRid(locNumInt), "rad"))
                }
                val stackBuilder2 = TaskStackBuilder.create(context)
                if (Location.isUS(locNumInt)) {
                    stackBuilder2.addParentStack(WX::class.java)
                } else {
                    stackBuilder2.addParentStack(CanadaRadarActivity::class.java)
                }
                stackBuilder2.addNextIntent(resultIntent2)
                val resultPendingIntent2 = stackBuilder2.getPendingIntent(y, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (!(MyApplication.alertOnlyOnce && oldNotifStr.contains(url2 + "radar"))) {
                    noti2 = createNotificationBigPicture(context, noMain, resultPendingIntent2, MyApplication.ICON_RADAR, bitmap)
                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        notifier2.notify(url2 + "radar", 1, noti2)
                    }
                }
                notifUrls += url2 + "radar" + MyApplication.notificationStrSep
            } // end if for radar
        } // end of if to test if alerts1 are enabled
        return notifUrls
    }

    internal fun sendNotificationCurrentConditions(context: Context, locNum: String, x: Int, y: Int): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        var notifUrls = ""
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val widgetsEnabled = Utility.readPref(context, "WIDGETS_ENABLED", "false").startsWith("t")
        val ccUpdateInterval = Utility.readPref(context, "CC_NOTIFICATION_INTERVAL", 30)
        val locLabel: String
        var noMain: String
        var noBody: String
        var noSummary: String
        var locLabelStr: String
        if (MyApplication.locations.size > locNumInt && (MyApplication.locations[locNumInt].ccNotification
                        || MyApplication.locations[locNumInt].sevenDayNotification
                        || widgetLocNum == locNum && widgetsEnabled)
        ) {
            locLabel = " current conditions"
            locLabelStr = "(" + Location.getName(locNumInt) + ")" + locLabel
            //val url = UtilityDownloadNws.get7DayUrl(Location.getLatLon(locNumInt))
            val url = Location.getIdentifier(locNumInt)
            // url above is used as the token for notifications and currenlty looks like
            // https://api.weather.gov/gridpoints/DTX/x,y/forecast
            // problem is if network is down it will be a non deterministic value so we need something different
            val currentUpdateTime = UtilityTime.currentTimeMillis()
            val lastUpdateTime = Utility.readPref(context, "CC" + locNum + "_LAST_UPDATE", 0.toLong())
            if (MyApplication.locations[locNumInt].ccNotification) notifUrls += url + "CC" + MyApplication.notificationStrSep
            if (MyApplication.locations[locNumInt].sevenDayNotification) notifUrls += url + "7day" + MyApplication.notificationStrSep
            if (currentUpdateTime > lastUpdateTime + 1000 * 60 * ccUpdateInterval) {
                val objCc = ObjectCurrentConditions(context, locNumInt)
                val objHazards = ObjectHazards(locNumInt)
                val objSevenDay = ObjectSevenDay(locNumInt)
                val updateTime = UtilityTime.currentTimeMillis()
                Utility.writePref(context, "CC" + locNum + "_LAST_UPDATE", updateTime)
                if (locNum == widgetLocNum && widgetsEnabled) UtilityWidget.widgetDownloadData(context, objCc, objSevenDay, objHazards)
                if (MyApplication.locations[locNumInt].ccNotification) {
                    noMain = locLabelStr
                    noBody = objCc.data + MyApplication.newline + objCc.status
                    noSummary = objCc.data + MyApplication.newline + objCc.status
                    val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val resultIntent: Intent
                    if (Location.isUS(locNumInt)) {
                        resultIntent = Intent(context, HourlyActivity::class.java)
                        resultIntent.putExtra(HourlyActivity.LOC_NUM, locNum)
                    } else {
                        resultIntent = Intent(context, CanadaHourlyActivity::class.java)
                        resultIntent.putExtra(CanadaHourlyActivity.LOC_NUM, locNum)
                    }
                    val stackBuilder = TaskStackBuilder.create(context)
                    if (Location.isUS(locNumInt))
                        stackBuilder.addParentStack(HourlyActivity::class.java)
                    else
                        stackBuilder.addParentStack(CanadaHourlyActivity::class.java)
                    stackBuilder.addNextIntent(resultIntent)
                    val resultPendingIntent = stackBuilder.getPendingIntent(x, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val tmpArr = noBody.split(MyApplication.DEGREE_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }
                    var smallIconResource = R.drawable.temp_0
                    if (tmpArr.isNotEmpty()) {
                        smallIconResource = if (Location.isUS(locNumInt)) {
                            UtilityTempIcon.getTempIcon(tmpArr[0])
                        } else {
                            val d = tmpArr[0].toDoubleOrNull() ?: 0.0
                            val ii = d.toInt()
                            UtilityTempIcon.getTempIcon(ii.toString())
                        }
                    }
                    val bmc = if (Location.isUS(locNumInt)) {
                        UtilityNws.getIcon(context, objCc.iconUrl)
                    } else {
                        UtilityNws.getIcon(context, UtilityCanada.translateIconNameCurrentConditions(objCc.data, objCc.status))
                    }
                    val noti = createNotificationBigTextBigIcon(
                            context,
                            false,
                            noMain,
                            noBody,
                            resultPendingIntent,
                            smallIconResource,
                            bmc,
                            noSummary,
                            NotificationCompat.PRIORITY_HIGH
                    )
                    notifier.notify(url + "CC", 1, noti)
                }
                if (MyApplication.locations[locNumInt].sevenDayNotification) {
                    locLabelStr = "(" + Location.getName(locNumInt) + ")" + " 7 day"
                    noMain = locLabelStr
                    noBody = objSevenDay.sevenDayShort
                    noSummary = objSevenDay.sevenDayShort
                    val resultIntent2 = Intent(context, TextScreenActivity::class.java)
                    resultIntent2.putExtra(TextScreenActivity.URL, arrayOf(objSevenDay.sevenDayLong, locLabelStr))
                    val stackBuilder2 = TaskStackBuilder.create(context)
                    stackBuilder2.addParentStack(TextScreenActivity::class.java)
                    stackBuilder2.addNextIntent(resultIntent2)
                    val resultPendingIntent2 = stackBuilder2.getPendingIntent(y, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val objectPendingIntents = ObjectPendingIntents(
                            context,
                            TextScreenActivity::class.java,
                            TextScreenActivity.URL,
                            arrayOf(objSevenDay.sevenDayLong, locLabelStr),
                            arrayOf(objSevenDay.sevenDayLong, locLabelStr, "sound")
                    )
                    objectPendingIntents.resultPendingIntent = resultPendingIntent2
                    val notifObj = ObjectNotification(
                            context,
                            false,
                            noMain,
                            noBody,
                            objectPendingIntents.resultPendingIntent,
                            MyApplication.ICON_FORECAST,
                            noSummary,
                            Notification.PRIORITY_MIN,
                            Color.YELLOW,
                            MyApplication.ICON_ACTION,
                            objectPendingIntents.resultPendingIntent2,
                            "7 Day Forecast"
                    )
                    val noti2 = createNotificationBigTextWithAction(notifObj)
                    notifObj.sendNotification(context, url + "7day", 1, noti2)
                } // end 7 day
            } // end if current time
        } // end if alert on cc1
        return notifUrls
    }

    // June 2019
    // change NotificationManager.IMPORTANCE_DEFAULT and NotificationManager.IMPORTANCE_LOW to NotificationManager.IMPORTANCE_HIGH
    // in attempt to automatically have notifications in Android Q show up in status bar
    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26 || notificationChannelInitialized) return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("default", "Channel name", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "wX weather"
        channel.setSound(
                Uri.parse(MyApplication.notifSoundUri), AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()
        )
        notificationManager.createNotificationChannel(channel)
        val channelNoSound = NotificationChannel(notiChannelStrNoSound, "wX No Sound", NotificationManager.IMPORTANCE_HIGH)
        channelNoSound.description = "wX weather no sound"
        channelNoSound.setSound(null, null)
        notificationManager.createNotificationChannel(channelNoSound)
        notificationChannelInitialized = true
    }

    private const val notiChannelStr = "default"
    const val notiChannelStrNoSound = "defaultNoSound2"

    private fun createNotificationBigPicture(context: Context, noMain: String, resultPendingIntent2: PendingIntent, iconRadar: Int, bitmap: Bitmap): Notification {
        initChannels(context)
        val noti2: Notification = NotificationCompat.BigPictureStyle(
                NotificationCompat.Builder(context, notiChannelStrNoSound)
                        .setContentTitle(noMain)
                        .setSmallIcon(iconRadar)
                        .setSound(null)
                        .setAutoCancel(MyApplication.alertAutocancel)
                        .setColor(UIPreferences.colorNotif)
                        .setLargeIcon(bitmap)
        ).bigPicture(bitmap).build()
        noti2.flags = noti2.flags or Notification.FLAG_ONLY_ALERT_ONCE
        noti2.contentIntent = resultPendingIntent2
        return noti2
    }

    fun createNotificationBigTextWithAction(notification: ObjectNotification): Notification {
        initChannels(notification.context)
        val onMs = 2000
        val offMs = 4000
        val noti: Notification
        if (notification.sound) {
            noti = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(notification.context, notiChannelStr)
                            .setContentTitle(notification.noMain)
                            .setContentText(notification.noBody)
                            .setContentIntent(notification.resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(MyApplication.alertAutocancel)
                            .setColor(UIPreferences.colorNotif)
                            .setSound(Uri.parse(MyApplication.notifSoundUri)) // was Settings.System.DEFAULT_NOTIFICATION_URI
                            .setPriority(notification.priority)
                            .addAction(
                                    notification.iconAction,
                                    notification.buttonStr,
                                    notification.actionPendingIntent
                            )
                            .setLights(notification.color, onMs, offMs)
                            .setSmallIcon(notification.iconAlert)
            ).bigText(notification.noSummary).build()
            if (MyApplication.notifSoundRepeat) noti.flags = noti.flags or Notification.FLAG_INSISTENT
            if (MyApplication.notifTts) UtilityTts.synthesizeTextAndPlay(notification.context, notification.noMain, notification.noMain)
        } else {
            noti = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(notification.context, notiChannelStrNoSound)
                            .setContentTitle(notification.noMain)
                            .setContentText(notification.noBody)
                            .setContentIntent(notification.resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setSound(null)
                            .setAutoCancel(MyApplication.alertAutocancel)
                            .setColor(UIPreferences.colorNotif)
                            .setPriority(notification.priority)
                            .addAction(notification.iconAction, notification.buttonStr, notification.actionPendingIntent)
                            .setLights(notification.color, onMs, offMs)
                            .setSmallIcon(notification.iconAlert)
            ).bigText(notification.noSummary).build()
        }
        return noti
    }

    fun createMediaControlNotification(context: Context, titleF: String) {
        initChannels(context)
        var title = titleF
        if (title == "")
            title = MyApplication.mediaNotifTtsTitle
        else
            MyApplication.mediaNotifTtsTitle = title
        val pauseIcon =
                if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying)
                    MyApplication.ICON_PAUSE_PRESSED
                else
                    MyApplication.ICON_PAUSE
        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val noti: Notification
        val resultIntent = Intent(context, VoiceCommandActivity::class.java)
        val resultIntent2 = Intent(context, AudioServiceToggleState::class.java)
        val resultIntentBack = Intent(context, AudioServiceBack::class.java)
        val resultIntentForward = Intent(context, AudioServiceForward::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(WX::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val requestID = UtilityTime.currentTimeMillis().toInt()
        val resultPendingIntent = stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val resultPendingIntent2 = PendingIntent.getService(context, requestID + 1, resultIntent2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val resultPendingIntentBack = PendingIntent.getService(context, requestID + 4, resultIntentBack, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val resultPendingIntentForward = PendingIntent.getService(context, requestID + 5, resultIntentForward, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notifCurrent = "true"
        val txt = if (notifCurrent.startsWith("t")) {
            val tabStr = UtilitySpc.checkSpc()
            "" + tabStr[0] + " " + tabStr[1].replace("MISC", "")
        } else {
            ""
        }
        val actionBack = NotificationCompat.Action.Builder(MyApplication.ICON_SKIP_BACK, "", resultPendingIntentBack).build()
        val actionPlay = NotificationCompat.Action.Builder(pauseIcon, "", resultPendingIntent2).build()
        val actionForward = NotificationCompat.Action.Builder(MyApplication.ICON_SKIP_FORWARD, "", resultPendingIntentForward).build()
        val style = MediaStyle().setShowActionsInCompactView(0, 1, 2)
        noti = NotificationCompat.Builder(context, notiChannelStrNoSound)
                .setContentTitle("wX $title")
                .setStyle(style)
                .setShowWhen(false)
                .setColor(UIPreferences.colorNotif)
                .setContentText(txt)
                .setContentIntent(resultPendingIntent)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(actionBack)
                .addAction(actionPlay)
                .addAction(actionForward)
                .setSmallIcon(MyApplication.ICON_MIC)
                .build()
        notifier.notify("wx_media", 1, noti)
    }

    // this is used primary by the current conditions notification
    // bug: https://code.google.com/p/android/issues/detail?id=43179&q=setSmallIcon&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
    // pulldown small icon taking default level

    private fun createNotificationBigTextBigIcon(context: Context, sound: Boolean, noMain: String, noBody: String, resultPendingIntent: PendingIntent, smallIcon: Int,
            iconAlert: Bitmap, noSummary: String, prio: Int): Notification {
        initChannels(context)
        val height = context.resources.getDimension(android.R.dimen.notification_large_icon_height).toInt()
        val width = context.resources.getDimension(android.R.dimen.notification_large_icon_width).toInt()
        val bitmap = Bitmap.createScaledBitmap(iconAlert, width, height, false)
        val noti: Notification
        if (sound) {
            noti = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(context, notiChannelStr)
                            .setContentTitle(noMain)
                            .setContentText(noBody)
                            .setContentIntent(resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(false)
                            .setColor(UIPreferences.colorNotif)
                            .setSound(Uri.parse(MyApplication.notifSoundUri))
                            .setPriority(prio)
                            .setSmallIcon(smallIcon)
                            .setLargeIcon(bitmap)
            ).bigText(noSummary).build()
            if (MyApplication.notifSoundRepeat)
                noti.flags = noti.flags or Notification.FLAG_INSISTENT
            if (MyApplication.notifTts)
                UtilityTts.synthesizeTextAndPlay(context, noMain, noMain)
        } else {
            noti = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(context, notiChannelStrNoSound)
                            .setContentTitle(noMain)
                            .setContentText(noBody)
                            .setSound(null)
                            .setColor(UIPreferences.colorNotif)
                            .setContentIntent(resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(false)
                            .setPriority(prio)
                            .setSmallIcon(smallIcon)
                            .setLargeIcon(bitmap)
            ).bigText(noSummary).build()
        }
        return noti
    }

    // TODO Add method to LatLon class
    internal fun storeWatchMcdLatLon(html: String): String {
        val coordinates = html.parseColumn("([0-9]{8}).*?")
        var string = ""
        coordinates.forEach { temp ->
            var xStrTmp = temp.substring(0, 4)
            var yStrTmp = temp.substring(4, 8)
            if (yStrTmp.matches("^0".toRegex())) {
                yStrTmp = yStrTmp.replace("^0".toRegex(), "")
                yStrTmp += "0"
            }
            xStrTmp = UtilityString.addPeriodBeforeLastTwoChars(xStrTmp)
            yStrTmp = UtilityString.addPeriodBeforeLastTwoChars(yStrTmp)
            try {
                var tmpDbl = yStrTmp.toDoubleOrNull() ?: 0.0
                if (tmpDbl < 40.00) {
                    tmpDbl += 100
                    yStrTmp = tmpDbl.toString()
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            string = "$string$xStrTmp $yStrTmp "
        }
        string += ":"
        string = string.replace(" :", ":")
        return string
    }
}
