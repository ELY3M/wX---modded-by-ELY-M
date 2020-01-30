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
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.util.UtilityTime

internal object WXGLPolygonWarnings {

    fun addGeneric(
            projectionType: ProjectionType,
            radarSite: String,
            objectPolygonWarning: ObjectPolygonWarning
    ): List<Double> {
        val warningList = mutableListOf<Double>()
        val prefToken = objectPolygonWarning.storage.value
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        val html = prefToken.replace("\n", "").replace(" ", "")
        val polygons = html.parseColumn(RegExp.warningLatLonPattern)
        val vtecs = html.parseColumn(RegExp.warningVtecPattern)
        var polyCount = -1
        polygons.forEach { polygon ->
            polyCount += 1
            if ( objectPolygonWarning.type == PolygonWarningType.SpecialWeatherStatement || (vtecs.size > polyCount && !vtecs[polyCount].startsWith("O.EXP") && !vtecs[polyCount].startsWith("O.CAN")  )
            ) {
                val polyTmp =
                        polygon.replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
                val testArr = polyTmp.split(" ")
                val y = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 == 0 }
                        .map {
                            it.toDoubleOrNull() ?: 0.0
                        }.toList()
                val x = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 != 0 }
                        .map {
                            it.toDoubleOrNull() ?: 0.0
                        }.toList()
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    var tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], projectionNumbers)
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    warningList.add(tmpCoords[0])
                    warningList.add(tmpCoords[1])
                    if (x.size == y.size) {
                        j = 1
                        while (j < x.size) {
                            tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], projectionNumbers)
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

    fun add(
            projectionType: ProjectionType,
            radarSite: String,
            polygonType: PolygonType
    ): List<Double> {
        val warningList = mutableListOf<Double>()
        val prefToken = when (polygonType) {
            PolygonType.TOR -> MyApplication.severeDashboardTor.value
            PolygonType.TST -> MyApplication.severeDashboardTst.value
            else -> MyApplication.severeDashboardFfw.value
        }
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        val html = prefToken.replace("\n", "").replace(" ", "")
        val polygons = html.parseColumn(RegExp.warningLatLonPattern)
        val vtecs = html.parseColumn(RegExp.warningVtecPattern)
        var polyCount = -1
        polygons.forEach { polygon ->
            polyCount += 1
            //val vtecIsCurrent = UtilityTime.isVtecCurrent(vtecAl[polyCount])
            if (vtecs.size > polyCount
                    && !vtecs[polyCount].startsWith("O.EXP")
                    && !vtecs[polyCount].startsWith("O.CAN")
                    && UtilityTime.isVtecCurrent(vtecs[polyCount])
            ) {
                val polyTmp = polygon.replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
                val testArr = polyTmp.split(" ")
                val y = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 == 0 }
                        .map {
                            it.toDoubleOrNull() ?: 0.0
                        }.toList()
                val x = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 != 0 }
                        .map {
                            it.toDoubleOrNull() ?: 0.0
                        }.toList()
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    var tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], projectionNumbers)
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    warningList.add(tmpCoords[0])
                    warningList.add(tmpCoords[1])
                    if (x.size == y.size) {
                        j = 1
                        while (j < x.size) {
                            tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], projectionNumbers)
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
}
