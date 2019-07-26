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

package joshuatee.wx.radar

import android.content.Context

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer

import joshuatee.wx.util.UtilityLog

internal object UtilityWXOGLPerfL3FourBit {

    // Used for Legacy 4bit radar - only SRM or spectrum width 30 or TDWR TR0
    // was decode4bit
    fun decodeRadial(
        context: Context,
        fn: String,
        radialStart: ByteBuffer,
        binWord: ByteBuffer
    ): Short {
        var numberOfRangeBins = 0.toShort()
        try {
            val fis = context.openFileInput(fn)
            val dis = DataInputStream(BufferedInputStream(fis))
            dis.skipBytes(170)
            numberOfRangeBins = dis.readUnsignedShort().toShort()
            dis.skipBytes(6)
            val numberOfRadials = dis.readUnsignedShort()
            val numberOfRleHalfWords = IntArray(numberOfRadials)
            radialStart.position(0)
            var s: Int
            var bin: Short
            var numOfBins: Int
            var u: Int
            (0..359).forEach { r ->
                numberOfRleHalfWords[r] = dis.readUnsignedShort()
                radialStart.putFloat((450 - dis.readUnsignedShort() / 10).toFloat())
                dis.skipBytes(2)
                s = 0
                while (s < numberOfRleHalfWords[r] * 2) {
                    bin = dis.readUnsignedByte().toShort()
                    numOfBins = bin.toInt() shr 4
                    u = 0
                    while (u < numOfBins) {
                        binWord.put((bin % 16).toByte())
                        u += 1
                    }
                    s += 1
                }
            }
            dis.close()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
        return numberOfRangeBins
    }

    fun decodeRaster(
            context: Context,
            fn: String,
            binWord: ByteBuffer
    ): Short {
        // FIXME is this used at all
        var numberOfRangeBins = 0.toShort()
        try {
            val fis = context.openFileInput(fn)
            val dis = DataInputStream(BufferedInputStream(fis))
            dis.skipBytes(172)
            val iCoordinateStart = dis.readUnsignedShort()
            val jCoordinateStart = dis.readUnsignedShort()
            val xScaleInt = dis.readUnsignedShort()
            val xScaleFractional = dis.readUnsignedShort()
            val yScaleInt = dis.readUnsignedShort()
            val yScaleFractional = dis.readUnsignedShort()
            val numberOfRows = dis.readUnsignedShort()
            val packingDescriptor = dis.readUnsignedShort()
            // 464 rows in NCR
            // 232 rows in NCZ
            var s: Int
            var bin: Short
            var numOfBins: Int
            var u: Int
            var totalPerRow = 0
            (0 until numberOfRows).forEach { r ->
                val numberOfBytes = dis.readUnsignedShort()
                totalPerRow = 0
                s = 0
                u = 0
                while (s < numberOfBytes) {
                    bin = dis.readUnsignedByte().toShort()
                    numOfBins = bin.toInt() shr 4
                    u = 0
                    while (u < numOfBins) {
                        binWord.put((bin % 16).toByte())
                        u += 1
                        totalPerRow += 1
                    }
                    s += 1
                }
            }
            dis.close()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
        return numberOfRangeBins
    }
}
