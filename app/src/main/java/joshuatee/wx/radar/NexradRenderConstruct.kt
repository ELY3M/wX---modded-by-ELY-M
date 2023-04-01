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
//Modded by ELY M.

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color
import android.util.Log
import joshuatee.wx.Extensions.isEven
import joshuatee.wx.Jni
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityLog

class NexradRenderConstruct(val context: Context, val state: NexradRenderState, val data: NexradRenderData, val scaleLength: (Float) -> Float) {

    //
    // for types RadarGeometryTypeEnum
    //   initialize the ObjectOglBuffers object for things like color and size
    //
    // TODO FIXME force a way to regen color
    //
    fun geographic(buffers: OglBuffers, forceColorReset: Boolean = false) {
        if (!buffers.isInitialized) {
            buffers.count = RadarGeometry.dataByType[buffers.geotype]!!.count
            buffers.breakSize = 30000
            buffers.initialize(4 * buffers.count, 0, 3 * buffers.breakSize * 2, RadarGeometry.dataByType[buffers.geotype]!!.colorInt)
            // TODO FIXME should be?  3 * buffers.breakSize * 2
            if (RadarPreferences.useJni) {
                Jni.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            } else {
                NexradRenderUtilities.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            }
            buffers.isInitialized = true
        } else if (forceColorReset) {
            buffers.initializeColor(RadarGeometry.dataByType[buffers.geotype]!!.colorInt)
            buffers.setToPositionZero()
            buffers.breakSize = 30000
            if (RadarPreferences.useJni) {
                NexradRenderUtilities.colorGen(buffers.colorBuffer,  buffers.breakSize * 2, buffers.colorArray)
            } else {
                NexradRenderUtilities.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            }
        }
        if (!RadarPreferences.useJni) {
            NexradRenderUtilities.genMercator(RadarGeometry.dataByType[buffers.geotype]!!.lineData, buffers.floatBuffer, state.projectionNumbers, buffers.count)
        } else {
            Jni.genMercato(
                    RadarGeometry.dataByType[buffers.geotype]!!.lineData,
                    buffers.floatBuffer,
                    state.projectionNumbers.xFloat,
                    state.projectionNumbers.yFloat,
                    state.projectionNumbers.xCenter.toFloat(),
                    state.projectionNumbers.yCenter.toFloat(),
                    state.projectionNumbers.oneDegreeScaleFactorFloat,
                    buffers.count
            )
        }
        buffers.setToPositionZero()
    }

    @Synchronized fun warningLines(polygonWarningType: PolygonWarningType) {
        if (data.warningBuffers[polygonWarningType]!!.warningType!!.isEnabled) {
            lines(data.warningBuffers[polygonWarningType]!!)
        }
    }

