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

import android.content.Context
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import joshuatee.wx.util.UtilityLog

internal object NexradDecodeFourBit {

    // Used for Legacy 4bit radar - only SRM or spectrum width 30 or TDWR TR0
    fun radial(context: Context, fileName: String, radialStart: ByteBuffer, binWord: ByteBuffer): Short {
        var numberOfRangeBins = 0.toShort()
        try {
            val fileInputStream = context.openFileInput(fileName)
            val dataInputStream = DataInputStream(BufferedInputStream(fileInputStream))
            dataInputStream.skipBytes(170)
            numberOfRangeBins = dataInputStream.readUnsignedShort().toShort()
            dataInputStream.skipBytes(6)
            // 360 for 4bit products
            val numberOfRadials = dataInputStream.readUnsignedShort()
            val numberOfRleHalfWords = IntArray(numberOfRadials)
            radialStart.position(0)
            var bin: Short
            var numOfBins: Int
            for (r in 0 until numberOfRadials) {
                numberOfRleHalfWords[r] = dataInputStream.readUnsignedShort()
                radialStart.putFloat(450.0f - dataInputStream.readUnsignedShort() / 10.0f)
                dataInputStream.skipBytes(2)
                for (s in 0 until numberOfRleHalfWords[r] * 2) {
                    bin = dataInputStream.readUnsignedByte().toShort()
                    numOfBins = bin.toInt() shr 4
                    for (u in 0 until numOfBins) {
                        binWord.put((bin % 16).toByte())
                    }
                }
            }
            dataInputStream.close()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
        return numberOfRangeBins
    }

    // comp ref
    fun raster(context: Context, fileName: String, binWord: ByteBuffer): Short {
        val numberOfRangeBins = 0.toShort()
        try {
            val fis = context.openFileInput(fileName)
            val dataInputStream = DataInputStream(BufferedInputStream(fis))
            dataInputStream.skipBytes(172)
            /*val iCoordinateStart = dis.readUnsignedShort()
            val jCoordinateStart = dis.readUnsignedShort()
            val xScaleInt = dis.readUnsignedShort()
            val xScaleFractional = dis.readUnsignedShort()
            val yScaleInt = dis.readUnsignedShort()
            val yScaleFractional = dis.readUnsignedShort()*/
//            repeat(6) {
//                dataInputStream.readUnsignedShort()
//            }
            dataInputStream.skipBytes(12)
            val numberOfRows = dataInputStream.readUnsignedShort()
            dataInputStream.readUnsignedShort() // packingDescriptor
            // 464 rows in NCR
            // 232 rows in NCZ
            var bin: Short
            var numOfBins: Int
            for (unused in 0 until numberOfRows) {
                val numberOfBytes = dataInputStream.readUnsignedShort()
                for (s in 0 until numberOfBytes) {
                    bin = dataInputStream.readUnsignedByte().toShort()
                    numOfBins = bin.toInt() shr 4
                    for (u in 0 until numOfBins) {
                        binWord.put((bin % 16).toByte())
                    }
                }
            }
            dataInputStream.close()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
        return numberOfRangeBins
    }
}
