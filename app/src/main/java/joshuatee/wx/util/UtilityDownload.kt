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

package joshuatee.wx.util

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateFormat

import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.UtilityLightning
import joshuatee.wx.activitiesmisc.UtilitySunMoon
import joshuatee.wx.activitiesmisc.UtilityUSHourly
import joshuatee.wx.audio.UtilityPlayList
import joshuatee.wx.canada.UtilityCanadaImg
import joshuatee.wx.external.ExternalSunriseLocation
import joshuatee.wx.external.ExternalSunriseSunsetCalculator
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.spc.*
import okhttp3.Request

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.UIPreferences
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.radar.UtilityAwcRadarMosaic
import joshuatee.wx.radar.UtilityUSImgNWSMosaic
import joshuatee.wx.vis.UtilityGOES16

object UtilityDownload {

    private fun get1KMURL() = UtilityImg.getBlankBitmap()

    private fun get2KMURL() = UtilityImg.getBlankBitmap()

    fun getRadarMosiac(context: Context): Bitmap {
        //val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val location = Location.currentLocationStr
        val rid1 = Location.getRid(context, location)
        var bitmap: Bitmap = UtilityImg.getBlankBitmap()
        try {
            if (!UIPreferences.useAwcRadarMosaic) {
                val ridLoc = Utility.readPref(context, "RID_LOC_$rid1", "")
                val nwsLocationArr = ridLoc.split(",").dropLastWhile { it.isEmpty() }
                val state = nwsLocationArr[0]
                var k = Utility.readPref(context, "WIDGET_RADAR_LEVEL", "1km")
                when (k) {
                    "regional" -> k = "regional"
                    "usa" -> k = "usa"
                }
                bitmap = if (Location.isUS(location)) {
                    if (k == "usa") {
                        UtilityUSImgNWSMosaic.get(context, "latest", false)
                    } else {
                        UtilityUSImgNWSMosaic.get(
                            context,
                            UtilityUSImgNWSMosaic.getSectorFromState(state),
                            false
                        )
                    }
                } else {
                    val prov = Utility.readPref(context, "NWS" + location + "_STATE", "")
                    UtilityCanadaImg.getRadarMosaicBitmapOptionsApplied(
                        context,
                        UtilityCanada.getECSectorFromProv(prov)
                    )
                }
            } else {
                //val prefToken = "AWCMOSAIC_PARAM_LAST_USED"
                //val index = Utility.readPref(context, prefToken, 0)
                //bitmap = UtilityAwcRadarMosaic.get(UtilityAwcRadarMosaic.sectors[index])

                var product = "rad_rala"
                val prefTokenSector = "AWCMOSAIC_SECTOR_LAST_USED"
                val prefTokenProduct = "AWCMOSAIC_PRODUCT_LAST_USED"
                var sector = "us"
                sector = Utility.readPref(prefTokenSector, sector)
                product = Utility.readPref(prefTokenProduct, product)
                bitmap = UtilityAwcRadarMosaic.get(sector, product)
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return bitmap
    }

    fun getImgProduct(context: Context, product: String): Bitmap {
        var url = ""
        var bm = UtilityImg.getBlankBitmap()
        val tmpArr: List<String>
        var needsBitmap = true
        when (product) {
            "GOES16" -> {
                needsBitmap = false
                val index = Utility.readPref(context, "GOES16_IMG_FAV_IDX", 0)
                bm = UtilityGOES16.getImage(
                    UtilityGOES16.codes[index],
                    Utility.readPref(context, "GOES16_SECTOR", "cgl")
                )
            }
            "VIS_1KM", "VIS_MAIN" -> {
                needsBitmap = false
                bm = get1KMURL()
            }
            "CARAIN" -> if (Location.x.contains("CANADA")) {
                needsBitmap = false
                var rid = Location.rid
                if (rid == "NAT") rid = "CAN"
                bm =
                    if (rid == "CAN" || rid == "PAC" || rid == "WRN" || rid == "ONT" || rid == "QUE" || rid == "ERN")
                        UtilityCanadaImg.getRadarMosaicBitmapOptionsApplied(context, rid)
                    else
                        UtilityCanadaImg.getRadarBitmapOptionsApplied(context, rid, "")
            }
            "RAD_1KM" -> {
            }
            "RAD_2KM" -> {
                needsBitmap = false
                bm = getRadarMosiac(context)
            }
            "IR_2KM", "WV_2KM", "VIS_2KM" -> {
                needsBitmap = false
                bm = get2KMURL()
            }
            "VIS_CONUS" -> {
                needsBitmap = false
                bm = UtilityGOES16.getImage("02", "CONUS")
            }
            "FMAP" -> url = "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaa.gif"
            "FMAP12" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/92fwbg.gif"
            "FMAP24" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/94fwbg.gif"
            "FMAP36" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/96fwbg.gif"
            "FMAP48" -> url = "${MyApplication.nwsWPCwebsitePrefix}/basicwx/98fwbg.gif"
            "FMAP3D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9jhwbg_conus.gif"
            "FMAP4D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9khwbg_conus.gif"
            "FMAP5D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9lhwbg_conus.gif"
            "FMAP6D" -> url = "${MyApplication.nwsWPCwebsitePrefix}/medr/9mhwbg_conus.gif"
            "QPF1" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_94qwbg.gif"
            "QPF2" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_98qwbg.gif"
            "QPF3" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_99qwbg.gif"
            "QPF1-2" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/d12_fill.gif"
            "QPF1-3" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/d13_fill.gif"
            "QPF4-5" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/95ep48iwbg_fill.gif"
            "QPF6-7" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/97ep48iwbg_fill.gif"
            "QPF1-5" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/p120i.gif"
            "QPF1-7" -> url = "${MyApplication.nwsWPCwebsitePrefix}/qpf/p168i.gif"
            "SPC_TST" -> {
                needsBitmap = false
                val images = UtilitySPC.tstormOutlookImages
                bm = UtilityImg.mergeImagesVertically(images)
            }
            "SWOD1" -> {
                needsBitmap = false
                bm = UtilitySPCSWO.getImageUrls("1", false)[0]
            }
            "WEATHERSTORY" -> {
                needsBitmap = false
                bm =
                    ("http://www.weather.gov/images/" + Location.wfo.toLowerCase() + "/wxstory/Tab2FileL.png").getImage()
            }
            "SWOD2" -> {
                needsBitmap = false
                bm = UtilitySPCSWO.getImageUrls("2", false)[0]
            }
            "SWOD3" -> {
                needsBitmap = false
                bm = UtilitySPCSWO.getImageUrls("3", false)[0]
            }
            "SWOD4" -> {
                needsBitmap = false
                bm = UtilitySPCSWO.getImageUrls("4", false)[0]
            }
            "SPCMESO1" -> {
                var param = "500mb"
                tmpArr = MyApplication.spcmesoFav.split(":")
                if (tmpArr.size > 3) param = tmpArr[3]
                needsBitmap = false
                bm = UtilitySPCMESOInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySPCMESO.defaultSector
                    )
                )
            }
            "SPCMESO2" -> {
                var param = "pmsl"
                tmpArr = MyApplication.spcmesoFav.split(":")
                if (tmpArr.size > 4) param = tmpArr[4]
                needsBitmap = false
                bm = UtilitySPCMESOInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySPCMESO.defaultSector
                    )
                )
            }
            "SPCMESO3" -> {
                var param = "ttd"
                tmpArr = MyApplication.spcmesoFav.split(":")
                if (tmpArr.size > 5) param = tmpArr[5]
                needsBitmap = false
                bm = UtilitySPCMESOInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySPCMESO.defaultSector
                    )
                )
            }
            "SPCMESO4" -> {
                var param = "rgnlrad"
                tmpArr = MyApplication.spcmesoFav.split(":")
                if (tmpArr.size > 6) param = tmpArr[6]
                needsBitmap = false
                bm = UtilitySPCMESOInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySPCMESO.defaultSector
                    )
                )
            }
            "SPCMESO5" -> {
                var param = "lllr"
                tmpArr = MyApplication.spcmesoFav.split(":")
                if (tmpArr.size > 7) param = tmpArr[7]
                needsBitmap = false
                bm = UtilitySPCMESOInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySPCMESO.defaultSector
                    )
                )
            }
            "SPCMESO6" -> {
                var param = "laps"
                tmpArr = MyApplication.spcmesoFav.split(":")
                if (tmpArr.size > 8) param = tmpArr[8]
                needsBitmap = false
                bm = UtilitySPCMESOInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySPCMESO.defaultSector
                    )
                )
            }
            "CONUSWV" -> {
                needsBitmap = false
                bm = UtilityGOES16.getImage("09", "CONUS")
            }
            "LTG" -> {
                needsBitmap = false
                bm = UtilityLightning.getImage(
                    Utility.readPref(
                        context,
                        "LIGHTNING_SECTOR",
                        "usa_big"
                    ), Utility.readPref(context, "LIGHTNING_PERIOD", "0.25")
                )
            }
            "SND" -> {
                needsBitmap = false
                bm = UtilitySPCSoundings.getImage(
                    context,
                    UtilityLocation.getNearestSnd(context, Location.latLon)
                )
            }
            "STRPT" -> url = UtilitySPC.getStormReportsTodayUrl()
            else -> {
                bm = get1KMURL()
                needsBitmap = false
            }
        }
        if (needsBitmap) {
            bm = url.getImage()
        }
        return bm
    }

    fun getTextProduct(context: Context, prodF: String): String {
        var text: String
        val prod = prodF.toUpperCase(Locale.US)
        if (prod == "AFDLOC") {
            text = getTextProduct(context, "afd" + Location.wfo.toLowerCase(Locale.US))
        } else if (prod == "HWOLOC") {
            text = getTextProduct(context, "hwo" + Location.wfo.toLowerCase(Locale.US))
        } else if (prod == "VFDLOC") {
            text = getTextProduct(context, "vfd" + Location.wfo.toLowerCase(Locale.US))
        } else if (prod == "SUNMOON") {
            text = UtilitySunMoon.getData(Location.locationIndex)
        } else if (prod == "HOURLY") {
            val textArr = UtilityUSHourly.getString(Location.currentLocation)
            text = textArr[0]
        } else if (prod == "SWPC3DAY") {
            text = "http://services.swpc.noaa.gov/text/3-day-forecast.txt".getHtmlSep()
        } else if (prod == "SWPC27DAY") {
            text = "http://services.swpc.noaa.gov/text/27-day-outlook.txt".getHtmlSep()
        } else if (prod == "SWPCWWA") {
            text = "http://services.swpc.noaa.gov/text/advisory-outlook.txt".getHtmlSep()
        } else if (prod == "SWPCHIGH") {
            text = "http://services.swpc.noaa.gov/text/weekly.txt".getHtmlSep()
        } else if (prod == "SWPCDISC") {
            text = "http://services.swpc.noaa.gov/text/discussion.txt".getHtmlSep()
        } else if (prod == "SWPC3DAYGEO") {
            text = ("http://services.swpc.noaa.gov/text/3-day-geomag-forecast.txt").getHtmlSep()
        } else if (prod.contains("MIATCP") || prod.contains("MIATCM") || prod.contains("MIATCD") || prod.contains(
                "MIAPWS"
            ) || prod.contains("MIAHS")
        ) {
            text = UtilityString.getNWSPRE("${MyApplication.nwsNhcWebsitePrefix}/text/$prod.shtml")
            if (prod.contains("MIATCD")) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
            text = text.replace("^<br>".toRegex(), "")
        } else if (prod.contains("MIAT")) {
            text = UtilityString.getHTMLandParseSep(
                "${MyApplication.nwsNhcWebsitePrefix}/ftp/pub/forecasts/discussion/$prod",
                "(.*)"
            )
            text = text.substring(text.indexOf('>') + 1)
            text = text.substring(text.indexOf('>') + 1)
            text = text.substring(text.indexOf('>') + 1)
            text = text.substring(text.indexOf('>') + 1)
            text = text.replace("^<br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks && (prod == "MIATWOAT" ||
                        prod == "MIATWDAT" ||
                        prod == "MIATWOEP" ||
                        prod == "MIATWDEP"
                        )
            ) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.startsWith("SCCNS")) {
            text = UtilityString.getHTMLandParseSep(
                "${MyApplication.nwsWPCwebsitePrefix}/discussions/nfd" + prod.toLowerCase(Locale.US).replace(
                    "ns",
                    ""
                ) + ".html", RegExp.pre2Pattern
            )
            text = text.replace("^<br><br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
            text = text.replace("<br>".toRegex(), "<BR>")
        } else if (prod.contains("SPCMCD")) {
            val no = prod.substring(6)
            val textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md$no.html"
            text = UtilityString.getHTMLandParseSep(textUrl, RegExp.pre2Pattern)
            text = text.replace("^<br><br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
            text = text.replace("<br>".toRegex(), "<BR>")
        } else if (prod.contains("SPCWAT")) {
            val no = prod.substring(6)
            val textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww$no.html"
            text = UtilityString.getHTMLandParseSep(textUrl, RegExp.pre2Pattern)
            text = text.replace("^<br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.contains("WPCMPD")) {
            val no = prod.substring(6)
            val textUrl =
                "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd_multi.php?md=$no"
            text = UtilityString.getHTMLandParseSep(textUrl, RegExp.pre2Pattern)
            text = text.replace("^<br>".toRegex(), "")
            text = text.replace("^ <br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.contains("QPFHSD")) {
            val textUrl =
                "${MyApplication.nwsWPCwebsitePrefix}/discussions/hpcdiscussions.php?disc=qpfhsd"
            text = UtilityString.getHTMLandParseSep(textUrl, RegExp.pre2Pattern)
            text = text.replace("^<br>".toRegex(), "")
            text = text.replace("^ <br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.startsWith("GLF") && !prod.contains("%")) {
            text = getTextProduct(context, "$prod%")
        } else if (prod.contains("FOCN45")) {
            text = "${MyApplication.NWS_RADAR_PUB}/data/raw/fo/focn45.cwwg..txt".getHtmlSep()
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace(" &nbsp", "")
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.startsWith("AWCN")) {
            text =
                ("${MyApplication.NWS_RADAR_PUB}/data/raw/aw/" + prod.toLowerCase(Locale.US) + ".cwwg..txt").getHtmlSep()
        } else if (prod.contains("NFD")) {
            text = ("http://www.opc.ncep.noaa.gov/mobile/mobile_product.php?id=" + prod.toUpperCase(
                Locale.US
            )).getHtml()
        } else if (prod.contains("FWDDY38")) {
            text = UtilityString.getHTMLandParseSep(
                "${MyApplication.nwsSPCwebsitePrefix}/products/exper/fire_wx/",
                "<pre>(.*?)</pre>"
            )
            text = text.replace("^<br>".toRegex(), "")
            text = text.replace("^ <br>".toRegex(), "")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace(" &nbsp", "")
                text = text.replace("<br><br>", "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.contains("FXCN01")) {
            text = ("${MyApplication.NWS_RADAR_PUB}/data/raw/fx/fxcn01.cwao..txt").getHtmlSep()
        } else if (prod.startsWith("VFD")) {
            val t2 = prod.substring(3)
            text = ("http://www.aviationweather.gov/fcstdisc/data?cwa=K$t2").getHtmlSep()
            text = text.parse("<!-- raw data starts -->(.*?)<!-- raw data ends -->")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                text = text.replace("<br> <br>".toRegex(), "<BR><BR>")
                text = text.replace("<br> {4}<br> {4}".toRegex(), "<BR><BR>")
                text = text.replace("<br>", " ")
            }
        } else if (prod.contains("FPCN48")) {
            text = "${MyApplication.NWS_RADAR_PUB}/data/raw/fp/fpcn48.cwao..txt".getHtmlSep()
        } else if (prod.contains("QPFPFD")) {
            val textUrl =
                MyApplication.nwsWPCwebsitePrefix + "/discussions/hpcdiscussions.php?disc=qpfpfd"
            text = textUrl.getHtmlSep()
            text = text.parse(RegExp.pre2Pattern)
        } else if (prod.contains("PMDTHR")) {
            text = UtilityString.getHTMLandParseSep(
                "http://www.cpc.noaa.gov/products/predictions/threats/threats.php",
                "<div id=\"discDiv\">(.*?)</div>"
            )
        } else if (prod.contains("CTOF")) {
            text = "Celsius to Fahrenheit table" + MyApplication.newline + UtilityMath.cToFTable()
        } else {
            val t1 = prod.substring(0, 3)
            var t2 = prod.substring(3)
            t2 = t2.replace("%", "")
            val html =
                UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/products/types/$t1/locations/$t2")
            val urlProd = html.parse("\"id\": \"(.*?)\"")
            val prodHtml =
                UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/products/$urlProd")
            text = UtilityString.parseAcrossLines(prodHtml, "\"productText\": \"(.*?)\\}")
            //text = text.replace("\\n\\n", "<BR>")
            //text = text.replace("\\n", " ")
            text = text.replace("\\n\\n", "<BR><BR>")
            text = text.replace("\\n", "<BR>")
        }
        UtilityPlayList.checkAndSave(context, prod, text)
        return text
    }

    fun getTextProduct(prodF: String, version: Int): String {
        val prod = prodF.toUpperCase(Locale.US)
        val t1 = prod.substring(0, 3)
        val t2 = prod.substring(3)
        val url =
            "http://forecast.weather.gov/product.php?site=NWS&product=$t1&issuedby=$t2&version=$version"
        var text = UtilityString.getHTMLandParseSep(url, RegExp.prePattern)
        text = text.replace(
            "Graphics available at <a href=\"${MyApplication.nwsWPCwebsitePrefix}/basicwx/basicwx_wbg.php\"><u>www.wpc.ncep.noaa.gov/basicwx/basicwx_wbg.php</u></a>",
            ""
        )
        text = text.substring(text.indexOf('>') + 1)
        text = text.substring(text.indexOf('>') + 1)
        text = text.substring(text.indexOf('>') + 1)
        text = text.substring(text.indexOf('>') + 1)
        text = text.replace("^<br>".toRegex(), "")
        if (UIPreferences.nwsTextRemovelinebreaks && t1 != "RTP") {
            text = text.replace("<br><br>", "<BR><BR>")
            text = text.replace("<br>", " ")
        }
        return text
    }

    fun getSunriseSunset(context: Context, locNum: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val lat: String
        val lon: String
        if (!Location.isUS(locNumInt)) {
            val latArr = Location.getX(locNumInt).split(":")
            val lonArr = Location.getY(locNumInt).split(":")
            if (latArr.size > 2 && lonArr.size > 1) {
                lat = latArr[2]
                lon = lonArr[1]
            } else
                return ""
        } else {
            lat = Location.getX(locNumInt)
            lon = Location.getY(locNumInt)
        }
        val location = ExternalSunriseLocation(lat, lon)
        val calculator = ExternalSunriseSunsetCalculator(location, TimeZone.getDefault())
        val officialSunriseCal =
            calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        val officialSunsetCal = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        val srTime: String
        val ssTime: String
        var amStr = ""
        var pmStr = ""
        if (!DateFormat.is24HourFormat(context)) {
            amStr = "am"
            pmStr = "pm"
            srTime = (officialSunriseCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                        ' ',
                        '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        } else {
            srTime = (officialSunriseCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                        ' ',
                        '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        }
        return "Sunrise: $srTime$amStr   Sunset: $ssTime$pmStr"
    }

    fun getSunriseSunsetShort(context: Context, locNum: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val lat: String
        val lon: String
        if (!MyApplication.locations[locNumInt].isUS) {
            val latArr = MyApplication.colon.split(Location.getX(locNumInt))
            val lonArr = MyApplication.colon.split(Location.getY(locNumInt))
            if (latArr.size > 2 && lonArr.size > 1) {
                lat = latArr[2]
                lon = lonArr[1]
            } else
                return ""
        } else {
            lat = Location.getX(locNumInt)
            lon = Location.getY(locNumInt)
        }
        val location = ExternalSunriseLocation(lat, lon)
        val calculator = ExternalSunriseSunsetCalculator(location, TimeZone.getDefault())
        val officialSunriseCal =
            calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance())
        val officialSunsetCal = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance())
        val srTime: String
        val ssTime: String
        var amStr = ""
        var pmStr = ""
        if (!DateFormat.is24HourFormat(context)) {
            amStr = "am"
            pmStr = "pm"
            srTime = (officialSunriseCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                        ' ',
                        '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        } else {
            srTime = (officialSunriseCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunriseCal.get(Calendar.MINUTE))).replace(
                        ' ',
                        '0'
                    )
            ssTime = (officialSunsetCal.get(Calendar.HOUR_OF_DAY)).toString() + ":" +
                    String.format("%2s", (officialSunsetCal.get(Calendar.MINUTE))).replace(' ', '0')
        }
        return "$srTime$amStr / $ssTime$pmStr"
    }

    fun getStringFromURLS(strURL: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(strURL).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body()!!.byteStream())
            val br = BufferedReader(InputStreamReader(inputStream))
            var line: String? = br.readLine()
            while (line != null) {
                out.append(line)
                line = br.readLine()
            }
            br.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
        return out.toString()
    }

    fun getStringFromUrlUnsafe(strURL: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(strURL).build()
            val response = MyApplication.httpClientUnsafe!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body()!!.byteStream())
            val br = BufferedReader(InputStreamReader(inputStream))
            var line: String? = br.readLine()
            while (line != null) {
                out.append(line)
                line = br.readLine()
            }
            br.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
        return out.toString()
    }

    fun getStringFromURLSepS(strURL: String): String {
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(strURL).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val br =
                BufferedReader(InputStreamReader(BufferedInputStream(response.body()!!.byteStream())))
            var line: String? = br.readLine()
            while (line != null) {
                out.append(line)
                out.append(breakStr)
                line = br.readLine()
            }
            br.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return out.toString().replace(breakStr, "<br>")
    }

    /*fun getStringFromURLSepSUnsafe(strURL: String): String {
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(strURL).build()
            val response = MyApplication.httpClientUnsafe!!.newCall(request).execute()
            val br =
                BufferedReader(InputStreamReader(BufferedInputStream(response.body()!!.byteStream())))
            var line: String? = br.readLine()
            while (line != null) {
                out.append(line)
                out.append(breakStr)
                line = br.readLine()
            }
            br.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return out.toString().replace(breakStr, "<br>")
    }*/

    fun getBitmapFromURLS(url: String): Bitmap {
        return try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            BitmapFactory.decodeStream(BufferedInputStream(response.body()!!.byteStream()))
        } catch (e: Exception) {
            UtilityImg.getBlankBitmap()
        } catch (e: OutOfMemoryError) {
            UtilityImg.getBlankBitmap()
        }
    }

    // FIXME lots of camelcase fixes needed in this file
    fun getBitmapFromUrlUnsafe(url: String): Bitmap {
        return try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClientUnsafe!!.newCall(request).execute()
            BitmapFactory.decodeStream(BufferedInputStream(response.body()!!.byteStream()))
        } catch (e: Exception) {
            UtilityImg.getBlankBitmap()
        } catch (e: OutOfMemoryError) {
            UtilityImg.getBlankBitmap()
        }
    }

    fun getInputStreamFromURL(strURL: String): InputStream? {
        return try {
            val request = Request.Builder().url(strURL).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            response.body()!!.byteStream()
        } catch (e: IOException) {
            UtilityLog.HandleException(e)
            null
        }
    }

    /* fun getInputStreamFromUrlUnsafe(strURL: String): InputStream? {
        return try {
            val request = Request.Builder().url(strURL).build()
            val response = MyApplication.httpClientUnsafe!!.newCall(request).execute()
            response.body()!!.byteStream()
        } catch (e: IOException) {
            UtilityLog.HandleException(e)
            null
        }
    }*/

    //check api.weather.gov first//
    //https://w1.weather.gov/data/LOT/FTMLOT
    //text = ("${MyApplication.nwsWeatherGov}/data/"+Location.wfo.toUpperCase(Locale.US)+"/FTM"+Location.wfo.toUpperCase(Locale.US)).getHtmlSep()
    //https://forecast.weather.gov/product.php?site=NWS&issuedby=ARX&product=FTM&format=TXT
    //https://forecast.weather.gov/product.php?site=NWS&issuedby=SFX&product=FTM&format=txt&version=1&glossary=0
    //<span style="color:Red;">None issued by this office recently.</span>
    fun getRadarStatusMessage(context: Context, rid: String): String {
        val ridSmall = if (rid.length == 4) {
            rid.replace("^T".toRegex(), "")
        } else {
            rid
        }
        var text: String = getTextProduct(context, "FTM" + ridSmall.toUpperCase(Locale.US))
        UtilityLog.d("wx", "getRadarStatus api text: " + text)
        if (text == "") {
            text = ("${MyApplication.nwsWeatherGov}/data/" + ridSmall.toUpperCase(Locale.US) + "/FTM" + ridSmall.toUpperCase(Locale.US)).getHtmlSep()
        }
        if (text.contains("<!DOCTYPE html PUBLIC") || text.contains("Forbidden")) {
            UtilityLog.d("wx", "getRadarStatus testtext: " + text)
            //try another url...
            UtilityLog.d("wx", "getRadarStatus trying another url for FTM")
            text = UtilityString.getHTMLandParseSep(
                    "https://forecast.weather.gov/product.php?site=NWS&issuedby="+ridSmall.toUpperCase(Locale.US)+"&product=FTM&format=TXT&glossary=0",
                    RegExp.prePattern
            )
            if (text == "") {
                text = "None issued by "+ridSmall.toUpperCase(Locale.US)+" office recently."
            }

        }
        UtilityLog.d("wx", "getRadarStatus text: "+text)
        return text
    }
}
