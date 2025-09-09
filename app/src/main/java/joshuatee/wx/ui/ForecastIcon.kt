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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import joshuatee.wx.R
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityNwsIcon
import androidx.core.graphics.createBitmap

class ForecastIcon {

    private var bitmap: Bitmap
    private var canvas: Canvas
    private val paint = ObjectPaint()
    private val paintStripe = PaintStripe()
    private val nullImage = R.drawable.white_box

    constructor(context: Context, weatherCondition: String) {
        bitmap = createBitmap(DIMENSIONS_INT, DIMENSIONS_INT)
        canvas = Canvas(bitmap)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fileName = UtilityNwsIcon.iconMap["$weatherCondition.png"] ?: nullImage
        val bitmap1 = UtilityImg.loadBitmap(context, fileName, false)
        val bitmap2 = Bitmap.createBitmap(bitmap1, 0, 0, DIMENSIONS_INT, DIMENSIONS_INT)
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    constructor(context: Context, leftWeatherCondition: String, rightWeatherCondition: String) {
        val leftCropA = if (leftWeatherCondition.contains("fg")) {
            MIDDLE_POINT_INT
        } else {
            4
        }
        val leftCropB = if (rightWeatherCondition.contains("fg")) {
            MIDDLE_POINT_INT
        } else {
            4
        }
        bitmap = createBitmap(DIMENSIONS_INT, DIMENSIONS_INT)
        canvas = Canvas(bitmap)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fileNameLeft = UtilityNwsIcon.iconMap["$leftWeatherCondition.png"] ?: nullImage
        val fileNameRight = UtilityNwsIcon.iconMap["$rightWeatherCondition.png"] ?: nullImage
        val bitmap1 = UtilityImg.loadBitmap(context, fileNameLeft, false)
        val bitmap2 = Bitmap.createBitmap(bitmap1, leftCropA, 0, HALF_WIDTH_INT, DIMENSIONS_INT)
        canvas.drawBitmap(bitmap2, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val bitmap3 = UtilityImg.loadBitmap(context, fileNameRight, false)
        val bitmap4 = Bitmap.createBitmap(bitmap3, leftCropB, 0, HALF_WIDTH_INT, DIMENSIONS_INT)
        canvas.drawBitmap(bitmap4, MIDDLE_POINT, 0.0f, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    fun drawLeftText(leftNumber: String) {
        val xTextLeft = 2.0f
        if (leftNumber != "" && leftNumber != "0") {
            canvas.drawRect(
                0.0f,
                DIMENSIONS - NUM_HEIGHT,
                HALF_WIDTH,
                DIMENSIONS,
                paintStripe.get()
            )
            canvas.drawText("$leftNumber%", xTextLeft, Y_TEXT, paint.get())
        }
    }

    fun drawRightText(rightNumber: String) {
        val xText = if (rightNumber == "100") {
            50.0f
        } else {
            58.0f
        }
        if (rightNumber != "" && rightNumber != "0") {
            canvas.drawRect(
                MIDDLE_POINT,
                DIMENSIONS - NUM_HEIGHT,
                DIMENSIONS,
                DIMENSIONS,
                paintStripe.get()
            )
            canvas.drawText("$rightNumber%", xText, Y_TEXT, paint.get())
        }
    }

    fun drawSingleText(number: String) {
        val yText = 84.0f
        val xText = if (number == "100") {
            50.0f
        } else {
            58.0f
        }
        if (number != "" && number != "0") {
            canvas.drawRect(
                0.0f,
                DIMENSIONS - NUM_HEIGHT,
                DIMENSIONS,
                DIMENSIONS,
                paintStripe.get()
            )
            canvas.drawText("$number%", xText, yText, paint.get())
        }
    }

    fun get() = bitmap

    companion object {

        private const val DIMENSIONS = 86.0f
        private const val DIMENSIONS_INT = 86
        private const val NUM_HEIGHT = 15.0f
        private const val HALF_WIDTH = 41.0f
        private const val HALF_WIDTH_INT = 41
        private const val MIDDLE_POINT = 45.0f
        private const val MIDDLE_POINT_INT = 45
        private const val Y_TEXT = 84.0f

        fun blankBitmap(): Bitmap = createBitmap(10, 10)
    }
}


