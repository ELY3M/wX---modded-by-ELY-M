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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class ObservationsActivity : VideoRecordActivity(), View.OnClickListener,
    Toolbar.OnMenuItemClickListener {

    companion object {
        const val LOC: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var img: TouchImageView2
    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstRun = false
    private var imageLoaded = false
    private val prefTokenIdx = "SFC_OBS_IMG_IDX"
    private lateinit var contextg: Context
    private lateinit var drw: ObjectNavDrawer

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_image_show_navdrawer_bottom_toolbar,
            R.menu.observations,
            true,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        img = findViewById(R.id.iv)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) UtilityImg.showNextImg(drw, ::getContentFixThis)
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) UtilityImg.showPrevImg(drw, ::getContentFixThis)
            }
        })
        drw = ObjectNavDrawer(this, UtilityObservations.labels, UtilityObservations.urls)
        drw.index = Utility.readPref(this, prefTokenIdx, 0)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            drw.index = position
            getContent()
        }
        getContent()
    }

    private fun getContentFixThis() {
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        title = "Observations"
        toolbar.subtitle = drw.getLabel()
        bitmap = withContext(Dispatchers.IO) { drw.getUrl().getImage() }
        if (drw.getUrl().contains("large_latestsfc.gif")) {
            img.setMaxZoom(16f)
        } else {
            img.setMaxZoom(4f)
        }
        img.setImageBitmap(bitmap)
        img.resetZoom()
        firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "OBS")
        imageLoaded = true
        Utility.writePref(contextg, prefTokenIdx, drw.index)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    UtilityShare.shareBitmap(this, "observations", bitmap)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "OBS")
        }
        super.onStop()
    }
}




