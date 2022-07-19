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
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class RadarMosaicNwsActivity : VideoRecordActivity() {

    //
    // Provides native interface to NWS radar mosaics along with animations
    //   https://www.weather.gov/media/notification/pdf2/pns22-09_ridge_ii_public_local_standard_radar_pages.pdf
    //   https://www.weather.gov/media/notification/pdf2/pns22-19_aviation_website_upgrade.pdf
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
    private val prefImagePosition = "RADARMOSAICNWS"
    private val prefTokenSector = "REMEMBER_NWSMOSAIC_SECTOR"
    private var sector = UtilityNwsRadarMosaic.getNearestMosaic(Location.latLon)
    private lateinit var animateButton: MenuItem

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.radarnwsmosaic, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        animateButton = menu.findItem(R.id.action_animate)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.radarnwsmosaic, iconsEvenlySpaced = true, bottomToolbar = false)
        val activityArguments = intent.getStringArrayExtra(URL)!!
        objectNavDrawer = ObjectNavDrawer(this, UtilityNwsRadarMosaic.labels, UtilityNwsRadarMosaic.sectors) { getContent() }
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, objectNavDrawer, "")
        img.setMaxZoom(8.0f)
        img.setListener(this, objectNavDrawer) { getContent() }
        if (activityArguments.isNotEmpty() && activityArguments[0] != "") {
            sector = activityArguments[0]
        }
        // sector = Utility.readPref(this, prefTokenSector, sector)
        objectNavDrawer.index = UtilityNwsRadarMosaic.sectors.indexOf(sector)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (animDrawable.isRunning) {
            animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
            animDrawable.stop()
        }
        title = objectNavDrawer.url
        FutureVoid(this, { bitmap = UtilityNwsRadarMosaic.get(objectNavDrawer.url) }, ::showImage)
    }

    private fun showImage() {
        img.setBitmap(bitmap)
        animRan = false
        img.firstRunSetZoomPosn(prefImagePosition)
        Utility.writePref(this@RadarMosaicNwsActivity, prefTokenSector, objectNavDrawer.url)
    }

    private fun getAnimate() {
        if (animDrawable.isRunning) {
            animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
            animDrawable.stop()
        } else {
            animateButton.setIcon(GlobalVariables.ICON_STOP_WHITE)
            FutureVoid(this@RadarMosaicNwsActivity,
                    { animDrawable = UtilityNwsRadarMosaic.getAnimation(this@RadarMosaicNwsActivity, objectNavDrawer.url) })
            { animRan = UtilityImgAnim.startAnimation(animDrawable, img) }
        }
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
