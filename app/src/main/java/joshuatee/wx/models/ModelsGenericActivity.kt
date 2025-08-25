/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.os.Bundle
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import androidx.core.view.GravityCompat
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.common.RegExp
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.Fab
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityString

class ModelsGenericActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // This code provides a native android interface to Weather Models
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup
    // arg3 - title string
    //

    companion object {
        const val INFO = ""
    }

    private var fab1: Fab? = null
    private var fab2: Fab? = null
    private val overlayImg = mutableListOf<String>()
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var om: ObjectModel
    private lateinit var objectModelLayout: ObjectModelLayout
    private lateinit var navDrawer: NavDrawer
    private lateinit var timeMenuItem: MenuItem
    private lateinit var runMenuItem: MenuItem
    private var firstRunTimeSet = false
    private var prefModelToken = ""
    private var titleString = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.models_generic_top, menu)
        if (om.prefModel == "SPCHRRR") {
            menu.findItem(R.id.action_model).isVisible = false
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_region).title = om.sector
        menu.findItem(R.id.action_model).title = om.model
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        val arguments = intent.getStringArrayExtra(INFO)
        val numberOfPanes: String
        // Keep for static pinned shortcuts
        if (arguments == null) {
            numberOfPanes = "1"
            prefModelToken = "NCEP"
            titleString = "NCEP"
        } else {
            numberOfPanes = arguments[0]
            prefModelToken = arguments[1]
            titleString = arguments[2]
        }
        title = titleString
        objectModelLayout = ObjectModelLayout(prefModelToken)
        if (To.int(numberOfPanes) == 1) {
            super.onCreate(
                savedInstanceState,
                objectModelLayout.layoutSinglePane,
                objectModelLayout.menuResId,
                bottomToolbar = true
            )
        } else {
            super.onCreate(
                savedInstanceState,
                objectModelLayout.layoutMultiPane,
                objectModelLayout.menuResId,
                bottomToolbar = true
            )
            val box = VBox.fromResource(this)
            if (UtilityUI.isLandScape(this)) {
                box.makeHorizontal()
            }
        }
        om = ObjectModel(this, prefModelToken, numberOfPanes)
        setupUI()
        setupModel()
    }

    private fun setupUI() {
        objectToolbarBottom.connect(this)
        if (om.modelType == ModelType.SPCHRRR) {
            overlayImg.addAll(
                listOf(
                    *TextUtils.split(
                        Utility.readPref(this, "SPCHRRR_OVERLAY", ""),
                        ":"
                    )
                )
            )
        }
        with(objectToolbarBottom) {
            timeMenuItem = find(R.id.action_time)
            runMenuItem = find(R.id.action_run)
            miStatusParam1 = find(R.id.action_status_param1)
            miStatusParam2 = find(R.id.action_status_param2)
        }
        fab1 = Fab(this, R.id.fab1) { om.leftClick() }
        fab2 = Fab(this, R.id.fab2) { om.rightClick() }
        if (om.numPanes < 2) {
            objectToolbarBottom.hide(R.id.action_img1)
            objectToolbarBottom.hide(R.id.action_img2)
            miStatusParam2.isVisible = false
        } else {
            objectToolbarBottom.hide(R.id.action_multipane)
        }
        miStatus = objectToolbarBottom.find(R.id.action_status)
        miStatus.title = "in through"
        navDrawer = NavDrawer(this, om.labels, om.params)
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        navDrawer.connect(::navDrawerSelected)
    }

    private fun navDrawerSelected(position: Int) {
        om.displayData.param[om.curImg] = navDrawer.tokens[position]
        om.displayData.paramLabel[om.curImg] = navDrawer.getLabel(position)
        getContent()
    }

    private fun getContent() {
        UtilityModels.getContent(this, om, overlayImg)
        updateMenuTitles()
    }

    private fun setupModel() {
        if (om.modelType == ModelType.SPCHRRR) {
            with(om.displayData) {
                (0 until om.numPanes).forEach {
                    param[it] = om.params[0]
                    param[it] = Utility.readPref(
                        this@ModelsGenericActivity,
                        om.prefParam + it.toString(),
                        param[it]
                    )
                    paramLabel[it] = om.labels[0]
                    paramLabel[it] = Utility.readPref(
                        this@ModelsGenericActivity,
                        om.prefParamLabel + it.toString(),
                        paramLabel[it]
                    )
                    if (!om.params.contains(param[it])) {
                        param[it] = om.params[0]
                        paramLabel[it] = om.labels[0]
                    }
                }
            }
            FutureVoid(::runStatusDownload, ::runStatusUpdate)
        } else {
            val modelPosition = Utility.readPrefInt(this, om.prefModelIndex, 0)
            om.setParams(modelPosition)
            with(om.displayData) {
                (0 until om.numPanes).forEach {
                    param[it] = om.params[0]
                    param[it] = Utility.readPref(
                        this@ModelsGenericActivity,
                        om.prefParam + it.toString(),
                        param[0]
                    )
                    paramLabel[it] = om.params[0]
                    paramLabel[it] = Utility.readPref(
                        this@ModelsGenericActivity,
                        om.prefParamLabel + it.toString(),
                        paramLabel[0]
                    )
                }
                if (!om.params.contains(param[0])) {
                    param[0] = om.params[0]
                    paramLabel[0] = om.labels[0]
                }
                if (om.numPanes > 1)
                    if (!om.params.contains(param[1])) {
                        param[1] = om.params[0]
                        paramLabel[1] = om.labels[0]
                    }
            }
            navDrawer.updateLists(om.labels, om.params)
            when (om.modelType) {
                ModelType.NCEP -> setupListRunZ(om.numberRuns)
                else -> {}
            }
            if (!om.sectors.contains(om.sector)) {
                om.sector = om.sectors[0]
            }
            om.times.clear()
            when (om.modelType) {
                ModelType.NCEP -> {
                    when (om.model) {
                        "HRRR" -> {
                            (om.startStep..om.endStep step om.stepAmount).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3) + "00")
                            }
                        }

                        "GEFS-SPAG", "GEFS-MEAN-SPRD" -> {
                            (0..181 step 6).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                            (192..385 step 12).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                        }

                        "GFS" -> {
                            (0..241 step 3).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                            (252..385 step 12).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                        }

                        "GEFS-WAVE" -> {
                            (0..239 step 3).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                            (240..384 step 6).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                        }

                        "GFS-WAVE" -> {
                            (0..71 step 3).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                            (72..180 step 6).forEach {
                                om.times.add(To.stringPadLeftZeros(it, 3))
                            }
                        }

                        else -> (om.startStep..om.endStep step om.stepAmount).forEach {
                            om.times.add(String.format(Locale.US, om.format, it))
                        }
                    }
                }

                else -> (om.startStep..om.endStep step om.stepAmount).forEach {
                    om.times.add(String.format(Locale.US, om.format, it))
                }
            }
            Utility.writePref(this, om.prefModel, om.model)
            FutureVoid(::runStatusDownload, ::runStatusUpdate)
        }
    }

    private fun runStatusDownload() {
        if (om.modelType == ModelType.NCEP) {
            om.rtd = UtilityModelNcepInputOutput.getRunTime(
                om.model,
                om.displayData.param[0],
                om.sectors[0]
            )
        } else {
            om.rtd = ObjectModelGet.runTime(om)
        }
    }

    private fun runStatusUpdate() {
        if (om.modelType == ModelType.SPCHRRR) {
            om.run = om.rtd.mostRecentRun
            miStatus.isVisible = true
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
            (om.startStep until om.endStep).forEach {
                om.times.add(To.stringPadLeftZeros(it, 2))
            }
            UtilityModels.updateTime(
                UtilityString.getLastXChars(om.run, 2),
                om.rtd.mostRecentRun,
                om.times,
                "",
                false
            )
            om.setTimeIdx(Utility.readPrefInt(this, om.prefRunPosn, 1))
            getContent()
        } else {
            if (om.modelType == ModelType.NCEP) {
                om.run = om.rtd.mostRecentRun
                om.rtd.listRun = om.ncepRuns
                miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
                (0 until om.times.size).forEach {
                    val items = RegExp.space.split(om.times[it])[0]
                    om.times[it] = "$items " + UtilityModels.convertTimeRunToTimeString(
                        om.rtd.mostRecentRun.replace(
                            "Z",
                            ""
                        ), items, true
                    )
                }
            } else {
                om.run = om.rtd.mostRecentRun
                miStatus.isVisible = true
                miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
                (0 until om.times.size).forEach {
                    om.times[it] = om.times[it] + " " + UtilityModels.convertTimeRunToTimeString(
                        om.rtd.mostRecentRun.replace("Z", ""),
                        om.times[it],
                        false
                    )
                }
            }
            if (!firstRunTimeSet) {
                firstRunTimeSet = true
                om.setTimeIdx(Utility.readPrefInt(this, om.prefRunPosn, 1))
            } else {
                om.setTimeIdx(1)
            }
            getContent()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            // SPCHRRR
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
                UtilityModels.getContent(this, om, overlayImg)
            }
            // SPCHRRR END
            R.id.action_time -> ObjectDialogue.generic(
                this,
                om.times,
                ::getContent
            ) { om.setTimeIdx(it) }

            R.id.action_run -> ObjectDialogue.generic(this, om.rtd.listRun, ::getContent) {
                om.run = om.rtd.listRun[it]
            }

            R.id.action_animate -> UtilityModels.getAnimate(om, listOf(""))
            R.id.action_img1 -> {
                om.curImg = 0
                om.setSubtitleRestoreZoom()
            }

            R.id.action_img2 -> {
                om.curImg = 1
                om.setSubtitleRestoreZoom()
            }

            R.id.action_multipane -> Route.model(this, "2", prefModelToken, titleString)
            R.id.action_share -> if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                checkOverlayPerms()
            } else {
                UtilityModels.legacyShare(this, om)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_region -> ObjectDialogue.generic(this, om.sectors, ::getContent) {
                om.sector = om.sectors[it]
//                om.sectorInt = it
                om.displayData.image.forEach { touchImage -> touchImage.resetZoom() }
            }

            R.id.action_model -> ObjectDialogue.generic(this, om.models, {}) {
                om.model = om.models[it]
                Utility.writePref(this, om.prefModel, om.model)
                Utility.writePrefInt(this, om.prefModelIndex, it)
                setupModel()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // SPCHRRR ONLY
    private fun overlaySelected(overlay: String) {
        if (overlayImg.contains(overlay)) {
            overlayImg.remove(overlay)
        } else {
            overlayImg.add(overlay)
        }
        getContent()
    }

    private fun updateMenuTitles() {
        invalidateOptionsMenu()
        timeMenuItem.title = om.getTimeLabel()
        runMenuItem.title = om.run
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        if (om.modelType == ModelType.SPCHRRR) {
            Utility.writePref(this, "SPCHRRR_OVERLAY", TextUtils.join(":", overlayImg))
        }
        om.imgSavePosnZoom()
        super.onStop()
    }

    private fun setupListRunZ(numberRuns: Int) {
        om.ncepRuns.clear()
        when (numberRuns) {
            1 -> om.ncepRuns.add("00Z")
            2 -> {
                om.ncepRuns.add("00Z")
                om.ncepRuns.add("12Z")
            }

            4 -> {
                om.ncepRuns.add("00Z")
                om.ncepRuns.add("06Z")
                om.ncepRuns.add("12Z")
                om.ncepRuns.add("18Z")
            }

            5 -> {
                om.ncepRuns.add("03Z")
                om.ncepRuns.add("09Z")
                om.ncepRuns.add("15Z")
                om.ncepRuns.add("21Z")
            }

            24 -> (0..23).forEach {
                om.ncepRuns.add(To.stringPadLeftZeros(it, 2) + "Z")
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_J -> if (event.isCtrlPressed) om.leftClick()
            KeyEvent.KEYCODE_K -> if (event.isCtrlPressed) om.rightClick()
            KeyEvent.KEYCODE_D -> if (event.isCtrlPressed) navDrawer.openGravity(GravityCompat.START)
            else -> super.onKeyUp(keyCode, event)
        }
        return true
    }
}
