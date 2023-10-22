//modded by ELY M. 
//done by ELY M. 

package joshuatee.wx.radar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import joshuatee.wx.MyApplication
import joshuatee.wx.*
import joshuatee.wx.util.*
import android.graphics.drawable.LayerDrawable
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import java.io.File
import kotlinx.coroutines.*
import java.io.FileOutputStream


/*
*
*  Conus Radar
*  Credit to Pykl3 and Joe Jurecka for this idea!
*
* */


/*
*
public class ConusRadar {
    private static ByteBuffer Bibb = null;
    private static ByteBuffer Btbb = null;
    private static ByteBuffer Bvbb = null;
    static final String IMAGEFILE = "tmp/conus.dat";
    public static final String REGISTRATIONFILE = "tmp/conus.reg";
    private static final int bitmapheight = 1024;
    private static final int bitmapwidth = 1024;
    static final String mLogTag = ConusRadar.class.getSimpleName();
    static final boolean mVerbose = false;
    private static final int maxpoints = 130;
    private static final int pixelsPerPoint = 16;
    private static String regurl = "https://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gfw";
    private static String url = "https://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gif";
    private boolean BUSY;
    private ShortBuffer BindexBuffer;
    private FloatBuffer BtextureBuffer;
    private ShortBuffer BvertexBuffer;
    public boolean FEATURE_ENABLED = true;
    private boolean REGISTRATIONFILEREAD;
    private Semaphore conusSemaphore = new Semaphore(1, true);
    private double degreesPerPixellat = -0.017971305190311d;
    private double degreesPerPixellon = 0.017971305190311d;
    private double east;
    private final Context mContext;
    private double north;
    private boolean okToPlot = false;
    private Location radarLocation;
    private double south;
    private int[] textures = new int[1];
    private boolean texturesLoaded = false;
    private boolean texturesLoading = false;
    private double west;

    private class AcquireData extends AsyncTask<Void, Void, Void> {
        private AcquireData() {
        }

        protected Void doInBackground(Void... voids) {
            try {
                ConusRadar.this.conusSemaphore.acquire();
                new PYKFile().getFile(ConusRadar.this.mContext, ConusRadar.regurl, Constants.appPath + ConusRadar.REGISTRATIONFILE);
                new PYKFile().getFile(ConusRadar.this.mContext, ConusRadar.url, Constants.appPath + ConusRadar.IMAGEFILE);
                if (new File(Constants.appPath + ConusRadar.REGISTRATIONFILE).exists()) {
                    ConusRadar.this.initParams();
                }
                ConusRadar.this.conusSemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
                new Logger(ConusRadar.mLogTag).writeException(e);
                ConusRadar.this.conusSemaphore.release();
            } catch (Throwable th) {
                ConusRadar.this.conusSemaphore.release();
                throw th;
            }
            return null;
        }
    }

    public ConusRadar(Context ctxt, float ct) {
        this.mContext = ctxt;
        Btbb = ByteBuffer.allocateDirect(1040);
        Btbb.order(ByteOrder.nativeOrder());
        Bvbb = ByteBuffer.allocateDirect(520);
        Bvbb.order(ByteOrder.nativeOrder());
        this.BtextureBuffer = Btbb.asFloatBuffer();
        this.BvertexBuffer = Bvbb.asShortBuffer();
        Bibb = ByteBuffer.allocateDirect(768);
        Bibb.order(ByteOrder.nativeOrder());
        this.BindexBuffer = Bibb.asShortBuffer();
    }

    public void calculateVertices() {
        if (this.radarLocation != null) {
            this.okToPlot = false;
            ClearVbb();
            CoordinateConversion cc = new CoordinateConversion();
            short[] vertices = new short[4];
            float[] tvertices = new float[4];
            short[] indices = new short[6];
            int eastwestpoints = 0;
            double deltalat = ((this.east - this.west) / 1024.0d) * 16.0d;
            double deltalon = ((this.north - this.south) / 1024.0d) * 16.0d;
            for (double i = 0.0d; i < ((double) 65); i += 1.0d) {
                double lo = this.west + (i * deltalat);
                XYLOC loc = cc.latLonToGl(this.radarLocation.getLatitude(), this.radarLocation.getLongitude(), this.south, lo);
                vertices[0] = (short) loc.y;
                vertices[1] = (short) loc.x;
                tvertices[0] = ((float) (eastwestpoints * 16)) / 1024.0f;
                tvertices[1] = 1.0f;
                loc = cc.latLonToGl(this.radarLocation.getLatitude(), this.radarLocation.getLongitude(), this.north, lo);
                vertices[2] = (short) loc.y;
                vertices[3] = (short) loc.x;
                tvertices[2] = ((float) (eastwestpoints * 16)) / 1024.0f;
                tvertices[3] = 0.0f;
                this.BvertexBuffer.put(vertices);
                this.BtextureBuffer.put(tvertices);
                eastwestpoints++;
            }
            for (short idx = (short) 0; idx < (short) 64; idx = (short) (idx + 1)) {
                indices[0] = (short) (idx * 2);
                indices[1] = (short) ((idx * 2) + 1);
                indices[2] = (short) ((idx * 2) + 3);
                indices[3] = (short) (idx * 2);
                indices[4] = (short) ((idx * 2) + 3);
                indices[5] = (short) ((idx * 2) + 2);
                this.BindexBuffer.put(indices);
            }
            this.BvertexBuffer.position(0);
            this.BtextureBuffer.position(0);
            this.BindexBuffer.position(0);
            this.okToPlot = true;
        }
    }

    public void ClearVbb() {
        this.okToPlot = false;
        if (Bvbb != null) {
            Bvbb.clear();
        }
        if (this.BvertexBuffer != null) {
            this.BvertexBuffer.clear();
        }
        if (Btbb != null) {
            Btbb.clear();
        }
        if (this.BtextureBuffer != null) {
            this.BtextureBuffer.clear();
        }
        if (Bibb != null) {
            Bibb.clear();
        }
        if (this.BindexBuffer != null) {
            this.BindexBuffer.clear();
        }
    }

    public void DownloadAndProcess(ExternalFileManager fm, Location radarLoc) {
        this.radarLocation = radarLoc;
        new AcquireData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void RecalculateVertices(Location loc) {
        this.radarLocation = loc;
        calculateVertices();
    }

    public void draw(GL10 gl) {
        if (this.FEATURE_ENABLED && this.REGISTRATIONFILEREAD) {
            gl.glFrontFace(2304);
            gl.glEnable(2884);
            gl.glCullFace(1029);
            gl.glEnableClientState(32884);
            gl.glEnable(3553);
            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glVertexPointer(2, 5122, 0, this.BvertexBuffer);
            if (!this.texturesLoaded) {
                if (!this.texturesLoading) {
                    this.texturesLoading = true;
                    ReadImageFile(gl, Constants.appPath + IMAGEFILE);
                    this.texturesLoading = false;
                } else {
                    return;
                }
            }
            if (this.okToPlot) {
                gl.glEnableClientState(32888);
                gl.glTexCoordPointer(2, 5126, 0, this.BtextureBuffer);
                gl.glBindTexture(3553, this.textures[0]);
                gl.glDrawElements(4, this.BindexBuffer.capacity(), 5123, this.BindexBuffer);
                gl.glDisableClientState(32888);
                gl.glDisable(3553);
                gl.glDisableClientState(32884);
                gl.glDisable(2884);
            }
        }
    }

    private void initParams() {
        if (this.radarLocation != null) {
            ParseFile();
        }
    }

    private void ParseFile() {
        this.okToPlot = false;
        this.REGISTRATIONFILEREAD = false;
        if (Tools.DoesFileExist(Constants.appPath + REGISTRATIONFILE) && Tools.DoesFileExist(Constants.appPath + IMAGEFILE)) {
            BufferedReader bufRdr = null;
            try {
                bufRdr = new BufferedReader(new FileReader(Constants.appPath + REGISTRATIONFILE), 16384);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                new Logger(mLogTag).writeException(e1);
            }
            try {
                this.degreesPerPixellon = Double.parseDouble(bufRdr.readLine().trim());
                double skew1 = Double.parseDouble(bufRdr.readLine().trim());
                double skew2 = Double.parseDouble(bufRdr.readLine().trim());
                this.degreesPerPixellat = Double.parseDouble(bufRdr.readLine().trim());
                this.west = Double.parseDouble(bufRdr.readLine().trim());
                this.north = Double.parseDouble(bufRdr.readLine().trim());
                this.south = this.north + (1024.0d * this.degreesPerPixellat);
                this.east = this.west + (1024.0d * this.degreesPerPixellon);
                try {
                    bufRdr.close();
                } catch (IOException e) {
                    Log.e(mLogTag, "Error Buffered Reader closing file " + Constants.appPath + REGISTRATIONFILE);
                    e.printStackTrace();
                    new Logger(mLogTag).writeException(e);
                }
                this.texturesLoaded = false;
                this.REGISTRATIONFILEREAD = true;
            } catch (Exception e2) {
                Log.e(mLogTag, "Error Buffered Reader Radar Info File " + Constants.appPath + REGISTRATIONFILE);
                e2.printStackTrace();
                new Logger(mLogTag).writeException(e2);
                try {
                    Log.e(mLogTag, "Closing buffered reader due to Exception " + Constants.appPath + REGISTRATIONFILE);
                    bufRdr.close();
                } catch (IOException e12) {
                    e12.printStackTrace();
                    new Logger(mLogTag).writeException(e12);
                    Log.e(mLogTag, "IO Exception  " + Constants.appPath + REGISTRATIONFILE);
                }
            }
        }
    }

    public void Process(Location loc) {
        this.radarLocation = loc;
        initParams();
    }

    private void ReadImageFile(GL10 gl, String filename) {
        if (this.BUSY) {
            Log.e(mLogTag, "Loading Textures Already Busy");
            return;
        }
        this.BUSY = true;
        this.texturesLoaded = TextureOperations.invalidateTexture(gl, this.textures);
        Options opts = new Options();
        opts.inScaled = false;
        opts.inPreferredConfig = Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile(filename, opts);
        if (bmp != null) {
            this.south = this.north + (((double) bmp.getHeight()) * this.degreesPerPixellat);
            this.east = this.west + (((double) bmp.getWidth()) * this.degreesPerPixellon);
            calculateVertices();
            Bitmap texturebitmap = Bitmap.createScaledBitmap(bmp, 1024, 1024, false);
            if (VERSION.SDK_INT >= 14) {
                texturebitmap = TextureOperations.restoreGifTransparency(texturebitmap);
            }
            bmp.recycle();
            this.textures[0] = TextureOperations.loadTextureFromBitmapFast(gl, texturebitmap);
            texturebitmap.recycle();
            this.texturesLoaded = true;
        } else {
            Log.e(mLogTag, "Loaded Bitmap was null");
        }
        this.texturesLoading = false;
        this.BUSY = false;
    }

    public void unloadTextures(GL10 lastKnownGL2) {
        this.texturesLoaded = TextureOperations.invalidateTexture(lastKnownGL2, this.textures);
    }
}


* */


