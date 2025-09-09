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
import android.util.Log
import android.widget.Toast
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.SpotterNetworkPositionReportService
import joshuatee.wx.radarcolorpalettes.ColorPalettes
import joshuatee.wx.settings.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.nio.charset.Charset
import kotlin.system.exitProcess

class StartupActivity : Activity() {

    //
    // This activity is the first activity started when the app starts.
    // It's job is to start the background service that handles notifications
    // and start the initial activity
    //
    // This occurs after MyApplication.onCreate is run (Application class)
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UtilityWXJobService.startService(this)
        Route(this, WX::class.java)
	//elys mod
        askPerms()
        finish()
    }
    
    //elys mod
    private fun askPerms() {
        PermissionRequester.initialize(applicationContext)
        val requester = PermissionRequester.instance()

        if (SDK_INT >= 34){
            CoroutineScope(Dispatchers.Main).launch {
                requester.request(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION
                ).collect { p ->
                    when (p) {
                        is PermissionResult.Granted -> {
                            askExternalStorageManager()
                        }
                        else -> {}
                    }

                }
            }
        }

        if(SDK_INT >= 30) {
            CoroutineScope(Dispatchers.Main).launch {
                requester.request(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE,
                ).collect { p ->
                    when (p) {
                        is PermissionResult.Granted -> {
                            askExternalStorageManager()
                        }
                        else -> {}
                    }

                }
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                requester.request(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).collect { p ->
                    when (p) {
                        is PermissionResult.Granted -> {
                            runme()
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    fun askExternalStorageManager() {
        if(SDK_INT >= 30) {
            if (Environment.isExternalStorageManager()) {
                Log.i("wx-elys", "ExternalStorageManager Perms are already granted :)")
                runme()
            } else {
                Toast.makeText(applicationContext, "This app need access to your phone memory or SD Card to make files and write files (/wX/ on your phone memory or sd card)\nThe all file access settings will open. Make sure to toggle it on to enable all files access for this app to function fully.\n You need to restart the app after you enabled the all files access for this app in the settings.\n", Toast.LENGTH_LONG).show()
                val permissionIntent = Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(permissionIntent)
                Thread.sleep(13000) //sleep for 13 secs and force restart
                exitProcess(0)
            }
        }
    }

    fun runme() {
        UtilityLog.d("wx-elys", "runme()")
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

        //start service for Spotter Network Location Auto Reporting
        if (RadarPreferences.sn_locationreport) {
            Log.d("wx-elys", "SN Auto Location Report started")
            applicationContext.startService(Intent(applicationContext, SpotterNetworkPositionReportService::class.java))
        }

    }


    fun checkfiles(drawable: Int, filename: String) {
        Log.d("wx-elys", "running files check on " + GlobalVariables.FilesPath)
        try {
            val dir = File(GlobalVariables.FilesPath)
            if (!dir.exists()) {
                Log.d("wx-elys", "making dir")
                dir.mkdirs()
            }

            var file = File(GlobalVariables.FilesPath + filename)
            var fileExists = file.exists()
            if (!fileExists) {
                //need to copy files!
                Log.d("wx-elys", filename + " does not exist.")
                var bitmap: Bitmap = BitmapFactory.decodeResource(resources, drawable)
                saveBitmapToFile(filename, bitmap)

            } else {
                Log.d("wx-elys", filename + " are there!")
            }
        } catch (e: Exception) {
            Log.d("wx-elys", "checkfiles - caught Exception!")
            e.printStackTrace()
        }
    }

    fun saveBitmapToFile(fileName: String, bm: Bitmap) {
        try {
            val file = File(GlobalVariables.FilesPath, fileName)
            val out = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Log.d("wx-elys", fileName + " copied!")
        } catch (e: Exception) {
            Log.d("wx-elys", "saveBitmapToFile - caught Exception!")
            e.printStackTrace()
        }

    }


    //check colortable files and copy if any missing//
    fun checkpalfiles(resourceId: Int, filename: String) {
        Log.d("wx-elys", "running files check on " + GlobalVariables.PalFilesPath)
        try {
            val dir = File(GlobalVariables.PalFilesPath)
            if (!dir.exists()) {
                Log.d("wx-elys", "making dir")
                dir.mkdirs()
            }

            var file = File(GlobalVariables.PalFilesPath + filename)
            var fileExists = file.exists()
            if (!fileExists) {
                //need to copy files!
                Log.d("wx-elys", filename + " does not exist.")
                saveRawToFile(filename, resourceId)
            } else {
                Log.d("wx-elys", filename + " are there!")
            }
        } catch (e: Exception) {
            Log.d("wx-elys", "checkpalfiles - caught Exception!")
            e.printStackTrace()
        }
    }

    private fun saveRawToFile(fileName: String, resourceId: Int) {
        try {
            val dir = GlobalVariables.PalFilesPath
            var ins: InputStream = resources.openRawResource(resourceId)
            var content = ins.readBytes().toString(Charset.defaultCharset())
            File("$dir/$fileName").printWriter().use {
                it.println(content)
            }
        } catch (e: Exception) {
            Log.d("wx-elys", "saveRawToFile - caught Exception!")
            e.printStackTrace()
        }
    }



}
