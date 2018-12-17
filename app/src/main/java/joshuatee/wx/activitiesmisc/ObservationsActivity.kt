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
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class ObservationsActivity : VideoRecordActivity(), OnMenuItemClickListener {

    companion object {
        const val LOC: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var img: TouchImageView2
    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstRun = false
    private var imageLoaded = false
    //private var imgUrl = UtilityObservations.urls[0]
    //private val prefToken = "SFC_OBS_IMG"
    private val prefTokenIdx = "SFC_OBS_IMG_IDX"
    private var imgIdx = 0
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_image_show_bottom_toolbar,
            R.menu.observations,
            true,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        img = findViewById(R.id.iv)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        //imgUrl = Utility.readPref(this, prefToken, imgUrl)
        imgIdx = Utility.readPref(this, prefTokenIdx, 0)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        title = "Observations"
        bitmap = withContext(Dispatchers.IO) { UtilityObservations.urls[imgIdx].getImage() }
        if (UtilityObservations.urls[imgIdx].contains("large_latestsfc.gif")) {
            img.setMaxZoom(16f)
        } else {
            img.setMaxZoom(4f)
        }
        img.setImageBitmap(bitmap)
        img.resetZoom()
        firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "OBS")
        imageLoaded = true
        //Utility.writePref(contextg, prefToken, imgUrl)
        Utility.writePref(contextg, prefTokenIdx, imgIdx)
        toolbar.subtitle = UtilityObservations.labels[imgIdx]
    }

    private fun getContent(idx: Int) {
        //imgUrl = UtilityObservations.urls[idx]
        imgIdx = idx
        getContent()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
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
            R.id.action_conus -> getContent(0)
            R.id.action_sw -> getContent(1)
            R.id.action_sc -> getContent(2)
            R.id.action_se -> getContent(3)
            R.id.action_cw -> getContent(4)
            R.id.action_c -> getContent(5)
            R.id.action_ce -> getContent(6)
            R.id.action_nw -> getContent(7)
            R.id.action_nc -> getContent(8)
            R.id.action_ne -> getContent(9)
            R.id.action_ak -> getContent(10)
            R.id.action_gulf_ak -> getContent(11)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "OBS")
        }
        super.onStop()
    }

    private fun showNextImg() {
        imgIdx += 1
        if (imgIdx == UtilityObservations.urls.size) {
            imgIdx = 0
        }
        //imgUrl = UtilityObservations.urls[imgIdx]
        getContent()
    }

    private fun showPrevImg() {
        imgIdx -= 1
        if (imgIdx == -1) {
            imgIdx = UtilityObservations.urls.size - 1
        }
        //imgUrl = UtilityObservations.urls[imgIdx]
        getContent()
    }
}