/*
*

Each of the RIDGE radar image has a "world file" associated with it.
A world file is an ASCII text file associated with an image and
contains the following lines:
Line 1: x-dimension of a pixel in map units
Line 2: rotation parameter
Line 3: rotation parameter
Line 4: NEGATIVE of y-dimension of a pixel in map units
Line 5: x-coordinate of center of upper left pixel
Line 6: y-coordinate of center of upper left pixel


* */



public object UtilityConusRadar {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    var conusbitmap: Bitmap? = null

    /*
        var gfw1 = ""
        var gfw2 = ""
        var gfw3 = ""
        var gfw4 = ""
        var gfw5 = ""
        var gfw6 = ""
*/


        private var initialized = false
        private var lastRefresh = 0.toLong()
        private val REFRESH_LOC_MIN = 5

/*
    fun getConusGfw() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            val currentTime1 = System.currentTimeMillis()
            val currentTimeSec = currentTime1 / 1000
            val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
            if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {

                //val urlgfw = "https://radar.weather.gov/Conus/RadarImg/latest_radaronly.gfw"
                val urlgfw = "${MyApplication.nwsRadarWebsitePrefix}/Conus/RadarImg/latest_radaronly.gfw"

                //READ and parase GFW file....
                var gfw = (urlgfw).getHtmlSep()
                UtilityLog.d("wx-elys", "gfw: " + gfw)
                val gfwArr = gfw.split("<br>").dropLastWhile { it.isEmpty() }
                //var tmpArr: List<String>
                gfwArr.forEach {
                    gfw1 = gfwArr[0]
                    gfw2 = gfwArr[1]
                    gfw3 = gfwArr[2]
                    gfw4 = gfwArr[3]
                    gfw5 = gfwArr[4]
                    gfw6 = gfwArr[5]
                }
            }

            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000

            //TODO TESTING
            var teststr = gfw1 + "\n" + gfw2 + "\n" + gfw3 + "\n" + gfw4 + "\n" + gfw5 + "\n" + gfw6 + "\n"
            UtilityLog.d("wx-elys", "conus gfw: " + teststr)


        }

    }
*/


