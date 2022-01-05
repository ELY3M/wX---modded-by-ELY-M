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
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.util.*
import kotlin.math.roundToInt
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.*

internal object UtilityRadarUI {

    const val longPressRadarSiteRegex = "\\) ([A-Z]{3,4}) "
    private const val lastRadarTimePref = "NEXRADDOWNLOAD_TIME_LAST_RAN"

    fun updateLastRadarTime(context: Context) {
        Utility.writePref(context, lastRadarTimePref, UtilityTime.getCurrentLocalTimeAsString())
    }

    fun getLastRadarTime(context: Context) = Utility.readPref(context, lastRadarTimePref, "")

    private fun getRadarStatus(activity: Activity, context: Context, wxglRender: WXGLRender) {
        FutureText2(context,
                { UtilityDownload.getRadarStatusMessage(context, wxglRender.rid) },
                { s ->
                    var radarStatus = s
                    if (radarStatus == "") {
                        radarStatus = "The current radar status for " + wxglRender.rid + " is not available."
                    }
                    ObjectDialogue(activity, Utility.fromHtml(radarStatus))
                }
        )
    }

    private fun getMetar(wxglSurfaceView: WXGLSurfaceView, activity: Activity, context: Context) {
        FutureText2(
                context,
                { UtilityMetar.findClosestMetar(context, wxglSurfaceView.latLon) },
                { s -> ObjectDialogue(activity, s) }
        )
    }

    private fun showNearestForecast(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        ObjectIntent(context, ForecastActivity::class.java, ForecastActivity.URL, arrayOf(wxglSurfaceView.newY.toString(), "-" + wxglSurfaceView.newX.toString()))
    }

    private fun showNearestMeteogram(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
        // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW
        val obsSite = UtilityMetar.findClosestObservation(context, wxglSurfaceView.latLon)
        ObjectIntent.showImage(context, arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram"))
    }

    private fun showNearestWarning(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        val polygonUrl = UtilityWXOGL.showTextProducts(wxglSurfaceView.latLon)
        if (polygonUrl != "") ObjectIntent.showHazard(context, arrayOf(polygonUrl, ""))
    }

    fun addItemsToLongPress(longPressList: MutableList<String>, lat: String, lon: String, context: Context, wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender, longPressDialogue: ObjectDialogue) {
        longPressList.clear()
        val locX = To.double(lat)
        val locY = To.double(lon)
        val pointX = wxglSurfaceView.newY.toDouble()
        val pointY = wxglSurfaceView.newX * -1.0
        val dist = LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val ridX = To.double(Utility.getRadarSiteX(wxglRender.rid))
        val ridY = -1.0 * To.double(Utility.getRadarSiteY(wxglRender.rid))
        val distRid = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val distRidKm = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.KM)
        // TODO FIXME look at iOS version and try to match in data provided and improve formatting
        val latLonTitle = UtilityStringExternal.truncate(wxglSurfaceView.newY.toString(), 6) + ", -" + UtilityStringExternal.truncate(wxglSurfaceView.newX.toString(), 6)
        longPressDialogue.setTitle(latLonTitle)
        longPressList.add(UtilityStringExternal.truncate(dist.toString(), 6) + " miles from location")
        longPressList.add(UtilityStringExternal.truncate(distRid.toString(), 6) + " miles from " + wxglRender.rid)
        val heightAgl = UtilityMath.getRadarBeamHeight(wxglRender.wxglNexradLevel3.degree, distRidKm)
        val heightMsl = (wxglRender.wxglNexradLevel3.radarHeight + heightAgl)
        longPressList.add("Beam Height MSL: " + heightMsl.roundToInt().toString() + " ft, AGL: " + heightAgl.roundToInt().toString() + " ft")
        if (MyApplication.radarShowWpcFronts) {
            var wpcFrontsTimeStamp = Utility.readPref(context,"WPC_FRONTS_TIMESTAMP", "")
            wpcFrontsTimeStamp = wpcFrontsTimeStamp.replace(UtilityTime.getYear().toString(), "")
            if (wpcFrontsTimeStamp.length > 6) {
                wpcFrontsTimeStamp = wpcFrontsTimeStamp.insert(4, " ")
            }
            longPressList.add(MyApplication.newline + "WPC Fronts: " + wpcFrontsTimeStamp)
        }
        longPressList += wxglRender.ridNewList.map { "Radar: (" + it.distance + " mi) " + it.name + " " + Utility.getRadarSiteName(it.name) }
        val obsSite = UtilityMetar.findClosestObservation(context, wxglSurfaceView.latLon)
        // FIXME TODO what is this doing?
        ObjectPolygonWarning.isCountNonZero()
        if ((MyApplication.radarWarnings || ObjectPolygonWarning.areAnyEnabled()) && ObjectPolygonWarning.isCountNonZero()) {
            longPressList.add("Show Warning text")
        }
        // Thanks to Ely
        if (MyApplication.radarWatMcd && MyApplication.watchLatLonList.value != "") {
            longPressList.add("Show Watch text")
        }
        if (MyApplication.radarWatMcd && MyApplication.mcdLatLon.value != "") {
            longPressList.add("Show MCD text")
        }
        if (MyApplication.radarMpd && MyApplication.mpdLatLon.value != "") {
            longPressList.add("Show MPD text")
        }
        // end Thanks to Ely
        longPressList.add("Show nearest observation: " + obsSite.name + " (" + obsSite.distance.toString() + " mi)")
        longPressList.add("Show nearest forecast: $latLonTitle")
        longPressList.add("Show nearest meteogram: " + obsSite.name)
        if (MyApplication.radarSpotters || MyApplication.radarSpottersLabel) longPressList.add("Show Spotter Info")
        longPressList.add("Show current radar status message: " + wxglRender.rid)
	val getridpoint: String = UtilityLocation.getNearestRadarSite(LatLon(pointX.toString(), pointY.toString()))
	longPressList.add("Show nearest radar status message: " + getridpoint)
	longPressList.add("Userpoint info: " + latLonTitle)
        longPressList.add("Add userpoint for: " + latLonTitle)
        longPressList.add("Delete userpoint for: " + latLonTitle)
        longPressList.add("Delete all userpoints")
        longPressDialogue.show()
    }

