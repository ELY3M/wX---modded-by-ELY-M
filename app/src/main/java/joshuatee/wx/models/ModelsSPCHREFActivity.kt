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

class ModelsSpcHrefActivity : VideoRecordActivity(), OnMenuItemClickListener {

    companion object { const val INFO = "" }

    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var objectNavDrawerCombo: ObjectNavDrawerCombo
    private lateinit var om: ObjectModel
    private var arguments: Array<String>? = arrayOf()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments = intent.getStringArrayExtra(INFO)
        // Keep for static pinned shortcuts
        if (arguments == null) {
            arguments = arrayOf("1", "SPCHREF", "SPC HREF")
        }
        om = ObjectModel(this, arguments!![1], arguments!![0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_spchref, R.menu.models_spchref, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_spchrefmultipane, R.menu.models_spchref, iconsEvenlySpaced = false, bottomToolbar = true)
            val box = VBox.fromResource(this)
            if (UtilityUI.isLandScape(this)) {
                box.makeHorizontal()
            }
        }
        objectToolbarBottom.connect(this)
        title = arguments!![2]
        timeMenuItem = objectToolbarBottom.find(R.id.action_time)
        runMenuItem = objectToolbarBottom.find(R.id.action_run)
        miStatusParam1 = objectToolbarBottom.find(R.id.action_status_param1)
        miStatusParam2 = objectToolbarBottom.find(R.id.action_status_param2)
        if (om.numPanes < 2) {
            fab1 = ObjectFab(this, R.id.fab1) { om.leftClick() }
            fab2 = ObjectFab(this, R.id.fab2) { om.rightClick() }
            objectToolbarBottom.hide(R.id.action_img1)
            objectToolbarBottom.hide(R.id.action_img2)
            if (UIPreferences.fabInModels) {
                objectToolbarBottom.hide(R.id.action_back)
                objectToolbarBottom.hide(R.id.action_forward)
            }
            fab1?.visibility = View.GONE
            fab2?.visibility = View.GONE
            miStatusParam2.isVisible = false
        } else {
            objectToolbarBottom.hide(R.id.action_multipane)
        }
        miStatus = objectToolbarBottom.find(R.id.action_status)
        miStatus.title = "in through"
        om.displayData = DisplayData(this, this, om.numPanes, om)
        om.sector = Utility.readPref(this, om.prefSector, om.sectors[0])
        UtilityModelSpcHrefInterface.createData()
        objectNavDrawerCombo = ObjectNavDrawerCombo(
                this,
                UtilityModelSpcHrefInterface.groups,
                UtilityModelSpcHrefInterface.longCodes,
                UtilityModelSpcHrefInterface.shortCodes,
                ""
        )
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        objectNavDrawerCombo.connect2 { _, _, groupPosition, childPosition, _ ->
            objectNavDrawerCombo.close()
            om.displayData.param[om.curImg] = objectNavDrawerCombo.getToken(groupPosition, childPosition)
            om.displayData.paramLabel[om.curImg] = objectNavDrawerCombo.getLabel(groupPosition, childPosition)
            UtilityModels.getContent(this, om, listOf(""))
            true
        }
        setupModel()
        getRunStatus()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> om.leftClick()
            R.id.action_forward -> om.rightClick()
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
            R.id.action_animate -> UtilityModels.getAnimate(this, om, listOf(""))
            R.id.action_time -> ObjectDialogue.generic(this, om.times, ::getContent) { om.setTimeIdx(it) }
            R.id.action_run -> ObjectDialogue.generic(this, om.rtd.listRun, ::getContent) { om.run = om.rtd.listRun[it] }
            R.id.action_multipane -> Route(this, ModelsSpcHrefActivity::class.java, INFO, arrayOf("2", arguments!![1], arguments!![2]))
            R.id.action_share -> if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this, om)
                }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_region -> ObjectDialogue.generic(this, UtilityModelSpcHrefInterface.sectorsLong, ::getContent) {
                om.sector = UtilityModelSpcHrefInterface.sectorsLong[it]
            }
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
        om.setTimeIdx(Utility.readPrefInt(this, om.prefRunPosn, 1))
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

    private fun setupModel() {
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = "500w_mean,500h_mean"
            om.displayData.param[it] = Utility.readPref(this, om.prefParam + it.toString(), om.displayData.param[it])
            om.displayData.paramLabel[it] = "500 mb Height/Wind"
            om.displayData.paramLabel[it] = Utility.readPref(this, om.prefParamLabel + it.toString(), om.displayData.paramLabel[it])
            if (!UtilityModelSpcHrefInterface.params.contains(om.displayData.param[it])) {
                om.displayData.param[it] = "500w_mean,500h_mean"
                om.displayData.paramLabel[it] = "500 mb Height/Wind"
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawerCombo.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        if (om.imageLoaded) {
            (0 until om.numPanes).forEach { UtilityImg.imgSavePosnZoom(this, om.displayData.image[it], om.modelProvider + om.numPanes.toString() + it.toString()) }
            Utility.writePrefInt(this, om.prefRunPosn, om.timeIndex)
        }
        super.onStop()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_J -> if (event.isCtrlPressed) om.leftClick()
            KeyEvent.KEYCODE_K -> if (event.isCtrlPressed) om.rightClick()
            else -> super.onKeyUp(keyCode, event)
        }
        return true
    }
}
