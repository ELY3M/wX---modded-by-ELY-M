/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.FutureBytes2
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.Route

internal object NexradRenderUI {

    private const val LAST_RADAR_TIME_PREF = "NEXRADDOWNLOAD_TIME_LAST_RAN"

    fun updateLastRadarTime(context: Context) {
        Utility.writePref(
            context,
            LAST_RADAR_TIME_PREF,
            ObjectDateTime.getCurrentLocalTimeAsString()
        )
    }

    fun getLastRadarTime(context: Context): String =
        Utility.readPref(context, LAST_RADAR_TIME_PREF, "")

    fun showRadarStatus(activity: Activity, radarSite: String) {
        FutureText2(
            { DownloadText.radarStatusMessage(activity, radarSite) })
        { s ->
            var radarStatus = s
            if (radarStatus == "") {
                radarStatus = "The current radar status for $radarSite is not available."
            }
            ObjectDialogue(activity, radarStatus)
        }
    }

    fun showMetar(context: Context, latLon: LatLon) {
        FutureText2(
            { Metar.findClosestMetar(context, latLon) })
        { s -> ObjectDialogue(context, s) }
    }

    fun showNearestMeteogram(context: Context, latLon: LatLon) {
        // https://lamp.mdl.nws.noaa.gov/lamp/meteoform.php
        val obsSite = Metar.findClosestObservation(context, latLon).codeName
        Route.image(context, getMeteogramUrl(obsSite), "$obsSite Meteogram")
    }

    private fun getMeteogramUrl(obsSite: String): String =
        "https://lamp.mdl.nws.noaa.gov/lamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=$obsSite"

    fun showNearestWarning(context: Context, latLon: LatLon) {
        val url = Warnings.show(latLon)
        if (url != "") {
            Route.hazard(context, url)
        }
    }

    fun showNearestMcd(context: Context, polygonType: PolygonType, latLon: LatLon) {
        val numberString = Watch.show(latLon, polygonType)
        if (numberString != "") {
            Route.mcd(context, numberString, polygonType.toString())
        }
    }

    fun showImageForShare(
        activity: Activity,
        indexString: String,
        radarSite: String,
        product: String
    ) {
        FutureBytes2({
            CanvasCreate.layeredImageFromFile(
                activity,
                radarSite,
                product,
                indexString
            )
        }) { bitmapForShare ->
            UtilityShare.bitmap(
                activity,
                radarSite + " (" + RadarSites.getRadarSiteName(radarSite) + ") " + product,
                bitmapForShare
            )
        }
    }

    fun showHelp(activity: Activity) {
        ObjectDialogue(
            activity,
            activity.resources.getString(R.string.help_radar)
                    + GlobalVariables.newline + GlobalVariables.newline
                    + activity.resources.getString(R.string.help_radar_drawing_tools)
                    + GlobalVariables.newline + GlobalVariables.newline
                    + activity.resources.getString(R.string.help_radar_recording)
                    + GlobalVariables.newline + GlobalVariables.newline
        )
    }
    
    //FIXME!!!!

    //elys mod
    fun showSpotterInfo(activity: Activity, latLon: LatLon) {
        FutureText2({ UtilitySpotter.findClosestSpotter(latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }


    //UserPoints System
    fun showUserPointInfo(activity: Activity, context: Context, latLon: LatLon)  {
        FutureText2({ UtilityUserPoints.findClosestUserPoint(context, latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }

    fun addUserPoint(act: Activity, context: Context, latLon: LatLon) {
        UtilityUserPoints.addUserPoint(context, latLon)
        ObjectDialogue(act, "added userpoint")
    }


    fun deleteUserPoint(activity: Activity, context: Context, latLon: LatLon)  {
        FutureText2({ UtilityUserPoints.deleteUserPoint(context, latLon) },
            { s -> ObjectDialogue(activity, s) }
        )
    }

    fun deleteAllUserPoints(activity: Activity, context: Context) {
        UtilityUserPoints.deleteAllUserPoints(context)
        ObjectDialogue(activity, "Deleted all userpoints")

    }
//////////////////////////////////////////


}
