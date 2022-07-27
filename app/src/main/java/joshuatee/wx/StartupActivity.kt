//modded by ELY M.  

package joshuatee.wx

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.intentfilter.androidpermissions.PermissionManager
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.Route
import joshuatee.wx.radarcolorpalettes.ColorPalettes
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityStorePreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.io.*
import java.nio.charset.Charset
import java.util.Collections.singleton
import kotlin.system.exitProcess


class StartupActivity : Activity(), ActivityCompat.OnRequestPermissionsResultCallback {

    //
    // This activity is the first activity started when the app starts.
    // It's job is to initialize preferences if not done previously,
    // display the splash screen, start the service that handles notifications,
    // and display the version in the title.
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Utility.readPrefWithNull(this, "LOC1_LABEL", null) == null) {
            UtilityStorePreferences.setDefaults(this)
        }
        MyApplication.initPreferences(this)
        Location.refreshLocationData(this)
        println("UtilityWXJobService started")
        UtilityWXJobService.startService(this)
        if (UIPreferences.mediaControlNotif) {
            UtilityNotification.createMediaControlNotification(applicationContext, "")
        }

        //storage permission so we can run checkfiles for custom icons//
        val storagepermissionManager = PermissionManager.getInstance(this)
        storagepermissionManager.checkPermissions(singleton(Manifest.permission.WRITE_EXTERNAL_STORAGE), object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {

                //FUCK YOU GOOGLE!!!!!!  They kept changing their code to "secure" the storage   FUCK YOU
                //my ass will be at your new HQ offices and chewing you out for what you did to me.  I fucking hate companies that censor.
                //I will make you pay my fucking income!!!!  FUCK YOU!!!!
                //Google company Execs need metal pipes in their asses for breaking their promoise not to censor!!!!!
                //file access permission functions moved to WX.kt
                if(SDK_INT >= 30) {
                    if(!Environment.isExternalStorageManager()) {
                        Toast.makeText(applicationContext, "This app need access to your phone memory or SD Card to make files and write files (/wX/ on your phone memory or sd card)\nThe all file access settings will open. Make sure to toggle it on to enable all files access for this app to function fully.\n You need to restart the app after you enabled the all files access for this app in the settings.\n", Toast.LENGTH_LONG).show()
                        val intent = Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                        //force restart :/
                        exitProcess(0)
                    } else {
                        runme()
                    }
                } else {
                    runme()
                }

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
        checkfiles(R.drawable.hailunknown, "hailunknown.png")
        checkfiles(R.drawable.hail05, "hail05.png")
        checkfiles(R.drawable.hail0, "hail0.png")
        checkfiles(R.drawable.hail1, "hail1.png")
        checkfiles(R.drawable.hail2, "hail2.png")
        checkfiles(R.drawable.hail3, "hail3.png")
        checkfiles(R.drawable.hail4, "hail4.png")
        checkfiles(R.drawable.hailbig, "hailbig.png")

        checkpalfiles(R.raw.colormap19, "colormap19.txt")
        checkpalfiles(R.raw.colormap30, "colormap30.txt")
        checkpalfiles(R.raw.colormap56, "colormap56.txt")
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
        ColorPalettes.initialize(applicationContext)

        if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
            Route(this, WX::class.java)
        } else {
            val wfo = Location.wfo
            val state = Utility.getWfoSiteName(wfo).split(",")[0]
            val radarSite = Location.getRid(this, Location.currentLocationStr)
            Route.radar(this, arrayOf(radarSite, state))
        }

    }



    fun checkfiles(drawable: Int, filename: String) {
        UtilityLog.d("wx", "running files check on " + GlobalVariables.FilesPath)
        val dir = File(GlobalVariables.FilesPath)
        if (!dir.exists()) {
            UtilityLog.d("wx", "making dir")
            dir.mkdirs()
        }

        var file = File(GlobalVariables.FilesPath + filename)
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            UtilityLog.d("wx", filename + " does not exist.")
            var bitmap: Bitmap = BitmapFactory.decodeResource(resources, drawable)
            saveBitmapToFile(filename, bitmap)

        } else {
            UtilityLog.d("wx", filename + " are there!")
        }
    }

    fun saveBitmapToFile(fileName: String, bm: Bitmap) {
        val file = File(GlobalVariables.FilesPath, fileName)
        try {
            val out = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            UtilityLog.d("wx", fileName + " copied!")
        } catch (e: Exception) {
            UtilityLog.d("wx", "checkfiles Exception!")
            e.printStackTrace()
        }

    }


    //check colortable files and copy if any missing//
    fun checkpalfiles(resourceId: Int, filename: String) {
        UtilityLog.d("wx", "running files check on " + GlobalVariables.PalFilesPath)
        val dir = File(GlobalVariables.PalFilesPath)
        if (!dir.exists()) {
            UtilityLog.d("wx", "making dir")
            dir.mkdirs()
        }

        var file = File(GlobalVariables.PalFilesPath + filename)
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            UtilityLog.d("wx", filename + " does not exist.")
            saveRawToFile(filename, resourceId)
        } else {
            UtilityLog.d("wx", filename + " are there!")
        }
    }

    private fun saveRawToFile(fileName: String, resourceId: Int) {
        val dir = GlobalVariables.PalFilesPath
        var ins:InputStream = resources.openRawResource(resourceId)
        var content = ins.readBytes().toString(Charset.defaultCharset())
        File("$dir/$fileName").printWriter().use {
            it.println(content)
        }
    }



}
