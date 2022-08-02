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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.ObjectAnimate
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class AwcRadarMosaicActivity : VideoRecordActivity() {

    //
    // Provides native interface to AWC radar mosaics along with animations
    //
    // arg1: "widget" (optional) - if this arg is specified it will show mosaic for widget location
    //       "location" for current location
    //

    companion object { const val URL = "" }

    private lateinit var objectAnimate: ObjectAnimate
    private lateinit var image: TouchImage
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private val prefImagePosition = "AWCRADARMOSAIC"
    private var product = "rad_rala"
    private val prefTokenSector = "AWCMOSAIC_SECTOR_LAST_USED"
    private val prefTokenProduct = "AWCMOSAIC_PRODUCT_LAST_USED"
    private var sector = "us"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.awcmosaic, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        objectAnimate.setButton(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.awcmosaic, iconsEvenlySpaced = true, bottomToolbar = false)
        objectNavDrawer = ObjectNavDrawer(this, UtilityAwcRadarMosaic.labels, UtilityAwcRadarMosaic.sectors) { getContent(product) }
        image = TouchImage(this, toolbar, R.id.iv, objectNavDrawer, "")
        objectAnimate = ObjectAnimate(this, image)
        toolbar.setOnClickListener { objectNavDrawer.open() }
        image.setMaxZoom(8.0f)
        image.connect(objectNavDrawer) { getContent(product) }
        sector = Utility.readPref(this, prefTokenSector, sector)
        product = Utility.readPref(this, prefTokenProduct, product)
        objectNavDrawer.index = UtilityAwcRadarMosaic.sectors.indexOf(sector)
        getContent(product)
    }

    override fun onRestart() {
        getContent(product)
        super.onRestart()
    }

    private fun getContent(productLocal: String = product) {
        objectAnimate.stop()
        product = productLocal
        toolbar.subtitle = objectNavDrawer.getLabel()
        title = product
        FutureBytes(this, UtilityAwcRadarMosaic.get(objectNavDrawer.url, product), ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        image.set(bitmap)
        image.firstRun(prefImagePosition)
        Utility.writePref(this, prefTokenSector, objectNavDrawer.url)
        Utility.writePref(this, prefTokenProduct, product)
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
            R.id.action_animate -> objectAnimate.animateClicked(::getContent) { UtilityAwcRadarMosaic.getAnimation(objectNavDrawer.url, product) }
            R.id.action_stop -> objectAnimate.stop()
            R.id.action_pause -> objectAnimate.pause()
            R.id.action_rad_rala -> getContent("rad_rala")
            R.id.action_rad_cref -> getContent("rad_cref")
            R.id.action_rad_tops18 -> getContent("rad_tops-18")
            R.id.action_sat_irbw -> getContent("sat_irbw")
            R.id.action_sat_ircol -> getContent("sat_ircol")
            R.id.action_sat_irnws -> getContent("sat_irnws")
            R.id.action_sat_vis -> getContent("sat_vis")
            R.id.action_sat_wv -> getContent("sat_wv")
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, "NWS mosaic", image.bitmap)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        image.imgSavePosnZoom(prefImagePosition)
        super.onStop()
    }
}
