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
import joshuatee.wx.objects.DownloadTimer

internal object UtilityMetar {

    // 0-3 are for nexrad radar
    // 4 is for the main screen
    // 5 is for canvas
    val metarDataList = List(6) { MetarData() }

    // A data structure (map) consisting of a Lat/Lon string array for each Obs site
    // A flag is used to track if it's been initialized
    private var initializedObsMap = false
    private val obsLatLon = mutableMapOf<String, Array<String>>()
    var timer = DownloadTimer("METAR")

    fun getStateMetarArrayForWXOGL(context: Context, rid: String, paneNumber: Int) {
        if (timer.isRefreshNeeded(context) || rid != metarDataList[paneNumber].obsStateOld) {
            val obsAl = mutableListOf<String>()
            val obsAlExt = mutableListOf<String>()
            val obsAlWb = mutableListOf<String>()
            val obsAlWbGust = mutableListOf<String>()
            val obsAlX = mutableListOf<Double>()
            val obsAlY = mutableListOf<Double>()
            val obsAlAviationColor = mutableListOf<Int>()
            metarDataList[paneNumber].obsStateOld = rid
            val obsList = getObservationSites(context, rid)
            // https://www.aviationweather.gov/metar/data?ids=KDTW%2CKARB&format=raw&date=&hours=0
            val html = "${MyApplication.nwsAWCwebsitePrefix}/adds/metars/index?submit=1&station_ids=$obsList&chk_metars=on".getHtml()
            val metarArrTmp = html.parseColumn("<FONT FACE=\"Monospace,Courier\">(.*?)</FONT><BR>")
            val metarArr = condenseObs(metarArrTmp)
            if (!initializedObsMap) {
                val text = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
                val lines = text.split("\n").dropLastWhile { it.isEmpty() }
                var tokens: List<String>
                lines.forEach {
                    tokens = it.split(" ")
                    obsLatLon[tokens[0]] = arrayOf(tokens[1], tokens[2])
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
            var latLon: Array<String>
            var windDir = ""
            var windInKt = ""
            var windGustInKt = ""
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
                    visBlobDisplay = visBlobArr.last()
                    visBlob = visBlobArr.last().replace("SM", "")
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
                        windGustInKt = windBlob.substring(6, 8)
                        windDirD = windDir.toDoubleOrNull() ?: 0.0
                        windBlob = windDir + " (" + UtilityMath.convertWindDir(windDirD) + ") " +
                                windInKt + " G " + windGustInKt + " kt"
                    }
                    if (tdArr.size > 1) {
                        t = tdArr[0]
                        d = tdArr[1]
                        t = UtilityMath.celsiusToFahrenheit(t.replace("M", "-"))
                        d = UtilityMath.celsiusToFahrenheit(d.replace("M", "-"))
                        obsSite = tmpArr2[0]
                        latLon = obsLatLon[obsSite] ?: arrayOf("0.0", "0.0")
                        if (latLon[0] != "0.0") {
                            obsAl.add(latLon[0] + ":" + latLon[1] + ":" + t + "/" + d)
                            obsAlExt.add(
                                latLon[0] + ":" + latLon[1] + ":" + t + "/" + d + " (" + obsSite + ")"
                                        + MyApplication.newline + pressureBlob + " - " + visBlobDisplay
                                        + MyApplication.newline + windBlob
                                        + MyApplication.newline + conditionsBlob
                                        + MyApplication.newline + timeBlob
                            )
                            try {
                                if (validWind) {
                                    obsAlWb.add(latLon[0] + ":" + latLon[1] + ":" + windDir + ":" + windInKt)
                                    val x = latLon[0].toDoubleOrNull() ?: 0.0
                                    val y = (latLon[1].toDoubleOrNull() ?: 0.0) * -1.0
                                    obsAlX.add(x)
                                    obsAlY.add(y)
                                    obsAlAviationColor.add(aviationColor)
                                }
                                if (validWindGust) {
                                    obsAlWbGust.add(latLon[0] + ":" + latLon[1] + ":" + windDir + ":" + windGustInKt)
                                }
                            } catch (e: Exception) {
                                UtilityLog.handleException(e)
                            }
                        }
                    }
                }
            }
            metarDataList[paneNumber].obsArr = obsAl.toList()
            metarDataList[paneNumber].obsArrExt = obsAlExt.toList()
            metarDataList[paneNumber].obsArrWb = obsAlWb.toList()
            metarDataList[paneNumber].x = DoubleArray(obsAlX.size)
            obsAlX.indices.forEach {
                metarDataList[paneNumber].x[it] = obsAlX[it]
            }
            metarDataList[paneNumber].y = DoubleArray(obsAlY.size)
            obsAlY.indices.forEach {
                metarDataList[paneNumber].y[it] = obsAlY[it]
            }
            metarDataList[paneNumber].obsArrWbGust = obsAlWbGust.toList()
            metarDataList[paneNumber].obsArrAviationColor = obsAlAviationColor.toList()
        }
    }

    //
    // Long press in nexrad radar uses this to find closest observation and return obs data
    //
    // Method below is similar, please see comments below for more information
    //
    fun findClosestMetar(context: Context, location: LatLon): String {
        /*val text = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val metarSites = mutableListOf<RID>()
        lines.indices.forEach {
            val tokens = lines[it].split(" ")
            metarSites.add(RID(tokens[0], LatLon(tokens[1], tokens[2])))
        }*/
        readMetarData(context)
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

    //
    // long press in nexrad radar uses this for nearest meteogram and to show obs name in long press itself
    // it returns a RID object for the closest observation
    //
    // This is also used on the main screen of the app to find the closest observation site
    //
    // Input file is like this
    // name Lat Lon
    // K1BM 47.2833333333 -110.366666667
    // K1BW 41.5166666667 -104.0
    //

    private var metarDataRaw = ""
    private val metarSites = mutableListOf<RID>()

    @Synchronized private fun readMetarData(context: Context) {
        if (metarSites.isEmpty()) {
            UtilityLog.d("wx", "CC init metar data")
            metarDataRaw = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
            val metarDataAsList = metarDataRaw.split("\n").dropLastWhile { it.isEmpty() }
            metarDataAsList.indices.forEach {
                val tokens = metarDataAsList[it].split(" ")
                metarSites.add(RID(tokens[0], LatLon(tokens[1], tokens[2])))
            }
        }
    }

    fun findClosestObservation(context: Context, location: LatLon): RID {
        //UtilityLog.d("wx", "OBS1: " + UtilityTime.currentTimeMillis())
        /*val metarDataRaw = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
        val metarDataAsList = metarDataRaw.split("\n").dropLastWhile { it.isEmpty() }
        val metarSites = mutableListOf<RID>()
        metarDataAsList.indices.forEach {
            val tokens = metarDataAsList[it].split(" ")
            metarSites.add(RID(tokens[0], LatLon(tokens[1], tokens[2])))
        }*/
        readMetarData(context)
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
        //UtilityLog.d("wx", "OBS2: " + UtilityTime.currentTimeMillis())
        // In the unlikely event no closest site is found just return the first one
        return if (bestRid == -1) {
            metarSites[0]
        } else {
            metarSites[bestRid]
        }
    }

    //
    // Returns a comma separated list of the closest obs to a particular radar site
    // obs site must be within 200 miles of location
    // Used only within this class in one spot
    // Used for nexrad radar when obs site is turn on
    //
    //
    private fun getObservationSites(context: Context, rid: String): String {
        val radarLocation = UtilityLocation.getSiteLocation(context, rid)
        val obsListSb = StringBuilder(100)
        readMetarData(context)
        /*val text = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        val obsSites = mutableListOf<RID>()
        lines.forEach {
            val tokens = it.split(" ")
            obsSites.add(RID(tokens[0], LatLon(tokens[1], tokens[2])))
        }*/
        val obsSiteRange = 200.0
        var currentDistance: Double
        metarSites.indices.forEach {
            currentDistance = LatLon.distance(radarLocation, metarSites[it].location, DistanceUnit.MILE)
            if (currentDistance < obsSiteRange) {
                obsListSb.append(metarSites[it].name)
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