    fun doLongPressAction(
            string: String,
            context: Context,
            activity: Activity,
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            function: (String) -> Unit
    ) {
        when {
            string.contains("miles from") -> {}
            string.contains("Show Warning text") -> showNearestWarning(context, wxglSurfaceView)
            string.contains("Show Watch text") -> showNearestProduct(context, PolygonType.WATCH, wxglSurfaceView)
            string.contains("Show MCD text") -> showNearestProduct(context, PolygonType.MCD, wxglSurfaceView)
            string.contains("Show MPD text") -> showNearestProduct(context, PolygonType.MPD, wxglSurfaceView)
            string.contains("Show nearest observation") -> getMetar(wxglSurfaceView, activity, context)
            string.contains("Show nearest meteogram") -> showNearestMeteogram(context, wxglSurfaceView)
            string.contains("Show current radar status message") -> getRadarStatus(activity, context, wxglRender)
            string.contains("Show nearest radar status message") -> showNearestRadarStatus(context, activity, wxglSurfaceView)
            string.contains("Show nearest forecast") -> showNearestForecast(context, wxglSurfaceView)
            string.contains("Show Spotter Info") -> showSpotterInfo(activity, wxglSurfaceView, context)
            string.contains("Userpoint info") -> showUserPointInfo(activity, context, wxglSurfaceView)
            string.contains("Add userpoint for") -> addUserPoint(activity, context, wxglSurfaceView)
            string.contains("Delete userpoint for") ->  deleteUserPoint(activity, context, wxglSurfaceView)
            string.contains("Delete all userpoints") -> deleteAllUserPoints(activity, context)
            else -> function(string)
        }
    }

    fun initGlviewFragment(
            wxglSurfaceView: WXGLSurfaceView,
            index: Int,
            wxglRenders: MutableList<WXGLRender>,
            wxglSurfaceViews: MutableList<WXGLSurfaceView>,
            wxglTextObjects: MutableList<WXGLTextObject>,
            changeListener: WXGLSurfaceView.OnProgressChangeListener
    ): Boolean {
        wxglSurfaceView.setEGLContextClientVersion(2)
        wxglTextObjects[index].setWXGLRender(wxglRenders[index])
        wxglRenders[index].indexString = index.toString()
        wxglSurfaceView.setRenderer(wxglRenders[index])
        wxglSurfaceView.setRenderVar(wxglRenders[index], wxglRenders, wxglSurfaceViews)
        wxglSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        wxglSurfaceView.setOnProgressChangeListener(changeListener)
        wxglRenders[index].zoom = MyApplication.wxoglSize.toFloat() / 10.0f
        wxglSurfaceView.scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        return true
    }

