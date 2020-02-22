/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import android.view.KeyEvent

import java.util.Locale

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsGenericActivity : VideoRecordActivity(), OnMenuItemClickListener,
        OnItemSelectedListener {

    // This code provides a native android interface to Weather Models
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup
    // arg3 - title string

    companion object {
        const val INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var spinnerSectorRan = false
    private var spinnerModelRan = false
    private var activityArguments: Array<String>? = arrayOf()
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var om: ObjectModel
    private lateinit var spRun: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private lateinit var drw: ObjectNavDrawer
    private var firstRunTimeSet = false

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        activityArguments = intent.getStringArrayExtra(INFO)
        if (activityArguments == null) {
            activityArguments = arrayOf("1", "NCEP", "NCEP")
        }
        om = ObjectModel(this, activityArguments!![1], activityArguments!![0])
        if (om.numPanes == 1) {
            super.onCreate(
                    savedInstanceState,
                    R.layout.activity_models_generic,
                    R.menu.models_generic,
                    iconsEvenlySpaced = false,
                    bottomToolbar = true
            )
        } else {
            super.onCreate(
                    savedInstanceState,
                    R.layout.activity_models_generic_multipane,
                    R.menu.models_generic,
                    iconsEvenlySpaced = false,
                    bottomToolbar = true
            )
            val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
            if (UtilityUI.isLandScape(this)) {
                linearLayout.orientation = LinearLayout.HORIZONTAL
            }
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = activityArguments!![2]
        val menu = toolbarBottom.menu
        miStatusParam1 = menu.findItem(R.id.action_status_param1)
        miStatusParam2 = menu.findItem(R.id.action_status_param2)
        if (om.numPanes < 2) {
            fab1 = ObjectFab(
                    this,
                    this,
                    R.id.fab1,
                    View.OnClickListener { UtilityModels.moveBack(om.spTime) })
            fab2 = ObjectFab(
                    this,
                    this,
                    R.id.fab2,
                    View.OnClickListener { UtilityModels.moveForward(om.spTime) })
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
        menu.findItem(R.id.action_map).isVisible = false
        om.spTime = ObjectSpinner(this, this, this, R.id.spinner_time)
        om.displayData = DisplayData(this, this, om.numPanes, om.spTime)
        spRun = ObjectSpinner(this, this, this, R.id.spinner_run)
        spSector = ObjectSpinner(this, this, this, R.id.spinner_sector, om.sectors)
        spSector.setSelection(om.sector)
        ObjectSpinner(this, this, this, R.id.spinner_model, om.models, om.model)
        drw = ObjectNavDrawer(this, om.labels, om.params)
        om.setUIElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, spRun, spSector)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            om.displayData.param[om.curImg] = drw.tokens[position]
            om.displayData.paramLabel[om.curImg] = drw.getLabel(position)
            UtilityModels.getContent(this, om, listOf(""), uiDispatcher)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        when (parent.id) {
            R.id.spinner_run -> if (!spinnerRunRan)
                spinnerRunRan = true
            R.id.spinner_time -> if (!spinnerTimeRan)
                spinnerTimeRan = true
            R.id.spinner_sector -> if (!spinnerSectorRan)
                spinnerSectorRan = true
        }
        if (parent.id == R.id.spinner_model) {
            spinnerRunRan = false
            spinnerSectorRan = false
            spinnerTimeRan = false
            firstRunTimeSet = false
            spinnerModelRan = true
            om.setParams(parent.selectedItemPosition)
            setupModel()
            Utility.writePref(this, om.prefModel, om.model)
            getRunStatus()
        } else if (firstRunTimeSet) { // && spinnerRunRan && spinnerTimeRan && spinnerSectorRan && spinnerModelRan
            UtilityModels.getContent(this, om, listOf(""), uiDispatcher)
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(om.spTime)
            R.id.action_forward -> UtilityModels.moveForward(om.spTime)
            R.id.action_animate -> UtilityModels.getAnimate(om, listOf(""), uiDispatcher)
            R.id.action_img1 -> {
                om.curImg = 0
                UtilityModels.setSubtitleRestoreIMGXYZOOM(
                        om.displayData.img,
                        toolbar,
                        "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1]
                )
            }
            R.id.action_img2 -> {
                om.curImg = 1
                UtilityModels.setSubtitleRestoreIMGXYZOOM(
                        om.displayData.img,
                        toolbar,
                        "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1]
                )
            }
            R.id.action_multipane -> ObjectIntent(
                    this,
                    ModelsGenericActivity::class.java,
                    INFO,
                    arrayOf("2", activityArguments!![1], activityArguments!![2])
            )
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this@ModelsGenericActivity, this@ModelsGenericActivity, om.animRan, om)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        if (om.modelType == ModelType.NCEP) {
            om.rtd = withContext(Dispatchers.IO) {
                UtilityModelNcepInputOutput.getRunTime(
                        om.model,
                        om.displayData.param[0],
                        om.sectors[0]
                )
            }
            om.time = om.rtd.mostRecentRun
            spRun.notifyDataSetChanged()
            spRun.setSelection(om.rtd.mostRecentRun)
            if (om.model == "CFS" && 0 == spRun.selectedItemPosition) {
                UtilityModels.getContent(this@ModelsGenericActivity, om, listOf(""), uiDispatcher)
            }
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
            var tmpStr: String
            (0 until om.spTime.size()).forEach {
                tmpStr = MyApplication.space.split(om.spTime[it])[0]
                om.spTime[it] = "$tmpStr " + UtilityModels.convertTimeRunToTimeString(
                        om.rtd.mostRecentRun.replace(
                                "Z",
                                ""
                        ), tmpStr, true
                )
            }
            om.spTime.notifyDataSetChanged()
            if (!firstRunTimeSet) {
                firstRunTimeSet = true
                om.spTime.setSelection(Utility.readPref(this@ModelsGenericActivity, om.prefRunPosn, 1))
            }
        } else {
            om.rtd = withContext(Dispatchers.IO) { om.getRunTime() }
            spRun.clear()
            spRun.addAll(om.rtd.listRun)
            miStatus.isVisible = true
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
            spRun.notifyDataSetChanged()
            (0 until om.spTime.size()).forEach {
                om.spTime[it] = om.spTime[it] + " " + UtilityModels.convertTimeRunToTimeString(
                        om.rtd.timeStrConv.replace("Z", ""),
                        om.spTime[it],
                        false
                )
            }
            om.spTime.notifyDataSetChanged()
            if (!firstRunTimeSet) {
                firstRunTimeSet = true
                om.spTime.setSelection(Utility.readPref(this@ModelsGenericActivity, om.prefRunPosn, 1))
            }
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

    override fun onStop() {
        if (om.imageLoaded) {
            (0 until om.numPanes).forEach {
                UtilityImg.imgSavePosnZoom(
                        this,
                        om.displayData.img[it],
                        om.modelProvider + om.numPanes.toString() + it.toString()
                )
            }
            Utility.writePref(this, om.prefRunPosn, om.spTime.selectedItemPosition)
        }
        super.onStop()
    }

    private fun setupModel() {
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = om.params[0]
            om.displayData.param[it] =
                    Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[0])
            om.displayData.paramLabel[it] = om.params[0]
            om.displayData.paramLabel[it] = Utility.readPref(
                    this,
                    om.prefParamLabel + it.toString(),
                    om.displayData.paramLabel[0]
            )
        }
        if (!UtilityModels.parmInArray(om.params, om.displayData.param[0])) {
            om.displayData.param[0] = om.params[0]
            om.displayData.paramLabel[0] = om.labels[0]
        }
        if (om.numPanes > 1)
            if (!UtilityModels.parmInArray(om.params, om.displayData.param[1])) {
                om.displayData.param[1] = om.params[0]
                om.displayData.paramLabel[1] = om.labels[0]
            }
        spSector.refreshData(this, om.sectors)
        spSector.setSelection(om.sector)
        drw.updateLists(this, om.labels, om.params)
        spRun.setSelection(0)
        when (om.modelType) {
            ModelType.NCEP -> {
                setupListRunZ(om.numberRuns)
            }
            else -> {}
        }
        om.spTime.setSelection(0)
        om.spTime.list.clear()
        when (om.modelType) {
            ModelType.GLCFS -> {
                (om.startStep..om.endStep step om.stepAmount).forEach {
                    om.spTime.list.add(
                            String.format(
                                    Locale.US,
                                    om.format,
                                    it
                            )
                    )
                }
                (51..121 step 3).forEach {
                    om.spTime.list.add(
                            String.format(
                                    Locale.US,
                                    om.format,
                                    it
                            )
                    )
                }
            }
            ModelType.NCEP -> {
                when (om.model) {
                    "HRRR" -> {
                        (om.startStep..om.endStep step om.stepAmount).forEach {
                            om.spTime.add(
                                    String.format(
                                            Locale.US,
                                            "%03d" + "00",
                                            it
                                    )
                            )
                        }
                    }
                    "GEFS-SPAG", "GEFS-MEAN-SPRD" -> {
                        (0..181 step 6).forEach {
                            om.spTime.add(
                                    String.format(
                                            Locale.US,
                                            "%03d",
                                            it
                                    )
                            )
                        }
                        (192..385 step 12).forEach {
                            om.spTime.add(
                                    String.format(
                                            Locale.US,
                                            "%03d",
                                            it
                                    )
                            )
                        }
                    }
                    "GFS" -> {
                        (0..241 step 3).forEach {
                            om.spTime.add(
                                    String.format(
                                            Locale.US,
                                            "%03d",
                                            it
                                    )
                            )
                        }
                        (252..385 step 12).forEach {
                            om.spTime.add(
                                    String.format(
                                            Locale.US,
                                            "%03d",
                                            it
                                    )
                            )
                        }
                    }
                    else -> {
                        (om.startStep..om.endStep step om.stepAmount).forEach {
                            om.spTime.list.add(
                                    String.format(Locale.US, om.format, it)
                            )
                        }
                    }
                }
            }
            else -> {
                (om.startStep..om.endStep step om.stepAmount).forEach {
                    om.spTime.list.add(
                            String.format(
                                    Locale.US,
                                    om.format,
                                    it
                            )
                    )
                }
            }
        }
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
            5 -> {
                spRun.add("03Z")
                spRun.add("09Z")
                spRun.add("15Z")
                spRun.add("21Z")
            }
            24 -> (0..23).forEach { spRun.add(String.format(Locale.US, "%02d", it) + "Z") }
        }
        spRun.notifyDataSetChanged()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_J -> {
                if (event.isCtrlPressed) {
                    UtilityModels.moveBack(om.spTime)
                }
                true
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) {
                    UtilityModels.moveForward(om.spTime)
                }
                true
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) {
                    drw.drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}

