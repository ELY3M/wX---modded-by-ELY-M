/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsSPCHREFActivity : VideoRecordActivity(), OnMenuItemClickListener,
    OnItemSelectedListener {

    companion object {
        const val INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var spRun: ObjectSpinner
    private lateinit var spSector: ObjectSpinner
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var spinnerSectorRan = false
    private var firstRunTimeSet = false
    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var contextg: Context
    private lateinit var om: ObjectModel
    private lateinit var activityArguments: Array<String>

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        contextg = this
        activityArguments = intent.getStringArrayExtra(INFO)
        om = ObjectModel(this, activityArguments[1], activityArguments[0])
        if (om.numPanes == 1) {
            super.onCreate(
                savedInstanceState,
                R.layout.activity_modelsspchref,
                R.menu.models_spchref,
                iconsEvenlySpaced = false,
                bottomToolbar = true
            )
        } else {
            super.onCreate(
                savedInstanceState,
                R.layout.activity_models_spchrefmultipane,
                R.menu.models_spchref,
                iconsEvenlySpaced = false,
                bottomToolbar = true
            )
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = activityArguments[2]
        val m = toolbarBottom.menu
        miStatusParam1 = m.findItem(R.id.action_status_param1)
        miStatusParam2 = m.findItem(R.id.action_status_param2)
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
            m.findItem(R.id.action_img1).isVisible = false
            m.findItem(R.id.action_img2).isVisible = false
            if (UIPreferences.fabInModels) {
                m.findItem(R.id.action_back).isVisible = false
                m.findItem(R.id.action_forward).isVisible = false
            }
            fab1?.setVisibility(View.GONE)
            fab2?.setVisibility(View.GONE)
            miStatusParam2.isVisible = false
        } else {
            m.findItem(R.id.action_multipane).isVisible = false
        }
        miStatus = m.findItem(R.id.action_status)
        miStatus.title = "in through"
        om.spTime = ObjectSpinner(this, this, this, R.id.spinner_time)
        om.displayData = DisplayData(this, this, om.numPanes, om.spTime)
        spRun = ObjectSpinner(this, this, this, R.id.spinner_run)
        om.sector = Utility.readPref(this, om.prefSector, "S19")
        spSector = ObjectSpinner(
            this,
            this,
            this,
            R.id.spinner_sector,
            UtilityModelSPCHREFInterface.sectorsLong,
            om.sector
        )
        UtilityModelSPCHREFInterface.createData()
        drw = ObjectNavDrawerCombo(
            this,
            UtilityModelSPCHREFInterface.groups,
            UtilityModelSPCHREFInterface.longCodes,
            UtilityModelSPCHREFInterface.shortCodes,
            this,
            ""
        )
        om.setUIElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, spRun, spSector)
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            om.displayData.param[om.curImg] = drw.getToken(groupPosition, childPosition)
            om.displayData.paramLabel[om.curImg] = drw.getLabel(groupPosition, childPosition)
            UtilityModels.getContent(this, om, listOf(""), uiDispatcher)
            true
        }
        setupModel()
        getRunStatus()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerRunRan && spinnerTimeRan && spinnerSectorRan) {
            UtilityModels.getContent(this, om, listOf(""), uiDispatcher)
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
            UtilityModels.updateTime(
                UtilityString.getLastXChars(spRun.selectedItem.toString(), 2),
                om.rtd.mostRecentRun, om.spTime.list, om.spTime.arrayAdapter, "", false
            )
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(om.spTime)
            R.id.action_forward -> UtilityModels.moveForward(om.spTime)
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
            R.id.action_animate -> UtilityModels.getAnimate(om, listOf(""), uiDispatcher)
            R.id.action_multipane -> ObjectIntent(
                this,
                ModelsSPCHREFActivity::class.java,
                ModelsSPCHREFActivity.INFO,
                arrayOf("2", activityArguments[1], activityArguments[2])
            )
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(contextg, om.animRan, om)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        om.rtd = withContext(Dispatchers.IO) { om.getRunTime() }
        spRun.clear()
        spRun.addAll(om.rtd.listRun)
        spRun.notifyDataSetChanged()
        miStatus.title = "in through " + om.rtd.imageCompleteStr
        spRun.setSelection(0)
        om.spTime.setSelection(0)
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            om.spTime.setSelection(Utility.readPref(contextg, om.prefRunPosn, 0))
        }
        om.spTime.notifyDataSetChanged()
        UtilityModels.getContent(contextg, om, listOf(""), uiDispatcher)
    }

    private fun setupModel() {
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = "500w_mean,500h_mean"
            om.displayData.param[it] =
                Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[it])
            om.displayData.paramLabel[it] = "500 mb Height/Wind"
            om.displayData.paramLabel[it] = Utility.readPref(
                this,
                om.prefParamLabel + it.toString(),
                om.displayData.paramLabel[it]
            )
            if (!UtilityModels.parmInArray(
                    UtilityModelSPCHREFInterface.params,
                    om.displayData.param[it]
                )
            ) {
                om.displayData.param[it] = "500w_mean,500h_mean"
                om.displayData.paramLabel[it] = "500 mb Height/Wind"
            }
        }
        spRun.setSelection(0)
        om.spTime.setSelection(0)
        om.spTime.clear()
        (om.startStep until om.endStep).forEach { om.spTime.add(String.format(Locale.US, "%02d", it)) }
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
}

