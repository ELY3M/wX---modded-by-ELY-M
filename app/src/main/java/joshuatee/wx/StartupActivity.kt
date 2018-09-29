package joshuatee.wx

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
import java.io.IOException


class StartupActivity : Activity() {

    // This activity is the first activity started when the app starts.
    // It's job is to initialize preferences if not done previously,
    // display the splash screen, start the service that handles notifications,
    // and display the version in the title.
    //

    var TAG = "joshuatee-StartupActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkfiles()
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
        finish()
    }


    fun checkfiles() {
        Log.i(TAG, "running files check on "+MyApplication.FilesPath)
        val dir = File(MyApplication.FilesPath)
        if (!dir.exists()) {
            Log.i(TAG, "making dir")
            dir.mkdirs()
        }

        var file = File(MyApplication.FilesPath+"location.png")
        var fileExists = file.exists()
        if(!fileExists)
        {
            //need to copy files!
            Log.i(TAG, "files does not exist.")
            var location: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.location)
            saveBitmapToFile("location.png", location)

        } else {
            Log.i(TAG, "all files are there!")
        }
    }


    fun saveBitmapToFile(fileName: String, bm: Bitmap) {
        val file = File(MyApplication.FilesPath, "location.png")
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
