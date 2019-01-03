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

import java.io.IOException
import java.io.InputStream
import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import okhttp3.Request

import joshuatee.wx.Extensions.*

import joshuatee.wx.NEXRAD_PRODUCT_STRING
import joshuatee.wx.RegExp

class WXGLDownload {

    private var rid1 = ""
    private var prod = ""

    fun getRadarFile(
        context: Context,
        urlStr: String,
        rid1: String,
        prod: String,
        idxStr: String,
        TDWR: Boolean
    ): String {
        val ridPrefix = UtilityWXOGL.getRidPrefix(rid1, TDWR)
        this.rid1 = rid1
        this.prod = prod
        if (!prod.contains("L2")) {
            // per http://www.nws.noaa.gov/om/notification/scn16-16wng.htm
            // http://weather.noaa.gov is being retired 2016/06/15
            // replace with http://tgftp.nws.noaa.gov
            // FIXME HTTPS
            val inputStream = UtilityDownload.getInputStreamFromURL(
                MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" +
                        NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(Locale.US) + "/sn.last"
            )
            //val inputStream = UtilityDownload.getInputStreamFromUrlUnsafe(MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" +
            //        NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(Locale.US) + "/sn.last")
            val l3BaseFn = "nids"
            inputStream?.let {
                UtilityIO.saveInputStream(
                    context,
                    inputStream,
                    l3BaseFn + idxStr + "_d"
                )
            }
            UtilityFileManagement.moveFile(context, l3BaseFn + idxStr + "_d", l3BaseFn + idxStr)
        } else {
            if (urlStr == "") {
                val inputStream = getInputStreamFromURLL2(iowaMesoL2(rid1), prod)
                inputStream?.let { UtilityIO.saveInputStream(context, it, "l2_d$idxStr") }
            } else {
                val inputStream = getInputStreamFromURLL2(iowaMesoL2ARCHIVE(rid1, urlStr), prod)
                inputStream?.let { UtilityIO.saveInputStream(context, it, "l2_d$idxStr") }
            }
            UtilityFileManagement.moveFile(context, "l2_d$idxStr", "l2$idxStr", 1024)
        }
        return ridPrefix
    }

    // Download a list of files and return the list as a list of Strings
    // Determines of Level 2 or Level 3 and calls appropriate method
    fun getRadarFilesForAnimation(context: Context, frameCount: Int): List<String> {
        val nidsArr: List<String>
        val ridPrefix = UtilityWXOGL.getRidPrefix(rid1, prod)
        nidsArr = if (!prod.contains("L2")) {
            getLevel3FilesForAnimation(
                context,
                frameCount,
                prod,
                ridPrefix,
                rid1.toLowerCase(Locale.US)
            )
        } else {
            getLevel2FilesForAnimation(
                context,
                MyApplication.nwsRadarLevel2Pub + ridPrefix.toUpperCase(Locale.US) + rid1.toUpperCase(
                    Locale.US
                ) + "/",
                frameCount
            )
        }
        return nidsArr
    }

