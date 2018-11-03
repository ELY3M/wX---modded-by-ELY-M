package com.MyPYK.Radar.Overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import com.MyPYK.Radar.Full.Constants;
import com.MyPYK.Radar.Full.Miscellaneous;
import com.MyPYK.RadarEngine.colorPallete;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;
import javax.microedition.khronos.opengles.GL10;

public class colorBarPlot {
    private static final String LOG_TAG = "colorBarPlot";
    private static ByteBuffer tbb = null;
    private static ByteBuffer vbb = null;
    private static boolean verbose = false;
    private boolean BUSY;
    private Bitmap bmp;
    private colorPallete colorpal = null;
    private boolean filter_low_returns = false;
    private float increment;
    private float minvalue;
    private int numlevels;
    private boolean okToPlot = false;
    private int productID;
    private FloatBuffer textureBuffer = null;
    private int[] textures = new int[2];
    private boolean texturesLoaded;
    private boolean updateTexture = false;
    private FloatBuffer vertexBuffer = null;
    float yt = -0.99f;

    public colorBarPlot(String str, Context context, float f) {
        if (verbose) {
            Log.d(LOG_TAG, "Instantiate ColorBarPlot");
        }
        this.yt = f;
        this.okToPlot = false;
        this.colorpal = new colorPallete(context);
        vbb = ByteBuffer.allocateDirect(32);
        vbb.order(ByteOrder.nativeOrder());
        this.vertexBuffer = vbb.asFloatBuffer();
        tbb = ByteBuffer.allocateDirect(32);
        tbb.order(ByteOrder.nativeOrder());
        this.textureBuffer = tbb.asFloatBuffer();
        if (verbose) {
            Log.d(LOG_TAG, "Color bar initialized");
        }
        InitializeColorBar();
    }

    public void setVerbose(boolean z) {
        verbose = z;
    }

    public void InitializeColorBar() {
        if (verbose) {
            Log.d(LOG_TAG, "ColorBarPlot Init");
        }
        this.okToPlot = false;
        this.textureBuffer.clear();
        this.vertexBuffer.clear();
        float[] fArr = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
        try {
            this.vertexBuffer.put(new float[]{-1.0f, this.yt, 1.0f, this.yt, -1.0f, -1.0f, 1.0f, -1.0f});
            this.textureBuffer.put(fArr);
        } catch (Exception e) {
            Log.e(LOG_TAG, "****Vertex put failed colors****");
            e.printStackTrace();
        }
        try {
            this.vertexBuffer.position(0);
            this.textureBuffer.position(0);
        } catch (NullPointerException unused) {
            Log.e(LOG_TAG, "Null pointer exception ColorBarPlot");
        } catch (Exception unused2) {
            Log.e(LOG_TAG, "Unhandled exception ColorBarPlot");
        }
        this.okToPlot = true;
        this.updateTexture = true;
    }

