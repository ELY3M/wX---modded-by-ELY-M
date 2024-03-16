/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.NotificationCompat
import joshuatee.wx.audio.UtilityTts
import android.app.NotificationChannel
import android.graphics.Color
import android.os.Build
import android.media.AudioAttributes
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
                .build()
        )
        notificationManager.createNotificationChannel(channel)
        val channelNoSound = NotificationChannel(NOTIFICATION_CHANNEL_STRING_NO_SOUND, "wX No Sound", NotificationManager.IMPORTANCE_HIGH)
        channelNoSound.description = "wX weather no sound"
        channelNoSound.setSound(null, null)
        notificationManager.createNotificationChannel(channelNoSound)
        notificationChannelInitialized = true
    }

    private const val NOTIFICATION_CHANNEL_STRING = "default"
    const val NOTIFICATION_CHANNEL_STRING_NO_SOUND = "defaultNoSound2"

    fun createNotificationBigPicture(
            context: Context,
            title: String,
            resultPendingIntent2: PendingIntent,
            iconRadar: Int,
            bitmap: Bitmap
    ): Notification {
        initChannels(context)
        val notification: Notification = NotificationCompat.BigPictureStyle(
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_STRING_NO_SOUND)
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
                    NotificationCompat.Builder(objectNotification.context, NOTIFICATION_CHANNEL_STRING)
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
                    NotificationCompat.Builder(objectNotification.context, NOTIFICATION_CHANNEL_STRING_NO_SOUND)
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
                    NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_STRING)
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
                    NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_STRING_NO_SOUND)
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
}
