/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.external.UtilityStringExternal
import android.content.ContentValues.TAG
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import android.view.WindowManager
import android.view.Display
import joshuatee.wx.UIPreferences


// thanks for skeletal framework
// https://newcircle.com/s/post/1036/android_2d_graphics_example

//TODO add a diallog if color bar gets tapped and remove text


class ViewColorLegendBottom(context: Context, private val product: String) : View(context) {

    var TAG = "ViewColorLegendBottom"
    private val myPaint = Paint()
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val h0CLabels = listOf("ND", "BI", "GC", "IC", "DS", "WS", "RA", "HR", "BD", "GR", "HA", "", "", "", "UK", "RF")

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private fun setColorWithBuffers(prodId: Int, index: Int, strokeWidth: Int) {
        myPaint.color = Color.rgb(MyApplication.colorMap[prodId]!!.redValues.get(index).toInt() and 0xFF,
                MyApplication.colorMap[prodId]!!.greenValues.get(index).toInt() and 0xFF,
                MyApplication.colorMap[prodId]!!.blueValues.get(index).toInt() and 0xFF)
        myPaint.strokeWidth = strokeWidth.toFloat()
    }

    public override fun onDraw(canvas: Canvas) {

        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        var SCREEN_HEIGHT = display.height
        var SCREEN_WIDTH = display.width
        //Log.i(TAG, "Height: " + SCREEN_HEIGHT + ", Width: " + SCREEN_WIDTH)

        paintText.style = Paint.Style.FILL
        paintText.strokeWidth = 1f
        paintText.textSize = 30f
        paintText.color = Color.WHITE
        if (!MyApplication.blackBg) {
            paintText.color = Color.BLACK
        }
        val startHeight = MyApplication.actionBarHeight
        var startWidth = 0
        val width = 50f
        val widthStarting = 0f
        val strokeWidth = 10
        val textFromLegend = 10f
        val heightFudge = 30f
        val screenHeight = MyApplication.dm.heightPixels.toFloat()
        val screenWidth = SCREEN_WIDTH
        var scaledHeight = (screenHeight - 2 * startHeight) / 256f
        var scaledWidth = (screenWidth - 2 * startWidth) / 256f
        //var scaledWidth = (screenWidth) / 256f
        //text
        val scaledHeightText = (screenHeight - 2 * startHeight) / (95f + 32f) // 95- -32
        val scaledWidthText = (screenWidth - 2 * startWidth) / (95f + 32f) // 95- -32
        val scaledHeightVel = (screenHeight - 2 * startHeight) / (127 * 2f) // 95- -32
        val scaledWidthVel = (screenWidth - 2 * startWidth) / (127 * 2f) // 95- -32
        var unitsDrawn = false

        var barheight: Float = 343f
        var textheight: Float = 350f

        //drawRect(float left, float top, float right, float bottom, Paint paint)

        when (product) {
            "N0Q", "L2REF", "TZL" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(94, 255 - it, strokeWidth)

                    //canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)

                    //canvas.drawRect(it * scaledWidth + startWidth, SCREEN_HEIGHT.toFloat(), it * SCREEN_WIDTH.toFloat() / 2, SCREEN_HEIGHT.toFloat() - 375f, myPaint)

                    //canvas.drawRect(it * SCREEN_WIDTH.toFloat() / 2, SCREEN_HEIGHT.toFloat(), it * scaledWidth + startWidth, SCREEN_HEIGHT.toFloat() - 500f, myPaint)


                    if (UIPreferences.radarToolbarTransparent) {
                        barheight = 130f
                        textheight = 140f
                    } else {
                        barheight = 340f
                        textheight = 350f
                    }

                    //TODO TESTING
                    canvas.drawRect(it * scaledWidth + startWidth, SCREEN_HEIGHT.toFloat(), it * SCREEN_WIDTH.toFloat() / 2, SCREEN_HEIGHT.toFloat() - barheight, myPaint)


                    /*
                    canvas.save()
                    canvas.rotate(30f)
                    //canvas.restore()
                    //Bottom
                    canvas.drawRect((it * scaledWidth + startWidth) + SCREEN_HEIGHT.toFloat(), SCREEN_HEIGHT.toFloat(), it * SCREEN_WIDTH.toFloat() / 2, SCREEN_HEIGHT.toFloat() - 1500f, myPaint)
                    canvas.restore()
                    */

                    /*
                    try {
                        Log.i(TAG, "left: " + it * scaledWidth + startWidth)
                        Log.i(TAG, "right: " + it * SCREEN_WIDTH.toFloat() / 2)
                    } catch (e: Exception) {
                        Log.i(TAG, "(e: Exception)")
                    }



                    try {
                        Log.i(TAG, "it: " + it)
                    } catch (e: Exception) {
                        Log.i(TAG, "(e: Exception)")
                    }

                    */



                }
                var units = " dBZ"
                (95 downTo 1).forEach {
                    if (it % 10 == 0) {
                        //canvas.drawText(it.toString() + units, scaledWidthText * (95 - it), SCREEN_HEIGHT.toFloat() + 350f, paintText)
                        canvas.drawText(it.toString() + units, scaledWidthText * (95 - it), SCREEN_HEIGHT.toFloat() - textheight, paintText)
                        if (!unitsDrawn) {
                            unitsDrawn = true
                            units = ""
                        }
                    }
                }
            }
            "N0U", "L2VEL", "TV0" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(99, 255 - it, strokeWidth)
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " KT"
                (122 downTo -130 + 1).forEach {
                    if (it % 10 == 0) {
                        canvas.drawText(it.toString() + units, widthStarting + width + textFromLegend, scaledHeightVel * (122 - it) + heightFudge + startHeight, paintText)
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
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " kg/m2"
                (70 downTo 1).forEach {
                    if (it % 5 == 0) {
                        canvas.drawText(it.toString() + units, widthStarting + width + textFromLegend, 3.64f * scaledHeightVel * (70 - it).toFloat() + heightFudge + startHeight, paintText)
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
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " K FT"
                (70 downTo 1).forEach {
                    if (it % 5 == 0) {
                        canvas.drawText(it.toString() + units, widthStarting + width + textFromLegend, 3.64f * scaledHeightVel * (70 - it).toFloat() + heightFudge + startHeight, paintText)
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
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " dB"
                (8 downTo -8 + 1).forEach {
                    canvas.drawText(it.toString() + units, widthStarting + width + textFromLegend, 16f * scaledHeightVel * (8 - it).toFloat() + heightFudge + startHeight, paintText)
                    if (!unitsDrawn) {
                        unitsDrawn = true
                        units = ""
                    }
                }
            }
            "N0C" -> {
                (0 until 256).forEach {
                    setColorWithBuffers(161, 255 - it, strokeWidth)
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight,
                            width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " CC"
                (100 downTo -1 step 1).forEach {
                    if (it % 5 == 0) {
                        canvas.drawText(UtilityStringExternal.truncate((it / 100.0).toString(), 4) + units, widthStarting + width + textFromLegend, 3f * scaledHeightVel * (100 - it).toFloat() + heightFudge + startHeight, paintText)
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
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " PHAS"
                (10 downTo -3 + 1).forEach {
                    canvas.drawText(it.toString() + units, widthStarting + width + textFromLegend, 20f * scaledHeightVel * (10 - it).toFloat() + heightFudge + startHeight, paintText)
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
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = ""
                (159 downTo -1 + 1).forEach {
                    if (it % 10 == 0) {
                        canvas.drawText(h0CLabels[it / 10] + units, widthStarting + width + textFromLegend, scaledHeight * (159 - it) + startHeight, paintText)
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
                    canvas.drawRect(widthStarting, it * scaledHeight + startHeight, width + widthStarting, it * scaledHeight + scaledHeight + startHeight, myPaint)
                }
                var units = " IN"
                var j = WXGLRadarActivity.dspLegendMax
                while (j > 0) {
                    canvas.drawText(UtilityStringExternal.truncate(j.toString(), 4) + units, widthStarting + width + textFromLegend,
                            255f / WXGLRadarActivity.dspLegendMax * scaledHeightVel * (WXGLRadarActivity.dspLegendMax - j) + heightFudge + startHeight, paintText)
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



