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

package joshuatee.wx.radar

import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder
import joshuatee.wx.Jni
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.RadarPreferences

open class ObjectOglBuffers() {

    var floatBuffer: ByteBuffer = ByteBuffer.allocate(0)
    var indexBuffer: ByteBuffer = ByteBuffer.allocate(0)
        private set
    var colorBuffer: ByteBuffer = ByteBuffer.allocate(0)
    var colorIntArray = listOf<Int>()
    var solidColorRed: Byte = 0
        private set
    var solidColorGreen: Byte = 0
        private set
    var solidColorBlue: Byte = 0
        private set
    var breakSize = 30000
    var chunkCount = 0
    var count = 0
        set(count) {
            field = count
            chunkCount = 1
            val totalBinsCounty = count / 4
            if (totalBinsCounty < breakSize) {
                breakSize = totalBinsCounty
            } else if (breakSize != 0) {
                chunkCount = totalBinsCounty / breakSize
                chunkCount += 1
            }
        }
    var isInitialized = false
        set(downloaded) {
            field = downloaded
            if (!isInitialized) chunkCount = 0
        }
    var lenInit = 7.5f
    var xList = DoubleArray(1)
    var yList = DoubleArray(1)
    var triangleCount = 0
    var scaleCutOff = 0.0f
    var type = PolygonType.NONE
    var geotype = GeographyType.NONE
    var warningType: ObjectPolygonWarning? = null

    constructor(type: PolygonType) : this() { this.type = type }

    constructor(warningType: ObjectPolygonWarning) : this() { this.warningType = warningType }

    constructor(geotype: PolygonType, scaleCutOff: Float) : this() {
        this.type = geotype
        this.scaleCutOff = scaleCutOff
    }

    constructor(geotype: GeographyType, scaleCutOff: Float) : this() {
        this.geotype = geotype
        this.scaleCutOff = scaleCutOff
    }

