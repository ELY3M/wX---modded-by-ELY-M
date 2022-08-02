/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

    private const val M_180_div_PI = (180.0 / PI).toFloat()
    private const val M_PI_div_4 = (PI / 4.0).toFloat()
    private const val M_PI_div_360 = (PI / 360.0).toFloat()
    private const val TWICE_PI = (2.0 * PI).toFloat()

    fun decode8BitAndGenRadials(context: Context, radarBuffers: ObjectOglRadarBuffers): Int {
        var totalBins = 0
        try {
            val dataInputStream = UtilityIO.uncompress(context, radarBuffers.fileName)
            dataInputStream.skipBytes(30)
            var numberOfRleHalfWords: Int
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
            var colorIndex = 0
            var radialIndex = 0
            var curLevel = 0.toByte()
            var angleSin: Float
            var angleCos: Float
            var angleVSin: Float
            var angleVCos: Float
            var angleNext = 0f
            var angle0 = 0f
            // val numberOfRadials = 360
            val numberOfRadials = radarBuffers.numberOfRadials
            for (radialNumber in 0 until numberOfRadials) {
                numberOfRleHalfWords = dataInputStream.readUnsignedShort()
                angle = 450f - dataInputStream.readUnsignedShort() / 10f
                dataInputStream.skipBytes(2)
                if (radialNumber < numberOfRadials - 1) {
                    dataInputStream.mark(100000)
                    dataInputStream.skipBytes(numberOfRleHalfWords + 2)
                    angleNext = 450f - dataInputStream.readUnsignedShort() / 10f
                    dataInputStream.reset()
                }
                level = 0.toByte()
                levelCount = 0
                binStart = radarBuffers.binSize
                if (radialNumber == 0) angle0 = angle
                angleV = if (radialNumber < numberOfRadials - 1) {
                    angleNext
                } else {
                    angle0
                }
                angleVCos = cos(angleV / M_180_div_PI)
                angleVSin = sin(angleV / M_180_div_PI)
                angleCos = cos(angle / M_180_div_PI)
                angleSin = sin(angle / M_180_div_PI)

                for (bin in 0 until numberOfRleHalfWords) {
                    try {
                        curLevel = (dataInputStream.readUnsignedByte() and 0xFF).toByte() // was dis2!!.readUnsignedByte().toInt()
                    } catch (e: Exception) {
                        UtilityLog.handleException(e)
                    }
                    if (bin == 0) {
                        level = curLevel
                    }
                    if (curLevel == level) {
                        levelCount += 1
                    } else {
                        radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleVCos)
                        radialIndex += 4
                        radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleVSin)
                        radialIndex += 4
                        radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleVCos)
                        radialIndex += 4
                        radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleVSin)
                        radialIndex += 4

                        radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleCos)
                        radialIndex += 4
                        radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleSin)
                        radialIndex += 4
                        radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleCos)
                        radialIndex += 4
                        radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleSin)
                        radialIndex += 4
                        repeat(4) {
                            radarBuffers.colorBuffer.put(colorIndex, radarBuffers.colormap.redValues.get(level.toInt() and 0xFF))
                            colorIndex += 1
                            radarBuffers.colorBuffer.put(colorIndex, radarBuffers.colormap.greenValues.get(level.toInt() and 0xFF))
                            colorIndex += 1
                            radarBuffers.colorBuffer.put(colorIndex, radarBuffers.colormap.blueValues.get(level.toInt() and 0xFF))
                            colorIndex += 1
                        }
                        totalBins += 1
                        level = curLevel
                        binStart = bin * radarBuffers.binSize
                        levelCount = 1
                    }
                }
            }
            dataInputStream.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return totalBins
    }

    fun genRadials(radarBuffers: ObjectOglRadarBuffers, binBuff: ByteBuffer, radialStart: ByteBuffer): Int {
        radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
        var totalBins = 0
        var angle: Float
        var angleV: Float
        var level: Int
        var levelCount: Int
        var binStart: Float
        var binIndex = 0
        var colorIndex = 0
        var radialIndex = 0
        var curLevel: Int
        var angleSin: Float
        var angleCos: Float
        var angleVSin: Float
        var angleVCos: Float
        val radarBlackHole: Float
        val radarBlackHoleAdd: Float
        when (radarBuffers.productCode.toInt()) {
            56, 30, 78, 80, 181 -> {
                radarBlackHole = 1.0f
                radarBlackHoleAdd = 0.0f
            }
            else -> {
                radarBlackHole = 4.0f
                radarBlackHoleAdd = 4.0f
            }
        }
        for (radialNumber in 0 until radarBuffers.numberOfRadials) {
            angle = radialStart.getFloat(radialNumber * 4)
            level = binBuff.get(binIndex).toInt()
            levelCount = 0
            binStart = radarBlackHole
            angleV = if (radialNumber < radarBuffers.numberOfRadials - 1) {
                radialStart.getFloat(radialNumber * 4 + 4)
            } else {
                radialStart.getFloat(0)
            }
            angleVCos = cos(angleV / M_180_div_PI)
            angleVSin = sin(angleV / M_180_div_PI)
            angleCos = cos(angle / M_180_div_PI)
            angleSin = sin(angle / M_180_div_PI)
            for (bin in 0 until radarBuffers.numRangeBins) {
                curLevel = binBuff.get(binIndex).toInt()
                binIndex += 1
                if (curLevel == level) {
                    levelCount += 1
                } else {
                    radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleVCos)
                    radialIndex += 4
                    radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleVSin)
                    radialIndex += 4
                    radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleVCos)
                    radialIndex += 4
                    radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleVSin)
                    radialIndex += 4

                    radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleCos)
                    radialIndex += 4
                    radarBuffers.floatBuffer.putFloat(radialIndex, (binStart + radarBuffers.binSize * levelCount) * angleSin)
                    radialIndex += 4
                    radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleCos)
                    radialIndex += 4
                    radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleSin)
                    radialIndex += 4
                    repeat(4) {
                        radarBuffers.colorBuffer.put(colorIndex, radarBuffers.colormap.redValues.get(level and 0xFF))
                        colorIndex += 1
                        radarBuffers.colorBuffer.put(colorIndex, radarBuffers.colormap.greenValues.get(level and 0xFF))
                        colorIndex += 1
                        radarBuffers.colorBuffer.put(colorIndex, radarBuffers.colormap.blueValues.get(level and 0xFF))
                        colorIndex += 1
                    }
                    totalBins += 1
                    level = curLevel
                    binStart = bin * radarBuffers.binSize + radarBlackHoleAdd
                    levelCount = 1
                }
            }
        }
        return totalBins
    }

    fun genMercator(inBuff: ByteBuffer, outBuff: ByteBuffer, pn: ProjectionNumbers, count: Int) {
        val pnXFloat = pn.xFloat
        val pnYFloat = pn.yFloat
        val pnXCenter = pn.xCenter.toFloat()
        val pnYCenter = pn.yCenter.toFloat()
        val oneDegreeScaleFactor = pn.oneDegreeScaleFactorFloat
        if (count * 4 <= outBuff.limit()) {
            for (iCount in 0 until count step 2) {
                outBuff.putFloat(iCount * 4 + 4,
                        -1.0f * (-((M_180_div_PI * log(tan((M_PI_div_4 + inBuff.getFloat(iCount * 4) * M_PI_div_360).toDouble()), E).toFloat() - M_180_div_PI * log(
                        tan((M_PI_div_4 + pnXFloat * M_PI_div_360).toDouble()), E).toFloat()) * oneDegreeScaleFactor) + pnYCenter))
                outBuff.putFloat(iCount * 4, -((inBuff.getFloat(iCount * 4 + 4) - pnYFloat) * oneDegreeScaleFactor) + pnXCenter)
            }
        }
    }

    //elys mod - keeping this
    fun generate4326Projection(inBuff: ByteBuffer, outBuff: ByteBuffer, pn: ProjectionNumbers, count: Int) {
        val pnXFloat = pn.xFloat
        val pnYFloat = pn.yFloat
        val pnXCenter = pn.xCenter
        val pnYCenter = pn.yCenter
        val pnScaleFloat = pn.scaleFloat
        if (count * 4 <= outBuff.limit()) {
            for (iCount in 0 until count step 2) {
                outBuff.putFloat(iCount * 4, (-((inBuff.getFloat(iCount * 4 + 4) - pnYFloat) * pnScaleFloat) + pnXCenter.toFloat()))
                outBuff.putFloat(iCount * 4 + 4, -(-((inBuff.getFloat(iCount * 4) - pnXFloat) * pnScaleFloat) + pnYCenter.toFloat()))
            }
        }
    }

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
            if (chunkIndex == chunkCount - 1) breakSize = remainder
            for (notUsed in 0 until breakSize) {
                indexBuff.putShort(indexForIndex, indexCount.toShort())
                indexForIndex += 2
                indexBuff.putShort(indexForIndex, (1 + indexCount).toShort())
                indexForIndex += 2
                indexBuff.putShort(indexForIndex, (2 + indexCount).toShort())
                indexForIndex += 2
                indexBuff.putShort(indexForIndex, indexCount.toShort())
                indexForIndex += 2
                indexBuff.putShort(indexForIndex, (2 + indexCount).toShort())
                indexForIndex += 2
                indexBuff.putShort(indexForIndex, (3 + indexCount).toShort())
                indexForIndex += 2
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
            if (it == chunkCount - 1) breakSize = remainder
            for (notUsed in 0 until breakSize) {
                indexBuff.putShort(indexForIndex, indexCount.toShort())
                indexForIndex += 2
                indexBuff.putShort(indexForIndex, (1 + indexCount).toShort())
                indexForIndex += 2
                indexCount += 2
            }
        }
    }

    fun genTriangle(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
        var pixYD: Float
        var pixXD: Float
        var indexCount = 0
        var test1: Float
        var test2: Float
        buffers.setToPositionZero()
        (0 until buffers.count).forEach { index ->
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + buffers.xList[index] * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + projectionNumbers.xDbl * M_PI_div_360), E).toFloat()
            pixYD = -((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
            pixXD = (-((buffers.yList[index] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
            buffers.putFloat(pixXD)
            buffers.putFloat(-pixYD)
            buffers.putFloat(pixXD - buffers.lenInit)
            buffers.putFloat(-pixYD + buffers.lenInit)
            buffers.putFloat(pixXD + buffers.lenInit)
            buffers.putFloat(-pixYD + buffers.lenInit)
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

    fun genTriangleUp(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
        var pixYD: Float
        var pixXD: Float
        var indexCount = 0
        var test1: Float
        var test2: Float
        buffers.setToPositionZero()
        (0 until buffers.count).forEach { index ->
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + buffers.xList[index] * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + projectionNumbers.xDbl * M_PI_div_360), E).toFloat()
            pixYD = -((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
            pixXD = (-((buffers.yList[index] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
            buffers.putFloat(pixXD)
            buffers.putFloat(-pixYD)
            buffers.putFloat(pixXD - buffers.lenInit)
            buffers.putFloat(-pixYD - buffers.lenInit)
            buffers.putFloat(pixXD + buffers.lenInit)
            buffers.putFloat(-pixYD - buffers.lenInit)
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

    fun genCircle(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
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
            test1 = M_180_div_PI * log(tan(M_PI_div_4 + buffers.xList[index] * M_PI_div_360), E).toFloat()
            test2 = M_180_div_PI * log(tan(M_PI_div_4 + projectionNumbers.xDbl * M_PI_div_360), E).toFloat()
            pixYD = -((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
            pixXD = (-((buffers.yList[index] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
            (0 until triangleAmount).forEach {
                buffers.putFloat(bufferIndex, pixXD)
                bufferIndex += 4
                buffers.putFloat(bufferIndex, -pixYD)
                bufferIndex += 4
                buffers.putFloat(bufferIndex, pixXD + len * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
                bufferIndex += 4
                buffers.putFloat(bufferIndex, -pixYD + len * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
                bufferIndex += 4
                buffers.putFloat(bufferIndex, pixXD + len * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
                bufferIndex += 4
                buffers.putFloat(bufferIndex, -pixYD + len * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
                bufferIndex += 4
                buffers.putIndex(indexForIndex, indexCount.toShort())
                indexForIndex += 2
                buffers.putIndex(indexForIndex, (indexCount + 1).toShort())
                indexForIndex += 2
                buffers.putIndex(indexForIndex, (indexCount + 2).toShort())
                indexForIndex += 2
                indexCount += 3
                repeat(3) {
                    buffers.putColor(buffers.solidColorRed)
                    buffers.putColor(buffers.solidColorGreen)
                    buffers.putColor(buffers.solidColorBlue)
                }
            }
        }
    }

    fun genCircleWithColor(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
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
                // TODO set the object colors to these values
                col[0] = Color.red(buffers.colorIntArray[iCount]).toByte()
                col[1] = Color.green(buffers.colorIntArray[iCount]).toByte()
                col[2] = Color.blue(buffers.colorIntArray[iCount]).toByte()
                test1 = M_180_div_PI * log(tan(M_PI_div_4 + buffers.xList[iCount] * M_PI_div_360), E).toFloat()
                test2 = M_180_div_PI * log(tan(M_PI_div_4 + projectionNumbers.xDbl * M_PI_div_360), E).toFloat()
                pixYD = -((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
                pixXD = (-((buffers.yList[iCount] - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
                (0 until triangleAmount).forEach {
                    buffers.putFloat(bufferIndex, pixXD)
                    bufferIndex += 4
                    buffers.putFloat(bufferIndex, -pixYD)
                    bufferIndex += 4
                    buffers.putFloat(bufferIndex, pixXD + len * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
                    bufferIndex += 4
                    buffers.putFloat(bufferIndex, -pixYD + len * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
                    bufferIndex += 4
                    buffers.putFloat(bufferIndex, pixXD + len * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
                    bufferIndex += 4
                    buffers.putFloat(bufferIndex, -pixYD + len * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
                    bufferIndex += 4
                    buffers.putIndex(indexForIndex, indexCount.toShort())
                    indexForIndex += 2
                    buffers.putIndex(indexForIndex, (indexCount + 1).toShort())
                    indexForIndex += 2
                    buffers.putIndex(indexForIndex, (indexCount + 2).toShort())
                    indexForIndex += 2
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

//elys mod
    fun genLocdot(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers, x: Double, y: Double) {
        buffers.setToPositionZero()
        val pixYD: Float
        val pixXD = (-((y - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
        var indexCount = 0
        val test1 = M_180_div_PI * log(tan(M_PI_div_4 + x * M_PI_div_360), E).toFloat()
        val test2 = M_180_div_PI * log(tan(M_PI_div_4 + projectionNumbers.xDbl * M_PI_div_360), E).toFloat()
        val length = buffers.lenInit * 2.0f
        val triangleAmount = buffers.triangleCount
        pixYD = -((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
        (0 until triangleAmount).forEach {
            buffers.putFloat(pixXD + length * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(-pixYD + length * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(pixXD + length * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(-pixYD + length * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putIndex(indexCount.toShort())
            buffers.putIndex((indexCount + 1).toShort())
            indexCount += 2
        }
    }
    
    /*
    
    fun genCircleLocdot(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers, x: Double, y: Double) {
        buffers.setToPositionZero()
        val test1 = M_180_div_PI * log(tan(M_PI_div_4 + x * M_PI_div_360), E).toFloat()
        val test2 = M_180_div_PI * log(tan(M_PI_div_4 + projectionNumbers.xDbl * M_PI_div_360), E).toFloat()
        val length = buffers.lenInit * 2.0f
        val triangleAmount = buffers.triangleCount
        var indexCount = 0
        val pixXD = (-((y - projectionNumbers.yDbl) * projectionNumbers.oneDegreeScaleFactor) + projectionNumbers.xCenter).toFloat()
        val pixYD = -((test1 - test2) * projectionNumbers.oneDegreeScaleFactorFloat) + projectionNumbers.yCenter.toFloat()
        (0 until triangleAmount).forEach {
            buffers.putFloat(pixXD + length * cos((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(-pixYD + length * sin((it * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(pixXD + length * cos(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putFloat(-pixYD + length * sin(((it + 1) * TWICE_PI / triangleAmount).toDouble()).toFloat())
            buffers.putIndex(indexCount.toShort())
            buffers.putIndex((indexCount + 1).toShort())
            indexCount += 2
        }
    }
    

    */

    fun decode8BitWX(context: Context, src: String, radialStartAngle: ByteBuffer, binWord: ByteBuffer): Short {
        var numberOfRangeBins = 0
        try {
            val ucarRandomAccessFile = UCARRandomAccessFile(UtilityIO.getFilePath(context, src))
            ucarRandomAccessFile.bigEndian = true
            // ADVANCE PAST WMO HEADER
            while (ucarRandomAccessFile.readShort().toInt() != -1) {
                // while (dis.readUnsignedShort() != 16) {
            }
            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int
            ucarRandomAccessFile.skipBytes(100)
            val magic = ByteArray(3)
            magic[0] = 'B'.code.toByte()
            magic[1] = 'Z'.code.toByte()
            magic[2] = 'h'.code.toByte()
            val compression = Compression.getCompression(magic)
            val compressedFileSize = ucarRandomAccessFile.length() - ucarRandomAccessFile.filePointer
            val buf = ByteArray(compressedFileSize.toInt())
            ucarRandomAccessFile.read(buf)
            ucarRandomAccessFile.close()
            val decompStream = compression.decompress(ByteArrayInputStream(buf))
            val dataInputStream = DataInputStream(BufferedInputStream(decompStream))
            dataInputStream.skipBytes(20)
            numberOfRangeBins = dataInputStream.readUnsignedShort()
            dataInputStream.skipBytes(6)
            val numberOfRadials = dataInputStream.readUnsignedShort()
            var numberOfRleHalfwords: Int
            binWord.position(0)
            radialStartAngle.position(0)
            var tnMod10: Int
            var tn: Int
            for (r in 0 until numberOfRadials) {
                numberOfRleHalfwords = dataInputStream.readUnsignedShort()
                tn = dataInputStream.readUnsignedShort()
                // the code below must stay as drawing to canvas is not as precise as opengl directly for some reason
                if (tn % 2 == 1) tn += 1
                tnMod10 = tn % 10
                if (tnMod10 in 1..4)
                    tn -= tnMod10
                else if (tnMod10 > 6)
                    tn = tn - tnMod10 + 10
                radialStartAngle.putFloat((450 - tn / 10).toFloat())
                dataInputStream.skipBytes(2)
                for (s in 0 until numberOfRleHalfwords) {
                    binWord.put((dataInputStream.readUnsignedByte() and 0xFF).toByte())
                }
            }
            dataInputStream.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        binWord.position(0)
        radialStartAngle.position(0)
        return numberOfRangeBins.toShort()
    }

    fun rect8bitwx(rBuff: ByteBuffer, binStart: Float, binSize: Float, levelCount: Int, angle: Float, angleV: Float, centerX: Int, centerY: Int) {
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

    fun colorGen(colorBuff: ByteBuffer, length: Int, colors: ByteArray) {
        try {
            if (length * 3 <= colorBuff.limit()) {
                for (notUsed in 0 until length) {
                    if (colorBuff.hasRemaining()) colorBuff.put(colors[0])
                    if (colorBuff.hasRemaining()) colorBuff.put(colors[1])
                    if (colorBuff.hasRemaining()) colorBuff.put(colors[2])
                }
            }
        } catch (e: Exception) { UtilityLog.handleException(e) }
    }
    
        //elys mods
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

    //for tvs / hi images
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
