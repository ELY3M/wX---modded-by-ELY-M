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
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.content.ContextCompat
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.*
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.*
import kotlinx.coroutines.*

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
    private var interval = 180000
    private val handler = Handler(Looper.getMainLooper())
    private var loopCount = 0
    private var inOglAnim = false
    private var inOglAnimPaused = false
    private var delay = 0
    private var locationManager: LocationManager? = null
    private var animTriggerDownloads = false
    private lateinit var objectImageMap: ObjectImageMap
    private var landScape = false
    private var settingsVisited = false
    private var nexradArguments = NexradArgumentsMultipane()
    private lateinit var nexradState: NexradStatePane
    private lateinit var nexradSubmenu: NexradSubmenu
    private lateinit var nexradLongPressMenu: NexradLongPressMenu
    private lateinit var nexradUI: NexradUI

    override fun onCreate(savedInstanceState: Bundle?) {
        nexradArguments.processArguments(intent.getStringArrayExtra(RID))
        landScape = UtilityUI.isLandScape(this)
        var heightDivider = 2
        //
        // determine correct layout to use
        //
        heightDivider = setupLayout(savedInstanceState, nexradArguments.numberOfPanes, heightDivider)
        val widthDivider = if (nexradArguments.numberOfPanes == 4) {
            2
        } else if (nexradArguments.numberOfPanes == 2 && landScape) {
            2
        } else {
            1
        }
        setupWindowOptions()
        nexradState = NexradStatePane(this,
                nexradArguments.numberOfPanes,
                listOf(R.id.rl1, R.id.rl2, R.id.rl3, R.id.rl4),
                widthDivider,
                heightDivider)
        nexradState.setupMultiPaneObjects(nexradArguments)
        nexradSubmenu = NexradSubmenu(objectToolbarBottom, nexradState)
        nexradLongPressMenu = NexradLongPressMenu(this, nexradState, nexradArguments, ::longPressRadarSiteSwitch)
        nexradUI = NexradUI(this, nexradState)
        nexradState.initGlView(nexradLongPressMenu.changeListener)
        objectImageMap = ObjectImageMap(this, R.id.map, toolbar, toolbarBottom, nexradState.wxglSurfaceViews)
        objectImageMap.connect(::mapSwitch, UtilityImageMap::mapToRid)
        nexradState.readPreferencesMultipane(this, nexradArguments)
        getContentParallel()
    }

    private fun setupLayout(savedInstanceState: Bundle?, numberOfPanes: Int, heightDividerF: Int): Int {
        var heightDivider = heightDividerF
        val layoutType: Int
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
        return heightDivider
    }

    private fun setupWindowOptions() {
        objectToolbarBottom.connect(this)
        toolbar.setOnClickListener { Route.severeDash(this) }
        UtilityUI.immersiveMode(this)
        UtilityFileManagement.deleteCacheFiles(this)
        UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        toolbar.setTitleTextColor(Color.WHITE)
        delay = UtilityImg.animInterval(this)
    }

    private fun checkForAutoRefresh() {
        if (RadarPreferences.wxoglRadarAutoRefresh || RadarPreferences.locationDotFollowsGps) {
            interval = 60000 * Utility.readPrefInt(this, "RADAR_REFRESH_INTERVAL", 3)
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
            startRepeatingTask()
        }
    }

    override fun onRestart() {
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        nexradSubmenu.setAnimateToPlay()
        if (objectImageMap.visibility == View.GONE) {
            nexradState.wxglTextObjects.forEach {
                it.initializeLabels(this)
                it.addLabels()
            }
        }
        getContentParallel()
        super.onRestart()
    }

    private fun getContent(z: Int) {
        getContentPrep(z)
        initGeom(z)
        FutureVoid(this, {
            NexradDraw.plotRadar(nexradState.wxglRenders[z], "", ::getGpsFromDouble, nexradState::getLatLon, false)
        }) {
            nexradState.showViews()
            nexradState.draw(z)
            nexradUI.setSubTitleMultiPane()
            UtilityRadarUI.updateLastRadarTime(this)
            if (RadarPreferences.wxoglCenterOnLocation) {
                nexradState.wxglSurfaceViews[z].resetView()
            }
        }
        NexradLayerDownload.download(
                this,
                nexradState.numberOfPanes,
                nexradState.wxglRenders[z],
                nexradState.wxglSurfaceViews[z],
                nexradState.wxglTextObjects,
                nexradUI::setTitleWithWarningCountsMultiPane)
    }

    // TODO FIXME move to NexradUI
    private fun getContentPrep(z: Int) {
        nexradState.adjustForTdwrMultiPane(z)
        toolbar.subtitle = ""
        nexradUI.setToolbarTitle()
        nexradSubmenu.adjustTiltAndProductMenus()
        invalidateOptionsMenu()
    }

    private fun initGeom(z: Int) {
        NexradDraw.initGeom(z,
                nexradState.oldRadarSites,
                nexradState.wxglRenders,
                nexradState.wxglTextObjects,
                objectImageMap,
                nexradState.wxglSurfaceViews,
                ::getGpsFromDouble,
                nexradState::getLatLon,
                false,
                settingsVisited
        )
        settingsVisited = false
    }

    private fun getAnimate(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        nexradState.showViews()
        inOglAnim = true
        withContext(Dispatchers.IO) {
            var timeMilli: Long
            var priorTime: Long
            var animArray = nexradUI.animateDownloadFilesMultiPane(frameCount)
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    animArray = nexradUI.animateDownloadFilesMultiPane(frameCount)
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
                        nexradState.wxglRenders.forEachIndexed { z, wxglRender ->
                            wxglRender.constructPolygons((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString(), "", false)
                        }
                    } else {
                        nexradState.wxglRenders.forEachIndexed { z, wxglRender ->
                            wxglRender.constructPolygons((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString(), "", true)
                        }
                    }
                    launch(uiDispatcher) { nexradUI.progressUpdateMultiPane((r + 1).toString(), (animArray[0].size).toString()) }
                    nexradState.drawAll()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        UtilityUI.immersiveMode(this)
    }

    private fun stopAnimationAndGetContent() {
        inOglAnim = false
        inOglAnimPaused = false
        // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
        // otherwise the new selection might overwrite in the OGLR object - hack
        if (nexradState.wxglRenders[0].product.contains("L2") || nexradState.wxglRenders[1].product.contains("L2")) {
            SystemClock.sleep(2000)
        }
        nexradSubmenu.setAnimateToPlay()
        getContentParallel()
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
            R.id.action_radar_markers -> Route.image(this, arrayOf("raw:radar_legend", "Radar Markers"))
            R.id.action_radar_site_status_l3 -> Route.webView(this, arrayOf("http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended"))
            R.id.action_radar_site_status_l2 -> Route.webView(this, arrayOf("http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended"))
            R.id.action_radar1 -> switchRadar(0)
            R.id.action_radar2 -> switchRadar(1)
            R.id.action_radar3 -> switchRadar(2)
            R.id.action_radar4 -> switchRadar(3)
            R.id.action_n0q, R.id.action_n0q_menu -> nexradState.getReflectivity(::getContentIntelligent)
            R.id.action_n0u, R.id.action_n0u_menu -> nexradState.getVelocity(::getContentIntelligent)
            R.id.action_n0b -> changeProduct("N" + nexradState.tilt + "B")
            R.id.action_n0g -> changeProduct("N" + nexradState.tilt + "G")
            R.id.action_n0s -> changeProduct("N" + nexradState.tilt + "S")
            R.id.action_net -> changeProduct("EET")
            R.id.action_N0X -> changeProduct("N" + nexradState.tilt + "X")
            R.id.action_N0C -> changeProduct("N" + nexradState.tilt + "C")
            R.id.action_N0K -> changeProduct("N" + nexradState.tilt + "K")
            R.id.action_H0C -> changeProduct("H" + nexradState.tilt + "C")
            R.id.action_about -> nexradUI.showRadarScanInfo()
            R.id.action_vil -> changeProduct("DVL")
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
            R.id.action_fav -> pauseButtonTapped()
            R.id.action_radar_4 -> startFourPaneNexrad()
            R.id.action_TDWR -> showTdwrDialog()
            R.id.action_ridmap -> objectImageMap.showMap(nexradState.numberOfPanes, nexradState.wxglTextObjects)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startFourPaneNexrad() {
        nexradState.writePreferencesMultipane(this, nexradArguments.doNotSavePref)
        Route.radarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", "4", "true"))
    }

    private fun startScreenRecord() {
        if (UIPreferences.recordScreenShare) {
            // TODO FIXME
//            showDistanceTool = "true"
            checkOverlayPerms()
        } else {
            UtilityRadarUI.getImageForShare(this, nexradState.render, "1")
        }
    }

    private fun pauseButtonTapped() {
        if (inOglAnim) {
            inOglAnimPaused = if (!inOglAnimPaused) {
                nexradSubmenu.setAnimateToResume()
                true
            } else {
                nexradSubmenu.setAnimateToPause()
                false
            }
        }
    }

    private fun animateRadar(frameCount: Int) {
        nexradSubmenu.setAnimateToStop()
        nexradSubmenu.setAnimateToPause()
        getAnimate(frameCount)
    }

    private fun changeTilt(tiltStr: String) {
        nexradState.changeTilt(tiltStr)
        getContent(nexradState.curRadar)
    }

    private fun changeProduct(product: String) {
        nexradState.product = product
        getContentIntelligent()
    }

    // user can select which radar # they want to control from the submenu
    private fun switchRadar(radarNumber: Int) {
        nexradState.curRadar = radarNumber
        nexradSubmenu.adjustTiltAndProductMenus()
        nexradUI.setToolbarTitle()
    }

    private fun mapSwitch(newRadarSite: String) {
        objectImageMap.hideMap()
        if (RadarPreferences.dualpaneshareposn) {
            nexradState.adjustAllPanesTo(newRadarSite)
        } else {
            nexradState.adjustOnePaneTo(nexradState.curRadar, newRadarSite)
        }
        getContentParallel()
    }

    override fun onStop() {
        super.onStop()
        nexradState.writePreferencesMultipaneOnStop(this, nexradArguments.doNotSavePref)
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        stopRepeatingTask()
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
                    getContentParallel()
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

    override fun onPause() {
        stopRepeatingTask()
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
            makeUseOfNewLocation(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun makeUseOfNewLocation(location: Location) {
        nexradState.latD = location.latitude
        nexradState.lonD = location.longitude
        getGpsFromDouble()
        nexradState.updateLocationDotsMultipane()
    }

    private fun getGpsFromDouble() {
        nexradArguments.locXCurrent = nexradState.latD
        nexradArguments.locYCurrent = nexradState.lonD
    }

    private fun longPressRadarSiteSwitch(s: String) {
        val newRadarSite = s.split(" ")[0]
        if (RadarPreferences.dualpaneshareposn) {
            nexradState.setAllPanesTo(newRadarSite)
            mapSwitch(nexradState.radarSite)
        } else {
            nexradState.radarSite = newRadarSite
            mapSwitch(nexradState.radarSite)
        }
    }

    // TODO FIXME move to NexradUI
    private fun showTdwrDialog() {
        val objectDialogue = ObjectDialogue(this, GlobalArrays.tdwrRadars)
        objectDialogue.connectCancel { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        objectDialogue.connect { dialog, itemIndex ->
            val s = GlobalArrays.tdwrRadars[itemIndex]
            nexradState.radarSite = s.split(" ")[0]
            // TODO FIXME single pane does not have this
//            if (nexradState.product.matches(Regex("N[0-3]Q"))) {
//                nexradState.product = "TZL"
//            } else {
//                nexradState.product = "TV0"
//            }
            mapSwitch(nexradState.radarSite)
            dialog.dismiss()
        }
        objectDialogue.show()
    }

    @Synchronized private fun getContentParallel() {
        nexradState.wxglRenders.indices.forEach {
            getContent(it)
        }
    }

    private fun getContentIntelligent() {
        if (RadarPreferences.dualpaneshareposn) {
            getContentParallel()
        } else {
            getContent(nexradState.curRadar)
        }
    }

    private fun toggleAnimate() {
        if (inOglAnim) {
            stopAnimationAndGetContent()
        } else {
            animateRadar(RadarPreferences.uiAnimIconFrames.toIntOrNull() ?: 0)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) objectImageMap.showMap(nexradState.numberOfPanes, nexradState.wxglTextObjects)
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) toggleAnimate()
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) nexradState.getReflectivity(::getContentIntelligent)
            KeyEvent.KEYCODE_V -> if (event.isCtrlPressed) nexradState.getVelocity(::getContentIntelligent)
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showRadarShortCuts())
            KeyEvent.KEYCODE_REFRESH -> getContentIntelligent()
            KeyEvent.KEYCODE_DPAD_UP -> if (event.isCtrlPressed) {
                    //wxglSurfaceViews.forEach{ it.zoomOutByKey(numberOfPanes.toFloat()) }
                } else {
                    nexradState.wxglSurfaceViews.forEach {
                        it.onScrollByKeyboard(0.0f, -20.0f)
                    }
                }
            KeyEvent.KEYCODE_DPAD_DOWN -> if (event.isCtrlPressed) {
                    //wxglSurfaceViews.forEach{ it.zoomInByKey(numberOfPanes.toFloat()) }
                } else {
                    nexradState.wxglSurfaceViews.forEach {
                        it.onScrollByKeyboard(0.0f, 20.0f)
                    }
                }
            KeyEvent.KEYCODE_DPAD_LEFT -> nexradState.wxglSurfaceViews.forEach { it.onScrollByKeyboard(-20.0f, 0.0f) }
            KeyEvent.KEYCODE_DPAD_RIGHT -> nexradState.wxglSurfaceViews.forEach { it.onScrollByKeyboard(20.0f, 0.0f) }
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}
