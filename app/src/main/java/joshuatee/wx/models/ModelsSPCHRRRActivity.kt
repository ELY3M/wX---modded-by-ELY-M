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

import java.util.Arrays
import java.util.Locale

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsSPCHRRRActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var spRun: ObjectSpinner
    private lateinit var spTime: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private var animRan = false
    private var paramLabel = ""
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var spinnerSectorRan = false
    private var run = "00Z"
    private var time = "00"
    private var param = "sfc_prec"
    private var model = "HRRR Experimental"
    private var sector = "US"
    private var firstRun = false
    private var imageLoaded = false
    private var firstRunTimeSet = false
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private val overlayImg = mutableListOf<String>()
    private val modelparmsImg = mutableListOf<String>()
    private lateinit var miStatus: MenuItem
    private val curImg = 0
    private var prefSector = ""
    private var prefModel = ""
    private var prefParam = ""
    private var prefParamLabel = ""
    private var prefRunPosn = ""
    private var rtd = RunTimeData()
    private lateinit var drw: ObjectNavDrawer
    private val numPanes = 1
    private lateinit var displayData: DisplayData
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_modelsspchrrr, R.menu.models_spchrrr, false, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        title = "HRRR"
        prefModel = "SPCHRRR"
        prefSector = "MODEL_" + prefModel + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = prefModel + "_RUN_POSN"
        overlayImg.addAll(Arrays.asList(*TextUtils.split(Utility.readPref(this, "SPCHRRR_OVERLAY", ""), ":")))
        modelparmsImg.addAll(Arrays.asList(*TextUtils.split(Utility.readPref(this, "SPCHRRR_PARMS", "refc:pmsl"), ":")))
        fab1 = ObjectFab(this, this, R.id.fab1)
        fab2 = ObjectFab(this, this, R.id.fab2)
        val m = toolbarBottom.menu
        if (UIPreferences.fabInModels) {
            fab1.setOnClickListener(View.OnClickListener { UtilityModels.moveBack(spTime) })
            fab2.setOnClickListener(View.OnClickListener { UtilityModels.moveForward(spTime) })
            val leftArrow = m.findItem(R.id.action_back)
            val rightArrow = m.findItem(R.id.action_forward)
            leftArrow.isVisible = false
            rightArrow.isVisible = false
        }
        fab1.setVisibility(View.GONE)
        fab2.setVisibility(View.GONE)
        miStatus = m.findItem(R.id.action_status)
        miStatus.title = "in through"
        model = Utility.readPref(this, prefModel, "HRRR")
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        displayData = DisplayData(this, this, this, numPanes, spTime)
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, UtilityModelSPCHRRRInterface.SECTORS)
        sector = Utility.readPref(this, prefSector, "S19")
        spSector.setSelection(sector)
        spTime.setOnItemSelectedListener(this)
        spRun.setOnItemSelectedListener(this)
        spSector.setOnItemSelectedListener(this)
        spRun.setSelection(0)
        spTime.setSelection(0)
        drw = ObjectNavDrawer(this, UtilityModelSPCHRRRInterface.LABELS, UtilityModelSPCHRRRInterface.PARAMS)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            param = drw.getToken(position)
            paramLabel = drw.getLabel(position)
            Utility.writePref(this, prefParam, param)
            Utility.writePref(this, prefParamLabel, paramLabel)
            modelParmSelected(param)
            getContent()
        }
        model = "HRRR"
        Utility.writePref(this, prefModel, model)
        setupHRRR()
        getRunStatus()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerRunRan && spinnerTimeRan && spinnerSectorRan) {
            getContent()
        } else {
            when (parent.id) {
                R.id.spinner_run -> if (!spinnerRunRan)
                    spinnerRunRan = true
                R.id.spinner_time -> if (!spinnerTimeRan)
                    spinnerTimeRan = true
                R.id.spinner_sector -> if (!spinnerSectorRan)
                    spinnerSectorRan = true
            }
        }
        if (parent.id == R.id.spinner_run) {
            UtilityModels.updateTime(UtilityString.getLastXChars(spRun.selectedItem.toString(), 2),
                    rtd.mostRecentRun, spTime.list, spTime.arrayAdapter, "", false)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        run = spRun.selectedItem.toString()
        time = spTime.selectedItem.toString()
        sector = spSector.selectedItem.toString()
        time = UtilityStringExternal.truncate(time, 2)
        Utility.writePref(contextg, prefSector, sector)
        withContext(Dispatchers.IO) {
            displayData.bitmap[curImg] = UtilityModelSPCHRRRInputOutput.getImage(contextg, sector, run, time, rtd.validTime, overlayImg, modelparmsImg)
        }
        displayData.img[curImg].visibility = View.VISIBLE
        displayData.img[curImg].setImageDrawable(UtilityImg.bitmapToLayerDrawable(contextg, displayData.bitmap[curImg]))
        displayData.img[curImg].setMaxZoom(4f)
        animRan = false
        if (!firstRun) {
            displayData.img[curImg].setZoom(MyApplication.spchrrrZoom, MyApplication.spchrrrX, MyApplication.spchrrrY)
            firstRun = true
            if (UIPreferences.fabInModels) {
                fab1.setVisibility(View.VISIBLE)
                fab2.setVisibility(View.VISIBLE)
            }
        }
        imageLoaded = true
        UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + modelparmsImg.toString().replace("[", "").replace("]", ""))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_layer_obs -> overlaySelected("bigsfc")
            R.id.action_layer_topo -> overlaySelected("topo")
            R.id.action_layer_rgnlrad -> overlaySelected("rgnlrad")
            R.id.action_layer_warns -> overlaySelected("warns")
            R.id.action_layer_otlk -> overlaySelected("otlk")
            R.id.action_layer_cwa -> overlaySelected("cwa")
            R.id.action_layer_hiway -> overlaySelected("hiway")
            R.id.action_layer_population -> overlaySelected("population")
            R.id.action_layer_cnty -> overlaySelected("cnty")
            R.id.action_layer_clear -> {
                overlayImg.clear()
                getContent()
            }
            R.id.action_clear_params -> {
                modelparmsImg.clear()
                getContent()
            }
            R.id.action_back -> UtilityModels.moveBack(spTime)
            R.id.action_forward -> UtilityModels.moveBack(spTime)
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
                        UtilityShare.shareAnimGif(this, model + " " + paramLabel + " " + spTime.selectedItem.toString(), displayData.animDrawable[curImg])
                    else
                        UtilityShare.shareBitmap(this, model + " " + paramLabel + " " + spTime.selectedItem.toString(), displayData.bitmap[curImg])
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun overlaySelected(mesoS: String) {
        if (overlayImg.contains(mesoS))
            overlayImg.remove(mesoS)
        else
            overlayImg.add(mesoS)
    }

    private fun modelParmSelected(mesoS: String) {
        if (modelparmsImg.contains(mesoS))
            modelparmsImg.remove(mesoS)
        else
            modelparmsImg.add(mesoS)
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        val timeAl = mutableListOf<String>()
        val spinnerTimeValue = spTime.selectedItemPosition
        (spinnerTimeValue until spTime.size()).mapTo(timeAl) { spTime.getItemAtPosition(it).toString() }
        withContext(Dispatchers.IO) {
            displayData.animDrawable[curImg] = UtilityModelSPCHRRRInputOutput.getAnimation(contextg, sector, run, rtd.validTime, spinnerTimeValue, spTime.list, overlayImg, modelparmsImg)
        }
        animRan = UtilityImgAnim.startAnimation(displayData.animDrawable[curImg], displayData.img[curImg])
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        rtd = withContext(Dispatchers.IO) { UtilityModelSPCHRRRInputOutput.runTime }
        spRun.clear()
        spRun.addAll(rtd.listRun)
        spRun.notifyDataSetChanged()
        miStatus.title = "in through " + rtd.imageCompleteStr
        toolbar.title = rtd.imageCompleteStr
        spRun.setSelection(0)
        spTime.setSelection(0)
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            spTime.setSelection(Utility.readPref(contextg, prefRunPosn, 0))
        }
        spTime.notifyDataSetChanged()
        getContent()
    }

    private fun setupHRRR() {
        param = "refc"
        param = Utility.readPref(this, prefParam, param)
        paramLabel = "Composite Reflectivity"
        paramLabel = Utility.readPref(this, prefParamLabel, paramLabel)
        if (!UtilityModels.parmInArray(UtilityModelSPCHRRRInterface.PARAMS, param)) {
            param = "refc"
            paramLabel = "Composite Reflectivity"
        }
        spRun.setSelection(0)
        spTime.setSelection(0)
        spTime.clear()
        (2 until 16).forEach { spTime.add(String.format(Locale.US, "%02d", it)) }
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
            Utility.writePref(this, "SPCHRRR_OVERLAY", TextUtils.join(":", overlayImg))
            Utility.writePref(this, "SPCHRRR_PARMS", TextUtils.join(":", modelparmsImg))
            UtilityImg.imgSavePosnZoom(this, displayData.img[curImg], prefModel)
            Utility.writePref(this, prefRunPosn, spTime.selectedItemPosition)
        }
        super.onStop()
    }
}