    fun lines(buffers: OglBuffers) {
        buffers.isInitialized = false
        var points = listOf<Double>()
        when (buffers.type) {
            PolygonType.MCD, PolygonType.MPD, PolygonType.WATCH, PolygonType.WATCH_TORNADO -> points = Watch.add(state.projectionNumbers, buffers.type).toList()
            PolygonType.STI -> points = NexradLevel3StormInfo.decode(state.projectionNumbers).toList()
            else -> if (buffers.warningType != null) {
                points = Warnings.add(state.projectionNumbers, buffers.warningType!!).toList()
            }
        }
        buffers.breakSize = 15000
        buffers.chunkCount = 1
        val totalBinsGeneric = points.size / 4
        val remainder: Int
        if (totalBinsGeneric < buffers.breakSize) {
            buffers.breakSize = totalBinsGeneric
            remainder = buffers.breakSize
        } else { //if (buffers.breakSize > 0) {
            buffers.chunkCount = totalBinsGeneric / buffers.breakSize
            remainder = totalBinsGeneric - buffers.breakSize * buffers.chunkCount
            buffers.chunkCount = buffers.chunkCount + 1
        }
        // FIXME need a better solution then this hack
        if (buffers.warningType == null) {
            buffers.initialize(4 * 4 * totalBinsGeneric, 0, 3 * 4 * totalBinsGeneric, buffers.type.color)
        } else {
            buffers.initialize(4 * 4 * totalBinsGeneric, 0, 3 * 4 * totalBinsGeneric, buffers.warningType!!.color)
        }
        if (RadarPreferences.useJni) {
            Jni.colorGen(buffers.colorBuffer, 4 * totalBinsGeneric, buffers.colorArray)
        } else {
            NexradRenderUtilities.colorGen(buffers.colorBuffer, 4 * totalBinsGeneric, buffers.colorArray)
        }
        var vList = 0
        (0 until buffers.chunkCount).forEach {
            if (it == buffers.chunkCount - 1) {
                buffers.breakSize = remainder
            }
            for (notUsed in 0 until buffers.breakSize) {
                if (points.size > (vList + 3)) {
                    buffers.putFloat(points[vList].toFloat())
                    buffers.putFloat(points[vList + 1].toFloat() * -1.0f)
                    buffers.putFloat(points[vList + 2].toFloat())
                    buffers.putFloat(points[vList + 3].toFloat() * -1.0f)
                    vList += 4
                }
            }
        }
        buffers.isInitialized = true
    }

    private fun linesShort(buffers: OglBuffers, list: List<Double>) {
        val remainder: Int
        buffers.initialize(4 * 4 * list.size, 0, 3 * 4 * list.size, buffers.type.color)
        try {
            if (RadarPreferences.useJni) {
                Jni.colorGen(buffers.colorBuffer, 4 * list.size, buffers.colorArray)
            } else {
                NexradRenderUtilities.colorGen(buffers.colorBuffer, 4 * list.size, buffers.colorArray)
            }
        } catch (e: java.lang.Exception) { UtilityLog.handleException(e) }
        buffers.breakSize = 15000
        buffers.chunkCount = 1
        val totalBinsSti = list.size / 4
        if (totalBinsSti < buffers.breakSize) {
            buffers.breakSize = totalBinsSti
            remainder = buffers.breakSize
        } else {
            buffers.chunkCount = totalBinsSti / buffers.breakSize
            remainder = totalBinsSti - buffers.breakSize * buffers.chunkCount
            buffers.chunkCount += 1
        }
        var vList = 0
        (0 until buffers.chunkCount).forEach {
            if (it == buffers.chunkCount - 1) {
                buffers.breakSize = remainder
            }
            for (notUsed in 0 until buffers.breakSize) {
                buffers.putFloat(list[vList].toFloat())
                buffers.putFloat(list[vList + 1].toFloat() * -1.0f)
                buffers.putFloat(list[vList + 2].toFloat())
                buffers.putFloat(list[vList + 3].toFloat() * -1.0f)
                vList += 4
            }
        }
        buffers.isInitialized = true
    }

    private fun triangles(buffers: OglBuffers) {
        buffers.count = buffers.xList.size
        val count = buffers.count * buffers.triangleCount
        when (buffers.type) {
            PolygonType.LOCDOT, PolygonType.SPOTTER -> buffers.initialize(24 * count, 12 * count, 9 * count, buffers.type.color)
            else -> buffers.initialize(4 * 6 * buffers.count, 4 * 3 * buffers.count, 9 * buffers.count, buffers.type.color)
        }
        buffers.lenInit = scaleLength(buffers.lenInit)
        buffers.draw(state.projectionNumbers)
        buffers.isInitialized = true
    }

