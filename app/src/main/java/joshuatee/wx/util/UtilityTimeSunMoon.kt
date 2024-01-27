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
//modded by ELY M.  

package joshuatee.wx.util

import android.content.Context
import android.text.format.DateFormat
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.externalSolarized.Solarized
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.radar.RID
import joshuatee.wx.settings.Location
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunTimes
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


object UtilityTimeSunMoon {

    //DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm").format(date)

    fun dateTimeFormatter(date: ZonedDateTime?): String {
        return DateTimeFormatter.ofPattern("MM-dd-yy hh:mm a z").format(date)
    }

    fun getData(latLon: LatLon): String {
        //val tzOffset: String

        var x = latLon.lat
        var y = latLon.lon
        /*
        if (Location.isUS(currentLoc)) {
            x = Location.x.toDouble()
            y = Location.y.toDouble()
        } else {
            val tmpX = RegExp.colon.split(Location.x)
            val tmpY = RegExp.colon.split(Location.y)
            if (tmpX.size > 2 && tmpY.size > 1) {
                x = tmpX[2].toDouble()
                y = tmpY[1].toDouble()
            }
        }
        */

        val now = Date()
        val suntimes: SunTimes = SunTimes.compute()
            .on(now)       // set a date
            .at(x, y)   // set a location
            .execute()     // get the results

        //sun rise/set
        val astronomical = SunTimes.compute().twilight(SunTimes.Twilight.ASTRONOMICAL).on(now).at(x, y).execute()
        val nautical = SunTimes.compute().twilight(SunTimes.Twilight.NAUTICAL).on(now).at(x, y).execute()
        val civil = SunTimes.compute().twilight(SunTimes.Twilight.CIVIL).on(now).at(x, y).execute()


        //moon rise/set
        val moontimes: MoonTimes = MoonTimes.compute()
            .on(now)       // set a date
            .at(x, y)   // set a location
            .execute();     // get the results

        //The phase angle is the angle sun-moon-earth,
        //0 = full phase, 180 = new.

        val illumination = MoonIllumination.compute().on(now).timezone(TimeZone.getDefault()).execute()
        val moonlitpercent = illumination.fraction * 100

        val phase = illumination.phase

        //val moonangle = illumination.angle
        val normalized = phase + 180.0
        val moonage = 29.0 * (normalized / 360.0) + 1.0

        //val moonphase = MoonPhase.compute().on(now).timezone(TimeZone.getDefault()).execute()
        //val test = moonphase.time


        //val header = "Sun/Moon Data" + MyApplication.newline
        var content = "Astronomical Rise: " + dateTimeFormatter(astronomical.rise) + GlobalVariables.newline
        content += "Nautical Rise: " + dateTimeFormatter(nautical.rise) + GlobalVariables.newline
        content += "Civil Rise: " + dateTimeFormatter(civil.rise) + GlobalVariables.newline
        content += "SunRise: " + dateTimeFormatter(suntimes.rise) + GlobalVariables.newline
        content += "Sun Upper Transit: " + dateTimeFormatter(suntimes.noon) + GlobalVariables.newline
        content += "SunSet: " + dateTimeFormatter(suntimes.set) + GlobalVariables.newline
        content += "Civil Set: " + dateTimeFormatter(civil.set) + GlobalVariables.newline
        content += "Nautical Set: " + dateTimeFormatter(nautical.set) + GlobalVariables.newline
        content += "Astronomical Set: " + dateTimeFormatter(astronomical.set) + GlobalVariables.newline

        content += "MoonRise: " + dateTimeFormatter(moontimes.rise) + GlobalVariables.newline
        //FIXME find out if can get moon upper transit
        content += "MoonSet: " + dateTimeFormatter(moontimes.set) + GlobalVariables.newline

        content += "Moon Age: " + moonage.toString() + GlobalVariables.newline
        content += "Moon Illumination: " + moonlitpercent.toString() + "%" + GlobalVariables.newline

        //get current moon phase
        val getCurrentPhase = getPhase(moonage)
        content += getCurrentPhase + " is the current phase" + GlobalVariables.newline

        return content

    }


