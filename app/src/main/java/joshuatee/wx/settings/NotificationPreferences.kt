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

package joshuatee.wx.settings

import android.content.Context
import android.provider.Settings
import joshuatee.wx.notifications.NotificationTextProduct
import joshuatee.wx.util.Utility

object NotificationPreferences {

    var alertNotificationSoundTornadoCurrent = false
    var alertNotificationSoundSpcmcd = false
    var alertNotificationSoundWpcmpd = false
    var alertNotificationSoundNhcAtl = false
    var alertNotificationSoundSpcwat = false
    var alertNotificationSoundSpcswo = false
    var alertNotificationSoundTextProd = false
    var notifSoundRepeat = false
    var notifTts = false
    var alertBlackoutAmCurrent = 0
    var alertBlackoutPmCurrent = 0
    var alertTornadoNotification = false
    var alertSpcMcdNotification = false
    var alertSpcWatchNotification = false
    var alertSpcSwoNotification = false
    var alertSpcSwoSlightNotification = false
    var alertWpcMpdNotification = false
    var alertBlackoutTornado = false
    var alertNhcEpacNotification = false
    var alertNhcAtlNotification = false
    var alertAutocancel = false
    var alertBlackout = false
    var alertOnlyOnce = false
    var notifSoundUri = ""

    const val NOTIFICATION_STRING_SEPARATOR = ","
    var notifTextProdStr = ""

    fun initialize(context: Context) {
        alertNotificationSoundTornadoCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_TORNADO", "false").startsWith("t")
        alertNotificationSoundSpcmcd =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_SPCMCD", "false").startsWith("t")
        alertNotificationSoundWpcmpd =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_WPCMPD", "false").startsWith("t")
        alertNotificationSoundNhcAtl =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_NHC_ATL", "false").startsWith("t")
        alertNotificationSoundSpcwat =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_SPCWAT", "false").startsWith("t")
        alertNotificationSoundSpcswo =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_SPCSWO", "false").startsWith("t")
        alertNotificationSoundTextProd =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND_TEXT_PROD", "false").startsWith("t")
        notifSoundRepeat = Utility.readPref(context, "NOTIF_SOUND_REPEAT", "false").startsWith("t")
        notifTts = Utility.readPref(context, "NOTIF_TTS", "false").startsWith("t")
        alertBlackoutAmCurrent = Utility.readPrefInt(context, "ALERT_BLACKOUT_AM", 7)
        alertBlackoutPmCurrent = Utility.readPrefInt(context, "ALERT_BLACKOUT_PM", 22)
        alertTornadoNotification =
            Utility.readPref(context, "ALERT_TORNADO_NOTIFICATION", "false").startsWith("t")
        alertSpcMcdNotification =
            Utility.readPref(context, "ALERT_SPCMCD_NOTIFICATION", "false").startsWith("t")
        alertSpcWatchNotification =
            Utility.readPref(context, "ALERT_SPCWAT_NOTIFICATION", "false").startsWith("t")
        alertSpcSwoNotification =
            Utility.readPref(context, "ALERT_SPCSWO_NOTIFICATION", "false").startsWith("t")
        alertSpcSwoSlightNotification =
            Utility.readPref(context, "ALERT_SPCSWO_SLIGHT_NOTIFICATION", "false").startsWith("t")
        alertWpcMpdNotification =
            Utility.readPref(context, "ALERT_WPCMPD_NOTIFICATION", "false").startsWith("t")
        alertBlackoutTornado =
            Utility.readPref(context, "ALERT_BLACKOUT_TORNADO", "true").startsWith("t")
        alertNhcEpacNotification =
            Utility.readPref(context, "ALERT_NHC_EPAC_NOTIFICATION", "false").startsWith("t")
        alertNhcAtlNotification =
            Utility.readPref(context, "ALERT_NHC_ATL_NOTIFICATION", "false").startsWith("t")
        alertAutocancel = Utility.readPref(context, "ALERT_AUTOCANCEL", "true").startsWith("t")
        alertBlackout = Utility.readPref(context, "ALERT_BLACKOUT", "false").startsWith("t")
        alertOnlyOnce = Utility.readPref(context, "ALERT_ONLYONCE", "true").startsWith("t")
        notifSoundUri = Utility.readPref(context, "NOTIF_SOUND_URI", "")
        if (notifSoundUri == "") {
            notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
        }
        notifTextProdStr = Utility.readPref(context, NotificationTextProduct.PREF_TOKEN, "")
    }
}
