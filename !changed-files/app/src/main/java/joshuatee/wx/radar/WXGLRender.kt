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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.graphics.Color
import android.opengl.GLSurfaceView.Renderer
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log

import joshuatee.wx.JNI
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

class WXGLRender(private val context: Context) : Renderer {

    // The is the OpenGL rendering engine that is used on the main screen and the main radar interface
    // The goal is to be highly performant and configurable as such this module relies on C code accessed via JNI extensively
    // Java can also be used in set in settings->radar

    companion object {
        var ridGlobal: String = ""
            private set
        var positionXGlobal: Float = 0f
            private set
        var positionYGlobal: Float = 0f
            private set
        const val ortIntGlobal: Int = 400
        var oneDegreeScaleFactorGlobal: Float = 0.0f
            private set
    }

    // this string is normally no string but for dual pane will be set to either 1 or 2 to differentiate timestamps
    var radarStatusStr: String = ""
    var idxStr: String = "0"
    private val mtrxProjection = FloatArray(16)
    private val mtrxView = FloatArray(16)
    private var mtrxProjectionAndView = FloatArray(16)
    var ridNewList: List<RID> = listOf()
    private var radarChunkCnt = 0
    private var lineCnt = 0
    private val breakSizeLine = 30000
    private val mtrxProjectionAndViewOrig = FloatArray(16)
    private var triangleIndexBuffer: ByteBuffer = ByteBuffer.allocate(0)
    private var lineIndexBuffer: ByteBuffer = ByteBuffer.allocate(0)
    private var gpsX = 0.toDouble()
    private var gpsY = 0.toDouble()
    private val radarBuffers = ObjectOglRadarBuffers(context, MyApplication.nexradRadarBackgroundColor)
    private val spotterBuffers = ObjectOglBuffers(PolygonType.SPOTTER, 0.30f)
    private val stateLineBuffers = ObjectOglBuffers(GeographyType.STATE_LINES, 0.0f)
    private val countyLineBuffers = ObjectOglBuffers(GeographyType.COUNTY_LINES, 0.75f)
    private val hwBuffers = ObjectOglBuffers(GeographyType.HIGHWAYS, 0.45f)
    private val hwExtBuffers = ObjectOglBuffers(GeographyType.HIGHWAYS_EXTENDED, 3.00f)
    private val lakeBuffers = ObjectOglBuffers(GeographyType.LAKES, 0.30f)
    private val stiBuffers = ObjectOglBuffers(PolygonType.STI, 0.0f)
    private val wbBuffers = ObjectOglBuffers(PolygonType.WIND_BARB, 0.30f)
    private val wbGustsBuffers = ObjectOglBuffers(PolygonType.WIND_BARB_GUSTS, 0.30f)
    private val mpdBuffers = ObjectOglBuffers(PolygonType.MPD)
    private val hiBuffers = ObjectOglBuffers(PolygonType.HI, 0.30f)
    private val tvsBuffers = ObjectOglBuffers(PolygonType.TVS, 0.30f)
    private val warningSpsBuffers = ObjectOglBuffers(PolygonType.SPS)
    private val warningSmwBuffers = ObjectOglBuffers(PolygonType.SMW)
    private val warningSvsBuffers = ObjectOglBuffers(PolygonType.SVS)
    private val warningFfwBuffers = ObjectOglBuffers(PolygonType.FFW)
    private val warningTstBuffers = ObjectOglBuffers(PolygonType.TST)
    private val warningTorBuffers = ObjectOglBuffers(PolygonType.TOR)
    private val watchBuffers = ObjectOglBuffers(PolygonType.WATCH)
    private val watchTornadoBuffers = ObjectOglBuffers(PolygonType.WATCH_TORNADO)
    private val mcdBuffers = ObjectOglBuffers(PolygonType.MCD)
    private val swoBuffers = ObjectOglBuffers()
    private val locdotBuffers = ObjectOglBuffers(PolygonType.LOCDOT)
    private val locCircleBuffers = ObjectOglBuffers()
    private val wbCircleBuffers = ObjectOglBuffers(PolygonType.WIND_BARB_CIRCLE, 0.30f)
    private val colorSwo = IntArray(5)
    private var breakSize15 = 15000
    private val breakSizeRadar = 15000
    private var mPositionHandle = 0
    private var colorHandle = 0
    private var tdwr = false
    private var chunkCount = 0
    private var totalBins = 0
    private var totalBinsOgl = 0
    var zoom: Float = 1.0f
        set(scale) {
            field = scale

            listOf(locdotBuffers, hiBuffers, spotterBuffers, tvsBuffers, wbCircleBuffers).forEach {
                if (it.isInitialized) {
                    it.lenInit = it.type.size
                    it.lenInit = scaleLength(it.lenInit)
                    it.draw(pn)
                }
            }

            if (locdotBuffers.isInitialized && MyApplication.locdotFollowsGps) {
                locCircleBuffers.lenInit = locdotBuffers.lenInit
                UtilityWXOGLPerf.genCircleLocdot(locCircleBuffers, pn, gpsX, gpsY)
            }
        }
    private var mSurfaceRatio = 0f
    var x: Float = 0f
        set(x) {
            field = x
            positionXGlobal = x
        }
    var y: Float = 0f
        set(y) {
            field = y
            positionYGlobal = y
        }
    var rid: String = ""
        set(rid) {
            field = rid
            ridGlobal = rid
        }
    private var prod = "N0Q"
    private val defaultLineWidth = 2.0f
    private var warnLineWidth = 2.0f
    private var watmcdLineWidth = 2.0f
    private var ridPrefixGlobal = ""
    private var bgColorFRed = 0.0f
    private var bgColorFGreen = 0.0f
    private var bgColorFBlue = 0.0f
    val ortInt: Int = 400
    private val provider = ProjectionType.WX_OGL
    private val rdL2 = WXGLNexradLevel2()
    val radarL3Object: WXGLNexradLevel3 = WXGLNexradLevel3()
    val rdDownload: WXGLDownload = WXGLDownload()
    private var pn = ProjectionNumbers()
    var product: String
        get() = prod
        set(value) {
            prod = value
        }

