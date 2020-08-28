/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

import java.util.Locale

import joshuatee.wx.MyApplication

import kotlin.math.*

object UtilityMath {

    fun distanceOfLine(x1: Double, y1: Double, x2: Double, y2: Double) = sqrt((x2 - x1).pow(2.0) + (y2 - y1).pow(2.0))

    fun computeTipPoint(x0: Double, y0: Double, x1: Double, y1: Double, right: Boolean): List<Double> {
        val dx = x1 - x0
        val dy = y1 - y0
        val length = sqrt(dx * dx + dy * dy)
        val dirX = dx / length
        val dirY = dy / length
        val height = sqrt(3.0) / 2 * length
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

    fun computeMidPoint(x0: Double, y0: Double, x1: Double, y1: Double, fraction: Double) = listOf(x0 + fraction * (x1 - x0) , y0 + fraction * (y1 - y0))

    // 42.98888 to 42.99
    fun latLonFix(x: String): String {
        val dblX = x.toDoubleOrNull() ?: 0.0
        var newX = "0.0"
        try {
            newX = String.format(Locale.US, "%.2f", dblX).replace("00$".toRegex(), "").replace("0$".toRegex(), "").replace("\\.$".toRegex(), "")
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return newX
    }

    // convert polar cords to rect
    fun toRect(r: Float, t: Float) = floatArrayOf((r * cos(t / (180.00f / PI))).toFloat(), (r * sin(t / (180.00f / PI))).toFloat())

    fun unitsPressure(value: String): String {
        var num = value.toDoubleOrNull() ?: 0.0
        return if (MyApplication.unitsM) {
            num *= 33.8637526
            String.format(Locale.US, "%.2f", num) + " mb"
        } else {
            String.format(Locale.US, "%.2f", num) + " in"
        }
    }

    fun celsiusToFahrenheit(value: String) = if (MyApplication.unitsF) {
            round(((value.toDoubleOrNull() ?: 0.0) * 9 / 5 + 32)).toInt().toString()
        } else {
            value
        }

    internal fun fahrenheitToCelsius(value: Double) = round(((value - 32) * 5 / 9)).toInt().toString()

    // used by celsiusToFahrenheitTable only
    private fun celsiusToFahrenheitAsInt(value: Int) = round((value * 9 / 5 + 32).toFloat()).toString()

    fun celsiusToFahrenheitTable(): String {
        var table = ""
        val cInit = -40
        for (z in 40 downTo cInit) { table += z.toString() + "  " + celsiusToFahrenheitAsInt(z) + MyApplication.newline }
        return table
    }

    internal fun roundToString(value: Double) = round(value.toFloat()).toInt().toString()

    internal fun pressureMBtoIn(value: String) = String.format(Locale.US, "%.2f", ((value.toDoubleOrNull() ?: 0.0) / 33.8637526)) + " in"

    fun pixPerDegreeLon(centerX: Double, factor: Double): Double {
        val radius = 180 / PI * (1 / cos(Math.toRadians(30.51))) * factor
        return radius * (PI / 180) * cos(Math.toRadians(centerX))
    }

    fun deg2rad(deg: Double) = deg * PI / 180.0

    fun rad2deg(rad: Double) = rad * 180.0 / PI

    fun convertWindDir(direction: Double): String {
        val windDirections = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N")
        val normalizedDirection = direction.toInt() % 360
        val listIndex = round((normalizedDirection.toDouble() / 22.5)).toInt()
        return windDirections[listIndex]
    }

    // https://training.weather.gov/wdtd/tools/misc/beamwidth/index.htm
    fun getRadarBeamHeight(degree: Float, distance: Double) = 3.281 * (sin(Math.toRadians(degree.toDouble())) * distance + distance * distance / 15417.82) * 1000.0

    fun heatIndex(temp: String , rh: String ): String {
        val t = temp.toDoubleOrNull() ?: 0.0
        val r = rh.toDoubleOrNull() ?: 0.0
        return if ( t > 80.0 && r > 40.0 ) {
            val s1 = -42.379
            val s2 = 2.04901523 * t
            val s3 = 10.14333127 * r
            val s4 = 0.22475541 * t * r
            val s5 = 6.83783 * 10.0.pow(-3.0) * t.pow(2.0)
            val s6 = 5.481717 * 10.0.pow(-2.0) * r.pow(2.0)
            val s7 = 1.22874 * 10.0.pow(-3.0) * t.pow(2.0) * r
            val s8 = 8.5282 * 10.0.pow(-4.0) * t * r.pow(2.0)
            val s9 = 1.99 * 10.0.pow(-6.0) * t.pow(2.0) * r.pow(2.0)
            val res1 = (s1 + s2 + s3 - s4 - s5 - s6 + s7 + s8 - s9).roundToInt().toString()
            res1
        } else {
            ""
        }
    }
}
