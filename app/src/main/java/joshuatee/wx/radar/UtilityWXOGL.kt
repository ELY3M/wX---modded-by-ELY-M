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

import joshuatee.wx.GlobalDictionaries
import joshuatee.wx.RegExp

object UtilityWXOGL {

    fun getMeteogramUrl(obsSite: String): String {
        return "https://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=$obsSite"
    }

    fun getRidPrefix(radarSite: String, product: String): String {
        var ridPrefix = when (radarSite) {
            "JUA" -> "t"
            "HKI", "HMO", "HKM", "HWA", "APD", "ACG", "AIH", "AHG", "AKC", "ABC", "AEC", "GUA" -> "p"
            else -> "k"
        }
        // FIXME need TDWR method
        if (product.startsWith("TV") || product == "TZL" || product.startsWith("TZ") || product == "N1P" || product == "NTP" || product == "ET" || product == "VIL") {
            ridPrefix = ""
        }
        return ridPrefix
    }

    fun getRidPrefix(radarSite: String, tdwr: Boolean): String {
        var ridPrefix = when (radarSite) {
            "JUA" -> "t"
            "HKI", "HMO", "HKM", "HWA", "APD", "ACG", "AIH", "AHG", "AKC", "ABC", "AEC", "GUA" -> "p"
            else -> "k"
        }
        if (tdwr) {
            ridPrefix = ""
        }
        return ridPrefix
    }

    fun getVwp(context: Context, radarSite: String): String {
        // https://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.48vwp/SI.kccx/
        val product = "VWP"
        val l3BaseFn = "nidsVWP"
        val indexString = "0"
        val ridPrefix = getRidPrefix(radarSite, product)
        val file: File
        val inputStream = UtilityDownload.getInputStreamFromUrl(
                MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/"
                        + GlobalDictionaries.NEXRAD_PRODUCT_STRING[product]
                        + "/SI." + ridPrefix + radarSite.toLowerCase(Locale.US) + "/sn.last"
        )
        if (inputStream != null) {
            UtilityIO.saveInputStream(context, inputStream, l3BaseFn + indexString + "_d")
        } else {
            return ""
        }
        file = File(context.filesDir, l3BaseFn + indexString + "_d")
        if (!file.renameTo(File(context.filesDir, l3BaseFn + indexString)))
            UtilityLog.d("wx", "Problem moving file to $l3BaseFn$indexString")
        var output = ""
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, l3BaseFn + indexString))
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
            var byte: Byte?
            var vSpotted = false
            output += "<font face=monospace><small>"
            try {
                while (!dis.isAtEndOfFile) {
                    byte = dis.readByte()
                    if (android.os.Build.VERSION.SDK_INT >= 19) {
                        if (byte.toChar() == 'V') {
                            vSpotted = true
                        }
                        if (Character.isAlphabetic(byte.toInt()) || Character.isWhitespace(byte.toInt()) || Character.isDigit(
                                        byte.toInt()
                                ) || Character.isISOControl(byte.toInt()) || Character.isDefined(byte.toInt())
                        ) {
                            if (vSpotted) {
                                output += if (byte == 0.toByte()) {
                                    "<br>"
                                } else {
                                    String(byteArrayOf(byte))
                                }
                            }
                        }
                    }
                }
            } catch (e: EOFException) {
                UtilityLog.handleException(e)
            } finally {
                try {
                    dis.close()
                } catch (e: IOException) {
                    UtilityLog.handleException(e)
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        output += "</small></font>"
        return output
    }

    fun showTextProducts(lat: Double, lon: Double): String {
        var html = MyApplication.severeDashboardTor.value + MyApplication.severeDashboardTst.value + MyApplication.severeDashboardFfw.value
        MyApplication.radarWarningPolygons.forEach {
            if (it.isEnabled) {
                html += it.storage.value
            }
        }
        // val warningLatLonPattern: Pattern = Pattern.compile("\"coordinates\":\\[\\[(.*?)\\]\\]\\}")
        // discard  "id": "https://api.weather.gov/alerts/NWS-IDP-PROD-3771044",            "type": "Feature",            "geometry": null,
        // Special Weather Statements can either have a polygon or maybe not, need to strip out those w/o polygon
        val urlList = html.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"").toMutableList()
        val urlListCopy = urlList.toMutableList()
        urlListCopy.forEach {
            //if (html.contains(Regex("\"id\"\\: ." + it + "\",            \"type\": \"Feature\",            \"geometry\": null"))) {
            if (html.contains(Regex("\"id\"\\: ." + it + "\",\\s*\"type\": \"Feature\",\\s*\"geometry\": null"))) {
                urlList.remove(it)
            }
        }
        html = html.replace("\n", "")
        html = html.replace(" ", "")
        val polygons = html.parseColumn(RegExp.warningLatLonPattern)
        var retStr = ""
        var testArr: List<String>
        var q = 0
        var notFound = true
        var polyCount = -1
        polygons.forEach { polys ->
            polyCount += 1
            //if (vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith("0.CAN")) {
            //if (true) {
            val polyTmp = polys.replace("[", "").replace("]", "").replace(",", " ")
            testArr = polyTmp.split(" ").dropLastWhile { it.isEmpty() }
            val y = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 == 0 }
                    .map {
                        it.toDoubleOrNull() ?: 0.0
                    }.toList()
            val x = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 != 0 }
                    .map {
                        it.toDoubleOrNull() ?: 0.0
                    }.toList()
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                val poly2 = ExternalPolygon.Builder()
                x.indices.forEach { j ->
                    poly2.addVertex(
                            ExternalPoint(
                                    x[j].toFloat(),
                                    y[j].toFloat()
                            )
                    )
                }
                val polygon2 = poly2.build()
                val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                if (contains && notFound) {
                    retStr = urlList[q]
                    notFound = false
                }
            }
            //}
            q += 1
        }
        return retStr
    }
}
