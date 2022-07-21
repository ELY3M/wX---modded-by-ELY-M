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

package joshuatee.wx.settings

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.radar.LatLon
import joshuatee.wx.radar.RID
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.util.*

object UtilityLocation {

    fun latLonAsDouble(): MutableList<Double> {
        val latLon = mutableListOf<Double>()
        (0 until joshuatee.wx.settings.Location.numLocations).forEach {
            val lat: String
            val lon: String
            if (!joshuatee.wx.settings.Location.getX(it).contains(":")) {
                lat = joshuatee.wx.settings.Location.getX(it)
                lon = joshuatee.wx.settings.Location.getY(it).replace("-", "")
            } else {
                val tmpXArr = joshuatee.wx.settings.Location.getX(it).split(":")
                lat = if (tmpXArr.size > 2) tmpXArr[2] else ""
                val tmpYArr = joshuatee.wx.settings.Location.getY(it).replace("-", "").split(":")
                lon = if (tmpYArr.size > 1) tmpYArr[1] else ""
            }
            latLon.add(To.double(lat))
            latLon.add(To.double(lon))
        }
        return latLon
    }

    fun getGps(context: Context): DoubleArray {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var location: Location? = null
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            for (i in providers.indices.reversed()) {
                location = locationManager.getLastKnownLocation(providers[i])
                if (location != null)
                    break
            }
        } else {
            UtilityLog.d("wx", "WARNING: permission not granted for roaming location")
        }
        val gps = DoubleArray(2)
        location?.let {
            gps[0] = it.latitude
            gps[1] = it.longitude
        }
        return gps
    }

    fun getNearestOffice(officeType: String, location: LatLon): String {
        var officeArray = GlobalArrays.radars
        var prefToken = "RID"
        if (officeType == "WFO") {
            officeArray = GlobalArrays.wfos
            prefToken = "NWS"
        }
        val sites = mutableListOf<RID>()
        officeArray.forEach {
            val labelArr = it.split(":")
            sites.add(RID(labelArr[0], getSiteLocation(labelArr[0], prefToken)))
        }
        sites.forEach {
            it.distance = LatLon.distance(location, it.location, DistanceUnit.KM)
        }
        sites.sortBy { it.distance }
        return sites[0].name
    }

    fun getNearestRadarSites(location: LatLon, count: Int, includeTdwr: Boolean = true): List<RID> {
        val radarSites = mutableListOf<RID>()
        GlobalArrays.radars.forEach {
            val labels = it.split(":")
            radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
        }
        if (includeTdwr) {
            GlobalArrays.tdwrRadars.forEach {
                val labels = it.split(" ")
                radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
            }
        }
        radarSites.forEach {
            it.distance = LatLon.distance(location, it.location, DistanceUnit.MILE)
        }
        radarSites.sortBy { it.distance }
        return radarSites.subList(0, count)
    }
    
    //elys mod
    fun getNearestRadarSite(location: LatLon): String {
        val radarSites = mutableListOf<RID>()
        GlobalArrays.radars.forEach {
            val labels = it.split(":")
            radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
        }
            GlobalArrays.tdwrRadars.forEach {
                val labels = it.split(" ")
                radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
            }
        var currentDistance: Double
        radarSites.forEach {
            currentDistance = LatLon.distance(location, it.location, DistanceUnit.MILE)
            it.distance = currentDistance
        }
//        Collections.sort(radarSites, RID.DESCENDING_COMPARATOR)
        radarSites.sortBy { it.distance }
        return radarSites[0].name
    }

    /*
    *
    //elys mod
    fun getNearestRadarSite(location: LatLon): String {
        val radarSites = mutableListOf<RID>()
        GlobalArrays.radars.forEach {
            val labels = it.split(":")
            radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
        }
        GlobalArrays.tdwrRadars.forEach {
            val labels = it.split(" ")
            radarSites.add(RID(labels[0], getSiteLocation(labels[0])))
        }
        var shortestDistance = 10000.00
        var currentDistance: Double
        var bestRid = -1
        radarSites.indices.forEach {
            currentDistance = LatLon.distance(location, radarSites[it].location, DistanceUnit.MILE)
            //it.distance = currentDistance.toInt()
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestRid = it
            }
        }
        if (bestRid == -1) return "NOTFOUND"
        return radarSites[bestRid].name
    }
    * */



    fun getNearestSoundingSite(location: LatLon): String {
        val sites = GlobalArrays.soundingSites.map { RID(it, getSiteLocation(it, "SND")) } as MutableList<RID>
        GlobalArrays.soundingSites.indices.forEach {
            sites[it].distance = LatLon.distance(location, sites[it].location, DistanceUnit.KM)
        }
        sites.sortBy { it.distance }
        return sites[0].name
    }

    fun getSiteLocation(site: String, officeType: String = "RID"): LatLon {
        // SND, NWS, or RID
        var addChar = "-"
        if (officeType == "NWS") {
            addChar = ""
        }
        val x: String
        val y: String
        when (officeType) {
            "RID" -> {
                x = Utility.getRadarSiteX(site.uppercase(Locale.US))
                y = addChar + Utility.getRadarSiteY(site.uppercase(Locale.US))
            }
            "NWS" -> {
                x = Utility.getWfoSiteX(site.uppercase(Locale.US))
                y = addChar + Utility.getWfoSiteY(site.uppercase(Locale.US))
            }
            "SND" -> {
                x = Utility.getSoundingSiteX(site.uppercase(Locale.US))
                y = addChar + Utility.getSoundingSiteY(site.uppercase(Locale.US))
            }
            else -> {
                x = "0.0"
                y = "-0.0"
            }
        }
        return LatLon(x, y)
    }

    fun hasAlerts(locNum: Int) = MyApplication.locations[locNum].notification
        || MyApplication.locations[locNum].notificationMcd
        || MyApplication.locations[locNum].ccNotification
        || MyApplication.locations[locNum].sevenDayNotification
        || MyApplication.locations[locNum].notificationSpcFw
        || MyApplication.locations[locNum].notificationSwo
        || MyApplication.locations[locNum].notificationWpcMpd
}
