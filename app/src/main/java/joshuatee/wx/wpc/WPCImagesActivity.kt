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
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class WpcImagesActivity : VideoRecordActivity(), View.OnClickListener {

    companion object { const val URL = "" }

    private var bitmap = UtilityImg.getBlankBitmap()
    private var timePeriod = 1
    private lateinit var objectNavDrawerCombo: ObjectNavDrawerCombo
    private lateinit var arguments: Array<String>
    private lateinit var image: TouchImage
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
        if (objectNavDrawerCombo.getUrl().contains("https://graphical.weather.gov/images/conus/")) {
            actionBack.isVisible = true
            actionForward.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpcimages, R.menu.wpcimages, iconsEvenlySpaced = true, bottomToolbar = false)
        image = TouchImage(this, R.id.img)
        image.setOnClickListener(this)
        image.setListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (image.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (image.currentZoom < 1.01f) showPrevImg()
            }
        })
        arguments = intent.getStringArrayExtra(URL)!!
        arguments.let {
            if (arguments.size > 1 && arguments[0] == "HS") {
                homeScreenId = arguments[1]
                calledFromHomeScreen = true
            }
        }
        UtilityWpcImages.create()
        objectNavDrawerCombo = ObjectNavDrawerCombo(this, UtilityWpcImages.groups, UtilityWpcImages.longCodes, UtilityWpcImages.shortCodes, "WPG_IMG")
        objectNavDrawerCombo.setListener { getContent() }
        toolbar.setOnClickListener { objectNavDrawerCombo.open() }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (!calledFromHomeScreen) {
            title = "Images"
            toolbar.subtitle = objectNavDrawerCombo.getLabel()
        } else {
            title = "Images"
            toolbar.subtitle = GlobalArrays.nwsImageProducts.findLast { it.startsWith("$homeScreenId:") }!!.split(":")[1]
        }
        FutureVoid(this, ::download, ::update)
    }

    private fun download() {
        if (!calledFromHomeScreen) {
            val getUrl = when {
                objectNavDrawerCombo.getUrl().contains("https://graphical.weather.gov/images/conus/") -> objectNavDrawerCombo.getUrl() + timePeriod + "_conus.png"
                objectNavDrawerCombo.getUrl().contains("aviationweather") -> objectNavDrawerCombo.getUrl()
                else -> objectNavDrawerCombo.getUrl()
            }
            Utility.writePref(this, "WPG_IMG_FAV_URL", objectNavDrawerCombo.getUrl())
            Utility.writePref(this, "WPG_IMG_IDX", objectNavDrawerCombo.imgIdx)
            Utility.writePref(this, "WPG_IMG_GROUPIDX", objectNavDrawerCombo.imgGroupIdx)
            bitmap = getUrl.getImage()
        } else {
            bitmap = UtilityDownload.getImageProduct(this, homeScreenId)
            calledFromHomeScreen = false
        }
    }

    private fun update() {
        image.setBitmap(bitmap)
        image.firstRunSetZoomPosn(prefImagePosition)
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawerCombo.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.onOptionsItemSelected(item)) {
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
                    UtilityShare.bitmap(this, objectNavDrawerCombo.getLabel(), bitmap)
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
        image.imgSavePosnZoom(prefImagePosition)
        super.onStop()
    }

    private fun showNextImg() {
        objectNavDrawerCombo.imgIdx += 1
        if (UtilityWpcImages.shortCodes[objectNavDrawerCombo.imgGroupIdx][objectNavDrawerCombo.imgIdx] == "") {
            objectNavDrawerCombo.imgIdx = 0
        }
        getContent()
    }

    private fun showPrevImg() {
        objectNavDrawerCombo.imgIdx -= 1
        if (objectNavDrawerCombo.imgIdx == -1) {
            for (j in UtilityWpcImages.shortCodes[objectNavDrawerCombo.imgGroupIdx].indices) {
                if (UtilityWpcImages.shortCodes[objectNavDrawerCombo.imgGroupIdx][j] == "") {
                    objectNavDrawerCombo.imgIdx = j - 1
                    break
                }
            }
        }
        getContent()
    }
}
