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
//modded by ELY M. 

package joshuatee.wx.radar

import android.graphics.Color
import joshuatee.wx.objects.LatLon
import java.nio.ByteBuffer
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityLog
import kotlin.math.*

internal object NexradRenderUtilities {

    private const val M_180_DIV_PI = (180.0 / PI).toFloat()
    private const val M_PI_DIV_4 = (PI / 4.0).toFloat()
    private const val M_PI_DIV_360 = (PI / 360.0).toFloat()
    private const val TWICE_PI = (2.0 * PI).toFloat()

    fun generateIndex(indexBuff: ByteBuffer, length: Int, breakSizeF: Int) {
        var breakSize = breakSizeF
        val remainder: Int
        var chunkCount = 1
        var indexForIndex = 0
        if (length < breakSize) {
            breakSize = length
            remainder = breakSize
        } else {
            chunkCount = length / breakSize
            remainder = length - breakSize * chunkCount
            chunkCount += 1
        }
        for (chunkIndex in 0 until chunkCount) {
            var indexCount = 0
            if (chunkIndex == chunkCount - 1) {
                breakSize = remainder
            }
            repeat(breakSize) {
                indexBuff.putShort(indexForIndex, indexCount.toShort())
                indexBuff.putShort(indexForIndex + 2, (1 + indexCount).toShort())
                indexBuff.putShort(indexForIndex + 4, (2 + indexCount).toShort())
                indexBuff.putShort(indexForIndex + 6, indexCount.toShort())
                indexBuff.putShort(indexForIndex + 8, (2 + indexCount).toShort())
                indexBuff.putShort(indexForIndex + 10, (3 + indexCount).toShort())
                indexForIndex += 12
                indexCount += 4
            }
        }
    }

    fun generateIndexLine(indexBuff: ByteBuffer, length: Int, breakSizeF: Int) {
        var breakSize = breakSizeF
        val remainder: Int
        var chunkCount = 1
        val totalBins = length / 4
        var indexForIndex = 0
        if (totalBins < breakSize) {
            breakSize = totalBins
            remainder = breakSize
        } else {
            chunkCount = totalBins / breakSize
            remainder = totalBins - breakSize * chunkCount
            chunkCount += 1
        }
        indexBuff.position(0)
        (0 until chunkCount).forEach {
            var indexCount = 0
            if (it == chunkCount - 1) {
                breakSize = remainder
            }
            repeat(breakSize) {
                indexBuff.putShort(indexForIndex, indexCount.toShort())
                indexBuff.putShort(indexForIndex + 2, (1 + indexCount).toShort())
                indexForIndex += 4
                indexCount += 2
            }
        }
    }

