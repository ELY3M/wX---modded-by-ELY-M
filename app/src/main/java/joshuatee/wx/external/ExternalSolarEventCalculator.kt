/*
 * Copyright 2008-2009 Mike Reedell / LuckyCatLabs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package joshuatee.wx.external

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Calendar
import java.util.TimeZone

//import com.luckycatlabs.sunrisesunset.Zenith;
//import ExternalSunriseLocation;

/**
 * Parent class of the Sunrise and Sunset calculator classes.
 */
internal class ExternalSolarEventCalculator {
    private val location: ExternalSunriseLocation
    private val timeZone: TimeZone

    /**
     * Computes the base longitude hour, lngHour in the algorithm.
     *
     * @return the longitude of the location of the solar event divided by 15 (deg/hour), in
     * `BigDecimal` form.
     */
    private val baseLongitudeHour: BigDecimal
        get() = divideBy(location.longitude, BigDecimal.valueOf(15))

    /**
     * Constructs a new `SolarEventCalculator` using the given parameters.
     *
     * @param location
     * `Location` of the place where the solar event should be calculated from.
     * @param timeZoneIdentifier
     * time zone identifier of the timezone of the location parameter. For example,
     * "America/New_York".
     */
    constructor(location: ExternalSunriseLocation, timeZoneIdentifier: String) {
        this.location = location
        this.timeZone = TimeZone.getTimeZone(timeZoneIdentifier)
    }

    /**
     * Constructs a new `SolarEventCalculator` using the given parameters.
     *
     * @param location
     * `Location` of the place where the solar event should be calculated from.
     * @param timeZone
     * timezone of the location parameter.
     */
    constructor(location: ExternalSunriseLocation, timeZone: TimeZone) {
        this.location = location
        this.timeZone = timeZone
    }

    /**
     * Computes the sunrise time for the given zenith at the given date.
     *
     * `Zenith` enum corresponding to the type of sunrise to compute.
     * `Calendar` object representing the date to compute the sunrise for.
     * @return the sunrise time, in HH:MM format (24-hour clock), 00:00 if the sun does not rise on the given
     * date.
     */
    //fun computeSunriseTime(solarZenith: ExternalZenith, date: Calendar): String {
    //    return getLocalTimeAsString(computeSolarEventTime(solarZenith, date, true))
    //}

    /**
     * Computes the sunrise time for the given zenith at the given date.
     *
     * @param solarZenith
     * `Zenith` enum corresponding to the type of sunrise to compute.
     * @param date
     * `Calendar` object representing the date to compute the sunrise for.
     * @return the sunrise time as a calendar or null for no sunrise
     */
    fun computeSunriseCalendar(solarZenith: ExternalZenith, date: Calendar): Calendar? {
        return getLocalTimeAsCalendar(computeSolarEventTime(solarZenith, date, true), date)
    }

    /**
     * Computes the sunset time for the given zenith at the given date.
     *
     * @param solarZenith
     * `Zenith` enum corresponding to the type of sunset to compute.
     * @param date
     * `Calendar` object representing the date to compute the sunset for.
     * @return the sunset time, in HH:MM format (24-hour clock), 00:00 if the sun does not set on the given
     * date.
     */
    /*fun computeSunsetTime(solarZenith: ExternalZenith, date: Calendar): String {
        return getLocalTimeAsString(computeSolarEventTime(solarZenith, date, false))
    }*/

    /**
     * Computes the sunset time for the given zenith at the given date.
     *
     * @param solarZenith
     * `Zenith` enum corresponding to the type of sunset to compute.
     * @param date
     * `Calendar` object representing the date to compute the sunset for.
     * @return the sunset time as a Calendar or null for no sunset.
     */
    fun computeSunsetCalendar(solarZenith: ExternalZenith, date: Calendar): Calendar? {
        return getLocalTimeAsCalendar(computeSolarEventTime(solarZenith, date, false), date)
    }

