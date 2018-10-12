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

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.util.*

class ModelsESRLActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // This code provides a native android interface to Weather Models @ NWS HRRR
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        const val INFO: String = ""
    }

    private lateinit var spRun: ObjectSpinner
    private lateinit var spTime: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private var animRan = false
    private var run = "00Z"
    private var time = "00"
    private var model = "HRRR"
    private var firstRun = false
    private var imageLoaded = false
    private var firstRunTimeSet = false
    private var numPanes = 0
    private var curImg = 0
    private var sectorInt = 0
    private var sectorOrig = ""
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var turl: Array<String>
    private lateinit var miStatus: MenuItem
    private var prefSector = ""
    private var prefModel = ""
    private var prefParam = ""
    private var prefParamLabel = ""
    private var prefRunPosn = ""
    private var modelProvider = ""
    private var rtd = RunTimeData()
    private lateinit var drw: ObjectNavDrawer
    private lateinit var displayData: DisplayData
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        turl = intent.getStringArrayExtra(INFO)
        contextg = this
        val numPanesStr = turl[0]
        numPanes = numPanesStr.toIntOrNull() ?: 0
        if (numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_modelsesrl, R.menu.models_esrl, false, true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_modelsesrlmultipane, R.menu.models_esrl, false, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        prefModel = turl[1] + numPanesStr
        prefSector = "MODEL_" + prefModel + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel"
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
        model = Utility.readPref(this, prefModel, "HRRR")
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        spTime.setOnItemSelectedListener(this)
        displayData = DisplayData(this, this, this, numPanes, spTime)
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spRun.setOnItemSelectedListener(this)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, UtilityModelESRLInterface.LIST_SECTOR_ARR_HRRR)
        spSector.setOnItemSelectedListener(this)
        sectorOrig = Utility.readPref(this, prefSector, "Full")
        spSector.setSelection(sectorOrig)
        val spModel = ObjectSpinner(this, this, R.id.spinner_model, UtilityModelESRLInterface.MODELS_ARR)
        spModel.setOnItemSelectedListener(this)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spModel.setSelection(model)
        drw = ObjectNavDrawer(this, UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS, UtilityModelESRLInterface.MODEL_HRRR_PARAMS)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, true)
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(position)
            displayData.paramLabel[curImg] = drw.getLabel(position)
            (0 until numPanes).forEach {
                Utility.writePref(this, prefParam + it.toString(), displayData.param[it])
                Utility.writePref(this, prefParamLabel + it.toString(), displayData.paramLabel[it])
            }
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (parent.id == R.id.spinner_model) {
            firstRunTimeSet = false
            when (parent.selectedItemPosition) {
                3 -> {
                    model = "RAP"
                    Utility.writePref(this, prefModel, model)
                    setupRAP()
                }
                4 -> {
                    model = "RAP_NCEP"
                    Utility.writePref(this, prefModel, model)
                    setupRAP()
                }
                0 -> {
                    model = "HRRR"
                    Utility.writePref(this, prefModel, model)
                    setupHRRR()
                }
                1 -> {
                    model = "HRRR_AK"
                    Utility.writePref(this, prefModel, model)
                    setupHRRRAK()
                }
                2 -> {
                    model = "HRRR_NCEP"
                    Utility.writePref(this, prefModel, model)
                    setupHRRR()
                }
            }
            GetRunStatus().execute()
        } else if (firstRunTimeSet)
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        if (parent.id == R.id.spinner_run)
            UtilityModels.updateTime(UtilityString.getLastXChars(spRun.selectedItem.toString(), 2),
                    rtd.mostRecentRun, spTime.list, spTime.arrayAdapter, "", false)

    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            run = spRun.selectedItem.toString()
            time = spTime.selectedItem.toString()
            sectorInt = spSector.selectedItemPosition
            sectorOrig = spSector.selectedItem.toString()
            time = UtilityStringExternal.truncate(time, 2)
            Utility.writePref(contextg, prefSector, sectorOrig)
        }

        override fun doInBackground(vararg params: String): String {
            (0 until numPanes).forEach {
                displayData.bitmap[it] = UtilityModelESRLInputOutput.getImage(model, sectorOrig, sectorInt, displayData.param[it], run, time)
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            Utility.writePref(contextg, prefRunPosn, spTime.selectedItemPosition)
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
                (0 until numPanes).forEach { UtilityImg.imgRestorePosnZoom(contextg, displayData.img[it], modelProvider + numPanes.toString() + it.toString()) }
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
            R.id.action_multipane -> ObjectIntent(this, ModelsESRLActivity::class.java, ModelsESRLActivity.INFO, arrayOf("2", turl[1]))
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
            (0 until numPanes).forEach {
                displayData.animDrawable[it] = UtilityModelESRLInputOutput.getAnimation(contextg, model, sectorOrig, sectorInt, displayData.param[it], run, spinnerTimeValue, spTime.list)
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            (0 until numPanes).forEach { UtilityImgAnim.startAnimation(displayData.animDrawable[it], displayData.img[it]) }
            animRan = true
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetRunStatus : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            rtd = UtilityModelESRLInputOutput.getRunTime(model)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            title = rtd.imageCompleteInt.toString()
            miStatus.title = "in through " + rtd.imageCompleteStr
            spRun.list.clear()
            spRun.list.addAll(rtd.listRun)
            spRun.notifyDataSetChanged()
            spTime.list.indices.forEach {
                spTime.list[it] = spTime.list[it] + " " + UtilityModels.convertTimeRuntoTimeString(rtd.timeStrConv.replace("Z", ""), spTime.list[it], false)
            }
            if (!firstRunTimeSet) {
                firstRunTimeSet = true
                val spinnerSetTo = Utility.readPref(contextg, prefRunPosn, 0)
                if (spTime.selectedItemId == spinnerSetTo.toLong())
                    GetContent().execute()
                else
                    spTime.setSelection(spinnerSetTo)
            }
            spTime.arrayAdapter.notifyDataSetChanged()
        }
    }

    private fun setupHRRR() {
        (0 until numPanes).forEach {
            displayData.param[it] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelESRLInterface.MODEL_HRRR_PARAMS, displayData.param[0])) {
            displayData.param[0] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS[0]
            displayData.paramLabel[0] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS[0]
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelESRLInterface.MODEL_HRRR_PARAMS, displayData.param[1])) {
                displayData.param[1] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS[0]
                displayData.paramLabel[1] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS[0]
            }
        spSector.refreshData(this, UtilityModelESRLInterface.LIST_SECTOR_ARR_HRRR)
        spSector.setSelection(sectorOrig)
        drw.updateLists(this, UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS, UtilityModelESRLInterface.MODEL_HRRR_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.list.clear()
        (0..23).forEach { spRun.list.add(String.format(Locale.US, "%02d", it) + "Z") }
        spTime.list.clear()
        (0..36).forEach { spTime.list.add(String.format(Locale.US, "%02d", it)) }
    }

    private fun setupHRRRAK() {
        (0 until numPanes).forEach {
            displayData.param[it] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelESRLInterface.MODEL_HRRR_PARAMS, displayData.param[0])) {
            displayData.param[0] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS[0]
            displayData.paramLabel[0] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS[0]
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelESRLInterface.MODEL_HRRR_PARAMS, displayData.param[1])) {
                displayData.param[1] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS[0]
                displayData.paramLabel[1] = UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS[0]
            }
        spSector.refreshData(this, UtilityModelESRLInterface.LIST_SECTOR_ARR_HRRR_AK)
        spSector.setSelection(sectorOrig)
        drw.updateLists(this, UtilityModelESRLInterface.MODEL_HRRR_PARAMS_LABELS, UtilityModelESRLInterface.MODEL_HRRR_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.list.clear()
        (0..23).forEach { spRun.list.add(String.format(Locale.US, "%02d", it) + "Z") }
        spTime.list.clear()
        (0..36).forEach { spTime.list.add(String.format(Locale.US, "%02d", it)) }
    }

    private fun setupRAP() {
        (0 until numPanes).forEach {
            displayData.param[it] = UtilityModelESRLInterface.MODEL_RAP_PARAMS[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[0])
            displayData.paramLabel[it] = UtilityModelESRLInterface.MODEL_RAP_PARAMS_LABELS[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[0])
        }
        if (!UtilityModels.parmInArray(UtilityModelESRLInterface.MODEL_RAP_PARAMS, displayData.param[0])) {
            displayData.param[0] = UtilityModelESRLInterface.MODEL_RAP_PARAMS[0]
            displayData.paramLabel[0] = UtilityModelESRLInterface.MODEL_RAP_PARAMS_LABELS[0]
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelESRLInterface.MODEL_RAP_PARAMS, displayData.param[1])) {
                displayData.param[1] = UtilityModelESRLInterface.MODEL_RAP_PARAMS[0]
                displayData.paramLabel[1] = UtilityModelESRLInterface.MODEL_RAP_PARAMS_LABELS[0]
            }
        spSector.refreshData(this, UtilityModelESRLInterface.LIST_SECTOR_ARR_RAP)
        spSector.setSelection(sectorOrig)
        drw.updateLists(this, UtilityModelESRLInterface.MODEL_RAP_PARAMS_LABELS, UtilityModelESRLInterface.MODEL_RAP_PARAMS)
        spRun.setSelection(0)
        spTime.setSelection(0)
        spRun.list.clear()
        (0..23).forEach { spRun.list.add(String.format(Locale.US, "%02d", it) + "Z") }
        spTime.list.clear()
        (0..39).forEach { spTime.list.add(String.format(Locale.US, "%02d", it)) }
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
            (0 until numPanes).forEach { UtilityImg.imgSavePosnZoom(this, displayData.img[it], modelProvider + numPanes.toString() + it.toString()) }
            Utility.writePref(this, prefRunPosn, spTime.selectedItemPosition)
        }
        super.onStop()
    }
}

