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
import android.os.*
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.*
import androidx.core.view.WindowCompat
import joshuatee.wx.R
import joshuatee.wx.ui.*
import joshuatee.wx.Extensions.*
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.*
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.*
import kotlinx.coroutines.*
//elys mod
import joshuatee.wx.activitiesmisc.WebView


class WXGLRadarActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // This activity is a general purpose viewer of nexrad and mosaic content
    // nexrad data is downloaded from NWS FTP, decoded and drawn using OpenGL ES
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
    private var interval = 180000 // 180 seconds by default
    private val handler = Handler(Looper.getMainLooper())    
    //elys mod
    private var conus_Handler_m: Handler? = null
    private var conus_Interval = 300000 // 5 mins for conus download might more is better
    //elys mod
    private var radarShown = true
    private var loopCount = 0
    private var inOglAnim = false
    private var inOglAnimPaused = false
    private var animTriggerDownloads = false
    private var delay = 0
    private var locationManager: LocationManager? = null
    private lateinit var objectImageMap: ObjectImageMap
    private var settingsVisited = false
    private var nexradArguments = NexradArgumentsSinglePane()
    private lateinit var nexradState: NexradStatePane
    private lateinit var nexradSubmenu: NexradSubmenu
    private lateinit var nexradLongPressMenu: NexradLongPressMenu
    private lateinit var nexradUI: NexradUI
    private lateinit var nexradColorLegend: NexradColorLegend

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswxoglradar_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = nexradState.radarSitesForFavorites.safeGet(0).split(" ")[0]
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        nexradArguments.processArguments(intent.getStringArrayExtra(RID))
        setupWindowOptions(savedInstanceState)
        nexradState = NexradStatePane(this, 1, listOf(R.id.rl), 1, 1)
        nexradSubmenu = NexradSubmenu(objectToolbarBottom, nexradState)
        nexradLongPressMenu = NexradLongPressMenu(this, nexradState, nexradArguments, ::longPressRadarSiteSwitch)
        nexradUI = NexradUI(this, nexradState, nexradSubmenu)
        nexradState.initGlView(nexradLongPressMenu.changeListener)
        objectImageMap = ObjectImageMap(this, R.id.map, toolbar, toolbarBottom, nexradState.wxglSurfaceViews)
        objectImageMap.connect(::mapSwitch, UtilityImageMap::mapToRid)
        nexradState.readPreferences(nexradArguments)
        nexradColorLegend = NexradColorLegend(this, nexradState)
        getContent()
    }

    private fun setupWindowOptions(savedInstanceState: Bundle?) {
        if (Utility.isThemeAllWhite()) {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl_white, R.menu.uswxoglradar, iconsEvenlySpaced = true, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl, R.menu.uswxoglradar, iconsEvenlySpaced = true, bottomToolbar = true)
        }
        objectToolbarBottom.connect(this)
        toolbar.setOnClickListener { Route.severeDash(this) }
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
        delay = UtilityImg.animInterval(this)
    }

    override fun onRestart() {
        UtilityLog.d("WXRADAR", "onRestart")
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        nexradSubmenu.setStarButton()
        nexradSubmenu.setAnimateToPlay()
        if (objectImageMap.isHidden) {
            nexradState.wxglTextObjects.forEach {
                it.initializeLabels(this)
                it.addLabels()
            }
        }
        getContent()
        super.onRestart()
    }

    private fun checkForAutoRefresh() {
        if (RadarPreferences.wxoglRadarAutoRefresh || RadarPreferences.locationDotFollowsGps) {
            interval = 60000 * Utility.readPrefInt(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
            startRepeatingTask()
        }
	//elys mod
        if (RadarPreferences.conusRadar) {
            conus_Handler_m = Handler(Looper.getMainLooper())
            start_conusimage()
        }
    }

    @Synchronized private fun getContent() {
        nexradUI.getContentPrep()
        initGeom()
        FutureVoid(this, {
            NexradDraw.plotRadar(
                    nexradState.render,
                    nexradArguments.urlStr,
                    ::getGPSFromDouble,
                    nexradState::getLatLon,
                    nexradArguments.archiveMode
            )
        }, {
            nexradState.draw()
            nexradUI.setSubTitle()
            UtilityRadarUI.updateLastRadarTime(this)
            // TODO FIXME multipane has below
//            if (RadarPreferences.wxoglCenterOnLocation) {
//                nexradState.wxglSurfaceViews[z].resetView()
//            }
        })
        nexradColorLegend.updateLegendAfterDownload()
        nexradState.oldProd = nexradState.product
        NexradLayerDownload.download(
                this,
                nexradState.numberOfPanes,
                nexradState.render,
                nexradState.surface,
                nexradState.wxglTextObjects,
                nexradUI::setTitleWithWarningCounts)
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
                nexradState::getLatLon,
                nexradArguments.archiveMode,
                settingsVisited
        )
        settingsVisited = false
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        nexradState.showViews()
        inOglAnim = true
        withContext(Dispatchers.IO) {
            var animArray = nexradUI.animateDownloadFiles(frameCount)
            var timeMilli: Long
            var priorTime: Long
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    animArray = nexradUI.animateDownloadFiles(frameCount)
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
                    if (loopCnt > 0) {
                        nexradState.render.constructPolygons("nexrad_anim$r", nexradArguments.urlStr, false)
                    } else {
                        nexradState.render.constructPolygons("nexrad_anim$r", nexradArguments.urlStr, true)
                    }
                    launch(uiDispatcher) {
                        nexradUI.progressUpdate((r + 1).toString(), animArray.size.toString())
                    }
                    nexradState.draw()
                    timeMilli = ObjectDateTime.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay) {
                        SystemClock.sleep(delay - (timeMilli - priorTime))
                    }
                    if (!inOglAnim) {
                        break
                    }
                    if (r == animArray.lastIndex) {
                        SystemClock.sleep(delay.toLong() * 2)
                    }
                }
                loopCnt += 1
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        UtilityUI.immersiveMode(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        UtilityUI.immersiveMode(this)
        if (inOglAnim && (item.itemId != R.id.action_fav) && (item.itemId != R.id.action_share) && (item.itemId != R.id.action_tools)) {
            stopAnimationAndGetContent()
            if (item.itemId == R.id.action_a) {
                return true
            }
        }
        when (item.itemId) {
            R.id.action_help -> UtilityRadarUI.showHelp(this)
            R.id.action_share -> startScreenRecord()
            R.id.action_settings -> { settingsVisited = true; Route.settingsRadar(this) }
            R.id.action_radar_markers -> Route.image(this,"raw:radar_legend", "Radar Markers")
            R.id.action_radar_2 -> showMultipaneRadar(2)
            R.id.action_radar_4 -> showMultipaneRadar(4)
            R.id.action_radar_site_status_l3 -> Route.webView(this, "http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended")
            R.id.action_radar_site_status_l2 -> Route.webView(this, "http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended")
            R.id.action_n0q, R.id.action_n0q_menu  -> nexradState.getReflectivity(::getContent)
            R.id.action_n0u, R.id.action_n0u_menu -> nexradState.getVelocity(::getContent)
            R.id.action_n0b -> changeProduct("N" + nexradState.tilt + "B")
            R.id.action_n0g -> changeProduct("N" + nexradState.tilt + "G")
            R.id.action_tz0 -> changeProduct("TZ${nexradState.tilt}")
            R.id.action_tv0 -> changeProduct("TV${nexradState.tilt}")
            R.id.action_tzl -> changeProduct("TZL")
            R.id.action_n0s -> changeProduct("N" + nexradState.tilt + "S")
            R.id.action_net -> changeProduct("EET")
            R.id.action_N0X -> changeProduct("N" + nexradState.tilt + "X")
            R.id.action_N0C -> changeProduct("N" + nexradState.tilt + "C")
            R.id.action_N0K -> changeProduct("N" + nexradState.tilt + "K")
            R.id.action_H0C -> changeProduct("H" + nexradState.tilt + "C")
            R.id.action_radar_showhide -> showRadar()
            R.id.action_legend -> nexradColorLegend.showLegend()
            R.id.action_about -> nexradUI.showRadarScanInfo()
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
            R.id.action_TDWR -> nexradUI.showTdwrDialog(::mapSwitch)
            R.id.action_ridmap -> objectImageMap.showMap(nexradState.numberOfPanes, nexradState.wxglTextObjects)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startScreenRecord() {
        if (UIPreferences.recordScreenShare) {
            showDistanceTool = "true"
            checkOverlayPerms()
        } else {
            UtilityRadarUI.getImageForShare(this, nexradState.render, "0")
        }
    }

    private fun animateRadar(frameCount: Int) {
        nexradSubmenu.setAnimateToStop()
        nexradSubmenu.setAnimateToPause()
        getAnimate(frameCount)
    }

    private fun changeTilt(tiltStr: String) {
        nexradState.changeTilt(tiltStr)
        getContent()
    }

    private fun changeProduct(product: String) {
        nexradState.product = product
        getContent()
    }

    private fun mapSwitch(newRadarSite: String) {
        objectImageMap.hideMap()
        nexradState.adjustPaneTo(newRadarSite)
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, nexradState.radarSite, nexradSubmenu.starButton, FavoriteType.RID)
    }

    override fun onStop() {
        super.onStop()
        nexradState.writePreferences(this, nexradArguments)
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        stopRepeatingTask()
	    //elys mod
        conus_Handler_m?.let { stop_conusimage() }
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            )
                it.removeUpdates(locationListener)
        }
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (loopCount > 0) {
                if (inOglAnim) {
                    animTriggerDownloads = true
                } else {
                    getContent()
//                        val currentTime = ObjectDateTime.currentTimeMillis()
//                        lastRefresh = currentTime / 1000
                }
            }
            loopCount += 1
            handler.postDelayed(this, interval.toLong())
        }
    }

    private fun startRepeatingTask() {
        loopCount = 0
        handler.removeCallbacks(runnable)
        runnable.run()
    }

    private fun stopRepeatingTask() {
        handler.removeCallbacks(runnable)
    }


    //elys mod
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
        stopRepeatingTask()
        conus_Handler_m?.let { stop_conusimage() }
        nexradState.onPause()
        super.onPause()
    }

    // https://developer.android.com/reference/android/app/Activity
    override fun onResume() {
        UtilityLog.d("WXRADAR", "onResume")
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

    // FIXME TODO in NexradArgumentsSinglePane, move archivedMode to parent class
    // move this method to NexradUI
    private fun getGPSFromDouble() {
        if (!nexradArguments.archiveMode) {
            nexradArguments.locXCurrent = nexradState.latD
            nexradArguments.locYCurrent = nexradState.lonD
        }
    }

    private fun longPressRadarSiteSwitch(s: String) {
        nexradState.radarSite = s.split(" ")[0]
        stopAnimation()
        mapSwitch(nexradState.radarSite)
    }

    private fun stopAnimation() {
        inOglAnim = false
        inOglAnimPaused = false
        nexradSubmenu.setAnimateToPlay()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        nexradState.radarSitesForFavorites = UtilityFavorites.setupMenu(this, nexradState.radarSite, FavoriteType.RID)
        when (item.itemId) {
            R.id.action_sector -> {
                ObjectDialogue.generic(this, nexradState.radarSitesForFavorites, ::getContent) {
                    if (nexradState.radarSitesForFavorites.size > 2) {
                        stopAnimation()
                        when (it) {
                            1 -> Route.favoriteAdd(this, FavoriteType.RID)
                            2 -> Route.favoriteRemove(this, FavoriteType.RID)
                            else -> {
                                if (nexradState.radarSitesForFavorites[it] == " ") {
                                    nexradState.radarSite = joshuatee.wx.settings.Location.rid
                                } else {
                                    nexradState.radarSite = nexradState.radarSitesForFavorites[it].split(" ").getOrNull(0) ?: ""
                                }
                                mapSwitch(nexradState.radarSite)
                            }
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

    private fun actionToggleFavorite() {
        if (inOglAnim) {
            inOglAnimPaused = if (!inOglAnimPaused) {
                nexradSubmenu.setAnimateToResume()
                true
            } else {
                nexradSubmenu.setAnimateToPause()
                false
            }
        } else {
            toggleFavorite()
        }
    }

    private fun showMultipaneRadar(numberOfPanes: Int) {
        if (!nexradArguments.fixedSite) {
            nexradState.writePreferences(this, nexradArguments)
            Route.radarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", numberOfPanes.toString(), "true"))
        } else {
            // FIXME TODO this is not yet working
            Route.radarMultiPane(this, arrayOf(nexradArguments.originalRadarSite, "", numberOfPanes.toString(), "true"))
        }
    }

    private fun toggleAnimate() {
        if (inOglAnim) {
            stopAnimationAndGetContent()
        } else {
            animateRadar(RadarPreferences.uiAnimIconFrames.toIntOrNull() ?: 0)
        }
    }

    private fun stopAnimationAndGetContent() {
        inOglAnim = false
        inOglAnimPaused = false
        // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
        // otherwise the new selection might overwrite in the OGLR object - hack
        if (nexradState.product.contains("L2")) {
            SystemClock.sleep(2000)
        }
        nexradSubmenu.setStarButton()
        nexradSubmenu.setAnimateToPlay()
        getContent()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_2 -> if (event.isCtrlPressed) showMultipaneRadar(2)
            KeyEvent.KEYCODE_4 -> if (event.isCtrlPressed) showMultipaneRadar(4)
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) objectImageMap.showMap(nexradState.numberOfPanes, nexradState.wxglTextObjects)
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) toggleAnimate()
            KeyEvent.KEYCODE_F -> if (event.isCtrlPressed) actionToggleFavorite()
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) nexradState.getReflectivity(::getContent)
            KeyEvent.KEYCODE_V -> if (event.isCtrlPressed) nexradState.getVelocity(::getContent)
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
