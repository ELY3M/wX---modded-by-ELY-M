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

import android.app.Activity
import android.content.Context
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.util.*
import kotlin.math.roundToInt
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.MyApplication //elys mod //need context...
import joshuatee.wx.objects.*
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation

internal object UtilityRadarUI {

    const val longPressRadarSiteRegex = "\\) ([A-Z]{3,4}) "
    private const val lastRadarTimePref = "NEXRADDOWNLOAD_TIME_LAST_RAN"

    fun updateLastRadarTime(context: Context) {
        Utility.writePref(context, lastRadarTimePref, ObjectDateTime.getCurrentLocalTimeAsString())
    }

    fun getLastRadarTime(context: Context) = Utility.readPref(context, lastRadarTimePref, "")

    private fun getRadarStatus(activity: Activity, wxglRender: WXGLRender) {
        FutureText2(activity,
                { UtilityDownload.getRadarStatusMessage(activity, wxglRender.rid) })
                { s ->
                    var radarStatus = s
                    if (radarStatus == "") {
                        radarStatus = "The current radar status for " + wxglRender.rid + " is not available."
                    }
                    ObjectDialogue(activity, radarStatus)
                }
    }

    private fun getMetar(wxglSurfaceView: WXGLSurfaceView, activity: Context) {
        FutureText2(activity,
                { UtilityMetar.findClosestMetar(activity, wxglSurfaceView.latLon) })
                { s -> ObjectDialogue(activity, s) }
    }

    private fun showNearestForecast(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        Route.forecast(context, arrayOf(wxglSurfaceView.newY.toString(), "-" + wxglSurfaceView.newX.toString()))
    }

    private fun showNearestMeteogram(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
        // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW
        val obsSite = UtilityMetar.findClosestObservation(context, wxglSurfaceView.latLon)
        Route.image(context, arrayOf(UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram"))
    }

    private fun showNearestWarning(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        val url = UtilityWXOGL.showTextProducts(wxglSurfaceView.latLon)
        if (url != "") {
            Route.hazard(context, arrayOf(url, ""))
        }
    }

    fun setupContextMenu(
            longPressList: MutableList<String>,
            lat: String,
            lon: String,
            context: Context,
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            longPressDialogue: ObjectDialogue
    ) {
        longPressList.clear()
        val locX = To.double(lat)
        val locY = To.double(lon)
        val pointX = wxglSurfaceView.newY
        val pointY = wxglSurfaceView.newX * -1.0
        val dist = LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val ridX = To.double(Utility.getRadarSiteX(wxglRender.rid))
        val ridY = -1.0 * To.double(Utility.getRadarSiteY(wxglRender.rid))
        val distRid = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.MILE)
        val distRidKm = LatLon.distance(LatLon(ridX, ridY), LatLon(pointX, pointY), DistanceUnit.KM)
        val latLonTitle = wxglSurfaceView.newY.toString().take(6) + ", -" + wxglSurfaceView.newX.toString().take(6)
        longPressDialogue.setTitle(latLonTitle)
        longPressList.add(dist.toString().take(6) + " miles from location")
        longPressList.add(distRid.toString().take(6) + " miles from " + wxglRender.rid)
        val heightAgl = UtilityMath.getRadarBeamHeight(wxglRender.wxglNexradLevel3.degree.toDouble(), distRidKm)
        val heightMsl = wxglRender.wxglNexradLevel3.radarHeight + heightAgl
        longPressList.add("Beam Height MSL: " + heightMsl.roundToInt().toString() + " ft, AGL: " + heightAgl.roundToInt().toString() + " ft")
        if (PolygonType.WPC_FRONTS.pref) {
            var wpcFrontsTimeStamp = Utility.readPref(context,"WPC_FRONTS_TIMESTAMP", "")
            wpcFrontsTimeStamp = wpcFrontsTimeStamp.replace(ObjectDateTime.getYear().toString(), "")
            if (wpcFrontsTimeStamp.length > 6) {
                wpcFrontsTimeStamp = wpcFrontsTimeStamp.insert(4, " ")
            }
	    //elys mod - No newline
            longPressList.add("WPC Fronts: " + wpcFrontsTimeStamp)
        }
        longPressList += wxglRender.ridNewList.map {
            "Radar: (" + it.distance.roundToInt() + " mi) " + it.name + " " + Utility.getRadarSiteName(it.name)
        }
        val obsSite = UtilityMetar.findClosestObservation(context, wxglSurfaceView.latLon)
        if ((RadarPreferences.warnings || ObjectPolygonWarning.areAnyEnabled()) && ObjectPolygonWarning.isCountNonZero()) {
            longPressList.add("Show Warning")
        }
        // Thanks to Ely
        if (RadarPreferences.watMcd && ObjectPolygonWatch.watchLatlonCombined.value != "") {
            longPressList.add("Show Watch")
        }
        if (RadarPreferences.watMcd && ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]!!.latLonList.value != "") {
            longPressList.add("Show MCD")
        }
        if (RadarPreferences.mpd && ObjectPolygonWatch.polygonDataByType[PolygonType.MPD]!!.latLonList.value != "") {
            longPressList.add("Show MPD")
        }
        // end Thanks to Ely
        longPressList.add("Nearest observation: " + obsSite.name + " (" + obsSite.distance.roundToInt() + " mi)")
        longPressList.add("Nearest forecast: $latLonTitle")
        longPressList.add("Nearest meteogram: " + obsSite.name)
	//elys mod
        longPressList.add("Current radar status message: " + wxglRender.rid)
	val getridpoint: String = UtilityLocation.getNearestRadarSite(LatLon(pointX.toString(), pointY.toString()))
	longPressList.add("Nearest radar status message: " + getridpoint)
	if (RadarPreferences.spotters || RadarPreferences.spottersLabel) longPressList.add("Spotter Info")
	longPressList.add("Userpoint info: " + latLonTitle)
        longPressList.add("Add userpoint for: " + latLonTitle)
        longPressList.add("Delete userpoint for: " + latLonTitle)
        longPressList.add("Delete all userpoints")
        longPressDialogue.show()
    }

