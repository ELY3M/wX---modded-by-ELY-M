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

package joshuatee.wx.radar

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.objects.ObjectAnimate
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

class RadarMosaicNwsActivity : VideoRecordActivity() {

    //
    // Provides native interface to NWS radar mosaics along with animations
    //   https://www.weather.gov/media/notification/pdf2/pns22-09_ridge_ii_public_local_standard_radar_pages.pdf
    //   https://www.weather.gov/media/notification/pdf2/pns22-19_aviation_website_upgrade.pdf
    //
    // arg1: "sector" (optional) - if this arg is not a empty string then the last used location will be used
    //

    companion object {
        const val URL = ""
    }

    private lateinit var objectAnimate: ObjectAnimate
    private lateinit var touchImage: TouchImage
    private lateinit var navDrawer: NavDrawer
    private val prefImagePosition = "RADARMOSAICNWS"
    private val prefTokenSector = "REMEMBER_NWSMOSAIC_SECTOR"
    private var sector = UtilityNwsRadarMosaic.getNearest(Location.latLon)
    private var saveLocation = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.radarnwsmosaic, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        objectAnimate.setButton(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.radarnwsmosaic, iconsEvenlySpaced = true, bottomToolbar = false)
        val arguments = intent.getStringArrayExtra(URL)!!
        setupUI()
        if (arguments.isNotEmpty() && arguments[0] != "") {
            sector = Utility.readPref(this, prefTokenSector, sector)
            saveLocation = true
        }
        navDrawer.index = UtilityNwsRadarMosaic.sectors.indexOf(sector)
        getContent()
    }

    private fun setupUI() {
        navDrawer = NavDrawer(this, UtilityNwsRadarMosaic.labels, UtilityNwsRadarMosaic.sectors) { getContent() }
        touchImage = TouchImage(this, toolbar, R.id.iv, navDrawer, "")
        objectAnimate = ObjectAnimate(this, touchImage)
        objectToolbar.connectClick { navDrawer.open() }
        touchImage.setMaxZoom(8.0f)
        touchImage.connect(navDrawer) { getContent() }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        objectAnimate.stop()
        title = navDrawer.url
        FutureBytes(UtilityNwsRadarMosaic.get(navDrawer.url), ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        touchImage.set(bitmap)
        touchImage.firstRun(prefImagePosition)
        if (saveLocation) {
            Utility.writePref(this, prefTokenSector, navDrawer.url)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_animate -> objectAnimate.animateClicked(::getContent) { UtilityNwsRadarMosaic.getAnimation(navDrawer.url) }
            R.id.action_stop -> objectAnimate.stop()
            R.id.action_pause -> objectAnimate.pause()
            R.id.action_share -> if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                checkOverlayPerms()
            } else {
                UtilityShare.bitmap(this, "NWS mosaic", touchImage)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        touchImage.imgSavePosnZoom(prefImagePosition)
        super.onStop()
    }
}
