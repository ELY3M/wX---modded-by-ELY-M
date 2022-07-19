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

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.Menu
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityString

class ModelsSpcHrefActivity : VideoRecordActivity(), OnMenuItemClickListener {

    companion object { const val INFO = "" }

    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var objectNavDrawerCombo: ObjectNavDrawerCombo
    private lateinit var om: ObjectModelNoSpinner
    private lateinit var activityArguments: Array<String>
    private lateinit var timeMenuItem: MenuItem
    private lateinit var runMenuItem: MenuItem

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.models_spchref_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_region).title = om.sector
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        activityArguments = intent.getStringArrayExtra(INFO)!!
        om = ObjectModelNoSpinner(this, activityArguments[1], activityArguments[0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_spchref, R.menu.models_spchref, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_spchrefmultipane, R.menu.models_spchref, iconsEvenlySpaced = false, bottomToolbar = true)
            val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
            if (UtilityUI.isLandScape(this)) {
                linearLayout.orientation = LinearLayout.HORIZONTAL
            }
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = activityArguments[2]
        val menu = toolbarBottom.menu
        timeMenuItem = menu.findItem(R.id.action_time)
        runMenuItem = menu.findItem(R.id.action_run)
        miStatusParam1 = menu.findItem(R.id.action_status_param1)
        miStatusParam2 = menu.findItem(R.id.action_status_param2)
        if (om.numPanes < 2) {
            fab1 = ObjectFab(this, this, R.id.fab1) { om.leftClick() }
            fab2 = ObjectFab(this, this, R.id.fab2) { om.rightClick() }
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
        om.sector = Utility.readPref(this, om.prefSector, om.sectors[0])
        UtilityModelSpcHrefInterface.createData()
        objectNavDrawerCombo = ObjectNavDrawerCombo(
                this,
                UtilityModelSpcHrefInterface.groups,
                UtilityModelSpcHrefInterface.longCodes,
                UtilityModelSpcHrefInterface.shortCodes,
                this,
                ""
        )
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        objectNavDrawerCombo.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            objectNavDrawerCombo.drawerLayout.closeDrawer(objectNavDrawerCombo.listView)
            om.displayData.param[om.curImg] = objectNavDrawerCombo.getToken(groupPosition, childPosition)
            om.displayData.paramLabel[om.curImg] = objectNavDrawerCombo.getLabel(groupPosition, childPosition)
            UtilityModels.getContentNonSpinner(this, om, listOf(""))
            true
        }
        setupModel()
        getRunStatus()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_back -> om.leftClick()
            R.id.action_forward -> om.rightClick()
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
            R.id.action_animate -> UtilityModels.getAnimate(this@ModelsSpcHrefActivity, om, listOf(""))
            R.id.action_time -> genericDialog(om.times) { om.setTimeIdx(it) }
            R.id.action_run -> genericDialog(om.rtd.listRun) { om.run = om.rtd.listRun[it] }
            R.id.action_multipane -> ObjectIntent(this, ModelsSpcHrefActivity::class.java, INFO, arrayOf("2", activityArguments[1], activityArguments[2]))
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this, this, om.animRan, om)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_region -> genericDialog(UtilityModelSpcHrefInterface.sectorsLong) { om.sector = UtilityModelSpcHrefInterface.sectorsLong[it] }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getRunStatus() {
        FutureVoid(this, ::getRunStatusDownload, ::getRunStatusUpdate)
    }

    private fun getRunStatusDownload() {
        om.rtd = om.getRunTime()
    }

    private fun getRunStatusUpdate() {
        miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
        om.run = om.rtd.mostRecentRun
        (om.startStep until om.endStep).forEach { om.times.add(String.format(Locale.US, "%02d", it)) }
        UtilityModels.updateTime(UtilityString.getLastXChars(om.run, 2), om.rtd.mostRecentRun, om.times, "", false)
        om.setTimeIdx(Utility.readPref(this@ModelsSpcHrefActivity, om.prefRunPosn, 1))
        getContent()
    }

    private fun getContent() {
        UtilityModels.getContentNonSpinner(this, om, listOf(""))
        updateMenuTitles()
    }

    private fun updateMenuTitles() {
        invalidateOptionsMenu()
        timeMenuItem.title = om.getTimeLabel()
        runMenuItem.title = om.run
    }

    private fun setupModel() {
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = "500w_mean,500h_mean"
            om.displayData.param[it] = Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[it])
            om.displayData.paramLabel[it] = "500 mb Height/Wind"
            om.displayData.paramLabel[it] = Utility.readPref(this, om.prefParamLabel + it.toString(), om.displayData.paramLabel[it])
            if (!UtilityModels.parameterInList(UtilityModelSpcHrefInterface.params, om.displayData.param[it])) {
                om.displayData.param[it] = "500w_mean,500h_mean"
                om.displayData.paramLabel[it] = "500 mb Height/Wind"
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawerCombo.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawerCombo.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    private fun genericDialog(list: List<String>, fn: (Int) -> Unit) {
        val objectDialogue = ObjectDialogue(this, list)
        objectDialogue.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        objectDialogue.setSingleChoiceItems { dialog, which ->
            fn(which)
            getContent()
            dialog.dismiss()
        }
        objectDialogue.show()
    }

    override fun onStop() {
        if (om.imageLoaded) {
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
