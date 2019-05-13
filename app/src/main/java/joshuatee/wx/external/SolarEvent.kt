/*

Kotlin port of https://github.com/imvenj/SunCalc.swift
Please see COPYING.SunCalc for license specified at above URL

which was ported from:
 (c) 2011-2015, Vladimir Agafonkin
 SunCalc is a JavaScript library for calculating sun/moon position and light phases.
 https://github.com/mourner/suncalc


 */

package joshuatee.wx.external

enum class SolarEvent {
    Sunrise,
    Sunset,
    SunriseEnd,
    SunsetEnd,
    Dawn,
    Dusk,
    NauticalDawn,
    NauticalDusk,
    AstronomicalDawn,
    AstronomicalDusk,
    GoldenHourEnd,
    GoldenHour,
    Noon,
    Nadir
}

