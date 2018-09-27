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
import android.os.AsyncTask
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

class ModelsNCEPActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // This code provides a native android interface to Weather Models @ NCEP
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        var INFO = ""
    }

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
        setupListRunZ()
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
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (parent.id == R.id.spinner_model) {
            when (parent.selectedItemPosition) {
                R.id.spinner_run, 1 -> {
                    model = "GFS"
                    Utility.writePref(this, prefModel, model)
                    setupGFS()
                }
                2 -> {
                    model = "NAM"
                    Utility.writePref(this, prefModel, model)
                    setupNAM()
                }
                4 -> {
                    model = "RAP"
                    Utility.writePref(this, prefModel, model)
                    setupRAP()
                }
                0 -> {
                    model = "HRRR"
                    Utility.writePref(this, prefModel, model)
                    setupHRRR()
                }
                3 -> {
                    model = "NAM-HIRES"
                    Utility.writePref(this, prefModel, model)
                    setupNAM4KM()
                }
                5 -> {
                    model = "HRW-NMMB"
                    Utility.writePref(this, prefModel, model)
                    setupHRWNMM()
                }
                6 -> {
                    model = "HRW-ARW"
                    Utility.writePref(this, prefModel, model)
                    setupHRWNMM()
                }
                7 -> {
                    model = "GEFS-SPAG"
                    Utility.writePref(this, prefModel, model)
                    setupGEFSSPAG()
                }
                8 -> {
                    model = "GEFS-MEAN-SPRD"
                    Utility.writePref(this, prefModel, model)
                    setupGEFSMNSPRD()
                }
                9 -> {
                    model = "SREF"
                    Utility.writePref(this, prefModel, model)
                    setupSREF()
                }
                10 -> {
                    model = "NAEFS"
                    Utility.writePref(this, prefModel, model)
                    setupNAEFS()
                }
                11 -> {
                    model = "POLAR"
                    Utility.writePref(this, prefModel, model)
                    setupPOLAR()
                }
                12 -> {
                    model = "WW3"
                    Utility.writePref(this, prefModel, model)
                    setupWW3()
                }
                13 -> {
                    model = "WW3-ENP"
                    Utility.writePref(this, prefModel, model)
                    setupWW3ENP()
                }
                14 -> {
                    model = "WW3-WNA"
                    Utility.writePref(this, prefModel, model)
                    setupWW3WNA()
                }
                15 -> {
                    model = "ESTOFS"
                    Utility.writePref(this, prefModel, model)
                    setupESTOFS()
                }
                16 -> {
                    model = "FIREWX"
                    Utility.writePref(this, prefModel, model)
                    setupFIREWX()
                }
            }
            GetRunStatus().execute()
        } else {
            if (firstRunTimeSet) {
                GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {

            if (model == "NAM4KM") model = "NAM-HIRES"
            if (model.contains("HRW") && model.contains("-AK")) model = model.replace("-AK", "")
            if (model.contains("HRW") && model.contains("-PR")) model = model.replace("-PR", "")
            time = if (model != "HRRR")
                UtilityStringExternal.truncate(time, 3)
            else
                UtilityStringExternal.truncate(time, 5)
            Utility.writePref(contextg, "MODEL_NCEP" + numPanes.toString() + "_SECTOR_LAST_USED", sector)
            (0 until numPanes).forEach { displayData.bitmap[it] = UtilityModelNCEPInputOutput.getImage(model, sector, displayData.param[it], run, time) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
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

        override fun onPreExecute() {
            run = spRun.selectedItem.toString()
            time = spTime.selectedItem.toString()
            sector = spSector.selectedItem.toString()
            model = spModel.selectedItem.toString()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(spTime)
            R.id.action_forward -> UtilityModels.moveForward(spTime)
            R.id.action_animate -> GetAnimate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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

    @SuppressLint("StaticFieldLeak")
    private inner class GetAnimate : AsyncTask<String, String, String>() {

        internal var spinnerTimeValue: Int = 0

        override fun onPreExecute() {
            spinnerTimeValue = spTime.selectedItemPosition
        }

        override fun doInBackground(vararg params: String): String {
            (0 until numPanes).forEach { displayData.animDrawable[it] = UtilityModelNCEPInputOutput.getAnimation(contextg, model, sector, displayData.param[it], run, spinnerTimeValue, spTime.list) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            (0 until numPanes).forEach { UtilityImgAnim.startAnimation(displayData.animDrawable[it], displayData.img[it]) }
            animRan = true
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetRunStatus : AsyncTask<String, String, String>() {

        internal var spinnerSectorFirst = ""

        override fun onPreExecute() {
            spinnerSectorFirst = spSector.getItemAtPosition(0).toString()
        }

        override fun doInBackground(vararg params: String): String {
            rtd = UtilityModelNCEPInputOutput.getRunTime(model, displayData.param[0], spinnerSectorFirst)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            time = rtd.mostRecentRun
            spRun.notifyDataSetChanged()
            spRun.setSelection(rtd.mostRecentRun)
            if (model == "CFS" && 0 == spRun.selectedItemPosition) {
                GetContent().execute()
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
        setupListRunZ()
        spTime.clear()
        (0..241 step 3).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
        (252..385 step 12).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupListRunZ() {
        spRun.clear()
        spRun.add("00Z")
        spRun.add("06Z")
        spRun.add("12Z")
        spRun.add("18Z")
        spRun.notifyDataSetChanged()
    }

    private fun setupNAM() {
        (0 until numPanes).forEach {
            displayData.param[it] = "500_vort_ht"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "500mb Vorticity, Wind, and Height"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_NAM_PARAMS, displayData.param[0])) {
            displayData.param[0] = "500_vort_ht"
            displayData.paramLabel[0] = "500mb Vorticity, Wind, and Height"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_NAM_PARAMS, displayData.param[1])) {
                displayData.param[1] = "500_vort_ht"
                displayData.paramLabel[1] = "500mb Vorticity, Wind, and Height"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAM)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_NAM_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_NAM_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..85 step 3).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupRAP() {
        (0 until numPanes).forEach {
            displayData.param[it] = "500_vort_ht"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "500mb Vorticity, Wind, and Height"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_RAP_PARAMS, displayData.param[0])) {
            displayData.param[0] = "500_vort_ht"
            displayData.paramLabel[0] = "500mb Vorticity, Wind, and Height"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_RAP_PARAMS, displayData.param[1])) {
                displayData.param[1] = "500_vort_ht"
                displayData.paramLabel[1] = "500mb Vorticity, Wind, and Height"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_RAP)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_RAP_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_RAP_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.clear()
        for (i in (0 until 24)) {
            spRun.add(String.format(Locale.US, "%02d", i) + "Z")
        }
        spTime.clear()
        for (i in (0 until 22)) {
            spTime.add(String.format(Locale.US, "%03d", i))
        }
    }

    private fun setupGEFSSPAG() {
        (0 until numPanes).forEach {
            displayData.param[it] = "500_510_552_ht"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "500mb 510/552 Height Contours"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS, displayData.param[0])) {
            displayData.param[0] = "500_510_552_ht"
            displayData.paramLabel[0] = "500mb 510/552 Height Contours"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS, displayData.param[1])) {
                displayData.param[1] = "500_510_552_ht"
                displayData.paramLabel[1] = "500mb 510/552 Height Contours"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_GEFS_SPAG)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_GEFS_SPAG_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..181 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
        (192..385 step 12).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupGEFSMNSPRD() {
        (0 until numPanes).forEach {
            displayData.param[it] = "mslp"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Mean Sea Level Pressure"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }

        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS, displayData.param[0])) {
            displayData.param[0] = "mslp"
            displayData.paramLabel[0] = "Mean Sea Level Pressure"
        }

        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS, displayData.param[1])) {
                displayData.param[1] = "mslp"
                displayData.paramLabel[1] = "Mean Sea Level Pressure"
            }

        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_GEFS_MNSPRD)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_GEFS_MNSPRD_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..181 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
        (192..385 step 12).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupHRWNMM() {
        (0 until numPanes).forEach {
            displayData.param[it] = "sim_radar_1km"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Simulated Radar Reflectivity 1km"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS, displayData.param[0])) {
            displayData.param[0] = "sim_radar_1km"
            displayData.paramLabel[0] = "Simulated Radar Reflectivity 1km"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS, displayData.param[1])) {
                displayData.param[1] = "sim_radar_1km"
                displayData.paramLabel[1] = "Simulated Radar Reflectivity 1km"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRW_NMM)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_HRW_NMM_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(1)
        spRun.clear()
        spRun.add("00Z")
        spRun.add("12Z")
        spTime.clear()
        (0 until 49).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupNAEFS() {
        (0 until numPanes).forEach {
            displayData.param[it] = "mslp"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Mean Sea Level Pressure"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS, displayData.param[0])) {
            displayData.param[0] = "mslp"
            displayData.paramLabel[0] = "Mean Sea Level Pressure"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS, displayData.param[1])) {
                displayData.param[1] = "mslp"
                displayData.paramLabel[1] = "Mean Sea Level Pressure"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAEFS)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_NAEFS_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (6..385 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupPOLAR() {
        (0 until numPanes).forEach {
            displayData.param[it] = "ice_drift"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Polar Ice Drift"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_POLAR_PARAMS, displayData.param[0])) {
            displayData.param[0] = "ice_drift"
            displayData.paramLabel[0] = "Polar Ice Drift"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_POLAR_PARAMS, displayData.param[1])) {
                displayData.param[1] = "ice_drift"
                displayData.paramLabel[1] = "Polar Ice Drift"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_POLAR)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_POLAR_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_POLAR_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.clear()
        spRun.add("00Z")
        spTime.clear()
        (24..385 step 24).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupWW3() {
        (0 until numPanes).forEach {
            displayData.param[it] = "regional_wv_dir_per"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Significant Wave Height and Wind"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_WW_3_PARAMS, displayData.param[0])) {
            displayData.param[0] = "regional_wv_dir_per"
            displayData.paramLabel[0] = "Significant Wave Height and Wind"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_WW_3_PARAMS, displayData.param[1])) {
                displayData.param[1] = "regional_wv_dir_per"
                displayData.paramLabel[1] = "Significant Wave Height and Wind"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_WW_3_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_WW_3_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..127 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupWW3ENP() {
        (0 until numPanes).forEach {
            displayData.param[it] = "regional_wv_ht"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Regional WW3 Model Sig Wave Height and Wind"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS, displayData.param[0])) {
            displayData.param[0] = "regional_wv_ht"
            displayData.paramLabel[0] = "Regional WW3 Model Sig Wave Height and Wind"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS, displayData.param[1])) {
                displayData.param[1] = "regional_wv_ht"
                displayData.paramLabel[1] = "Regional WW3 Model Sig Wave Height and Wind"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3_ENP)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_WW_3_ENP_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..127 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupWW3WNA() {
        (0 until numPanes).forEach {
            displayData.param[it] = "regional_wv_ht"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Regional WW3 Model Sig Wave Height and Wind"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS, displayData.param[0])) {
            displayData.param[0] = "regional_wv_ht"
            displayData.paramLabel[0] = "Regional WW3 Model Sig Wave Height and Wind"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS, displayData.param[1])) {
                displayData.param[1] = "regional_wv_ht"
                displayData.paramLabel[1] = "Regional WW3 Model Sig Wave Height and Wind"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_WW_3_WNA)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_WW_3_WNA_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..127 step 6).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupHRRR() {
        (0 until numPanes).forEach {
            displayData.param[it] = "sim_radar_1km"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Simulated Reflectivity"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_HRRR_PARAMS, displayData.param[0])) {
            displayData.param[0] = "sim_radar_1km"
            displayData.paramLabel[0] = "Simulated Reflectivity"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_HRRR_PARAMS, displayData.param[1])) {
                displayData.param[1] = "sim_radar_1km"
                displayData.paramLabel[1] = "Simulated Reflectivity"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_HRRR)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_HRRR_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_HRRR_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.clear()
        for (i in (0 until 24)) {
            spRun.add(String.format(Locale.US, "%02d", i) + "Z")
        }
        spTime.clear()
        for (i in (0 until 19)) {
            spTime.add(String.format(Locale.US, "%03d", i) + "00")
        }
    }

    private fun setupNAM4KM() {
        (0 until numPanes).forEach {
            displayData.param[it] = "sim_radar_1km"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Simulated Radar Reflectivity 1km"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS, displayData.param[0])) {
            displayData.param[0] = "sim_radar_1km"
            displayData.paramLabel[0] = "Simulated Radar Reflectivity 1km"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS, displayData.param[1])) {
                displayData.param[1] = "sim_radar_1km"
                displayData.paramLabel[1] = "Simulated Radar Reflectivity 1km"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_NAM_4_KM)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_NAM_4_KM_PARAMS)
        spRun.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0..60 step 1).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
        spTime.setSelection(1)
    }

    private fun setupSREF() {
        (0 until numPanes).forEach {
            displayData.param[it] = "mslp"
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = "Mean Sea Level Pressure"
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_SREF_PARAMS, displayData.param[0])) {
            displayData.param[0] = "mslp"
            displayData.paramLabel[0] = "Mean Sea Level Pressure"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_SREF_PARAMS, displayData.param[1])) {
                displayData.param[1] = "mslp"
                displayData.paramLabel[1] = "Mean Sea Level Pressure"
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_SREF)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_SREF_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_SREF_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.clear()
        spRun.add("03Z")
        spRun.add("09Z")
        spRun.add("15Z")
        spRun.add("21Z")
        spTime.clear()
        (0..88 step 3).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupESTOFS() {
        (0 until numPanes).forEach {
            displayData.param[it] = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS_LABELS[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS, displayData.param[0])) {
            displayData.param[0] = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS[0]
            displayData.paramLabel[0] = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS_LABELS[0]
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS, displayData.param[1])) {
                displayData.param[1] = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS[1]
                displayData.paramLabel[1] = UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS_LABELS[1]
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_ESTOFS)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_ESTOFS_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0 until 181).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
    }

    private fun setupFIREWX() {
        (0 until numPanes).forEach {
            displayData.param[it] = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS_LABELS[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS, displayData.param[0])) {
            displayData.param[0] = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS[0]
            displayData.paramLabel[0] = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS_LABELS[0]
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS, displayData.param[1])) {
                displayData.param[1] = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS[1]
                displayData.paramLabel[1] = UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS_LABELS[1]
            }
        addItemsOnSpinnerSectors(UtilityModelNCEPInterface.LIST_SECTOR_ARR_FIREWX)
        drw.updateLists(this, UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS_LABELS, UtilityModelNCEPInterface.MODEL_FIREWX_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        setupListRunZ()
        spTime.clear()
        (0 until 37).forEach { spTime.add(String.format(Locale.US, "%03d", it)) }
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

