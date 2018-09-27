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
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class ModelsNSSLWRFActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // This code provides a native android interface to Weather Models @ https://ensemble.ucar.edu/index.php
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - show map - "true" or "false"
    // arg3 - pref model token and hash lookup
    // arg4 - title string

    companion object {
        const val INFO = ""
    }

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
    private var prefModel = ""
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var imageMap: ObjectImageMap
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
            super.onCreate(savedInstanceState, R.layout.activity_models_generic, R.menu.models_generic, false, true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_multipane, R.menu.models_generic, false, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        prefModel = turl[2]
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
        if (turl[1] == "false") {
            m.findItem(R.id.action_map).isVisible = false
        }
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        spTime.setOnItemSelectedListener(this)
        displayData = DisplayData(this, this, this, numPanes, spTime)
        imageMap = if (UIPreferences.fabInModels && numPanes < 2) {
            ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(displayData.img[0], fab1.fab, fab2.fab))
        } else {
            ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(displayData.img[0]))
        }
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                sector = UtilityImageMap.maptoWFO(id)
                mapSwitch(sector.toUpperCase(Locale.US))
            }

            override fun onBubbleClicked(id: Int) {}
        })
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spRun.setOnItemSelectedListener(this)
        spSector = ObjectSpinner(this, this, R.id.spinner_sector, UtilityModelNSSLWRFInterface.SECTORS)
        spSector.setOnItemSelectedListener(this)
        spSector.setSelection(Utility.readPref(this, prefSector, UtilityModelNSSLWRFInterface.SECTORS[0]))
        drw = ObjectNavDrawer(this, UtilityModelNSSLWRFInterface.LABELS, UtilityModelNSSLWRFInterface.PARAMS)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(position)
            displayData.paramLabel[curImg] = drw.getLabel(position)
            (0 until numPanes).forEach {
                Utility.writePref(this, prefParam + it.toString(), displayData.param[it])
                Utility.writePref(this, prefParamLabel + it.toString(), displayData.paramLabel[it])
            }
            GetContent().execute()
        }
        setupModel()
        GetRunStatus().execute()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (firstRunTimeSet) {
            GetContent().execute()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            run = spRun.selectedItem.toString()
            time = spTime.selectedItem.toString()
            if (prefModel == "NCAR_ENSEMBLE") {
                sector = spSector.selectedItem.toString()
            }
            Utility.writePref(contextg, prefSector, sector)
        }

        override fun doInBackground(vararg params: String): String {
            (0 until numPanes).forEach { displayData.bitmap[it] = UtilityModelNSSLWRFInputOutput.getImage(sector, displayData.param[it], run, time) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
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
            }
            imageLoaded = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_map -> imageMap.toggleMap()
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

    @SuppressLint("StaticFieldLeak")
    private inner class GetAnimate : AsyncTask<String, String, String>() {

        internal var spinnerTimeValue: Int = 0

        override fun onPreExecute() {
            spinnerTimeValue = spTime.selectedItemPosition
        }

        override fun doInBackground(vararg params: String): String {
            (0 until numPanes).forEach { displayData.animDrawable[it] = UtilityModelNSSLWRFInputOutput.getAnimation(contextg, sector, displayData.param[it], run, spinnerTimeValue, spTime.list) }
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
            rtd = UtilityModelNSSLWRFInputOutput.runTime
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            spRun.clear()
            spRun.addAll(rtd.listRun)
            miStatus.title = "in through " + rtd.imageCompleteStr
            spRun.notifyDataSetChanged()
            (0 until spTime.size()).forEach { spTime[it] = spTime[it] + " " + UtilityModels.convertTimeRuntoTimeString(rtd.timeStrConv.replace("Z", ""), spTime[it], false) }
            spTime.notifyDataSetChanged()
            if (!firstRunTimeSet) {
                firstRunTimeSet = true
                spTime.setSelection(Utility.readPref(contextg, prefRunPosn, 1))
            }
        }
    }

    private fun setupModel() {
        (0 until numPanes).forEach {
            displayData.param[it] = UtilityModelNSSLWRFInterface.PARAMS[0]
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[it])
            displayData.paramLabel[it] = UtilityModelNSSLWRFInterface.LABELS[0]
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[it])

        }
        if (!UtilityModels.parmInArray(UtilityModelNSSLWRFInterface.PARAMS, displayData.param[0])) {
            displayData.param[0] = "speed500_mean"
            displayData.paramLabel[0] = "500 mb Wind/Height"
        }
        if (numPanes > 1)
            if (!UtilityModels.parmInArray(UtilityModelNSSLWRFInterface.PARAMS, displayData.param[1])) {
                displayData.param[1] = "speed500_mean"
                displayData.paramLabel[1] = "500 mb Wind/Height"
            }
        spRun.setSelection(0)
        spTime.setSelection(0)
        spTime.clear()
        (0 until 37).forEach { spTime.add(String.format(Locale.US, "%02d", it)) }
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

    private fun mapSwitch(loc: String) {
        if (UIPreferences.fabInModels && numPanes == 1) {
            fab1.setVisibility(View.VISIBLE)
            fab2.setVisibility(View.VISIBLE)
        }
        sector = loc
        GetContent().execute()
    }
}

