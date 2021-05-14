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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path

import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.settings.UtilityLocation
import java.util.*

object UtilityCanvasStormInfo {

    private const val stiBaseFileName = "nids_sti_tab"

    fun drawNexRadStormMotion(context: Context, projectionType: ProjectionType, bitmap: Bitmap, radarSite: String) {
        val textSize = 22
        WXGLDownload.getNidsTab(context, "STI", radarSite.lowercase(Locale.US), stiBaseFileName + "")
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 2f
        if (projectionType === ProjectionType.WX_RENDER || projectionType === ProjectionType.WX_RENDER_48) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (projectionType.needsBlackPaint) paint.color = Color.rgb(0, 0, 0)
        paint.textSize = textSize.toFloat()
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val stormList = mutableListOf<Double>()
        val location = UtilityLocation.getSiteLocation(radarSite)
        try {
            val ucarRandomAccessFile = UCARRandomAccessFile(UtilityIO.getFilePath(context, stiBaseFileName + ""))
            ucarRandomAccessFile.bigEndian = true
            val data = UtilityLevel3TextProduct.read(ucarRandomAccessFile)
            val posn = data.parseColumn(RegExp.stiPattern1)
            val motion = data.parseColumn(RegExp.stiPattern2)
            var posnStr = ""
            var motionStr = ""
            posn.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
                .forEach {
                    posnStr += it.replace("/", " ")
                }
            motion.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
                .forEach {
                    motionStr += it.replace("/", " ")
                }
            val posnNumbers = posnStr.parseColumnAll(RegExp.stiPattern3)
            val motNumbers = motionStr.parseColumnAll(RegExp.stiPattern3)
            var endPoint: DoubleArray
            val degreeShift = 180.00
            val arrowLength = 2.0
            val arrowBend = 20.0
            val sti15IncrementLength = 0.40
            if (posnNumbers.size == motNumbers.size && posnNumbers.size > 1) {
                for (s in posnNumbers.indices step 2) {
                    val ecc = ExternalGeodeticCalculator()
                    val degree = posnNumbers[s].toDouble()
                    val nm = posnNumbers[s + 1].toDouble()
                    val degree2 = motNumbers[s].toDouble()
                    val nm2 = motNumbers[s + 1].toDouble()
                    var start = ExternalGlobalCoordinates(location)
                    var ec = ecc.calculateEndingGlobalCoordinates(start, degree, nm * 1852.0)
                    stormList += UtilityCanvasProjection.computeMercatorNumbers(ec, projectionNumbers).toList()
                    start = ExternalGlobalCoordinates(ec)
                    ec = ecc.calculateEndingGlobalCoordinates(start, degree2 + degreeShift, nm2 * 1852.0)
                    // mercator expects lat/lon to both be positive as many products have this
                    val list = UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, projectionNumbers).toList()
                    val ecList = mutableListOf<ExternalGlobalCoordinates>()
                    val latLons = mutableListOf<LatLon>()
                    (0..3).forEach { z ->
                        ecList.add(ecc.calculateEndingGlobalCoordinates(start, degree2 + degreeShift, nm2 * 1852.0 * z.toDouble() * 0.25))
                        latLons.add(LatLon(UtilityCanvasProjection.computeMercatorNumbers(ecList[z], projectionNumbers)))
                    }
                    stormList += list
                    endPoint = list.toDoubleArray()
                    if (nm2 > 0.01) {
                        start = ExternalGlobalCoordinates(ec)
                        drawLine(stormList, endPoint, ecc, projectionNumbers, start, degree2 + arrowBend, arrowLength * 1852.0)
                        drawLine(stormList, endPoint, ecc, projectionNumbers, start, degree2 - arrowBend, arrowLength * 1852.0)
                        // 15,30,45 min ticks
                        val stormTrackTickMarkAngleOff90 = 45.0
                        latLons.indices.forEach { z ->
                            // first line
                            drawTickMarks(stormList, latLons[z], ecc, projectionNumbers, ecList[z], degree2 - (90.0 + stormTrackTickMarkAngleOff90), arrowLength * 1852.0 * sti15IncrementLength)
                            drawTickMarks(stormList, latLons[z], ecc, projectionNumbers, ecList[z], degree2 + (90.0 - stormTrackTickMarkAngleOff90), arrowLength * 1852.0 * sti15IncrementLength)
                            // 2nd line
                            drawTickMarks(stormList, latLons[z], ecc, projectionNumbers, ecList[z], degree2 - (90.0 - stormTrackTickMarkAngleOff90), arrowLength * 1852.0 * sti15IncrementLength)
                            drawTickMarks(stormList, latLons[z], ecc, projectionNumbers, ecList[z], degree2 + (90.0 + stormTrackTickMarkAngleOff90), arrowLength * 1852.0 * sti15IncrementLength)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        val stormLists = FloatArray(stormList.size)
        stormList.indices.forEach { stormLists[it] = stormList[it].toFloat() }
        paint.color = MyApplication.radarColorSti
        canvas.drawLines(stormLists, paint)
        val wallPath = Path()
        wallPath.reset()
        for (i in 0 until stormList.size step 4) {
            val list: List<Double>
            val list2: List<Double>
            if (projectionType.isMercator) {
                list = UtilityCanvasProjection.computeMercatorNumbers(stormLists[i].toDouble(), stormLists[i + 1].toDouble(), projectionNumbers).toList()
                list2 = UtilityCanvasProjection.computeMercatorNumbers(stormLists[i + 2].toDouble(), stormLists[i + 3].toDouble(), projectionNumbers).toList()
            } else {
                list = UtilityCanvasProjection.computeMercatorNumbers(stormLists[i].toDouble(), stormLists[i + 1].toDouble(), projectionNumbers).toList()
                list2 = UtilityCanvasProjection.computeMercatorNumbers(stormLists[i + 2].toDouble(), stormLists[i + 3].toDouble(), projectionNumbers).toList()
            }
            wallPath.reset()
            wallPath.moveTo(list[0].toFloat(), list[1].toFloat())
            wallPath.lineTo(list2[0].toFloat(), list2[1].toFloat())
            canvas.drawPath(wallPath, paint)
        }
    }

    // FIXME are these the same as in Level3Common ?
    private fun drawTickMarks(
        list: MutableList<Double>,
        startPoint: LatLon,
        ecc: ExternalGeodeticCalculator,
        pn: ProjectionNumbers,
        ecArr: ExternalGlobalCoordinates,
        startBearing: Double,
        distance: Double
    ) {
        list.add(startPoint.lat)
        list.add(startPoint.lon)
        val start = ExternalGlobalCoordinates(ecArr)
        val ec = ecc.calculateEndingGlobalCoordinates(start, startBearing, distance)
        list += UtilityCanvasProjection.computeMercatorNumbers(ec, pn).toList()
    }

    private fun drawLine(
        list: MutableList<Double>,
        startPoint: DoubleArray,
        ecc: ExternalGeodeticCalculator,
        pn: ProjectionNumbers,
        start: ExternalGlobalCoordinates,
        startBearing: Double,
        distance: Double
    ) {
        list.add(startPoint[0])
        list.add(startPoint[1])
        val ec = ecc.calculateEndingGlobalCoordinates(start, startBearing, distance)
        list += UtilityCanvasProjection.computeMercatorNumbers(ec.latitude, ec.longitude * -1, pn).toList()
    }
}
