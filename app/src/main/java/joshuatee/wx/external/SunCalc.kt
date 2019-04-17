/*

Kotlin port of https://github.com/imvenj/SunCalc.swift
Please see COPYING.SunCalc for license specified at above URL

 */

package joshuatee.wx.external

import android.text.format.DateUtils
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

/*

public enum SolarEvent {
    case sunrise
    case sunset
    case sunriseEnd
    case sunsetEnd
    case dawn
    case dusk
    case nauticalDawn
    case nauticalDusk
    case astronomicalDawn
    case astronomicalDusk
    case goldenHourEnd
    case goldenHour
    case noon
    case nadir

    var solarAngle: Double {
        switch self {
        case .sunrise, .sunset: return -0.833
        case .sunriseEnd, .sunsetEnd: return -0.3
        case .dawn, .dusk: return -6.0
        case .nauticalDawn, .nauticalDusk: return -12.0
        case .astronomicalDawn, .astronomicalDusk: return -18.0
        case .goldenHourEnd, .goldenHour: return 6.0
        case .noon: return 90.0
        case .nadir: return -90.0
        }
    }
} */

class SunCalc {

    /*public enum SolarEventError: Error {
        case sunNeverRise
        case sunNeverSet
    }

    public enum LunarEventError: Error {
        case moonNeverRise(Date?)
        case moonNeverSet(Date?)
    }*/

