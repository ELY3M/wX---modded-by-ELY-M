package joshuatee.wx.radar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.location.Location
import android.opengl.GLUtils
import android.os.Environment
import android.util.Log
import joshuatee.wx.MyApplication
import joshuatee.wx.util.ProjectionNumbers
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import javax.microedition.khronos.opengles.GL11Ext



class IconLoad(sf: Int) {
    internal val LOG_TAG = "iconload"
    private val iconfile = "test.png"
    private val cropRect = IntArray(4)
    internal var radarLocation: Location? = null
    private val scalingFactor: Float
    private val textures = IntArray(1)
    private var texturesLoaded = false

    init {
        this.scalingFactor = 0.5f * sf.toFloat()
        FillCropRect()
    }

    private fun FillCropRect() {
        Log.i(this.LOG_TAG, "FillCropRect()")
        val opts = Options()
        opts.inScaled = false
        val f = File(MyApplication.FilesPath + iconfile)
        if (f.exists()) {
            val bitmap = BitmapFactory.decodeFile(f.absolutePath, opts)
            this.cropRect[0] = 0
            this.cropRect[1] = bitmap.height
            this.cropRect[2] = bitmap.width
            this.cropRect[3] = -bitmap.height
            return
        }
        Log.e(this.LOG_TAG, "Unable to find texture " + f.absolutePath)
    }

    private fun loadGLTexture(gl: GL10) {
        Log.d(this.LOG_TAG, "Loading Textures")
        this.texturesLoaded = false
        if (this.textures[0] != 0) {
            invalidateTexture(gl)
        }
        val opts = Options()
        opts.inScaled = false
        val f = File(MyApplication.FilesPath + iconfile)
        if (f.exists()) {
            val bitmap = BitmapFactory.decodeFile(f.absolutePath, opts)
            this.cropRect[0] = 0
            this.cropRect[1] = bitmap.height
            this.cropRect[2] = bitmap.width
            this.cropRect[3] = -bitmap.height
            this.textures[0] = loadTextureFromBitmapFast(gl, bitmap)
            bitmap.recycle()
            this.texturesLoaded = true
            return
        }
        Log.e(this.LOG_TAG, "Unable to find texture " + f.absolutePath)
    }


    fun drawIcon(gl: GL10, x: Int, y: Int) {
        Log.i(LOG_TAG, "drawIcon")
            if (!texturesLoaded) {
                Log.i(LOG_TAG, "!textureloaded")
                loadGLTexture(gl)
            }
            beginDrawing(gl)
            draw(gl, x, y)
            endDrawing(gl)
    }

    fun unloadTextures(lastKnownGL2: GL10) {
        if (this.textures[0] != 0) {
            invalidateTexture(lastKnownGL2)
        }
    }

    private fun invalidateTexture(gl: GL10?) {
        this.texturesLoaded = false
        if (gl != null) {
            val len = this.textures.size
            Log.d(this.LOG_TAG, ">>>>>Invalidate Textures (Count $len)<<<<<")
            gl.glDeleteTextures(len, this.textures, 0)
            for (i in 0 until len) {
                this.textures[i] = 0
            }
        }
    }

    private fun loadTextureFromBitmapFast(gl: GL10, bitmap: Bitmap): Int {
        Log.i(LOG_TAG, "loadTextureFromBitmapFast")
        val tex = IntArray(1)
        gl.glGenTextures(1, tex, 0)
        gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, tex[0])
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE.toFloat())
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        Log.d(LOG_TAG, "Bitmap Loaded.  Width=" + bitmap.width + " Height=" + bitmap.height)
        return tex[0]
    }

    fun beginDrawing(gl: GL10) {
        Log.i(LOG_TAG, "beginDrawing")
        gl.glBindTexture(GL10.GL_TEXTURE_2D, this.textures[0])
        gl.glShadeModel(GL10.GL_FLAT)
        gl.glEnable(GL10.GL_BLEND)
        gl.glColor4x(0, 0, 0, 0)
    }

    fun draw(gl: GL10, x: Int, y: Int) {
        Log.i(LOG_TAG, "draw")
        gl.glEnable(GL10.GL_TEXTURE_2D)
        (gl as GL11).glTexParameteriv(GL10.GL_TEXTURE_2D, 35741, this.cropRect, 0)
        (gl as GL11Ext).glDrawTexiOES(x, y, 0, this.cropRect[2], -this.cropRect[3])
        gl.glDisable(GL10.GL_TEXTURE_2D)
    }

    private fun endDrawing(gl: GL10) {
        Log.i(LOG_TAG, "endDrawing")
        gl.glDisable(GL10.GL_BLEND)
    }



    fun genImage(gl: GL10, buffers: ObjectOglBuffers, x: Int, y: Int) {
        buffers.setToPositionZero()
        var icon: IconLoad = IconLoad(10)
        icon.drawIcon(gl, x,y)


    }



}
