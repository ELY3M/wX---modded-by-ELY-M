package joshuatee.wx.radar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

public class IconLoad {

    private static String iconfile = "test.png";
    final String LOG_TAG = "iconload";
    public float SIZE = 1.0f;
    private int UserIndex = 0;
    private String WRKFILE = (this.applicationFilePath + "test.png");
    private String applicationFilePath = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath().toString())).append("/wX/").toString();
    private Context context;
    private int[] cropRect = new int[4];
    public boolean disableMaps = false;
    private short[] iconlocx = new short[this.maxUserPoints];
    private short[] iconlocy = new short[this.maxUserPoints];
    private float[] mScratch = new float[8];
    private int maxUserPoints = 500;
    public boolean okToPlot = false;
    double proximityDegrees = 4.0d;
    Location radarLocation;
    private float scalingFactor;
    private String shapeTarget = null;
    private int[] textures = new int[1];
    private boolean texturesLoaded = false;
    ArrayList<PointInfo> usera = new ArrayList();
    private int userpointcount;
    boolean verbose = false;

    public class PointInfo {
        double lat;
        double lon;
        String name;
        short x;
        short y;
    }

    public IconLoad(Context ct, int sf, float s) {
        this.context = ct;
        this.scalingFactor = 0.5f * ((float) sf);
        FillCropRect();
    }

    private void FillCropRect() {
        Options opts = new Options();
        opts.inScaled = false;
        File f = new File(this.applicationFilePath + iconfile);
        if (f.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
            this.cropRect[0] = 0;
            this.cropRect[1] = bitmap.getHeight();
            this.cropRect[2] = bitmap.getWidth();
            this.cropRect[3] = -bitmap.getHeight();
            return;
        }
        Log.e(this.LOG_TAG, "Unable to find texture " + f.getAbsolutePath());
    }

    private void loadGLTexture(GL10 gl) {
        if (this.verbose) {
            Log.d(this.LOG_TAG, "Loading Textures");
        }
        this.texturesLoaded = false;
        if (this.textures[0] != 0) {
            invalidateTexture(gl);
        }
        Options opts = new Options();
        opts.inScaled = false;
        File f = new File(this.applicationFilePath + iconfile);
        if (f.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
            this.cropRect[0] = 0;
            this.cropRect[1] = bitmap.getHeight();
            this.cropRect[2] = bitmap.getWidth();
            this.cropRect[3] = -bitmap.getHeight();
            this.textures[0] = loadTextureFromBitmapFast(gl, bitmap);
            bitmap.recycle();
            this.texturesLoaded = true;
            return;
        }
        Log.e(this.LOG_TAG, "Unable to find texture " + f.getAbsolutePath());
    }




    public void drawIcon(GL10 gl) {
        if (this.okToPlot) {
            if (!this.texturesLoaded) {
                loadGLTexture(gl);
            }
            beginDrawing(gl);
            endDrawing(gl);
        }
    }

    public void unloadTextures(GL10 lastKnownGL2) {
        if (this.textures[0] != 0) {
            invalidateTexture(lastKnownGL2);
        }
    }

    private void invalidateTexture(GL10 gl) {
        this.texturesLoaded = false;
        if (gl != null) {
            int len = this.textures.length;
            if (this.verbose) {
                Log.d(this.LOG_TAG, ">>>>>Invalidate Textures (Count " + len + ")<<<<<");
            }
            gl.glDeleteTextures(len, this.textures, 0);
            for (int i = 0; i < len; i++) {
                this.textures[i] = 0;
            }
        }
    }

    private int loadTextureFromBitmapFast(GL10 gl, Bitmap bitmap) {
        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);
        gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 1);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, tex[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        if (this.verbose) {
            Log.d(LOG_TAG, "Bitmap Loaded.  Width=" + bitmap.getWidth() + " Height=" + bitmap.getHeight());
        }
        return tex[0];
    }

    public void beginDrawing(GL10 gl) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, this.textures[0]);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glEnable(GL10.GL_BLEND);
        gl.glColor4x(65536, 65536, 65536, 65536);
    }

    public void draw(GL10 gl, int x, int y) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        ((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, 35741, this.cropRect, 0);
        ((GL11Ext) gl).glDrawTexiOES(x, y, 0, this.cropRect[2], -this.cropRect[3]);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    private void endDrawing(GL10 gl) {
        gl.glDisable(GL10.GL_BLEND);
    }


}
