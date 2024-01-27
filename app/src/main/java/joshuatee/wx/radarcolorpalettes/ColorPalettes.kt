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

package joshuatee.wx.radarcolorpalettes

import android.content.Context

internal object ColorPalettes {

    fun initialize(context: Context) {
        val cm94 = ColorPalette(context, 94)
        ColorPalette.colorMap[94] = cm94
        ColorPalette.colorMap[94]!!.initialize()
        ColorPalette.colorMap[153] = cm94
        ColorPalette.colorMap[180] = cm94
        ColorPalette.colorMap[186] = cm94
        ColorPalette.colorMap[2153] = cm94
        val cm99 = ColorPalette(context, 99)
        ColorPalette.colorMap[99] = cm99
        ColorPalette.colorMap[99]!!.initialize()
        ColorPalette.colorMap[154] = cm99
        ColorPalette.colorMap[182] = cm99
        ColorPalette.colorMap[2154] = cm99
        val cm172 = ColorPalette(context, 172)
        ColorPalette.colorMap[172] = cm172
        ColorPalette.colorMap[172]!!.initialize()
        ColorPalette.colorMap[170] = cm172
        listOf(30, 56, 134, 135, 159, 161, 163, 165).forEach {
            ColorPalette.colorMap[it] = ColorPalette(context, it)
            ColorPalette.colorMap[it]!!.initialize()
        }
        val cm19 = ColorPalette(context, 19)
        ColorPalette.colorMap[19] = cm19
        ColorPalette.colorMap[19]!!.initialize()
        ColorPalette.colorMap[181] = cm19
        // below 2 composite reflectivity
        ColorPalette.colorMap[37] = cm19
        ColorPalette.colorMap[38] = cm19
    }
}
