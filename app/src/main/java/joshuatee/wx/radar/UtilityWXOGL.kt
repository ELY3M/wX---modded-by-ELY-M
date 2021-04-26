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

import joshuatee.wx.MyApplication
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

import joshuatee.wx.Extensions.*

import joshuatee.wx.RegExp
import joshuatee.wx.objects.ObjectWarning

object UtilityWXOGL {

    fun getMeteogramUrl(obsSite: String) = "https://www.nws.noaa.gov/mdl/gfslamp/meteo.php?BackHour=0&TempBox=Y&DewBox=Y&SkyBox=Y&WindSpdBox=Y&WindDirBox=Y&WindGustBox=Y&CigBox=Y&VisBox=Y&ObvBox=Y&PtypeBox=N&PopoBox=Y&LightningBox=Y&ConvBox=Y&sta=$obsSite"

    fun getRidPrefix(radarSite: String, product: String): String {
        var ridPrefix = when (radarSite) {
            "JUA" -> "t"
            "HKI", "HMO", "HKM", "HWA", "APD", "ACG", "AIH", "AHG", "AKC", "ABC", "AEC", "GUA" -> "p"
            else -> "k"
        }
        if (WXGLNexrad.isProductTdwr(product)) {
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
        val url = WXGLDownload.getRadarFileUrl(radarSite, product, false)
        val inputStream = url.getInputStream()
        if (inputStream != null) {
            UtilityIO.saveInputStream(context, inputStream, l3BaseFn + indexString + "_d")
        } else {
            return ""
        }
        val file = File(context.filesDir, l3BaseFn + indexString + "_d")
        if (!file.renameTo(File(context.filesDir, l3BaseFn + indexString)))
            UtilityLog.d("wx", "Problem moving file to $l3BaseFn$indexString")
        var output = ""
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, l3BaseFn + indexString))
            dis.bigEndian = true
            // ADVANCE PAST WMO HEADER
            while (true) if (dis.readShort().toInt() == -1) break
            dis.skipBytes(26) // 3 int (12) + 7*2 (14)
            while (true) if (dis.readShort().toInt() == -1) break
            var byte: Byte?
            var vSpotted = false
            output += "<font face=monospace><small>"
            try {
                while (!dis.isAtEndOfFile) {
                    byte = dis.readByte()
                    if (byte.toChar() == 'V') vSpotted = true
                    if (Character.isAlphabetic(byte.toInt()) || Character.isWhitespace(byte.toInt())
                            || Character.isDigit(byte.toInt()) || Character.isISOControl(byte.toInt()) || Character.isDefined(byte.toInt())) {
                        if (vSpotted) {
                            output += if (byte == 0.toByte()) {
                                "<br>"
                            } else {
                                String(byteArrayOf(byte))
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

//    fun showTextProducts(latLon: LatLon): String {
//        var html = MyApplication.severeDashboardTor.value + MyApplication.severeDashboardTst.value + MyApplication.severeDashboardFfw.value
//        MyApplication.radarWarningPolygons.forEach { if (it.isEnabled) html += it.storage.value }
//        // discard  "id": "https://api.weather.gov/alerts/NWS-IDP-PROD-3771044",            "type": "Feature",            "geometry": null,
//        // Special Weather Statements can either have a polygon or maybe not, need to strip out those w/o polygon
//        val urlList = html.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/urn.*?)\"").toMutableList()
//        val urlListCopy = urlList.toMutableList()
//        urlListCopy.forEach {
//            if (html.contains(Regex("\"id\"\\: ." + it + "\",\\s*\"type\": \"Feature\",\\s*\"geometry\": null"))) {
//                urlList.remove(it)
//            }
//        }
//        html = html.replace("\n", "").replace(" ", "")
//        val polygons = html.parseColumn(RegExp.warningLatLonPattern)
//        var string = ""
//        var notFound = true
//        polygons.forEachIndexed { urlIndex, polygon ->
//            val polygonTmp = polygon.replace("[", "").replace("]", "").replace(",", " ")
//            val latLons = LatLon.parseStringToLatLons(polygonTmp)
//            if (latLons.isNotEmpty()) {
//                val contains = ExternalPolygon.polygonContainsPoint(latLon, latLons)
//                if (contains && notFound) {
//                    string = urlList[urlIndex]
//                    notFound = false
//                }
//            }
//        }
//        return string
//    }

    fun showTextProducts(latLon: LatLon): String {
        var html = MyApplication.severeDashboardTor.value + MyApplication.severeDashboardTst.value + MyApplication.severeDashboardFfw.value
        MyApplication.radarWarningPolygons.forEach {
            if (it.isEnabled) html += it.storage.value
        }
//        for (data in ObjectPolygonWarning.polygonList) {
//            let it = ObjectPolygonWarning.polygonDataByType[data]!
//            if it.isEnabled {
//                warningChunk += it.storage.value
//            }
//        }
        val warnings = ObjectWarning.parseJson(html)
        var urlToOpen = ""
        var notFound = true
        for (w in warnings) {
            // qDebug() << w.geometry << " " << w.vtec << " " << w.sender;
            val latLons = w.getPolygonAsLatLons(1)
            if (latLons.isNotEmpty()) {
                val contains = ExternalPolygon.polygonContainsPoint(latLon, latLons)
                if (contains && notFound) {
                    urlToOpen = w.url
                    notFound = false
                }
            }
        }
        return urlToOpen
    }



}
