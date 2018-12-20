package joshuatee.wx.radar.Sprite;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;


public class Sprite extends Rectangle {

	static String TAG = "joshuatee Sprite";

	private static final String VERTEX_SHADER_CODE = "uniform mat4 uMVPMatrix;"
			+ "attribute vec4 vPosition;" + "attribute vec2 a_texCoord;"
			+ "varying vec2 v_texCoord;" + "void main() {"
			+ "  gl_Position = uMVPMatrix * vPosition;"
			+ "  v_texCoord = a_texCoord;" + "}";
	private static final String FRAGMENT_SHADER_CODE = "precision mediump float;"
			+ "varying vec2 v_texCoord;"
			+ "uniform sampler2D s_texture;"
			+ "void main() {"
			+ "  gl_FragColor = texture2D( s_texture, v_texCoord );" + "}";

	private static final float UV[] = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f };

	protected int mTextureId;

	protected FloatBuffer mUVBuffer;

	protected Bitmap mBitmap;

	protected int mTexCoordLoc, mSamplerLoc;

	public Sprite(Vector2f pos, float w, float h, Bitmap bitmap) {
		super(pos, w, h);
		mBitmap = bitmap;
	}

	public Sprite(float x, float y, float w, float h, Bitmap bitmap) {
		super(x, y, w, h);
		mBitmap = bitmap;
	}

	@Override
	protected void init() {
		super.init();

		ByteBuffer uvb = ByteBuffer.allocateDirect(UV.length * 4);
		uvb.order(ByteOrder.nativeOrder());
		mUVBuffer = uvb.asFloatBuffer();
		mUVBuffer.put(UV);
		mUVBuffer.position(0);

		int[] textureNames = new int[1];
		GLES20.glGenTextures(1, textureNames, 0);
		mTextureId = textureNames[0] - 1;

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + mTextureId);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureNames[0]);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
		mBitmap.recycle();

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);
	}

	@Override
	public void draw(float[] mvpMatrix) {
		
		float[] tmpMvpMatrix = new float[16];
		Matrix.multiplyMM(tmpMvpMatrix, 0, mvpMatrix, 0, mModelMatrix, 0);
		
		GLES20.glUseProgram(mProgram);

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

		mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
		GLES20.glEnableVertexAttribArray(mTexCoordLoc);
		GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false,
				0, mUVBuffer);

		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		checkGlError("glGetUniformLocation");

		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, tmpMvpMatrix, 0);
		checkGlError("glUniformMatrix4fv");

		mSamplerLoc = GLES20.glGetUniformLocation(mProgram, "s_texture");
		GLES20.glUniform1i(mSamplerLoc, mTextureId);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDEX_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTexCoordLoc);

		GLES20.glDisable(GLES20.GL_BLEND);
	}



	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 *
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 *
	 * If the operation is not successful, the check throws an error.
	 *
	 * @param glOperation
	 *            - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError "
					+ error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

}
