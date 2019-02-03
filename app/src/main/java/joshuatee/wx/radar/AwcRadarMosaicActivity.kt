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

package joshuatee.wx.radar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce

class AwcRadarMosaicActivity : VideoRecordActivity(), Toolbar.OnMenuItemClickListener {

    // Provides native interface to AWC radar mosaics along with animations
    //
    // arg1: "widget" (optional) - if this arg is specified it will show mosaic for widget location
    //       "location" for current location

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var animDrawable = AnimationDrawable()
    private lateinit var img: ObjectTouchImageView
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var contextg: Context
    private lateinit var drw: ObjectNavDrawer
    private val prefImagePosition = "AWCRADARMOSAIC"
    private var product = "rad_rala"
    private val prefTokenSector = "AWCMOSAIC_SECTOR_LAST_USED"
    private val prefTokenProduct = "AWCMOSAIC_PRODUCT_LAST_USED"
    private var sector = "us"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_awcmosaic,
            R.menu.awcmosaic,
            iconsEvenlySpaced = true,
            bottomToolbar = true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        drw = ObjectNavDrawer(this, UtilityAwcRadarMosaic.labels, UtilityAwcRadarMosaic.sectors)
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, drw, "")
        img.setMaxZoom(8.0f)
        img.setListener(this, drw, ::getContentFixThis)
        sector = Utility.readPref(prefTokenSector, sector)
        product = Utility.readPref(prefTokenProduct, product)
        drw.index = UtilityAwcRadarMosaic.sectors.indexOf(sector)
        drw.setListener(::getContentFixThis)
        toolbarBottom.setOnClickListener { drw.drawerLayout.openDrawer(drw.listView) }
        getContent(product)
    }

    private fun getContentFixThis() {
        getContent(product)
    }

    override fun onRestart() {
        getContent(product)
        super.onRestart()
    }

    private fun getContent(productLocal: String) = GlobalScope.launch(uiDispatcher) {
        product = productLocal
        toolbar.subtitle = drw.getLabel()
        bitmap = withContext(Dispatchers.IO) {
            UtilityAwcRadarMosaic.get(drw.getUrl(), product)
        }
        img.setBitmap(bitmap)
        animRan = false
        img.firstRunSetZoomPosn(prefImagePosition)
        Utility.writePref(contextg, prefTokenSector, drw.getUrl())
        Utility.writePref(contextg, prefTokenProduct, product)
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        animDrawable = withContext(Dispatchers.IO) {
            UtilityAwcRadarMosaic.getAnimation(contextg, drw.getUrl(), product)
        }
        animRan = UtilityImgAnim.startAnimation(animDrawable, img)
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
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.RADAR_MOSAIC)
            R.id.action_animate -> getAnimate()
            R.id.action_stop -> animDrawable.stop()
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
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    if (animRan) {
                        UtilityShare.shareAnimGif(
                            this,
                            "NWS mosaic",
                            animDrawable
                        )
                    } else {
                        UtilityShare.shareBitmap(
                            this,
                            "NWS mosaic",
                            bitmap
                        )
                    }
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


