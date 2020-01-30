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

import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder

import joshuatee.wx.util.UtilityLog

class ObjectColorPalette(val context: Context, private val colormapCode: String) {

    var redValues: ByteBuffer = ByteBuffer.allocateDirect(16)
        private set
    var greenValues: ByteBuffer = ByteBuffer.allocateDirect(16)
        private set
    var blueValues: ByteBuffer = ByteBuffer.allocateDirect(16)
        private set

    private fun setupBuffers(size: Int) {
        redValues = ByteBuffer.allocateDirect(size)
        redValues.order(ByteOrder.nativeOrder())
        greenValues = ByteBuffer.allocateDirect(size)
        greenValues.order(ByteOrder.nativeOrder())
        blueValues = ByteBuffer.allocateDirect(size)
        blueValues.order(ByteOrder.nativeOrder())
    }

    fun init() {
        when (colormapCode) {
            "19" -> {
                setupBuffers(16)
                UtilityColorPalette4bitGeneric.generate(context, colormapCode)
            }
            "30" -> {
                setupBuffers(16)
                UtilityColorPalette4bitGeneric.generate(context, colormapCode)
            }
            "56" -> {
                setupBuffers(16)
                UtilityColorPalette4bitGeneric.generate(context, colormapCode)
            }
            "165" -> {
                setupBuffers(256)
                try {
                    UtilityColorPalette165.loadColorMap(context)
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
            else -> {
                setupBuffers(256)
                try {
                    UtilityColorPaletteGeneric.loadColorMap(context, colormapCode)
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
        }
    }
}


