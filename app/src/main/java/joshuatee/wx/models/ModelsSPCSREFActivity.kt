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

import android.os.AsyncTask
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

class ModelsSPCSREFActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener, OnItemSelectedListener {

    // native interface to the mobile SPC SREF website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        const val INFO = ""
    }

    private var initSpinnerSetup = false
    private var animRan = false
    private var runStr = "f000"
    private var runModelStr = ""
    private var favList = listOf<String>()
    private lateinit var star: MenuItem
    private var firstRun = false
    private var imageLoaded = false
    private var numPanes = 0
    private var curImg = 0
    private lateinit var fab1: ObjectFab
    private lateinit var fab2: ObjectFab
    private lateinit var turl: Array<String>
    private lateinit var miStatus: MenuItem
    private var prefParam = ""
    private var prefParamLabel = ""
    private var prefRunPosn = ""
    private var modelProvider = ""
    private lateinit var spRun: ObjectSpinner
    private lateinit var spTime: ObjectSpinner
    private lateinit var spFav: ObjectSpinner
    private var rtd = RunTimeData()
    private lateinit var drw: ObjectNavDrawerCombo
    private var spinnerRunRan = false
    private var spinnerTimeRan = false
    private var firstRunTimeSet = false
    private lateinit var displayData: DisplayData
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        turl = intent.getStringArrayExtra(INFO)
        contextg = this
        val numPanesStr = turl[0]
        numPanes = numPanesStr.toIntOrNull() ?: 0
        if (numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_models_spcsref, R.menu.models_spcsref, false, true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_models_spcsrefmultipane, R.menu.models_spcsref, false, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        star = menu.findItem(R.id.action_fav)
        star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        val prefModel = turl[1]
        prefParam = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = "MODEL_" + prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        prefRunPosn = "MODEL_" + prefModel + numPanesStr + "_RUN_POSN"
        modelProvider = "MODEL_$prefModel$numPanesStr"
        title = "SPC SREF"
        val m = toolbarBottom.menu
        if (numPanes < 2) {
            fab1 = ObjectFab(this, this, R.id.fab1)
            fab2 = ObjectFab(this, this, R.id.fab2)
            m.findItem(R.id.action_img1).isVisible = false
            m.findItem(R.id.action_img2).isVisible = false
            if (UIPreferences.fabInModels) {
                fab1.setOnClickListener(OnClickListener { UtilityModels.moveBack(spTime) })
                fab2.setOnClickListener(OnClickListener { UtilityModels.moveForward(spTime) })
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
        spTime = ObjectSpinner(this, this, R.id.spinner_time)
        spTime.setOnItemSelectedListener(this)
        (0..88 step 3).forEach { spTime.add("f" + String.format(Locale.US, "%03d", it)) }
        spTime.notifyDataSetChanged()
        displayData = DisplayData(this, this, this, numPanes, spTime)
        (0 until numPanes).forEach {
            displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), "SREF_H5__")
            displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), "[MN]:500MB Height~Wind~Temp~Isotach")
        }
        spRun = ObjectSpinner(this, this, R.id.spinner_run)
        spRun.setOnItemSelectedListener(this)
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, displayData.param[curImg])
        spFav = ObjectSpinner(this, this, R.id.spinner1, favList)
        spFav.setOnItemSelectedListener(this)
        UtilityModelsSPCSREFInterface.createData()
        drw = ObjectNavDrawerCombo(this, UtilityModelsSPCSREFInterface.GROUPS, UtilityModelsSPCSREFInterface.LONG_CODES, UtilityModelsSPCSREFInterface.SHORT_CODES)
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(groupPosition, childPosition)
            displayData.paramLabel[curImg] = drw.getLabel(groupPosition, childPosition)
            refreshSpinner()
            true
        }
        GetRunStatus().execute()
    }

    override fun onRestart() {
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, displayData.param[curImg])
        spFav.refreshData(this, favList)
        super.onRestart()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            if (MyApplication.srefFav.contains(":" + displayData.param[curImg] + ":"))
                star.setIcon(MyApplication.STAR_ICON)
            else
                star.setIcon(MyApplication.STAR_OUTLINE_ICON)
            runStr = UtilityStringExternal.truncate(spTime.selectedItem.toString(), 4)
            if (spRun.list.size > 0) {
                runModelStr = MyApplication.space.split(spRun.selectedItem.toString().replace("z", ""))[0]
            }
        }

        override fun doInBackground(vararg params: String): String {
            (0 until numPanes).forEach { displayData.bitmap[it] = UtilityModelsSPCSREFInputOutput.getImage(contextg, displayData.param[it], runModelStr, runStr) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            (0 until numPanes).forEach {
                if (numPanes > 1)
                    UtilityImg.resizeViewSetImgByHeight(displayData.bitmap[it], displayData.img[it])
                else
                    displayData.img[it].setImageBitmap(displayData.bitmap[it])
                displayData.img[it].setMaxZoom(4f)
            }
            animRan = false
            if (!firstRun) {
                (0 until numPanes).forEach { UtilityImg.imgRestorePosnZoom(contextg, displayData.img[it], modelProvider + numPanes.toString() + it.toString()) }
                if (UIPreferences.fabInModels && numPanes < 2) {
                    fab1.setVisibility(View.VISIBLE)
                    fab2.setVisibility(View.VISIBLE)
                }
                firstRun = true
            }
            if (numPanes > 1)
                UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
            imageLoaded = true
            (0 until numPanes).forEach {
                Utility.writePref(contextg, prefParam + it.toString(), displayData.param[it])
                Utility.writePref(contextg, prefParamLabel + it.toString(), displayData.paramLabel[it])
            }
            imageLoaded = true
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetRunStatus : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            rtd = UtilityModelsSPCSREFInputOutput.runTime
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            spRun.clear()
            spRun.addAll(rtd.listRun)
            spRun.notifyDataSetChanged()
            (0 until spTime.size()).forEach { spTime[it] = spTime[it] + " " + UtilityModels.convertTimeRuntoTimeString(rtd.mostRecentRun.replace("z", ""), spTime[it].replace("f", ""), false) }
            spTime.notifyDataSetChanged()
            spRun.setSelection(0)
            initSpinnerSetup = true
            miStatus.title = Utility.fromHtml(rtd.imageCompleteStr.replace("in through", "-"))
            val titleTmpArr = MyApplication.space.split(rtd.imageCompleteStr.replace("in through", "-"))
            if (titleTmpArr.size > 2) {
                toolbar.subtitle = Utility.fromHtml(titleTmpArr[2])
            }
            if (!firstRunTimeSet) {
                firstRunTimeSet = true
                spTime.setSelection(Utility.readPref(contextg, prefRunPosn, 0))
            }
            spTime.notifyDataSetChanged()
            if (spTime.selectedItemPosition == 0 || numPanes > 1) {
                GetContent().execute()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AnimateRadar : AsyncTask<String, String, String>() {

        internal var spinnerTimeValue: Int = 0

        override fun onPreExecute() {
            spinnerTimeValue = spTime.selectedItemPosition
        }

        override fun doInBackground(vararg params: String): String {
            (0 until numPanes).forEach { displayData.animDrawable[it] = UtilityModelsSPCSREFInputOutput.getAnimation(contextg, displayData.param[it], runModelStr, spinnerTimeValue, spTime.list) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            (0 until numPanes).forEach { UtilityImgAnim.startAnimation(displayData.animDrawable[it], displayData.img[it]) }
            animRan = true
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> UtilityModels.moveBack(spTime)
            R.id.action_forward -> UtilityModels.moveForward(spTime)
            R.id.action_img1 -> {
                curImg = 0
                UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
            }
            R.id.action_img2 -> {
                curImg = 1
                UtilityModels.setSubtitleRestoreIMGXYZOOM(displayData.img, toolbar, "(" + (curImg + 1).toString() + ")" + displayData.param[0] + "/" + displayData.param[1])
            }
            R.id.action_multipane -> ObjectIntent(this, ModelsSPCSREFActivity::class.java, ModelsSPCSREFActivity.INFO, arrayOf("2", turl[1]))
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
                    val title = "SREF" + " " + displayData.param[0]
                    if (animRan)
                        UtilityShare.shareAnimGif(this, title + " " + spTime.selectedItem.toString(), displayData.animDrawable[0])
                    else
                        UtilityShare.shareBitmap(this, title + " " + spTime.selectedItem.toString(), displayData.bitmap[0])
                }
            }
            R.id.action_animate -> AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
                        displayData.param[curImg] = favList[pos]
                        if (initSpinnerSetup) {
                            GetContent().execute()
                        }
                    }
                }
            } else {
                GetContent().execute()
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
            UtilityModels.updateTime(UtilityString.getLastXChars(spRun.selectedItem.toString().replace("z", ""), 2), rtd.mostRecentRun, spTime.list,
                    spTime.arrayAdapter, "f", false)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun toggleFavorite() {
        UtilityFavorites.toggleFavorite(this, displayData.param[curImg], star, "SREF_FAV")
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, displayData.param[curImg])
        spFav.refreshData(this, favList)
    }

    private fun refreshSpinner() {
        favList = UtilityFavorites.setupFavMenuSREF(MyApplication.srefFav, displayData.param[curImg])
        spFav.refreshData(this, favList)
    }

    override fun onStop() {
        if (imageLoaded) {
            (0 until numPanes).forEach { UtilityImg.imgSavePosnZoom(this, displayData.img[it], modelProvider + numPanes.toString() + it.toString()) }
            Utility.writePref(this, prefRunPosn, spTime.selectedItemPosition)
        }
        super.onStop()
    }
}



