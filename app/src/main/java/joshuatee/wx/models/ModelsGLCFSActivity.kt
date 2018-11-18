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

package joshuatee.wx.models

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.content.res.Configuration
import java.util.Locale

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class ModelsGLCFSActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // This code provides a native android interface to NWS Greak Lakes Coastal Forecast model
    //
    // http://www.glerl.noaa.gov/res/glcfs
    //

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var paramLabel = ""
    private var spinnerTimeRan = false
    private var spinnerSectorRan = false
    private var time = "01"
    private var paramTmp = ""
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private var sector = ""
    private lateinit var spTime: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private lateinit var drw: ObjectNavDrawer
    private val numPanes = 1
    private val curImg = 0
    private lateinit var displayData: DisplayData
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_modelsglcfs, R.menu.models_glcfs, false, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        title = "GLCFS"
        fab1 = ObjectFab(this, this, R.id.fab1)
        fab2 = ObjectFab(this, this, R.id.fab2)
        if (UIPreferences.fabInModels) {
            fab1.setOnClickListener(View.OnClickListener { UtilityModels.moveBack(spTime) })
            fab2.setOnClickListener(View.OnClickListener { UtilityModels.moveForward(spTime) })
            val m = toolbarBottom.menu
            val leftArrow = m.findItem(R.id.action_back)
            val rightArrow = m.findItem(R.id.action_forward)
            leftArrow.isVisible = false
            rightArrow.isVisible = false
        }
        fab1.setVisibility(View.GONE)
        fab2.setVisibility(View.GONE)
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        for (i in (1 until 14)) {
            spTime.add(String.format(Locale.US, "%02d", i))
        }
        for (i in (15..100 step 3)) {
            spTime.add(String.format(Locale.US, "%02d", i))
        }
        for (i in (102..121 step 3)) {
            spTime.add(String.format(Locale.US, "%03d", i))
        }
        spTime.notifyDataSetChanged()
        displayData = DisplayData(this, this, this, numPanes, spTime)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, UtilityModelGLCFSInterface.SECTORS)
        spTime.setOnItemSelectedListener(this)
        spSector.setOnItemSelectedListener(this)
        sector = Utility.readPref(this, "MODEL_GLCFS_SECTOR_LAST_USED", "s")
        spSector.setSelection(sector)
        spTime.setSelection(Utility.readPref(this, "GLCFS_RUN_POSN", 0))
        drw = ObjectNavDrawer(this, UtilityModelGLCFSInterface.LABELS, UtilityModelGLCFSInterface.PARAMS)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, true)
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(position)
            paramLabel = drw.getLabel(position)
            Utility.writePref(this, "MODEL_GLCFS_PARAM_LAST_USED", displayData.param[curImg])
            Utility.writePref(this, "MODEL_GLCFS_PARAM_LAST_USED_LABEL", paramLabel)
            getContent()
        }
        setupGLCFS()
        getContent()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerTimeRan && spinnerSectorRan) {
            getContent()
        } else {
            when (parent.id) {
                R.id.spinner_time -> if (!spinnerTimeRan)
                    spinnerTimeRan = true
                R.id.spinner_sector -> if (!spinnerSectorRan)
                    spinnerSectorRan = true
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        toolbar.subtitle = paramLabel
        time = spTime.selectedString
        val sectorOrig = spSector.selectedString
        sector = ""
        if (sectorOrig.split(" ").size > 1) {
            sector = sectorOrig.split(" ")[1].substring(0, 1).toLowerCase()
        }
        paramTmp = displayData.param[curImg]
        Utility.writePref(contextg, "MODEL_GLCFS_SECTOR_LAST_USED", sectorOrig)

        // http://www.glerl.noaa.gov/res/glcfs/fcast/swv+05.gif
        displayData.bitmap[curImg] = withContext(Dispatchers.IO) { UtilityModelGLCFSInputOutput.getImage(sector, paramTmp, time) }

        displayData.img[curImg].visibility = View.VISIBLE
        displayData.img[curImg].setImageDrawable(UtilityImg.bitmapToLayerDrawable(contextg, displayData.bitmap[curImg]))
        animRan = false
        if (!firstRun) {
            UtilityImg.imgRestorePosnZoom(contextg, displayData.img[curImg], "GLCFS")
            firstRun = true
            if (UIPreferences.fabInModels) {
                fab1.setVisibility(View.VISIBLE)
                fab2.setVisibility(View.VISIBLE)
            }
        }
        imageLoaded = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(spTime)
            R.id.action_forward -> UtilityModels.moveForward(spTime)
            R.id.action_animate -> getAnimate()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan)
                        UtilityShare.shareAnimGif(this, paramLabel + " " + spTime.selectedItem.toString(), displayData.animDrawable[curImg])
                    else
                        UtilityShare.shareBitmap(this, paramLabel + " " + spTime.selectedItem.toString(), displayData.bitmap[curImg])
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        val spinnerTimeValue = spTime.selectedItemPosition
        displayData.animDrawable[curImg] = withContext(Dispatchers.IO) { UtilityModelGLCFSInputOutput.getAnimation(contextg, sector, paramTmp, spinnerTimeValue, spTime.list) }
        animRan = UtilityImgAnim.startAnimation(displayData.animDrawable[curImg], displayData.img[curImg])
    }

    private fun setupGLCFS() {
        displayData.param[curImg] = "wv"
        displayData.param[curImg] = Utility.readPref(this, "MODEL_GLCFS_PARAM_LAST_USED", displayData.param[curImg])
        paramLabel = "Wave Height"
        paramLabel = Utility.readPref(this, "MODEL_GLCFS_PARAM_LAST_USED_LABEL", paramLabel)
        if (!UtilityModels.parmInArray(UtilityModelGLCFSInterface.PARAMS, displayData.param[curImg])) {
            displayData.param[curImg] = "1ref_sfc"
            paramLabel = "1 km agl reflectivity"
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, displayData.img[curImg], "GLCFS")
            Utility.writePref(this, "GLCFS_RUN_POSN", spTime.selectedItemPosition)
        }
        super.onStop()
    }
}

