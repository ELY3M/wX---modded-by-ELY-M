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

object UtilityDownloadNWS {

    private const val USER_AGENT_STR = "Android joshuatee.wx joshua.tee@gmail.com"

    internal fun get7DayJSON(location: LatLon): String {
        var x = location.latString
        var y = location.lonString
        x = UtilityMath.latLonFix(x)
        y = UtilityMath.latLonFix(y)
        return UtilityDownloadNWS.getNWSStringFromURLJSON("https://api.weather.gov/points/$x,$y/forecast")
    }

    fun getHazardData(url: String): String {
        return UtilityDownloadNWS.getNWSStringFromURLJSON(url)
    }

    fun get7DayURL(x: String, y: String): String = "https://forecast-v3.weather.gov/point/$x,$y"

    fun getLatLonForZone(zone: String): List<String> {
        var html = getNWSStringFromURL("https://api.weather.gov/zones/forecast/" + zone.toUpperCase(Locale.US))
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

    fun getCAP(sector: String): String = if (sector == "us") {
        getNWSStringFromURLXML("https://api.weather.gov/alerts/active?region_type=land")
    } else {
        //getNWSStringFromURLXML("https://api.weather.gov/alerts/active?state=" + sector.toUpperCase(Locale.US))
        getNWSStringFromURLXML("https://api.weather.gov/alerts/active/area/" + sector.toUpperCase(Locale.US))
    }

    // https://forecast-v3.weather.gov/documentation?redirect=legacy
    // http://www.nws.noaa.gov/os/notification/pns16-35forecastgov.htm

    fun getNWSStringFromURL(url: String): String = getNWSStringFromURLBase(url, "application/vnd.noaa.dwml+xml;version=1")

    private fun getNWSStringFromURLJSON(url: String) = getNWSStringFromURLBase(url, "application/geo+json;version=1")

    //private fun getNWSStringFromURLLDJSON(url: String) = getNWSStringFromURLBase(url, "application/ld+json;version=1")

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

    fun getNWSStringFromURLSep(strURL: String): String {
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

    private fun getNWSStringFromURLXML(url: String) = getNWSStringFromURLBase(url, "application/atom+xml")
}
