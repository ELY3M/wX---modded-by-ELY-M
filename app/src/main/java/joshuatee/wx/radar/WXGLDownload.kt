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

import java.io.IOException
import java.io.InputStream
import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import okhttp3.Request

import joshuatee.wx.Extensions.*

import joshuatee.wx.GlobalDictionaries
import joshuatee.wx.RegExp

class WXGLDownload {

    companion object {

        private const val nwsRadarLevel2Pub = "https://nomads.ncep.noaa.gov/pub/data/nccf/radar/nexrad_level2/"

        fun getRadarFile(context: Context, urlStr: String, radarSite: String, product: String, indexString: String, tdwr: Boolean): String {
            val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, tdwr)
            if (!product.contains("L2")) {
                val inputStream = getRadarFileUrl(radarSite, product, tdwr).getInputStream()
                val l3BaseFn = "nids"
                inputStream?.let { UtilityIO.saveInputStream(context, inputStream, l3BaseFn + indexString + "_d") }
                UtilityFileManagement.moveFile(context, l3BaseFn + indexString + "_d", l3BaseFn + indexString)
            } else {
                if (urlStr == "") {
                    val inputStream = getInputStreamFromUrlL2(getLevel2Url(radarSite), product)
                    inputStream?.let { UtilityIO.saveInputStream(context, it, "l2_d$indexString") }
                } else {
                    val inputStream = getInputStreamFromUrlL2(iowaMesoL2Archive(radarSite, urlStr), product)
                    inputStream?.let { UtilityIO.saveInputStream(context, it, "l2_d$indexString") }
                }
                UtilityFileManagement.moveFile(context, "l2_d$indexString", "l2$indexString", 1024)
            }
            return ridPrefix
        }