    //elys mod
    fun constructMarker(buffers: OglBuffers) {
        buffers.count = buffers.xList.size
        if (buffers.count == 0) {
            Log.i("constructMarker", "buffer count is 0")
            Log.i("constructMarker", "Not loading anything!")
            buffers.isInitialized = false
        } else {
            Log.i("constructMarker", "buffer count: " + buffers.count)
            buffers.triangleCount = 1
            buffers.initialize(
                    24 * buffers.count * buffers.triangleCount,
                    12 * buffers.count * buffers.triangleCount,
                    9 * buffers.count * buffers.triangleCount, 0)

            buffers.lenInit = 0f //scaleLength(buffers.lenInit)
            buffers.draw(state.projectionNumbers)
            buffers.isInitialized = true
        }
    }
	
    @Synchronized fun swoLines() {
        data.swoBuffers.isInitialized = false
        val hashSwo = SwoDayOne.polygonBy.toMap()
        var coordinates: DoubleArray
        val fSize = (0..4).filter { hashSwo[it] != null }.sumOf { hashSwo.getOrElse(it) { listOf() }.size }
        data.swoBuffers.breakSize = 15000
        data.swoBuffers.chunkCount = 1
        val totalBinsSwo = fSize / 4
        data.swoBuffers.initialize(4 * 4 * totalBinsSwo, 0, 3 * 2 * totalBinsSwo)
        if (totalBinsSwo < data.swoBuffers.breakSize) {
            data.swoBuffers.breakSize = totalBinsSwo
        } else {
            data.swoBuffers.chunkCount = totalBinsSwo / data.swoBuffers.breakSize
            data.swoBuffers.chunkCount = data.swoBuffers.chunkCount + 1
        }
        (0..4).forEach {
            if (hashSwo[it] != null) {
                for (j in hashSwo.getOrElse(it) { listOf() }.indices step 4) {
                    data.swoBuffers.putColor(Color.red(SwoDayOne.colors[it]).toByte())
                    data.swoBuffers.putColor(Color.green(SwoDayOne.colors[it]).toByte())
                    data.swoBuffers.putColor(Color.blue(SwoDayOne.colors[it]).toByte())
                    data.swoBuffers.putColor(Color.red(SwoDayOne.colors[it]).toByte())
                    data.swoBuffers.putColor(Color.green(SwoDayOne.colors[it]).toByte())
                    data.swoBuffers.putColor(Color.blue(SwoDayOne.colors[it]).toByte())
                    coordinates = Projection.computeMercatorNumbers(hashSwo.getOrElse(it) { listOf() }[j], (hashSwo.getOrElse(it) { listOf() }[j + 1] * -1.0f), state.projectionNumbers)
                    data.swoBuffers.putFloat(coordinates[0].toFloat())
                    data.swoBuffers.putFloat(coordinates[1].toFloat() * -1.0f)
                    coordinates = Projection.computeMercatorNumbers(hashSwo.getOrElse(it) { listOf() }[j + 2], (hashSwo.getOrElse(it) { listOf() }[j + 3] * -1.0f), state.projectionNumbers)
                    data.swoBuffers.putFloat(coordinates[0].toFloat())
                    data.swoBuffers.putFloat(coordinates[1].toFloat() * -1.0f)
                }
            }
        }
        data.swoBuffers.isInitialized = true
    }

