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
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.util.UtilityLog

internal object NotificationWatch {

    fun send(context: Context): String {
        var notificationUrls = ""
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        if (NotificationPreferences.alertSpcWatchNotification || UIPreferences.checkspc) {
            try {
                val watchData = PolygonWatch.byType[PolygonType.WATCH]!!.getImmediate(context)
                watchData.numberList.forEachIndexed { index, watchNumber ->
                    if (NotificationPreferences.alertSpcWatchNotification) {
                        val label = "(CONUS) SPC Watch #$watchNumber"
                        val text = watchData.htmlList[index].replace("<.*?>".toRegex(), " ")
                        val polygonType = PolygonType.WATCH
                        val objectPendingIntents = ObjectPendingIntents(
                            context,
                            SpcMcdWatchShowActivity::class.java,
                            SpcMcdWatchShowActivity.NUMBER,
                            arrayOf(watchNumber, polygonType.toString(), ""),
                            arrayOf(watchNumber, polygonType.toString(), "sound")
                        )
                        val cancelString = "usspcwat$watchNumber"
                        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                                context,
                                cancelString
                            ))
                        ) {
                            val sound =
                                NotificationPreferences.alertNotificationSoundSpcwat && !inBlackout
                            val objectNotification = ObjectNotification(
                                context,
                                sound,
                                label,
                                text,
                                objectPendingIntents,
                                GlobalVariables.ICON_ALERT_2,
                                GlobalVariables.ICON_ACTION,
                                context.resources.getString(R.string.read_aloud)
                            )
                            objectNotification.send(cancelString)
                        }
                        notificationUrls += cancelString + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        return notificationUrls
    }
}
