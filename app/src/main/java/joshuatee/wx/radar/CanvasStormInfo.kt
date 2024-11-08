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

@file:Suppress("SameParameterValue")

package joshuatee.wx.radar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.To
import joshuatee.wx.objects.LatLon
import joshuatee.wx.parseColumn
import joshuatee.wx.parseColumnAll
import joshuatee.wx.settings.RadarPreferences
import java.util.Locale
import java.util.regex.Pattern

object CanvasStormInfo {

    private val pattern1: Pattern = Pattern.compile("AZ/RAN(.*?)V")
    private val pattern2: Pattern = Pattern.compile("MVT(.*?)V")
    private val pattern3: Pattern = Pattern.compile("\\d+")

    fun draw(projectionType: ProjectionType, bitmap: Bitmap, radarSite: String) {
        val textSize = 22
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 2.0f
        canvas.translate(CanvasMain.xOffset, CanvasMain.yOffset)
        if (projectionType.needsBlackPaint) {
            paint.color = Color.rgb(0, 0, 0)
        }
        paint.textSize = textSize.toFloat()
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val stormList = mutableListOf<Double>()
        val location = RadarSites.getLatLon(radarSite)
        val data = NexradLevel3TextProduct.download("STI", radarSite.lowercase(Locale.US))
        val posn = data.parseColumn(pattern1)
        val motion = data.parseColumn(pattern2)
        var posnStr = ""
        var motionStr = ""
        // TODO FIXME
        posn.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
            .forEach {
                posnStr += it.replace("/", " ")
            }
        motion.map { it.replace("NEW", "0/ 0").replace("/ ", "/").replace("\\s+".toRegex(), " ") }
            .forEach {
                motionStr += it.replace("/", " ")
            }
        val posnNumbers = posnStr.parseColumnAll(pattern3)
        val motNumbers = motionStr.parseColumnAll(pattern3)
        val degreeShift = 180.00
        val arrowLength = 2.0
        val arrowBend = 20.0
        val sti15IncrementLength = 0.40
        if (posnNumbers.size == motNumbers.size && posnNumbers.size > 1) {
            for (s in posnNumbers.indices step 2) {
                val degree = To.double(posnNumbers[s])
                val nm = To.double(posnNumbers[s + 1])
                val degree2 = To.double(motNumbers[s])
                val nm2 = To.double(motNumbers[s + 1])
                var start = ExternalGlobalCoordinates(location)
                var ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                    start,
                    degree,
                    nm * 1852.0
                )
                stormList += Projection.computeMercatorNumbers(ec, projectionNumbers).toList()
                start = ExternalGlobalCoordinates(ec)
                ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                    start,
                    degree2 + degreeShift,
                    nm2 * 1852.0
                )
                // mercator expects lat/lon to both be positive as many products have this
                val list = Projection.computeMercatorNumbers(
                    ec.latitude,
                    ec.longitude * -1,
                    projectionNumbers
                ).toList()
                val ecList = mutableListOf<ExternalGlobalCoordinates>()
                val latLons = mutableListOf<LatLon>()
                (0..3).forEach { z ->
                    ecList.add(
                        ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            start,
                            degree2 + degreeShift,
                            nm2 * 1852.0 * z.toDouble() * 0.25
                        )
                    )
                    latLons.add(
                        LatLon(
                            Projection.computeMercatorNumbers(
                                ecList[z],
                                projectionNumbers
                            )
                        )
                    )
                }
                stormList += list
                val endPoint = list.toDoubleArray()
                if (nm2 > 0.01) {
                    start = ExternalGlobalCoordinates(ec)
                    drawLine(
                        stormList,
                        endPoint,
                        projectionNumbers,
                        start,
                        degree2 + arrowBend,
                        arrowLength * 1852.0
                    )
                    drawLine(
                        stormList,
                        endPoint,
                        projectionNumbers,
                        start,
                        degree2 - arrowBend,
                        arrowLength * 1852.0
                    )
                    // 15,30,45 min ticks
                    val stormTrackTickMarkAngleOff90 = 45.0
                    latLons.indices.forEach { z ->
                        // first line
                        drawTickMarks(
                            stormList,
                            latLons[z],
                            projectionNumbers,
                            ecList[z],
                            degree2 - (90.0 + stormTrackTickMarkAngleOff90),
                            arrowLength * 1852.0 * sti15IncrementLength
                        )
                        drawTickMarks(
                            stormList,
                            latLons[z],
                            projectionNumbers,
                            ecList[z],
                            degree2 + (90.0 - stormTrackTickMarkAngleOff90),
                            arrowLength * 1852.0 * sti15IncrementLength
                        )
                        // 2nd line
                        drawTickMarks(
                            stormList,
                            latLons[z],
                            projectionNumbers,
                            ecList[z],
                            degree2 - (90.0 - stormTrackTickMarkAngleOff90),
                            arrowLength * 1852.0 * sti15IncrementLength
                        )
                        drawTickMarks(
                            stormList,
                            latLons[z],
                            projectionNumbers,
                            ecList[z],
                            degree2 + (90.0 + stormTrackTickMarkAngleOff90),
                            arrowLength * 1852.0 * sti15IncrementLength
                        )
                    }
                }
            }
        }
        val stormLists = FloatArray(stormList.size)
        stormList.indices.forEach {
            stormLists[it] = stormList[it].toFloat()
        }
        paint.color = RadarPreferences.colorSti
        canvas.drawLines(stormLists, paint)
        val wallPath = Path()
        wallPath.reset()
        for (i in 0 until stormList.size step 4) {
            val list: List<Float>
            val list2: List<Float>
            if (projectionType.isMercator) {
                list = Projection.computeMercatorNumbers(
                    stormLists[i],
                    stormLists[i + 1],
                    projectionNumbers
                )
                list2 = Projection.computeMercatorNumbers(
                    stormLists[i + 2],
                    stormLists[i + 3],
                    projectionNumbers
                )
            } else {
                list = Projection.computeMercatorNumbers(
                    stormLists[i],
                    stormLists[i + 1],
                    projectionNumbers
                )
                list2 = Projection.computeMercatorNumbers(
                    stormLists[i + 2],
                    stormLists[i + 3],
                    projectionNumbers
                )
            }
            with(wallPath) {
                reset()
                moveTo(list[0], list[1])
                lineTo(list2[0], list2[1])
            }
            canvas.drawPath(wallPath, paint)
        }
    }

    // FIXME are these the same as in Level3Common ?
    @Suppress("SameParameterValue")
    private fun drawTickMarks(
        list: MutableList<Double>,
        startPoint: LatLon,
        pn: ProjectionNumbers,
        ecArr: ExternalGlobalCoordinates,
        startBearing: Double,
        distance: Double
    ) {
        list.add(startPoint.lat)
        list.add(startPoint.lon)
        val start = ExternalGlobalCoordinates(ecArr)
        val ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
            start,
            startBearing,
            distance
        )
        list += Projection.computeMercatorNumbers(ec, pn).toList()
    }

    @Suppress("SameParameterValue")
    private fun drawLine(
        list: MutableList<Double>,
        startPoint: DoubleArray,
        pn: ProjectionNumbers,
        start: ExternalGlobalCoordinates,
        startBearing: Double,
        distance: Double
    ) {
        list.add(startPoint[0])
        list.add(startPoint[1])
        val ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
            start,
            startBearing,
            distance
        )
        list += Projection.computeMercatorNumbers(ec.latitude, ec.longitude * -1.0, pn).toList()
    }
}
