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
//modded by ELY M.  

package joshuatee.wx.radar

//elys mod - leave this alone
import android.graphics.*
import android.graphics.Paint.Style
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.Location
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.ObjectWarning
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.UtilityCities
import joshuatee.wx.util.UtilityLog
import java.nio.ByteBuffer

internal object CanvasDraw {

    fun warnings(
        projectionType: ProjectionType,
        bitmap: Bitmap,
        projectionNumbers: ProjectionNumbers
    ) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        val path = Path()
        if (projectionType.needsCanvasShift) {
            canvas.translate(CanvasMain.xOffset, CanvasMain.yOffset)
        }
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        listOf(
            PolygonWarningType.FlashFloodWarning,
            PolygonWarningType.TornadoWarning,
            PolygonWarningType.ThunderstormWarning
        ).forEach {
            paint.color = PolygonWarning.byType[it]!!.color
            val html = PolygonWarning.byType[it]!!.getData()
            val warnings = ObjectWarning.parseJson(html)
            val warningList = mutableListOf<Double>()
            for (w in warnings) {
                if (it == PolygonWarningType.SpecialWeatherStatement || it == PolygonWarningType.SpecialMarineWarning || w.isCurrent) {
                    val latLons = w.getPolygonAsLatLons(-1)
                    warningList += LatLon.latLonListToListOfDoubles(latLons, projectionNumbers)
                }
            }
            addWarnings(warningList, canvas, path, paint)
        }
    }

    fun cities(
        projectionType: ProjectionType,
        bitmap: Bitmap,
        projectionNumbers: ProjectionNumbers,
        textSize: Int
    ) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        with(paint) {
            style = Style.FILL
            strokeWidth = 1.0f
            color = RadarPreferences.colorCity
        }
        if (projectionType.needsCanvasShift) {
            canvas.translate(CanvasMain.xOffset, CanvasMain.yOffset)
        }
        if (projectionType.needsBlackPaint) {
            paint.color = Color.rgb(0, 0, 0)
        }
        paint.textSize = textSize.toFloat()
        UtilityCities.list.forEach {
            val coordinates = Projection.computeMercatorNumbers(it.x, it.y, projectionNumbers)
            if (textSize > 0) {
                canvas.drawText(
                    RegExp.comma.split(it.city)[0],
                    coordinates[0].toFloat() + 4.0f,
                    coordinates[1].toFloat() - 4.0f,
                    paint
                )
                canvas.drawCircle(coordinates[0].toFloat(), coordinates[1].toFloat(), 2.0f, paint)
            } else {
                canvas.drawCircle(coordinates[0].toFloat(), coordinates[1].toFloat(), 1.0f, paint)
            }
        }
    }

    fun locationDotForCurrentLocation(
        projectionType: ProjectionType,
        bitmap: Bitmap,
        projectionNumbers: ProjectionNumbers
    ) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        with(paint) {
            style = Style.FILL
            strokeWidth = 1.0f
            color = RadarPreferences.colorLocdot
        }
        if (projectionType.needsCanvasShift) {
            canvas.translate(CanvasMain.xOffset, CanvasMain.yOffset)
        }
        val x = To.double(Location.x)
        val y = To.double(Location.y.replace("-", ""))
        val coordinates = Projection.computeMercatorNumbers(x, y, projectionNumbers)
        paint.color = RadarPreferences.colorLocdot
	    //elys mod
        //custom locationdot//
        if (RadarPreferences.locationDotFollowsGps) {
            val locationicon: Bitmap = BitmapFactory.decodeFile(GlobalVariables.FilesPath + "location.png");
            val locationiconresized: Bitmap = Bitmap.createScaledBitmap(locationicon, RadarPreferences.locIconSize, RadarPreferences.locIconSize, false)
            canvas.drawBitmap(locationiconresized, coordinates[0].toFloat(), coordinates[1].toFloat(), null)
        } else {
        canvas.drawCircle(coordinates[0].toFloat(), coordinates[1].toFloat(), 2.0f, paint)
    }

    fun mcd(
        projectionType: ProjectionType,
        bitmap: Bitmap,
        projectionNumbers: ProjectionNumbers,
        polygonType: PolygonType
    ) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.color = Color.rgb(255, 0, 0)
        val path = Path()
        if (projectionType.needsCanvasShift) {
            canvas.translate(CanvasMain.xOffset, CanvasMain.yOffset)
        }
        paint.strokeWidth = projectionNumbers.polygonWidth.toFloat()
        paint.color = polygonType.color
        val prefToken = PolygonWatch.byType[polygonType]!!.latLonList.value
        val list = prefToken.split(":").dropLastWhile { it.isEmpty() }
        drawMcd(list, canvas, path, paint, projectionNumbers)
    }

    private fun drawMcd(
        polygons: List<String>,
        canvas: Canvas,
        path: Path,
        paint: Paint,
        projectionNumbers: ProjectionNumbers
    ) {
        val warningList = mutableListOf<Double>()
        polygons.forEach { polygon ->
            val latLons = LatLon.parseStringToLatLons(polygon, 1.0, false)
            warningList += LatLon.latLonListToListOfDoubles(latLons, projectionNumbers)
        }
        if (warningList.size > 3) {
            path.reset()
            warningList.indices.step(4).forEach {
                path.moveTo(warningList[it].toFloat(), warningList[it + 1].toFloat())
                path.lineTo(warningList[it].toFloat(), warningList[it + 1].toFloat())
                path.lineTo(warningList[it + 2].toFloat(), warningList[it + 3].toFloat())
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun addWarnings(warnings: List<Double>, canvas: Canvas, path: Path, paint: Paint) {
        if (warnings.size > 3) {
            path.reset()
            warnings.indices.step(4).forEach {
                path.moveTo(warnings[it].toFloat(), warnings[it + 1].toFloat())
                path.lineTo(warnings[it].toFloat(), warnings[it + 1].toFloat())
                path.lineTo(warnings[it + 2].toFloat(), warnings[it + 3].toFloat())
                canvas.drawPath(path, paint)
            }
        }
    }

    fun geometry(
        projectionType: ProjectionType,
        bitmap: Bitmap,
        radarSite: String,
        geographyType: RadarGeometryTypeEnum,
        genericByteBuffer: ByteBuffer
    ) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.strokeWidth = (RadarGeometry.dataByType[geographyType]!!.lineSize / 2.0).toFloat()
        paint.color = RadarGeometry.dataByType[geographyType]!!.colorInt
        if (projectionType.needsCanvasShift) canvas.translate(
            CanvasMain.xOffset,
            CanvasMain.yOffset
        )
        val path = Path()
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        genericByteBuffer.position(0)
        try {
            val tmpBuffer = ByteBuffer.allocateDirect(genericByteBuffer.capacity())
            if (projectionType.isMercator) {
                UtilityCanvasProjection.computeMercatorFloatToBuffer(
                    genericByteBuffer,
                    tmpBuffer,
                    projectionNumbers
                )
            } else {
                UtilityCanvasProjection.compute4326NumbersFloatToBuffer(
                    genericByteBuffer,
                    tmpBuffer,
                    projectionNumbers
                )
            }
            tmpBuffer.position(0)
            while (tmpBuffer.position() < tmpBuffer.capacity()) {
                path.moveTo(tmpBuffer.float, tmpBuffer.float)
                path.lineTo(tmpBuffer.float, tmpBuffer.float)
            }
            canvas.drawPath(path, paint)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
    }
}
