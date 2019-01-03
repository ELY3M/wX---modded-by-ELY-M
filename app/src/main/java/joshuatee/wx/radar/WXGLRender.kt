/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.graphics.*
import android.opengl.*
import android.opengl.Matrix
import android.opengl.GLSurfaceView.Renderer
import android.util.Log
import joshuatee.wx.JNI
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.*
import android.graphics.Bitmap


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
        var hailSizeIcon: String = "hail0.png"
        var hailSize: Double = 0.toDouble()
    }

    val TAG: String = "joshuatee WXGLRender"
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
    private val zoomToHideMiscFeatures = 0.5f //was 0.5f
    private val radarBuffers = ObjectOglRadarBuffers(context, MyApplication.nexradRadarBackgroundColor)
    private val spotterBuffers = ObjectOglBuffers(PolygonType.SPOTTER, zoomToHideMiscFeatures)
    private val stateLineBuffers = ObjectOglBuffers(GeographyType.STATE_LINES, 0.0f)
    private val countyLineBuffers = ObjectOglBuffers(GeographyType.COUNTY_LINES, 0.75f)
    private val hwBuffers = ObjectOglBuffers(GeographyType.HIGHWAYS, 0.45f)
    private val hwExtBuffers = ObjectOglBuffers(GeographyType.HIGHWAYS_EXTENDED, 3.00f)
    private val lakeBuffers = ObjectOglBuffers(GeographyType.LAKES, zoomToHideMiscFeatures)
    private val stiBuffers = ObjectOglBuffers(PolygonType.STI, zoomToHideMiscFeatures)
    private val wbBuffers = ObjectOglBuffers(PolygonType.WIND_BARB, zoomToHideMiscFeatures)
    private val wbGustsBuffers = ObjectOglBuffers(PolygonType.WIND_BARB_GUSTS, zoomToHideMiscFeatures)
    private val mpdBuffers = ObjectOglBuffers(PolygonType.MPD)
    private val hiBuffers = ObjectOglBuffers(PolygonType.HI, zoomToHideMiscFeatures)
    private val tvsBuffers = ObjectOglBuffers(PolygonType.TVS, zoomToHideMiscFeatures)
    private val warningTorBuffers = ObjectOglBuffers(PolygonType.TOR, 0.0f)
    private val warningSvrBuffers = ObjectOglBuffers(PolygonType.SVR, 0.0f)
    private val warningEwwBuffers = ObjectOglBuffers(PolygonType.EWW, 0.0f)
    private val warningFfwBuffers = ObjectOglBuffers(PolygonType.FFW, 0.0f)
    private val warningSmwBuffers = ObjectOglBuffers(PolygonType.SMW, 0.0f)
    private val warningSvsBuffers = ObjectOglBuffers(PolygonType.SVS, 0.0f)
    private val warningSpsBuffers = ObjectOglBuffers(PolygonType.SPS, 0.0f)
    private val watchSvrBuffers = ObjectOglBuffers(PolygonType.WATCH_SVR, 0.0f)
    private val watchTorBuffers = ObjectOglBuffers(PolygonType.WATCH_TOR)
    private val mcdBuffers = ObjectOglBuffers(PolygonType.MCD, 0.0f)
    private val swoBuffers = ObjectOglBuffers()
    private val locdotBuffers = ObjectOglBuffers(PolygonType.LOCDOT, 0.0f)
    private val locIconBuffers = ObjectOglBuffers()
    private val locBugBuffers = ObjectOglBuffers()
    private val wbCircleBuffers = ObjectOglBuffers(PolygonType.WIND_BARB_CIRCLE, zoomToHideMiscFeatures)
    private val conusRadarBuffers = ObjectOglBuffers()
    private val colorSwo = IntArray(5)
    private var breakSize15 = 15000
    private val breakSizeRadar = 15000
    private var mPositionHandle = 0
    private var colorHandle = 0
    private var tdwr = false
    private var chunkCount = 0
    private var totalBins = 0
    private var totalBinsOgl = 0
    var displayHold: Boolean = false
    private var mSizeHandle = 0
    private var iTexture: Int = 0

    private var conusradarId = -1
    private var locationId = -1
    private var locationBugId = -1
    private var tvsId = -1
    private var hiId = -1



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
                locIconBuffers.lenInit = 0f //was locdotBuffers.lenInit
                UtilityWXOGLPerf.genLocdot(locIconBuffers, pn, gpsX, gpsY)
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
    private var defaultLineWidth = 1.0f  //was 2.0f
    private var warnLineWidth = 2.0f
    private var watmcdLineWidth = 2.0f
    private var ridPrefixGlobal = ""
    private var bgColorFRed = 0.0f
    private var bgColorFGreen = 0.0f
    private var bgColorFBlue = 0.0f
    val ortInt: Int = 400
    private val provider = ProjectionType.WX_OGL
    // this controls if the projection is mercator (nexrad) or 4326 / rectangular
    // after you zoom out past a certain point you need to hide the nexrad, show the mosaic
    // and reconstruct all geometry and warning/watch lines using 4326 projection (set this variable to false to not use mercator transformation )
    // so far, only the base geometry ( state lines, county, etc ) respect this setting
    private var useMercatorProjection = true
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
        defaultLineWidth = MyApplication.radarDefaultLinesize.toFloat()
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
                //FIXME this might be better way to do SRM tilts
                product.contains("NSW") || product.contains("N0S") || product.contains("N1S") || product.contains("N2S") || product.contains("N3S") -> {
                    radarL3Object.decocodeAndPlotNexradLevel3FourBit(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
                /*
		        product.contains("NSW") -> {
                    radarL3Object.decocodeAndPlotNexradLevel3FourBit(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
                product.contains("N1S") -> {
                    radarL3Object.decocodeAndPlotNexradLevel3FourBit(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
                product.contains("N2S") -> {
                    radarL3Object.decocodeAndPlotNexradLevel3FourBit(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
                product.contains("N3S") -> {
                    radarL3Object.decocodeAndPlotNexradLevel3FourBit(context, radarBuffers.fn, radarStatusStr)
                    radarBuffers.extractL3Data(radarL3Object)
                }
                */
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
                totalBins = if (radarBuffers.productCode != 56.toShort() && radarBuffers.productCode != 30.toShort()) {
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

        OpenGLShader.sp_loadimage = GLES20.glCreateProgram()
        GLES20.glAttachShader(OpenGLShader.sp_loadimage, OpenGLShader.loadShader(GLES20.GL_VERTEX_SHADER, OpenGLShader.vs_loadimage))
        GLES20.glAttachShader(OpenGLShader.sp_loadimage, OpenGLShader.loadShader(GLES20.GL_FRAGMENT_SHADER, OpenGLShader.fs_loadimage))
        GLES20.glLinkProgram(OpenGLShader.sp_loadimage)

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

        // FIXME
        // whether or not to respect the display being touched needs to be stored in
        // objectglbuffers. The wXL23 Metal code is more generic and thus each element drawn will need
        // to be checked. Will do this later when I have more time
        if (!displayHold) {

            //FIXME use real plotting....
         /*
            Log.i(TAG, "zoom: " + zoom)
            if (MyApplication.radarConusRadar) {
                if (zoom < 0.093f) {
                    Log.i(TAG, "zoom out to conusradar")
                    drawConusRadar(conusRadarBuffers)
            }
        }
         */

            // TODO do custom icons for hail//
        listOf(spotterBuffers).forEach {
            if (zoom > it.scaleCutOff) {
                drawTriangles(it)
            }
        }

        if (zoom > hiBuffers.scaleCutOff) {
            drawHI(hiBuffers)
        }

        if (zoom > tvsBuffers.scaleCutOff) {
            drawTVS(tvsBuffers)
        }

            GLES20.glLineWidth(3.0f)
            listOf(stiBuffers, wbGustsBuffers, wbBuffers).forEach {
                if (zoom > it.scaleCutOff) {
                    drawPolygons(it, 16)
                }
            }

            if (zoom > wbCircleBuffers.scaleCutOff) {
                drawTriangles(wbCircleBuffers)
            }

            GLES20.glLineWidth(defaultLineWidth)

        //drawTriangles(locdotBuffers)
        //drawLocation(locdotBuffers)

            if (MyApplication.locdotFollowsGps) {
                locIconBuffers.chunkCount = 1
                drawLocation(locIconBuffers)
            } else {
                drawTriangles(locdotBuffers)
            }
        } //displayhold


        if (MyApplication.locdotBug) {
            Log.i(TAG, "bearing: " + WXGLRadarActivity.bearingCurrent)
            Log.i(TAG, "speed: " + WXGLRadarActivity.speedCurrent)
            if (WXGLRadarActivity.speedCurrent >= 0.43) {
                //set up location bug
                Log.i(TAG, "location bug!!!!")
                drawLocationBug(locBugBuffers)
            }
        }


        GLES20.glLineWidth(warnLineWidth)
        listOf(warningSpsBuffers, warningSvsBuffers, warningSmwBuffers, warningSvrBuffers, warningEwwBuffers, warningFfwBuffers, warningTorBuffers).forEach { drawPolygons(it, 8) }
        GLES20.glLineWidth(watmcdLineWidth)
        listOf(mpdBuffers, mcdBuffers, watchSvrBuffers, watchTorBuffers, swoBuffers).forEach { drawPolygons(it, 8) }

        //TODO try to use real plotting without adding usa map....
        //hack job!!!
        if (!displayHold) {
            Log.i(TAG, "zoom: " + zoom)
            if (MyApplication.radarConusRadar) {
                if (zoom < 0.093f) {
                    Log.i(TAG, "zoom out to conusradar")
                    drawConusRadar(conusRadarBuffers)
                }
            }
        }

    }


//point sprite blah//
    private fun drawConusRadar(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, mtrxProjectionAndView, 0)
            mSizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            //var conusbitmap: Bitmap? = OpenGLShader.LoadBitmap(MyApplication.FilesPath + "conus.gif")

            GLES20.glUniform1f(mSizeHandle, 1000f) //was 1600f
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            //val conusbitmap: Bitmap? = ///UtilityConusRadar.nwsConusRadar(context)
            conusradarId = OpenGLShader.LoadTexture(MyApplication.FilesPath + "conus.gif")
            GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, conusradarId)
            GLES20.glUniform1i(iTexture, 0)
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            //GLES20.glDrawElements(GLES20.GL_POINTS, 1, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
            GLES20.glDrawElements(GLES20.GL_POINTS, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
            GLES20.glUseProgram(OpenGLShader.sp_SolidColor)
        }
    }


    private fun drawLocation(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
                GLES20.glUseProgram(OpenGLShader.sp_loadimage)
                mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, mtrxProjectionAndView, 0)
                mSizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
                GLES20.glUniform1f(mSizeHandle, MyApplication.radarLocIconSize.toFloat())
                iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
                locationId = OpenGLShader.LoadTexture(MyApplication.FilesPath + "location.png")
                GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                GLES20.glEnableVertexAttribArray(mPositionHandle)
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, locationId)
                GLES20.glUniform1i(iTexture, 0)
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                GLES20.glDrawElements(GLES20.GL_POINTS, 1, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
                //GLES20.glDrawElements(GLES20.GL_POINTS, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
                GLES20.glUseProgram(OpenGLShader.sp_SolidColor)
        }
    }

    private fun drawLocationBug(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, mtrxProjectionAndView, 0)
            mSizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(mSizeHandle, MyApplication.radarLocBugSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            val rotatebug: Bitmap = OpenGLShader.RotateBitmap(MyApplication.FilesPath + "headingbug.png", WXGLRadarActivity.bearingCurrent.toDouble())
            locationBugId = OpenGLShader.LoadBitmapTexture(rotatebug)
            GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, locationBugId)
            GLES20.glUniform1i(iTexture, 0)
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glDrawElements(GLES20.GL_POINTS, 1, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
            //GLES20.glDrawElements(GLES20.GL_POINTS, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
            GLES20.glUseProgram(OpenGLShader.sp_SolidColor)
        }
    }



    private fun drawTVS(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
                GLES20.glUseProgram(OpenGLShader.sp_loadimage)
                mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, mtrxProjectionAndView, 0)
                mSizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
                GLES20.glUniform1f(mSizeHandle, MyApplication.radarTvsSize.toFloat())
                iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
                tvsId = OpenGLShader.LoadTexture(MyApplication.FilesPath + "tvs.png")
                GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                GLES20.glEnableVertexAttribArray(mPositionHandle)
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tvsId)
                GLES20.glUniform1i(iTexture, 0)
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                //GLES20.glDrawElements(GLES20.GL_POINTS, 1, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
                GLES20.glDrawElements(GLES20.GL_POINTS, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
                GLES20.glUseProgram(OpenGLShader.sp_SolidColor)


        }
    }

    //FIXME need to pick a icon based on hail size//
    private fun drawHI(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, mtrxProjectionAndView, 0)
            mSizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(mSizeHandle, MyApplication.radarHiSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            hiId = OpenGLShader.LoadTexture(MyApplication.FilesPath + hailSizeIcon)
            GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, hiId)
            GLES20.glUniform1i(iTexture, 0)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            //GLES20.glDrawElements(GLES20.GL_POINTS, 1, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
            GLES20.glDrawElements(GLES20.GL_POINTS, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
            GLES20.glUseProgram(OpenGLShader.sp_SolidColor)


        }
    }


    // FIXME CRASHING HERE sometimes -- FIXED via "displayhold" code
    /*
        java.lang.IllegalArgumentException: Must use a native order direct Buffer
        at android.opengl.GLES20.glVertexAttribPointerBounds(Native Method)
        at android.opengl.GLES20.glVertexAttribPointer(GLES20.java:1906)
        at joshuatee.wx.radar.WXGLRender.drawTriangles(WXGLRender.kt:388)
        at joshuatee.wx.radar.WXGLRender.onDrawFrame(WXGLRender.kt:359)
    * */
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

    // FIXME this check for 4326 will need to be done in other locations as well but for now just testing to see
    // if the rectangular projection is realized.
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
            if (useMercatorProjection) {
                UtilityWXOGLPerf.genMercator(buffers.geotype.relativeBuffer, buffers.floatBuffer, pn, buffers.count)
            } else {
                UtilityWXOGLPerf.generate4326Projection(buffers.geotype.relativeBuffer, buffers.floatBuffer, pn, buffers.count)
            }
        } else {
            if (useMercatorProjection) {
                JNI.genMercato(buffers.geotype.relativeBuffer, buffers.floatBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(), pn.yCenter.toFloat(), pn.oneDegreeScaleFactorFloat, buffers.count)
            } else {
                // FIXME - will want native code version for 4326
                UtilityWXOGLPerf.generate4326Projection(buffers.geotype.relativeBuffer, buffers.floatBuffer, pn, buffers.count)
            }
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
        } catch (e: java.lang.Exception) {
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
        constructGenericLines(watchTorBuffers)
        constructGenericLines(watchSvrBuffers)
    }

    fun deconstructWATMCDLines() {
        deconstructGenericLines(mcdBuffers)
        deconstructGenericLines(watchTorBuffers)
        deconstructGenericLines(watchSvrBuffers)
    }

    fun constructTorWarningLines() {
        constructGenericLines(warningTorBuffers)
    }
    fun deconstructTorWarningLines() {
        deconstructGenericLines(warningTorBuffers)
    }

    fun constructSvrWarningLines() {
        constructGenericLines(warningSvrBuffers)
    }
    fun deconstructSvrWarningLines() {
        deconstructGenericLines(warningSvrBuffers)
    }

    fun constructEwwWarningLines() {
        constructGenericLines(warningEwwBuffers)
    }
    fun deconstructEwwWarningLines() {
        deconstructGenericLines(warningEwwBuffers)
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
        if (MyApplication.locdotFollowsGps) {
            locdotBuffers.lenInit = 0f
        } else {
            locdotBuffers.lenInit = MyApplication.radarLocdotSize.toFloat()
        }
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



        //Custom location icon//
        locIconBuffers.triangleCount = 1 //was 36
        locIconBuffers.initialize(32 * locIconBuffers.triangleCount,
                8 * locIconBuffers.triangleCount,
                6 * locIconBuffers.triangleCount,
                MyApplication.radarColorLocdot)


        //location bug
        locBugBuffers.triangleCount = 1 //was 36
        locBugBuffers.initialize(32 * locBugBuffers.triangleCount,
                8 * locBugBuffers.triangleCount,
                6 * locBugBuffers.triangleCount,
                MyApplication.radarColorLocdot)


        /* not needed .. if have custom location icon
        if (MyApplication.radarUseJni) {
            JNI.colorGen(locIconBuffers.colorBuffer, 2 * locIconBuffers.triangleCount, locIconBuffers.colorArray)
        } else {
            UtilityWXOGLPerf.colorGen(locIconBuffers.colorBuffer, 2 * locIconBuffers.triangleCount, locIconBuffers.colorArray)
        }
        */


        if (MyApplication.locdotFollowsGps) {
            locIconBuffers.lenInit = locdotBuffers.lenInit
            UtilityWXOGLPerf.genLocdot(locIconBuffers, pn, gpsX, gpsY)
            //location bug//
            if (MyApplication.locdotBug) {
                locBugBuffers.lenInit = 0f
                UtilityWXOGLPerf.genLocdot(locBugBuffers, pn, gpsX, gpsY)
            }

        }



        locdotBuffers.isInitialized = true
        locIconBuffers.isInitialized = true
        locBugBuffers.isInitialized = true
    }

    fun deconstructLocationDot() {
        locdotBuffers.isInitialized = false
        locIconBuffers.isInitialized = false
        locBugBuffers.isInitialized = false
    }


    /*
    *
    *
-127.620375523875420
50.406626367301044
    * */

    fun constructConusRadar() {
        conusRadarBuffers.lenInit = 0f
        conusRadarBuffers.triangleCount = 1 //was 36
        conusRadarBuffers.initialize(32 * conusRadarBuffers.triangleCount,
                8 * conusRadarBuffers.triangleCount,
                6 * conusRadarBuffers.triangleCount,
                0)

        //editor.putString("RID_latest_X", "36.105") // nws conus
        //editor.putString("RID_latest_Y", "97.141")

        UtilityWXOGLPerf.genMarker(conusRadarBuffers, pn, 36.105, 97.141)
        //UtilityWXOGLPerf.genLocdot(conusRadarBuffers, pn, 40.750220, 99.476964)
        //UtilityWXOGLPerf.genLocdot(conusRadarBuffers, pn, pn.xDbl, pn.yDbl)
        //UtilityWXOGLPerf.genMercator(MyApplication.stateRelativeBuffer, conusRadarBuffers.floatBuffer, pn, conusRadarBuffers.count)


        conusRadarBuffers.isInitialized = true
    }

    fun deconstructConusRadar() {
        conusRadarBuffers.isInitialized = false
    }


    fun constructConusRadar2() {
        if (!conusRadarBuffers.isInitialized) {
            conusRadarBuffers.count = conusRadarBuffers.geotype.count
            conusRadarBuffers.breakSize = 30000
            conusRadarBuffers.initialize(4 * conusRadarBuffers.count, 0, 3 * conusRadarBuffers.breakSize * 2, conusRadarBuffers.geotype.color)
            if (MyApplication.radarUseJni) {
                JNI.colorGen(conusRadarBuffers.colorBuffer, conusRadarBuffers.breakSize * 2, conusRadarBuffers.colorArray)
            } else {
                UtilityWXOGLPerf.colorGen(conusRadarBuffers.colorBuffer, conusRadarBuffers.breakSize * 2, conusRadarBuffers.colorArray)
            }
            conusRadarBuffers.isInitialized = true
        }
        if (!MyApplication.radarUseJni) {
            UtilityWXOGLPerf.genMercator(conusRadarBuffers.geotype.relativeBuffer, conusRadarBuffers.floatBuffer, pn, conusRadarBuffers.count)
        } else {
            JNI.genMercato(conusRadarBuffers.geotype.relativeBuffer, conusRadarBuffers.floatBuffer, pn.xFloat, pn.yFloat, pn.xCenter.toFloat(), pn.yCenter.toFloat(), pn.oneDegreeScaleFactorFloat, conusRadarBuffers.count)
        }
        conusRadarBuffers.setToPositionZero()
    }

    fun deconstructConusRadar2() {
        conusRadarBuffers.isInitialized = false
    }

    fun constructSpotters() {
        spotterBuffers.isInitialized = false //leave it at false or the app will crash randomly
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
        hiBuffers.lenInit = 0f //MyApplication.radarHiSize.toFloat()
        val stormList = WXGLNexradLevel3HailIndex.decodeAndPlot(context, rid, idxStr)
        hiBuffers.setXYList(stormList)
        WXGLNexradLevel3HailIndex.hailList
        constructMarker(hiBuffers)
    }

    fun deconstructHI() {
        hiBuffers.isInitialized = false
    }

    private fun constructTriangles(buffers: ObjectOglBuffers) {
        buffers.count = buffers.xList.size
        when (buffers.type) {
            PolygonType.LOCDOT, PolygonType.SPOTTER -> buffers.initialize(
                24 * buffers.count * buffers.triangleCount,
                12 * buffers.count * buffers.triangleCount,
                9 * buffers.count * buffers.triangleCount,
                buffers.type.color
            )
            else -> buffers.initialize(
                4 * 6 * buffers.count,
                4 * 3 * buffers.count,
                9 * buffers.count,
                buffers.type.color
            )
        }
        buffers.lenInit = scaleLength(buffers.lenInit)
        buffers.draw(pn)
        buffers.isInitialized = true
    }

    private fun constructMarker(buffers: ObjectOglBuffers) {
        buffers.count = buffers.xList.size
        if (buffers.count == 0) {
            Log.i(TAG, "buffer count is 0")
            Log.i(TAG, "Not loading anything!")
            buffers.isInitialized = false
        } else {
            Log.i(TAG, "buffer count: " + buffers.count)
            buffers.triangleCount = 1
            buffers.initialize(
                    24 * buffers.count * buffers.triangleCount,
                    12 * buffers.count * buffers.triangleCount,
                    9 * buffers.count * buffers.triangleCount, 0)
            buffers.lenInit = 0f //scaleLength(buffers.lenInit)
            buffers.draw(pn)
            buffers.isInitialized = true
        }
    }

    fun constructTVS() {
        tvsBuffers.lenInit = 0f //MyApplication.radarTvsSize.toFloat()
        val stormList = WXGLNexradLevel3TVS.decodeAndPlot(context, rid, idxStr)
        tvsBuffers.setXYList(stormList)
        constructMarker(tvsBuffers)

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
            PolygonType.MCD, PolygonType.MPD, PolygonType.WATCH_SVR, PolygonType.WATCH_TOR -> fList = UtilityWatch.addWat(context, provider, rid, buffers.type).toList()
            PolygonType.TOR, PolygonType.SVR, PolygonType.EWW, PolygonType.FFW, PolygonType.SMW, PolygonType.SVS -> fList = WXGLPolygonWarnings.addWarnings(context, provider, rid, buffers.type).toList()
            PolygonType.SPS -> fList = WXGLPolygonWarnings.addSPS(context, provider, rid, buffers.type).toList()
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
        buffers.initialize(
            4 * 4 * totalBinsGeneric,
            0,
            3 * 4 * totalBinsGeneric,
            buffers.type.color
        )
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
            9 * wbCircleBuffers.count * wbCircleBuffers.triangleCount
        )
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
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(
                        hashSWO[it]!![j],
                        (hashSWO[it]!![j + 1] * -1.0f),
                        pn
                    )
                    swoBuffers.putFloat(tmpCoords[0].toFloat())
                    swoBuffers.putFloat(tmpCoords[1].toFloat() * -1.0f)
                    tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(
                        hashSWO[it]!![j + 2],
                        (hashSWO[it]!![j + 3] * -1.0f),
                        pn
                    )
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
