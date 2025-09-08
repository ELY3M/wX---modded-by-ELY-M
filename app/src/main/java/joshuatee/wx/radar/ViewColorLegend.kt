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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import joshuatee.wx.MyApplication
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.UtilityLog

@SuppressLint("ViewConstructor")
class ViewColorLegend(context: Context, private val product: String) : View(context) {

    private val myPaint = Paint()
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    //elys mod
    private val width = RadarPreferences.showLegendWidth.toFloat() //was 50f
    private val startHeight = UIPreferences.actionBarHeight.toFloat()
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

    private fun setColorWithBuffers(prodId: Int, index: Int) {
        try {
            myPaint.color = Color.rgb(
                ColorPalette.colorMap[prodId]!!.redValues.get(index).toInt() and 0xFF,
                ColorPalette.colorMap[prodId]!!.greenValues.get(index).toInt() and 0xFF,
                ColorPalette.colorMap[prodId]!!.blueValues.get(index).toInt() and 0xFF
            )
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        myPaint.strokeWidth = 10.0f
    }

    private fun drawRect(canvas: Canvas, index: Int, scaledHeight: Float) {
        canvas.drawRect(
            0.0f,
            index * scaledHeight + startHeight,
            width,
            index * scaledHeight + scaledHeight + startHeight, myPaint
        )
    }

    private fun drawText(canvas: Canvas, label: String, y: Float) {
        val heightFudge = 30.0f
        val textFromLegend = 10.0f
        canvas.drawText(
            label,
            width + textFromLegend,
            y + startHeight + heightFudge,
            paintText
        )
    }

    public override fun onDraw(canvas: Canvas) {
        with(paintText) {
            style = Paint.Style.FILL
            strokeWidth = 1.0f
	    //elys mod
            textSize = RadarPreferences.showLegendTextSize.toFloat() //was 30f
            color = RadarPreferences.showLegendTextColor
        }
        val screenHeight = MyApplication.dm.heightPixels.toFloat()
        var scaledHeight = (screenHeight - 2.0f * startHeight) / 256.0f
        val scaledHeightText = (screenHeight - 2.0f * startHeight) / (95.0f + 32.0f)
        val scaledHeightVel = (screenHeight - 2.0f * startHeight) / (127.0f * 2.0f)
        when (product) {
            "N0Q", "L2REF", "TZL", "TZ0", "N1Q", "N2Q", "N3Q" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(94, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " dBZ"
                (95 downTo 1).forEach {
                    if (it % 10 == 0) {
                        drawText(
                            canvas,
                            it.toString() + units,
                            scaledHeightText * (95 - it)
                        )
                        units = ""
                    }
                }
            }

            "N0U", "L2VEL", "TV0", "N1U", "N2U", "N3U" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(99, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " KT"
                (122 downTo -129).forEach {
                    if (it % 10 == 0) {
                        drawText(
                            canvas,
                            it.toString() + units,
                            scaledHeightVel * (122 - it)
                        )
                        units = ""
                    }
                }
            }

            "DVL" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(134, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " kg/m2"
                (70 downTo 1).forEach {
                    if (it % 5 == 0) {
                        drawText(
                            canvas,
                            it.toString() + units,
                            3.64f * scaledHeightVel * (70 - it)
                        )
                        units = ""
                    }
                }
            }

            "EET" -> {
                scaledHeight = (screenHeight - 2 * startHeight) / 70.0f
                (0..70).forEach {
                    setColorWithBuffers(135, 70 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " K FT"
                (70 downTo 1).forEach {
                    if (it % 5 == 0) {
                        drawText(
                            canvas,
                            it.toString() + units,
                            3.64f * scaledHeightVel * (70 - it)
                        )
                        units = ""
                    }
                }
            }

            "N0X", "N1X", "N2X", "N3X" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(159, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " dB"
                (8 downTo -8 + 1).forEach {
                    drawText(
                        canvas,
                        it.toString() + units,
                        16.0f * scaledHeightVel * (8 - it)
                    )
                    units = ""
                }
            }

            "N0C", "N1C", "N2C", "N3C" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(161, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " CC"
                (100 downTo -1 step 1).forEach {
                    if (it % 5 == 0) {
                        drawText(
                            canvas,
                            (it / 100.0).toString().take(4) + units,
                            3.0f * scaledHeightVel * (100 - it) + heightFudge
                        )
                        units = ""
                    }
                }
            }

            "N0K", "N1K", "N2K", "N3K" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(163, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " PHAS"
                (10 downTo -3 + 1).forEach {
                    drawText(
                        canvas,
                        it.toString() + units,
                        20.0f * scaledHeightVel * (10 - it)
                    )
                    units = ""
                }
            }

            "H0C" -> {
                scaledHeight = (screenHeight - 2 * startHeight) / 160.0f
                (0..159).forEach {
                    setColorWithBuffers(165, 160 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                (159 downTo -1 + 1).forEach {
                    if (it % 10 == 0) {
                        drawText(
                            canvas,
                            h0CLabels[it / 10],
                            scaledHeight * (159 - it) - 50
                        )
                    }
                }
            }

            "DSA", "DAA" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(172, 255 - it)
                    drawRect(canvas, it, scaledHeight)
                }
                var units = " IN"
                var j = WXGLRadarActivity.dspLegendMax
                while (j > 0) {
                    drawText(
                        canvas, j.toString().take(4) + units,
                        255.0f / WXGLRadarActivity.dspLegendMax * scaledHeightVel * (WXGLRadarActivity.dspLegendMax - j)
                    )
                    units = ""
                    j -= WXGLRadarActivity.dspLegendMax / 16.0f
                }
            }

            else -> {}
        }
    }
}
