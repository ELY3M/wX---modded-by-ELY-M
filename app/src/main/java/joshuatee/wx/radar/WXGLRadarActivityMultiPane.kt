/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx.radar

import android.annotation.SuppressLint
import java.io.File

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.ContextCompat
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.os.Handler
import android.util.Log

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.activitiesmisc.WebscreenABModels
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.ImageMap
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.settings.SettingsRadarActivity
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences

import joshuatee.wx.TDWR_RIDS
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType

import joshuatee.wx.radar.SpotterNetworkPositionReport.SendPosition

class WXGLRadarActivityMultiPane : VideoRecordActivity(), OnMenuItemClickListener {

    // This activity is a general purpose viewer of nexrad and mosaic content
    // nexrad data is downloaded from NWS FTP, decoded and drawn using OpenGL ES
    // Unlike the traditional viewer this one shows multiple nexrad radars at the same time
    // nexrad sites, products, zoom and x/y are saved on stop and restored on start
    //
    // Arguments
    // 1: RID
    // 2: State NO LONGER NEEDED
    // 3: number of panes

    companion object {
        const val RID: String = ""

    }

    private var TAG = "joshuatee-WXGLRadarActivityMultiPane"
    private var numPanes = 4
    private var numPanesArr = listOf<Int>()
    private var mHandler: Handler? = null
    private var mInterval = 180000
    private var sn_Handler_m: Handler? = null
    private var sn_Interval = 180000 // 180 seconds by default
    private var loopCount = 0
    private var animRan = false
    private var ridChanged = true
    private var restartedZoom = false
    private var inOglAnim = false
    private var inOglAnimPaused = false
    private var infoArr = Array(2) { "" }
    private var oglInView = true
    private var oglrArr = mutableListOf<WXGLRender>()
    private var glviewArr = mutableListOf<WXGLSurfaceView>()
    private var tilt = "0"
    private var oldRidArr = Array(2) { "" }
    private lateinit var imageMap: ObjectImageMap
    private var mapShown = false
    private lateinit var star: MenuItem
    private lateinit var anim: MenuItem
    private var delay = 0
    private var frameCntStrGlobal = ""
    private var locXCurrent = ""
    private var locYCurrent = ""
    private var infoAnim = Array(2) { "" }
    private var tmpArr1 = Array(2) { "" }
    private var tmpArr2 = Array(2) { "" }
    private var tmpArr3 = Array(2) { "" }
    private var tmpArr4 = Array(2) { "" }
    private val latlonArr = mutableListOf("", "")
    private var latD = 0.0
    private var lonD = 0.0
    private var locationManager: LocationManager? = null
    private var animTriggerDownloads = false
    private var curRadar = 0
    private var idxIntG = 0
    private val alertDialogStatusAl = mutableListOf<String>()
    private lateinit var contextg: Context
    private var idxIntAl = 0
    private var prefPrefix = "WXOGL_DUALPANE"
    private var rlArr = mutableListOf<RelativeLayout>()
    private var wxgltextArr = mutableListOf<WXGLTextObject>()
    private lateinit var act: Activity
    private var diaStatus: ObjectDialogue? = null

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        val turl = intent.getStringArrayExtra(RID)
        numPanes = turl[2].toIntOrNull() ?: 0
        numPanesArr = (0 until numPanes).toList()
        UtilityFileManagement.deleteCacheFiles(this)
        if (numPanes == 2) {
            if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane_immersive, R.menu.uswxoglradarmultipane, true, true)
            else
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane, R.menu.uswxoglradarmultipane, true, true)
        } else {
            if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane_quad_immersive, R.menu.uswxoglradarmultipane, true, true)
            else
                super.onCreate(savedInstanceState, R.layout.activity_uswxoglmultipane_quad, R.menu.uswxoglradarmultipane, true, true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        act = this
        UtilityUI.immersiveMode(this as Activity)
        locXCurrent = joshuatee.wx.settings.Location.x
        locYCurrent = joshuatee.wx.settings.Location.y
        infoAnim = Array(numPanes) { "" }
        oldRidArr = Array(numPanes) { "" }
        infoArr = Array(numPanes) { "" }
        var widthDivider = 1
        if (numPanes == 4) {
            widthDivider = 2
            prefPrefix = "WXOGL_QUADPANE"
        }
        contextg = this
        alertDialogStatus()
        UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        val latlonArrD = UtilityLocation.getGPS(this as Context)
        latD = latlonArrD[0]
        lonD = latlonArrD[1]
        val menu = toolbarBottom.menu
        star = menu.findItem(R.id.action_fav)
        anim = menu.findItem(R.id.action_a)
        val rad3 = menu.findItem(R.id.action_radar3)
        val rad4 = menu.findItem(R.id.action_radar4)
        if (numPanes == 2) {
            rad3.isVisible = false
            rad4.isVisible = false
        }
        if (!UIPreferences.radarImmersiveMode) {
            val blank = menu.findItem(R.id.action_blank)
            blank.isVisible = false
            menu.findItem(R.id.action_level3_blank).isVisible = false
            menu.findItem(R.id.action_level2_blank).isVisible = false
            menu.findItem(R.id.action_animate_blank).isVisible = false
            menu.findItem(R.id.action_tilt_blank).isVisible = false
            menu.findItem(R.id.action_tools_blank).isVisible = false
        }
        if (android.os.Build.VERSION.SDK_INT < 21) menu.findItem(R.id.action_share).title = "Share"
        delay = UtilityImg.animInterval(this)
        numPanesArr.forEach {
            glviewArr.add(WXGLSurfaceView(this, widthDivider, numPanes))
            glviewArr[it].idxInt = it
            oglrArr.add(WXGLRender(this))
            oglrArr[it].radarStatusStr = (it + 1).toString()
            oglrArr[it].idxStr = (it + 1).toString()
        }
        val elementIds = listOf(R.id.rl1, R.id.rl2, R.id.rl3, R.id.rl4)
        if (numPanes == 4) {
            numPanesArr.forEach {
                rlArr.add(findViewById(elementIds[it]))
                rlArr[it].addView(glviewArr[it])
                val params = rlArr[it].layoutParams
                if (Build.VERSION.SDK_INT >= 19 && (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent))
                    params.height = MyApplication.dm.heightPixels / 2 + UtilityUI.statusBarHeight(this)
                else
                    params.height = MyApplication.dm.heightPixels / 2 - MyApplication.actionBarHeight / 2 - UtilityUI.statusBarHeight(this) / 2 - (UtilityUI.navigationBarHeight(this) / 2.0).toInt()
                if (Build.VERSION.SDK_INT >= 19
                        && UIPreferences.radarToolbarTransparent
                        && !UIPreferences.radarImmersiveMode
                        && numPanes == 4)
                    params.height = MyApplication.dm.heightPixels / 2 - UtilityUI.statusBarHeight(this) / 2

                params.width = MyApplication.dm.widthPixels / 2
            }
        } else if (numPanes == 2) {
            numPanesArr.forEach {
                rlArr.add(findViewById(elementIds[it]))
                rlArr[it].addView(glviewArr[it])
                val params = rlArr[it].layoutParams
                //params.height = MyApplication.dm.heightPixels/2 - MyApplication.actionBarHeight
                params.height = MyApplication.dm.heightPixels / 2 - (MyApplication.actionBarHeight / 2) - UtilityUI.statusBarHeight(this) / 2 - (UtilityUI.navigationBarHeight(this) / 2.0).toInt()
                params.width = MyApplication.dm.widthPixels
            }
        }
        numPanesArr.forEach { initGLVIEW(glviewArr[it], oglrArr[it]) }
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, rlArr.toList() as List<View> + glviewArr.toList() as List<View>)
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                ridMapSwitch(UtilityImageMap.maptoRid(id))
            }

            override fun onBubbleClicked(id: Int) {}
        })
        oglInView = true
        numPanesArr.forEach { oglrArr[it].rid = Utility.readPref(this, prefPrefix + "_RID" + (it + 1).toString(), turl[0]) }
        if (MyApplication.dualpaneshareposn) {
            (1 until numPanes).forEach { oglrArr[it].rid = oglrArr[0].rid }
        }
        numPanesArr.forEach { oldRidArr[it] = "" }
        val defaultProducts = listOf("N0Q", "N0U", "N0C", "DVL")
        (0..(numPanes - 1)).forEach { oglrArr[it].product = Utility.readPref(this, prefPrefix + "_PROD" + (it + 1).toString(), defaultProducts[it]) }

        glviewArr[0].scaleFactor = Utility.readPref(this, prefPrefix + "_ZOOM1", MyApplication.wxoglSize.toFloat() / 10.0f)
        oglrArr[0].setViewInitial(Utility.readPref(this, prefPrefix + "_ZOOM1", MyApplication.wxoglSize.toFloat() / 10.0f),
                Utility.readPref(this, prefPrefix + "_X1", 0.0f),
                Utility.readPref(this, prefPrefix + "_Y1", 0.0f))

        if (MyApplication.dualpaneshareposn) {
            (1 until numPanes).forEach {
                glviewArr[it].scaleFactor = glviewArr[0].scaleFactor
                oglrArr[it].setViewInitial(Utility.readPref(this, prefPrefix + "_ZOOM1", MyApplication.wxoglSize.toFloat() / 10.0f),
                        oglrArr[0].x, oglrArr[0].y)
            }
        } else {
            (1 until numPanes).forEach {
                glviewArr[it].scaleFactor = Utility.readPref(this, prefPrefix + "_ZOOM" + (it + 1).toString(), MyApplication.wxoglSize.toFloat() / 10.0f)
                oglrArr[it].setViewInitial(Utility.readPref(this, prefPrefix + "_ZOOM" + (it + 1).toString(), MyApplication.wxoglSize.toFloat() / 10.0f),
                        Utility.readPref(this, prefPrefix + "_X" + (it + 1).toString(), 0.0f),
                        Utility.readPref(this, prefPrefix + "_Y" + (it + 1).toString(), 0.0f))
            }
        }
        numPanesArr.forEach {
            wxgltextArr.add(WXGLTextObject(this, rlArr[it], glviewArr[it], oglrArr[it], numPanes))
            glviewArr[it].wxgltextArr = wxgltextArr
            wxgltextArr[it].initTV(this)
        }
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)
            getContentSerial()
        else
            getContentParallel()
        if (MyApplication.wxoglLocationAutorefresh) {
            mInterval = 60000 * Utility.readPref(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000.toLong(), 30.0f, locationListener)
            if (MyApplication.wxoglkeepscreenon) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if (MyApplication.sn_locationreport) {
                Log.i(TAG, "sending sn location report")
            }
            mHandler = Handler()
            startRepeatingTask()
        }
        if (MyApplication.sn_locationreport) {
            Log.i(TAG, "starting location report")
            sn_Handler_m = Handler()
            start_sn_reporting()
        }

    }

    override fun onRestart() {
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        anim.setIcon(MyApplication.ICON_PLAY)
        restartedZoom = true
        numPanesArr.forEach {
            wxgltextArr[it].initTV(this)
            wxgltextArr[it].addTV()
        }
        // spotter code is serialized for now
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)
            getContentSerial()
        else
            getContentParallel()
        if (MyApplication.wxoglLocationAutorefresh) {
            mInterval = 60000 * Utility.readPref(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager?.let {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000.toLong(), 30.toFloat(), locationListener)
            }
            if (MyApplication.wxoglkeepscreenon) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            mHandler = Handler()
            startRepeatingTask()
        }
        if (MyApplication.sn_locationreport) {
            Log.i(TAG, "starting location report")
            sn_Handler_m = Handler()
            start_sn_reporting()
        }

        super.onRestart()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        lateinit var glv: WXGLSurfaceView
        lateinit var ogl: WXGLRender
        var z = 0

        fun setVars(glvg: WXGLSurfaceView, OGLRg: WXGLRender, zee: Int) {
            this.glv = glvg
            this.ogl = OGLRg
            this.z = zee
        }

        override fun onPreExecute() {
            if ((oglrArr[z].product == "N0Q" || oglrArr[z].product == "N1Q" || oglrArr[z].product == "N2Q" || oglrArr[z].product == "N3Q" || oglrArr[z].product == "L2REF") && WXGLNexrad.isRIDTDWR(oglrArr[z].rid)) oglrArr[z].product = "TZL"
            if (oglrArr[z].product == "TZL" && !WXGLNexrad.isRIDTDWR(oglrArr[z].rid)) oglrArr[z].product = "N0Q"
            if ((oglrArr[z].product == "N0U" || oglrArr[z].product == "N1U" || oglrArr[z].product == "N2U" || oglrArr[z].product == "N3U" || oglrArr[z].product == "L2VEL") && WXGLNexrad.isRIDTDWR(oglrArr[z].rid)) oglrArr[z].product = "TV0"
            if (oglrArr[z].product == "TV0" && !WXGLNexrad.isRIDTDWR(oglrArr[z].rid)) oglrArr[z].product = "N0U"
            //prodArr[z] = WXGLNexrad.checkTdwrProd(prodArr[z],WXGLNexrad.isRIDTDWR(rid1Arr[z]))
            toolbar.subtitle = ""
            setToolbarTitle()
            initWXOGLGeom(glv, ogl, z)
        }

        override fun doInBackground(vararg params: String): String {
            ogl.constructPolygons("", "", true)
            if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) {
                ogl.constructSpotters()
            } else {
                ogl.deconstructSpotters()
            }
            if (PolygonType.STI.pref)
                ogl.constructSTILines()
            else
                ogl.deconstructSTILines()
            if (PolygonType.HI.pref)
                ogl.constructHI()
            else
                ogl.deconstructHI()
            if (PolygonType.TVS.pref)
                ogl.constructTVS()
            else
                ogl.deconstructTVS()
            if (MyApplication.locdotFollowsGps) {
                getGPSFromDouble()
                locXCurrent = latlonArr[0]
                locYCurrent = latlonArr[1]
            }
            if (PolygonType.LOCDOT.pref || MyApplication.locdotFollowsGps)
                ogl.constructLocationDot(locXCurrent, locYCurrent, false)
            else
                ogl.deconstructLocationDot()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            if (!oglInView) {
                glviewShow()
                oglInView = true
            }
            if (ridChanged && !restartedZoom) ridChanged = false
            if (restartedZoom) {
                restartedZoom = false
                ridChanged = false
            }
            if (PolygonType.SPOTTER_LABELS.pref) UtilityWXGLTextObject.updateSpotterLabels(numPanes, wxgltextArr)
            glv.requestRender()
            setSubTitle()
            animRan = false
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AnimateRadar : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            if (!oglInView) {
                glviewShow()
                oglInView = true
            }
            inOglAnim = true
            animRan = true
        }

        override fun doInBackground(vararg params: String): String {
            var fh: File
            var timeMilli: Long
            var priorTime: Long
            val frameCntStr = params[0]
            frameCntStrGlobal = frameCntStr
            val animArray = Array(numPanes) { Array(frameCntStr.toIntOrNull() ?: 0) { _ -> "" } }
            numPanesArr.forEach { z ->
                animArray[z] = oglrArr[z].rdDownload.getRadarByFTPAnimation(contextg, frameCntStr).toTypedArray()
                try {
                    (0 until animArray[z].size).forEach { r ->
                        fh = File(contextg.filesDir, animArray[z][r])
                        contextg.deleteFile((z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())
                        if (!fh.renameTo(File(contextg.filesDir, (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())))
                            UtilityLog.d("wx", "Problem moving to " + (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())
                    }
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                }
            }
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    numPanesArr.forEach { z ->
                        animArray[z] = oglrArr[z].rdDownload.getRadarByFTPAnimation(contextg, frameCntStr).toTypedArray()
                        try {
                            (0 until animArray[z].size).forEach { r ->
                                fh = File(contextg.filesDir, animArray[z][r])
                                contextg.deleteFile((z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())
                                if (!fh.renameTo(File(contextg.filesDir, (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())))
                                    UtilityLog.d("wx", "Problem moving to " + (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())
                            }
                        } catch (e: Exception) {
                            UtilityLog.HandleException(e)
                        }
                    }
                    animTriggerDownloads = false
                }
                for (r in 0 until animArray[0].size) {
                    while (inOglAnimPaused) SystemClock.sleep(delay.toLong())
                    // formerly priorTime was set at the end but that is goofed up with pause
                    priorTime = System.currentTimeMillis()
                    // added because if paused and then another icon life vel/ref it won't load correctly, likely
                    // timing issue
                    if (!inOglAnim) break
                    if (loopCnt > 0) {
                        numPanesArr.forEach { z ->
                            oglrArr[z].constructPolygons((z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString(), "", false)
                        }
                    } else {
                        numPanesArr.forEach { z ->
                            oglrArr[z].constructPolygons((z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString(), "", true)
                        }
                    }
                    publishProgress((r + 1).toString(), (animArray[0].size).toString())
                    numPanesArr.forEach { glviewArr[it].requestRender() }
                    timeMilli = System.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay) SystemClock.sleep(delay - ((timeMilli - priorTime)))
                    if (!inOglAnim) break
                    if (r == (animArray[0].size - 1)) SystemClock.sleep(delay.toLong() * 2)
                }
                loopCnt += 1
            }
            return "Executed"
        }

        override fun onProgressUpdate(vararg values: String) {
            //This method runs on the UI thread, it receives progress updates
            //from the background thread and publishes them to the status bar
            //mNotificationHelper.progressUpdate(progress[0])
            if ((values[1].toIntOrNull() ?: 0) > 1) {
                setSubTitle(values[0], values[1])
            } else {
                toolbar.subtitle = "Problem downloading"
            }
        }

        override fun onPostExecute(result: String) {
            UtilityFileManagement.deleteCacheFiles(contextg)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        UtilityUI.immersiveMode(this as Activity)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        UtilityUI.immersiveMode(this)
        if (inOglAnim && (item.itemId != R.id.action_fav) && (item.itemId != R.id.action_share) && (item.itemId != R.id.action_tools)) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            // (revert) 2016_08 have this apply to Level 3 in addition to Level 2
            if (oglrArr[0].product.contains("L2") || oglrArr[1].product.contains("L2")) SystemClock.sleep(2000)
            anim.setIcon(MyApplication.ICON_PLAY)
            if (item.itemId == R.id.action_a) return true
        }
        when (item.itemId) {
            R.id.action_help -> UtilityAlertDialog.showHelpText(resources.getString(R.string.help_radar)
                    + MyApplication.newline + MyApplication.newline
                    + resources.getString(R.string.help_radar_drawingtools)
                    + MyApplication.newline + MyApplication.newline
                    + resources.getString(R.string.help_radar_recording)
                    + MyApplication.newline + MyApplication.newline
                    , this)
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan) {
                        val animDrawable = UtilityUSImgWX.animationFromFiles(this, oglrArr[curRadar].rid, oglrArr[curRadar].product, frameCntStrGlobal, (curRadar + 1).toString(), true)
                        UtilityShare.shareAnimGif(this,
                                oglrArr[curRadar].rid + " (" + Utility.readPref(this, "RID_LOC_" + oglrArr[curRadar].rid, "")
                                        + ") " + oglrArr[curRadar].product, animDrawable)
                    } else {
                        UtilityShare.shareBitmap(this, oglrArr[curRadar].rid +
                                " (" + Utility.readPref(this, "RID_LOC_" + oglrArr[curRadar].rid, "") + ") "
                                + oglrArr[curRadar].product, UtilityUSImgWX.layeredImgFromFile(applicationContext, oglrArr[curRadar].rid, oglrArr[curRadar].product, "0", true))
                    }
                }
            }
            R.id.action_settings -> startActivity(Intent(contextg, SettingsRadarActivity::class.java))
            R.id.action_radar_markers -> ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf("raw:radar_legend", "Radar Markers", "false"))
            R.id.action_radar_site_status_l3 -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf("http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3)))
            R.id.action_radar_site_status_l2 -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf("http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2)))
            R.id.action_radar1 -> switchRadar(0)
            R.id.action_radar2 -> switchRadar(1)
            R.id.action_radar3 -> switchRadar(2)
            R.id.action_radar4 -> switchRadar(3)
            R.id.action_n0q -> {
                if (!WXGLNexrad.isRIDTDWR(oglrArr[curRadar].rid)) {
                    oglrArr[curRadar].product = "N" + tilt + "Q"
                } else {
                    oglrArr[curRadar].product = "TZL"
                }
                getContentIntelligent()
            }
            R.id.action_n0u -> {
                if (!WXGLNexrad.isRIDTDWR(oglrArr[curRadar].rid)) {
                    oglrArr[curRadar].product = "N" + tilt + "U"
                } else {
                    oglrArr[curRadar].product = "TV$tilt"
                }
                getContentIntelligent()
            }
            R.id.action_n0s -> changeProd("N" + tilt + "S")
            R.id.action_net -> changeProd("EET")
            R.id.action_N0X -> changeProd("N" + tilt + "X")
            R.id.action_N0C -> changeProd("N" + tilt + "C")
            R.id.action_N0K -> changeProd("N" + tilt + "K")
            R.id.action_H0C -> changeProd("H" + tilt + "C")
            R.id.action_about -> showRadarScanInfo()
            R.id.action_vil -> changeProd("DVL")
            R.id.action_dsp -> changeProd("DSA")
            R.id.action_daa -> changeProd("DAA")
            R.id.action_l2vel -> changeProd("L2VEL")
            R.id.action_l2ref -> changeProd("L2REF")
            R.id.action_tilt1 -> changeTilt("0")
            R.id.action_tilt2 -> changeTilt("1")
            R.id.action_tilt3 -> changeTilt("2")
            R.id.action_tilt4 -> changeTilt("3")
            R.id.action_a12 -> animateRadar("12")
            R.id.action_a18 -> animateRadar("18")
            R.id.action_a6_sm -> animateRadar("6")
            R.id.action_a -> animateRadar(MyApplication.uiAnimIconFrames)
            R.id.action_a36 -> animateRadar("36")
            R.id.action_a72 -> animateRadar("72")
            R.id.action_a144 -> animateRadar("144")
            R.id.action_a3 -> animateRadar("3")
            R.id.action_fav -> {
                if (inOglAnim) {
                    inOglAnimPaused = if (!inOglAnimPaused) {
                        star.setIcon(MyApplication.ICON_PLAY)
                        true
                    } else {
                        star.setIcon(MyApplication.ICON_PAUSE)
                        false
                    }
                }
            }
            R.id.action_TDWR -> alertDialogTDWR()
            R.id.action_ridmap -> {
                imageMap.toggleMap()
                oglInView = if (imageMap.map.visibility != View.GONE) {
                    UtilityWXGLTextObject.hideTV(numPanes, wxgltextArr)
                    false
                } else {
                    UtilityWXGLTextObject.showTV(numPanes, wxgltextArr)
                    true
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun animateRadar(frameCnt: String) {
        anim.setIcon(MyApplication.ICON_STOP)
        star.setIcon(MyApplication.ICON_PAUSE)
        AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameCnt, oglrArr[curRadar].product)
    }

    private fun changeTilt(tiltStr: String) {
        tilt = tiltStr
        oglrArr[curRadar].product = oglrArr[curRadar].product.replace("N[0-3]".toRegex(), "N$tilt")
        title = oglrArr[curRadar].product
        getContent(glviewArr[curRadar], oglrArr[curRadar], curRadar)
    }

    private fun changeProd(prodF: String) {
        oglrArr[curRadar].product = prodF
        getContentIntelligent()
    }

    private fun switchRadar(radarNumber: Int) {
        curRadar = radarNumber
        idxIntAl = radarNumber
        setToolbarTitle()
    }

    private fun ridMapSwitch(r: String) {
        mapShown = false
        UtilityWXGLTextObject.showTV(numPanes, wxgltextArr)
        if (inOglAnim) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            if (oglrArr[0].product.contains("L2") || oglrArr[1].product.contains("L2")) SystemClock.sleep(2000)
            anim.setIcon(MyApplication.ICON_PLAY)
        }

        if (MyApplication.dualpaneshareposn) {
            // if one long presses change the currently active radar as well
            curRadar = idxIntAl
            numPanesArr.forEach {
                oglrArr[it].rid = r
                glviewArr[it].scaleFactor = MyApplication.wxoglSize / 10.0f
                oglrArr[it].setViewInitial(MyApplication.wxoglSize / 10.0f, 0.0f, 0.0f)
            }
        } else {
            // if one long presses change the currently active radar as well
            curRadar = idxIntAl
            oglrArr[idxIntAl].rid = r
            glviewArr[idxIntAl].scaleFactor = MyApplication.wxoglSize / 10.0f
            oglrArr[idxIntAl].setViewInitial(MyApplication.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)
            getContentSerial()
        else
            getContentParallel()
    }

    private fun showRadarScanInfo() {
        var scanInfo = ""
        numPanesArr.forEach {
            infoArr[it] = Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "")
            scanInfo = scanInfo + infoArr[it] + MyApplication.newline + MyApplication.newline
        }
        UtilityAlertDialog.showHelpText(scanInfo, this)
    }


    override fun onStop() {
        super.onStop()
        numPanesArr.forEach { WXGLNexrad.savePrefs(this, prefPrefix, it + 1, oglrArr[it]) }
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        mHandler?.let { stopRepeatingTask() }
        sn_Handler_m?.let { stop_sn_reporting() }
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                it.removeUpdates(locationListener)
        }
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            idxIntAl = idxInt
            if (progress != 50000) {
                alertDialogStatusAl.clear()
                var dist = 0.0
                var distRid = 0.0
                val locX: Double
                val locY: Double
                val pointX: Double
                val pointY: Double
                val ridX: Double
                val ridY: Double
                // FIXME remove try and combine var decl
                try {
                    locX = locXCurrent.toDouble()
                    locY = locYCurrent.toDouble()
                    pointX = glviewArr[idxInt].newY.toDouble()
                    pointY = (glviewArr[idxInt].newX * -1).toDouble()
                    dist = LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
                    ridX = (Utility.readPref(contextg, "RID_" + oglrArr[idxIntAl].rid + "_X", "0.0")).toDouble()
                    ridY = -1.0 * (Utility.readPref(contextg, "RID_" + oglrArr[idxIntAl].rid + "_Y", "0.0")).toDouble()
                    distRid = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                }
                diaStatus!!.setTitle(UtilityStringExternal.truncate((glviewArr[idxInt].newX).toString(), 6)
                        + ",-" + UtilityStringExternal.truncate((glviewArr[idxInt].newY).toString(), 6))
                alertDialogStatusAl.add(UtilityStringExternal.truncate(dist.toString(), 6) + " miles from location")
                alertDialogStatusAl.add(UtilityStringExternal.truncate(distRid.toString(), 6) + " miles from " + oglrArr[idxIntAl].rid)
                oglrArr[idxIntAl].ridNewList.mapTo(alertDialogStatusAl) {
                    "Radar: (" + it.distance + " mi) " + it.name + " " + Utility.readPref(contextg, "RID_LOC_" + it.name, "")
                }
                alertDialogStatusAl.add("Show warning text")
                alertDialogStatusAl.add("Show nearest observation")
                alertDialogStatusAl.add("Show nearest meteogram")
                alertDialogStatusAl.add("Show Spotter Info")
                alertDialogStatusAl.add("Show radar status message")
                diaStatus!!.show()
            } else {
                numPanesArr.forEach { wxgltextArr[it].addTV() }
            }
        }
    }

    private fun initGLVIEW(glv: WXGLSurfaceView, ogl: WXGLRender) {
        glv.setEGLContextClientVersion(2)
        //glv.setEGLConfigChooser(8, 8, 8, 8, 16, 0) // a test to see if android emulator will now work
        glv.setRenderer(ogl)
        glv.setRenderVar(ogl, oglrArr, glviewArr, act)
        glv.fullScreen = false
        glv.setOnProgressChangeListener(changeListener)
        glv.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glv.toolbar = toolbar
        glv.toolbarBottom = toolbarBottom
    }

    private fun initWXOGLGeom(glv: WXGLSurfaceView, ogl: WXGLRender, z: Int) {
        ogl.initGEOM()
        if (oldRidArr[z] != oglrArr[z].rid) {
            ogl.setChunkCount(0)
            ogl.setChunkCountSti(0)
            ogl.setHiInit(false)
            ogl.setTvsInit(false)
            Thread(Runnable {
                ogl.constructStateLines()
                glv.requestRender()
            }).start()
            Thread(Runnable {
                if (GeographyType.LAKES.pref)
                    ogl.constructLakes()
                else
                    ogl.deconstructLakes()
            }).start()
            Thread(Runnable {
                if (GeographyType.COUNTY_LINES.pref) {
                    ogl.constructCounty()
                    glv.requestRender()
                } else
                    ogl.deconstructCounty()
            }).start()
            Thread(Runnable {
                if (GeographyType.HIGHWAYS.pref) {
                    ogl.constructHWLines()
                    glv.requestRender()
                } else
                    ogl.deconstructHWLines()
            }).start()
            Thread(Runnable {
                if (MyApplication.radarHwEnhExt) {
                    ogl.constructHWEXTLines()
                    glv.requestRender()
                } else
                    ogl.deconstructHWEXTLines()
            }).start()
            //wxgltextArr[z].setRid(rid)
            wxgltextArr[z].addTV()
            oldRidArr[z] = oglrArr[z].rid
        }

        Thread(Runnable {
            if (PolygonType.TOR.pref)
                ogl.constructTorWarningLines()
            else
                ogl.deconstructTorWarningLines()

            if (PolygonType.SVR.pref)
                ogl.constructSvrWarningLines()
            else
                ogl.deconstructSvrWarningLines()

            if (PolygonType.EWW.pref)
                ogl.constructEwwWarningLines()
            else
                ogl.deconstructEwwWarningLines()

            if (PolygonType.FFW.pref)
                ogl.constructFfwWarningLines()
            else
                ogl.deconstructFfwWarningLines()

            if (PolygonType.SMW.pref)
                ogl.constructSmwWarningLines()
            else
                ogl.deconstructSmwWarningLines()

            if (PolygonType.SVS.pref)
                ogl.constructSvsWarningLines()
            else
                ogl.deconstructSvsWarningLines()

            if (PolygonType.SPS.pref)
                ogl.constructSpsWarningLines()
            else
                ogl.deconstructSpsWarningLines()

            if (PolygonType.MCD.pref)
                ogl.constructWATMCDLines()
            else
                ogl.deconstructWATMCDLines()
            if (PolygonType.MPD.pref)
                ogl.constructMPDLines()
            else
                ogl.deconstructMPDLines()
            glv.requestRender()
        }).start()
        if (MyApplication.locdotFollowsGps) {
            getGPSFromDouble()
            locXCurrent = latlonArr[0]
            locYCurrent = latlonArr[1]
        }
        if (PolygonType.LOCDOT.pref || MyApplication.locdotFollowsGps)
            ogl.constructLocationDot(locXCurrent, locYCurrent, false)
        else
            ogl.deconstructLocationDot()
        if (imageMap.map.visibility != View.VISIBLE) {
            numPanesArr.forEach { glviewArr[it].visibility = View.VISIBLE }
        }
    }

    private val handler = Handler()

    private val mStatusChecker: Runnable? = object : Runnable {
        override fun run() {
            if (loopCount > 0) {
                if (inOglAnim) {
                    animTriggerDownloads = true
                } else {
                    numPanesArr.forEach { getContentSingleThreaded(glviewArr[it], oglrArr[it], it) }
                }
            }
            loopCount += 1
            handler.postDelayed(this, mInterval.toLong())
        }
    }

    private fun startRepeatingTask() {
        mStatusChecker!!.run()
    }

    private fun stopRepeatingTask() {
        mHandler!!.removeCallbacks(mStatusChecker)
    }

    //report your spotter network location
    private val sn_handler = Handler()
    private val sn_reporter: Runnable = object : Runnable {
        override fun run() {

            ///GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

            Log.i(TAG, "SendPosition(contextg) on lat: "+latD+" lon: "+lonD)
            SendPosition(contextg)

            sn_handler.postDelayed(this, sn_Interval.toLong())
        }
    }

    private fun start_sn_reporting() {
        sn_reporter.run()
    }

    private fun stop_sn_reporting() {
        sn_handler!!.removeCallbacks(sn_reporter)
    }


    override fun onPause() {
        numPanesArr.forEach { glviewArr[it].onPause() }
        super.onPause()
    }

    override fun onResume() {
        numPanesArr.forEach { glviewArr[it].onResume() }
        super.onResume()
    }

    private fun setToolbarTitle() {
        if (numPanes == 4) {
            title = if (MyApplication.dualpaneshareposn) {
                (curRadar + 1).toString() + ":" + oglrArr[0].rid + "(" + oglrArr[0].product + "," + oglrArr[1].product + "," + oglrArr[2].product + "," + oglrArr[3].product + ")"
            } else {
                (curRadar + 1).toString() + ": " + oglrArr[0].rid + "(" + oglrArr[0].product + ") " + oglrArr[1].rid + "(" + oglrArr[1].product + ") " + oglrArr[2].rid + "(" + oglrArr[2].product + ") " + oglrArr[3].rid + "(" + oglrArr[3].product + ")"
            }
        } else if (numPanes == 2) {
            title = if (MyApplication.dualpaneshareposn) {
                (curRadar + 1).toString() + ":" + oglrArr[0].rid + "(" + oglrArr[0].product + "," + oglrArr[1].product + ")"
            } else {
                (curRadar + 1).toString() + ": " + oglrArr[0].rid + "(" + oglrArr[0].product + ") " + oglrArr[1].rid + "(" + oglrArr[1].product + ") "
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            makeUseOfNewLocation(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun makeUseOfNewLocation(location: Location) {
        latD = location.latitude
        lonD = location.longitude
        getGPSFromDouble()
        locXCurrent = latlonArr[0]
        locYCurrent = latlonArr[1]
        numPanesArr.forEach {
            oglrArr[it].constructLocationDot(locXCurrent, locYCurrent, false)
            glviewArr[it].requestRender()
        }
    }

    private fun getGPSFromDouble() {
        try {
            latlonArr[0] = latD.toString()
            latlonArr[1] = lonD.toString()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    private fun alertDialogStatus() {
        diaStatus = ObjectDialogue(contextg, alertDialogStatusAl)
        diaStatus!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(act)
        })
        diaStatus!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogStatusAl[which]
            dialog.dismiss()
            if (strName.contains("Radar:")) {
                val ridNew = strName.parse("\\) ([A-Z]{3,4}) ")
                if (MyApplication.dualpaneshareposn) {
                    numPanesArr.forEach {
                        oglrArr[it].rid = ridNew
                        oglrArr[it].rid = ridNew
                    }
                    ridChanged = true
                    ridMapSwitch(oglrArr[curRadar].rid)
                } else {
                    oglrArr[idxIntAl].rid = ridNew
                    ridChanged = true
                    ridMapSwitch(oglrArr[idxIntAl].rid)
                }
            } else if (strName.contains("Show warning text")) {
                val polygonUrl = UtilityWXOGL.showTextProducts(glviewArr[idxIntAl].newY.toDouble(), glviewArr[idxIntAl].newX.toDouble() * -1.0)
                if (polygonUrl != "") ObjectIntent(contextg, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, arrayOf(polygonUrl, ""))
            } else if (strName.contains("Show nearest observation")) {
                idxIntG = idxIntAl
                GetMetar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            } else if (strName.contains("Show nearest meteogram")) {
                // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
                idxIntG = idxIntAl
                val obsSite = UtilityMetar.findClosestObservation(contextg, LatLon(glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0))
                ObjectIntent(contextg, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram"))

            }
            else if (strName.contains("Show Spotter Info")) {
                GetSpotter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            else if (strName.contains("Show radar status message")) {
                GetRadarStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        })
    }

    private fun alertDialogTDWR() {
        val diaTdwr = ObjectDialogue(contextg, TDWR_RIDS)
        diaTdwr.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(act)
        })
        diaTdwr.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = TDWR_RIDS[which]
            oglrArr[curRadar].rid = MyApplication.space.split(strName)[0]
            if (oglrArr[curRadar].product == "N0Q")
                oglrArr[curRadar].product = "TZL"
            else
                oglrArr[curRadar].product = "TV0"
            ridMapSwitch(oglrArr[curRadar].rid)
            getContent(glviewArr[curRadar], oglrArr[curRadar], curRadar)
            dialog.dismiss()
        })
        diaTdwr.show()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetRadarStatus : AsyncTask<String, String, String>() {

        var radarStatus = ""

        override fun doInBackground(vararg params: String): String {
            radarStatus = UtilityDownload.getRadarStatusMessage(contextg, oglrArr[idxIntAl].rid)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)
        }
    }

    private fun getContentSerial() {
        numPanesArr.forEach { getContentSingleThreaded(glviewArr[it], oglrArr[it], it) }
    }

    private fun getContentParallel() {
        numPanesArr.forEach { getContent(glviewArr[it], oglrArr[it], it) }
    }

    private fun glviewShow() {
        numPanesArr.forEach {
            glviewArr[it].visibility = View.VISIBLE
            rlArr[it].visibility = View.VISIBLE
        }
    }

    private fun getContentIntelligent() {
        if (MyApplication.dualpaneshareposn) {
            if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref)
                getContentSerial()
            else
                getContentParallel()
        } else {
            getContent(glviewArr[curRadar], oglrArr[curRadar], curRadar)
        }
    }

    private fun setSubTitle() {
        if (numPanes == 4) {
            numPanesArr.forEach { infoArr[it] = Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "") }
            tmpArr1 = MyApplication.space.split(infoArr[0])
            tmpArr2 = MyApplication.space.split(infoArr[1])
            tmpArr3 = MyApplication.space.split(infoArr[2])
            tmpArr4 = MyApplication.space.split(infoArr[3])
            if (tmpArr1.size > 3 && tmpArr2.size > 3 && tmpArr3.size > 3 && tmpArr4.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3] + "/" + tmpArr3[3] + "/" + tmpArr4[3]
            else
                toolbar.subtitle = ""
        } else if (numPanes == 2) {
            numPanesArr.forEach { infoArr[it] = Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "") }
            tmpArr1 = MyApplication.space.split(infoArr[0])
            tmpArr2 = MyApplication.space.split(infoArr[1])
            if (tmpArr1.size > 3 && tmpArr2.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3]
            else
                toolbar.subtitle = ""
        }
    }

    // used for animations - BUG: code is not used as animations are not working across all 4 ( commented out code not working )
    private fun setSubTitle(a: String, b: String) {
        if (numPanes == 4) {
            numPanesArr.forEach { infoAnim[it] = Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "") }
            tmpArr1 = MyApplication.space.split(infoAnim[0])
            tmpArr2 = MyApplication.space.split(infoAnim[1])
            tmpArr3 = MyApplication.space.split(infoAnim[2])
            tmpArr4 = MyApplication.space.split(infoAnim[3])
            if (tmpArr1.size > 3 && tmpArr2.size > 3 && tmpArr3.size > 3 && tmpArr4.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3] + "/" + tmpArr3[3] + "/" + tmpArr4[3] + "(" + a + "/" + b + ")"
            else
                toolbar.subtitle = ""
        } else if (numPanes == 2) {
            numPanesArr.forEach { infoAnim[it] = Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "") }
            tmpArr1 = MyApplication.space.split(infoAnim[0])
            tmpArr2 = MyApplication.space.split(infoAnim[1])
            if (tmpArr1.size > 3 && tmpArr2.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3] + "/" + "(" + a + "/" + b + ")"
            else
                toolbar.subtitle = ""
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetMetar : AsyncTask<String, String, String>() {

        var txt = ""

        override fun doInBackground(vararg params: String): String {
            txt = UtilityMetar.findClosestMetar(contextg, LatLon(glviewArr[idxIntG].newY.toDouble(), (glviewArr[idxIntG].newX * -1).toDouble()))
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityAlertDialog.showHelpText(txt, act)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetSpotter : AsyncTask<String, String, String>() {

        var txt = ""

        override fun doInBackground(vararg params: String): String {
            txt = UtilitySpotter.findClosestSpotter(LatLon(glviewArr[idxIntG].newY.toDouble(), (glviewArr[idxIntG].newX * -1).toDouble()))
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityAlertDialog.showHelpText(txt, act)
        }
    }

    private fun getContent(glvg: WXGLSurfaceView, OGLRg: WXGLRender, curRadar: Int) {
        val gc = GetContent()
        gc.setVars(glvg, OGLRg, curRadar)
        gc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun getContentSingleThreaded(glvg: WXGLSurfaceView, OGLRg: WXGLRender, curRadar: Int) {
        val gc = GetContent()
        gc.setVars(glvg, OGLRg, curRadar)
        gc.execute()
    }
}

