package joshuatee.wx.radar.Sprite

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log


internal var TAG = "joshuatee Sprite"

/*
private val VERTEX_SHADER_CODE = ("uniform mat4 uMVPMatrix;"
        + "attribute vec4 vPosition;" + "attribute vec2 a_texCoord;"
        + "varying vec2 v_texCoord;" + "void main() {"
        + "  gl_Position = uMVPMatrix * vPosition;"
        + "  v_texCoord = a_texCoord;" + "}")
private val FRAGMENT_SHADER_CODE = ("precision mediump float;"
        + "varying vec2 v_texCoord;"
        + "uniform sampler2D s_texture;"
        + "void main() {"
        + "  gl_FragColor = texture2D( s_texture, v_texCoord );" + "}")

private val UV = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)
*/

class Sprite : Rectangle {


    private var UV = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f)

    protected var mTextureId: Int = 0

    var uvb = ByteBuffer.allocateDirect(UV.size * 4)
    var mUVBuffer: FloatBuffer = uvb.asFloatBuffer()

    var mBitmap: Bitmap

    var mTexCoordLoc: Int = 0
    var mSamplerLoc: Int = 0

    constructor(pos: Shape.Vector2f, w: Float, h: Float, bitmap: Bitmap) : super(pos, w, h) {
        mBitmap = bitmap
    }

    constructor(x: Float, y: Float, w: Float, h: Float, bitmap: Bitmap) : super(x, y, w, h) {
        mBitmap = bitmap
    }

    override public fun init() {
        super.init()
        Log.i(TAG, "Sprite init()...")
        var uvb = ByteBuffer.allocateDirect(UV.size * 4)
        uvb.order(ByteOrder.nativeOrder())
        mUVBuffer = uvb.asFloatBuffer()
        mUVBuffer.put(UV)
        mUVBuffer.position(0)

        val textureNames = IntArray(1)
        GLES20.glGenTextures(1, textureNames, 0)
        mTextureId = textureNames[0] - 1

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + mTextureId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
        //mBitmap.recycle()

        var vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        var fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }

    override fun draw(mvpMatrix: FloatArray) {
        Log.i(TAG, "Sprite draw()...")
        var tmpMvpMatrix = FloatArray(16)
        Matrix.multiplyMM(tmpMvpMatrix, 0, mvpMatrix, 0, mModelMatrix, 0)

        GLES20.glUseProgram(mProgram)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer)

        mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "a_texCoord")
        GLES20.glEnableVertexAttribArray(mTexCoordLoc)
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false,
                0, mUVBuffer)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        //checkGlError("glGetUniformLocation")

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, tmpMvpMatrix, 0)
        //checkGlError("glUniformMatrix4fv")

        mSamplerLoc = GLES20.glGetUniformLocation(mProgram, "s_texture")
        GLES20.glUniform1i(mSamplerLoc, mTextureId)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_ORDER.size, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer)
        //GLES20.glDisableVertexAttribArray(mPositionHandle)
        //GLES20.glDisableVertexAttribArray(mTexCoordLoc)
        //GLES20.glDisable(GLES20.GL_BLEND)
    }




    /*

        fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }
*/


}
