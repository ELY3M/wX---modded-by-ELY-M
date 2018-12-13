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
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.os.Handler
import android.view.Window
import android.util.Log

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.AdhocForecastActivity
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.activitiesmisc.WebscreenABModels
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.telecine.TelecineService
import joshuatee.wx.util.ImageMap
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.settings.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences

import joshuatee.wx.TDWR_RIDS
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType

import joshuatee.wx.radar.SpotterNetworkPositionReport.SendPosition

import kotlinx.coroutines.*

class WXGLRadarActivity : VideoRecordActivity(), OnItemSelectedListener, OnMenuItemClickListener {

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
        var RID: String = ""
        var dspLegendMax: Float = 0.0f
        var velMax: Short = 120
        var velMin: Short = -120
        var spotterId: String = ""
        var spotterShowSelected: Boolean = false
        var bearingCurrent = 0.0f
        var speedCurrent = 0.0f
    }

    private var TAG = "joshuatee-WXGLRadarActivity"
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var oglr: WXGLRender
    private var oldProd = ""
    private var firstRun = true
    private var oldRid = ""
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
    private var oglrArr = mutableListOf<WXGLRender>()
    private var glviewArr = mutableListOf<WXGLSurfaceView>()
    private var restarted = false
    private var tiltOption = true
    private lateinit var glview: WXGLSurfaceView
    private var tilt = "0"
    private var ridArrLoc = listOf<String>()
    private lateinit var imageMap: ObjectImageMap
    private var mapShown = false
    private lateinit var star: MenuItem
    private lateinit var anim: MenuItem
    private lateinit var tiltMenu: MenuItem
    private lateinit var l3Menu: MenuItem
    private lateinit var l2Menu: MenuItem
    private var delay = 0
    private val prefTokenLocation = "RID_LOC_"
    private val prefToken = "RID_FAV"
    private var frameCntStrGlobal = ""
    private var locXCurrent = ""
    private var locYCurrent = ""
    private var urlStr = ""
    private var fixedSite = false
    private lateinit var rl: RelativeLayout
    private val latlonArr = mutableListOf("", "")
    private var latD = 0.0
    private var lonD = 0.0
    private var locationManager: LocationManager? = null
    private var animTriggerDownloads = false
    private val alertDialogStatusAl = mutableListOf<String>()
    private lateinit var contextg: Context
    private var legendShown = false
    private val numPanes = 1
    private var numPanesArr = listOf<Int>()
    private var wxgltextArr = mutableListOf<WXGLTextObject>()
    private lateinit var act: Activity
    private lateinit var sp: ObjectSpinner
    private var diaStatus: ObjectDialogue? = null

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_uswxogl, R.menu.uswxoglradar, true, true)
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityUI.immersiveMode(this as Activity)


        if (UIPreferences.radarStatusBarTransparent && Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }

        act = this
        spotterShowSelected = false
        locXCurrent = joshuatee.wx.settings.Location.x
        locYCurrent = joshuatee.wx.settings.Location.y
        val activityArguments = intent.getStringArrayExtra(RID)
        numPanesArr = (0 until numPanes).toList()
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
            if (activityArguments.size > 3)
                fixedSite = true
            if (activityArguments.size < 7)
                archiveMode = false
        }
        contextg = this
        alertDialogStatus()
        UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        if (archiveMode && !spotterShowSelected) toolbarBottom.visibility = View.GONE
        val latLonArrD = UtilityLocation.getGPS(this)
        latD = latLonArrD[0]
        lonD = latLonArrD[1]
        val menu = toolbarBottom.menu
        star = menu.findItem(R.id.action_fav)
        anim = menu.findItem(R.id.action_a)
        tiltMenu = menu.findItem(R.id.action_tilt)
        l3Menu = menu.findItem(R.id.action_l3)
        l2Menu = menu.findItem(R.id.action_l2)
        if (!UIPreferences.radarImmersiveMode) {
            val blank = menu.findItem(R.id.action_blank)
            blank.isVisible = false
            menu.findItem(R.id.action_level3_blank).isVisible = false
            menu.findItem(R.id.action_level2_blank).isVisible = false
            menu.findItem(R.id.action_animate_blank).isVisible = false
            menu.findItem(R.id.action_tilt_blank).isVisible = false
            menu.findItem(R.id.action_tools_blank).isVisible = false
        }
        if (android.os.Build.VERSION.SDK_INT > 20)
            menu.findItem(R.id.action_jellybean_drawtools).isVisible = false
        else
            menu.findItem(R.id.action_share).title = "Share"
        delay = UtilityImg.animInterval(this)
        img = findViewById(R.id.iv)
        img.setMaxZoom(6.0f)
        glview = WXGLSurfaceView(this, 1, numPanes)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf(img, glview))
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                ridMapSwitch(UtilityImageMap.maptoRid(id))
            }

            override fun onBubbleClicked(id: Int) {}
        })
        rl = findViewById(R.id.rl)
        rl.addView(glview)
        val rlArr = arrayOf(rl)
        initGLVIEW()
        oglr.product = "N0Q"
        oglInView = true
        if (activityArguments == null)
            oglr.rid = joshuatee.wx.settings.Location.rid
        else
            oglr.rid = activityArguments[0]
        // hack, in rare cases a user will save a location that doesn't pick up RID
        if (oglr.rid == "") oglr.rid = "TLX"
        //state = turl[1]
        if (activityArguments != null && activityArguments.size > 2) {
            oglr.product = activityArguments[2]
            if (oglr.product == "N0R") {
                oglr.product = "N0Q"
            }
        }
        numPanesArr.forEach {
            wxgltextArr.add(WXGLTextObject(this, rlArr[it], glviewArr[it], oglrArr[it], numPanes))
            glviewArr[it].wxgltextArr = wxgltextArr
            wxgltextArr[it].initTV(this)
        }
        if (MyApplication.wxoglRememberLocation && !archiveMode && !fixedSite) {
            glview.scaleFactor = MyApplication.wxoglZoom
            if (MyApplication.wxoglRid != "") oglr.rid = MyApplication.wxoglRid
            oglr.product = MyApplication.wxoglProd
            oglr.setViewInitial(MyApplication.wxoglZoom, MyApplication.wxoglX, MyApplication.wxoglY)
        }
        if (MyApplication.radarShowLegend) showLegend()
        title = oglr.product
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.ridFav, oglr.rid, prefTokenLocation, prefToken)
        sp = ObjectSpinner(this, this, R.id.spinner1, ridArrLoc)
        sp.setOnItemSelectedListener(this)
        if (MyApplication.wxoglRadarAutorefresh) {
            // 180000 is 3 min
            mInterval = 60000 * Utility.readPref(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000.toLong(), 30.toFloat(), locationListener)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mHandler = Handler()
            startRepeatingTask()
        }
	
        if (MyApplication.sn_locationreport) {
            Log.i(TAG, "starting location report")
            sn_Handler_m = Handler()
            start_sn_reporting()
        }

        if (MyApplication.radarConusRadar) {
            conus_Handler_m = Handler()
            start_conusimage()
        }



    }

    override fun onRestart() {
        delay = UtilityImg.animInterval(this)
        inOglAnim = false
        inOglAnimPaused = false
        if (MyApplication.ridFav.contains(":$oglr.rid:"))
            star.setIcon(MyApplication.STAR_ICON)
        else
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        anim.setIcon(MyApplication.ICON_PLAY)
        restarted = true
        restartedZoom = true
        numPanesArr.forEach {
            wxgltextArr[it].initTV(this)
            wxgltextArr[it].addTV()
        }
        // if the top toolbar is not showing then neither are showing and the only restart
        // is from an app switch or resume from sleep, therefore get content directly
        if (glview.toolbarsHidden) {
            getContent()
        } else {
            ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.ridFav, oglr.rid, prefTokenLocation, prefToken)
            sp.refreshData(contextg, ridArrLoc)
        }
        if (MyApplication.wxoglRadarAutorefresh) {
            mInterval = 60000 * Utility.readPref(this, "RADAR_REFRESH_INTERVAL", 3)
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000.toLong(), 30.toFloat(), locationListener)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mHandler = Handler()
            startRepeatingTask()
        }
	
        if (MyApplication.sn_locationreport) {
            Log.i(TAG, "starting location report")
            sn_Handler_m = Handler()
            start_sn_reporting()
        }

        if (MyApplication.radarConusRadar) {
            conus_Handler_m = Handler()
            start_conusimage()
        }
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {

        //@SuppressLint("StaticFieldLeak")
        //private inner class GetContent : AsyncTask<String, String, String>() {
        //override fun onPreExecute() {
        val ridIsTdwr = WXGLNexrad.isRIDTDWR(oglr.rid)
        if (ridIsTdwr) {
            l3Menu.isVisible = false
            l2Menu.isVisible = false
        } else {
            l3Menu.isVisible = true
            l2Menu.isVisible = true
        }
        if ((oglr.product == "N0Q" || oglr.product == "N1Q" || oglr.product == "N2Q" || oglr.product == "N3Q" || oglr.product == "L2REF") && ridIsTdwr) oglr.product = "TZL"
        if (oglr.product == "TZL" && !ridIsTdwr) oglr.product = "N0Q"
        if ((oglr.product == "N0U" || oglr.product == "N1U" || oglr.product == "N2U" || oglr.product == "N3U" || oglr.product == "L2VEL") && ridIsTdwr) oglr.product = "TV0"
        if (oglr.product == "TV0" && !ridIsTdwr) oglr.product = "N0U"
        //prod = WXGLNexrad.checkTdwrProd(prod, ridIsTdwr)
        title = oglr.product
        if (MyApplication.ridFav.contains(":" + oglr.rid + ":"))
            star.setIcon(MyApplication.STAR_ICON)
        else
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        toolbar.subtitle = ""
        if (!oglr.product.startsWith("2")) initWXOGLGeom()
        //}

        //override fun doInBackground(vararg params: String): String {
        withContext(Dispatchers.IO) {
            oglr.constructPolygons("", urlStr, true)
            if ((PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) && !archiveMode)
                oglr.constructSpotters()
            else
                oglr.deconstructSpotters()
            if (PolygonType.STI.pref && !archiveMode)
                oglr.constructSTILines()
            else
                oglr.deconstructSTILines()
            if (PolygonType.HI.pref && !archiveMode)
                oglr.constructHI()
            else
                oglr.deconstructHI()
            if (PolygonType.TVS.pref && !archiveMode)
                oglr.constructTVS()
            else
                oglr.deconstructTVS()
            if (MyApplication.locdotFollowsGps && !archiveMode) {
                getGPSFromDouble()
                locXCurrent = latlonArr[0]
                locYCurrent = latlonArr[1]
            }
            if (PolygonType.LOCDOT.pref || archiveMode || MyApplication.locdotFollowsGps) {
                oglr.constructLocationDot(locXCurrent, locYCurrent, archiveMode)
            } else {
                oglr.deconstructLocationDot()
            }
            if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) {
                UtilityMetar.getStateMetarArrayForWXOGL(contextg, oglr.rid)
            }
            if (PolygonType.WIND_BARB.pref && !archiveMode) {
                oglr.constructWBLines()
            } else {
                oglr.deconstructWBLines()
            }
            if (PolygonType.SWO.pref && !archiveMode) {
                UtilitySWOD1.getSWO()
                oglr.constructSWOLines()
            } else {
                oglr.deconstructSWOLines()
            }
            //return "Executed"
        }

        //override fun onPostExecute(result: String) {
        if (!oglInView) {
            img.visibility = View.GONE
            glview.visibility = View.VISIBLE
            oglInView = true
        }
        if (ridChanged && !restartedZoom) {
            ridChanged = false
        }
        if (restartedZoom) {
            restartedZoom = false
            ridChanged = false
        }
        if (PolygonType.SPOTTER_LABELS.pref && !archiveMode) {
            UtilityWXGLTextObject.updateSpotterLabels(numPanes, wxgltextArr)
        }
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) {
            UtilityWXGLTextObject.updateObs(numPanes, wxgltextArr)
        }
        glview.requestRender()
        if (legendShown && oglr.product != oldProd && oglr.product != "DSA" && oglr.product != "DAA") {
            updateLegend()
        }
        if (legendShown && (oglr.product == "DSA" || oglr.product == "DAA" || oglr.product == "N0U")) {
            dspLegendMax = (255.0f / oglr.radarL3Object.halfword3132) * 0.01f
            velMax = oglr.radarL3Object.halfword48
            velMin = oglr.radarL3Object.halfword47
            updateLegend()
        }
        oldProd = oglr.product
        val info = Utility.readPref(contextg, "WX_RADAR_CURRENT_INFO", "")
        //val tmpArr = MyApplication.space.split(info)
        val tmpArr = info.split(" ")
        if (tmpArr.size > 3)
            toolbar.subtitle = tmpArr[3]
        else
            toolbar.subtitle = ""
        animRan = false
        firstRun = false
        //}
    }

    private fun getAnimate(frameCntStr: String) = GlobalScope.launch(uiDispatcher) {
        if (!oglInView) {
            img.visibility = View.GONE
            glview.visibility = View.VISIBLE
            oglInView = true
        }
        inOglAnim = true
        animRan = true

        withContext(Dispatchers.IO) {
            //val frameCntStr = params[0]
            frameCntStrGlobal = frameCntStr
            var animArray = oglr.rdDownload.getRadarByFTPAnimation(contextg, frameCntStr)
            var fh: File
            var timeMilli: Long
            var priorTime: Long
            try {
                animArray.indices.forEach {
                    fh = File(contextg.filesDir, animArray[it])
                    contextg.deleteFile("nexrad_anim" + it.toString())
                    if (!fh.renameTo(File(contextg.filesDir, "nexrad_anim" + it.toString())))
                        UtilityLog.d("wx", "Problem moving to " + "nexrad_anim" + it.toString())
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            var loopCnt = 0
            while (inOglAnim) {
                if (animTriggerDownloads) {
                    animArray = oglr.rdDownload.getRadarByFTPAnimation(contextg, frameCntStr)
                    try {
                        animArray.indices.forEach {
                            fh = File(contextg.filesDir, animArray[it])
                            contextg.deleteFile("nexrad_anim" + it.toString())
                            if (!fh.renameTo(File(contextg.filesDir, "nexrad_anim" + it.toString())))
                                UtilityLog.d("wx", "Problem moving to " + "nexrad_anim" + it.toString())
                        }
                    } catch (e: Exception) {
                        UtilityLog.HandleException(e)
                    }
                    animTriggerDownloads = false
                }
                for (r in 0 until animArray.size) {
                    while (inOglAnimPaused) SystemClock.sleep(delay.toLong())
                    // formerly priorTime was set at the end but that is goofed up with pause
                    priorTime = System.currentTimeMillis()
                    // added because if paused and then another icon life vel/ref it won't load correctly, likely timing issue
                    if (!inOglAnim) break
                    // if the first pass has completed, for L2 no longer uncompress, use the existing decomp files
                    if (loopCnt > 0)
                        oglr.constructPolygons("nexrad_anim" + r.toString(), urlStr, false)
                    else
                        oglr.constructPolygons("nexrad_anim" + r.toString(), urlStr, true)
                    //publishProgress((r + 1).toString(), animArray.size.toString())
                    launch(uiDispatcher) {
                        progressUpdate((r + 1).toString(), animArray.size.toString())
                    }
                    glview.requestRender()
                    timeMilli = System.currentTimeMillis()
                    if ((timeMilli - priorTime) < delay)
                        SystemClock.sleep(delay - ((timeMilli - priorTime)))
                    if (!inOglAnim) break
                    if (r == (animArray.size - 1)) SystemClock.sleep(delay.toLong() * 2)
                }
                loopCnt += 1
            }
        }
    }

    private fun progressUpdate(vararg values: String) {
        if ((values[1].toIntOrNull() ?: 0) > 1) {
            val tmpArrAnim = Utility.readPref(contextg, "WX_RADAR_CURRENT_INFO", "").split(" ")
            if (tmpArrAnim.size > 3)
                toolbar.subtitle = tmpArrAnim[3] + " (" + values[0] + "/" + values[1] + ")"
            else
                toolbar.subtitle = ""
        } else {
            toolbar.subtitle = "Problem downloading"
        }
    }

    /* @SuppressLint("StaticFieldLeak")
     private inner class AnimateRadarD : AsyncTask<String, String, String>() {

         override fun onPreExecute() {
             if (!oglInView) {
                 img.visibility = View.GONE
                 glview.visibility = View.VISIBLE
                 oglInView = true
             }
             inOglAnim = true
             animRan = true
         }

         override fun doInBackground(vararg params: String): String {
             val frameCntStr = params[0]
             frameCntStrGlobal = frameCntStr
             var animArray = oglr.rdDownload.getRadarByFTPAnimation(contextg, frameCntStr)
             var fh: File
             var timeMilli: Long
             var priorTime: Long
             try {
                 animArray.indices.forEach {
                     fh = File(contextg.filesDir, animArray[it])
                     contextg.deleteFile("nexrad_anim" + it.toString())
                     if (!fh.renameTo(File(contextg.filesDir, "nexrad_anim" + it.toString())))
                         UtilityLog.d("wx", "Problem moving to " + "nexrad_anim" + it.toString())
                 }
             } catch (e: Exception) {
                 UtilityLog.HandleException(e)
             }
             var loopCnt = 0
             while (inOglAnim) {
                 if (animTriggerDownloads) {
                     animArray = oglr.rdDownload.getRadarByFTPAnimation(contextg, frameCntStr)
                     try {
                         animArray.indices.forEach {
                             fh = File(contextg.filesDir, animArray[it])
                             contextg.deleteFile("nexrad_anim" + it.toString())
                             if (!fh.renameTo(File(contextg.filesDir, "nexrad_anim" + it.toString())))
                                 UtilityLog.d("wx", "Problem moving to " + "nexrad_anim" + it.toString())
                         }
                     } catch (e: Exception) {
                         UtilityLog.HandleException(e)
                     }
                     animTriggerDownloads = false
                 }
                 for (r in 0 until animArray.size) {
                     while (inOglAnimPaused) SystemClock.sleep(delay.toLong())
                     // formerly priorTime was set at the end but that is goofed up with pause
                     priorTime = System.currentTimeMillis()
                     // added because if paused and then another icon life vel/ref it won't load correctly, likely timing issue
                     if (!inOglAnim) break
                     // if the first pass has completed, for L2 no longer uncompress, use the existing decomp files
                     if (loopCnt > 0)
                         oglr.constructPolygons("nexrad_anim" + r.toString(), urlStr, false)
                     else
                         oglr.constructPolygons("nexrad_anim" + r.toString(), urlStr, true)
                     publishProgress((r + 1).toString(), animArray.size.toString())
                     glview.requestRender()
                     timeMilli = System.currentTimeMillis()
                     if ((timeMilli - priorTime) < delay)
                         SystemClock.sleep(delay - ((timeMilli - priorTime)))
                     if (!inOglAnim) break
                     if (r == (animArray.size - 1)) SystemClock.sleep(delay.toLong() * 2)
                 }
                 loopCnt += 1
             }
             return "Executed"
         }

         override fun onProgressUpdate(vararg values: String) {
             if ((values[1].toIntOrNull() ?: 0) > 1) {
                 val tmpArrAnim = Utility.readPref(contextg, "WX_RADAR_CURRENT_INFO", "").split(" ")
                 if (tmpArrAnim.size > 3)
                     toolbar.subtitle = tmpArrAnim[3] + " (" + values[0] + "/" + values[1] + ")"
                 else
                     toolbar.subtitle = ""
             } else {
                 toolbar.subtitle = "Problem downloading"
             }
         }
     }*/

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        UtilityUI.immersiveMode(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        UtilityUI.immersiveMode(this as Activity)
        if (inOglAnim && (item.itemId != R.id.action_fav) && (item.itemId != R.id.action_share) && (item.itemId != R.id.action_tools)) {
            inOglAnim = false
            inOglAnimPaused = false
            // if an L2 anim is in process sleep for 1 second to let the current decode/render finish
            // otherwise the new selection might overwrite in the OGLR object - hack
            // (revert) 2016_08 have this apply to Level 3 in addition to Level 2
            if (oglr.product.contains("L2")) SystemClock.sleep(2000)
            if (MyApplication.ridFav.contains(":$oglr.rid:"))
                star.setIcon(MyApplication.STAR_ICON)
            else
                star.setIcon(MyApplication.STAR_OUTLINE_ICON)
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
            R.id.action_jellybean_drawtools -> {
                val tI = TelecineService.newIntent(this, 1, Intent())
                tI.putExtra("show_distance_tool", showDistanceTool)
                tI.putExtra("show_recording_tools", "false")
                startService(tI)
            }
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20) {
                    showDistanceTool = "true"
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan) {
                        val animDrawable = UtilityUSImgWX.animationFromFiles(this, oglr.rid, oglr.product, frameCntStrGlobal, "", true)
                        UtilityShare.shareAnimGif(this, oglr.rid + " (" + Utility.readPref(contextg, "RID_LOC_" + oglr.rid, "") + ") " + oglr.product, animDrawable)
                    } else {
                        UtilityShare.shareBitmap(this, oglr.rid + " (" + Utility.readPref(contextg, "RID_LOC_" + oglr.rid, "") + ") " + oglr.product, UtilityUSImgWX.layeredImgFromFile(applicationContext, oglr.rid, oglr.product, "0", true))
                    }
                }
            }
            R.id.action_settings -> startActivity(Intent(this, SettingsRadarActivity::class.java))
            R.id.action_radar_markers -> ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf("raw:radar_legend", "Radar Markers", "false"))
            R.id.action_radar_site_status_l3 -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf("http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3)))
            R.id.action_radar_site_status_l2 -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf("http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2)))
            R.id.action_n0q -> {
                if (MyApplication.radarIconsLevel2 && oglr.product.matches("N[0-3]Q".toRegex())) {
                    oglr.product = "L2REF"
                    tiltOption = false
                } else {
                    if (!WXGLNexrad.isRIDTDWR(oglr.rid)) {
                        oglr.product = "N" + tilt + "Q"
                        tiltOption = true
                    } else {
                        oglr.product = "TZL"
                        tiltOption = false
                    }
                }
                getContent()
            }
            R.id.action_n0u -> {
                if (MyApplication.radarIconsLevel2 && oglr.product.matches("N[0-3]U".toRegex())) {
                    oglr.product = "L2VEL"
                    tiltOption = false
                } else {
                    if (!WXGLNexrad.isRIDTDWR(oglr.rid)) {
                        oglr.product = "N" + tilt + "U"
                        tiltOption = true
                    } else {
                        oglr.product = "TV$tilt"
                        tiltOption = true
                    }
                }
                getContent()
            }
            R.id.action_n0s -> changeProd("N" + tilt + "S", true)
            R.id.action_net -> changeProd("EET", false)
            R.id.action_N0X -> changeProd("N" + tilt + "X", true)
            R.id.action_N0C -> changeProd("N" + tilt + "C", true)
            R.id.action_N0K -> changeProd("N" + tilt + "K", true)
            R.id.action_H0C -> changeProd("H" + tilt + "C", true)
            R.id.action_legend -> showLegend()
            R.id.action_about -> showRadarScanInfo()
            R.id.action_vil -> changeProd("DVL", false)
            R.id.action_dsp -> changeProd("DSA", false)
            R.id.action_daa -> changeProd("DAA", false)
            R.id.action_nsw -> changeProd("NSW", false)
            R.id.action_l2vel -> changeProd("L2VEL", false)
            R.id.action_l2ref -> changeProd("L2REF", false)
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
            R.id.action_NVW -> getContentVWP()
            R.id.action_fav -> {
                if (inOglAnim) {
                    inOglAnimPaused = if (!inOglAnimPaused) {
                        star.setIcon(MyApplication.ICON_PLAY)
                        true
                    } else {
                        star.setIcon(MyApplication.ICON_PAUSE)
                        false
                    }
                } else
                    toggleFavorite()
            }
            R.id.action_TDWR -> alertDialogTDWR()
            R.id.action_ridmap -> {
                imageMap.toggleMap()
                if (imageMap.map.visibility != View.GONE) {
                    UtilityWXGLTextObject.hideTV(numPanes, wxgltextArr)
                } else {
                    UtilityWXGLTextObject.showTV(numPanes, wxgltextArr)
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun animateRadar(frameCnt: String) {
        anim.setIcon(MyApplication.ICON_STOP)
        star.setIcon(MyApplication.ICON_PAUSE)
        //AnimateRadar().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameCnt)
        getAnimate(frameCnt)
    }

    private fun changeProd(prodF: String, canTilt: Boolean) {
        oglr.product = prodF
        tiltOption = canTilt
        getContent()
    }

    private fun changeTilt(tiltStr: String) {
        tilt = tiltStr
        oglr.product = oglr.product.replace("N[0-3]".toRegex(), "N$tilt")
        getContent()
    }

    fun ridMapSwitch(r: String) {
        oglr.rid = r
        mapShown = false
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.ridFav, oglr.rid, prefTokenLocation, prefToken)
        sp.refreshData(contextg, ridArrLoc)
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleFavoriteString(this, oglr.rid, star, prefToken)
        ridArrLoc = UtilityFavorites.setupFavMenu(this, ridFav, oglr.rid, prefTokenLocation, prefToken)
        sp.refreshData(contextg, ridArrLoc)
    }

    private fun showRadarScanInfo() {
        val info = Utility.readPref(contextg, "WX_RADAR_CURRENT_INFO", "")
        UtilityAlertDialog.showHelpText(info, this as Activity)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (ridArrLoc.size > 2) {
            inOglAnim = false
            inOglAnimPaused = false
            anim.setIcon(MyApplication.ICON_PLAY)
            when (pos) {
                1 -> ObjectIntent(this, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("RID"))
                2 -> ObjectIntent(this, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("RID"))
                else -> {
                    if (ridArrLoc[pos] == " ") {
                        oglr.rid = joshuatee.wx.settings.Location.rid
                    } else {
                        oglr.rid = ridArrLoc[pos].split(" ").getOrNull(0) ?: ""
                    }
                    tiltMenu.isVisible = tiltOption
                    if (!restarted && !(MyApplication.wxoglRememberLocation && firstRun)) {
                        img.resetZoom()
                        img.setZoom(1.0f)
                        glview.scaleFactor = MyApplication.wxoglSize / 10.0f
                        oglr.setViewInitial(MyApplication.wxoglSize / 10.0f, 0.0f, 0.0f)
                    }
                    restarted = false
                    ridChanged = true
                    getContent()
                }
            }
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
        }
        UtilityUI.immersiveMode(this as Activity)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onStop() {
        super.onStop()
        if (!archiveMode && !fixedSite) {
            WXGLNexrad.savePrefs(this, "WXOGL", oglr)
        }
        // otherwise cpu will spin with no fix but to kill app
        inOglAnim = false
        mHandler?.let { stopRepeatingTask() }
        sn_Handler_m?.let { stop_sn_reporting() }
        conus_Handler_m?.let { stop_conusimage() }
        locationManager?.let {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                it.removeUpdates(locationListener)
        }
    }

    private val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {

        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                alertDialogStatusAl.clear()
                val locX = locXCurrent.toDoubleOrNull() ?: 0.0
                val locY = locYCurrent.toDoubleOrNull() ?: 0.0
                val pointX = glview.newY.toDouble()
                val pointY = glview.newX * -1.0
                val dist = LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
                val ridX = (Utility.readPref(contextg, "RID_" + oglr.rid + "_X", "0.0")).toDouble()
                val ridY = -1.0 * (Utility.readPref(contextg, "RID_" + oglr.rid + "_Y", "0.0")).toDouble()
                val distRid = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)
                diaStatus!!.setTitle(UtilityStringExternal.truncate(glview.newY.toString(), 6)
                        + ",-" + UtilityStringExternal.truncate(glview.newX.toString(), 6))
                alertDialogStatusAl.add(UtilityStringExternal.truncate(dist.toString(), 6) + " miles from location")
                alertDialogStatusAl.add(UtilityStringExternal.truncate(distRid.toString(), 6) + " miles from " + oglr.rid)
                oglr.ridNewList.mapTo(alertDialogStatusAl) { "Radar: (" + it.distance + " mi) " + it.name + " " + Utility.readPref(contextg, "RID_LOC_" + it.name, "") }
                alertDialogStatusAl.add("Show warning text")
                if (MyApplication.radarWatMcd) {
                    alertDialogStatusAl.add("Show Watch text")
                    alertDialogStatusAl.add("Show MCD text")
                }
                if (MyApplication.radarMpd) {
                    alertDialogStatusAl.add("Show MPD text")
                }
                alertDialogStatusAl.add("Show nearest observation")
                alertDialogStatusAl.add("Show nearest forecast")
                alertDialogStatusAl.add("Show nearest meteogram")
                if (MyApplication.radarSpotters || MyApplication.radarSpottersLabel) {
                    alertDialogStatusAl.add("Show Spotter Info")
                }
                alertDialogStatusAl.add("Show radar status message")
                diaStatus!!.show()
            } else {
                numPanesArr.forEach {
                    wxgltextArr[it].addTV()
                }
            }
        }
    }

    private fun initGLVIEW() {
        glview.setEGLContextClientVersion(2)
        oglr = WXGLRender(this as Activity)
        oglrArr.clear()
        oglrArr.add(oglr)
        glviewArr.clear()
        glviewArr.add(glview)
        //glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0) // a test to see if android emulator will now work
        glview.setRenderer(oglr)
        glview.setRenderVar(oglr, oglrArr, glviewArr, act)
        glview.fullScreen = true
        glview.setOnProgressChangeListener(changeListener)
        glview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glview.toolbar = toolbar
        glview.toolbarBottom = toolbarBottom
        glview.archiveMode = archiveMode
    }

    private fun initWXOGLGeom() {
        oglr.initGEOM()
        if (oldRid != oglr.rid) {
            oglr.setChunkCount(0)
            oglr.setChunkCountSti(0)
            oglr.setHiInit(false)
            oglr.setTvsInit(false)
            Thread(Runnable {
                oglr.constructStateLines()
                glview.requestRender()
            }).start()
            Thread(Runnable {
                if (GeographyType.LAKES.pref)
                    oglr.constructLakes()
                else
                    oglr.deconstructLakes()
            }).start()
            Thread(Runnable {
                if (GeographyType.COUNTY_LINES.pref) {
                    oglr.constructCounty()
                    glview.requestRender()
                } else
                    oglr.deconstructCounty()
            }).start()
            Thread(Runnable {
                if (GeographyType.HIGHWAYS.pref) {
                    oglr.constructHWLines()
                    glview.requestRender()
                } else
                    oglr.deconstructHWLines()
            }).start()
            Thread(Runnable {
                if (MyApplication.radarHwEnhExt) {
                    oglr.constructHWEXTLines()
                    glview.requestRender()
                } else
                    oglr.deconstructHWEXTLines()
            }).start()
            //wxgltextArr[0].setRid(rid1)
            wxgltextArr[0].addTV()
            oldRid = oglr.rid
        }

        Thread(Runnable {
            if (MyApplication.radarConusRadar && !archiveMode)
                oglr.constructConusRadar()
            else
                oglr.deconstructConusRadar()
        }).start()

        Thread(Runnable {
            if (PolygonType.TOR.pref && !archiveMode)
                oglr.constructTorWarningLines()
            else
                oglr.deconstructTorWarningLines()

            if (PolygonType.SVR.pref && !archiveMode)
                oglr.constructSvrWarningLines()
            else
                oglr.deconstructSvrWarningLines()

            if (PolygonType.EWW.pref && !archiveMode)
                oglr.constructEwwWarningLines()
            else
                oglr.deconstructEwwWarningLines()

            if (PolygonType.FFW.pref && !archiveMode)
                oglr.constructFfwWarningLines()
            else
                oglr.deconstructFfwWarningLines()

            if (PolygonType.SMW.pref && !archiveMode)
                oglr.constructSmwWarningLines()
            else
                oglr.deconstructSmwWarningLines()

            if (PolygonType.SVS.pref && !archiveMode)
                oglr.constructSvsWarningLines()
            else
                oglr.deconstructSvsWarningLines()

            if (PolygonType.SPS.pref && !archiveMode)
                oglr.constructSpsWarningLines()
            else
                oglr.deconstructSpsWarningLines()

            if (PolygonType.MCD.pref && !archiveMode)
                oglr.constructWATMCDLines()
            else
                oglr.deconstructWATMCDLines()

            if (PolygonType.MPD.pref && !archiveMode)
                oglr.constructMPDLines()
            else
                oglr.deconstructMPDLines()

            glview.requestRender()
        }).start()
        if (MyApplication.locdotFollowsGps && !archiveMode) {
            getGPSFromDouble()
            locXCurrent = latlonArr[0]
            locYCurrent = latlonArr[1]
        }
        if (PolygonType.LOCDOT.pref || MyApplication.locdotFollowsGps) // added locdot gps apr 2016
            oglr.constructLocationDot(locXCurrent, locYCurrent, archiveMode)
        else
            oglr.deconstructLocationDot()
        img.visibility = View.GONE
        glview.visibility = View.VISIBLE
    }

    private val handler = Handler()

    private val mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            if (loopCount > 0) {
                if (inOglAnim)
                    animTriggerDownloads = true
                else
                    getContent()
            }
            loopCount += 1
            handler.postDelayed(this, mInterval.toLong())
        }
    }

    private fun startRepeatingTask() {
        mStatusChecker.run()
    }

    private fun stopRepeatingTask() {
        mHandler!!.removeCallbacks(mStatusChecker)
    }

    //report your spotter network location
    private val sn_handler = Handler()
    private val sn_reporter: Runnable = object : Runnable {
        override fun run() {

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


    //conus radar
    private val conus_handler = Handler()
    private val conus_image: Runnable = object : Runnable {
        override fun run() {
            Log.i(TAG, "downloading new conus image")
            UtilityConusRadar.getConusImage()
            conus_handler.postDelayed(this, conus_Interval.toLong())
        }
    }

    private fun start_conusimage() {
        conus_image.run()
    }

    private fun stop_conusimage() {
        conus_handler!!.removeCallbacks(conus_image)
    }

    override fun onPause() {
        glview.onPause()
        super.onPause()
    }

    override fun onResume() {
        glview.onResume()
        super.onResume()
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (MyApplication.locdotFollowsGps && !archiveMode) makeUseOfNewLocation(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    private fun makeUseOfNewLocation(location: Location) {
        latD = location.latitude
        lonD = location.longitude
        bearingCurrent = location.bearing
        speedCurrent = location.speed
        Log.i(TAG, "bearing: "+bearingCurrent)
        Log.i(TAG, "speed: "+speedCurrent)
        Log.i(TAG, "speed in mph: "+(speedCurrent * 3.6 * 0.62137119))
        getGPSFromDouble()
        locXCurrent = latlonArr[0]
        locYCurrent = latlonArr[1]
        oglr.constructLocationDot(locXCurrent, locYCurrent, archiveMode)
        glview.requestRender()
    }

    private fun getGPSFromDouble() {
        latlonArr[0] = latD.toString()
        latlonArr[1] = lonD.toString()
    }

    private fun alertDialogStatus() {
        diaStatus = ObjectDialogue(contextg, alertDialogStatusAl)
        diaStatus!!.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(act)
        })
        diaStatus!!.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            val strName = alertDialogStatusAl[which]
            if (strName.contains("Radar:")) {
                val ridNew = strName.parse("\\) ([A-Z]{3,4}) ")
                oglr.rid = ridNew
                ridChanged = true
                ridMapSwitch(oglr.rid)
            } else if (strName.contains("Show warning text")) {
                val polygonUrl = UtilityWXOGL.showTextProducts(glview.newY.toDouble(), glview.newX.toDouble() * -1.0)
                if (polygonUrl != "") ObjectIntent(this, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, arrayOf(polygonUrl, ""))
            } else if (strName.contains("Show Watch text")) {
                getWatch()
            } else if (strName.contains("Show MCD text")) {
                getMCD()
            } else if (strName.contains("Show MPD text")) {
                getMPD()
            } else if (strName.contains("Show nearest observation")) {
                getMetar()
            } else if (strName.contains("Show nearest meteogram")) {
                // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
                // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW

                val obsSite = UtilityMetar.findClosestObservation(contextg, LatLon(glview.newY.toDouble(), glview.newX * -1.0))
                ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram"))
            } else if (strName.contains("Show Spotter Info")) {
                getSpotterInfo()
            } else if (strName.contains("Show radar status message")) {
                getRadarStatus()
            } else if (strName.contains("Show nearest forecast")) {
                ObjectIntent(this, AdhocForecastActivity::class.java, AdhocForecastActivity.URL, arrayOf(glview.newY.toString(), "-" + glview.newX.toString()))
            }
            dialog.dismiss()
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
            oglr.rid = strName.split(" ").getOrNull(0) ?: ""
            oglr.product = "TZL"
            ridMapSwitch(oglr.rid)
            title = oglr.product
            getContent()
            dialog.dismiss()
        })
        diaTdwr.show()
    }

    private fun getRadarStatus() = GlobalScope.launch(uiDispatcher) {
        var radarStatus = withContext(Dispatchers.IO) { UtilityDownload.getRadarStatusMessage(contextg, oglr.rid) }
        if (radarStatus == "") { radarStatus = "The current radar status is not available." }
        UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)
    }

    private var legend: ViewColorLegend? = null

    private fun showLegend() {
        if (!legendShown) {
            if (oglr.product == "DSA" || oglr.product == "DAA")
                dspLegendMax = (255.0f / oglr.radarL3Object.halfword3132) * 0.01f
            velMax = oglr.radarL3Object.halfword48
            velMin = oglr.radarL3Object.halfword47
            legendShown = true
            val rLParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
            legend = ViewColorLegend(this as Activity, oglr.product)
            rl.addView(legend, rLParams)
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
        val rLParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT) // change FILL_PARENT
        rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
        legend = ViewColorLegend(this as Activity, oglr.product)
        rl.addView(legend, rLParams)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (Utility.readPref(contextg, "LAUNCH_TO_RADAR", "false") == "false")
                    NavUtils.navigateUpFromSameTask(this)
                else
                    navigateUp()
                //true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // thanks http://stackoverflow.com/questions/19999619/navutils-navigateupto-does-not-start-any-activity user882209
    private fun navigateUp() {
        val upIntent = NavUtils.getParentActivityIntent(this)
        if (NavUtils.shouldUpRecreateTask(this, upIntent!!) || isTaskRoot) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities()
        } else {
            NavUtils.navigateUpTo(this, upIntent)
        }
    }

    private fun getMetar() = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) { UtilityMetar.findClosestMetar(contextg, LatLon(glview.newY.toDouble(), glview.newX.toDouble() * -1.0)) }
        UtilityAlertDialog.showHelpText(txt, act)
    }
    private fun getContentVWP() = GlobalScope.launch(uiDispatcher) {
        //val txt = withContext(Dispatchers.IO) { UtilityWXOGL.getVWP(contextg, oglr.rid) }
        //ObjectIntent(contextg, TextScreenActivity::class.java, TextScreenActivity.URL, arrayOf(txt, oglr.rid + " VAD Wind Profile"))
        var vmpurl = "https://weather.cod.edu/satrad/nexrad/index.php?type="+oglr.rid+"-NVW"
        ObjectIntent(contextg, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf(vmpurl, oglr.rid + " VAD Wind Profile"))

    }
    
    private fun getSpotterInfo() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) { UtilitySpotter.findClosestSpotter(LatLon(glview.newY.toDouble(), glview.newX.toDouble() * -1.0)) }
        UtilityAlertDialog.showHelpText(txt, act)
    }

    private fun getWatch() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) {  UtilityWat.showWatchProducts(contextg, glview.newY.toDouble(), glview.newX.toDouble() * -1.0) }
        if (txt != "") {
            UtilityAlertDialog.showHelpText(txt, act)
        }
    }

    private fun getMCD() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) {  UtilityWat.showMCDProducts(contextg, glview.newY.toDouble(), glview.newX.toDouble() * -1.0) }
        if (txt != "") {
            UtilityAlertDialog.showHelpText(txt, act)
        }
    }
    private fun getMPD() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) {  UtilityWat.showMPDProducts(contextg, glview.newY.toDouble(), glview.newX.toDouble() * -1.0) }
        if (txt != "") {
            UtilityAlertDialog.showHelpText(txt, act)
        }
    }


}
