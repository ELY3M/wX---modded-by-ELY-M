package joshuatee.wx.radar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.opengles.GL10

// thanks! http://androidblog.reindustries.com/a-real-open-gl-es-2-0-2d-tutorial-part-1/

//Thanks to this for LoadTexture
//http://opengles2learning.blogspot.com/2011/05/applying-texture-to-point-sprite.html

internal object OpenGLShader {

    var TAG: String = "joshuatee OpenGLShader"

    // Program variables
    var sp_SolidColor: Int = 0

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


    fun LoadTexture(imagefile: String): Int {

        Log.i(TAG, "Loadtexture: "+imagefile)
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
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0)
            Log.i(TAG, "Loaded texture" + ":H:" + img!!.height + ":W:" + img.width)
        } catch (e: Exception) {
            Log.i(TAG, e.toString() + ":" + e.message + ":" + e.localizedMessage)
        }

        try {
            img!!.recycle()
        } catch (e: NullPointerException) {
            Log.i(TAG, e.toString() + ":" + e.message + ":" + e.localizedMessage)
        }

        return textures[0]
    }


}