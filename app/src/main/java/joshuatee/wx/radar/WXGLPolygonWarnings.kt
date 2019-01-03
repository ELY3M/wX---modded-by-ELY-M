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
import org.json.JSONArray
import org.json.JSONObject
import java.io.StringReader
import java.util.regex.Pattern
import org.json.JSONException

import org.apache.commons.lang3.ArrayUtils;
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.GsonBuilder




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
    //This only make SPS Polygons for radar//
    var spsList = mutableListOf<SpecialWeatherList>()
    fun addSPS(
            context: Context,
            provider: ProjectionType,
            rid1: String,
            type: PolygonType
    ): List<Double> {
        val spsList = mutableListOf<Double>()
        val prefToken = when (type) {
            PolygonType.SPS -> MyApplication.severeDashboardSps.valueGet()
            else -> MyApplication.severeDashboardSps.valueGet()
        }


/*


2019-01-03 03:20:59.845 24519-25235/? D/SpecialWeather: $.features[3].geometry.type = Polygon
2019-01-03 03:20:59.849 24519-25235/? D/SpecialWeather: $.features[3].properties.@id = https://api.weather.gov/alerts/NWS-IDP-PROD-3291870
2019-01-03 03:20:59.850 24519-25235/? D/SpecialWeather: $.features[3].properties.@type = wx:Alert
2019-01-03 03:20:59.850 24519-25235/? D/SpecialWeather: $.features[3].properties.id = NWS-IDP-PROD-3291870
2019-01-03 03:20:59.850 24519-25235/? D/SpecialWeather: $.features[3].properties.areaDesc = Franklin; Jackson
2019-01-03 03:20:59.851 24519-25235/? D/SpecialWeather: $.features[3].properties.sent = 2018-12-31T16:28:00-06:00
2019-01-03 03:20:59.852 24519-25235/? D/SpecialWeather: $.features[3].properties.effective = 2018-12-31T16:28:00-06:00
2019-01-03 03:20:59.853 24519-25235/? D/SpecialWeather: $.features[3].properties.onset = 2018-12-31T16:28:00-06:00
2019-01-03 03:20:59.853 24519-25235/? D/SpecialWeather: $.features[3].properties.expires = 2018-12-31T17:15:00-06:00
2019-01-03 03:20:59.853 24519-25235/? D/SpecialWeather: $.features[3].properties.status = Actual
2019-01-03 03:20:59.854 24519-25235/? D/SpecialWeather: $.features[3].properties.messageType = Alert
2019-01-03 03:20:59.854 24519-25235/? D/SpecialWeather: $.features[3].properties.category = Met
2019-01-03 03:20:59.854 24519-25235/? D/SpecialWeather: $.features[3].properties.severity = Moderate
2019-01-03 03:20:59.854 24519-25235/? D/SpecialWeather: $.features[3].properties.certainty = Observed
2019-01-03 03:20:59.854 24519-25235/? D/SpecialWeather: $.features[3].properties.urgency = Expected
2019-01-03 03:20:59.855 24519-25235/? D/SpecialWeather: $.features[3].properties.event = Special Weather Statement
2019-01-03 03:20:59.855 24519-25235/? D/SpecialWeather: $.features[3].properties.sender = w-nws.webmaster@noaa.gov
2019-01-03 03:20:59.855 24519-25235/? D/SpecialWeather: $.features[3].properties.senderName = NWS Huntsville AL
2019-01-03 03:20:59.855 24519-25235/? D/SpecialWeather: $.features[3].properties.headline = Special Weather Statement issued December 31 at 4:28PM CST by NWS Huntsville AL
2019-01-03 03:20:59.855 24519-25235/? D/SpecialWeather: $.features[3].properties.description = At 428 PM CST, Doppler radar was tracking strong thunderstorms along



 */
            var count = 0
            //var geometry = ""
            var headline = ""
            var nwsheadline = ""
            var description = ""
            var latlon = ""

        /*
        val parser: Parser = Parser.default()
        val stringBuilder: StringBuilder = StringBuilder(MyApplication.severeDashboardSps.valueGet())
        val json: JsonObject = parser.parse(stringBuilder) as JsonObject
        UtilityLog.d("SpecialWeather","features: ${json.array<String>("features")}")
        UtilityLog.d("SpecialWeather","geometry: ${json.string("geometry")}")
        UtilityLog.d("SpecialWeather","properties: ${json.array<String>("properties")}")
        */

        fun getheadline(id: String): String {
            var getstring = ""
            val Matcher = object : PathMatcher {
                override fun pathMatches(path: String) = Pattern.matches(".*features\\[$id\\].properties.headline", path)
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

        fun getdescription(id: String): String {
            var getstring = ""
            val Matcher = object : PathMatcher {
                override fun pathMatches(path: String) = Pattern.matches(".*features\\[$id\\].properties.description", path)
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


        fun getlatlon(id: String): String {
            var getstring = ""
            val Matcher = object : PathMatcher {
                override fun pathMatches(path: String) = Pattern.matches(".*features\\[$id\\].geometry.*", path)
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

      //val array = parse(MyApplication.severeDashboardSps.valueGet()) as JsonArray<JsonObject>
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


                    getheadline(getid)
                    getdescription(getid)
                    getlatlon(getid)




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
        

        //val gson = Gson()
        //val test = gson.fromJson(MyApplication.severeDashboardSps.valueGet(), features::class.java)
        //UtilityLog.d("SpecialWeather","test: "+test)



/*
//add texts/lat lons in a list//
var spsText = MyApplication.severeDashboardSps.valueGet()
val urlList = spsText.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"")
UtilityLog.d("SpecialWeather", "urllist: "+urlList)

val sendername = spsText.parseColumn(RegExp.spsSenderName)
UtilityLog.d("SpecialWeather", "sendername: "+sendername)

val headline = spsText.parseColumn(RegExp.spsHeadLine)
UtilityLog.d("SpecialWeather", "headline: "+headline)

val nwsheadline = spsText.parseColumn(RegExp.spsNwsHeadLine)
UtilityLog.d("SpecialWeather", "nwsheadline: "+nwsheadline)

val description = spsText.parseColumn(RegExp.spsDescription)
UtilityLog.d("SpecialWeather", "description: "+description)

val instruction = spsText.parseColumn(RegExp.spsInstruction)
UtilityLog.d("SpecialWeather", "instruction: "+instruction)


try {
    spsText = prefToken.replace("\n", "").replace(" ", "")
} catch (e: OutOfMemoryError) {
    UtilityLog.HandleException(e)
}
val latlon = spsText.parseColumn(RegExp.warningLatLonPattern)
UtilityLog.d("SpecialWeather", "latlon: "+latlon)


//try to make a list
sendername.forEach {
    UtilityLog.d("SpecialWeather", "sendname: "+it)
}
headline.forEach {
    UtilityLog.d("SpecialWeather", "headline: "+it)
}

nwsheadline.forEach {
    UtilityLog.d("SpecialWeather", "nwsheadline: "+it)
}

description.forEach {
    UtilityLog.d("SpecialWeather", "description: "+it)
}

instruction.forEach {
    UtilityLog.d("SpecialWeather", "instruction: "+it)
}
latlon.forEach {
    UtilityLog.d("SpecialWeather", "latlon: "+it)
}

*/



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
        ///val vtecAl = spsHTML.parseColumn(RegExp.warningVtecPattern)
        var polyCount = -1
        polygonArr.forEach { polygon ->
            polyCount += 1
            //if (vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith("0.CAN")) {
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
            //} //vtec
        }
        return spsList
    }


}



class features2 {

    @SerializedName("geometry")
    private val geometry: String? = null
    @SerializedName("type")
    private val type: String? = null
    @SerializedName("coordinates")
    private val coordinates: DoubleArray? = null

}

/*
internal class features {
    var geometry: GeometryData? = null

    override fun toString(): String {
        return "Geometry [geometry=$geometry]"
    }
}


internal class GeometryData {
    var type: String? = null
    var coordinates: Array<Array<DoubleArray>>? = null

    override fun toString(): String {
        return "GeometryData [type=" + type + ", coordinates=" + ArrayUtils.toString(coordinates) + "]"
    }
}
*/




/*
class Coordinates {
    internal var latitude: Float = 0.toFloat()
    internal var longitude: Float = 0.toFloat()
}

class CoordinatesList1 {
    internal var list1: ArrayList<Coordinates>? = null
}

class CoordinatesList2 {
    internal var list2: ArrayList<CoordinatesList1>? = null
}

class Geometry {

    internal var type: String? = null

    internal var coordinates: ArrayList<CoordinatesList2>? = null
}
*/

/*
internal class Geometry {
    var geometry: GeometryInfo? = null

    override fun toString(): String {
        return "Geometry [geometry=$geometry]"
    }
}

internal class GeometryInfo {
    var type: String? = null
    var coordinates: Array<Array<DoubleArray>>? = null

    override fun toString(): String {
        return "GeometryInfo [type=" + type + ", coordinates=" + ArrayUtils.toString(coordinates) + "]"
    }
}
*/