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

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import joshuatee.wx.R
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
            citySize: Int,
    ) {
        // FIXME use this instead of radarSite and projectionType
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val countyProvider = projectionType === ProjectionType.WX_RENDER_48 || projectionType === ProjectionType.WX_RENDER
        val cityProvider = true
        val windBarbProvider = projectionType.isMercator
        val stormMotionProvider = projectionType.isMercator
        val geometryData = getLocalGeometryData(context)
        if (PolygonType.TST.pref) {
            CanvasDraw.warnings(projectionType, bitmapCanvas, projectionNumbers)
        }
        if (RadarGeometry.dataByType[RadarGeometryTypeEnum.HwLines]!!.isEnabled) {
            CanvasDraw.geometry(
                    projectionType,
                    bitmapCanvas,
                    radarSite,
                    RadarGeometryTypeEnum.HwLines,
                    geometryData.highways
            )
        }
        if (RadarPreferences.cities && cityProvider) {
            CanvasDraw.cities(projectionType, bitmapCanvas, projectionNumbers, citySize)
        }
        CanvasDraw.geometry(
                projectionType,
                bitmapCanvas,
                radarSite,
                RadarGeometryTypeEnum.StateLines,
                geometryData.stateLines
        )
        if (RadarGeometry.dataByType[RadarGeometryTypeEnum.LakeLines]!!.isEnabled) {
            CanvasDraw.geometry(
                    projectionType,
                    bitmapCanvas,
                    radarSite,
                    RadarGeometryTypeEnum.LakeLines,
                    geometryData.lakes
            )
        }
        if (countyProvider) {
            if (RadarGeometry.dataByType[RadarGeometryTypeEnum.CountyLines]!!.isEnabled) {
                CanvasDraw.geometry(
                        projectionType,
                        bitmapCanvas,
                        radarSite,
                        RadarGeometryTypeEnum.CountyLines,
                        geometryData.counties
                )
            }
        }
        if (PolygonType.LOCDOT.pref) {
            CanvasDraw.locationDotForCurrentLocation(projectionType, bitmapCanvas, projectionNumbers)
        }
        if (PolygonType.WIND_BARB.pref && windBarbProvider) {
            CanvasWindbarbs.draw(context, projectionType, bitmapCanvas, radarSite, true, 5)
            CanvasWindbarbs.draw(context, projectionType, bitmapCanvas, radarSite, false, 5)
        }
        if (PolygonType.STI.pref && stormMotionProvider) {
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
                    RadarGeometryTypeEnum.HwLines,
                    RadarGeometryTypeEnum.CountyLines,
                    RadarGeometryTypeEnum.StateLines,
                    RadarGeometryTypeEnum.LakeLines
            )) {
                when (type) {
                    RadarGeometryTypeEnum.StateLines -> {
                        stateRelativeBuffer = ByteBuffer.allocateDirect(4 * countState)
                        stateRelativeBuffer.order(ByteOrder.nativeOrder())
                        stateRelativeBuffer.position(0)
                        listOf(3).forEach { loadBuffer(context.resources, fileIds[it], stateRelativeBuffer, countArr[it]) }
                    }

                    RadarGeometryTypeEnum.HwLines -> {
                        hwRelativeBuffer = ByteBuffer.allocateDirect(4 * countHw)
                        hwRelativeBuffer.order(ByteOrder.nativeOrder())
                        hwRelativeBuffer.position(0)
                        for (s in intArrayOf(1)) {
                            loadBuffer(context.resources, fileIds[s], hwRelativeBuffer, countArr[s])
                        }
                    }

                    RadarGeometryTypeEnum.LakeLines -> {
                        lakesRelativeBuffer = ByteBuffer.allocateDirect(4 * countLakes)
                        lakesRelativeBuffer.order(ByteOrder.nativeOrder())
                        lakesRelativeBuffer.position(0)
                        val s = 0
                        loadBuffer(context.resources, fileIds[s], lakesRelativeBuffer, countArr[s])
                    }

                    RadarGeometryTypeEnum.CountyLines -> {
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
