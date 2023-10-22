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

// joshua.tee@gmail.com made 3 changes below 2022-11-06

//package dev.zotov.phototime.solarized
//modded by ELY M.

package joshuatee.wx.externalSolarized

//import androidx.annotation.FloatRange
import java.time.LocalDateTime
import java.util.TimeZone

/**
 * Main class that used to calculate sun phases like golden hour, blue hour, sunrise, sunset etc
 *
 * @param latitude the latitude at which the device is located
 * @param longitude the longitude at which the device is located
 * @param date the date that will be used in the calculation of phases
 * @param timeZone of LocalDateTime
 */
class Solarized(
//    @FloatRange(from = -90.0, to = 90.0) val latitude: Double,
//    @FloatRange(from = -180.0, to = 180.0) val longitude: Double,
        val latitude: Double,
        val longitude: Double,
        val date: LocalDateTime,
        val timeZone: TimeZone = TimeZone.getDefault()
) {

    val list: SunPhaseList?
        get() {
            val blueHourMorningStartDate = algorithm(
                    time = DateTime.Morning,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-7.9),
                    timeZone = timeZone,
            ) ?: return null

            val blueHourMorningEndDate = algorithm(
                    // and goldenHourMorningStartDate too
                    time = DateTime.Morning,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-4.0),
                    timeZone = timeZone,
            ) ?: return null

            val goldenHourMorningEndDate = algorithm(
                    // and dayStartDate too
                    time = DateTime.Morning,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(6.0),
                    timeZone = timeZone,
            ) ?: return null

            val goldenHourEveningStartDate = algorithm(
                    // and dayEndDate too
                    time = DateTime.Evening,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(6.0),
                    timeZone = timeZone,
            ) ?: return null

            val goldenHourEveningEndDate = algorithm(
                    // and blueHourEveningStartDate too
                    time = DateTime.Evening,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-4.0),
                    timeZone = timeZone,
            ) ?: return null

            val blueHourEveningEndDate = algorithm(
                    time = DateTime.Evening,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-7.9),
                    timeZone = timeZone,
            ) ?: return null

            return SunPhaseList(
                    firstLight = firstLight,
                    morningBlueHour = SunPhase.BlueHour(
                            start = blueHourMorningStartDate,
                            end = blueHourMorningEndDate
                    ),
                    sunrise = sunrise ?: return null,
                    morningGoldenHour = SunPhase.GoldenHour(
                            start = blueHourMorningEndDate,
                            end = goldenHourMorningEndDate
                    ),
                    day = SunPhase.Day(
                            start = goldenHourMorningEndDate,
                            end = goldenHourEveningStartDate,
                    ),
                    eveningGoldenHour = SunPhase.GoldenHour(
                            start = goldenHourEveningStartDate,
                            end = goldenHourEveningEndDate,
                    ),
                    sunset = sunset ?: return null,
                    eveningBlueHour = SunPhase.BlueHour(
                            start = goldenHourEveningEndDate,
                            end = blueHourEveningEndDate,
                    ),
                    lastLight = lastLight,
            )
        }

    @Suppress("unused")
            /** [SunPhase.GoldenHour] daylight is redder and softer than when the sun is higher in the sky */
    val goldenHour = object : TwiceADaySunPhases<SunPhase.GoldenHour> {
        private fun base(time: DateTime): SunPhase.GoldenHour? {
            val goldenHourStartDate = algorithm(
                    time = time,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(6.0),
                    timeZone = timeZone,
            ) ?: return null
            val goldenHourEndDate = algorithm(
                    time = time,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-4.0),
                    timeZone = timeZone,
            ) ?: return null

            return if (time == DateTime.Evening)
                SunPhase.GoldenHour(goldenHourStartDate, goldenHourEndDate)
            else
                SunPhase.GoldenHour(goldenHourEndDate, goldenHourStartDate)
        }

        /** Morning golden hour */
        override val morning: SunPhase.GoldenHour?
            get() = base(DateTime.Morning)

        /** Morning golden hour */
        override val evening: SunPhase.GoldenHour?
            get() = base(DateTime.Evening)
    }

    @Suppress("unused")
            /** [SunPhase.BlueHour] when the Sun is at a significant depth below the horizon but the viewer can see light */
    val blueHour = object : TwiceADaySunPhases<SunPhase.BlueHour> {
        private fun base(time: DateTime): SunPhase.BlueHour? {
            val blueHourStartDate = algorithm(
                    time = time,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-4.0),
                    timeZone = timeZone,
            ) ?: return null
            val blueHourEndDate = algorithm(
                    time = time,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-7.9),
                    timeZone = timeZone,
            ) ?: return null

            return if (time == DateTime.Evening)
                SunPhase.BlueHour(blueHourStartDate, blueHourEndDate)
            else
                SunPhase.BlueHour(blueHourEndDate, blueHourStartDate)
        }

        /** Morning golden hour */
        override val morning: SunPhase.BlueHour?
            get() = base(DateTime.Morning)

        /** Morning golden hour */
        override val evening: SunPhase.BlueHour?
            get() = base(DateTime.Evening)
    }


    /** [SunPhase.FirstLight] when first light come to viewer */
    val firstLight: SunPhase.FirstLight?
        get() {
            val firstLightDate = algorithm(
                    time = DateTime.Morning,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Civil, // joshua.tee@gmail.com was Twilight.Astronomical
                    timeZone = timeZone,
            ) ?: return null
            return SunPhase.FirstLight(firstLightDate)
        }

    /** [SunPhase.Sunrise] when the upper rim of the Sun appears on the horizon in the morning */
    val sunrise: SunPhase.Sunrise?
        get() {
            val sunriseDate = algorithm(
                    time = DateTime.Morning,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(-0.83),
                    timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunrise(sunriseDate)
        }


    /** [SunPhase.Day] no soft light, just usual day light */
    val day: SunPhase.Day?
        get() {
            val dayStartDate = algorithm(
                    time = DateTime.Morning,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(6.0),
                    timeZone = timeZone,
            ) ?: return null
            val dayEndDate = algorithm(
                    time = DateTime.Evening,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Custom(6.0),
                    timeZone = timeZone,
            ) ?: return null
            return SunPhase.Day(dayStartDate, dayEndDate)
        }

    /** [SunPhase.Sunset] when the Sun below the horizon */
    val sunset: SunPhase.Sunset?
        get() {
            val sunsetDate = algorithm(
                    time = DateTime.Evening,
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Official,
                    timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunset(sunsetDate)
        }

    /** [SunPhase.LastLight] When last light visible to viewer */
    val lastLight: SunPhase.LastLight?
        get() {
            val sunsetDate = algorithm(
                    time = DateTime.Evening, // joshua.tee@gmail.com was DateTime.Morning
                    date = date,
                    latitude = latitude,
                    longitude = longitude,
                    twilight = Twilight.Civil, // joshua.tee@gmail.com was Twilight.Astronomical
                    timeZone = timeZone,
            ) ?: return null
            return SunPhase.LastLight(sunsetDate)
        }


//elys mod

    //sunrise
    val AstronomicalRise: SunPhase.Sunrise?
        get() {
            val Date = algorithm(
                time = DateTime.Morning,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Astronomical,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunrise(Date)
        }
    val NauticalRise: SunPhase.Sunrise?
        get() {
            val Date = algorithm(
                time = DateTime.Morning,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Nautical,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunrise(Date)
        }
    val CivilRise: SunPhase.Sunrise?
        get() {
            val Date = algorithm(
                time = DateTime.Morning,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Civil,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunrise(Date)
        }
    val SunRise: SunPhase.Sunrise?
        get() {
            val Date = algorithm(
                time = DateTime.Morning,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Official,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunrise(Date)
        }

    //sunset
    val AstronomicalSet: SunPhase.Sunset?
        get() {
            val Date = algorithm(
                time = DateTime.Evening,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Astronomical,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunset(Date)
        }
    val NauticalSet: SunPhase.Sunset?
        get() {
            val Date = algorithm(
                time = DateTime.Evening,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Nautical,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunset(Date)
        }
    val CivilSet: SunPhase.Sunset?
        get() {
            val Date = algorithm(
                time = DateTime.Evening,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Civil,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunset(Date)
        }
    val SunSet: SunPhase.Sunset?
        get() {
            val Date = algorithm(
                time = DateTime.Evening,
                date = date,
                latitude = latitude,
                longitude = longitude,
                twilight = Twilight.Official,
                timeZone = timeZone,
            ) ?: return null
            return SunPhase.Sunset(Date)
        }
/////////////end of elys mod






}

interface TwiceADaySunPhases<T : SunPhase> {
    val morning: T?
    val evening: T?
}