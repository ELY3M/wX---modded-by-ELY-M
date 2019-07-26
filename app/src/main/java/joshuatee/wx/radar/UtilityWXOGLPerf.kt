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
//modded by ELY M. 

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.ByteBuffer

import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.bzip2.Compression

import kotlin.math.*

internal object UtilityWXOGLPerf {

    private const val M_180_div_PI: Float = (180.0 / PI).toFloat()
    private const val M_PI_div_4: Float = (PI / 4.0).toFloat()
    private const val M_PI_div_360: Float = (PI / 360.0).toFloat()
    private const val TWICE_PI: Float = (2.0f * PI).toFloat()

    fun decode8BitAndGenRadials(context: Context, radarBuffers: ObjectOglRadarBuffers): Int {
        var totalBins = 0
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, radarBuffers.fn))
            dis.bigEndian = true
            // ADVANCE PAST WMO HEADER
            while (dis.readShort().toInt() != -1) {
                // while (dis.readUnsignedShort() != 16) {
            }
            dis.skipBytes(100)
            val magic = ByteArray(3)
            magic[0] = 'B'.toByte()
            magic[1] = 'Z'.toByte()
            magic[2] = 'h'.toByte()
            val compression = Compression.getCompression(magic)
            val compressedFileSize = dis.length() - dis.filePointer
            val buf = ByteArray(compressedFileSize.toInt())
            dis.read(buf)
            dis.close()
            val decompStream = compression.decompress(ByteArrayInputStream(buf))
            val dis2 = DataInputStream(BufferedInputStream(decompStream))
            dis2.skipBytes(30)
            var r = 0
            var numberOfRleHalfwords: Int
            radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
            radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
            radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
            radarBuffers.floatBuffer.position(0)
            radarBuffers.colorBuffer.position(0)
            var angle: Float
            var angleV: Float
            var level: Byte
            var levelCount: Int
            var binStart: Float
            var bin: Int
            var cI = 0
            var rI = 0
            var curLevel = 0.toByte()
            var angleSin: Float
            var angleCos: Float
            var angleVSin: Float
            var angleVCos: Float
            var angleNext = 0f
            var angle0 = 0f
            val numberOfRadials = 360
            while (r < numberOfRadials) {
                numberOfRleHalfwords = dis2.readUnsignedShort()
                angle = 450f - dis2.readUnsignedShort() / 10f
                dis2.skipBytes(2)
                if (r < numberOfRadials - 1) {
                    dis2.mark(100000)
                    dis2.skipBytes(numberOfRleHalfwords + 2)
                    angleNext = 450f - dis2.readUnsignedShort() / 10f
                    dis2.reset()
                }
                level = 0.toByte()
                levelCount = 0
                binStart = radarBuffers.binSize
                if (r == 0) angle0 = angle
                angleV = if (r < numberOfRadials - 1)
                    angleNext
                else
                    angle0
                bin = 0
                while (bin < numberOfRleHalfwords) {
                    try {
                        curLevel =
                            (dis2.readUnsignedByte() and 0xFF).toByte() // was dis2!!.readUnsignedByte().toInt()
                    } catch (e: Exception) {
                        UtilityLog.handleException(e)
                    }
                    if (bin == 0)
                        level = curLevel
                    if (curLevel == level)
                        levelCount += 1
                    else {
                        angleVCos = cos((angleV / M_180_div_PI).toDouble()).toFloat()
                        angleVSin = sin((angleV / M_180_div_PI).toDouble()).toFloat()
                        radarBuffers.floatBuffer.putFloat(rI, binStart * angleVCos)
                        rI += 4
                        radarBuffers.floatBuffer.putFloat(rI, binStart * angleVSin)
                        rI += 4
                        radarBuffers.floatBuffer.putFloat(
                            rI,
                            (binStart + radarBuffers.binSize * levelCount) * angleVCos
                        )
                        rI += 4
                        radarBuffers.floatBuffer.putFloat(
                            rI,
                            (binStart + radarBuffers.binSize * levelCount) * angleVSin
                        )
                        rI += 4
                        angleCos = cos((angle / M_180_div_PI).toDouble()).toFloat()
                        angleSin = sin((angle / M_180_div_PI).toDouble()).toFloat()
                        radarBuffers.floatBuffer.putFloat(
                            rI,
                            (binStart + radarBuffers.binSize * levelCount) * angleCos
                        )
                        rI += 4
                        radarBuffers.floatBuffer.putFloat(
                            rI,
                            (binStart + radarBuffers.binSize * levelCount) * angleSin
                        )
                        rI += 4
                        radarBuffers.floatBuffer.putFloat(rI, binStart * angleCos)
                        rI += 4
                        radarBuffers.floatBuffer.putFloat(rI, binStart * angleSin)
                        rI += 4
                        (0..3).forEach { _ ->
                            radarBuffers.colorBuffer.put(
                                cI++,
                                radarBuffers.colormap.redValues.get(level.toInt() and 0xFF)
                            )
                            radarBuffers.colorBuffer.put(
                                cI++,
                                radarBuffers.colormap.greenValues.get(level.toInt() and 0xFF)
                            )
                            radarBuffers.colorBuffer.put(
                                cI++,
                                radarBuffers.colormap.blueValues.get(level.toInt() and 0xFF)
                            )
                        }
                        totalBins += 1
                        level = curLevel
                        binStart = bin * radarBuffers.binSize
                        levelCount = 1
                    }
                    bin += 1
                }
                r += 1
            }
            dis2.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return totalBins
    }

    fun genRadials(
        radarBuffers: ObjectOglRadarBuffers,
        binBuff: ByteBuffer,
        radialStart: ByteBuffer
    ): Int {
        radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
        var totalBins = 0
        var g = 0
        var angle: Float
        var angleV: Float
        var level: Int
        var levelCount: Int
        var binStart: Float
        var bin: Int
        var bI = 0
        var cI = 0
        var rI = 0
        var curLevel: Int
        var angleSin: Float
        var angleCos: Float
        var angleVSin: Float
        var angleVCos: Float
        val radarBlackHole: Float
        val radarBlackHoleAdd: Float
        if (radarBuffers.productCode == 56.toShort()
                || radarBuffers.productCode == 30.toShort()
                || radarBuffers.productCode == 78.toShort()
                || radarBuffers.productCode == 80.toShort()
                || radarBuffers.productCode == 181.toShort()
        ) {
            radarBlackHole = 1.0f
            radarBlackHoleAdd = 0.0f
        } else {
            radarBlackHole = 4.0f
            radarBlackHoleAdd = 4.0f
        }
        while (g < radarBuffers.numberOfRadials) {
            angle = radialStart.getFloat(g * 4)
            level = binBuff.get(bI).toInt()
            levelCount = 0
            binStart = radarBlackHole
            angleV = if (g < radarBuffers.numberOfRadials - 1)
                radialStart.getFloat(g * 4 + 4)
            else
                radialStart.getFloat(0)
            bin = 0
            while (bin < radarBuffers.numRangeBins) {
                curLevel = binBuff.get(bI).toInt()
                bI += 1
                if (curLevel == level)
                    levelCount += 1
                else {
                    angleVCos = cos((angleV / M_180_div_PI).toDouble()).toFloat()
                    angleVSin = sin((angleV / M_180_div_PI).toDouble()).toFloat()
                    radarBuffers.floatBuffer.putFloat(rI, binStart * angleVCos)
                    rI += 4
                    radarBuffers.floatBuffer.putFloat(rI, binStart * angleVSin)
                    rI += 4
                    radarBuffers.floatBuffer.putFloat(
                        rI,
                        (binStart + radarBuffers.binSize * levelCount) * angleVCos
                    )
                    rI += 4
                    radarBuffers.floatBuffer.putFloat(
                        rI,
                        (binStart + radarBuffers.binSize * levelCount) * angleVSin
                    )
                    rI += 4
                    angleCos = cos((angle / M_180_div_PI).toDouble()).toFloat()
                    angleSin = sin((angle / M_180_div_PI).toDouble()).toFloat()
                    radarBuffers.floatBuffer.putFloat(
                        rI,
                        (binStart + radarBuffers.binSize * levelCount) * angleCos
                    )
                    rI += 4
                    radarBuffers.floatBuffer.putFloat(
                        rI,
                        (binStart + radarBuffers.binSize * levelCount) * angleSin
                    )
                    rI += 4
                    radarBuffers.floatBuffer.putFloat(rI, binStart * angleCos)
                    rI += 4
                    radarBuffers.floatBuffer.putFloat(rI, binStart * angleSin)
                    rI += 4
                    (0..3).forEach { _ ->
                        radarBuffers.colorBuffer.put(
                            cI++,
                            radarBuffers.colormap.redValues.get(level and 0xFF)
                        )
                        radarBuffers.colorBuffer.put(
                            cI++,
                            radarBuffers.colormap.greenValues.get(level and 0xFF)
                        )
                        radarBuffers.colorBuffer.put(
                            cI++,
                            radarBuffers.colormap.blueValues.get(level and 0xFF)
                        )
                    }
                    totalBins += 1
                    level = curLevel
                    binStart = bin * radarBuffers.binSize + radarBlackHoleAdd
                    levelCount = 1
                }
                bin += 1
            }
            g += 1
        }
        return totalBins
    }

    // FIXME rename 2 char vars to something better
    fun genMercator(inBuff: ByteBuffer, outBuff: ByteBuffer, pn: ProjectionNumbers, count: Int) {
        val centerX = pn.xFloat
        val centerY = pn.yFloat
        val xImageCenterPixels = pn.xCenter.toFloat()
        val yImageCenterPixels = pn.yCenter.toFloat()
        val oneDegreeScaleFactor = pn.oneDegreeScaleFactorFloat
        var iCount = 0
        if (count * 4 <= outBuff.limit()) {
            while (iCount < count) {
                outBuff.putFloat(
                    iCount * 4 + 4,
                    -1.0f * (-((M_180_div_PI * log(
                        tan((M_PI_div_4 + inBuff.getFloat(iCount * 4) * M_PI_div_360).toDouble()),
                        E
                    ).toFloat() - M_180_div_PI * log(
                        tan((M_PI_div_4 + centerX * M_PI_div_360).toDouble()),
                        E
                    ).toFloat()) * oneDegreeScaleFactor) + yImageCenterPixels)
                )
                outBuff.putFloat(
                    iCount * 4,
                    -((inBuff.getFloat(iCount * 4 + 4) - centerY) * oneDegreeScaleFactor) + xImageCenterPixels
                )
                iCount += 2
            }
        }
    }

    fun generate4326Projection(
        inBuff: ByteBuffer,
        outBuff: ByteBuffer,
        pn: ProjectionNumbers,
        count: Int
    ) {
        val pnXFloat = pn.xFloat
        val pnYFloat = pn.yFloat
        val pnXCenter = pn.xCenter
        val pnYCenter = pn.yCenter
        val pnScaleFloat = pn.scaleFloat
        var iCount = 0
        if (count * 4 <= outBuff.limit()) {
            while (iCount < count) {
                outBuff.putFloat(
                    iCount * 4,
                    (-((inBuff.getFloat(iCount * 4 + 4) - pnYFloat) * pnScaleFloat) + pnXCenter.toFloat())
                )
                outBuff.putFloat(
                    iCount * 4 + 4,
                    -(-((inBuff.getFloat(iCount * 4) - pnXFloat) * pnScaleFloat) + pnYCenter.toFloat())
                )
                iCount += 2
            }
        }
    }

    fun genIndex(indexBuff: ByteBuffer, len: Int, breakSizeF: Int) {
        var breakSize = breakSizeF
        var incr: Int
        val remainder: Int
        var chunkCount = 1
        var iCount = 0
        if (len < breakSize) {
            breakSize = len
            remainder = breakSize
        } else {
            chunkCount = len / breakSize
            remainder = len - breakSize * chunkCount
            chunkCount += 1
        }
        var chunkIndex = 0
        var j: Int
        while (chunkIndex < chunkCount) {
            incr = 0
            if (chunkIndex == chunkCount - 1) breakSize = remainder
            j = 0
            while (j < breakSize) {
                indexBuff.putShort(iCount, incr.toShort())
                iCount += 2
                indexBuff.putShort(iCount, (1 + incr).toShort())
                iCount += 2
                indexBuff.putShort(iCount, (2 + incr).toShort())
                iCount += 2
                indexBuff.putShort(iCount, incr.toShort())
                iCount += 2
                indexBuff.putShort(iCount, (2 + incr).toShort())
                iCount += 2
                indexBuff.putShort(iCount, (3 + incr).toShort())
                iCount += 2
                incr += 4
                j += 1
            }
            chunkIndex += 1
        }
    }

    fun genIndexLine(indexBuff: ByteBuffer, len: Int, breakSizeF: Int) {
        var breakSize = breakSizeF
        var incr: Int
        val remainder: Int
        var chunkCount = 1
        val totalBins = len / 4
        var iCount = 0
        if (totalBins < breakSize) {
            breakSize = totalBins
            remainder = breakSize
        } else {
            chunkCount = totalBins / breakSize
            remainder = totalBins - breakSize * chunkCount
            chunkCount += 1
        }
        var j: Int
        indexBuff.position(0)
        (0 until chunkCount).forEach {
            incr = 0
            if (it == chunkCount - 1)
                breakSize = remainder
            j = 0
            while (j < breakSize) {
                indexBuff.putShort(iCount, incr.toShort())
                iCount += 2
                indexBuff.putShort(iCount, (1 + incr).toShort())
                iCount += 2
                incr += 2
                j += 1
            }
        }
    }

    fun genTriangle(
        buffers: ObjectOglBuffers,
        pn: ProjectionNumbers,
        x: DoubleArray,
        y: DoubleArray
    ) {
        var pointX: Double
        var pointY: Double
        var pixYD: Float
        var pixXD: Float
        var iCount = 0
        var ixCount = 0
        var test1: Float
        var test2: Float
        buffers.setToPositionZero()
        while (iCount < buffers.count) {
            pointX = x[iCount]
            pointY = y[iCount]
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + pointX * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
            pixYD = -((test1 - test2) * pn.oneDegreeScaleFactorFloat) + pn.yCenter.toFloat()
            pixXD = (-((pointY - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
            buffers.putFloat(pixXD)
            buffers.putFloat(-pixYD)
            buffers.putFloat(pixXD - buffers.lenInit)
            buffers.putFloat(-pixYD + buffers.lenInit)
            buffers.putFloat(pixXD + buffers.lenInit)
            buffers.putFloat(-pixYD + buffers.lenInit)
            buffers.putIndex(ixCount.toShort())
            buffers.putIndex((ixCount + 1).toShort())
            buffers.putIndex((ixCount + 2).toShort())
            ixCount += 3
            (0..2).forEach { _ ->
                // TODO use class method
                buffers.putColor(buffers.solidColorRed)
                buffers.putColor(buffers.solidColorGreen)
                buffers.putColor(buffers.solidColorBlue)
            }
            iCount += 1
        }
    }

    fun genTriangleUp(
        buffers: ObjectOglBuffers,
        pn: ProjectionNumbers,
        x: DoubleArray,
        y: DoubleArray
    ) {
        var pointX: Double
        var pointY: Double
        var pixYD: Float
        var pixXD: Float
        var iCount = 0
        var ixCount = 0
        var test1: Float
        var test2: Float
        buffers.setToPositionZero()
        while (iCount < buffers.count) {
            pointX = x[iCount]
            pointY = y[iCount]
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + pointX * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
            pixYD = -((test1 - test2) * pn.oneDegreeScaleFactorFloat) + pn.yCenter.toFloat()
            pixXD = (-((pointY - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
            buffers.putFloat(pixXD)
            buffers.putFloat(-pixYD)
            buffers.putFloat(pixXD - buffers.lenInit)
            buffers.putFloat(-pixYD - buffers.lenInit)
            buffers.putFloat(pixXD + buffers.lenInit)
            buffers.putFloat(-pixYD - buffers.lenInit)
            buffers.putIndex(ixCount.toShort())
            buffers.putIndex((ixCount + 1).toShort())
            buffers.putIndex((ixCount + 2).toShort())
            ixCount += 3
            (0..2).forEach { _ ->
                // TODO use class method
                buffers.putColor(buffers.solidColorRed)
                buffers.putColor(buffers.solidColorGreen)
                buffers.putColor(buffers.solidColorBlue)
            }
            iCount += 1
        }
    }

    fun genCircle(
        buffers: ObjectOglBuffers,
        pn: ProjectionNumbers,
        x: DoubleArray,
        y: DoubleArray
    ) {
        var pointX: Double
        var pointY: Double
        var pixYD: Float
        var pixXD: Float
        var iCount = 0
        var ixCount = 0
        var test1: Float
        var test2: Float
        val len = buffers.lenInit * 0.50f
        val triangleAmount = buffers.triangleCount
        var iI = 0
        var lI = 0
        buffers.setToPositionZero()
        while (iCount < buffers.count) {
            pointX = x[iCount]
            pointY = y[iCount]
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + pointX * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
            pixYD = -((test1 - test2) * pn.oneDegreeScaleFactorFloat) + pn.yCenter.toFloat()
            pixXD = (-((pointY - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
            (0 until triangleAmount).forEach {
                buffers.putFloat(lI, pixXD)
                lI += 4
                buffers.putFloat(lI, -pixYD)
                lI += 4
                buffers.putFloat(
                    lI,
                    pixXD + len * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat()
                )
                lI += 4
                buffers.putFloat(
                    lI,
                    -pixYD + len * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat()
                )
                lI += 4
                buffers.putFloat(
                    lI,
                    pixXD + len * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat()
                )
                lI += 4
                buffers.putFloat(
                    lI,
                    -pixYD + len * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat()
                )
                lI += 4
                buffers.putIndex(iI, ixCount.toShort())
                iI += 2
                buffers.putIndex(iI, (ixCount + 1).toShort())
                iI += 2
                buffers.putIndex(iI, (ixCount + 2).toShort())
                iI += 2
                ixCount += 3
                (0..2).forEach { _ ->
                    buffers.putColor(buffers.solidColorRed)
                    buffers.putColor(buffers.solidColorGreen)
                    buffers.putColor(buffers.solidColorBlue)
                }
            }
            iCount += 1
        }
    }

    fun genCircleWithColor(
        buffers: ObjectOglBuffers,
        pn: ProjectionNumbers,
        x: DoubleArray,
        y: DoubleArray
    ) {
        var pointX: Double
        var pointY: Double
        var pixYD: Float
        var pixXD: Float
        var iCount: Int
        var ixCount = 0
        var test1: Float
        var test2: Float
        val len = buffers.lenInit * 0.50f
        var iI = 0
        var lI = 0
        val col = ByteArray(3)
        val triangleAmount = buffers.triangleCount
        buffers.setToPositionZero()
        if (buffers.colorIntArray.size == buffers.count) {
            iCount = 0
            while (iCount < buffers.count && iCount < x.size && iCount < y.size) {
                // TODO set the object colors to these values
                col[0] = Color.red(buffers.colorIntArray[iCount]).toByte()
                col[1] = Color.green(buffers.colorIntArray[iCount]).toByte()
                col[2] = Color.blue(buffers.colorIntArray[iCount]).toByte()
                pointX = x[iCount]
                pointY = y[iCount]
                test1 = M_180_div_PI * log(tan(M_PI_div_4 + pointX * M_PI_div_360), E).toFloat()
                test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
                pixYD = -((test1 - test2) * pn.oneDegreeScaleFactorFloat) + pn.yCenter.toFloat()
                pixXD = (-((pointY - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
                (0 until triangleAmount).forEach {
                    buffers.putFloat(lI, pixXD)
                    lI += 4
                    buffers.putFloat(lI, -pixYD)
                    lI += 4
                    buffers.putFloat(
                        lI,
                        pixXD + len * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat()
                    )
                    lI += 4
                    buffers.putFloat(
                        lI,
                        -pixYD + len * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat()
                    )
                    lI += 4
                    buffers.putFloat(
                        lI,
                        pixXD + len * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat()
                    )
                    lI += 4
                    buffers.putFloat(
                        lI,
                        -pixYD + len * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat()
                    )
                    lI += 4
                    buffers.putIndex(iI, ixCount.toShort())
                    iI += 2
                    buffers.putIndex(iI, (ixCount + 1).toShort())
                    iI += 2
                    buffers.putIndex(iI, (ixCount + 2).toShort())
                    iI += 2
                    ixCount += 3
                    (0..2).forEach { _ ->
                        // TODO use class method
                        buffers.putColor(col[0])
                        buffers.putColor(col[1])
                        buffers.putColor(col[2])
                    }
                }
                iCount += 1
            }
        }
    }

    fun genLocdot(buffers: ObjectOglBuffers, pn: ProjectionNumbers, x: Double, y: Double) {
        buffers.setToPositionZero()
        val pixYD: Float
        val pixXD = (-((y - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
        var ixCount = 0
        val test1 = M_180_div_PI * log(tan(M_PI_div_4 + x * M_PI_div_360), E).toFloat()
        val test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
        val len = buffers.lenInit * 2.0f
        val triangleAmount = buffers.triangleCount
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

    fun decode8BitWX(
        context: Context,
        src: String,
        radialStartAngle: ByteBuffer,
        binWord: ByteBuffer
    ): Short {
        var numberOfRangeBins = 0
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, src))
            dis.bigEndian = true
            // ADVANCE PAST WMO HEADER
            while (dis.readShort().toInt() != -1) {
                // while (dis.readUnsignedShort() != 16) {
            }
            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int
            dis.skipBytes(100)
            val magic = ByteArray(3)
            magic[0] = 'B'.toByte()
            magic[1] = 'Z'.toByte()
            magic[2] = 'h'.toByte()
            val compression = Compression.getCompression(magic)
            val compressedFileSize = dis.length() - dis.filePointer
            val buf = ByteArray(compressedFileSize.toInt())
            dis.read(buf)
            dis.close()
            val decompStream = compression.decompress(ByteArrayInputStream(buf))
            val dis2 = DataInputStream(BufferedInputStream(decompStream))
            dis2.skipBytes(20)
            numberOfRangeBins = dis2.readUnsignedShort()
            dis2.skipBytes(6)
            val numberOfRadials = dis2.readUnsignedShort()
            var r = 0
            var numberOfRleHalfwords: Int
            binWord.position(0)
            radialStartAngle.position(0)
            var tnMod10: Int
            var tn: Int
            var s: Int
            while (r < numberOfRadials) {
                numberOfRleHalfwords = dis2.readUnsignedShort()
                tn = dis2.readUnsignedShort()
                // the code below must stay as drawing to canvas is not as precise as opengl directly for some reason
                if (tn % 2 == 1)
                    tn += 1
                tnMod10 = tn % 10
                if (tnMod10 in 1..4)
                    tn -= tnMod10
                else if (tnMod10 > 6)
                    tn = tn - tnMod10 + 10
                radialStartAngle.putFloat((450 - tn / 10).toFloat())
                dis2.skipBytes(2)
                s = 0
                while (s < numberOfRleHalfwords) {
                    binWord.put((dis2.readUnsignedByte() and 0xFF).toByte())
                    s += 1
                }
                r += 1
            }
            dis2.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        binWord.position(0)
        radialStartAngle.position(0)
        return numberOfRangeBins.toShort()
    }

    fun rect8bitwx(
        rBuff: ByteBuffer,
        binStart: Float,
        binSize: Float,
        levelCount: Int,
        angle: Float,
        angleV: Float,
        centerX: Int,
        centerY: Int
    ) {
        rBuff.position(0)
        rBuff.putFloat(binStart * cos((angle / M_180_div_PI).toDouble()).toFloat() + centerX)
        rBuff.putFloat((binStart * sin((angle / M_180_div_PI).toDouble()).toFloat() - centerY) * -1)
        rBuff.putFloat((binStart + binSize * levelCount) * cos((angle / M_180_div_PI).toDouble()).toFloat() + centerX)
        rBuff.putFloat(((binStart + binSize * levelCount) * sin((angle / M_180_div_PI).toDouble()).toFloat() - centerY) * -1)
        rBuff.putFloat((binStart + binSize * levelCount) * cos(((angle - angleV) / M_180_div_PI).toDouble()).toFloat() + centerX)
        rBuff.putFloat(((binStart + binSize * levelCount) * sin(((angle - angleV) / M_180_div_PI).toDouble()).toFloat() - centerY) * -1)
        rBuff.putFloat(binStart * cos(((angle - angleV) / M_180_div_PI).toDouble()).toFloat() + centerX)
        rBuff.putFloat((binStart * sin(((angle - angleV) / M_180_div_PI).toDouble()).toFloat() - centerY) * -1)
    }

    fun colorGen(colorBuff: ByteBuffer, len: Int, colArr: ByteArray) {
        if (len * 3 <= colorBuff.limit()) {
            (0 until len).forEach { _ ->
                if (colorBuff.hasRemaining()) colorBuff.put(colArr[0])
                if (colorBuff.hasRemaining()) colorBuff.put(colArr[1])
                if (colorBuff.hasRemaining()) colorBuff.put(colArr[2])
            }
        }
    }
    
    
        fun genConus(buffers: ObjectOglBuffers, pn: ProjectionNumbers, x: Double, y: Double) {
        buffers.setToPositionZero()
        val pixYD: Float
        val pixXD = (-((y - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
        var ixCount = 0
        val test1 = M_180_div_PI * log(tan(M_PI_div_4 + x * M_PI_div_360), E).toFloat()
        val test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
        val len = 1 //buffers.lenInit * 2.0f
        val triangleAmount = 1 //buffers.triangleCount
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

    //for single images
    fun genMarker(buffers: ObjectOglBuffers, pn: ProjectionNumbers, x: Double, y: Double) {
        buffers.setToPositionZero()
        val pixYD: Float
        val pixXD = (-((y - pn.yDbl) * pn.oneDegreeScaleFactor) + pn.xCenter).toFloat()
        var ixCount = 0
        val test1 = M_180_div_PI * log(tan(M_PI_div_4 + x * M_PI_div_360), E).toFloat()
        val test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
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

    //for tvs / hi imagees
    fun genMarkerList(buffers: ObjectOglBuffers, pn: ProjectionNumbers, x: DoubleArray, y: DoubleArray) {
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
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + pointX * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + pn.xDbl * M_PI_div_360), E).toFloat()
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
