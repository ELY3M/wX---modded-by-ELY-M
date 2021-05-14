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


package joshuatee.wx.radar

import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView.Renderer
import android.opengl.*
import android.opengl.Matrix
import joshuatee.wx.Extensions.isEven
import android.util.Log
import joshuatee.wx.Jni
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.*
import android.graphics.Bitmap
import java.nio.FloatBuffer
import java.nio.ShortBuffer


class WXGLRender(private val context: Context, val paneNumber: Int) : Renderer {

    // The is the OpenGL rendering engine that is used on the main screen and the main radar interface
    // The goal is to be highly performant and configurable as such this module relies on C code accessed via JNI extensively
    // Java can also be used in set in settings->radar

    companion object {
        var ridGlobal = ""
            private set
        var positionXGlobal = 0f
            private set
        var positionYGlobal = 0f
            private set
        const val ortIntGlobal = 400
        var oneDegreeScaleFactorGlobal = 0.0f
            private set
        var hailSizeIcon = "hail0.png"
        var hailSize = 0.toDouble()

        var degreesPerPixellat = -0.017971305190311 //had -
        var degreesPerPixellon = 0.017971305190311
        var north = 0.toDouble()
        var south = 0.toDouble()
        var west = 0.toDouble()
        var east = 0.toDouble()
        var newbottom = 0.toDouble()
        var newleft = 0.toDouble()


    }

    val TAG: String = "joshuatee WXGLRender"
    // this string is normally no string but for dual pane will be set to either 1 or 2 to differentiate timestamps
    var radarStatusStr = ""
    var indexString = "0"
    private val matrixProjection = FloatArray(16)
    private val matrixView = FloatArray(16)
    private var matrixProjectionAndView = FloatArray(16)
    var ridNewList = listOf<RID>()
    private var radarChunkCnt = 0
    private var lineCnt = 0
    private val breakSizeLine = 30000
    private val matrixProjectionAndViewOrig = FloatArray(16)
    private var triangleIndexBuffer = ByteBuffer.allocate(0)
    private var lineIndexBuffer = ByteBuffer.allocate(0)
    private var gpsX = 0.toDouble()
    private var gpsY = 0.toDouble()
    private val zoomToHideMiscFeatures = 0.5f
    private val radarBuffers = ObjectOglRadarBuffers(context, MyApplication.nexradRadarBackgroundColor)
    private val spotterBuffers = ObjectOglBuffers(PolygonType.SPOTTER, zoomToHideMiscFeatures)
    private val stateLineBuffers = ObjectOglBuffers(GeographyType.STATE_LINES, 0.0f)
    private val countyLineBuffers = ObjectOglBuffers(GeographyType.COUNTY_LINES, 0.75f) // was .75
    private val hwBuffers = ObjectOglBuffers(GeographyType.HIGHWAYS, 0.45f)
    private val hwExtBuffers = ObjectOglBuffers(GeographyType.HIGHWAYS_EXTENDED, 3.00f)
    private val lakeBuffers = ObjectOglBuffers(GeographyType.LAKES, zoomToHideMiscFeatures)
    private val stiBuffers = ObjectOglBuffers(PolygonType.STI, zoomToHideMiscFeatures)
    private val wbBuffers = ObjectOglBuffers(PolygonType.WIND_BARB, zoomToHideMiscFeatures)
    private val wbGustsBuffers = ObjectOglBuffers(PolygonType.WIND_BARB_GUSTS, zoomToHideMiscFeatures)
    private val mpdBuffers = ObjectOglBuffers(PolygonType.MPD)
    private val hiBuffers = ObjectOglBuffers(PolygonType.HI, zoomToHideMiscFeatures)
    private val tvsBuffers = ObjectOglBuffers(PolygonType.TVS, zoomToHideMiscFeatures)
    private val warningFfwBuffers = ObjectOglBuffers(PolygonType.FFW)
    private val warningTstBuffers = ObjectOglBuffers(PolygonType.TST)
    private val warningTorBuffers = ObjectOglBuffers(PolygonType.TOR)
    private val watchBuffers = ObjectOglBuffers(PolygonType.WATCH)
    private val watchTornadoBuffers = ObjectOglBuffers(PolygonType.WATCH_TORNADO)
    private val mcdBuffers = ObjectOglBuffers(PolygonType.MCD)
    private val swoBuffers = ObjectOglBuffers()
    private val userPointsBuffers = ObjectOglBuffers(PolygonType.USERPOINTS, zoomToHideMiscFeatures)
    private val locationDotBuffers = ObjectOglBuffers(PolygonType.LOCDOT, 0.0f)
    private val locIconBuffers = ObjectOglBuffers()
    private val locBugBuffers = ObjectOglBuffers()
    private val wbCircleBuffers = ObjectOglBuffers(PolygonType.WIND_BARB_CIRCLE, zoomToHideMiscFeatures)
    private val conusRadarBuffers = ObjectOglBuffers()
    private val genericWarningBuffers = mutableListOf<ObjectOglBuffers>()
    private var wpcFrontBuffersList = mutableListOf<ObjectOglBuffers>()
    private var wpcFrontPaints = mutableListOf<Int>()
    private val colorSwo = intArrayOf(Color.MAGENTA, Color.RED, Color.rgb(255, 140, 0), Color.YELLOW, Color.rgb(0, 100, 0))
    private var breakSize15 = 15000
    private val breakSizeRadar = 15000
    private var positionHandle = 0
    private var colorHandle = 0
    private var tdwr = false
    private var chunkCount = 0
    private var totalBins = 0
    private var totalBinsOgl = 0
    var gpsLatLonTransformed = floatArrayOf(0.0f, 0.0f)
    var displayHold = false
    var displayConus = false
    private var sizeHandle = 0
    private var iTexture: Int = 0

    private var conusradarId = -1
    private var userPointId = -1
    private var locationId = -1
    private var locationBugId = -1
    private var tvsId = -1
    private var hiId = -1



