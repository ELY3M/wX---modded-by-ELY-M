/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

    private fun interpolate(a: Float, b: Float, proportion: Float): Float = a + (b - a) * proportion

    private fun interpolateHue(a: Float, b: Float, proportion: Float): Float {
        val diff = b - a
        val fudgeFactor = 0.01f
        var transformedColor: Float
        // hue ranges from 0-360
        val total = 360.0f
        return if (diff > ((total / 2.0f) - fudgeFactor)) {
            transformedColor = (total - (b - a)) * -1.0f
            if (transformedColor < 0.0f) {
                transformedColor += total
                transformedColor
            } else {
                transformedColor
            }
        } else {
            if (b > a) {
                transformedColor = (a + (b - a) * proportion).absoluteValue
                transformedColor
            } else {
                transformedColor = a + (b - a) * proportion
                if ( a > 270.0f && b < 90.0f) {
                    transformedColor = a + (360.0f - a + b) * proportion
                }
                transformedColor
            }
        }
    }

    // Returns an interpolated color, between `a` and `b`
    fun interpolateColor(a: Int, b: Int, proportion: Float): Int {
        val hsvA = FloatArray(3)
        val hsvB = FloatArray(3)
        Color.colorToHSV(a, hsvA)
        Color.colorToHSV(b, hsvB)
        (0..2).forEach {
            if (it > 0) {
                hsvB[it] = interpolate(hsvA[it], hsvB[it], proportion)
            } else {
                hsvB[it] = interpolateHue(hsvA[it], hsvB[it], proportion)
            }
        }
        return Color.HSVToColor(hsvB)
    }
}
