package joshuatee.wx

import android.Manifest
import android.os.Bundle
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.system.Os.read
import android.util.Log

import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.*
import joshuatee.wx.util.Utility
import androidx.core.app.ActivityCompat
import com.intentfilter.androidpermissions.PermissionManager
import java.lang.System.out
import java.util.Collections.singleton
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Environment.getExternalStorageDirectory
import java.io.*
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Handler
import java.nio.charset.Charset
import java.util.*


class StartupActivity : Activity(), ActivityCompat.OnRequestPermissionsResultCallback {

    // This activity is the first activity started when the app starts.
    // It's job is to initialize preferences if not done previously,
    // display the splash screen, start the service that handles notifications,
    // and display the version in the title.
    //

    var TAG = "joshuatee-StartupActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Utility.readPrefWithNull(this, "NWS_UNR_X", null) == null) {
            UtilityPref.prefInitStateCode(this)
            UtilityPref.prefInitStateCodeLookup(this)
            UtilityPref.prefInitNWSXY(this)
            UtilityPref.prefInitRIDXY(this)
            UtilityPref.prefInitRIDXY2(this)
            UtilityPref.prefInitNWSLoc(this)
            UtilityPref2.prefInitSetDefaults(this)
            UtilityPref3.prefInitRIDLoc(this)
            UtilityPref.prefInitBig(this)
            UtilityPref.prefInitTwitterCA(this)
            UtilityPref4.prefInitSoundingSites(this)
        }
        if (Utility.readPrefWithNull(this, "SND_AOT_X", null) == null) UtilityPref4.prefInitSoundingSitesLoc(this)
        MyApplication.initPreferences(this)
        Location.refreshLocationData(this)
        UtilityWXJobService.startService(this)
        if (UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotif(applicationContext, "")
        }


        //storage permission so we can run checkfiles for custom icons//
        val storagepermissionManager = PermissionManager.getInstance(this)
        storagepermissionManager.checkPermissions(singleton(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {
                Log.i(TAG, "Storage Permissions Granted")
                checkfiles(R.drawable.headingbug, "headingbug.png")
                checkfiles(R.drawable.star_cyan, "star_cyan.png")
                checkfiles(R.drawable.location, "location.png")

                checkpalfiles(R.raw.colormap134cod, "colormap134cod.txt")
                checkpalfiles(R.raw.colormap135cod, "colormap135cod.txt")
                checkpalfiles(R.raw.colormap159cod, "colormap159cod.txt")
                checkpalfiles(R.raw.colormap161cod, "colormap161cod.txt")
                checkpalfiles(R.raw.colormap163cod, "colormap163cod.txt")
                checkpalfiles(R.raw.colormap165cod, "colormap165cod.txt")
                checkpalfiles(R.raw.colormap172cod, "colormap172cod.txt")
                checkpalfiles(R.raw.colormapbvaf, "colormapbvaf.txt")
                checkpalfiles(R.raw.colormapbvcod, "colormapbvcod.txt")
                checkpalfiles(R.raw.colormapbveak, "colormapbveak.txt")
                checkpalfiles(R.raw.colormaprefaf, "colormaprefaf.txt")
                checkpalfiles(R.raw.colormaprefcode, "colormaprefcode.txt")
                checkpalfiles(R.raw.colormaprefcodenh, "colormaprefcodenh.txt")
                checkpalfiles(R.raw.colormaprefdkenh, "colormaprefdkenh.txt")
                checkpalfiles(R.raw.colormaprefeak, "colormaprefeak.txt")
                checkpalfiles(R.raw.colormaprefmenh, "colormaprefmenh.txt")
                checkpalfiles(R.raw.colormaprefnssl, "colormaprefnssl.txt")
                checkpalfiles(R.raw.colormaprefnwsd, "colormaprefnwsd.txt")
                //elys custom color tables
                checkpalfiles(R.raw.colormapownref, "colormapownref.txt")
                checkpalfiles(R.raw.colormapownvel, "colormapownvel.txt")
                checkpalfiles(R.raw.colormapownenhvel, "colormapownenhvel.txt")
                //need to run it again
                ColorPalettes.init(applicationContext)

                runme()



            }

            override fun onPermissionDenied() {
                Log.i(TAG, "Storage Permissions Denied")
            }
        })






        //location permission//  this is needed!
        val locationpermissionManager = PermissionManager.getInstance(this)
        locationpermissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_FINE_LOCATION), object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {
                Log.i(TAG, "Location Permissions Granted")
            }

            override fun onPermissionDenied() {
                Log.i(TAG, "Location Permissions Denied")
            }
        })


        finish()
    }


    fun runme() {

        if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
            ObjectIntent(this, WX::class.java)
        } else {
            val nws1Current = Location.wfo
            val nws1StateCurrent = Utility.readPref(this, "NWS_LOCATION_$nws1Current", "").split(",")[0]
            val rid1 = Location.getRid(this, Location.currentLocationStr)
            ObjectIntent(this, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, arrayOf(rid1, nws1StateCurrent))

        }

    }

