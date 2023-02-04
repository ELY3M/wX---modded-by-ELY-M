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

package joshuatee.wx.settings

import android.provider.Settings
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.NotificationTextProduct

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

    var mediaNotifTtsTitle = ""
    const val notificationStrSep = ","
    var notifTextProdStr = ""

    fun initPreferences() {
        alertNotificationSoundTornadoCurrent = getInitialPreference("ALERT_NOTIFICATION_SOUND_TORNADO", "false")
        alertNotificationSoundSpcmcd = getInitialPreference("ALERT_NOTIFICATION_SOUND_SPCMCD", "false")
        alertNotificationSoundWpcmpd = getInitialPreference("ALERT_NOTIFICATION_SOUND_WPCMPD", "false")
        alertNotificationSoundNhcAtl = getInitialPreference("ALERT_NOTIFICATION_SOUND_NHC_ATL", "false")
        alertNotificationSoundSpcwat = getInitialPreference("ALERT_NOTIFICATION_SOUND_SPCWAT", "false")
        alertNotificationSoundSpcswo = getInitialPreference("ALERT_NOTIFICATION_SOUND_SPCSWO", "false")
        alertNotificationSoundTextProd = getInitialPreference("ALERT_NOTIFICATION_SOUND_TEXT_PROD", "false")
        notifSoundRepeat = getInitialPreference("NOTIF_SOUND_REPEAT", "false")
        notifTts = getInitialPreference("NOTIF_TTS", "false")
        alertBlackoutAmCurrent = getInitialPreference("ALERT_BLACKOUT_AM", -1)
        alertBlackoutPmCurrent = getInitialPreference("ALERT_BLACKOUT_PM", -1)
        alertTornadoNotification = getInitialPreference("ALERT_TORNADO_NOTIFICATION", "false")
        alertSpcMcdNotification = getInitialPreference("ALERT_SPCMCD_NOTIFICATION", "false")
        alertSpcWatchNotification = getInitialPreference("ALERT_SPCWAT_NOTIFICATION", "false")
        alertSpcSwoNotification = getInitialPreference("ALERT_SPCSWO_NOTIFICATION", "false")
        alertSpcSwoSlightNotification = getInitialPreference("ALERT_SPCSWO_SLIGHT_NOTIFICATION", "false")
        alertWpcMpdNotification = getInitialPreference("ALERT_WPCMPD_NOTIFICATION", "false")
        alertBlackoutTornado = getInitialPreference("ALERT_BLACKOUT_TORNADO", "false")
        alertNhcEpacNotification = getInitialPreference("ALERT_NHC_EPAC_NOTIFICATION", "false")
        alertNhcAtlNotification = getInitialPreference("ALERT_NHC_ATL_NOTIFICATION", "false")
        alertAutocancel = getInitialPreference("ALERT_AUTOCANCEL", "false")
        alertBlackout = getInitialPreference("ALERT_BLACKOUT", "false")
        alertOnlyOnce = getInitialPreference("ALERT_ONLYONCE", "false")
        notifSoundUri = getInitialPreferenceString("NOTIF_SOUND_URI", "")
        if (notifSoundUri == "") {
            notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
        }
        notifTextProdStr = getInitialPreferenceString(NotificationTextProduct.PREF_TOKEN, "")
    }

    private fun getInitialPreference(pref: String, initValue: Int): Int =
            MyApplication.preferences.getInt(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: String): Boolean =
            (MyApplication.preferences.getString(pref, initValue) ?: initValue).startsWith("t")

    private fun getInitialPreferenceString(pref: String, initValue: String): String =
            MyApplication.preferences.getString(pref, initValue) ?: initValue
}
