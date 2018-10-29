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

import android.content.Context
import android.graphics.Bitmap.Config
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*
import java.nio.file.Files.exists
import android.provider.MediaStore.Images.Media.getBitmap
import android.content.ContentValues.TAG
import android.util.Log
import java.io.File
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.FileNotFoundException
import android.provider.MediaStore.Images.Media.getBitmap
import android.R.attr.bitmap
import android.graphics.*
import android.opengl.ETC1Util.createTexture
import joshuatee.wx.util.ProjectionNumbers
import kotlin.math.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.opengl.GLES10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10


object UtilityIcons {

    var TAG: String = "joshuatee UtilityIcons"

    var unloadTextures: Boolean = false;


    fun sample() {
        var bitmap: Bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        var canvas: Canvas = Canvas(bitmap)
        var paint: Paint = Paint()
        paint.setColor(Color.CYAN)
        paint.setTextSize(62f)
        paint.setStyle(Paint.Style.FILL)
        canvas.drawText("SAAMPLE!!!!!!!!!!!!!!!!!!103!!!!!!!!!!!!!!!!!", 32f, 32f, paint)

    }


    ///https://stackoverflow.com/questions/36419134/fast-way-to-load-bitmap-and-make-a-texture-in-android
    fun setlocation() {
        var texture: Int = 0;
        var bitmap: Bitmap? = null

        val options = BitmapFactory.Options()
        options.inScaled = false
        val file = File(MyApplication.FilesPath + "location.png")
        try {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        // upload texture by bitmap
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap!!.recycle()
    }

    fun TestLocation() {
        var texture: Int = 0;
        var bitmap: Bitmap
        val sourceBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(sourceBitmap)
        val textPaint = Paint()
        textPaint.setTextSize(100f)
        textPaint.setAntiAlias(true)
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.CYAN);
        canvas.drawText("TESTING!!!!!!!!103!!!!!!", 16f, 40f, textPaint)
        val textureHandles = IntArray(1)
        bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_4444)


        val canvas1 = Canvas(bitmap)
        //val matrix = Matrix()
        //matrix.setScale(1, -1, sourceBitmap.width / 2, sourceBitmap.height / 2)
        canvas1.drawBitmap(sourceBitmap, 300f, 300f, Paint())
        sourceBitmap.recycle()
        GLES20.glGenTextures(1, textureHandles, 0)
        texture = textureHandles[0]
        if (texture !== -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap!!.recycle()
        }
        if (texture == -1) {
            Log.e(TAG, "Failed to create texture.")

        }
    }

    fun simple() {
        val options = BitmapFactory.Options()
        options.inScaled = false
        val file = File(MyApplication.FilesPath + "location.png")
        val img = BitmapFactory.decodeFile(file.getAbsolutePath(), options)
        val texId = IntArray(1)
        GLES20.glGenTextures(1, texId, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId[0])
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
    }


    fun LocdotOld(buffers: ObjectOglBuffers) {
        buffers.setToPositionZero()

        Log.i(TAG, "LocDot...");
        var texture: Int = 0;
        var bitmap: Bitmap
        val sourceBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sourceBitmap)
        val textPaint = Paint()
        textPaint.setTextSize(100f)
        textPaint.setAntiAlias(true)
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.CYAN);
        canvas.drawText("TESTING!!!!!!!!103!!!!!!", 250f, 250f, textPaint)
        val textureHandles = IntArray(1)
        bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)


        val canvas1 = Canvas(bitmap)
        //val matrix = Matrix()
        //matrix.setScale(1, -1, sourceBitmap.width / 2, sourceBitmap.height / 2)
        canvas1.drawBitmap(sourceBitmap, 500f, 500f, Paint())
        sourceBitmap.recycle()
        GLES20.glGenTextures(1, textureHandles, 0)
        texture = textureHandles[0]
        if (texture !== -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap!!.recycle()
        }
        if (texture == -1) {
            Log.e(TAG, "Failed to create texture.")

        }
    }


    //LOAD location.png
    fun loadlocation(): Int {
        val textureHandle = IntArray(1)

        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] != 0) {
            val options = BitmapFactory.Options()
            options.inScaled = false   // No pre-scaling

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            // Read in the resource
            //TODO add in check for location.png
            val bitmap: Bitmap = BitmapFactory.decodeFile(MyApplication.FilesPath + "location.png")
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap!!.recycle()
        }

        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }

        return textureHandle[0]
    }


    fun Locdot(buffers: ObjectOglBuffers) {
        buffers.setToPositionZero()

        val textureHandle = IntArray(1)

        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val options = BitmapFactory.Options()
            options.inScaled = false   // No pre-scaling

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            // Read in the resource
            //TODO add in check for location.png
            val bitmap: Bitmap = BitmapFactory.decodeFile(MyApplication.FilesPath + "location.png")
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap!!.recycle()
        }
        if (textureHandle[0] == -1) {
            throw RuntimeException("Error loading texture.")
        }
    }


    fun loadicon(img: String): Int {
        var textures: IntArray
        textures = IntArray(1)
        GLES10.glEnable(GL10.GL_TEXTURE_2D)
        GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        GLES10.glGenTextures(1, textures, 0)
        GLES10.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
        GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST.toFloat())
        GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat());
        GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat());
        var bitmap: Bitmap = BitmapFactory.decodeFile(img)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        Log.i(TAG, "loadicon textures: "+textures[0])
        return textures[0]
}

    private val VERTEX_COORDINATES = floatArrayOf(-1.0f, +1.0f, 0.0f, +1.0f, +1.0f, 0.0f, -1.0f, -1.0f, 0.0f, +1.0f, -1.0f, 0.0f)

    private val TEXTURE_COORDINATES = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)

    private val TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind()
    private val VERTEX_BUFFER = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind()



    fun runicon(textures: Int) {
        Log.i(TAG, "runicon textures: "+textures)
        GLES10.glActiveTexture(GL10.GL_TEXTURE0)
        GLES10.glBindTexture(GL10.GL_TEXTURE_2D, textures)
        GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
        GLES10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
        GLES10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)

    }