    @Synchronized fun wpcFronts() {
        data.wpcFrontBuffersList.clear()
        data.wpcFrontPaints.clear()
        var coordinates: DoubleArray
        val fronts = WpcFronts.fronts.toList()
        repeat(fronts.size) {
            val buff = OglBuffers()
            buff.breakSize = 30000
            buff.chunkCount = 1
            data.wpcFrontBuffersList.add(buff)
        }
        fronts.indices.forEach { z ->
            val front = fronts[z]
            data.wpcFrontBuffersList[z].count = front.coordinates.size * 2
            data.wpcFrontBuffersList[z].initialize(4 * data.wpcFrontBuffersList[z].count, 0, 3 * data.wpcFrontBuffersList[z].count)
            data.wpcFrontBuffersList[z].isInitialized = true
            when (front.type) {
                FrontTypeEnum.COLD -> data.wpcFrontPaints.add(Color.rgb(0, 127, 255))
                FrontTypeEnum.WARM -> data.wpcFrontPaints.add(Color.rgb(255, 0, 0))
                FrontTypeEnum.STNRY -> data.wpcFrontPaints.add(Color.rgb(0, 127, 255))
                FrontTypeEnum.STNRY_WARM -> data.wpcFrontPaints.add(Color.rgb(255, 0, 0))
                FrontTypeEnum.OCFNT -> data.wpcFrontPaints.add(Color.rgb(255, 0, 255))
                FrontTypeEnum.TROF -> data.wpcFrontPaints.add(Color.rgb(254, 216, 177))
            }
            for (j in 0 until front.coordinates.size step 2) {
                if ( j < front.coordinates.size - 1) {
                    coordinates = Projection.computeMercatorNumbers(front.coordinates[j].lat, front.coordinates[j].lon, state.projectionNumbers)
                    data.wpcFrontBuffersList[z].putFloat(coordinates[0].toFloat())
                    data.wpcFrontBuffersList[z].putFloat((coordinates[1] * -1.0f).toFloat())
                    data.wpcFrontBuffersList[z].putColor(Color.red(data.wpcFrontPaints[z]).toByte())
                    data.wpcFrontBuffersList[z].putColor(Color.green(data.wpcFrontPaints[z]).toByte())
                    data.wpcFrontBuffersList[z].putColor(Color.blue(data.wpcFrontPaints[z]).toByte())
                    coordinates = Projection.computeMercatorNumbers(front.coordinates[j + 1].lat, front.coordinates[j + 1].lon, state.projectionNumbers)
                    data.wpcFrontBuffersList[z].putFloat(coordinates[0].toFloat())
                    data.wpcFrontBuffersList[z].putFloat((coordinates[1] * -1.0f).toFloat())
                    data.wpcFrontBuffersList[z].putColor(Color.red(data.wpcFrontPaints[z]).toByte())
                    data.wpcFrontBuffersList[z].putColor(Color.green(data.wpcFrontPaints[z]).toByte())
                    data.wpcFrontBuffersList[z].putColor(Color.blue(data.wpcFrontPaints[z]).toByte())
                }
            }
        }
    }

    //elys mod - custom location icon
   fun locationDot(locXCurrent: Double, locYCurrent: Double, archiveMode: Boolean) {
        var locationMarkers = mutableListOf<Double>()
        //elys mod
        if (RadarPreferences.locationDotFollowsGps) {
            data.locationDotBuffers.lenInit = 0f
        } else {
            data.locationDotBuffers.lenInit = PolygonType.LOCDOT.size
        }
        if (PolygonType.LOCDOT.pref) {
            locationMarkers = UtilityLocation.latLonAsDouble().toMutableList()
        }
        if (RadarPreferences.locationDotFollowsGps || archiveMode) {
            locationMarkers.add(locXCurrent)
            locationMarkers.add(locYCurrent * -1.0)
            state.gpsLatLon.lat = locXCurrent
            state.gpsLatLon.lon = locYCurrent * -1.0
        }
        data.locationDotBuffers.xList = locationMarkers.filterIndexed { index: Int, _: Double -> index.isEven() }.toDoubleArray()
        data.locationDotBuffers.yList = locationMarkers.filterIndexed { index: Int, _: Double -> !index.isEven() }.toDoubleArray()
        data.locationDotBuffers.triangleCount = 12
        triangles(data.locationDotBuffers)


        //elys mod
        //Custom location icon//
        data.locIconBuffers.triangleCount = 1 //was 36
        data.locIconBuffers.initialize(32 * data.locIconBuffers.triangleCount,
            8 * data.locIconBuffers.triangleCount,
            6 * data.locIconBuffers.triangleCount,
            RadarPreferences.colorLocdot)

        //elys mod
        //location bug
        data.locBugBuffers.triangleCount = 1 //was 36
        data.locBugBuffers.initialize(32 * data.locBugBuffers.triangleCount,
            8 * data.locBugBuffers.triangleCount,
            6 * data.locBugBuffers.triangleCount,
            RadarPreferences.colorLocdot)

        //elys mod
        /* not needed .. have custom location icon
        if (RadarPreferences.useJni) {
            Jni.colorGen(data.locCircleBuffers.colorBuffer, 2 * data.locCircleBuffers.triangleCount, data.locCircleBuffers.colorArray)
        } else {
            NexradRenderUtilities.colorGen(data.locCircleBuffers.colorBuffer, 2 * data.locCircleBuffers.triangleCount, data.locCircleBuffers.colorArray)
        }
        */


        if (RadarPreferences.locationDotFollowsGps) {
            data.locIconBuffers.lenInit = 0f
            val gpsCoordinates = Projection.computeMercatorNumbers(state.gpsLatLon.lat, state.gpsLatLon.lon, state.projectionNumbers)
            state.gpsLatLonTransformed[0] = -1.0f * gpsCoordinates[0].toFloat()
            state.gpsLatLonTransformed[1] = gpsCoordinates[1].toFloat()


            NexradRenderUtilities.genLocdot(data.locIconBuffers, state.projectionNumbers, state.gpsLatLon)



            //location bug//
            if (RadarPreferences.locdotBug) {
                data.locBugBuffers.lenInit = 0f
                NexradRenderUtilities.genLocdot(data.locBugBuffers, state.projectionNumbers, state.gpsLatLon)
            }

        }

        data.locationDotBuffers.isInitialized = true
        data.locIconBuffers.isInitialized = true
        data.locBugBuffers.isInitialized = true
    }

