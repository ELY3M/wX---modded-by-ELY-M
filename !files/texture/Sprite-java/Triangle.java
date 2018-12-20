package joshuatee.wx.radar.Sprite;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Triangle extends Shape {

	public Triangle(float x, float y, float w, float h) {
		super(x, y, w, h, 3);
	}

	public Triangle(Vector2f pos, float w, float h) {
		super(pos, w, h, 3);
	}

	@Override
	protected void init() {
		super.init();

		ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_COUNT
				* COORDS_PER_VERTEX * 4);
		bb.order(ByteOrder.nativeOrder());

		Vector3f top = new Vector3f(mPos.x + mWidth / 2, mPos.y, 0);
		Vector3f bottomLeft = new Vector3f(mPos.x, mPos.y - mHeight, 0);
		Vector3f bottomRight = new Vector3f(mPos.x + mWidth, mPos.y - mHeight,
				0);

		mVertexBuffer = bb.asFloatBuffer();
		mVertexBuffer.put(new float[] { top.x, top.y, top.z, bottomLeft.x,
				bottomLeft.y, bottomLeft.z, bottomRight.x, bottomRight.y,
				bottomRight.z });
		mVertexBuffer.position(0);
	}

	public void draw(float[] mvpMatrix) {

		float[] tmpMvpMatrix = new float[16];
		Matrix.multiplyMM(tmpMvpMatrix, 0, mvpMatrix, 0, mModelMatrix, 0);
		
		GLES20.glUseProgram(mProgram);

		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 0, mVertexBuffer);

		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		GLES20.glUniform4fv(mColorHandle, 1, mShapeColor, 0);

		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, tmpMvpMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

}
