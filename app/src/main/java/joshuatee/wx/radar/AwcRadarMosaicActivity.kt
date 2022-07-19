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

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
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

    private var animRan = false
    private var animDrawable = AnimationDrawable()
    private lateinit var img: ObjectTouchImageView
    private var bitmap = UtilityImg.getBlankBitmap()
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

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.awcmosaic, iconsEvenlySpaced = true, bottomToolbar = false)
        objectNavDrawer = ObjectNavDrawer(this, UtilityAwcRadarMosaic.labels, UtilityAwcRadarMosaic.sectors) { getContent(product) }
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, objectNavDrawer, "")
        img.setMaxZoom(8.0f)
        img.setListener(this, objectNavDrawer) { getContent(product) }
        sector = Utility.readPref(this, prefTokenSector, sector)
        product = Utility.readPref(this, prefTokenProduct, product)
        objectNavDrawer.index = UtilityAwcRadarMosaic.sectors.indexOf(sector)
        getContent(product)
    }

    override fun onRestart() {
        getContent(product)
        super.onRestart()
    }

    private fun getContent(productLocal: String) {
        product = productLocal
        toolbar.subtitle = objectNavDrawer.getLabel()
        title = product
        FutureVoid(this, { bitmap = UtilityAwcRadarMosaic.get(objectNavDrawer.url, product) }, ::showImage)
    }

    private fun showImage() {
        img.setBitmap(bitmap)
        animRan = false
        img.firstRunSetZoomPosn(prefImagePosition)
        Utility.writePref(this@AwcRadarMosaicActivity, prefTokenSector, objectNavDrawer.url)
        Utility.writePref(this@AwcRadarMosaicActivity, prefTokenProduct, product)
    }

    private fun getAnimate() {
        FutureVoid(this@AwcRadarMosaicActivity,
            { animDrawable = UtilityAwcRadarMosaic.getAnimation(this@AwcRadarMosaicActivity, objectNavDrawer.url, product) })
            { animRan = UtilityImgAnim.startAnimation(animDrawable, img) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_animate -> getAnimate()
            R.id.action_stop -> animDrawable.stop()
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
                    if (animRan) {
                        //UtilityShare.animGif(this, "NWS mosaic", animDrawable)
                    } else {
                        UtilityShare.bitmap(this, this, "NWS mosaic", bitmap)
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        img.imgSavePosnZoom(this, prefImagePosition)
        super.onStop()
    }
}
