/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.os.Bundle
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.Menu
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.DisplayData
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg

class ModelsGenericActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // This code provides a native android interface to Weather Models
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup
    // arg3 - title string
    //

    companion object { const val INFO = "" }

    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private var arguments: Array<String>? = arrayOf()
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var om: ObjectModel
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var timeMenuItem: MenuItem
    private lateinit var runMenuItem: MenuItem
    private var firstRunTimeSet = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.models_generic_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_region).title = om.sector
        menu.findItem(R.id.action_model).title = om.model
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments = intent.getStringArrayExtra(INFO)
        // Keep for static pinned shortcuts
        if (arguments == null) {
            arguments = arrayOf("1", "NCEP", "NCEP")
        }
        title = arguments!![2]
        om = ObjectModel(this, arguments!![1], arguments!![0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_nospinner, R.menu.models_generic, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_multipane_nospinner, R.menu.models_generic, iconsEvenlySpaced = false, bottomToolbar = true)
            val box = VBox.fromResource(this)
            if (UtilityUI.isLandScape(this)) {
                box.makeHorizontal()
            }
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        timeMenuItem = menu.findItem(R.id.action_time)
        runMenuItem = menu.findItem(R.id.action_run)
        miStatusParam1 = menu.findItem(R.id.action_status_param1)
        miStatusParam2 = menu.findItem(R.id.action_status_param2)
        if (om.numPanes < 2) {
            fab1 = ObjectFab(this, R.id.fab1) { om.leftClick() }
            fab2 = ObjectFab(this, R.id.fab2) { om.rightClick() }
            menu.findItem(R.id.action_img1).isVisible = false
            menu.findItem(R.id.action_img2).isVisible = false
            if (UIPreferences.fabInModels) {
                val leftArrow = menu.findItem(R.id.action_back)
                val rightArrow = menu.findItem(R.id.action_forward)
                leftArrow.isVisible = false
                rightArrow.isVisible = false
            }
            fab1?.visibility = View.GONE
            fab2?.visibility = View.GONE
            miStatusParam2.isVisible = false
        } else {
            menu.findItem(R.id.action_multipane).isVisible = false
        }
        miStatus = menu.findItem(R.id.action_status)
        miStatus.title = "in through"
        om.displayData = DisplayData(this, this, om.numPanes, om)
        objectNavDrawer = ObjectNavDrawer(this, om.labels, om.params)
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        objectNavDrawer.connect { _, _, position, _ ->
            objectNavDrawer.setItemChecked(position, false)
            objectNavDrawer.close()
            om.displayData.param[om.curImg] = objectNavDrawer.tokens[position]
            om.displayData.paramLabel[om.curImg] = objectNavDrawer.getLabel(position)
            getContent()
        }
        setupModel()
        getRunStatus()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> om.leftClick()
            R.id.action_forward -> om.rightClick()
            R.id.action_time -> ObjectDialogue.generic(this, om.times, ::getContent) { om.setTimeIdx(it) }
            R.id.action_run -> ObjectDialogue.generic(this, om.rtd.listRun, ::getContent) { om.run = om.rtd.listRun[it] }
            R.id.action_animate -> UtilityModels.getAnimate(this ,om, listOf(""))
            R.id.action_img1 -> {
                om.curImg = 0
                UtilityModels.setSubtitleRestoreZoom(
                        om.displayData.image,
                        toolbar,
                        "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1]
                )
            }
            R.id.action_img2 -> {
                om.curImg = 1
                UtilityModels.setSubtitleRestoreZoom(
                        om.displayData.image,
                        toolbar,
                        "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1]
                )
            }
            R.id.action_multipane -> Route.model(this, arrayOf("2", arguments!![1], arguments!![2]))
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this, om.animRan, om)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_region -> ObjectDialogue.generic(this, om.sectors, ::getContent) {
                om.sector = om.sectors[it]
                om.sectorInt = it
            }
            R.id.action_model -> ObjectDialogue.generic(this, om.models, {}) {
                om.model = om.models[it]
                Utility.writePref(this, om.prefModel, om.model)
                Utility.writePrefInt(this, om.prefModelIndex, it)
                setupModel()
                getRunStatus()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getRunStatus() {
        FutureVoid(this, ::getRunStatusDownload, ::getRunStatusUpdate)
    }

    private fun getRunStatusDownload() {
        if (om.modelType == ModelType.NCEP) {
            om.rtd = UtilityModelNcepInputOutput.getRunTime(om.model, om.displayData.param[0], om.sectors[0])
        } else {
            om.rtd = om.getRunTime()
        }
    }

    private fun getRunStatusUpdate() {
        if (om.modelType == ModelType.NCEP) {
            om.run = om.rtd.mostRecentRun
            om.rtd.listRun = om.ncepRuns
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
            (0 until om.times.size).forEach {
                val items = RegExp.space.split(om.times[it])[0]
                om.times[it] = "$items " + UtilityModels.convertTimeRunToTimeString(om.rtd.mostRecentRun.replace("Z", ""), items, true)
            }
        } else {
            om.run = om.rtd.mostRecentRun
            miStatus.isVisible = true
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
            (0 until om.times.size).forEach {
                om.times[it] = om.times[it] + " " + UtilityModels.convertTimeRunToTimeString(
                        om.rtd.timeStrConv.replace("Z", ""),
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

    private fun getContent() {
        UtilityModels.getContent(this, om, listOf(""))
        updateMenuTitles()
    }

    private fun updateMenuTitles() {
        invalidateOptionsMenu()
        timeMenuItem.title = om.getTimeLabel()
        runMenuItem.title = om.run
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        if (om.imageLoaded) {
            (0 until om.numPanes).forEach {
                UtilityImg.imgSavePosnZoom(this, om.displayData.image[it], om.modelProvider + om.numPanes.toString() + it.toString())
            }
            Utility.writePrefInt(this, om.prefRunPosn, om.timeIndex)
        }
        super.onStop()
    }

    private fun setupModel() {
        val modelPosition = Utility.readPrefInt(this, om.prefModelIndex, 0)
        om.setParams(modelPosition)
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = om.params[0]
            om.displayData.param[it] = Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[0])
            om.displayData.paramLabel[it] = om.params[0]
            om.displayData.paramLabel[it] = Utility.readPref(this, om.prefParamLabel + it.toString(), om.displayData.paramLabel[0])
        }
        if (!om.params.contains(om.displayData.param[0])) {
            om.displayData.param[0] = om.params[0]
            om.displayData.paramLabel[0] = om.labels[0]
        }
        if (om.numPanes > 1)
            if (!om.params.contains(om.displayData.param[1])) {
                om.displayData.param[1] = om.params[0]
                om.displayData.paramLabel[1] = om.labels[0]
            }
        objectNavDrawer.updateLists(om.labels, om.params)
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
                            om.times.add(String.format(Locale.US, "%03d" + "00", it))
                        }
                    }
                    "GEFS-SPAG", "GEFS-MEAN-SPRD" -> {
                        (0..181 step 6).forEach {
                            om.times.add(String.format(Locale.US, "%03d", it))
                        }
                        (192..385 step 12).forEach {
                            om.times.add(String.format(Locale.US, "%03d", it))
                        }
                    }
                    "GFS" -> {
                        (0..241 step 3).forEach {
                            om.times.add(String.format(Locale.US, "%03d", it))
                        }
                        (252..385 step 12).forEach {
                            om.times.add(String.format(Locale.US, "%03d", it))
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
            24 -> (0..23).forEach { om.ncepRuns.add(String.format(Locale.US, "%02d", it) + "Z") }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_J -> {
                if (event.isCtrlPressed) om.leftClick()
                true
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) om.rightClick()
                true
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) {
                    objectNavDrawer.openGravity(GravityCompat.START)
                }
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}
