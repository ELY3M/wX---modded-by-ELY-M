/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

internal object UtilityColorPalette30 {

    // FIXME copied from 56 needs to be adjusted

    fun gen30() {
        val obj30 = MyApplication.colorMap[30]!!
        obj30.redValues.position(0)
        obj30.greenValues.position(0)
        obj30.blueValues.position(0)
        obj30.redValues.put(0.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(255.toByte())
        obj30.redValues.put(255.toByte())
        obj30.redValues.put(192.toByte())
        obj30.redValues.put(255.toByte())
        obj30.redValues.put(153.toByte())
        obj30.redValues.put(64.toByte()) // RF
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.redValues.put(64.toByte())
        obj30.greenValues.put(0.toByte())  // green start
        obj30.greenValues.put(64.toByte())
        obj30.greenValues.put(144.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(85.toByte())
        obj30.greenValues.put(0.toByte()) // RF
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.greenValues.put(0.toByte())
        obj30.blueValues.put(0.toByte())  // blue start
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(0.toByte())
        obj30.blueValues.put(0.toByte())
        obj30.blueValues.put(0.toByte())
        obj30.blueValues.put(255.toByte())
        obj30.blueValues.put(201.toByte())
        obj30.blueValues.put(123.toByte()) // RF
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
        obj30.blueValues.put(64.toByte())
    }
}
