/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
//modded by ELY M.   

package joshuatee.wx.radar

import android.annotation.SuppressLint
import java.io.File

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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

import joshuatee.wx.*
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.WebscreenABModels
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.settings.SettingsRadarActivity
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*

import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.radar.SpotterNetworkPositionReport.SendPosition
import kotlinx.coroutines.*

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
    // 4: coming from single pane

    companion object {
        const val RID: String = ""
    }

    private var TAG = "joshuatee-WXGLRadarActivityMultiPane"
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
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
    private var frameCountGlobal = 0
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
    private val alertDialogStatusAl = mutableListOf<String>()
    private lateinit var contextg: Context
    private var idxIntAl = 0
    private var prefPrefix = "WXOGL_DUALPANE"
    private var rlArr = mutableListOf<RelativeLayout>()
    private var wxgltextArr = mutableListOf<WXGLTextObject>()
    private lateinit var act: Activity
    private var alertDialogRadarLongPress: ObjectDialogue? = null
    private var dontSavePref = false
    private var useSinglePanePref = false
    private var landScape = false

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        val activityArguments = intent.getStringArrayExtra(RID)
        if (activityArguments.size > 3 ) {
            if (activityArguments[3] == "true") {
                dontSavePref = true
                useSinglePanePref = true
            }
        }
        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            landScape = true
        }
        numPanes = activityArguments[2].toIntOrNull() ?: 0
        numPanesArr = (0 until numPanes).toList()
        UtilityFileManagement.deleteCacheFiles(this)
        var widthDivider = 1
        var heightDivider = 2
        val layoutType: Int
        if (numPanes == 2) {
            if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent) {
                if (landScape) {
                    layoutType = R.layout.activity_uswxoglmultipane_immersive_landscape
                    heightDivider = 1
                } else {
                    layoutType = R.layout.activity_uswxoglmultipane_immersive
                }
            } else {
                if (landScape) {
                    layoutType = R.layout.activity_uswxoglmultipane_immersive_landscape
                    heightDivider = 1
                } else {
                    layoutType = R.layout.activity_uswxoglmultipane
                }
            }
            super.onCreate(
                    savedInstanceState,
                    layoutType,
                    R.menu.uswxoglradarmultipane,
                    iconsEvenlySpaced = true,
                    bottomToolbar = true
            )
        } else {
            layoutType = if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent) {
                R.layout.activity_uswxoglmultipane_quad_immersive
            } else {
                R.layout.activity_uswxoglmultipane_quad
            }
            super.onCreate(
                    savedInstanceState,
                    layoutType,
                    R.menu.uswxoglradarmultipane,
                    iconsEvenlySpaced = true,
                    bottomToolbar = true
            )
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        act = this
        UtilityUI.immersiveMode(this as Activity)
        locXCurrent = joshuatee.wx.settings.Location.x
        locYCurrent = joshuatee.wx.settings.Location.y
        infoAnim = Array(numPanes) { "" }
        oldRidArr = Array(numPanes) { "" }
        infoArr = Array(numPanes) { "" }

        if (numPanes == 4) {
            widthDivider = 2
            prefPrefix = "WXOGL_QUADPANE"
        }
        contextg = this
        if (MyApplication.checkinternet) {
            Utility.checkInternet(contextg)
        }
        if (useSinglePanePref) {
            prefPrefix = "WXOGL"
        }
        setupAlertDialogRadarLongPress()
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
        if (Build.VERSION.SDK_INT < 21) menu.findItem(R.id.action_share).title = "Share"
        delay = UtilityImg.animInterval(this)
        numPanesArr.forEach {
            glviewArr.add(WXGLSurfaceView(this, widthDivider, numPanes, heightDivider))
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
                    params.height = MyApplication.dm.heightPixels / 2 +
                            UtilityUI.statusBarHeight(this)
                else
                    params.height = MyApplication.dm.heightPixels /
                            2 - MyApplication.actionBarHeight /
                            2 - UtilityUI.statusBarHeight(this) / 2 -
                            (UtilityUI.navigationBarHeight(this) / 2.0).toInt()
                if (Build.VERSION.SDK_INT >= 19
                        && UIPreferences.radarToolbarTransparent
                        && !UIPreferences.radarImmersiveMode
                        && numPanes == 4
                )
                    params.height = MyApplication.dm.heightPixels / 2 - UtilityUI.statusBarHeight(
                            this
                    ) / 2

                params.width = MyApplication.dm.widthPixels / 2
            }
        } else if (numPanes == 2) {
            numPanesArr.forEach {
                rlArr.add(findViewById(elementIds[it]))
                rlArr[it].addView(glviewArr[it])
                val params = rlArr[it].layoutParams
                if (!landScape) {
                    params.height = MyApplication.dm.heightPixels / 2 -
                            (MyApplication.actionBarHeight / 2) - UtilityUI.statusBarHeight(this) / 2 -
                            (UtilityUI.navigationBarHeight(this) / 2.0).toInt()
                    params.width = MyApplication.dm.widthPixels
                } else {
                    params.width = MyApplication.dm.widthPixels
                    params.height = MyApplication.dm.heightPixels
                }
            }
        }
        numPanesArr.forEach {
            UtilityRadarUI.initGlview(
                    glviewArr[it],
                    glviewArr,
                    oglrArr[it],
                    oglrArr,
                    act,
                    toolbar,
                    toolbarBottom,
                    changeListener
            )
        }
        imageMap = ObjectImageMap(
                this,
                this,
                R.id.map,
                toolbar,
                toolbarBottom,
                rlArr.toList() as List<View> + glviewArr.toList() as List<View>
        )
        imageMap.addClickHandler(::ridMapSwitch, UtilityImageMap::maptoRid)
        oglInView = true
        numPanesArr.forEach {
            if (!useSinglePanePref) {
                oglrArr[it].rid = Utility.readPref(
                        this,
                        prefPrefix + "_RID" + (it + 1).toString(),
                        activityArguments[0]
                )
            } else {
                oglrArr[it].rid = Utility.readPref(
                        this,
                        prefPrefix + "_RID",
                        activityArguments[0]
                )
            }
        }
        if (MyApplication.dualpaneshareposn) {
            (1 until numPanes).forEach { oglrArr[it].rid = oglrArr[0].rid }
        }
        numPanesArr.forEach { oldRidArr[it] = "" }
        val defaultProducts = listOf("N0Q", "N0U", "N0C", "DVL")
        (0 until numPanes).forEach {
            oglrArr[it].product = Utility.readPref(
                    this,
                    prefPrefix + "_PROD" + (it + 1).toString(),
                    defaultProducts[it]
            )
        }
        var zoomPref = "_ZOOM1"
        var xPref = "_X1"
        var yPref = "_Y1"
        if (useSinglePanePref) {
            zoomPref = "_ZOOM"
            xPref = "_X"
            yPref = "_Y"
        }
        glviewArr[0].scaleFactor = Utility.readPref(
                this,
                prefPrefix + zoomPref,
                MyApplication.wxoglSize.toFloat() / 10.0f
        )
        oglrArr[0].setViewInitial(
                Utility.readPref(
                        this,
                        prefPrefix + zoomPref,
                        MyApplication.wxoglSize.toFloat() / 10.0f
                ),
                Utility.readPref(this, prefPrefix + xPref, 0.0f),
                Utility.readPref(this, prefPrefix + yPref, 0.0f)
        )
        if (MyApplication.dualpaneshareposn) {
            (1 until numPanes).forEach {
                glviewArr[it].scaleFactor = glviewArr[0].scaleFactor
                oglrArr[it].setViewInitial(
                        Utility.readPref(
                                this,
                                prefPrefix + zoomPref,
                                MyApplication.wxoglSize.toFloat() / 10.0f
                        ),
                        oglrArr[0].x, oglrArr[0].y
                )
            }
        } else {
            (1 until numPanes).forEach {
                glviewArr[it].scaleFactor = Utility.readPref(
                        this,
                        prefPrefix + "_ZOOM" + (it + 1).toString(),
                        MyApplication.wxoglSize.toFloat() / 10.0f
                )
                oglrArr[it].setViewInitial(
                        Utility.readPref(
                                this,
                                prefPrefix + "_ZOOM" + (it + 1).toString(),
                                MyApplication.wxoglSize.toFloat() / 10.0f
                        ),
                        Utility.readPref(this, prefPrefix + "_X" + (it + 1).toString(), 0.0f),
                        Utility.readPref(this, prefPrefix + "_Y" + (it + 1).toString(), 0.0f)
                )
            }
        }
        numPanesArr.forEach {
            wxgltextArr.add(WXGLTextObject(this, rlArr[it], glviewArr[it], oglrArr[it], numPanes))
            glviewArr[it].wxgltextArr = wxgltextArr
            wxgltextArr[it].initTV(this)
        }
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) {
            getContentSerial()
        } else {
            getContentParallel()
        }
        checkForAutoRefresh()
    }

    private fun checkForAutoRefresh() {
        if (MyApplication.wxoglRadarAutorefresh) {
            mInterval = 60000 * Utility.readPref(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (gpsEnabled != null && gpsEnabled) {
                    locationManager?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            (MyApplication.radarLocationUpdateInterval * 1000).toLong(),
                            MyApplication.radarLocationUpdateDistanceInMeters.toFloat(),
                            locationListener
                    )
                }
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
            if (imageMap.map.visibility == View.GONE) {
                wxgltextArr[it].initTV(this)
                wxgltextArr[it].addTV()
            }
        }
        // spotter code is serialized for now
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) {
            getContentSerial()
        } else {
            getContentParallel()
        }
        checkForAutoRefresh()
        super.onRestart()
    }

    private fun getContent(glv: WXGLSurfaceView, ogl: WXGLRender, z: Int) =
            GlobalScope.launch(uiDispatcher) {
                if ((oglrArr[z].product == "N0Q" || oglrArr[z].product == "N1Q" || oglrArr[z].product == "N2Q" || oglrArr[z].product == "N3Q" || oglrArr[z].product == "L2REF") && WXGLNexrad.isRidTdwr(
                                oglrArr[z].rid
                        )
                ) oglrArr[z].product = "TZL"
                if (oglrArr[z].product == "TZL" && !WXGLNexrad.isRidTdwr(oglrArr[z].rid)) oglrArr[z].product =
                        "N0Q"
                if ((oglrArr[z].product == "N0U" || oglrArr[z].product == "N1U" || oglrArr[z].product == "N2U" || oglrArr[z].product == "N3U" || oglrArr[z].product == "L2VEL") && WXGLNexrad.isRidTdwr(
                                oglrArr[z].rid
                        )
                ) oglrArr[z].product = "TV0"
                if (oglrArr[z].product == "TV0" && !WXGLNexrad.isRidTdwr(oglrArr[z].rid)) oglrArr[z].product =
                        "N0U"
                toolbar.subtitle = ""
                setToolbarTitle()
                UtilityRadarUI.initWxoglGeom(
                        glv,
                        ogl,
                        z,
                        oldRidArr,
                        oglrArr,
                        wxgltextArr,
                        numPanesArr,
                        imageMap,
                        glviewArr,
                        ::getGPSFromDouble,
                        ::getLatLon
                )
                withContext(Dispatchers.IO) {
                    UtilityRadarUI.plotRadar(
                            ogl,
                            "",
                            contextg,
                            ::getGPSFromDouble,
                            ::getLatLon,
                            false
                    )
                }
                if (!oglInView) {
                    glviewShow()
                    oglInView = true
                }
                if (ridChanged && !restartedZoom) ridChanged = false
                if (restartedZoom) {
                    restartedZoom = false
                    ridChanged = false
                }
                if (PolygonType.SPOTTER_LABELS.pref) UtilityWXGLTextObject.updateSpotterLabels(
                        numPanes,
                        wxgltextArr
                )
            //FIXME make obs texts work proper on multi-pane
            if (PolygonType.OBS.pref) UtilityWXGLTextObject.updateObs(
                    numPanes,
                    wxgltextArr
            )
	    //TODO test me in multi-pane
            if (PolygonType.HAIL_LABELS.pref) UtilityWXGLTextObject.updateHailLabels(
                    numPanes,
                    wxgltextArr
            )
                glv.requestRender()
                setSubTitle()
                animRan = false
            }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        if (!oglInView) {
            glviewShow()
            oglInView = true
        }
        inOglAnim = true
        animRan = true
        withContext(Dispatchers.IO) {
            var fh: File
            var timeMilli: Long
            var priorTime: Long
            frameCountGlobal = frameCount
            val animArray = Array(numPanes) { Array(frameCount) { "" } }
            numPanesArr.forEach { z ->
                animArray[z] = oglrArr[z].rdDownload.getRadarFilesForAnimation(contextg, frameCount)
                        .toTypedArray()
                try {
                    (0 until animArray[z].size).forEach { r ->
                        fh = File(contextg.filesDir, animArray[z][r])
                        contextg.deleteFile((z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())
                        if (!fh.renameTo(
                                        File(
                                                contextg.filesDir,
                                                (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString()
                                        )
                                )
                        )
                            UtilityLog.d(
                                    "wx",
                                    "Problem moving to " + (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString()
                            )
                    }
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                }
            }
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    numPanesArr.forEach { z ->
                        animArray[z] =
                                oglrArr[z].rdDownload.getRadarFilesForAnimation(
                                        contextg,
                                        frameCount
                                )
                                        .toTypedArray()
                        try {
                            (0 until animArray[z].size).forEach { r ->
                                fh = File(contextg.filesDir, animArray[z][r])
                                contextg.deleteFile((z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString())
                                if (!fh.renameTo(
                                                File(
                                                        contextg.filesDir,
                                                        (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString()
                                                )
                                        )
                                )
                                    UtilityLog.d(
                                            "wx",
                                            "Problem moving to " + (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString()
                                    )
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
                            oglrArr[z].constructPolygons(
                                    (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString(),
                                    "",
                                    false
                            )
                        }
                    } else {
                        numPanesArr.forEach { z ->
                            oglrArr[z].constructPolygons(
                                    (z + 1).toString() + oglrArr[z].product + "nexrad_anim" + r.toString(),
                                    "",
                                    true
                            )
                        }
                    }
                    launch(uiDispatcher) {
                        progressUpdate((r + 1).toString(), (animArray[0].size).toString())
                    }
                    numPanesArr.forEach { glviewArr[it].requestRender() }
                    timeMilli = System.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay)
                        SystemClock.sleep(delay - ((timeMilli - priorTime)))
                    if (!inOglAnim)
                        break
                    if (r == (animArray[0].size - 1))
                        SystemClock.sleep(delay.toLong() * 2)
                }
                loopCnt += 1
            }
        }
        UtilityFileManagement.deleteCacheFiles(contextg)
    }

    private fun progressUpdate(vararg values: String) {
        if ((values[1].toIntOrNull() ?: 0) > 1) {
            setSubTitle(values[0], values[1])
        } else {
            toolbar.subtitle = "Problem downloading"
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
            if (oglrArr[0].product.contains("L2") || oglrArr[1].product.contains("L2")) SystemClock.sleep(
                    2000
            )
            anim.setIcon(MyApplication.ICON_PLAY)
            if (item.itemId == R.id.action_a) return true
        }
        when (item.itemId) {
            R.id.action_help -> UtilityAlertDialog.showHelpText(
                    resources.getString(R.string.help_radar)
                            + MyApplication.newline + MyApplication.newline
                            + resources.getString(R.string.help_radar_drawingtools)
                            + MyApplication.newline + MyApplication.newline
                            + resources.getString(R.string.help_radar_recording)
                            + MyApplication.newline + MyApplication.newline
                    , this
            )
            R.id.action_share -> {
                if (Build.VERSION.SDK_INT > 20) {
                    checkOverlayPerms()
                } else {
                    if (animRan) {
                        val animDrawable = UtilityUSImgWX.animationFromFiles(
                                this,
                                oglrArr[curRadar].rid,
                                oglrArr[curRadar].product,
                                frameCountGlobal,
                                (curRadar + 1).toString(),
                                true
                        )
                        UtilityShare.shareAnimGif(
                                this,
                                oglrArr[curRadar].rid + " (" + Utility.readPref(
                                        this,
                                        "RID_LOC_" + oglrArr[curRadar].rid,
                                        ""
                                )
                                        + ") " + oglrArr[curRadar].product, animDrawable
                        )
                    } else {
                        UtilityShare.shareBitmap(
                                this,
                                oglrArr[curRadar].rid +
                                        " (" + Utility.readPref(
                                        this,
                                        "RID_LOC_" + oglrArr[curRadar].rid,
                                        ""
                                ) + ") "
                                        + oglrArr[curRadar].product,
                                UtilityUSImgWX.layeredImgFromFile(
                                        applicationContext,
                                        oglrArr[curRadar].rid,
                                        oglrArr[curRadar].product,
                                        "0",
                                        true
                                )
                        )
                    }
                }
            }
            R.id.action_settings -> startActivity(
                    Intent(
                            contextg,
                            SettingsRadarActivity::class.java
                    )
            )
            R.id.action_radar_markers -> ObjectIntent(
                    this,
                    ImageShowActivity::class.java,
                    ImageShowActivity.URL,
                    arrayOf("raw:radar_legend", "Radar Markers", "false")
            )
            R.id.action_radar_site_status_l3 -> ObjectIntent(
                    this,
                    WebscreenABModels::class.java,
                    WebscreenABModels.URL,
                    arrayOf(
                            "http://radar3pub.ncep.noaa.gov",
                            resources.getString(R.string.action_radar_site_status_l3)
                    )
            )
            R.id.action_radar_site_status_l2 -> ObjectIntent(
                    this,
                    WebscreenABModels::class.java,
                    WebscreenABModels.URL,
                    arrayOf(
                            "http://radar2pub.ncep.noaa.gov",
                            resources.getString(R.string.action_radar_site_status_l2)
                    )
            )
            R.id.action_radar1 -> switchRadar(0)
            R.id.action_radar2 -> switchRadar(1)
            R.id.action_radar3 -> switchRadar(2)
            R.id.action_radar4 -> switchRadar(3)
            R.id.action_n0q, R.id.action_n0q_menu -> {
                if (!WXGLNexrad.isRidTdwr(oglrArr[curRadar].rid)) {
                    oglrArr[curRadar].product = "N" + tilt + "Q"
                } else {
                    oglrArr[curRadar].product = "TZL"
                }
                getContentIntelligent()
            }
            R.id.action_n0u, R.id.action_n0u_menu -> {
                if (!WXGLNexrad.isRidTdwr(oglrArr[curRadar].rid)) {
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
            R.id.action_nsw -> changeProd("NSW")
            R.id.action_l2vel -> changeProd("L2VEL")
            R.id.action_l2ref -> changeProd("L2REF")
            R.id.action_tilt1 -> changeTilt("0")
            R.id.action_tilt2 -> changeTilt("1")
            R.id.action_tilt3 -> changeTilt("2")
            R.id.action_tilt4 -> changeTilt("3")
            R.id.action_a12 -> animateRadar(12)
            R.id.action_a18 -> animateRadar(18)
            R.id.action_a6_sm -> animateRadar(6)
            R.id.action_a -> animateRadar(MyApplication.uiAnimIconFrames.toIntOrNull() ?: 0)
            R.id.action_a36 -> animateRadar(36)
            R.id.action_a72 -> animateRadar(72)
            R.id.action_a144 -> animateRadar(144)
            R.id.action_a3 -> animateRadar(3)
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

    private fun animateRadar(frameCount: Int) {
        anim.setIcon(MyApplication.ICON_STOP)
        star.setIcon(MyApplication.ICON_PAUSE)
        getAnimate(frameCount)
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
            if (oglrArr[0].product.contains("L2") || oglrArr[1].product.contains("L2")) SystemClock.sleep(
                    2000
            )
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
        if (!dontSavePref) {
            numPanesArr.forEach { WXGLNexrad.savePrefs(this, prefPrefix, it + 1, oglrArr[it]) }
        }
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        mHandler?.let { stopRepeatingTask() }
        sn_Handler_m?.let { stop_sn_reporting() }
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            )
                it.removeUpdates(locationListener)
        }
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            // FIXME needed?
            idxIntAl = idxInt
            if (progress != 50000) {
                UtilityRadarUI.addItemsToLongPress(
                        alertDialogStatusAl,
                        locXCurrent,
                        locYCurrent,
                        contextg,
                        glviewArr[idxInt],
                        oglrArr[idxIntAl],
                        alertDialogRadarLongPress!!
                )
            } else {
                numPanesArr.forEach { wxgltextArr[it].addTV() }
            }
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
            Log.d(TAG, "SendPosition(contextg) on lat: "+latD+" lon: "+lonD)
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
        numPanesArr.forEach {
            oglrArr[it].constructLocationDot(locXCurrent, locYCurrent, false)
            glviewArr[it].requestRender()
        }
    }

    private fun getGPSFromDouble() {
        try {
            latlonArr[0] = latD.toString()
            latlonArr[1] = lonD.toString()
            locXCurrent = latlonArr[0]
            locYCurrent = latlonArr[1]
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    private fun getLatLon() = LatLon(locXCurrent, locYCurrent)

    private fun setupAlertDialogRadarLongPress() {
        alertDialogRadarLongPress = ObjectDialogue(contextg, alertDialogStatusAl)
        alertDialogRadarLongPress!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(act)
        })
        alertDialogRadarLongPress!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogStatusAl[which]
            UtilityRadarUI.doLongPressAction(
                    strName,
                    contextg,
                    act,
                    glviewArr[idxIntAl],
                    oglrArr[idxIntAl],
                    uiDispatcher,
                    ::longPressRadarSiteSwitch
            )
            dialog.dismiss()
        })
    }

    private fun longPressRadarSiteSwitch(strName: String) {
        val ridNew = strName.parse(UtilityRadarUI.longPressRadarSiteRegex)
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
    }

    private fun alertDialogTDWR() {
        val diaTdwr = ObjectDialogue(contextg, GlobalArrays.tdwrRadars)
        diaTdwr.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(act)
        })
        diaTdwr.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = GlobalArrays.tdwrRadars[which]
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
            numPanesArr.forEach {
                infoArr[it] =
                        Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "")
            }
            tmpArr1 = MyApplication.space.split(infoArr[0])
            tmpArr2 = MyApplication.space.split(infoArr[1])
            tmpArr3 = MyApplication.space.split(infoArr[2])
            tmpArr4 = MyApplication.space.split(infoArr[3])
            if (tmpArr1.size > 3 && tmpArr2.size > 3 && tmpArr3.size > 3 && tmpArr4.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3] + "/" + tmpArr3[3] + "/" +
                        tmpArr4[3]
            else
                toolbar.subtitle = ""
        } else if (numPanes == 2) {
            numPanesArr.forEach {
                infoArr[it] =
                        Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "")
            }
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
            numPanesArr.forEach {
                infoAnim[it] =
                        Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "")
            }
            tmpArr1 = MyApplication.space.split(infoAnim[0])
            tmpArr2 = MyApplication.space.split(infoAnim[1])
            tmpArr3 = MyApplication.space.split(infoAnim[2])
            tmpArr4 = MyApplication.space.split(infoAnim[3])
            if (tmpArr1.size > 3 && tmpArr2.size > 3 && tmpArr3.size > 3 && tmpArr4.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3] + "/" + tmpArr3[3] + "/" +
                        tmpArr4[3] + "(" + a + "/" + b + ")"
            else
                toolbar.subtitle = ""
        } else if (numPanes == 2) {
            numPanesArr.forEach {
                infoAnim[it] =
                        Utility.readPref(this, "WX_RADAR_CURRENT_INFO" + (it + 1).toString(), "")
            }
            tmpArr1 = MyApplication.space.split(infoAnim[0])
            tmpArr2 = MyApplication.space.split(infoAnim[1])
            if (tmpArr1.size > 3 && tmpArr2.size > 3)
                toolbar.subtitle = tmpArr1[3] + "/" + tmpArr2[3] + "/" + "(" + a + "/" + b + ")"
            else
                toolbar.subtitle = ""
        }
    }

    fun getContentSingleThreaded(glvg: WXGLSurfaceView, OGLRg: WXGLRender, curRadar: Int) {
        getContent(glvg, OGLRg, curRadar)
    }
}

