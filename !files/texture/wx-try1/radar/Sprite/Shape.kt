package joshuatee.wx.radar.Sprite

import java.nio.FloatBuffer

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log


abstract class Shape(x: Float, y: Float, protected var mWidth: Float, protected var mHeight: Float, protected val VERTEX_COUNT: Int) {


    public val VERTEX_SHADER_CODE = ("uniform mat4 uMVPMatrix;"
            + "attribute vec4 vPosition;" + "void main() {"
            + "  gl_Position = uMVPMatrix * vPosition;" + "}")

    public val FRAGMENT_SHADER_CODE = ("precision mediump float;"
            + "uniform vec4 vColor;"
            + "void main() {"
            + "  gl_FragColor = vColor;" + "}")

    public val COORDS_PER_VERTEX = 3




    protected var mProgram: Int = 0
    protected var mPositionHandle: Int = 0
    protected var mColorHandle: Int = 0
    protected var mMVPMatrixHandle: Int = 0

    protected var mVertexBuffer: FloatBuffer? = null

    protected var mModelMatrix = FloatArray(16)

    protected var mShapeColor: FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    protected var mPos: Vector3f


    protected var mSpeed = Vector2f(0.0f, 0.0f)

    class Vector3f(var x: Float, var y: Float, var z: Float)

    class Vector2f(var x: Float, var y: Float)

    init {
        mPos = Vector3f(x, y, 0f)

        Matrix.setIdentityM(mModelMatrix, 0)
    }

    constructor(pos: Vector2f, w: Float, h: Float, vertexCount: Int) : this(pos.x, pos.y, w, h, vertexCount) {}

    protected open fun init() {
        Log.i(TAG, "shape init()...")
        mShapeColor = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }

    abstract fun draw(mvpMatrix: FloatArray)


    fun setColor(r: Int, g: Int, b: Int, a: Int) {
        mShapeColor[0] = r / 255.0f
        mShapeColor[1] = g / 255.0f
        mShapeColor[2] = b / 255.0f
        mShapeColor[3] = a / 255.0f
    }

    open fun translate(x: Float, y: Float) {
        Matrix.translateM(mModelMatrix, 0, x, y, 0f)
        mPos.x += x
        mPos.y += y
    }

    open fun moveTo(x: Float, y: Float) {
        Matrix.translateM(mModelMatrix, 0, x - mPos.x, y - mPos.y, 0f)
        mPos.x = x
        mPos.y = y
    }


    fun setSpeed(x: Float, y: Float) {
        mSpeed.x = x
        mSpeed.y = y
    }

    fun speed(): Vector2f {
        return mSpeed
    }

    fun update() {
        translate(mSpeed.x, mSpeed.y)
    }

    fun center(): Vector2f {
        return Vector2f(mPos.x + mWidth / 2, mPos.y - mHeight / 2)
    }





        fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

}
