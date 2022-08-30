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
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.*
import joshuatee.wx.settings.UtilityLocation

internal object UtilityRadarUI {

    private const val lastRadarTimePref = "NEXRADDOWNLOAD_TIME_LAST_RAN"

    fun updateLastRadarTime(context: Context) {
        Utility.writePref(context, lastRadarTimePref, ObjectDateTime.getCurrentLocalTimeAsString())
    }

    fun getLastRadarTime(context: Context) = Utility.readPref(context, lastRadarTimePref, "")

    fun getRadarStatus(activity: Activity, wxglRender: WXGLRender) {
        val radar = wxglRender.ridNewList[0].name
        FutureText2(activity,
                { UtilityDownload.getRadarStatusMessage(activity, radar) })
                { s ->
                    var radarStatus = s
                    if (radarStatus == "") {
                        radarStatus = "The current radar status for $radar is not available."
                    }
                    ObjectDialogue(activity, radarStatus)
                }
    }

    fun getMetar(wxglSurfaceView: WXGLSurfaceView, activity: Context) {
        FutureText2(activity,
                { UtilityMetar.findClosestMetar(activity, wxglSurfaceView.latLon) })
                { s -> ObjectDialogue(activity, s) }
    }

    fun showNearestMeteogram(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        // http://www.nws.noaa.gov/mdl/gfslamp/meteoform.php
        // http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=KTEW
        val obsSite = UtilityMetar.findClosestObservation(context, wxglSurfaceView.latLon)
        Route.image(context, UtilityWXOGL.getMeteogramUrl(obsSite.name), obsSite.name + " Meteogram")
    }

    fun showNearestWarning(context: Context, wxglSurfaceView: WXGLSurfaceView) {
        val url = UtilityWXOGL.showTextProducts(wxglSurfaceView.latLon)
        if (url != "") {
            Route.hazard(context, url)
        }
    }

    fun showNearestProduct(context: Context, polygonType: PolygonType, wxglSurfaceView: WXGLSurfaceView) {
        val numberString = UtilityWatch.show(wxglSurfaceView.latLon, polygonType)
        if (numberString != "") {
            Route.mcd(context, numberString, polygonType.toString())
        }
    }

    fun getImageForShare(activity: Activity, wxglRender: WXGLRender, indexString: String) {
        FutureBytes2(activity, { UtilityUSImgWX.layeredImgFromFile(
                activity,
                wxglRender.rid,
                wxglRender.product,
                indexString)
        }) { bitmapForShare -> UtilityShare.bitmap(activity,
                wxglRender.rid + " (" + Utility.getRadarSiteName(wxglRender.rid) + ") " + wxglRender.product,
                bitmapForShare
        )}
    }

    fun showHelp(activity: Activity) {
        ObjectDialogue(activity,
                activity.resources.getString(R.string.help_radar)
                        + GlobalVariables.newline + GlobalVariables.newline
                        + activity.resources.getString(R.string.help_radar_drawingtools)
                        + GlobalVariables.newline + GlobalVariables.newline
                        + activity.resources.getString(R.string.help_radar_recording)
                        + GlobalVariables.newline + GlobalVariables.newline
        )
    }

    //elys mod
    fun showSpotterInfo(activity: Activity, glview: WXGLSurfaceView, context: Context) {
        FutureText2(
            context,
            { UtilitySpotter.findClosestSpotter(glview.latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }


    //UserPoints System
    fun showUserPointInfo(activity: Activity, context: Context, glview: WXGLSurfaceView)  {
        FutureText2(
            context,
            { UtilityUserPoints.findClosestUserPoint(context, glview.latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }

    fun addUserPoint(act: Activity, context: Context, glview: WXGLSurfaceView) {
        UtilityUserPoints.addUserPoint(context, glview.latLon)
        ObjectDialogue(act, "added userpoint")
    }


    fun deleteUserPoint(activity: Activity, context: Context, glview: WXGLSurfaceView)  {
        FutureText2(
            context,
            { UtilityUserPoints.deleteUserPoint(context, glview.latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }

    fun deleteAllUserPoints(activity: Activity, context: Context) {
        UtilityUserPoints.deleteAllUserPoints(context)
        ObjectDialogue(activity, "Deleted all userpoints")

    }
//////////////////////////////////////////


}
