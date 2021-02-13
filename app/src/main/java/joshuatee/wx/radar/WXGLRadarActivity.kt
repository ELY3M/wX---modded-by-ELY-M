/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.os.Handler
import android.os.Looper
import android.view.*

import joshuatee.wx.R
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.telecine.TelecineService
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.SettingsRadarActivity
import joshuatee.wx.ui.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences

import joshuatee.wx.GlobalArrays
import joshuatee.wx.activitiesmisc.SevereDashboardActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.*
import joshuatee.wx.radar.SpotterNetworkPositionReport.SendPosition
import kotlinx.coroutines.*

//elys mod
import joshuatee.wx.activitiesmisc.WebView

class WXGLRadarActivity : VideoRecordActivity(), OnMenuItemClickListener {

    // This activity is a general purpose viewer of nexrad and mosaic content
    // nexrad data is downloaded from NWS FTP, decoded and drawn using OpenGL ES
    //
    //
    // Arguments
    // 1: RID
    // 2: State - NO LONGER NEEDED
    // 3: Product ( optional )
    // 4: Fixed site ( simply having a 4th arg will prevent remember location from working )
    // 4: URL String ( optional, archive )
    // 5: X ( optional, archive )
    // 6: Y ( optional, archive )
    //

    companion object {
        var RID = ""
        var dspLegendMax = 0.0f
        var velMax: Short = 120
        var velMin: Short = -120
        var spotterId = ""
        var spotterShowSelected = false
        var bearingCurrent = 0.0f
        var speedCurrent = 0.0f
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var wxglRender: WXGLRender
    private var oldProd = ""
    private var firstRun = true
    private var oldRadarSites = Array(1) { "" }
    private var mHandler: Handler? = null
    private var mInterval = 180000 // 180 seconds by default
    private var sn_Handler_m: Handler? = null
    private var sn_Interval = 180000 // 180 seconds by default
    private var conus_Handler_m: Handler? = null
    private var conus_Interval = 300000 // 5 mins for conus download might more is better
    private var loopCount = 0
    private var animRan = false
    private var archiveMode = false
    private var ridChanged = true
    private var restartedZoom = false
    private lateinit var img: TouchImageView2
    private var firstTime = true
    private var inOglAnim = false
    private var inOglAnimPaused = false
    private var oglInView = true
    private var wxglRenders = mutableListOf<WXGLRender>()
    private var wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    private var restarted = false
    private var tiltOption = true
    private lateinit var wxglSurfaceView: WXGLSurfaceView
    private var tilt = "0"
    private var radarSitesForFavorites = listOf<String>()
    private lateinit var objectImageMap: ObjectImageMap
    private var mapShown = false
    private lateinit var starButton: MenuItem
    private lateinit var animateButton: MenuItem
    private lateinit var tiltMenu: MenuItem
    private lateinit var tiltMenuOption4: MenuItem
    private lateinit var l3Menu: MenuItem
    private lateinit var l2Menu: MenuItem
    private lateinit var tdwrMenu: MenuItem
    private var delay = 0
    private val prefToken = "RID_FAV"
    private var frameCountGlobal = 0
    private var locXCurrent = ""
    private var locYCurrent = ""
    private var urlStr = ""
    private var fixedSite = false
    private lateinit var rl: RelativeLayout
    private var latD = 0.0
    private var lonD = 0.0
    private var locationManager: LocationManager? = null
    private var animTriggerDownloads = false
    private val dialogStatusList = mutableListOf<String>()
    private var legendShown = false
    private var radarShown = true
    private val numberOfPanes = 1
    private var paneList = listOf<Int>()
    private var wxglTextObjects = mutableListOf<WXGLTextObject>()
    private var dialogRadarLongPress: ObjectDialogue? = null
    private var isGetContentInProgress = false
    private val animateButtonPlayString = "Animate Frames"
    private val animateButtonStopString = "Stop animation"
    private val pauseButtonString = "Pause animation"
    private val starButtonString = "Toggle favorite"
    private val resumeButtonString = "Resume animation"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswxoglradar_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = radarSitesForFavorites.safeGet(0).split(" ")[0]
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Utility.isThemeAllWhite()) {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl_white, R.menu.uswxoglradar, iconsEvenlySpaced = true, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl, R.menu.uswxoglradar, iconsEvenlySpaced = true, bottomToolbar = true)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        toolbar.setOnClickListener { ObjectIntent(this, SevereDashboardActivity::class.java) }
        UtilityUI.immersiveMode(this)
        if (UIPreferences.radarStatusBarTransparent) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }
        spotterShowSelected = false
        isGetContentInProgress = false
        locXCurrent = joshuatee.wx.settings.Location.x
        locYCurrent = joshuatee.wx.settings.Location.y
        val activityArguments = intent.getStringArrayExtra(RID)
        paneList = (0 until numberOfPanes).toList()
        UtilityFileManagement.deleteCacheFiles(this)
        // for L2 archive called from storm reports
        if (activityArguments != null) {
            if (activityArguments.size > 6) {
                urlStr = activityArguments[4]
                locXCurrent = activityArguments[5]
                locYCurrent = activityArguments[6]
                archiveMode = true
            } else if (activityArguments.size > 4) {
                spotterId = activityArguments[4]
                spotterShowSelected = true
            }
            if (activityArguments.size > 3) fixedSite = true
            if (activityArguments.size < 7) archiveMode = false
        }
        if (MyApplication.checkinternet) {
            Utility.checkInternet(this@WXGLRadarActivity)
        }
        setupAlertDialogRadarLongPress()
        UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        toolbar.setTitleTextColor(Color.WHITE)
        if (archiveMode && !spotterShowSelected) toolbarBottom.visibility = View.GONE
        val latLonArrD = UtilityLocation.getGps(this)
        latD = latLonArrD[0]
        lonD = latLonArrD[1]
        val menu = toolbarBottom.menu
        starButton = menu.findItem(R.id.action_fav)
        animateButton = menu.findItem(R.id.action_a)
        tiltMenu = menu.findItem(R.id.action_tilt)
        tiltMenuOption4 = menu.findItem(R.id.action_tilt4)
        l3Menu = menu.findItem(R.id.action_l3)
        l2Menu = menu.findItem(R.id.action_l2)
        tdwrMenu = menu.findItem(R.id.action_tdwr)
        if (!UIPreferences.radarImmersiveMode) {
            menu.findItem(R.id.action_blank).isVisible = false
            menu.findItem(R.id.action_level3_blank).isVisible = false
            menu.findItem(R.id.action_level2_blank).isVisible = false
            menu.findItem(R.id.action_animate_blank).isVisible = false
            menu.findItem(R.id.action_tilt_blank).isVisible = false
            menu.findItem(R.id.action_tools_blank).isVisible = false
        }
        menu.findItem(R.id.action_jellybean_drawtools).isVisible = false
        delay = UtilityImg.animInterval(this)
        img = findViewById(R.id.iv)
        img.maxZoom = 6.0f
        wxglSurfaceView = WXGLSurfaceView(this, 1, numberOfPanes, 1)
        objectImageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf(img, wxglSurfaceView))
        objectImageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToRid)
        rl = findViewById(R.id.rl)
        rl.addView(wxglSurfaceView)
        val relativeLayouts = arrayOf(rl)
        wxglRender = WXGLRender(this, 0)
        wxglRenders.add(wxglRender)
        wxglSurfaceViews.add(wxglSurfaceView)
        UtilityRadarUI.initGlView(
                wxglSurfaceView,
                wxglSurfaceViews,
                wxglRender,
                wxglRenders,
                this,
                toolbar,
                toolbarBottom,
                changeListener,
                archiveMode
        )
        wxglRender.product = "N0Q"
        oglInView = true
        if (activityArguments == null) {
            wxglRender.rid = joshuatee.wx.settings.Location.rid
        } else {
            wxglRender.rid = activityArguments[0]
        }
        // hack, in rare cases a user will save a location that doesn't pick up RID
        if (wxglRender.rid == "") wxglRender.rid = "TLX"
        if (activityArguments != null && activityArguments.size > 2) {
            wxglRender.product = activityArguments[2]
            if (wxglRender.product == "N0R") wxglRender.product = "N0Q"
        }
        paneList.forEach {
            wxglTextObjects.add(WXGLTextObject(this, relativeLayouts[it], wxglSurfaceViews[it], wxglRenders[it], numberOfPanes, it))
            wxglSurfaceViews[it].wxglTextObjects = wxglTextObjects
            wxglTextObjects[it].initializeLabels(this)
        }
        if (MyApplication.wxoglRememberLocation && !archiveMode && !fixedSite) {
            wxglSurfaceView.scaleFactor = MyApplication.wxoglZoom
            if (MyApplication.wxoglRid != "") wxglRender.rid = MyApplication.wxoglRid
            wxglRender.product = MyApplication.wxoglProd
            wxglRender.setViewInitial(MyApplication.wxoglZoom, MyApplication.wxoglX, MyApplication.wxoglY)
        } else {
            wxglSurfaceView.scaleFactor = MyApplication.wxoglSize / 10.0f
            wxglRender.setViewInitial(MyApplication.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
        if (MyApplication.radarShowLegend) showLegend()
        title = wxglRender.product
        checkForAutoRefresh()
        getContent()
    }

    private fun adjustTiltMenu() {
        if (isTdwr()) {
            tiltMenuOption4.isVisible = false
            tiltMenu.isVisible = wxglRender.product.matches(Regex("[A-Z][A-Z][0-2]"))
        } else {
            tiltMenuOption4.isVisible = true
            tiltMenu.isVisible = wxglRender.product.matches(Regex("[A-Z][0-3][A-Z]"))
        }
    }

    private fun isTdwr() = wxglRender.product in WXGLNexrad.tdwrProductList

    private fun setStarButton() {
        if (MyApplication.ridFav.contains(":" + wxglRender.rid + ":")) {
            starButton.setIcon(MyApplication.STAR_ICON_WHITE)
        } else {
            starButton.setIcon(MyApplication.STAR_OUTLINE_ICON_WHITE)
        }
        starButton.title = starButtonString
    }

    override fun onRestart() {
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        setStarButton()
        animateButton.setIcon(MyApplication.ICON_PLAY_WHITE)
        animateButton.title = animateButtonPlayString
        restarted = true
        restartedZoom = true
        paneList.forEach {
            if (objectImageMap.map.visibility == View.GONE) {
                wxglTextObjects[it].initializeLabels(this)
                wxglTextObjects[it].addLabels()
            }
        }
        getContent()
        checkForAutoRefresh()
        super.onRestart()
    }

    private fun checkForAutoRefresh() {
        if (MyApplication.wxoglRadarAutoRefresh || MyApplication.locationDotFollowsGps) {
            mInterval = 60000 * Utility.readPref(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (gpsEnabled != null && gpsEnabled) {
                    locationManager?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            //20000.toLong(),
                            (MyApplication.radarLocationUpdateInterval * 1000).toLong(),
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
        if (MyApplication.sn_locationreport) {
            UtilityLog.d("wx", "starting location report")
            sn_Handler_m = Handler(Looper.getMainLooper())
            start_sn_reporting()
        }

        if (MyApplication.radarConusRadar) {
            conus_Handler_m = Handler(Looper.getMainLooper())
            start_conusimage()
        }
        //super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        radarSitesForFavorites = UtilityFavorites.setupMenu(this@WXGLRadarActivity, MyApplication.ridFav, wxglRender.rid, prefToken)
        invalidateOptionsMenu()
        if (!isGetContentInProgress) {
            isGetContentInProgress = true
            val ridIsTdwr = WXGLNexrad.isRidTdwr(wxglRender.rid)
            if (ridIsTdwr) {
                l3Menu.isVisible = false
                l2Menu.isVisible = false
                tdwrMenu.isVisible = true
            } else {
                l3Menu.isVisible = true
                l2Menu.isVisible = true
                tdwrMenu.isVisible = false
            }
            if ((wxglRender.product.matches(Regex("N[0-3]Q")) || wxglRender.product == "L2REF") && ridIsTdwr) {
                if (tilt == "3") tilt = "2"
                wxglRender.product = "TZL"
            }
            if ((wxglRender.product == "TZL" || wxglRender.product.startsWith("TZ")) && !ridIsTdwr) {
                wxglRender.product = "N" + tilt + "Q"
            }
            if ((wxglRender.product.matches(Regex("N[0-3]U")) || wxglRender.product == "L2VEL") && ridIsTdwr) {
                if (tilt == "3") tilt = "2"
                wxglRender.product = "TV$tilt"
            }
            if (wxglRender.product.startsWith("TV") && !ridIsTdwr) {
                wxglRender.product = "N" + tilt + "U"
            }
            title = wxglRender.product
            adjustTiltMenu()
            setStarButton()
            toolbar.subtitle = ""
            if (!wxglRender.product.startsWith("2")) {
                UtilityRadarUI.initWxOglGeom(
                        wxglSurfaceView,
                        wxglRender,
                        0,
                        oldRadarSites, // was oldRidArr
                        wxglRenders,
                        wxglTextObjects,
                        paneList,
                        objectImageMap,
                        wxglSurfaceViews,
                        ::getGPSFromDouble,
                        ::getLatLon,
                        archiveMode
                )
            }
            withContext(Dispatchers.IO) {
                UtilityRadarUI.plotRadar(
                        wxglRender,
                        urlStr,
                        this@WXGLRadarActivity,
                        ::getGPSFromDouble,
                        ::getLatLon,
                        //true,
                        archiveMode
                )
            }
            if (!oglInView) {
                img.visibility = View.GONE
                wxglSurfaceView.visibility = View.VISIBLE
                oglInView = true
            }
            if (ridChanged && !restartedZoom) ridChanged = false
            if (restartedZoom) {
                restartedZoom = false
                ridChanged = false
            }
            if (PolygonType.SPOTTER_LABELS.pref && !archiveMode) UtilityWXGLTextObject.updateSpotterLabels(numberOfPanes, wxglTextObjects)
	    //elys mod
	    if (PolygonType.HAIL_LABELS.pref && !archiveMode) {
            UtilityWXGLTextObject.updateHailLabels(numberOfPanes, wxglTextObjects)
            }
            if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) UtilityWXGLTextObject.updateObservations(numberOfPanes, wxglTextObjects)
            wxglSurfaceView.requestRender()
            if (legendShown && wxglRender.product != oldProd && wxglRender.product != "DSA" && wxglRender.product != "DAA") updateLegend()
            if (legendShown && (wxglRender.product == "DSA" || wxglRender.product == "DAA" || wxglRender.product == "N0U")) {
                dspLegendMax = (255.0f / wxglRender.wxglNexradLevel3.halfword3132) * 0.01f
                velMax = wxglRender.wxglNexradLevel3.halfword48
                velMin = wxglRender.wxglNexradLevel3.halfword47
                updateLegend()
            }
            oldProd = wxglRender.product
            setSubTitle()
            animRan = false
            firstRun = false
            withContext(Dispatchers.IO) { UtilityDownloadWarnings.get(this@WXGLRadarActivity) }
            if (!wxglRender.product.startsWith("2")) UtilityRadarUI.plotWarningPolygons(wxglSurfaceView, wxglRender, archiveMode)
            // FIXME move to method
            val tstCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTst.value)
            val torCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTor.value)
            val ffwCount = UtilityVtec.getStormCount(MyApplication.severeDashboardFfw.value)
            if (MyApplication.radarWarnings) title = wxglRender.product + " (" + tstCount.toString() + "," + torCount.toString() + "," + ffwCount.toString() + ")"
            if (PolygonType.MCD.pref && !archiveMode) {
                withContext(Dispatchers.IO) {
                    UtilityDownloadMcd.get(this@WXGLRadarActivity)
                    UtilityDownloadWatch.get(this@WXGLRadarActivity)
                }
                if (!wxglRender.product.startsWith("2")) UtilityRadarUI.plotMcdWatchPolygons(wxglSurfaceView, wxglRender, archiveMode)
            }
            if (PolygonType.MPD.pref && !archiveMode) {
                withContext(Dispatchers.IO) { UtilityDownloadMpd.get(this@WXGLRadarActivity) }
                if (!wxglRender.product.startsWith("2")) UtilityRadarUI.plotMpdPolygons(wxglSurfaceView, wxglRender, archiveMode)
            }
            if (MyApplication.radarShowWpcFronts && !archiveMode) {
                withContext(Dispatchers.IO) { UtilityWpcFronts.get(this@WXGLRadarActivity) }
                if (!wxglRender.product.startsWith("2")) UtilityRadarUI.plotWpcFronts(wxglSurfaceView, wxglRender, archiveMode)
                UtilityWXGLTextObject.updateWpcFronts(numberOfPanes, wxglTextObjects)
            }
            UtilityRadarUI.updateLastRadarTime(this@WXGLRadarActivity)
            isGetContentInProgress = false
        } // end check is get content in progress
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        if (!oglInView) {
            img.visibility = View.GONE
            wxglSurfaceView.visibility = View.VISIBLE
            oglInView = true
        }
        inOglAnim = true
        animRan = true
        withContext(Dispatchers.IO) {
            frameCountGlobal = frameCount
            var animArray = WXGLDownload.getRadarFilesForAnimation(this@WXGLRadarActivity, frameCount, wxglRender.rid, wxglRender.product)
            var file: File
            var timeMilli: Long
            var priorTime: Long
            try {
                animArray.indices.forEach {
                    file = File(this@WXGLRadarActivity.filesDir, animArray[it])
                    this@WXGLRadarActivity.deleteFile("nexrad_anim$it")
                    if (!file.renameTo(File(this@WXGLRadarActivity.filesDir, "nexrad_anim$it")))
                        UtilityLog.d("wx", "Problem moving to nexrad_anim$it")
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    // TODO pass wxglRender only
                    animArray = WXGLDownload.getRadarFilesForAnimation(this@WXGLRadarActivity, frameCount, wxglRender.rid, wxglRender.product)
                    try {
                        animArray.indices.forEach {
                            file = File(this@WXGLRadarActivity.filesDir, animArray[it])
                            this@WXGLRadarActivity.deleteFile("nexrad_anim$it")
                            if (!file.renameTo(File(this@WXGLRadarActivity.filesDir, "nexrad_anim$it")))
                                UtilityLog.d("wx", "Problem moving to nexrad_anim$it")
                        }
                    } catch (e: Exception) {
                        UtilityLog.handleException(e)
                    }
                    animTriggerDownloads = false
                }
                for (r in animArray.indices) {
                    while (inOglAnimPaused) SystemClock.sleep(delay.toLong())
                    // formerly priorTime was set at the end but that is goofed up with pause
                    priorTime = UtilityTime.currentTimeMillis()
                    // added because if paused and then another icon life vel/ref it won't load correctly, likely timing issue
                    if (!inOglAnim) break
                    // if the first pass has completed, for L2 no longer uncompress, use the existing decomp files
                    if (loopCnt > 0)
                        wxglRender.constructPolygons("nexrad_anim$r", urlStr, false)
                    else
                        wxglRender.constructPolygons("nexrad_anim$r", urlStr, true)
                    launch(uiDispatcher) { progressUpdate((r + 1).toString(), animArray.size.toString()) }
                    wxglSurfaceView.requestRender()
                    timeMilli = UtilityTime.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay) SystemClock.sleep(delay - ((timeMilli - priorTime)))
                    if (!inOglAnim) break
                    if (r == (animArray.lastIndex)) SystemClock.sleep(delay.toLong() * 2)
                }
                loopCnt += 1
            }
        }
    }

    private fun progressUpdate(vararg values: String) {
        if ((values[1].toIntOrNull() ?: 0) > 1) {
            val list = WXGLNexrad.getRadarInfo(this@WXGLRadarActivity,"").split(" ")
            if (list.size > 3)
                toolbar.subtitle = list[3] + " (" + values[0] + "/" + values[1] + ")"
            else
                toolbar.subtitle = ""
        } else {
            toolbar.subtitle = "Problem downloading"
        }
    }

    private fun setSubTitle() {
        val items = WXGLNexrad.getRadarInfo(this@WXGLRadarActivity,"").split(" ")
        if (items.size > 3) {
            toolbar.subtitle = items[3]
            if (UtilityTime.isRadarTimeOld(items[3]))
                toolbar.setSubtitleTextColor(Color.RED)
            else
                toolbar.setSubtitleTextColor(Color.LTGRAY)
        } else {
            toolbar.subtitle = ""
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        UtilityUI.immersiveMode(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        UtilityUI.immersiveMode(this)
        // This code is mostly duplicated below in the keyboard shortcut area
        if (inOglAnim && (item.itemId != R.id.action_fav) && (item.itemId != R.id.action_share) && (item.itemId != R.id.action_tools)) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            // (revert) 2016_08 have this apply to Level 3 in addition to Level 2
            if (wxglRender.product.contains("L2")) SystemClock.sleep(2000)
            setStarButton()
            animateButton.setIcon(MyApplication.ICON_PLAY_WHITE)
            animateButton.title = animateButtonPlayString
            getContent()
            if (item.itemId == R.id.action_a) return true
        }
        // TODO mark begin of menu stuff
        when (item.itemId) {
            R.id.action_help -> ObjectDialogue( this,
                    resources.getString(R.string.help_radar)
                            + MyApplication.newline + MyApplication.newline
                            + resources.getString(R.string.help_radar_drawingtools)
                            + MyApplication.newline + MyApplication.newline
                            + resources.getString(R.string.help_radar_recording)
                            + MyApplication.newline + MyApplication.newline
            )
            R.id.action_jellybean_drawtools -> {
                val intent = TelecineService.newIntent(this, 1, Intent())
                intent.putExtra("show_distance_tool", showDistanceTool)
                intent.putExtra("show_recording_tools", "false")
                startService(intent)
            }
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    showDistanceTool = "true"
                    checkOverlayPerms()
                } else {
                    if (animRan) {
                        val animDrawable = UtilityUSImgWX.animationFromFiles(this, wxglRender.rid, wxglRender.product, frameCountGlobal, "", true)
                        UtilityShare.animGif(
                                this,
                                wxglRender.rid + " (" + Utility.getRadarSiteName(wxglRender.rid) + ") " + wxglRender.product,
                                animDrawable
                        )
                    } else {
                        getImageForShare()
                    }
                }
            }
            R.id.action_settings -> startActivity(Intent(this, SettingsRadarActivity::class.java))
            R.id.action_radar_markers -> ObjectIntent.showImage(this, arrayOf("raw:radar_legend", "Radar Markers", "false"))
            R.id.action_radar_2 -> showMultipaneRadar("2")
            R.id.action_radar_4 -> showMultipaneRadar("4")
            R.id.action_radar_site_status_l3 -> ObjectIntent.showWebView(this, arrayOf("http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended"))
            R.id.action_radar_site_status_l2 -> ObjectIntent.showWebView(this, arrayOf("http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended"))
            R.id.action_n0q, R.id.action_n0q_menu  -> getReflectivity()
            R.id.action_n0u, R.id.action_n0u_menu -> getVelocity()
            R.id.action_tz0 -> changeProd("TZ$tilt", true)
            R.id.action_tv0 -> changeProd("TV$tilt", true)
            R.id.action_tzl -> changeProd("TZL", true)
            R.id.action_n0s -> changeProd("N" + tilt + "S", true)
            R.id.action_net -> changeProd("EET", false)
            R.id.action_N0X -> changeProd("N" + tilt + "X", true)
            R.id.action_N0C -> changeProd("N" + tilt + "C", true)
            R.id.action_N0K -> changeProd("N" + tilt + "K", true)
            R.id.action_H0C -> changeProd("H" + tilt + "C", true)
            R.id.action_radar_showhide -> showRadar()
            R.id.action_legend -> showLegend()
            R.id.action_about -> showRadarScanInfo()
            R.id.action_dvl -> changeProd("DVL", false)
            R.id.action_dsp -> changeProd("DSA", false)
            R.id.action_daa -> changeProd("DAA", false)
            R.id.action_nsw -> changeProd("NSW", false)
            //R.id.action_n1p -> changeProd("N1P", false)
            //R.id.action_ntp -> changeProd("NTP", false)
            R.id.action_ncr -> changeProd("NCR", false)
            R.id.action_ncz -> changeProd("NCZ", false)
            //I need those!  ELY M. 
	    R.id.action_et -> changeProd("ET", false)
            R.id.action_vil -> changeProd("VIL", false)
            R.id.action_l2vel -> changeProd("L2VEL", false)
            R.id.action_l2ref -> changeProd("L2REF", false)
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
            R.id.action_NVW -> getContentVwp()
            R.id.action_fav -> actionToggleFavorite()
            R.id.action_TDWR -> alertDialogTdwr()
            R.id.action_ridmap -> showMap()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getImageForShare() = GlobalScope.launch(uiDispatcher) {
        val bitmapForShare = withContext(Dispatchers.IO) { UtilityUSImgWX.layeredImgFromFile(
                this@WXGLRadarActivity,
                wxglRender.rid,
                wxglRender.product,
                "0",
                true
        ) }
        UtilityShare.bitmap(
                this@WXGLRadarActivity,
                this@WXGLRadarActivity,
                wxglRender.rid + " (" + Utility.getRadarSiteName(wxglRender.rid) + ") " + wxglRender.product,
                bitmapForShare
        )
    }

    private fun animateRadar(frameCount: Int) {
        animateButton.setIcon(MyApplication.ICON_STOP_WHITE)
        animateButton.title = animateButtonStopString
        starButton.setIcon(MyApplication.ICON_PAUSE_WHITE)
        starButton.title = pauseButtonString
        getAnimate(frameCount)
    }

    private fun changeProd(product: String, canTilt: Boolean) {
        wxglRender.product = product
        adjustTiltMenu()
        tiltOption = canTilt
        getContent()
    }

    private fun changeTilt(tiltStr: String) {
        tilt = tiltStr
        wxglRender.product = wxglRender.product.replace("N[0-3]".toRegex(), "N$tilt")
        if (wxglRender.product.startsWith("TR")) wxglRender.product = wxglRender.product.replace("TR[0-3]".toRegex(), "TR$tilt")
        if (wxglRender.product.startsWith("TZ")) wxglRender.product = wxglRender.product.replace("TZ[0-3]".toRegex(), "TZ$tilt")
        if (wxglRender.product.startsWith("TV")) wxglRender.product = wxglRender.product.replace("TV[0-3]".toRegex(), "TV$tilt")
        getContent()
    }

    private fun mapSwitch(radarSite: String) {
        objectImageMap.hideMap()
        wxglRender.rid = radarSite
        mapShown = false
        wxglSurfaceView.scaleFactor = MyApplication.wxoglSize / 10.0f
        wxglRender.setViewInitial(MyApplication.wxoglSize / 10.0f, 0.0f, 0.0f)
        adjustTiltMenu()
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, wxglRender.rid, starButton, prefToken)
    }

    private fun showRadarScanInfo() { ObjectDialogue(this, WXGLNexrad.getRadarInfo(this@WXGLRadarActivity,"")) }

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

    override fun onStop() {
        super.onStop()
        if (!archiveMode && !fixedSite) WXGLNexrad.savePrefs(this, "WXOGL", wxglRender)
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        mHandler?.let { stopRepeatingTask() }
        sn_Handler_m?.let { stop_sn_reporting() }
        conus_Handler_m?.let { stop_conusimage() }
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            )
                it.removeUpdates(locationListener)
        }
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                UtilityRadarUI.addItemsToLongPress(
                        dialogStatusList,
                        locXCurrent,
                        locYCurrent,
                        this@WXGLRadarActivity,
                        wxglSurfaceView,
                        wxglRender,
                        dialogRadarLongPress!!
                )
            } else {
                paneList.forEach { wxglTextObjects[it].addLabels() }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            if (mHandler != null) {
                if (loopCount > 0) { if (inOglAnim) animTriggerDownloads = true else getContent() }
                loopCount += 1
                handler.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun startRepeatingTask() {
        mHandler!!.removeCallbacks(mStatusChecker)
        mStatusChecker.run()
    }

    private fun stopRepeatingTask() {
        mHandler!!.removeCallbacks(mStatusChecker)
        mHandler = null
    }

    //report your spotter network location
    private val sn_handler = Handler(Looper.getMainLooper())
    private val sn_reporter: Runnable = object : Runnable {
        override fun run() {
            UtilityLog.d("wx", "SendPosition(this@WXGLRadarActivity) on lat: "+latD+" lon: "+lonD)
            SendPosition(this@WXGLRadarActivity)
            sn_handler.postDelayed(this, sn_Interval.toLong())
        }
    }
    private fun start_sn_reporting() {
        sn_Handler_m!!.removeCallbacks(sn_reporter)
        sn_reporter.run()
    }
    private fun stop_sn_reporting() {
        sn_Handler_m!!.removeCallbacks(sn_reporter)
        sn_Handler_m = null
    }
    //conus radar
    private val conus_handler = Handler(Looper.getMainLooper())
    private val conus_image: Runnable = object : Runnable {
        override fun run() {
            UtilityLog.d("wx", "downloading new conus image")
            //UtilityConusRadar.getConusGfw()
            UtilityConusRadar.getConusImage()
            conus_handler.postDelayed(this, conus_Interval.toLong())
        }
    }
    private fun start_conusimage() {
        conus_Handler_m!!.removeCallbacks(conus_image)
        conus_image.run()
    }
    private fun stop_conusimage() {
        conus_Handler_m!!.removeCallbacks(conus_image)
	    conus_Handler_m = null
    }
    override fun onPause() {
        mHandler?.let { stopRepeatingTask() }
        sn_Handler_m?.let { stop_sn_reporting() }
        conus_Handler_m?.let { stop_conusimage() }
        wxglSurfaceView.onPause()
        super.onPause()
    }

    override fun onResume() {
        checkForAutoRefresh()
        wxglSurfaceView.onResume()
        super.onResume()
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) { if (MyApplication.locationDotFollowsGps && !archiveMode) makeUseOfNewLocation(location) }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun makeUseOfNewLocation(location: Location) {
        latD = location.latitude
        lonD = location.longitude
        bearingCurrent = location.bearing
        speedCurrent = location.speed
        UtilityLog.d("wx", "bearing: "+bearingCurrent)
        UtilityLog.d("wx", "speed: "+speedCurrent)
        UtilityLog.d("wx", "speed in mph: "+(speedCurrent * 3.6 * 0.62137119))
        getGPSFromDouble()
        wxglRender.constructLocationDot(locXCurrent, locYCurrent, archiveMode)
        wxglSurfaceView.requestRender()
        if (MyApplication.wxoglCenterOnLocation) {
            UtilityWXGLTextObject.hideLabels(1, wxglTextObjects)
            UtilityWXGLTextObject.showLabels(1, wxglTextObjects)
        }
    }

    private fun getGPSFromDouble() {
        if (!archiveMode) {
            locXCurrent = latD.toString()
            locYCurrent = lonD.toString()
        }
    }

    private fun getLatLon() = LatLon(locXCurrent, locYCurrent)

    private fun setupAlertDialogRadarLongPress() {
        dialogRadarLongPress = ObjectDialogue(this@WXGLRadarActivity, dialogStatusList)
        dialogRadarLongPress!!.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this@WXGLRadarActivity)
        }
        dialogRadarLongPress!!.setSingleChoiceItems { dialog, which ->
            val strName = dialogStatusList[which]
            UtilityRadarUI.doLongPressAction(
                    strName,
                    this@WXGLRadarActivity,
                    this@WXGLRadarActivity,
                    wxglSurfaceView,
                    wxglRender,
                    uiDispatcher,
                    ::longPressRadarSiteSwitch
            )
            dialog.dismiss()
        }
    }

    private fun longPressRadarSiteSwitch(strName: String) {
        wxglRender.rid = strName.parse(UtilityRadarUI.longPressRadarSiteRegex)
        ridChanged = true
        stopAnimation()
        mapSwitch(wxglRender.rid)
    }

    private fun alertDialogTdwr() {
        val diaTdwr = ObjectDialogue(this@WXGLRadarActivity, GlobalArrays.tdwrRadars)
        diaTdwr.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this@WXGLRadarActivity)
        }
        diaTdwr.setSingleChoiceItems { dialog, which ->
            val strName = GlobalArrays.tdwrRadars[which]
            wxglRender.rid = strName.split(" ").getOrNull(0) ?: ""
            wxglRender.product = "TZL"
            mapSwitch(wxglRender.rid)
            title = wxglRender.product
            getContent()
            dialog.dismiss()
        }
        diaTdwr.show()
    }

    private var legend: ViewColorLegend? = null

    private fun showLegend() {
        if (!legendShown) {
            if (wxglRender.product == "DSA" || wxglRender.product == "DAA") dspLegendMax = (255.0f / wxglRender.wxglNexradLevel3.halfword3132) * 0.01f
            velMax = wxglRender.wxglNexradLevel3.halfword48
            velMin = wxglRender.wxglNexradLevel3.halfword47
            legendShown = true
            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
            legend = ViewColorLegend(this, wxglRender.product)
            rl.addView(legend, layoutParams)
            MyApplication.radarShowLegend = true
            Utility.writePref(this, "RADAR_SHOW_LEGEND", "true")
        } else {
            rl.removeView(legend)
            legendShown = false
            MyApplication.radarShowLegend = false
            Utility.writePref(this, "RADAR_SHOW_LEGEND", "false")
        }
    }

    private fun updateLegend() {
        rl.removeView(legend)
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
        legend = ViewColorLegend(this, wxglRender.product)
        rl.addView(legend, layoutParams)
    }

    private fun stopAnimation() {
        inOglAnim = false
        inOglAnimPaused = false
        animateButton.setIcon(MyApplication.ICON_PLAY_WHITE)
        animateButton.title = animateButtonPlayString
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        radarSitesForFavorites = UtilityFavorites.setupMenu(this, MyApplication.ridFav, wxglRender.rid, prefToken)
        when (item.itemId) {
            R.id.action_sector -> {
                genericDialog(radarSitesForFavorites) {
                    if (radarSitesForFavorites.size > 2) {
                        stopAnimation()
                        when (it) {
                            1 -> ObjectIntent.favoriteAdd(this, arrayOf("RID"))
                            2 -> ObjectIntent.favoriteRemove(this, arrayOf("RID"))
                            else -> {
                                if (radarSitesForFavorites[it] == " ") {
                                    wxglRender.rid = joshuatee.wx.settings.Location.rid
                                } else {
                                    wxglRender.rid = radarSitesForFavorites[it].split(" ").getOrNull(0) ?: ""
                                }
                                mapSwitch(wxglRender.rid)
                                getContent()
                            }
                        }
                        if (firstTime) {
                            UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                            firstTime = false
                        }
                    }
                    UtilityUI.immersiveMode(this)
                }
            }
            android.R.id.home -> {
                if (Utility.readPref(this@WXGLRadarActivity, "LAUNCH_TO_RADAR", "false") == "false") {
                    NavUtils.navigateUpFromSameTask(this)
                } else {
                    navigateUp()
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateUp() {
        val upIntent = NavUtils.getParentActivityIntent(this)
        if (NavUtils.shouldUpRecreateTask(this, upIntent!!) || isTaskRoot) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities()
        } else {
            NavUtils.navigateUpTo(this, upIntent)
        }
    }

    /* orinigal
    private fun getContentVwp() = GlobalScope.launch(uiDispatcher) {
        val data = withContext(Dispatchers.IO) { UtilityWXOGL.getVwp(this@WXGLRadarActivity, wxglRender.rid) }
        ObjectIntent.showText(this@WXGLRadarActivity, arrayOf(data, wxglRender.rid + " VAD Wind Profile"))
    }
    */

    private fun getContentVwp() = GlobalScope.launch(uiDispatcher) {
        //val txt = withContext(Dispatchers.IO) { UtilityWXOGL.getVwp(this@WXGLRadarActivity, oglr.rid) }
        //ObjectIntent(this@WXGLRadarActivity, TextScreenActivity::class.java, TextScreenActivity.URL, arrayOf(txt, oglr.rid + " VAD Wind Profile"))
        var vmpurl = "https://weather.cod.edu/satrad/nexrad/index.php?type="+wxglRender.rid+"-NVW"
        ObjectIntent(this@WXGLRadarActivity, WebView::class.java, WebView.URL, arrayOf(vmpurl, wxglRender.rid + " VAD Wind Profile"))

    }

    private fun getReflectivity() {
        if (MyApplication.radarIconsLevel2 && wxglRender.product.matches("N[0-3]Q".toRegex())) {
            wxglRender.product = "L2REF"
            tiltOption = false
        } else {
            if (!WXGLNexrad.isRidTdwr(wxglRender.rid)) {
                wxglRender.product = "N" + tilt + "Q"
                tiltOption = true
            } else {
                wxglRender.product = "TZL"
                tiltOption = false
            }
        }
        getContent()
    }

    private fun getVelocity() {
        if (MyApplication.radarIconsLevel2 && wxglRender.product.matches("N[0-3]U".toRegex())) {
            wxglRender.product = "L2VEL"
            tiltOption = false
        } else {
            if (!WXGLNexrad.isRidTdwr(wxglRender.rid)) {
                wxglRender.product = "N" + tilt + "U"
                tiltOption = true
            } else {
                wxglRender.product = "TV$tilt"
                tiltOption = true
            }
        }
        getContent()
    }

    private fun actionToggleFavorite() {
        if (inOglAnim) {
            inOglAnimPaused = if (!inOglAnimPaused) {
                starButton.setIcon(MyApplication.ICON_PLAY_WHITE)
                starButton.title = resumeButtonString
                true
            } else {
                starButton.setIcon(MyApplication.ICON_PAUSE_WHITE)
                starButton.title = pauseButtonString
                false
            }
        } else {
            toggleFavorite()
        }
    }

    private fun showMap() {
        objectImageMap.toggleMap()
        if (objectImageMap.map.visibility != View.GONE) {
            UtilityWXGLTextObject.hideLabels(numberOfPanes, wxglTextObjects)
        } else {
            UtilityWXGLTextObject.showLabels(numberOfPanes, wxglTextObjects)
        }
    }

    private fun showMultipaneRadar(numberOfPanes: String) {
        if (!archiveMode && !fixedSite) WXGLNexrad.savePrefs(this, "WXOGL", wxglRender)
        ObjectIntent.showRadarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", numberOfPanes, "true"))
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_2 -> {
                if (event.isCtrlPressed) showMultipaneRadar("2")
                return true
            }
            KeyEvent.KEYCODE_4 -> {
                if (event.isCtrlPressed) showMultipaneRadar("4")
                return true
            }
            KeyEvent.KEYCODE_L -> {
                if (event.isCtrlPressed) showMap()
                return true
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
                return true
            }
            KeyEvent.KEYCODE_A -> {
                if (event.isCtrlPressed) {
                    if (inOglAnim) {
                        inOglAnim = false
                        inOglAnimPaused = false
                        // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
                        // otherwise the new selection might overwrite in the OGLR object - hack
                        // (revert) 2016_08 have this apply to Level 3 in addition to Level 2
                        if (wxglRender.product.contains("L2")) SystemClock.sleep(2000)
                        setStarButton()
                        animateButton.setIcon(MyApplication.ICON_PLAY_WHITE)
                        animateButton.title = animateButtonPlayString
                        getContent()
                    } else {
                        animateRadar(MyApplication.uiAnimIconFrames.toIntOrNull() ?: 0)
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_F -> {
                if (event.isCtrlPressed) actionToggleFavorite()
                return true
            }
            KeyEvent.KEYCODE_R -> {
                if (event.isCtrlPressed) getReflectivity()
                return true
            }
            KeyEvent.KEYCODE_V -> {
                if (event.isCtrlPressed) getVelocity()
                return true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isAltPressed) ObjectDialogue(this, Utility.showRadarShortCuts())
                return true
            }
            KeyEvent.KEYCODE_REFRESH -> {
                getContent()
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (event.isCtrlPressed) {
                    wxglSurfaceView.zoomOutByKey()
                } else {
                    wxglSurfaceView.onScrollByKeyboard(0.0f, -20.0f)
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (event.isCtrlPressed) {
                    wxglSurfaceView.zoomInByKey()
                } else {
                    wxglSurfaceView.onScrollByKeyboard(0.0f, 20.0f)
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                wxglSurfaceView.onScrollByKeyboard(-20.0f, 0.0f)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                wxglSurfaceView.onScrollByKeyboard(20.0f, 0.0f)
                return true
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
    
    
    private fun showRadar() {
        UtilityLog.d("radarshow", "showRadar() radarShown: "+radarShown)
        if (radarShown) {
            UtilityLog.d("radarshow", "showRadar() setting to false")
            radarShown = false
            MyApplication.radarShowRadar = false
            Utility.writePref(this, "RADAR_SHOW_RADAR", "false")
            UtilityLog.d("radarshow", "showRadar() MyApplication.radarShowRadar: "+MyApplication.radarShowRadar)
        } else {
            UtilityLog.d("radarshow", "showRadar() setting to true")
            radarShown = true
            MyApplication.radarShowRadar = true
            Utility.writePref(this, "RADAR_SHOW_RADAR", "true")
            UtilityLog.d("radarshow", "showRadar() MyApplication.radarShowRadar: "+MyApplication.radarShowRadar)
        }
    }


}
