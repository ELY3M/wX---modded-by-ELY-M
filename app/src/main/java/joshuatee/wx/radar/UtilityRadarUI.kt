/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import joshuatee.wx.activitiesmisc.AdhocForecastActivity
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap

import joshuatee.wx.util.*
import kotlinx.coroutines.*

internal object UtilityRadarUI {

    const val longPressRadarSiteRegex = "\\) ([A-Z]{3,4}) "

    private fun getRadarStatus(
        act: Activity,
        context: Context,
        uiDispatcher: CoroutineDispatcher,
        oglr: WXGLRender
    ) = GlobalScope.launch(uiDispatcher) {
        var radarStatus = withContext(Dispatchers.IO) {
            UtilityDownload.getRadarStatusMessage(
                context,
                oglr.rid
            )
        }
        UtilityLog.d("wx", "UtilityRadarUI radarStatus: "+radarStatus)
        if (radarStatus == "") {
            radarStatus = "The current radar status for " + oglr.rid + " is not available."
        }
        UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)
    }

    private fun getMetar(
        glview: WXGLSurfaceView,
        act: Activity,
        context: Context,
        uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityMetar.findClosestMetar(context, glview.latLon)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }

    private fun showNearestForecast(context: Context, glview: WXGLSurfaceView) {
        ObjectIntent(
            context,
            AdhocForecastActivity::class.java,
            AdhocForecastActivity.URL,
            arrayOf(glview.newY.toString(), "-" + glview.newX.toString())
        )
    }

    private fun showNearestMeteogram(context: Context, glview: WXGLSurfaceView) {
        // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
        // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW
        val obsSite = UtilityMetar.findClosestObservation(
            context,
            glview.latLon
        )
        ObjectIntent(
            context,
            ImageShowActivity::class.java,
            ImageShowActivity.URL,
            arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram")
        )
    }

    private fun showNearestWarning(context: Context, glview: WXGLSurfaceView) {
        val polygonUrl = UtilityWXOGL.showTextProducts(
            glview.newY.toDouble(),
            glview.newX.toDouble() * -1.0
        )
        if (polygonUrl != "") ObjectIntent(
            context,
            USAlertsDetailActivity::class.java,
            USAlertsDetailActivity.URL,
            arrayOf(polygonUrl, "")
        )
    }
    
    
    private fun showNearestWatch(
            context: Context,
            act: Activity,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityWat.showWatchProducts(context, glview.newY.toDouble(), glview.newX.toDouble() * -1.0)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }

    private fun showNearestMcd(
            context: Context,
            act: Activity,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityWat.showMCDProducts(context, glview.newY.toDouble(), glview.newX.toDouble() * -1.0)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }


    private fun showNearestMpd(
            context: Context,
            act: Activity,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilityWat.showMPDProducts(context, glview.newY.toDouble(), glview.newX.toDouble() * -1.0)
        }
        UtilityAlertDialog.showHelpText(txt, act)
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

    fun addItemsToLongPress(
        alertDialogRadarLongpressAl: MutableList<String>,
        lat: String,
        lon: String,
        context: Context,
        glview: WXGLSurfaceView,
        oglr: WXGLRender,
        alertDialogRadarLongPress: ObjectDialogue
    ) {

        alertDialogRadarLongpressAl.clear()

        val locX = lat.toDoubleOrNull() ?: 0.0
        val locY = lon.toDoubleOrNull() ?: 0.0
        val pointX = glview.newY.toDouble()
        val pointY = glview.newX * -1.0
        val dist =
            LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val ridX = (Utility.readPref(context, "RID_" + oglr.rid + "_X", "0.0")).toDouble()
        val ridY =
            -1.0 * (Utility.readPref(context, "RID_" + oglr.rid + "_Y", "0.0")).toDouble()
        val distRid =
            LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)

        // FIXME look at iOS version and try to match in data provided and improve formatting
        val latLonTitle = UtilityStringExternal.truncate(glview.newY.toString(), 6) +
                ", -" +
                UtilityStringExternal.truncate(glview.newX.toString(), 6)
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
            ) + " miles from " + oglr.rid
        )
        oglr.ridNewList.mapTo(alertDialogRadarLongpressAl) {
            "Radar: (" + it.distance + " mi) " + it.name + " " + Utility.readPref(
                context,
                "RID_LOC_" + it.name,
                ""
            )
        }
        val obsSite = UtilityMetar.findClosestObservation(
            context,
            glview.latLon
        )
        alertDialogRadarLongpressAl.add("Show warning text")
        if (MyApplication.radarWatMcd) {
            alertDialogRadarLongpressAl.add("Show watch text")
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
        alertDialogRadarLongpressAl.add("Show radar status message: " + oglr.rid)
        alertDialogRadarLongPress.show()
    }
    
        fun doLongPressAction(
        strName: String,
        context: Context,
        act: Activity,
        glview: WXGLSurfaceView,
        oglr: WXGLRender,
        uiDispatcher: CoroutineDispatcher,
        fn: (strName: String) -> Unit
    ) {
        when {
            strName.contains("Show warning text") -> {
                showNearestWarning(context, glview)
            }
            strName.contains("Show watch text") -> {
                showNearestWatch(context, act, glview, uiDispatcher)
            }
            strName.contains("Show MCD text") -> {
                showNearestMcd(context, act, glview, uiDispatcher)
            }
            strName.contains("Show MPD text") -> {
                showNearestMpd(context, act, glview, uiDispatcher)
            }
            strName.contains("Show nearest observation") -> {
                getMetar(glview, act, context, uiDispatcher)
            }
            strName.contains("Show nearest meteogram") -> {
                showNearestMeteogram(context, glview)
            }
            strName.contains("Show radar status message") -> {
                getRadarStatus(act, context, uiDispatcher, oglr)
            }
            strName.contains("Show nearest forecast") -> {
                showNearestForecast(context, glview)
            }
            strName.contains("Show Spotter Info") -> {
                showSpotterInfo(act, glview, uiDispatcher)
            }
            else -> fn(strName)
        }
    }

    fun initGlviewFragment(
        glviewloc: WXGLSurfaceView,
        z: Int,
        oglrArr: MutableList<WXGLRender>,
        glviewArr: MutableList<WXGLSurfaceView>,
        wxgltextArr: MutableList<WXGLTextObject>,
        changeListener: WXGLSurfaceView.OnProgressChangeListener
    ): Boolean {
        glviewloc.setEGLContextClientVersion(2)
        wxgltextArr[z].setOGLR(oglrArr[z])
        oglrArr[z].idxStr = z.toString()
        glviewloc.setRenderer(oglrArr[z])
        glviewloc.setRenderVar(oglrArr[z], oglrArr, glviewArr)
        glviewloc.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glviewloc.setOnProgressChangeListener(changeListener)
        oglrArr[z].zoom = MyApplication.wxoglSize.toFloat() / 10.0f
        glviewloc.scaleFactor = MyApplication.wxoglSize.toFloat() / 10.0f
        return true
    }

    fun initGlview(
        glview: WXGLSurfaceView,
        glviewArr: MutableList<WXGLSurfaceView>,
        oglr: WXGLRender,
        oglrArr: MutableList<WXGLRender>,
        act: Activity,
        toolbar: Toolbar,
        toolbarBottom: Toolbar,
        changeListener: WXGLSurfaceView.OnProgressChangeListener,
        archiveMode: Boolean = false
    ) {
        glview.setEGLContextClientVersion(2)
        glview.setRenderer(oglr)
        glview.setRenderVar(oglr, oglrArr, glviewArr, act)
        glview.fullScreen = true
        glview.setOnProgressChangeListener(changeListener)
        glview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        glview.toolbar = toolbar
        glview.toolbarBottom = toolbarBottom
        glview.archiveMode = archiveMode
    }

    fun initWxoglGeom(
        glv: WXGLSurfaceView,
        ogl: WXGLRender,
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
        ogl.initGEOM()
        if (oldRidArr[z] != oglrArr[z].rid) {
            ogl.setChunkCount(0)
            ogl.setChunkCountSti(0)
            ogl.setHiInit(false)
            ogl.setTvsInit(false)
            Thread(Runnable {
                ogl.constructStateLines()
                glv.requestRender()
            }).start()
            Thread(Runnable {
                if (GeographyType.LAKES.pref)
                    ogl.constructLakes()
                else
                    ogl.deconstructLakes()
            }).start()
            Thread(Runnable {
                if (GeographyType.COUNTY_LINES.pref) {
                    ogl.constructCounty()
                    glv.requestRender()
                } else
                    ogl.deconstructCounty()
            }).start()
            Thread(Runnable {
                if (GeographyType.HIGHWAYS.pref) {
                    ogl.constructHWLines()
                    glv.requestRender()
                } else
                    ogl.deconstructHWLines()
            }).start()
            Thread(Runnable {
                if (MyApplication.radarHwEnhExt) {
                    ogl.constructHWEXTLines()
                    glv.requestRender()
                } else
                    ogl.deconstructHWEXTLines()
            }).start()
            wxgltextArr[z].addTV()
            oldRidArr[z] = oglrArr[z].rid
        }

        //conus radar
        Thread(Runnable {
            if (MyApplication.radarConusRadar && !archiveMode) {
                ogl.constructConusRadar()
                glv.requestRender()
            } else {
                ogl.deconstructConusRadar()
            }
        }).start()

        Thread(Runnable {

            if (PolygonType.TOR.pref && !archiveMode)
                ogl.constructTorWarningLines()
            else
                ogl.deconstructTorWarningLines()

            if (PolygonType.SVR.pref && !archiveMode)
                ogl.constructSvrWarningLines()
            else
                ogl.deconstructSvrWarningLines()

            if (PolygonType.EWW.pref && !archiveMode)
                ogl.constructEwwWarningLines()
            else
                ogl.deconstructEwwWarningLines()

            if (PolygonType.FFW.pref && !archiveMode)
                ogl.constructFfwWarningLines()
            else
                ogl.deconstructFfwWarningLines()

            if (PolygonType.SMW.pref && !archiveMode)
                ogl.constructSmwWarningLines()
            else
                ogl.deconstructSmwWarningLines()

            if (PolygonType.SVS.pref && !archiveMode)
                ogl.constructSvsWarningLines()
            else
                ogl.deconstructSvsWarningLines()

            if (PolygonType.SPS.pref && !archiveMode)
                ogl.constructSpsWarningLines()
            else
                ogl.deconstructSpsWarningLines()

            if (PolygonType.MCD.pref && !archiveMode)
                ogl.constructWATMCDLines()
            else
                ogl.deconstructWATMCDLines()

            if (PolygonType.MPD.pref && !archiveMode)
                ogl.constructMPDLines()
            else
                ogl.deconstructMPDLines()
            glv.requestRender()
        }).start()
        if (MyApplication.locdotFollowsGps) {
            fnGps()
            //locXCurrent = latlonArr[0]
            //locYCurrent = latlonArr[1]
        }
        if (PolygonType.LOCDOT.pref || MyApplication.locdotFollowsGps) {
            val latLon = fnGetLatLon()
            UtilityLog.d("wx", "LAT: " + latLon.latString)
            UtilityLog.d("wx", "LON: " + latLon.lonString)
            ogl.constructLocationDot(latLon.latString, latLon.lonString, archiveMode)
        } else {
            ogl.deconstructLocationDot()
        }
        if (imageMap != null && imageMap.map.visibility != View.VISIBLE) {
            numPanesArr.forEach { glviewArr[it].visibility = View.VISIBLE }
        }
    }

    fun plotRadar(
        oglr: WXGLRender,
        urlStr: String,
        context: Context,
        fnGps: () -> Unit,
        fnGetLatLon: () -> LatLon,
            //TODO why?!?!? showExtras is only in this file....
            //TODO not needed
        //showExtras: Boolean,
        archiveMode: Boolean = false
    ) {
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
            fnGps()
        }
        if (PolygonType.LOCDOT.pref || archiveMode || MyApplication.locdotFollowsGps) {
            val latLon = fnGetLatLon()
            UtilityLog.d("wx", "LAT: " + latLon.latString)
            UtilityLog.d("wx", "LON: " + latLon.lonString)
            oglr.constructLocationDot(latLon.latString, latLon.lonString, archiveMode)
            //oglr.constructLocationDot(locXCurrent, locYCurrent, archiveMode)
        } else {
            oglr.deconstructLocationDot()
        }

        //if (showExtras) {
            if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && !archiveMode) {
                UtilityMetar.getStateMetarArrayForWXOGL(context, oglr.rid)
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
        //} //showExtras
    }
}
