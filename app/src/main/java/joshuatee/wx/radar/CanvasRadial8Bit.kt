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
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import java.io.IOException

internal object CanvasRadial8Bit {

    // code below is used for nexrad widget and for notification that shows radar
    fun decodeAndPlot(context: Context, bitmap: Bitmap, fileName: String, product: String) {
        CanvasMain.setImageOffset(context)
        try {
            val canvas = Canvas(bitmap)
            val zeroColor = RadarPreferences.nexradBackgroundColor
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, fileName))
            dis.bigEndian = true
            while (true) {
                if (dis.readShort().toInt() == -1) {
                    break
                }
            }
            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int
            //val latitudeOfRadar = dis.readInt() / 1000.0
            //val longitudeOfRadar = dis.readInt() / 1000.0
            //val heightOfRadar = dis.readUnsignedShort().toShort()
            dis.readInt()
            dis.readInt()
            dis.readUnsignedShort().toShort()
            val productCode = dis.readUnsignedShort().toShort()
            dis.readUnsignedShort().toShort()
            dis.skipBytes(6)
            val volumeScanDate = dis.readUnsignedShort().toShort()
            val volumeScanTime = dis.readInt()
            val date = ObjectDateTime.radarTime(volumeScanDate, volumeScanTime)
            NexradUtil.writeRadarTimeForWidget(context, date)
//            dis.skipBytes(74)
            dis.close()
            val rangeBinAlloc = 1390 // 460 for reflect, set to max possible for velocity - was 1200 for velocity, TZL requires 1390
            val numberOfRadials = 360
            val radialStart = ByteBuffer.allocateDirect(4 * numberOfRadials)
            radialStart.position(0)
            radialStart.order(ByteOrder.nativeOrder())
            val binWord = ByteBuffer.allocateDirect(numberOfRadials * rangeBinAlloc)
            binWord.order(ByteOrder.nativeOrder())
            val rBuff = ByteBuffer.allocateDirect(32)
            rBuff.order(ByteOrder.nativeOrder())
            rBuff.position(0)
            val numberOfRangeBins = NexradDecodeEightBit.forCanvas(context, fileName, radialStart, binWord)
            val binSize = NexradUtil.getBinSize(productCode.toInt()) * 0.2f * UIPreferences.widgetNexradSize.toFloat()
            val centerX = 500 + CanvasMain.xOffset.toInt()
            val centerY = 500 + CanvasMain.yOffset.toInt()
            val paint = Paint()
            paint.style = Style.FILL
            val path = Path()
            val angleV = 1.0f
            val colorMapProductCode = when (product) {
                "L2REF" -> 94
                "N0Q" -> 94
                "L2VEL" -> 99
                "N0U" -> 99
                "EET" -> 135
                "DVL" -> 134
                "N0X" -> 159
                "N0C" -> 161
                "N0K" -> 163
                "H0C" -> 165
                "N0S" -> 56
                "DAA" -> 172
                "DSA" -> 172
                else -> 94
            }
            val bufR = ColorPalette.colorMap[colorMapProductCode]!!.redValues
            val bufG = ColorPalette.colorMap[colorMapProductCode]!!.greenValues
            val bufB = ColorPalette.colorMap[colorMapProductCode]!!.blueValues
            for (g in 0 until numberOfRadials) {
                val angle = radialStart.float
                binWord.mark()
                var level = binWord.get().toInt() and 0xFF
                binWord.reset()
                var levelCount = 0
                var binStart = binSize
                for (bin in 0 until numberOfRangeBins) {
                    val tmpVal = binWord.get().toInt() and 0xFF
                    if (tmpVal == level) {
                        levelCount += 1
                    } else {
                        NexradDecodeEightBit.rect8bit(
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
                            var b = bufR.get(level)
                            val red = b.toInt() and 0xFF
                            b = bufG.get(level)
                            val green = b.toInt() and 0xFF
                            b = bufB.get(level)
                            val blue = b.toInt() and 0xFF
                            paint.color = Color.rgb(red, green, blue)
                        }
                        path.rewind() // only needed when reusing this path for a new build
                        rBuff.position(0)
                        val x1 = rBuff.float
                        val y1 = rBuff.float
                        with(path) {
                            moveTo(x1, y1)
                            lineTo(rBuff.float, rBuff.float)
                            lineTo(rBuff.float, rBuff.float)
                            lineTo(rBuff.float, rBuff.float)
                            lineTo(x1, y1)
                        }
                        canvas.drawPath(path, paint)
                        level = tmpVal
                        binStart = bin * binSize
                        levelCount = 1
                    }
                }
                if (numberOfRangeBins % 2 != 0) {
                    binWord.position(binWord.position() + 4)
                }
            }
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
    }
}
