package joshuatee.wx

import android.Manifest
import android.os.Bundle
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.*
import joshuatee.wx.util.Utility
import java.io.File
import java.io.FileOutputStream
import androidx.core.app.ActivityCompat
import com.intentfilter.androidpermissions.PermissionManager
import java.util.Collections.singleton


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
        if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
            ObjectIntent(this, WX::class.java)
        } else {
            val nws1Current = Location.wfo
            val nws1StateCurrent = Utility.readPref(this, "NWS_LOCATION_$nws1Current", "").split(",")[0]
            val rid1 = Location.getRid(this, Location.currentLocationStr)
            ObjectIntent(this, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, arrayOf(rid1, nws1StateCurrent))
        }


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

        //storage permission so we can run checkfiles for custom icons//
        val storagepermissionManager = PermissionManager.getInstance(this)
        storagepermissionManager.checkPermissions(singleton(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {
                Log.i(TAG, "Storage Permissions Granted")
                checkfiles(R.drawable.headingbug, "headingbug.png")
                checkfiles(R.drawable.location, "location.png")
            }

            override fun onPermissionDenied() {
                Log.i(TAG, "Storage Permissions Denied")
            }
        })

        finish()
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
            Log.i(TAG, "files does not exist.")
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
            Log.i(TAG, "file copied!")
        } catch (e: Exception) {
            Log.i(TAG, "checkfiles Exception!")
            e.printStackTrace()
        }

    }
}
