/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.external.ExternalPolygon

internal object UtilityWatch {

    fun add(projectionNumbers: ProjectionNumbers, polygonType: PolygonType): List<Double> {
        val warningList = mutableListOf<Double>()
        val prefToken = when (polygonType) {
            PolygonType.MCD -> MyApplication.mcdLatLon.value
            PolygonType.WATCH -> MyApplication.watchLatLon.value
            PolygonType.WATCH_TORNADO -> MyApplication.watchLatLonTor.value
            PolygonType.MPD -> MyApplication.mpdLatLon.value
            else -> ""
        }
        if (prefToken != "") {
            val polygons = prefToken.split(":").dropLastWhile { it.isEmpty() }
            polygons.forEach { polygon ->
                val latLons = LatLon.parseStringToLatLons(polygon, 1.0, false)
                warningList += LatLon.latLonListToListOfDoubles(latLons, projectionNumbers)
            }
        }
        return warningList
    }

    fun show(latLon: LatLon, type: PolygonType): String {
        val numberList: List<String>
        val watchLatLon: String
        when (type) {
            PolygonType.WATCH -> {
                numberList = MyApplication.watchNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.watchLatLonList.value
            }
            PolygonType.MCD -> {
                numberList = MyApplication.mcdNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.mcdLatLon.value
            }
            PolygonType.MPD -> {
                numberList = MyApplication.mpdNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.mpdLatLon.value
            }
            else -> {
                numberList = MyApplication.watchNoList.value.split(":").dropLastWhile { it.isEmpty() }
                watchLatLon = MyApplication.watchLatLonList.value
            }
        }
        val polygons = watchLatLon.split(":").dropLastWhile { it.isEmpty() }
        var notFound = true
        var text = ""
        polygons.indices.forEach { z ->
            val latLons = LatLon.parseStringToLatLons(polygons[z],-1.0, false)
            if (latLons.isNotEmpty()) {
                val contains = ExternalPolygon.polygonContainsPoint(latLon, latLons)
                if (contains && notFound) {
                    text = numberList[z]
                    notFound = false
                }
            }
        }
        return text
    }
}

