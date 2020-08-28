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
import android.content.DialogInterface
import android.os.Bundle
import android.content.res.Configuration

import java.util.Locale

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class ModelsSpcHrrrActivity : VideoRecordActivity(), OnMenuItemClickListener { // OnItemSelectedListener

    companion object { const val INFO = "" }

    private val uiDispatcher = Dispatchers.Main
    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private val overlayImg = mutableListOf<String>()
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var drw: ObjectNavDrawer
    private lateinit var om: ObjectModelNoSpinner
    private lateinit var activityArguments: Array<String>
    private lateinit var timeMenuItem: MenuItem
    private lateinit var runMenuItem: MenuItem

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.models_spchrrr_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (om.sector == "") {
            menu.findItem(R.id.action_region).title = om.sectors[0]
        } else {
            menu.findItem(R.id.action_region).title = om.sector
        }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        activityArguments = intent.getStringArrayExtra(INFO)!!
        om = ObjectModelNoSpinner(this, activityArguments[1], activityArguments[0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_nospinner, R.menu.models_spchrrr, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_multipane_nospinner, R.menu.models_spchrrr, iconsEvenlySpaced = false, bottomToolbar = true)
            val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
            if (UtilityUI.isLandScape(this)) linearLayout.orientation = LinearLayout.HORIZONTAL
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = activityArguments[2]
        overlayImg.addAll(listOf(*TextUtils.split(Utility.readPref(this, "SPCHRRR_OVERLAY", ""), ":")))
        val menu = toolbarBottom.menu
        timeMenuItem = menu.findItem(R.id.action_time)
        runMenuItem = menu.findItem(R.id.action_run)
        miStatusParam1 = menu.findItem(R.id.action_status_param1)
        miStatusParam2 = menu.findItem(R.id.action_status_param2)
        if (om.numPanes < 2) {
            fab1 = ObjectFab(this, this, R.id.fab1, View.OnClickListener { om.leftClick() })
            fab2 = ObjectFab(this, this, R.id.fab2, View.OnClickListener { om.rightClick() })
            menu.findItem(R.id.action_img1).isVisible = false
            menu.findItem(R.id.action_img2).isVisible = false
            if (UIPreferences.fabInModels) {
                menu.findItem(R.id.action_back).isVisible = false
                menu.findItem(R.id.action_forward).isVisible = false
            }
            fab1?.visibility = View.GONE
            fab2?.visibility = View.GONE
            miStatusParam2.isVisible = false
        } else {
            menu.findItem(R.id.action_multipane).isVisible = false
        }
        miStatus = menu.findItem(R.id.action_status)
        miStatus.title = "in through"
        om.displayData = DisplayDataNoSpinner(this, this, om.numPanes, om)
        drw = ObjectNavDrawer(this, UtilityModelSpcHrrrInterface.labels, UtilityModelSpcHrrrInterface.params)
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            om.displayData.param[om.curImg] = drw.tokens[position]
            om.displayData.paramLabel[om.curImg] = drw.getLabel(position)
            UtilityModels.getContentNonSpinner(this, om, overlayImg, uiDispatcher)
            updateMenuTitles()
        }
        setupModel()
        getRunStatus()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
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
                UtilityModels.getContentNonSpinner(this, om, overlayImg, uiDispatcher)
            }
            R.id.action_multipane -> ObjectIntent(this, ModelsSpcHrrrActivity::class.java, INFO, arrayOf("2", activityArguments[1], activityArguments[2]))
            R.id.action_back -> om.leftClick()
            R.id.action_forward -> om.rightClick()
            R.id.action_animate -> UtilityModels.getAnimate(om, overlayImg, uiDispatcher)
            R.id.action_time -> genericDialog(om.times) { om.setTimeIdx(it) }
            R.id.action_run -> genericDialog(om.rtd.listRun) { om.run = om.rtd.listRun[it] }
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this,this, om.animRan, om)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_region -> genericDialog(UtilityModelSpcHrrrInterface.sectors) { om.sector = UtilityModelSpcHrrrInterface.sectors[it] }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun overlaySelected(overlay: String) {
        if (overlayImg.contains(overlay)) overlayImg.remove(overlay) else overlayImg.add(overlay)
        getContent()
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        om.rtd = withContext(Dispatchers.IO) { om.getRunTime() }
        miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
        om.run = om.rtd.mostRecentRun
        (om.startStep until om.endStep).forEach { om.times.add(String.format(Locale.US, "%02d", it)) }
        UtilityModels.updateTime(UtilityString.getLastXChars(om.run, 2), om.rtd.mostRecentRun, om.times, "", false)
        om.setTimeIdx(Utility.readPref(this@ModelsSpcHrrrActivity, om.prefRunPosn, 1))
        getContent()
    }

    private fun getContent() {
        UtilityModels.getContentNonSpinner(this, om, overlayImg, uiDispatcher)
        updateMenuTitles()
    }

    private fun updateMenuTitles() {
        invalidateOptionsMenu()
        timeMenuItem.title = om.getTimeLabel()
        runMenuItem.title = om.run
    }

    private fun setupModel() {
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = om.params[0]
            om.displayData.param[it] = Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[it])
            om.displayData.paramLabel[it] = om.labels[0]
            om.displayData.paramLabel[it] = Utility.readPref(this, om.prefParamLabel + it.toString(), om.displayData.paramLabel[it])
            if (!UtilityModels.parameterInList(om.params, om.displayData.param[it])) {
                om.displayData.param[it] = om.params[0]
                om.displayData.paramLabel[it] = om.labels[0]
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

    private fun genericDialog(list: List<String>, fn: (Int) -> Unit) {
        val objectDialogue = ObjectDialogue(this, list)
        objectDialogue.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        })
        objectDialogue.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            fn(which)
            getContent()
            dialog.dismiss()
        })
        objectDialogue.show()
    }

    override fun onStop() {
        if (om.imageLoaded) {
            Utility.writePref(this, "SPCHRRR_OVERLAY", TextUtils.join(":", overlayImg))
            (0 until om.numPanes).forEach { UtilityImg.imgSavePosnZoom(this, om.displayData.img[it], om.modelProvider + om.numPanes.toString() + it.toString()) }
            Utility.writePref(this, om.prefRunPosn, om.timeIndex)
        }
        super.onStop()
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
            else -> super.onKeyUp(keyCode, event)
        }
    }
}

