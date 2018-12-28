/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class GOES16Activity : VideoRecordActivity(), View.OnClickListener,
    Toolbar.OnMenuItemClickListener {

    companion object {
        const val RID: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var img: TouchImageView2
    private var animDrawable = AnimationDrawable()
    private lateinit var drw: ObjectNavDrawer
    private var sector = "cgl"
    private var oldSector = "cgl"
    private var savePrefs = true
    private lateinit var activityArguments: Array<String>
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_image_show_navdrawer_bottom_toolbar,
            R.menu.goes16,
            true,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        img = findViewById(R.id.iv)
        img.setMaxZoom(8f)
        img.setOnClickListener(this)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) UtilityImg.showNextImg(drw, ::getContentFixThis)
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) UtilityImg.showPrevImg(drw, ::getContentFixThis)
            }
        })
        activityArguments = intent.getStringArrayExtra(RID)
        drw = ObjectNavDrawer(this, UtilityGOES16.labels, UtilityGOES16.codes)
        readPrefs(this)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, true)
            drw.drawerLayout.closeDrawer(drw.listView)
            drw.index = position
            getContent(sector)
        }
        getContent(sector)
    }

    // FIXME
    private fun getContentFixThis() {
        getContent(sector)
    }

    private fun getContent(sectorF: String) = GlobalScope.launch(uiDispatcher) {
        sector = sectorF
        writePrefs()
        toolbar.subtitle = drw.getLabel()
        val url = withContext(Dispatchers.IO) { UtilityGOES16.getUrl(drw.getUrl(), sector) }
        bitmap = withContext(Dispatchers.IO) { url.getImage() }
        img.setImageBitmap(bitmap)
        if (!firstRun) {
            img.setZoom("GOES16_IMG")
            firstRun = true
        }
        imageLoaded = true
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

    private fun readPrefs(context: Context) {
        if (activityArguments.isNotEmpty() && activityArguments[0] == "") {
            sector = Utility.readPref(context, "GOES16_SECTOR", sector)
            drw.index = Utility.readPref(context, "GOES16_IMG_FAV_IDX", 0)
        } else {
            if (activityArguments.size > 1) {
                sector = activityArguments[0]
                drw.index = 9
                savePrefs = false
            }
        }
        oldSector = sector
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.GOES16)
            R.id.action_a24 -> getAnimate(24)
            R.id.action_a36 -> getAnimate(36)
            R.id.action_a48 -> getAnimate(48)
            R.id.action_a60 -> getAnimate(60)
            R.id.action_a72 -> getAnimate(72)
            R.id.action_a84 -> getAnimate(84)
            R.id.action_a96 -> getAnimate(96)
            R.id.action_FD -> getContent("FD")
            R.id.action_CONUS -> getContent("CONUS")
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
            R.id.action_gm -> getContent("gm")
            R.id.action_car -> getContent("car")
            R.id.action_eus -> getContent("eus")
            R.id.action_pr -> getContent("pr")
            R.id.action_taw -> getContent("taw")
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else
                    UtilityShare.shareText(this, drw.getLabel(), "", bitmap)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        getContent(sector)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) UtilityImg.imgSavePosnZoom(this, img, "GOES16_IMG")
        super.onStop()
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        animDrawable = withContext(Dispatchers.IO) {
            UtilityGOES16.getAnimation(
                contextg,
                drw.getUrl(),
                sector,
                frameCount
            )
        }
        UtilityImgAnim.startAnimation(animDrawable, img)
    }
}
