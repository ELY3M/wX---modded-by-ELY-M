/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.graphics.Color
import android.preference.PreferenceManager

object UtilityPref2 {

    fun prefInitSetDefaults(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        val value = preferences.getString("ALERT1_NOTIFICATION", null)
        if (value == null) {
            val stateDefault = "Oklahoma"
            val simpleModeDefault = "false"
            val themeBlueDefault = "white"
            val refreshMainMinDefault = 15
            val locNumIntDefault = 1
            val refreshSpcMinDefault = 15
            val loc1LabelDefault = "home"
            val zipcodeDefault = "73069"
            val dstOffset1Default = "3600.0000"
            val rawOffset1Default = "-21600.0000"
            val locDisplayImgDefault = "true"
            val alerts1Default = "true"
            val alertNotificationDefault = "false"
            val alertTornadoNotificationDefault = "false"
            val alertSpcmcdNotificationDefault = "false"
            val alertSpcwatNotificationDefault = "false"
            val alertSpcswoNotificationDefault = "false"
            val alertSpcswoSlightNotificationDefault = "false"
            val alertWpcmpdNotificationDefault = "false"
            val alertCcNotificationDefault = "false"
            val alert7Day1NotificationDefault = "false"
            val alertNotificationSoundDefault = "false"
            val alertNotificationRadarDefault = "false"
            val alertNotificationSoundTornadoDefault = "false"
            val alertNotificationSoundSpcmcdDefault = "false"
            val alertNotificationSoundSpcwatDefault = "false"
            val alertNotificationSoundSpcswoDefault = "false"
            val alertNotificationSoundWpcmpdDefault = "false"
            val alertNotificationIntervalDefault = 12
            val alertBlackoutDefault = "false"
            val alertCodRadarDefault = "WXOGL"
            val alertBlackoutAmDefault = 7
            val alertBlackoutPmDefault = 22
            val alertBlackoutTornadoDefault = "true"
            val current1Default = "true"
            val mdDefault = "true"
            val mdCntDefault = "2"
            val watchDefault = "true"
            val watchCntDefault = "2"
            val checkspcDefault = "false"
            val checkwpcDefault = "false"
            val checktorDefault = "false"
            val county1Default = "Cleveland"
            val zone1Default = "OKC027"
            val stateCodeDefault = "OK"
            val stateCodeIrDefault = "TX"
            val loc1XDefault = "35.231"
            val loc1YDefault = "-97.451"
            val loc1NwsDefault = "OUN"
            val nws1Default = "OUN"
            val rid1Default = "TLX"
            val nws1DefaultState = "OK"
            val nwsRadarBgBlack = "true"
            val codWarningsDefault = "false"
            val codCitiesDefault = "false"
            val mediaControlNotifDefault = "false"
            val codForVisDefault = "true"
            val ouForSoundingsDefault = "false"
            editor.putString("UI_ICONS_EVENLY_SPACED", "false")
            editor.putString("ALERT_ONLYONCE", "true")
            editor.putString("ALERT_AUTOCANCEL", "true")
            editor.putInt("RADAR_WARN_LINESIZE", 4)
            editor.putInt("RADAR_WATMCD_LINESIZE", 4)
            editor.putInt("WXOGL_SIZE", 13) // was 15
            editor.putString("LOCK_TOOLBARS", "true")
            editor.putString("RADAR_SHOW_COUNTY", "true")
            editor.putInt("RADAR_COLOR_HW", Color.BLUE)
            editor.putInt("RADAR_COLOR_STATE", Color.WHITE)
            editor.putString("ALERT1_NOTIFICATION", alertNotificationDefault)
            editor.putString("ALERT_TORNADO_NOTIFICATION", alertTornadoNotificationDefault)
            editor.putString("ALERT_SPCMCD_NOTIFICATION", alertSpcmcdNotificationDefault)
            editor.putString("ALERT_SPCWAT_NOTIFICATION", alertSpcwatNotificationDefault)
            editor.putString("ALERT_SPCSWO_NOTIFICATION", alertSpcswoNotificationDefault)
            editor.putString("ALERT_SPCSWO_SLIGHT_NOTIFICATION", alertSpcswoSlightNotificationDefault)
            editor.putString("ALERT_WPCMPD_NOTIFICATION", alertWpcmpdNotificationDefault)
            editor.putString("ALERT_CC1_NOTIFICATION", alertCcNotificationDefault)
            editor.putString("ALERT_7DAY_1_NOTIFICATION", alert7Day1NotificationDefault)
            editor.putString("ALERT_NOTIFICATION_RADAR1", alertNotificationRadarDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND1", alertNotificationSoundDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND_TORNADO", alertNotificationSoundTornadoDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND_SPCMCD", alertNotificationSoundSpcmcdDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND_SPCWAT", alertNotificationSoundSpcwatDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND_SPCSWO", alertNotificationSoundSpcswoDefault)
            editor.putString("ALERT_NOTIFICATION_SOUND_WPCMPD", alertNotificationSoundWpcmpdDefault)
            editor.putInt("ALERT_NOTIFICATION_INTERVAL", alertNotificationIntervalDefault)
            editor.putString("ALERT_BLACKOUT", alertBlackoutDefault)
            editor.putString("ALERT_COD_RADAR", alertCodRadarDefault)
            editor.putInt("ALERT_BLACKOUT_AM", alertBlackoutAmDefault)
            editor.putInt("ALERT_BLACKOUT_PM", alertBlackoutPmDefault)
            editor.putString("ALERT_BLACKOUT_TORNADO", alertBlackoutTornadoDefault)
            //editor.apply()
            editor.putInt("REFRESH_MAIN_MIN", refreshMainMinDefault)
            editor.putInt("LOC_NUM_INT", locNumIntDefault)
            editor.putInt("REFRESH_SPC_MIN", refreshSpcMinDefault)
            editor.putString("LOC1_X", loc1XDefault)
            editor.putString("LOC1_Y", loc1YDefault)
            editor.putString("LOC1_NWS", loc1NwsDefault)
            editor.putString("LOC1_LABEL", loc1LabelDefault)
            editor.putString("COUNTY1", county1Default)
            editor.putString("ZONE1", zone1Default)
            editor.putString("SIMPLE_MODE", simpleModeDefault)
            editor.putString("LOC_DISPLAY_IMG", locDisplayImgDefault)
            editor.putString("ALERTS1", alerts1Default)
            editor.putString("CURRENT1", current1Default)
            editor.putString("MD_SHOW", mdDefault)
            editor.putString("MD_CNT", mdCntDefault)
            editor.putString("WATCH_SHOW", watchDefault)
            editor.putString("WATCH_CNT", watchCntDefault)
            editor.putString("CHECKSPC", checkspcDefault)
            editor.putString("CHECKWPC", checkwpcDefault)
            editor.putString("CHECKTOR", checktorDefault)
            editor.putString("STATE", stateDefault)
            editor.putString("STATE_CODE", stateCodeDefault)
            editor.putString("STATE_CODE_IR", stateCodeIrDefault)
            editor.putString("ZIPCODE1", zipcodeDefault)
            editor.putString("LOC1_TIMERAW", rawOffset1Default)
            editor.putString("LOC1_TIMEDST", dstOffset1Default)
            editor.putString("NWS1", nws1Default)
            editor.putString("RID1", rid1Default)
            editor.putString("NWS1_STATE", nws1DefaultState)
            editor.putString("THEME_BLUE", themeBlueDefault)
            editor.putString("NWS_RADAR_BG_BLACK", nwsRadarBgBlack)
            editor.putString("COD_WARNINGS_DEFAULT", codWarningsDefault)
            editor.putString("COD_CITIES_DEFAULT", codCitiesDefault)
            editor.putString("MEDIA_CONTROL_NOTIF", mediaControlNotifDefault)
            editor.putString("COD_FOR_VIS", codForVisDefault)
            editor.putString("OU_FOR_SOUNDINGS", ouForSoundingsDefault)
            editor.putString("OU_FOR_SOUNDINGS", ouForSoundingsDefault)
            editor.putString("CARDS_MAIN_SCREEN", "true")
            editor.putInt("ELEVATION_PREF", 10)
            editor.apply()
        }
    }
}
