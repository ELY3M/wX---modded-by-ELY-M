//modded by ELY M.  

package joshuatee.wx

import android.Manifest
import android.os.Bundle
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.*
import joshuatee.wx.util.Utility
import androidx.core.app.ActivityCompat
import com.intentfilter.androidpermissions.PermissionManager
import joshuatee.wx.util.UtilityLog
import java.util.Collections.singleton
import java.io.*
import java.nio.charset.Charset
import android.content.Context
import android.net.ConnectivityManager
import joshuatee.wx.ui.ObjectAlertDetail
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.UtilityUI.makeToastLegacy
import joshuatee.wx.util.UtilityAlertDialog.showDialogBox
import joshuatee.wx.util.UtilityAlertDialog.showDialogueWithContext
import joshuatee.wx.util.UtilityAlertDialog.showHelpText


class StartupActivity : Activity(), ActivityCompat.OnRequestPermissionsResultCallback {

    // This activity is the first activity started when the app starts.
    // It's job is to initialize preferences if not done previously,
    // display the splash screen, start the service that handles notifications,
    // and display the version in the title.
    //

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
        if (Utility.readPrefWithNull(
                this,
                "SND_LIX_X",
                null
            ) == null
        ) UtilityPref4.prefInitSoundingSitesLoc(this)
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
                runme()

            }

            override fun onPermissionDenied() {
                UtilityLog.d("wx", "Storage Permissions Denied")
            }
        })


        //location permission//  this is needed!
        val locationpermissionManager = PermissionManager.getInstance(this)
        locationpermissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_FINE_LOCATION), object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {
                UtilityLog.d("wx", "Location Permissions Granted")
            }

            override fun onPermissionDenied() {
                UtilityLog.d("wx", "Location Permissions Denied")
            }
        })



        finish()
    }


    fun runme() {

        UtilityLog.d("wx", "Storage Permissions Granted")
        checkfiles(R.drawable.headingbug, "headingbug.png")
        checkfiles(R.drawable.star_cyan, "star_cyan.png")
        checkfiles(R.drawable.location, "location.png")
        checkfiles(R.drawable.tvs, "tvs.png")
        checkfiles(R.drawable.userpoint, "userpoint.png")

        checkfiles(R.drawable.hail05, "hail05.png")
        checkfiles(R.drawable.hail0, "hail0.png")
        checkfiles(R.drawable.hail1, "hail1.png")
        checkfiles(R.drawable.hail2, "hail2.png")
        checkfiles(R.drawable.hail3, "hail3.png")
        checkfiles(R.drawable.hail4, "hail4.png")
        checkfiles(R.drawable.hailbig, "hailbig.png")

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

        if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
            ObjectIntent(this, WX::class.java)
        } else {
            val nws1Current = Location.wfo
            val nws1StateCurrent = Utility.readPref(this, "NWS_LOCATION_$nws1Current", "").split(",")[0]
            val rid1 = Location.getRid(this, Location.currentLocationStr)
            ObjectIntent(this, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, arrayOf(rid1, nws1StateCurrent))

        }

    }



    fun checkfiles(drawable: Int, filename: String) {
        UtilityLog.d("wx", "running files check on "+MyApplication.FilesPath)
        val dir = File(MyApplication.FilesPath)
        if (!dir.exists()) {
            UtilityLog.d("wx", "making dir")
            dir.mkdirs()
        }

        var file = File(MyApplication.FilesPath+filename)
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            UtilityLog.d("wx", filename+" does not exist.")
            var bitmap: Bitmap = BitmapFactory.decodeResource(resources, drawable)
            saveBitmapToFile(filename, bitmap)

        } else {
            UtilityLog.d("wx", filename+" are there!")
        }
    }

    fun saveBitmapToFile(fileName: String, bm: Bitmap) {
        val file = File(MyApplication.FilesPath, fileName)
        try {
            val out = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            UtilityLog.d("wx", fileName+" copied!")
        } catch (e: Exception) {
            UtilityLog.d("wx", "checkfiles Exception!")
            e.printStackTrace()
        }

    }


    //check colortable files and copy if any missing//
    fun checkpalfiles(resourceId: Int, filename: String) {
        UtilityLog.d("wx", "running files check on "+MyApplication.PalFilesPath)
        val dir = File(MyApplication.PalFilesPath)
        if (!dir.exists()) {
            UtilityLog.d("wx", "making dir")
            dir.mkdirs()
        }

        var file = File(MyApplication.PalFilesPath+filename)
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            UtilityLog.d("wx", filename+" does not exist.")
            saveRawToFile(filename, resourceId)
        } else {
            UtilityLog.d("wx", filename+" are there!")
        }
    }

    private fun saveRawToFile(fileName: String, resourceId: Int) {
        val dir = MyApplication.PalFilesPath
        var ins:InputStream = resources.openRawResource(resourceId)
        var content = ins.readBytes().toString(Charset.defaultCharset())
        File("$dir/$fileName").printWriter().use {
            it.println(content)
        }
    }








}
