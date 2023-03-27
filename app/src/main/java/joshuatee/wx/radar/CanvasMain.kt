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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.lang.Exception

object CanvasMain {

    var xOffset = 0.0f
    var yOffset = 0.0f

    fun setImageOffset(context: Context) {
        when (Utility.readPref(context, "WIDGET_NEXRAD_CENTER", "Center")) {
            "Center" -> {
                xOffset = 0.0f
                yOffset = 0.0f
            }
            "NW" -> {
                xOffset = -85.0f
                yOffset = -85.0f
            }
            "NE" -> {
                xOffset = 85.0f
                yOffset = -85.0f
            }
            "SW" -> {
                xOffset = -85.0f
                yOffset = 85.0f
            }
            "SE" -> {
                xOffset = 85.0f
                yOffset = 85.0f
            }
            "N" -> {
                xOffset = 0.0f
                yOffset = -85.0f
            }
            "E" -> {
                xOffset = 85.0f
                yOffset = 0.0f
            }
            "S" -> {
                xOffset = 0.0f
                yOffset = 85.0f
            }
            "W" -> {
                xOffset = -85.0f
                yOffset = 0.0f
            }
        }
    }

    fun addCanvasItems(
            context: Context,
            bitmapCanvas: Bitmap,
            projectionType: ProjectionType,
            radarSite: String,
            citySize: Int
    ) {
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        listOf(
                RadarGeometryTypeEnum.CountyLines,
                RadarGeometryTypeEnum.StateLines,
                RadarGeometryTypeEnum.HwLines,
                RadarGeometryTypeEnum.LakeLines
        ).forEach {
            CanvasDraw.geometry(
                projectionType,
                bitmapCanvas,
                radarSite,
                it, RadarGeometry.dataByType[it]!!.lineData
            )
        }
        if (PolygonType.LOCDOT.pref) {
            CanvasDraw.locationDotForCurrentLocation(projectionType, bitmapCanvas, projectionNumbers)
        }
        if (PolygonType.WIND_BARB.pref) {
            CanvasWindbarbs.draw(context, projectionType, bitmapCanvas, radarSite, true, 5)
            CanvasWindbarbs.draw(context, projectionType, bitmapCanvas, radarSite, false, 5)
        }
        if (PolygonType.STI.pref) {
            try {
                CanvasStormInfo.draw(projectionType, bitmapCanvas, radarSite)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (PolygonType.MCD.pref) {
            listOf(PolygonType.MCD, PolygonType.WATCH, PolygonType.WATCH_TORNADO).forEach {
                CanvasDraw.mcd(projectionType, bitmapCanvas, projectionNumbers, it)
            }
        }
        if (PolygonType.MPD.pref) {
            CanvasDraw.mcd(projectionType, bitmapCanvas, projectionNumbers, PolygonType.MPD)
        }
        if (PolygonType.TST.pref) {
            CanvasDraw.warnings(projectionType, bitmapCanvas, projectionNumbers)
        }
        if (RadarPreferences.cities) {
            CanvasDraw.cities(projectionType, bitmapCanvas, projectionNumbers, citySize)
        }
    }
    //elys mod
    //for Conus Radar
    fun addCanvasConus(
        context: Context,
        bitmapCanvas: Bitmap,
        projectionType: ProjectionType,
        radarSite: String
    ) {
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        listOf(
            RadarGeometryTypeEnum.CountyLines,
            RadarGeometryTypeEnum.StateLines,
            RadarGeometryTypeEnum.HwLines,
            RadarGeometryTypeEnum.LakeLines
        ).forEach {
            CanvasDraw.geometry(
                projectionType,
                bitmapCanvas,
                radarSite,
                it, RadarGeometry.dataByType[it]!!.lineData
            )
        }
        if (PolygonType.LOCDOT.pref) {
                CanvasDraw.locationDotForCurrentLocation(projectionType, bitmapCanvas, projectionNumbers)
        }
        /*
        if (PolygonType.MCD.pref) {
            arrayOf(PolygonType.MCD, PolygonType.WATCH, PolygonType.WATCH_TORNADO).forEach {
                CanvasDraw.mcd(projectionType, bitmapCanvas, projectionNumbers, it)
            }
        }
        if (PolygonType.MPD.pref) {
            CanvasDraw.mcd(projectionType, bitmapCanvas, projectionNumbers, PolygonType.MPD)
        }
        if (PolygonType.TST.pref) {
            CanvasDraw.addWarnings(projectionType, bitmapCanvas, projectionNumbers)
        }
        */
        //if (RadarPreferences.cities) {
        //    UtilityCanvas.drawCitiesUS(projectionType, bitmapCanvas, projectionNumbers, citySize)
        //}
    }

}
