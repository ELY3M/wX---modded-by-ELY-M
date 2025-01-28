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

package joshuatee.wx.settings

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.Site
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import kotlin.math.roundToInt

object UtilityLocation {

    fun latLonAsDouble(): List<Double> {
        val latLon = mutableListOf<Double>()
        (0 until joshuatee.wx.settings.Location.numLocations).forEach {
            val lat: String
            val lon: String
            if (!joshuatee.wx.settings.Location.getX(it).contains(":")) {
                lat = joshuatee.wx.settings.Location.getX(it)
                lon = joshuatee.wx.settings.Location.getY(it).replace("-", "")
            } else {
                val tmpXArr = joshuatee.wx.settings.Location.getX(it).split(":")
                lat = if (tmpXArr.size > 2) {
                    tmpXArr[2]
                } else {
                    ""
                }
                val tmpYArr = joshuatee.wx.settings.Location.getY(it).replace("-", "").split(":")
                lon = if (tmpYArr.size > 1) {
                    tmpYArr[1]
                } else {
                    ""
                }
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
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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

    fun getNearest(latLon: LatLon, sectorToLatLon: Map<String, LatLon>): String {
        val sites = mutableListOf<Site>()
        sectorToLatLon.forEach { (k, v) ->
            sites.add(Site.fromLatLon(k, v, LatLon.distance(latLon, v)))
        }
        sites.sortBy { it.distance }
        return sites[0].codeName
    }

    fun getNearestCity(context: Context, latLon: LatLon): String {
        val cityData =
            UtilityIO.rawFileToStringArrayFromResource(context.resources, R.raw.cityall)
        val cityToLatlon = mutableMapOf<String, LatLon>()
        for (line in cityData) {
            val items = line.split(",")
            if (items.size > 4) {
//                if (cityToLatlon.contains(items[0])) {
//                }
                if (To.int(items[4]) > 1000) {
                    cityToLatlon[items[1]] = LatLon(items[2], items[3])
                }
            }
        }
        val sites = mutableListOf<Site>()
        for (m in cityToLatlon) {
            sites.add(Site.fromLatLon(m.key, m.value, LatLon.distance(latLon, m.value)))
        }
        sites.sortBy { it.distance }
        val bearingToCity = LatLon.calculateDirection(latLon, cityToLatlon[sites[0].codeName]!!)
        val distanceToCIty = LatLon.distance(latLon, cityToLatlon[sites[0].codeName]!!).roundToInt()
        return sites[0].codeName + " is " + To.string(distanceToCIty) + " miles to the " + bearingToCity
    }

    fun hasAlerts(locNum: Int): Boolean =
        joshuatee.wx.settings.Location.locations[locNum].notification
                || joshuatee.wx.settings.Location.locations[locNum].notificationMcd
                || joshuatee.wx.settings.Location.locations[locNum].ccNotification
                || joshuatee.wx.settings.Location.locations[locNum].sevenDayNotification
                || joshuatee.wx.settings.Location.locations[locNum].notificationSpcFw
                || joshuatee.wx.settings.Location.locations[locNum].notificationSwo
                || joshuatee.wx.settings.Location.locations[locNum].notificationWpcMpd
}