        // Download a list of files and return the list as a list of Strings
        // Determines of Level 2 or Level 3 and calls appropriate method
        fun getRadarFilesForAnimation(context: Context, frameCount: Int, radarSite: String, product: String): List<String> {
            val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, product)
            return if (!product.contains("L2")) {
                getLevel3FilesForAnimation(context, frameCount, product, ridPrefix, radarSite.toLowerCase(Locale.US))
            } else {
                getLevel2FilesForAnimation(context, nwsRadarLevel2Pub + ridPrefix.toUpperCase(Locale.US) + radarSite.toUpperCase(Locale.US) + "/", frameCount)
            }
        }

        // getRadarFilesForAnimation  getLevel3FilesForAnimation  getLevel2FilesForAnimation
        // listOfFiles
        // Download a list of files and return the list as a list of Strings
        // Determines of Level 2 or Level 3 and calls appropriate method
        private fun getLevel3FilesForAnimation(context: Context, frameCount: Int, product: String, ridPrefix: String, radarSite: String): List<String> {
            val fileList = mutableListOf<String>()
            var htmlOut = getRadarDirectoryUrl(radarSite, product, ridPrefix).getHtml()
            var snFiles = htmlOut.parseColumn(RegExp.utilnxanimPattern1)
            var snDates = htmlOut.parseColumn(RegExp.utilnxanimPattern2)
            if (snDates.isEmpty()) {
                htmlOut = getRadarDirectoryUrl(radarSite, product, ridPrefix).getHtml()
                snFiles = htmlOut.parseColumn(RegExp.utilnxanimPattern1)
                snDates = htmlOut.parseColumn(RegExp.utilnxanimPattern2)
            }
            if (snDates.isEmpty()) {
                htmlOut = getRadarDirectoryUrl(radarSite, product, ridPrefix).getHtml()
                snFiles = htmlOut.parseColumn(RegExp.utilnxanimPattern1)
                snDates = htmlOut.parseColumn(RegExp.utilnxanimPattern2)
            }
            if (snDates.isEmpty()) {
                return listOf("")
            }
            var mostRecentSn = ""
            val mostRecentTime = snDates.last()
            (0 until snDates.lastIndex).filter { snDates[it] == mostRecentTime }.forEach { mostRecentSn = snFiles[it] }
            try {
                val seq = mostRecentSn.replace("sn.", "").toIntOrNull() ?: 0
                var j = 0
                var k = seq - frameCount + 1
                while (j < frameCount) {
                    // files range from 0000 to 0250, if num is negative add 251
                    var tmpK = k
                    if (tmpK < 0) {
                        tmpK += 251
                    }
                    fileList.add("sn." + String.format("%4s", tmpK.toString()).replace(' ', '0'))
                    k += 1
                    j += 1
                }
                j = 0
                while (j < fileList.size) {
                    val url = getRadarDirectoryUrl(radarSite, product, ridPrefix) + fileList[j]
                    val inputStream = url.getInputStream()
                    inputStream?.let { UtilityIO.saveInputStream(context, inputStream, fileList[j]) }
                    j += 1
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            return fileList
        }

        // Level 2: Download a list of files and return the list as a list of Strings
        private fun getLevel2FilesForAnimation(context: Context, baseUrl: String, frameCount: Int): List<String> {
            val fileList = mutableListOf<String>()
            val list = (baseUrl + "dir.list").getHtmlSep().replace("<br>", " ").split(" ").dropLastWhile { it.isEmpty() }
            if (list.isEmpty()) {
                return listOf("")
            }
            val arrLength = list.size
            var additionalAdd = 0
            val fnSize = list[list.size - 2].toIntOrNull() ?: 1
            val fnPrevSize = list[list.size - 4].toIntOrNull() ?: 1
            val ratio = fnSize.toFloat() / fnPrevSize.toFloat()
            if (ratio < 0.75) additionalAdd = 1
            (0 until frameCount).forEach { count ->
                val token = list.getOrNull(arrLength - (frameCount - count + additionalAdd) * 2 + 1)
                if (token != null) {
                    fileList.add(token)
                    val inputStream = (baseUrl + token).getInputStream()
                    inputStream?.let { UtilityIO.saveInputStream(context, inputStream, token) }
                }
            }
            return fileList
        }

        // TODO refactor variable names
        fun getLevel2Url(radarSite: String): String {
            val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, false).toUpperCase(Locale.US)
            val baseUrl = "$nwsRadarLevel2Pub$ridPrefix$radarSite/"
            val list = (baseUrl + "dir.list").getHtmlSep().replace("<br>", " ").split(" ").dropLastWhile { it.isEmpty() }
            if (list.size < 4) {
                return ""
            }
            var fileName = list.last()
            val fnPrev = list[list.size - 3]
            val fnSize = list[list.size - 2].toIntOrNull() ?: 1
            val fnPrevSize = list[list.size - 4].toIntOrNull() ?: 1
            val ratio = fnSize.toFloat() / fnPrevSize.toFloat()
            if (ratio < 0.75) {
                fileName = fnPrev
            }
            return baseUrl + fileName
        }

        private fun getInputStreamFromUrlL2(url: String, product: String): InputStream? {
            // This method is used exclusively for download of partial binary files for Level 2
            // experimentation has shown that L2REF and L2VEL lowest tiles are at the start of the
            // file so "Range" HTTP header is used to download just what is needed based on prod
            // requested
            if (url == "") {
                return null
            }
            var byteEnd = "2450000"
            if (product == "L2VEL") {
                byteEnd = "3000000"
            }
            return try {
                val request = Request.Builder().url(url).header("Range", "bytes=0-$byteEnd").build()
                val response = MyApplication.httpClient!!.newCall(request).execute() // was client
                response.body!!.byteStream()
            } catch (e: IOException) {
                null
            }
        }

        private fun iowaMesoL2Archive(radarSite: String, url: String): String {
            val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, false).toUpperCase(Locale.US)
            val baseUrl = "http://mesonet-nexrad.agron.iastate.edu/level2/raw/$ridPrefix$radarSite/"
            val tmpStr = (baseUrl + "dir.list").getHtmlSep()
            val regexp = ".*?($ridPrefix$url[0-9]).*?"
            return baseUrl + tmpStr.parse(regexp)
        }

        fun getNidsTab(context: Context, product: String, radarSite: String, fileName: String) {
            val url = getRadarFileUrl(radarSite, product, false)
            val inputStream = url.getInputStream()
            inputStream?.let { UtilityIO.saveInputStream(context, it, fileName) }
        }

        fun getRadarFileUrl(radarSite: String, product: String, tdwr: Boolean): String {
            val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, tdwr)
            return MyApplication.nwsRadarPub + "SL.us008001/DF.of/DC.radar/" + GlobalDictionaries.nexradProductString[product] + "/SI." + ridPrefix + radarSite.toLowerCase(Locale.US) + "/sn.last"
        }

        private fun getRadarDirectoryUrl(radarSite: String, product: String, ridPrefix: String): String {
            return MyApplication.nwsRadarPub + "SL.us008001/DF.of/DC.radar/" + GlobalDictionaries.nexradProductString[product] + "/SI." + ridPrefix + radarSite.toLowerCase(Locale.US) + "/"
        }
    }
}
