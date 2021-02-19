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

package joshuatee.wx.util

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.radar.LatLon
import okhttp3.Request

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences

object UtilityDownloadNws {

    private const val USER_AGENT_STR = "Android ${MyApplication.packageNameAsString} ${MyApplication.emailAsString}"
    private const val ACCEPT_STR = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"

    var forecastZone = ""

    fun getHazardData(url: String) = getStringFromUrlJson(url)

    /*fun getLatLonForZone(zone: String): List<String> {
        var html = (MyApplication.nwsApiUrl + "/zones/forecast/" + zone.toUpperCase(Locale.US)).getNwsHtml()
        html = html.replace("\n", "")
        html = html.replace(" ", "")
        val polygons = html.parseColumn(RegExp.warningLatLonPattern)
        var lat = "42.00"
        var lon = "-84.00"
        polygons.forEach { polygon ->
            val polyTmp = polygon.replace("[", "").replace("]", "").replace(",", " ")
            val items = polyTmp.split(" ").dropLastWhile { it.isEmpty() }
            if (items.size > 1) {
                lat = items[1]
                lon = items[0]
            }
        }
        return listOf(lat, lon)
    }*/

    fun getCap(sector: String) = if (sector == "us") {
        getStringFromUrlXml(MyApplication.nwsApiUrl + "/alerts/active?region_type=land")
    } else {
        getStringFromUrlXml(MyApplication.nwsApiUrl + "/alerts/active/area/" + sector.toUpperCase(Locale.US))
    }

    // https://forecast-v3.weather.gov/documentation?redirect=legacy
    // http://www.nws.noaa.gov/os/notification/pns16-35forecastgov.htm

//    fun getStringFromUrl(url: String) = getStringFromURLBase(url, "application/vnd.noaa.dwml+xml;version=1")
//
//    private fun getStringFromUrlJson(url: String) = getStringFromURLBase(url, "application/geo+json;version=1")

//    fun getStringFromUrl(url: String) = getStringFromURLBase(url, ACCEPT_STR)
//
//    private fun getStringFromUrlJson(url: String) = getStringFromURLBase(url, ACCEPT_STR)

    fun getStringFromUrl(url: String) = getStringFromUrlBaseNoAcceptHeader(url)

    private fun getStringFromUrlJson(url: String) = getStringFromUrlBaseNoAcceptHeader(url)

    fun getStringFromUrlNoAcceptHeader(url: String) = getStringFromUrlBaseNoHeader(url)

    private fun getStringFromURLBase(url: String, header: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT_STR)
                    //.addHeader("Accept", ACCEPT_STR)
                    .addHeader("Accept", header)
                    .build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                out.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return out.toString()
    }

    private fun getStringFromUrlBaseNoAcceptHeader(url: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT_STR)
                    .build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                out.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return out.toString()
    }

    private fun getStringFromUrlBaseNoHeader(url: String): String {
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT_STR)
                    .addHeader("Accept", ACCEPT_STR)
                    .build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                out.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return out.toString()
    }

    fun getStringFromUrlSep(strURL: String): String {
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(strURL)
                    .header("User-Agent", USER_AGENT_STR)
                    .addHeader("Accept", "application/vnd.noaa.dwml+xml;version=1")
                    .build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                out.append(line)
                line = bufferedReader.readLine()
            }
            out.append(breakStr)
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return out.toString().replace(breakStr, "<br>")
    }

    private fun getStringFromUrlXml(url: String) = getStringFromURLBase(url, "application/atom+xml")

    fun getHourlyData(latLon: LatLon): String {
        val pointsData = getLocationPointData(latLon)
        val hourlyUrl = pointsData.parse("\"forecastHourly\": \"(.*?)\"")
        return hourlyUrl.getNwsHtml()
    }

    fun get7DayData(latLon: LatLon): String {
        if (UIPreferences.useNwsApi) {
            val pointsData = getLocationPointData(latLon)
            val forecastUrl = pointsData.parse("\"forecast\": \"(.*?)\"")
            // set static var at object level for use elsewhere
            forecastZone = forecastUrl
            return forecastUrl.getNwsHtml()
        } else {
            return UtilityUS.getLocationHtml(latLon.latString, latLon.lonString)
        }
    }

    private fun getLocationPointData(latLon: LatLon) = (MyApplication.nwsApiUrl + "/points/" + latLon.latString + "," + latLon.lonString).getNwsHtml()
}
