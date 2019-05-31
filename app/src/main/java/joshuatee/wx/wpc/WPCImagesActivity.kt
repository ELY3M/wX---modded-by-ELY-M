/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_wpcimages.*

class WpcImagesActivity : VideoRecordActivity(), View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmap = UtilityImg.getBlankBitmap()
    private var timePeriod = 1
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var actionBack: MenuItem
    private lateinit var actionForward: MenuItem
    private lateinit var drw: ObjectNavDrawerCombo

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_wpcimages,
                R.menu.wpcimages,
                iconsEvenlySpaced = true,
                bottomToolbar = true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        img.setOnClickListener(this)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        val menu = toolbarBottom.menu
        actionBack = menu.findItem(R.id.action_back)
        actionForward = menu.findItem(R.id.action_forward)
        actionBack.isVisible = false
        actionForward.isVisible = false
        UtilityWpcImages.createData()
        drw = ObjectNavDrawerCombo(
                this,
                UtilityWpcImages.groups,
                UtilityWpcImages.longCodes,
                UtilityWpcImages.shortCodes,
                this,
                "WPG_IMG"
        )
        drw.setListener(::getContentFixThis)
        toolbar.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        toolbarBottom.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        getContent()
    }

    private fun getContentFixThis() {
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        val getUrl: String
        title = drw.getLabel()
        when {
            drw.getUrl().contains("http://graphical.weather.gov/images/conus/") -> {
                getUrl = drw.getUrl() + timePeriod + "_conus.png"
                actionBack.isVisible = true
                actionForward.isVisible = true
            }
            drw.getUrl().contains("aviationweather") -> {
                actionBack.isVisible = true
                actionForward.isVisible = true
                getUrl = drw.getUrl()
            }
            else -> {
                actionBack.isVisible = false
                actionForward.isVisible = false
                getUrl = drw.getUrl()
            }
        }
        Utility.writePref(this@WpcImagesActivity, "WPG_IMG_FAV_URL", drw.getUrl())
        Utility.writePref(this@WpcImagesActivity, "WPG_IMG_IDX", drw.imgIdx)
        Utility.writePref(this@WpcImagesActivity, "WPG_IMG_GROUPIDX", drw.imgGroupIdx)
        bitmap = withContext(Dispatchers.IO) { getUrl.getImage() }
        img.setImageBitmap(bitmap)
        if (!firstRun) {
            img.setZoom("WPCIMG")
            firstRun = true
        }
        imageLoaded = true
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
                drw.imgIdx += 1
                if (drw.getUrl().contains("aviationweather")) {
                    if (drw.imgIdx >= numAviationImg) {
                        drw.imgIdx = 0
                    }
                }
                getContent()
            }
            R.id.action_back -> {
                timePeriod--
                drw.imgIdx--
                if (drw.getUrl().contains("aviationweather")) {
                    if (drw.imgIdx < 0) {
                        drw.imgIdx = numAviationImg - 1
                    }
                }
                getContent()
            }
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else
                    UtilityShare.shareText(this, drw.getLabel(), "", bitmap)
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
            for (j in 0 until UtilityWpcImages.shortCodes[drw.imgGroupIdx].size) {
                if (UtilityWpcImages.shortCodes[drw.imgGroupIdx][j] == "") {
                    drw.imgIdx = j - 1
                    break
                }
            }
        }
        getContent()
    }
}