    var zoom = 1.0f
        set(scale) {
            field = scale
            listOf(locationDotBuffers, hiBuffers, spotterBuffers, tvsBuffers, wbCircleBuffers).forEach {
                if (it.isInitialized) {
                    it.lenInit = it.type.size
                    it.lenInit = scaleLength(it.lenInit)
                    it.draw(projectionNumbers)
                }
            }
            if (locationDotBuffers.isInitialized && MyApplication.locationDotFollowsGps) {
                locIconBuffers.lenInit = 0f //was locationDotBuffers.lenInit
                UtilityWXOGLPerf.genLocdot(locIconBuffers, projectionNumbers, gpsX, gpsY)
            }
        }
    private var surfaceRatio = 0f
    var x = 0f
        set(x) {
            field = x
            positionXGlobal = x
        }
    var y = 0f
        set(y) {
            field = y
            positionYGlobal = y
        }
    var rid = ""
        set(rid) {
            field = rid
            ridGlobal = rid
        }
    private var prod = "N0Q"
    private var defaultLineWidth = 1.0f  //was 2.0f
    private var ridPrefixGlobal = ""
    private var bgColorFRed = 0.0f
    private var bgColorFGreen = 0.0f
    private var bgColorFBlue = 0.0f
    val ortInt = 400
    var zoomScreenScaleFactor = 1.0
    private val projectionType = ProjectionType.WX_OGL
    // this controls if the projection is mercator (nexrad) or 4326 / rectangular
    // after you zoom out past a certain point you need to hide the nexrad, show the mosaic
    // and reconstruct all geometry and warning/watch lines using 4326 projection (set this variable to false to not use mercator transformation )
    // so far, only the base geometry ( state lines, county, etc ) respect this setting
    private var useMercatorProjection = true
    private val wxglNexradLevel2 = WXGLNexradLevel2()
    val wxglNexradLevel3 = WXGLNexradLevel3()
    private var projectionNumbers = ProjectionNumbers()
    var product: String
        get() = prod
        set(value) { prod = value }

