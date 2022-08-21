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

import java.io.File
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.*
import androidx.core.view.WindowCompat
import joshuatee.wx.R
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.telecine.TelecineService
import joshuatee.wx.ui.*
import joshuatee.wx.Extensions.*
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.*
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.*
import joshuatee.wx.radar.SpotterNetworkPositionReport.SendPosition
import kotlinx.coroutines.*
//elys mod
import joshuatee.wx.activitiesmisc.WebView


class WXGLRadarActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
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
    private var mHandler: Handler? = null
    private var mInterval = 180000 // 180 seconds by default
    //elys mod
    //private var sn_Handler_m: Handler? = null
    //private var sn_Interval = 180000 // 180 seconds by default
    private var conus_Handler_m: Handler? = null
    private var conus_Interval = 300000 // 5 mins for conus download might more is better
    private var loopCount = 0
    private var firstTime = true
    private var inOglAnim = false
    private var inOglAnimPaused = false
    private lateinit var objectImageMap: ObjectImageMap
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
    private lateinit var relativeLayout: RelativeLayout
    private var locationManager: LocationManager? = null
    private var animTriggerDownloads = false
    private val dialogStatusList = mutableListOf<String>()
    private var legendShown = false
    //elys mod
    private var radarShown = true
    private var dialogRadarLongPress: ObjectDialogue? = null
    private val animateButtonPlayString = "Animate Frames"
    private val animateButtonStopString = "Stop animation"
    private val pauseButtonString = "Pause animation"
    private val starButtonString = "Toggle favorite"
    private val resumeButtonString = "Resume animation"
    private var settingsVisited = false
    private var nexradArguments = NexradArguments()
    private lateinit var nexradState: NexradState

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswxoglradar_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = nexradState.radarSitesForFavorites.safeGet(0).split(" ")[0]
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Utility.isThemeAllWhite()) {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl_white, R.menu.uswxoglradar, iconsEvenlySpaced = true, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl, R.menu.uswxoglradar, iconsEvenlySpaced = true, bottomToolbar = true)
        }	
    	//elys mod
        if (UIPreferences.checkinternet) {
            Utility.checkInternet(this)
        }	
        objectToolbarBottom.connect(this)
        toolbar.setOnClickListener { Route.severeDash(this) }
        // set static var to false on start of activity
        spotterShowSelected = false

        val arguments = intent.getStringArrayExtra(RID)
        nexradArguments.processArguments(arguments)
        nexradArguments.locXCurrent = joshuatee.wx.settings.Location.x
        nexradArguments.locYCurrent = joshuatee.wx.settings.Location.y

        setupWindowOptions()
        setupRadarLongPress()
        nexradState = NexradState(this, 1, listOf(R.id.rl), 1, 1)
        val latLonArrD = UtilityLocation.getGps(this)
        nexradState.latD = latLonArrD[0]
        nexradState.lonD = latLonArrD[1]
        setupMenu()
        delay = UtilityImg.animInterval(this)
        NexradDraw.initGlView(nexradState.curRadar, nexradState,this, changeListener, nexradArguments.archiveMode)
        //
        // clickable map setup
        //
        objectImageMap = ObjectImageMap(this, R.id.map, toolbar, toolbarBottom, nexradState.wxglSurfaceViews)
        objectImageMap.connect(::mapSwitch, UtilityImageMap::mapToRid)
        //
        // set config, show color palette legend, get content, and check for auto refresh
        //
        nexradState.readPreferences(arguments, nexradArguments)
        if (RadarPreferences.showLegend) {
            showLegend()
        }
        checkForAutoRefresh()
        getContent()
    }

    private fun setupWindowOptions() {
        UtilityUI.immersiveMode(this)
        if (UIPreferences.radarStatusBarTransparent) {
//            This constant was deprecated in API level 30.
//            Use Window#setStatusBarColor(int) with a half-translucent color instead.
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= 30) {
                window.statusBarColor = Color.TRANSPARENT
                WindowCompat.setDecorFitsSystemWindows(window, false)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
        UtilityFileManagement.deleteCacheFiles(this)
        UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        toolbar.setTitleTextColor(Color.WHITE)
        if (nexradArguments.archiveMode && !spotterShowSelected) {
            toolbarBottom.visibility = View.GONE
        }
    }

    fun setupMenu() {
        starButton = objectToolbarBottom.getFavIcon()
        animateButton = objectToolbarBottom.find(R.id.action_a)
        tiltMenu = objectToolbarBottom.find(R.id.action_tilt)
        tiltMenuOption4 = objectToolbarBottom.find(R.id.action_tilt4)
        l3Menu = objectToolbarBottom.find(R.id.action_l3)
        l2Menu = objectToolbarBottom.find(R.id.action_l2)
        tdwrMenu = objectToolbarBottom.find(R.id.action_tdwr)
        if (!UIPreferences.radarImmersiveMode) {
            objectToolbarBottom.hide(R.id.action_blank)
            objectToolbarBottom.hide(R.id.action_level3_blank)
            objectToolbarBottom.hide(R.id.action_level2_blank)
            objectToolbarBottom.hide(R.id.action_animate_blank)
            objectToolbarBottom.hide(R.id.action_tilt_blank)
            objectToolbarBottom.hide(R.id.action_tools_blank)
        }
        objectToolbarBottom.hide(R.id.action_jellybean_drawtools)
        // FIXME TODO disable new Level3 super-res until NWS is past deployment phase
        objectToolbarBottom.hide(R.id.action_n0b)
        objectToolbarBottom.hide(R.id.action_n0g)
    }

    private fun adjustTiltMenu() {
        if (WXGLNexrad.isTdwr(nexradState.product )) {
            tiltMenuOption4.isVisible = false
            tiltMenu.isVisible = nexradState.product.matches(Regex("[A-Z][A-Z][0-2]"))
        } else {
            tiltMenuOption4.isVisible = true
            tiltMenu.isVisible = nexradState.product.matches(Regex("[A-Z][0-3][A-Z]"))
        }
    }

    private fun setStarButton() {
        if (UIPreferences.ridFav.contains(":" + nexradState.radarSite + ":")) {
            starButton.setIcon(GlobalVariables.STAR_ICON_WHITE)
        } else {
            starButton.setIcon(GlobalVariables.STAR_OUTLINE_ICON_WHITE)
        }
        starButton.title = starButtonString
    }

    override fun onRestart() {
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        setStarButton()
        animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
        animateButton.title = animateButtonPlayString
        if (objectImageMap.visibility == View.GONE) {
            nexradState.wxglTextObjects.forEach {
                it.initializeLabels(this)
                it.addLabels()
            }
        }
        getContent()
        checkForAutoRefresh()
        super.onRestart()
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
                            //20000.toLong(),
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
/*
    	//elys mod
        if (RadarPreferences.sn_locationreport) {
            UtilityLog.d("wx", "starting location report")
            sn_Handler_m = Handler(Looper.getMainLooper())
            start_sn_reporting()
        }
*/

        if (RadarPreferences.conusRadar) {
            conus_Handler_m = Handler(Looper.getMainLooper())
            start_conusimage()
        }
        //super.onRestart()
    }

    @Synchronized private fun getContent() {
        getContentPrep()
        initGeom()
        FutureVoid(this, {
            NexradDraw.plotRadar(
                    nexradState.render,
                    nexradArguments.urlStr,
                    ::getGPSFromDouble,
                    ::getLatLon,
                    nexradArguments.archiveMode
            )
        }, {
                nexradState.surface.visibility = View.VISIBLE
                nexradState.draw()
                UtilityRadarUI.updateLastRadarTime(this)
                setSubTitle()
        })
        updateLegendAfterDownload()
        nexradState.oldProd = nexradState.product
        // TODO FIXME download should take NexradState only
        NexradLayerDownload.download(
                this,
                nexradState.numberOfPanes,
                nexradState.render,
                nexradState.surface,
                nexradState.wxglTextObjects,
                ::setTitleWithWarningCounts)
    }

    private fun getContentPrep() {
        nexradState.radarSitesForFavorites = UtilityFavorites.setupMenu(this, UIPreferences.ridFav, nexradState.radarSite, prefToken)
        invalidateOptionsMenu()
        val ridIsTdwr = WXGLNexrad.isRidTdwr(nexradState.radarSite)
        if (ridIsTdwr) {
            l3Menu.isVisible = false
            l2Menu.isVisible = false
            tdwrMenu.isVisible = true
        } else {
            l3Menu.isVisible = true
            l2Menu.isVisible = true
            tdwrMenu.isVisible = false
        }
        nexradState.adjustForTdwrSinglePane()
        title = nexradState.product
        adjustTiltMenu()
        setStarButton()
        toolbar.subtitle = ""
    }

    private fun initGeom() {
        NexradDraw.initGeom(
                0,
                nexradState.oldRadarSites,
                nexradState.wxglRenders,
                nexradState.wxglTextObjects,
                objectImageMap,
                nexradState.wxglSurfaceViews,
                ::getGPSFromDouble,
                ::getLatLon,
                nexradArguments.archiveMode,
                settingsVisited
        )
        settingsVisited = false
    }

    private fun updateLegendAfterDownload() {
        if (legendShown && nexradState.product != nexradState.oldProd && nexradState.product != "DSA" && nexradState.product != "DAA") {
            updateLegend()
        }
        if (legendShown && (nexradState.product == "DSA" || nexradState.product == "DAA" || nexradState.product == "N0U")) {
            dspLegendMax = (255.0f / nexradState.render.wxglNexradLevel3.halfword3132) * 0.01f
            velMax = nexradState.render.wxglNexradLevel3.halfword48
            velMin = nexradState.render.wxglNexradLevel3.halfword47
            updateLegend()
        }
    }

    private fun setTitleWithWarningCounts() {
        if (RadarPreferences.warnings) {
            title = nexradState.product + " (" +
                    WXGLPolygonWarnings.getCount(PolygonWarningType.ThunderstormWarning).toString() + "," +
                    WXGLPolygonWarnings.getCount(PolygonWarningType.TornadoWarning).toString() + "," +
                    WXGLPolygonWarnings.getCount(PolygonWarningType.FlashFloodWarning).toString() + ")"
        }
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        nexradState.surface.visibility = View.VISIBLE
        inOglAnim = true
        withContext(Dispatchers.IO) {
            frameCountGlobal = frameCount
            var animArray = WXGLDownload.getRadarFilesForAnimation(this@WXGLRadarActivity, frameCount, nexradState.radarSite, nexradState.product)
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
                    animArray = WXGLDownload.getRadarFilesForAnimation(this@WXGLRadarActivity, frameCount, nexradState.radarSite, nexradState.product)
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
                    while (inOglAnimPaused) {
                        SystemClock.sleep(delay.toLong())
                    }
                    // formerly priorTime was set at the end but that is goofed up with pause
                    priorTime = ObjectDateTime.currentTimeMillis()
                    // added because if paused and then another icon life vel/ref it won't load correctly, likely timing issue
                    if (!inOglAnim) {
                        break
                    }
                    // if the first pass has completed, for L2 no longer uncompress, use the existing decomp files
                    if (loopCnt > 0)
                        nexradState.render.constructPolygons("nexrad_anim$r", nexradArguments.urlStr, false)
                    else
                        nexradState.render.constructPolygons("nexrad_anim$r", nexradArguments.urlStr, true)
                    launch(uiDispatcher) { progressUpdate((r + 1).toString(), animArray.size.toString()) }
                    nexradState.draw()
                    timeMilli = ObjectDateTime.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay) {
                        SystemClock.sleep(delay - ((timeMilli - priorTime)))
                    }
                    if (!inOglAnim) {
                        break
                    }
                    if (r == (animArray.lastIndex)) {
                        SystemClock.sleep(delay.toLong() * 2)
                    }
                }
                loopCnt += 1
            }
        }
    }

    private fun progressUpdate(vararg values: String) {
        if ((values[1].toIntOrNull() ?: 0) > 1) {
            val list = WXGLNexrad.getRadarInfo(this, "").split(" ")
            if (list.size > 3)
                toolbar.subtitle = list[3] + " (" + values[0] + "/" + values[1] + ")"
            else
                toolbar.subtitle = ""
        } else {
            toolbar.subtitle = "Problem downloading"
        }
    }

    private fun setSubTitle() {
        val items = WXGLNexrad.getRadarInfo(this,"").split(" ")
        if (items.size > 3) {
            toolbar.subtitle = items[3]
            if (ObjectDateTime.isRadarTimeOld(items[3]))
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
            if (nexradState.product.contains("L2")) {
                SystemClock.sleep(2000)
            }
            setStarButton()
            animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
            animateButton.title = animateButtonPlayString
            getContent()
            if (item.itemId == R.id.action_a) {
                return true
            }
        }
        when (item.itemId) {
            R.id.action_help -> ObjectDialogue( this,
                    resources.getString(R.string.help_radar)
                            + GlobalVariables.newline + GlobalVariables.newline
                            + resources.getString(R.string.help_radar_drawingtools)
                            + GlobalVariables.newline + GlobalVariables.newline
                            + resources.getString(R.string.help_radar_recording)
                            + GlobalVariables.newline + GlobalVariables.newline
            )
            R.id.action_jellybean_drawtools -> {
                val intent = TelecineService.newIntent(this, 1, Intent())
                intent.putExtra("show_distance_tool", showDistanceTool)
                intent.putExtra("show_recording_tools", "false")
                startService(intent)
            }
            R.id.action_share -> if (UIPreferences.recordScreenShare) {
                    showDistanceTool = "true"
                    checkOverlayPerms()
                } else {
                    UtilityRadarUI.getImageForShare(this, nexradState.render, "0")
                }
            R.id.action_settings -> { settingsVisited = true; Route.settingsRadar(this) }
            R.id.action_radar_markers -> Route.image(this, arrayOf("raw:radar_legend", "Radar Markers", "false"))
            R.id.action_radar_2 -> showMultipaneRadar("2")
            R.id.action_radar_4 -> showMultipaneRadar("4")
            R.id.action_radar_site_status_l3 -> Route.webView(this, arrayOf("http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended"))
            R.id.action_radar_site_status_l2 -> Route.webView(this, arrayOf("http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended"))
            R.id.action_n0q, R.id.action_n0q_menu  -> getReflectivity()
            R.id.action_n0u, R.id.action_n0u_menu -> getVelocity()
            R.id.action_n0b -> changeProduct("N" + nexradState.tilt + "B")
            R.id.action_n0g -> changeProduct("N" + nexradState.tilt + "G")
            R.id.action_tz0 -> changeProduct("TZ$nexradState.tilt")
            R.id.action_tv0 -> changeProduct("TV$nexradState.tilt")
            R.id.action_tzl -> changeProduct("TZL")
            R.id.action_n0s -> changeProduct("N" + nexradState.tilt + "S")
            R.id.action_net -> changeProduct("EET")
            R.id.action_N0X -> changeProduct("N" + nexradState.tilt + "X")
            R.id.action_N0C -> changeProduct("N" + nexradState.tilt + "C")
            R.id.action_N0K -> changeProduct("N" + nexradState.tilt + "K")
            R.id.action_H0C -> changeProduct("H" + nexradState.tilt + "C")
            R.id.action_radar_showhide -> showRadar()
            R.id.action_legend -> showLegend()
            R.id.action_about -> showRadarScanInfo()
            R.id.action_dvl -> changeProduct("DVL")
            R.id.action_dsp -> changeProduct("DSA")
            R.id.action_daa -> changeProduct("DAA")
            R.id.action_nsw -> changeProduct("NSW")
            R.id.action_ncr -> changeProduct("NCR")
            R.id.action_ncz -> changeProduct("NCZ")
            //I need those!  ELY M. 
	    R.id.action_et -> changeProduct("ET")
            R.id.action_vil -> changeProduct("VIL")
            R.id.action_l2vel -> changeProduct("L2VEL")
            R.id.action_l2ref -> changeProduct("L2REF")
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
            R.id.action_NVW -> getContentVwp()
            R.id.action_fav -> actionToggleFavorite()
            R.id.action_TDWR -> showTdwrDialog()
            R.id.action_ridmap -> objectImageMap.showMap(nexradState.numberOfPanes, nexradState.wxglTextObjects)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun animateRadar(frameCount: Int) {
        animateButton.setIcon(GlobalVariables.ICON_STOP_WHITE)
        animateButton.title = animateButtonStopString
        starButton.setIcon(GlobalVariables.ICON_PAUSE_WHITE)
        starButton.title = pauseButtonString
        getAnimate(frameCount)
    }

    private fun changeProduct(product: String) {
        nexradState.product = product
        getContent()
    }

    private fun changeTilt(tiltStr: String) {
        nexradState.changeTilt(tiltStr)
        getContent()
    }

    private fun mapSwitch(newRadarSite: String) {
        objectImageMap.hideMap()
        nexradState.adjustPaneTo(newRadarSite)
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, nexradState.radarSite, starButton, prefToken)
    }

    private fun showRadarScanInfo() {
        ObjectDialogue(this, WXGLNexrad.getRadarInfo(this,""))
    }

    override fun onStop() {
        super.onStop()
        nexradState.writePreferences(this, nexradArguments)
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        mHandler?.let { stopRepeatingTask() }
	    //elys mod
        //sn_Handler_m?.let { stop_sn_reporting() }
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
                UtilityRadarUI.setupContextMenu(
                        dialogStatusList,
                        nexradArguments.locXCurrent,
                        nexradArguments.locYCurrent,
                        this@WXGLRadarActivity,
                        nexradState.surface,
                        nexradState.render,
                        dialogRadarLongPress!!
                )
            } else {
                nexradState.wxglTextObjects.forEach {
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
                        getContent()
                    }
                }
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

/*
    //elys mod
    //report your spotter network location
    private val sn_handler = Handler(Looper.getMainLooper())
    private val sn_reporter: Runnable = object : Runnable {
        override fun run() {
            UtilityLog.d("wx", "SendPosition(this) on lat: "+nexradState.latD+" lon: "+nexradState.lonD)
            SendPosition(applicationContext)
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
*/

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
        ////sn_Handler_m?.let { stop_sn_reporting() }
        conus_Handler_m?.let { stop_conusimage() }
        nexradState.onPause()
        super.onPause()
    }

    override fun onResume() {
        checkForAutoRefresh()
        nexradState.onResume()
        super.onResume()
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (RadarPreferences.locationDotFollowsGps && !nexradArguments.archiveMode) {
                makeUseOfNewLocation(location)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun makeUseOfNewLocation(location: Location) {
        nexradState.latD = location.latitude
        nexradState.lonD = location.longitude
	//elys mod
        bearingCurrent = location.bearing
        speedCurrent = location.speed
        UtilityLog.d("wx", "bearing: "+bearingCurrent)
        UtilityLog.d("wx", "speed: "+speedCurrent)
        UtilityLog.d("wx", "speed in mph: "+(speedCurrent * 3.6 * 0.62137119))
        getGPSFromDouble()
        nexradState.updateLocationDots(nexradArguments)
    }

    private fun getGPSFromDouble() {
        if (!nexradArguments.archiveMode) {
            nexradArguments.locXCurrent = nexradState.latD.toString()
            nexradArguments.locYCurrent = nexradState.lonD.toString()
        }
    }

    private fun getLatLon() = LatLon(nexradState.latD, nexradState.lonD)

    private fun setupRadarLongPress() {
        dialogRadarLongPress = ObjectDialogue(this, dialogStatusList)
        dialogRadarLongPress!!.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        dialogRadarLongPress!!.connect { dialog, which ->
            val strName = dialogStatusList[which]
            UtilityRadarUI.doLongPressAction(
                    strName,
                    this,
                    nexradState.surface,
                    nexradState.render,
                    ::longPressRadarSiteSwitch
            )
            dialog.dismiss()
        }
    }

    private fun longPressRadarSiteSwitch(strName: String) {
        nexradState.radarSite = strName.parse(UtilityRadarUI.longPressRadarSiteRegex)
        stopAnimation()
        mapSwitch(nexradState.radarSite)
    }

    private fun showTdwrDialog() {
        val diaTdwr = ObjectDialogue(this, GlobalArrays.tdwrRadars)
        diaTdwr.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        diaTdwr.connect { dialog, which ->
            val strName = GlobalArrays.tdwrRadars[which]
            nexradState.radarSite = strName.split(" ").getOrNull(0) ?: ""
            nexradState.product = "TZL"
            mapSwitch(nexradState.radarSite)
            dialog.dismiss()
        }
        diaTdwr.show()
    }

    private var legend: ViewColorLegend? = null

    private fun showLegend() {
        if (!legendShown) {
            if (nexradState.product == "DSA" || nexradState.product == "DAA") {
                dspLegendMax = (255.0f / nexradState.render.wxglNexradLevel3.halfword3132) * 0.01f
            }
            velMax = nexradState.render.wxglNexradLevel3.halfword48
            velMin = nexradState.render.wxglNexradLevel3.halfword47
            legendShown = true
            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
            legend = ViewColorLegend(this, nexradState.product)
            relativeLayout.addView(legend, layoutParams)
            RadarPreferences.showLegend = true
            Utility.writePref(this, "RADAR_SHOW_LEGEND", "true")
        } else {
            relativeLayout.removeView(legend)
            legendShown = false
            RadarPreferences.showLegend = false
            Utility.writePref(this, "RADAR_SHOW_LEGEND", "false")
        }
    }

    private fun updateLegend() {
        relativeLayout.removeView(legend)
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
        legend = ViewColorLegend(this, nexradState.product)
        relativeLayout.addView(legend, layoutParams)
    }

    private fun stopAnimation() {
        inOglAnim = false
        inOglAnimPaused = false
        animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
        animateButton.title = animateButtonPlayString
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        nexradState.radarSitesForFavorites = UtilityFavorites.setupMenu(this, UIPreferences.ridFav, nexradState.radarSite, prefToken)
        when (item.itemId) {
            R.id.action_sector -> {
                ObjectDialogue.generic(this, nexradState.radarSitesForFavorites, ::getContent) {
                    if (nexradState.radarSitesForFavorites.size > 2) {
                        stopAnimation()
                        when (it) {
                            1 -> Route.favoriteAdd(this, arrayOf("RID"))
                            2 -> Route.favoriteRemove(this, arrayOf("RID"))
                            else -> {
                                if (nexradState.radarSitesForFavorites[it] == " ") {
                                    nexradState.radarSite = joshuatee.wx.settings.Location.rid
                                } else {
                                    nexradState.radarSite = nexradState.radarSitesForFavorites[it].split(" ").getOrNull(0) ?: ""
                                }
                                mapSwitch(nexradState.radarSite)
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
                if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
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
    private fun getContentVwp() {
        FutureText2(this,
                { UtilityWXOGL.getVwp(this, nexradState.radarSite) })
                { data -> Route.text(this, arrayOf(data, nexradState.radarSite + " VAD Wind Profile")) }
    }
*/    

    //elys mod
    private fun getContentVwp() = GlobalScope.launch(uiDispatcher) {
        var vmpurl = "https://weather.cod.edu/satrad/nexrad/index.php?type="+nexradState.radarSite+"-NVW"
        Route(this@WXGLRadarActivity, WebView::class.java, WebView.URL, arrayOf(vmpurl, nexradState.radarSite + " VAD Wind Profile"))

    }    
    private fun getReflectivity() {
        if (RadarPreferences.iconsLevel2 && nexradState.product.matches("N[0-3]Q".toRegex())) {
            nexradState.product = "L2REF"
        } else {
            if (!WXGLNexrad.isRidTdwr(nexradState.radarSite)) {
                nexradState.product = "N" + nexradState.tilt + "Q"
            } else {
                nexradState.product = "TZL"
            }
        }
        getContent()
    }

    private fun getVelocity() {
        if (RadarPreferences.iconsLevel2 && nexradState.product.matches("N[0-3]U".toRegex())) {
            nexradState.product = "L2VEL"
        } else {
            if (!WXGLNexrad.isRidTdwr(nexradState.radarSite)) {
                nexradState.product = "N" + nexradState.tilt + "U"
            } else {
                nexradState.product = "TV$nexradState.tilt"
            }
        }
        getContent()
    }

    private fun actionToggleFavorite() {
        if (inOglAnim) {
            inOglAnimPaused = if (!inOglAnimPaused) {
                starButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
                starButton.title = resumeButtonString
                true
            } else {
                starButton.setIcon(GlobalVariables.ICON_PAUSE_WHITE)
                starButton.title = pauseButtonString
                false
            }
        } else {
            toggleFavorite()
        }
    }

    private fun showMultipaneRadar(numberOfPanes: String) {
        nexradState.writePreferences(this, nexradArguments)
        Route.radarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", numberOfPanes, "true"))
    }

    fun toggleAnimate() {
        if (inOglAnim) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            if (nexradState.product.contains("L2")) {
                SystemClock.sleep(2000)
            }
            setStarButton()
            animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
            animateButton.title = animateButtonPlayString
            getContent()
        } else {
            animateRadar(RadarPreferences.uiAnimIconFrames.toIntOrNull() ?: 0)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_2 -> if (event.isCtrlPressed) showMultipaneRadar("2")
            KeyEvent.KEYCODE_4 -> if (event.isCtrlPressed) showMultipaneRadar("4")
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) objectImageMap.showMap(nexradState.numberOfPanes, nexradState.wxglTextObjects)
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) toggleAnimate()
            KeyEvent.KEYCODE_F -> if (event.isCtrlPressed) actionToggleFavorite()
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) getReflectivity()
            KeyEvent.KEYCODE_V -> if (event.isCtrlPressed) getVelocity()
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showRadarShortCuts())
            KeyEvent.KEYCODE_REFRESH -> getContent()
            KeyEvent.KEYCODE_DPAD_UP -> if (event.isCtrlPressed) {
                    nexradState.surface.zoomOutByKey()
                } else {
                    nexradState.surface.onScrollByKeyboard(0.0f, -20.0f)
                }
            KeyEvent.KEYCODE_DPAD_DOWN -> if (event.isCtrlPressed) {
                    nexradState.surface.zoomInByKey()
                } else {
                    nexradState.surface.onScrollByKeyboard(0.0f, 20.0f)
                }
            KeyEvent.KEYCODE_DPAD_LEFT -> nexradState.surface.onScrollByKeyboard(-20.0f, 0.0f)
            KeyEvent.KEYCODE_DPAD_RIGHT -> nexradState.surface.onScrollByKeyboard(20.0f, 0.0f)
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
    //elys mod
    private fun showRadar() {
        UtilityLog.d("radarshow", "showRadar() radarShown: "+radarShown)
        if (radarShown) {
            UtilityLog.d("radarshow", "showRadar() setting to false")
            radarShown = false
            RadarPreferences.showRadar = false
            Utility.writePref(this, "RADAR_SHOW_RADAR", "false")
            UtilityLog.d("radarshow", "showRadar() radarShowRadar: "+RadarPreferences.showRadar)
        } else {
            UtilityLog.d("radarshow", "showRadar() setting to true")
            radarShown = true
            RadarPreferences.showRadar = true
            Utility.writePref(this, "RADAR_SHOW_RADAR", "true")
            UtilityLog.d("radarshow", "showRadar() radarShowRadar: "+RadarPreferences.showRadar)
        }
    }
}
