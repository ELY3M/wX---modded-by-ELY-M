// The following is applicable to files in src/main/kotlin/joshuatee/wx/externalSolarized/
// downloaded from https://github.com/phototime/solarized-android on 2022-11-05

//new file:   app/src/main/java/joshuatee/wx/externalSolarized/Algorithm.kt
//new file:   app/src/main/java/joshuatee/wx/externalSolarized/Models.kt
//new file:   app/src/main/java/joshuatee/wx/externalSolarized/Solarized.kt
//
//
//DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
//Version 2, December 2004
//
//Copyright (C) 2021 Yaroslav Zotov m@zotov.dev
//
//Everyone is permitted to copy and distribute verbatim or modified
//copies of this license document, and changing it is allowed as long
//as the name is changed.
//
//DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
//TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
//
//0. You just DO WHAT THE FUCK YOU WANT TO.

//package dev.zotov.phototime.solarized
@file:Suppress("LocalVariableName")

package joshuatee.wx.externalSolarized

//import androidx.annotation.FloatRange
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
//import java.time.temporal.TemporalUnit
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * The core of the entire library.
 * Used to calculate everything using the power of math and astronomy
 *
 * The algorithm is based on the Ed Williams Sunrise/Sunset algorithm
 * More info can be found [here](http://edwilliams.org/sunrise_sunset_algorithm.html)
 *
 * @param time which datetime will used to calculate
 * @param date the date on which the position of the sun should be calculated
 * @param latitude of the viewer on earth
 * @param longitude of the viewer on earth
 * @param twilight what angle sun should reach
 */
internal fun algorithm(
        time: DateTime,
        date: LocalDateTime,
//    @FloatRange(from = -90.0, to = 90.0) latitude: Double,
//    @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        latitude: Double,
        longitude: Double,
        twilight: Twilight,
        timeZone: TimeZone
): LocalDateTime? {

    // first calculate the day of the year
    val day = date.atZone(ZoneOffset.UTC).dayOfYear

    // longitude to hour value and calculate an approx. time
    val lngHour = longitude / 15
    val hourTime = if (time == DateTime.Morning) 6.0 else 18.0
    val t = day + (hourTime - lngHour) / 24

    // Calculate the suns mean anomaly
    val M = (0.9856 * t) - 3.289

    // Calculate the sun's true longitude
    val subexpression1 = 1.916 * sin(M.radians)
    val subexpression2 = 0.020 * sin(2 * M.radians)
    var L = M + subexpression1 + subexpression2 + 282.634
    L = L.normalise(360.0)

    // sun's right ascension
    var RA = atan(0.91764 * tan(L.radians)).degrees
    RA = RA.normalise(360.0)

    // RA value needs to be in the same quadrant as L
    val Lquadrant = floor(L / 90) * 90
    val RAquadrant = floor(RA / 90) * 90
    RA += (Lquadrant - RAquadrant)
    // RA into hours
    RA /= 15

    // declination
    val sinDec = 0.39782 * sin(L.radians)
    val cosDec = cos(asin(sinDec))

    // calculate zenith (point right above viewer)
    val zenith = -1 * twilight.degrees + 90

    // local hour angle
    val cosH =
            (cos(zenith.radians) - (sinDec * sin(latitude.radians))) / (cosDec * cos(latitude.radians))

    // no transition
    if (cosH > 1 || cosH < -1) return null

    val tempH = if (time == DateTime.Morning) 360 - acos(cosH).degrees else acos(cosH).degrees
    val H = tempH / 15.0

    // local mean time of rising
    val T = H + RA - (0.06571 * t) - 6.622

    val UT = (T - lngHour).normalise(24.0)

    val hour = floor(UT).toInt()
    val minute = floor((UT - hour) * 60.0).toInt()
    val second = ((((UT - hour) * 60) - minute) * 60.0).toInt()
    val shouldBeYesterday = lngHour > 0 && UT > 12 && time == DateTime.Morning
    val shouldBeTomorrow = lngHour < 0 && UT < 12 && time == DateTime.Evening
    val setDate = when {
        shouldBeYesterday -> date.minusDays(1)
        shouldBeTomorrow -> date.plusDays(1)
        else -> date
    }

    val timezoneOffset = TimeUnit.HOURS.convert(
            timeZone.getOffset(date.atZone(ZoneId.of(timeZone.id)).toInstant().toEpochMilli()).toLong(),
            TimeUnit.MILLISECONDS
    )

    return setDate.withHour(hour).withMinute(minute).withSecond(second).plusHours(timezoneOffset)
}


/**
 * Enum used to refine the query and determine for what time calculations are required
 * Calculations such as golden hour, sunrise/sunset, blue hour occur twice a day: in the morning and in the evening
 */
internal enum class DateTime {
    Morning,
    Evening,
}

/** Convert from degrees to radians */
internal val Double.radians: Double get() = this * PI / 180

/** Convert from radians to degrees */
internal val Double.degrees: Double get() = this * 180 / PI

/**
 * If [this] is negative, add [maximum] to [this] until [this] will be positive
 * if [this] > [maximum], subtract [maximum] from [this] until [this] will be less than [maximum]
 */
internal fun Double.normalise(maximum: Double): Double {
    var value = this
    while (value < 0) value += maximum
    while (value > maximum) value -= maximum
    return value
}

/** Define what position on sky relative to the earth sun should reach */
internal sealed class Twilight {
    object Official : Twilight()
    object Civil : Twilight()
    object Nautical : Twilight()
    object Astronomical : Twilight()
    data class Custom(val value: Double) : Twilight()

    /**
     * Angle, formed by sun position, viewer and a ray (emanating from an observer perpendicular to the zenith)
     * If sun under ray (relative to viewer) the [degrees] < 0
     */
    val degrees: Double
        get() {
            return when (this) {
                is Official -> -35.0 / 60.0
                is Civil -> -6.0
                is Nautical -> -12.0
                is Astronomical -> -15.0
                is Custom -> this.value
            }
        }
}

