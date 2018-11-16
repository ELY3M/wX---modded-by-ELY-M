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

package joshuatee.wx.radar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class USNWSMosaicActivity : VideoRecordActivity(), OnClickListener, OnItemSelectedListener, Toolbar.OnMenuItemClickListener {

    // Provides native interface to NWS radar mosaics along with animations
    //
    // arg1: "widget" (optional) - if this arg is specified it will show mosaic for widget location
    //       "location" for current location

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var animDrawable = AnimationDrawable()
    private lateinit var img: TouchImageView2
    private var firstTime = true
    private var nwsRadarMosaicSectorCurrent = ""
    private var nwsRadarMosaicSectorLabelCurrent = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private var imageLoaded = false
    private var imgIdx = 0
    private lateinit var sp: ObjectSpinner
    private var doNotSavePref = false
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_nwsmosaic, R.menu.nwsmosaic, true, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        img.setMaxZoom(8.0f)
        img.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) showNextImg()
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) showPrevImg()
            }
        })
        val turl = intent.getStringArrayExtra(URL)
        if (turl == null) {
            nwsRadarMosaicSectorLabelCurrent = Utility.readPref(this, "NWS_RADAR_MOSAIC_SECTOR_CURRENT", "Central Great Lakes")
        } else {
            if (turl.isNotEmpty() && turl[0] == "location") {
                val rid1 = Location.rid
                val ridLoc = Utility.readPref(this, "RID_LOC_$rid1", "")
                val nwsLocationArr = ridLoc.split(",").dropLastWhile { it.isEmpty() }
                val state = nwsLocationArr.getOrNull(0) ?: ""
                nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.getNWSSectorFromState(state)
                nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.getNWSSectorLabelFromCode(nwsRadarMosaicSectorLabelCurrent)
                doNotSavePref = true
            } else if (turl.isNotEmpty() && turl[0] == "widget") {
                val widgetLocNum = Utility.readPref(this, "WIDGET_LOCATION", "1")
                val rid1 = Location.getRid(this, widgetLocNum)
                val ridLoc = Utility.readPref(this, "RID_LOC_$rid1", "")
                val nwsLocationArr = ridLoc.split(",").dropLastWhile { it.isEmpty() }
                val state = Utility.readPref(this, "STATE_CODE_" + nwsLocationArr.getOrNull(0), "")
                nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.getNWSSectorFromState(state)
                nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.getNWSSectorLabelFromCode(nwsRadarMosaicSectorLabelCurrent)
            } else {
                nwsRadarMosaicSectorLabelCurrent = Utility.readPref(this, "NWS_RADAR_MOSAIC_SECTOR_CURRENT", "Central Great Lakes")
            }
        }
        sp = ObjectSpinner(this, this, R.id.spinner1, UtilityUSImgNWSMosaic.NWS_SECTORS_LABELS)
        sp.setOnItemSelectedListener(this)
        imgIdx = findPosition(nwsRadarMosaicSectorLabelCurrent)
        sp.setSelection(imgIdx)
    }

    private fun findPosition(keyF: String): Int {
        var key = keyF
        if (key == "latest") {
            key = "CONUS"
        }
        return UtilityUSImgNWSMosaic.NWS_SECTORS_LABELS.indices.firstOrNull { key == UtilityUSImgNWSMosaic.NWS_SECTORS_LABELS[it] }
                ?: 0
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        //@SuppressLint("StaticFieldLeak")
        //private inner class GetContent : AsyncTask<String, String, String>() {

        //override fun doInBackground(vararg params: String): String {
        bitmap = withContext(Dispatchers.IO) { UtilityUSImgNWSMosaic.nwsMosaic(contextg, nwsRadarMosaicSectorCurrent, true) }
        //return "Executed"
        //}

        // FIXME bug in API 28 after changing
        //override fun onPostExecute(result: String) {
        if (!doNotSavePref) {
            Utility.writePref(contextg, "NWS_RADAR_MOSAIC_SECTOR_CURRENT", nwsRadarMosaicSectorLabelCurrent)
        }
        img.setImageBitmap(bitmap)
        animRan = false
        //firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "NWSRADMOS")
        //if (!firstRun) {
        img.setZoom("NWSRADMOS")
        //    firstRun = true
        //}
        imageLoaded = true
        //}
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        //@SuppressLint("StaticFieldLeak")
        //private inner class AnimateRadar : AsyncTask<String, String, String>() {

        //override fun doInBackground(vararg params: String): String {
        animDrawable = withContext(Dispatchers.IO) { UtilityUSImgNWSMosaic.nwsMosaicAnimation(contextg, nwsRadarMosaicSectorCurrent, frameCount, true) }
        //return "Executed"
        //}

        //override fun onPostExecute(result: String) {
        animRan = UtilityImgAnim.startAnimation(animDrawable, img)
        //}
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.RADAR_MOSAIC)
            R.id.action_a12 -> getAnimate(12)
            R.id.action_a18 -> getAnimate(18)
            R.id.action_a6 -> getAnimate(6)
            R.id.action_stop -> animDrawable.stop()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan) {
                        UtilityShare.shareAnimGif(this, "NWS mosaic: $nwsRadarMosaicSectorLabelCurrent", animDrawable)
                    } else {
                        UtilityShare.shareBitmap(this, "NWS mosaic: $nwsRadarMosaicSectorLabelCurrent", bitmap)
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (firstTime) {
            UtilityToolbar.fullScreenMode(toolbar)
            firstTime = false
        }
        if (pos == UtilityUSImgNWSMosaic.NWS_SECTORS.size - 1) {
            nwsRadarMosaicSectorCurrent = "latest"
            nwsRadarMosaicSectorLabelCurrent = "CONUS"
        } else {
            nwsRadarMosaicSectorCurrent = UtilityUSImgNWSMosaic.NWS_SECTORS[pos]
            nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.NWS_SECTORS_LABELS[pos]
        }
        img.resetZoom()
        imgIdx = pos
        getContent()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "NWSRADMOS")
        }
        super.onStop()
    }

    private fun showNextImg() {
        imgIdx += 1
        if (imgIdx == UtilityUSImgNWSMosaic.NWS_SECTORS.size) {
            imgIdx = 0
        }
        nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.NWS_SECTORS_LABELS[imgIdx]
        nwsRadarMosaicSectorCurrent = UtilityUSImgNWSMosaic.NWS_SECTORS[imgIdx]
        sp.setSelection(imgIdx)
    }

    private fun showPrevImg() {
        imgIdx -= 1
        if (imgIdx == -1) {
            imgIdx = UtilityUSImgNWSMosaic.NWS_SECTORS.size - 1
        }
        nwsRadarMosaicSectorLabelCurrent = UtilityUSImgNWSMosaic.NWS_SECTORS_LABELS[imgIdx]
        nwsRadarMosaicSectorCurrent = UtilityUSImgNWSMosaic.NWS_SECTORS[imgIdx]
        sp.setSelection(imgIdx)
    }
}