    companion object {
        const val radPerDegree: Double = PI / 180
        private const val e = 23.4397 * radPerDegree
        const val j0: Double = 0.0009
        private const val j1970: Double = 2440588.0
        const val j2000: Double = 2451545.0
        const val secondsPerDay: Double = 86400.0

        private fun julianDays(): Double {
            //return timeIntervalSince1970 / Date.secondsPerDay - 0.5 + j1970
            return (System.currentTimeMillis() / 1000) / DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS - 0.5 + j1970
        }

        fun daysSince2000(): Double {
            return julianDays() - j2000
        }

        //fun hoursLater(h: Double): Date {
        //    return addingTimeInterval(h * 3600.0)
        //}
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
        return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(h))
    }

    private fun siderealTime(d: Double, lw: Double): Double {
        return radPerDegree * (280.16 + 360.9856235 * d) - lw
    }

    private fun astroRefraction(aH: Double): Double {
        val h: Double = if (aH < 0) {
            0.0
        } else {
            aH
        }
        return 0.0002967 / tan(h + 0.00312536 / (h + 0.08901179))
    }

    private fun solarMeanAnomaly(d: Double): Double {
        return radPerDegree * (357.5291 + 0.98560028 * d)
    }

    private fun eclipticLongitude(m: Double): Double {
        val c =
            radPerDegree * (1.9148 * sin(m) + 0.02 * sin(2.0 * m) + 0.0003 * sin(3.0 * m))
        val p = radPerDegree * 102.9372
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

    private fun moonCoordinates(d: Double): MoonCoordinate {
        val l = radPerDegree * (218.316 + 13.176396 * d)
        val m = radPerDegree * (134.963 + 13.064993 * d)
        val f = radPerDegree * (93.272 + 13.229350 * d)
        val altL = l + radPerDegree * 6.289 * sin(m)
        val b = radPerDegree * 5.128 * sin(f)
        val dt = 385001.0 - 20905.0 * cos(m)
        return MoonCoordinate(rightAscension(altL, b), declination(altL, b), dt)
    }

    fun sunPosition(date: Date, location: Location): AzimuthCoordinate {
        val lw = radPerDegree * location.longitude * -1.0
        val phi = radPerDegree * location.latitude
        val d = daysSince2000()
        val c = sunCoordinates(d)
        val h = siderealTime(d, lw) - c.rightAscension
        return AzimuthCoordinate(azimuth(h, phi, c.declination), altitude(h, phi, c.declination))
    }

    fun moonPosition(date: Date, location: Location): MoonPosition {
        val lw = radPerDegree * location.longitude * -1.0
        val phi = radPerDegree * location.latitude
        val d = daysSince2000()
        val c = moonCoordinates(d)
        val h = siderealTime(d, lw) - c.rightAscension
        var h1 = altitude(h, phi, c.declination)
        val pa = atan2(sin(h), tan(phi) * cos(c.declination) - sin(c.declination) * cos(h))
        h1 += astroRefraction(h1)
        return MoonPosition(azimuth(h, phi, c.declination), h1, c.distance, pa)
    }

    fun moonIllumination(date: Date = Date()): MoonIllumination {
        val d = daysSince2000()
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

    /*  private fun hourAngle(h: Double, phi: Double, d: Double): Double {
        val cosH = (sin(h) - sin(phi) * sin(d)) / (cos(phi) * cos(d))
        if (cosH > 1) {
            throw SolarEventError.sunNeverRise
        }
        if (cosH < -1) {
            throw SolarEventError.sunNeverSet
        }
        return acos(cosH)
    }

    private fun getSetJ(
        h: Double,
        lw: Double,
        phi: Double,
        dec: Double,
        n: Double,
        m: Double,
        l: Double
    ): Double {
        val w = hourAngle(h, phi, dec)
        val a = approximateTransit(w, lw, n)
        return solarTransitJ(a, m, l)
    }

    public fun time(date: Date, event: SolarEvent, atLocation location: Location): Date {
        val lw = SunCalc.radPerDegree * location.longitude * -1.0
        val phi = SunCalc.radPerDegree * location.latitude
        val d = SunCalc.daysSince2000()
        val n = julianCycle(d, lw)
        val ds = approximateTransit(0.0, lw, n)
        val m = solarMeanAnomaly(ds)
        val l = eclipticLongitude(m)
        val dec = declination(l, 0.0)
        val jNoon = solarTransitJ(ds, m,  l)
        val noon = Date(jNoon)
        val angle = event.solarAngle
        val jSet = getSetJ(angle * SunCalc.radPerDegree, lw, phi, dec, n, m,  l)
        switch event {
        case .noon: return noon
        case .nadir:
            val nadir = Date(julianDays: jNoon - 0.5)
            return nadir
        case .sunset, .dusk, .goldenHour, .astronomicalDusk, .nauticalDusk:
            return Date(julianDays: jSet)
        case .sunrise, .dawn, .goldenHourEnd, .astronomicalDawn, .nauticalDawn:
            val jRise = jNoon - (jSet - jNoon)
            return Date(julianDays: jRise)
        default:
            return Date()
        }
    }

    //public fun moonTimes(date: Date, location: Location) throws -> (moonRiseTime: Date, moonSetTime: Date) {
    public fun moonTimes(date: Date, location: Location) {
        val date = date.beginning()
        val hc = 0.133 * SunCalc.radPerDegree
        var h0 = moonPosition(date, location).altitude - hc
        var riseHour: Double?
        var setHour: Double?
        var ye: Double = 0.0
        for (i in 1..24) {
            if (i % 2 == 0) {
                continue
            }
            val h1 = moonPosition(date.hoursLater(i.toDouble()), location).altitude - hc
            val h2 = moonPosition(date.hoursLater(i.toDouble() + 1.0), location).altitude - hc
            val a = (h0 + h2) / 2.0 - h1
            val b = (h2 - h0) / 2.0
            val xe = -b / (2.0 * a)
            ye = (a * xe + b) * xe + h1
            val d = b * b - 4.0 * a * h1
            if (d >= 0) {
                val dx = sqrt(d) / (a.absoluteValue * 2.0)
                var roots = 0
                var x1 = xe - dx
                val x2 = xe + dx
                if (x1.absoluteValue < 1.0) { roots += 1 }
                if (x2.absoluteValue < 1.0) { roots += 1 }
                if (x1 < -1.0) { x1 = x2 }
                if (roots == 1) {
                    if (h0 < 0.0) {
                        riseHour = i.toDouble() + x1
                    } else {
                        setHour = i.toDouble() + x1
                    }
                } else if (roots == 2) {
                    riseHour = i.toDouble() + (ye < 0 ? x2 : x1)
                    setHour = i.toDouble() + (ye < 0 ? x1 : x2)
                }

                if (riseHour != null && setHour != null ) {
                    break
                }
            }
            h0 = h2
        }
        if val riseHour = riseHour, val setHour = setHour {
            return (date.hoursLater(riseHour), date.hoursLater(setHour))
        } else {
            if (ye > 0) {
                val rise = (riseHour == nil) ? nil : date.hoursLater(riseHour!)
                throw LunarEventError.moonNeverSet(rise)
            } else {
                val set = (setHour == nil) ? nil : date.hoursLater(setHour!)
                throw LunarEventError.moonNeverRise(set)
            }
        }
    }*/
}
