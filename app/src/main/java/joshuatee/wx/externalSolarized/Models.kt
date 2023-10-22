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

package joshuatee.wx.externalSolarized

import java.time.LocalDateTime

sealed class SunPhase {
    /**
     * When first light come to viewer. Angle is -18
     * @property date when sun is 18deg below horizon
     */
    data class FirstLight(val date: LocalDateTime) : SunPhase()

    /**
     * When the Sun is at a significant depth below the horizon but the viewer can see light. Angle is -6...-4
     * @property start when sun is 6deg below horizon
     * @property end when sun is 4deg below horizon
     */
    data class BlueHour(val start: LocalDateTime, val end: LocalDateTime) : SunPhase()

    /**
     * When the upper rim of the Sun appears on the horizon in the morning. Angle is 0.83deg
     * @property date when sun is 0.83deg above horizon
     */
    data class Sunrise(val date: LocalDateTime) : SunPhase()

    /**
     * Daylight is redder and softer than when the sun is higher in the sky. Angle is -4...6
     * @property start when sun is 4deg below horizon
     * @property end when sun is 6deg above horizon
     */
    data class GoldenHour(val start: LocalDateTime, val end: LocalDateTime) : SunPhase()

    /**
     * No soft light, just usual day light. Angle is 6...6 (diff sides)
     * @property start when sun is 6deg above horizon (one side)
     * @property end when sun is 6deg above horizon (another side)
     */
    data class Day(val start: LocalDateTime, val end: LocalDateTime) : SunPhase()

    /**
     * When the Sun below the horizon. Angle is Official
     * @property date when sun is 35.0 / 60.0deg below horizon
     */
    data class Sunset(val date: LocalDateTime) : SunPhase()

    /**
     * When last light visible to viewer. Angle is -18.0
     * @property date when sun is 18.0 below horizon
     */
    data class LastLight(val date: LocalDateTime) : SunPhase()
}

/**
 * List, containing all sun phases during daytime
 */
data class SunPhaseList(
        val firstLight: SunPhase.FirstLight?,
        val morningBlueHour: SunPhase.BlueHour,
        val sunrise: SunPhase.Sunrise,
        val morningGoldenHour: SunPhase.GoldenHour,
        val day: SunPhase.Day,
        val eveningGoldenHour: SunPhase.GoldenHour,
        val sunset: SunPhase.Sunset,
        val eveningBlueHour: SunPhase.BlueHour,
        val lastLight: SunPhase.LastLight?,
)