    init {
        bgColorFRed = Color.red(MyApplication.nexradRadarBackgroundColor) / 255.0f
        bgColorFGreen = Color.green(MyApplication.nexradRadarBackgroundColor) / 255.0f
        bgColorFBlue = Color.blue(MyApplication.nexradRadarBackgroundColor) / 255.0f
        warnLineWidth = MyApplication.radarWarnLinesize.toFloat()
        watmcdLineWidth = MyApplication.radarWatmcdLinesize.toFloat()
        try {
            triangleIndexBuffer = ByteBuffer.allocateDirect(12 * breakSize15)
            lineIndexBuffer = ByteBuffer.allocateDirect(4 * breakSizeLine)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        triangleIndexBuffer.order(ByteOrder.nativeOrder())
        triangleIndexBuffer.position(0)
        lineIndexBuffer.order(ByteOrder.nativeOrder())
        lineIndexBuffer.position(0)
        if (!MyApplication.radarUseJni) {
            UtilityWXOGLPerf.genIndex(triangleIndexBuffer, breakSize15, breakSize15)
            UtilityWXOGLPerf.genIndexLine(lineIndexBuffer, breakSizeLine * 4, breakSizeLine * 2)
        } else {
            JNI.genIndex(triangleIndexBuffer, breakSize15, breakSize15)
            JNI.genIndexLine(lineIndexBuffer, breakSizeLine * 4, breakSizeLine * 2)
        }
    }

    fun initGEOM() {
        totalBins = 0
        if (prod == "TV0" || prod == "TZL") {
            tdwr = true
            val oldRid = this.rid
            if (this.rid == "") {
                this.rid = oldRid
                tdwr = false
                prod = "N0Q"
            }
        }
        pn = ProjectionNumbers(context, this.rid, provider)
        oneDegreeScaleFactorGlobal = pn.oneDegreeScaleFactorFloat
    }

    // final arg is whether or not to perform decompression
    fun constructPolygons(fileName: String, urlStr: String, performDecomp: Boolean) {
        radarBuffers.fn = fileName
        totalBins = 0
        // added to allow animations to skip a frame and continue
        if (product == "TV0" || product == "TZL") {
            tdwr = true
            val oldRid = this.rid
            if (this.rid == "") {
                this.rid = oldRid
                tdwr = false
                product = "N0Q"
            }
        }
        // if fn is empty string then we need to fetch the radar file
        // if set, its part of an anim sequence
        if (radarBuffers.fn == "") {
            ridPrefixGlobal = rdDownload.getRadarFile(context, urlStr, this.rid, prod, idxStr, tdwr)
            radarBuffers.fn = if (!product.contains("L2")) {
                val l3BaseFn = "nids"
                l3BaseFn + idxStr
            } else {
                "l2$idxStr"
            }
        }
        radarBuffers.setProductCodeFromString(product)
        try {
            when {
                product.contains("L2") -> {
                    rdL2.decocodeAndPlotNexradL2(context, radarBuffers.fn, prod, radarStatusStr, idxStr, performDecomp)
                    radarBuffers.extractL2Data(rdL2)
                }
                product.contains("N0S") -> {
                    radarL3Object.decocodeAndPlotNexradLevel3FourBit(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
                else -> {
                    radarL3Object.decocodeAndPlotNexradDigital(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        if (radarBuffers.numRangeBins == 0) {
            radarBuffers.numRangeBins = 460
            radarBuffers.numberOfRadials = 360
        }
        radarBuffers.initialize()
        radarBuffers.setToPositionZero()
        val objColPal: ObjectColorPalette = if (MyApplication.colorMap.containsKey(radarBuffers.productCode.toInt())) {
            MyApplication.colorMap[radarBuffers.productCode.toInt()]!!
        } else {
            MyApplication.colorMap[94]!!
        }
        val cR = objColPal.redValues
        val cG = objColPal.greenValues
        val cB = objColPal.blueValues
        try {
            if (!product.contains("L2")) {
                totalBins = if (radarBuffers.productCode != 56.toShort()) {
                    if (!MyApplication.radarUseJni)
                        UtilityWXOGLPerf.decode8BitAndGenRadials(context, radarBuffers)
                    else {
                        JNI.decode8BitAndGenRadials(UtilityIO.getFilePath(context, radarBuffers.fn), radarL3Object.seekStart,
                                radarL3Object.compressedFileSize, radarL3Object.iBuff, radarL3Object.oBuff, radarBuffers.floatBuffer, radarBuffers.colorBuffer,
                                radarBuffers.binSize, Color.red(radarBuffers.bgColor).toByte(), Color.green(radarBuffers.bgColor).toByte(), Color.blue(radarBuffers.bgColor).toByte(), cR, cG, cB)
                    }
                } else {
                    UtilityWXOGLPerf.genRadials(radarBuffers, radarL3Object.binWord, radarL3Object.radialStart)
                }
            } else {
                rdL2.binWord.position(0)
                totalBins = if (MyApplication.radarUseJni)
                    JNI.level2GenRadials(radarBuffers.floatBuffer, radarBuffers.colorBuffer, rdL2.binWord, rdL2.radialStartAngle,
                            radarBuffers.numberOfRadials, radarBuffers.numRangeBins, radarBuffers.binSize, radarBuffers.bgColor, cR, cG, cB, radarBuffers.productCode.toInt())
                else
                    UtilityWXOGLPerf.genRadials(radarBuffers, rdL2.binWord, rdL2.radialStartAngle)
            } // level 2 , level 3 check
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        breakSize15 = 15000
        chunkCount = 1
        if (totalBins < breakSize15) {
            breakSize15 = totalBins
        } else {
            chunkCount = totalBins / breakSize15
            chunkCount += 1
        }
        radarBuffers.setToPositionZero()
        tdwr = false
        totalBinsOgl = totalBins
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(bgColorFRed, bgColorFGreen, bgColorFBlue, 1f)
        OpenGLShader.sp_SolidColor = GLES20.glCreateProgram()
        GLES20.glAttachShader(OpenGLShader.sp_SolidColor, OpenGLShader.loadShader(GLES20.GL_VERTEX_SHADER, OpenGLShader.vs_SolidColor))
        GLES20.glAttachShader(OpenGLShader.sp_SolidColor, OpenGLShader.loadShader(GLES20.GL_FRAGMENT_SHADER, OpenGLShader.fs_SolidColor))
        GLES20.glLinkProgram(OpenGLShader.sp_SolidColor)
        GLES20.glUseProgram(OpenGLShader.sp_SolidColor)
        val vertexShaderUniform = OpenGLShaderUniform.loadShader(GLES20.GL_VERTEX_SHADER, OpenGLShaderUniform.vs_SolidColorUnfiform)
        val fragmentShaderUniform = OpenGLShaderUniform.loadShader(GLES20.GL_FRAGMENT_SHADER, OpenGLShaderUniform.fs_SolidColorUnfiform)
        OpenGLShaderUniform.sp_SolidColorUniform = GLES20.glCreateProgram()
        GLES20.glAttachShader(OpenGLShaderUniform.sp_SolidColorUniform, vertexShaderUniform)
        GLES20.glAttachShader(OpenGLShaderUniform.sp_SolidColorUniform, fragmentShaderUniform)
        GLES20.glLinkProgram(OpenGLShaderUniform.sp_SolidColorUniform)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glUseProgram(OpenGLShader.sp_SolidColor)
        GLES20.glClearColor(bgColorFRed, bgColorFGreen, bgColorFBlue, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_SolidColor, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_SolidColor, "a_Color")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        // required for color on VBO basis
        GLES20.glEnableVertexAttribArray(colorHandle)
        mtrxProjectionAndView = mtrxProjectionAndViewOrig
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0)
        Matrix.translateM(mtrxProjectionAndView, 0, x, y, 0f)
        Matrix.scaleM(mtrxProjectionAndView, 0, zoom, zoom, 1f)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_SolidColor, "uMVPMatrix"), 1, false, mtrxProjectionAndView, 0)
        (0 until chunkCount).forEach {
            radarChunkCnt = if (it < chunkCount - 1) {
                breakSizeRadar * 6
            } else {
                6 * (totalBinsOgl - it * breakSizeRadar)
            }
            try {
                radarBuffers.floatBuffer.position(it * breakSizeRadar * 32)
                GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, radarBuffers.floatBuffer.slice().asFloatBuffer())
                radarBuffers.colorBuffer.position(it * breakSizeRadar * 12)
                GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, radarBuffers.colorBuffer.slice())
                triangleIndexBuffer.position(0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, radarChunkCnt, GLES20.GL_UNSIGNED_SHORT, triangleIndexBuffer.slice().asShortBuffer())
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
        GLES20.glLineWidth(defaultLineWidth)
        listOf(countyLineBuffers, stateLineBuffers, hwBuffers, hwExtBuffers, lakeBuffers).forEach {
            if (zoom > it.scaleCutOff) {
                drawElement(it)
            }
        }
        listOf(spotterBuffers, hiBuffers, tvsBuffers).forEach {
            if (zoom > it.scaleCutOff) {
                drawTriangles(it)
            }
        }
        GLES20.glLineWidth(3.0f)
        listOf(stiBuffers, wbGustsBuffers, wbBuffers).forEach {
            if (zoom > it.scaleCutOff) {
                drawPolygons(it, 16)
            }
        }
        // FIXME found out why this is crashing//
        listOf(wbCircleBuffers).forEach {
            if (zoom > it.scaleCutOff) {
                drawTriangles(wbCircleBuffers)
            }
        }
        GLES20.glLineWidth(defaultLineWidth)
        drawTriangles(locdotBuffers)
        if (MyApplication.locdotFollowsGps && locCircleBuffers.floatBuffer.capacity() != 0 && locCircleBuffers.indexBuffer.capacity() != 0 && locCircleBuffers.colorBuffer.capacity() != 0) {
            locCircleBuffers.chunkCount = 1
            drawPolygons(locCircleBuffers, 16)
        }
        GLES20.glLineWidth(warnLineWidth)
        listOf(warningSpsBuffers, warningSvsBuffers, warningSmwBuffers, warningTstBuffers, warningFfwBuffers, warningTorBuffers).forEach { drawPolygons(it, 8) }
        GLES20.glLineWidth(watmcdLineWidth)
        listOf(mpdBuffers, mcdBuffers, watchBuffers, watchTornadoBuffers, swoBuffers).forEach { drawPolygons(it, 8) }
    }

    private fun drawTriangles(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, buffers.colorBuffer.slice().asFloatBuffer())
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
        }
    }

    private fun drawPolygons(buffers: ObjectOglBuffers, countDivisor: Int) {
        if (buffers.isInitialized) {
            // FIXME is chunkcount ever above one? "it" is never reference in the loop
            (0 until buffers.chunkCount).forEach { _ ->
                lineIndexBuffer.position(0)
                buffers.setToPositionZero()
                GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, buffers.colorBuffer)
                GLES20.glDrawElements(GLES20.GL_LINES, buffers.floatBuffer.capacity() / countDivisor, GLES20.GL_UNSIGNED_SHORT, lineIndexBuffer.slice().asShortBuffer())
            }
        }
    }

    private fun drawElement(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            (0 until buffers.chunkCount).forEach {
                lineCnt = if (it < buffers.chunkCount - 1) {
                    breakSizeLine * 2
                } else {
                    2 * (buffers.count / 4 - it * breakSizeLine)
                }
                try {
                    buffers.floatBuffer.position(it * 480000)
                    buffers.colorBuffer.position(0)
                    lineIndexBuffer.position(0)
                    GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                    GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, buffers.colorBuffer.slice())
                    GLES20.glDrawElements(GLES20.GL_LINES, lineCnt, GLES20.GL_UNSIGNED_SHORT, lineIndexBuffer.slice().asShortBuffer())
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        mSurfaceRatio = width.toFloat() / height
        (0..15).forEach {
            mtrxProjection[it] = 0.0f
            mtrxView[it] = 0.0f
            mtrxProjectionAndView[it] = 0.0f
        }
        Matrix.orthoM(mtrxProjection, 0, (-1 * ortInt).toFloat(), ortInt.toFloat(), -1f * ortInt.toFloat() * (1 / mSurfaceRatio), ortInt * (1 / mSurfaceRatio), 1f, -1f)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0)
        Matrix.multiplyMM(mtrxProjectionAndViewOrig, 0, mtrxProjection, 0, mtrxView, 0)
        Matrix.translateM(mtrxProjectionAndView, 0, x, y, 0f)
        Matrix.scaleM(mtrxProjectionAndView, 0, zoom, zoom, 1f)
    }

    private fun scaleLength(currentLength: Float): Float {
        return if (zoom > 1.01f) {
            currentLength / zoom * 2
        } else {
            currentLength
        }
    }

    fun constructStateLines() {
        constructGenericGeographic(stateLineBuffers)
    }

    fun constructHWLines() {
        constructGenericGeographic(hwBuffers)
    }

    fun deconstructHWLines() {
        deconstructGenericGeographic(hwBuffers)
    }

    fun constructHWEXTLines() {
        constructGenericGeographic(hwExtBuffers)
    }

    fun deconstructHWEXTLines() {
        deconstructGenericGeographic(hwExtBuffers)
    }

    fun constructLakes() {
        constructGenericGeographic(lakeBuffers)
    }

    fun deconstructLakes() {
        deconstructGenericGeographic(lakeBuffers)
    }

    fun constructCounty() {
        constructGenericGeographic(countyLineBuffers)
    }

    fun deconstructCounty() {
        deconstructGenericGeographic(countyLineBuffers)
    }

    private fun constructGenericGeographic(buffers: ObjectOglBuffers) {
        if (!buffers.isInitialized) {
            buffers.count = buffers.geotype.count
            buffers.breakSize = 30000
            buffers.initialize(4 * buffers.count, 0, 3 * buffers.breakSize * 2, buffers.geotype.color)
            if (MyApplication.radarUseJni) {
                JNI.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            } else {
                UtilityWXOGLPerf.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            }
            buffers.isInitialized = true
        }
        if (!MyApplication.radarUseJni) {
            UtilityWXOGLPerf.genMercato(buffers.geotype.relativeBuffer, buffers.floatBuffer, pn, buffers.count)
        } else {
            JNI.genMercato(buffers.geotype.relativeBuffer, buffers.floatBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(), pn.yCenter.toFloat(), pn.oneDegreeScaleFactorFloat, buffers.count)
        }
        buffers.setToPositionZero()
    }

    private fun deconstructGenericGeographic(buffers: ObjectOglBuffers) {
        buffers.isInitialized = false
    }

    private fun constructGenericLinesShort(buffers: ObjectOglBuffers, f: List<Double>) {
        val remainder: Int
        buffers.initialize(4 * 4 * f.size, 0, 3 * 4 * f.size, buffers.type.color)
        try {
            if (MyApplication.radarUseJni) {
                JNI.colorGen(buffers.colorBuffer, 4 * f.size, buffers.colorArray)
            } else {
                UtilityWXOGLPerf.colorGen(buffers.colorBuffer, 4 * f.size, buffers.colorArray)
            }
        } catch (e: java.lang.Exception){
            UtilityLog.HandleException(e)
        }
        buffers.breakSize = 15000
        buffers.chunkCount = 1
        val totalBinsSti = f.size / 4
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
            (0 until buffers.breakSize).forEach { _ ->
                buffers.putFloat(f[vList].toFloat())
                buffers.putFloat(f[vList + 1].toFloat() * -1)
                buffers.putFloat(f[vList + 2].toFloat())
                buffers.putFloat(f[vList + 3].toFloat() * -1)
                vList += 4
            }
        }
        buffers.isInitialized = true
    }

    fun constructSTILines() {
        val fSti = WXGLNexradLevel3StormInfo.decodeAndPlot(context, idxStr, rid, provider)
        constructGenericLinesShort(stiBuffers, fSti)
    }

    fun deconstructSTILines() {
        deconstructGenericLines(stiBuffers)
    }

    fun constructWATMCDLines() {
        constructGenericLines(mcdBuffers)
        constructGenericLines(watchBuffers)
        constructGenericLines(watchTornadoBuffers)
    }

    fun deconstructWATMCDLines() {
        deconstructGenericLines(mcdBuffers)
        deconstructGenericLines(watchBuffers)
        deconstructGenericLines(watchTornadoBuffers)
    }

    fun constructTorWarningLines() {
        constructGenericLines(warningTorBuffers)
    }
    fun deconstructTorWarningLines() {
        deconstructGenericLines(warningTorBuffers)
    }

    fun constructTstWarningLines() {
        constructGenericLines(warningTstBuffers)
    }
    fun deconstructTstWarningLines() {
        deconstructGenericLines(warningTstBuffers)
    }

    fun constructFfwWarningLines() {
        constructGenericLines(warningFfwBuffers)
    }
    fun deconstructFfwWarningLines() {
        deconstructGenericLines(warningFfwBuffers)
    }

    fun constructSmwWarningLines() {
        constructGenericLines(warningSmwBuffers)
    }
    fun deconstructSmwWarningLines() {
        deconstructGenericLines(warningSmwBuffers)
    }

    fun constructSvsWarningLines() {
        constructGenericLines(warningSvsBuffers)
    }
    fun deconstructSvsWarningLines() {
        deconstructGenericLines(warningSvsBuffers)
    }

    fun constructSpsWarningLines() {
        constructGenericLines(warningSpsBuffers)
    }
    fun deconstructSpsWarningLines() {
        deconstructGenericLines(warningSpsBuffers)
    }


    fun constructLocationDot(locXCurrent: String, locYCurrentF: String, archiveMode: Boolean) {
        var locYCurrent = locYCurrentF
        var locmarkerAl = mutableListOf<Double>()
        locdotBuffers.lenInit = MyApplication.radarLocdotSize.toFloat()
        locYCurrent = locYCurrent.replace("-", "")
        val x = locXCurrent.toDoubleOrNull() ?: 0.0
        val y = locYCurrent.toDoubleOrNull() ?: 0.0
        if (PolygonType.LOCDOT.pref) {
            locmarkerAl = UtilityLocation.latLonAsDouble
        }
        if (MyApplication.locdotFollowsGps || archiveMode) {
            locmarkerAl.add(x)
            locmarkerAl.add(y)
            gpsX = x
            gpsY = y
        }
        locdotBuffers.xList = DoubleArray(locmarkerAl.size)
        locdotBuffers.yList = DoubleArray(locmarkerAl.size)
        var xx = 0
        var yy = 0
        locmarkerAl.indices.forEach {
            if (it and 1 == 0) {
                locdotBuffers.xList[xx] = locmarkerAl[it]
                xx += 1
            } else {
                locdotBuffers.yList[yy] = locmarkerAl[it]
                yy += 1
            }
        }

        locdotBuffers.triangleCount = 12
        constructTriangles(locdotBuffers)
        locCircleBuffers.triangleCount = 36
        locCircleBuffers.initialize(32 * locCircleBuffers.triangleCount,
                8 * locCircleBuffers.triangleCount,
                6 * locCircleBuffers.triangleCount,
                MyApplication.radarColorLocdot)
        if (MyApplication.radarUseJni) {
            JNI.colorGen(locCircleBuffers.colorBuffer, 2 * locCircleBuffers.triangleCount, locCircleBuffers.colorArray)
        } else {
            UtilityWXOGLPerf.colorGen(locCircleBuffers.colorBuffer, 2 * locCircleBuffers.triangleCount, locCircleBuffers.colorArray)
        }
        if (MyApplication.locdotFollowsGps) {
            locCircleBuffers.lenInit = locdotBuffers.lenInit
            UtilityWXOGLPerf.genCircleLocdot(locCircleBuffers, pn, gpsX, gpsY)
        }
        locdotBuffers.isInitialized = true
        locCircleBuffers.isInitialized = true
    }

    fun deconstructLocationDot() {
        locdotBuffers.isInitialized = false
        locCircleBuffers.isInitialized = false
    }

    fun constructSpotters() {
        spotterBuffers.isInitialized = true
        spotterBuffers.lenInit = MyApplication.radarSpotterSize.toFloat()
        spotterBuffers.triangleCount = 6
        UtilitySpotter.spotterData
        spotterBuffers.xList = UtilitySpotter.x
        spotterBuffers.yList = UtilitySpotter.y
        constructTriangles(spotterBuffers)
    }

    fun deconstructSpotters() {
        spotterBuffers.isInitialized = false
    }

    fun constructHI() {
        hiBuffers.lenInit = MyApplication.radarHiSize.toFloat()
        val stormList = WXGLNexradLevel3HailIndex.decodeAndPlot(context, rid, idxStr)
        hiBuffers.setXYList(stormList)
        constructTriangles(hiBuffers)
    }

    private fun constructTriangles(buffers: ObjectOglBuffers) {
        buffers.count = buffers.xList.size
        when (buffers.type) {
            PolygonType.LOCDOT, PolygonType.SPOTTER -> buffers.initialize(
                    24 * buffers.count * buffers.triangleCount,
                    12 * buffers.count * buffers.triangleCount,
                    9 * buffers.count * buffers.triangleCount,
                    buffers.type.color)
            else -> buffers.initialize(
                    4 * 6 * buffers.count,
                    4 * 3 * buffers.count,
                    9 * buffers.count,
                    buffers.type.color)
        }
        buffers.lenInit = scaleLength(buffers.lenInit)
        buffers.draw(pn)
        buffers.isInitialized = true
    }

    fun deconstructHI() {
        hiBuffers.isInitialized = false
    }

    fun constructTVS() {
        tvsBuffers.lenInit = MyApplication.radarTvsSize.toFloat()
        val stormList = WXGLNexradLevel3TVS.decodeAndPlot(context, rid, idxStr)
        tvsBuffers.setXYList(stormList)
        constructTriangles(tvsBuffers)
    }

    fun deconstructTVS() {
        tvsBuffers.isInitialized = false
    }

    fun constructMPDLines() {
        constructGenericLines(mpdBuffers)
    }

    fun deconstructMPDLines() {
        deconstructGenericLines(mpdBuffers)
    }

    private fun constructGenericLines(buffers: ObjectOglBuffers) {
        var fList = listOf<Double>()
        when (buffers.type) {
            PolygonType.MCD, PolygonType.MPD, PolygonType.WATCH, PolygonType.WATCH_TORNADO -> fList = UtilityWat.addWat(context, provider, rid, buffers.type).toList()
            PolygonType.TST, PolygonType.TOR, PolygonType.FFW, PolygonType.SMW, PolygonType.SVS, PolygonType.SPS -> fList = WXGLPolygonWarnings.addWarnings(context, provider, rid, buffers.type).toList()
            PolygonType.STI -> fList = WXGLNexradLevel3StormInfo.decodeAndPlot(context, idxStr, rid, provider).toList()
            else -> {
            }
        }
        buffers.breakSize = 15000
        buffers.chunkCount = 1
        val totalBinsGeneric = fList.size / 4
        var remainder = 0
        if (totalBinsGeneric < buffers.breakSize) {
            buffers.breakSize = totalBinsGeneric
            remainder = buffers.breakSize
        } else if (buffers.breakSize > 0) {
            buffers.chunkCount = totalBinsGeneric / buffers.breakSize
            remainder = totalBinsGeneric - buffers.breakSize * buffers.chunkCount
            buffers.chunkCount = buffers.chunkCount + 1
        }
        buffers.initialize(4 * 4 * totalBinsGeneric, 0, 3 * 4 * totalBinsGeneric, buffers.type.color)
        if (MyApplication.radarUseJni) {
            JNI.colorGen(buffers.colorBuffer, 4 * totalBinsGeneric, buffers.colorArray)
        } else {
            UtilityWXOGLPerf.colorGen(buffers.colorBuffer, 4 * totalBinsGeneric, buffers.colorArray)
        }
        var vList = 0
        (0 until buffers.chunkCount).forEach {
            if (it == buffers.chunkCount - 1) {
                buffers.breakSize = remainder
            }
            (0 until buffers.breakSize).forEach { _ ->
                if (fList.size > (vList + 3)) {
                    buffers.putFloat(fList[vList].toFloat())
                    buffers.putFloat(fList[vList + 1].toFloat() * -1.0f)
                    buffers.putFloat(fList[vList + 2].toFloat())
                    buffers.putFloat(fList[vList + 3].toFloat() * -1.0f)
                    vList += 4
                }
            }
        }
        buffers.isInitialized = true
    }

    private fun deconstructGenericLines(buffers: ObjectOglBuffers) {
        buffers.chunkCount = 0
        buffers.isInitialized = false
    }

    fun constructWBLines() {
        val fWb = WXGLNexradLevel3WindBarbs.decodeAndPlot(context, rid, provider, false)
        constructGenericLinesShort(wbBuffers, fWb)
        constructWBLinesGusts()
        constructWBCircle()
    }

    private fun constructWBLinesGusts() {
        val fWbGusts = WXGLNexradLevel3WindBarbs.decodeAndPlot(context, rid, provider, true)
        constructGenericLinesShort(wbGustsBuffers, fWbGusts)
    }

    fun deconstructWBLines() {
        wbBuffers.isInitialized = false
        deconstructWBLinesGusts()
        deconstructWBCircle()
    }

    private fun deconstructWBLinesGusts() {
        wbGustsBuffers.isInitialized = false
    }

    private fun constructWBCircle() {
        wbCircleBuffers.lenInit = MyApplication.radarAviationSize.toFloat()
        wbCircleBuffers.xList = UtilityMetar.x
        wbCircleBuffers.yList = UtilityMetar.y
        wbCircleBuffers.colorIntArray = UtilityMetar.obsArrAviationColor
        wbCircleBuffers.count = wbCircleBuffers.xList.size
        wbCircleBuffers.triangleCount = 6
        wbCircleBuffers.initialize(
                24 * wbCircleBuffers.count * wbCircleBuffers.triangleCount,
                12 * wbCircleBuffers.count * wbCircleBuffers.triangleCount,
                9 * wbCircleBuffers.count * wbCircleBuffers.triangleCount)
        wbCircleBuffers.lenInit = scaleLength(wbCircleBuffers.lenInit)
        wbCircleBuffers.draw(pn)
        wbCircleBuffers.isInitialized = true
    }

    private fun deconstructWBCircle() {
        wbCircleBuffers.isInitialized = false
    }

    fun constructSWOLines() {
        val hashSWO = UtilitySWOD1.HASH_SWO.toMap()
        colorSwo[0] = Color.MAGENTA
        colorSwo[1] = Color.RED
        colorSwo[2] = Color.rgb(255, 140, 0)
        colorSwo[3] = Color.YELLOW
        colorSwo[4] = Color.rgb(0, 100, 0)
        var tmpCoords: DoubleArray
        val fSize = (0..4).filter { hashSWO[it] != null }.sumBy { hashSWO[it]!!.size }
        swoBuffers.breakSize = 15000
        swoBuffers.chunkCount = 1
        val totalBinsSwo = fSize / 4
        swoBuffers.initialize(4 * 4 * totalBinsSwo, 0, 3 * 2 * totalBinsSwo)
        if (totalBinsSwo < swoBuffers.breakSize) {
            swoBuffers.breakSize = totalBinsSwo
        } else {
            swoBuffers.chunkCount = totalBinsSwo / swoBuffers.breakSize
            swoBuffers.chunkCount = swoBuffers.chunkCount + 1
        }
        swoBuffers.isInitialized = true
        (0..4).forEach {
            if (hashSWO[it] != null) {
                var j = 0
                while (j < hashSWO[it]!!.size) {
                    swoBuffers.putColor(Color.red(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.green(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.blue(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.red(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.green(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.blue(colorSwo[it]).toByte())
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(hashSWO[it]!![j], (hashSWO[it]!![j + 1] * -1.0f), pn)
                    swoBuffers.putFloat(tmpCoords[0].toFloat())
                    swoBuffers.putFloat(tmpCoords[1].toFloat() * -1.0f)
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(hashSWO[it]!![j + 2], (hashSWO[it]!![j + 3] * -1.0f), pn)
                    swoBuffers.putFloat(tmpCoords[0].toFloat())
                    swoBuffers.putFloat(tmpCoords[1].toFloat() * -1.0f)
                    j += 4
                }
            }
        }
    }

    fun deconstructSWOLines() {
        swoBuffers.isInitialized = false
    }

    fun setHiInit(hiInit: Boolean) {
        hiBuffers.isInitialized = hiInit
    }

    fun setTvsInit(tvsInit: Boolean) {
        tvsBuffers.isInitialized = tvsInit
    }

    val oneDegreeScaleFactor: Float
        get() = pn.oneDegreeScaleFactorFloat

    fun setChunkCountSti(chunkCountSti: Int) {
        this.stiBuffers.chunkCount = chunkCountSti
    }

    fun setChunkCount(chunkCount: Int) {
        this.chunkCount = chunkCount
    }

    fun setViewInitial(zoom: Float, x: Float, y: Float) {
        this.zoom = zoom
        this.x = x
        this.y = y
    }
}