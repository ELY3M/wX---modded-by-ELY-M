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

package joshuatee.wx.vis

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.ObjectAnimate
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut
import joshuatee.wx.util.To

class GoesActivity : VideoRecordActivity() {

    //
    // GOES 16 / GOES 17 image viewer
    // https://www.star.nesdis.noaa.gov/GOES/index.php
    //
    // Misc tab, ObjectWidgetGeneric.kt have: arrayOf("CONUS", "09")
    // NHC has arrayOf(url, "")
    // Cloud icon has arrayOf("")
    //
    //  arg1: sector or url(NHC) (ie "CONUS") or ""
    //  arg2 (optional): product (ie "09", "23" is lightning) or for NHC ""
    //

    companion object { const val RID = "" }

    private lateinit var objectAnimate: ObjectAnimate
    private lateinit var touchImage: TouchImage
    private lateinit var navDrawer: NavDrawer
    private var sector = "cgl"
    private var oldSector = "cgl"
    private var savePrefs = true
    private lateinit var arguments: Array<String>
    private val prefImagePosition = "GOES16_IMG"
    // NHC
    var goesFloater = false
    private var goesFloaterUrl = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.goes16, menu)
        if (goesFloater) {
            menu.setGroupVisible(R.id.sectors, false)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        objectAnimate.setButton(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.goes16, iconsEvenlySpaced = true, bottomToolbar = false)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        arguments = intent.getStringArrayExtra(RID)!!
        setupUI()
        readPrefs()
        getContent(sector)
    }

    private fun setupUI() {
        navDrawer = NavDrawer(this, UtilityGoes.labels, UtilityGoes.codes) { getContent(sector) }
        touchImage = TouchImage(this, toolbar, R.id.iv, navDrawer, "")
        objectAnimate = ObjectAnimate(this, touchImage)
        objectToolbar.connectClick { navDrawer.open() }
        touchImage.setMaxZoom(8.0f)
        touchImage.connect(navDrawer) { getContent(sector) }
    }

    private fun getContent(sector: String) {
        this.sector = sector
        writePrefs()
        setTitle(UtilityGoes.sectorToName[sector] ?: "", navDrawer.getLabel())
        if (!goesFloater) {
            FutureBytes(this, UtilityGoes.getImage(navDrawer.url, sector), ::display)
        } else {
            FutureBytes(this, UtilityGoes.getImageGoesFloater(goesFloaterUrl, navDrawer.url), ::display)
        }
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

    private fun writePrefs() {
        if (savePrefs) {
            Utility.writePref(this, "GOES16_SECTOR", sector)
            Utility.writePrefInt(this, "GOES16_IMG_FAV_IDX", navDrawer.index)
        }
    }

    private fun readPrefs() {
        if (arguments.isNotEmpty() && arguments[0] == "") {
            sector = Utility.readPref(this, "GOES16_SECTOR", sector)
            navDrawer.index = Utility.readPrefInt(this, "GOES16_IMG_FAV_IDX", 0)
        } else if (arguments.size > 1 && arguments[0].contains("http")) {
            // NHC floater
            goesFloater = true
            goesFloaterUrl = arguments[0]
            navDrawer.index = 0
            sector = goesFloaterUrl
            savePrefs = false
        } else {
            if (arguments.size > 1) {
                sector = arguments[0]
                navDrawer.index = To.int(arguments[1])
                savePrefs = false
            }
        }
        oldSector = sector
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.GOES16)
            R.id.action_animate -> {
                if (objectAnimate.isRunning()) {
                    objectAnimate.stop()
                } else {
                    getAnimate(To.int(RadarPreferences.uiAnimIconFrames))
                }
            }
            R.id.action_a12 -> getAnimate(12)
            R.id.action_a24 -> getAnimate(24)
            R.id.action_a36 -> getAnimate(36)
            R.id.action_a48 -> getAnimate(48)
            R.id.action_a60 -> getAnimate(60)
            R.id.action_a72 -> getAnimate(72)
            R.id.action_a84 -> getAnimate(84)
            R.id.action_a96 -> getAnimate(96)
            R.id.action_pause -> objectAnimate.pause()
            R.id.action_FD -> getContent("FD")
            R.id.action_FD_G17 -> getContent("FD-G17")
            R.id.action_CONUS -> getContent("CONUS")
            R.id.action_CONUS_G17 -> getContent("CONUS-G17")
            R.id.action_pnw -> getContent("pnw")
            R.id.action_nr -> getContent("nr")
            R.id.action_umv -> getContent("umv")
            R.id.action_cgl -> getContent("cgl")
            R.id.action_ne -> getContent("ne")
            R.id.action_psw -> getContent("psw")
            R.id.action_sr -> getContent("sr")
            R.id.action_sp -> getContent("sp")
            R.id.action_smv -> getContent("smv")
            R.id.action_se -> getContent("se")
            R.id.action_ak -> getContent("ak")
            R.id.action_cak -> getContent("cak")
            R.id.action_sea -> getContent("sea")
            R.id.action_hi -> getContent("hi")
            R.id.action_can -> getContent("can")
            R.id.action_mex -> getContent("mex")
            R.id.action_nsa -> getContent("nsa")
            R.id.action_ssa -> getContent("ssa")
            R.id.action_gm -> getContent("gm")
            R.id.action_car -> getContent("car")
            R.id.action_eus -> getContent("eus")
            R.id.action_pr -> getContent("pr")
            R.id.action_cam -> getContent("cam")
            R.id.action_taw -> getContent("taw")
            R.id.action_tpw -> getContent("tpw")
            R.id.action_tsp -> getContent("tsp")
            R.id.action_wus -> getContent("wus")
            R.id.action_eep -> getContent("eep")
            R.id.action_np -> getContent("np")
            R.id.action_na -> getContent("na")
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
        getContent(sector)
        super.onRestart()
    }

    override fun onStop() {
        touchImage.imgSavePosnZoom(prefImagePosition)
        super.onStop()
    }

    private fun getAnimate(frameCount: Int) {
        if (!goesFloater) {
            objectAnimate.animateClicked({}) { UtilityGoes.getAnimation(navDrawer.url, sector, frameCount) }
        } else {
            objectAnimate.animateClicked({}) { UtilityGoes.getAnimationGoesFloater(navDrawer.url, sector, frameCount) }
        }
    }
}
