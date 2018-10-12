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
import android.os.AsyncTask
import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class OPCImagesActivity : VideoRecordActivity(), View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private var bitmap = UtilityImg.getBlankBitmap()
    private var timePeriod = 1
    private var firstRun = false
    private var imageLoaded = false
    private var imgUrl = ""
    private lateinit var img: TouchImageView2
    private var title = "OPC"
    private lateinit var actionBack: MenuItem
    private lateinit var actionForward: MenuItem
    private var imgIdx = 0
    private lateinit var drw: ObjectNavDrawer
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer_bottom_toolbar, R.menu.opcimages, true, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        setTitle(title)
        title = Utility.readPref(this, "OPC_IMG_FAV_TITLE", UtilityOPCImages.LABELS[0])
        imgUrl = Utility.readPref(this, "OPC_IMG_FAV_URL", UtilityOPCImages.URL_INDEX[0])
        imgIdx = Utility.readPref(this, "OPC_IMG_FAV_IDX", 0)
        toolbar.subtitle = title
        val menu = toolbarBottom.menu
        actionBack = menu.findItem(R.id.action_back)
        actionForward = menu.findItem(R.id.action_forward)
        actionBack.isVisible = false
        actionForward.isVisible = false
        drw = ObjectNavDrawer(this, UtilityOPCImages.LABELS, UtilityOPCImages.URL_INDEX)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            title = drw.getLabel(position)
            imgUrl = drw.getToken(position)
            imgIdx = position
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        internal var getUrl = ""

        override fun onPreExecute() {
            toolbar.subtitle = title
            if (imgUrl.contains("http://graphical.weather.gov/images/conus/")) {
                getUrl = imgUrl + timePeriod.toString() + "_conus.png"
                actionBack.isVisible = true
                actionForward.isVisible = true
            } else {
                actionBack.isVisible = false
                actionForward.isVisible = false
                getUrl = imgUrl
            }
            Utility.writePref(contextg, "OPC_IMG_FAV_TITLE", title)
            Utility.writePref(contextg, "OPC_IMG_FAV_URL", imgUrl)
            Utility.writePref(contextg, "OPC_IMG_FAV_IDX", imgIdx)
        }

        override fun doInBackground(vararg params: String): String {
            bitmap = getUrl.getImage()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.setImageBitmap(bitmap)
            firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "OPCIMG")
            imageLoaded = true
        }
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
            R.id.action_forward -> {
                timePeriod += 1
                GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_back -> {
                timePeriod -= 1
                GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    UtilityShare.shareBitmap(this, "OPC", bitmap)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onClick(v: View) {
        when (v.id) {R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "OPCIMG")
        }
        super.onStop()
    }

    private fun showNextImg() {
        imgIdx += 1
        if (imgIdx == UtilityOPCImages.URL_INDEX.size) {
            imgIdx = 0
        }
        title = UtilityOPCImages.LABELS[imgIdx]
        imgUrl = UtilityOPCImages.URL_INDEX[imgIdx]
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun showPrevImg() {
        imgIdx -= 1
        if (imgIdx == -1) {
            imgIdx = UtilityOPCImages.URL_INDEX.size - 1
        }
        title = UtilityOPCImages.LABELS[imgIdx]
        imgUrl = UtilityOPCImages.URL_INDEX[imgIdx]
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}