/*
    fun createTexture(bitmap: Bitmap): Int {
        val texture = createTexture(bitmap)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        Log.i(TAG, "texImage2D")
        return texture
    }
*/



    //pykl3

    fun loadBitmaps() {

        Log.d(TAG, "Load Bitmaps called")


        Log.d(TAG, "Loading new bitmaps")

        val options = BitmapFactory.Options()
        var bitmap: Bitmap? = null
        options.inScaled = false
        val file = File(MyApplication.FilesPath + "location.png")

        if (file.exists()) {
            val decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath(), options)
            if (decodeFile != null) {
                bitmap = Bitmap.createScaledBitmap(decodeFile, 63, 63, false)
                decodeFile.recycle()
            } else {
                Log.e(TAG, "decodefile is null")
            }

            return
        }
        Log.e(TAG, "Unable to find texture ")
        return
    }



    fun UnloadTextures() {
        unloadTextures = true
    }


    fun TestIcon(x: Double, y: Double)  {
        var textureHandle: Int = 0;
        var bitmap: Bitmap
        val sourceBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(sourceBitmap)
        val textPaint = Paint()
        textPaint.setTextSize(100f)
        textPaint.setAntiAlias(true)
        textPaint.setARGB(0,0,255,255)
        canvas.drawText("TESTING!!!!!!!!103!!!!!!", 16f, 40f, textPaint)
        val textureHandles = IntArray(1)
        bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_4444)


        val canvas1 = Canvas(bitmap)
        //val matrix = Matrix()
        //matrix.setScale(1, -1, sourceBitmap.width / 2, sourceBitmap.height / 2)
        canvas1.drawBitmap(sourceBitmap, 200f, 200f, Paint())
        sourceBitmap.recycle()
        GLES20.glGenTextures(1, textureHandles, 0)
        textureHandle = textureHandles[0]
        //val textureHandle = createTexture(bitmap)
        if (textureHandle !== -1) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
        if (textureHandle == -1) {
            Log.e(TAG, "Failed to create texture.")

        }
    }


}
