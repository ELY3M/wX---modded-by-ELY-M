package joshuatee.wx.radar

/*
*
* Big Credit to pykl3 and Joe Jurecka for this function
*
* */

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build.FINGERPRINT
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import joshuatee.wx.MyApplication
import joshuatee.wx.radar.LatLon
import joshuatee.wx.util.UtilityDownload.getBitmapFromURLS
import joshuatee.wx.util.UtilityDownload.getStringFromURLS
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Semaphore
import javax.microedition.khronos.opengles.GL10

class ConusRadar(private val mContext: Context, ct: Float) {


    val TAG = "ConusRadar"
    val mVerbose = true

    private var BUSY: Boolean = false
    private val BindexBuffer: ShortBuffer?
    private val BtextureBuffer: FloatBuffer?
    private val BvertexBuffer: IntBuffer?
    var FEATURE_ENABLED = true
    private var REGISTRATIONFILEREAD: Boolean = false
    private val conusSemaphore = Semaphore(1, true)
    private var degreesPerPixellat = -0.017971305190311
    private var degreesPerPixellon = 0.017971305190311
    private var east: Double = 0.toDouble()
    private var north: Double = 0.toDouble()
    private var okToPlot = false
    private var south: Double = 0.toDouble()
    private val textures = IntArray(1)
    private var texturesLoaded = false
    private var texturesLoading = false
    private var west: Double = 0.toDouble()

    private var Bibb: ByteBuffer? = null
    private var Btbb: ByteBuffer? = null
    private var Bvbb: ByteBuffer? = null
    val IMAGEFILE = MyApplication.FilesPath + "conus.gif"
    val REGISTRATIONFILE = MyApplication.FilesPath + "conus.gfw"
    private val bitmapheight = 1024
    private val bitmapwidth = 1024
    private val maxpoints = 130
    private val pixelsPerPoint = 16


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i(TAG, "onLocationChanged lat: " + location.latitude + " lon: " + location.longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }


