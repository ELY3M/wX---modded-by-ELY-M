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

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.util.CurrentConditions
import joshuatee.wx.util.Hazards
import joshuatee.wx.util.SevenDay
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityForecastIcon
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.R
import joshuatee.wx.WX
import androidx.core.app.NotificationManagerCompat
import joshuatee.wx.misc.CapAlert
import joshuatee.wx.misc.HourlyActivity
import joshuatee.wx.misc.ForecastActivity
import joshuatee.wx.misc.AlertsDetailActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parseColumn
import joshuatee.wx.settings.NotificationPreferences

object NotificationLocal {

    //
    // notifications - location based alerts and location radar
    //
    internal fun send(context: Context, locNum: String, y: Int): String {
        val locationIndex = (locNum.toIntOrNull() ?: 1) - 1
        var notificationUrls = ""
        val oldNotifStr = Utility.readPref(context, "NOTIF_STR", "")
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        //
        // if locations are enabled for a valid location
        //
        if (Location.locations.size > locationIndex && Location.locations[locationIndex].notification) {
            var alertPresent = false
            if (Location.isUS(locationIndex)) {
                //
                // US notification
                //
                val oldnotifUrls = notificationUrls
                notificationUrls += checkForNotifications(context, locationIndex, inBlackout)
                if (oldnotifUrls != notificationUrls) {
                    alertPresent = true
                }
            }
            //
            // if an alert is present and user wants a notification with a mini radar image
            // Canada is no longer supported
            //
            if (alertPresent && Location.locations[locationIndex].notificationRadar && Location.isUS(
                    locationIndex
                )
            ) {
                val url2 = Location.getRid(locationIndex) + "US"
                val bitmap = UtilityImg.getNexradRefBitmap(context, Location.getRid(locationIndex))
                val title =
                    "(" + Location.getName(locationIndex) + ") " + Location.getRid(locationIndex) + " Radar"
                val notifier2 =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val resultIntent2 = Intent(context, WXGLRadarActivity::class.java)
                resultIntent2.putExtra(
                    WXGLRadarActivity.RID,
                    arrayOf(Location.getRid(locationIndex), "STATE NOT USED")
                )
                val stackBuilder2 = TaskStackBuilder.create(context)
                stackBuilder2.addParentStack(WX::class.java)
                stackBuilder2.addNextIntent(resultIntent2)
                val resultPendingIntent2 = stackBuilder2.getPendingIntent(
                    y,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (!(NotificationPreferences.alertOnlyOnce && oldNotifStr.contains(url2 + "radar"))) {
                    val noti2 = UtilityNotification.createNotificationBigPicture(
                        context,
                        title,
                        resultPendingIntent2,
                        GlobalVariables.ICON_RADAR_WHITE,
                        bitmap
                    )
                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        notifier2.notify(url2 + "radar", 1, noti2)
                    }
                }
                notificationUrls += url2 + "radar" + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
            } // end if for radar
        } // end of if to test if alerts are enabled
        return notificationUrls
    }

    //
    // send notifications for current conditions or seven day if configured
    //    not sent as frequently as other notifications
    //
    internal fun sendCurrentConditionsAnd7Day(context: Context, locNum: String, x: Int): String {
        val locationIndex = To.int(locNum) - 1
        var notificationUrls = ""
        val ccUpdateInterval = Utility.readPrefInt(context, "CC_NOTIFICATION_INTERVAL", 30)
        if (Location.locations.size > locationIndex && (Location.locations[locationIndex].ccNotification || Location.locations[locationIndex].sevenDayNotification)) {
            val url = Location.getIdentifier(locationIndex)
            // url above is used as the token for notifications and currently looks like
            // https://api.weather.gov/gridpoints/DTX/x,y/forecast
            // problem is if network is down it will be a non deterministic value so we need something different
            val currentUpdateTime = ObjectDateTime.currentTimeMillis()
            val lastUpdateTime =
                Utility.readPrefLong(context, "CC" + locNum + "_LAST_UPDATE", 0.toLong())
            if (Location.locations[locationIndex].ccNotification) {
                notificationUrls += url + "CC" + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
            }
            if (Location.locations[locationIndex].sevenDayNotification) {
                notificationUrls += url + "7day" + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
            }
            if (currentUpdateTime > lastUpdateTime + 1000 * 60 * ccUpdateInterval) {
                Utility.writePrefLong(
                    context,
                    "CC" + locNum + "_LAST_UPDATE",
                    ObjectDateTime.currentTimeMillis()
                )
                sendCurrentConditions(context, locationIndex, locNum, url, x)
                send7Day(context, locationIndex, url)
            } // end if current time
        } // end if alert on cc1
        return notificationUrls
    }

