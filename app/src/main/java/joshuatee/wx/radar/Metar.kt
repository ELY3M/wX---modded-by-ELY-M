/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
import joshuatee.wx.R
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityMath
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.OfficeTypeEnum
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityLog
import java.util.regex.Pattern

internal object Metar {

    // 0-3 are for nexrad radar
    // 4 is for the main screen
    // 5 is for canvas
    val data = List(6) { MetarData() }
    // A data structure (map) consisting of a Lat/Lon string array for each Obs site
    private val obsLatLon = mutableMapOf<String, Array<String>>()
    val timer = DownloadTimer("METAR")
    private val metarSites = mutableListOf<RID>()
    private val pattern1: Pattern = Pattern.compile(".*? (M?../M?..) .*?")
    private val pattern2: Pattern = Pattern.compile(".*? A([0-9]{4})")
    private val pattern3: Pattern = Pattern.compile("AUTO ([0-9].*?KT) .*?")
    private val pattern4: Pattern = Pattern.compile("Z ([0-9].*?KT) .*?")
    private val pattern5: Pattern = Pattern.compile("SM (.*?) M?[0-9]{2}/")

    fun get(context: Context, rid: String, paneNumber: Int) {
        if (timer.isRefreshNeeded(context) || rid != data[paneNumber].obsStateOld) {
            val obsAl = mutableListOf<String>()
            val obsAlExt = mutableListOf<String>()
            val obsAlWb = mutableListOf<String>()
            val obsAlWbGust = mutableListOf<String>()
            val obsAlX = mutableListOf<Double>()
            val obsAlY = mutableListOf<Double>()
            val obsAlAviationColor = mutableListOf<Int>()
            data[paneNumber].obsStateOld = rid
            val obsList = getNearbyObsSites(context, rid)
            // https://www.aviationweather.gov/metar/data?ids=KDTW%2CKARB&format=raw&date=&hours=0
            // FIXME TODO new format? https://beta.aviationweather.gov/cgi-bin/data/metar.php?ids=KORD,KSEA,KATL
            // FIXME TODO new format has each obs on single line, no HTML tags

            // OLD - current
            val html = "${GlobalVariables.nwsAWCwebsitePrefix}/adds/metars/index?submit=1&station_ids=$obsList&chk_metars=on".getHtml()
            val metarsTmp = html.parseColumn("<FONT FACE=\"Monospace,Courier\">(.*?)</FONT><BR>")

            // NEW
//            val html = "https://beta.aviationweather.gov/cgi-bin/data/metar.php?ids=$obsList".getHtmlWithNewLine()
//            val metarsTmp = html.split(GlobalVariables.newline)

            val metars = condense(metarsTmp)
            initObsMap(context)
            metars.forEach { z ->
                var validWind = false
                var validWindGust = false
                if ((z.startsWith("K") || z.startsWith("P")) && !z.contains("NIL")) {
                    val tmpArr2 = RegExp.space.split(z)
                    val tmpBlob = z.parse(pattern1)
                    val tdArr = RegExp.slash.split(tmpBlob)
                    var timeBlob = ""
                    if (tmpArr2.size > 1) {
                        timeBlob = tmpArr2[1]
                    }
                    var pressureBlob = z.parse(pattern2)
                    var windBlob = z.parse(pattern3)
                    if (windBlob == "") {
                        windBlob = z.parse(pattern4)
                    }
                    val conditionsBlob = z.parse(pattern5)
                    var visBlob = z.parse(" ([0-9].*?SM) ")
                    val visBlobArr = RegExp.space.split(visBlob)
                    val visBlobDisplay = visBlobArr.last()
                    visBlob = visBlobArr.last().replace("SM", "")
                    // might have 1/2 or 1/4 , just call it zero
                    val visInt = when {
                        visBlob.contains("/") -> 0
                        visBlob != "" -> To.int(visBlob)
                        else -> 20000
                    }
                    // ceiling can be deduced from the lowest height with broken (BKN) or overcast (OVC) reported.
                    var ovcStr = conditionsBlob.parse("OVC([0-9]{3})")
                    var bknStr = conditionsBlob.parse("BKN([0-9]{3})")
                    var ovcInt = 100000
                    var bknInt = 100000
                    if (ovcStr != "") {
                        ovcStr += "00"
                        ovcInt = To.int(ovcStr)
                    }
                    if (bknStr != "") {
                        bknStr += "00"
                        bknInt = To.int(bknStr)
                    }
                    val lowestCig = if (bknInt < ovcInt) {
                        bknInt
                    } else {
                        ovcInt
                    }
                    val aviationColor = if (visInt > 5 && lowestCig > 3000) {
                        Color.GREEN
                    } else if (visInt in 3..5 || lowestCig in 1000..3000) {
                        Color.rgb(0, 100, 255)
                    } else if (visInt in 1..2 || lowestCig in 500..999) {
                        Color.RED
                    } else if (visInt < 1 || lowestCig < 500) {
                        Color.MAGENTA
                    } else {
                        Color.GREEN
                    }
                    //  green, blue, red, and purple
                    // VFR 	> 5 mi 	and > 3000 ft AGL
                    // Marginal VFR 	Between 3 and 5 mi 	and/or Between 1,000 and 3,000 ft AGL
                    // IFR 	1 mi or more but less than 3 mi 	and/or 500 ft or more but less than 1,000 ft
                    // Low IFR 	< 1 mi 	and/or < 500 ft
                    if (pressureBlob.length == 4) {
                        pressureBlob = StringBuilder(pressureBlob).insert(pressureBlob.length - 2, ".").toString()
                        pressureBlob = UtilityMath.unitsPressure(pressureBlob)
                    }
                    // 19011G16KT
                    // 18011KT
                    var windDir = ""
                    var windInKt = ""
                    var windGustInKt = ""
                    if (windBlob.contains("KT") && windBlob.length == 7) {
                        validWind = true
                        windDir = windBlob.substring(0, 3)
                        windInKt = windBlob.substring(3, 5)
                        val windDirD = To.double(windDir)
                        windBlob = windDir + " (" + UtilityMath.convertWindDir(windDirD) + ") " + windInKt + " kt"
                    } else if (windBlob.contains("KT") && windBlob.length == 10) {
                        validWind = true
                        validWindGust = true
                        windDir = windBlob.substring(0, 3)
                        windInKt = windBlob.substring(3, 5)
                        windGustInKt = windBlob.substring(6, 8)
                        val windDirD = To.double(windDir)
                        windBlob = windDir + " (" + UtilityMath.convertWindDir(windDirD) + ") " + windInKt + " G " + windGustInKt + " kt"
                    }
                    if (tdArr.size > 1) {
                        var temperature = tdArr[0]
                        var dewPoint = tdArr[1]
                        temperature = UtilityMath.celsiusToFahrenheit(temperature.replace("M", "-"))
                        dewPoint = UtilityMath.celsiusToFahrenheit(dewPoint.replace("M", "-"))
                        val obsSite = tmpArr2[0]
                        val latLon = obsLatLon[obsSite] ?: arrayOf("0.0", "0.0")
                        if (latLon[0] != "0.0") {
                            obsAl.add(latLon[0] + ":" + latLon[1] + ":" + temperature + "/" + dewPoint)
                            obsAlExt.add(
                                latLon[0] + ":" + latLon[1] + ":" + temperature + "/" + dewPoint + " (" + obsSite + ")"
                                        + GlobalVariables.newline + pressureBlob + " - " + visBlobDisplay
                                        + GlobalVariables.newline + windBlob
                                        + GlobalVariables.newline + conditionsBlob
                                        + GlobalVariables.newline + timeBlob
                            )
                            if (validWind) {
                                obsAlWb.add(latLon[0] + ":" + latLon[1] + ":" + windDir + ":" + windInKt)
                                val x = To.double(latLon[0])
                                val y = To.double(latLon[1]) * -1.0
                                obsAlX.add(x)
                                obsAlY.add(y)
                                obsAlAviationColor.add(aviationColor)
                            }
                            if (validWindGust) {
                                obsAlWbGust.add(latLon[0] + ":" + latLon[1] + ":" + windDir + ":" + windGustInKt)
                            }
                        }
                    }
                }
            }
            data[paneNumber].obsArr = obsAl.toList()
            data[paneNumber].obsArrExt = obsAlExt.toList()
            data[paneNumber].obsArrWb = obsAlWb.toList()
            data[paneNumber].x = DoubleArray(obsAlX.size)

            obsAlX.indices.forEach {
                data[paneNumber].x[it] = obsAlX[it]
            }
            data[paneNumber].y = DoubleArray(obsAlY.size)
            obsAlY.indices.forEach {
                data[paneNumber].y[it] = obsAlY[it]
            }
            data[paneNumber].obsArrWbGust = obsAlWbGust.toList()
            data[paneNumber].obsArrAviationColor = obsAlAviationColor.toList()
        }
    }

