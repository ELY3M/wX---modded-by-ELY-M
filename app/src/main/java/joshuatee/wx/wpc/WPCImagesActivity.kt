/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
import android.os.Bundle
import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import android.view.View
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class WpcImagesActivity : VideoRecordActivity(), View.OnClickListener {

    companion object { const val URL = "" }

    private var bitmap = UtilityImg.getBlankBitmap()
    private var timePeriod = 1
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var activityArguments: Array<String>
    private lateinit var img: TouchImageView2
    private var calledFromHomeScreen = false
    private var homeScreenId = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.wpcimages, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val actionBack = menu.findItem(R.id.action_back)
        val actionForward = menu.findItem(R.id.action_forward)
        actionBack!!.isVisible = false
        actionForward!!.isVisible = false
        if (drw.getUrl().contains("https://graphical.weather.gov/images/conus/")) {
            actionBack.isVisible = true
            actionForward.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpcimages, R.menu.wpcimages, iconsEvenlySpaced = true, bottomToolbar = false)
        img = findViewById(R.id.img)
        img.setOnClickListener(this)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        activityArguments = intent.getStringArrayExtra(URL)!!
        activityArguments.let {
            if (activityArguments.size > 1 && activityArguments[0] == "HS") {
                homeScreenId = activityArguments[1]
                calledFromHomeScreen = true
            }
        }
        UtilityWpcImages.create()
        drw = ObjectNavDrawerCombo(this, UtilityWpcImages.groups, UtilityWpcImages.longCodes, UtilityWpcImages.shortCodes, this, "WPG_IMG")
        drw.setListener { getContent() }
        toolbar.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (!calledFromHomeScreen) {
            title = "Images"
            toolbar.subtitle = drw.getLabel()
        } else {
            title = "Images"
            toolbar.subtitle = GlobalArrays.nwsImageProducts.findLast { it.startsWith("$homeScreenId:") }!!.split(":")[1]
        }
        FutureVoid(this, ::download, ::update)
    }

    private fun download() {
        if (!calledFromHomeScreen) {
            val getUrl = when {
                drw.getUrl().contains("https://graphical.weather.gov/images/conus/") -> drw.getUrl() + timePeriod + "_conus.png"
                drw.getUrl().contains("aviationweather") -> drw.getUrl()
                else -> drw.getUrl()
            }
            Utility.writePref(this@WpcImagesActivity, "WPG_IMG_FAV_URL", drw.getUrl())
            Utility.writePref(this@WpcImagesActivity, "WPG_IMG_IDX", drw.imgIdx)
            Utility.writePref(this@WpcImagesActivity, "WPG_IMG_GROUPIDX", drw.imgGroupIdx)
            bitmap = getUrl.getImage()
        } else {
            bitmap = UtilityDownload.getImageProduct(this@WpcImagesActivity, homeScreenId)
            calledFromHomeScreen = false
        }
    }

    private fun update() {
        img.setImageBitmap(bitmap)
        if (!firstRun && activityArguments.size < 2) {
            img.setZoom("WPCIMG")
            firstRun = true
        }
        imageLoaded = true
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_forward -> {
                timePeriod += 1
                getContent()
            }
            R.id.action_back -> {
                timePeriod -= 1
                getContent()
            }
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else
                    UtilityShare.bitmap(this, this, drw.getLabel(), bitmap)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded && activityArguments.size < 2) {
            UtilityImg.imgSavePosnZoom(this, img, "WPCIMG")
        }
        super.onStop()
    }

    private fun showNextImg() {
        drw.imgIdx += 1
        if (UtilityWpcImages.shortCodes[drw.imgGroupIdx][drw.imgIdx] == "") {
            drw.imgIdx = 0
        }
        getContent()
    }

    private fun showPrevImg() {
        drw.imgIdx -= 1
        if (drw.imgIdx == -1) {
            for (j in UtilityWpcImages.shortCodes[drw.imgGroupIdx].indices) {
                if (UtilityWpcImages.shortCodes[drw.imgGroupIdx][j] == "") {
                    drw.imgIdx = j - 1
                    break
                }
            }
        }
        getContent()
    }
}
