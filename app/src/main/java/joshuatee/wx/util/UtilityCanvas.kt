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
//modded by ELY M.  

package joshuatee.wx.util

//elys mod //leave it
import android.graphics.*
import android.graphics.Paint.Style
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.Location
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.*
import joshuatee.wx.radar.UtilityCanvasProjection
import joshuatee.wx.settings.RadarPreferences

internal object UtilityCanvas {

    fun addWarnings(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        val path = Path()
        val paintList = listOf(RadarPreferences.radarColorFfw, RadarPreferences.radarColorTstorm, RadarPreferences.radarColorTor)
        val dataList = listOf(ObjectPolygonWarning.severeDashboardFfw.value, ObjectPolygonWarning.severeDashboardTst.value, ObjectPolygonWarning.severeDashboardTor.value)
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        dataList.forEachIndexed { index, it ->
            paint.color = paintList[index]
            val data = it.replace("\n", "").replace(" ", "")
            val warnings = UtilityString.parseColumnMutable(data, RegExp.warningLatLonPattern)
            val warningsFiltered = mutableListOf<String>()
            val vtecs = data.parseColumn(RegExp.warningVtecPattern)
            warnings.forEachIndexed { i, _ ->
                warnings[i] = warnings[i].replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
                if (!(vtecs[i].startsWith("O.EXP") || vtecs[i].startsWith("O.CAN"))) {
                    warningsFiltered.add(warnings[i])
                }
            }
            canvasDrawWarnings(warningsFiltered, vtecs, canvas, path, paint, projectionType.isMercator, projectionNumbers)
        }
    }

    fun drawCitiesUS(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers, textSize: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = GeographyType.CITIES.color
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (projectionType.needsBlackPaint) {
            paint.color = Color.rgb(0, 0, 0)
        }
        paint.textSize = textSize.toFloat()
        UtilityCities.list.indices.forEach {
            val coordinates = if (projectionType.isMercator) {
                UtilityCanvasProjection.computeMercatorNumbers(UtilityCities.list[it].x, UtilityCities.list[it].y, projectionNumbers)
            } else {
                UtilityCanvasProjection.compute4326Numbers(UtilityCities.list[it].x, UtilityCities.list[it].y, projectionNumbers)
            }
            if (textSize > 0) {
                canvas.drawText(
                        RegExp.comma.split(UtilityCities.list[it].city)[0],
                        coordinates[0].toFloat() + 4,
                        coordinates[1].toFloat() - 4,
                        paint
                )
                canvas.drawCircle(coordinates[0].toFloat(), coordinates[1].toFloat(), 2f, paint)
            } else {
                canvas.drawCircle(coordinates[0].toFloat(), coordinates[1].toFloat(), 1f, paint)
            }
        }
    }