    private fun sendCurrentConditions(
        context: Context,
        locationIndex: Int,
        locNum: String,
        url: String,
        x: Int
    ) {
        if (Location.locations[locationIndex].ccNotification) {
            val locLabel = " current conditions"
            val label = "(" + Location.getName(locationIndex) + ")" + locLabel
            val currentConditions = CurrentConditions(locationIndex)
            currentConditions.timeCheck()
            val text = currentConditions.data + GlobalVariables.newline + currentConditions.status
            val notifier =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val resultIntent: Intent
            if (Location.isUS(locationIndex)) {
                resultIntent = Intent(context, HourlyActivity::class.java)
                resultIntent.putExtra(HourlyActivity.LOC_NUM, locNum)
            } else {
                resultIntent = Intent(context, HourlyActivity::class.java)
            }
            val stackBuilder = TaskStackBuilder.create(context)
            if (Location.isUS(locationIndex))
                stackBuilder.addParentStack(HourlyActivity::class.java)
            else
                stackBuilder.addParentStack(HourlyActivity::class.java)
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                x,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            //
            // determine temp icon to show
            //
            val tmpArr =
                text.split(GlobalVariables.DEGREE_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }
            var smallIconResource = R.drawable.temp_0
            if (tmpArr.isNotEmpty()) {
                smallIconResource = if (Location.isUS(locationIndex)) {
                    UtilityTempIcon.getTempIcon(tmpArr[0])
                } else {
                    val d = To.double(tmpArr[0])
                    UtilityTempIcon.getTempIcon(d.toInt().toString())
                }
            }
            //
            // get forecast icon
            //
            val bitmap = if (Location.isUS(locationIndex)) {
                UtilityForecastIcon.getIcon(context, currentConditions.iconUrl)
            } else {
                UtilityForecastIcon.getIcon(context, "ra")
            }
            val notification = UtilityNotification.createNotificationBigTextBigIcon(
                context,
                false,
                label,
                text,
                resultPendingIntent,
                smallIconResource,
                bitmap,
                text,
                NotificationCompat.PRIORITY_HIGH
            )
            notifier.notify(url + "CC", 1, notification)
        }
    }

    private fun send7Day(context: Context, locationIndex: Int, url: String) {
        if (Location.locations[locationIndex].sevenDayNotification) {
            val label = "(" + Location.getName(locationIndex) + ")" + " 7 day"
            val objSevenDay = SevenDay(locationIndex)
            val objectPendingIntents = ObjectPendingIntents(
                context,
                ForecastActivity::class.java,
                ForecastActivity.URL,
                arrayOf(Location.locations[locationIndex].x, Location.locations[locationIndex].y),
                arrayOf(Location.locations[locationIndex].x, Location.locations[locationIndex].y)
            )
            val objectNotification = ObjectNotification(
                context,
                false,
                label,
                objSevenDay.sevenDayShort,
                objectPendingIntents,
                GlobalVariables.ICON_FORECAST,
                GlobalVariables.ICON_ACTION,
                "7 Day Forecast",
                NotificationCompat.PRIORITY_MIN // 2022-09-03 was Notification.PRIORITY_MIN
            )
            objectNotification.send(url + "7day")
        } // end 7 day
    }

    //
    // check for local alerts and send notification if found (called by function send)
    //
    private fun checkForNotifications(
        context: Context,
        locationIndex: Int,
        inBlackout: Boolean
    ): String {
        val tornadoWarningString = "Tornado Warning"
        val html = Hazards.getHtml(Location.getLatLon(locationIndex))
        var notificationUrls = ""
        val locationLabel = "(" + Location.getName(locationIndex) + ") "
        val ids = html.parseColumn("\"@id\": \"(.*?)\"")
        val hazardTitles = html.parseColumn("\"event\": \"(.*?)\"")
        hazardTitles.forEachIndexed { i, hazardTitle ->
            if (ids.size > i) {
                val url = ids[i]
                val capAlert = CapAlert.createFromUrl(url)
                if (UtilityNotificationTools.nwsLocalAlertNotFiltered(context, hazardTitle)) {
                    val title = locationLabel + hazardTitle
                    val text = hazardTitle + " " + capAlert.area + " " + capAlert.summary
                    val objectPendingIntents = ObjectPendingIntents(
                        context,
                        AlertsDetailActivity::class.java,
                        AlertsDetailActivity.URL,
                        arrayOf(url, ""),
                        arrayOf(url, "sound")
                    )
                    val tornadoWarningPresent = hazardTitle.contains(tornadoWarningString)
                    if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(
                            context,
                            url
                        ))
                    ) {
                        val sound =
                            Location.locations[locationIndex].sound && !inBlackout || Location.locations[locationIndex].sound
                                    && tornadoWarningPresent && NotificationPreferences.alertBlackoutTornado
                        val objectNotification = ObjectNotification(
                            context,
                            sound,
                            title,
                            text,
                            objectPendingIntents,
                            GlobalVariables.ICON_ALERT,
                            GlobalVariables.ICON_ACTION,
                            context.resources.getString(R.string.read_aloud)
                        )
                        objectNotification.send(url)
                    }
                    notificationUrls += url + NotificationPreferences.NOTIFICATION_STRING_SEPARATOR
                }
            }
        }
        return notificationUrls
    }
}
