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
import joshuatee.wx.util.To

object CanvasWindbarbs {

    fun draw(context: Context, projectionType: ProjectionType, bitmap: Bitmap, radarSite: String, isGust: Boolean, index: Int) {
        val paintTextSize = 22
        Metar.get(context, radarSite, index)
        val wbCircleXArr = Metar.data[index].x
        val wbCircleYArr = Metar.data[index].y
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        with (paint) {
            style = Style.FILL
            strokeWidth = 2.0f
            color = if (isGust) Color.RED else RadarPreferences.colorObsWindbarbs
            textSize = paintTextSize.toFloat()
        }
        canvas.translate(CanvasMain.xOffset, CanvasMain.yOffset)
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val stormList = mutableListOf<Double>()
        val arrWb = if (!isGust) Metar.data[index].obsArrWb else Metar.data[index].obsArrWbGust
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
                    locXDbl = To.double(metarArr[0])
                    locYDbl = To.double(metarArr[1])
                    angle = To.int(metarArr[2])
                    length = To.int(metarArr[3])
                }
                if (length > 4) {
                    val degree2 = angle.toDouble()
                    val startLength = 0.0
                    var start = ExternalGlobalCoordinates(locXDbl, locYDbl)
                    var ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, 0.0, startLength)
                    stormList += Projection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, projectionNumbers).toList()
                    start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree2 + degreeShift, barbLength * nmScaleFactor * barbLengthScaleFactor)
                    val end = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                    stormList += Projection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, projectionNumbers).toList()
                    val barbCount = length / 10
                    var halfBarb = false
                    var oneHalfBarb = false
                    if (length - barbCount * 10 > 4 && length > 10 || length in 5..9) {
                        halfBarb = true
                    }
                    if (length in 5..9) {
                        oneHalfBarb = true
                    }
                    (0 until barbCount).forEach { j ->
                        ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(end, degree2, barbOffset + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor)
                        stormList += NexradLevel3Common.drawLine(ec, projectionNumbers, degree2 - arrowBend * 2.0, startLength + arrowLength * nmScaleFactor)
                    }
                    val halfBarbOffsetFudge = if (oneHalfBarb) nmScaleFactor * 1.0 else 0.0
                    if (halfBarb) {
                        ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                                end,
                                degree2,
                                barbOffset + halfBarbOffsetFudge + startLength + (barbCount - 1).toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor
                        )
                        stormList += NexradLevel3Common.drawLine(ec, projectionNumbers, degree2 - arrowBend * 2.0, startLength + arrowLength / 2.0 * nmScaleFactor)
                    }
                } // if length greater then 4
            } // loop over wind barbs
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        val stormListArr = stormList.map { it.toFloat() }.toFloatArray()
        canvas.drawLines(stormListArr, paint)
        // draw aviation circle on top
        wbCircleXArr.indices.forEach { k ->
            if (Metar.data[index].obsArrAviationColor.size > k) {
                paint.color = Metar.data[index].obsArrAviationColor[k]
                val list = Projection.computeMercatorNumbers(wbCircleXArr[k].toFloat(), wbCircleYArr[k].toFloat(), projectionNumbers)
                val pixXInit = list[0]
                val pixYInit = list[1]
                canvas.drawCircle(pixXInit, pixYInit, 5.0f, paint)
            }
        }
    }
}
