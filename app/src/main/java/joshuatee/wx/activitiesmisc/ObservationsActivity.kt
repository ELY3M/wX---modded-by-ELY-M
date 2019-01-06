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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class ObservationsActivity : VideoRecordActivity(), Toolbar.OnMenuItemClickListener {

    companion object {
        const val LOC: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var img: ObjectTouchImageView
    private var bitmap = UtilityImg.getBlankBitmap()
    private val prefTokenIdx = "SFC_OBS_IMG_IDX"
    private lateinit var contextg: Context
    private lateinit var drw: ObjectNavDrawer
    private val prefImagePosition = "OBS"

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
        title = "Observations"
        toolbarBottom.setOnMenuItemClickListener(this)
        drw = ObjectNavDrawer(this, UtilityObservations.labels, UtilityObservations.urls)
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, drw, prefTokenIdx)
        img.setListener(this, drw, ::getContentFixThis)
        drw.index = Utility.readPref(this, prefTokenIdx, 0)
        drw.setListener(::getContentFixThis)
        getContent()
    }

    private fun getContentFixThis() {
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        toolbar.subtitle = drw.getLabel()
        bitmap = withContext(Dispatchers.IO) { drw.getUrl().getImage() }
        if (drw.getUrl().contains("large_latestsfc.gif")) {
            img.setMaxZoom(16f)
        } else {
            img.setMaxZoom(4f)
        }
        img.setBitmap(bitmap)
        img.resetZoom()
        img.firstRunSetZoomPosn(prefImagePosition)
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

    override fun onStop() {
        img.imgSavePosnZoom(this, prefImagePosition)
        super.onStop()
    }
}




