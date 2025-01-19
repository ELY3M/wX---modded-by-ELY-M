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

package joshuatee.wx.objects

import joshuatee.wx.util.UtilityLog

class Sites(
    private val nameDict: Map<String, String>,
    private val latDict: Map<String, String>,
    private val lonDict: Map<String, String>,
    lonReversed: Boolean = false
) {

    val sites = mutableListOf<Site>()
    val byCode = mutableMapOf<String, Site>()
    val codeList = mutableListOf<String>()
    val nameList = mutableListOf<String>()

    init {
        checkValidityMaps()

        nameDict.forEach { (key, value) ->
            sites.add(Site(key, value, latDict[key]!!, lonDict[key]!!, lonReversed))
            byCode[key] = sites.last()
        }

        sites.sortBy { it.fullName }

        sites.forEach { site ->
            codeList.add(site.codeName)
            nameList.add("${site.codeName}: ${site.fullName}")
        }
    }

    private fun checkValidityMaps() {
        val k1 = nameDict.keys
        val k2 = latDict.keys
        val k3 = lonDict.keys
        if (k1 != k2) {
            UtilityLog.d(
                "wX",
                "mismatch between names and lat " + (k2 - k1).toString() + " " + (k1 - k2).toString()
            )
        }
        if (k1 != k3) {
            UtilityLog.d(
                "wX",
                "mismatch between names and lon" + (k3 - k1).toString() + " " + (k1 - k3).toString()
            )
        }
    }

    fun getNearest(latLon: LatLon): String {
        for (site in sites) {
            site.distance = LatLon.distance(latLon, site.latLon).toInt()
        }
        sites.sortBy { it.distance }
        return sites[0].codeName
    }

    fun getNearestSite(latLon: LatLon, order: Int = 0): Site {
        for (site in sites) {
            site.distance = LatLon.distance(latLon, site.latLon).toInt()
        }
        sites.sortBy { it.distance }
        return sites[order]
    }

    // FIXME TODO
//    fun getNearestList(latLon: LatLon, count: Int = 5): List<String> {
//        for (site in sites) {
//            site.distance = LatLon.distance(latLon, site.latLon).toInt()
//        }
////        self.sites.sort(key=lambda x: x.distance, reverse=False)
//        sites.sortBy { it.distance }
//        return [site.codeName for site in self.sites[0:count]]
//    }

    fun getNearestInMiles(latLon: LatLon): Int {
        for (site in sites) {
            site.distance = LatLon.distance(latLon, site.latLon).toInt()
        }
        sites.sortBy { it.distance }
        return sites[0].distance
    }
}