    public void SetLevels(String str, int i) {
        this.okToPlot = false;
        this.productID = i;
        if (verbose) {
            String str2 = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Make ColorBar for ProductCode ");
            stringBuilder.append(i);
            Log.d(str2, stringBuilder.toString());
        }
        this.textureBuffer.put(new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f});
        this.textureBuffer.position(0);
        this.updateTexture = true;
        this.okToPlot = true;
    }

    public void draw(GL10 gl10, boolean z) {
        if (this.okToPlot) {
            gl10.glFrontFace(2304);
            gl10.glEnable(2884);
            gl10.glEnableClientState(32884);
            gl10.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
            if (this.updateTexture || !this.texturesLoaded) {
                this.updateTexture = false;
                CreateTextures(gl10);
            } else {
                gl10.glEnable(3553);
                gl10.glTexEnvf(8960, 8704, 7681.0f);
                if (z) {
                    gl10.glBindTexture(3553, this.textures[1]);
                } else {
                    gl10.glBindTexture(3553, this.textures[0]);
                }
                gl10.glEnableClientState(32888);
                gl10.glTexCoordPointer(2, 5126, 0, this.textureBuffer);
            }
            gl10.glVertexPointer(2, 5126, 0, this.vertexBuffer);
            gl10.glDrawArrays(5, 0, 4);
            if (this.textures[0] > 0) {
                gl10.glDisableClientState(32888);
            }
            gl10.glDisable(3553);
            gl10.glDisableClientState(32884);
            gl10.glDisable(2884);
        }
    }

    private void CreateTextures(GL10 gl10) {
        if (this.BUSY) {
            Log.e(LOG_TAG, "Loading Textures Already Busy");
            return;
        }
        String str;
        StringBuilder stringBuilder;
        this.BUSY = true;
        Miscellaneous miscellaneous = new Miscellaneous();
        if (verbose) {
            Log.d(LOG_TAG, ">>>>>CREATETEXTURES <<<<<");
        }
        this.texturesLoaded = TextureOperations.invalidateTexture(gl10, this.textures);
        gl10.glGenTextures(2, this.textures, 0);
        int ProductCodeLookup = miscellaneous.ProductCodeLookup(this.productID);
        String format = String.format(Locale.US, "/colorbar/scale%d.dat", new Object[]{Integer.valueOf(ProductCodeLookup)});
        if (verbose) {
            str = LOG_TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Loading colorbar ");
            stringBuilder.append(Constants.appPath);
            stringBuilder.append(format);
            Log.i(str, stringBuilder.toString());
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(Constants.appPath);
        stringBuilder2.append(format);
        Bitmap ReadPngFile = ReadPngFile(gl10, stringBuilder2.toString());
        if (ReadPngFile != null) {
            this.textures[0] = TextureOperations.loadTextureFromBitmapFastSmoothed(gl10, ReadPngFile);
            if (verbose) {
                str = LOG_TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("COLORBAR ");
                stringBuilder.append(ReadPngFile.getWidth());
                stringBuilder.append("/");
                stringBuilder.append(ReadPngFile.getHeight());
                Log.d(str, stringBuilder.toString());
            }
            ReadPngFile.recycle();
        } else if (verbose) {
            Log.d(LOG_TAG, "COLORBAR Not loaded");
        }
        String format2 = String.format(Locale.US, "/colorbar/conus.dat", new Object[]{Integer.valueOf(ProductCodeLookup)});
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(Constants.appPath);
        stringBuilder3.append(format2);
        Bitmap ReadPngFile2 = ReadPngFile(gl10, stringBuilder3.toString());
        if (ReadPngFile2 != null) {
            this.textures[1] = TextureOperations.loadTextureFromBitmapFastSmoothed(gl10, ReadPngFile2);
            if (verbose) {
                String str2 = LOG_TAG;
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("COLORBAR ");
                stringBuilder3.append(ReadPngFile2.getWidth());
                stringBuilder3.append("/");
                stringBuilder3.append(ReadPngFile2.getHeight());
                Log.d(str2, stringBuilder3.toString());
            }
            ReadPngFile2.recycle();
        } else if (verbose) {
            Log.d(LOG_TAG, "CONUS COLORBAR Not loaded");
        }
        this.texturesLoaded = true;
        this.BUSY = false;
    }

    private Bitmap ReadPngFile(GL10 gl10, String str) {
        if (!new File(str).exists()) {
            return null;
        }
        Config config = Config.ARGB_8888;
        Options options = new Options();
        options.inScaled = false;
        options.inPreferredConfig = config;
        Bitmap decodeFile = BitmapFactory.decodeFile(str, options);
        if (decodeFile != null && verbose) {
            String str2 = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Loaded ");
            stringBuilder.append(str);
            stringBuilder.append(" into index 0 TexInt ");
            stringBuilder.append(this.textures[0]);
            Log.d(str2, stringBuilder.toString());
        }
        return decodeFile;
    }

    public void unloadTextures(GL10 gl10) {
        this.texturesLoaded = TextureOperations.invalidateTexture(gl10, this.textures);
    }

    public void recomputeCoordinates(float f) {
        this.yt = f;
        InitializeColorBar();
    }
}
