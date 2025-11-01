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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color
import joshuatee.wx.R
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityMath
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.Site
import joshuatee.wx.objects.Sites
import joshuatee.wx.parse
import joshuatee.wx.util.To
import java.util.regex.Pattern

internal object Metar {

    // 0-3 are for nexrad radar
    // 4 is for the main screen
    // 5 is for canvas
    val data = List(6) { MetarData() }

    val timer = DownloadTimer("METAR")
    private val pattern1: Pattern = Pattern.compile(".*? (M?../M?..) .*?")
    private val pattern2: Pattern = Pattern.compile(".*? A([0-9]{4})")
    private val pattern3: Pattern = Pattern.compile("AUTO ([0-9].*?KT) .*?")
    private val pattern4: Pattern = Pattern.compile("Z ([0-9].*?KT) .*?")
    private val pattern5: Pattern = Pattern.compile("SM (.*?) M?[0-9]{2}/")
    lateinit var sites: Sites

    fun initialize(context: Context) {
        val name = mutableMapOf<String, String>()
        val lat = mutableMapOf<String, String>()
        val lon = mutableMapOf<String, String>()
        val lines =
            UtilityIO.rawFileToStringArrayFromResource(context.resources, R.raw.obs_all)
        for (line in lines) {
            val items = line.trimEnd().split(",")
            if (items.size > 2) {
                name[items[0]] = items[1] + ", " + items[2]
                lat[items[0]] = items[3]
                lon[items[0]] = items[4]
            }
        }
        sites = Sites(name, lat, lon, false)
    }