    // getRadarFilesForAnimation  getLevel3FilesForAnimation  getLevel2FilesForAnimation
    // listOfFiles
    // Download a list of files and return the list as a list of Strings
    // Determines of Level 2 or Level 3 and calls appropriate method
    private fun getLevel3FilesForAnimation(
        context: Context,
        frameCount: Int,
        prod: String,
        ridPrefix: String,
        rid1: String
    ): List<String> {
        // FIXME HTTPS there are 4 places below you need to add Unsafe
        val nidsArr = mutableListOf<String>()
        var htmlOut =
            (MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" + NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(
                Locale.US
            ) + "/").getHtml()
        var snFiles = htmlOut.parseColumn(RegExp.utilnxanimPattern1)
        var snDates = htmlOut.parseColumn(RegExp.utilnxanimPattern2)
        if (snDates.isEmpty()) {
            htmlOut =
                    (MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" + NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(
                        Locale.US
                    ) + "/").getHtml()
            snFiles = htmlOut.parseColumn(RegExp.utilnxanimPattern1)
            snDates = htmlOut.parseColumn(RegExp.utilnxanimPattern2)
        }
        if (snDates.isEmpty()) {
            htmlOut =
                    (MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" + NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(
                        Locale.US
                    ) + "/").getHtml()
            snFiles = htmlOut.parseColumn(RegExp.utilnxanimPattern1)
            snDates = htmlOut.parseColumn(RegExp.utilnxanimPattern2)
        }
        if (snDates.isEmpty()) {
            return listOf("")
        }
        var mostRecentSn = ""
        val mostRecentTime = snDates[snDates.size - 1]
        (0 until snDates.size - 1).filter { snDates[it] == mostRecentTime }
            .forEach { mostRecentSn = snFiles[it] }
        try {
            val seq = mostRecentSn.replace("sn.", "").toIntOrNull() ?: 0
            var j = 0
            var k = seq - frameCount + 1
            while (j < frameCount) {
                // files range from 0000 to 0250, if num is negative add 251
                var tmpK = k
                if (tmpK < 0)
                    tmpK += 251
                nidsArr.add("sn." + String.format("%4s", tmpK.toString()).replace(' ', '0'))
                k += 1
                j += 1
            }
            j = 0
            while (j < nidsArr.size) {
                val inputStream = UtilityDownload.getInputStreamFromURL(
                    MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" +
                            NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(
                        Locale.US
                    ) + "/" + nidsArr[j]
                )
                inputStream?.let { UtilityIO.saveInputStream(context, inputStream, nidsArr[j]) }
                j += 1
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return nidsArr
    }

    // Level 2: Download a list of files and return the list as a list of Strings
    private fun getLevel2FilesForAnimation(
        context: Context,
        baseUrl: String,
        frameCount: Int
    ): List<String> {
        val l2Arr = mutableListOf<String>()
        val tmpArr = (baseUrl + "dir.list").getHtmlSep().replace("<br>", " ").split(" ")
            .dropLastWhile { it.isEmpty() }
        if (tmpArr.isEmpty()) {
            return listOf("")
        }
        val arrLength = tmpArr.size
        var additionalAdd = 0
        val fnSize = tmpArr[tmpArr.size - 2].toIntOrNull() ?: 1
        val fnPrevSize = tmpArr[tmpArr.size - 4].toIntOrNull() ?: 1
        val ratio = fnSize.toFloat() / fnPrevSize.toFloat()
        if (ratio < 0.75) additionalAdd = 1
        (0 until frameCount).forEach { count ->
            val token = tmpArr.getOrNull(arrLength - (frameCount - count + additionalAdd) * 2 + 1)
            if (token != null) {
                l2Arr.add(token)
                val inputStream = UtilityDownload.getInputStreamFromURL(baseUrl + token)
                inputStream?.let { UtilityIO.saveInputStream(context, inputStream, token) }
            }
        }
        return l2Arr
    }

    fun iowaMesoL2(rid1: String): String {
        var fn: String
        val ridPrefix = UtilityWXOGL.getRidPrefix(rid1, false).toUpperCase(Locale.US)
        val baseUrl = MyApplication.nwsRadarLevel2Pub + ridPrefix + rid1 + "/"
        val tmpArr = (baseUrl + "dir.list").getHtmlSep().replace("<br>", " ").split(" ")
            .dropLastWhile { it.isEmpty() }
        if (tmpArr.size < 4) {
            return ""
        }
        fn = tmpArr[tmpArr.size - 1]
        val fnPrev = tmpArr[tmpArr.size - 3]
        val fnSize = tmpArr[tmpArr.size - 2].toIntOrNull() ?: 1
        val fnPrevSize = tmpArr[tmpArr.size - 4].toIntOrNull() ?: 1
        val ratio = fnSize.toFloat() / fnPrevSize.toFloat()
        if (ratio < 0.75) fn = fnPrev
        return baseUrl + fn
    }

    private fun getInputStreamFromURLL2(strURL: String, prod: String): InputStream? {
        // This method is used exclusively for download of partial binary files for Level 2
        // experimentation has shown that L2REF and L2VEL lowest tiles are at the start of the
        // file so "Range" HTTP header is used to download just what is needed based on prod
        // requested
        // testing of specific radar binary files
        //return UtilityIO.readRawFile(R.raw.l2missingradials);
        if (strURL == "") {
            return null
        }
        var byteEnd = "2450000"
        if (prod == "L2VEL")
            byteEnd = "3000000"
        return try {
            val request = Request.Builder().url(strURL).header("Range", "bytes=0-$byteEnd").build()
            val response = MyApplication.httpClient!!.newCall(request).execute() // was client
            response.body()!!.byteStream()
        } catch (e: IOException) {
            null
        }
    }

    private fun iowaMesoL2ARCHIVE(rid1: String, urlStr: String): String {
        val ridPrefix = UtilityWXOGL.getRidPrefix(rid1, false).toUpperCase(Locale.US)
        val baseUrl = "http://mesonet-nexrad.agron.iastate.edu/level2/raw/$ridPrefix$rid1/"
        val tmpStr = (baseUrl + "dir.list").getHtmlSep()
        val regexp = ".*?($ridPrefix$urlStr[0-9]).*?"
        return baseUrl + tmpStr.parse(regexp)
    }

    companion object {
        fun getNidsTab(context: Context, product: String, radarSite: String, fileName: String) {
            val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, false)
            val url =
                MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" + NEXRAD_PRODUCT_STRING[product]!! + "/SI." + ridPrefix + radarSite.toLowerCase() + "/sn.last"
            val inputstream = UtilityDownload.getInputStreamFromURL(url)
            inputstream?.let { UtilityIO.saveInputStream(context, it, fileName) }
        }
    }
}
