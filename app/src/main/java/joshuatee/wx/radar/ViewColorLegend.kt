/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.external.UtilityStringExternal

class ViewColorLegend(context: Context, private val product: String) : View(context) {

    private val myPaint = Paint()
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val h0CLabels = listOf(
        "ND",
        "BI",
        "GC",
        "IC",
        "DS",
        "WS",
        "RA",
        "HR",
        "BD",
        "GR",
        "HA",
        "",
        "",
        "",
        "UK",
        "RF"
    )

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private fun setColorWithBuffers(prodId: Int, index: Int, strokeWidth: Int) {
        myPaint.color = Color.rgb(
            MyApplication.colorMap[prodId]!!.redValues.get(index).toInt() and 0xFF,
            MyApplication.colorMap[prodId]!!.greenValues.get(index).toInt() and 0xFF,
            MyApplication.colorMap[prodId]!!.blueValues.get(index).toInt() and 0xFF
        )
        myPaint.strokeWidth = strokeWidth.toFloat()
    }

    public override fun onDraw(canvas: Canvas) {
        paintText.style = Paint.Style.FILL
        paintText.strokeWidth = 1f
        paintText.textSize = MyApplication.radarShowLegendTextSize.toFloat() //was 30f
        paintText.color = MyApplication.radarShowLegendTextColor
        //if (!MyApplication.blackBg) {
        //    paintText.color = Color.BLACK
        //}
        val startHeight = MyApplication.actionBarHeight.toFloat()
        val width = MyApplication.radarShowLegendWidth.toFloat() //was 50f
        val widthStarting = 0f
        val strokeWidth = 10
        val textFromLegend = 10f
        val heightFudge = 30f
        val screenHeight = MyApplication.dm.heightPixels.toFloat()
        var scaledHeight = (screenHeight - 2 * startHeight) / 256f
        val scaledHeightText = (screenHeight - 2 * startHeight) / (95f + 32f) // 95- -32
        val scaledHeightVel = (screenHeight - 2 * startHeight) / (127 * 2f) // 95- -32
        var unitsDrawn = false
        when (product) {
            "N0Q", "L2REF", "TZL" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(94, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " dBZ"
                (95 downTo 1).forEach {
                    if (it % 10 == 0) {
                        canvas.drawText(
                            it.toString() + units,
                            widthStarting + width + textFromLegend,
                            scaledHeightText * (95 - it) + heightFudge + startHeight,
                            paintText
                        )
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }

            //TODO figure out how to read pal files//
            "N0U", "L2VEL", "TV0" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(99, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " KT"
                val max = 122
                val min = -129
                //val max = 230
                //val min = -230
                //val max = WXGLRadarActivity.velMax
                //val min = WXGLRadarActivity.velMin
                //val stepSize: Int = (max - min) / 25
                val stepSize = 10
                //scaledHeightVel = (screenHeight - 2 * startHeight) / (max - min)
                (max downTo min).forEach {
                    //(122 downTo -130 + 1).forEach {
                    if (it % stepSize == 0) {
                        //canvas.drawText(it.toString() + units, widthStarting + width + textFromLegend, scaledHeightVel * (max - it) + heightFudge + startHeight, paintText) // max was 122
                        canvas.drawText(
                            it.toString() + units,
                            widthStarting + width + textFromLegend,
                            scaledHeightVel * (122 - it) + heightFudge + startHeight,
                            paintText
                        )
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }
            "DVL" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(134, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " kg/m2"
                (70 downTo 1).forEach {
                    if (it % 5 == 0) {
                        canvas.drawText(
                            it.toString() + units,
                            widthStarting + width + textFromLegend,
                            3.64f * scaledHeightVel * (70 - it).toFloat() + heightFudge + startHeight,
                            paintText
                        )
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }
            "EET" -> {
                scaledHeight = (screenHeight - 2 * startHeight) / 70f
                (0..70).forEach {
                    setColorWithBuffers(135, 70 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " K FT"
                (70 downTo 1).forEach {
                    if (it % 5 == 0) {
                        canvas.drawText(
                            it.toString() + units,
                            widthStarting + width + textFromLegend,
                            3.64f * scaledHeightVel * (70 - it).toFloat() + heightFudge + startHeight,
                            paintText
                        )
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }
            "N0X" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(159, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " dB"
                (8 downTo -8 + 1).forEach {
                    canvas.drawText(
                        it.toString() + units,
                        widthStarting + width + textFromLegend,
                        16f * scaledHeightVel * (8 - it).toFloat() + heightFudge + startHeight,
                        paintText
                    )
                    if (!unitsDrawn) {
                        unitsDrawn = true
                        units = ""
                    }
                }
            }
            "N0C" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(161, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " CC"
                (100 downTo -1 step 1).forEach {
                    if (it % 5 == 0) {
                        canvas.drawText(
                            UtilityStringExternal.truncate(
                                (it / 100.0).toString(),
                                4
                            ) + units,
                            widthStarting + width + textFromLegend,
                            3f * scaledHeightVel * (100 - it).toFloat() + heightFudge + startHeight,
                            paintText
                        )
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }
            "N0K" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(163, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " PHAS"
                (10 downTo -3 + 1).forEach {
                    canvas.drawText(
                        it.toString() + units,
                        widthStarting + width + textFromLegend,
                        20f * scaledHeightVel * (10 - it).toFloat() + heightFudge + startHeight,
                        paintText
                    )
                    if (!unitsDrawn) {
                        unitsDrawn = true
                        units = ""
                    }
                }
            }
            "H0C" -> {
                scaledHeight = (screenHeight - 2 * startHeight) / 160f
                (0..159).forEach {
                    setColorWithBuffers(165, 160 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = ""
                (159 downTo -1 + 1).forEach {
                    if (it % 10 == 0) {
                        canvas.drawText(
                            h0CLabels[it / 10] + units,
                            widthStarting + width + textFromLegend,
                            scaledHeight * (159 - it) + startHeight,
                            paintText
                        )
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }
            "DSA", "DAA" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(172, 255 - it, strokeWidth)
                    canvas.drawRect(
                        widthStarting,
                        it * scaledHeight + startHeight,
                        width + widthStarting,
                        it * scaledHeight + scaledHeight + startHeight,
                        myPaint
                    )
                }
                var units = " IN"
                var j = WXGLRadarActivity.dspLegendMax
                while (j > 0) {
                    canvas.drawText(
                        UtilityStringExternal.truncate(j.toString(), 4) + units,
                        widthStarting + width + textFromLegend,
                        255f / WXGLRadarActivity.dspLegendMax * scaledHeightVel * (WXGLRadarActivity.dspLegendMax - j) + heightFudge + startHeight,
                        paintText
                    )
                    if (!unitsDrawn) {
                        unitsDrawn = true
                        units = ""
                    }
                    j -= WXGLRadarActivity.dspLegendMax / 16f
                }
            }
            else -> {
            }
        }
    }
}



