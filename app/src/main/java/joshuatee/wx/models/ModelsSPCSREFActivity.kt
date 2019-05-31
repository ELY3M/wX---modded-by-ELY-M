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
import java.util.Locale

import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.R
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.util.UtilityString
import kotlinx.coroutines.*

class ModelsSpcSrefActivity : VideoRecordActivity(), OnMenuItemClickListener, OnItemSelectedListener {

    // native interface to the mobile SPC SREF website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        const val INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var initSpinnerSetup = false
    private var favList = listOf<String>()
    private lateinit var star: MenuItem
    private var fab1: ObjectFab? = null
    private var fab2: ObjectFab? = null
    private lateinit var activityArguments: Array<String>
    private lateinit var miStatus: MenuItem
    private lateinit var miStatusParam1: MenuItem
    private lateinit var miStatusParam2: MenuItem
    private lateinit var spRun: ObjectSpinner
    private lateinit var spFav: ObjectSpinner
    private lateinit var drw: ObjectNavDrawerCombo
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var firstRunTimeSet = false
    private lateinit var om: ObjectModel

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        activityArguments = intent.getStringArrayExtra(INFO)
        om = ObjectModel(this, activityArguments[1], activityArguments[0])
        if (om.numPanes == 1) {
            super.onCreate(
                    savedInstanceState,
                    R.layout.activity_models_spcsref,
                    R.menu.models_spcsref,
                    iconsEvenlySpaced = false,
                    bottomToolbar = true
            )
        } else {
            super.onCreate(
                    savedInstanceState,
                    R.layout.activity_models_spcsrefmultipane,
                    R.menu.models_spcsref,
                    iconsEvenlySpaced = false,
                    bottomToolbar = true
            )
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        miStatusParam1 = menu.findItem(R.id.action_status_param1)
        miStatusParam2 = menu.findItem(R.id.action_status_param2)
        star = menu.findItem(R.id.action_fav)
        star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        title = activityArguments[2]
        val m = toolbarBottom.menu
        if (om.numPanes < 2) {
            fab1 = ObjectFab(
                    this,
                    this,
                    R.id.fab1,
                    OnClickListener { UtilityModels.moveBack(om.spTime) })
            fab2 = ObjectFab(
                    this,
                    this,
                    R.id.fab2,
                    OnClickListener { UtilityModels.moveForward(om.spTime) })
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
        setupModel()
        spRun = ObjectSpinner(this, this, this, R.id.spinner_run)
        favList = UtilityFavorites.setupFavMenuSref(
                MyApplication.srefFav,
                om.displayData.param[om.curImg]
        )
        spFav = ObjectSpinner(this, this, this, R.id.spinner1, favList)
        UtilityModelSpcSrefInterface.createData()
        om.setUIElements(toolbar, fab1, fab2, miStatusParam1, miStatusParam2, spRun, spRun)
        drw = ObjectNavDrawerCombo(
                this,
                UtilityModelSpcSrefInterface.groups,
                UtilityModelSpcSrefInterface.longCodes,
                UtilityModelSpcSrefInterface.shortCodes,
                this,
                ""
        )
        drw.setListener(::refreshSpinner)
        getRunStatus()
    }

    override fun onRestart() {
        favList = UtilityFavorites.setupFavMenuSref(
                MyApplication.srefFav,
                om.displayData.param[om.curImg]
        )
        spFav.refreshData(this, favList)
        super.onRestart()
    }

    private fun updateStarIcon() {
        if (MyApplication.srefFav.contains(":" + om.displayData.param[om.curImg] + ":"))
            star.setIcon(MyApplication.STAR_ICON)
        else
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        om.rtd = withContext(Dispatchers.IO) { om.getRunTime() }
        spRun.clear()
        spRun.addAll(om.rtd.listRun)
        spRun.notifyDataSetChanged()
        (0 until om.spTime.size()).forEach {
            om.spTime[it] = om.spTime[it] + " " +
                    UtilityModels.convertTimeRunToTimeString(
                            om.rtd.mostRecentRun.replace("z", ""),
                            om.spTime[it].replace("f", ""),
                            false
                    )
        }
        om.spTime.notifyDataSetChanged()
        spRun.setSelection(0)
        initSpinnerSetup = true
        miStatus.title = Utility.fromHtml(om.rtd.imageCompleteStr.replace("in through", "-"))
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            om.spTime.setSelection(Utility.readPref(this@ModelsSpcSrefActivity, om.prefRunPosn, 0))
        }
        om.spTime.notifyDataSetChanged()
        if (om.spTime.selectedItemPosition == 0 || om.numPanes > 1) {
            updateStarIcon()
            UtilityModels.getContent(this@ModelsSpcSrefActivity, om, listOf(""), uiDispatcher)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
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
            R.id.action_multipane -> ObjectIntent(
                    this,
                    ModelsSpcSrefActivity::class.java,
                    INFO,
                    arrayOf("2", activityArguments[1], activityArguments[2])
            )
            R.id.action_fav -> toggleFavorite()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityModels.legacyShare(this@ModelsSpcSrefActivity, om.animRan, om)
                }
            }
            R.id.action_animate -> UtilityModels.getAnimate(om, listOf(""), uiDispatcher)
            R.id.action_help -> showHelpTextDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    private fun showHelpTextDialog() {
        UtilityAlertDialog.showHelpTextWeb(
                "${MyApplication.nwsSPCwebsitePrefix}/exper/sref/about_sref.html",
                this
        )
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerRunRan && spinnerTimeRan) {
            if (parent.id == R.id.spinner1) {
                when (pos) {
                    1 -> ObjectIntent(
                            this,
                            FavAddActivity::class.java,
                            FavAddActivity.TYPE,
                            arrayOf("SREF")
                    )
                    2 -> ObjectIntent(
                            this,
                            FavRemoveActivity::class.java,
                            FavRemoveActivity.TYPE,
                            arrayOf("SREF")
                    )
                    else -> {
                        om.displayData.param[om.curImg] = favList[pos]
                        if (initSpinnerSetup) {
                            updateStarIcon()
                            UtilityModels.getContent(this, om, listOf(""), uiDispatcher)
                        }
                    }
                }
            } else {
                updateStarIcon()
                UtilityModels.getContent(this, om, listOf(""), uiDispatcher)
            }
        } else {
            when (parent.id) {
                R.id.spinner_run -> if (!spinnerRunRan)
                    spinnerRunRan = true
                R.id.spinner_time -> if (!spinnerTimeRan)
                    spinnerTimeRan = true
            }
        }
        if (parent.id == R.id.spinner_run) {
            UtilityModels.updateTime(
                    UtilityString.getLastXChars(spRun.selectedItem.toString().replace("z", ""), 2),
                    om.rtd.mostRecentRun,
                    om.spTime.list,
                    om.spTime.arrayAdapter,
                    "f",
                    false
            )
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun toggleFavorite() {
        UtilityFavorites.toggleFavorite(this, om.displayData.param[om.curImg], star, "SREF_FAV")
        favList = UtilityFavorites.setupFavMenuSref(
                MyApplication.srefFav,
                om.displayData.param[om.curImg]
        )
        spFav.refreshData(this, favList)
    }

    private fun refreshSpinner() {
        om.displayData.param[om.curImg] = drw.getUrl()
        om.displayData.paramLabel[om.curImg] = drw.getLabel()
        favList = UtilityFavorites.setupFavMenuSref(
                MyApplication.srefFav,
                om.displayData.param[om.curImg]
        )
        spFav.refreshData(this, favList)
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
        (om.startStep..om.endStep step om.stepAmount).forEach {
            om.spTime.add(
                    "f" + String.format(
                            Locale.US,
                            "%03d",
                            it
                    )
            )
        }
        om.spTime.notifyDataSetChanged()
        (0 until om.numPanes).forEach {
            om.displayData.param[it] =
                    Utility.readPref(this, om.prefParam + it.toString(), "SREF_H5__")
            om.displayData.paramLabel[it] = Utility.readPref(
                    this,
                    om.prefParamLabel + it.toString(),
                    "[MN]:500MB Height~Wind~Temp~Isotach"
            )
        }
    }
}



