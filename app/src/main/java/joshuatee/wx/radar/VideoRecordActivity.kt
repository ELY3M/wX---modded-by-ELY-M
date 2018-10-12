/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import android.Manifest
import android.annotation.TargetApi
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

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.telecine.TelecineService
import joshuatee.wx.ui.UtilityToolbar

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
abstract class VideoRecordActivity : AppCompatActivity() {

    companion object {
        private const val CREATE_SCREEN_CAPTURE = 4242
        /** code to post/handler request for permission  */
        private const val REQUEST_CODE_PERM = 999
    }

    protected var showDistanceTool: String = "false"
    protected lateinit var toolbar: Toolbar
    protected lateinit var toolbarBottom: Toolbar

    protected fun onCreate(savedInstanceState: Bundle?, layoutResId: Int, menuResId: Int?, iconsEvenlySpaced: Boolean, bottomToolbar: Boolean) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        toolbar = findViewById(R.id.toolbar_top)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbarBottom = if (bottomToolbar) {
            findViewById(R.id.toolbar_bottom)
        } else {
            Toolbar(this as Context)
        }
        // for model activities need to force false regardless of user setting
        if (menuResId != null && bottomToolbar) {
            if (MyApplication.iconsEvenSpaced && iconsEvenlySpaced) {
                UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, menuResId)
            } else {
                toolbarBottom.inflateMenu(menuResId)
            }
            toolbar.setOnClickListener { toolbarBottom.showOverflowMenu() }
            toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        }
        UtilityToolbar.fullScreenMode(toolbar, false)
    }


    protected fun fireScreenCaptureIntent() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (android.os.Build.VERSION.SDK_INT > 20) {
            val intent = manager.createScreenCaptureIntent()
            startActivityForResult(intent, CREATE_SCREEN_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PERM) {
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
                fireScreenCaptureIntent()
            }
        }
        if (requestCode == CREATE_SCREEN_CAPTURE && resultCode == Activity.RESULT_OK) {
            //val ti = TelecineService.newIntent(this, resultCode, data)
            val ti = TelecineService.newIntent(applicationContext, resultCode, data)
            ti.putExtra("show_distance_tool", showDistanceTool)
            ti.putExtra("show_recording_tools", "true")
            startService(ti)
        }
    }

    protected fun checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps  */
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission  */
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            /** request permission via start activity for result  */
            startActivityForResult(intent, REQUEST_CODE_PERM)
            //fireScreenCaptureIntent()

        } else {
            fireScreenCaptureIntent()
        }
    }

    protected //permission is automatically granted on sdk<23 upon installation
    val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    true
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    false
                }
            } else {
                true
            }
        }

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
