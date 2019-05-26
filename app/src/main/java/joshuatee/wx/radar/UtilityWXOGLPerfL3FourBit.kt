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

    // Used for Legacy 4bit radar - only SRM or spectrum width 30
    // was decode4bit
    fun decode(
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
            val numberOfRleHalfwords = IntArray(numberOfRadials)
            radialStart.position(0)
            var s: Int
            var bin: Short
            var numOfBins: Int
            var u: Int
            (0..359).forEach { r ->
                numberOfRleHalfwords[r] = dis.readUnsignedShort()
                radialStart.putFloat((450 - dis.readUnsignedShort() / 10).toFloat())
                dis.skipBytes(2)
                s = 0
                while (s < numberOfRleHalfwords[r] * 2) {
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
}
