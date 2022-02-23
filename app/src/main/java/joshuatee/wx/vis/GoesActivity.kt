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

package joshuatee.wx.vis

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.Extensions.startAnimation
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import joshuatee.wx.util.To

class GoesActivity : VideoRecordActivity() {

    //
    // GOES 16 / GOES 17 image viewer
    // https://www.star.nesdis.noaa.gov/GOES/index.php
    //
    // Misc tab and ObjectWidgetGeneric.kt have:
    //    GoesActivity::class.java,
    //    GoesActivity.RID,
    //    arrayOf("CONUS", "09"),
    //

    companion object { const val RID = "" }

    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var img: ObjectTouchImageView
    private var animDrawable = AnimationDrawable()
    private lateinit var drw: ObjectNavDrawer
    private var sector = "cgl"
    private var oldSector = "cgl"
    private var savePrefs = true
    private lateinit var activityArguments: Array<String>
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

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.goes16, iconsEvenlySpaced = true, bottomToolbar = false)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        activityArguments = intent.getStringArrayExtra(RID)!!
        drw = ObjectNavDrawer(this, UtilityGoes.labels, UtilityGoes.codes) { getContent(sector) }
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv, drw, "")
        img.setMaxZoom(8.0f)
        img.setListener(this, drw) { getContent(sector) }
        readPrefs()
        getContent(sector)
    }

    private fun getContent(sectorF: String) {
        sector = sectorF
        writePrefs()
        toolbar.title = UtilityGoes.sectorToName[sector] ?: ""
        toolbar.subtitle = drw.getLabel()
        if (!goesFloater) {
            FutureVoid(this, { bitmap = UtilityGoes.getImage(drw.url, sector) }, ::display)
        } else {
            FutureVoid(this, { bitmap = UtilityGoes.getImageGoesFloater(goesFloaterUrl, drw.url) }, ::display)
        }
    }

    private fun display() {
        img.setBitmap(bitmap)
        img.firstRunSetZoomPosn(prefImagePosition)
        if (oldSector != sector) {
            img.setZoom(1.0f)
            oldSector = sector
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    private fun writePrefs() {
        if (savePrefs) {
            Utility.writePref(this, "GOES16_SECTOR", sector)
            Utility.writePref(this, "GOES16_IMG_FAV_IDX", drw.index)
        }
    }

    private fun readPrefs() {
        if (activityArguments.isNotEmpty() && activityArguments[0] == "") {
            sector = Utility.readPref(this@GoesActivity, "GOES16_SECTOR", sector)
            drw.index = Utility.readPref(this@GoesActivity, "GOES16_IMG_FAV_IDX", 0)
        } else if (activityArguments.size > 1 && activityArguments[0].contains("http")) {
            // NHC floater
            goesFloater = true
            goesFloaterUrl = activityArguments[0]
            drw.index = 0
            sector = goesFloaterUrl
            savePrefs = false
        } else {
            if (activityArguments.size > 1) {
                sector = activityArguments[0]
                drw.index = To.int(activityArguments[1])
                savePrefs = false
            }
        }
        oldSector = sector
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.GOES16)
            R.id.action_a12 -> getAnimate(12)
            R.id.action_a24 -> getAnimate(24)
            R.id.action_a36 -> getAnimate(36)
            R.id.action_a48 -> getAnimate(48)
            R.id.action_a60 -> getAnimate(60)
            R.id.action_a72 -> getAnimate(72)
            R.id.action_a84 -> getAnimate(84)
            R.id.action_a96 -> getAnimate(96)
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
            R.id.action_wus -> getContent("wus")
            R.id.action_eep -> getContent("eep")
            R.id.action_np -> getContent("np")
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, this, drw.getLabel(), bitmap)
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
        img.imgSavePosnZoom(this, prefImagePosition)
        super.onStop()
    }

    private fun getAnimate(frameCount: Int) {
        if (!goesFloater) {
            FutureVoid(this@GoesActivity,
                { animDrawable = UtilityGoes.getAnimation(this@GoesActivity, drw.url, sector, frameCount) })
                { animDrawable.startAnimation(img) }
        } else {
            FutureVoid(this@GoesActivity,
                { animDrawable = UtilityGoes.getAnimationGoesFloater(this@GoesActivity, drw.url, sector, frameCount) })
                { animDrawable.startAnimation(img) }
        }
    }
}
