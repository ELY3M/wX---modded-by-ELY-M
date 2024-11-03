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
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.objects.LatLon
import joshuatee.wx.radar.RID
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityLog
import java.util.Locale

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

    fun getNearestRadarSiteCode(location: LatLon): String {
        val officeArray = GlobalArrays.radars
//        val prefToken = OfficeTypeEnum.RADAR
//        if (officeType == OfficeTypeEnum.WFO) {
//            officeArray = GlobalArrays.wfos
//            prefToken = OfficeTypeEnum.WFO
//        }
        val sites = mutableListOf<RID>()
        officeArray.forEach {
            val labelArr = it.split(":")
            sites.add(
                RID(
                    labelArr[0],
                    getSiteLocation(labelArr[0]),
                    LatLon.distance(
                        location,
                        getSiteLocation(labelArr[0]),
                    )
                )
            )
        }
        sites.sortBy { it.distance }
        return sites[0].name
    }

    fun getNearestRadarSites(location: LatLon, count: Int, includeTdwr: Boolean = true): List<RID> {
        val radarSites = mutableListOf<RID>()
        GlobalArrays.radars.forEach {
            val labels = it.split(":")
            radarSites.add(
                RID(
                    labels[0],
                    getSiteLocation(labels[0]),
                    LatLon.distance(
                        location,
                        getSiteLocation(labels[0]),
                    )
                )
            )
        }
        if (includeTdwr) {
            GlobalArrays.tdwrRadars.forEach {
                val labels = it.split(":")
                radarSites.add(
                    RID(
                        labels[0],
                        getSiteLocation(labels[0]),
                        LatLon.distance(
                            location,
                            getSiteLocation(labels[0]),
                        )
                    )
                )
            }
        }
        radarSites.sortBy { it.distance }
        return radarSites.subList(0, count)
    }

//    fun getNearestSoundingSite(location: LatLon): String {
//        val sites = GlobalArrays.soundingSites.map {
//            RID(it, getSiteLocation(it, OfficeTypeEnum.SOUNDING), LatLon.distance(location, getSiteLocation(it, OfficeTypeEnum.SOUNDING), DistanceUnit.KM))
//        }.toMutableList()
//        sites.forEach {
//            it.distance = LatLon.distance(location, it.location, DistanceUnit.KM)
//        }
//        sites.sortBy { it.distance }
//        return sites[0].name
//    }

    fun getSiteLocation(site: String): LatLon {
        // SND, NWS, or RID
//        val addChar = if (officeType == OfficeTypeEnum.WFO) {
//            ""
//        } else {
//            "-"
//        }
        val addChar = "-"
        val x = getRadarSiteX(site.uppercase(Locale.US))
        val y = addChar + getRadarSiteY(site.uppercase(Locale.US))
//        when (officeType) {
//            OfficeTypeEnum.RADAR -> {
//                x = getRadarSiteX(site.uppercase(Locale.US))
//                y = addChar + getRadarSiteY(site.uppercase(Locale.US))
//            }
//
//            OfficeTypeEnum.WFO -> {
//                x = getWfoSiteX(site.uppercase(Locale.US))
//                y = addChar + getWfoSiteY(site.uppercase(Locale.US))
//            }
//
//            OfficeTypeEnum.SOUNDING -> {
////                x = getSoundingSiteX(site.uppercase(Locale.US))
////                y = addChar + getSoundingSiteY(site.uppercase(Locale.US))
//                val latLon = SoundingSites.sites.byCode[site.uppercase(Locale.US)]!!
//                x = latLon.lat
//                y = latLon.lon
//            }
//        }
        return LatLon(x, y)
    }

    fun getRadarSiteName(radarSite: String): String = RadarSites.name[radarSite] ?: ""

    fun getRadarSiteX(radarSite: String): String = RadarSites.lat[radarSite] ?: ""

    fun getRadarSiteY(radarSite: String): String = RadarSites.lon[radarSite] ?: ""

//    private fun getWfoSiteX(site: String): String = WfoSites.lat[site] ?: ""
//
//    private fun getWfoSiteY(site: String): String = WfoSites.lon[site] ?: ""

//    fun getWfoSiteName(wfo: String): String = WfoSites.names[wfo] ?: ""

//    private fun getSoundingSiteX(site: String): String = SoundingSites.lat[site] ?: ""
//
//    private fun getSoundingSiteY(site: String): String = SoundingSites.lon[site] ?: ""
//
//    fun getSoundingSiteName(wfo: String): String {
//        var site = WfoSites.name[wfo] ?: ""
//        if (site == "") {
//            site = SoundingSites.name[wfo] ?: ""
//        }
//        return site
//    }

    fun getNearest(latLon: LatLon, sectorToLatLon: Map<String, LatLon>): String {
        val sites = mutableListOf<RID>()
        sectorToLatLon.forEach { (k, v) ->
            sites.add(RID(k, v, LatLon.distance(latLon, v)))
        }
        sites.sortBy { it.distance }
        return sites[0].name
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