    private fun getFileOutputStream(fileName: String): FileOutputStream? {
        var fos: FileOutputStream? = null
        try {
            //val dir = File(context.filesDir.toString())
            val dir = File(GlobalVariables.FilesPath)
            //if (!dir.mkdirs())
            //    UtilityLog.d("wx-elys", "failed to mkdir: " + context.filesDir)
            val file = File(dir, fileName)
            fos = FileOutputStream(file)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return fos
    }

    // save image
    private fun saveImage(bitmap: Bitmap) {
        val fos = getFileOutputStream(GlobalVariables.conusImageName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos!!)
        fos?.close()
    }


    fun layerDrawableToBitmapConusSquareOrg(layers: MutableList<Drawable>): Bitmap {
        val drawable = LayerDrawable(layers.toTypedArray())
        val bitmap: Bitmap
        val width = 3400
        val height = 3400
        if (width > 0 && height > 0) {
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                return UtilityImg.getBlankBitmap()
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 800, 3400, 2450)
            drawable.draw(canvas)
        } else {
            bitmap = UtilityImg.getBlankBitmap()
        }
        return bitmap
    }

    //adjusted for size of new ridge radar.
    fun layerDrawableToBitmapConusSquare(layers: MutableList<Drawable>): Bitmap {
        val drawable = LayerDrawable(layers.toTypedArray())
        val bitmap: Bitmap
        val width = 3400
        val height = 3400
        if (width > 0 && height > 0) {
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                return UtilityImg.getBlankBitmap()
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 800, 3400, 3400)
            drawable.draw(canvas)
        } else {
            bitmap = UtilityImg.getBlankBitmap()
        }
        return bitmap
    }


    fun nwsConusRadarSquare(context: Context) {
        val imgUrl =
                "${GlobalVariables.nwsConusRadar}"
        val layers = mutableListOf<Drawable>()
        var bitmap = imgUrl.getImage()
        var bitmapCanvas = UtilityImg.getBlankBitmap()
        //if (MyApplication.blackBg) {
        bitmap = UtilityImg.eraseBackground(bitmap, -1)
        //}
        if (bitmap.height > 10) {
            bitmapCanvas = Bitmap.createBitmap(3400, 3400, Bitmap.Config.ARGB_8888)

        }

        UtilityLog.d("wx-elys", "bitmap size: "+bitmap.width +" "+bitmap.height)
        UtilityLog.d("wx-elys", "bitmapcanvas size: "+bitmapCanvas.width +" "+bitmapCanvas.height)
        layers.add(BitmapDrawable(context.resources, bitmap))
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        //val finalbitmap: Bitmap = UtilityImg.layerDrawableToBitmap(layers)
        val finalbitmap: Bitmap = layerDrawableToBitmapConusSquare(layers)
        saveImage(finalbitmap)
    }

    //TODO Hack job! need to use real plotting with nwsConusRadar(context: Context)
    fun nwsConusRadarWithMap(context: Context) {
        val imgUrl =
                "${GlobalVariables.nwsConusRadar}"
        val layers = mutableListOf<Drawable>()
        val cd = if (RadarPreferences.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        var scaleType = ProjectionType.WX_RENDER
        var bitmap = imgUrl.getImage()
        var bitmapCanvas = UtilityImg.getBlankBitmap()
        //if (MyApplication.blackBg) {
        //    bitmap = UtilityImg.eraseBG(bitmap, -1)
        //}
        if (bitmap.height > 10) {
            bitmapCanvas = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            CanvasMain.addCanvasConus(context, bitmapCanvas, scaleType, "latest")
        }
        layers.add(cd)
        layers.add(BitmapDrawable(context.resources, bitmap))
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        val finalbitmap: Bitmap = UtilityImg.layerDrawableToBitmap(layers)
        saveImage(finalbitmap)
    }
    //TODO Hack job! need to use real plotting with nwsConusRadar(context: Context)
    fun nwsConusRadarWithMapSquare(context: Context) {
        val imgUrl = "${GlobalVariables.nwsConusRadar}"
        val layers = mutableListOf<Drawable>()
        val cd = if (RadarPreferences.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        var scaleType = ProjectionType.WX_RENDER
        var bitmap = imgUrl.getImage()
        var bitmapCanvas = UtilityImg.getBlankBitmap()
        //if (MyApplication.blackBg) {
        //    bitmap = UtilityImg.eraseBG(bitmap, -1)
        //}
        //testing 12-30-20
        //bitmap = UtilityImg.eraseBackground(bitmap, Color.WHITE)
        //testing 12-30-20
        //bitmap.eraseColor(Color.TRANSPARENT);
        //bitmap = UtilityImg.eraseBackground(bitmap, Color.WHITE)
        //test //erase light blue
        //bitmap = UtilityImg.eraseBackground(bitmap, Color.rgb(204,255,255))


        //var bitmap1 = UtilityImg.changeColor(bitmap, Color.WHITE, Color.BLACK)

        var fbitmap = bitmap

        if (fbitmap.height > 10) {
            bitmapCanvas = Bitmap.createBitmap(fbitmap.width, fbitmap.height, Bitmap.Config.ARGB_8888)
            CanvasMain.addCanvasConus(context, bitmapCanvas, scaleType, "latest")
        }
        layers.add(cd)
        layers.add(BitmapDrawable(context.resources, fbitmap))
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        val finalbitmap: Bitmap = layerDrawableToBitmapConusSquare(layers)
        saveImage(finalbitmap)
    }


    fun nwsConusRadar(context: Context) {
        val imgUrl = "${GlobalVariables.nwsConusRadar}"
        val layers = mutableListOf<Drawable>()
        var bitmap = imgUrl.getImage()
        bitmap = UtilityImg.eraseBackground(bitmap, -1)
        layers.add(BitmapDrawable(context.resources, bitmap))
        saveImage(bitmap)
    }

    fun getConusImage() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            //nwsConusRadarWithMapSquare(MyApplication.appContext)
        try {
            nwsConusRadarWithMapSquare(MyApplication.appContext)
        } catch (e: Exception) {
        UtilityLog.d("wx-elys", "getConusImage() failed! error: "+e.toString())
        UtilityLog.handleException(e)
    }
        }
    }

