/*

Kotlin port of https://github.com/imvenj/SunCalc.swift
Please see COPYING.SunCalc for license specified at above URL

which was ported from:
 (c) 2011-2015, Vladimir Agafonkin
 SunCalc is a JavaScript library for calculating sun/moon position and light phases.
 https://github.com/mourner/suncalc


 */

package joshuatee.wx.external

import joshuatee.wx.radar.LatLon
import joshuatee.wx.util.UtilityLog
import java.util.*
import kotlin.math.*

class AzimuthCoordinate(val azimuth: Double, val altitude: Double)
class EclipticCoordinate(val rightAscension: Double, val declination: Double)
class MoonPosition(
        val azimuth: Double,
        val altitude: Double,
        val distance: Double,
        val parallacticAngle: Double
)

class MoonCoordinate(val rightAscension: Double, val declination: Double, val distance: Double)
class MoonIllumination(val fraction: Double, val phase: Double, val angle: Double)

class Location(var latitude: Double, var longitude: Double)

class SunCalc {

    companion object {

        const val rad  = PI / 180
        const val j0 = 0.0009

// sun calculations are based on http://aa.quae.nl/en/reken/zonpositie.html formulas
// date/time constants and conversions

        const val dayMs = 1000 * 60 * 60 * 24
        const val j1970 = 2440588
        const val j2000 = 2451545
        const val e = rad * 23.4397 // obliquity of the Earth
    }


    private fun toJulian(date: Calendar): Double {
        //val cal = Calendar.getInstance()
        //return  date.timeInMillis / dayMs - 0.5 + j1970;
        // FIXME why was the - 0.5 needed in Dart/Swift
        return  (date.timeInMillis / dayMs - 0.5 + j1970)
    } // JS: date.valueOf()

    private fun fromJulian(j: Double): Calendar {
        val number = (j + 0.5 - j1970) * dayMs
        val cal = Calendar.getInstance()
        cal.timeInMillis = number.toLong()
        return cal
    }

    private fun toDays(date: Calendar): Double {
        return toJulian(date) - j2000
    }

    private fun rightAscension(l: Double, b: Double): Double {
        return atan2(sin(l) * cos(e) - tan(b) * sin(e), cos(l))
    }

