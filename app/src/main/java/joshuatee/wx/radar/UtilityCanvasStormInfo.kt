/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.graphics.Path

import joshuatee.wx.external.ExternalEllipsoid
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.settings.UtilityLocation

object UtilityCanvasStormInfo {

    private const val stiBaseFn = "nids_sti_tab"

    fun drawNexRadStormMotion(
        context: Context,
        provider: ProjectionType,
        bitmap: Bitmap,
        radarSite: String
    ) {
        val textSize = 22
        WXGLDownload.getNidsTab(context, "STI", radarSite.toLowerCase(), stiBaseFn + "")
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 2f
        if (provider === ProjectionType.WX_RENDER || provider === ProjectionType.WX_RENDER_48) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (provider.needsBlackPaint) {
            paint.color = Color.rgb(0, 0, 0)
        }
        paint.textSize = textSize.toFloat()
        val pn = ProjectionNumbers(radarSite, provider)
        var tmpCoords: DoubleArray
        var tmpCoords2: DoubleArray
        val stormList = mutableListOf<Double>()
        val stormListArr: FloatArray
        val retStr: String
        val location = UtilityLocation.getSiteLocation(context, radarSite)
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, stiBaseFn + ""))
            dis.bigEndian = true
            retStr = UtilityLevel3TextProduct.read(dis)
            val posn = retStr.parseColumn(RegExp.stiPattern1)
            val motion = retStr.parseColumn(RegExp.stiPattern2)
            var posnStr = ""
            var motionStr = ""
            posn
                .map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
                .forEach { posnStr += it.replace("/", " ") }
            motion
                .map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
                .forEach { motionStr += it.replace("/", " ") }
            val posnNumbers = posnStr.parseColumnAll(RegExp.stiPattern3)
            val motNumbers = motionStr.parseColumnAll(RegExp.stiPattern3)
            var degree: Double
            var nm: Double
            var degree2: Double
            var nm2: Double
            val bearing = DoubleArray(2)
            var start: ExternalGlobalCoordinates
            var ec: ExternalGlobalCoordinates
            val ecArr = Array(4) { ExternalGlobalCoordinates(0.0, 0.0) }
            val tmpCoordsArr = Array(4) { LatLon() }
            var endPoint: DoubleArray
            val degreeShift = 180.00
            val arrowLength = 2.0
            val arrowBend = 20.0
            val sti15IncrLen = 0.40
            if (posnNumbers.size == motNumbers.size && posnNumbers.size > 1) {
                var s = 0
                while (s < posnNumbers.size) {
                    val ecc = ExternalGeodeticCalculator()
                    degree = posnNumbers[s].toDouble()
                    nm = posnNumbers[s + 1].toDouble()
                    degree2 = motNumbers[s].toDouble()
                    nm2 = motNumbers[s + 1].toDouble()
                    start = ExternalGlobalCoordinates(location)
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        start,
                        degree,
                        nm * 1852.0,
                        bearing
                    )
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec, pn)
                    stormList.add(tmpCoords[0])
                    stormList.add(tmpCoords[1])
                    start = ExternalGlobalCoordinates(ec)
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        start,
                        degree2 + degreeShift,
                        nm2 * 1852.0,
                        bearing
                    )
                    // mercator expects lat/lon to both be positive as many products have this
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(
                        ec.latitude,
                        ec.longitude * -1,
                        pn
                    )
                    ecArr.indices.forEach {
                        ecArr[it] = ecc.calculateEndingGlobalCoordinates(
                            ExternalEllipsoid.WGS84,
                            start,
                            degree2 + degreeShift,
                            nm2 * 1852.0 * it.toDouble() * 0.25,
                            bearing
                        )
                        tmpCoordsArr[it] = LatLon(
                            UtilityCanvasProjection.computeMercatorNumbers(
                                ecArr[it],
                                pn
                            )
                        )
                    }
                    stormList.add(tmpCoords[0])
                    stormList.add(tmpCoords[1])
                    endPoint = tmpCoords
                    if (nm2 > 0.01) {
                        start = ExternalGlobalCoordinates(ec)
                        drawLine(
                            stormList,
                            endPoint,
                            ecc,
                            pn,
                            start,
                            degree2 + arrowBend,
                            arrowLength * 1852.0,
                            bearing
                        )
                        drawLine(
                            stormList,
                            endPoint,
                            ecc,
                            pn,
                            start,
                            degree2 - arrowBend,
                            arrowLength * 1852.0,
                            bearing
                        )
                        // 15,30,45 min ticks
                        val stormTrackTickMarkAngleOff90 = 45.0
                        tmpCoordsArr.indices.forEach { z ->
                            // first line
                            drawTickMarks(
                                stormList,
                                tmpCoordsArr[z],
                                ecc,
                                pn,
                                ecArr[z],
                                degree2 - (90.0 + stormTrackTickMarkAngleOff90),
                                arrowLength * 1852.0 * sti15IncrLen,
                                bearing
                            )
                            drawTickMarks(
                                stormList,
                                tmpCoordsArr[z],
                                ecc,
                                pn,
                                ecArr[z],
                                degree2 + (90.0 - stormTrackTickMarkAngleOff90),
                                arrowLength * 1852.0 * sti15IncrLen,
                                bearing
                            )
                            // 2nd line
                            drawTickMarks(
                                stormList,
                                tmpCoordsArr[z],
                                ecc,
                                pn,
                                ecArr[z],
                                degree2 - (90.0 - stormTrackTickMarkAngleOff90),
                                arrowLength * 1852.0 * sti15IncrLen,
                                bearing
                            )
                            drawTickMarks(
                                stormList,
                                tmpCoordsArr[z],
                                ecc,
                                pn,
                                ecArr[z],
                                degree2 + (90.0 + stormTrackTickMarkAngleOff90),
                                arrowLength * 1852.0 * sti15IncrLen,
                                bearing
                            )
                        }
                    }
                    s += 2
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        stormListArr = FloatArray(stormList.size)
        stormList.indices.forEach { stormListArr[it] = stormList[it].toFloat() }
        paint.color = MyApplication.radarColorSti
        canvas.drawLines(stormListArr, paint)
        val wallPath = Path()
        wallPath.reset()
        var i = 0
        while (i < stormListArr.size) {
            if (provider.isMercator) {
                tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(
                    stormListArr[i].toDouble(),
                    stormListArr[i + 1].toDouble(),
                    pn
                )
                tmpCoords2 = UtilityCanvasProjection.computeMercatorNumbers(
                    stormListArr[i + 2].toDouble(),
                    stormListArr[i + 3].toDouble(),
                    pn
                )
            } else {
                tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(
                    stormListArr[i].toDouble(),
                    stormListArr[i + 1].toDouble(),
                    pn
                )
                tmpCoords2 = UtilityCanvasProjection.computeMercatorNumbers(
                    stormListArr[i + 2].toDouble(),
                    stormListArr[i + 3].toDouble(),
                    pn
                )
            }
            wallPath.reset()
            wallPath.moveTo(tmpCoords[0].toFloat(), tmpCoords[1].toFloat())
            wallPath.lineTo(tmpCoords2[0].toFloat(), tmpCoords2[1].toFloat())
            canvas.drawPath(wallPath, paint)
            i += 4
        }
    }

    private fun drawTickMarks(
        list: MutableList<Double>,
        startPoint: LatLon,
        ecc: ExternalGeodeticCalculator,
        pn: ProjectionNumbers,
        ecArr: ExternalGlobalCoordinates,
        startBearing: Double,
        distance: Double,
        bearing: DoubleArray
    ) {
        list.add(startPoint.lat)
        list.add(startPoint.lon)
        val start = ExternalGlobalCoordinates(ecArr)
        val ec = ecc.calculateEndingGlobalCoordinates(
            ExternalEllipsoid.WGS84,
            start,
            startBearing,
            distance,
            bearing
        )
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(ec, pn)
        list.add(tmpCoords[0])
        list.add(tmpCoords[1])
    }

    private fun drawLine(
        list: MutableList<Double>,
        startPoint: DoubleArray,
        ecc: ExternalGeodeticCalculator,
        pn: ProjectionNumbers,
        start: ExternalGlobalCoordinates,
        startBearing: Double,
        distance: Double,
        bearing: DoubleArray
    ) {
        list.add(startPoint[0])
        list.add(startPoint[1])
        val ec = ecc.calculateEndingGlobalCoordinates(
            ExternalEllipsoid.WGS84,
            start,
            startBearing,
            distance,
            bearing
        )
        val tmpCoords =
            UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn)
        list.add(tmpCoords[0])
        list.add(tmpCoords[1])
    }
}
