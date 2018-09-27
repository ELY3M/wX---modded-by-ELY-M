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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import joshuatee.wx.MyApplication

import joshuatee.wx.external.ExternalEllipsoid
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.ProjectionNumbers

object UtilityCanvasWindbarbs {

    fun drawWindbarbs(context: Context, provider: ProjectionType, bm1: Bitmap, rid1: String, isGust: Boolean) {
        val textSize = 22
        UtilityMetar.getStateMetarArrayForWXOGL(context, rid1)
        val wbCircleXArr = UtilityMetar.x
        val wbCircleYArr = UtilityMetar.y
        var mercato = false
        if (provider !== ProjectionType.NWS_MOSAIC) {
            mercato = true
        }
        val canvas = Canvas(bm1)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 2f
        if (provider === ProjectionType.WX_RENDER || provider === ProjectionType.WX_RENDER_48) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (isGust) {
            paint.color = Color.RED
        } else {
            paint.color = MyApplication.radarColorObsWindbarbs
        }
        paint.textSize = textSize.toFloat()
        val pn = ProjectionNumbers(context, rid1, provider)
        var pixXInit: Double
        var pixYInit: Double
        var tmpCoords: DoubleArray
        val stormList = mutableListOf<Double>()
        val stormListArr: FloatArray
        val arrWb = if (!isGust) {
            UtilityMetar.obsArrWb
        } else {
            UtilityMetar.obsArrWbGust
        }
        try {
            var locXDbl = 0.0
            var locYDbl = 0.0
            var degree: Double
            var nm: Double
            var degree2: Double
            val bearing = DoubleArray(2)
            var start: ExternalGlobalCoordinates
            var end: ExternalGlobalCoordinates
            var ec: ExternalGlobalCoordinates
            val degreeShift = 180.00
            val arrowLength = 2.5
            val arrowSpacing = 3.0
            val barbLengthScaleFactor = 0.4
            val arrowBend = 60.0
            val nmScaleFactor = -1852.0
            var startLength: Double
            val barbLength = 15.0
            val barbOffset = 0.0
            arrWb.forEach { s ->
                val ecc = ExternalGeodeticCalculator()
                val metarArr = s.split(":").dropLastWhile { it.isEmpty() }
                var angle = 0
                var length = 0
                if (metarArr.size > 3) {
                    locXDbl = metarArr[0].toDoubleOrNull() ?: 0.0
                    locYDbl = metarArr[1].toDoubleOrNull() ?: 0.0
                    angle = metarArr[2].toIntOrNull() ?: 0
                    length = metarArr[3].toIntOrNull() ?: 0
                }
                if (length > 4) {
                    degree = 0.0
                    nm = 0.0
                    degree2 = angle.toDouble()
                    startLength = nm * nmScaleFactor
                    start = ExternalGlobalCoordinates(locXDbl, locYDbl)
                    ec = ecc.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, start, degree, startLength, bearing)
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
                    stormList.add(tmpCoords[0])
                    stormList.add(tmpCoords[1])
                    start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                    ec = ecc.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, start, degree2 + degreeShift, barbLength * nmScaleFactor * barbLengthScaleFactor, bearing)
                    end = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
                    stormList.add(tmpCoords[0])
                    stormList.add(tmpCoords[1])
                    val barbCount = length / 10
                    var halfBarb = false
                    var oneHalfBarb = false
                    if (length - barbCount * 10 > 4 && length > 10 || length in 5..9) {
                        halfBarb = true
                    }
                    if (length in 5..9) {
                        oneHalfBarb = true
                    }
                    var j = 0
                    while (j < barbCount) {
                        ec = ecc.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, end, degree2, barbOffset + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor, bearing)
                        tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
                        stormList.add(tmpCoords[0])
                        stormList.add(tmpCoords[1])

                        start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                        ec = ecc.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, start, degree2 - arrowBend * 2.0, startLength + arrowLength * nmScaleFactor, bearing)
                        tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
                        stormList.add(tmpCoords[0])
                        stormList.add(tmpCoords[1])
                        j += 1
                    }
                    var halfBarbOffsetFudge = 0.0
                    if (oneHalfBarb) {
                        halfBarbOffsetFudge = nmScaleFactor * 1.0
                    }
                    if (halfBarb) {
                        ec = ecc.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, end, degree2, barbOffset + halfBarbOffsetFudge + startLength + j.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor, bearing)
                        tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
                        stormList.add(tmpCoords[0])
                        stormList.add(tmpCoords[1])

                        start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                        ec = ecc.calculateEndingGlobalCoordinates(ExternalEllipsoid.WGS84, start, degree2 - arrowBend * 2.0, startLength + arrowLength / 2.0 * nmScaleFactor, bearing)
                        tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
                        stormList.add(tmpCoords[0])
                        stormList.add(tmpCoords[1])
                    }
                } // if length greater then 4
            } // loop over wind barbs
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        stormListArr = FloatArray(stormList.size)
        stormList.indices.forEach { stormListArr[it] = stormList[it].toFloat() }
        canvas.drawLines(stormListArr, paint)
        // draw aviation circle on top
        wbCircleXArr.indices.forEach { k ->
            if (UtilityMetar.obsArrAviationColor.size > k) {
                if (mercato) {
                    paint.color = UtilityMetar.obsArrAviationColor[k]
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(wbCircleXArr[k], wbCircleYArr[k], pn)
                } else {
                    paint.color = UtilityMetar.obsArrAviationColor[k]
                    tmpCoords = UtilityCanvasProjection.compute4326Numbers(wbCircleXArr[k], wbCircleYArr[k], pn)
                }
                pixXInit = tmpCoords[0]
                pixYInit = tmpCoords[1]
                canvas.drawCircle(pixXInit.toFloat(), pixYInit.toFloat(), 5f, paint)
            }
        }
    }
}
