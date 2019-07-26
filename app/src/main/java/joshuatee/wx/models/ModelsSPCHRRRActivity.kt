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
import android.os.Bundle
import android.content.res.Configuration

import java.util.Locale

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsSpcHrrrActivity : VideoRecordActivity(), OnMenuItemClickListener, OnItemSelectedListener {

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
    private val overlayImg = mutableListOf<String>()
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var drw: ObjectNavDrawer
    private lateinit var om: ObjectModel
    private lateinit var activityArguments: Array<String>

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        activityArguments = intent.getStringArrayExtra(INFO)
        om = ObjectModel(this, activityArguments[1], activityArguments[0])
        if (om.numPanes == 1) {
            super.onCreate(
                    savedInstanceState,
                    R.layout.activity_models_generic,
                    R.menu.models_spchrrr,
                    iconsEvenlySpaced = false,
                    bottomToolbar = true
            )
        } else {
            super.onCreate(
                    savedInstanceState,
                    R.layout.activity_models_generic_multipane,
                    R.menu.models_spchrrr,
                    iconsEvenlySpaced = false,
                    bottomToolbar = true
            )
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = activityArguments[2]
        overlayImg.addAll(
                listOf(*TextUtils.split(
                                Utility.readPref(
                                        this,
                                        "SPCHRRR_OVERLAY",
                                        ""
                                ), ":"
                        ))
        )
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
        spSector = ObjectSpinner(
                this,
                this,
                this,
                R.id.spinner_sector,
                UtilityModelSpcHrrrInterface.sectors,
                om.sector
        )
        ObjectSpinner(this, this, this, R.id.spinner_model, om.models, om.model)
        drw = ObjectNavDrawer(
                this,
                UtilityModelSpcHrrrInterface.labels,
                UtilityModelSpcHrrrInterface.params
        )
        om.setUIElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, spRun, spSector)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            om.displayData.param[om.curImg] = drw.getToken(position)
            om.displayData.paramLabel[om.curImg] = drw.getLabel(position)
            UtilityModels.getContent(this, om, overlayImg, uiDispatcher)
        }
        setupModel()
        getRunStatus()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerRunRan && spinnerTimeRan && spinnerSectorRan) {
            UtilityModels.getContent(this, om, overlayImg, uiDispatcher)
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
                UtilityModels.getContent(this, om, overlayImg, uiDispatcher)
            }
            R.id.action_multipane -> ObjectIntent(
                    this,
                    ModelsSpcHrrrActivity::class.java,
                    INFO,
                    arrayOf("2", activityArguments[1], activityArguments[2])
            )
            R.id.action_back -> UtilityModels.moveBack(om.spTime)
            R.id.action_forward -> UtilityModels.moveForward(om.spTime)
            R.id.action_animate -> UtilityModels.getAnimate(om, overlayImg, uiDispatcher)
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this@ModelsSpcHrrrActivity,this@ModelsSpcHrrrActivity, om.animRan, om)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun overlaySelected(overlay: String) {
        if (overlayImg.contains(overlay)) {
            overlayImg.remove(overlay)
        } else {
            overlayImg.add(overlay)
        }
        UtilityModels.getContent(this@ModelsSpcHrrrActivity, om, overlayImg, uiDispatcher)
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        om.rtd = withContext(Dispatchers.IO) { om.getRunTime() }
        spRun.clear()
        spRun.addAll(om.rtd.listRun)
        spRun.notifyDataSetChanged()
        miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
        spRun.setSelection(0)
        om.spTime.setSelection(0)
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            om.spTime.setSelection(Utility.readPref(this@ModelsSpcHrrrActivity, om.prefRunPosn, 0))
        }
        om.spTime.notifyDataSetChanged()
        UtilityModels.getContent(this@ModelsSpcHrrrActivity, om, overlayImg, uiDispatcher)
    }

    private fun setupModel() {
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = om.params[0]
            om.displayData.param[it] =
                    Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[it])
            om.displayData.paramLabel[it] = om.labels[0]
            om.displayData.paramLabel[it] = Utility.readPref(
                    this,
                    om.prefParamLabel + it.toString(),
                    om.displayData.paramLabel[it]
            )
            if (!UtilityModels.parmInArray(om.params, om.displayData.param[it])) {
                om.displayData.param[it] = om.params[0]
                om.displayData.paramLabel[it] = om.labels[0]
            }
        }
        spRun.setSelection(0)
        om.spTime.setSelection(0)
        om.spTime.clear()
        (om.startStep until om.endStep).forEach {
            om.spTime.add(
                    String.format(
                            Locale.US,
                            "%02d",
                            it
                    )
            )
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
            Utility.writePref(this, "SPCHRRR_OVERLAY", TextUtils.join(":", overlayImg))
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

