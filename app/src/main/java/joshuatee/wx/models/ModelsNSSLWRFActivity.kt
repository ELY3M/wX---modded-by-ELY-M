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
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsNSSLWRFActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // This code provides a native android interface to Weather Models @ https://ensemble.ucar.edu/index.php
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - show map - "true" or "false"
    // arg3 - pref model token and hash lookup
    // arg4 - title string

    companion object {
        const val INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var run = "00Z"
    private var time = "00"
    private var firstRun = false
    private var imageLoaded = false
    private var sector = ""
    private var firstRunTimeSet = false
    private var numPanes = 1
    private var numPanesStr = "1"
    private var curImg = 0
    private var model = "WRF"
    private var prefModel = ""
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var turl: Array<String>
    private lateinit var miStatus: MenuItem
    private lateinit var spRun: ObjectSpinner
    private lateinit var spTime: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private lateinit var drw: ObjectNavDrawer
    private var prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
    private var prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
    private var prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
    private var prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
    private var modelProvider = "MODEL_$prefModel$numPanesStr"
    private var rtd = RunTimeData()
    private lateinit var displayData: DisplayData
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        turl = intent.getStringArrayExtra(INFO)
        contextg = this
        numPanesStr = turl[0]
        numPanes = numPanesStr.toIntOrNull() ?: 0
        if (numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_nssl, R.menu.models_generic, false, true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_nssl_multipane, R.menu.models_generic, false, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        prefModel = turl[2]
        model = Utility.readPref(this, prefModel, "WRF")
        prefSector = "MODEL_" + prefModel + numPanesStr + "_SECTOR_LAST_USED"
        prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel$numPanesStr"
        title = turl[3]
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
        m.findItem(R.id.action_map).isVisible = false
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        spTime.setOnItemSelectedListener(this)
        displayData = DisplayData(this, this, this, numPanes, spTime)
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spRun.setOnItemSelectedListener(this)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, UtilityModelNSSLWRFInterface.sectorsLong)
        spSector.setOnItemSelectedListener(this)
        spSector.setSelection(Utility.readPref(this, prefSector, UtilityModelNSSLWRFInterface.sectorsLong[0]))
        val spModel = ObjectSpinner(this, this, R.id.spinner_model, UtilityModelNSSLWRFInterface.models)
        spModel.setOnItemSelectedListener(this)
        spModel.setSelection(model)
        drw = ObjectNavDrawer(this, UtilityModelNSSLWRFInterface.labelsNsslWrf, UtilityModelNSSLWRFInterface.paramsNsslWrf)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(position)
            displayData.paramLabel[curImg] = drw.getLabel(position)
            (0 until numPanes).forEach {
                Utility.writePref(this, prefParam + it.toString(), displayData.param[it])
                Utility.writePref(this, prefParamLabel + it.toString(), displayData.paramLabel[it])
            }
            getContent()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (parent.id == R.id.spinner_model) {
            firstRunTimeSet = false
            when (parent.selectedItemPosition) {
                0 -> {
                    model = "WRF"
                    Utility.writePref(this, prefModel, model)
                    setupModel(UtilityModelNSSLWRFInterface.paramsNsslWrf, UtilityModelNSSLWRFInterface.labelsNsslWrf, 1, 36)
                }
                1 -> {
                    model = "FV3"
                    Utility.writePref(this, prefModel, model)
                    setupModel(UtilityModelNSSLWRFInterface.paramsNsslFv3, UtilityModelNSSLWRFInterface.labelsNsslFv3, 1, 60)
                }
                2 -> {
                    model = "HRRRV3"
                    Utility.writePref(this, prefModel, model)
                    setupModel(UtilityModelNSSLWRFInterface.paramsNsslHrrrv3, UtilityModelNSSLWRFInterface.labelsNsslHrrrv3, 1, 36)
                }
                3 -> {
                    model = "WRF_3KM"
                    Utility.writePref(this, prefModel, model)
                    setupModel(UtilityModelNSSLWRFInterface.paramsNsslWrf, UtilityModelNSSLWRFInterface.labelsNsslWrf, 1, 36)
                }
            }
            getRunStatus()
        } else if (firstRunTimeSet) {
            getContent()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        run = spRun.selectedItem.toString()
        time = spTime.selectedItem.toString()
        sector = spSector.selectedItem.toString()
        Utility.writePref(contextg, prefSector, sector)

        withContext(Dispatchers.IO) {
            (0 until numPanes).forEach { displayData.bitmap[it] = UtilityModelNSSLWRFInputOutput.getImage(contextg, model, sector, displayData.param[it], run, time) }
        }

        (0 until numPanes).forEach {
            if (numPanes > 1)
                UtilityImg.resizeViewSetImgByHeight(displayData.bitmap[it], displayData.img[it])
            else
                displayData.img[it].setImageBitmap(displayData.bitmap[it])
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
        if (numPanes > 1) {
            UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
        } else {
            toolbar.subtitle = displayData.paramLabel[0]
        }
        imageLoaded = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
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
            R.id.action_multipane -> ObjectIntent(this, ModelsNSSLWRFActivity::class.java, ModelsNSSLWRFActivity.INFO, arrayOf("2", turl[1], turl[2], turl[3]))
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
                        UtilityShare.shareAnimGif(this, prefModel + " " + displayData.paramLabel[0] + " " + spTime.selectedItem.toString(), displayData.animDrawable[0])
                    else
                        UtilityShare.shareBitmap(this, prefModel + " " + displayData.paramLabel[0] + " " + spTime.selectedItem.toString(), displayData.bitmap[0])
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        val spinnerTimeValue = spTime.selectedItemPosition
        withContext(Dispatchers.IO) {
            (0 until numPanes).forEach { displayData.animDrawable[it] = UtilityModelNSSLWRFInputOutput.getAnimation(contextg, model, sector, displayData.param[it], run, spinnerTimeValue, spTime.list) }
        }

        (0 until numPanes).forEach { UtilityImgAnim.startAnimation(displayData.animDrawable[it], displayData.img[it]) }
        animRan = true
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        rtd = withContext(Dispatchers.IO) { UtilityModelNSSLWRFInputOutput.runTime }
        spRun.clear()
        spRun.addAll(rtd.listRun)
        miStatus.isVisible = false
        spRun.notifyDataSetChanged()
        (0 until spTime.size()).forEach { spTime[it] = spTime[it] + " " + UtilityModels.convertTimeRuntoTimeString(rtd.timeStrConv.replace("Z", ""), spTime[it], false) }
        spTime.notifyDataSetChanged()
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            spTime.setSelection(Utility.readPref(contextg, prefRunPosn, 1))
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
            (0 until numPanes).forEach { UtilityImg.imgSavePosnZoom(this, displayData.img[it], modelProvider + numPanes.toString() + it.toString()) }
            Utility.writePref(this, prefRunPosn, spTime.selectedItemPosition)
        }
        super.onStop()
    }

    private fun setupModel(params: List<String>, labels: List<String>, startStep: Int, endStep: Int) {
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
        //spSector.refreshData(this, UtilityModelNSSLWRFInterface.sectorsLong)
        //spSector.setSelection(sectorOrig)
        drw.updateLists(this, labels, params)
        spRun.setSelection(0)
        spTime.setSelection(0)
        //spRun.list.clear()
        //(0..23).forEach { spRun.list.add(String.format(Locale.US, "%02d", it) + "Z") }
        spTime.list.clear()
        (startStep..endStep).forEach { spTime.list.add(String.format(Locale.US, "%02d", it)) }
    }
}