    init {
        bgColorFRed = Color.red(MyApplication.nexradRadarBackgroundColor) / 255.0f
        bgColorFGreen = Color.green(MyApplication.nexradRadarBackgroundColor) / 255.0f
        bgColorFBlue = Color.blue(MyApplication.nexradRadarBackgroundColor) / 255.0f
        defaultLineWidth = MyApplication.radarDefaultLinesize.toFloat()
        try {
            triangleIndexBuffer = ByteBuffer.allocateDirect(12 * breakSize15)
            lineIndexBuffer = ByteBuffer.allocateDirect(4 * breakSizeLine)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (ooo: OutOfMemoryError) {
            UtilityLog.handleException(ooo)
        }
        triangleIndexBuffer.order(ByteOrder.nativeOrder())
        triangleIndexBuffer.position(0)
        lineIndexBuffer.order(ByteOrder.nativeOrder())
        lineIndexBuffer.position(0)
        if (!MyApplication.radarUseJni) {
            UtilityWXOGLPerf.generateIndex(triangleIndexBuffer, breakSize15, breakSize15)
            UtilityWXOGLPerf.generateIndexLine(lineIndexBuffer, breakSizeLine * 4, breakSizeLine * 2)
        } else {
            Jni.genIndex(triangleIndexBuffer, breakSize15, breakSize15)
            Jni.genIndexLine(lineIndexBuffer, breakSizeLine * 4, breakSizeLine * 2)
        }
        MyApplication.radarWarningPolygons.forEach { genericWarningBuffers.add(ObjectOglBuffers(it)) }
        if (UtilityUI.isTablet()) {
            zoomScreenScaleFactor = 2.0
        }
    }

    fun initializeGeometry() {
        totalBins = 0
        if (WXGLNexrad.isProductTdwr(prod)) {
            tdwr = true
            val oldRid = this.rid
            if (this.rid == "") {
                this.rid = oldRid
                tdwr = false
                prod = "N0Q"
            }
        }
        projectionNumbers = ProjectionNumbers(this.rid, projectionType)
        oneDegreeScaleFactorGlobal = projectionNumbers.oneDegreeScaleFactorFloat
    }

    // final arg is whether or not to perform decompression
    fun constructPolygons(fileName: String, urlStr: String, performDecomp: Boolean) {
        radarBuffers.fileName = fileName
        totalBins = 0
        // added to allow animations to skip a frame and continue
        if (WXGLNexrad.isProductTdwr(product)) {
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
        if (radarBuffers.fileName == "") {
            ridPrefixGlobal = WXGLDownload.getRadarFile(context, urlStr, this.rid, prod, indexString, tdwr)
            radarBuffers.fileName = if (!product.contains("L2")) {
                val l3BaseFn = "nids"
                l3BaseFn + indexString
            } else {
                "l2$indexString"
            }
        }
        radarBuffers.setProductCodeFromString(product)
        try {
            when {
                // Level 2
                product.contains("L2") -> {
                    wxglNexradLevel2.decodeAndPlot(context, radarBuffers.fileName, prod, radarStatusStr, indexString, performDecomp)
                    radarBuffers.extractL2Data(wxglNexradLevel2)
                }
                // 4bit products spectrum width, comp ref, storm relative mean velocity
                product.contains("NSW") || product.startsWith("NC") || product.matches(Regex("N[0-3]S")) -> {
                    wxglNexradLevel3.decodeAndPlotFourBit(context, radarBuffers.fileName, radarStatusStr)
                    radarBuffers.extractL3Data(wxglNexradLevel3)
                }
                // Level 3 8bit
                else -> {
                    wxglNexradLevel3.decodeAndPlot(context, radarBuffers.fileName, rid, radarStatusStr)
                    radarBuffers.extractL3Data(wxglNexradLevel3)
                }
            }
        } catch (e: Exception) { UtilityLog.handleException(e) }
        if (radarBuffers.numRangeBins == 0) {
            radarBuffers.numRangeBins = 460
            radarBuffers.numberOfRadials = 360
        }
        radarBuffers.initialize()
        radarBuffers.setToPositionZero()
        val objectColorPalette =
                if (MyApplication.colorMap.containsKey(radarBuffers.productCode.toInt())) {
                    MyApplication.colorMap[radarBuffers.productCode.toInt()]!!
                } else {
                    MyApplication.colorMap[94]!!
                }
        try {
            val fourBitProducts = listOf<Short>(56, 30, 181, 78, 80, 37, 38, 41, 57)
            if (product.startsWith("NC") || radarBuffers.productCode.toInt() == 41 || radarBuffers.productCode.toInt() == 57) {
                totalBins = UtilityWXOGLPerfRaster.generate(radarBuffers, wxglNexradLevel3.binWord)
            } else if (!product.contains("L2")) {
                totalBins = if (!fourBitProducts.contains(radarBuffers.productCode)) {
                    if (!MyApplication.radarUseJni)
                        UtilityWXOGLPerf.decode8BitAndGenRadials(context, radarBuffers)
                    else {
                        Jni.decode8BitAndGenRadials(
                                UtilityIO.getFilePath(context, radarBuffers.fileName),
                                wxglNexradLevel3.seekStart,
                                wxglNexradLevel3.compressedFileSize,
                                wxglNexradLevel3.iBuff,
                                wxglNexradLevel3.oBuff,
                                radarBuffers.floatBuffer,
                                radarBuffers.colorBuffer,
                                radarBuffers.binSize,
                                Color.red(radarBuffers.bgColor).toByte(),
                                Color.green(radarBuffers.bgColor).toByte(),
                                Color.blue(radarBuffers.bgColor).toByte(),
                                objectColorPalette.redValues,
                                objectColorPalette.greenValues,
                                objectColorPalette.blueValues
                        )
                    }
                } else {
                    UtilityWXOGLPerf.genRadials(radarBuffers, wxglNexradLevel3.binWord, wxglNexradLevel3.radialStart)
                }
            } else {
                wxglNexradLevel2.binWord.position(0)
                totalBins = if (MyApplication.radarUseJni)
                    Jni.level2GenRadials(
                            radarBuffers.floatBuffer,
                            radarBuffers.colorBuffer,
                            wxglNexradLevel2.binWord,
                            wxglNexradLevel2.radialStartAngle,
                            radarBuffers.numberOfRadials,
                            radarBuffers.numRangeBins,
                            radarBuffers.binSize,
                            radarBuffers.bgColor,
                            objectColorPalette.redValues,
                            objectColorPalette.greenValues,
                            objectColorPalette.blueValues,
                            radarBuffers.productCode.toInt()
                    )
                else
                    UtilityWXOGLPerf.genRadials(radarBuffers, wxglNexradLevel2.binWord, wxglNexradLevel2.radialStartAngle)
            } // level 2 , level 3 check
        } catch (e: Exception) { UtilityLog.handleException(e) }
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
        GLES20.glClearColor(bgColorFRed, bgColorFGreen, bgColorFBlue, 1.0f)
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

        //shader for conus
        OpenGLShader.sp_conus = GLES20.glCreateProgram()
        GLES20.glAttachShader(OpenGLShader.sp_conus, OpenGLShader.loadShader(GLES20.GL_VERTEX_SHADER, OpenGLShader.vs_conus))
        GLES20.glAttachShader(OpenGLShader.sp_conus, OpenGLShader.loadShader(GLES20.GL_FRAGMENT_SHADER, OpenGLShader.fs_conus))
        GLES20.glLinkProgram(OpenGLShader.sp_conus)

    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glUseProgram(OpenGLShader.sp_SolidColor)
        GLES20.glClearColor(bgColorFRed, bgColorFGreen, bgColorFBlue, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_SolidColor, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_SolidColor, "a_Color")
        GLES20.glEnableVertexAttribArray(positionHandle)
        // required for color on VBO basis
        GLES20.glEnableVertexAttribArray(colorHandle)
        matrixProjectionAndView = matrixProjectionAndViewOrig
        Matrix.multiplyMM(matrixProjectionAndView, 0, matrixProjection, 0, matrixView, 0)
        if (!MyApplication.wxoglCenterOnLocation) {
            Matrix.translateM(matrixProjectionAndView, 0, x, y, 0.0f)
        } else {
            Matrix.translateM(matrixProjectionAndView, 0, gpsLatLonTransformed[0] * zoom, gpsLatLonTransformed[1] * zoom, 0.0f)
        }
        Matrix.scaleM(matrixProjectionAndView, 0, zoom, zoom, 1.0f)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_SolidColor, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
	
        //show/hide radar
        UtilityLog.d("radarshow", "showradar: " + MyApplication.radarShowRadar)
        if (MyApplication.radarShowRadar) {
        (0 until chunkCount).forEach {
            radarChunkCnt = if (it < chunkCount - 1) {
                breakSizeRadar * 6
            } else {
                6 * (totalBinsOgl - it * breakSizeRadar)
            }
            try {
                radarBuffers.floatBuffer.position(it * breakSizeRadar * 32)
                GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, radarBuffers.floatBuffer.slice().asFloatBuffer())
                radarBuffers.colorBuffer.position(it * breakSizeRadar * 12)
                GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, radarBuffers.colorBuffer.slice())
                triangleIndexBuffer.position(0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, radarChunkCnt, GLES20.GL_UNSIGNED_SHORT, triangleIndexBuffer.slice().asShortBuffer())
            } catch (e: Exception) { UtilityLog.handleException(e) }
        }
        } //show/hide radar
        GLES20.glLineWidth(defaultLineWidth)
        listOf(countyLineBuffers, stateLineBuffers, hwBuffers, hwExtBuffers, lakeBuffers).forEach {
            if (zoom > it.scaleCutOff) {
                GLES20.glLineWidth(it.geotype.lineWidth.toFloat())
                drawElement(it)
            }
        }
        // whether or not to respect the display being touched needs to be stored in object gl buffers
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
                    GLES20.glLineWidth(it.type.size)
                    drawPolygons(it, 16)
                }
            }
            listOf(wbCircleBuffers).forEach {
                if (zoom > it.scaleCutOff) {
                    drawTriangles(it)
                }
            }

            GLES20.glLineWidth(defaultLineWidth)

        //drawTriangles(locationDotBuffers)
        //drawLocation(locationDotBuffers)


            if (MyApplication.radarUserPoints) {
                if (zoom > userPointsBuffers.scaleCutOff) {
                    drawUserPoints(userPointsBuffers)
                }
            }


            if (MyApplication.locationDotFollowsGps) {
                locIconBuffers.chunkCount = 1
                drawLocation(locIconBuffers)
            } else {
	        //drawTriangles(wbCircleBuffers)
            //GLES20.glLineWidth(defaultLineWidth)
            // FIXME use new configurable
            GLES20.glLineWidth(MyApplication.radarGpsCircleLineSize.toFloat())
            drawTriangles(locationDotBuffers)
            }


        if (MyApplication.locdotBug)  {
            Log.i(TAG, "bearing: " + WXGLRadarActivity.bearingCurrent)
            Log.i(TAG, "speed: " + WXGLRadarActivity.speedCurrent)
            if (WXGLRadarActivity.speedCurrent >= 0.43) {
                //set up location bug
                Log.i(TAG, "location bug!!!!")
                drawLocationBug(locBugBuffers)
            }
        }
        GLES20.glLineWidth(PolygonType.TOR.size)
        listOf(warningTstBuffers, warningFfwBuffers, warningTorBuffers).forEach { drawPolygons(it, 8) }
        genericWarningBuffers.forEach { if (it.warningType!!.isEnabled) drawPolygons(it, 8) }
        GLES20.glLineWidth(PolygonType.WATCH_TORNADO.size)
        listOf(mpdBuffers, mcdBuffers, watchBuffers, watchTornadoBuffers).forEach { drawPolygons(it, 8) }
        GLES20.glLineWidth(PolygonType.SWO.size)
        listOf(swoBuffers).forEach { drawPolygons(it, 8) }
        if (zoom < (0.50 / zoomScreenScaleFactor)) {
            GLES20.glLineWidth(MyApplication.radarWatchMcdLineSize)
            wpcFrontBuffersList.forEach { drawElement(it) }
        }



    } //displayhold


