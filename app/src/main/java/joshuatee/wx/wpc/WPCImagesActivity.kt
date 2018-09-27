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

package joshuatee.wx.wpc

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class WPCImagesActivity : VideoRecordActivity(), View.OnClickListener, Toolbar.OnMenuItemClickListener {

    companion object {
        const val URL = ""
    }

    private var bitmap = UtilityImg.getBlankBitmap()
    private var timePeriod = 1
    private var firstRun = false
    private var imageLoaded = false
    private var imgUrl = ""
    private lateinit var img: TouchImageView2
    private var title = ""
    private lateinit var actionBack: MenuItem
    private lateinit var actionForward: MenuItem
    private var imgIdx = 0
    private var imgGroupIdx = 0
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpcimages, R.menu.wpcimages, true, true)
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
        title = Utility.readPref(this, "WPG_IMG_FAV_TITLE", UtilityWPCImages.LABELS[0])
        imgUrl = Utility.readPref(this, "WPG_IMG_FAV_URL", UtilityWPCImages.PARAMS[0])
        imgIdx = Utility.readPref(this, "WPG_IMG_IDX", 0)
        imgGroupIdx = Utility.readPref(this, "WPG_IMG_GROUPIDX", 0)
        setTitle(title)
        val menu = toolbarBottom.menu
        actionBack = menu.findItem(R.id.action_back)
        actionForward = menu.findItem(R.id.action_forward)
        actionBack.isVisible = false
        actionForward.isVisible = false
        UtilityWPCImages.createData()
        drw = ObjectNavDrawerCombo(this, UtilityWPCImages.GROUPS, UtilityWPCImages.LONG_CODES, UtilityWPCImages.SHORT_CODES)
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            imgUrl = drw.getToken(groupPosition, childPosition)
            title = drw.getLabel(groupPosition, childPosition)
            imgIdx = childPosition
            imgGroupIdx = groupPosition
            GetContent().execute()
            true
        }
        toolbar.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        toolbarBottom.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        selectItem(findPosn(imgUrl))
    }

    private fun findPosn(url: String) = (0 until UtilityWPCImages.PARAMS.size).firstOrNull { UtilityWPCImages.PARAMS[it] == url }
            ?: 0

    private fun selectItem(position: Int) {
        drw.listView.setItemChecked(position, false)
        drw.drawerLayout.closeDrawer(drw.listView)
        title = UtilityWPCImages.LABELS[position]
        imgUrl = UtilityWPCImages.PARAMS[position]
        GetContent().execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        internal var getUrl = ""

        override fun onPreExecute() {
            setTitle(title)
            when {
                imgUrl.contains("http://graphical.weather.gov/images/conus/") -> {
                    getUrl = imgUrl + timePeriod + "_conus.png"
                    actionBack.isVisible = true
                    actionForward.isVisible = true
                }
                imgUrl.contains("aviationweather") -> {
                    actionBack.isVisible = true
                    actionForward.isVisible = true
                    getUrl = imgUrl
                }
                else -> {
                    actionBack.isVisible = false
                    actionForward.isVisible = false
                    getUrl = imgUrl
                }
            }
            Utility.writePref(contextg, "WPG_IMG_FAV_TITLE", title)
            Utility.writePref(contextg, "WPG_IMG_FAV_URL", imgUrl)
            Utility.writePref(contextg, "WPG_IMG_IDX", imgIdx)
            Utility.writePref(contextg, "WPG_IMG_GROUPIDX", imgGroupIdx)
        }

        override fun doInBackground(vararg params: String): String {
            bitmap = getUrl.getImage()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.setImageBitmap(bitmap)
            if (!firstRun) {
                img.setZoom("WPCIMG")
                firstRun = true
            }
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
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        val numAviationImg = 14
        when (item.itemId) {
            R.id.action_forward -> {
                timePeriod += 1
                imgIdx += 1
                if (imgUrl.contains("aviationweather")) {
                    if (imgIdx >= numAviationImg) {
                        imgIdx = 0
                    }
                    imgUrl = UtilityWPCImages.SHORT_CODES[imgGroupIdx][imgIdx]
                    title = UtilityWPCImages.LONG_CODES[imgGroupIdx][imgIdx]
                }
                GetContent().execute()
            }
            R.id.action_back -> {
                timePeriod--
                imgIdx--
                if (imgUrl.contains("aviationweather")) {
                    if (imgIdx < 0) {
                        imgIdx = numAviationImg - 1
                    }
                    imgUrl = UtilityWPCImages.SHORT_CODES[imgGroupIdx][imgIdx]
                    title = UtilityWPCImages.LONG_CODES[imgGroupIdx][imgIdx]
                }
                GetContent().execute()
            }
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {

                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else
                    UtilityShare.shareText(this, title, "", bitmap)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onClick(v: View) {
        when (v.id) {R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "WPCIMG")
        }
        super.onStop()
    }

    private fun showNextImg() {
        imgIdx += 1
        if (UtilityWPCImages.SHORT_CODES[imgGroupIdx][imgIdx] == "") {
            imgIdx = 0
        }
        imgUrl = UtilityWPCImages.SHORT_CODES[imgGroupIdx][imgIdx]
        title = UtilityWPCImages.LONG_CODES[imgGroupIdx][imgIdx]
        GetContent().execute()
    }

    private fun showPrevImg() {
        imgIdx -= 1
        if (imgIdx == -1) {
            for (j in 0 until UtilityWPCImages.SHORT_CODES[imgGroupIdx].size) {
                if (UtilityWPCImages.SHORT_CODES[imgGroupIdx][j] == "") {
                    imgIdx = j - 1
                    break
                }
            }
        }
        imgUrl = UtilityWPCImages.SHORT_CODES[imgGroupIdx][imgIdx]
        title = UtilityWPCImages.LONG_CODES[imgGroupIdx][imgIdx]
        GetContent().execute()
    }
}
