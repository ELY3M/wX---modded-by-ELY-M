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

package joshuatee.wx.radar

import java.nio.ByteBuffer
import java.nio.ByteOrder
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.radarcolorpalettes.ColorPalette

class OglRadarBuffers(var bgColor: Int) : OglBuffers() {

    var fileName = "nids"
    var numberOfRadials = 360
    var binSize = 0.0f
    var numRangeBins = 0
    var productCode = 94.toShort()

    val colormap: ColorPalette
        get() = ColorPalette.colorMap[productCode.toInt()]!!

    fun extractL3Data(rd: NexradLevel3) {
        productCode = rd.productCode
        binSize = rd.binSize
        numRangeBins = rd.numberOfRangeBins.toInt()
        if (productCode == 2153.toShort() || productCode == 2154.toShort()) {
            numberOfRadials = 720
        }
    }

    fun extractL2Data(rd: NexradLevel2) {
        numberOfRadials = rd.radialStartAngle.capacity() / 4
        binSize = rd.binSize
        numRangeBins = rd.numberOfRangeBins
    }

    fun setProductCodeFromString(product: String) {
        productCode = if (product == "L2REF") {
            153.toShort()
        } else {
            154.toShort()
        }
    }

    override fun setToPositionZero() {
        floatBuffer.order(ByteOrder.nativeOrder())
        colorBuffer.order(ByteOrder.nativeOrder())
        floatBuffer.position(0)
        colorBuffer.position(0)
    }

    fun initialize() {
        try {
            if (productCode == 37.toShort() || productCode == 38.toShort() || productCode == 41.toShort() || productCode == 57.toShort()) {
                if (floatBuffer.capacity() < 32 * 464 * 464) {
                    floatBuffer = ByteBuffer.allocateDirect(32 * 464 * 464)
                }
                if (colorBuffer.capacity() < 12 * 464 * 464) {
                    colorBuffer = ByteBuffer.allocateDirect(12 * 464 * 464)
                }
            } else {
                if (floatBuffer.capacity() < 32 * numberOfRadials * numRangeBins) {
                    floatBuffer = ByteBuffer.allocateDirect(32 * numberOfRadials * numRangeBins)
                }
                if (colorBuffer.capacity() < 12 * numberOfRadials * numRangeBins) {
                    colorBuffer = ByteBuffer.allocateDirect(12 * numberOfRadials * numRangeBins)
                }
            }
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }
}
