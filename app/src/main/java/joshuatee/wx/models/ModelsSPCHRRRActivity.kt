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
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.objects.DisplayData
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityString

class ModelsSpcHrrrActivity : VideoRecordActivity(), OnMenuItemClickListener { // OnItemSelectedListener

    companion object { const val INFO = "" }

    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private val overlayImg = mutableListOf<String>()
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var om: ObjectModel
    private lateinit var arguments: Array<String>
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
        arguments = intent.getStringArrayExtra(INFO)!!
        om = ObjectModel(this, arguments[1], arguments[0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_nospinner, R.menu.models_spchrrr, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_generic_multipane_nospinner, R.menu.models_spchrrr, iconsEvenlySpaced = false, bottomToolbar = true)
            val box = VBox.fromResource(this)
            if (UtilityUI.isLandScape(this)) {
                box.makeHorizontal()
            }
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        title = arguments[2]
        overlayImg.addAll(listOf(*TextUtils.split(Utility.readPref(this, "SPCHRRR_OVERLAY", ""), ":")))
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
        om.displayData = DisplayData(this, this, om.numPanes, om)
        objectNavDrawer = ObjectNavDrawer(this, UtilityModelSpcHrrrInterface.labels, UtilityModelSpcHrrrInterface.params)
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        objectNavDrawer.setListener2 { _, _, position, _ ->
            objectNavDrawer.setItemChecked(position, false)
            objectNavDrawer.close()
            om.displayData.param[om.curImg] = objectNavDrawer.tokens[position]
            om.displayData.paramLabel[om.curImg] = objectNavDrawer.getLabel(position)
            UtilityModels.getContent(this, om, overlayImg)
            updateMenuTitles()
        }
        setupModel()
        getRunStatus()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_img1 -> {
                om.curImg = 0
                UtilityModels.setSubtitleRestoreIMGXYZOOM(
                        om.displayData.image,
                        toolbar,
                        "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1]
                )
            }
            R.id.action_img2 -> {
                om.curImg = 1
                UtilityModels.setSubtitleRestoreIMGXYZOOM(
                        om.displayData.image,
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
                UtilityModels.getContent(this, om, overlayImg)
            }
            R.id.action_multipane -> Route(this, ModelsSpcHrrrActivity::class.java, INFO, arrayOf("2", arguments[1], arguments[2]))
            R.id.action_back -> om.leftClick()
            R.id.action_forward -> om.rightClick()
            R.id.action_animate -> UtilityModels.getAnimate(this, om, overlayImg)
            R.id.action_time -> ObjectDialogue.generic(this, om.times, ::getContent) { om.setTimeIdx(it) }
            R.id.action_run -> ObjectDialogue.generic(this, om.rtd.listRun, ::getContent) { om.run = om.rtd.listRun[it] }
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
            R.id.action_region -> ObjectDialogue.generic(this, UtilityModelSpcHrrrInterface.sectors, ::getContent) {
                om.sector = UtilityModelSpcHrrrInterface.sectors[it]
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun overlaySelected(overlay: String) {
        if (overlayImg.contains(overlay)) overlayImg.remove(overlay) else overlayImg.add(overlay)
        getContent()
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
        om.setTimeIdx(Utility.readPrefInt(this, om.prefRunPosn, 1))
        getContent()
    }

    private fun getContent() {
        UtilityModels.getContent(this, om, overlayImg)
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
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        if (om.imageLoaded) {
            Utility.writePref(this, "SPCHRRR_OVERLAY", TextUtils.join(":", overlayImg))
            (0 until om.numPanes).forEach {
                UtilityImg.imgSavePosnZoom(this, om.displayData.image[it], om.modelProvider + om.numPanes.toString() + it.toString())
            }
            Utility.writePrefInt(this, om.prefRunPosn, om.timeIndex)
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
