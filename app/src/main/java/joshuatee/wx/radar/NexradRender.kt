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
import joshuatee.wx.Jni
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.ProjectionNumbers
import android.graphics.Bitmap
import android.graphics.RectF
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class NexradRender(private val context: Context, val paneNumber: Int) : Renderer {

    //
    // The is the OpenGL rendering engine that is used on the main screen and the main radar interface
    // The goal is to be highly performant and configurable as such this module *used* to rely on C code accessed via JNI extensively
    // Kotlin can also be used in set in settings->radar and has been the default since 2017 as the performance is quite good
    // and it's much easier to debug issue
    //

    companion object {
        //elys mod
        //used for conus radar
        var degreesPerPixellat = -0.017971305190311 //had -
        var degreesPerPixellon = 0.017971305190311
        var north = 0.toDouble()
        var south = 0.toDouble()
        var west = 0.toDouble()
        var east = 0.toDouble()
        var newbottom = 0.toDouble()
        var newleft = 0.toDouble()
    }
    val data = NexradRenderData(context)
    val state = NexradRenderState(paneNumber, data, ::scaleLength)
    private val matrixProjection = FloatArray(16)
    private val matrixView = FloatArray(16)
    private var matrixProjectionAndView = FloatArray(16)
    private var radarChunkCnt = 0
    private var lineCnt = 0
    private val breakSizeLine = 30000
    private val matrixProjectionAndViewOrig = FloatArray(16)
    private var triangleIndexBuffer = ByteBuffer.allocate(0)
    private var lineIndexBuffer = ByteBuffer.allocate(0)
    private var breakSize15 = 15000
    private val breakSizeRadar = 15000
    private var positionHandle = 0
    private var colorHandle = 0
    private var chunkCount = 0
    private var totalBins = 0
    private var totalBinsOgl = 0
    //elys mod
    var displayConus = false
    private var sizeHandle = 0
    private var iTexture: Int = 0

    private var conusradarId = -1
    private var userPointId = -1
    private var locationId = -1
    private var locationBugId = -1
    private var tvsId = -1
    private var hiId = -1

    //elys mod
    private var defaultLineWidth = 1.0f  //was 2.0f
    private val wxglNexradLevel2 = NexradLevel2()
    val wxglNexradLevel3 = NexradLevel3()
    val construct = NexradRenderConstruct(context, state, data, ::scaleLength)

    init {
        initializeIndexBuffers()
	    //elys mod
        defaultLineWidth = RadarPreferences.defaultLinesize.toFloat()
        PolygonWarning.polygonList.forEach {
            data.warningBuffers[it] = OglBuffers(PolygonWarning.byType[it]!!)
        }
    }

    private fun initializeIndexBuffers() {
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
        if (!RadarPreferences.useJni) {
            NexradRenderUtilities.generateIndex(triangleIndexBuffer, breakSize15, breakSize15)
            NexradRenderUtilities.generateIndexLine(lineIndexBuffer, breakSizeLine * 4, breakSizeLine * 2)
        } else {
            Jni.genIndex(triangleIndexBuffer, breakSize15, breakSize15)
            Jni.genIndexLine(lineIndexBuffer, breakSizeLine * 4, breakSizeLine * 2)
        }
    }

    // compute projection numbers based of LAT/LON of radar site
    fun initializeGeometry() {
        totalBins = 0
        if (NexradUtil.isProductTdwr(state.product)) {
            // not sure how we would get in a situation where rid == "" and a TDWR product is selected
            // but no harm to leave it around for now
            val oldRid = state.rid
            if (state.rid == "") {
                state.rid = oldRid
                state.product = "N0Q"
            }
        }
        state.projectionNumbers = ProjectionNumbers(state.rid, state.projectionType)
        NexradRenderState.oneDegreeScaleFactorGlobal = state.projectionNumbers.oneDegreeScaleFactorFloat
    }

    // download/decode radar file
    // 2nd to final arg is whether or not to perform decompression
    // final arg is only used for level2 archive radar files from SPC Storm reports (deprecated)
    @Synchronized fun constructPolygons(fileName: String, performDecomp: Boolean, urlStr: String = "") {
        totalBins = 0
        NexradRenderRadar.downloadRadarFile(context, data, state, fileName, urlStr)
        NexradRenderRadar.decodeRadarHeader(context, data, state, wxglNexradLevel2, wxglNexradLevel3, performDecomp)
        totalBins = NexradRenderRadar.createRadials(context, data, state, wxglNexradLevel2, wxglNexradLevel3)
        breakSize15 = 15000
        chunkCount = 1
        if (totalBins < breakSize15) {
            breakSize15 = totalBins
        } else {
            chunkCount = totalBins / breakSize15
            chunkCount += 1
        }
        data.radarBuffers.setToPositionZero()
        totalBinsOgl = totalBins
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        state.bgColorFRed = Color.red(RadarPreferences.nexradBackgroundColor) / 255.0f
        state.bgColorFGreen = Color.green(RadarPreferences.nexradBackgroundColor) / 255.0f
        state.bgColorFBlue = Color.blue(RadarPreferences.nexradBackgroundColor) / 255.0f
        data.radarBuffers.bgColor = RadarPreferences.nexradBackgroundColor
        GLES20.glClearColor(state.bgColorFRed, state.bgColorFGreen, state.bgColorFBlue, 1.0f)
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
	
	    //elys mod
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
        GLES20.glClearColor(state.bgColorFRed, state.bgColorFGreen, state.bgColorFBlue, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_SolidColor, "vPosition")
        colorHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_SolidColor, "a_Color")
        GLES20.glEnableVertexAttribArray(positionHandle)
        // required for color on VBO basis
        GLES20.glEnableVertexAttribArray(colorHandle)
        matrixProjectionAndView = matrixProjectionAndViewOrig
        Matrix.multiplyMM(matrixProjectionAndView, 0, matrixProjection, 0, matrixView, 0)
        if (!RadarPreferences.wxoglCenterOnLocation) {
            Matrix.translateM(matrixProjectionAndView, 0, state.x, state.y, 0.0f)
        } else {
            Matrix.translateM(matrixProjectionAndView, 0, state.gpsLatLonTransformed[0] * state.zoom, state.gpsLatLonTransformed[1] * state.zoom, 0.0f)
        }
        Matrix.scaleM(matrixProjectionAndView, 0, state.zoom, state.zoom, 1.0f)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_SolidColor, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
        //
        // Draw Nexrad radar
        //
	    //elys mod
        //show/hide radar
        UtilityLog.d("radarshow", "displayHold: " + state.displayHold)
        UtilityLog.d("radarshow", "showRadarWhenPan: " + RadarPreferences.showRadarWhenPan)
        UtilityLog.d("radarshow", "hideradar: " + RadarPreferences.hideRadar)
        if ((!RadarPreferences.hideRadar) && (!(state.displayHold && !RadarPreferences.showRadarWhenPan))) {
        //org
        //if (!(state.displayHold && !RadarPreferences.showRadarWhenPan)) {
            for (it in 0 until chunkCount) {
                radarChunkCnt = if (it < chunkCount - 1) {
                    breakSizeRadar * 6
                } else {
                    6 * (totalBinsOgl - it * breakSizeRadar)
                }
                try {
                    data.radarBuffers.floatBuffer.position(it * breakSizeRadar * 32)
                    GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, data.radarBuffers.floatBuffer.slice().asFloatBuffer())
                    data.radarBuffers.colorBuffer.position(it * breakSizeRadar * 12)
                    GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, data.radarBuffers.colorBuffer.slice())
                    triangleIndexBuffer.position(0)
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES, radarChunkCnt, GLES20.GL_UNSIGNED_SHORT, triangleIndexBuffer.slice().asShortBuffer())
                } catch (e: Exception) { UtilityLog.handleException(e) }
            }
        }
        //
        // Geography
        //
        RadarGeometry.orderedTypes.forEach {
            if (RadarGeometry.dataByType[it]!!.isEnabled && state.zoom > data.geographicBuffers[it]!!.scaleCutOff) {
                GLES20.glLineWidth(RadarGeometry.dataByType[it]!!.lineSize)
                drawElement(data.geographicBuffers[it]!!)
            }
        }
        // whether or not to respect the display being touched needs to be stored in object gl buffers
        if (!state.displayHold) {
	
	
            //elys mod - hailmod
	        //Custom Hail Icons
            data.hiBuffersList.forEach {
                if (state.zoom > data.zoomToHideMiscFeatures) {
                    drawHI(it, it.hailIcon)
                }
            }
	    
	        //elys mod
	        //Custom TVS Icon
            listOf(data.tvsBuffers).forEach {
            if (state.zoom > it.scaleCutOff) {
            drawTVS(it)
            }
            }

            GLES20.glLineWidth(3.0f)
            //
            // storm tracks, wind barbs
            //
            listOf(data.stiBuffers).forEach {
                if (it.type.pref && state.zoom > it.scaleCutOff) {
                    GLES20.glLineWidth(it.type.size)
                    drawPolygons(it, 8)
                }
            }
            listOf(data.wbGustsBuffers, data.wbBuffers).forEach {
                if (it.type.pref && state.zoom > it.scaleCutOff) {
                    GLES20.glLineWidth(it.type.size)
                    drawPolygons(it, 16)
                }
            }    
            //elys mod
            // spotters, wb circles
            //
            listOf(data.spotterBuffers, data.wbCircleBuffers).forEach {
                if (it.type.pref && state.zoom > it.scaleCutOff) {
                    drawTriangles(it)
                }
            }	    	
            // elys mod
            // UserPoints
	        //
            if (RadarPreferences.userPoints) {
                listOf(data.userPointsBuffers).forEach {
                ///if (state.zoom > data.userPointsBuffers.scaleCutOff) {
		        if (it.type.pref && state.zoom > it.scaleCutOff) {
                    drawUserPoints(it)
                }
                }
            }	
	
	
            //
            // location dots
	        //
            //elys mod
            if (RadarPreferences.locationDotFollowsGps) {
                data.locIconBuffers.chunkCount = 1
                drawLocation(data.locIconBuffers)
            } else {
            GLES20.glLineWidth(RadarPreferences.gpsCircleLineSize.toFloat())
            drawTriangles(data.locationDotBuffers)
            }


	    //elys mod
	    //Location bug like in Pykl3   
        if (RadarPreferences.locdotBug)  {
            if (NexradUI.speedCurrent >= 0.43) {
                //set up location bug
                drawLocationBug(data.locBugBuffers)
            }
        }
        //
        // warnings
        //
        GLES20.glLineWidth(PolygonType.TOR.size)
        data.warningBuffers.values.forEach {
            if (it.warningType!!.isEnabled) {
                drawPolygons(it, 8)
            }
        }
        //
        // MCD, Watch
        //
        if (PolygonType.MCD.pref) {
            GLES20.glLineWidth(PolygonType.WATCH_TORNADO.size)
            listOf(PolygonType.MCD, PolygonType.WATCH, PolygonType.WATCH_TORNADO).forEach {
                drawPolygons(data.polygonBuffers[it]!!, 8)
            }
        }
        //
        // MPD
        //
        if (PolygonType.MPD.pref) {
            GLES20.glLineWidth(PolygonType.WATCH_TORNADO.size)
            drawPolygons(data.polygonBuffers[PolygonType.MPD]!!, 8)
        }
        //
        // SPC Convective Outlook
        //
        if (PolygonType.SWO.pref) {
            GLES20.glLineWidth(PolygonType.SWO.size)
            drawPolygons(data.swoBuffers, 8)
        }
        //
        // WPC Fronts
        //
        if (PolygonType.WPC_FRONTS.pref && state.zoom < (0.50 / state.zoomScreenScaleFactor)) {
            GLES20.glLineWidth(PolygonType.WPC_FRONTS.size)
            data.wpcFrontBuffersList.forEach {
                drawElement(it)
            }
        }



        } //displayHold

