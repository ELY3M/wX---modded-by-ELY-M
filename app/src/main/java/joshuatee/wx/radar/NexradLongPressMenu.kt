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
import joshuatee.wx.Extensions.insert
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.objects.OfficeTypeEnum
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMath
import kotlin.math.roundToInt
//elys mod
import joshuatee.wx.MyApplication //elys mod //need context...

class NexradLongPressMenu(val activity: Activity, val nexradState: NexradState, nexradArguments: NexradArguments, longPressFunction: (String) -> Unit) {

    private val radarLongPressItems = mutableListOf<String>()
    private var longPressDialogue = ObjectDialogue(activity, radarLongPressItems)

    init {
        longPressDialogue.connectCancel { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(activity)
        }
        longPressDialogue.connect { dialog, itemIndex ->
            doLongPressAction(activity,
                    radarLongPressItems[itemIndex],
                    nexradState.surface.latLon,
                    nexradState.render.state.closestRadarSites.first().name,
                    longPressFunction
            )
            dialog.dismiss()
        }
    }

    val changeListener = object : NexradRenderSurfaceView.OnProgressChangeListener {
        override fun onProgressChanged(progress: Int, idx: Int, idxInt: Int) {
            if (progress != 50000) {
                with (nexradState) {
                    if (numberOfPanes > 1) {
                        curRadar = idxInt
                    }
                    if (isHomeScreen) {
                        curRadar = idx
                    }
                    setupContextMenu(
                        activity,
                        radarLongPressItems,
                        nexradArguments.locXCurrent,
                        nexradArguments.locYCurrent,
                        surface.latLon,
                        render.wxglNexradLevel3,
                        render.state.rid,
                        render.state.closestRadarSites,
                        longPressDialogue
                    )
                }
            } else {
                nexradState.wxglTextObjects.forEach {
                    it.addLabels()
                }
            }
        }
    }

