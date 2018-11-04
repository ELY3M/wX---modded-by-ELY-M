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

package joshuatee.wx.radar

import android.content.Context

import java.io.EOFException
import java.io.File
import java.io.IOException
import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

import joshuatee.wx.Extensions.*

import joshuatee.wx.NEXRAD_PRODUCT_STRING
import joshuatee.wx.RegExp

object UtilityWXOGL {

    fun getMeteogramUrl(obsSite: String): String {
        return "http://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=$obsSite"
    }

    fun getRidPrefix(rid1: String, prod: String): String {
        var ridPrefix = when (rid1) {
            "JUA" -> "t"
            "HKI", "HMO", "HKM", "HWA", "APD", "ACG", "AIH", "AHG", "AKC", "ABC", "AEC", "GUA" -> "p"
            else -> "k"
        }
        if (prod == "TV0" || prod == "TZL") ridPrefix = ""
        return ridPrefix
    }

    fun getRidPrefix(rid1: String, TDWR: Boolean): String {
        var ridPrefix = when (rid1) {
            "JUA" -> "t"
            "HKI", "HMO", "HKM", "HWA", "APD", "ACG", "AIH", "AHG", "AKC", "ABC", "AEC", "GUA" -> "p"
            else -> "k"
        }
        if (TDWR) ridPrefix = ""
        return ridPrefix
    }

    fun getVWP(context: Context, rid1: String): String {
        // http://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.48vwp/SI.kccx/
        val prod = "VWP"
        val l3BaseFn = "nidsVWP"
        val idxStr = "0"
        val ridPrefix = getRidPrefix(rid1, prod)
        val fh: File
        val inputStream = UtilityDownload.getInputStreamFromURL(MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" + NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(Locale.US) + "/sn.last")
        if (inputStream != null) {
            UtilityIO.saveInputStream(context, inputStream, l3BaseFn + idxStr + "_d")
        } else {
            return ""
        }
        fh = File(context.filesDir, l3BaseFn + idxStr + "_d")
        if (!fh.renameTo(File(context.filesDir, l3BaseFn + idxStr)))
            UtilityLog.d("wx", "Problem moving file to $l3BaseFn$idxStr")
        // process textual product
        val sb = StringBuilder(1500)
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, l3BaseFn + idxStr))
            dis.bigEndian = true
            // ADVANCE PAST WMO HEADER
            while (true) {
                if (dis.readShort().toInt() == -1) {
                    break
                }
            }
            dis.skipBytes(26) // 3 int (12) + 7*2 (14)
            while (true) {
                if (dis.readShort().toInt() == -1) {
                    break
                }
            }
            var b: Byte?
            var vSpotted = false
            sb.append("<font face=monospace><small>")
            try {
                while (!dis.isAtEndOfFile) {
                    b = dis.readByte()
                    if (android.os.Build.VERSION.SDK_INT >= 19) {
                        if (b.toChar() == 'V') {
                            vSpotted = true
                        }
                        if (Character.isAlphabetic(b.toInt()) || Character.isWhitespace(b.toInt()) || Character.isDigit(b.toInt()) || Character.isISOControl(b.toInt()) || Character.isDefined(b.toInt())) {
                            if (vSpotted) {
                                if (b == 0.toByte()) {
                                    sb.append("<br>")
                                } else {
                                    sb.append(String(byteArrayOf(b)))
                                }
                            }
                        }
                    }
                }
            } catch (e: EOFException) {
                UtilityLog.HandleException(e)
            } finally {
                try {
                    dis.close()
                } catch (e: IOException) {
                    UtilityLog.HandleException(e)
                }
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        sb.append("</small></font>")
        return sb.toString()
    }

    fun showTextProducts(lat: Double, lon: Double): String {
        var warningHTML = MyApplication.severeDashboardTor.valueGet() + MyApplication.severeDashboardSvr.valueGet() + MyApplication.severeDashboardEww.valueGet() + MyApplication.severeDashboardFfw.valueGet() + MyApplication.severeDashboardSmw.valueGet() + MyApplication.severeDashboardSps.valueGet()
        val urlList = warningHTML.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"")
        warningHTML = warningHTML.replace("\n", "")
        warningHTML = warningHTML.replace(" ", "")
        val polygonArr = warningHTML.parseColumn(RegExp.warningLatLonPattern)
        val vtecAl = warningHTML.parseColumn(RegExp.warningVtecPattern)
        var retStr = ""
        var testArr: List<String>
        var q = 0
        var notFound = true
        var polyCount = -1
        polygonArr.forEach { polys ->
            polyCount += 1
            if (vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith("0.CAN")) {
                val polyTmp = polys.replace("[", "").replace("]", "").replace(",", " ")
                testArr = polyTmp.split(" ").dropLastWhile { it.isEmpty() }
                val y = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }.toList()
                val x = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }.toList()
                if (y.size > 3 && x.size > 3 && x.size == y.size) {
                    val poly2 = ExternalPolygon.Builder()
                    x.indices.forEach { j -> poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat())) }
                    val polygon2 = poly2.build()
                    val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                    if (contains && notFound) {
                        retStr = urlList[q]
                        notFound = false
                    }
                }
            }
            q += 1
        }
        return retStr
    }
}
