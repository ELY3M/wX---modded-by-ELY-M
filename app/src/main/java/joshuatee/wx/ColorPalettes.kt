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

package joshuatee.wx

import android.content.Context
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette

internal object ColorPalettes {

    fun initialize(context: Context) {
        val productCodes = listOf(30, 56, 134, 135, 159, 161, 163, 165)
        val cm94 = ObjectColorPalette(context, 94)
        MyApplication.colorMap[94] = cm94
        MyApplication.colorMap[94]!!.initialize()
        MyApplication.colorMap[153] = cm94
        MyApplication.colorMap[180] = cm94
        MyApplication.colorMap[186] = cm94
        MyApplication.colorMap[2153] = cm94
        val cm99 = ObjectColorPalette(context, 99)
        MyApplication.colorMap[99] = cm99
        MyApplication.colorMap[99]!!.initialize()
        MyApplication.colorMap[154] = cm99
        MyApplication.colorMap[182] = cm99
        MyApplication.colorMap[2154] = cm99
        val cm172 = ObjectColorPalette(context, 172)
        MyApplication.colorMap[172] = cm172
        MyApplication.colorMap[172]!!.initialize()
        MyApplication.colorMap[170] = cm172
        productCodes.forEach {
            MyApplication.colorMap[it] = ObjectColorPalette(context, it)
            MyApplication.colorMap[it]!!.initialize()
        }
        val cm19 = ObjectColorPalette(context, 19)
        MyApplication.colorMap[19] = cm19
        MyApplication.colorMap[19]!!.initialize()
        MyApplication.colorMap[181] = cm19
        // below 2 composite reflectivity
        MyApplication.colorMap[37] = cm19
        MyApplication.colorMap[38] = cm19
    }
}
