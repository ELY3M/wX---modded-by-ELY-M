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
import joshuatee.wx.util.Utility

class ObjectLocation(val context: Context, locNumInt: Int) {

    val x: String
    val y: String
    val name: String
    val wfo: String
    val rid: String
    private val alertNotificationCurrent: String
    private val alertNotificationRadarCurrent: String
    private val alertCcNotificationCurrent: String
    private val alertSevenDayNotificationCurrent: String
    private val alertNotificationSoundCurrent: String
    private val alertNotificationMcdCurrent: String
    private val alertNotificationSwoCurrent: String
    private val alertNotificationSpcfwCurrent: String
    private val alertNotificationWpcmpdCurrent: String
    val isUS: Boolean
    var observation = ""
    private val prefNumberString: String

    init {
        val jStr = (locNumInt + 1).toString()
        prefNumberString = jStr
        x = Utility.readPref(context, "LOC" + jStr + "_X", "0.0")
        y = Utility.readPref(context, "LOC" + jStr + "_Y", "0.0")
        name = Utility.readPref(context, "LOC" + jStr + "_LABEL", "")
        wfo = Utility.readPref(context, "NWS$jStr", "")
        rid = Utility.readPref(context, "RID$jStr", "")
        alertNotificationCurrent =
            Utility.readPref(context, "ALERT" + jStr + "_NOTIFICATION", "false")
        alertNotificationRadarCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_RADAR$jStr", "false")
        alertCcNotificationCurrent =
            Utility.readPref(context, "ALERT_CC" + jStr + "_NOTIFICATION", "false")
        alertSevenDayNotificationCurrent =
            Utility.readPref(context, "ALERT_7DAY_" + jStr + "_NOTIFICATION", "false")
        alertNotificationSoundCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_SOUND$jStr", "false")
        alertNotificationMcdCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_MCD$jStr", "false")
        alertNotificationSwoCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_SWO$jStr", "false")
        alertNotificationSpcfwCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_SPCFW$jStr", "false")
        alertNotificationWpcmpdCurrent =
            Utility.readPref(context, "ALERT_NOTIFICATION_WPCMPD$jStr", "false")
        observation = Utility.readPref(context, "LOC" + jStr + "_OBSERVATION", "")
        isUS = Location.us(x)
    }

    fun saveToNewSlot(newLocNumInt: Int) {
        val iStr = (newLocNumInt + 1).toString()
        Utility.writePref(context, "ALERT" + iStr + "_NOTIFICATION", alertNotificationCurrent)
        Utility.writePref(context, "ALERT_CC" + iStr + "_NOTIFICATION", alertCcNotificationCurrent)
        Utility.writePref(
            context,
            "ALERT_7DAY_" + iStr + "_NOTIFICATION",
            alertSevenDayNotificationCurrent
        )
        Utility.writePref(context, "ALERT_NOTIFICATION_SOUND$iStr", alertNotificationSoundCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_MCD$iStr", alertNotificationMcdCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_SWO$iStr", alertNotificationSwoCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_SPCFW$iStr", alertNotificationSpcfwCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_WPCMPD$iStr", alertNotificationWpcmpdCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_RADAR$iStr", alertNotificationRadarCurrent)
        Utility.writePref(context, "LOC" + iStr + "_X", x)
        Utility.writePref(context, "LOC" + iStr + "_Y", y)
        Utility.writePref(context, "LOC" + iStr + "_LABEL", name)
        Utility.writePref(context, "NWS$iStr", wfo)
        Utility.writePref(context, "RID$iStr", rid)
        Utility.writePref(context, "LOC" + iStr + "_OBSERVATION", observation)
        Location.refresh(context)
    }

    fun updateObservation(observation: String) {
        this.observation = observation
        Utility.writePref(context, "LOC" + prefNumberString + "_OBSERVATION", observation)
    }

    val notification get() = alertNotificationCurrent.startsWith("t")

    val notificationRadar get() = alertNotificationRadarCurrent.startsWith("t")

    val ccNotification get() = alertCcNotificationCurrent.startsWith("t")

    val sevenDayNotification get() = alertSevenDayNotificationCurrent.startsWith("t")

    val sound get() = alertNotificationSoundCurrent.startsWith("t")

    val notificationMcd get() = alertNotificationMcdCurrent.startsWith("t")

    val notificationSwo get() = alertNotificationSwoCurrent.startsWith("t")

    val notificationSpcFw get() = alertNotificationSpcfwCurrent.startsWith("t")

    val notificationWpcMpd get() = alertNotificationWpcmpdCurrent.startsWith("t")
}