    val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000.toLong(), 30.toFloat(), locationListener)
    var radarLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)


    fun downloadFile(url: String, name: String): File {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val file = File(MyApplication.FilesPath, name)
        val body = response.body()
        val sink = Okio.buffer(Okio.sink(file))

        body?.source().use { input ->
            sink.use { output ->
                output.writeAll(input)
            }
        }

        return file
    }

    fun AcquireData() {



        //fun doInBackground(vararg voids: Void): Void? {
            try {
                conusSemaphore.acquire()
                Log.i(TAG, "downloading conus")
                //getStringFromURLS(MyApplication.NWS_CONUS_RADAR_GFW)
                //getBitmapFromURLS(MyApplication.NWS_CONUS_RADAR)
                downloadFile(MyApplication.NWS_CONUS_RADAR, "conus.gif")
                downloadFile(MyApplication.NWS_CONUS_RADAR_GFW, "conus.gfw")
                if (File(REGISTRATIONFILE).exists()) {
                    Log.i(TAG, "found reg file - running initParams()")
                    initParams(radarLocation)
                } else {
                    Log.i(TAG, "failed to find regfile")
                }
                conusSemaphore.release()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                conusSemaphore.release()
            } catch (th: Throwable) {
                conusSemaphore.release()
                throw th
            }

            //return null
        //}
    }

    init {
        Btbb = ByteBuffer.allocateDirect(1040)
        Btbb!!.order(ByteOrder.nativeOrder())
        Bvbb = ByteBuffer.allocateDirect(520)
        Bvbb!!.order(ByteOrder.nativeOrder())
        this.BtextureBuffer = Btbb!!.asFloatBuffer()
        this.BvertexBuffer = Bvbb!!.asIntBuffer()
        Bibb = ByteBuffer.allocateDirect(768)
        Bibb!!.order(ByteOrder.nativeOrder())
        this.BindexBuffer = Bibb!!.asShortBuffer()
    }

    fun calculateVertices() {
        if (radarLocation != null) {
            this.okToPlot = false
            ClearVbb()
            val cc = CoordinateConversion()
            val vertices = IntArray(4)
            val tvertices = FloatArray(4)
            val indices = ShortArray(6)
            var eastwestpoints = 0
            val deltalat = (this.east - this.west) / 1024.0 * 16.0
            val deltalon = (this.north - this.south) / 1024.0 * 16.0
            var i = 0.0
            while (i < 65.toDouble()) {
                val lo = this.west + i * deltalat
                var loc = cc.latLonToGl(radarLocation.latitude, radarLocation.longitude, this.south, lo)
                vertices[0] = loc.y
                vertices[1] = loc.x
                tvertices[0] = (eastwestpoints * 16).toFloat() / 1024.0f
                tvertices[1] = 1.0f
                loc = cc.latLonToGl(radarLocation.latitude, radarLocation.longitude, this.north, lo)
                vertices[2] = loc.y
                vertices[3] = loc.x
                tvertices[2] = (eastwestpoints * 16).toFloat() / 1024.0f
                tvertices[3] = 0.0f
                this.BvertexBuffer!!.put(vertices)
                this.BtextureBuffer!!.put(tvertices)
                eastwestpoints++
                i += 1.0
            }
            var idx = 0.toShort()
            while (idx < 64.toShort()) {
                indices[0] = (idx * 2).toShort()
                indices[1] = (idx * 2 + 1).toShort()
                indices[2] = (idx * 2 + 3).toShort()
                indices[3] = (idx * 2).toShort()
                indices[4] = (idx * 2 + 3).toShort()
                indices[5] = (idx * 2 + 2).toShort()
                this.BindexBuffer!!.put(indices)
                idx = (idx + 1).toShort()
            }
            this.BvertexBuffer!!.position(0)
            this.BtextureBuffer!!.position(0)
            this.BindexBuffer!!.position(0)
            this.okToPlot = true
        }
    }

    fun ClearVbb() {
        this.okToPlot = false
        if (Bvbb != null) {
            Bvbb!!.clear()
        }
        if (this.BvertexBuffer != null) {
            this.BvertexBuffer.clear()
        }
        if (Btbb != null) {
            Btbb!!.clear()
        }
        if (this.BtextureBuffer != null) {
            this.BtextureBuffer.clear()
        }
        if (Bibb != null) {
            Bibb!!.clear()
        }
        if (this.BindexBuffer != null) {
            this.BindexBuffer.clear()
        }
    }

    //fun DownloadAndProcess(radarLoc: Location) {
    //    this.radarLocation = radarLoc
    //    AcquireData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *arrayOfNulls(0))
    //}

    fun RecalculateVertices(loc: Location) {
        radarLocation = loc
        //calculateVertices()
    }

    fun draw(gl: GL10) {
        Log.i(TAG, "draw")
        if (REGISTRATIONFILEREAD) {
            Log.i(TAG, "draw2")
            gl.glFrontFace(2304)
            gl.glEnable(2884)
            gl.glCullFace(1029)
            gl.glEnableClientState(32884)
            gl.glEnable(3553)
            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f)
            gl.glVertexPointer(2, 5122, 0, this.BvertexBuffer)
            if (!this.texturesLoaded) {
                if (!this.texturesLoading) {
                    this.texturesLoading = true
                    ReadImageFile(gl, IMAGEFILE)
                    this.texturesLoading = false
                } else {
                    return
                }
            }
            if (this.okToPlot) {
                gl.glEnableClientState(32888)
                gl.glTexCoordPointer(2, 5126, 0, this.BtextureBuffer)
                gl.glBindTexture(3553, this.textures[0])
                gl.glDrawElements(4, this.BindexBuffer!!.capacity(), 5123, this.BindexBuffer)
                gl.glDisableClientState(32888)
                gl.glDisable(3553)
                gl.glDisableClientState(32884)
                gl.glDisable(2884)
            }
        } else {
            Log.i(TAG, "read reg file failed "+REGISTRATIONFILE)
            Log.i(TAG, "trying to parsefile again")
            ParseFile()
        }
    }

    private fun initParams(radarLocation: Location) {
        if (radarLocation != null) {
            Log.i(TAG, "ParseFile()")
            ParseFile()
        } else {
            Log.i(TAG, "radarLocation is null!")
        }
    }

    private fun ParseFile() {
        Log.i(TAG, "parsefile")
        this.okToPlot = false
        this.REGISTRATIONFILEREAD = false
        if (File(REGISTRATIONFILE).exists() && File(IMAGEFILE).exists()) {
            Log.i(TAG, "found both files")
            var bufRdr: BufferedReader? = null
            try {
                bufRdr = BufferedReader(FileReader(REGISTRATIONFILE), 16384)

            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
                Log.i(TAG, "FileNotFoundException "+REGISTRATIONFILE)
            }

            try {

                degreesPerPixellon = java.lang.Double.parseDouble(bufRdr!!.readLine().trim { it <= ' ' })
                val skew1 = java.lang.Double.parseDouble(bufRdr.readLine().trim { it <= ' ' })
                val skew2 = java.lang.Double.parseDouble(bufRdr.readLine().trim { it <= ' ' })
                degreesPerPixellat = java.lang.Double.parseDouble(bufRdr.readLine().trim { it <= ' ' })
                west = java.lang.Double.parseDouble(bufRdr.readLine().trim { it <= ' ' })
                north = java.lang.Double.parseDouble(bufRdr.readLine().trim { it <= ' ' })


                south = north + 1024.0 * this.degreesPerPixellat
                east = west + 1024.0 * this.degreesPerPixellon


                Log.i(TAG, "degreesPerPixellon: "+degreesPerPixellon)
                Log.i(TAG, "degreesPerPixellat: "+degreesPerPixellat)
                Log.i(TAG, "north: "+north)
                Log.i(TAG, "south: "+south)
                Log.i(TAG, "west: "+west)
                Log.i(TAG, "east: "+east)

                try {
                    bufRdr.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error Buffered Reader closing file " + REGISTRATIONFILE)
                    e.printStackTrace()
                }

                this.texturesLoaded = false
                this.REGISTRATIONFILEREAD = true
            } catch (e2: Exception) {
                Log.e(TAG, "Error Buffered Reader Radar Info File " + REGISTRATIONFILE)
                e2.printStackTrace()
                try {
                    Log.e(TAG, "Closing buffered reader due to Exception " + REGISTRATIONFILE)
                    bufRdr!!.close()
                } catch (e12: IOException) {
                    e12.printStackTrace()
                    Log.e(TAG, "IO Exception  " + REGISTRATIONFILE)
                }

            }

        } else {
            Log.i(TAG, "read reg/img file failed" +IMAGEFILE)
        }
    }

    fun Process(loc: Location) {
        radarLocation = loc
        initParams(loc)
    }

    private fun ReadImageFile(gl: GL10, filename: String) {
        Log.i(TAG, "ReadImageFile")
        if (this.BUSY) {
            Log.e(TAG, "Loading Textures Already Busy")
            return
        }
        this.BUSY = true
        this.texturesLoaded = TextureOperations.invalidateTexture(gl, this.textures)
        val opts = Options()
        opts.inScaled = false
        opts.inPreferredConfig = Config.ARGB_8888
        val bmp = BitmapFactory.decodeFile(filename, opts)
        if (bmp != null) {
            south = north + bmp.height.toDouble() * degreesPerPixellat
            east = west + bmp.width.toDouble() * degreesPerPixellon

            Log.i(TAG, "ReadImageFile degreesPerPixellon: "+degreesPerPixellon)
            Log.i(TAG, "ReadImageFile degreesPerPixellat: "+degreesPerPixellat)
            Log.i(TAG, "ReadImageFile north: "+north)
            Log.i(TAG, "ReadImageFile south: "+south)
            Log.i(TAG, "ReadImageFile west: "+west)
            Log.i(TAG, "ReadImageFile east: "+east)


            //calculateVertices()
            var texturebitmap = Bitmap.createScaledBitmap(bmp, 1024, 1024, false)
            if (VERSION.SDK_INT >= 14) {
                texturebitmap = TextureOperations.restoreGifTransparency(texturebitmap)
            }
            bmp.recycle()
            this.textures[0] = TextureOperations.loadTextureFromBitmapFast(gl, texturebitmap)
            texturebitmap.recycle()
            this.texturesLoaded = true
        } else {
            Log.e(TAG, "Loaded Bitmap was null")
        }
        this.texturesLoading = false
        this.BUSY = false
    }

    fun unloadTextures(lastKnownGL2: GL10) {
        this.texturesLoaded = TextureOperations.invalidateTexture(lastKnownGL2, this.textures)
    }


}
