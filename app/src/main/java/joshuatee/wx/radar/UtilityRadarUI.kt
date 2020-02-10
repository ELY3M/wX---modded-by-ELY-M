/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.ForecastActivity
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap

import joshuatee.wx.util.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import joshuatee.wx.Extensions.*

internal object UtilityRadarUI {

    const val longPressRadarSiteRegex = "\\) ([A-Z]{3,4}) "
    private const val lastRadarTimePref = "NEXRADDOWNLOAD_TIME_LAST_RAN"

    fun updateLastRadarTime(context: Context) {
        Utility.writePref(
            context,
            lastRadarTimePref,
            UtilityTime.getCurrentLocalTimeAsString()
        )
    }

    fun getLastRadarTime(context: Context): String {
        return Utility.readPref(context, lastRadarTimePref, "")
    }

    private fun getRadarStatus(
            act: Activity,
            context: Context,
            uiDispatcher: CoroutineDispatcher,
            wxglRender: WXGLRender
    ) = GlobalScope.launch(uiDispatcher) {
        var radarStatus = withContext(Dispatchers.IO) {
            UtilityDownload.getRadarStatusMessage(
                    context,
                    wxglRender.rid
            )
        }
        if (radarStatus == "") {
            radarStatus = "The current radar status for " + wxglRender.rid + " is not available."
        }
        UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)
    }

    private fun getMetar(
            wxglSurfaceView: WXGLSurfaceView,
            activity: Activity,
            context: Context,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityMetar.findClosestMetar(context, wxglSurfaceView.latLon)
        }
        UtilityAlertDialog.showHelpText(txt, activity)
    }

    private fun showNearestForecast(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        ObjectIntent(
                context,
                ForecastActivity::class.java,
                ForecastActivity.URL,
                arrayOf(wxglSurfaceView.newY.toString(), "-" + wxglSurfaceView.newX.toString())
        )
    }

    private fun showNearestMeteogram(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
        // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW
        val obsSite = UtilityMetar.findClosestObservation(
                context,
                wxglSurfaceView.latLon
        )
        ObjectIntent(
                context,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram")
        )
    }

    private fun showNearestWarning(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        val polygonUrl = UtilityWXOGL.showTextProducts(
                wxglSurfaceView.newY.toDouble(),
                wxglSurfaceView.newX.toDouble() * -1.0
        )
        if (polygonUrl != "") ObjectIntent(
                context,
                USAlertsDetailActivity::class.java,
                USAlertsDetailActivity.URL,
                arrayOf(polygonUrl, "")
        )
    }

    fun addItemsToLongPress(
            alertDialogRadarLongpressAl: MutableList<String>,
            lat: String,
            lon: String,
            context: Context,
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            alertDialogRadarLongPress: ObjectDialogue
    ) {
        alertDialogRadarLongpressAl.clear()
        val locX = lat.toDoubleOrNull() ?: 0.0
        val locY = lon.toDoubleOrNull() ?: 0.0
        val pointX = wxglSurfaceView.newY.toDouble()
        val pointY = wxglSurfaceView.newX * -1.0
        val dist = LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val ridX = Utility.getRadarSiteX(wxglRender.rid).toDoubleOrNull() ?: 0.0
        val ridY = -1.0 * (Utility.getRadarSiteY(wxglRender.rid).toDoubleOrNull() ?: 0.0)
        val distRid = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val distRidKm = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.KM)
        // FIXME look at iOS version and try to match in data provided and improve formatting
        val latLonTitle = UtilityStringExternal.truncate(wxglSurfaceView.newY.toString(), 6) +
                ", -" +
                UtilityStringExternal.truncate(wxglSurfaceView.newX.toString(), 6)
        alertDialogRadarLongPress.setTitle(latLonTitle)
        alertDialogRadarLongpressAl.add(
                UtilityStringExternal.truncate(
                        dist.toString(),
                        6
                ) + " miles from location"
        )
        alertDialogRadarLongpressAl.add(
                UtilityStringExternal.truncate(
                        distRid.toString(),
                        6
                ) + " miles from " + wxglRender.rid
        )
