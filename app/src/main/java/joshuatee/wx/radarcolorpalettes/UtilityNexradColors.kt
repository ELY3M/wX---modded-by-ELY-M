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

package joshuatee.wx.radarcolorpalettes

import android.graphics.Color
import kotlin.math.absoluteValue

internal object UtilityNexradColors {

    // thanks Mark Renouf and Stuck
    // http://stackoverflow.com/questions/4414673/android-color-between-two-colors-based-on-percentage

    private fun interpolate(a: Float, b: Float, proportion: Float) = a + (b - a) * proportion

    private fun interpolateHue(a: Float, b: Float, proportion: Float): Float {
        val diff = b - a
        val fudgeFactor = 0.01
        var transformedColor: Float
        val total = 360f // hue ranges from 0-360
        return if (diff > ((total / 2) - fudgeFactor)) {
            transformedColor = (total - (b - a)) * -1
            if (transformedColor < 0) {
                transformedColor += total
                transformedColor
            } else {
                transformedColor
            }
        } else {
            if ( b > a) {
                transformedColor = (a + (b - a) * proportion).absoluteValue
                transformedColor
            } else {
                transformedColor = a + (b - a) * proportion
                if ( a > 270.0 && b < 90.0) {
                    transformedColor = (a + (360 - a + b)*proportion)
                }
                transformedColor
            }
        }
    }

    /** Returns an interpoloated color, between `a` and `b`  */
    fun interpolateColor(a: Int, b: Int, proportion: Double): Int {
        val hsva = FloatArray(3)
        val hsvb = FloatArray(3)
        Color.colorToHSV(a, hsva)
        Color.colorToHSV(b, hsvb)
        (0..2).forEach {
            if (it > 0) {
                hsvb[it] = interpolate(hsva[it], hsvb[it], proportion.toFloat())
            } else {
                hsvb[it] = interpolateHue(hsva[it], hsvb[it], proportion.toFloat())
            }
        }
        return Color.HSVToColor(hsvb)
    }
}


