/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
//modded by ELY M.

package joshuatee.wx.radar

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import joshuatee.wx.R
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityFileManagement

class NexradUI(
        val activity: VideoRecordActivity,
        val nexradState: NexradStatePane,
        private val nexradSubmenu: NexradSubmenu,
        val nexradArguments: NexradArguments,
        val getContent: () -> Unit
) {

    //elys mod - location bug
    companion object {
        var bearingCurrent = 0.0f
        var speedCurrent = 0.0f
    }

    // auto update interval, 180 seconds by default
    var interval = 180000
    // delay between animation frames
    var delay = 0
    val objectImageMap = ObjectImageMap(activity, R.id.map, activity.objectToolbar, activity.objectToolbarBottom, nexradState.wxglSurfaceViews)
    // auto refresh and GPS location
    val handler = Handler(Looper.getMainLooper())
    private var locationManager: LocationManager? = null
    var loopCount = 0
    // animation processing
    var inOglAnim = false
    var inOglAnimPaused = false
    var animTriggerDownloads = false
    //
    var settingsVisited = false

    init {
        objectImageMap.connect(::mapSwitch, UtilityImageMap::mapToRid)
        UtilityToolbar.transparentToolbars(activity.objectToolbar, activity.objectToolbarBottom)
        activity.objectToolbar.setTextColor(Color.WHITE)
        activity.objectToolbar.connectClick { Route.severeDash(activity) }
        UtilityFileManagement.deleteCacheFiles(activity)
        delay = UtilityImg.animInterval(activity)

        if (nexradState.numberOfPanes == 1) {
            if (UIPreferences.radarStatusBarTransparent) {
//            This constant was deprecated in API level 30.
//            Use Window#setStatusBarColor(int) with a half-translucent color instead.
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            window.statusBarColor = Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= 30) {
                    activity.window.statusBarColor = Color.TRANSPARENT
                    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                } else {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
        }
    }

    fun getContentPrep() {
        nexradState.radarSitesForFavorites = UtilityFavorites.setupMenu(activity, nexradState.radarSite, FavoriteType.RID)
        activity.invalidateOptionsMenu()
        nexradState.adjustForTdwrSinglePane()
        activity.title = nexradState.product
        nexradSubmenu.adjustTiltAndProductMenus()
        nexradSubmenu.setStarButton()
        activity.toolbar.subtitle = ""
    }

    fun getContentPrepMultiPane(z: Int) {
        nexradState.adjustForTdwrMultiPane(z)
        activity.toolbar.subtitle = ""
        setToolbarTitle()
        nexradSubmenu.adjustTiltAndProductMenus()
        activity.invalidateOptionsMenu()
    }

    fun showTdwrDialog() {
        val objectDialogue = ObjectDialogue(activity, GlobalArrays.tdwrRadars)
        objectDialogue.connectCancel { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(activity)
        }
        objectDialogue.connect { dialog, itemIndex ->
            val s = GlobalArrays.tdwrRadars[itemIndex]
            nexradState.radarSite = s.split(":")[0]
            mapSwitch(nexradState.radarSite)
            dialog.dismiss()
        }
        objectDialogue.show()
    }

    fun showRadarScanInfo() {
        if (nexradState.numberOfPanes == 1) {
            ObjectDialogue(activity, NexradUtil.getRadarInfo(activity, ""))
        } else {
            val scanInfoList = nexradState.wxglRenders.indices.map { NexradUtil.getRadarInfo(activity, (it + 1).toString()) }
            ObjectDialogue(activity, scanInfoList.joinToString(GlobalVariables.newline + GlobalVariables.newline))
        }
    }

    fun setTitleWithWarningCounts() {
        if (RadarPreferences.warnings) {
            activity.title = nexradState.product + " " + Warnings.getCountString()
        }
    }

    fun setSubTitle() {
        val items = NexradUtil.getRadarInfo(activity,"").split(" ")
        if (items.size > 3) {
            activity.toolbar.subtitle = items[3]
            if (ObjectDateTime.isRadarTimeOld(items[3]))
                activity.toolbar.setSubtitleTextColor(Color.RED)
            else
                activity.toolbar.setSubtitleTextColor(Color.LTGRAY)
        } else {
            activity.toolbar.subtitle = ""
        }
    }

    //
    // multipane
    //
    fun setToolbarTitle(updateWithWarnings: Boolean = false) {
        activity.title = nexradState.setToolbarTitleMultipane(updateWithWarnings)
    }

    fun setTitleWithWarningCountsMultiPane() {
        setToolbarTitle(RadarPreferences.warnings)
    }
    //
    // multipane end
    //

    fun setSubTitleMultiPane(a: String = "", b: String = "") {
        // take each radar timestamp and split into a list
        // make sure the list is the correct size (more then 3 elements)
        // create another list consisting of the 4th item of each list (the HH:MM:SS)
        // set the subtitle to a string which is the new list joined by "/"
        val radarInfoList = nexradState.wxglRenders.indices.map { NexradUtil.getRadarInfo(activity, (it + 1).toString()) }
        val tmpArray = radarInfoList.map { it.split(" ") }
        if (tmpArray.all { it.size > 3}) {
            val tmpArray2 = tmpArray.map { it[3] }
            var s = tmpArray2.joinToString("/")
            if (a != "" && b != "") {
                s += "($a/$b)"
            }
            activity.toolbar.subtitle = s
        } else {
            activity.toolbar.subtitle = ""
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

    fun hideMap() {
        objectImageMap.hideMap()
    }

    fun showMap() {
        objectImageMap.showMap(nexradState.wxglTextObjects)
    }

    fun checkForAutoRefresh() {
        if (RadarPreferences.wxoglRadarAutoRefresh || RadarPreferences.locationDotFollowsGps) {
            interval = 60000 * Utility.readPrefInt(activity, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (gpsEnabled != null && gpsEnabled) {
                    locationManager?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            (RadarPreferences.locationUpdateInterval * 1000).toLong(),
                            NexradUtil.radarLocationUpdateDistanceInMeters,
                            locationListener
                    )
                }
            }
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            startRepeatingTask()
	        //elys mod
            //download latest conus image
            UtilityConusRadar.getConusImage()
        }
    }

    val runnable: Runnable = object : Runnable {
        override fun run() {
            if (loopCount > 0) {
                if (inOglAnim) {
                    animTriggerDownloads = true
                } else {
                    getContent()
                }
            }
            loopCount += 1
            handler.postDelayed(this, interval.toLong())
        }
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
        getGPSFromDouble()
        nexradState.updateLocationDots(nexradArguments)
    }

    fun getGPSFromDouble() {
        if (!nexradArguments.archiveMode) {
            nexradArguments.locXCurrent = nexradState.latD
            nexradArguments.locYCurrent = nexradState.lonD
        }
    }

    private fun startRepeatingTask() {
        loopCount = 0
        handler.removeCallbacks(runnable)
        runnable.run()
    }

    fun stopRepeatingTask() {
        handler.removeCallbacks(runnable)
    }

    fun onRestart() {
        delay = UtilityImg.animInterval(activity)
        inOglAnim = false
        inOglAnimPaused = false
        hideMap()
    }

    fun mapSwitch(newRadarSite: String) {
        hideMap()
        if (nexradState.numberOfPanes == 1) {
            nexradState.adjustPaneTo(newRadarSite)
        } else {
            if (RadarPreferences.dualpaneshareposn) {
                nexradState.adjustAllPanesTo(newRadarSite)
            } else {
                nexradState.adjustOnePaneTo(nexradState.curRadar, newRadarSite)
            }
        }
        getContent()
    }

    fun onStop() {
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        stopRepeatingTask()
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            )
                it.removeUpdates(locationListener)
        }
    }

    fun stopAnimationAndGetContent() {
        inOglAnim = false
        inOglAnimPaused = false
        // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
        // otherwise the new selection might overwrite in the OGLR object - hack
        if (nexradState.numberOfPanes > 1) {
            if (nexradState.wxglRenders[0].state.product.contains("L2") || nexradState.wxglRenders[1].state.product.contains("L2")) {
                SystemClock.sleep(2000)
            }
        } else {
            if (nexradState.product.contains("L2")) {
                SystemClock.sleep(2000)
            }
            nexradSubmenu.setStarButton()
        }
        nexradSubmenu.setAnimateToPlay()
        getContent()
    }

    fun pauseButtonTapped() {
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

    fun longPressRadarSiteSwitch(s: String) {
        nexradState.radarSite = s.split(" ")[0]
        stopAnimation()
        if (RadarPreferences.dualpaneshareposn && nexradState.numberOfPanes > 1) {
            nexradState.setAllPanesTo(nexradState.radarSite)
            mapSwitch(nexradState.radarSite)
        } else {
            mapSwitch(nexradState.radarSite)
        }
    }

    fun stopAnimation() {
        inOglAnim = false
        inOglAnimPaused = false
        nexradSubmenu.setAnimateToPlay()
    }

    // single pane only
    fun actionToggleFavorite() {
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

    private fun toggleFavorite() {
        UtilityFavorites.toggle(activity, nexradState.radarSite, nexradSubmenu.starButton, FavoriteType.RID)
    }
}