    fun initGlView(
            wxglSurfaceView: WXGLSurfaceView,
            wxglSurfaceViews: MutableList<WXGLSurfaceView>,
            wxglRender: WXGLRender,
            wxglRenders: MutableList<WXGLRender>,
            activity: Activity,
            toolbar: Toolbar,
            toolbarBottom: Toolbar,
            changeListener: WXGLSurfaceView.OnProgressChangeListener,
            archiveMode: Boolean = false
    ) {
        wxglSurfaceView.setEGLContextClientVersion(2)
        wxglSurfaceView.setRenderer(wxglRender)
        wxglSurfaceView.setRenderVar(wxglRender, wxglRenders, wxglSurfaceViews, activity)
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
            index: Int,
            oldRadarSites: Array<String>,
            wxglRenders: MutableList<WXGLRender>,
            wxglTextObjects: MutableList<WXGLTextObject>,
            paneList: List<Int>,
            imageMap: ObjectImageMap?,
            wxglSurfaceViews: MutableList<WXGLSurfaceView>,
            fnGps: () -> Unit,
            fnGetLatLon: () -> LatLon,
            archiveMode: Boolean = false
    ) {
        wxglRender.initializeGeometry()
        if (oldRadarSites[index] != wxglRenders[index].rid) {
            wxglRender.setChunkCount(0)
            wxglRender.setChunkCountSti(0)
            wxglRender.setHiInit(false)
            wxglRender.setTvsInit(false)
            Thread {
                wxglRender.constructStateLines()
                wxglSurfaceView.requestRender()
            }.start()
            Thread {
                if (GeographyType.LAKES.pref) wxglRender.constructLakes() else wxglRender.deconstructLakes()
            }.start()
            Thread {
                if (GeographyType.COUNTY_LINES.pref) {
                    wxglRender.constructCounty()
                    wxglSurfaceView.requestRender()
                } else
                    wxglRender.deconstructCounty()
            }.start()
            Thread {
                if (GeographyType.HIGHWAYS.pref) {
                    wxglRender.constructHWLines()
                    wxglSurfaceView.requestRender()
                } else
                    wxglRender.deconstructHWLines()
            }.start()
            Thread {
                if (MyApplication.radarHwEnhExt) {
                    wxglRender.constructHWEXTLines()
                    wxglSurfaceView.requestRender()
                } else
                    wxglRender.deconstructHWEXTLines()
            }.start()
            wxglTextObjects[index].addLabels()
            oldRadarSites[index] = wxglRenders[index].rid
        }

	//elys mod
        //conus radar
        Thread {
            if (MyApplication.radarConusRadar && !archiveMode) {
                wxglRender.constructConusRadar()
                wxglSurfaceView.requestRender()
            } else {
                wxglRender.deconstructConusRadar()
            }
        }.start()

        Thread {
            if (PolygonType.TST.pref && !archiveMode) wxglRender.constructWarningLines() else wxglRender.deconstructWarningLines()
            if (PolygonType.MCD.pref && !archiveMode) wxglRender.constructWatchMcdLines() else wxglRender.deconstructWatchMcdLines()
            if (PolygonType.MPD.pref && !archiveMode) wxglRender.constructMpdLines() else wxglRender.deconstructMpdLines()
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }.start()
        if (MyApplication.locationDotFollowsGps) fnGps()
        if (PolygonType.LOCDOT.pref || MyApplication.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRender.constructLocationDot(latLon.latString, latLon.lonString, archiveMode)
        } else {
            wxglRender.deconstructLocationDot()
        }
	
	//elys mod
        if (PolygonType.USERPOINTS.pref && !archiveMode) wxglRender.constructUserPoints() else wxglRender.deconstructUserPoints()

        if (imageMap != null && imageMap.map.visibility != View.VISIBLE) {
            paneList.forEach { wxglSurfaceViews[it].visibility = View.VISIBLE }
        }
    }

