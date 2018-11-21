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

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.objects.ObjectIntent
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

class ModelsNCEPActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // This code provides a native android interface to Weather Models @ NCEP
    // http://mag.ncep.noaa.gov/model-guidance-model-area.php
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        var INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var firstRun = false
    private var firstRunTimeSet = false
    private var imageLoaded = false
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var spinnerSectorRan = false
    private var run = "00Z"
    private var time = "00"
    private var model = "GFS"
    private var sector = "US"
    private var numPanes = 2
    private var curImg = 0
    private lateinit var miStatus: MenuItem
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var prefSector: String
    private lateinit var prefModel: String
    private var prefParam = ""
    private var prefParamLabel = ""
    private var prefRunPosn = ""
    private lateinit var spRun: ObjectSpinner
    private lateinit var spTime: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private lateinit var spModel: ObjectSpinner
    private var rtd = RunTimeData()
    private lateinit var drw: ObjectNavDrawer
    private lateinit var displayData: DisplayData
    private var intentArg = ""
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        var turl = intent.getStringArrayExtra(INFO)
        contextg = this
        if (turl == null) {
            turl = arrayOf("1", "NCEP")
        }
        val numPanesStr = turl[0]
        intentArg = turl[1]
        numPanes = numPanesStr.toIntOrNull() ?: 0
        if (numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_modelsncep, R.menu.models_ncep, false, true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_modelsncepmultipane, R.menu.models_ncep, false, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = turl[1]
        prefModel = turl[1] + numPanesStr
        prefSector = "MODEL_" + prefModel + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = prefModel + "_RUN_POSN"
        val m = toolbarBottom.menu
        if (numPanes < 2) {
            fab1 = ObjectFab(this, this, R.id.fab1)
            fab2 = ObjectFab(this, this, R.id.fab2)
            m.findItem(R.id.action_img1).isVisible = false
            m.findItem(R.id.action_img2).isVisible = false
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
        } else {
            m.findItem(R.id.action_multipane).isVisible = false
        }
        miStatus = m.findItem(R.id.action_status)
        miStatus.title = "in through"
        model = Utility.readPref(this, prefModel, model)
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        spTime.setOnItemSelectedListener(this)
        displayData = DisplayData(this, this, this, numPanes, spTime)
        displayData.param[0] = "sfc_prec"
        if (numPanes > 1) displayData.param[1] = "500_spd"
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spRun.setOnItemSelectedListener(this)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, listOf())
        spSector.setOnItemSelectedListener(this)
        sector = Utility.readPref(this, prefSector, "CENT-US")
        spSector.setSelection(sector)
        spModel = ObjectSpinner(this, this, R.id.spinner_model, UtilityModelNCEPInterface.MODELS_ARR)
        spModel.setOnItemSelectedListener(this)
        if (model != "NAM-HIRES") {
            spModel.setSelection(model)
        } else {
            spModel.setSelection("NAM4KM")
        }
        (0 until 24).forEach { spRun.add(String.format(Locale.US, "%02d", it) + "Z") }
        (0 until 16).forEach { spRun.add(String.format(Locale.US, "%03d", it) + "00") }
        setupListRunZ(4)
        spRun.notifyDataSetChanged()
        spRun.setSelection(0)
        spTime.setSelection(0)
        drw = ObjectNavDrawer(this, UtilityModelNCEPInterface.MODEL_HRRR_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_HRRR_PARAMS)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(position)
            displayData.paramLabel[curImg] = drw.getLabel(position)
            for (i in 0 until numPanes) {
                Utility.writePref(this, "MODEL_NCEP" + numPanes.toString() + "_PARAM_LAST_USED" + i.toString(), displayData.param[i])
                Utility.writePref(this, "MODEL_NCEP" + numPanes.toString() + "_PARAM_LAST_USED_LABEL" + i.toString(), displayData.paramLabel[i])
            }
            getContent()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (parent.id == R.id.spinner_model) {
            when (parent.selectedItemPosition) {
                R.id.spinner_run,
                1 -> {
                    model = "GFS"
                    setupGFS()
                }
                2 -> {
                    model = "NAM"
                    setupModel(UtilityModelNCEPInterface.MODEL_NAM_PARAMS,
                            UtilityModelNCEPInterface.MODEL_NAM_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAM,
                            0, 85, 3, 4)

                }
                4 -> {
                    model = "RAP"
                    setupModel(UtilityModelNCEPInterface.MODEL_RAP_PARAMS,
                            UtilityModelNCEPInterface.MODEL_RAP_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_RAP,
                            0, 39, 1, 24)
                }
                0 -> {
                    model = "HRRR"
                    setupModel(UtilityModelNCEPInterface.MODEL_HRRR_PARAMS,
                            UtilityModelNCEPInterface.MODEL_HRRR_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRRR,
                            0, 18, 1, 24)
                }
                3 -> {
                    model = "NAM-HIRES"
                    setupModel(UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS,
                            UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAM_4_KM,
                            0, 60, 1, 4)
                }
                5 -> {
                    model = "HRW-NMMB"
                    setupModel(UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS,
                            UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRW_NMM,
                            0, 49, 1, 2)
                }
                6 -> {
                    model = "HRW-ARW"
                    setupModel(UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS,
                            UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRW_NMM,
                            0, 49, 1, 2)
                }
                7 -> {
                    model = "GEFS-SPAG"
                    setupModel(UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS,
                            UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_GEFS_SPAG,
                            0, 385, 6, 4)
                }
                8 -> {
                    model = "GEFS-MEAN-SPRD"
                    setupModel(UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS,
                            UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_GEFS_MNSPRD,
                            0, 385, 6, 4)
                }
                9 -> {
                    model = "SREF"
                    setupModel(UtilityModelNCEPInterface.MODEL_SREF_PARAMS,
                            UtilityModelNCEPInterface.MODEL_SREF_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_SREF,
                            0, 88, 3, 5)
                }
                10 -> {
                    model = "NAEFS"
                    setupModel(UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS,
                            UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAEFS,
                            6, 385, 6, 4)
                }
                11 -> {
                    model = "POLAR"
                    setupModel(UtilityModelNCEPInterface.MODEL_POLAR_PARAMS,
                            UtilityModelNCEPInterface.MODEL_POLAR_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_POLAR,
                            24, 385, 24, 1)
                }
                12 -> {
                    model = "WW3"
                    setupModel(UtilityModelNCEPInterface.MODEL_WW_3_PARAMS,
                            UtilityModelNCEPInterface.MODEL_WW_3_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3,
                            0, 127, 6, 4)
                }
                13 -> {
                    model = "WW3-ENP"
                    setupModel(UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS,
                            UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3_ENP,
                            0, 127, 6, 4)
                }
                14 -> {
                    model = "WW3-WNA"
                    setupModel(UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS,
                            UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3_WNA,
                            0, 127, 6, 4)
                }
                15 -> {
                    model = "ESTOFS"
                    setupModel(UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS,
                            UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_ESTOFS,
                            0, 181, 1, 4)
                }
                16 -> {
                    model = "FIREWX"
                    setupModel(UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS,
                            UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS_LABELS,
                            UtilityModelNCEPInterface.LIST_SECTOR_ARR_FIREWX,
                            0, 37, 1, 4)
                }
            }
            Utility.writePref(this, prefModel, model)
            getRunStatus()
        } else {
            if (firstRunTimeSet) {
                getContent()
            } else {
                when (parent.id) {
                    R.id.spinner_run -> if (!spinnerRunRan) spinnerRunRan = true
                    R.id.spinner_time -> if (!spinnerTimeRan) spinnerTimeRan = true
                    R.id.spinner_sector -> if (!spinnerSectorRan) spinnerSectorRan = true
                }
            }
        }
        if (parent.id == R.id.spinner_run) {
            UtilityModels.updateTime(spRun.selectedItem.toString(), rtd.mostRecentRun, spTime.list, spTime.arrayAdapter, "", true)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun getContent() = GlobalScope.launch(uiDispatcher) {

        run = spRun.selectedItem.toString()
        time = spTime.selectedItem.toString()
        sector = spSector.selectedItem.toString()
        model = spModel.selectedItem.toString()

        withContext(Dispatchers.IO) {
            if (model == "NAM4KM") model = "NAM-HIRES"
            if (model.contains("HRW") && model.contains("-AK")) model = model.replace("-AK", "")
            if (model.contains("HRW") && model.contains("-PR")) model = model.replace("-PR", "")
            time = if (model != "HRRR")
                UtilityStringExternal.truncate(time, 3)
            else
                UtilityStringExternal.truncate(time, 5)
            Utility.writePref(contextg, "MODEL_NCEP" + numPanes.toString() + "_SECTOR_LAST_USED", sector)
            (0 until numPanes).forEach { displayData.bitmap[it] = UtilityModelNCEPInputOutput.getImage(model, sector, displayData.param[it], run, time) }
        }

        (0 until numPanes).forEach {
            displayData.img[it].visibility = View.VISIBLE
            if (numPanes > 1)
                UtilityImg.resizeViewSetImgByHeight(displayData.bitmap[it], displayData.img[it])
            else
                displayData.img[it].setImageBitmap(displayData.bitmap[it])
            displayData.img[it].setMaxZoom(4f)
        }
        animRan = false
        if (!firstRun) {
            (0 until numPanes).forEach { UtilityImg.imgRestorePosnZoom(contextg, displayData.img[it], "NCEP" + numPanes.toString() + it.toString()) }
            if (UIPreferences.fabInModels && numPanes < 2) {
                fab1.setVisibility(View.VISIBLE)
                fab2.setVisibility(View.VISIBLE)
            }
            firstRun = true
        }
        if (numPanes > 1)
            UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
        else
            UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0])
        imageLoaded = true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(spTime)
            R.id.action_forward -> UtilityModels.moveForward(spTime)
            R.id.action_animate -> getAnimate()
            R.id.action_img1 -> {
                curImg = 0
                UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
            }
            R.id.action_img2 -> {
                curImg = 1
                UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
            }
            R.id.action_multipane -> ObjectIntent(this, ModelsNCEPActivity::class.java, ModelsNCEPActivity.INFO, arrayOf("2", intentArg))
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
                        UtilityShare.shareAnimGif(this, model + " " + displayData.paramLabel[0] + " " + spTime.selectedItem.toString(), displayData.animDrawable[0])
                    else
                        UtilityShare.shareBitmap(this, model + " " + displayData.paramLabel[0] + " " + spTime.selectedItem.toString(), displayData.bitmap[0])
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        val spinnerTimeValue = spTime.selectedItemPosition
        withContext(Dispatchers.IO) {
            (0 until numPanes).forEach { displayData.animDrawable[it] = UtilityModelNCEPInputOutput.getAnimation(contextg, model, sector, displayData.param[it], run, spinnerTimeValue, spTime.list) }
        }
        (0 until numPanes).forEach { UtilityImgAnim.startAnimation(displayData.animDrawable[it], displayData.img[it]) }
        animRan = true
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        //val spinnerSectorFirst = spSector.getItemAtPosition(0).toString()
        rtd = withContext(Dispatchers.IO) { UtilityModelNCEPInputOutput.getRunTime(model, displayData.param[0], sector) }
        time = rtd.mostRecentRun
        spRun.notifyDataSetChanged()
        spRun.setSelection(rtd.mostRecentRun)
        if (model == "CFS" && 0 == spRun.selectedItemPosition) {
            getContent()
        }
        title = rtd.imageCompleteStr
        miStatus.title = "in through " + rtd.imageCompleteStr
        var tmpStr: String
        (0 until spTime.size()).forEach {
            tmpStr = MyApplication.space.split(spTime[it])[0]
            spTime[it] = tmpStr + " " + UtilityModels.convertTimeRuntoTimeString(rtd.mostRecentRun.replace("Z", ""), tmpStr, true)
        }
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            spTime.setSelection(Utility.readPref(contextg, prefRunPosn, 1))
        }
        spTime.notifyDataSetChanged()
    }

    private fun addItemsOnSpinnerSectors(listSectorArr: List<String>) {
        spSector.refreshData(this, listSectorArr)
        sector = Utility.readPref(this, prefSector, "CENT-US")
        spSector.setSelection(sector)
    }

    private fun setupGFS() {
        (0 until numPanes).forEach {
            displayData.param[it] = "500_vort_ht"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "500mb Vorticity, Wind, and Height"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_GFS_PARAMS, displayData.param[0])) {
            displayData.param[0] = "500_vort_ht"
            displayData.paramLabel[0] = "500mb Vorticity, Wind, and Height"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_GFS_PARAMS, displayData.param[1])) {
                displayData.param[1] = "500_vort_ht"
                displayData.paramLabel[1] = "500mb Vorticity, Wind, and Height"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_GFS)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_GFS_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_GFS_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ(4)
        spTime.clear()
        (0..241 step 3).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
        (252..385 step 12).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupListRunZ(numberRuns: Int) {
        spRun.clear()
        when (numberRuns) {
            1 -> spRun.add("00Z")
            2 -> {
                spRun.add("00Z")
                spRun.add("12Z")
            }
            4 -> {
                spRun.add("00Z")
                spRun.add("06Z")
                spRun.add("12Z")
                spRun.add("18Z")
            }
            5 -> {  // FIXME use enum
                spRun.add("03Z")
                spRun.add("09Z")
                spRun.add("15Z")
                spRun.add("21Z")
            }
            24 -> (0..23).forEach { spRun.add(String.format(Locale.US, "%02d", it) + "Z") }
        }
        spRun.notifyDataSetChanged()
    }

    private fun setupModel(params: List<String>, labels: List<String>, sectors: List<String>, startStepTime: Int, endStepTime: Int, stepAmount: Int, numberRuns: Int) {
        (0 until numPanes).forEach {
            displayData.param[it] = params[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = params[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(params, displayData.param[0])) {
            displayData.param[0] = params[0]
            displayData.paramLabel[0] = labels[0]
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(params, displayData.param[1])) {
                displayData.param[1] = params[0]
                displayData.paramLabel[1] = labels[0]
            }
        addItemsOnSpinnerSectors(sectors)
        drw.updateLists(this, labels, params)
        spRun.setSelection(0)
        spTime.setSelection(1)
        setupListRunZ(numberRuns)
        spTime.clear()
        when (model) {
            "HRRR" -> (startStepTime..endStepTime step stepAmount).forEach { spTime.add(String.format(Locale.US, "%03d" + "00", it)) }
            "GEFS-SPAG", "GEFS-MEAN-SPRD" -> {
                (0..181 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
                (192..385 step 12).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
            }
            else -> (startStepTime..endStepTime step stepAmount).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
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
            (0 until numPanes).forEach { UtilityImg.imgSavePosnZoom(this, displayData.img[it], "NCEP" + numPanes.toString() + it.toString()) }
            Utility.writePref(this, prefRunPosn, spTime.selectedItemPosition)
        }
        super.onStop()
    }
}

