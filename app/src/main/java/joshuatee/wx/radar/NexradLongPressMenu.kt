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
//Modded by ELY M.  

package joshuatee.wx.radar

import android.app.Activity
import android.content.Context
import joshuatee.wx.Extensions.insert
import joshuatee.wx.objects.*
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMath
import kotlin.math.roundToInt
//elys mod
import joshuatee.wx.MyApplication //elys mod //need context...
import joshuatee.wx.settings.UtilityLocation


class NexradLongPressMenu(
        val activity: Activity,
        val nexradState: NexradState,
        nexradArguments: NexradArguments,
        longPressRadarSiteSwitch: (String) -> Unit
) {

    private val radarLongPressItems = mutableListOf<String>()
    private var longPressDialogue = ObjectDialogue(activity, radarLongPressItems)

    init {
        longPressDialogue.connectCancel { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(activity)
        }
        longPressDialogue.connect { dialog, itemIndex ->
            val item = radarLongPressItems[itemIndex]
            doLongPressAction(
                    item,
                    activity,
                    nexradState.surface,
                    nexradState.render,
                    longPressRadarSiteSwitch
            )
            dialog.dismiss()
        }
    }

    val changeListener = object : WXGLSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                if (nexradState.numberOfPanes > 1) {
                    nexradState.curRadar = idxInt
                }
                if (nexradState.isHomeScreen) {
                    nexradState.curRadar = idx
                }
                setupContextMenu(
                        radarLongPressItems,
                        nexradArguments.locXCurrent,
                        nexradArguments.locYCurrent,
                        activity,
                        nexradState.surface,
                        nexradState.render,
                        longPressDialogue
                )
            } else {
                nexradState.wxglTextObjects.forEach {
                    it.addLabels()
                }
            }
        }
    }

    companion object {

        fun setupContextMenu(
                longPressList: MutableList<String>,
                locX: Double,
                locY: Double,
                context: Context,
                wxglSurfaceView: WXGLSurfaceView,
                wxglRender: WXGLRender,
                longPressDialogue: ObjectDialogue
        ) {
            longPressList.clear()
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
                longPressList.add("WPC Fronts: $wpcFrontsTimeStamp")
            }
            longPressList += wxglRender.ridNewList.map {
                // "Radar: (" + it.distance.roundToInt() + " mi) " + it.name + " " + Utility.getRadarSiteName(it.name)
                it.name + " " + Utility.getRadarSiteName(it.name) + " " + it.distance.roundToInt().toString() + " mi"
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
            longPressList.add("Observation: " + obsSite.name + " (" + obsSite.distance.roundToInt() + " mi)")
            longPressList.add("Forecast: $latLonTitle")
            longPressList.add("Meteogram: " + obsSite.name)
    	//elys mod
	    //TODO Merge this?? and just use one with the lat/lon??
        longPressList.add("Current Radar status message: " + wxglRender.rid)
	    val getridpoint = UtilityLocation.getNearestRadarSite(wxglSurfaceView.latLon)
	    longPressList.add("Radar status message: " + getridpoint)
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
                s.contains("Show Warning") -> UtilityRadarUI.showNearestWarning(activity, wxglSurfaceView)
                s.contains("Show Watch") -> UtilityRadarUI.showNearestProduct(activity, PolygonType.WATCH, wxglSurfaceView)
                s.contains("Show MCD") -> UtilityRadarUI.showNearestProduct(activity, PolygonType.MCD, wxglSurfaceView)
                s.contains("Show MPD") -> UtilityRadarUI.showNearestProduct(activity, PolygonType.MPD, wxglSurfaceView)
                s.startsWith("Observation") -> UtilityRadarUI.getMetar(wxglSurfaceView, activity)
                s.startsWith("Forecast") -> UtilityRadarUI.showNearestForecast(activity, wxglSurfaceView)
                s.startsWith("Meteogram") -> UtilityRadarUI.showNearestMeteogram(activity, wxglSurfaceView)
            s.startsWith("Current Radar status message") -> UtilityRadarUI.getRadarStatus(activity, wxglRender)
                s.startsWith("Beam") -> {}
            //elys mod //need context....
            s.contains("Radar status message") -> UtilityRadarUI.showNearestRadarStatus(MyApplication.appContext, activity, wxglSurfaceView)
            s.contains("Spotter Info") -> UtilityRadarUI.showSpotterInfo(activity, wxglSurfaceView, MyApplication.appContext)
            s.contains("Userpoint info") -> UtilityRadarUI.showUserPointInfo(activity, MyApplication.appContext, wxglSurfaceView)
            s.contains("Add userpoint for") -> UtilityRadarUI.addUserPoint(activity, MyApplication.appContext, wxglSurfaceView)
            s.contains("Delete userpoint for") ->  UtilityRadarUI.deleteUserPoint(activity, MyApplication.appContext, wxglSurfaceView)
            s.contains("Delete all userpoints") -> UtilityRadarUI.deleteAllUserPoints(activity, MyApplication.appContext)
                else -> function(s)
            }
        }
    }
}
