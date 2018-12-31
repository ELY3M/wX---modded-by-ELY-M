/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.util.UtilityLog

internal object WXGLPolygonWarnings {

    fun addWarnings(
        context: Context,
        provider: ProjectionType,
        rid1: String,
        type: PolygonType
    ): List<Double> {
        val warningList = mutableListOf<Double>()
        val prefToken = when (type) {
            PolygonType.TOR -> MyApplication.severeDashboardTor.valueGet()
            PolygonType.SVR -> MyApplication.severeDashboardSvr.valueGet()
            PolygonType.EWW -> MyApplication.severeDashboardEww.valueGet()
            PolygonType.FFW -> MyApplication.severeDashboardFfw.valueGet()
            PolygonType.SMW -> MyApplication.severeDashboardSmw.valueGet()
            ///PolygonType.SPS -> MyApplication.severeDashboardSps.valueGet()
            else -> MyApplication.severeDashboardSvr.valueGet()
        }
        val pn = ProjectionNumbers(context, rid1, provider)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        var warningHTML = ""
        try {
            warningHTML = prefToken.replace("\n", "").replace(" ", "")
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
        val polygonArr = warningHTML.parseColumn(RegExp.warningLatLonPattern)
        val vtecAl = warningHTML.parseColumn(RegExp.warningVtecPattern)
        var polyCount = -1
        polygonArr.forEach { polygon ->
            polyCount += 1
            if (vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith(
                    "0.CAN"
                )
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


    //SPS do not have VTEC
    fun addSPS(
            context: Context,
            provider: ProjectionType,
            rid1: String,
            type: PolygonType
    ): List<Double> {
        val warningList = mutableListOf<Double>()
        val prefToken = when (type) {
            PolygonType.SPS -> MyApplication.severeDashboardSps.valueGet()
            else -> MyApplication.severeDashboardSps.valueGet()
        }

        UtilityLog.d("SpecialWeather", "SPS: "+MyApplication.severeDashboardSps.valueGet())


        val pn = ProjectionNumbers(context, rid1, provider)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        var warningHTML = ""
        try {
            warningHTML = prefToken.replace("\n", "").replace(" ", "")
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
        val polygonArr = warningHTML.parseColumn(RegExp.warningLatLonPattern)
        ///val vtecAl = warningHTML.parseColumn(RegExp.warningVtecPattern)
        var polyCount = -1
        polygonArr.forEach { polygon ->
            polyCount += 1
            //if (vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith("0.CAN")) {
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
            //} //vtec
        }
        return warningList
    }

}