    private fun computeSolarEventTime(
        solarZenith: ExternalZenith,
        date: Calendar,
        isSunrise: Boolean
    ): BigDecimal? {
        date.timeZone = this.timeZone
        val longitudeHour = getLongitudeHour(date, isSunrise)

        val meanAnomaly = getMeanAnomaly(longitudeHour)
        val sunTrueLong = getSunTrueLongitude(meanAnomaly)
        val cosineSunLocalHour = getCosineSunLocalHour(sunTrueLong, solarZenith)
        if (cosineSunLocalHour.toDouble() < -1.0 || cosineSunLocalHour.toDouble() > 1.0) {
            return null
        }

        val sunLocalHour = getSunLocalHour(cosineSunLocalHour, isSunrise)
        val localMeanTime = getLocalMeanTime(sunTrueLong, longitudeHour, sunLocalHour)
        return getLocalTime(localMeanTime, date)
    }

    /**
     * Computes the longitude time, t in the algorithm.
     *
     * @return longitudinal time in `BigDecimal` form.
     */
    private fun getLongitudeHour(date: Calendar, isSunrise: Boolean?): BigDecimal {
        var offset = 18
        if (isSunrise!!) {
            offset = 6
        }
        val dividend = BigDecimal.valueOf(offset.toLong()).subtract(baseLongitudeHour)
        val addend = divideBy(dividend, BigDecimal.valueOf(24))
        val longHour = getDayOfYear(date).add(addend)
        return setScale(longHour)
    }

    /**
     * Computes the mean anomaly of the Sun, M in the algorithm.
     *
     * @return the suns mean anomaly, M, in `BigDecimal` form.
     */
    private fun getMeanAnomaly(longitudeHour: BigDecimal): BigDecimal {
        val meanAnomaly =
            multiplyBy(BigDecimal("0.9856"), longitudeHour).subtract(BigDecimal("3.289"))
        return setScale(meanAnomaly)
    }

    /**
     * Computes the true longitude of the sun, L in the algorithm, at the given location, adjusted to fit in
     * the range [0-360].
     *
     * @param meanAnomaly
     * the suns mean anomaly.
     * @return the suns true longitude, in `BigDecimal` form.
     */
    private fun getSunTrueLongitude(meanAnomaly: BigDecimal): BigDecimal {
        val sinMeanAnomaly = BigDecimal(Math.sin(convertDegreesToRadians(meanAnomaly).toDouble()))
        val sinDoubleMeanAnomaly = BigDecimal(
            Math.sin(
                multiplyBy(convertDegreesToRadians(meanAnomaly), BigDecimal.valueOf(2))
                    .toDouble()
            )
        )

        val firstPart = meanAnomaly.add(multiplyBy(sinMeanAnomaly, BigDecimal("1.916")))
        val secondPart =
            multiplyBy(sinDoubleMeanAnomaly, BigDecimal("0.020")).add(BigDecimal("282.634"))
        var trueLongitude = firstPart.add(secondPart)

        if (trueLongitude.toDouble() > 360) {
            trueLongitude = trueLongitude.subtract(BigDecimal.valueOf(360))
        }
        return setScale(trueLongitude)
    }

    /**
     * Computes the suns right ascension, RA in the algorithm, adjusting for the quadrant of L and turning it
     * into degree-hours. Will be in the range [0,360].
     *
     * @param sunTrueLong
     * Suns true longitude, in `BigDecimal`
     * @return suns right ascension in degree-hours, in `BigDecimal` form.
     */
    private fun getRightAscension(sunTrueLong: BigDecimal): BigDecimal {
        val tanL = BigDecimal(Math.tan(convertDegreesToRadians(sunTrueLong).toDouble()))

        val innerParens = multiplyBy(convertRadiansToDegrees(tanL), BigDecimal("0.91764"))
        var rightAscension = BigDecimal(Math.atan(convertDegreesToRadians(innerParens).toDouble()))
        rightAscension = setScale(convertRadiansToDegrees(rightAscension))

        if (rightAscension.toDouble() < 0) {
            rightAscension = rightAscension.add(BigDecimal.valueOf(360))
        } else if (rightAscension.toDouble() > 360) {
            rightAscension = rightAscension.subtract(BigDecimal.valueOf(360))
        }

        val ninety = BigDecimal.valueOf(90)
        var longitudeQuadrant = sunTrueLong.divide(ninety, 0, RoundingMode.FLOOR)
        longitudeQuadrant = longitudeQuadrant.multiply(ninety)

        var rightAscensionQuadrant = rightAscension.divide(ninety, 0, RoundingMode.FLOOR)
        rightAscensionQuadrant = rightAscensionQuadrant.multiply(ninety)

        val augend = longitudeQuadrant.subtract(rightAscensionQuadrant)
        return divideBy(rightAscension.add(augend), BigDecimal.valueOf(15))
    }

