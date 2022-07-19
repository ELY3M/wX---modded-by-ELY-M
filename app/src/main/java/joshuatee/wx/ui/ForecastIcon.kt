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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import joshuatee.wx.R
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityNwsIcon

class ForecastIcon {

    var bitmap: Bitmap
    var canvas: Canvas
    var context: Context
    val paint = ObjectPaint()
    val paintStripe = ObjectPaintStripe()
    val nullImage = R.drawable.white_box

    constructor(context: Context, weatherCondition: String) {
        this.context = context
        bitmap = Bitmap.createBitmap(dimensions, dimensions, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fileName = UtilityNwsIcon.iconMap["$weatherCondition.png"] ?: nullImage
        val bitmap1 = UtilityImg.loadBitmap(context, fileName, false)
        val bitmap2 = Bitmap.createBitmap(bitmap1, 0, 0, dimensions, dimensions)
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    constructor(context: Context, leftWeatherCondition: String, rightWeatherCondition: String) {
        this.context = context
        val leftCropA = if (leftWeatherCondition.contains("fg")) {
            middlePoint
        } else {
            4
        }
        val leftCropB = if (rightWeatherCondition.contains("fg")) {
            middlePoint
        } else {
            4
        }
        bitmap = Bitmap.createBitmap(dimensions, dimensions, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        canvas.drawColor(UtilityTheme.primaryColorFromSelectedTheme)
        val fileNameLeft = UtilityNwsIcon.iconMap["$leftWeatherCondition.png"] ?: nullImage
        val fileNameRight = UtilityNwsIcon.iconMap["$rightWeatherCondition.png"] ?: nullImage
        val bitmap1 = UtilityImg.loadBitmap(context, fileNameLeft, false)
        val bitmap2 = Bitmap.createBitmap(bitmap1, leftCropA, 0, halfWidth, dimensions)
        canvas.drawBitmap(bitmap2, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        val bitmap3 = UtilityImg.loadBitmap(context, fileNameRight, false)
        val bitmap4 = Bitmap.createBitmap(bitmap3, leftCropB, 0, halfWidth, dimensions)
        canvas.drawBitmap(bitmap4, middlePoint.toFloat(), 0.0f, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    fun drawLeftText(leftNumber: String) {
        val xTextLeft = 2
        if (leftNumber != "" && leftNumber != "0") {
            canvas.drawRect(0.0f, (dimensions - numHeight).toFloat(), halfWidth.toFloat(), dimensions.toFloat(), paintStripe.paint)
            canvas.drawText("$leftNumber%", xTextLeft.toFloat(), yText.toFloat(), paint.paint)
        }
    }

    fun drawRightText(rightNumber: String) {
        val xText = if (rightNumber == "100") {
            50
        } else {
            58
        }
        if (rightNumber != "" && rightNumber != "0") {
            canvas.drawRect(middlePoint.toFloat(), (dimensions - numHeight).toFloat(), dimensions.toFloat(), dimensions.toFloat(), paintStripe.paint)
            canvas.drawText("$rightNumber%", xText.toFloat(), yText.toFloat(), paint.paint)
        }
    }

    fun drawSingleText(number: String) {
        val yText = 84
        val xText = if (number == "100") {
            50
        } else {
            58
        }
        if (number != "" && number != "0") {
            canvas.drawRect(0.0f, (dimensions - numHeight).toFloat(), dimensions.toFloat(), dimensions.toFloat(), paintStripe.paint)
            canvas.drawText("$number%", xText.toFloat(), yText.toFloat(), paint.paint)
        }
    }

    fun get(): Bitmap {
        return bitmap
    }

    companion object {

        private const val dimensions = 86
        private const val numHeight = 15
        private const val halfWidth = 41
        private const val middlePoint = 45
        private const val yText = 84

        fun blankBitmap() : Bitmap {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
    }
}