//TODO  remove this conus radar....   
//elys mod
////Conus Radar
/*
        //TODO try to use real plotting without adding usa map....
        //hack job!!!
        if (!displayHold) {
            Log.i(TAG, "zoom: " + zoom)
            Log.i(TAG, "zoom setting: "+RadarPreferences.radarConusRadarZoom+ " math: "+(RadarPreferences.radarConusRadarZoom / 1000.0))
            if (RadarPreferences.radarConusRadar) {
                if (state.zoom < (RadarPreferences.radarConusRadarZoom / 1000.0).toFloat()) {
                    Log.i(TAG, "zoom out to conusradar")
                    drawConusRadarTest(conusRadarBuffers)
                }
            }
        }
        */

        if (displayConus) {
            drawConusRadarTest(data.conusRadarBuffers)
        } 
        //TODO try to use real plotting without adding usa map....
        //hack job!!!
        if (!state.displayHold) {
            //UtilityLog.d("wx-elys", "zoom: " + state.zoom)
            //UtilityLog.d("wx-elys", "zoom setting: "+RadarPreferences.conusRadarZoom+ " math: "+(RadarPreferences.conusRadarZoom / 1000.0))
            if (RadarPreferences.conusRadar) {
                if (state.zoom < (RadarPreferences.conusRadarZoom / 1000.0).toFloat()) {
                    UtilityLog.d("wx-elys", "zoom out to conusradar")
                    displayConus = true
                } else { 
		displayConus = false 
		}
            }
        }
