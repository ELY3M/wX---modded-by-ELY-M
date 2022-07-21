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
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.*

class ModelsSpcSrefActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // native interface to the mobile SPC SREF website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup
    //

    companion object { const val INFO = "" }

    private var favList = listOf<String>()
    private lateinit var star: MenuItem
    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private lateinit var activityArguments: Array<String>
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var objectNavDrawerCombo: ObjectNavDrawerCombo
    private lateinit var om: ObjectModelNoSpinner
    private lateinit var timeMenuItem: MenuItem
    private lateinit var runMenuItem: MenuItem
    private val prefToken = "SPCSREF_FAV"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.models_spcsref_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_param).title = om.displayData.param.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        activityArguments = intent.getStringArrayExtra(INFO)!!
        om = ObjectModelNoSpinner(this, activityArguments[1], activityArguments[0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_spcsref, R.menu.models_spcsref, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_spcsrefmultipane, R.menu.models_spcsref, iconsEvenlySpaced = false, bottomToolbar = true)
            val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
            if (UtilityUI.isLandScape(this)) {
                linearLayout.orientation = LinearLayout.HORIZONTAL
            }
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        timeMenuItem = menu.findItem(R.id.action_time)
        runMenuItem = menu.findItem(R.id.action_run)
        miStatusParam1 = menu.findItem(R.id.action_status_param1)
        miStatusParam2 = menu.findItem(R.id.action_status_param2)
        star = menu.findItem(R.id.action_fav)
        star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        title = activityArguments[2]
        if (om.numPanes < 2) {
            fab1 = ObjectFab(this, R.id.fab1) {
                om.leftClick()
                getContent()
            }
            fab2 = ObjectFab(this, R.id.fab2) {
                om.rightClick()
                getContent()
            }
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
        setupModel()
        favList = UtilityFavorites.setupMenu(this, UIPreferences.srefFav, om.displayData.param[om.curImg], prefToken)
        UtilityModelSpcSrefInterface.createData()
        om.setUiElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, ::getContent)
        objectNavDrawerCombo = ObjectNavDrawerCombo(
                this,
                UtilityModelSpcSrefInterface.groups,
                UtilityModelSpcSrefInterface.longCodes,
                UtilityModelSpcSrefInterface.shortCodes,
                ""
        )
        objectNavDrawerCombo.setListener(::refreshSpinner)
        getRunStatus()
    }

    private fun updateStarIcon() {
        if (UIPreferences.srefFav.contains(":" + om.displayData.param[om.curImg] + ":"))
            star.setIcon(GlobalVariables.STAR_ICON)
        else
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
    }

    private fun getRunStatus() {
        FutureVoid(this, ::getRunStatusDownload, ::getRunStatusUpdate)
    }

    private fun getRunStatusDownload() {
        om.rtd = om.getRunTime()
    }

    private fun getRunStatusUpdate() {
        (0 until om.times.size).forEach {
            om.times[it] = om.times[it] + " " +
            UtilityModels.convertTimeRunToTimeString(
                    om.rtd.mostRecentRun.replace("z", ""),
                    om.times[it].replace("f", ""),
                    false
            )
        }
        miStatus.title = om.rtd.mostRecentRun + " - " + om.rtd.imageCompleteStr
        om.run = om.rtd.listRun.safeGet(0)
        om.setTimeIdx(Utility.readPref(this, om.prefRunPosn, 1))
        getContent()
    }

    private fun getContent() {
        favList = UtilityFavorites.setupMenu(this, UIPreferences.srefFav, om.displayData.param[om.curImg], prefToken)
        updateMenuTitles()
        updateStarIcon()
        UtilityModels.getContentNonSpinner(this, om, listOf(""))
    }

    private fun updateMenuTitles() {
        timeMenuItem.title = om.getTimeLabel()
        runMenuItem.title = om.run
        invalidateOptionsMenu()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> {
                om.leftClick()
                getContent()
            }
            R.id.action_forward -> {
                om.rightClick()
                getContent()
            }
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
            R.id.action_multipane -> ObjectIntent(this, ModelsSpcSrefActivity::class.java, INFO, arrayOf("2", activityArguments[1], activityArguments[2]))
            R.id.action_fav -> toggleFavorite()
            R.id.action_time -> genericDialog(om.times) { om.setTimeIdx(it) }
            R.id.action_run -> genericDialog(om.rtd.listRun) { om.run = om.rtd.listRun[it] }
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this, om.animRan, om)
                }
            }
            R.id.action_animate -> UtilityModels.getAnimate(this, om, listOf(""))
            R.id.action_help -> showHelpTextDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        favList = UtilityFavorites.setupMenu(this, UIPreferences.srefFav, om.displayData.param[om.curImg], prefToken)
        when (item.itemId) {
            R.id.action_param -> genericDialog(favList) {
                when (it) {
                    1 -> ObjectIntent.favoriteAdd(this, arrayOf("SREF"))
                    2 -> ObjectIntent.favoriteRemove(this, arrayOf("SREF"))
                    else -> {
                        om.displayData.param[om.curImg] = favList[it].split(" ").safeGet(0)
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
        objectNavDrawerCombo.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawerCombo.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    private fun showHelpTextDialog() {
        UtilityAlertDialog.showHelpTextWeb("${GlobalVariables.nwsSPCwebsitePrefix}/exper/sref/about_sref.html", this)
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, om.displayData.param[om.curImg], star, "SREF_FAV")
    }

    private fun refreshSpinner() {
        om.displayData.param[om.curImg] = objectNavDrawerCombo.getUrl()
        om.displayData.paramLabel[om.curImg] = objectNavDrawerCombo.getLabel()
        getContent()
    }

    override fun onStop() {
        if (om.imageLoaded) {
            (0 until om.numPanes).forEach {
                UtilityImg.imgSavePosnZoom(this, om.displayData.img[it], om.modelProvider + om.numPanes.toString() + it.toString())
            }
            Utility.writePref(this, om.prefRunPosn, om.timeIndex)
        }
        super.onStop()
    }

    private fun setupModel() {
        (om.startStep..om.endStep step om.stepAmount).forEach { om.times.add("f" + String.format(Locale.US, "%03d", it)) }
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = Utility.readPref(this, om.prefParam + it.toString(), "SREF_H5__")
            om.displayData.paramLabel[it] = Utility.readPref(this, om.prefParamLabel + it.toString(), "[MN]:500MB Height~Wind~Temp~Isotach")
        }
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
