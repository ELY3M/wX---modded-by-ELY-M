/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */
 //Modded by ELY M.  

package joshuatee.wx.radar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.telecine.TelecineService
import joshuatee.wx.ui.ObjectToolbar
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.UtilityLog

abstract class VideoRecordActivity : AppCompatActivity() {

    companion object {
        private const val CREATE_SCREEN_CAPTURE = 4242

        /** code to post/handler request for permission  */
        private const val REQUEST_CODE_PERM = 999
    }

    protected var showDistanceTool = "false"
    lateinit var toolbar: Toolbar
    lateinit var toolbarBottom: Toolbar
    lateinit var objectToolbar: ObjectToolbar
    lateinit var objectToolbarBottom: ObjectToolbar

    protected fun onCreate(savedInstanceState: Bundle?, layoutResId: Int, menuResId: Int?, bottomToolbar: Boolean) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBottom = if (bottomToolbar) {
            findViewById(R.id.toolbar_bottom)
        } else {
            Toolbar(this)
        }
        objectToolbar = ObjectToolbar(toolbar)
        objectToolbarBottom = ObjectToolbar(toolbarBottom)
        // for model activities need to force false regardless of user setting
        if (menuResId != null && bottomToolbar) {
            toolbarBottom.inflateMenu(menuResId)
            toolbar.setOnClickListener { toolbarBottom.showOverflowMenu() }
            toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        }
        UtilityToolbar.setElevation(toolbar)
    }

    fun setTitle(s: String) {
        title = s
    }

    fun setTitle(s: String, sub: String) {
        title = s
        toolbar.subtitle = sub
    }

    protected fun checkOverlayPerms() {
        UtilityLog.d("wx", "checkOverlayPerms start")
        if (isStoragePermissionGranted) {
            UtilityLog.d("wx", "checkOverlayPerms start, storage granted")
            checkDrawOverlayPermission()
        } else {
            UtilityLog.d("wx", "checkOverlayPerms start, storage not granted")
        }
    }

    private fun fireScreenCaptureIntent() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = manager.createScreenCaptureIntent()
        startActivityForResult(intent, CREATE_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PERM) {
            if (Settings.canDrawOverlays(this)) {
                fireScreenCaptureIntent()
            }
        }
        if (requestCode == CREATE_SCREEN_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT > 31) { // was 32, Android 12L is showing in crash reports
                val intent = TelecineService.newIntent(this, 1, Intent())
                intent.putExtra("show_distance_tool", showDistanceTool)
                intent.putExtra("show_recording_tools", "false")
                startService(intent)
            } else {
                val intent = TelecineService.newIntent(applicationContext, resultCode, data)
                intent.putExtra("show_distance_tool", showDistanceTool)
                intent.putExtra("show_recording_tools", "true")
                startService(intent)
            }
            // draw tools only?
//            val intent = TelecineService.newIntent(this, 1, Intent())
//            intent.putExtra("show_distance_tool", showDistanceTool)
//            intent.putExtra("show_recording_tools", "false")
//            startService(intent)
        }
    }

    private fun checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps  */
        if (!Settings.canDrawOverlays(this)) {
            UtilityLog.d("wx", "checkDrawOverlayPermission - perm check")
            /** if not construct intent to request permission  */
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            /** request permission via start activity for result  */
            startActivityForResult(intent, REQUEST_CODE_PERM)
        } else {
            UtilityLog.d("wx", "checkDrawOverlayPermission - fireScreenCaptureIntent")
            fireScreenCaptureIntent()
        }
    }

    private val isStoragePermissionGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= 33) {
                return true
            }
            return if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        }

//    private val isStoragePermissionGranted: Boolean
//        get() {
//            return if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                true
//            } else {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
//                false
//            }
//        }

    // https://developer.android.com/training/permissions/requesting.html

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkDrawOverlayPermission()
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                }
            }
        }// other 'case' lines to check for other
    }
}