    private fun getCosineSunLocalHour(sunTrueLong: BigDecimal, zenith: ExternalZenith): BigDecimal {
        val sinSunDeclination = getSinOfSunDeclination(sunTrueLong)
        val cosineSunDeclination = getCosineOfSunDeclination(sinSunDeclination)

        val zenithInRads = convertDegreesToRadians(zenith.degrees())
        val cosineZenith = BigDecimal.valueOf(Math.cos(zenithInRads.toDouble()))
        val sinLatitude =
            BigDecimal.valueOf(Math.sin(convertDegreesToRadians(location.latitude).toDouble()))
        val cosLatitude =
            BigDecimal.valueOf(Math.cos(convertDegreesToRadians(location.latitude).toDouble()))

        val sinDeclinationTimesSinLat = sinSunDeclination.multiply(sinLatitude)
        val dividend = cosineZenith.subtract(sinDeclinationTimesSinLat)
        val divisor = cosineSunDeclination.multiply(cosLatitude)

        return setScale(divideBy(dividend, divisor))
    }

    private fun getSinOfSunDeclination(sunTrueLong: BigDecimal): BigDecimal {
        val sinTrueLongitude =
            BigDecimal.valueOf(Math.sin(convertDegreesToRadians(sunTrueLong).toDouble()))
        val sinOfDeclination = sinTrueLongitude.multiply(BigDecimal("0.39782"))
        return setScale(sinOfDeclination)
    }

    private fun getCosineOfSunDeclination(sinSunDeclination: BigDecimal): BigDecimal {
        val arcSinOfSinDeclination = BigDecimal.valueOf(Math.asin(sinSunDeclination.toDouble()))
        val cosDeclination = BigDecimal.valueOf(Math.cos(arcSinOfSinDeclination.toDouble()))
        return setScale(cosDeclination)
    }

    private fun getSunLocalHour(cosineSunLocalHour: BigDecimal, isSunrise: Boolean?): BigDecimal {
        val arcCosineOfCosineHourAngle = getArcCosineFor(cosineSunLocalHour)
        var localHour = convertRadiansToDegrees(arcCosineOfCosineHourAngle)
        if (isSunrise!!) {
            localHour = BigDecimal.valueOf(360).subtract(localHour)
        }
        return divideBy(localHour, BigDecimal.valueOf(15))
    }

    private fun getLocalMeanTime(
        sunTrueLong: BigDecimal,
        longitudeHour: BigDecimal,
        sunLocalHour: BigDecimal
    ): BigDecimal {
        val rightAscension = this.getRightAscension(sunTrueLong)
        val innerParens = longitudeHour.multiply(BigDecimal("0.06571"))
        var localMeanTime = sunLocalHour.add(rightAscension).subtract(innerParens)
        localMeanTime = localMeanTime.subtract(BigDecimal("6.622"))

        if (localMeanTime.toDouble() < 0) {
            localMeanTime = localMeanTime.add(BigDecimal.valueOf(24))
        } else if (localMeanTime.toDouble() > 24) {
            localMeanTime = localMeanTime.subtract(BigDecimal.valueOf(24))
        }
        return setScale(localMeanTime)
    }

    private fun getLocalTime(localMeanTime: BigDecimal, date: Calendar): BigDecimal {
        val utcTime = localMeanTime.subtract(baseLongitudeHour)
        val utcOffSet = getUTCOffSet(date)
        val utcOffSetTime = utcTime.add(utcOffSet)
        return adjustForDST(utcOffSetTime, date)
    }

    private fun adjustForDST(localMeanTime: BigDecimal, date: Calendar): BigDecimal {
        var localTime = localMeanTime
        if (timeZone.inDaylightTime(date.time)) {
            localTime = localTime.add(BigDecimal.ONE)
        }
        if (localTime.toDouble() > 24.0) {
            localTime = localTime.subtract(BigDecimal.valueOf(24))
        }
        return localTime
    }

