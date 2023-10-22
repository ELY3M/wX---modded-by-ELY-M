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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import joshuatee.wx.audio.AudioServiceBack
import joshuatee.wx.audio.AudioServiceForward
import joshuatee.wx.audio.AudioServiceToggleState
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.audio.VoiceCommandActivity
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityString
import android.app.NotificationChannel
import android.graphics.Color
import android.os.Build
import joshuatee.wx.WX
import android.media.AudioAttributes
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parseColumn
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.settings.UIPreferences

object UtilityNotification {

    private var notificationChannelInitialized = false

    // June 2019
    // change NotificationManager.IMPORTANCE_DEFAULT and NotificationManager.IMPORTANCE_LOW to NotificationManager.IMPORTANCE_HIGH
    // in attempt to automatically have notifications in Android Q show up in status bar
    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26 || notificationChannelInitialized) {
            return
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("default", "Channel name", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "wX weather"
        channel.setSound(
                Uri.parse(NotificationPreferences.notifSoundUri), AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
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

    fun createNotificationBigPicture(
            context: Context,
            title: String,
            resultPendingIntent2: PendingIntent,
            iconRadar: Int,
            bitmap: Bitmap
    ): Notification {
        initChannels(context)
        val notification: Notification = NotificationCompat.BigPictureStyle(
                NotificationCompat.Builder(context, notiChannelStrNoSound)
                        .setContentTitle(title)
                        .setSmallIcon(iconRadar)
                        .setSound(null)
                        .setAutoCancel(NotificationPreferences.alertAutocancel)
                        .setColor(UIPreferences.colorNotif)
                        .setLargeIcon(bitmap)
        ).bigPicture(bitmap).build()!!
        notification.flags = notification.flags or Notification.FLAG_ONLY_ALERT_ONCE
        notification.contentIntent = resultPendingIntent2
        return notification
    }

    fun createNotificationBigTextWithAction(objectNotification: ObjectNotification): Notification {
        initChannels(objectNotification.context)
        val onMs = 2000
        val offMs = 4000
        val notification: Notification
        if (objectNotification.sound) {
            notification = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(objectNotification.context, notiChannelStr)
                            .setContentTitle(objectNotification.title)
                            .setContentText(objectNotification.text)
                            .setContentIntent(objectNotification.objectPendingIntents.resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(NotificationPreferences.alertAutocancel)
                            .setColor(UIPreferences.colorNotif)
                            .setSound(Uri.parse(NotificationPreferences.notifSoundUri)) // was Settings.System.DEFAULT_NOTIFICATION_URI
                            .setPriority(objectNotification.priority)
                            .addAction(
                                    objectNotification.iconAction,
                                    objectNotification.buttonStr,
                                    objectNotification.objectPendingIntents.resultPendingIntent2
                            )
                            .setLights(Color.YELLOW, onMs, offMs)
                            .setSmallIcon(objectNotification.iconAlert)
            ).bigText(objectNotification.text).build()!!
            if (NotificationPreferences.notifSoundRepeat) {
                notification.flags = notification.flags or Notification.FLAG_INSISTENT
            }
            if (NotificationPreferences.notifTts) {
                UtilityTts.synthesizeTextAndPlay(objectNotification.context, objectNotification.title, objectNotification.title)
            }
        } else {
            notification = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(objectNotification.context, notiChannelStrNoSound)
                            .setContentTitle(objectNotification.title)
                            .setContentText(objectNotification.text)
                            .setContentIntent(objectNotification.objectPendingIntents.resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setSound(null)
                            .setAutoCancel(NotificationPreferences.alertAutocancel)
                            .setColor(UIPreferences.colorNotif)
                            .setPriority(objectNotification.priority)
                            .addAction(objectNotification.iconAction, objectNotification.buttonStr, objectNotification.objectPendingIntents.resultPendingIntent2)
                            .setLights(Color.YELLOW, onMs, offMs)
                            .setSmallIcon(objectNotification.iconAlert)
            ).bigText(objectNotification.text).build()!!
        }
        return notification
    }

    fun createMediaControlNotification(context: Context, titleF: String) {
        initChannels(context)
        var title = titleF
        if (title == "")
            title = NotificationPreferences.mediaNotifTtsTitle
        else
            NotificationPreferences.mediaNotifTtsTitle = title
        val pauseIcon =
                if (UtilityTts.mediaPlayer != null && !UtilityTts.mediaPlayer!!.isPlaying)
                    GlobalVariables.ICON_PAUSE_PRESSED
                else
                    GlobalVariables.ICON_PAUSE
        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification
        val resultIntent = Intent(context, VoiceCommandActivity::class.java)
        val resultIntent2 = Intent(context, AudioServiceToggleState::class.java)
        val resultIntentBack = Intent(context, AudioServiceBack::class.java)
        val resultIntentForward = Intent(context, AudioServiceForward::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(WX::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val requestID = ObjectDateTime.currentTimeMillis().toInt()
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
        val actionBack = NotificationCompat.Action.Builder(GlobalVariables.ICON_SKIP_BACK, "", resultPendingIntentBack).build()
        val actionPlay = NotificationCompat.Action.Builder(pauseIcon, "", resultPendingIntent2).build()
        val actionForward = NotificationCompat.Action.Builder(GlobalVariables.ICON_SKIP_FORWARD, "", resultPendingIntentForward).build()
        val style = MediaStyle().setShowActionsInCompactView(0, 1, 2)
        notification = NotificationCompat.Builder(context, notiChannelStrNoSound)
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
                .setSmallIcon(GlobalVariables.ICON_MIC)
                .build()
        notifier.notify("wx_media", 1, notification)
    }

    // this is used primary by the current conditions notification
    // bug: https://code.google.com/p/android/issues/detail?id=43179&q=setSmallIcon&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars
    // pull down small icon taking default level
    fun createNotificationBigTextBigIcon(
            context: Context,
            sound: Boolean,
            title: String,
            text: String,
            resultPendingIntent: PendingIntent,
            smallIcon: Int,
            iconAlert: Bitmap,
            noSummary: String,
            prio: Int
    ): Notification {
        initChannels(context)
        val height = context.resources.getDimension(android.R.dimen.notification_large_icon_height).toInt()
        val width = context.resources.getDimension(android.R.dimen.notification_large_icon_width).toInt()
        val bitmap = Bitmap.createScaledBitmap(iconAlert, width, height, false)
        val notification: Notification
        if (sound) {
            notification = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(context, notiChannelStr)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setContentIntent(resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(false)
                            .setColor(UIPreferences.colorNotif)
                            .setSound(Uri.parse(NotificationPreferences.notifSoundUri))
                            .setPriority(prio)
                            .setSmallIcon(smallIcon)
                            .setLargeIcon(bitmap)
            ).bigText(noSummary).build()!!
            if (NotificationPreferences.notifSoundRepeat)
                notification.flags = notification.flags or Notification.FLAG_INSISTENT
            if (NotificationPreferences.notifTts)
                UtilityTts.synthesizeTextAndPlay(context, title, title)
        } else {
            notification = NotificationCompat.BigTextStyle(
                    NotificationCompat.Builder(context, notiChannelStrNoSound)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setSound(null)
                            .setColor(UIPreferences.colorNotif)
                            .setContentIntent(resultPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setAutoCancel(false)
                            .setPriority(prio)
                            .setSmallIcon(smallIcon)
                            .setLargeIcon(bitmap)
            ).bigText(noSummary).build()!!
        }
        return notification
    }

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
            var tmpDbl = To.double(yStrTmp)
            if (tmpDbl < 40.00) {
                tmpDbl += 100.0
                yStrTmp = tmpDbl.toString()
            }
            string = "$string$xStrTmp $yStrTmp "
        }
        string += ":"
        string = string.replace(" :", ":")
        return string
    }
}
