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
        val officialSunriseCal =
            calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        val officialSunsetCal = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        return listOf(officialSunriseCal, officialSunsetCal)
    }

   /* fun getSunTimesForHomescreen(): String {
        val sunCalc = SunCalc()
        val now = Calendar.getInstance()
        val time = Date()
        now.time = time
        val formatter = SimpleDateFormat("MM-dd h:mm a", Locale.US)
        val sunRiseDate = sunCalc.time(now, SolarEvent.Sunrise, Location.latLon)
        val sunSetDate = sunCalc.time(now, SolarEvent.Sunset, Location.latLon)
        val sunRise = formatter.format(sunRiseDate.time)
        val sunSet = formatter.format(sunSetDate.time)
        return "Sunrise: $sunRise Sunset: $sunSet"
    }

    fun getMoonTimesForHomescreen(): String {
        var moonRise = ""
        var moonSet = ""
        val sunCalc = SunCalc()
        val now = Calendar.getInstance()
        val formatter = SimpleDateFormat("MM-dd h:mm a", Locale.US)
        val moonTimes = sunCalc.moonTimes(now, Location.latLon)
        if (moonTimes[0] != null) {
            moonRise = formatter.format(moonTimes[0]!!.time)
        }
        if (moonTimes[1] != null) {
            moonSet = formatter.format(moonTimes[1]!!.time)
        }
        return "Moonrise: $moonRise Moonset: $moonSet"
    }

    fun getMoonIlluminationForHomescreen(): String {
        val sunCalc = SunCalc()
        val now = Calendar.getInstance()
        val moonIllumination = sunCalc.moonIllumination(now)
        return moonPhaseFromIllumination(moonIllumination.phase) + " " + moonIllumination.phase.toString()
    }*/

    /*fun moonPhaseFromIllumination(phase: Double): String {
        val phaseString: String
        if (phase < 0.02) {
            phaseString = "New Moon"
        } else if (0.02 <= phase && phase < 0.23) {
            phaseString = "Waxing Crescent"
        } else if (0.23 <= phase && phase < 0.27) {
            phaseString = "First Quarter"
        } else if (0.27 <= phase && phase < 0.47) {
            phaseString = "Waxing Gibbous"
        } else if (0.47 <= phase && phase < 0.52) {
            phaseString = "Full Moon"
        } else if (0.52 <= phase && phase < 0.73) {
            phaseString = "Waning Gibbous"
        } else if (0.73 <= phase && phase < 0.77) {
            phaseString = "Last Quarter"
        } else if (0.77 <= phase && phase < 1.01) {
            phaseString = "Waning Crescent"
        } else {
            phaseString = "unknown"
        }
        return phaseString
    }*/

    fun getSunriseSunset(context: Context, locNum: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val lat: String
        val lon: String
        if (!Location.isUS(locNumInt)) {
            val latArr = Location.getX(locNumInt).split(":")
            val lonArr = Location.getY(locNumInt).split(":")
            if (latArr.size > 2 && lonArr.size > 1) {
                lat = latArr[2]
                lon = lonArr[1]
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
        val srTime: String
        val ssTime: String
        var amStr = ""
        var pmStr = ""
        if (!DateFormat.is24HourFormat(context)) {
            amStr = "am"
            pmStr = "pm"
            srTime = (officialSunriseCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                            ' ',
                            '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        } else {
            srTime = (officialSunriseCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                            ' ',
                            '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        }
        return "Sunrise: $srTime$amStr   Sunset: $ssTime$pmStr"
    }

    fun getSunriseSunsetShort(context: Context, locNum: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val lat: String
        val lon: String
        if (!MyApplication.locations[locNumInt].isUS) {
            val latArr = MyApplication.colon.split(Location.getX(locNumInt))
            val lonArr = MyApplication.colon.split(Location.getY(locNumInt))
            if (latArr.size > 2 && lonArr.size > 1) {
                lat = latArr[2]
                lon = lonArr[1]
            } else
                return ""
        } else {
            lat = Location.getX(locNumInt)
            lon = Location.getY(locNumInt)
        }
        val location = ExternalSunriseLocation(lat, lon)
        val calculator = ExternalSunriseSunsetCalculator(location, TimeZone.getDefault())
        val officialSunriseCal =
                calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        val officialSunsetCal = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        val srTime: String
        val ssTime: String
        var amStr = ""
        var pmStr = ""
        if (!DateFormat.is24HourFormat(context)) {
            amStr = "am"
            pmStr = "pm"
            srTime = (officialSunriseCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                            ' ',
                            '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        } else {
            srTime = (officialSunriseCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                            ' ',
                            '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        }
        return "$srTime$amStr / $ssTime$pmStr"
    }
}
