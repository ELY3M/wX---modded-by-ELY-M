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

package joshuatee.wx.util

import java.util.Locale

import joshuatee.wx.MyApplication

import kotlin.math.*

object UtilityMath {

    // 42.98888 to 42.99
    fun latLonFix(x: String): String {
        val dblX = x.toDoubleOrNull() ?: 0.0
        var newX = "0.0"
        try {
            newX = String.format(Locale.US, "%.2f", dblX)
            newX = newX.replace("00$".toRegex(), "")
            newX = newX.replace("0$".toRegex(), "")
            newX = newX.replace("\\.$".toRegex(), "")
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return newX
    }

    // convert polar cords to rect
    fun toRect(r: Float, t: Float): FloatArray = floatArrayOf(
        (r * cos(t / (180.00f / PI))).toFloat(),
        (r * sin(t / (180.00f / PI))).toFloat()
    )

    fun unitsPressure(valueF: String): String {
        var value = valueF
        var tmpNum = value.toDoubleOrNull() ?: 0.0
        if (MyApplication.unitsM) {
            tmpNum *= 33.8637526
            value = String.format(Locale.US, "%.2f", tmpNum) + " mb"
        } else {
            value = String.format(Locale.US, "%.2f", tmpNum) + " in"
        }
        return value
    }

    fun celsiusToFahrenheit(valueF: String): String {
        var value = valueF
        var tmpNum: Double
        if (MyApplication.unitsF) {
            tmpNum = value.toDoubleOrNull() ?: 0.0
            tmpNum = tmpNum * 9 / 5 + 32
            value = round(tmpNum).toInt().toString()
        }
        return value
    }

    internal fun fahrenheitToCelsius(valueDF: Double): String {
        val valueD = (valueDF - 32) * 5 / 9
        return round(valueD).toInt().toString()
    }

    private fun celsiusToFahrenheitAsInt(valueF: Int): String {
        var value = valueF
        var retVal = ""
        if (MyApplication.unitsF) {
            value = value * 9 / 5 + 32
            retVal = round(value.toFloat()).toString()
        }
        return retVal
    }

    fun celsiusToFahrenheitTable(): String {
        var table = ""
        val cInit = -40
        for (z in 40 downTo cInit) {
            val f = celsiusToFahrenheitAsInt(z)
            table += z.toString() + "  " + f + MyApplication.newline
        }
        return table
    }

    internal fun roundToString(valueD: Double) = round(valueD.toFloat()).toInt().toString()

    internal fun pressureMBtoIn(valueF: String): String {
        var value = valueF
        var tmpNum = value.toDoubleOrNull() ?: 0.0
        tmpNum /= 33.8637526
        value = String.format(Locale.US, "%.2f", tmpNum)
        return "$value in"
    }

    fun pixPerDegreeLon(centerX: Double, factor: Double): Double {
        val radius = 180 / PI * (1 / cos(Math.toRadians(30.51))) * factor
        return radius * (PI / 180) * cos(Math.toRadians(centerX))
    }

    fun deg2rad(deg: Double): Double = deg * PI / 180.0

    fun rad2deg(rad: Double): Double = rad * 180.0 / PI

    fun convertWindDir(direction: Double): String {
        var dirStr = ""
        if (direction > 337.5 || direction <= 22.5)
            dirStr = "N"
        else if (direction > 22.5 && direction <= 67.5)
            dirStr = "NE"
        else if (direction > 67.5 && direction <= 112.5)
            dirStr = "E"
        else if (direction > 112.5 && direction <= 157.5)
            dirStr = "SE"
        else if (direction > 157.5 && direction <= 202.5)
            dirStr = "S"
        else if (direction > 202.5 && direction <= 247.5)
            dirStr = "SW"
        else if (direction > 247.5 && direction <= 292.5)
            dirStr = "W"
        else if (direction > 292.5 && direction <= 337.5)
            dirStr = "NW"
        return dirStr
    }

    // https://training.weather.gov/wdtd/tools/misc/beamwidth/index.htm
    fun getRadarBeamHeight(degree: Float, distance: Double): Double {
        return 3.281 * (sin(Math.toRadians(degree.toDouble())) * distance + distance * distance / 15417.82) * 1000.0
    }

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

    /*static String getWindChill(double tempD, double mphD )
	{
		double windChillD = 35.74 + 0.6215 * tempD - 35.75 * Math.pow(mphD,0.16) + 0.4275*tempD * Math.pow(mphD,0.16);
		return "(" + UtilityMath.unitsTemp(Integer.toString((int)(Math.round(windChillD)))) + MyApplication.DEGREE_SYMBOL + ")";
	}*/

}