    fun addLocationDotForCurrentLocation(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = RadarPreferences.radarColorLocdot
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val x = Location.x.toDoubleOrNull() ?: 0.0
        val y = Location.y.replace("-", "").toDoubleOrNull() ?: 0.0
        val coordinates = if (projectionType.isMercator) {
            UtilityCanvasProjection.computeMercatorNumbers(x, y, projectionNumbers)
        } else {
            UtilityCanvasProjection.compute4326Numbers(x, y, projectionNumbers)
        }
        paint.color = RadarPreferences.radarColorLocdot

	//elys mod
        //custom locationdot//
        if (RadarPreferences.locationDotFollowsGps) {
            UtilityLog.d("wx", "Path to location.png: "+ GlobalVariables.FilesPath + "location.png")
            val locationicon: Bitmap = BitmapFactory.decodeFile(GlobalVariables.FilesPath + "location.png");
            val locationiconresized: Bitmap = Bitmap.createScaledBitmap(locationicon, RadarPreferences.radarLocIconSize, RadarPreferences.radarLocIconSize, false)
            canvas.drawBitmap(locationiconresized, coordinates[0].toFloat(), coordinates[1].toFloat(), null)
        } else {
        canvas.drawCircle(coordinates[0].toFloat(), coordinates[1].toFloat(), 2f, paint)

        }

    }
    fun addMcd(projectionType: ProjectionType, bitmap: Bitmap, projectionNumbers: ProjectionNumbers, polygonType: PolygonType) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.color = Color.rgb(255, 0, 0)
        val path = Path()
        if (projectionType.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        paint.color = polygonType.color
        // TODO FIXME refactor
        val prefToken = when (polygonType) {
            PolygonType.MCD -> ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]!!.latLonList.value
            PolygonType.MPD -> ObjectPolygonWatch.polygonDataByType[PolygonType.MPD]!!.latLonList.value
            PolygonType.WATCH -> ObjectPolygonWatch.polygonDataByType[PolygonType.WATCH]!!.latLonList.value
            PolygonType.WATCH_TORNADO -> ObjectPolygonWatch.polygonDataByType[PolygonType.WATCH_TORNADO]!!.latLonList.value
            else -> ""
        }
        val list = prefToken.split(":").dropLastWhile { it.isEmpty() }
        canvasDrawWatchMcdMpd(list, canvas, path, paint, projectionType.isMercator, projectionNumbers)
    }

    private fun canvasDrawWatchMcdMpd(warnings: List<String>, canvas: Canvas, path: Path, paint: Paint, isMercator: Boolean, projectionNumbers: ProjectionNumbers) {
        warnings.forEach { warning ->
            val latLons = LatLon.parseStringToLatLons(warning, 1.0, false)
            path.reset()
            if (latLons.isNotEmpty()) {
                val startCoordinates = if (isMercator) {
                    UtilityCanvasProjection.computeMercatorNumbers(latLons[0], projectionNumbers)
                } else {
                    UtilityCanvasProjection.compute4326Numbers(latLons[0], projectionNumbers)
                }
                val firstX = startCoordinates[0]
                val firstY = startCoordinates[1]
                path.moveTo(firstX.toFloat(), firstY.toFloat())
                (1 until latLons.size).forEach { index ->
                    val coordinates = if (isMercator) {
                        UtilityCanvasProjection.computeMercatorNumbers(latLons[index], projectionNumbers)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(latLons[index], projectionNumbers)
                    }
                    path.lineTo(coordinates[0].toFloat(), coordinates[1].toFloat())
                }
                path.lineTo(firstX.toFloat(), firstY.toFloat())
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun canvasDrawWarnings(warnings: List<String>, vtecs: List<String>, canvas: Canvas, path: Path, paint: Paint, isMercator: Boolean, projectionNumbers: ProjectionNumbers) {
        warnings.forEachIndexed { polygonCount, warning ->
            if (vtecs.isNotEmpty() && vtecs.size > polygonCount && !vtecs[polygonCount].startsWith("0.EXP") && !vtecs[polygonCount].startsWith("0.CAN")) {
                val latLons = LatLon.parseStringToLatLons(warning)
                path.reset()
                if (latLons.isNotEmpty()) {
                    val startCoordinates = if (isMercator) {
                        UtilityCanvasProjection.computeMercatorNumbers(latLons[0], projectionNumbers)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(latLons[0], projectionNumbers)
                    }
                    val firstX = startCoordinates[0]
                    val firstY = startCoordinates[1]
                    path.moveTo(firstX.toFloat(), firstY.toFloat())
                    (1 until latLons.size).forEach { index ->
                        val coordinates = if (isMercator) {
                            UtilityCanvasProjection.computeMercatorNumbers(latLons[index], projectionNumbers)
                        } else {
                            UtilityCanvasProjection.compute4326Numbers(latLons[index], projectionNumbers)
                        }
                        path.lineTo(coordinates[0].toFloat(), coordinates[1].toFloat())
                    }
                    path.lineTo(firstX.toFloat(), firstY.toFloat())
                    canvas.drawPath(path, paint)
                }
            }
        }
    }
}
