/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.MyApplication
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.settings.Location

import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunTimes
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import java.text.DecimalFormat
import java.util.*



//TODO use SunCalc Lib instead of a website!!!
//FIXME the whole website is broke!!!
object UtilitySunMoon {


    fun getData(currentLoc: Int): String {
        //val tzOffset: String
        var x = 0.0
        var y = 0.0
        if (Location.isUS(currentLoc)) {
            x = Location.x.toDouble()
            y = Location.y.toDouble()
        } else {
            val tmpX = MyApplication.colon.split(Location.x)
            val tmpY = MyApplication.colon.split(Location.y)
            if (tmpX.size > 2 && tmpY.size > 1) {
                x = tmpX[2].toDouble()
                y = tmpY[1].toDouble()
            }
        }
        //val timeZone = UtilityTime.getDateAsString("Z")
        //tzOffset = timeZone.substring(0, 3) + "." + timeZone.substring(3, 5)
        //val url = "https://api.usno.navy.mil/rstt/oneday?date=today&coords=$x,$y&tz=$tzOffset"
        //return url.getHtmlUnsafe()


        val now: Date = Date()
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
        val phase = illumination.phase
        val moonFracillum = illumination.fraction * 100
        val moonangle = illumination.angle
        val normalized = phase + 180.0
        val moonage = 29.0 * (normalized / 360.0) + 1.0


        val moonphase = MoonPhase.compute().on(now).timezone(TimeZone.getDefault()).execute()
        val test = moonphase.time


        val header = "Sun/Moon Data" + MyApplication.newline
        var content = "Astronomical Rise: " + astronomical.rise + MyApplication.newline
        content += "Nautical Rise: " + nautical.rise + MyApplication.newline
        content += "Civil Rise: " + civil.rise + MyApplication.newline
        content += "SunRise: " + suntimes.rise + MyApplication.newline
        content += "Sun Upper Transit: " + suntimes.noon + MyApplication.newline
        content += "SunSet: " + suntimes.set + MyApplication.newline
        content += "Civil Set: " + civil.set + MyApplication.newline
        content += "Nautical Set: " + nautical.set + MyApplication.newline
        content += "Astronomical Set: " + astronomical.set + MyApplication.newline

        content += "MoonRise: " + moontimes.rise + MyApplication.newline
        //FIXME find out if can get moon upper transit
        content += "MoonSet: " + moontimes.set + MyApplication.newline

        content += "Moon Age: " + UtilityStringExternal.truncate(moonage.toString(), 5) + MyApplication.newline
        content += "Moon Illumination: " + UtilityStringExternal.truncate(moonFracillum.toString(),5) + "%" + MyApplication.newline

        //get current moon phase
        val getCurrentPhase = getPhase(moonage)
        content += getCurrentPhase + " is the current phase" + MyApplication.newline

        return content

    }


    //FIXME redo moon's phase.



    /*



 */

    fun roundTo(value: Double, places: Int): Double {
        val scale = Math.pow(10.0, places.toDouble())
        return Math.round(value * scale) / scale
    }

    /*
         NEW_MOON(0.0),
        /**
         * Waxing half moon.
         */
        FIRST_QUARTER(90.0),

        /**
         * Full moon.
         */
        FULL_MOON(180.0),

        /**
         * Waning half moon.
         */
        LAST_QUARTER(270.0);
     */

    private fun getPhasefromDegree(illumination: Double): String {
        var illum = roundTo(illumination , 3)
        var phaseString = "unknown"
        if (illum   >= 0.0 && illum   < 0.02) {
            phaseString = "New Moon"
        }
        if (illum   >= 0.02 && illum   < 0.23) {
            phaseString = "Waxing Crescent"
        }
        if (illum   >= 0.23 && illum   < 0.27) {
            phaseString = "First Quarter"
        }
        if (illum   >= 0.27 && illum   < 0.47) {
            phaseString = "Waxing Gibbous"
        }
        if (illum   >= 180 && illum   < 180) {
            phaseString = "Full Moon"
        }
        if (illum   >= 0.52 && illum   < 0.73) {
            phaseString = "Waning Gibbous"
        }
        if (illum   >= 0.270 && illum   < 0.77) {
            phaseString = "Last Quarter"
        }
        if (illum   >= 0.77 && illum   < 1.01) {
            phaseString = "Waning Crescent"
        }

    return phaseString + " ("+illum+") "

    }



    private fun getPhase(age: Double): String {
        if (age >= 29 || age <= 1) {
            return "New Moon"
        }
        if (age > 4 && age < 10) {
            return "Waxing Crescent"
        }
        if (age >= 7 && age <= 8) {
            return "First Quarter"
        }

        if (age >= 14 && age <= 15) {
            return "Full Moon"
        }

        if (age >= 21 && age <= 22) {
            return "Waning Crescent"
        }

        if (age > 1 && age < 7) {
            return "Crescent Concave"
        }

        if (age > 8 && age < 14) {
            return "Crescent Gibbous"
        }

        if (age > 15 && age < 21) {
            return "Waning Gibbous"
        }

        return if (age > 22 && age < 29) {
            "Waning Crescent"
        } else ""

    }


}