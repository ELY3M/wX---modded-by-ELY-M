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
//modded by ELY M.  

package joshuatee.wx.radar

import android.content.Context
import com.beust.klaxon.*
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.util.UtilityLog
import java.io.StringReader
import java.util.regex.Pattern
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray


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
            //else -> MyApplication.severeDashboardSvr.valueGet()
            else -> "" //bug fix when svr warnings are struck and not expiring as it should have.
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
        UtilityLog.d("wx", "warningHTML: "+warningHTML)
        UtilityLog.d("wx", "warningList: "+warningList)
        return warningList
    }


    //SPS do not have VTEC
    var specialWeatherList = mutableListOf<SpecialWeather>()
    fun addSPS(
            context: Context,
            provider: ProjectionType,
            rid1: String,
            type: PolygonType
    ): List<Double> {
        val spsList = mutableListOf<Double>()
        val prefToken = when (type) {
            PolygonType.SPS -> MyApplication.severeDashboardSps.valueGet()
                //else -> MyApplication.severeDashboardSps.valueGet()
                else -> "" //bug fix to avoid "struck" polygons

        }

        //make sure we clear sps list first before
        spsList.clear()
        var count = 0

        fun getid(id: String): String {
            var getstring = ""
            val Matcher = object : PathMatcher {
                override fun pathMatches(path: String) = Pattern.matches(".*features\\[$id\\].properties.@id", path)
                override fun onMatch(path: String, value: Any) {
                    getstring = "$value"
                    UtilityLog.d("SpecialWeather", "$path = $value")

                }
            }

            Klaxon()
                    .pathMatcher(Matcher)
                    .parseJsonObject(StringReader(MyApplication.severeDashboardSps.valueGet()))

            return getstring
        }



        fun getcoordinates(id: String): String {
            var getstring = ""
            val document = Configuration.defaultConfiguration().jsonProvider().parse(MyApplication.severeDashboardSps.valueGet())
            //$.features[1].geometry.coordinates
            val getlatlons = JsonPath.read<JSONArray>(document, "$.features[$id].geometry.coordinates")
            UtilityLog.d("SpecialWeather","getlatlons["+id+"]: "+getlatlons)
            getstring = "\"coordinates\":"+getlatlons.toString().replace("\n", "").replace(" ", "")+"}"
            UtilityLog.d("SpecialWeather","getstring["+id+"]: "+getstring)
            return getlatlons.toString()
        }



        val polyMatcher = object : PathMatcher {
            //features[1].geometry.type
            override fun pathMatches(path: String) = Pattern.matches(".*features.*geometry.*type.*", path)
            override fun onMatch(path: String, value: Any) {
                count++
                UtilityLog.d("SpecialWeather","count: $count $path = $value")
                if (value == "Polygon") {
                    UtilityLog.d("SpecialWeather", "found polygon")
                    //we need to grab texts and latlons from json with polygon in it

                    val getid = path.parse("\\$\\.features\\[(.*?)\\]\\.geometry\\.type")
                    UtilityLog.d("SpecialWeather", "getid: "+getid)


                    specialWeatherList.add(
                            SpecialWeather(
                                    getid(getid),
                                    getcoordinates(getid)

                            )
                    )




                }
                }


        }




        try {

            Klaxon()
                    .pathMatcher(polyMatcher)
                    .parseJsonObject(StringReader(MyApplication.severeDashboardSps.valueGet()))

        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }


        val pn = ProjectionNumbers(context, rid1, provider)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        var spsHTML = ""
        try {
            spsHTML = prefToken.replace("\n", "").replace(" ", "")
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }


        //build SPS Polygons
        val polygonArr = spsHTML.parseColumn(RegExp.warningLatLonPattern)
        var polyCount = -1
        polygonArr.forEach { polygon ->
            polyCount += 1
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
                    var tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    spsList.add(tmpCoords[0])
                    spsList.add(tmpCoords[1])
                    if (x.size == y.size) {
                        j = 1
                        while (j < x.size) {
                            tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], pn)
                            spsList.add(tmpCoords[0])
                            spsList.add(tmpCoords[1])
                            spsList.add(tmpCoords[0])
                            spsList.add(tmpCoords[1])
                            j += 1
                        }
                        spsList.add(pixXInit)
                        spsList.add(pixYInit)
                    }
                }
        }
        return spsList
    }






}