    //elys mod
    fun userPoints() {
        data.userPointsBuffers.lenInit = 0f
        UtilityUserPoints.userPointsData
        data.userPointsBuffers.xList = UtilityUserPoints.x
        data.userPointsBuffers.yList = UtilityUserPoints.y
        constructMarker(data.userPointsBuffers)
    }


    private fun wBCircle(paneNumber: Int) {
        val wbCircleBuffers = data.wbCircleBuffers
        wbCircleBuffers.lenInit = PolygonType.WIND_BARB_CIRCLE.size
        wbCircleBuffers.xList = Metar.data[paneNumber].x
        wbCircleBuffers.yList = Metar.data[paneNumber].y
        wbCircleBuffers.colorIntArray = Metar.data[paneNumber].obsArrAviationColor
        wbCircleBuffers.count = wbCircleBuffers.xList.size
        wbCircleBuffers.triangleCount = 6
        val count = wbCircleBuffers.count * wbCircleBuffers.triangleCount
        wbCircleBuffers.initialize(24 * count, 12 * count, 9 * count)
        wbCircleBuffers.lenInit = scaleLength(wbCircleBuffers.lenInit)
        wbCircleBuffers.draw(state.projectionNumbers)
        wbCircleBuffers.isInitialized = true
    }


    ///I fucking hate this!!!!!
    /*
    fun tvs(rid: String, indexString: String, tvsBuffers: ObjectOglBuffers) {
        tvsBuffers.lenInit = PolygonType.TVS.size
        tvsBuffers.setXYList(WXGLNexradLevel3TVS.decodeAndPlot(context, rid, indexString))
        triangles(tvsBuffers)
    }
*/

    //elys mod - custom TVS icon
    fun tvs() {
        data.tvsBuffers.lenInit = 0f
        data.tvsBuffers.setXYList(NexradLevel3TVS.decode(state.rid))
        constructMarker(data.tvsBuffers)
    }


    //I fucking hate this!!!!
/*
    fun hailIndex(rid: String, indexString: String, hiBuffers: ObjectOglBuffers) {
        hiBuffers.lenInit = PolygonType.HI.size
        hiBuffers.setXYList(WXGLNexradLevel3HailIndex.decodeAndPlot(context, rid, indexString))
        triangles(hiBuffers)
    }
*/

