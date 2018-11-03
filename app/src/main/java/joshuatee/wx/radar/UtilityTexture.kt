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


import android.util.Log
import android.opengl.GLES20
import android.opengl.GLUtils
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.opengl.Matrix
import joshuatee.wx.MyApplication
import org.intellij.lang.annotations.Language
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10





object UtilityTexture {

    var TAG: String = "joshuatee UtilityTexture"

    //var unloadTextures: Boolean = false;


    fun loadimage(gl: GL10, imagefile: String): Int {
        val options = BitmapFactory.Options()
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val textures = IntArray(1)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glGenTextures(1, textures, 0)
        gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        val bitmap = BitmapFactory.decodeFile(imagefile, options)
        if (bitmap != null) {
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        } else {
            Log.i(TAG, "bitmap is null")
        }
        return textures[0]
    }

    val VERTEX_COORDINATES = floatArrayOf(-1.0f, +1.0f, 0.0f, +1.0f, +1.0f, 0.0f, -1.0f, -1.0f, 0.0f, +1.0f, -1.0f, 0.0f)

    val TEXTURE_COORDINATES = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)

    val TEXCOORD_BUFFER = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDINATES).rewind()
    val VERTEX_BUFFER = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDINATES).rewind()


    fun drawimage(gl: GL10, texture: Int) {
        gl.glActiveTexture(GL10.GL_TEXTURE0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, VERTEX_BUFFER)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, TEXCOORD_BUFFER)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
    }

////////////////////////////////////////////////////////////////////////////////////////////////
// Geometric variables


    var vertices: FloatArray = floatArrayOf(10.0f, 200f, 0.0f, 10.0f, 100f, 0.0f, 100f, 100f, 0.0f, 100f, 200f, 0.0f)
    var indices: ShortArray = shortArrayOf(0, 1, 2, 0, 2, 3)
    var uvs: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)

