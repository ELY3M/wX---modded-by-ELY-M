//modded by ELY M. 

package joshuatee.wx.radar

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import joshuatee.wx.util.UtilityLog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10

// thanks! http://androidblog.reindustries.com/a-real-open-gl-es-2-0-2d-tutorial-part-1/

internal object OpenGLShader {

    // Program variables
    var sp_SolidColor: Int = 0
    var sp_loadimage: Int = 0
    var sp_loadconus: Int = 0
    var sp_conus: Int = 0

    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    const val vs_SolidColor =
            "uniform    mat4        uMVPMatrix;" +
                    "attribute  vec4        vPosition;" +
                    "attribute  vec3        a_Color;" + // was attribute

                    "varying  vec3        v_Color;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_Color = a_Color;" +
                    "}"

    const val fs_SolidColor =
            "precision mediump float;" +
                    "varying vec3 v_Color;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(v_Color,1.0);" +
                    "}"





    //Thanks to this for point sprite shader and codes
    //http://opengles2learning.blogspot.com/2011/05/applying-texture-to-point-sprite.html
    //This is my modified shader :) ELY M.
    var vs_loadimage =
            "uniform    mat4        uMVPMatrix;" +
                    "attribute  vec4        vPosition;" +
                    "uniform  float        imagesize;" +
                    "void main() {" +
                    "  gl_PointSize = imagesize;" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}"

    var fs_loadimage =
            "precision mediump float;" +
                    "uniform sampler2D u_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_texture, gl_PointCoord);" +
                    "}"


/*


    val vs_conus = "uniform mat4 uMVPMatrix;" +
         "attribute vec4 vPosition;" +
         "attribute vec2 a_texCoords;" +
         "varying vec2 v_texCoords;" +
         "void main() {" +
         "  gl_Position = uMVPMatrix * vPosition;" +
         "  v_texCoords = a_texCoords;" +
         "}"

    val fs_conus = (
        "precision mediump float;" +
        "varying vec2 v_texCoords;" +
        "uniform sampler2D u_texture;" +
        "void main() {" +
        "  gl_FragColor = texture2D(u_texture, v_texCoords);" +
        "}")


 */

    val vs_conus = "uniform mat4 uMVPMatrix;" +
         "attribute vec4 vPosition;" +
         "attribute vec2 a_texCoords;" +
         "varying vec2 v_texCoords;" +
         "void main() {" +
         "  gl_Position = uMVPMatrix * vPosition;" +
         "  v_texCoords = a_texCoords;" +
         "}"

    val fs_conus = (
        "precision mediump float;" +
        "varying vec2 v_texCoords;" +
        "uniform sampler2D u_texture;" +
        "void main() {" +
        "  gl_FragColor = texture2D(u_texture, v_texCoords);" +
        "}")


    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // return the shader
        return shader
    }


    fun LoadImage(imagefile: String) {
        val texturenames = IntArray(1)
        GLES20.glGenTextures(1, texturenames, 0)
        val options = BitmapFactory.Options()
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmp = BitmapFactory.decodeFile(imagefile, options)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
        ///UtilityLog.d("wx", "Loaded texture" + ":H:" + bmp.height + ":W:" + bmp.width)

        try {
            bmp.recycle()
        } catch (e: NullPointerException) {
            UtilityLog.handleException(e)
        }

    }


    fun LoadTexture(imagefile: String): Int {
        var img: Bitmap? = null
        val textures = IntArray(1)
        try {
            val options = BitmapFactory.Options()
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            img = BitmapFactory.decodeFile(imagefile, options)
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D , GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D , GLES20.GL_TEXTURE_WRAP_T , GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0)
            //UtilityLog.d("wx", "Loaded texture" + ":H:" + img!!.height + ":W:" + img.width)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }

        try {
            img!!.recycle()
        } catch (e: NullPointerException) {
            UtilityLog.handleException(e)
        }

        return textures[0]
    }

    fun LoadBitmap(imagefile: String): Bitmap? {
        var img: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            img = BitmapFactory.decodeFile(imagefile, options)

        } catch (e: NullPointerException) {
            UtilityLog.handleException(e)
        }
        return img
    }

    fun resizeBitmap(imagefile: String, size: Double): Bitmap {
        val bitmap: Bitmap? = LoadBitmap(imagefile)
        val newWidth: Double = (bitmap!!.getWidth() * (size / 100))
        val newHeight: Double = (bitmap!!.getHeight() * (size / 100))
        return Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), newHeight.toInt(), true)
    }

    fun RotateBitmap(imagefile: String, d: Double): Bitmap {
        //UtilityLog.d("wx", "rotating bitmap: "+ imagefile + " to: "+d)
        val bitmap: Bitmap? = LoadBitmap(imagefile)
        val matrix = Matrix()
        matrix.setRotate(d.toFloat())
        matrix.postTranslate(0.0f, bitmap!!.width.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun LoadBitmapTexture(img: Bitmap): Int {
        val textures = IntArray(1)
        try {
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D , GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D , GLES20.GL_TEXTURE_WRAP_T , GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0)
            ///UtilityLog.d("wx","Loaded texture" + ":H:" + img.height + ":W:" + img.width)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }

        try {
            img.recycle()
        } catch (e: NullPointerException) {
            UtilityLog.handleException(e)
        }

        return textures[0]
    }


}