    //elys mod - hailmod
    //hiBuffersList
    @Synchronized fun hailIndex() {
        val hailList = NexradLevel3HailIndex.decode(state.rid)
        data.hiBuffersList = mutableListOf()
        hailList.indices.forEach {
            val buff = OglBuffers()
            buff.isInitialized = true
            buff.lenInit = 0f
            val hailSizeNumber = hailList[it].hailSizeNumber
            buff.hailIcon = hailList[it].hailIcon
            buff.hailSizeNumber = hailList[it].hailSizeNumber
            val stormList = mutableListOf<Double>()
            stormList.add(hailList[it].latD)
            stormList.add(hailList[it].lonD)
            buff.setXYList(stormList)


            if (hailSizeNumber in 0.0..0.24) {
                Log.i("hailconstloop", "it: "+it+" hail05")
                buff.hailIcon = "hail05.png"
            }
            if (hailSizeNumber in 0.24..0.98) {
                Log.i("hailconstloop", "it: "+it+" hail0")
                buff.hailIcon = "hail0.png"
            }
            if (hailSizeNumber in 0.99..1.98) {
                Log.i("hailconstloop", "it: "+it+" hail1")
                buff.hailIcon = "hail1.png"
            }
            if (hailSizeNumber in 1.99..2.98) {
                Log.i("hailconstloop", "it: "+it+" hail2")
                buff.hailIcon = "hail2.png"
            }
            if (hailSizeNumber in 2.99..3.98) {
                Log.i("hailconstloop", "it: "+it+" hail3")
                buff.hailIcon = "hail3.png"
            }
            if (hailSizeNumber in 3.99..4.98) {
                Log.i("hailconstloop", "it: "+it+" hail4")
                buff.hailIcon = "hail4.png"
            }
            //big hail --- only use 1 icon for any hail over 5 inch
            if (hailSizeNumber in 4.99..99.99) {
                Log.i("hailconstloop", "it: "+it+" hailBig")
                buff.hailIcon = "hailbig.png"
            }

            data.hiBuffersList.add(buff)
            constructMarker(buff)
        }

    }
    fun spotters() {
        val spotterBuffers = data.spotterBuffers
        spotterBuffers.isInitialized = false
        spotterBuffers.lenInit = PolygonType.SPOTTER.size
        spotterBuffers.triangleCount = 6
        UtilitySpotter.get(context)
        spotterBuffers.xList = UtilitySpotter.x
        spotterBuffers.yList = UtilitySpotter.y
        triangles(spotterBuffers)
    }

    fun windBarbs() {
        linesShort(data.wbBuffers, NexradLevel3WindBarbs.decodeAndPlot(state.rid, state.projectionType, false, state.paneNumber))
        linesShort(data.wbGustsBuffers, NexradLevel3WindBarbs.decodeAndPlot(state.rid, state.projectionType, true, state.paneNumber))
        wBCircle(state.paneNumber)
    }

    //elys mod - Conus Radar
    fun ConusRadar() {
        data.conusRadarBuffers.lenInit = 0f
        data.conusRadarBuffers.isInitialized = true
    }

    //fun deconstructConusRadar() {
    //    data.conusRadarBuffers.isInitialized = false
    //}

    //elys mod - hail icon mod
    fun setHiInit(hiInit: Boolean) {
        data.hiBuffersList.forEach { it.isInitialized = hiInit }
    }
    //elys mod - User Points System
    fun setUserPointsInit(userPointsInit: Boolean) {
        data.userPointsBuffers.isInitialized = userPointsInit
    } 

    fun setTvsInit(tvsInit: Boolean) {
        data.tvsBuffers.isInitialized = tvsInit
    }

    fun setChunkCountSti(chunkCountSti: Int) {
        data.stiBuffers.chunkCount = chunkCountSti
    }
}