    /**
     * Returns the local rise/set time in the form HH:MM.
     *
     * `BigDecimal` representation of the local rise/set time.
     * @return `String` representation of the local rise/set time in HH:MM format.
     */
    private fun getLocalTimeAsString(localTimeParam: BigDecimal?): String {
        if (localTimeParam == null) {
            return "99:99"
        }

        var localTime: BigDecimal = localTimeParam
        if (localTime.compareTo(BigDecimal.ZERO) == -1) {
            localTime = localTime.add(BigDecimal.valueOf(24.0))
        }
        val timeComponents =
            localTime.toPlainString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var hour = timeComponents[0].toIntOrNull() ?: 0

        var minutes = BigDecimal("0." + timeComponents[1])
        minutes = minutes.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN)
        if (minutes.toInt() == 60) {
            minutes = BigDecimal.ZERO
            hour += 1
        }
        if (hour == 24) {
            hour = 0
        }

        val minuteString =
            if (minutes.toInt() < 10) "0" + minutes.toPlainString() else minutes.toPlainString()
        val hourString = if (hour < 10) "0" + hour.toString() else hour.toString()
        return "$hourString:$minuteString"
    }

    /**
     * Returns the local rise/set time in the form HH:MM.
     *
     * @param localTimeParam
     * `BigDecimal` representation of the local rise/set time.
     * @return `Calendar` representation of the local time as a calendar, or null for none.
     */
    private fun getLocalTimeAsCalendar(localTimeParam: BigDecimal?, date: Calendar): Calendar? {
        if (localTimeParam == null) {
            return null
        }

        // Create a clone of the input calendar so we get locale/timezone information.
        val resultTime = date.clone() as Calendar

        var localTime: BigDecimal = localTimeParam
        if (localTime.compareTo(BigDecimal.ZERO) == -1) {
            localTime = localTime.add(BigDecimal.valueOf(24.0))
            resultTime.add(Calendar.HOUR_OF_DAY, -24)
        }
        val timeComponents =
            localTime.toPlainString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var hour = timeComponents[0].toIntOrNull() ?: 0
        var minutes = BigDecimal("0." + timeComponents[1])
        minutes = minutes.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN)
        if (minutes.toInt() == 60) {
            minutes = BigDecimal.ZERO
            hour += 1
        }
        if (hour == 24) {
            hour = 0
        }
        // Set the local time
        resultTime.set(Calendar.HOUR_OF_DAY, hour)
        resultTime.set(Calendar.MINUTE, minutes.toInt())
        resultTime.set(Calendar.SECOND, 0)
        resultTime.set(Calendar.MILLISECOND, 0)
        resultTime.timeZone = date.timeZone
        return resultTime
    }

    /** ******* UTILITY METHODS (Should probably go somewhere else. *****************  */

    private fun getDayOfYear(date: Calendar): BigDecimal {
        return BigDecimal(date.get(Calendar.DAY_OF_YEAR))
    }

    private fun getUTCOffSet(date: Calendar): BigDecimal {
        val offSetInMillis = BigDecimal(date.get(Calendar.ZONE_OFFSET))
        return offSetInMillis.divide(BigDecimal(3600000), MathContext(2))
    }

    private fun getArcCosineFor(radians: BigDecimal): BigDecimal {
        val arcCosine = BigDecimal.valueOf(Math.acos(radians.toDouble()))
        return setScale(arcCosine)
    }

    private fun convertRadiansToDegrees(radians: BigDecimal): BigDecimal {
        return multiplyBy(radians, BigDecimal(180 / Math.PI))
    }

    private fun convertDegreesToRadians(degrees: BigDecimal): BigDecimal {
        return multiplyBy(degrees, BigDecimal.valueOf(Math.PI / 180.0))
    }

    private fun multiplyBy(multiplicand: BigDecimal, multiplier: BigDecimal): BigDecimal {
        return setScale(multiplicand.multiply(multiplier))
    }

    private fun divideBy(dividend: BigDecimal, divisor: BigDecimal): BigDecimal {
        return dividend.divide(divisor, 4, RoundingMode.HALF_EVEN)
    }

    private fun setScale(number: BigDecimal): BigDecimal {
        return number.setScale(4, RoundingMode.HALF_EVEN)
    }
}
