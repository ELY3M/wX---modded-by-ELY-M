/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import joshuatee.wx.JNI
import joshuatee.wx.MyApplication
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType

open class ObjectOglBuffers() {

    var floatBuffer: ByteBuffer = ByteBuffer.allocate(0)
    var indexBuffer: ByteBuffer = ByteBuffer.allocate(0)
        private set
    var colorBuffer: ByteBuffer = ByteBuffer.allocate(0)
    var colorIntArray: List<Int> = listOf()
    var solidColorRed: Byte = 0
        private set
    var solidColorGreen: Byte = 0
        private set
    var solidColorBlue: Byte = 0
        private set
    var breakSize: Int = 30000
    var chunkCount: Int = 0
    var count: Int = 0
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
    var isInitialized: Boolean = false
        set(downloaded) {
            field = downloaded
            if (!isInitialized) {
                chunkCount = 0
            }
        }
    var lenInit: Float = 7.5f
    var xList: DoubleArray = DoubleArray(1)
    var yList: DoubleArray = DoubleArray(1)
    var triangleCount: Int = 0
    var scaleCutOff: Float = 0.0f
    var type: PolygonType = PolygonType.NONE
    var geotype: GeographyType = GeographyType.NONE

    constructor(type: PolygonType) : this() {
        this.type = type
    }

    constructor(geotype: GeographyType, scaleCutOff: Float) : this() {
        this.geotype = geotype
        this.scaleCutOff = scaleCutOff
    }

    constructor(type: PolygonType, scaleCutOff: Float) : this() {
        this.type = type
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
            UtilityLog.HandleException(e)
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
            UtilityLog.HandleException(e)
        }
    }

    fun putFloat(index: Int, newValue: Float) {
        floatBuffer.putFloat(index, newValue)
    }

    fun putIndex(newValue: Short) {
        indexBuffer.putShort(newValue)
    }

    fun putIndex(index: Int, newValue: Short) {
        indexBuffer.putShort(index, newValue)
    }

    fun putColor(b: Byte) {
        if (colorBuffer.position() < colorBuffer.capacity()) {
            try {
                colorBuffer.put(b)
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
    }

    val colorArray: ByteArray
        get() = byteArrayOf(solidColorRed, solidColorGreen, solidColorBlue)

    // provided a list of X1 Y2 X2 Y2 , break apart into seperate X,Y lists
    // used by TVS and HI
    fun setXYList(combinedLatLonList: List<Double>) {
        xList = DoubleArray(combinedLatLonList.size / 2)
        yList = DoubleArray(combinedLatLonList.size / 2)
        var j = 0
        while (j < combinedLatLonList.size) {
            xList[j / 2] = combinedLatLonList[j]
            yList[j / 2] = combinedLatLonList[j + 1]
            j += 2
        }
    }

    fun draw(pn: ProjectionNumbers) {
        when (type) {
            PolygonType.HI -> ObjectOglBuffers.redrawTriangleUp(this, pn)
            PolygonType.SPOTTER -> ObjectOglBuffers.redrawCircle(this, pn)
            PolygonType.TVS -> ObjectOglBuffers.redrawTriangleUp(this, pn)
            PolygonType.LOCDOT -> ObjectOglBuffers.redrawCircle(this, pn)
            PolygonType.WIND_BARB_CIRCLE -> ObjectOglBuffers.redrawCircleWithColor(this, pn)
            else -> ObjectOglBuffers.redrawTriangle(this, pn)
        }
    }

    companion object {
        // TVS
        private fun redrawTriangle(buffers: ObjectOglBuffers, pn: ProjectionNumbers) {
            if (!MyApplication.radarUseJni)
                UtilityWXOGLPerf.genTriangle(buffers, pn, buffers.xList, buffers.yList)
            else
                JNI.genTriangle(buffers.floatBuffer, buffers.indexBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(), pn.yCenter.toFloat(),
                        pn.oneDegreeScaleFactorFloat, buffers.xList, buffers.yList, buffers.count, buffers.lenInit, buffers.colorBuffer, buffers.colorArray)
        }

        // HI
        private fun redrawTriangleUp(buffers: ObjectOglBuffers, pn: ProjectionNumbers) {
            if (!MyApplication.radarUseJni)
                UtilityWXOGLPerf.genTriangleUp(buffers, pn, buffers.xList, buffers.yList)
            else
                JNI.genTriangleUp(buffers.floatBuffer, buffers.indexBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(), pn.yCenter.toFloat(),
                        pn.oneDegreeScaleFactorFloat, buffers.xList, buffers.yList, buffers.count, buffers.lenInit, buffers.colorBuffer, buffers.colorArray)
        }

        // LOCDOT, SPOTTER
        private fun redrawCircle(buffers: ObjectOglBuffers, pn: ProjectionNumbers) {
            if (!MyApplication.radarUseJni)
                UtilityWXOGLPerf.genCircle(buffers, pn, buffers.xList, buffers.yList)
            else
                JNI.genCircle(buffers.floatBuffer, buffers.indexBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(), pn.yCenter.toFloat(), pn.oneDegreeScaleFactorFloat,
                        buffers.xList, buffers.yList, buffers.count, buffers.lenInit, buffers.triangleCount, buffers.colorBuffer, buffers.colorArray)
        }

        // WIND BARB CIRCLE
        private fun redrawCircleWithColor(buffers: ObjectOglBuffers, pn: ProjectionNumbers) {
            if (!MyApplication.radarUseJni)
                UtilityWXOGLPerf.genCircleWithColor(buffers, pn, buffers.xList, buffers.yList)
            else
                JNI.genCircleWithColor(buffers.floatBuffer, buffers.indexBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(),
                        pn.yCenter.toFloat(), pn.oneDegreeScaleFactorFloat, buffers.xList, buffers.yList, buffers.count, buffers.lenInit,
                        buffers.triangleCount, buffers.colorBuffer, buffers.colorIntArray.toIntArray())
        }
    }
}


