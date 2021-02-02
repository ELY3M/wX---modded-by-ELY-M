/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

package joshuatee.wx.util

import android.content.Context
import android.text.format.DateFormat
import joshuatee.wx.MyApplication
import joshuatee.wx.external.ExternalSunriseLocation
import joshuatee.wx.external.ExternalSunriseSunsetCalculator
import joshuatee.wx.radar.RID
import joshuatee.wx.settings.Location

import java.util.Calendar
import java.util.TimeZone

object UtilityTimeSunMoon {

    fun getSunriseSunsetFromObs(obs: RID): List<Calendar> {
        val location = ExternalSunriseLocation(obs.location.latString, obs.location.lonString)
        val calculator = ExternalSunriseSunsetCalculator(location, TimeZone.getDefault())
        val officialSunriseCal: Calendar = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        val officialSunsetCal: Calendar = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        return listOf(officialSunriseCal, officialSunsetCal)
    }

    fun getSunriseSunset(context: Context, locNum: String, shortFormat: Boolean): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val lat: String
        val lon: String
        if (!Location.isUS(locNumInt)) {
            val latItems = Location.getX(locNumInt).split(":").dropLastWhile { it.isEmpty() }
            val lonItems = Location.getY(locNumInt).split(":").dropLastWhile { it.isEmpty() }
            if (latItems.size > 2 && lonItems.size > 1) {
                lat = latItems[2]
                lon = lonItems[1]
            } else
                return ""
        } else {
            lat = Location.getX(locNumInt)
            lon = Location.getY(locNumInt)
        }
        val location = ExternalSunriseLocation(lat, lon)
        val calculator = ExternalSunriseSunsetCalculator(location, TimeZone.getDefault())
        val officialSunriseCal = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        val officialSunsetCal = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        val sunRiseTime: String
        val sunSetTime: String
        var am = ""
        var pm = ""
        if (!DateFormat.is24HourFormat(context)) {
            am = "am"
            pm = "pm"
            sunRiseTime = officialSunriseCal.get(Calendar.HOUR).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(' ', '0')
            sunSetTime = officialSunsetCal.get(Calendar.HOUR).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        } else {
            sunRiseTime = officialSunriseCal.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(' ', '0')
            sunSetTime = officialSunsetCal.get(Calendar.HOUR_OF_DAY).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        }
        return if (shortFormat) {
            "$sunRiseTime$am / $sunSetTime$pm"
        } else {
            "Sunrise: $sunRiseTime$am   Sunset: $sunSetTime$pm"
        }
    }

    fun getForHomeScreen(context: Context): String {
        return getSunriseSunset(context, Location.currentLocationStr, false) + MyApplication.newline + UtilityTime.gmtTime()
    }
}
