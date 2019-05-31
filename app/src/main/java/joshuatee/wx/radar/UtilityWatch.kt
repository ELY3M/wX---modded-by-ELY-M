/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon

internal object UtilityWatch {

    fun add(
        provider: ProjectionType,
        radarSite: String,
        type: PolygonType
    ): List<Double> {
        var testArr: Array<String>
        val warningList = mutableListOf<Double>()
        var prefToken = ""
        when (type) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatlon.value
            PolygonType.WATCH -> prefToken = MyApplication.watchLatlon.value
            PolygonType.WATCH_TORNADO -> prefToken = MyApplication.watchLatlonTor.value
            PolygonType.MPD -> prefToken = MyApplication.mpdLatlon.value
            else -> {
            }
        }
        val pn = ProjectionNumbers(radarSite, provider)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        val textFfw = prefToken
        if (textFfw != "") {
            val tmpArr = MyApplication.colon.split(textFfw)
            tmpArr.forEach { it ->
                testArr = MyApplication.space.split(it)
                val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    var tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    warningList.add(tmpCoords[0])
                    warningList.add(tmpCoords[1])
                    if (x.size == y.size) {
                        j = 1
                        while (j < x.size) {
                            tmpCoords =
                                UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], pn)
                            warningList.add(tmpCoords[0])
                            warningList.add(tmpCoords[1])
                            warningList.add(tmpCoords[0])
                            warningList.add(tmpCoords[1])
                            j += 1
                        }
                        warningList.add(pixXInit)
                        warningList.add(pixYInit)
                    }
                }
            }
        }
        return warningList
    }

    fun show(lat: Double, lon: Double, type: PolygonType): String {
        var text = ""
        val textWatNoList: String
        val mcdNoArr: Array<String>
        val watchLatLon: String
        when (type) {
            PolygonType.WATCH -> {
                textWatNoList = MyApplication.watchNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.watchLatlonList.value
            }
            PolygonType.MCD -> {
                textWatNoList = MyApplication.mcdNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.mcdLatlon.value
            }
            PolygonType.MPD -> {
                textWatNoList = MyApplication.mpdNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.mpdLatlon.value
            }
            else -> {
                textWatNoList = MyApplication.watchNoList.value
                mcdNoArr = MyApplication.colon.split(textWatNoList)
                watchLatLon = MyApplication.watchLatlonList.value
            }
        }
        val latLonArr = MyApplication.colon.split(watchLatLon)
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()
        var i: Int
        var testArr: List<String>
        var z = 0
        var notFound = true
        while (z < latLonArr.size) {
            testArr = latLonArr[z].split(" ")
            x.clear()
            y.clear()
            i = 0
            while (i < testArr.size) {
                if (i and 1 == 0) {
                    x.add(testArr[i].toDoubleOrNull() ?: 0.0)
                } else {
                    y.add((testArr[i].toDoubleOrNull() ?: 0.0) * -1)
                }
                i += 1
            }
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                for (j in x.indices) {
                    poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                    text = mcdNoArr[z]
                    notFound = false
                }
            }
            z += 1

        }
        return text
    }
}

