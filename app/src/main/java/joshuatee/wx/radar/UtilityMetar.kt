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

import android.content.Context
import android.graphics.Color

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityMath
import joshuatee.wx.settings.UtilityLocation

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.objects.DistanceUnit

internal object UtilityMetar {

    private var initialized = false
    private var initializedObsMap = false
    var obsArr = listOf<String>()
    var obsArrExt = listOf<String>()
    var obsArrWb = listOf<String>()
    var x = DoubleArray(1)
        private set
    var y = DoubleArray(1)
        private set
    var obsArrWbGust = listOf<String>()
    var obsArrAviationColor = listOf<Int>()
    private var obsStateOld = ""
    private var lastRefresh = 0.toLong()
    private const val REFRESH_LOC_MIN = 5
    private val OBS_LATLON = mutableMapOf<String, Array<String>>()

    fun getStateMetarArrayForWXOGL(context: Context, rid: String) {
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized || rid != obsStateOld) {
            val obsAl = mutableListOf<String>()
            val obsAlExt = mutableListOf<String>()
            val obsAlWb = mutableListOf<String>()
            val obsAlWbGust = mutableListOf<String>()
            val obsAlX = mutableListOf<Double>()
            val obsAlY = mutableListOf<Double>()
            val obsAlAviationColor = mutableListOf<Int>()
            obsStateOld = rid
            val obsList = getObservationSites(context, rid)
            // https://www.aviationweather.gov/metar/data?ids=KDTW%2CKARB&format=raw&date=&hours=0
            val html =
                "${MyApplication.nwsAWCwebsitePrefix}/adds/metars/index?submit=1&station_ids=$obsList&chk_metars=on".getHtml()
            val metarArrTmp =
                    html.parseColumn("<FONT FACE=\"Monospace,Courier\">(.*?)</FONT><BR>")
            val metarArr = condenseObs(metarArrTmp)
            if (!initializedObsMap) {
                val xmlFileInputStream = context.resources.openRawResource(R.raw.us_metar3)
                val text = UtilityIO.readTextFile(xmlFileInputStream)
                val lines = text.split("\n").dropLastWhile { it.isEmpty() }
                var tmpArr: List<String>
                lines.forEach {
                    tmpArr = it.split(" ")
                    OBS_LATLON[tmpArr[0]] = arrayOf(tmpArr[1], tmpArr[2])
                }
                initializedObsMap = true
            }
            var tmpArr2: Array<String>
            var obsSite: String
            var tmpBlob: String
            var pressureBlob: String
            var windBlob: String
            var conditionsBlob: String
            var timeBlob = ""
            var visBlob: String
            var visBlobDisplay: String
            var visBlobArr: Array<String>
            var visInt: Int
            var aviationColor: Int
            var tdArr: Array<String>
            var t: String
            var d: String
            var latlon: Array<String>
            var windDir = ""
            var windInKt = ""
            var windgustInKt = ""
            var windDirD: Double
            var validWind: Boolean
            var validWindGust: Boolean
            metarArr.forEach { z ->
                validWind = false
                validWindGust = false
                if ((z.startsWith("K") || z.startsWith("P")) && !z.contains("NIL")) {
                    tmpArr2 = MyApplication.space.split(z)
                    tmpBlob = z.parse(RegExp.patternMetarWxogl1) // ".*? (M{0,1}../M{0,1}..) .*?"
                    tdArr = MyApplication.slash.split(tmpBlob)
                    if (tmpArr2.size > 1) {
                        timeBlob = tmpArr2[1]
                    }
                    pressureBlob = z.parse(RegExp.patternMetarWxogl2) // ".*? A([0-9]{4})"
                    windBlob = z.parse(RegExp.patternMetarWxogl3) // "AUTO ([0-9].*?KT) .*?"
                    if (windBlob == "") {
                        windBlob = z.parse(RegExp.patternMetarWxogl4)
                    }
                    conditionsBlob =
                        z.parse(RegExp.patternMetarWxogl5) // "SM (.*?) M{0,1}[0-9]{2}/"
                    visBlob = z.parse(" ([0-9].*?SM) ")
                    visBlobArr = MyApplication.space.split(visBlob)
                    visBlobDisplay = visBlobArr[visBlobArr.size - 1]
                    visBlob = visBlobArr[visBlobArr.size - 1].replace("SM", "")
                    // might have 1/2 or 1/4 , just call it zero
                    visInt = when {
                        visBlob.contains("/") -> 0
                        visBlob != "" -> visBlob.toIntOrNull() ?: 0
                        else -> 20000
                    }
                    // ceiling can be deduced from the lowest height with broken (BKN) or overcast (OVC) reported.
                    var ovcStr = conditionsBlob.parse("OVC([0-9]{3})")
                    var bknStr = conditionsBlob.parse("BKN([0-9]{3})")
                    var ovcInt = 100000
                    var bknInt = 100000
                    val lowestCig: Int
                    if (ovcStr != "") {
                        ovcStr += "00"
                        ovcInt = ovcStr.toIntOrNull() ?: 0
                    }
                    if (bknStr != "") {
                        bknStr += "00"
                        bknInt = bknStr.toIntOrNull() ?: 0
                    }
                    lowestCig = if (bknInt < ovcInt) {
                        bknInt
                    } else {
                        ovcInt
                    }
                    aviationColor = Color.GREEN
                    if (visInt > 5 && lowestCig > 3000) {
                        aviationColor = Color.GREEN
                    }
                    if (visInt in 3..5 || lowestCig in 1000..3000) {
                        aviationColor = Color.rgb(0, 100, 255)
                    }
                    if (visInt in 1..2 || lowestCig in 500..999) {
                        aviationColor = Color.RED
                    }
                    if (visInt < 1 || lowestCig < 500) {
                        aviationColor = Color.MAGENTA
                    }
                    //  green, blue, red, and purple
                    // VFR 	> 5 mi 	and > 3000 ft AGL
                    // Marginal VFR 	Between 3 and 5 mi 	and/or Between 1,000 and 3,000 ft AGL
                    // IFR 	1 mi or more but less than 3 mi 	and/or 500 ft or more but less than 1,000 ft
                    // Low IFR 	< 1 mi 	and/or < 500 ft
                    if (pressureBlob.length == 4) {
                        pressureBlob =
                            StringBuilder(pressureBlob).insert(pressureBlob.length - 2, ".")
                                .toString()
                        pressureBlob = UtilityMath.unitsPressure(pressureBlob)
                    }
                    // 19011G16KT
                    // 18011KT
                    if (windBlob.contains("KT") && windBlob.length == 7) {
                        validWind = true
                        windDir = windBlob.substring(0, 3)
                        windInKt = windBlob.substring(3, 5)
                        windDirD = windDir.toDoubleOrNull() ?: 0.0
                        windBlob = windDir + " (" + UtilityMath.convertWindDir(windDirD) + ") " +
                                windInKt + " kt"
                    } else if (windBlob.contains("KT") && windBlob.length == 10) {
                        validWind = true
                        validWindGust = true
                        windDir = windBlob.substring(0, 3)
                        windInKt = windBlob.substring(3, 5)
                        windgustInKt = windBlob.substring(6, 8)
                        windDirD = windDir.toDoubleOrNull() ?: 0.0
                        windBlob = windDir + " (" + UtilityMath.convertWindDir(windDirD) + ") " +
                                windInKt + " G " + windgustInKt + " kt"
                    }
                    if (tdArr.size > 1) {
                        t = tdArr[0]
                        d = tdArr[1]
                        t = UtilityMath.cTof(t.replace("M", "-"))
                        d = UtilityMath.cTof(d.replace("M", "-"))
                        obsSite = tmpArr2[0]
                        latlon = OBS_LATLON[obsSite] ?: arrayOf("0.0", "0.0")
                        if (latlon[0] != "0.0") {
                            obsAl.add(latlon[0] + ":" + latlon[1] + ":" + t + "/" + d)
                            obsAlExt.add(
                                latlon[0] + ":" + latlon[1] + ":" + t + "/" + d + " (" + obsSite + ")"
                                        + MyApplication.newline + pressureBlob + " - " + visBlobDisplay
                                        + MyApplication.newline + windBlob
                                        + MyApplication.newline + conditionsBlob
                                        + MyApplication.newline + timeBlob
                            )
                            try {
                                if (validWind) {
                                    obsAlWb.add(latlon[0] + ":" + latlon[1] + ":" + windDir + ":" + windInKt)
                                    val x = latlon[0].toDoubleOrNull() ?: 0.0
                                    val y = (latlon[1].toDoubleOrNull() ?: 0.0) * -1.0
                                    obsAlX.add(x)
                                    obsAlY.add(y)
                                    obsAlAviationColor.add(aviationColor)
                                }
                                if (validWindGust) {
                                    obsAlWbGust.add(latlon[0] + ":" + latlon[1] + ":" + windDir + ":" + windgustInKt)
                                }
                            } catch (e: Exception) {
                                UtilityLog.HandleException(e)
                            }
                        }
                    }
                }
            }
            obsArr = obsAl.toList()
            obsArrExt = obsAlExt.toList()
            obsArrWb = obsAlWb.toList()
            x = DoubleArray(obsAlX.size)
            obsAlX.indices.forEach { x[it] = obsAlX[it] }
            y = DoubleArray(obsAlY.size)
            obsAlY.indices.forEach { y[it] = obsAlY[it] }
            obsArrWbGust = obsAlWbGust.toList()
            obsArrAviationColor = obsAlAviationColor.toList()
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
        }
    }

    fun findClosestMetar(context: Context, location: LatLon): String {
        val xmlFileInputStream = context.resources.openRawResource(R.raw.us_metar3)
        val text = UtilityIO.readTextFile(xmlFileInputStream)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val metarSites = mutableListOf<RID>()
        lines.indices.forEach {
            val tmpArr = lines[it].split(" ")
            metarSites.add(RID(tmpArr[0], LatLon(tmpArr[1], tmpArr[2])))
        }
        var shortestDistance = 1000.00
        var currentDistance: Double
        var bestRid = -1
        metarSites.indices.forEach {
            currentDistance = LatLon.distance(location, metarSites[it].location, DistanceUnit.KM)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestRid = it
            }
        }
        // http://weather.noaa.gov/pub/data/observations/metar/decoded/KCSV.TXT
        return if (bestRid == -1) {
            "Please select a location in the United States."
        } else {
            (MyApplication.NWS_RADAR_PUB + "data/observations/metar/decoded/" + metarSites[bestRid].name + ".TXT").getHtmlSep()
                .replace("<br>", MyApplication.newline)
        }
    }

    fun findClosestObservation(context: Context, location: LatLon): RID {
        val xmlFileInputStream = context.resources.openRawResource(R.raw.us_metar3)
        val text = UtilityIO.readTextFile(xmlFileInputStream)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val metarSites = mutableListOf<RID>()
        lines.indices.forEach {
            val tmpArr = lines[it].split(" ")
            metarSites.add(RID(tmpArr[0], LatLon(tmpArr[1], tmpArr[2])))
        }
        var shortestDistance = 1000.00
        var currentDistance: Double
        var bestRid = -1
        metarSites.indices.forEach {
            currentDistance = LatLon.distance(location, metarSites[it].location, DistanceUnit.KM)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestRid = it
            }
        }
        return if (bestRid == -1) {
            metarSites[0]
        } else {
            metarSites[bestRid]
        }
    }

    private fun getObservationSites(context: Context, rid: String): String {
        val radarLocation = UtilityLocation.getSiteLocation(context, rid)
        val obsListSb = StringBuilder(100)
        val xmlFileInputStream = context.resources.openRawResource(R.raw.us_metar3)
        val text = UtilityIO.readTextFile(xmlFileInputStream)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val obsSites = mutableListOf<RID>()
        lines.forEach {
            val tmpArr = it.split(" ")
            obsSites.add(RID(tmpArr[0], LatLon(tmpArr[1], tmpArr[2])))
        }
        val obsSiteRange = 200.0
        var currentDistance: Double
        obsSites.indices.forEach {
            currentDistance =
                LatLon.distance(radarLocation, obsSites[it].location, DistanceUnit.MILE)
            if (currentDistance < obsSiteRange) {
                obsListSb.append(obsSites[it].name)
                obsListSb.append(",")
            }
        }
        return obsListSb.toString().replace(",$".toRegex(), "")
        //return "KARB,KYIP,KBLF,KBKW,KCKB,KCMH,KCRW,KDCA,KEKN,KHLG,KHTS,KI16,KILN,KLEX,KLNP,KLOZ,KMGW,KMRB,KPIT,KPKB,KROA,KTRI,KW22,KW99,KZZV";
    }

    // used to condense a list of metar that contains multiple entries for one site, newest is first so simply grab first/append
    private fun condenseObs(list: List<String>): List<String> {
        val siteMap = mutableMapOf<String, Boolean>()
        val goodObsList = mutableListOf<String>()
        list.forEach {
            val tokens = it.split(" ")
            if (tokens.count() > 3) {
                if (siteMap[tokens[0]] != true) {
                    siteMap[tokens[0]] = true
                    goodObsList.add(it)
                }
            }
        }
        return goodObsList
    }
}
