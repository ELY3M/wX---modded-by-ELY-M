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

package joshuatee.wx.radarcolorpalettes

import joshuatee.wx.MyApplication

internal object UtilityColorPalette57 {

    fun generate() {
        val radarColorPaletteCode = 57
        val obj56 = MyApplication.colorMap[radarColorPaletteCode]!!
        // fixme naming
        obj56.redValues.position(0)
        obj56.greenValues.position(0)
        obj56.blueValues.position(0)

        obj56.redValues.put(0.toByte())
        obj56.redValues.put(156.toByte())
        obj56.redValues.put(118.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(238.toByte())
        obj56.redValues.put(201.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(208.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(218.toByte())
        obj56.redValues.put(174.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(231.toByte())

        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(156.toByte())
        obj56.greenValues.put(118.toByte())
        obj56.greenValues.put(170.toByte())
        obj56.greenValues.put(140.toByte())
        obj56.greenValues.put(112.toByte())
        obj56.greenValues.put(251.toByte())
        obj56.greenValues.put(187.toByte())
        obj56.greenValues.put(255.toByte())
        obj56.greenValues.put(208.toByte())
        obj56.greenValues.put(96.toByte())
        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(255.toByte())
        obj56.greenValues.put(0.toByte())

        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(156.toByte())
        obj56.blueValues.put(118.toByte())
        obj56.blueValues.put(170.toByte())
        obj56.blueValues.put(140.toByte())
        obj56.blueValues.put(112.toByte())
        obj56.blueValues.put(144.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(112.toByte())
        obj56.blueValues.put(96.toByte())
        obj56.blueValues.put(96.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(255.toByte())
        obj56.blueValues.put(255.toByte())
        obj56.blueValues.put(255.toByte())
    }
}