    fun genTriangle(buffers: OglBuffers, projectionNumbers: ProjectionNumbers) {
        var pixYD: Float
        var pixXD: Float
        var indexCount = 0
        var test1: Float
        var test2: Float
        buffers.setToPositionZero()
        (0 until buffers.count).forEach { index ->
            test1 = M_180_DIV_PI * log(
                tan(M_PI_DIV_4 + buffers.xList[index] * M_PI_DIV_360),
                E
            ).toFloat()
            test2 = M_180_DIV_PI * log(
                tan(M_PI_DIV_4 + projectionNumbers.xDbl * M_PI_DIV_360),
                E
            ).toFloat()
            pixYD =
                -1.0f * ((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
            pixXD =
                (-1.0f * ((buffers.yList[index] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
            buffers.putFloat(pixXD)
            buffers.putFloat(-1.0f * pixYD)
            buffers.putFloat(pixXD - buffers.lenInit)
            buffers.putFloat(-1.0f * pixYD + buffers.lenInit)
            buffers.putFloat(pixXD + buffers.lenInit)
            buffers.putFloat(-1.0f * pixYD + buffers.lenInit)
            buffers.putIndex(indexCount.toShort())
            buffers.putIndex((indexCount + 1).toShort())
            buffers.putIndex((indexCount + 2).toShort())
            indexCount += 3
            (0..2).forEach { _ ->
                buffers.putColor(buffers.solidColorRed)
                buffers.putColor(buffers.solidColorGreen)
                buffers.putColor(buffers.solidColorBlue)
            }
        }
    }

    fun genTriangleUp(buffers: OglBuffers, projectionNumbers: ProjectionNumbers) {
        var pixYD: Float
        var pixXD: Float
        var indexCount = 0
        var test1: Float
        var test2: Float
        buffers.setToPositionZero()
        (0 until buffers.count).forEach { index ->
            test1 = M_180_DIV_PI * log(
                tan(M_PI_DIV_4 + (buffers.xList.getOrNull(index) ?: 0.0) * M_PI_DIV_360),
                E
            ).toFloat()
            test2 = M_180_DIV_PI * log(
                tan(M_PI_DIV_4 + projectionNumbers.xDbl * M_PI_DIV_360),
                E
            ).toFloat()
            pixYD =
                -1.0f * ((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
            pixXD =
                (-1.0f * (((buffers.yList.getOrNull(index)
                    ?: 0.0) - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
            buffers.putFloat(pixXD)
            buffers.putFloat(-pixYD)
            buffers.putFloat(pixXD - buffers.lenInit)
            buffers.putFloat(-1.0f * pixYD - buffers.lenInit)
            buffers.putFloat(pixXD + buffers.lenInit)
            buffers.putFloat(-1.0f * pixYD - buffers.lenInit)
            buffers.putIndex(indexCount.toShort())
            buffers.putIndex((indexCount + 1).toShort())
            buffers.putIndex((indexCount + 2).toShort())
            indexCount += 3
            (0..2).forEach { _ ->
                buffers.putColor(buffers.solidColorRed)
                buffers.putColor(buffers.solidColorGreen)
                buffers.putColor(buffers.solidColorBlue)
            }
        }
    }

    fun genCircle(buffers: OglBuffers, projectionNumbers: ProjectionNumbers) {
        var pixYD: Float
        var pixXD: Float
        var indexCount = 0
        var test1: Float
        var test2: Float
        val len = buffers.lenInit * 0.50f
        val triangleAmount = buffers.triangleCount
        var indexForIndex = 0
        var bufferIndex = 0
        buffers.setToPositionZero()
        (0 until buffers.count).forEach { index ->
            test1 = M_180_DIV_PI * log(
                tan(M_PI_DIV_4 + buffers.xList[index] * M_PI_DIV_360),
                E
            ).toFloat()
            test2 = M_180_DIV_PI * log(
                tan(M_PI_DIV_4 + projectionNumbers.xDbl * M_PI_DIV_360),
                E
            ).toFloat()
            pixYD =
                -1.0f * ((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
            pixXD =
                (-1.0 * ((buffers.yList[index] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
            (0 until triangleAmount).forEach {
                buffers.putFloat(bufferIndex, pixXD)
                buffers.putFloat(bufferIndex + 4, -1.0f * pixYD)
                buffers.putFloat(bufferIndex + 8, pixXD + len * cos(it * TWICE_PI / triangleAmount))
                buffers.putFloat(
                    bufferIndex + 12,
                    -1.0f * pixYD + len * sin(it * TWICE_PI / triangleAmount)
                )
                buffers.putFloat(
                    bufferIndex + 16,
                    pixXD + len * cos((it + 1) * TWICE_PI / triangleAmount)
                )
                buffers.putFloat(
                    bufferIndex + 20,
                    -1.0f * pixYD + len * sin((it + 1) * TWICE_PI / triangleAmount)
                )
                bufferIndex += 24
                buffers.putIndex(indexForIndex, indexCount.toShort())
                buffers.putIndex(indexForIndex + 2, (indexCount + 1).toShort())
                buffers.putIndex(indexForIndex + 4, (indexCount + 2).toShort())
                indexForIndex += 6
                indexCount += 3
                repeat(3) {
                    buffers.putColor(buffers.solidColorRed)
                    buffers.putColor(buffers.solidColorGreen)
                    buffers.putColor(buffers.solidColorBlue)
                }
            }
        }
    }

    fun genCircleWithColor(buffers: OglBuffers, projectionNumbers: ProjectionNumbers) {
        var pixYD: Float
        var pixXD: Float
        var iCount: Int
        var indexCount = 0
        var test1: Float
        var test2: Float
        val len = buffers.lenInit * 0.50f
        var indexForIndex = 0
        var bufferIndex = 0
        val col = ByteArray(3)
        val triangleAmount = buffers.triangleCount
        buffers.setToPositionZero()
        if (buffers.colorIntArray.size == buffers.count) {
            iCount = 0
            while (iCount < buffers.count && iCount < buffers.xList.size && iCount < buffers.yList.size) {
                col[0] = Color.red(buffers.colorIntArray[iCount]).toByte()
                col[1] = Color.green(buffers.colorIntArray[iCount]).toByte()
                col[2] = Color.blue(buffers.colorIntArray[iCount]).toByte()
                test1 = M_180_DIV_PI * log(
                    tan(M_PI_DIV_4 + buffers.xList[iCount] * M_PI_DIV_360),
                    E
                ).toFloat()
                test2 = M_180_DIV_PI * log(
                    tan(M_PI_DIV_4 + projectionNumbers.xDbl * M_PI_DIV_360),
                    E
                ).toFloat()
                pixYD =
                    -1.0f * ((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
                pixXD =
                    (-1.0 * ((buffers.yList[iCount] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
                (0 until triangleAmount).forEach {
                    buffers.putFloat(bufferIndex, pixXD)
                    buffers.putFloat(bufferIndex + 4, -1.0f * pixYD)
                    buffers.putFloat(
                        bufferIndex + 8,
                        pixXD + len * cos(it * TWICE_PI / triangleAmount)
                    )
                    buffers.putFloat(
                        bufferIndex + 12,
                        -1.0f * pixYD + len * sin(it * TWICE_PI / triangleAmount)
                    )
                    buffers.putFloat(
                        bufferIndex + 16,
                        pixXD + len * cos((it + 1) * TWICE_PI / triangleAmount)
                    )
                    buffers.putFloat(
                        bufferIndex + 20,
                        -1.0f * pixYD + len * sin((it + 1) * TWICE_PI / triangleAmount)
                    )
                    bufferIndex += 24
                    buffers.putIndex(indexForIndex, indexCount.toShort())
                    buffers.putIndex(indexForIndex + 2, (indexCount + 1).toShort())
                    buffers.putIndex(indexForIndex + 4, (indexCount + 2).toShort())
                    indexForIndex += 6
                    indexCount += 3
                    repeat(3) {
                        buffers.putColor(col[0])
                        buffers.putColor(col[1])
                        buffers.putColor(col[2])
                    }
                }
                iCount += 1
            }
        }
    }
/* NOT USED by ELY M.
    fun genCircleLocdot(buffers: OglBuffers, projectionNumbers: ProjectionNumbers, latLon: LatLon) {
        buffers.setToPositionZero()
        val test1 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + latLon.lat * M_PI_DIV_360), E).toFloat()
        val test2 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + projectionNumbers.xDbl * M_PI_DIV_360), E).toFloat()
        val length = buffers.lenInit * 2.0f
        val triangleAmount = buffers.triangleCount
        var indexCount = 0
        val pixXD = (-1.0 * ((latLon.lon - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
        val pixYD = -1.0f * ((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
        (0 until triangleAmount).forEach {
            buffers.putFloat(pixXD + length * cos(it * TWICE_PI / triangleAmount))
            buffers.putFloat(-1.0f * pixYD + length * sin(it * TWICE_PI / triangleAmount))
            buffers.putFloat(pixXD + length * cos((it + 1) * TWICE_PI / triangleAmount))
            buffers.putFloat(-1.0f * pixYD + length * sin((it + 1) * TWICE_PI / triangleAmount))
            buffers.putIndex(indexCount.toShort())
            buffers.putIndex((indexCount + 1).toShort())
            indexCount += 2
        }
    }
*/
    //elys mod
    fun genLocdot(buffers: OglBuffers, projectionNumbers: ProjectionNumbers, latLon: LatLon) {
        buffers.setToPositionZero()
        val test1 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + latLon.lat * M_PI_DIV_360), E).toFloat()
        val test2 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + projectionNumbers.xDbl * M_PI_DIV_360), E).toFloat()
        val length = buffers.lenInit * 2.0f
        val triangleAmount = buffers.triangleCount
        var indexCount = 0
        val pixXD = (-1.0 * ((latLon.lon - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
        val pixYD = -1.0f * ((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
        (0 until triangleAmount).forEach {
            buffers.putFloat(pixXD + length * cos(it * TWICE_PI / triangleAmount))
            buffers.putFloat(-1.0f * pixYD + length * sin(it * TWICE_PI / triangleAmount))
            buffers.putFloat(pixXD + length * cos((it + 1) * TWICE_PI / triangleAmount))
            buffers.putFloat(-1.0f * pixYD + length * sin((it + 1) * TWICE_PI / triangleAmount))
            buffers.putIndex(indexCount.toShort())
            buffers.putIndex((indexCount + 1).toShort())
            indexCount += 2
        }
    }

    fun colorGen(colorBuff: ByteBuffer, length: Int, colors: ByteArray) {
        try {
            if (length * 3 <= colorBuff.limit()) {
                repeat(length) {
                    if (colorBuff.hasRemaining()) colorBuff.put(colors[0])
                    if (colorBuff.hasRemaining()) colorBuff.put(colors[1])
                    if (colorBuff.hasRemaining()) colorBuff.put(colors[2])
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun genMercator(inBuff: ByteBuffer, outBuff: ByteBuffer, pn: ProjectionNumbers, count: Int) {
        val pnXFloat = pn.xFloat
        val pnYFloat = pn.yFloat
        val pnXCenter = pn.xCenter.toFloat()
        val pnYCenter = pn.yCenter.toFloat()
        val oneDegreeScaleFactor = pn.oneDegreeScaleFactorFloat
        if (count * 4 <= outBuff.limit()) {
            for (iCount in 0 until count step 2) {
                outBuff.putFloat(
                    iCount * 4 + 4,
                    ((M_180_DIV_PI * log(
                        tan((M_PI_DIV_4 + inBuff.getFloat(iCount * 4) * M_PI_DIV_360).toDouble()),
                        E
                    ).toFloat() - M_180_DIV_PI * log(
                        tan((M_PI_DIV_4 + pnXFloat * M_PI_DIV_360).toDouble()), E
                    ).toFloat()) * oneDegreeScaleFactor) + pnYCenter
                )
                outBuff.putFloat(
                    iCount * 4,
                    -1.0f * ((inBuff.getFloat(iCount * 4 + 4) - pnYFloat) * oneDegreeScaleFactor) + pnXCenter
                )
            }
        }
    }

    //for single images
    fun genMarker(buffers: OglBuffers, pn: ProjectionNumbers, x: Double, y: Double) {
        buffers.setToPositionZero()
        val pixYD: Float
        val pixXD = (-((y - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
        var ixCount = 0
        val test1 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + x * M_PI_DIV_360), E).toFloat()
        val test2 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + pn.xDbl * M_PI_DIV_360), E).toFloat()
        val len = 0f
        val triangleAmount = 1
        pixYD = -((test1 - test2) * pn.oneDegreeScaleFactorFloat) + pn.yCenter.toFloat()
        (0 until triangleAmount).forEach {
            buffers.putFloat(pixXD + len * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(-pixYD + len * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(pixXD + len * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(-pixYD + len * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putIndex(ixCount.toShort())
            buffers.putIndex((ixCount + 1).toShort())
            ixCount += 2
        }
    }

    //for tvs / hi images
    fun genMarkerList(buffers: OglBuffers, pn: ProjectionNumbers, x: DoubleArray, y: DoubleArray) {
        var pointX: Double
        var pointY: Double
        var pixYD: Float
        var pixXD: Float
        var iCount = 0
        var ixCount = 0
        var test1: Float
        var test2: Float
        val len = 0f //buffers.lenInit * 0.50f
        val triangleAmount = 1 //buffers.triangleCount
        var iI = 0
        var lI = 0
        buffers.setToPositionZero()
        while (iCount < buffers.count) {
            pointX = x[iCount]
            pointY = y[iCount]
            test1 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + pointX * M_PI_DIV_360), E).toFloat()
            test2 = M_180_DIV_PI * log(tan(M_PI_DIV_4 + pn.xDbl * M_PI_DIV_360), E).toFloat()
            pixYD = -((test1 - test2) * pn.oneDegreeScaleFactorFloat) + pn.yCenter.toFloat()
            pixXD = (-((pointY - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
            (0 until triangleAmount).forEach {
                buffers.putFloat(lI, pixXD)
                lI += 4
                buffers.putFloat(lI, -pixYD)
                lI += 4
                buffers.putFloat(lI, pixXD + len * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
                lI += 4
                buffers.putFloat(lI, -pixYD + len * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
                lI += 4
                buffers.putFloat(lI, pixXD + len * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
                lI += 4
                buffers.putFloat(lI, -pixYD + len * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
                lI += 4
                buffers.putIndex(iI, ixCount.toShort())
                iI += 2
                buffers.putIndex(iI, (ixCount + 1).toShort())
                iI += 2
                buffers.putIndex(iI, (ixCount + 2).toShort())
                iI += 2
                ixCount += 3
            }
            iCount += 1
        }
    }
}
