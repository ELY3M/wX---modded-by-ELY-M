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
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.OfficeTypeEnum
import joshuatee.wx.parse
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityLog
import java.util.regex.Pattern

internal object Metar {

    // 0-3 are for nexrad radar
    // 4 is for the main screen
    // 5 is for canvas
    val data = List(6) { MetarData() }

    // A data structure (map) consisting of a Lat/Lon string array for each Obs site
    private val obsLatLon = mutableMapOf<String, LatLon>()
    val timer = DownloadTimer("METAR")
    private val metarSites = mutableListOf<RID>()
    private val pattern1: Pattern = Pattern.compile(".*? (M?../M?..) .*?")
    private val pattern2: Pattern = Pattern.compile(".*? A([0-9]{4})")
    private val pattern3: Pattern = Pattern.compile("AUTO ([0-9].*?KT) .*?")
    private val pattern4: Pattern = Pattern.compile("Z ([0-9].*?KT) .*?")
    private val pattern5: Pattern = Pattern.compile("SM (.*?) M?[0-9]{2}/")

    @Synchronized
    fun get(context: Context, rid: String, paneNumber: Int) {
        if (timer.isRefreshNeeded() || rid != data[paneNumber].obsStateOld) {
            val obsAl = mutableListOf<String>()
            val obsAlExt = mutableListOf<String>()
            val obsAlWb = mutableListOf<String>()
            val obsAlWbGust = mutableListOf<String>()
            val obsAlX = mutableListOf<Double>()
            val obsAlY = mutableListOf<Double>()
            val obsAlAviationColor = mutableListOf<Int>()
            data[paneNumber].obsStateOld = rid
            val obsList = getNearbyObsSites(context, rid)

            val html =
                "https://www.aviationweather.gov/cgi-bin/data/metar.php?ids=$obsList".getHtmlWithNewLine()
            val metarsTmp = html.split(GlobalVariables.newline)
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

                    @Suppress("KotlinConstantConditions")
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
                        windDir = windBlob.substring(0, 3)
                        windInKt = windBlob.substring(3, 5)
                        val windDirInt = To.int(windDir)
                        windBlob =
                            windDir + " (" + UtilityMath.bearingToDirection(windDirInt) + ") " + windInKt + " kt"
                    } else if (windBlob.contains("KT") && windBlob.length == 10) {
                        validWind = true
                        validWindGust = true
                        windDir = windBlob.substring(0, 3)
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
                        val latLon = obsLatLon[obsSite] ?: LatLon("0.0", "0.0")
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

    @Synchronized
    private fun initObsMap(context: Context) {
        if (obsLatLon.isEmpty()) {
            val lines =
                UtilityIO.rawFileToStringArrayFromResource(context.resources, R.raw.us_metar3)
            lines.forEach { line ->
                val tokens = line.split(" ")
                obsLatLon[tokens[0]] = LatLon(tokens[1], tokens[2])
            }
        }
    }

    //
    // Long press in nexrad radar uses this to find closest observation and return obs data
    //
    fun findClosestMetar(context: Context, location: LatLon): String {
        val localMetarSite = findClosestObservation(context, location)
        return (GlobalVariables.TGFTP_WEBSITE_PREFIX + "/data/observations/metar/decoded/" + localMetarSite.name + ".TXT").getHtmlWithNewLine()
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
    @Synchronized
    private fun loadMetarData(context: Context) {
        if (metarSites.isEmpty()) {
            val metarDataAsList =
                UtilityIO.rawFileToStringArrayFromResource(context.resources, R.raw.us_metar3)
            metarDataAsList.forEach {
                val tokens = it.split(" ")
                metarSites.add(RID(tokens[0], LatLon(tokens[1], tokens[2]), 0.0))
            }
        }
    }

    // causing crash reports in the current condition notification for
    // Exception java.lang.IllegalArgumentException: Comparison method violates its general contract!
    // possibly reverting to older code below
    fun findClosestObservation(context: Context, location: LatLon, index: Int = 0): RID {
        loadMetarData(context)
        val obsSites = metarSites.toMutableList()
//        obsSites.forEach {
//            it.distance = LatLon.distance(location, it.location, DistanceUnit.MILE).toInt()
//        }
        for (it in obsSites.indices) {
            obsSites[it].distance =
                LatLon.distance(location, obsSites[it].location, DistanceUnit.MILE)
        }
        try {
            obsSites.sortBy { it.distance }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return obsSites[index]
    }

    //
    // Returns a comma separated list of the closest obs to a particular radar site
    // obs site must be within 200 miles of location
    // Used only within this class in one spot
    // Used for nexrad radar when obs site is turn on
    //
    @Suppress("SpellCheckingInspection")
    private fun getNearbyObsSites(context: Context, radarSite: String): String {
        val radarLocation = UtilityLocation.getSiteLocation(radarSite, OfficeTypeEnum.RADAR)
        val obsListSb = StringBuilder(100)
        loadMetarData(context)
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

/*

https://aviationweather.gov/cgi-bin/data/metar.php?ids=K0F2,K13K,K1F0,K1K1,K3AU,K4O4,KAAO,KADH,KADM,KADS,KAFW,KASG,KAVK,KAXS,KBEC,KBKN,KBVO,KCDS,KCFV,KCHK,KCLK,KCNU,KCQB,KCSM,KCUH,KCWC,KDAL,KDEQ,KDFW,KDTO,KDUA,KDUC,KELK,KEND,KEQA,KEWK,KF00,KF05,KF46,KFDR,KFSI,KFSM,KFTW,KFWS,KFYV,KGAG,KGCM,KGKY,KGLE,KGMJ,KGOK,KGPM,KGVT,KGYI,KGZL,KH71,KHBR,KHHF,KHQZ,KHSD,KHUT,KIAB,KICT,KIDP,KJLN,KJSV,KJWG,KLAW,KLNC,KLTS,KLUD,KMEZ,KMIO,KMKO,KMLC,KMWL,KNFW,KOJA,KOKC,KOKM,KOUN,KOWP,KP28,KPNC,KPPF,KPRX,KPTT,KPVJ,KPWA,KRBD,KRCE,KRKR,KROG,KRPH,KRQO,KRVS,KSLG,KSLR,KSNL,KSPS,KSRE,KSWO,KTIK,KTKI,KTQH,KTRL,KTUL,KVBT,KWDG,KWLD,KWWR,KXBP,KXNA

sample:

K0F2 021015Z AUTO 13007KT 5SM BR OVC005 19/18 A3007 RMK AO2 T01850178
K13K 021015Z AUTO 07007KT 10SM BKN015 OVC026 10/09 A3016 RMK AO2 LTG DSNT W T01020086
K1F0 021015Z AUTO 13007KT 5SM BR OVC005 18/17 A3009 RMK AO2 T01820172
K3AU 021015Z AUTO 09007KT 10SM OVC013 13/08 A3010 RMK AO2
K4O4 021015Z AUTO 08006KT 6SM BR OVC005 17/17 A3013 RMK AO2
KAAO 021017Z AUTO 04005KT 10SM VCTS OVC013 13/09 A3010 RMK AO2 LTG DSNT ALQDS T01330094
KADH 021015Z AUTO 13009KT 1SM BR OVC003 17/17 A3011 RMK AO2
KADS 021015Z AUTO 12008KT 9SM BKN010 OVC022 20/19 A3007 RMK AO2
KAFW 021016Z 12007KT 7SM BKN010 BKN014 OVC032 20/18 A3006 RMK AO2 CIG 007V013 T02000183
KAVK 021015Z AUTO 15008KT 10SM -RA BKN012 OVC024 16/15 A3002 RMK AO2 LTG DSNT W-NE
KAXS 021015Z AUTO 14016KT 3SM -RA BR OVC003 18/18 A3004 RMK AO2
KBEC 021015Z AUTO 10008KT 10SM VCTS OVC013 14/10 A3009 RMK AO2 LTG DSNT SW THRU N T01350095
KBKN 021015Z AUTO 14011KT 10SM OVC008 15/15 A3009 RMK AO2
KBVO 020953Z AUTO 19003KT 10SM BKN030 12/11 A3014 RMK AO2 SLP200 T01220106
KCDS 020953Z AUTO 15011KT 4SM -RA BR OVC003 17/17 A3003 RMK AO2 RAB52 SLP152 P0000 T01720172
KCFV 020952Z AUTO 13004KT 10SM OVC026 13/11 A3014 RMK AO2 SLP202 T01330106
KCHK 021015Z AUTO 13007KT 7SM OVC007 18/17 A3006 RMK AO2 T01760167
KCLK 021015Z AUTO 16009KT 3SM BR OVC006 17/16 A3006 RMK AO2 LTG DSNT N AND SW
KCNU 021017Z AUTO 12008KT 10SM BKN022 OVC080 12/08 A3016 RMK AO2 T01220083
KCQB 021015Z AUTO 11005KT 10SM OVC008 16/14 A3012 RMK AO2 T01570141
KCSM 021001Z AUTO 16017KT 2 1/2SM -RA BR OVC004 17/17 A3005 RMK AO2 P0000 T01720167
KCUH 021015Z AUTO 15005KT 10SM OVC012 16/13 A3010 RMK AO2 LTG DSNT NW T01600133
KCWC 021015Z AUTO 17011G15KT 120V180 10SM BKN012 OVC017 20/18 A3007 RMK AO2 LTG DSNT W T01950176
KDAL 021024Z 13006KT 6SM -RA BR OVC009 20/19 A3006 RMK AO2 RAB15 P0000 T02000189
KDEQ 021006Z AUTO 08004KT 10SM OVC009 18/16 A3013 RMK AO2 T01780161
KDFW 020953Z 11007KT 7SM OVC010 20/18 A3006 RMK AO2 SLP171 T02000183 $
KDTO 021000Z AUTO 14006KT 10SM BKN010 OVC014 20/18 A3007 RMK AO2 T02000183
KDUA 021015Z AUTO 11008KT 3SM BR OVC003 18/18 A3013 RMK AO2
KDUC 021015Z AUTO 13010KT 7SM BR OVC006 18/17 A3009 RMK AO2
KELK 021015Z AUTO 16009G16KT 5SM BR OVC003 17/17 A3004 RMK AO2 LTG DSNT N AND W RAE1013 P0001 T01690169 $
KEND 021010Z AUTO 14006KT 10SM FEW009 BKN025 OVC033 16/14 A3003 RMK AO2 CIG 023 RWY35C SLP163 $
KEQA 021015Z AUTO 08007KT 10SM OVC013 13/10 A3012 RMK AO2
KEWK 021015Z AUTO 16012KT 10SM -TSRA FEW024 BKN032 OVC075 13/13 A3011 RMK AO2 LTG DSNT ALQDS PNO $
KF00 021015Z AUTO 10005KT 5SM BR OVC004 18/18 A3011 RMK AO2 T01790178
KF05 021020Z AUTO 13010KT 5SM OVC007 19/19 A3004 RMK AO2 LTG DSNT W T01920186
KF46 021015Z AUTO 11005KT 7SM OVC007 20/20 A3008 RMK AO2 T01950195
KFDR 020953Z AUTO 14011KT 8SM OVC010 19/17 A3003 RMK AO2 SLP154 T01890167
KFSM 020953Z 08008KT 10SM OVC019 17/13 A3016 RMK AO2 SLP210 T01670128
KFTW 020953Z 11006KT 8SM OVC009 20/18 A3006 RMK AO2 CIG 007V012 SLP171 T02000183
KFWS 021015Z AUTO 11004KT 10SM OVC007 20/19 A3008 RMK AO2 T02010190
KFYV 020953Z AUTO 00000KT 10SM OVC014 14/12 A3018 RMK AO2 SLP211 T01440117
KGAG 021009Z AUTO 17016G22KT 9SM VCTS -RA OVC009 17/16 A3002 RMK AO2 LTG DSNT SW-N RAB00 CIG 004V011 P0000 T01720161
KGCM 021015Z AUTO 11006KT 10SM OVC023 16/15 A3013 RMK AO2
KGKY 020953Z AUTO 11007KT 10SM BKN011 OVC029 21/18 A3007 RMK AO2 CIG 009V014 SLP174 T02110183
KGMJ 021015Z AUTO 09004KT 10SM OVC023 12/11 A3016 RMK AO2 TSNO
KGOK 020953Z AUTO 17011G17KT 10SM OVC007 17/16 A3009 RMK AO2 SLP183 T01670161
KGPM 021015Z AUTO 09006KT 10SM SCT010 OVC024 20/20 A3008 RMK AO2 T02020201

 */
