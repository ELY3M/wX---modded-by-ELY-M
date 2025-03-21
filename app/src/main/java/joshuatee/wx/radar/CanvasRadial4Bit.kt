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

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Paint.Style
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityMath

@Suppress("SpellCheckingInspection")
internal object CanvasRadial4Bit {

    fun decodeAndPlot(context: Context, bitmap: Bitmap, fileName: String, product: String) {
        val canvas = Canvas(bitmap)
        val nwsRadarBgBlack = Utility.readPref(context, "NWS_RADAR_BG_BLACK", "")
        val zeroColor = if (nwsRadarBgBlack != "true") {
            ContextCompat.getColor(context, R.color.white)
        } else {
            ContextCompat.getColor(context, R.color.black)
        }
        val isVelocity = product.contains("S") || product.contains("V") || product.contains("U")
        val dis = try {
            val fis = context.openFileInput(fileName)
            DataInputStream(BufferedInputStream(fis))
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            null
        }
        try {
            if (dis != null) {
                dis.skipBytes(50)
                val latitudeOfRadar = dis.readInt() / 1000.0
                val longitudeOfRadar = dis.readInt() / 1000.0
                val heightOfRadar = dis.readUnsignedShort().toShort()
                val productCode = dis.readUnsignedShort().toShort()
                val operationalMode = dis.readUnsignedShort().toShort()
                dis.skipBytes(6)
                val volumeScanDate = dis.readUnsignedShort().toShort()
                val volumeScanTime = dis.readInt()
                val date = ObjectDateTime.radarTime(volumeScanDate, volumeScanTime)
                dis.skipBytes(6)
                val radarInfo = date + GlobalVariables.newline +
                        "Radar Mode: " + operationalMode.toInt()
                    .toString() + GlobalVariables.newline +
                        "Product Code: " + productCode.toInt()
                    .toString() + GlobalVariables.newline +
                        "Radar height: " + heightOfRadar.toInt()
                    .toString() + GlobalVariables.newline +
                        "Radar Lat: " + latitudeOfRadar.toString() + GlobalVariables.newline +
                        "Radar Lon: " + longitudeOfRadar.toString() + GlobalVariables.newline
                NexradUtil.writeRadarInfo(context, "", radarInfo)
                dis.skipBytes(88)
                val numberOfRangeBins = dis.readUnsignedShort()
                dis.skipBytes(6)
                val numberOfRadials = dis.readUnsignedShort()
                val numberOfRleHalfwords = IntArray(numberOfRadials)
                val radialStartAngle = FloatArray(numberOfRadials)
                val radialAngleDelta = FloatArray(numberOfRadials)
                val binWord = Array(numberOfRadials) { IntArray(numberOfRangeBins) }
                var tnMod10: Int
                for (r in 0 until numberOfRadials) {
                    numberOfRleHalfwords[r] = dis.readUnsignedShort()
                    var tn = dis.readUnsignedShort()
                    if (tn % 2 == 1) {
                        tn += 1
                    }
                    tnMod10 = tn % 10
                    if (tnMod10 in 1..4) {
                        tn -= tnMod10
                    } else if (tnMod10 > 6) {
                        tn = tn - tnMod10 + 10
                    }
                    radialStartAngle[r] = (450.0f - tn / 10.0f)
                    radialAngleDelta[r] = dis.readUnsignedShort().toFloat()
                    radialAngleDelta[r] = 1.0f
                    var binCount = 0
                    for (s in 0 until numberOfRleHalfwords[r] * 2) {
                        // old 4 bit
                        val bin = dis.readUnsignedByte()
                        val numOfBins = bin shr 4
                        for (u in 0 until numOfBins) {
                            binWord[r][binCount] = bin % 16
                            binCount += 1
                        }
                    }
                }
                dis.close()
                val graphColor = IntArray(16)
                graphColor[0] = Color.parseColor("#000000")
                graphColor[1] = Color.parseColor("#00ECEC")
                graphColor[2] = Color.parseColor("#01A0F6")
                graphColor[3] = Color.parseColor("#0000F6")
                graphColor[4] = Color.parseColor("#00FF00")
                graphColor[5] = Color.parseColor("#00C800")
                graphColor[6] = Color.parseColor("#009000")
                graphColor[7] = Color.parseColor("#FFFF00")
                graphColor[8] = Color.parseColor("#E7C000")
                graphColor[9] = Color.parseColor("#FF9000")
                graphColor[10] = Color.parseColor("#FF0000")
                graphColor[11] = Color.parseColor("#D60000")
                graphColor[12] = Color.parseColor("#C00000")
                graphColor[13] = Color.parseColor("#FF00FF")
                graphColor[14] = Color.parseColor("#9955C9")
                graphColor[15] = Color.parseColor("#FFFFFF")
                val graphColor2 = IntArray(16)
                graphColor2[0] = Color.parseColor("#000000")
                graphColor2[1] = Color.parseColor("#02FC02")
                graphColor2[2] = Color.parseColor("#01E401")
                graphColor2[3] = Color.parseColor("#01C501")
                graphColor2[4] = Color.parseColor("#07AC04")
                graphColor2[5] = Color.parseColor("#068F03")
                graphColor2[6] = Color.parseColor("#047202")
                graphColor2[7] = Color.parseColor("#7C977B")
                graphColor2[8] = Color.parseColor("#987777")
                graphColor2[9] = Color.parseColor("#890000")
                graphColor2[10] = Color.parseColor("#A20000")
                graphColor2[11] = Color.parseColor("#B90000")
                graphColor2[12] = Color.parseColor("#D80000")
                graphColor2[13] = Color.parseColor("#EF0000")
                graphColor2[14] = Color.parseColor("#FE0000")
                graphColor2[15] = Color.parseColor("#9000A0")
                val binSize = NexradUtil.getBinSize(productCode.toInt())
                val centerX = 500
                val centerY = 500
                var xy1: FloatArray
                var xy2: FloatArray
                var xy3: FloatArray
                var xy4: FloatArray
                val paint = Paint()
                paint.style = Style.FILL
                val path = Path()
                var angle: Float
                var angleV: Float
                var level: Int
                var levelCount: Int
                var binStart: Float
                for (g in 0 until numberOfRadials) {
                    angle = radialStartAngle[g]
                    angleV = radialAngleDelta[g]
                    level = binWord[g][0]
                    levelCount = 0
                    binStart = binSize
                    for (bin in 0 until numberOfRangeBins) {
                        if (binWord[g][bin] == level && bin != numberOfRangeBins - 1) {
                            levelCount += 1
                        } else {
                            xy1 = UtilityMath.toRect(binStart, angle)
                            xy2 = UtilityMath.toRect(binStart + binSize * levelCount, angle)
                            xy3 =
                                UtilityMath.toRect(binStart + binSize * levelCount, angle - angleV)
                            xy4 = UtilityMath.toRect(binStart, angle - angleV)
                            xy1[0] += centerX.toFloat()
                            xy2[0] += centerX.toFloat()
                            xy3[0] += centerX.toFloat()
                            xy4[0] += centerX.toFloat()
                            xy1[1] = (xy1[1] - centerY) * -1.0f
                            xy2[1] = (xy2[1] - centerY) * -1.0f
                            xy3[1] = (xy3[1] - centerY) * -1.0f
                            xy4[1] = (xy4[1] - centerY) * -1.0f
                            if (level == 0) {
                                paint.color = zeroColor
                            } else {
                                if (isVelocity) {
                                    paint.color = graphColor2[level]
                                } else {
                                    paint.color = graphColor[level]
                                }
                            }
                            with(path) {
                                rewind()
                                moveTo(xy1[0], xy1[1])
                                lineTo(xy2[0], xy2[1])
                                lineTo(xy3[0], xy3[1])
                                lineTo(xy4[0], xy4[1])
                                lineTo(xy1[0], xy1[1])
                            }
                            canvas.drawPath(path, paint)
                            level = binWord[g][bin]
                            binStart = bin * binSize
                            levelCount = 1
                        }
                    }
                }
            }
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
    }
}