    fun initialize(floatCount: Int, indexCount: Int, colorCount: Int) {
        try {
            floatBuffer = ByteBuffer.allocateDirect(floatCount)
            floatBuffer.order(ByteOrder.nativeOrder())
            indexBuffer = ByteBuffer.allocateDirect(indexCount)
            indexBuffer.order(ByteOrder.nativeOrder())
            colorBuffer = ByteBuffer.allocateDirect(colorCount)
            colorBuffer.order(ByteOrder.nativeOrder())
            setToPositionZero()
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
    }

    fun initialize(floatCount: Int, indexCount: Int, colorCount: Int, solidColor: Int) {
        this.initialize(floatCount, indexCount, colorCount)
        solidColorRed = Color.red(solidColor).toByte()
        solidColorGreen = Color.green(solidColor).toByte()
        solidColorBlue = Color.blue(solidColor).toByte()
    }

    open fun setToPositionZero() {
        floatBuffer.position(0)
        indexBuffer.position(0)
        colorBuffer.position(0)
    }

    fun putFloat(newValue: Float) {
        try {
            floatBuffer.putFloat(newValue)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun putFloat(index: Int, newValue: Float) { floatBuffer.putFloat(index, newValue) }

    fun putIndex(newValue: Short) { indexBuffer.putShort(newValue) }

    fun putIndex(index: Int, newValue: Short) { indexBuffer.putShort(index, newValue) }

    fun putColor(b: Byte) {
        if (colorBuffer.position() < colorBuffer.capacity()) {
            try {
                colorBuffer.put(b)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
    }

    val colorArray: ByteArray
        get() = byteArrayOf(solidColorRed, solidColorGreen, solidColorBlue)

    // provided a list of X1 Y2 X2 Y2 , break apart into separate X,Y lists
    // used by TVS and HI
    fun setXYList(combinedLatLonList: List<Double>) {
        xList = DoubleArray(combinedLatLonList.size / 2)
        yList = DoubleArray(combinedLatLonList.size / 2)
        for (j in combinedLatLonList.indices step 2) {
            xList[j / 2] = combinedLatLonList[j]
            yList[j / 2] = combinedLatLonList[j + 1]
        }
    }

    fun draw(projectionNumbers: ProjectionNumbers) {
        when (type) {
            PolygonType.HI -> redrawTriangleUp(this, projectionNumbers)
            PolygonType.SPOTTER -> redrawCircle(this, projectionNumbers)
            PolygonType.TVS -> redrawTriangleUp(this, projectionNumbers)
            PolygonType.LOCDOT -> redrawCircle(this, projectionNumbers)
            PolygonType.WIND_BARB_CIRCLE -> redrawCircleWithColor(this, projectionNumbers)
            else -> redrawTriangle(this, projectionNumbers)
        }
    }

    companion object {
        // TVS
        private fun redrawTriangle(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
            if (!RadarPreferences.radarUseJni) {
                UtilityWXOGLPerf.genTriangle(buffers, projectionNumbers)
            } else {
                Jni.genTriangle(
                        buffers.floatBuffer,
                        buffers.indexBuffer,
                        projectionNumbers.xFloat,
                        projectionNumbers.yFloat,
                        projectionNumbers.xCenter.toFloat(),
                        projectionNumbers.yCenter.toFloat(),
                        projectionNumbers.oneDegreeScaleFactorFloat,
                        buffers.xList,
                        buffers.yList,
                        buffers.count,
                        buffers.lenInit,
                        buffers.colorBuffer,
                        buffers.colorArray
                )
            }
        }

        // HI
        private fun redrawTriangleUp(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
            if (!RadarPreferences.radarUseJni) {
                UtilityWXOGLPerf.genTriangleUp(buffers, projectionNumbers)
            } else {
                Jni.genTriangleUp(
                        buffers.floatBuffer,
                        buffers.indexBuffer,
                        projectionNumbers.xFloat,
                        projectionNumbers.yFloat,
                        projectionNumbers.xCenter.toFloat(),
                        projectionNumbers.yCenter.toFloat(),
                        projectionNumbers.oneDegreeScaleFactorFloat,
                        buffers.xList,
                        buffers.yList,
                        buffers.count,
                        buffers.lenInit,
                        buffers.colorBuffer,
                        buffers.colorArray
                )
            }
        }

        // LOCDOT, SPOTTER
        private fun redrawCircle(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
            if (!RadarPreferences.radarUseJni) {
                UtilityWXOGLPerf.genCircle(buffers, projectionNumbers)
            } else {
                Jni.genCircle(
                        buffers.floatBuffer,
                        buffers.indexBuffer,
                        projectionNumbers.xFloat,
                        projectionNumbers.yFloat,
                        projectionNumbers.xCenter.toFloat(),
                        projectionNumbers.yCenter.toFloat(),
                        projectionNumbers.oneDegreeScaleFactorFloat,
                        buffers.xList,
                        buffers.yList,
                        buffers.count,
                        buffers.lenInit,
                        buffers.triangleCount,
                        buffers.colorBuffer,
                        buffers.colorArray
                )
            }
        }

        // WIND BARB CIRCLE
        private fun redrawCircleWithColor(buffers: ObjectOglBuffers, projectionNumbers: ProjectionNumbers) {
            if (!RadarPreferences.radarUseJni) {
                UtilityWXOGLPerf.genCircleWithColor(buffers, projectionNumbers)
            } else {
                Jni.genCircleWithColor(
                        buffers.floatBuffer,
                        buffers.indexBuffer,
                        projectionNumbers.xFloat,
                        projectionNumbers.yFloat,
                        projectionNumbers.xCenter.toFloat(),
                        projectionNumbers.yCenter.toFloat(),
                        projectionNumbers.oneDegreeScaleFactorFloat,
                        buffers.xList,
                        buffers.yList,
                        buffers.count,
                        buffers.lenInit,
                        buffers.triangleCount,
                        buffers.colorBuffer,
                        buffers.colorIntArray.toIntArray()
                )
            }
        }
    }
}
