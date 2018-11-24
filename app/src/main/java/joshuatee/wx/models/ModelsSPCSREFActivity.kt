/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString
import kotlinx.coroutines.*

class ModelsSPCSREFActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // native interface to the mobile SPC SREF website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        const val INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var initSpinnerSetup = false
    private var animRan = false
    //private var runStr = "f000"
    //private var runModelStr = ""
    private var favList = listOf<String>()
    private lateinit var star: MenuItem
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var turl: Array<String>
    private lateinit var miStatus: MenuItem
    private lateinit var spRun: ObjectSpinner
    private lateinit var spFav: ObjectSpinner
    private lateinit var drw: ObjectNavDrawerCombo
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var firstRunTimeSet = false
    private lateinit var contextg: Context
    private lateinit var om: ObjectModel

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        turl = intent.getStringArrayExtra(INFO)
        contextg = this
        om = ObjectModel(this, turl[1], turl[0])
        if (om.numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_spcsref, R.menu.models_spcsref, false, true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_spcsrefmultipane, R.menu.models_spcsref, false, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        star = menu.findItem(R.id.action_fav)
        star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        title = turl[2]
        val m = toolbarBottom.menu
        if (om.numPanes < 2) {
            fab1 = ObjectFab(this, this, R.id.fab1)
            fab2 = ObjectFab(this, this, R.id.fab2)
            m.findItem(R.id.action_img1).isVisible = false
            m.findItem(R.id.action_img2).isVisible = false
            if (UIPreferences.fabInModels) {
                fab1.setOnClickListener(OnClickListener { UtilityModels.moveBack(om.spTime) })
                fab2.setOnClickListener(OnClickListener { UtilityModels.moveForward(om.spTime) })
                val leftArrow = m.findItem(R.id.action_back)
                val rightArrow = m.findItem(R.id.action_forward)
                leftArrow.isVisible = false
                rightArrow.isVisible = false
            }
            fab1.setVisibility(View.GONE)
            fab2.setVisibility(View.GONE)
        } else {
            m.findItem(R.id.action_multipane).isVisible = false
        }
        miStatus = m.findItem(R.id.action_status)
        miStatus.title = "in through"
        om.spTime = ObjectSpinner(this, this, R.id.spinner_time)
        om.spTime.setOnItemSelectedListener(this)
        om.displayData = DisplayData(this, this, this, om.numPanes, om.spTime)
        setupModel()
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spRun.setOnItemSelectedListener(this)
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, om.displayData.param[om.curImg])
        spFav = ObjectSpinner(this, this, R.id.spinner1, favList)
        spFav.setOnItemSelectedListener(this)
        UtilityModelsSPCSREFInterface.createData()
        drw = ObjectNavDrawerCombo(this, UtilityModelsSPCSREFInterface.GROUPS, UtilityModelsSPCSREFInterface.LONG_CODES, UtilityModelsSPCSREFInterface.SHORT_CODES)
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            om.displayData.param[om.curImg] = drw.getToken(groupPosition, childPosition)
            om.displayData.paramLabel[om.curImg] = drw.getLabel(groupPosition, childPosition)
            refreshSpinner()
            true
        }
        getRunStatus()
    }

    override fun onRestart() {
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, om.displayData.param[om.curImg])
        spFav.refreshData(this, favList)
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        if (MyApplication.srefFav.contains(":" + om.displayData.param[om.curImg] + ":"))
            star.setIcon(MyApplication.STAR_ICON)
        else
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        //runStr = UtilityStringExternal.truncate(om.spTime.selectedItem.toString(), 4)
        //if (spRun.list.size > 0) {
        //    runModelStr = MyApplication.space.split(spRun.selectedItem.toString().replace("z", ""))[0]
        //}
        om.run = spRun.selectedItem.toString()
        om.time = om.spTime.selectedItem.toString()
        if (om.truncateTime) {
            om.time = UtilityStringExternal.truncate(om.time, om.timeTruncate)
        }
        withContext(Dispatchers.IO) {
            (0 until om.numPanes).forEach {
                om.displayData.bitmap[it] = om.getImage(it)
            }
        }
        (0 until om.numPanes).forEach {
            if (om.numPanes > 1)
                UtilityImg.resizeViewSetImgByHeight(om.displayData.bitmap[it], om.displayData.img[it])
            else
                om.displayData.img[it].setImageBitmap(om.displayData.bitmap[it])
            om.displayData.img[it].setMaxZoom(4f)
        }
        animRan = false
        if (!firstRun) {
            (0 until om.numPanes).forEach { UtilityImg.imgRestorePosnZoom(contextg, om.displayData.img[it], om.modelProvider + om.numPanes.toString() + it.toString()) }
            if (UIPreferences.fabInModels && om.numPanes < 2) {
                fab1.setVisibility(View.VISIBLE)
                fab2.setVisibility(View.VISIBLE)
            }
            firstRun = true
        }
        if (om.numPanes > 1) {
            UtilityModels.setSubtitleRestoreIMGXYZOOM(om.displayData.img, toolbar, "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1])
        } else {
            toolbar.subtitle = om.displayData.paramLabel[0]
        }
        imageLoaded = true
        (0 until om.numPanes).forEach {
            Utility.writePref(contextg, om.prefParam + it.toString(), om.displayData.param[it])
            Utility.writePref(contextg, om.prefParamLabel + it.toString(), om.displayData.paramLabel[it])
        }
        imageLoaded = true
    }

    private fun getRunStatus() = GlobalScope.launch(uiDispatcher) {
        om.rtd = withContext(Dispatchers.IO) { om.getRunTime() }
        spRun.clear()
        spRun.addAll(om.rtd.listRun)
        spRun.notifyDataSetChanged()
        (0 until om.spTime.size()).forEach {
            om.spTime[it] = om.spTime[it] + " " +
                    UtilityModels.convertTimeRuntoTimeString(om.rtd.mostRecentRun.replace("z", ""), om.spTime[it].replace("f", ""), false)
        }
        om.spTime.notifyDataSetChanged()
        spRun.setSelection(0)
        initSpinnerSetup = true
        miStatus.title = Utility.fromHtml(om.rtd.imageCompleteStr.replace("in through", "-"))
        //val titleTmpArr = MyApplication.space.split(om.rtd.imageCompleteStr.replace("in through", "-"))
        //if (titleTmpArr.size > 2) {
        //    toolbar.subtitle = Utility.fromHtml(titleTmpArr[2])
        //}
        if (!firstRunTimeSet) {
            firstRunTimeSet = true
            om.spTime.setSelection(Utility.readPref(contextg, om.prefRunPosn, 0))
        }
        om.spTime.notifyDataSetChanged()
        if (om.spTime.selectedItemPosition == 0 || om.numPanes > 1) {
            getContent()
        }
    }

    private fun getAnimate() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            (0 until om.numPanes).forEach { om.displayData.animDrawable[it] = om.getAnimate(it) }
        }
        (0 until om.numPanes).forEach { UtilityImgAnim.startAnimation(om.displayData.animDrawable[it], om.displayData.img[it]) }
        animRan = true
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
                UtilityModels.setSubtitleRestoreIMGXYZOOM(om.displayData.img, toolbar, "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1])
            }
            R.id.action_img2 -> {
                om.curImg = 1
                UtilityModels.setSubtitleRestoreIMGXYZOOM(om.displayData.img, toolbar, "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1])
            }
            R.id.action_multipane -> ObjectIntent(this, ModelsSPCSREFActivity::class.java, ModelsSPCSREFActivity.INFO, arrayOf("2", turl[1], turl[2]))
            R.id.action_fav -> toggleFavorite()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    val title = "SREF" + " " + om.displayData.param[0]
                    if (animRan)
                        UtilityShare.shareAnimGif(this, title + " " + om.spTime.selectedItem.toString(), om.displayData.animDrawable[0])
                    else
                        UtilityShare.shareBitmap(this, title + " " + om.spTime.selectedItem.toString(), om.displayData.bitmap[0])
                }
            }
            R.id.action_animate -> getAnimate()
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
        UtilityAlertDialog.showHelpTextWeb("${MyApplication.nwsSPCwebsitePrefix}/exper/sref/about_sref.html", this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (spinnerRunRan && spinnerTimeRan) {
            if (parent.id == R.id.spinner1) {
                when (pos) {
                    1 -> ObjectIntent(this, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("SREF"))
                    2 -> ObjectIntent(this, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("SREF"))
                    else -> {
                        om.displayData.param[om.curImg] = favList[pos]
                        if (initSpinnerSetup) {
                            getContent()
                        }
                    }
                }
            } else {
                getContent()
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
            UtilityModels.updateTime(UtilityString.getLastXChars(spRun.selectedItem.toString().replace("z", ""), 2), om.rtd.mostRecentRun, om.spTime.list,
                    om.spTime.arrayAdapter, "f", false)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun toggleFavorite() {
        UtilityFavorites.toggleFavorite(this, om.displayData.param[om.curImg], star, "SREF_FAV")
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, om.displayData.param[om.curImg])
        spFav.refreshData(this, favList)
    }

    private fun refreshSpinner() {
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, om.displayData.param[om.curImg])
        spFav.refreshData(this, favList)
    }

    override fun onStop() {
        if (imageLoaded) {
            (0 until om.numPanes).forEach { UtilityImg.imgSavePosnZoom(this, om.displayData.img[it], om.modelProvider + om.numPanes.toString() + it.toString()) }
            Utility.writePref(this, om.prefRunPosn, om.spTime.selectedItemPosition)
        }
        super.onStop()
    }

    private fun setupModel() {
        (om.startStep..om.endStep step om.stepAmount).forEach { om.spTime.add("f" + String.format(Locale.US, "%03d", it)) }
        om.spTime.notifyDataSetChanged()
        (0 until om.numPanes).forEach {
            om.displayData.param[it] = Utility.readPref(this, om.prefParam + it.toString(), "SREF_H5__")
            om.displayData.paramLabel[it] = Utility.readPref(this, om.prefParamLabel + it.toString(), "[MN]:500MB Height~Wind~Temp~Isotach")
        }
    }
}



