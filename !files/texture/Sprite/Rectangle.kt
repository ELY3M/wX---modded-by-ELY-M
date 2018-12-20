package joshuatee.wx.radar.Sprite

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log


open class Rectangle : Shape {



    internal var TAG = "joshuatee Rectangle"
    protected val INDEX_ORDER = shortArrayOf(0, 1, 2, 0, 2, 3)
    protected val VERTEX_STRIDE = COORDS_PER_VERTEX * 4

    val ib = ByteBuffer.allocateDirect(INDEX_ORDER.size * 2)
    var mIndexBuffer: ShortBuffer = ib.asShortBuffer()

    //protected var mBoundingBox: RectF


    constructor(x: Float, y: Float, w: Float, h: Float) : super(x, y, w, h, 4) {}

    constructor(pos: Shape.Vector2f, w: Float, h: Float) : super(pos, w, h, 4) {}

    override fun init() {
        super.init()

        Log.i(TAG, "rect init()...")
        val vb = ByteBuffer.allocateDirect(VERTEX_COUNT * COORDS_PER_VERTEX * 4)
        vb.order(ByteOrder.nativeOrder())

        val topLeft = Shape.Vector3f(mPos.x, mPos.y, 0f)
        val bottomLeft = Shape.Vector3f(mPos.x, mPos.y - mHeight, 0f)
        val bottomRight = Shape.Vector3f(mPos.x + mWidth, mPos.y - mHeight, 0f)
        val topRight = Shape.Vector3f(mPos.x + mWidth, mPos.y, 0f)

        //mBoundingBox = RectF(topLeft.x, -topLeft.y, bottomRight.x, -bottomRight.y)

        mVertexBuffer = vb.asFloatBuffer()
        mVertexBuffer!!.put(floatArrayOf(topLeft.x, topLeft.y, topLeft.z, bottomLeft.x, bottomLeft.y, bottomLeft.z, bottomRight.x, bottomRight.y, bottomRight.z, topRight.x, topRight.y, topRight.z))
        mVertexBuffer!!.position(0)

        val ib = ByteBuffer.allocateDirect(INDEX_ORDER.size * 2)
        ib.order(ByteOrder.nativeOrder())
        mIndexBuffer = ib.asShortBuffer()
        mIndexBuffer.put(INDEX_ORDER)
        mIndexBuffer.position(0)
    }

    override fun draw(mvpMatrix: FloatArray) {

        Log.i(TAG, "rect draw()...")
        val tmpMvpMatrix = FloatArray(16)
        Matrix.multiplyMM(tmpMvpMatrix, 0, mvpMatrix, 0, mModelMatrix, 0)

        GLES20.glUseProgram(mProgram)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer)

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        GLES20.glUniform4fv(mColorHandle, 1, mShapeColor, 0)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        //checkGlError("glGetUniformLocation")

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, tmpMvpMatrix, 0)
        //checkGlError("glUniformMatrix4fv")

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_ORDER.size, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }


    override fun translate(x: Float, y: Float) {
        super.translate(x, y)

        //mBoundingBox.left += x
        //mBoundingBox.right += x
        //mBoundingBox.top -= y
        //mBoundingBox.bottom -= y
    }

    override fun moveTo(x: Float, y: Float) {
        translate(x - mPos.x, y - mPos.y)
    }


}


public val VERTEX_SHADER_CODE = ("uniform mat4 uMVPMatrix;"
        + "attribute vec4 vPosition;" + "void main() {"
        + "  gl_Position = uMVPMatrix * vPosition;" + "}")

public val FRAGMENT_SHADER_CODE = ("precision mediump float;"
        + "uniform vec4 vColor;"
        + "void main() {"
        + "  gl_FragColor = vColor;" + "}")

public val COORDS_PER_VERTEX = 3