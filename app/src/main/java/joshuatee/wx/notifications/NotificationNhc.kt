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

import android.content.Context
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.nhc.NhcStormActivity
import joshuatee.wx.nhc.Nhc
import joshuatee.wx.nhc.NhcStormDetails
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

object NotificationNhc {

    fun muteNotification(context: Context, title: String) {
        var muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        if (!muteStr.contains(title)) {
            muteStr += ":$title"
            Utility.writePref(context, "NOTIF_NHC_MUTE", muteStr)
        }
    }

    internal fun send(context: Context): String {
        var notificationUrls = ""
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        val storms = Nhc.getTextData(context)
        if (NotificationPreferences.alertNhcEpacNotification) {
            storms.forEach {
                if (it.stormId.startsWith("ep")) {
                    if (!muteStr.contains(it.stormId)) {
                        notificationUrls += sendNotification(context, NotificationPreferences.alertNotificationSoundNhcAtl, it)
                    } else {
                        UtilityLog.d("wx", "blocking " + it.stormId)
                    }
                }
            }
        }
        if (NotificationPreferences.alertNhcAtlNotification) {
            storms.forEach {
                if (it.stormId.startsWith("al")) {
                    if (!muteStr.contains(it.stormId)) {
                        notificationUrls += sendNotification(context, NotificationPreferences.alertNotificationSoundNhcAtl, it)
                    } else {
                        UtilityLog.d("wx", "blocking " + it.stormId)
                    }
                }
            }
        }
        return notificationUrls
    }

    private fun sendNotification(context: Context, soundPref: Boolean, stormData: NhcStormDetails): String {
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val objectPendingIntents = ObjectPendingIntents(context, NhcStormActivity::class.java, NhcStormActivity.URL, stormData)
        val cancelString = stormData.stormId + stormData.advisoryIssuanceNumber
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = soundPref && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    stormData.stormId,
                    stormData.summaryForNotification(),
                    objectPendingIntents,
                    GlobalVariables.ICON_NHC_1,
                    GlobalVariables.ICON_ACTION,
                    context.resources.getString(R.string.read_aloud)
            )
            objectNotification.send(cancelString)
        }
        return cancelString + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
    }
}
