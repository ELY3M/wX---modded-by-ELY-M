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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.ProjectionNumbers

object UtilityCanvasWindbarbs {

    fun draw(context: Context, projectionType: ProjectionType, bitmap: Bitmap, radarSite: String, isGust: Boolean, index: Int) {
        val textSize = 22
        UtilityMetar.getStateMetarArrayForWXOGL(context, radarSite, 5)
        val wbCircleXArr = UtilityMetar.metarDataList[index].x
        val wbCircleYArr = UtilityMetar.metarDataList[index].y
        var mercator = false
        if (projectionType !== ProjectionType.NWS_MOSAIC) {
            mercator = true
        }
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 2f
        if (projectionType === ProjectionType.WX_RENDER || projectionType === ProjectionType.WX_RENDER_48) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (isGust) {
            paint.color = Color.RED
        } else {
            paint.color = RadarPreferences.colorObsWindbarbs
        }
        paint.textSize = textSize.toFloat()
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        var pixXInit: Double
        var pixYInit: Double
        val stormList = mutableListOf<Double>()
        val arrWb = if (!isGust) {
            UtilityMetar.metarDataList[index].obsArrWb
        } else {
            UtilityMetar.metarDataList[index].obsArrWbGust
        }
        try {
            val degreeShift = 180.00
            val arrowLength = 2.5
            val arrowSpacing = 3.0
            val barbLengthScaleFactor = 0.4
            val arrowBend = 60.0
            val nmScaleFactor = -1852.0
            val barbLength = 15.0
            val barbOffset = 0.0
            arrWb.forEach { s ->
                val metarArr = s.split(":").dropLastWhile { it.isEmpty() }
                var angle = 0
                var length = 0
                var locXDbl = 0.0
                var locYDbl = 0.0
                if (metarArr.size > 3) {
                    locXDbl = metarArr[0].toDoubleOrNull() ?: 0.0
                    locYDbl = metarArr[1].toDoubleOrNull() ?: 0.0
                    angle = metarArr[2].toIntOrNull() ?: 0
                    length = metarArr[3].toIntOrNull() ?: 0
                }
                if (length > 4) {
                    val degree2 = angle.toDouble()
                    val startLength = 0.0
                    var start = ExternalGlobalCoordinates(locXDbl, locYDbl)
                    var ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, 0.0, startLength)
                    stormList += UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, projectionNumbers).toList()
                    start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree2 + degreeShift, barbLength * nmScaleFactor * barbLengthScaleFactor)
                    val end = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                    stormList += UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, projectionNumbers).toList()
                    val barbCount = length / 10
                    var halfBarb = false
                    var oneHalfBarb = false
                    if (length - barbCount * 10 > 4 && length > 10 || length in 5..9) {
                        halfBarb = true
                    }
                    if (length in 5..9) oneHalfBarb = true
                    (0 until barbCount).forEach { j ->
                        ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(end, degree2, barbOffset + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor)
                        stormList += WXGLNexradLevel3Common.drawLine(ec, projectionNumbers, degree2 - arrowBend * 2.0, startLength + arrowLength * nmScaleFactor)
                    }
                    var halfBarbOffsetFudge = 0.0
                    if (oneHalfBarb) halfBarbOffsetFudge = nmScaleFactor * 1.0
                    if (halfBarb) {
                        ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                                end,
                                degree2,
                                barbOffset + halfBarbOffsetFudge + startLength + (barbCount - 1).toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor
                        )
                        stormList += WXGLNexradLevel3Common.drawLine(ec, projectionNumbers, degree2 - arrowBend * 2.0, startLength + arrowLength / 2.0 * nmScaleFactor)
                    }
                } // if length greater then 4
            } // loop over wind barbs
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        val stormListArr = FloatArray(stormList.size)
        stormList.indices.forEach { stormListArr[it] = stormList[it].toFloat() }
        canvas.drawLines(stormListArr, paint)
        // draw aviation circle on top
        wbCircleXArr.indices.forEach { k ->
            if (UtilityMetar.metarDataList[index].obsArrAviationColor.size > k) {
                paint.color = UtilityMetar.metarDataList[index].obsArrAviationColor[k]
                val list = if (mercator) {
                    UtilityCanvasProjection.computeMercatorNumbers(wbCircleXArr[k], wbCircleYArr[k], projectionNumbers).toList()
                } else {
                    UtilityCanvasProjection.compute4326Numbers(wbCircleXArr[k], wbCircleYArr[k], projectionNumbers).toList()
                }
                pixXInit = list[0]
                pixYInit = list[1]
                canvas.drawCircle(pixXInit.toFloat(), pixYInit.toFloat(), 5f, paint)
            }
        }
    }
}
