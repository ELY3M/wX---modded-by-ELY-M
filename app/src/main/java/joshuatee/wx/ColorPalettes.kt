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

package joshuatee.wx

import android.content.Context
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette

internal object ColorPalettes {

    fun init(context: Context) {
        val colorMapIntArr = listOf(30, 56, 134, 135, 159, 161, 163, 165)
        val cm94 = ObjectColorPalette(context, "94")
        MyApplication.colorMap[94] = cm94
        MyApplication.colorMap[94]!!.init()
        MyApplication.colorMap[153] = cm94
        MyApplication.colorMap[186] = cm94
        val cm99 = ObjectColorPalette(context, "99")
        MyApplication.colorMap[99] = cm99
        MyApplication.colorMap[99]!!.init()
        MyApplication.colorMap[154] = cm99
        MyApplication.colorMap[182] = cm99
        val cm172 = ObjectColorPalette(context, "172")
        MyApplication.colorMap[172] = cm172
        MyApplication.colorMap[172]!!.init()
        MyApplication.colorMap[170] = cm172
        colorMapIntArr.forEach {
            MyApplication.colorMap[it] = ObjectColorPalette(context, it.toString())
            MyApplication.colorMap[it]!!.init()
        }
        val cm19 = ObjectColorPalette(context, "19")
        MyApplication.colorMap[19] = cm19
        MyApplication.colorMap[19]!!.init()
        MyApplication.colorMap[181] = cm19
        // below 2 composite reflectivity
        MyApplication.colorMap[37] = cm19
        MyApplication.colorMap[38] = cm19

        // below 2 precip prod used for TDWR
        val cm78 = ObjectColorPalette(context, "78")
        MyApplication.colorMap[78] = cm78
        MyApplication.colorMap[78]!!.init()
        MyApplication.colorMap[80] = cm78

        val cm41 = ObjectColorPalette(context, "41")
        MyApplication.colorMap[41] = cm41
        MyApplication.colorMap[41]!!.init()

        val cm57 = ObjectColorPalette(context, "57")
        MyApplication.colorMap[57] = cm57
        MyApplication.colorMap[57]!!.init()
    }
}
