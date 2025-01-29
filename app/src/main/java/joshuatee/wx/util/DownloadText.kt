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
//modded by ELY M. //keeping sun/moon

package joshuatee.wx.util

import java.util.Locale
import android.content.Context
import joshuatee.wx.misc.UtilityHourly
import joshuatee.wx.audio.UtilityPlayList
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.Location
import joshuatee.wx.common.RegExp
import joshuatee.wx.getHtml
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.getNwsHtml
import joshuatee.wx.parse
import joshuatee.wx.parseAcrossLines
import joshuatee.wx.removeHtml
import joshuatee.wx.removeLineBreaks
import joshuatee.wx.settings.Location.latLon
import joshuatee.wx.settings.UtilityLocation

@Suppress("SpellCheckingInspection")
object DownloadText {

    private const val USE_NWS_API = false

    fun byProduct(context: Context, prodF: String): String {
        var text: String
        // TODO FIXME rename to product
        val prod = prodF.uppercase(Locale.US)
        when {
            prod == "AFDLOC" -> text = byProduct(context, "afd" + Location.wfo.lowercase(Locale.US))
            prod == "HWOLOC" -> text = byProduct(context, "hwo" + Location.wfo.lowercase(Locale.US))
            prod == "VFDLOC" -> text = byProduct(context, "vfd" + Location.wfo.lowercase(Locale.US))
            //elys mod
	        prod == "SUNMOON" -> text = UtilityTimeSunMoon.getData(latLon)
            prod == "HOURLY" -> text = UtilityHourly.get(Location.currentLocation)[0]
            prod == "QPF94E" -> {
                val textUrl = "https://www.wpc.ncep.noaa.gov/qpf/ero.php?opt=curr&day=" + "1"
                val html = textUrl.getHtmlWithNewLine()
                text = UtilityString.extractPre(html).removeLineBreaks().removeHtml()
            }

            prod == "QPF98E" -> {
                val textUrl = "https://www.wpc.ncep.noaa.gov/qpf/ero.php?opt=curr&day=" + "2"
                val html = textUrl.getHtmlWithNewLine()
                text = UtilityString.extractPre(html).removeLineBreaks().removeHtml()
            }

            prod == "QPF99E" -> {
                val textUrl = "https://www.wpc.ncep.noaa.gov/qpf/ero.php?opt=curr&day=" + "3"
                val html = textUrl.getHtmlWithNewLine()
                text = UtilityString.extractPre(html).removeLineBreaks().removeHtml()
            }

            prod == "SWPC3DAY" -> text =
                (GlobalVariables.NWS_SWPC_WEBSITE_PREFIX + "/text/3-day-forecast.txt").getHtmlWithNewLine()

            prod == "SWPC27DAY" -> text =
                (GlobalVariables.NWS_SWPC_WEBSITE_PREFIX + "/text/27-day-outlook.txt").getHtmlWithNewLine()

            prod == "SWPCWWA" -> text =
                (GlobalVariables.NWS_SWPC_WEBSITE_PREFIX + "/text/advisory-outlook.txt").getHtmlWithNewLine()

            prod == "SWPCHIGH" -> text =
                (GlobalVariables.NWS_SWPC_WEBSITE_PREFIX + "/text/weekly.txt").getHtmlWithNewLine()
                    .removeLineBreaks()

            prod == "SWPCDISC" -> text =
                (GlobalVariables.NWS_SWPC_WEBSITE_PREFIX + "/text/discussion.txt").getHtmlWithNewLine()
                    .removeLineBreaks()

            prod == "SWPC3DAYGEO" -> text =
                (GlobalVariables.NWS_SWPC_WEBSITE_PREFIX + "/text/3-day-geomag-forecast.txt").getHtmlWithNewLine()

            prod.startsWith("MIATWS") -> text =
                "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/ftp/pub/forecasts/discussion/$prod".getHtmlWithNewLine()

            prod.contains("MIATCP") || prod.contains("MIATCM")
                    || prod.contains("MIATCD") || prod.contains("MIAPWS")
                    || prod.contains("MIAHS") || prod.startsWith("MIAWPC")
                    || prod.startsWith("HFOTCP") || prod.startsWith("HFOTCD")
                    || prod.startsWith("HFOTCM") || prod.startsWith("HFOPWS") -> {
                val url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/text/$prod.shtml"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeHtml()
            }

            prod.contains("MIAT") || prod == "HFOTWOCP" -> text =
                "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/ftp/pub/forecasts/discussion/$prod".getHtmlWithNewLine()
                    .removeLineBreaks()

            prod.startsWith("SCCNS") -> {
                val url =
                    "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/discussions/nfd" + prod.lowercase(
                        Locale.US
                    ).replace("ns", "") + ".html"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeHtml()
            }

            prod.contains("SPCMCD") -> {
                val no = prod.substring(6)
                val textUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/md/md$no.html"
                text = textUrl.getHtmlWithNewLine().parseAcrossLines(RegExp.PRE2)
            }

            prod.contains("SPCWAT") -> {
                val no = prod.substring(6)
                val textUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/watch/ww$no.html"
                text = textUrl.getHtmlWithNewLine().parseAcrossLines(RegExp.PRE2)
            }

            prod.contains("WPCMPD") -> {
                val no = prod.substring(6)
                val textUrl =
                    "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/metwatch/metwatch_mpd_multi.php?md=$no"
                text = textUrl.getHtmlWithNewLine().parseAcrossLines(RegExp.PRE2)
            }

            prod.startsWith("GLF") && !prod.contains("%") -> text = byProduct(context, "$prod%")
            prod.contains("FOCN45") -> text =
                "${GlobalVariables.TGFTP_WEBSITE_PREFIX}/data/raw/fo/focn45.cwwg..txt".getHtmlWithNewLine()
                    .removeLineBreaks()

            prod.startsWith("AWCN") -> text =
                ("${GlobalVariables.TGFTP_WEBSITE_PREFIX}/data/raw/aw/" + prod.lowercase(Locale.US) + ".cwwg..txt").getHtmlWithNewLine()
                    .removeLineBreaks()

            prod == "HSFSP" -> text =
                "https://tgftp.nws.noaa.gov/data/forecasts/marine/high_seas/south_hawaii.txt".getHtmlWithNewLine()

            prod.contains("NFD") -> {
                text =
                    (GlobalVariables.NWS_OPC_WEBSITE_PREFIX + "/mobile/mobile_product.php?id=" + prod.uppercase(
                        Locale.US
                    )).getHtml()
                text = Utility.fromHtml(text)
            }
            // use forecast but site=NWS
            prod.contains("OFF")
                    || prod == "UVICAC"
                    || prod == "RWRMX"
                    || prod.startsWith("TPT") -> {
                val product = prod.substring(0, 3)
                val site = prod.substring(3)
                val url =
                    "https://forecast.weather.gov/product.php?site=NWS&issuedby=$site&product=$product&format=txt&version=1&glossary=0"
                val html = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(html)
            }

            prod.startsWith("GLF") -> {
                val product = prod.substring(0, 3)
                val site = prod.substring(3).replace("%", "")
                val url =
                    "https://forecast.weather.gov/product.php?site=NWS&issuedby=$site&product=$product&format=txt&version=1&glossary=0"
                val html = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(html)
            }

            prod.contains("FWDDY1") -> {
                val url = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/fire_wx/fwdy1.html"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeLineBreaks().removeHtml()
            }

            prod.contains("FWDDY2") -> {
                val url = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/fire_wx/fwdy2.html"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeLineBreaks().removeHtml()
            }

            prod.contains("FWDDY38") -> {
                val url = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/exper/fire_wx/"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPre(text).removeLineBreaks().removeHtml()
            }

            prod.startsWith("VFD") -> {
                val t2 = prod.substring(3)
                text =
                    (GlobalVariables.NWS_AWC_WEBSITE_PREFIX + "/api/data/fcstdisc?cwa=K$t2" + "&type=afd").getHtmlWithNewLine()
                text = text.removeLineBreaks().removeHtml()
            }

            prod.contains("FPCN48") -> text =
                "${GlobalVariables.TGFTP_WEBSITE_PREFIX}/data/raw/fp/fpcn48.cwao..txt".getHtmlWithNewLine()

            prod.contains("PMDTHR") -> {
                val url =
                    GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX + "/products/predictions/threats/threats.php"
                text = url.getHtmlWithNewLine()
                text = text.parseAcrossLines("<div id=\"discDiv\">(.*?)</div>")
                text = text.removeLineBreaks().removeHtml()
            }

            prod.contains("USHZD37") -> {
                text =
                    "product discontinued via SCN23-101: Termination of the Weather Prediction Center Day 3-7 Hazards Outlook Discussion Effective November 15, 2023"
            }

            prod.contains("WEEK34") -> {
                val textUrl =
                    "https://www.cpc.ncep.noaa.gov/products/predictions/WK34/texts/week34fcst.txt"
                text = textUrl.getHtmlWithNewLine()
                text = text.removeHtml()
            }

            prod.contains("PMD30D") -> {
                val textUrl =
                    GlobalVariables.TGFTP_WEBSITE_PREFIX + "/data/raw/fx/fxus07.kwbc.pmd.30d.txt"
                text = textUrl.getHtmlWithNewLine()
                text = text.removeLineBreaks()
            }

            prod.contains("PMD90D") -> {
                val textUrl =
                    GlobalVariables.TGFTP_WEBSITE_PREFIX + "/data/raw/fx/fxus05.kwbc.pmd.90d.txt"
                text = textUrl.getHtmlWithNewLine()
                text = text.removeLineBreaks()
            }

            prod.contains("PMDHCO") -> {
                val textUrl =
                    GlobalVariables.TGFTP_WEBSITE_PREFIX + "/data/raw/fx/fxhw40.kwbc.pmd.hco.txt"
                text = textUrl.getHtmlWithNewLine()
            }

            prod.contains("PMDMRD") -> {
                val textUrl =
                    GlobalVariables.TGFTP_WEBSITE_PREFIX + "/data/raw/fx/fxus06.kwbc.pmd.mrd.txt"
                text = textUrl.getHtmlWithNewLine().removeLineBreaks()
            }

            prod.startsWith("FTM") -> {
                val radarSite = prod.substring(3, 6)
                val url =
                    "https://forecast.weather.gov/product.php?site=NWS&product=FTM&issuedby=$radarSite"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(text)
            }

            prod.startsWith("RWR") -> {
                val product = prod.substring(0, 3)
                val location = prod.substring(3).replace("%", "")
                val state = WfoSites.getState(location)
                val url =
                    "https://forecast.weather.gov/product.php?site=$location&issuedby=$state&product=$product"
                // https://forecast.weather.gov/product.php?site=ILX&issuedby=IL&product=RWR
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(text)
            }

            prod.startsWith("NSH") || (prod.startsWith("RTP") && prod.length == 6) -> {
                val product = prod.substring(0, 3)
                val location = prod.substring(3).replace("%", "")
                val url =
                    "https://forecast.weather.gov/product.php?site=$location&issuedby=$location&product=$product"
                text = url.getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(text)
            }

            prod.startsWith("RTP") && prod.length == 5 -> {
                val product = prod.substring(0, 3)
                val location = prod.substring(3, 5).replace("%", "")
                val url =
                    GlobalVariables.NWS_API_URL + "/products/types/$product/locations/$location"
                val html = url.getNwsHtml()
                val urlProd = html.parse("\"id\": \"(.*?)\"")
                val prodHtml = (GlobalVariables.NWS_API_URL + "/products/$urlProd").getNwsHtml()
                text = UtilityString.parseAcrossLines(prodHtml, "\"productText\": \"(.*?)\\}")
                text = text.replace("\\n\\n", "\n")
                text = text.replace("\\n", "\n")
            }

            prod.startsWith("CLI") -> {
                val location = prod.substring(3, 6).replace("%", "")
                val wfo = prod.substring(6).replace("%", "")
                // TODO each WFO has multiple locations for this product
                text =
                    "https://forecast.weather.gov/product.php?site=$wfo&product=CLI&issuedby=$location".getHtmlWithNewLine()
                text = UtilityString.extractPreLsr(text)
            }

            prod.contains("CTOF") -> text =
                "Celsius to Fahrenheit table" + GlobalVariables.newline + UtilityMath.celsiusToFahrenheitTable()

            else -> {
                // Feb 8 2020 Sat
                // The NWS API for text products has been unstable Since Wed Feb 5
                // resorting to alternatives
                val t1 = prod.substring(0, 3)
                val t2 = prod.substring(3).replace("%", "")
                if (USE_NWS_API) {
                    val url = GlobalVariables.NWS_API_URL + "/products/types/$t1/locations/$t2"
                    val html = url.getNwsHtml()
                    val urlProd = html.parse("\"id\": \"(.*?)\"")
                    val prodHtml = (GlobalVariables.NWS_API_URL + "/products/$urlProd").getNwsHtml()
                    text = UtilityString.parseAcrossLines(prodHtml, "\"productText\": \"(.*?)\\}")
                    if (!prod.startsWith("RTP")) {
                        text = text.replace("\\n\\n", "<BR>")
                        text = text.replace("\\n", " ")
                    } else {
                        text = text.replace("\\n", "\n")
                    }
                } else {
                    when (prod) {
                        "SWODY1" -> {
                            val url = "https://www.spc.noaa.gov/products/outlook/day1otlk.html"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        "SWODY2" -> {
                            val url = "https://www.spc.noaa.gov/products/outlook/day2otlk.html"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        "SWODY3" -> {
                            val url = "https://www.spc.noaa.gov/products/outlook/day3otlk.html"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        "SWOD48" -> {
                            val url = "https://www.spc.noaa.gov/products/exper/day4-8/"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        "PMDSPD", "PMDEPD", "PMDHI", "PMDAK", "QPFERD", "QPFHSD" -> {
                            val url =
                                "https://www.wpc.ncep.noaa.gov/discussions/hpcdiscussions.php?disc=" + prod.lowercase(
                                    Locale.US
                                )
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        "PMDSA" -> {
                            val url =
                                "https://www.wpc.ncep.noaa.gov/discussions/hpcdiscussions.php?disc=fxsa20"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        "PMDCA" -> {
                            val url =
                                "https://www.wpc.ncep.noaa.gov/discussions/hpcdiscussions.php?disc=fxca20"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }

                        else -> {
                            // https://forecast.weather.gov/product.php?site=DTX&issuedby=DTX&product=AFD&format=txt&version=1&glossary=0
                            val url = "https://forecast.weather.gov/product.php?site=" +
                                    t2 +
                                    "&issuedby=" +
                                    t2 +
                                    "&product=" +
                                    t1 +
                                    "&format=txt&version=1&glossary=0"
                            val html = url.getHtmlWithNewLine()
                            text = UtilityString.extractPreLsr(html).removeLineBreaks().removeHtml()
                        }
                    }
                }
            }
        }
        UtilityPlayList.checkAndSave(context, prod, text)
        return text
    }

    fun byProduct(prodF: String, version: Int): String {
        val prod = prodF.uppercase(Locale.US)
        val t1 = prod.substring(0, 3)
        val t2 = prod.substring(3)
        val url =
            "https://forecast.weather.gov/product.php?site=NWS&product=$t1&issuedby=$t2&version=$version"
        return if (t1 != "LSR") {
            url.getHtmlWithNewLine().parseAcrossLines(RegExp.PRE).removeLineBreaks()
        } else {
            url.getHtmlWithNewLine().parseAcrossLines(RegExp.PRE)
        }
    }

    fun radarStatusMessage(context: Context, radarSite: String): String {
        val ridSmall = if (radarSite.length == 4) {
            radarSite.replace("^T".toRegex(), "")
        } else {
            radarSite
        }
        return byProduct(context, "FTM" + ridSmall.uppercase(Locale.US))
    }
}