    private fun declination(l: Double, b: Double): Double {
        return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(l))
    }

    private fun azimuth(h: Double, phi: Double, dec: Double): Double {
        return atan2(sin(h), cos(h) * sin(phi) - tan(dec) * cos(phi))
    }

    private fun altitude(h: Double, phi: Double, dec: Double): Double {

        val intermediate1 = sin(phi) * sin(dec)
        val intermediate2 = cos(phi) * cos(dec) * cos(h)
        val intermediate =  intermediate1 + intermediate2
        //UtilityLog.d("wx", "moon position: " + h.toString() + " " + cos(h).toString() + " " + intermediate2.toString())
        return asin(intermediate)
    }

    private fun siderealTime(d: Double, lw: Double): Double {
        //UtilityLog.d("wx","side: " + d.toString() + " " + lw.toString());
        return rad * (280.16 + 360.9856235 * d) - lw
    }

   /* private fun astroRefraction(aH: Double): Double {
        val h: Double = if (aH < 0) {
            0.0
        } else {
            aH
        }
        return 0.0002967 / tan(h + 0.00312536 / (h + 0.08901179))
    }*/

    private fun astroRefraction(hF: Double): Double {
        var h = hF
        if (h < 0)
        // the following formula works for positive altitudes only.
            h = 0.0 // if h = -0.08901179 a div/0 would occur.

        // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
        return 0.0002967 / tan(h + 0.00312536 / (h + 0.08901179))
    }

    private fun solarMeanAnomaly(d: Double): Double {
        return rad * (357.5291 + 0.98560028 * d)
    }

    private fun eclipticLongitude(m: Double): Double {
        val c = rad * (1.9148 * sin(m) + 0.02 * sin(2.0 * m) + 0.0003 * sin(3.0 * m))
        val p = rad * 102.9372
        return m + c + p + PI
    }

    private fun sunCoordinates(d: Double): EclipticCoordinate {
        val m = solarMeanAnomaly(d)
        val l = eclipticLongitude(m)
        return EclipticCoordinate(rightAscension(l, 0.0), declination(l, 0.0))
    }

    private fun julianCycle(d: Double, lw: Double): Double {
        val v = (d - j0) - (lw / (2.0 * PI))
        return round(v)
    }

    private fun approximateTransit(hT: Double, lw: Double, n: Double): Double {
        return j0 + (hT + lw) / (2.0 * PI) + n
    }

    private fun solarTransitJ(ds: Double, m: Double, l: Double): Double {
        return j2000 + ds + 0.0053 * sin(m) - 0.0069 * sin(2.0 * l)
    }

    // returns set time for the given sun altitude
    private fun getSetJ(h: Double, lw: Double, phi: Double, dec: Double, n: Double, M: Double, L: Double): Double {
        val w = hourAngle(h, phi, dec)
        val a = approximateTransit(w, lw, n)
        return solarTransitJ(a, M, L)
    }

    private fun hourAngle(h: Double, phi: Double, d: Double): Double {
        return acos((sin(h) - sin(phi) * sin(d)) / (cos(phi) * cos(d)))
    }

    private fun hoursLater(date: Calendar, h: Double): Calendar {
        //val cal = date
        //cal.timeInMillis += (h * dayMs / 24).toInt()
        //return cal
        //UtilityLog.d("wx", h.toString())
        val newDate = date.clone() as Calendar
        newDate.add(Calendar.MINUTE, (h * 60).toInt())
        //UtilityLog.d("wx", newDate.toString())
        return newDate

        //return DateTime.fromMillisecondsSinceEpoch(date.millisecondsSinceEpoch + (h * dayMs / 24).toInt())
    }

    private fun moonCoordinates(d: Double): MoonCoordinate {
        val l = rad * (218.316 + 13.176396 * d)
        val m = rad * (134.963 + 13.064993 * d)
        val f = rad * (93.272 + 13.229350 * d)
        val altL = l + rad * 6.289 * sin(m)
        val b = rad * 5.128 * sin(f)
        val dt = 385001.0 - 20905.0 * cos(m)
        return MoonCoordinate(rightAscension(altL, b), declination(altL, b), dt)
    }

    fun sunPosition(date: Calendar, lat: Double , lng: Double): AzimuthCoordinate {
        val lw = rad * -lng
        val phi = rad * lat
        val d = toDays(date)
        UtilityLog.d("wx", d.toString())
        val c = sunCoordinates(d)
        val h = siderealTime(d, lw) - c.rightAscension
        return AzimuthCoordinate(azimuth(h, phi, c.declination), altitude(h, phi, c.declination))
    }

   /* private fun moonPosition(date: Calendar, location: LatLon): MoonPosition {
        val lw = rad * location.lon * -1.0
        val phi = rad * location.lat
        val d = toDays(date)
        val c = moonCoordinates(d)
        //UtilityLog.d("wx", "base time0: " + c.rightAscension.toString())
        //UtilityLog.d("wx", "base time0: " + c.declination.toString())
        val h = siderealTime(d, lw) - c.rightAscension
        var h1 = altitude(h, phi, c.declination)
        val pa = atan2(sin(h), tan(phi) * cos(c.declination) - sin(c.declination) * cos(h))
        h1 += astroRefraction(h1)
        return MoonPosition(azimuth(h, phi, c.declination), h1, c.distance, pa)
    }*/

    private fun moonPosition(date: Calendar, latlon: LatLon): MoonPosition {
        val lw = rad * -1.0 * latlon.lon
        val phi = rad * latlon.lat
        val d = toDays(date)
        UtilityLog.d("Wx", "days: $d")
        val c = moonCoordinates(d)
        //print(c.rightAscension);
        //print(c.declination);
        val H = siderealTime(d, lw) - c.rightAscension

        var h = altitude(H, phi, c.declination)
        //UtilityLog.d("wx", "alt H: " + H.toString())
        //UtilityLog.d("wx", "alt phi: " + phi.toString())
        //UtilityLog.d("wx", "alt dec: " + c.declination.toString())
       // UtilityLog.d("wx", "alt H in moon position: " + h.toString()  + " "+  H.toString() + " " + phi.toString() + " " + c.declination.toString() + " " + d.toString())
        // formula 14.1 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        val pa = atan2(sin(H), tan(phi) * cos(c.declination) - sin(c.declination) * cos(H))
        h += astroRefraction(h) // altitude correction for refraction
        //UtilityLog.d("wx", "altH: " + h)
        return MoonPosition(azimuth(H, phi, c.declination), h, c.distance, pa)
    }

    fun moonIllumination(date: Calendar): MoonIllumination {
        val d = toDays(date)
        val s = sunCoordinates(d)
        val m = moonCoordinates(d)
        val sDist = 149598000.0 // Distance from earth to sun
        val phi = acos(
                sin(s.declination)
                        * sin(m.declination)
                        + cos(s.declination) * cos(m.declination) * cos(s.rightAscension - m.rightAscension)
        )
        val inc = atan2(sDist * sin(phi), m.distance - sDist * cos(phi))
        val angle = atan2(
                cos(s.declination) * sin(s.rightAscension - m.rightAscension),
                sin(s.declination)
                        * cos(m.declination)
                        - cos(s.declination) * sin(m.declination) * cos(s.rightAscension - m.rightAscension)
        )
        val retVal = if (angle < 0.0) {
            -1.0
        } else {
            1.0
        }
        return MoonIllumination((1.0 + cos(inc)) / 2.0, 0.5 + 0.5 * inc * retVal / PI, angle)
    }

    fun time(date: Calendar, event: SolarEvent, location: LatLon): Calendar {
        val lw = rad * location.lon * -1.0
        val phi = rad * location.lat
        val d = toDays(date)
        UtilityLog.d("wx", d.toString())
        val n = julianCycle(d, lw)
        val ds = approximateTransit(0.0, lw, n)
        val m = solarMeanAnomaly(ds)
        val l = eclipticLongitude(m)
        val dec = declination(l, 0.0)
        val jNoon = solarTransitJ(ds, m,  l)
        val noon = fromJulian(jNoon)
        val angle = solarAngle(event)
        val jSet = getSetJ(angle * rad, lw, phi, dec, n, m,  l)
        when (event) {
            SolarEvent.Noon -> return noon
            SolarEvent.Nadir -> return fromJulian(jNoon - 0.5)
            SolarEvent.Sunset -> return fromJulian(jSet)
            SolarEvent.Dusk -> return fromJulian(jSet)
            SolarEvent.GoldenHour -> return fromJulian(jSet)
            SolarEvent.AstronomicalDusk -> return fromJulian(jSet)
            SolarEvent.NauticalDusk -> return fromJulian(jSet)
            SolarEvent.Sunrise -> {
                val jRise = jNoon -(jSet - jNoon)
                return fromJulian(jRise)
            }
            SolarEvent.Dawn -> {
                val jRise = jNoon -(jSet - jNoon)
                return fromJulian(jRise)
            }
            SolarEvent.GoldenHourEnd -> {
                val jRise = jNoon -(jSet - jNoon)
                return fromJulian(jRise)
            }
            SolarEvent.AstronomicalDawn -> {
                val jRise = jNoon -(jSet - jNoon)
                return fromJulian(jRise)
            }
            SolarEvent.NauticalDawn -> {
                val jRise = jNoon -(jSet - jNoon)
                return fromJulian(jRise)
            }
            else -> return Calendar.getInstance()
        }
    }

    private fun solarAngle(event: SolarEvent): Double {
        when (event) {
            SolarEvent.Sunrise -> return -0.833
            SolarEvent.Sunset -> return -0.833
            SolarEvent.SunriseEnd -> return -0.3
            SolarEvent.SunsetEnd -> return -0.3
            SolarEvent.Dawn -> return -6.0
            SolarEvent.Dusk -> return -6.0
            SolarEvent.NauticalDawn -> return -12.0
            SolarEvent.NauticalDusk -> return -12.0
            SolarEvent.AstronomicalDawn -> return -18.0
            SolarEvent.AstronomicalDusk -> return -18.0
            SolarEvent.GoldenHourEnd -> return 6.0
            SolarEvent.GoldenHour -> return 6.0
            SolarEvent.Noon -> return 90.0
            SolarEvent.Nadir -> return -90.0
        }
    }

    // calculations for moon rise/set times are based on http://www.stargazing.net/kepler/moonrise.html article
    fun moonTimes(date: Calendar, location: LatLon): List<Calendar?> {
        //val t = DateTime(date.year, date.month, date.day)
        // thanks https://stackoverflow.com/questions/6850874/how-to-create-a-java-date-object-of-midnight-today-and-midnight-tomorrow
        // today
        //val t = GregorianCalendar()

        //val t = Calendar.getInstance()
        //t.time = date.time
        val t = date.clone() as Calendar
        t.set(Calendar.HOUR_OF_DAY, 0)
        t.set(Calendar.MINUTE, 0)
        t.set(Calendar.SECOND, 0)
        t.set(Calendar.MILLISECOND, 0)


        val hc = 0.133 * rad
        var h0 = moonPosition(t, location).altitude - hc
        UtilityLog.d("wx", "base time: $h0")
        var ye: Double
        var d: Double
        var roots: Int
        var x1 = 0.0
        var x2 = 0.0
        var dx: Double
        var riseHour: Double? = null
        var setHour: Double? = null
        // go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses zero (which means rise or set)
        //for (var i = 1; i <= 24; i += 2) {
        for (i in 1 until 24 step 2) {
            val h1 = moonPosition(hoursLater(t, i.toDouble()), location).altitude - hc
            val h2 = moonPosition(hoursLater(t, i.toDouble() + 1), location).altitude - hc
            val a = (h0 + h2) / 2 - h1
            val b = (h2 - h0) / 2
            val xe = -b / (2 * a)
            ye = (a * xe + b) * xe + h1
            d = b * b - 4 * a * h1
            roots = 0
            if (d >= 0) {
                dx = sqrt(d) / (abs(a) * 2)
                x1 = xe - dx
                x2 = xe + dx
                if (abs(x1) <= 1) {
                    roots++
                }
                if (abs(x2) <= 1) {
                    roots++
                }
                if (x1 < -1) {
                    x1 = x2
                }
            }
            if (roots == 1) {
                if (h0 < 0) {
                    riseHour = i + x1
                } else {
                    setHour = i + x1
                }

            } else if (roots == 2) {
                var add: Double = if (ye < 0){
                    x2
                } else {
                    x1
                }
                riseHour = i + add
                add = if (ye < 0) {
                    x1
                } else {
                    x2
                }
                setHour = i + add
            }
            if (riseHour != null && setHour != null) {
                break
            }
            h0 = h2
        }
        UtilityLog.d("wx", riseHour.toString())
        UtilityLog.d("wx", setHour.toString())
        val result: MutableList<Calendar?> = mutableListOf(null, null)
        if (riseHour != null)
            result[0] = hoursLater(t, riseHour)
        if (setHour != null)
            result[1] = hoursLater(t, setHour)
        if (riseHour == null && setHour == null) {
            return listOf(null, null)
        }
        return result
    }
}


