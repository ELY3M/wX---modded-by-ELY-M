package joshuatee.wx.radar.Sprite;

import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;


public abstract class Shape {

	private static final String VERTEX_SHADER_CODE = "uniform mat4 uMVPMatrix;"
			+ "attribute vec4 vPosition;" + "void main() {"
			+ "  gl_Position = uMVPMatrix * vPosition;" + "}";

	private static final String FRAGMENT_SHADER_CODE = "precision mediump float;"
			+ "uniform vec4 vColor;"
			+ "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	protected static final int COORDS_PER_VERTEX = 3;

	public static class Vector3f {
		public float x, y, z;

		public Vector3f(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static class Vector2f {
		public float x, y;

		public Vector2f(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	protected final int VERTEX_COUNT;

	protected int mProgram;
	protected int mPositionHandle;
	protected int mColorHandle;
	protected int mMVPMatrixHandle;

	protected FloatBuffer mVertexBuffer;

	protected float[] mModelMatrix = new float[16];

	protected float mShapeColor[];

	protected Vector3f mPos;
	protected float mWidth, mHeight;


	protected Vector2f mSpeed = new Vector2f(0.0f, 0.0f);

	public Shape(float x, float y, float w, float h, int vertexCount) {
		mPos = new Vector3f(x, y, 0);
		mWidth = w;
		mHeight = h;
		VERTEX_COUNT = vertexCount;

		Matrix.setIdentityM(mModelMatrix, 0);
	}

	public Shape(Vector2f pos, float w, float h, int vertexCount) {
		this(pos.x, pos.y, w, h, vertexCount);
	}

	protected void init() {
		mShapeColor = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };

		int vertexShader = OpenGL2DRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				VERTEX_SHADER_CODE);
		int fragmentShader = OpenGL2DRenderer.loadShader(
				GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);
	}

	public abstract void draw(float[] mvpMatrix);


	public void setColor(int r, int g, int b, int a) {
		mShapeColor[0] = r / 255.0f;
		mShapeColor[1] = g / 255.0f;
		mShapeColor[2] = b / 255.0f;
		mShapeColor[3] = a / 255.0f;
	}

	public void translate(float x, float y) {
		Matrix.translateM(mModelMatrix, 0, x, y, 0);
		mPos.x += x;
		mPos.y += y;
	}

	public void moveTo(float x, float y) {
		Matrix.translateM(mModelMatrix, 0, x - mPos.x, y - mPos.y, 0);
		mPos.x = x;
		mPos.y = y;
	}


	public void setSpeed(float x, float y) {
		mSpeed.x = x;
		mSpeed.y = y;
	}

	public Vector2f speed() {
		return mSpeed;
	}

	public void update() {
		translate(mSpeed.x, mSpeed.y);
	}

	public Vector2f center() {
		return new Vector2f(mPos.x + mWidth / 2, mPos.y - mHeight / 2);
	}
}