    @Synchronized
    fun get(radarSite: String, paneNumber: Int) {
        if (timer.isRefreshNeeded() || radarSite != data[paneNumber].obsStateOld) {
            val obsAl = mutableListOf<String>()
            val obsAlExt = mutableListOf<String>()
            val obsAlWb = mutableListOf<String>()
            val obsAlWbGust = mutableListOf<String>()
            val obsAlX = mutableListOf<Double>()
            val obsAlY = mutableListOf<Double>()
            val obsAlAviationColor = mutableListOf<Int>()
            data[paneNumber].obsStateOld = radarSite
            val obsList = getNearbyObsSites(radarSite)
            // https://aviationweather.gov/data/api/#changes
            val html =
                "https://www.aviationweather.gov/api/data/metar?ids=$obsList".getHtmlWithNewLine()
            val metarsTmp = html.split(GlobalVariables.newline)
            val metars = condense(metarsTmp)
            metars.forEach { z ->
                var validWind = false
                var validWindGust = false
                if ((z.startsWith("K") || z.startsWith("P") || z.startsWith("T")) && !z.contains("NIL")) {
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
                    // green, blue, red, and purple
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
                    var windDir = ""
                    var windInKt = ""
                    var windGustInKt = ""
                    if (windBlob.contains("KT") && windBlob.length == 7) {
                        validWind = true
                        windDir = windBlob.take(3)
                        windInKt = windBlob.substring(3, 5)
                        val windDirInt = To.int(windDir)
                        windBlob =
                            windDir + " (" + UtilityMath.bearingToDirection(windDirInt) + ") " + windInKt + " kt"
                    } else if (windBlob.contains("KT") && windBlob.length == 10) {
                        validWind = true
                        validWindGust = true
                        windDir = windBlob.take(3)
                        windInKt = windBlob.substring(3, 5)
                        windGustInKt = windBlob.substring(6, 8)
                        val windDirInt = To.int(windDir)
                        windBlob =
                            windDir + " (" + UtilityMath.bearingToDirection(windDirInt) + ") " + windInKt + " G " + windGustInKt + " kt"
                    }
                    if (tdArr.size > 1) {
                        var temperature = tdArr[0]
                        var dewPoint = tdArr[1]
                        temperature = UtilityMath.celsiusToFahrenheit(temperature.replace("M", "-"))
                        dewPoint = UtilityMath.celsiusToFahrenheit(dewPoint.replace("M", "-"))
                        val obsSite = tmpArr2[0]
//                        val latLon = obsLatLon[obsSite] ?: LatLon("0.0", "0.0")
                        val latLon = sites.byCode[obsSite]?.latLon ?: LatLon(0.0, 0.0)
                        if (latLon.latString != "0.0") {
                            obsAl.add("$latLon:$temperature/$dewPoint")
                            obsAlExt.add(
                                latLon.toString() + ":" + temperature + "/" + dewPoint + " (" + obsSite + ")"
                                        + GlobalVariables.newline + pressureBlob + " - " + visBlobDisplay
                                        + GlobalVariables.newline + windBlob
                                        + GlobalVariables.newline + conditionsBlob
                                        + GlobalVariables.newline + timeBlob
                            )
                            if (validWind) {
                                obsAlWb.add("$latLon:$windDir:$windInKt")
                                obsAlX.add(latLon.lat)
                                obsAlY.add(latLon.lon * -1.0)
                                obsAlAviationColor.add(aviationColor)
                            }
                            if (validWindGust) {
                                obsAlWbGust.add("$latLon:$windDir:$windGustInKt")
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

    //
    // Long press in nexrad radar uses this to find closest observation and return obs data
    //
    fun findClosestMetar(location: LatLon): String {
        val localMetarSite = findClosestObservation(location)
        return (GlobalVariables.TGFTP_WEBSITE_PREFIX + "/data/observations/metar/decoded/" + localMetarSite.codeName + ".TXT").getHtmlWithNewLine()
    }

    fun findClosestObservation(latLon: LatLon, order: Int = 0): Site =
        sites.getNearestSite(latLon, order)

    //
    // Returns a comma separated list of the closest obs to a particular radar site
    // obs site must be within 200 miles of location
    // Used only within this class in one spot
    // Used for nexrad radar when obs site is turn on
    //
    private fun getNearbyObsSites(radarSite: String): String {
        val radarLocation = RadarSites.getLatLon(radarSite)
        val obsListSb = StringBuilder(1000)
        synchronized(sites.sites) {
            sites.sites.forEach {
                if (LatLon.distance(radarLocation, it.latLon) < 200.0) {
                    obsListSb.append(it.codeName)
                    obsListSb.append(",")
                }
            }
        }
        return obsListSb.toString().replace(",$".toRegex(), "")
    }

    // used to condense a list of metar that contains multiple entries for one site, newest is first so simply grab first/append
    // entries now look like this, strip off the lead token
    // SPECI KLPR 242048Z AUTO 28005KT 1 1/2SM +RA BR BKN012 OVC060 21/19 A2993 RMK AO2 RAB1959 P0007
    // METAR KCMH 242051Z 22003KT 10SM FEW030 OVC041 24/17 A2991 RMK AO2 SLP122 T02390172 58010
    private fun condense(list: List<String>): List<String> {
        val siteMap = mutableMapOf<String, Boolean>()
        val goodObsList = mutableListOf<String>()
        list.forEach {
            val line = it.replace("METAR ", "").replace("SPECI ", "")
            val tokens = line.split(" ")
            if (tokens.count() > 3) {
                if (siteMap[tokens[0]] != true) {
                    siteMap[tokens[0]] = true
                    goodObsList.add(line)
                }
            }
        }
        return goodObsList
    }
}

/*

https://aviationweather.gov/cgi-bin/data/metar.php?ids=K1BW,K1CW,K1HW,K2WX,K7L2,K9V9,KAIA,KANW,KBFF,KBHK,KBWW,KBYG,KCDR,KCUT,KD07,KD57,KDGW,KDIK,KEAN,KECS,KGCC,KGRN,KGUR,KHEI,KICR,KIEN,KMBG,KPHP,KPIR,KRAP,KRCA,KSPF,KTIF,KTOR,KVTN,KW43

K1BW 081955Z AUTO 36006KT 8SM -RA BKN025 OVC030 02/01 A3022 RMK AO2 RAE1859DZB1859E03RAB03 SLP258 P0002 T00220007 $
K1CW 081955Z AUTO 02004KT 10SM -RA SCT029 OVC039 04/02 A3021 RMK AO2 RAB07E20DZB20E22RAB22 SLP236 P0000 T00420017 $
K1HW 081955Z AUTO 03009KT 10SM -RA OVC036 05/02 A3017 RMK AO2 RAE06DZB06E12RAB12 SLP229 P0000 T00510018 $
K2WX 081956Z AUTO 18012KT 13/M07 A3014 RMK AO1 SLP220 T01281072
K7L2 082015Z AUTO 18007KT 10SM CLR 12/M02 A3024 RMK AO2
K9V9 082015Z AUTO 00000KT 10SM CLR 11/02 A3024 RMK AO2 T01050019
KAIA 081953Z AUTO 00000KT 10SM OVC050 08/01 A3020 RMK AO2 SLP232 T00830011
KANW 082015Z AUTO 09004KT 10SM SCT100 12/03 A3025 RMK AO2 T01220025
KBFF 081953Z AUTO 26005KT 10SM OVC055 06/M01 A3022 RMK AO2 SLP243 T00611011
KBHK 081951Z AUTO 20011KT 10SM CLR 15/M02 A3016 RMK AO2 SLP223 T01501022
KBWW 082015Z AUTO 20011KT 10SM CLR 14/M05 A3015 RMK AO2
KBYG 081953Z AUTO 14011KT 10SM CLR 13/M01 A3013 RMK AO2 SLP192 T01281011
KCDR 081953Z AUTO 29004KT 10SM BKN080 10/01 A3020 RMK AO2 SLP245 T01000006
KCUT 081953Z AUTO 19007KT 10SM CLR 11/01 A3021 RMK AO2 SLP228 T01110006
KD07 081956Z AUTO 19005KT 13/M05 A3018 RMK AO1 SLP236 T01331050
KD57 082015Z AUTO 21008G15KT 10SM CLR 14/M03 A3016 RMK AO2
KDGW 081953Z AUTO 00000KT 10SM CLR 07/M06 A3020 RMK AO2 SLP246 T00721056
KDIK 081956Z AUTO 19012KT 10SM CLR 14/M02 A3015 RMK AO2 SLP223 T01391017
KEAN 082015Z AUTO 00000KT 10SM -DZ SCT043 BKN060 OVC070 04/M02 A3021 RMK AO2 T00401017
KECS 082015Z AUTO 19006KT 10SM CLR 08/M07 A3022 RMK AO2
KGCC 081953Z AUTO 19008KT 10SM CLR 12/M04 A3016 RMK AO2 SLP222 T01171044
KGRN 082015Z AUTO 00000KT 10SM CLR 11/01 A3020 RMK AO2 T01050007
KGUR 081955Z AUTO 31003KT 9SM BKN065 OVC120 06/M01 A3019 RMK AO2 SLP265 T00631014
KHEI 081953Z AUTO 18010KT 10SM CLR 14/M03 A3018 RMK AO2 SLP234 T01441033
KICR 081953Z AUTO 00000KT 10SM CLR 13/02 A3024 RMK AO2 SLP246 T01330017
KIEN 081952Z AUTO 00000KT 10SM FEW085 10/01 A3021 RMK AO2 SLP242 T01000006
KMBG 081952Z AUTO 00000KT 10SM CLR 14/M02 A3018 RMK AO2 SLP230 T01391017
KPHP 081955Z AUTO 00000KT 10SM CLR 13/01 A3020 RMK AO2 SLP233 T01330006
KPIR 081953Z AUTO 20004KT 10SM CLR 11/01 A3021 RMK AO2 SLP239 T01060011
KRAP 081952Z 11005KT 10SM CLR 12/M07 A3019 RMK AO2 SLP239 T01221067
KRCA 081955Z AUTO 12007KT 10SM CLR 11/M07 A3015 RMK AO2 SLP229 T01101073 $
KSPF 082015Z AUTO 03007KT 10SM CLR 11/M05 A3019 RMK AO2
KTIF 082015Z AUTO 11007KT 10SM BKN070 OVC080 11/01 A3023 RMK AO2 T01130013
KTOR 081953Z AUTO 00000KT 10SM OVC048 04/M02 A3021 RMK AO2 SLP247 T00391017
KVTN 081952Z AUTO VRB04KT 10SM CLR 12/02 A3024 RMK AO2 SLP249 T01220022

 */
