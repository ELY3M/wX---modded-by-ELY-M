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

package joshuatee.wx.radar

import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Paint.Style
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.*
import java.lang.AssertionError
import java.io.IOException

internal object UtilityNexradRadial8Bit {

    // code below is used for nexrad widget and for notification that shows radar
    fun decodeAndPlot(context: Context, bitmap: Bitmap, fileName: String, product: String) {
        val binWord: ByteBuffer
        val radialStart: ByteBuffer
        val rBuff = ByteBuffer.allocateDirect(32)
        UtilityCanvasMain.setImageOffset(context)
        val canvas = Canvas(bitmap)
        val zeroColor = RadarPreferences.nexradRadarBackgroundColor
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, fileName))
            dis.bigEndian = true
            while (true) {
                if (dis.readShort().toInt() == -1) break
            }
            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int
            //val latitudeOfRadar = dis.readInt() / 1000.0
            //val longitudeOfRadar = dis.readInt() / 1000.0
            //val heightOfRadar = dis.readUnsignedShort().toShort()
            dis.readInt() / 1000.0
            dis.readInt() / 1000.0
            dis.readUnsignedShort().toShort()
            val productCode = dis.readUnsignedShort().toShort()
            //val operationalMode = dis.readUnsignedShort().toShort()
            dis.readUnsignedShort().toShort()
            dis.skipBytes(6)
            val volumeScanDate = dis.readUnsignedShort().toShort()
            val volumeScanTime = dis.readInt()
            val d = UtilityTime.radarTime(volumeScanDate, volumeScanTime)
            try {
                WXGLNexrad.writeRadarTimeForWidget(context, d.toString())
            } catch (e: Exception) {
                WXGLNexrad.writeRadarTimeForWidget(context, "")
                UtilityLog.handleException(e)
            } catch (e: AssertionError) {
                WXGLNexrad.writeRadarTimeForWidget(context, "")
            }
            dis.skipBytes(74)
            val rangeBinAlloc = 1390 // 460 for reflect, set to max possible for velocity - was 1200 for velocity, TZL requires 1390
            val numberOfRadials = 360
            radialStart = ByteBuffer.allocateDirect(4 * numberOfRadials)
            radialStart.position(0)
            radialStart.order(ByteOrder.nativeOrder())
            dis.close()
            binWord = ByteBuffer.allocateDirect(numberOfRadials * rangeBinAlloc)
            binWord.order(ByteOrder.nativeOrder())
            rBuff.order(ByteOrder.nativeOrder())
            rBuff.position(0)
            val numberOfRangeBins = UtilityWXOGLPerf.decode8BitWX(context, fileName, radialStart, binWord)
            val binSize = WXGLNexrad.getBinSize(productCode.toInt()) * 0.2f * UIPreferences.widgetNexradSize.toFloat()
            val centerX = 500 + UtilityCanvasMain.xOffset.toInt()
            val centerY = 500 + UtilityCanvasMain.yOffset.toInt()
            val paint = Paint()
            paint.style = Style.FILL
            val path = Path()
            var angle: Float
            val angleV = 1.0f
            var level: Int
            var levelCount: Int
            var binStart: Float
            var tmpVal: Int
            var x1: Float
            var y1: Float
            var red: Int
            var green: Int
            var blue: Int
            var b: Byte
            var colRgb: Int
            val bufR: ByteBuffer
            val bufG: ByteBuffer
            val bufB: ByteBuffer
            val colorMapProductCode: Int
            when (product) {
                "L2REF" -> colorMapProductCode = 94
                "N0Q" -> colorMapProductCode = 94
                "L2VEL" -> colorMapProductCode = 99
                "N0U" -> colorMapProductCode = 99
                "EET" -> colorMapProductCode = 135
                "DVL" -> colorMapProductCode = 134
                "N0X" -> colorMapProductCode = 159
                "N0C" -> colorMapProductCode = 161
                "N0K" -> colorMapProductCode = 163
                "H0C" -> colorMapProductCode = 165
                "N0S" -> colorMapProductCode = 56
                "DAA" -> colorMapProductCode = 172
                "DSA" -> colorMapProductCode = 172
                else -> colorMapProductCode = 94
            }
            bufR = ObjectColorPalette.colorMap[colorMapProductCode]!!.redValues
            bufG = ObjectColorPalette.colorMap[colorMapProductCode]!!.greenValues
            bufB = ObjectColorPalette.colorMap[colorMapProductCode]!!.blueValues
            for (g in 0 until numberOfRadials) {
                angle = radialStart.float
                binWord.mark()
                level = binWord.get().toInt() and 0xFF
                binWord.reset()
                levelCount = 0
                binStart = binSize
                for (bin in 0 until numberOfRangeBins) {
                    tmpVal = binWord.get().toInt() and 0xFF
                    if (tmpVal == level) {
                        levelCount += 1
                    } else {
                        UtilityWXOGLPerf.rect8bitwx(
                            rBuff,
                            binStart,
                            binSize,
                            levelCount,
                            angle,
                            angleV,
                            centerX,
                            centerY
                        )
                        if (level == 0)
                            paint.color = zeroColor
                        else {
                            b = bufR.get(level)
                            red = b.toInt() and 0xFF
                            b = bufG.get(level)
                            green = b.toInt() and 0xFF
                            b = bufB.get(level)
                            blue = b.toInt() and 0xFF
                            colRgb = Color.rgb(red, green, blue)
                            paint.color = colRgb
                        }
                        path.rewind() // only needed when reusing this path for a new build
                        rBuff.position(0)
                        x1 = rBuff.float
                        y1 = rBuff.float
                        path.moveTo(x1, y1)
                        path.lineTo(rBuff.float, rBuff.float)
                        path.lineTo(rBuff.float, rBuff.float)
                        path.lineTo(rBuff.float, rBuff.float)
                        path.lineTo(x1, y1)
                        canvas.drawPath(path, paint)
                        level = tmpVal
                        binStart = bin * binSize
                        levelCount = 1
                    }
                }
                if (numberOfRangeBins % 2 != 0) binWord.position(binWord.position() + 4)
            }
        } catch (e: IOException) { UtilityLog.handleException(e) }
    }
}