/*

	//Ely's mod 
        //get msl and AGL here
        val radarheight = WXGLNexradLevel3.radarHeight
        val distance = UtilityStringExternal.truncate(distRidKm.toString(), 4).toDouble()
        //val distancekm = distance * 1.60934
        //var degree = WXGLNexradLevel3.radarElevation
        //var degree = WXGLNexradLevel3.getproductSpecific / 10.0f
        val degree = WXGLNexradLevel3.getElevation



        UtilityLog.d("wx", "radarHeight: "+radarheight)
        UtilityLog.d("wx", "degree: "+degree)


        val heightAGL = 3.281 * (Math.sin(Math.toRadians(degree.toDouble())) * distance + distance * distance / 15417.82) * 1000.0
        val heightMSL = (radarheight + heightAGL)

        alertDialogRadarLongpressAl.add("Beam Height MSL: "+UtilityStringExternal.truncate(heightMSL.toString(), 6)+" ft, AGL: "+UtilityStringExternal.truncate(heightAGL.toString(), 6)+" ft")

*/

	//joshua's MSL+AGL Beam Height//
        val heightAgl = UtilityMath.getRadarBeamHeight(wxglRender.radarL3Object.degree, distRidKm)
        val heightMsl = (wxglRender.radarL3Object.radarHeight + heightAgl)
        alertDialogRadarLongpressAl.add("Beam Height MSL: " + heightMsl.roundToInt().toString() + " ft, AGL: " + heightAgl.roundToInt().toString() + " ft")
        if (MyApplication.radarShowWpcFronts) {
            var wpcFrontsTimeStamp = Utility.readPref(context,"WPC_FRONTS_TIMESTAMP", "")
            wpcFrontsTimeStamp = wpcFrontsTimeStamp.replace(UtilityTime.getYear().toString(), "")
            wpcFrontsTimeStamp = wpcFrontsTimeStamp.insert(4, " ")
            alertDialogRadarLongpressAl.add(MyApplication.newline + "WPC Fronts: " + wpcFrontsTimeStamp)
        }
        wxglRender.ridNewList.mapTo(alertDialogRadarLongpressAl) {
            "Radar: (" + it.distance + " mi) " + it.name + " " + Utility.getRadarSiteName(it.name)
        }
        val obsSite = UtilityMetar.findClosestObservation(context, wxglSurfaceView.latLon)
        if (MyApplication.radarWarnings) {
            alertDialogRadarLongpressAl.add("Show Warning text")
        }
        if (MyApplication.radarWatMcd) {
            alertDialogRadarLongpressAl.add("Show Watch text")
            alertDialogRadarLongpressAl.add("Show MCD text")
        }
        if (MyApplication.radarMpd) {
            alertDialogRadarLongpressAl.add("Show MPD text")
        }
        alertDialogRadarLongpressAl.add("Show nearest observation: " + obsSite.name)
        alertDialogRadarLongpressAl.add("Show nearest forecast: $latLonTitle")
        alertDialogRadarLongpressAl.add("Show nearest meteogram: " + obsSite.name)
        if (MyApplication.radarSpotters || MyApplication.radarSpottersLabel) {
            alertDialogRadarLongpressAl.add("Show Spotter Info")
        }
        alertDialogRadarLongpressAl.add("Show current radar status message: " + wxglRender.rid)
	val getridpoint: String = UtilityLocation.getNearestRadarSite(LatLon(pointX.toString(), pointY.toString()))
	alertDialogRadarLongpressAl.add("Show nearest radar status message: " + getridpoint)
	alertDialogRadarLongpressAl.add("userpoint info: " + latLonTitle)
        alertDialogRadarLongpressAl.add("Add userpoint for: " + latLonTitle)
        alertDialogRadarLongpressAl.add("Delete userpoint for: " + latLonTitle)
        alertDialogRadarLongpressAl.add("Delete all userpoints")
        alertDialogRadarLongPress.show()
    }

    fun doLongPressAction(
            strName: String,
            context: Context,
            activity: Activity,
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            uiDispatcher: CoroutineDispatcher,
            function: (strName: String) -> Unit
    ) {
        when {
            strName.contains("Show Warning text") -> {
                showNearestWarning(context, wxglSurfaceView)
            }
            strName.contains("Show Watch text") -> {
                showNearestProduct(context, PolygonType.WATCH, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("Show MCD text") -> {
                showNearestProduct(context, PolygonType.MCD, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("Show MPD text") -> {
                showNearestProduct(context, PolygonType.MPD, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("Show nearest observation") -> {
                getMetar(wxglSurfaceView, activity, context, uiDispatcher)
            }
            strName.contains("Show nearest meteogram") -> {
                showNearestMeteogram(context, wxglSurfaceView)
            }
            strName.contains("Show radar status message") -> {
                getRadarStatus(activity, context, uiDispatcher, wxglRender)
            }
            strName.contains("Show nearest radar status message") -> {
                showNearestRadarStatus(context, activity, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("Show nearest forecast") -> {
                showNearestForecast(context, wxglSurfaceView)
            }
            strName.contains("Show Spotter Info") -> {
                showSpotterInfo(activity, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("userpoint info") -> {
                showUserPointInfo(activity, context, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("Add userpoint for") -> {
                addUserPoint(activity, context, wxglRender, wxglSurfaceView)
            }
            strName.contains("Delete userpoint for") -> {
                deleteUserPoint(activity, context, wxglRender, wxglSurfaceView, uiDispatcher)
            }
            strName.contains("Delete all userpoints") -> {
                deleteAllUserPoints(activity, context, uiDispatcher)
            }
            else -> function(strName)
        }
    }

    fun initGlviewFragment(
            wxglSurfaceView: WXGLSurfaceView,
            z: Int,
            oglrs: MutableList<WXGLRender>,
            glviews: MutableList<WXGLSurfaceView>,
            wxgltexts: MutableList<WXGLTextObject>,
            changeListener: WXGLSurfaceView.OnProgressChangeListener
    ): Boolean {
        wxglSurfaceView.setEGLContextClientVersion(2)
        wxgltexts[z].setWXGLRender(oglrs[z])
        oglrs[z].indexString = z.toString()
        wxglSurfaceView.setRenderer(oglrs[z])
        wxglSurfaceView.setRenderVar(oglrs[z], oglrs, glviews)
        wxglSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        wxglSurfaceView.setOnProgressChangeListener(changeListener)
        oglrs[z].zoom = MyApplication.wxoglSize.toFloat() / 10.0f
        wxglSurfaceView.scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        return true
    }

    fun initGlView(
            wxglSurfaceView: WXGLSurfaceView,
            glviewArr: MutableList<WXGLSurfaceView>,
            wxglRender: WXGLRender,
            oglrArr: MutableList<WXGLRender>,
            act: Activity,
            toolbar: Toolbar,
            toolbarBottom: Toolbar,
            changeListener: WXGLSurfaceView.OnProgressChangeListener,
            archiveMode: Boolean = false
    ) {
        wxglSurfaceView.setEGLContextClientVersion(2)
        wxglSurfaceView.setRenderer(wxglRender)
        wxglSurfaceView.setRenderVar(wxglRender, oglrArr, glviewArr, act)
        wxglSurfaceView.fullScreen = true
        wxglSurfaceView.setOnProgressChangeListener(changeListener)
        wxglSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        wxglSurfaceView.toolbar = toolbar
        wxglSurfaceView.toolbarBottom = toolbarBottom
        wxglSurfaceView.archiveMode = archiveMode
    }

    fun initWxOglGeom(
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            z: Int,
            oldRidArr: Array<String>,
            oglrArr: MutableList<WXGLRender>,
            wxgltextArr: MutableList<WXGLTextObject>,
            numPanesArr: List<Int>,
            imageMap: ObjectImageMap?,
            glviewArr: MutableList<WXGLSurfaceView>,
            fnGps: () -> Unit,
            fnGetLatLon: () -> LatLon,
            archiveMode: Boolean = false
    ) {
        wxglRender.initializeGeometry()
        if (oldRidArr[z] != oglrArr[z].rid) {
            wxglRender.setChunkCount(0)
            wxglRender.setChunkCountSti(0)
            wxglRender.setHiInit(false)
            wxglRender.setTvsInit(false)
            Thread(Runnable {
                wxglRender.constructStateLines()
                wxglSurfaceView.requestRender()
            }).start()
            Thread(Runnable {
                if (GeographyType.LAKES.pref)
                    wxglRender.constructLakes()
                else
                    wxglRender.deconstructLakes()
            }).start()
            Thread(Runnable {
                if (GeographyType.COUNTY_LINES.pref) {
                    wxglRender.constructCounty()
                    wxglSurfaceView.requestRender()
                } else
                    wxglRender.deconstructCounty()
            }).start()
            Thread(Runnable {
                if (GeographyType.HIGHWAYS.pref) {
                    wxglRender.constructHWLines()
                    wxglSurfaceView.requestRender()
                } else
                    wxglRender.deconstructHWLines()
            }).start()
            Thread(Runnable {
                if (MyApplication.radarHwEnhExt) {
                    wxglRender.constructHWEXTLines()
                    wxglSurfaceView.requestRender()
                } else
                    wxglRender.deconstructHWEXTLines()
            }).start()
            wxgltextArr[z].addTextLabels()
            oldRidArr[z] = oglrArr[z].rid
        }

        //conus radar
        Thread(Runnable {
            if (MyApplication.radarConusRadar && !archiveMode) {
                wxglRender.constructConusRadar()
                wxglSurfaceView.requestRender()
            } else {
                wxglRender.deconstructConusRadar()
            }
        }).start()

        Thread(Runnable {
            if (PolygonType.TST.pref && !archiveMode)
                wxglRender.constructWarningLines()
            else
                wxglRender.deconstructWarningLines()
            if (PolygonType.MCD.pref && !archiveMode)
                wxglRender.constructWatchMcdLines()
            else
                wxglRender.deconstructWatchMcdLines()
            if (PolygonType.MPD.pref && !archiveMode)
                wxglRender.constructMpdLines()
            else
                wxglRender.deconstructMpdLines()
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }).start()
        if (MyApplication.locationDotFollowsGps) {
            fnGps()
        }
        if (PolygonType.LOCDOT.pref || MyApplication.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRender.constructLocationDot(latLon.latString, latLon.lonString, archiveMode)
        } else {
            wxglRender.deconstructLocationDot()
        }

        if (PolygonType.USERPOINTS.pref && !archiveMode)
            wxglRender.constructUserPoints()
        else
            wxglRender.deconstructUserPoints()

        if (imageMap != null && imageMap.map.visibility != View.VISIBLE) {
            numPanesArr.forEach { glviewArr[it].visibility = View.VISIBLE }
        }
    }

    /*fun plotPolygons(
            glv: WXGLSurfaceView,
            ogl: WXGLRender,
            archiveMode: Boolean = false
    ) {
        Thread(Runnable {
            if (PolygonType.TST.pref && !archiveMode)
                ogl.constructWarningLines()
            else
                ogl.deconstructWarningLines()
            if (PolygonType.MCD.pref && !archiveMode)
                ogl.constructWatchMcdLines()
            else
                ogl.deconstructWatchMcdLines()
            if (PolygonType.MPD.pref && !archiveMode)
                ogl.constructMpdLines()
            else
                ogl.deconstructMpdLines()
            ogl.constructGenericWarningLines()
            glv.requestRender()
        }).start()
    }*/

    fun plotWarningPolygons(
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            archiveMode: Boolean = false
    ) {
        Thread(Runnable {
            if (PolygonType.TST.pref && !archiveMode) {
                wxglRender.constructWarningLines()
            } else {
                wxglRender.deconstructWarningLines()
            }
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }).start()
    }

    fun plotMcdWatchPolygons(
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            archiveMode: Boolean = false
    ) {
        Thread(Runnable {
            if (PolygonType.MCD.pref && !archiveMode) {
                wxglRender.constructWatchMcdLines()
            } else {
                wxglRender.deconstructWatchMcdLines()
            }
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }).start()
    }

    fun plotMpdPolygons(
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            archiveMode: Boolean = false
    ) {
        Thread(Runnable {
            if (PolygonType.MPD.pref && !archiveMode) {
                wxglRender.constructMpdLines()
            } else {
                wxglRender.deconstructMpdLines()
            }
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }).start()
    }

    fun plotWpcFronts(
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            archiveMode: Boolean = false
    ) {
        Thread(Runnable {
            if (MyApplication.radarShowWpcFronts && !archiveMode) {
                wxglRender.constructWpcFronts()
            } else {
                wxglRender.deconstructWpcFronts()
            }
            wxglSurfaceView.requestRender()
        }).start()
    }

    fun plotRadar(
            wxglRender: WXGLRender,
            urlString: String,
            context: Context,
            fnGps: () -> Unit,
            fnGetLatLon: () -> LatLon,
            //TODO why?!?!? showExtras is only in this file....
            //TODO not needed
            //showExtras: Boolean,
            archiveMode: Boolean = false
    ) {
        wxglRender.constructPolygons("", urlString, true)
        // work-around for a bug in which raster doesn't show on first launch
        if (wxglRender.product == "NCR" || wxglRender.product == "NCZ") {
            wxglRender.constructPolygons("", urlString, true)
        }
        if ((PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) && !archiveMode)
            wxglRender.constructSpotters()
        else
            wxglRender.deconstructSpotters()
        if (PolygonType.STI.pref && !archiveMode)
            wxglRender.constructStiLines()
        else
            wxglRender.deconstructStiLines()
        if (PolygonType.HI.pref && !archiveMode)
            wxglRender.constructHi()
        else
            wxglRender.deconstructHi()
        if (PolygonType.TVS.pref && !archiveMode)
            wxglRender.constructTvs()
        else
            wxglRender.deconstructTvs()
        if (MyApplication.locationDotFollowsGps && !archiveMode) {
            fnGps()
        }
        if (PolygonType.LOCDOT.pref || archiveMode || MyApplication.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRender.constructLocationDot(latLon.latString, latLon.lonString, archiveMode)
        } else {
            wxglRender.deconstructLocationDot()
        }
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) {
            UtilityMetar.getStateMetarArrayForWXOGL(context, wxglRender.rid, wxglRender.paneNumber)
        }
        if (PolygonType.WIND_BARB.pref && !archiveMode) {
            wxglRender.constructWBLines()
        } else {
            wxglRender.deconstructWBLines()
        }
        //if (showExtras) {
            /*if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) {
                UtilityMetar.getStateMetarArrayForWXOGL(context, wxglRender.rid, wxglRender.paneNumber)
            }
            if (PolygonType.WIND_BARB.pref && !archiveMode) {
                wxglRender.constructWBLines()
            } else {
                wxglRender.deconstructWBLines()
            }*/
            if (PolygonType.SWO.pref && !archiveMode) {
                UtilitySwoD1.get(context)
                wxglRender.constructSwoLines()
            } else {
                wxglRender.deconstructSwoLines()
            }
        //}
    }

    fun resetGlview(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender) {
        wxglSurfaceView.scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        wxglRender.setViewInitial(MyApplication.wxoglSize.toFloat() / 10.0f, 0.0f, 0.0f)
        wxglSurfaceView.requestRender()
    }

    private fun showNearestProduct(
            context: Context,
            polygonType: PolygonType,
            wxglSurfaceView: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val text = withContext(Dispatchers.IO) {
            UtilityWatch.show(wxglSurfaceView.newY.toDouble(), wxglSurfaceView.newX.toDouble() * -1.0, polygonType)
        }
        if (text != "") {
            ObjectIntent(
                    context,
                    SpcMcdWatchShowActivity::class.java,
                    SpcMcdWatchShowActivity.NO,
                    arrayOf(text, "", polygonType.toString())
            )
        }
    }
            var getrid: String = ""
    private fun showNearestRadarStatus(
            context: Context,
            act: Activity,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        var radarStatus = withContext(Dispatchers.IO) {
            val pointX = glview.newY.toDouble()
            val pointY = glview.newX * -1.0
            getrid = UtilityLocation.getNearestRadarSite(LatLon(pointX.toString(), pointY.toString())
            )
            UtilityLog.d("wx", "point: x: "+pointX+ "y: "+pointY)
            UtilityLog.d("wx", "radar status on point: "+getrid)
            UtilityDownload.getRadarStatusMessage(context, getrid)
        }
        UtilityLog.d("wx", "UtilityRadarUI radarStatus: "+radarStatus)
        if (radarStatus == "") {
            radarStatus = "The current radar status for " + getrid + " is not available."
        }
        UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)

    }
    

    private fun showSpotterInfo(
            act: Activity,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilitySpotter.findClosestSpotter(glview.latLon)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }


    //UserPoints System
    private fun showUserPointInfo(
            act: Activity,
            context: Context,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityUserPoints.findClosestUserPoint(context, glview.latLon)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }

    private fun addUserPoint(act: Activity, context: Context, oglr: WXGLRender, glview: WXGLSurfaceView) {
        UtilityUserPoints.addUserPoint(context, oglr, glview, glview.latLon)
        UtilityAlertDialog.showHelpText("added userpoint", act)
    }


    private fun deleteUserPoint(
            act: Activity,
            context: Context,
            oglr: WXGLRender,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityUserPoints.deleteUserPoint(
                    context, oglr, glview, glview.latLon)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }

    private fun deleteAllUserPoints(
            act: Activity,
            context: Context,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        UtilityUserPoints.deleteAllUserPoints(context)
        UtilityAlertDialog.showHelpText("Deleted all userpoints", act)
    }



}