    @Synchronized private fun initObsMap(context: Context) {
        if (obsLatLon.isEmpty()) {
            val text = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
            val lines = text.split("\n").dropLastWhile { it.isEmpty() }
            lines.forEach { line ->
                val tokens = line.split(" ")
                obsLatLon[tokens[0]] = arrayOf(tokens[1], tokens[2])
            }
        }
    }

    //
    // Long press in nexrad radar uses this to find closest observation and return obs data
    //
    fun findClosestMetar(context: Context, location: LatLon): String {
        val localMetarSite = findClosestObsSite(context, location)
        return (GlobalVariables.nwsRadarPub + "data/observations/metar/decoded/" + localMetarSite.name + ".TXT").getHtmlWithNewLine()
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
    @Synchronized private fun readData(context: Context) {
        if (metarSites.isEmpty()) {
            val metarDataRaw = UtilityIO.readTextFileFromRaw(context.resources, R.raw.us_metar3)
            val metarDataAsList = metarDataRaw.split("\n").dropLastWhile { it.isEmpty() }
            metarDataAsList.forEach {
                val tokens = it.split(" ")
                metarSites.add(RID(tokens[0], LatLon(tokens[1], tokens[2]),0))
            }
        }
    }

    // causing crash reports in the current condition notification for
    // Exception java.lang.IllegalArgumentException: Comparison method violates its general contract!
    // possibly reverting to older code below
    fun findClosestObsSite(context: Context, location: LatLon, index: Int = 0): RID {
        readData(context)
        val localMetarSites = metarSites.toMutableList()
        localMetarSites.forEach {
            it.distance = LatLon.distance(location, it.location, DistanceUnit.MILE).toInt()
        }
        try {
            localMetarSites.sortBy { it.distance }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return localMetarSites[index]
    }

    // OLD
//    fun findClosestObsSite(context: Context, location: LatLon, index: Int = 0): RID {
//        readData(context)
//        var shortestDistance = 1000.00
//        var currentDistance: Double
//        var bestRid = -1
//        metarSites.indices.forEach {
//            currentDistance = LatLon.distance(location, metarSites[it].location, DistanceUnit.MILE)
//            metarSites[it].distance = currentDistance
//            if (currentDistance < shortestDistance) {
//                shortestDistance = currentDistance
//                bestRid = it
//            }
//        }
//        // In the unlikely event no closest site is found just return the first one
//        return if (bestRid == -1) metarSites[0] else metarSites[bestRid]
//    }

    //
    // Returns a comma separated list of the closest obs to a particular radar site
    // obs site must be within 200 miles of location
    // Used only within this class in one spot
    // Used for nexrad radar when obs site is turn on
    //
    private fun getNearbyObsSites(context: Context, radarSite: String): String {
        val radarLocation = UtilityLocation.getSiteLocation(radarSite, OfficeTypeEnum.RADAR)
        val obsListSb = StringBuilder(100)
        readData(context)
        val obsSiteRange = 200.0
        var currentDistance: Double
        metarSites.forEach {
            currentDistance = LatLon.distance(radarLocation, it.location, DistanceUnit.MILE)
            if (currentDistance < obsSiteRange) {
                obsListSb.append(it.name)
                obsListSb.append(",")
            }
        }
        return obsListSb.toString().replace(",$".toRegex(), "")
        //return "KARB,KYIP,KBLF,KBKW,KCKB,KCMH,KCRW,KDCA,KEKN,KHLG,KHTS,KI16,KILN,KLEX,KLNP,KLOZ,KMGW,KMRB,KPIT,KPKB,KROA,KTRI,KW22,KW99,KZZV";
    }

    // used to condense a list of metar that contains multiple entries for one site, newest is first so simply grab first/append
    private fun condense(list: List<String>): List<String> {
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
