/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package joshuatee.wx.radar

//The following has chunks of code from Level2VolumeScan.java so using the license for that file
//This file is no longer in use but serves as a reference since the code used by OGL has been stripped of uneeded parts.

import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Paint.Style
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMath

// The code in this file is used exclusively by the code path that generates bitmaps ( ie not OpenGL radar )

internal object CanvasLevel2 {

    private const val DECOMP_FN = "l2.decomp"

    // FIXME needs refactor - all utilNexrad*
    fun decodeAndPlot(context: Context, bitmap: Bitmap, prod: String) {
        val canvas = Canvas(bitmap)
        val productCode = if (prod == "L2VEL") {
            154
        } else {
            153
        }
        val numberOfRadials = 720
        val numberOfRangeBins = 916
        // 1832 vel 1192 vel
        val zeroColor = if (Utility.readPref(context, "NWS_RADAR_BG_BLACK", "") != "true") {
            ContextCompat.getColor(context, R.color.white)
        } else {
            ContextCompat.getColor(context, R.color.black)
        }
        val radialStartAngle = ByteBuffer.allocateDirect(720 * 4)
        radialStartAngle.order(ByteOrder.nativeOrder())
        radialStartAngle.position(0)
        val binWord = ByteBuffer.allocateDirect(720 * numberOfRangeBins)
        binWord.order(ByteOrder.nativeOrder())
        binWord.position(0)
        val days = ByteBuffer.allocateDirect(2)
        days.order(ByteOrder.nativeOrder())
        days.position(0)
        val msecs = ByteBuffer.allocateDirect(4)
        msecs.order(ByteOrder.nativeOrder())
        msecs.position(0)
        Level2.decode(context, DECOMP_FN, binWord, radialStartAngle, productCode, days, msecs)
        val centerX = 500
        val centerY = 500
        val binSize = NexradUtil.getBinSize(productCode)
        var xy1: FloatArray
        var xy2: FloatArray
        var xy3: FloatArray
        var xy4: FloatArray
        val paint = Paint()
        paint.style = Style.FILL
        val path = Path()
        var g = 0
        var angle: Float
        var angleV: Float
        var level: Int
        var levelCount: Int
        var binStart: Float
        var bin: Int
        val cR: ByteBuffer
        val cG: ByteBuffer
        val cB: ByteBuffer
        val colorMapProductCode: Int
        if (productCode == 153) {
            colorMapProductCode = 94
            cR = ColorPalette.colorMap[colorMapProductCode]!!.redValues
            cG = ColorPalette.colorMap[colorMapProductCode]!!.greenValues
            cB = ColorPalette.colorMap[colorMapProductCode]!!.blueValues
        } else {
            colorMapProductCode = 99
            cR = ColorPalette.colorMap[colorMapProductCode]!!.redValues
            cG = ColorPalette.colorMap[colorMapProductCode]!!.greenValues
            cB = ColorPalette.colorMap[colorMapProductCode]!!.blueValues
        }
        var tmpVal: Int
        while (g < numberOfRadials) {
            angle = radialStartAngle.float
            angleV = 0.50f
            binWord.mark()
            level = binWord.get().toInt() and 0xFF
            binWord.reset()
            levelCount = 0
            binStart = binSize
            bin = 0
            while (bin < numberOfRangeBins) {
                tmpVal = binWord.get().toInt() and 0xFF
                if (tmpVal == level) {
                    levelCount += 1
                } else {
                    xy1 = UtilityMath.toRect(binStart, angle)
                    xy2 = UtilityMath.toRect(binStart + binSize * levelCount, angle)
                    xy3 = UtilityMath.toRect(binStart + binSize * levelCount, angle - angleV)
                    xy4 = UtilityMath.toRect(binStart, angle - angleV)
                    xy1[0] += centerX.toFloat()
                    xy2[0] += centerX.toFloat()
                    xy3[0] += centerX.toFloat()
                    xy4[0] += centerX.toFloat()
                    xy1[1] = (xy1[1] - centerY) * -1
                    xy2[1] = (xy2[1] - centerY) * -1
                    xy3[1] = (xy3[1] - centerY) * -1
                    xy4[1] = (xy4[1] - centerY) * -1
                    if (level == 0)
                        paint.color = zeroColor
                    else
                        paint.color = Color.rgb(
                                cR.get(level).toInt() and 0xFF,
                                cG.get(level).toInt() and 0xFF,
                                cB.get(level).toInt() and 0xFF
                        )
                    with(path) {
                        rewind() // only needed when reusing this path for a new build
                        moveTo(xy1[0], xy1[1])
                        lineTo(xy2[0], xy2[1])
                        lineTo(xy3[0], xy3[1])
                        lineTo(xy4[0], xy4[1])
                        lineTo(xy1[0], xy1[1])
                    }
                    canvas.drawPath(path, paint)
                    level = tmpVal
                    binStart = bin * binSize
                    levelCount = 1
                }
                bin += 1
            }
            g += 1
        }
    }
}
