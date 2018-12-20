package joshuatee.wx.radar.Sprite;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Rectangle extends Shape {

	static String TAG = "joshuatee Rectangle";

	protected static final short INDEX_ORDER[] = { 0, 1, 2, 0, 2, 3 };
	protected static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;

	protected ShortBuffer mIndexBuffer;

	protected RectF mBoundingBox;

	protected boolean mIsGrounded = false;

	public Rectangle(float x, float y, float w, float h) {
		super(x, y, w, h, 4);
	}

	public Rectangle(Vector2f pos, float w, float h) {
		super(pos, w, h, 4);
	}

	@Override
	protected void init() {
		super.init();

		ByteBuffer vb = ByteBuffer.allocateDirect(VERTEX_COUNT
				* COORDS_PER_VERTEX * 4);
		vb.order(ByteOrder.nativeOrder());

		Vector3f topLeft = new Vector3f(mPos.x, mPos.y, 0);
		Vector3f bottomLeft = new Vector3f(mPos.x, mPos.y - mHeight, 0);
		Vector3f bottomRight = new Vector3f(mPos.x + mWidth, mPos.y - mHeight, 0);
		Vector3f topRight = new Vector3f(mPos.x + mWidth, mPos.y, 0);

		mBoundingBox = new RectF(topLeft.x, -topLeft.y, bottomRight.x, -bottomRight.y);

		mVertexBuffer = vb.asFloatBuffer();
		mVertexBuffer.put(new float[] { topLeft.x, topLeft.y, topLeft.z,
				bottomLeft.x, bottomLeft.y, bottomLeft.z, bottomRight.x,
				bottomRight.y, bottomRight.z, topRight.x, topRight.y,
				topRight.z });
		mVertexBuffer.position(0);

		ByteBuffer ib = ByteBuffer.allocateDirect(INDEX_ORDER.length * 2);
		ib.order(ByteOrder.nativeOrder());
		mIndexBuffer = ib.asShortBuffer();
		mIndexBuffer.put(INDEX_ORDER);
		mIndexBuffer.position(0);
	}

	@Override
	public void draw(float[] mvpMatrix) {


		float[] tmpMvpMatrix = new float[16];
		Matrix.multiplyMM(tmpMvpMatrix, 0, mvpMatrix, 0, mModelMatrix, 0);

		GLES20.glUseProgram(mProgram);

		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		GLES20.glUniform4fv(mColorHandle, 1, mShapeColor, 0);

		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		checkGlError("glGetUniformLocation");

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, tmpMvpMatrix, 0);
		checkGlError("glUniformMatrix4fv");

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}


	@Override
	public void translate(float x, float y) {
		super.translate(x, y);

		mBoundingBox.left += x;
		mBoundingBox.right += x;
		mBoundingBox.top -= y;
		mBoundingBox.bottom -= y;
	}

	@Override
	public void moveTo(float x, float y) {
		translate(x - mPos.x, y - mPos.y);
	}


	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError "
					+ error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

}