//////////////////Conus Radar End////////////////





    } //onDrawFrame(gl: GL10) End


//elys mod
////////Conus Radar///////////
private fun drawConusRadarTest(buffers: OglBuffers) {
    if (buffers.isInitialized) {
        buffers.setToPositionZero()

        var vertexBuffer: FloatBuffer
        var drawListBuffer: ShortBuffer
        var uvBuffer: FloatBuffer


        //use conus shader
        GLES20.glUseProgram(OpenGLShader.sp_conus)



        val conusbitmap: Bitmap? = OpenGLShader.LoadBitmap(GlobalVariables.FilesPath + GlobalVariables.conusImageName)
        val ridx = Utility.readPref(context, "RID_" + state.rid + "_X", "0.0f").toFloat()
        val ridy = Utility.readPref(context, "RID_" + state.rid + "_Y", "0.0f").toFloat() / -1.0
        UtilityLog.d("wx-elys", state.rid + " rid x: " + ridx + " y: " + ridy)
/*

            UtilityLog.d("wx-elys", "gfw1: " + UtilityConusRadar.gfw1)
            UtilityLog.d("wx-elys", "gfw2: " + UtilityConusRadar.gfw2)
            UtilityLog.d("wx-elys", "gfw3: " + UtilityConusRadar.gfw3)
            UtilityLog.d("wx-elys", "gfw4: " + UtilityConusRadar.gfw4)
            UtilityLog.d("wx-elys", "gfw5: " + UtilityConusRadar.gfw5)
            UtilityLog.d("wx-elys", "gfw6: " + UtilityConusRadar.gfw6)
            degreesPerPixellon = UtilityConusRadar.gfw1.toDouble()
            degreesPerPixellat = UtilityConusRadar.gfw4.toDouble()
            west = UtilityConusRadar.gfw5.toDouble()
            north = UtilityConusRadar.gfw6.toDouble()
            south = north + conusbitmap!!.height.toDouble() * degreesPerPixellat
            east = west + conusbitmap!!.width.toDouble() * degreesPerPixellon


            UtilityLog.d("wx-elys", "north: " + north)
            UtilityLog.d("wx-elys", "south: " + south)
            UtilityLog.d("wx-elys", "west: " + west)
            UtilityLog.d("wx-elys", "east: " + east)

            //from aweather
            //https://github.com/Andy753421/AWeather
            val awest = UtilityConusRadar.gfw5.toDouble()
            val anorth = UtilityConusRadar.gfw6.toDouble()
            val asouth = anorth - UtilityConusRadar.gfw1.toDouble() * conusbitmap.height.toDouble()
            val aeast = awest + UtilityConusRadar.gfw1.toDouble() * conusbitmap.width.toDouble()

            val midofwest = awest + UtilityConusRadar.gfw1.toDouble() * conusbitmap.width.toDouble() / 2
            val midofsouth = anorth - UtilityConusRadar.gfw1.toDouble() * conusbitmap.height.toDouble() / 2

            UtilityLog.d("wx-elys", "awest: " + awest)
            UtilityLog.d("wx-elys", "anorth: " + anorth)
            UtilityLog.d("wx-elys", "asouth: " + asouth)
            UtilityLog.d("wx-elys", "aeast: " + aeast)
            UtilityLog.d("wx-elys", "midofwest: " + midofwest)
            UtilityLog.d("wx-elys", "midofsouth: " + midofsouth)
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
        //UtilityLog.d("wx-elys", "riddist: " + riddist)
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
        val base = RectF(-conusbitmap!!.width.toFloat(), conusbitmap.height.toFloat(), conusbitmap.width.toFloat(), -conusbitmap.height.toFloat())
        val scale = 3.0f //was 2.0f

        UtilityLog.d("wx-elys", "left: " + base.left)
        UtilityLog.d("wx-elys", "right: " + base.right)
        UtilityLog.d("wx-elys", "bottom: " + base.bottom)
        UtilityLog.d("wx-elys", "top: " + base.top)

        val left = base.left * scale
        val right = base.right * scale
        val bottom = base.bottom * scale
        val top = base.top * scale

        /*
        val westnorth = LatLon(west, north)
        val westsouth = LatLon(west, south)
        val eastsouth = LatLon(east, south)
        val eastnorth = LatLon(east, north)

        UtilityLog.d("wx-elys", "westnorth: " + westnorth)
        UtilityLog.d("wx-elys", "westsouth: " + westsouth)
        UtilityLog.d("wx-elys", "eastsouth: " + eastsouth)
        UtilityLog.d("wx-elys", "eastnorth: " + eastnorth)
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
        OpenGLShader.LoadImage(GlobalVariables.FilesPath + GlobalVariables.conusImageName)

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
//////////Conus Radar End/////////////////////




//elys mod
//point sprite blah//
    private fun drawConusRadar(buffers: OglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            //var conusbitmap: Bitmap? = OpenGLShader.LoadBitmap(RadarPreferences.FilesPath + "conus.gif")

            GLES20.glUniform1f(sizeHandle, 1600f) //was 1600f
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            //val conusbitmap: Bitmap? = ///UtilityConusRadar.nwsConusRadar(context)
            conusradarId = OpenGLShader.LoadTexture(GlobalVariables.FilesPath + GlobalVariables.conusImageName)
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

    private fun drawUserPoints(buffers: OglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(sizeHandle, RadarPreferences.userPointSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            userPointId = OpenGLShader.LoadTexture(GlobalVariables.FilesPath + "userpoint.png")
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

    private fun drawLocation(buffers: OglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
                GLES20.glUseProgram(OpenGLShader.sp_loadimage)
                positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
                sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
                GLES20.glUniform1f(sizeHandle, RadarPreferences.locIconSize.toFloat())
                iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
                locationId = OpenGLShader.LoadTexture(GlobalVariables.FilesPath + "location.png")
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

    private fun drawLocationBug(buffers: OglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(sizeHandle, RadarPreferences.locBugSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            val rotatebug: Bitmap = OpenGLShader.RotateBitmap(GlobalVariables.FilesPath + "headingbug.png", NexradUI.bearingCurrent.toDouble())
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



    private fun drawTVS(buffers: OglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
                GLES20.glUseProgram(OpenGLShader.sp_loadimage)
                positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
                GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
                sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
                GLES20.glUniform1f(sizeHandle, RadarPreferences.tvsSize.toFloat())
                iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
                tvsId = OpenGLShader.LoadTexture(GlobalVariables.FilesPath + "tvs.png")
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

    //elys mod - hailmod
    private fun drawHI(buffers: OglBuffers, hailicon: String) {
        if (buffers.isInitialized) {
            Log.i("drawhi", "start of drawhi")
            buffers.setToPositionZero()
            GLES20.glUseProgram(OpenGLShader.sp_loadimage)
            positionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_loadimage, "vPosition")
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "uMVPMatrix"), 1, false, matrixProjectionAndView, 0)
            sizeHandle = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "imagesize")
            GLES20.glUniform1f(sizeHandle, RadarPreferences.hiSize.toFloat())
            iTexture = GLES20.glGetUniformLocation(OpenGLShader.sp_loadimage, "u_texture")
            hiId = OpenGLShader.LoadTexture(GlobalVariables.FilesPath + hailicon)
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

        Log.i("drawhi", "hailicon: " + buffers.hailIcon)
    }

    // FIXME CRASHING HERE sometimes -- FIXED via "displayhold" code
    /*
        java.lang.IllegalArgumentException: Must use a native order direct Buffer
        at android.opengl.GLES20.glVertexAttribPointerBounds(Native Method)
        at android.opengl.GLES20.glVertexAttribPointer(GLES20.java:1906)
        at joshuatee.wx.radar.WXGLRender.drawTriangles(WXGLRender.kt:388)
        at joshuatee.wx.radar.WXGLRender.onDrawFrame(WXGLRender.kt:359)
    * */
///////////  
    private fun drawTriangles(buffers: OglBuffers) {
        if (buffers.isInitialized) {
            buffers.setToPositionZero()
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, buffers.floatBuffer.slice().asFloatBuffer())
            GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, buffers.colorBuffer.slice().asFloatBuffer())
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, buffers.floatBuffer.capacity() / 8, GLES20.GL_UNSIGNED_SHORT, buffers.indexBuffer.slice().asShortBuffer())
        }
    }

    private fun drawPolygons(buffers: OglBuffers, countDivisor: Int) {
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

    private fun drawElement(buffers: OglBuffers) {
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
        val surfaceRatio = width.toFloat() / height
        for (it in 0..15) {
            matrixProjection[it] = 0.0f
            matrixView[it] = 0.0f
            matrixProjectionAndView[it] = 0.0f
        }
        Matrix.orthoM(matrixProjection, 0, (-1.0f * state.ortInt), state.ortInt.toFloat(), -1.0f * state.ortInt * (1.0f / surfaceRatio),
                state.ortInt * (1.0f / surfaceRatio), 1.0f, -1.0f)
        Matrix.setLookAtM(matrixView, 0, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        Matrix.multiplyMM(matrixProjectionAndView, 0, matrixProjection, 0, matrixView, 0)
        Matrix.multiplyMM(matrixProjectionAndViewOrig, 0, matrixProjection, 0, matrixView, 0)
        if (!RadarPreferences.wxoglCenterOnLocation) {
            Matrix.translateM(matrixProjectionAndView, 0, state.x, state.y, 0.0f)
        } else {
            Matrix.translateM(matrixProjectionAndView, 0, state.gpsLatLonTransformed[0] * state.zoom, state.gpsLatLonTransformed[1] * state.zoom, 0.0f)
        }
        Matrix.scaleM(matrixProjectionAndView, 0, state.zoom, state.zoom, 1.0f)
    }

    private fun scaleLength(currentLength: Float): Float = if (state.zoom > 1.01f) {
        currentLength / state.zoom * 2.0f
    } else {
        currentLength
    }

//TODO move....        
//elys mod 
//conus radar
/*
    fun constructConusRadar() {
        data.conusRadarBuffers.lenInit = 0f
        data.conusRadarBuffers.isInitialized = true
    }


    fun deconstructConusRadar() {
        data.conusRadarBuffers.isInitialized = false
    }
*/
    fun setChunkCount(chunkCount: Int) {
        this.chunkCount = chunkCount
    }

    fun setViewInitial(zoom: Float, x: Float, y: Float) {
        state.zoom = zoom
        state.x = x
        state.y = y
    }
}