    companion object {

        fun setupContextMenu(
                context: Context,
                longPressList: MutableList<String>,
                locX: Double,
                locY: Double,
                latLon: LatLon,
                wxglNexradLevel3: NexradLevel3,
                radarSite: String,
                closestRadarSites: List<RID>,
                longPressDialogue: ObjectDialogue
        ) {
            longPressList.clear()
            val dist = LatLon.distance(LatLon(locX, locY), latLon, DistanceUnit.MILE)
//            val ridX = To.double(UtilityLocation.getRadarSiteX(radarSite))
//            val ridY = -1.0 * To.double(UtilityLocation.getRadarSiteY(radarSite))
//            val distRid = LatLon.distance(LatLon(ridX, ridY), latLon, DistanceUnit.MILE)
//            val distRidKm = LatLon.distance(LatLon(ridX, ridY), latLon, DistanceUnit.KM)
            val radarSiteLatLon = UtilityLocation.getSiteLocation(radarSite, OfficeTypeEnum.RADAR)
            val distRid = LatLon.distance(radarSiteLatLon, latLon, DistanceUnit.MILE)
            val distRidKm = LatLon.distance(radarSiteLatLon, latLon, DistanceUnit.KM)
            val latLonTitle = latLon.prettyPrint()
            longPressDialogue.setTitle(latLonTitle)
            longPressList.add("${dist.toString().take(6)} miles from location")
            longPressList.add("${distRid.toString().take(6)} miles from $radarSite")
            val heightAgl = UtilityMath.getRadarBeamHeight(wxglNexradLevel3.degree.toDouble(), distRidKm)
            val heightMsl = wxglNexradLevel3.radarHeight + heightAgl
            //elys mod - WIP
	    val dbz = ""
            val windSpeed = ""
            longPressList.add("Beam Height MSL: ${heightMsl.roundToInt()} ft, AGL: ${heightAgl.roundToInt()} ft")
            if (PolygonType.WPC_FRONTS.pref) {
                longPressList.add("WPC Fronts: ${getWpcFrontTimeStamp(context)}")
            }
            longPressList += closestRadarSites.map {
                "${it.name} ${UtilityLocation.getRadarSiteName(it.name)} ${it.distance} mi"
            }
            //elys mod
	    if ((RadarPreferences.warnings || PolygonWarning.areAnyEnabled()) /*&& PolygonWarning.isCountNonZero()*/) {
                longPressList.add("Show Warning")
            }
            // Thanks to Ely
            if (RadarPreferences.watMcd && PolygonWatch.watchLatlonCombined.value != "") {
                longPressList.add("Show Watch")
            }
            if (RadarPreferences.watMcd && PolygonWatch.byType[PolygonType.MCD]!!.latLonList.value != "") {
                longPressList.add("Show MCD")
            }
            if (RadarPreferences.mpd && PolygonWatch.byType[PolygonType.MPD]!!.latLonList.value != "") {
                longPressList.add("Show MPD")
            }
            // end Thanks to Ely
            val obsSite = Metar.findClosestObsSite(context, latLon)
            //elys mod
            longPressList.add("Radar Mosaic")
            longPressList.add("GOES Satellite")
            ///
            longPressList.add("Observation: " + obsSite.name + " (" + obsSite.distance + " mi)")
            longPressList.add("Forecast: $latLonTitle")
            longPressList.add("Meteogram: " + obsSite.name)
            longPressList.add("Radar status message: " + closestRadarSites.first().name)
            //elys mod
            if (RadarPreferences.spotters || RadarPreferences.spottersLabel) longPressList.add("Spotter Info")
	        longPressList.add("Userpoint info: " + latLonTitle)
            longPressList.add("Add userpoint for: " + latLonTitle)
            longPressList.add("Delete userpoint for: " + latLonTitle)
            longPressList.add("Delete all userpoints")
            longPressDialogue.show()
        }

        private fun getWpcFrontTimeStamp(context: Context): String {
            var timeStamp = Utility.readPref(context,"WPC_FRONTS_TIMESTAMP", "")
                                    .replace(ObjectDateTime.getYear().toString(), "")
            if (timeStamp.length > 6) {
                timeStamp = timeStamp.insert(4, " ")
            }
            return timeStamp
        }

        fun doLongPressAction(activity: Activity, s: String, latLon: LatLon, radarSite: String, function: (String) -> Unit) {
            when {
                s.contains("miles from") -> {}
                s.contains("Show Warning") -> NexradRenderUI.showNearestWarning(activity, latLon)
                s.contains("Show Watch") -> NexradRenderUI.showNearestMcd(activity, PolygonType.WATCH, latLon)
                s.contains("Show MCD") -> NexradRenderUI.showNearestMcd(activity, PolygonType.MCD, latLon)
                s.contains("Show MPD") -> NexradRenderUI.showNearestMcd(activity, PolygonType.MPD, latLon)
                //elys mod
                s.contains("Radar Mosaic") -> Route.radarMosaicConus(activity)
                s.contains("GOES Satellite") -> Route.vis00(activity)
                ///
                s.startsWith("Observation") -> NexradRenderUI.showMetar(activity, latLon)
                s.startsWith("Forecast") -> Route.forecast(activity, latLon)
                s.startsWith("Meteogram") -> NexradRenderUI.showNearestMeteogram(activity, latLon)
                s.startsWith("Radar status message") -> NexradRenderUI.showRadarStatus(activity, radarSite)
                s.startsWith("Beam") -> {}
            	//elys mod //need context....
            	s.contains("Spotter Info") -> NexradRenderUI.showSpotterInfo(activity, latLon, MyApplication.appContext)
            	s.contains("Userpoint info") -> NexradRenderUI.showUserPointInfo(activity, MyApplication.appContext, latLon)
            	s.contains("Add userpoint for") -> NexradRenderUI.addUserPoint(activity, MyApplication.appContext, latLon)
            	s.contains("Delete userpoint for") ->  NexradRenderUI.deleteUserPoint(activity, MyApplication.appContext, latLon)
            	s.contains("Delete all userpoints") -> NexradRenderUI.deleteAllUserPoints(activity, MyApplication.appContext)
                else -> function(s)
            }
        }
    }
}
