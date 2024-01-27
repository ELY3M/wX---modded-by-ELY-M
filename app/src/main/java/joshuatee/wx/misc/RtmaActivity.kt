/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

package joshuatee.wx.misc

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.*
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

class RtmaActivity : VideoRecordActivity() {

    //
    // RTMA
    // https://mag.ncep.noaa.gov/observation-type-area.php
    //
    //  arg1: product (ex. "10m_wnd")
    //

    companion object {
        const val RID = ""
    }

    private lateinit var objectAnimate: ObjectAnimate
    private lateinit var touchImage: TouchImage
    private lateinit var navDrawer: NavDrawer
    private var sector = ""
    private var oldSector = ""
    private lateinit var arguments: Array<String>
    private val prefImagePosition = "RTMA_IMG"
    private var runTimes = listOf<String>()
    private var runTimeIndex = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.rtma, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        objectAnimate.setButton(menu)
        menu.findItem(R.id.action_time).title = runTimes.firstOrNull() ?: ""
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.rtma, bottomToolbar = false)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        arguments = intent.getStringArrayExtra(RID)!!
        setupUI()
        readPrefs()
        FutureVoid(::getRunTime, ::getContent)
    }

    private fun setupUI() {
        navDrawer = NavDrawer(this, UtilityRtma.labels, UtilityRtma.codes, ::getContent)
        touchImage = TouchImage(this, toolbar, R.id.iv, navDrawer, "")
        objectAnimate = ObjectAnimate(this, touchImage)
        objectToolbar.connectClick { navDrawer.open() }
        touchImage.setMaxZoom(8.0f)
        touchImage.connect(navDrawer) { getContent() }
    }

    private fun getRunTime() {
        runTimes = UtilityRtma.getTimes()
        invalidateOptionsMenu()
    }

    private fun getContent() {
        setTitle(sector, navDrawer.getLabel())
        FutureBytes(UtilityRtma.getUrl(navDrawer.url, sector, runTimes.getOrNull(runTimeIndex)
                ?: ""), ::display)
    }

    private fun getContentBySector(sector: String) {
        this.sector = sector
        setTitle(sector, navDrawer.getLabel())
        FutureBytes(UtilityRtma.getUrl(navDrawer.url, sector, runTimes.getOrNull(0)
                ?: ""), ::display)
    }

    private fun display(bitmap: Bitmap) {
        touchImage.set(bitmap)
        touchImage.firstRun(prefImagePosition)
        if (oldSector != sector) {
            touchImage.setZoom(1.0f)
            oldSector = sector
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    private fun readPrefs() {
        if (arguments.isNotEmpty()) {
            navDrawer.index = UtilityRtma.codes.indexOf(arguments[0])
        }
        sector = UtilityRtma.getNearest(Location.latLon)
        oldSector = sector
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_time -> ObjectDialogue.generic(this, runTimes, ::getContent) { i -> runTimeIndex = i }
            R.id.action_alaska -> getContentBySector("alaska")
            R.id.action_ca -> getContentBySector("ca")
            R.id.action_co -> getContentBySector("co")
            R.id.action_fl -> getContentBySector("fl")
            R.id.action_guam -> getContentBySector("guam")
            R.id.action_gulf_coast -> getContentBySector("gulf-coast")
            R.id.action_mi -> getContentBySector("mi")
            R.id.action_mid_atl -> getContentBySector("mid-atl")
            R.id.action_mid_west -> getContentBySector("mid-west")
            R.id.action_mt -> getContentBySector("mt")
            R.id.action_nc_sc -> getContentBySector("nc_sc")
            R.id.action_nd_sd -> getContentBySector("nd_sd")
            R.id.action_new_eng -> getContentBySector("new-eng")
            R.id.action_nw_pacific -> getContentBySector("nw-pacific")
            R.id.action_ohio_valley -> getContentBySector("ohio-valley")
            R.id.action_sw_us -> getContentBySector("sw_us")
            R.id.action_tx -> getContentBySector("tx")
            R.id.action_wi -> getContentBySector("wi")
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, navDrawer.getLabel(), touchImage)
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        FutureVoid(::getRunTime, ::getContent)
        super.onRestart()
    }

    override fun onStop() {
        touchImage.imgSavePosnZoom(prefImagePosition)
        super.onStop()
    }
}
