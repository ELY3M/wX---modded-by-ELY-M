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

package joshuatee.wx.activitiesmisc

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class LightningActivity : VideoRecordActivity() {

    //
    // Used to view lighting data
    //

    private var bitmap = UtilityImg.getBlankBitmap()
    private var period = "0.25"
    private var periodPretty = "15 MIN"
    private lateinit var image: TouchImage
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private val prefTokenIdx = "LIGHTNING_SECTOR_IDX"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.lightning_activity, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.lightning_activity, iconsEvenlySpaced = true, bottomToolbar = false)
        objectNavDrawer = ObjectNavDrawer(this, UtilityLightning.labels, UtilityLightning.urls, ::getContent)
        image = TouchImage(this, toolbar, toolbarBottom, R.id.iv, objectNavDrawer, prefTokenIdx)
        objectNavDrawer.index = Utility.readPrefInt(this, prefTokenIdx, 0)
        period = Utility.readPref(this, "LIGHTNING_PERIOD", period)
        periodPretty = UtilityLightning.getTimePretty(period)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        title = "Lightning " + objectNavDrawer.getLabel()
        toolbar.subtitle = periodPretty
        FutureVoid(this, { bitmap = UtilityLightning.getImage(objectNavDrawer.url, period) }, ::showImage)
    }

    private fun showImage() {
        image.set(bitmap)
        image.firstRun("LIGHTNING")
        Utility.writePref(this@LightningActivity, "LIGHTNING_PERIOD", period)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, "Lightning Strikes " + objectNavDrawer.getLabel() + " $periodPretty", bitmap)
                }
            }
            R.id.action_15min -> setPeriodGetContent("0.25")
            R.id.action_2hr -> setPeriodGetContent("2")
            R.id.action_12hr -> setPeriodGetContent("12")
            R.id.action_24hr -> setPeriodGetContent("24")
            R.id.action_48hr -> setPeriodGetContent("48")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setPeriodGetContent(period: String) {
        this.period = period
        periodPretty = UtilityLightning.getTimePretty(period)
        getContent()
    }

    override fun onStop() {
        image.imgSavePosnZoom("LIGHTNING")
        super.onStop()
    }
}
