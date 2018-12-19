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
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.AdhocForecastActivity
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectDialogue

import joshuatee.wx.util.*
import kotlinx.coroutines.*

internal object UtilityRadarUI {

    const val longPressRadarSiteRegex = "\\) ([A-Z]{3,4}) "

    fun getRadarStatus(
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
        if (radarStatus == "") {
            radarStatus = "The current radar status for " + oglr.rid + " is not available."
        }
        UtilityAlertDialog.showHelpText(Utility.fromHtml(radarStatus), act)
    }

    fun getMetar(
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

    fun showNearestForecast(context: Context, glview: WXGLSurfaceView) {
        ObjectIntent(
            context,
            AdhocForecastActivity::class.java,
            AdhocForecastActivity.URL,
            arrayOf(glview.newY.toString(), "-" + glview.newX.toString())
        )
    }

    fun showNearestMeteogram(context: Context, glview: WXGLSurfaceView) {
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

    fun showNearestWarning(context: Context, glview: WXGLSurfaceView) {
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
        alertDialogRadarLongPress.setTitle(
            UtilityStringExternal.truncate(glview.newY.toString(), 6)
                    + ",-" + UtilityStringExternal.truncate(glview.newX.toString(), 6)
        )
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

        // FIXME show site/office in initial long press for obs / meteogram and radar status
        alertDialogRadarLongpressAl.add("Show warning text")
        if (MyApplication.radarWatMcd) {
            alertDialogRadarLongpressAl.add("Show watch text")
            alertDialogRadarLongpressAl.add("Show MCD text")
        }
        if (MyApplication.radarMpd) {
            alertDialogRadarLongpressAl.add("Show MPD text")
        }
        alertDialogRadarLongpressAl.add("Show nearest observation")
        alertDialogRadarLongpressAl.add("Show nearest forecast")
        alertDialogRadarLongpressAl.add("Show nearest meteogram")
        if (MyApplication.radarSpotters || MyApplication.radarSpottersLabel) {
            alertDialogRadarLongpressAl.add("Show Spotter Info")
        }
        alertDialogRadarLongpressAl.add("Show radar status message")
        alertDialogRadarLongPress.show()
    }


    fun showNearestWatch(
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

    fun showNearestMcd(
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


    fun showNearestMpd(
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

    fun showSpotterInfo(
            act: Activity,
            glview: WXGLSurfaceView,
            uiDispatcher: CoroutineDispatcher
    ) = GlobalScope.launch(uiDispatcher) {
        val txt = withContext(Dispatchers.IO) {
            UtilitySpotter.findClosestSpotter(glview.latLon)
        }
        UtilityAlertDialog.showHelpText(txt, act)
    }



    //from single pane
    /*


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


     */



    //from multipane
    /*


    private fun getMetar() = GlobalScope.launch(uiDispatcher) {
    val txt = withContext(Dispatchers.IO) { UtilityMetar.findClosestMetar(contextg, LatLon(glviewArr[idxIntG].newY.toDouble(), (glviewArr[idxIntG].newX * -1).toDouble())) }
    UtilityAlertDialog.showHelpText(txt, act)
    }


    private fun getSpotterInfo() = GlobalScope.launch(uiDispatcher) {
    var txt = withContext(Dispatchers.IO) { UtilitySpotter.findClosestSpotter(LatLon(glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0)) }
    UtilityAlertDialog.showHelpText(txt, act)
    }

    private fun getWatch() = GlobalScope.launch(uiDispatcher) {
    var txt = withContext(Dispatchers.IO) {  UtilityWat.showWatchProducts(contextg, glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0) }
    if (txt != "") {
    UtilityAlertDialog.showHelpText(txt, act)
    }
    }

    private fun getMCD() = GlobalScope.launch(uiDispatcher) {
    var txt = withContext(Dispatchers.IO) {  UtilityWat.showMCDProducts(contextg, glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0) }
    if (txt != "") {
    UtilityAlertDialog.showHelpText(txt, act)
    }
    }
    private fun getMPD() = GlobalScope.launch(uiDispatcher) {
    var txt = withContext(Dispatchers.IO) {  UtilityWat.showMPDProducts(contextg, glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0) }
    if (txt != "") {
    UtilityAlertDialog.showHelpText(txt, act)
    }
    }


     */


    //locfrag
    /*


    private fun getSpotterInfo() = GlobalScope.launch(uiDispatcher) {
         var txt = withContext(Dispatchers.IO) { UtilitySpotter.findClosestSpotter(LatLon(glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0)) }
         UtilityAlertDialog.showHelpText(txt, activityReference)
    }

    private fun getWatch() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) {  UtilityWat.showWatchProducts(MyApplication.appContext, glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0) }
        if (txt != "") {
            UtilityAlertDialog.showHelpText(txt, activityReference)
        }
    }

    private fun getMCD() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) {  UtilityWat.showMCDProducts(MyApplication.appContext, glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0) }
        if (txt != "") {
            UtilityAlertDialog.showHelpText(txt, activityReference)
        }
    }

    private fun getMPD() = GlobalScope.launch(uiDispatcher) {
        var txt = withContext(Dispatchers.IO) {  UtilityWat.showMPDProducts(MyApplication.appContext, glviewArr[idxIntG].newY.toDouble(), glviewArr[idxIntG].newX.toDouble() * -1.0) }
        if (txt != "") {
            UtilityAlertDialog.showHelpText(txt, activityReference)
        }
    }

     */


}
