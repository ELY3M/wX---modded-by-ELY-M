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
import android.graphics.Color
import androidx.core.app.NotificationCompat
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.nhc.NhcStormActivity
import joshuatee.wx.nhc.ObjectNhcStormDetails
import joshuatee.wx.nhc.UtilityNhc
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

object UtilityNotificationNhc {

    fun muteNotification(context: Context, title: String) {
        var muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        if (!muteStr.contains(title)) {
            muteStr += ":$title"
            Utility.writePref(context, "NOTIF_NHC_MUTE", muteStr)
        }
    }

    internal fun send(context: Context, epac: Boolean, atl: Boolean): String {
        var notifUrls = ""
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        val storms = UtilityNhc.getHurricaneInfo()

        if (epac) {
            storms.forEach {
                if (it.id.startsWith("ep")) {
                    if (!muteStr.contains(it.id)) {
                        notifUrls += sendNotification(context, NotificationPreferences.alertNotificationSoundNhcAtl, it)
                    } else {
                        UtilityLog.d("wx", "blocking " + it.id)
                    }
                }
            }
        }

        if (atl) {
            storms.forEach {
                if (it.id.startsWith("al")) {
                    if (!muteStr.contains(it.id)) {
                        notifUrls += sendNotification(context, NotificationPreferences.alertNotificationSoundNhcAtl, it)
                    } else {
                        UtilityLog.d("wx", "blocking " + it.id)
                    }
                }
            }
        }

//        if (epac || atl) {
//            storms.forEach {
//                if (!muteStr.contains(it.id)) {
//                    notifUrls += sendNotification(context, MyApplication.alertNotificationSoundNhcAtl, it)
//                } else {
//                    UtilityLog.d("wx", "blocking " + it.id)
//                }
//            }
//        }

        return notifUrls
    }

    private fun sendNotification(context: Context, soundPref: Boolean, stormData: ObjectNhcStormDetails): String {
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val objPI = ObjectPendingIntents(context, NhcStormActivity::class.java, NhcStormActivity.URL, stormData)
        val cancelString = stormData.id + stormData.dateTime
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = soundPref && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    stormData.id,
                    stormData.summaryForNotification(),
                    objPI.resultPendingIntent,
                    GlobalVariables.ICON_NHC_1,
                    stormData.summaryForNotification(),
                    NotificationCompat.PRIORITY_HIGH,
                    Color.YELLOW,
                    GlobalVariables.ICON_ACTION,
                    objPI.resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelString, 1, notification)
        }
        return cancelString + NotificationPreferences.notificationStrSep
    }
}