    fun doLongPressAction(
            s: String,
            activity: Activity,
            wxglSurfaceView: WXGLSurfaceView,
            wxglRender: WXGLRender,
            function: (String) -> Unit
    ) {
        when {
            s.contains("miles from") -> {}
            s.contains("Show Warning") -> showNearestWarning(activity, wxglSurfaceView)
            s.contains("Show Watch") -> showNearestProduct(activity, PolygonType.WATCH, wxglSurfaceView)
            s.contains("Show MCD") -> showNearestProduct(activity, PolygonType.MCD, wxglSurfaceView)
            s.contains("Show MPD") -> showNearestProduct(activity, PolygonType.MPD, wxglSurfaceView)
            s.contains("Nearest observation") -> getMetar(wxglSurfaceView, activity)
            s.contains("Nearest forecast") -> showNearestForecast(activity, wxglSurfaceView)
            s.contains("Nearest meteogram") -> showNearestMeteogram(activity, wxglSurfaceView)
            s.contains("Current radar status message") -> getRadarStatus(activity, wxglRender)
	    s.startsWith("Beam") -> {}
            //elys mod //need context....
            s.contains("Nearest radar status message") -> showNearestRadarStatus(MyApplication.appContext, activity, wxglSurfaceView)
            s.contains("Spotter Info") -> showSpotterInfo(activity, wxglSurfaceView, MyApplication.appContext)
            s.contains("Userpoint info") -> showUserPointInfo(activity, MyApplication.appContext, wxglSurfaceView)
            s.contains("Add userpoint for") -> addUserPoint(activity, MyApplication.appContext, wxglSurfaceView)
            s.contains("Delete userpoint for") ->  deleteUserPoint(activity, MyApplication.appContext, wxglSurfaceView)
            s.contains("Delete all userpoints") -> deleteAllUserPoints(activity, MyApplication.appContext)
            else -> function(s)
        }
    }

    private fun showNearestProduct(context: Context, polygonType: PolygonType, wxglSurfaceView: WXGLSurfaceView) {
        val text = UtilityWatch.show(wxglSurfaceView.latLon, polygonType)
        if (text != "") {
            Route.mcd(context, arrayOf(text, "", polygonType.toString()))
        }
    }

    fun getImageForShare(activity: Activity, wxglRender: WXGLRender, indexString: String) {
        FutureBytes2(activity, { UtilityUSImgWX.layeredImgFromFile(
                activity,
                wxglRender.rid,
                wxglRender.product,
                indexString,
                true)
        }) { bitmapForShare -> UtilityShare.bitmap(activity,
                wxglRender.rid + " (" + Utility.getRadarSiteName(wxglRender.rid) + ") " + wxglRender.product,
                bitmapForShare
        )}
    }
                  
    //elys mod
    var getrid: String = ""
    private fun showNearestRadarStatus(context: Context, activity: Activity, glview: WXGLSurfaceView) {
        FutureText2(
            context,
            {
                val pointX = glview.newY
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
//////////////////////////////////////////


}