/*
        //TODO try to use real plotting without adding usa map....
        //hack job!!!
        if (!displayHold) {
            Log.i(TAG, "zoom: " + zoom)
            Log.i(TAG, "zoom setting: "+MyApplication.radarConusRadarZoom+ " math: "+(MyApplication.radarConusRadarZoom / 1000.0))
            if (MyApplication.radarConusRadar) {
                if (zoom < (MyApplication.radarConusRadarZoom / 1000.0).toFloat()) {
                    Log.i(TAG, "zoom out to conusradar")
                    drawConusRadarTest(conusRadarBuffers)
                }
            }
        }
        */

        if (displayConus) {
            drawConusRadarTest(conusRadarBuffers)
        }
        //TODO try to use real plotting without adding usa map....
        //hack job!!!
        if (!displayHold) {
            UtilityLog.d("wx", "zoom: " + zoom)
            UtilityLog.d("wx", "zoom setting: "+MyApplication.radarConusRadarZoom+ " math: "+(MyApplication.radarConusRadarZoom / 1000.0))
            if (MyApplication.radarConusRadar) {
                if (zoom < (MyApplication.radarConusRadarZoom / 1000.0).toFloat()) {
                    UtilityLog.d("wx", "zoom out to conusradar")
                    displayConus = true
                } else { displayConus = false }
            }
        }


    }





    private fun drawConusRadarTest(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()

            var vertexBuffer: FloatBuffer
            var drawListBuffer: ShortBuffer
            var uvBuffer: FloatBuffer


            //use conus shader
            GLES20.glUseProgram(OpenGLShader.sp_conus)



            val conusbitmap: Bitmap? = OpenGLShader.LoadBitmap(MyApplication.FilesPath + MyApplication.conusImageName)
            val ridx = Utility.readPref(context, "RID_" + rid + "_X", "0.0f").toFloat()
            val ridy = Utility.readPref(context, "RID_" + rid + "_Y", "0.0f").toFloat() / -1.0
            UtilityLog.d("wx", rid + " rid x: " + ridx + " y: " + ridy)
/*

            UtilityLog.d("wx", "gfw1: " + UtilityConusRadar.gfw1)
            UtilityLog.d("wx", "gfw2: " + UtilityConusRadar.gfw2)
            UtilityLog.d("wx", "gfw3: " + UtilityConusRadar.gfw3)
            UtilityLog.d("wx", "gfw4: " + UtilityConusRadar.gfw4)
            UtilityLog.d("wx", "gfw5: " + UtilityConusRadar.gfw5)
            UtilityLog.d("wx", "gfw6: " + UtilityConusRadar.gfw6)
            degreesPerPixellon = UtilityConusRadar.gfw1.toDouble()
            degreesPerPixellat = UtilityConusRadar.gfw4.toDouble()
            west = UtilityConusRadar.gfw5.toDouble()
            north = UtilityConusRadar.gfw6.toDouble()
            south = north + conusbitmap!!.height.toDouble() * degreesPerPixellat
            east = west + conusbitmap!!.width.toDouble() * degreesPerPixellon


            UtilityLog.d("wx", "north: " + north)
            UtilityLog.d("wx", "south: " + south)
            UtilityLog.d("wx", "west: " + west)
            UtilityLog.d("wx", "east: " + east)

            //from aweather
            //https://github.com/Andy753421/AWeather
            val awest = UtilityConusRadar.gfw5.toDouble()
            val anorth = UtilityConusRadar.gfw6.toDouble()
            val asouth = anorth - UtilityConusRadar.gfw1.toDouble() * conusbitmap.height.toDouble()
            val aeast = awest + UtilityConusRadar.gfw1.toDouble() * conusbitmap.width.toDouble()

            val midofwest = awest + UtilityConusRadar.gfw1.toDouble() * conusbitmap.width.toDouble() / 2
            val midofsouth = anorth - UtilityConusRadar.gfw1.toDouble() * conusbitmap.height.toDouble() / 2

            UtilityLog.d("wx", "awest: " + awest)
            UtilityLog.d("wx", "anorth: " + anorth)
            UtilityLog.d("wx", "asouth: " + asouth)
            UtilityLog.d("wx", "aeast: " + aeast)
            UtilityLog.d("wx", "midofwest: " + midofwest)
            UtilityLog.d("wx", "midofsouth: " + midofsouth)
*/



            /*
            val mRatio = conusbitmap.width / conusbitmap.height
            val mLeft = awest.toFloat()
            val mBottom = asouth.toFloat()
            val mTop = anorth.toFloat()
            val near = 1.0f
            val far = 10.0f
            Matrix.frustumM(matrixProjectionAndView, 0, mLeft, mRatio.toFloat(), mBottom, mTop, near, far)
            */


            //val riddist = LatLon.distance(LatLon(ridx.toDouble(), ridy.toDouble()), LatLon(south, west), DistanceUnit.MILE)
            //UtilityLog.d("wx", "riddist: " + riddist)
            //getNewConusPoint(south, west, riddist)


            /*

	gchar *clear = g_malloc0(2048*2048*4);
	glBindTexture(GL_TEXTURE_2D, tile->tex);

	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	glPixelStorei(GL_PACK_ALIGNMENT, 1);
	glTexImage2D(GL_TEXTURE_2D, 0, 4, 2048, 2048, 0,GL_RGBA, GL_UNSIGNED_BYTE, clear);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 1,1, CONUS_WIDTH/2,CONUS_HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
	tile->coords.n = 1.0/(CONUS_WIDTH/2);
	tile->coords.w = 1.0/ CONUS_HEIGHT;
	tile->coords.s = tile->coords.n +  CONUS_HEIGHT   / 2048.0;
	tile->coords.e = tile->coords.w + (CONUS_WIDTH/2) / 2048.0;
	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	glFlush();
	g_free(clear);

             */


            //triangle
            val base = RectF(-conusbitmap!!.width.toFloat(), conusbitmap!!.height.toFloat(), conusbitmap!!.width.toFloat(), -conusbitmap!!.height.toFloat())
            val scale = 3.0f //was 2.0f

            UtilityLog.d("wx", "left: " + base.left)
            UtilityLog.d("wx", "right: " + base.right)
            UtilityLog.d("wx", "bottom: " + base.bottom)
            UtilityLog.d("wx", "top: " + base.top)

            val left = base.left * scale
            val right = base.right * scale
            val bottom = base.bottom * scale
            val top = base.top * scale

            /*
            val westnorth = LatLon(west, north)
            val westsouth = LatLon(west, south)
            val eastsouth = LatLon(east, south)
            val eastnorth = LatLon(east, north)

            UtilityLog.d("wx", "westnorth: " + westnorth)
            UtilityLog.d("wx", "westsouth: " + westsouth)
            UtilityLog.d("wx", "eastsouth: " + eastsouth)
            UtilityLog.d("wx", "eastnorth: " + eastnorth)
            */


            val vertices = floatArrayOf(
                    left, top, 0.0f,
                    left, bottom, 0.0f,
                    right, bottom, 0.0f,
                    right, top, 0.0f)


            val indices = shortArrayOf(0, 1, 2, 0, 2, 3) // The order of vertexrendering.

            // The vertex buffer.
            val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
            vbb.order(ByteOrder.nativeOrder())
            vertexBuffer = vbb.asFloatBuffer()
            vertexBuffer.put(vertices)
            vertexBuffer.position(0)

            // initialize byte buffer for the draw list
            val dlb = ByteBuffer.allocateDirect(indices.size * 2)
            dlb.order(ByteOrder.nativeOrder())
            drawListBuffer = dlb.asShortBuffer()
            drawListBuffer.put(indices)
            drawListBuffer.position(0)


            //texture
            val uvs = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)

            // The texture buffer
            val tbb = ByteBuffer.allocateDirect(uvs.size * 4)
            tbb.order(ByteOrder.nativeOrder())
            uvBuffer = tbb.asFloatBuffer()
            uvBuffer.put(uvs)
            uvBuffer.position(0)
            OpenGLShader.LoadImage(MyApplication.FilesPath + MyApplication.conusImageName)

            val mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_conus, "vPosition")
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

            val mTexCoordLoc = GLES20.glGetAttribLocation(OpenGLShader.sp_conus, "a_texCoords")
            GLES20.glEnableVertexAttribArray(mTexCoordLoc)
            GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, uvBuffer)
            val mtrxhandle = GLES20.glGetUniformLocation(OpenGLShader.sp_conus, "uMVPMatrix")
            GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, matrixProjectionAndView, 0)
            val conusTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_conus, "u_texture")
            GLES20.glUniform1i(conusTexture, 0)
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle)
            GLES20.glDisableVertexAttribArray(mTexCoordLoc)
            //back to regular shader
            GLES20.glUseProgram(OpenGLShader.sp_SolidColor)

        }

    }




