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

package joshuatee.wx.util

import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import java.util.Locale
import kotlin.math.*

object UtilityMath {

    fun distanceOfLine(x1: Double, y1: Double, x2: Double, y2: Double): Double =
        sqrt((x2 - x1).pow(2.0) + (y2 - y1).pow(2.0))

    fun computeTipPoint(
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        right: Boolean
    ): List<Double> {
        val dx = x1 - x0
        val dy = y1 - y0
        val length = sqrt(dx * dx + dy * dy)
        val dirX = dx / length
        val dirY = dy / length
        val height = sqrt(3.0) / 2.0 * length
        val cx = x0 + dx * 0.5
        val cy = y0 + dy * 0.5
        val pDirX = -dirY
        val rx: Double
        val ry: Double
        if (right) {
            rx = cx + height * pDirX
            ry = cy + height * dirX
        } else {
            rx = cx - height * pDirX
            ry = cy - height * dirX
        }
        return listOf(rx, ry)
    }

    fun computeMidPoint(
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        fraction: Double
    ): List<Double> =
        listOf(x0 + fraction * (x1 - x0), y0 + fraction * (y1 - y0))

    // 42.98888 to 42.99
    fun latLonFix(x: String): String {
        val dblX = To.double(x)
        var newX = "0.0"
        try {
            newX = String.format(Locale.US, "%.2f", dblX).replace("00$".toRegex(), "")
                .replace("0$".toRegex(), "").replace("\\.$".toRegex(), "")
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return newX
    }

    // convert polar cords to rect
    fun toRect(r: Float, t: Float): FloatArray =
        floatArrayOf((r * cos(t / (180.0f / PI))).toFloat(), (r * sin(t / (180.0f / PI))).toFloat())

    fun unitsPressure(value: String): String {
        val num = To.double(value)
        return if (UIPreferences.unitsM) {
            To.stringFromFloatFixed(num * 33.8637526, 2)
        } else {
            To.stringFromFloatFixed(num, 2) + " in"
        }
    }

    fun celsiusToFahrenheit(value: String): String = if (UIPreferences.unitsF) {
        (To.double(value) * 9.0 / 5.0 + 32.0).roundToInt().toString()
    } else {
        value
    }

    fun knotsToMph(value: String): String =
        (To.double(value) * 1.151).roundToInt().toString()

    internal fun fahrenheitToCelsius(value: Double): String =
        ((value - 32.0) * 5.0 / 9.0).roundToInt().toString()

    // used by celsiusToFahrenheitTable only
    private fun celsiusToFahrenheitAsInt(value: Int): String =
        (value * 9.0 / 5.0 + 32.0).roundToInt().toString()

    fun celsiusToFahrenheitTable(): String =
        (40 downTo -40).joinToString("") {
            it.toString() + "  " + celsiusToFahrenheitAsInt(it) + GlobalVariables.newline
        }

    internal fun pressureMBtoIn(value: String): String =
        To.stringFromFloatFixed((To.double(value) / 33.8637526), 2) + " in"

    fun pixPerDegreeLon(centerX: Double, factor: Double): Double {
        val radius = 180.0 / PI * (1.0 / cos(Math.toRadians(30.51))) * factor
        return radius * (PI / 180.0) * cos(Math.toRadians(centerX))
    }

    fun deg2rad(deg: Double): Double = deg * PI / 180.0

    fun rad2deg(rad: Double): Double = rad * 180.0 / PI

    private val windDirections = listOf(
        "N",
        "NNE",
        "NE",
        "ENE",
        "E",
        "ESE",
        "SE",
        "SSE",
        "S",
        "SSW",
        "SW",
        "WSW",
        "W",
        "WNW",
        "NW",
        "NNW",
        "N"
    )

    fun bearingToDirection(direction: Int): String {
        val normalizedDirection = direction % 360
        val listIndex = (normalizedDirection.toDouble() / 22.5).roundToInt()
        return windDirections[listIndex]
    }

    // https://training.weather.gov/wdtd/tools/misc/beamwidth/index.htm
    fun getRadarBeamHeight(degree: Double, distance: Double): Double =
        3.281 * (sin(Math.toRadians(degree)) * distance + distance * distance / 15417.82) * 1000.0

    fun heatIndex(temp: String, rh: String): String {
        val t = To.double(temp)
        val r = To.double(rh)
        return if (t > 80.0 && r > 40.0) {
            val s1 = -42.379
            val s2 = 2.04901523 * t
            val s3 = 10.14333127 * r
            val s4 = 0.22475541 * t * r
            val s5 = 6.83783 * 10.0.pow(-3.0) * t.pow(2.0)
            val s6 = 5.481717 * 10.0.pow(-2.0) * r.pow(2.0)
            val s7 = 1.22874 * 10.0.pow(-3.0) * t.pow(2.0) * r
            val s8 = 8.5282 * 10.0.pow(-4.0) * t * r.pow(2.0)
            val s9 = 1.99 * 10.0.pow(-6.0) * t.pow(2.0) * r.pow(2.0)
            (s1 + s2 + s3 - s4 - s5 - s6 + s7 + s8 - s9).roundToInt().toString()
        } else {
            ""
        }
    }

    //
    // legacy forecast
    //
    fun unitsTemp(value: String): String {
        if (!UIPreferences.unitsF) {
            val tmpNum = To.double(value)
            fahrenheitToCelsius(tmpNum)
        }
        return value
    }
}
