/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectTouchImageView
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

import kotlinx.coroutines.*

class LightningActivity : VideoRecordActivity(), Toolbar.OnMenuItemClickListener {

    //
    // Used to view lighting data
    //
    // Arguments
    // 1: URL TODO what is this arg for?
    //

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmap = UtilityImg.getBlankBitmap()
    private var period = "0.25"
    private var periodPretty = "15 MIN"
    private lateinit var img: ObjectTouchImageView
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private val prefTokenIdx = "LIGHTNING_SECTOR_IDX"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer_bottom_toolbar, R.menu.lightning_activity, iconsEvenlySpaced = true, bottomToolbar = true)
        toolbarBottom.setOnMenuItemClickListener(this)
        toolbar.setOnClickListener { toolbar.showOverflowMenu() }
        objectNavDrawer = ObjectNavDrawer(this, UtilityLightning.labels, UtilityLightning.urls)
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, objectNavDrawer, prefTokenIdx)
        objectNavDrawer.index = Utility.readPref(this, prefTokenIdx, 0)
        objectNavDrawer.setListener(::getContentFixThis)
        period = Utility.readPref(this, "LIGHTNING_PERIOD", period)
        periodPretty = UtilityLightning.getTimePretty(period)
        toolbarBottom.setOnClickListener { objectNavDrawer.drawerLayout.openDrawer(objectNavDrawer.listView) }
        getContent()
    }

    private fun getContentFixThis() {
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        title = "Lightning " + objectNavDrawer.getLabel()
        toolbar.subtitle = periodPretty
        bitmap = withContext(Dispatchers.IO) {
            UtilityLightning.getImage(objectNavDrawer.url, period)
        }
        img.setBitmap(bitmap)
        img.firstRunSetZoomPosn("LIGHTNING")
        Utility.writePref(this@LightningActivity, "LIGHTNING_PERIOD", period)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.shareBitmap(
                            this,
                            this,
                            "Lightning Strikes " + objectNavDrawer.getLabel() + " $periodPretty",
                            bitmap
                    )
                }
            }
            R.id.action_15min -> setPeriodGetContent("0.25", "15 MIN")
            R.id.action_2hr -> setPeriodGetContent("2", "2 HR")
            R.id.action_12hr -> setPeriodGetContent("12", "12 HR")
            R.id.action_24hr -> setPeriodGetContent("24", "24 HR")
            R.id.action_48hr -> setPeriodGetContent("48", "48 HR")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setPeriodGetContent(period: String, periodPretty: String) {
        this.period = period
        this.periodPretty = periodPretty
        getContent()
    }

    override fun onStop() {
        img.imgSavePosnZoom(this, "LIGHTNING")
        super.onStop()
    }
}
