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

internal object UtilityColorPalette41 {

    fun generate() {
        val radarColorPaletteCode = 41
        val obj56 = MyApplication.colorMap[radarColorPaletteCode]!!
        // fixme naming
        obj56.redValues.position(0)
        obj56.greenValues.position(0)
        obj56.blueValues.position(0)

        obj56.redValues.put(0.toByte())
        obj56.redValues.put(156.toByte())
        obj56.redValues.put(118.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(50.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(0.toByte())
        obj56.redValues.put(254.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(174.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(255.toByte())
        obj56.redValues.put(231.toByte())

        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(156.toByte())
        obj56.greenValues.put(118.toByte())
        obj56.greenValues.put(224.toByte())
        obj56.greenValues.put(176.toByte())
        obj56.greenValues.put(144.toByte())
        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(251.toByte())
        obj56.greenValues.put(187.toByte())
        obj56.greenValues.put(239.toByte())
        obj56.greenValues.put(191.toByte())
        obj56.greenValues.put(255.toByte())
        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(0.toByte())
        obj56.greenValues.put(255.toByte())
        obj56.greenValues.put(0.toByte())

        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(156.toByte())
        obj56.blueValues.put(118.toByte())
        obj56.blueValues.put(255.toByte())
        obj56.blueValues.put(255.toByte())
        obj56.blueValues.put(204.toByte())
        obj56.blueValues.put(150.toByte())
        obj56.blueValues.put(144.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(0.toByte())
        obj56.blueValues.put(255.toByte())
        obj56.blueValues.put(255.toByte())
    }
}