//point sprite blah//
    private fun drawConusRadar(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            //var conusbitmap: Bitmap? = OpenGLShader.LoadBitmap(MyApplication.FilesPath + "conus.gif")

            GLES20.glUniform1f(sizeHandle, 1600f) //was 1600f
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            //val conusbitmap: Bitmap? = ///UtilityConusRadar.nwsConusRadar(context)
            conusradarId = OpenGLShader.LoadTexture(MyApplication.FilesPath + MyApplication.conusImageName)
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(positionHandle)
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

    private fun drawUserPoints(buffers: ObjectOglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(sizeHandle, MyApplication.radarUserPointSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            userPointId = OpenGLShader.LoadTexture(MyApplication.FilesPath + "userpoint.png")
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, userPointId)
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
                positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
                sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
                GLES20.glUniform1f(sizeHandle, MyApplication.radarLocIconSize.toFloat())
                iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
                locationId = OpenGLShader.LoadTexture(MyApplication.FilesPath + "location.png")
                GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                GLES20.glEnableVertexAttribArray(positionHandle)
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
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(sizeHandle, MyApplication.radarLocBugSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            val rotatebug: Bitmap = OpenGLShader.RotateBitmap(MyApplication.FilesPath + "headingbug.png", WXGLRadarActivity.bearingCurrent.toDouble())
            locationBugId = OpenGLShader.LoadBitmapTexture(rotatebug)
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(positionHandle)
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
                positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
                sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
                GLES20.glUniform1f(sizeHandle, MyApplication.radarTvsSize.toFloat())
                iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
                tvsId = OpenGLShader.LoadTexture(MyApplication.FilesPath + "tvs.png")
                GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                GLES20.glEnableVertexAttribArray(positionHandle)
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
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(sizeHandle, MyApplication.radarHiSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            hiId = OpenGLShader.LoadTexture(MyApplication.FilesPath + hailSizeIcon)
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glEnableVertexAttribArray(positionHandle)
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
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, buffers.colorBuffer.slice().asFloatBuffer())
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
        }
    }

    private fun drawPolygons(buffers: ObjectOglBuffers, countDivisor: Int) {
        if (buffers.isInitialized) {
            // FIXME is chunk count ever above one? "it" is never reference in the loop
            (0 until buffers.chunkCount).forEach { _ ->
                lineIndexBuffer.position(0)
                buffers.setToPositionZero()
                GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
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
                    GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
                    GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, buffers.colorBuffer.slice())
                    GLES20.glDrawElements(GLES20.GL_LINES, lineCnt, GLES20.GL_UNSIGNED_SHORT, lineIndexBuffer.slice().asShortBuffer())
                } catch (e: Exception) { }
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceRatio = width.toFloat() / height
        (0..15).forEach {
            matrixProjection[it] = 0.0f
            matrixView[it] = 0.0f
            matrixProjectionAndView[it] = 0.0f
        }
        Matrix.orthoM(matrixProjection, 0, (-1 * ortInt).toFloat(), ortInt.toFloat(), -1.0f * ortInt.toFloat() * (1 / surfaceRatio),
                ortInt * (1 / surfaceRatio), 1.0f, -1.0f)
        Matrix.setLookAtM(matrixView, 0, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        Matrix.multiplyMM(matrixProjectionAndView, 0, matrixProjection, 0, matrixView, 0)
        Matrix.multiplyMM(matrixProjectionAndViewOrig, 0, matrixProjection, 0, matrixView, 0)
        if (!MyApplication.wxoglCenterOnLocation) {
            Matrix.translateM(matrixProjectionAndView, 0, x, y, 0.0f)
        } else {
            Matrix.translateM(matrixProjectionAndView, 0, gpsLatLonTransformed[0] * zoom, gpsLatLonTransformed[1] * zoom, 0.0f)
        }
        Matrix.scaleM(matrixProjectionAndView, 0, zoom, zoom, 1.0f)
    }

    private fun scaleLength(currentLength: Float) = if (zoom > 1.01f) currentLength / zoom * 2 else currentLength

    fun constructStateLines() = constructGenericGeographic(stateLineBuffers)

    fun constructHWLines() = constructGenericGeographic(hwBuffers)

    fun deconstructHWLines() = deconstructGenericGeographic(hwBuffers)

    fun constructHWEXTLines() = constructGenericGeographic(hwExtBuffers)

    fun deconstructHWEXTLines() = deconstructGenericGeographic(hwExtBuffers)

    fun constructLakes() = constructGenericGeographic(lakeBuffers)

    fun deconstructLakes() = deconstructGenericGeographic(lakeBuffers)

    fun constructCounty() = constructGenericGeographic(countyLineBuffers)

    fun deconstructCounty() = deconstructGenericGeographic(countyLineBuffers)

    // if the rectangular projection is realized.
    private fun constructGenericGeographic(buffers: ObjectOglBuffers) {
        if (!buffers.isInitialized) {
            buffers.count = buffers.geotype.count
            buffers.breakSize = 30000
            buffers.initialize(4 * buffers.count, 0, 3 * buffers.breakSize * 2, buffers.geotype.color)
            if (MyApplication.radarUseJni) {
                Jni.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            } else {
                UtilityWXOGLPerf.colorGen(buffers.colorBuffer, buffers.breakSize * 2, buffers.colorArray)
            }
            buffers.isInitialized = true
        }
        if (!MyApplication.radarUseJni) {
            if (useMercatorProjection) {
                UtilityWXOGLPerf.genMercator(buffers.geotype.relativeBuffer, buffers.floatBuffer, projectionNumbers, buffers.count)
            } else {
                UtilityWXOGLPerf.generate4326Projection(buffers.geotype.relativeBuffer, buffers.floatBuffer, projectionNumbers, buffers.count)
            }
        } else {
            if (useMercatorProjection) {
                Jni.genMercato(
                        buffers.geotype.relativeBuffer,
                        buffers.floatBuffer,
                        projectionNumbers.xFloat,
                        projectionNumbers.yFloat,
                        projectionNumbers.xCenter.toFloat(),
                        projectionNumbers.yCenter.toFloat(),
                        projectionNumbers.oneDegreeScaleFactorFloat,
                        buffers.count
                )
            } else {
                // This is not used at the moment
                UtilityWXOGLPerf.generate4326Projection(buffers.geotype.relativeBuffer, buffers.floatBuffer, projectionNumbers, buffers.count)
            }
        }
        buffers.setToPositionZero()
    }

    private fun deconstructGenericGeographic(buffers: ObjectOglBuffers) { buffers.isInitialized = false }

    private fun constructGenericLinesShort(buffers: ObjectOglBuffers, list: List<Double>) {
        val remainder: Int
        buffers.initialize(4 * 4 * list.size, 0, 3 * 4 * list.size, buffers.type.color)
        try {
            if (MyApplication.radarUseJni) {
                Jni.colorGen(buffers.colorBuffer, 4 * list.size, buffers.colorArray)
            } else {
                UtilityWXOGLPerf.colorGen(buffers.colorBuffer, 4 * list.size, buffers.colorArray)
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
                buffers.putFloat(list[vList + 1].toFloat() * -1)
                buffers.putFloat(list[vList + 2].toFloat())
                buffers.putFloat(list[vList + 3].toFloat() * -1)
                vList += 4
            }
        }
        buffers.isInitialized = true
    }

    fun constructStiLines() {
        constructGenericLinesShort(stiBuffers, WXGLNexradLevel3StormInfo.decodeAndPlot(context, indexString, projectionNumbers))
    }

    fun deconstructStiLines() { deconstructGenericLines(stiBuffers) }

    fun constructWatchMcdLines() {
        constructGenericLines(mcdBuffers)
        constructGenericLines(watchBuffers)
        constructGenericLines(watchTornadoBuffers)
    }

    fun deconstructWatchMcdLines() {
        deconstructGenericLines(mcdBuffers)
        deconstructGenericLines(watchBuffers)
        deconstructGenericLines(watchTornadoBuffers)
    }

    fun constructWarningLines() {
        constructGenericLines(warningTstBuffers)
        constructGenericLines(warningTorBuffers)
        constructGenericLines(warningFfwBuffers)
    }

    fun deconstructWarningLines() {
        deconstructGenericLines(warningTstBuffers)
        deconstructGenericLines(warningTorBuffers)
        deconstructGenericLines(warningFfwBuffers)
    }

    fun constructGenericWarningLines() {
        genericWarningBuffers.forEach {
            if (it.warningType!!.isEnabled) {
                constructGenericLines(it)
            } else {
                deconstructGenericLines(it)
            }
        }
    }

    fun constructLocationDot(locXCurrent: String, locYCurrentF: String, archiveMode: Boolean) {
        var locYCurrent = locYCurrentF
        var locationMarkers = mutableListOf<Double>()
	
        if (MyApplication.locationDotFollowsGps) {
            locationDotBuffers.lenInit = 0f
        } else {
            locationDotBuffers.lenInit = MyApplication.radarLocdotSize.toFloat()
        }
        locYCurrent = locYCurrent.replace("-", "")
        val x = locXCurrent.toDoubleOrNull() ?: 0.0
        val y = locYCurrent.toDoubleOrNull() ?: 0.0
        if (PolygonType.LOCDOT.pref) {
            locationMarkers = UtilityLocation.latLonAsDouble
        }
        if (MyApplication.locationDotFollowsGps || archiveMode) {
            locationMarkers.add(x)
            locationMarkers.add(y)
            gpsX = x
            gpsY = y
        }
        locationDotBuffers.xList = locationMarkers.filterIndexed { index: Int, _: Double -> index.isEven() }.toDoubleArray()
        locationDotBuffers.yList = locationMarkers.filterIndexed { index: Int, _: Double -> !index.isEven() }.toDoubleArray()
        locationDotBuffers.triangleCount = 12
        constructTriangles(locationDotBuffers)



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


        /* not needed .. have custom location icon
        if (MyApplication.radarUseJni) {
            Jni.colorGen(
                    locCircleBuffers.colorBuffer,
                    2 * locCircleBuffers.triangleCount,
                    locCircleBuffers.colorArray
            )
        } else {
            UtilityWXOGLPerf.colorGen(
                    locCircleBuffers.colorBuffer,
                    2 * locCircleBuffers.triangleCount,
                    locCircleBuffers.colorArray
            )
        }
        */


        if (MyApplication.locationDotFollowsGps) {
            locIconBuffers.lenInit = locationDotBuffers.lenInit
            val gpsCoordinates = UtilityCanvasProjection.computeMercatorNumbers(gpsX, gpsY, projectionNumbers)
            gpsLatLonTransformed[0] = -gpsCoordinates[0].toFloat()
            gpsLatLonTransformed[1] = gpsCoordinates[1].toFloat()


            UtilityWXOGLPerf.genLocdot(locIconBuffers, projectionNumbers, gpsX, gpsY)
            
	    
	    
	    
	    
	    
	    
	    //location bug//
            if (MyApplication.locdotBug) {
                locBugBuffers.lenInit = 0f
                UtilityWXOGLPerf.genLocdot(locBugBuffers, projectionNumbers, gpsX, gpsY)
            }

        }



        locationDotBuffers.isInitialized = true
        locIconBuffers.isInitialized = true
        locBugBuffers.isInitialized = true
    }

    fun deconstructLocationDot() {
        locationDotBuffers.isInitialized = false
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

        /*
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
        */

        conusRadarBuffers.isInitialized = true
    }

    fun deconstructConusRadar() {
        conusRadarBuffers.isInitialized = false
    }

    fun constructSpotters() {
        spotterBuffers.isInitialized = false //leave it at false or the app will crash randomly
        spotterBuffers.lenInit = MyApplication.radarSpotterSize.toFloat()
        spotterBuffers.triangleCount = 6
        UtilitySpotter.get(context)
        spotterBuffers.xList = UtilitySpotter.x
        spotterBuffers.yList = UtilitySpotter.y
        constructTriangles(spotterBuffers)
    }

    fun deconstructSpotters() {
        spotterBuffers.isInitialized = false
    }

    fun constructHi() {
        hiBuffers.lenInit = 0f //MyApplication.radarHiSize.toFloat()
        val stormList = WXGLNexradLevel3HailIndex.decodeAndPlot(context, rid, indexString)
        hiBuffers.setXYList(stormList)
        WXGLNexradLevel3HailIndex.hailList
        constructMarker(hiBuffers)
    }

    fun deconstructHi() {
        hiBuffers.isInitialized = false
    }


    fun constructUserPoints() {
        userPointsBuffers.lenInit = 0f
        UtilityUserPoints.userPointsData
        userPointsBuffers.xList = UtilityUserPoints.x
        userPointsBuffers.yList = UtilityUserPoints.y
        constructMarker(userPointsBuffers)
    }

    fun deconstructUserPoints() {
        userPointsBuffers.isInitialized = false
    }

    private fun constructTriangles(buffers: ObjectOglBuffers) {
        buffers.count = buffers.xList.size
        val count = buffers.count * buffers.triangleCount
        when (buffers.type) {
            PolygonType.LOCDOT, PolygonType.SPOTTER -> buffers.initialize(24 * count, 12 * count, 9 * count, buffers.type.color)
            //PolygonType.LOCDOT, PolygonType.SPOTTER -> buffers.initialize(24 * count, 12 * count, 9 * count, Color.RED)
            else -> buffers.initialize(4 * 6 * buffers.count, 4 * 3 * buffers.count, 9 * buffers.count, buffers.type.color)
        }
        buffers.lenInit = scaleLength(buffers.lenInit)
        buffers.draw(projectionNumbers)
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
            buffers.draw(projectionNumbers)
            buffers.isInitialized = true
        }
    }

    fun constructTvs() {
        tvsBuffers.lenInit = 0f //MyApplication.radarTvsSize.toFloat()
        tvsBuffers.setXYList(WXGLNexradLevel3TVS.decodeAndPlot(context, rid, indexString))
        constructMarker(tvsBuffers)

    }

    fun deconstructTvs() {
        tvsBuffers.isInitialized = false
    }

    fun constructMpdLines() = constructGenericLines(mpdBuffers)

    fun deconstructMpdLines() = deconstructGenericLines(mpdBuffers)

    private fun constructGenericLines(buffers: ObjectOglBuffers) {
        var list = listOf<Double>()
        when (buffers.type) {
            PolygonType.MCD, PolygonType.MPD, PolygonType.WATCH, PolygonType.WATCH_TORNADO -> list = UtilityWatch.add(projectionNumbers, buffers.type).toList()
            PolygonType.TST, PolygonType.TOR, PolygonType.FFW -> list = WXGLPolygonWarnings.add(projectionNumbers, buffers.type).toList()
            PolygonType.STI -> list = WXGLNexradLevel3StormInfo.decodeAndPlot(context, indexString, projectionNumbers).toList()
            else -> if (buffers.warningType != null) list = WXGLPolygonWarnings.addGeneric(projectionNumbers, buffers.warningType!!).toList()
        }
        buffers.breakSize = 15000
        buffers.chunkCount = 1
        val totalBinsGeneric = list.size / 4
        var remainder = 0
        if (totalBinsGeneric < buffers.breakSize) {
            buffers.breakSize = totalBinsGeneric
            remainder = buffers.breakSize
        } else if (buffers.breakSize > 0) {
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
        if (MyApplication.radarUseJni) {
            Jni.colorGen(buffers.colorBuffer, 4 * totalBinsGeneric, buffers.colorArray)
        } else {
            UtilityWXOGLPerf.colorGen(buffers.colorBuffer, 4 * totalBinsGeneric, buffers.colorArray)
        }
        var vList = 0
        (0 until buffers.chunkCount).forEach {
            if (it == buffers.chunkCount - 1) {
                buffers.breakSize = remainder
            }
            for (notUsed in 0 until buffers.breakSize) {
                if (list.size > (vList + 3)) {
                    buffers.putFloat(list[vList].toFloat())
                    buffers.putFloat(list[vList + 1].toFloat() * -1.0f)
                    buffers.putFloat(list[vList + 2].toFloat())
                    buffers.putFloat(list[vList + 3].toFloat() * -1.0f)
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
        constructGenericLinesShort(wbBuffers, WXGLNexradLevel3WindBarbs.decodeAndPlot(rid, projectionType, false, paneNumber))
        constructWBLinesGusts()
        constructWBCircle()
    }

    private fun constructWBLinesGusts() {
        constructGenericLinesShort(wbGustsBuffers, WXGLNexradLevel3WindBarbs.decodeAndPlot(rid, projectionType, true, paneNumber))
    }

    fun deconstructWBLines() {
        wbBuffers.isInitialized = false
        deconstructWBLinesGusts()
        deconstructWBCircle()
    }

    fun constructWpcFronts() {
        wpcFrontBuffersList = mutableListOf()
        wpcFrontPaints = mutableListOf()
        var coordinates: DoubleArray
        val fronts = UtilityWpcFronts.fronts.toMutableList()
        fronts.forEach { _ ->
            val buff = ObjectOglBuffers()
            buff.breakSize = 30000
            buff.chunkCount = 1
            wpcFrontBuffersList.add(buff)
        }
        fronts.indices.forEach { z ->
            val front = fronts[z]
            wpcFrontBuffersList[z].count = front.coordinates.size * 2
            wpcFrontBuffersList[z].initialize(4 * wpcFrontBuffersList[z].count, 0, 3 * wpcFrontBuffersList[z].count)
            wpcFrontBuffersList[z].isInitialized = true
            when (front.type) {
                FrontTypeEnum.COLD -> wpcFrontPaints.add(Color.rgb(0, 127, 255))
                FrontTypeEnum.WARM -> wpcFrontPaints.add(Color.rgb(255, 0, 0))
                FrontTypeEnum.STNRY -> wpcFrontPaints.add(Color.rgb(0, 127, 255))
                FrontTypeEnum.STNRY_WARM -> wpcFrontPaints.add(Color.rgb(255, 0, 0))
                FrontTypeEnum.OCFNT -> wpcFrontPaints.add(Color.rgb(255, 0, 255))
                FrontTypeEnum.TROF -> wpcFrontPaints.add(Color.rgb(254, 216, 177))
            }
            for (j in 0 until front.coordinates.size step 2) {
                if ( j < front.coordinates.size - 1) { // stationary front workaround
                    coordinates = UtilityCanvasProjection.computeMercatorNumbers(front.coordinates[j].lat, front.coordinates[j].lon, projectionNumbers)
                    wpcFrontBuffersList[z].putFloat(coordinates[0].toFloat())
                    wpcFrontBuffersList[z].putFloat((coordinates[1] * -1.0f).toFloat())
                    wpcFrontBuffersList[z].putColor(Color.red(wpcFrontPaints[z]).toByte())
                    wpcFrontBuffersList[z].putColor(Color.green(wpcFrontPaints[z]).toByte())
                    wpcFrontBuffersList[z].putColor(Color.blue(wpcFrontPaints[z]).toByte())
                    coordinates = UtilityCanvasProjection.computeMercatorNumbers(front.coordinates[j + 1].lat, front.coordinates[j + 1].lon, projectionNumbers)
                    wpcFrontBuffersList[z].putFloat(coordinates[0].toFloat())
                    wpcFrontBuffersList[z].putFloat((coordinates[1] * -1.0f).toFloat())
                    wpcFrontBuffersList[z].putColor(Color.red(wpcFrontPaints[z]).toByte())
                    wpcFrontBuffersList[z].putColor(Color.green(wpcFrontPaints[z]).toByte())
                    wpcFrontBuffersList[z].putColor(Color.blue(wpcFrontPaints[z]).toByte())
                }
            }
        }
    }

    fun deconstructWpcFronts() {
        wpcFrontBuffersList = mutableListOf()
    }

    private fun deconstructWBLinesGusts() {
        wbGustsBuffers.isInitialized = false
    }

    private fun constructWBCircle() {
        wbCircleBuffers.lenInit = MyApplication.radarAviationSize.toFloat()
        wbCircleBuffers.xList = UtilityMetar.metarDataList[paneNumber].x
        wbCircleBuffers.yList = UtilityMetar.metarDataList[paneNumber].y
        wbCircleBuffers.colorIntArray = UtilityMetar.metarDataList[paneNumber].obsArrAviationColor
        wbCircleBuffers.count = wbCircleBuffers.xList.size
        wbCircleBuffers.triangleCount = 6
        val count = wbCircleBuffers.count * wbCircleBuffers.triangleCount
        wbCircleBuffers.initialize(24 * count, 12 * count, 9 * count)
        wbCircleBuffers.lenInit = scaleLength(wbCircleBuffers.lenInit)
        wbCircleBuffers.draw(projectionNumbers)
        wbCircleBuffers.isInitialized = true
    }

    private fun deconstructWBCircle() {
        wbCircleBuffers.isInitialized = false
    }

    fun constructSwoLines() {
        val hashSwo = UtilitySwoDayOne.HASH_SWO.toMap()
        var coordinates: DoubleArray
        val fSize = (0..4).filter { hashSwo[it] != null }.sumBy { hashSwo.getOrElse(it) { listOf() }.size }
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
            if (hashSwo[it] != null) {
                for (j in hashSwo.getOrElse(it) { listOf() }.indices step 4) {
                    swoBuffers.putColor(Color.red(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.green(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.blue(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.red(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.green(colorSwo[it]).toByte())
                    swoBuffers.putColor(Color.blue(colorSwo[it]).toByte())
                    coordinates = UtilityCanvasProjection.computeMercatorNumbers(hashSwo.getOrElse(it) { listOf() }[j], (hashSwo.getOrElse(it) { listOf() }[j + 1] * -1.0f), projectionNumbers)
                    swoBuffers.putFloat(coordinates[0].toFloat())
                    swoBuffers.putFloat(coordinates[1].toFloat() * -1.0f)
                    coordinates = UtilityCanvasProjection.computeMercatorNumbers(hashSwo.getOrElse(it) { listOf() }[j + 2], (hashSwo.getOrElse(it) { listOf() }[j + 3] * -1.0f), projectionNumbers)
                    swoBuffers.putFloat(coordinates[0].toFloat())
                    swoBuffers.putFloat(coordinates[1].toFloat() * -1.0f)
                }
            }
        }
    }

    fun deconstructSwoLines() {
        swoBuffers.isInitialized = false
    }

    fun setHiInit(hiInit: Boolean) {
        hiBuffers.isInitialized = hiInit
    }

    fun setTvsInit(tvsInit: Boolean) {
        tvsBuffers.isInitialized = tvsInit
    }

    val oneDegreeScaleFactor: Float
        get() = projectionNumbers.oneDegreeScaleFactorFloat

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
