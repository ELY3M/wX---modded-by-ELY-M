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
import android.os.Build
import android.view.KeyEvent
import android.view.Menu
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import joshuatee.wx.R
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.Fab
import joshuatee.wx.ui.NavDrawerCombo
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityString

class ModelsSpcHrefActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // native interface to the mobile SPC HREF/SREF website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup
    // arg3 - title string
    //

    companion object { const val INFO = "" }

    // SREF ONLY
    private var favList = listOf<String>()
    private lateinit var star: MenuItem
    // END SREF ONLY
    private var fab1: Fab? = null
    private var fab2: Fab? = null
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var navDrawerCombo: NavDrawerCombo
    private lateinit var om: ObjectModel
    private lateinit var objectModelLayout: ObjectModelLayout
    private lateinit var timeMenuItem: MenuItem
    private lateinit var runMenuItem: MenuItem

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(objectModelLayout.topMenuResId, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (om.modelType == ModelType.SPCHREF) {
            menu.findItem(R.id.action_region).title = om.sector
        }
        if (om.modelType == ModelType.SPCSREF) {
            menu.findItem(R.id.action_param).title = om.displayData.param.safeGet(0)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var arguments = intent.getStringArrayExtra(INFO)
        // Keep for static pinned shortcuts
        if (arguments == null) {
            arguments = arrayOf("1", "SPCHREF", "SPC HREF")
        }
        title = arguments[2]
        objectModelLayout = ObjectModelLayout(this, arguments[1], arguments[0])
        if (To.int(arguments[0]) == 1) {
            super.onCreate(savedInstanceState, objectModelLayout.layoutSinglePane, objectModelLayout.menuResId, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, objectModelLayout.layoutMultiPane, objectModelLayout.menuResId, iconsEvenlySpaced = false, bottomToolbar = true)
            val box = VBox.fromResource(this)
            if (UtilityUI.isLandScape(this)) {
                box.makeHorizontal()
            }
        }
        om = ObjectModel(this, arguments[1], arguments[0])
        setupUI()
        setupModel()
    }

    private fun setupUI() {
        objectToolbarBottom.connect(this)
        with (objectToolbarBottom) {
            timeMenuItem = find(R.id.action_time)
            runMenuItem = find(R.id.action_run)
            miStatusParam1 = find(R.id.action_status_param1)
            miStatusParam2 = find(R.id.action_status_param2)
        }
        if (om.modelType == ModelType.SPCSREF) {
            star = objectToolbarBottom.getFavIcon()
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        if (om.numPanes < 2) {
            fab1 = Fab(this, R.id.fab1) { om.leftClick() }
            fab2 = Fab(this, R.id.fab2) { om.rightClick() }
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
        if (om.modelType == ModelType.SPCSREF) {
            favList = UtilityFavorites.setupMenu(this, om.displayData.param[om.curImg], FavoriteType.SREF)
        }
        om.createDataFn()
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        navDrawerCombo = NavDrawerCombo(this, om.groups, om.longCodes, om.shortCodes,"")
        navDrawerCombo.connect(::navDrawerSelected)
    }

    private fun navDrawerSelected() {
        om.displayData.param[om.curImg] = navDrawerCombo.getUrl()
        om.displayData.paramLabel[om.curImg] = navDrawerCombo.getLabel()
        getContent()
    }

    private fun getContent() {
        if (om.modelType == ModelType.SPCSREF) {
            favList = UtilityFavorites.setupMenu(this, om.displayData.param[om.curImg], FavoriteType.SREF)
            updateStarIcon()
        }
        UtilityModels.getContent(this, om, listOf(""))
        updateMenuTitles()
    }

    private fun setupModel() {
        var defaultParam = "500w_mean,500h_mean"
        var defaultLabel = "500 mb Height/Wind"
        if (om.modelType == ModelType.SPCSREF) {
            (om.startStep..om.endStep step om.stepAmount).forEach {
                om.times.add("f" + To.stringPadLeftZeros(it, 3))
            }
            defaultParam = "SREF_H5__"
            defaultLabel = "[MN]:500MB Height~Wind~Temp~Isotach"
        }
        with (om.displayData) {
            (0 until om.numPanes).forEach {
                param[it] = Utility.readPref(this@ModelsSpcHrefActivity, om.prefParam + it.toString(), defaultParam)
                paramLabel[it] = Utility.readPref(this@ModelsSpcHrefActivity, om.prefParamLabel + it.toString(), defaultLabel)
            }
        }
        FutureVoid(this, ::runStatusDownload, ::runStatusUpdate)
    }

    private fun runStatusDownload() {
        om.rtd = om.getRunTime()
    }

    private fun runStatusUpdate() {
        if (om.modelType == ModelType.SPCSREF) {
            (0 until om.times.size).forEach {
                om.times[it] = om.times[it] + " " +
                UtilityModels.convertTimeRunToTimeString(
                        om.rtd.mostRecentRun.replace("z", ""),
                        om.times[it].replace("f", ""),
                        false
                )
            }
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr.replace("</a>&nbsp in through <b>", " ")
            om.run = om.rtd.listRun.safeGet(0)
            om.setTimeIdx(Utility.readPrefInt(this, om.prefRunPosn, 1))
            getContent()
        } else {
            miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
            om.run = om.rtd.mostRecentRun
            (om.startStep until om.endStep).forEach {
                om.times.add(To.stringPadLeftZeros(it, 2))
            }
            UtilityModels.updateTime(UtilityString.getLastXChars(om.run, 2), om.rtd.mostRecentRun, om.times, "", false)
            om.setTimeIdx(Utility.readPrefInt(this, om.prefRunPosn, 1))
            getContent()
        }
    }

    private fun updateMenuTitles() {
        timeMenuItem.title = om.getTimeLabel()
        runMenuItem.title = om.run
        invalidateOptionsMenu()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (navDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> om.leftClick()
            R.id.action_forward -> om.rightClick()
            R.id.action_img1 -> {
                om.curImg = 0
                om.setSubtitleRestoreZoom()
            }
            R.id.action_img2 -> {
                om.curImg = 1
                om.setSubtitleRestoreZoom()
            }
            R.id.action_multipane -> om.routeFn(this)
            R.id.action_animate -> UtilityModels.getAnimate(this, om, listOf(""))
            R.id.action_time -> ObjectDialogue.generic(this, om.times, ::getContent) { om.setTimeIdx(it) }
            R.id.action_run -> ObjectDialogue.generic(this, om.rtd.listRun, ::getContent) { om.run = om.rtd.listRun[it] }
            R.id.action_share -> if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this, om)
                }
            R.id.action_fav -> toggleFavorite()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // SREF only
    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, om.displayData.param[om.curImg], star, FavoriteType.SREF)
    }

    private fun updateStarIcon() {
        if (om.modelType == ModelType.SPCSREF) {
            if (UIPreferences.favorites[FavoriteType.SREF]!!.contains(":" + om.displayData.param[om.curImg] + ":")) {
                star.setIcon(GlobalVariables.STAR_ICON)
            } else {
                star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        if (om.modelType == ModelType.SPCSREF) {
            favList = UtilityFavorites.setupMenu(this, om.displayData.param[om.curImg], FavoriteType.SREF)
        }
        when (item.itemId) {
            R.id.action_region -> ObjectDialogue.generic(this, om.sectorsLong, ::getContent) {
                om.sector = om.sectorsLong[it]
            }
            // SREF only
            R.id.action_param -> ObjectDialogue.generic(this, favList, ::getContent) {
                when (it) {
                    1 -> Route.favoriteAdd(this, FavoriteType.SREF)
                    2 -> Route.favoriteRemove(this, FavoriteType.SREF)
                    else -> {
                        om.displayData.param[om.curImg] = favList[it].split(" ").safeGet(0)
                        val index = om.params.indexOf(om.displayData.param[om.curImg])
                        om.displayData.paramLabel[om.curImg] = om.labels[index]
                        getContent()
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawerCombo.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        om.imgSavePosnZoom()
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
