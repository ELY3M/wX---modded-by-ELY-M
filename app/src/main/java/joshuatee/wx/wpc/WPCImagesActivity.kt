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

import android.os.Bundle
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import joshuatee.wx.getImage
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes2
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.NavDrawerCombo
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.DownloadImage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

class WpcImagesActivity : VideoRecordActivity(), View.OnClickListener {

    companion object {
        const val URL = ""
    }

    private var timePeriod = 1
    private lateinit var navDrawerCombo: NavDrawerCombo
    private lateinit var touchImage: TouchImage
    private var calledFromHomeScreen = false
    private var homeScreenId = ""
    private val prefImagePosition = "WPCIMG"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.wpcimages, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val actionBack = menu.findItem(R.id.action_back)
        val actionForward = menu.findItem(R.id.action_forward)
        actionBack!!.isVisible = false
        actionForward!!.isVisible = false
        if (navDrawerCombo.getUrl().contains("https://graphical.weather.gov/images/conus/")) {
            actionBack.isVisible = true
            actionForward.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpcimages, R.menu.wpcimages, iconsEvenlySpaced = true, bottomToolbar = false)
        val arguments = intent.getStringArrayExtra(URL)!!
        arguments.let {
            if (arguments.size > 1 && arguments[0] == "HS") {
                homeScreenId = arguments[1]
                calledFromHomeScreen = true
            }
        }
        setupUI()
        getContent()
    }

    private fun setupUI() {
        touchImage = TouchImage(this, R.id.img)
        touchImage.connectClick(this)
        touchImage.connect2(::showNextImg, ::showPrevImg)
        UtilityWpcImages.create()
        navDrawerCombo = NavDrawerCombo(this, UtilityWpcImages.groups, UtilityWpcImages.longCodes, UtilityWpcImages.shortCodes, "WPG_IMG")
        navDrawerCombo.connect(::getContent)
        objectToolbar.connectClick { navDrawerCombo.open() }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (!calledFromHomeScreen) {
            setTitle("Images", navDrawerCombo.getLabel())
        } else {
            val subtitle = GlobalArrays.nwsImageProducts.findLast { it.startsWith("$homeScreenId:") }!!.split(":")[1]
            setTitle("Images", subtitle)
        }
        FutureBytes2(::download, ::update)
    }

    private fun download(): Bitmap {
        return if (!calledFromHomeScreen) {
            val getUrl = when {
                navDrawerCombo.getUrl().contains("https://graphical.weather.gov/images/conus/") -> navDrawerCombo.getUrl() + timePeriod + "_conus.png"
                navDrawerCombo.getUrl().contains("aviationweather") -> navDrawerCombo.getUrl()
                else -> navDrawerCombo.getUrl()
            }
            Utility.writePref(this, "WPG_IMG_FAV_URL", navDrawerCombo.getUrl())
            Utility.writePrefInt(this, "WPG_IMG_IDX", navDrawerCombo.imgIdx)
            Utility.writePrefInt(this, "WPG_IMG_GROUPIDX", navDrawerCombo.imgGroupIdx)
            getUrl.getImage()
        } else {
            calledFromHomeScreen = false
            DownloadImage.byProduct(this, homeScreenId)
        }
    }

    private fun update(bitmap: Bitmap) {
        touchImage.set(bitmap)
        touchImage.firstRun(prefImagePosition)
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawerCombo.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawerCombo.onOptionsItemSelected(item)) {
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
                if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                    checkOverlayPerms()
                } else
                    UtilityShare.bitmap(this, navDrawerCombo.getLabel(), touchImage)
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
        touchImage.imgSavePosnZoom(prefImagePosition)
        super.onStop()
    }

    private fun showNextImg() {
        navDrawerCombo.imgIdx += 1
        if (UtilityWpcImages.shortCodes[navDrawerCombo.imgGroupIdx][navDrawerCombo.imgIdx] == "") {
            navDrawerCombo.imgIdx = 0
        }
        getContent()
    }

    private fun showPrevImg() {
        navDrawerCombo.imgIdx -= 1
        if (navDrawerCombo.imgIdx == -1) {
            for (j in UtilityWpcImages.shortCodes[navDrawerCombo.imgGroupIdx].indices) {
                if (UtilityWpcImages.shortCodes[navDrawerCombo.imgGroupIdx][j] == "") {
                    navDrawerCombo.imgIdx = j - 1
                    break
                }
            }
        }
        getContent()
    }
}