/*
    private fun Render(m: FloatArray) {
        var uvBuffer: FloatBuffer
        // The texture buffer
        val bb = ByteBuffer.allocateDirect(uvs.size * 4)
        bb.order(ByteOrder.nativeOrder())
        uvBuffer = bb.asFloatBuffer()
        uvBuffer.put(uvs)
        uvBuffer.position(0)

        // clear Screen and Depth Buffer, we have set the clear color as black.
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // get handle to vertex shader's vPosition member
        val mPositionHandle = GLES20.glGetAttribLocation(OpenGLShader.sp_Image, "vPosition")

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // Get handle to texture coordinates location
        val mTexCoordLoc = GLES20.glGetAttribLocation(OpenGLShader.sp_Image, "a_texCoord")

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc)

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer)

        // Get handle to shape's transformation matrix
        val mtrxhandle = GLES20.glGetUniformLocation(OpenGLShader.sp_Image, "uMVPMatrix")

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0)

        // Get handle to textures locations
        val mSamplerLoc = GLES20.glGetUniformLocation(OpenGLShader.sp_Image, "s_texture")

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0)

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTexCoordLoc)

    }
*/

    fun SetupImage(imagefile: String) {
        var indices: ShortArray
        val uvs: FloatArray
        val uvBuffer: FloatBuffer
        // Create our UV coordinates.
        uvs = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)

        // The texture buffer
        val bb = ByteBuffer.allocateDirect(uvs.size * 4)
        bb.order(ByteOrder.nativeOrder())
        uvBuffer = bb.asFloatBuffer()
        uvBuffer.put(uvs)
        uvBuffer.position(0)

        // Generate Textures, if more needed, alter these numbers.
        val texturenames = IntArray(1)
        GLES20.glGenTextures(1, texturenames, 0)

        // Temporary create a bitmap
        val options = BitmapFactory.Options()
        options.inScaled = false
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmp = BitmapFactory.decodeFile(imagefile, options)

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0])

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)

        // We are done using the bitmap so we should recycle it.
        bmp.recycle()

    }



    /////////////////////////////////////////////////////////////////////////////////
    // ANOTHER///////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    // region Constants
    val MVP_MATRIX = "uMVPMatrix"
    val POSITION = "vPosition"
    val TEXTURE_COORDINATE = "vTextureCoordinate"
    // endregion

    // region Buffers
    private val POSITION_MATRIX = floatArrayOf(-1f, -1f, 1f, // X1,Y1,Z1
            1f, -1f, 1f, // X2,Y2,Z2
            -1f, 1f, 1f, // X3,Y3,Z3
            1f, 1f, 1f)// X4,Y4,Z4
    private val positionBuffer = ByteBuffer.allocateDirect(POSITION_MATRIX.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(POSITION_MATRIX)
    private val TEXTURE_COORDS = floatArrayOf(0f, 1f, // X1,Y1
            1f, 1f, // X2,Y2
            0f, 0f, // X3,Y3
            1f, 0f)// X4,Y4
    private val textureCoordsBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDS)
    // endregion Buffers

    // region Shaders

    /*
   * @Language("GLSL") may require you to install a plugin to support GLSL
   * but it's only for code highlighting and is not required
   */

    @Language("GLSL")
    private val VERTEX_SHADER = "" +
            "precision mediump float;" +
            "uniform mat4 " + MVP_MATRIX + ";" +
            "attribute vec4 " + POSITION + ";" +
            "attribute vec4 " + TEXTURE_COORDINATE + ";" +
            "varying vec2 position;" +
            "void main(){" +
            " gl_Position = " + MVP_MATRIX + " * " + POSITION + ";" +
            " position = " + TEXTURE_COORDINATE + ".xy;" +
            "}"
    @Language("GLSL")
    private val FRAGMENT_SHADER = "" +
            "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 position;" +
            "void main() {" +
            "    gl_FragColor = texture2D(uTexture, position);" +
            "}"
    // endregion Shaders

    // region Variables
    private var vPosition: Int = 0
    private var vTexturePosition: Int = 0
    private var uMVPMatrix: Int = 0
    private var scale = 1f
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    // endregion Variables



    //public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    fun createsurface(pngImage: String) {
        // A little bit of initialization
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        //Matrix.setRotateM(rotationMatrix, 0, 0, 0, 0, 1.0f);
        // First, we load the picture into a texture that OpenGL will be able to use
        val bitmap = loadBitmap(pngImage)
        val texture = createFBOTexture(bitmap.getWidth(), bitmap.getHeight())
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap)
        // Then, we load the shaders into a program
        val iVShader: Int
        val iFShader: Int
        val iProgId: Int
        val link = IntArray(1)
        iVShader = loadShader(VERTEX_SHADER, GLES20.GL_VERTEX_SHADER)
        iFShader = loadShader(FRAGMENT_SHADER, GLES20.GL_FRAGMENT_SHADER)
        iProgId = GLES20.glCreateProgram()
        GLES20.glAttachShader(iProgId, iVShader)
        GLES20.glAttachShader(iProgId, iFShader)
        GLES20.glLinkProgram(iProgId)
        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            throw RuntimeException("Program couldn't be loaded")
        }
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        GLES20.glUseProgram(iProgId)
        // Now that our program is loaded and in use, we'll retrieve the handles of the parameters
        // we pass to our shaders
        vPosition = GLES20.glGetAttribLocation(iProgId, POSITION)
        vTexturePosition = GLES20.glGetAttribLocation(iProgId, TEXTURE_COORDINATE)
        uMVPMatrix = GLES20.glGetUniformLocation(iProgId, MVP_MATRIX)
    }



    //public void onDrawFrame(GL10 gl10) {
    fun drawtextures() {
        // We have setup that the background color will be black with GLES20.glClearColor in
        // onSurfaceCreated, now is the time to ask OpenGL to clear the screen with this color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Using matrices, we set the camera at the center, advanced of 7 looking to the center back
        // of -1
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 7f, 0f, 0f, -1f, 0f, 1f, 0f)
        // We combine the scene setup we have done in onSurfaceChanged with the camera setup
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        // We combile that with the applied rotation
        //Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, rotationMatrix, 0);
        // Finally, we apply the scale to our Matrix
        Matrix.scaleM(mvpMatrix, 0, scale, scale, scale)
        // We attach the float array containing our Matrix to the correct handle
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
        // We pass the buffer for the position
        positionBuffer.position(0)
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glEnableVertexAttribArray(vPosition)
        // We pass the buffer for the texture position
        textureCoordsBuffer.position(0)
        GLES20.glVertexAttribPointer(vTexturePosition, 2, GLES20.GL_FLOAT, false, 0, textureCoordsBuffer)
        GLES20.glEnableVertexAttribArray(vTexturePosition)
        // We draw our square which will represent our logo
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(vPosition)
        GLES20.glDisableVertexAttribArray(vTexturePosition)
    }




    //load from sdcard//
    private fun loadBitmap(image: String): Bitmap {
        val imagefile = MyApplication.FilesPath + image
        try {
            val options = BitmapFactory.Options()
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            return BitmapFactory.decodeFile(imagefile, options)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw RuntimeException()
        }

    }

    private fun createFBOTexture(width: Int, height: Int): Int {
        val temp = IntArray(1)
        GLES20.glGenFramebuffers(1, temp, 0)
        val handleID = temp[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handleID)
        val fboTex = createTexture(width, height)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex, 0)
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("GL_FRAMEBUFFER status incomplete")
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return handleID
    }

    private fun createTexture(width: Int, height: Int): Int {
        val mTextureHandles = IntArray(1)
        GLES20.glGenTextures(1, mTextureHandles, 0)
        val textureID = mTextureHandles[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        return textureID
    }

    private fun loadShader(strSource: String, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES20.glCreateShader(iType)
        GLES20.glShaderSource(iShader, strSource)
        GLES20.glCompileShader(iShader)
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            throw RuntimeException("Compilation failed : " + GLES20.glGetShaderInfoLog(iShader))
        }
        return iShader
    }



/*

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

*/
}