    fun plotWarningPolygons(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender, archiveMode: Boolean = false) {
        Thread {
            if (PolygonType.TST.pref && !archiveMode) wxglRender.constructWarningLines() else wxglRender.deconstructWarningLines()
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotMcdWatchPolygons(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender, archiveMode: Boolean = false) {
        Thread {
            if (PolygonType.MCD.pref && !archiveMode) wxglRender.constructWatchMcdLines() else wxglRender.deconstructWatchMcdLines()
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotMpdPolygons(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender, archiveMode: Boolean = false) {
        Thread {
            if (PolygonType.MPD.pref && !archiveMode) wxglRender.constructMpdLines() else wxglRender.deconstructMpdLines()
            wxglRender.constructGenericWarningLines()
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotWpcFronts(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender, archiveMode: Boolean = false) {
        Thread {
            if (MyApplication.radarShowWpcFronts && !archiveMode) wxglRender.constructWpcFronts() else wxglRender.deconstructWpcFronts()
            wxglSurfaceView.requestRender()
        }.start()
    }

    //elys mod - removed showExtras to show everything
    fun plotRadar(
            wxglRender: WXGLRender,
            urlString: String,
            context: Context,
            fnGps: () -> Unit,
            fnGetLatLon: () -> LatLon,
            /* showExtras: Boolean, */ 
            archiveMode: Boolean = false
    ) {
        wxglRender.constructPolygons("", urlString, true)
        // work-around for a bug in which raster doesn't show on first launch
        if (wxglRender.product == "NCR" || wxglRender.product == "NCZ") wxglRender.constructPolygons("", urlString, true)
        if ((PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) && !archiveMode) wxglRender.constructSpotters() else wxglRender.deconstructSpotters()
        if (PolygonType.STI.pref && !archiveMode) wxglRender.constructStiLines() else wxglRender.deconstructStiLines()
        if (PolygonType.HI.pref && !archiveMode) wxglRender.constructHi() else wxglRender.deconstructHi()
        if (PolygonType.TVS.pref && !archiveMode) wxglRender.constructTvs() else wxglRender.deconstructTvs()
        if (MyApplication.locationDotFollowsGps && !archiveMode) fnGps()
        if (PolygonType.LOCDOT.pref || archiveMode || MyApplication.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRender.constructLocationDot(latLon.latString, latLon.lonString, archiveMode)
        } else {
            wxglRender.deconstructLocationDot()
        }
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) UtilityMetar.getStateMetarArrayForWXOGL(context, wxglRender.rid, wxglRender.paneNumber)
        if (PolygonType.WIND_BARB.pref && !archiveMode) wxglRender.constructWBLines() else wxglRender.deconstructWBLines()
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
                UtilitySwoDayOne.get(context)
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

    private fun showNearestProduct(context: Context, polygonType: PolygonType, wxglSurfaceView: WXGLSurfaceView) {
        //val text = withContext(Dispatchers.IO) { UtilityWatch.show(wxglSurfaceView.latLon, polygonType) }
        //if (text != "") {
        //    ObjectIntent.showMcd(context, arrayOf(text, "", polygonType.toString()))
        //}

        FutureText2(
            context,
            { UtilityWatch.show(wxglSurfaceView.latLon, polygonType) },
            { s -> ObjectIntent.showMcd(context, arrayOf(s, "", polygonType.toString())) }
        )




    }
    
    //elys mod
    var getrid: String = ""
    private fun showNearestRadarStatus(context: Context, activity: Activity, glview: WXGLSurfaceView) {
        FutureText2(
            context,
            {
                val pointX = glview.newY.toDouble()
                val pointY = glview.newX * -1.0
                getrid = UtilityLocation.getNearestRadarSite(
                    LatLon(
                        pointX.toString(),
                        pointY.toString()
                    )
                )
                UtilityLog.d("wx", "point: x: " + pointX + "y: " + pointY)
                UtilityLog.d("wx", "radar status on point: " + getrid)
                UtilityDownload.getRadarStatusMessage(context, getrid)

            },
            {   s ->
                var radarStatus = s
                if (radarStatus == "") {
                    radarStatus = "The current radar status for " + getrid + " is not available."
                }
                ObjectDialogue(activity, Utility.fromHtml(radarStatus))
            }
        )


    }



    /*
    var getrid: String = ""
    private fun showNearestRadarStatus(context: Context, act: Activity, glview: WXGLSurfaceView, uiDispatcher: CoroutineDispatcher) = GlobalScope.launch(uiDispatcher) {
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
        ObjectDialogue(act, Utility.fromHtml(radarStatus))

    }
     */


    private fun showSpotterInfo(activity: Activity, glview: WXGLSurfaceView, context: Context) {
        FutureText2(
            context,
            { UtilitySpotter.findClosestSpotter(glview.latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }


    //UserPoints System
    private fun showUserPointInfo(activity: Activity, context: Context, glview: WXGLSurfaceView)  {
        FutureText2(
            context,
            { UtilityUserPoints.findClosestUserPoint(context, glview.latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }

    private fun addUserPoint(act: Activity, context: Context, glview: WXGLSurfaceView) {
        UtilityUserPoints.addUserPoint(context, glview.latLon)
        ObjectDialogue(act, "added userpoint")
    }


    private fun deleteUserPoint(activity: Activity, context: Context, glview: WXGLSurfaceView)  {
        FutureText2(
            context,
            { UtilityUserPoints.deleteUserPoint(context, glview.latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }

    private fun deleteAllUserPoints(activity: Activity, context: Context) {
        UtilityUserPoints.deleteAllUserPoints(context)
        ObjectDialogue(activity, "Deleted all userpoints")

    }



}
