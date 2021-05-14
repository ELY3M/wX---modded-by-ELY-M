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
//modded by ELY M. 

package joshuatee.wx.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap

import joshuatee.wx.R
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.radar.GeometryData
import joshuatee.wx.radar.UtilityCanvasStormInfo
import joshuatee.wx.radar.UtilityCanvasWindbarbs
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

object UtilityCanvasMain {

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
            hwLineWidth: Int,
            citySize: Int,
            isInteractive: Boolean
    ) {
        // FIXME use this instead of radarSite and projectionType
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val highwayProvider = projectionType.isCanvas
        val stateLinesProvider = projectionType.isCanvas
        val countyProvider = projectionType === ProjectionType.WX_RENDER_48 || projectionType === ProjectionType.WX_RENDER
        val cityProvider = true
        val windBarbProvider = projectionType.isMercator
        val stormMotionProvider = projectionType.isMercator
        // if a widget or notification load the GEOM data in real-time
        val geometryData = if (isInteractive) {
            GeometryData(
                    GeographyType.HIGHWAYS.relativeBuffer, GeographyType.COUNTY_LINES.relativeBuffer,
                    GeographyType.STATE_LINES.relativeBuffer, GeographyType.LAKES.relativeBuffer
            )
        } else {
            getLocalGeometryData(context)
        }
        if (PolygonType.TST.pref) { UtilityCanvas.addWarnings(projectionType, bitmapCanvas, projectionNumbers) }
        if (GeographyType.HIGHWAYS.pref && highwayProvider) {
            UtilityCanvasGeneric.draw(
                    projectionType,
                    bitmapCanvas,
                    radarSite,
                    hwLineWidth,
                    GeographyType.HIGHWAYS,
                    geometryData.highways
            )
        }
        if (GeographyType.CITIES.pref && cityProvider) UtilityCanvas.drawCitiesUS(projectionType, bitmapCanvas, projectionNumbers, citySize)
        if (stateLinesProvider) {
            UtilityCanvasGeneric.draw(
                    projectionType,
                    bitmapCanvas,
                    radarSite,
                    1,
                    GeographyType.STATE_LINES,
                    geometryData.stateLines
            )
            if (GeographyType.LAKES.pref) {
                UtilityCanvasGeneric.draw(
                        projectionType,
                        bitmapCanvas,
                        radarSite,
                        hwLineWidth,
                        GeographyType.LAKES,
                        geometryData.lakes
                )
            }
        }
        if (countyProvider) {
            if (GeographyType.COUNTY_LINES.pref) {
                UtilityCanvasGeneric.draw(
                        projectionType,
                        bitmapCanvas,
                        radarSite,
                        hwLineWidth,
                        GeographyType.COUNTY_LINES,
                        geometryData.counties
                )
            }
        }
        if (PolygonType.LOCDOT.pref) UtilityCanvas.addLocationDotForCurrentLocation(projectionType, bitmapCanvas, projectionNumbers)
        if (PolygonType.WIND_BARB.pref && windBarbProvider) {
            UtilityCanvasWindbarbs.draw(context, projectionType, bitmapCanvas, radarSite, true, 5)
            UtilityCanvasWindbarbs.draw(context, projectionType, bitmapCanvas, radarSite, false, 5)
        }
        if (PolygonType.STI.pref && stormMotionProvider) {
            try {
                UtilityCanvasStormInfo.drawNexRadStormMotion(context, projectionType, bitmapCanvas, radarSite)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        if (PolygonType.MCD.pref) {
            arrayOf(PolygonType.MCD, PolygonType.WATCH, PolygonType.WATCH_TORNADO).forEach {
                UtilityCanvas.addMcd(projectionType, bitmapCanvas, projectionNumbers, it)
            }
        }
        if (PolygonType.MPD.pref) UtilityCanvas.addMcd(projectionType, bitmapCanvas, projectionNumbers, PolygonType.MPD)
    }
 
    //elys mod    
    //for Conus Radar
    fun addCanvasConus(
            context: Context,
            bitmapCanvas: Bitmap,
            projectionType: ProjectionType,
            radarSite: String,
            hwLineWidth: Int
    ) {

        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val stateLinesProvider = projectionType.isCanvas

        // if a widget or notification load the GEOM data in real-time
        val geometryData = getLocalGeometryData(context)

        //FIXME!!!!!!!  TEST ME!!!!!!//
        if (PolygonType.TST.pref) { UtilityCanvas.addWarnings(projectionType, bitmapCanvas, projectionNumbers) }

        if (stateLinesProvider) {
            UtilityCanvasGeneric.draw(
                    projectionType,
                    bitmapCanvas,
                    radarSite,
                    1,
                    GeographyType.STATE_LINES,
                    geometryData.stateLines
            )
            if (GeographyType.LAKES.pref) {
                UtilityCanvasGeneric.draw(
                        projectionType,
                        bitmapCanvas,
                        radarSite,
                        hwLineWidth,
                        GeographyType.LAKES,
                        geometryData.lakes
                )
            }
        }

        if (PolygonType.LOCDOT.pref) UtilityCanvas.addLocationDotForCurrentLocation(projectionType, bitmapCanvas, projectionNumbers)

        if (PolygonType.MCD.pref) {
            arrayOf(PolygonType.MCD, PolygonType.WATCH, PolygonType.WATCH_TORNADO).forEach {
                UtilityCanvas.addMcd(projectionType, bitmapCanvas, projectionNumbers, it)
            }
        }
        if (PolygonType.MPD.pref) UtilityCanvas.addMcd(projectionType, bitmapCanvas, projectionNumbers, PolygonType.MPD)

    }

    private fun getLocalGeometryData(context: Context): GeometryData {
        val canadaResId = R.raw.ca
        val mexicoResId = R.raw.mx
        val caCnt = 161792
        val mxCnt = 151552
        val stateLinesFileResId = R.raw.statev2
        var stateRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        val countState = 200000 // v3 205748
        var hwRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        val countHw = 862208 // on disk size 3448832 yields  862208
        val hwExtRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        val countHwExt = 770048 // on disk 3080192 yields 770048
        val hwExtFileResId = R.raw.hwv4ext // 2016_04_06
        var lakesRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        val countLakes = 503808 // was 14336 + 489476
        var countyRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        val countCounty = 212992 // file on disk is 851968, should be
        val hwFileResId = R.raw.hwv4
        val lakesFileResId = R.raw.lakesv3
        val countyFileResId = R.raw.county
        val fileIds = listOf(
                lakesFileResId,
                hwFileResId,
                countyFileResId,
                stateLinesFileResId,
                canadaResId,
                mexicoResId,
                hwExtFileResId
        )
        val countArr = listOf(countLakes, countHw, countCounty, countState, caCnt, mxCnt, countHwExt)
        try {
            for (type in listOf(
                    GeographyType.HIGHWAYS,
                    GeographyType.COUNTY_LINES,
                    GeographyType.STATE_LINES,
                    GeographyType.LAKES
            )) {
                when (type) {
                    GeographyType.STATE_LINES -> {
                        stateRelativeBuffer = ByteBuffer.allocateDirect(4 * countState)
                        stateRelativeBuffer.order(ByteOrder.nativeOrder())
                        stateRelativeBuffer.position(0)
                        listOf(3).forEach { loadBuffer(context.resources, fileIds[it], stateRelativeBuffer, countArr[it]) }
                    }
                    GeographyType.HIGHWAYS -> {
                        hwRelativeBuffer = ByteBuffer.allocateDirect(4 * countHw)
                        hwRelativeBuffer.order(ByteOrder.nativeOrder())
                        hwRelativeBuffer.position(0)
                        for (s in intArrayOf(1)) { loadBuffer(context.resources, fileIds[s], hwRelativeBuffer, countArr[s]) }
                    }
                    GeographyType.HIGHWAYS_EXTENDED -> {
                        for (s in intArrayOf(6)) { loadBuffer(context.resources, fileIds[s], hwExtRelativeBuffer, countArr[s]) }
                    }
                    GeographyType.LAKES -> {
                        lakesRelativeBuffer = ByteBuffer.allocateDirect(4 * countLakes)
                        lakesRelativeBuffer.order(ByteOrder.nativeOrder())
                        lakesRelativeBuffer.position(0)
                        val s = 0
                        loadBuffer(context.resources, fileIds[s], lakesRelativeBuffer, countArr[s])
                    }
                    GeographyType.COUNTY_LINES -> {
                        countyRelativeBuffer = ByteBuffer.allocateDirect(4 * countCounty)
                        countyRelativeBuffer.order(ByteOrder.nativeOrder())
                        countyRelativeBuffer.position(0)
                        val s = 2
                        loadBuffer(context.resources, fileIds[s], countyRelativeBuffer, countArr[s])
                    }
                    else -> {}
                }
            }
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return GeometryData(hwRelativeBuffer, countyRelativeBuffer, stateRelativeBuffer, lakesRelativeBuffer)
    }

    private fun loadBuffer(resources: Resources, fileId: Int, byteBuffer: ByteBuffer, count: Int) {
        try {
            val inputStream = resources.openRawResource(fileId)
            val dataInputStream = DataInputStream(BufferedInputStream(inputStream))
            (0 until count).forEach { _ -> byteBuffer.putFloat(dataInputStream.readFloat()) }
            dataInputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
    }
}
