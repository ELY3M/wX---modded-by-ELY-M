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

package joshuatee.wx.util

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.radar.LatLon
import okhttp3.Request

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

object UtilityDownloadNws {

    // FIXME remove nws from method name as class name already has it

    private const val USER_AGENT_STR =
        "Android ${MyApplication.packageNameAsString} ${MyApplication.emailAsString}"

    fun getHazardData(url: String): String {
        return getNwsStringFromUrlJson(url)
    }

    fun getLatLonForZone(zone: String): List<String> {
        var html = (MyApplication.nwsApiUrl + "/zones/forecast/" + zone.toUpperCase(Locale.US)).getNwsHtml()
        html = html.replace("\n", "")
        html = html.replace(" ", "")
        val polygonArr = html.parseColumn(RegExp.warningLatLonPattern)
        var test: List<String>
        var lat = "42.00"
        var lon = "-84.00"
        var polyTmp: String
        polygonArr.forEach { poly ->
            polyTmp = poly.replace("[", "").replace("]", "").replace(",", " ")
            test = polyTmp.split(" ").dropLastWhile { it.isEmpty() }
            if (test.size > 1) {
                lat = test[1]
                lon = test[0]
            }
        }
        return listOf(lat, lon)
    }

    fun getCap(sector: String): String = if (sector == "us") {
        getNwsStringFromUrlXml(MyApplication.nwsApiUrl + "/alerts/active?region_type=land")
    } else {
        getNwsStringFromUrlXml(
                MyApplication.nwsApiUrl + "/alerts/active/area/" + sector.toUpperCase(
                Locale.US
            )
        )
    }

    // https://forecast-v3.weather.gov/documentation?redirect=legacy
    // http://www.nws.noaa.gov/os/notification/pns16-35forecastgov.htm

    fun getNwsStringFromUrl(url: String): String =
        getNWSStringFromURLBase(url, "application/vnd.noaa.dwml+xml;version=1")

    private fun getNwsStringFromUrlJson(url: String): String =
        getNWSStringFromURLBase(url, "application/geo+json;version=1")

    fun getNwsStringFromUrlNoAcceptHeader(url: String): String =
            getNwsStringFromUrlBaseNoHeader(url)

    private fun getNWSStringFromURLBase(url: String, header: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT_STR)
                .addHeader("Accept", header)
                .build()
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
        }
        return out.toString()
    }

    private fun getNwsStringFromUrlBaseNoHeader(url: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT_STR)
                    .build()
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
        }
        return out.toString()
    }

    fun getNwsStringFromUrlSep(strURL: String): String {
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                .url(strURL)
                .header("User-Agent", USER_AGENT_STR)
                .addHeader("Accept", "application/vnd.noaa.dwml+xml;version=1")
                .build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body()!!.byteStream())
            val br = BufferedReader(InputStreamReader(inputStream))
            var line: String? = br.readLine()
            while (line != null) {
                out.append(line)
                line = br.readLine()
            }
            out.append(breakStr)
            br.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return out.toString().replace(breakStr, "<br>")
    }

    private fun getNwsStringFromUrlXml(url: String) =
        getNWSStringFromURLBase(url, "application/atom+xml")


    // Following methods derived from flutter port in response to June 2019 change
    fun getHourlyData(latLon: LatLon): String {
        val pointsData = getLocationPointData(latLon)
        val hourlyUrl = pointsData.parse("\"forecastHourly\": \"(.*?)\"")
        return hourlyUrl.getNwsHtml()
    }

    fun get7DayData(latLon: LatLon): String {
        val pointsData = getLocationPointData(latLon)
        val forecastUrl = pointsData.parse("\"forecast\": \"(.*?)\"")
        return forecastUrl.getNwsHtml()
    }

    fun get7DayUrl(latLon: LatLon): String {
        val pointsData = getLocationPointData(latLon)
        return pointsData.parse("\"forecast\": \"(.*?)\"")
    }

    private fun getLocationPointData(latLon: LatLon): String {
        val url = MyApplication.nwsApiUrl + "/points/" + latLon.latString + "," + latLon.lonString
        return url.getNwsHtml()
    }
}
