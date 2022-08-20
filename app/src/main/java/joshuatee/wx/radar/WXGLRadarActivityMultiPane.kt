/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.content.ContextCompat
import joshuatee.wx.Extensions.parse
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.*
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.*
import kotlinx.coroutines.*
import joshuatee.wx.radar.SpotterNetworkPositionReport.SendPosition
import java.io.File

class WXGLRadarActivityMultiPane : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // This activity is a general purpose viewer of nexrad
    // nexrad data is downloaded from NWS TGFTP, decoded and drawn using OpenGL ES
    // Unlike the traditional viewer this one shows multiple nexrad radars at the same time
    // nexrad sites, products, zoom and x/y are saved on stop and restored on start
    //
    // Arguments
    // 1: RID
    // 2: State NO LONGER NEEDED
    // 3: number of panes
    // 4: coming from single pane
    //

    companion object { const val RID = "" }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var numberOfPanes = 4
    private var mHandler: Handler? = null
    private var mInterval = 180000
    private var sn_Handler_m: Handler? = null
    private var sn_Interval = 180000 // 180 seconds by default
    private var loopCount = 0
    private var inOglAnim = false
    private var inOglAnimPaused = false
    private var wxglRenders = mutableListOf<WXGLRender>()
    private var wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    private var tilt = "0"
    private var oldRadarSites = Array(2) { "" }
    private lateinit var objectImageMap: ObjectImageMap
    private lateinit var pauseButton: MenuItem
    private lateinit var animateButton: MenuItem
    private var delay = 0
    private var frameCountGlobal = 0
    private var locXCurrent = ""
    private var locYCurrent = ""
    private var latD = 0.0
    private var lonD = 0.0
    private var locationManager: LocationManager? = null
    private var animTriggerDownloads = false
    private var curRadar = 0
    private val alertDialogStatusList = mutableListOf<String>()
    private var idxIntAl = 0
    private var prefPrefix = "WXOGL_DUALPANE"
    private var relativeLayouts = mutableListOf<RelativeLayout>()
    private var wxglTextObjects = mutableListOf<WXGLTextObject>()
    private var dialogRadarLongPress: ObjectDialogue? = null
    private var doNotSavePref = false
    private var useSinglePanePref = false
    private var landScape = false
    private lateinit var l3Menu: MenuItem
    private lateinit var l2Menu: MenuItem
    private lateinit var tdwrMenu: MenuItem
    private lateinit var tiltMenu: MenuItem
    private lateinit var tiltMenuOption4: MenuItem
    private val animateButtonPlayString = "Animate Frames"
    private val animateButtonStopString = "Stop animation"
    private val pauseButtonString = "Pause animation"
    private val resumeButtonString = "Resume animation"
    private var settingsVisited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val arguments = intent.getStringArrayExtra(RID)
        if (arguments != null && arguments.size > 3 ) {
            if (arguments[3] == "true") { // invoked from single pane radar
                doNotSavePref = true
                useSinglePanePref = true
            }
        }
        landScape = UtilityUI.isLandScape(this)
        numberOfPanes = arguments!![2].toIntOrNull() ?: 0
        UtilityFileManagement.deleteCacheFiles(this)
        var widthDivider = 1
        var heightDivider = 2
        val layoutType: Int
        //
        // determine correct layout to use
        //
        if (numberOfPanes == 2) {
            if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent) {
                if (landScape) {
                    layoutType = if (Utility.isThemeAllWhite())
                        R.layout.activity_uswxoglmultipane_immersive_landscape_white
                    else
                        R.layout.activity_uswxoglmultipane_immersive_landscape
                    heightDivider = 1
                } else {
                    layoutType = if (Utility.isThemeAllWhite())
                        R.layout.activity_uswxoglmultipane_immersive_white
                    else
                        R.layout.activity_uswxoglmultipane_immersive
                }
            } else {
                if (landScape) {
                    layoutType = if (Utility.isThemeAllWhite())
                        R.layout.activity_uswxoglmultipane_immersive_landscape_white
                    else
                        R.layout.activity_uswxoglmultipane_immersive_landscape
                    heightDivider = 1
                } else {
                    layoutType = if (Utility.isThemeAllWhite())
                        R.layout.activity_uswxoglmultipane_white
                    else
                        R.layout.activity_uswxoglmultipane
                }
            }
            super.onCreate(savedInstanceState, layoutType, R.menu.uswxoglradarmultipane, iconsEvenlySpaced = true, bottomToolbar = true)
        } else {
            layoutType = if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent) {
                if (Utility.isThemeAllWhite())
                    R.layout.activity_uswxoglmultipane_quad_immersive_white
                else
                    R.layout.activity_uswxoglmultipane_quad_immersive
            } else {
                if (Utility.isThemeAllWhite())
                    R.layout.activity_uswxoglmultipane_quad_white
                else
                    R.layout.activity_uswxoglmultipane_quad
            }
            super.onCreate(savedInstanceState, layoutType, R.menu.uswxoglradarmultipane, iconsEvenlySpaced = true, bottomToolbar = true)
        }
        objectToolbarBottom.connect(this)
        UtilityUI.immersiveMode(this)
        locXCurrent = joshuatee.wx.settings.Location.x
        locYCurrent = joshuatee.wx.settings.Location.y
        oldRadarSites = Array(numberOfPanes) { "" }
        if (numberOfPanes == 4) {
            widthDivider = 2
            prefPrefix = "WXOGL_QUADPANE"
        } else if (numberOfPanes == 2 && landScape) {
            widthDivider = 2
        }
        if (useSinglePanePref) {
            prefPrefix = "WXOGL"
        }
        setupAlertDialogRadarLongPress()
        UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        toolbar.setTitleTextColor(Color.WHITE)
        val latLonListAsDoubles = UtilityLocation.getGps(this)
        latD = latLonListAsDoubles[0]
        lonD = latLonListAsDoubles[1]
        setupMenu()
        delay = UtilityImg.animInterval(this)
        //
        // setup WXGLSurfaceViews, WXGLRenders
        //
        repeat(numberOfPanes) {
            wxglSurfaceViews.add(WXGLSurfaceView(this, widthDivider, numberOfPanes, heightDivider))
            wxglSurfaceViews[it].idxInt = it
            wxglRenders.add(WXGLRender(this, it))
            wxglRenders[it].radarStatusStr = (it + 1).toString()
            wxglRenders[it].indexString = (it + 1).toString()
        }
        //
        // set dimensions of layouts
        //
        val elementIds = listOf(R.id.rl1, R.id.rl2, R.id.rl3, R.id.rl4)
        if (numberOfPanes == 4) {
            wxglRenders.indices.forEach {
                relativeLayouts.add(findViewById(elementIds[it]))
                relativeLayouts[it].addView(wxglSurfaceViews[it])
                val params = relativeLayouts[it].layoutParams
                if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)
                    params.height = MyApplication.dm.heightPixels / 2 + UtilityUI.statusBarHeight(this)
                else
                    params.height = MyApplication.dm.heightPixels /
                            2 - UIPreferences.actionBarHeight /
                            2 - UtilityUI.statusBarHeight(this) / 2 -
                            (UtilityUI.navigationBarHeight(this) / 2.0).toInt()
                if (UIPreferences.radarToolbarTransparent && !UIPreferences.radarImmersiveMode && numberOfPanes == 4) {
                    params.height = MyApplication.dm.heightPixels / 2 - UtilityUI.statusBarHeight(this) / 2
                }
                params.width = MyApplication.dm.widthPixels / 2
            }
        } else if (numberOfPanes == 2) {
            wxglRenders.indices.forEach {
                relativeLayouts.add(findViewById(elementIds[it]))
                relativeLayouts[it].addView(wxglSurfaceViews[it])
                val params = relativeLayouts[it].layoutParams
                if (!landScape) {
                    params.height = MyApplication.dm.heightPixels / 2 -
                            (UIPreferences.actionBarHeight / 2) - UtilityUI.statusBarHeight(this) / 2 -
                            (UtilityUI.navigationBarHeight(this) / 2.0).toInt()
                    params.width = MyApplication.dm.widthPixels
                } else {
                    params.width = MyApplication.dm.widthPixels
                    params.height = MyApplication.dm.heightPixels
                }
            }
        }
        wxglRenders.indices.forEach {
            NexradDraw.initGlView(
                    wxglSurfaceViews[it],
                    wxglSurfaceViews,
                    wxglRenders[it],
                    wxglRenders,
                    this,
                    toolbar,
                    toolbarBottom,
                    changeListener
            )
        }
        //
        // clickable map setup
        //
        objectImageMap = ObjectImageMap(
                this,
                R.id.map,
                toolbar,
                toolbarBottom,
                relativeLayouts.toList() as List<View> + wxglSurfaceViews.toList() as List<View>
        )
        objectImageMap.connect(::mapSwitch, UtilityImageMap::mapToRid)
        //
        // set radar sites
        //
        wxglRenders.forEachIndexed { index, wxglRender ->
            var initialRadarSite = arguments[0]
            if (arguments[0] == "") {
                initialRadarSite = joshuatee.wx.settings.Location.rid
            }
            if (!useSinglePanePref) {
                wxglRender.rid = Utility.readPref(this, prefPrefix + "_RID" + (index + 1).toString(), initialRadarSite)
            } else {
                wxglRender.rid = Utility.readPref(this, prefPrefix + "_RID", initialRadarSite)
            }
        }
        if (RadarPreferences.dualpaneshareposn) {
                (1 until numberOfPanes).forEach {
                    wxglRenders[it].rid = wxglRenders[0].rid
                }
        }
        //
        // restore last product used
        //
        val defaultProducts = listOf("N0Q", "N0U", "N0C", "DVL")
        wxglRenders.indices.forEach {
            oldRadarSites[it] = ""
            wxglRenders[it].product = Utility.readPref(this, prefPrefix + "_PROD" + (it + 1).toString(), defaultProducts[it])
        }
        var zoomPref = "_ZOOM1"
        var xPref = "_X1"
        var yPref = "_Y1"
        if (useSinglePanePref) {
            zoomPref = "_ZOOM"
            xPref = "_X"
            yPref = "_Y"
        }
        //
        // restore session (x, y, zoom)
        //
        wxglSurfaceViews[0].scaleFactor = Utility.readPrefFloat(this, prefPrefix + zoomPref, RadarPreferences.wxoglSize.toFloat() / 10.0f)
        wxglRenders[0].setViewInitial(
                Utility.readPrefFloat(this, prefPrefix + zoomPref, RadarPreferences.wxoglSize.toFloat() / 10.0f),
                Utility.readPrefFloat(this, prefPrefix + xPref, 0.0f),
                Utility.readPrefFloat(this, prefPrefix + yPref, 0.0f)
        )
        if (RadarPreferences.dualpaneshareposn) {
            (1 until numberOfPanes).forEach {
                wxglSurfaceViews[it].scaleFactor = wxglSurfaceViews[0].scaleFactor
                wxglRenders[it].setViewInitial(
                        Utility.readPrefFloat(this, prefPrefix + zoomPref, RadarPreferences.wxoglSize.toFloat() / 10.0f),
                        wxglRenders[0].x, wxglRenders[0].y
                )
            }
        } else {
            (1 until numberOfPanes).forEach {
                wxglSurfaceViews[it].scaleFactor = Utility.readPrefFloat(this, prefPrefix + "_ZOOM" + (it + 1).toString(), RadarPreferences.wxoglSize.toFloat() / 10.0f)
                wxglRenders[it].setViewInitial(
                        Utility.readPrefFloat(this, prefPrefix + "_ZOOM" + (it + 1).toString(), RadarPreferences.wxoglSize.toFloat() / 10.0f),
                        Utility.readPrefFloat(this, prefPrefix + "_X" + (it + 1).toString(), 0.0f),
                        Utility.readPrefFloat(this, prefPrefix + "_Y" + (it + 1).toString(), 0.0f)
                )
            }
        }
        //
        // text label object setup
        //
        wxglSurfaceViews.indices.forEach {
            wxglTextObjects.add(WXGLTextObject(this, relativeLayouts[it], wxglSurfaceViews[it], wxglRenders[it], numberOfPanes, it))
            wxglSurfaceViews[it].wxglTextObjects = wxglTextObjects
            wxglTextObjects[it].initializeLabels(this)
        }
        getContentParallel()
        checkForAutoRefresh()
    }

    private fun setupMenu() {
        pauseButton = objectToolbarBottom.getFavIcon()
        pauseButton.title = pauseButtonString
        animateButton = objectToolbarBottom.find(R.id.action_a)
        tiltMenu = objectToolbarBottom.find(R.id.action_tilt)
        tiltMenuOption4 = objectToolbarBottom.find(R.id.action_tilt4)
        l3Menu = objectToolbarBottom.find(R.id.action_l3)
        l2Menu = objectToolbarBottom.find(R.id.action_l2)
        tdwrMenu = objectToolbarBottom.find(R.id.action_tdwr)
        val rad3 = objectToolbarBottom.find(R.id.action_radar3)
        val rad4 = objectToolbarBottom.find(R.id.action_radar4)
        val quadPaneJump = objectToolbarBottom.find(R.id.action_radar_4)
        if (numberOfPanes == 2) {
            rad3.isVisible = false
            rad4.isVisible = false
        } else {
            quadPaneJump.isVisible = false
        }
        if (!UIPreferences.radarImmersiveMode) {
            objectToolbarBottom.hide(R.id.action_blank)
            objectToolbarBottom.hide(R.id.action_level3_blank)
            objectToolbarBottom.hide(R.id.action_level2_blank)
            objectToolbarBottom.hide(R.id.action_animate_blank)
            objectToolbarBottom.hide(R.id.action_tilt_blank)
            objectToolbarBottom.hide(R.id.action_tools_blank)
        }
        // disable new Level3 super-res until NWS is past deployment phase
        objectToolbarBottom.hide(R.id.action_n0b)
        objectToolbarBottom.hide(R.id.action_n0g)
    }

    private fun checkForAutoRefresh() {
        if (RadarPreferences.wxoglRadarAutoRefresh || RadarPreferences.locationDotFollowsGps) {
            mInterval = 60000 * Utility.readPrefInt(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (gpsEnabled != null && gpsEnabled) {
                    locationManager?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            (RadarPreferences.locationUpdateInterval * 1000).toLong(),
                            WXGLNexrad.radarLocationUpdateDistanceInMeters,
                            locationListener
                    )
                }
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mHandler = Handler(Looper.getMainLooper())
            startRepeatingTask()
        }
	
	//elys mod
        if (RadarPreferences.sn_locationreport) {
            UtilityLog.d("wx", "starting location report")
            sn_Handler_m = Handler(Looper.getMainLooper())
            start_sn_reporting()
        }

    }

    override fun onRestart() {
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
        animateButton.title = animateButtonPlayString
        if (objectImageMap.visibility == View.GONE) {
            wxglTextObjects.forEach {
                it.initializeLabels(this)
                it.addLabels()
            }
        }
//        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) {
//            getContentSerial()
//        } else {
        getContentParallel()
//        }
        checkForAutoRefresh()
        super.onRestart()
    }

    private fun getContent(z: Int) {
        getContentPrep(z)
        initGeom(z)
        FutureVoid(this, {
            NexradDraw.plotRadar(wxglRenders[z], "", ::getGpsFromDouble, ::getLatLon, false)
        }) {
            glviewShow()
            wxglSurfaceViews[z].requestRender()
            setSubTitle()
            UtilityRadarUI.updateLastRadarTime(this)
            if (RadarPreferences.wxoglCenterOnLocation) {
                wxglSurfaceViews[z].resetView()
            }
        }
        NexradLayerDownload.download(this, numberOfPanes, wxglRenders[z], wxglSurfaceViews[z], wxglTextObjects, {})
    }

    private fun getContentPrep(z: Int) {
        if ((wxglRenders[z].product.matches(Regex("N[0-3]Q")) || wxglRenders[z].product == "L2REF") && WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "TZL"
        }
        if (wxglRenders[z].product == "TZL" && !WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "N0Q"
        }
        if ((wxglRenders[z].product.matches(Regex("N[0-3]U")) || wxglRenders[z].product == "L2VEL") && WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "TV0"
        }
        if (wxglRenders[z].product == "TV0" && !WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "N0U"
        }
        toolbar.subtitle = ""
        setToolbarTitle()
        adjustTiltAndProductMenus()
    }

    private fun initGeom(z: Int) {
        NexradDraw.initGeom(
                z,
                oldRadarSites,
                wxglRenders,
                wxglTextObjects,
                objectImageMap,
                wxglSurfaceViews,
                ::getGpsFromDouble,
                ::getLatLon,
                false,
                settingsVisited
        )
        settingsVisited = false
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        glviewShow()
        inOglAnim = true
        withContext(Dispatchers.IO) {
            var file: File
            var timeMilli: Long
            var priorTime: Long
            frameCountGlobal = frameCount
            val animArray = Array(numberOfPanes) { Array(frameCount) { "" } }
            wxglRenders.forEachIndexed { z, wxglRender ->
                animArray[z] = WXGLDownload.getRadarFilesForAnimation(this@WXGLRadarActivityMultiPane, frameCount, wxglRender.rid, wxglRender.product).toTypedArray()
                try {
                    animArray[z].indices.forEach { r ->
                        file = File(this@WXGLRadarActivityMultiPane.filesDir, animArray[z][r])
                        this@WXGLRadarActivityMultiPane.deleteFile((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())
                        if (!file.renameTo(File(this@WXGLRadarActivityMultiPane.filesDir, (z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())))
                            UtilityLog.d("wx", "Problem moving to " + (z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())
                    }
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    wxglRenders.forEachIndexed { z, wxglRender ->
                        animArray[z] = WXGLDownload.getRadarFilesForAnimation(this@WXGLRadarActivityMultiPane, frameCount, wxglRender.rid, wxglRender.product).toTypedArray()
                        try {
                            animArray[z].indices.forEach { r ->
                                file = File(this@WXGLRadarActivityMultiPane.filesDir, animArray[z][r])
                                this@WXGLRadarActivityMultiPane.deleteFile((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())
                                if (!file.renameTo(File(this@WXGLRadarActivityMultiPane.filesDir, (z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())))
                                    UtilityLog.d("wx", "Problem moving to " + (z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())
                            }
                        } catch (e: Exception) {
                            UtilityLog.handleException(e)
                        }
                    }
                    animTriggerDownloads = false
                }
                for (r in animArray[0].indices) {
                    while (inOglAnimPaused) {
                        SystemClock.sleep(delay.toLong())
                    }
                    // formerly priorTime was set at the end but that is goofed up with pause
                    priorTime = ObjectDateTime.currentTimeMillis()
                    // added because if paused and then another icon life vel/ref it won't load correctly, likely
                    // timing issue
                    if (!inOglAnim) {
                        break
                    }
                    if (loopCnt > 0) {
                        wxglRenders.forEachIndexed { z, wxglRender ->
                            wxglRender.constructPolygons((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString(), "", false)
                        }
                    } else {
                        wxglRenders.forEachIndexed { z, wxglRender ->
                            wxglRender.constructPolygons((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString(), "", true)
                        }
                    }
                    launch(uiDispatcher) { progressUpdate((r + 1).toString(), (animArray[0].size).toString()) }
                    wxglSurfaceViews.forEach {
                        it.requestRender()
                    }
                    timeMilli = ObjectDateTime.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay) {
                        SystemClock.sleep(delay - ((timeMilli - priorTime)))
                    }
                    if (!inOglAnim) {
                        break
                    }
                    if (r == (animArray[0].lastIndex)) {
                        SystemClock.sleep(delay.toLong() * 2)
                    }
                }
                loopCnt += 1
            }
        }
        UtilityFileManagement.deleteCacheFiles(this@WXGLRadarActivityMultiPane)
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
        UtilityUI.immersiveMode(this)
    }

    private fun adjustTiltAndProductMenus() {
        if (WXGLNexrad.isTdwr(wxglRenders[curRadar].product)) {
            l3Menu.isVisible = false
            l2Menu.isVisible = false
            tdwrMenu.isVisible = true
            tiltMenu.isVisible = wxglRenders[curRadar].product.matches(Regex("[A-Z][A-Z][0-2]"))
        } else {
            l3Menu.isVisible = true
            l2Menu.isVisible = true
            tdwrMenu.isVisible = false
            tiltMenu.isVisible = wxglRenders[curRadar].product.matches(Regex("[A-Z][0-3][A-Z]"))
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        UtilityUI.immersiveMode(this)
        if (inOglAnim && (item.itemId != R.id.action_fav) && (item.itemId != R.id.action_share) && (item.itemId != R.id.action_tools)) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            if (wxglRenders[0].product.contains("L2") || wxglRenders[1].product.contains("L2")) {
                SystemClock.sleep(2000)
            }
            animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
            animateButton.title = animateButtonPlayString
            getContentParallel()
            if (item.itemId == R.id.action_a) {
                return true
            }
        }
        when (item.itemId) {
            R.id.action_help -> ObjectDialogue(this,
                    resources.getString(R.string.help_radar)
                            + GlobalVariables.newline + GlobalVariables.newline
                            + resources.getString(R.string.help_radar_drawingtools)
                            + GlobalVariables.newline + GlobalVariables.newline
                            + resources.getString(R.string.help_radar_recording)
                            + GlobalVariables.newline + GlobalVariables.newline
            )
            R.id.action_share -> if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityRadarUI.getImageForShare(this, wxglRenders[curRadar], "1")
                }
            R.id.action_settings -> { settingsVisited = true; Route.settingsRadar(this) }
            R.id.action_radar_markers -> Route.image(this, arrayOf("raw:radar_legend", "Radar Markers", "false"))
            R.id.action_radar_site_status_l3 -> Route.webView(this, arrayOf("http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended"))
            R.id.action_radar_site_status_l2 -> Route.webView(this, arrayOf("http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended"))
            R.id.action_radar1 -> switchRadar(0)
            R.id.action_radar2 -> switchRadar(1)
            R.id.action_radar3 -> switchRadar(2)
            R.id.action_radar4 -> switchRadar(3)
            R.id.action_n0q, R.id.action_n0q_menu -> {
                if (!WXGLNexrad.isRidTdwr(wxglRenders[curRadar].rid)) {
                    wxglRenders[curRadar].product = "N" + tilt + "Q"
                } else {
                    wxglRenders[curRadar].product = "TZL"
                }
                getContentIntelligent()
            }
            R.id.action_n0u, R.id.action_n0u_menu -> {
                if (!WXGLNexrad.isRidTdwr(wxglRenders[curRadar].rid)) {
                    wxglRenders[curRadar].product = "N" + tilt + "U"
                } else {
                    wxglRenders[curRadar].product = "TV$tilt"
                }
                getContentIntelligent()
            }
            R.id.action_n0b -> changeProd("N" + tilt + "B")
            R.id.action_n0g -> changeProd("N" + tilt + "G")
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
            R.id.action_ncr -> changeProd("NCR")
            R.id.action_ncz -> changeProd("NCZ")	    
            //I need those!  ELY M. 
	        R.id.action_et -> changeProd("ET")
            R.id.action_vil -> changeProd("VIL")
            R.id.action_l2vel -> changeProd("L2VEL")
            R.id.action_l2ref -> changeProd("L2REF")
            R.id.action_tilt1 -> changeTilt("0")
            R.id.action_tilt2 -> changeTilt("1")
            R.id.action_tilt3 -> changeTilt("2")
            R.id.action_tilt4 -> changeTilt("3")
            R.id.action_a12 -> animateRadar(12)
            R.id.action_a18 -> animateRadar(18)
            R.id.action_a6_sm -> animateRadar(6)
            R.id.action_a -> animateRadar(RadarPreferences.uiAnimIconFrames.toIntOrNull() ?: 0)
            R.id.action_a36 -> animateRadar(36)
            R.id.action_a72 -> animateRadar(72)
            R.id.action_a144 -> animateRadar(144)
            R.id.action_a3 -> animateRadar(3)
            R.id.action_fav -> {
                if (inOglAnim) {
                    inOglAnimPaused = if (!inOglAnimPaused) {
                        pauseButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
                        pauseButton.title = resumeButtonString
                        true
                    } else {
                        pauseButton.setIcon(GlobalVariables.ICON_PAUSE_WHITE)
                        pauseButton.title = pauseButtonString
                        false
                    }
                }
            }
            R.id.action_radar_4 -> {
                if (!doNotSavePref) {
                    wxglRenders.forEachIndexed { index, wxglRender ->
                        WXGLNexrad.savePrefs(this, prefPrefix, index + 1, wxglRender)
                    }
                } else {
                    wxglRenders.forEachIndexed { index, wxglRender ->
                        WXGLNexrad.saveProductPrefs(this, prefPrefix, index + 1, wxglRender)
                    }
                }
                Route.radarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", "4", "true"))
            }
            R.id.action_TDWR -> alertDialogTdwr()
            R.id.action_ridmap -> objectImageMap.showMap(numberOfPanes, wxglTextObjects)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun animateRadar(frameCount: Int) {
        animateButton.setIcon(GlobalVariables.ICON_STOP_WHITE)
        animateButton.title = animateButtonStopString
        pauseButton.setIcon(GlobalVariables.ICON_PAUSE_WHITE)
        pauseButton.title = pauseButtonString
        getAnimate(frameCount)
    }

    private fun changeTilt(tiltStr: String) {
        tilt = tiltStr
        wxglRenders[curRadar].product = wxglRenders[curRadar].product.replace("N[0-3]".toRegex(), "N$tilt")
        title = wxglRenders[curRadar].product
        getContent(curRadar)
    }

    private fun changeProd(product: String) {
        wxglRenders[curRadar].product = product
        getContentIntelligent()
    }

    private fun switchRadar(radarNumber: Int) {
        curRadar = radarNumber
        idxIntAl = radarNumber
        setToolbarTitle()
        adjustTiltAndProductMenus()
    }

    private fun mapSwitch(newRadar: String) {
        objectImageMap.hideMap()
        UtilityWXGLTextObject.showLabels(numberOfPanes, wxglTextObjects)
        if (inOglAnim) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for a bit to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            if (wxglRenders[0].product.contains("L2") || wxglRenders[1].product.contains("L2")) {
                SystemClock.sleep(2000)
            }
            animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
            animateButton.title = animateButtonPlayString
        }
        if (RadarPreferences.dualpaneshareposn) {
            // if one long presses change the currently active radar as well
            curRadar = idxIntAl
            wxglRenders.indices.forEach {
                wxglRenders[it].rid = newRadar
                wxglSurfaceViews[it].scaleFactor = RadarPreferences.wxoglSize / 10.0f
                wxglRenders[it].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
            }
        } else {
            // if one long presses change the currently active radar as well
            curRadar = idxIntAl
            wxglRenders[idxIntAl].rid = newRadar
            wxglSurfaceViews[idxIntAl].scaleFactor = RadarPreferences.wxoglSize / 10.0f
            wxglRenders[idxIntAl].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
        getContentParallel()
    }

    private fun showRadarScanInfo() {
        val scanInfoList = wxglRenders.indices.map { WXGLNexrad.getRadarInfo(this,(it + 1).toString()) }
        ObjectDialogue(this, scanInfoList.joinToString(GlobalVariables.newline + GlobalVariables.newline))
    }

    override fun onStop() {
        super.onStop()
        if (!doNotSavePref) {
            wxglRenders.forEachIndexed { index, wxglRender ->
                WXGLNexrad.savePrefs(this, prefPrefix, index + 1, wxglRender)
            }
        } else {
            // TODO FIXME why is this here?
            wxglRenders.forEachIndexed { index, wxglRender ->
                WXGLNexrad.saveProductPrefs(this, prefPrefix, index + 1, wxglRender)
            }
        }
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        mHandler?.let { stopRepeatingTask() }
	//elys mod
        sn_Handler_m?.let { stop_sn_reporting() }
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            )
                it.removeUpdates(locationListener)
        }
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            idxIntAl = idxInt
            if (progress != 50000) {
                UtilityRadarUI.setupContextMenu(
                        alertDialogStatusList,
                        locXCurrent,
                        locYCurrent,
                        this@WXGLRadarActivityMultiPane,
                        wxglSurfaceViews[idxInt],
                        wxglRenders[idxIntAl],
                        dialogRadarLongPress!!
                )
            } else {
                wxglTextObjects.forEach {
                    it.addLabels()
                }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    private val mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            if (mHandler != null) {
                if (loopCount > 0) {
                    if (inOglAnim) {
                        animTriggerDownloads = true
                    } else {
                        getContentParallel()
                    }
                }
                loopCount += 1
                handler.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun startRepeatingTask() {
        mStatusChecker.run()
    }

    private fun stopRepeatingTask() {
        mHandler!!.removeCallbacks(mStatusChecker)
        mHandler = null
    }

    //elys mod
    //report your spotter network location
    private val sn_handler = Handler(Looper.getMainLooper())
    private val sn_reporter: Runnable = object : Runnable {
        override fun run() {
            UtilityLog.d("wx", "SendPosition(this) on lat: "+latD+" lon: "+lonD)
            SendPosition(applicationContext)
            sn_handler.postDelayed(this, sn_Interval.toLong())
        }
    }

    private fun start_sn_reporting() {
        sn_reporter.run()
    }

    private fun stop_sn_reporting() {
        sn_Handler_m!!.removeCallbacks(sn_reporter)
	    sn_Handler_m = null
    }


    override fun onPause() {
        mHandler?.let { stopRepeatingTask() }
        wxglSurfaceViews.forEach {
            it.onPause()
        }
        super.onPause()
    }

    override fun onResume() {
        checkForAutoRefresh()
        wxglSurfaceViews.forEach {
            it.onResume()
        }
        super.onResume()
    }

    private fun setToolbarTitle() {
        if (numberOfPanes == 4) {
            title = if (RadarPreferences.dualpaneshareposn) {
                (curRadar + 1).toString() + ":" + wxglRenders[0].rid + "(" + wxglRenders[0].product + "," + wxglRenders[1].product + "," + wxglRenders[2].product + "," + wxglRenders[3].product + ")"
            } else {
                (curRadar + 1).toString() + ": " + wxglRenders[0].rid + "(" + wxglRenders[0].product + ") " + wxglRenders[1].rid + "(" + wxglRenders[1].product + ") " + wxglRenders[2].rid + "(" + wxglRenders[2].product + ") " + wxglRenders[3].rid + "(" + wxglRenders[3].product + ")"
            }
        } else if (numberOfPanes == 2) {
            title = if (RadarPreferences.dualpaneshareposn) {
                (curRadar + 1).toString() + ":" + wxglRenders[0].rid + "(" + wxglRenders[0].product + "," + wxglRenders[1].product + ")"
            } else {
                (curRadar + 1).toString() + ": " + wxglRenders[0].rid + "(" + wxglRenders[0].product + ") " + wxglRenders[1].rid + "(" + wxglRenders[1].product + ") "
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) { makeUseOfNewLocation(location) }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun makeUseOfNewLocation(location: Location) {
        latD = location.latitude
        lonD = location.longitude
        getGpsFromDouble()
        wxglRenders.indices.forEach {
            wxglRenders[it].constructLocationDot(locXCurrent, locYCurrent, false)
            wxglSurfaceViews[it].requestRender()
            if (RadarPreferences.wxoglCenterOnLocation) {
                wxglSurfaceViews[it].resetView()
                UtilityWXGLTextObject.hideLabels(it, wxglTextObjects)
                UtilityWXGLTextObject.showLabels(it, wxglTextObjects)
            }
        }
    }

    private fun getGpsFromDouble() {
        locXCurrent = latD.toString()
        locYCurrent = lonD.toString()
    }

    private fun getLatLon() = LatLon(locXCurrent, locYCurrent)

    private fun setupAlertDialogRadarLongPress() {
        dialogRadarLongPress = ObjectDialogue(this, alertDialogStatusList)
        dialogRadarLongPress!!.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        dialogRadarLongPress!!.connect { dialog, which ->
            val strName = alertDialogStatusList[which]
            UtilityRadarUI.doLongPressAction(
                    strName,
                    this,
                    wxglSurfaceViews[idxIntAl],
                    wxglRenders[idxIntAl],
                    ::longPressRadarSiteSwitch
            )
            dialog.dismiss()
        }
    }

    private fun longPressRadarSiteSwitch(s: String) {
        val newRadarSite = s.parse(UtilityRadarUI.longPressRadarSiteRegex)
        if (RadarPreferences.dualpaneshareposn) {
            wxglRenders.forEach {
                it.rid = newRadarSite
            }
            mapSwitch(wxglRenders[curRadar].rid)
        } else {
            wxglRenders[idxIntAl].rid = newRadarSite
            mapSwitch(wxglRenders[idxIntAl].rid)
        }
    }

    private fun alertDialogTdwr() {
        val objectDialogue = ObjectDialogue(this, GlobalArrays.tdwrRadars)
        objectDialogue.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        objectDialogue.connect { dialog, which ->
            val radarFullName = GlobalArrays.tdwrRadars[which]
            wxglRenders[curRadar].rid = radarFullName.split(" ")[0]
            if (wxglRenders[curRadar].product.matches(Regex("N[0-3]Q"))) {
                wxglRenders[curRadar].product = "TZL"
            } else {
                wxglRenders[curRadar].product = "TV0"
            }
            mapSwitch(wxglRenders[curRadar].rid)
            dialog.dismiss()
        }
        objectDialogue.show()
    }

    @Synchronized private fun getContentParallel() {
        wxglRenders.indices.forEach {
            getContent(it)
        }
    }

    private fun glviewShow() {
        wxglSurfaceViews.indices.forEach {
            wxglSurfaceViews[it].visibility = View.VISIBLE
            relativeLayouts[it].visibility = View.VISIBLE
        }
    }

    private fun getContentIntelligent() {
        if (RadarPreferences.dualpaneshareposn) {
            getContentParallel()
        } else {
            getContent(curRadar)
        }
    }

    private fun setSubTitle(a: String = "", b: String = "") {
        // take each radar timestamp and split into a list
        // make sure the list is the correct size (more then 3 elements)
        // create another list consisting of the 4th item of each list (the HH:MM:SS)
        // set the subtitle to a string which is the new list joined by "/"
        val radarInfoList = wxglRenders.indices.map { WXGLNexrad.getRadarInfo(this,(it + 1).toString()) }
        val tmpArray = radarInfoList.map { it.split(" ") }
        if (tmpArray.all { it.size > 3}) {
            val tmpArray2 = tmpArray.map { it[3] }
            var s = tmpArray2.joinToString("/")
            if (a != "" && b != "") {
                s += "($a/$b)"
            }
            toolbar.subtitle = s
        } else {
            toolbar.subtitle = ""
        }
    }
    // infoArr will look like the following for 2 panes
    /*
    [Fri Aug 19 07:22:51 EDT 2022
    Radar Mode: 2
    VCP: 12
    Product Code: 94
    Radar height: 776
    Radar Lat: 32.573
    Radar Lon: -97.303, Fri Aug 19 07:22:51 EDT 2022
    Radar Mode: 2
    VCP: 12
    Product Code: 99
    Radar height: 776
    Radar Lat: 32.573
    Radar Lon: -97.303]
     */

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) objectImageMap.showMap(numberOfPanes, wxglTextObjects)
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showRadarShortCuts())
            KeyEvent.KEYCODE_REFRESH -> getContentIntelligent()
            KeyEvent.KEYCODE_DPAD_UP -> if (event.isCtrlPressed) {
                    //wxglSurfaceViews.forEach{ it.zoomOutByKey(numberOfPanes.toFloat()) }
                } else {
                    wxglSurfaceViews.forEach { it.onScrollByKeyboard(0.0f, -20.0f) }
                }
            KeyEvent.KEYCODE_DPAD_DOWN -> if (event.isCtrlPressed) {
                    //wxglSurfaceViews.forEach{ it.zoomInByKey(numberOfPanes.toFloat()) }
                } else {
                    wxglSurfaceViews.forEach { it.onScrollByKeyboard(0.0f, 20.0f) }
                }
            KeyEvent.KEYCODE_DPAD_LEFT -> wxglSurfaceViews.forEach { it.onScrollByKeyboard(-20.0f, 0.0f) }
            KeyEvent.KEYCODE_DPAD_RIGHT -> wxglSurfaceViews.forEach { it.onScrollByKeyboard(20.0f, 0.0f) }
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}