/*
    fun resizeConus(imagefile: String): Bitmap {
        val bitmap: Bitmap? = OpenGLShader.LoadBitmap(imagefile)
        return Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)
    }
*/

    /*
    //get conus bitmap!!//
    fun nwsConusRadarBitmap(context: Context): Bitmap {
        val imgUrl = "${MyApplication.nwsRadarWebsitePrefix}/Conus/RadarImg/latest_radaronly.gif"
        val layers = mutableListOf<Drawable>()
        var bitmap = imgUrl.getImage()
        bitmap = UtilityImg.eraseBG(bitmap, -1)
        layers.add(BitmapDrawable(context.resources, bitmap))
        //saveImage(context, bitmap, "conus.gif")
        return UtilityImg.layerDrawableToBitmap(layers)
    }
*/

}






/*

import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityImg


object UtilityConusRadar {




    fun getconus(context: Context): Bitmap {
        //val imgUrl = "http://radar.weather.gov/Conus/RadarImg/latest_radaronly.gif"

        val imgUrl = MyApplication.NWS_CONUS_RADAR

        val layers = mutableListOf<Drawable>()
        val cd = if (MyApplication.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        var scaleType = ProjectionType.NWS_MOSAIC

        var bitmap = imgUrl.getImage()
        var bitmapCanvas = UtilityImg.getBlankBitmap()
        if (MyApplication.blackBg) {
            bitmap = UtilityImg.eraseBG(bitmap, -1)
        }
        if (bitmap.height > 10) {
            bitmapCanvas = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, "latest", 1, 13, false)
        }
        layers.add(cd)
        layers.add(BitmapDrawable(context.resources, bitmap))
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    }

    */