fun checkperms() {


    //storage permission so we can run checkfiles for custom icons//
    val storagepermissionManager = PermissionManager.getInstance(this)
    storagepermissionManager.checkPermissions(singleton(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionManager.PermissionRequestListener {
        override fun onPermissionGranted() {
            Log.i(TAG, "Storage Permissions Granted")
            checkfiles(R.drawable.headingbug, "headingbug.png")
            checkfiles(R.drawable.star_cyan, "star_cyan.png")
            checkfiles(R.drawable.location, "location.png")

            checkpalfiles(R.raw.colormap134cod, "colormap134cod.txt")
            checkpalfiles(R.raw.colormap135cod, "colormap135cod.txt")
            checkpalfiles(R.raw.colormap159cod, "colormap159cod.txt")
            checkpalfiles(R.raw.colormap161cod, "colormap161cod.txt")
            checkpalfiles(R.raw.colormap163cod, "colormap163cod.txt")
            checkpalfiles(R.raw.colormap165cod, "colormap165cod.txt")
            checkpalfiles(R.raw.colormap172cod, "colormap172cod.txt")
            checkpalfiles(R.raw.colormapbvaf, "colormapbvaf.txt")
            checkpalfiles(R.raw.colormapbvcod, "colormapbvcod.txt")
            checkpalfiles(R.raw.colormapbveak, "colormapbveak.txt")
            checkpalfiles(R.raw.colormaprefaf, "colormaprefaf.txt")
            checkpalfiles(R.raw.colormaprefcode, "colormaprefcode.txt")
            checkpalfiles(R.raw.colormaprefcodenh, "colormaprefcodenh.txt")
            checkpalfiles(R.raw.colormaprefdkenh, "colormaprefdkenh.txt")
            checkpalfiles(R.raw.colormaprefeak, "colormaprefeak.txt")
            checkpalfiles(R.raw.colormaprefmenh, "colormaprefmenh.txt")
            checkpalfiles(R.raw.colormaprefnssl, "colormaprefnssl.txt")
            checkpalfiles(R.raw.colormaprefnwsd, "colormaprefnwsd.txt")
            //elys custom color tables
            checkpalfiles(R.raw.colormapownref, "colormapownref.txt")
            checkpalfiles(R.raw.colormapownvel, "colormapownvel.txt")
            checkpalfiles(R.raw.colormapownenhvel, "colormapownenhvel.txt")

            ColorPalettes.init(applicationContext)


        }

        override fun onPermissionDenied() {
            Log.i(TAG, "Storage Permissions Denied")
        }
    })

}


    fun checkfiles(drawable: Int, filename: String) {
        Log.i(TAG, "running files check on "+MyApplication.FilesPath)
        val dir = File(MyApplication.FilesPath)
        if (!dir.exists()) {
            Log.i(TAG, "making dir")
            dir.mkdirs()
        }

        var file = File(MyApplication.FilesPath+filename)
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            Log.i(TAG, filename+" does not exist.")
            var bitmap: Bitmap = BitmapFactory.decodeResource(resources, drawable)
            saveBitmapToFile(filename, bitmap)

        } else {
            Log.i(TAG, filename+" are there!")
        }
    }

    fun saveBitmapToFile(fileName: String, bm: Bitmap) {
        val file = File(MyApplication.FilesPath, fileName)
        try {
            val out = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Log.i(TAG, fileName+" copied!")
        } catch (e: Exception) {
            Log.i(TAG, "checkfiles Exception!")
            e.printStackTrace()
        }

    }


    //check colortable files and copy if any missing//
    fun checkpalfiles(resourceId: Int, filename: String) {
        Log.i(TAG, "running files check on "+MyApplication.PalFilesPath)
        val dir = File(MyApplication.PalFilesPath)
        if (!dir.exists()) {
            Log.i(TAG, "making dir")
            dir.mkdirs()
        }

        var file = File(MyApplication.PalFilesPath+filename)
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            Log.i(TAG, filename+" does not exist.")
            saveRawToFile(filename, resourceId)
        } else {
            Log.i(TAG, filename+" are there!")
        }
    }

    private fun saveRawToFile(fileName: String, resourceId: Int) {
        val dir = MyApplication.PalFilesPath
        var ins:InputStream = resources.openRawResource(resourceId)
        var content = ins.readBytes().toString(Charset.defaultCharset())
        //println(content)
        File("$dir/$fileName").printWriter().use {
            it.println(content)
        }
    }



}
