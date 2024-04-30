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

package joshuatee.wx.radar

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.telecine.TelecineService
import joshuatee.wx.ui.ObjectToolbar
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.UtilityLog

abstract class VideoRecordActivity : AppCompatActivity() {

//    companion object {
//        /** code to post/handler request for permission  */
//        private const val REQUEST_CODE_PERM = 999
//    }

    lateinit var toolbar: Toolbar
    lateinit var toolbarBottom: Toolbar
    lateinit var objectToolbar: ObjectToolbar
    lateinit var objectToolbarBottom: ObjectToolbar
    private lateinit var telecineService: TelecineService

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
        checkDrawOverlayPermission()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_CODE_PERM) {
//            if (Settings.canDrawOverlays(this)) {
//                telecineService = TelecineService()
//                telecineService.start(this)
//            }
//        }
//    }

    private fun checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            UtilityLog.d("wx", "checkDrawOverlayPermission - perm check")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            startActivityForResult(intent, REQUEST_CODE_PERM)
            startForResult.launch(intent)
        } else {
            telecineService = TelecineService()
            telecineService.start(this)
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _: ActivityResult ->
        if (Settings.canDrawOverlays(this)) {
            telecineService = TelecineService()
            telecineService.start(this)
        }

//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            if (Settings.canDrawOverlays(this)) {
//                telecineService = TelecineService()
//                telecineService.start(this)
//            }
//        }
    }

    // https://developer.android.com/training/permissions/requesting.html
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkDrawOverlayPermission()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