    private fun getPhase(age: Double): String {
        if (age > 27 || age < 3) {
            return "New Moon"
        }

        if (age > 4 && age < 10) {
            return "Waxing Crescent"
        }

        if (age > 11 && age < 17) {
            return "Full Moon"
        }

        return if (age > 19 && age < 25) {
            "Waning Crescent"
        } else "Unknown"

    }


    private fun getPhaseTest(age: Double): String {
        if (age >= 29 || age <= 1) {
            return "New"
        }

        if (age >= 7 && age <= 8) {
            return "First Quarter"
        }

        if (age >= 14 && age <= 15) {
            return "Full"
        }

        if (age >= 21 && age <= 22) {
            return "Waning Crescent"
        }

        if (age > 1 && age < 7) {
            return "Crescent Concave"
        }

        if (age > 8 && age < 14) {
            return "Crescente Gibosa"
        }

        if (age > 15 && age < 21) {
            return "Minguante Gibosa"
        }

        return if (age > 22 && age < 29) {
            "Waning Crescent"
        } else ""

    }
    // used in ObjectMetar
    fun getSunriseSunsetFromObs(obs: RID): List<ObjectDateTime> {
        val solarized = Solarized(obs.location.lat, obs.location.lon, ObjectDateTime().now)
        val sunrise = ObjectDateTime(solarized.sunrise?.date ?: ObjectDateTime().now)
        val sunset = ObjectDateTime(solarized.sunset?.date ?: ObjectDateTime().now)
        return listOf(sunrise, sunset)
    }

    fun getSunriseSunset(context: Context, latLon: LatLon, shortFormat: Boolean = false): String {
        //elys mod MM-dd-yy hh:mm a z
        val timeFormat = if (!DateFormat.is24HourFormat(context)) {
            "MM-dd-yy h:mm a"
        } else {
            "MM-dd-yy H:mm"
        }
        val solarized = Solarized(latLon.lat, latLon.lon, ObjectDateTime().get())
        val formatter = DateTimeFormatter.ofPattern(timeFormat)
        val roundingAdd = 30L
        val sunRiseTime = solarized.sunrise?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val sunSetTime = solarized.sunset?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val dawnTime = solarized.firstLight?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val duskTime = solarized.lastLight?.date?.plusSeconds(roundingAdd)?.format(formatter)
	
        //elys mod
        val astRise = solarized.AstronomicalRise?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val nauRise = solarized.NauticalRise?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val civilRise = solarized.CivilRise?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val sunRise = solarized.SunRise?.date?.plusSeconds(roundingAdd)?.format(formatter)

        val astSet = solarized.AstronomicalSet?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val nauSet = solarized.NauticalSet?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val civilSet = solarized.CivilSet?.date?.plusSeconds(roundingAdd)?.format(formatter)
        val sunSet = solarized.SunSet?.date?.plusSeconds(roundingAdd)?.format(formatter)

	
        return if (shortFormat) {
            "$sunRiseTime / $sunSetTime"
        } else {

            "Astronomical Sunrise: $astRise" + GlobalVariables.newline +
            "Nautical Sunrise: $nauRise" + GlobalVariables.newline +
            "Civil Sunrise: $civilRise" + GlobalVariables.newline +
            "Sunrise: $sunRise" + GlobalVariables.newline +
                    "Sunset: $sunSet" + GlobalVariables.newline +
                    "Civil Sunset: $civilSet" + GlobalVariables.newline +
                    "Nautical Sunset: $nauSet" + GlobalVariables.newline +
            "Astronomical Sunset: $astSet" + GlobalVariables.newline + GlobalVariables.newline

            //"Dawn: $dawnTime $am   Dusk: $duskTime $pm"
        }
    }

    // used by SunRiseCard
    fun getForHomeScreen(context: Context, latLon: LatLon): String =
            //elys mod - detailed sun/moon times
            getSunriseSunset(context, latLon) +
	GlobalVariables.newline + "Detailed Sun and Moon Times: "  + GlobalVariables.newline + getData(latLon) + GlobalVariables.newline + ObjectDateTime.gmtTime()
}






















