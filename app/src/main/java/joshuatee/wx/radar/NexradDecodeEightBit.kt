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
import android.graphics.Color
import java.nio.ByteBuffer
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import kotlin.math.*

internal object NexradDecodeEightBit {

    private const val M_180_DIV_PI = (180.0 / PI).toFloat()

    fun andCreateRadials(context: Context, radarBuffers: OglRadarBuffers): Int {
        var totalBins = 0
        try {
            val dataInputStream = UtilityIO.uncompress(context, radarBuffers.fileName)
            dataInputStream.skipBytes(30)
            radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
            radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
            radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
            radarBuffers.floatBuffer.position(0)
            radarBuffers.colorBuffer.position(0)
            var colorIndex = 0
            var radialIndex = 0
            var angleNext = 0.0f
            var angle0 = 0.0f
            val numberOfRadials = radarBuffers.numberOfRadials
            for (radialNumber in 0 until numberOfRadials) {
                val numberOfRleHalfWords = dataInputStream.readUnsignedShort()
                val angle = 450.0f - dataInputStream.readUnsignedShort() / 10.0f
                dataInputStream.skipBytes(2)
                if (radialNumber < numberOfRadials - 1) {
                    dataInputStream.mark(100000)
                    dataInputStream.skipBytes(numberOfRleHalfWords + 2)
                    angleNext = 450.0f - dataInputStream.readUnsignedShort() / 10.0f
                    dataInputStream.reset()
                }
                var level = 0.toByte()
                var levelCount = 0
                var binStart = radarBuffers.binSize
                if (radialNumber == 0) {
                    angle0 = angle
                }
                val angleV = if (radialNumber < numberOfRadials - 1) {
                    angleNext
                } else {
                    angle0
                }
                val angleVCos = cos(angleV / M_180_DIV_PI)
                val angleVSin = sin(angleV / M_180_DIV_PI)
                val angleCos = cos(angle / M_180_DIV_PI)
                val angleSin = sin(angle / M_180_DIV_PI)
                for (bin in 0 until numberOfRleHalfWords) {
                    val curLevel = (dataInputStream.readUnsignedByte() and 0xFF).toByte()
                    if (bin == 0) {
                        level = curLevel
                    }
                    if (curLevel == level) {
                        levelCount += 1
                    } else {
                        radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleVCos)
                        radarBuffers.floatBuffer.putFloat(radialIndex + 4, binStart * angleVSin)
                        radarBuffers.floatBuffer.putFloat(
                            radialIndex + 8,
                            (binStart + radarBuffers.binSize * levelCount) * angleVCos
                        )
                        radarBuffers.floatBuffer.putFloat(
                            radialIndex + 12,
                            (binStart + radarBuffers.binSize * levelCount) * angleVSin
                        )
                        radarBuffers.floatBuffer.putFloat(
                            radialIndex + 16,
                            (binStart + radarBuffers.binSize * levelCount) * angleCos
                        )
                        radarBuffers.floatBuffer.putFloat(
                            radialIndex + 20,
                            (binStart + radarBuffers.binSize * levelCount) * angleSin
                        )
                        radarBuffers.floatBuffer.putFloat(radialIndex + 24, binStart * angleCos)
                        radarBuffers.floatBuffer.putFloat(radialIndex + 28, binStart * angleSin)
                        radialIndex += 32
                        for (unused in 0..3) {
                            radarBuffers.colorBuffer.put(
                                colorIndex,
                                radarBuffers.colormap.redValues.get(level.toInt() and 0xFF)
                            )
                            radarBuffers.colorBuffer.put(
                                colorIndex + 1,
                                radarBuffers.colormap.greenValues.get(level.toInt() and 0xFF)
                            )
                            radarBuffers.colorBuffer.put(
                                colorIndex + 2,
                                radarBuffers.colormap.blueValues.get(level.toInt() and 0xFF)
                            )
                            colorIndex += 3
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

    // level2
    fun createRadials(
        radarBuffers: OglRadarBuffers,
        binBuff: ByteBuffer,
        radialStart: ByteBuffer
    ): Int {
        radarBuffers.colormap.redValues.put(0, Color.red(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.greenValues.put(0, Color.green(radarBuffers.bgColor).toByte())
        radarBuffers.colormap.blueValues.put(0, Color.blue(radarBuffers.bgColor).toByte())
        var totalBins = 0
        var binIndex = 0
        var colorIndex = 0
        var radialIndex = 0
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
            val angle = radialStart.getFloat(radialNumber * 4)
            var level = binBuff.get(binIndex).toInt()
            var levelCount = 0
            var binStart = radarBlackHole
            val angleV = if (radialNumber < radarBuffers.numberOfRadials - 1) {
                radialStart.getFloat(radialNumber * 4 + 4)
            } else {
                radialStart.getFloat(0)
            }
            val angleVCos = cos(angleV / M_180_DIV_PI)
            val angleVSin = sin(angleV / M_180_DIV_PI)
            val angleCos = cos(angle / M_180_DIV_PI)
            val angleSin = sin(angle / M_180_DIV_PI)
            for (bin in 0 until radarBuffers.numRangeBins) {
                val curLevel = binBuff.get(binIndex).toInt()
                binIndex += 1
                if (curLevel == level) {
                    levelCount += 1
                } else {
                    radarBuffers.floatBuffer.putFloat(radialIndex, binStart * angleVCos)
                    radarBuffers.floatBuffer.putFloat(radialIndex + 4, binStart * angleVSin)
                    radarBuffers.floatBuffer.putFloat(
                        radialIndex + 8,
                        (binStart + radarBuffers.binSize * levelCount) * angleVCos
                    )
                    radarBuffers.floatBuffer.putFloat(
                        radialIndex + 12,
                        (binStart + radarBuffers.binSize * levelCount) * angleVSin
                    )
                    radarBuffers.floatBuffer.putFloat(
                        radialIndex + 16,
                        (binStart + radarBuffers.binSize * levelCount) * angleCos
                    )
                    radarBuffers.floatBuffer.putFloat(
                        radialIndex + 20,
                        (binStart + radarBuffers.binSize * levelCount) * angleSin
                    )
                    radarBuffers.floatBuffer.putFloat(radialIndex + 24, binStart * angleCos)
                    radarBuffers.floatBuffer.putFloat(radialIndex + 28, binStart * angleSin)
                    radialIndex += 32
                    for (unused in 0..3) {
                        radarBuffers.colorBuffer.put(
                            colorIndex,
                            radarBuffers.colormap.redValues.get(level and 0xFF)
                        )
                        radarBuffers.colorBuffer.put(
                            colorIndex + 1,
                            radarBuffers.colormap.greenValues.get(level and 0xFF)
                        )
                        radarBuffers.colorBuffer.put(
                            colorIndex + 2,
                            radarBuffers.colormap.blueValues.get(level and 0xFF)
                        )
                        colorIndex += 3
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

    //
    // canvas based widget
    //
    fun forCanvas(
        context: Context,
        src: String,
        radialStartAngle: ByteBuffer,
        binWord: ByteBuffer
    ): Short {
        var numberOfRangeBins = 0
        try {
            val dataInputStream = UtilityIO.uncompress(context, src)
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
                if (tn % 2 == 1) {
                    tn += 1
                }
                tnMod10 = tn % 10
                if (tnMod10 in 1..4) {
                    tn -= tnMod10
                } else if (tnMod10 > 6) {
                    tn = tn - tnMod10 + 10
                }
                radialStartAngle.putFloat(450.0f - tn / 10.0f)
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

    //
    // canvas based widget
    //
    fun rect8bit(
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
        rBuff.putFloat(binStart * cos(angle / M_180_DIV_PI) + centerX)
        rBuff.putFloat((binStart * sin(angle / M_180_DIV_PI) - centerY) * -1.0f)
        rBuff.putFloat((binStart + binSize * levelCount) * cos(angle / M_180_DIV_PI) + centerX)
        rBuff.putFloat(((binStart + binSize * levelCount) * sin(angle / M_180_DIV_PI) - centerY) * -1.0f)
        rBuff.putFloat((binStart + binSize * levelCount) * cos((angle - angleV) / M_180_DIV_PI) + centerX)
        rBuff.putFloat(((binStart + binSize * levelCount) * sin((angle - angleV) / M_180_DIV_PI) - centerY) * -1.0f)
        rBuff.putFloat(binStart * cos((angle - angleV) / M_180_DIV_PI) + centerX)
        rBuff.putFloat((binStart * sin((angle - angleV) / M_180_DIV_PI) - centerY) * -1.0f)
    }
}
