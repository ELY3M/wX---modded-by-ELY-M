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

package joshuatee.wx.util

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.LatLon
import okhttp3.Request
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getNwsHtml
import joshuatee.wx.parse

// https://www.weather.gov/documentation/services-web-api
// default format via accept header is GeoJSON: application/geo+json

object UtilityDownloadNws {

    private const val USER_AGENT = "Android ${GlobalVariables.PACKAGE_NAME} ${GlobalVariables.EMAIL}"
    private const val ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"

    fun getHazardData(url: String): String = url.getNwsHtml()

    // used for USWarningsWithRadarActivity / AlertSummary / CapAlert.initializeFromCap (XML) - CONUS wide alerts activity
    fun getCap(sector: String): String = if (sector == "us") {
        getStringFromUrlXml(GlobalVariables.NWS_API_URL + "/alerts/active?region_type=land")
    } else {
        getStringFromUrlXml(GlobalVariables.NWS_API_URL + "/alerts/active/area/" + sector.uppercase(Locale.US))
    }

    // used for CapAlert (XML)
    private fun getStringFromUrlXml(url: String): String {
        UtilityLog.download("getStringFromURLBase: $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .addHeader("Accept", "application/atom+xml")
                    .build()
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body.byteStream())
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

    // target for String.getNwsHtml()
    fun getStringFromUrlBaseNoAcceptHeader1(url: String): String {
        UtilityLog.download("getStringFromUrlBaseNoAcceptHeader1 getNwsHtml: $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build()
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body.byteStream())
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

    // PolygonWarning.kt
    // FYI - this is probably not needed and could use getStringFromUrlBaseNoAcceptHeader1 instead
    fun getStringFromUrlBaseNoHeader1(url: String): String {
        UtilityLog.download("getStringFromUrlBaseNoHeader1: $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .addHeader("Accept", ACCEPT_HEADER)
                    .build()
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body.byteStream())
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

    // used by CapAlert.kt
    fun getStringFromUrlSep(url: String): String {
        UtilityLog.download("getStringFromUrlSep: $url")
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .addHeader("Accept", "application/vnd.noaa.dwml+xml;version=1") // TODO FIXME, not valid defaulting to application/geo+json
                    .build()
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body.byteStream())
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

    fun getHourlyData(latLon: LatLon): String {
        val pointsData = getLocationPointData(latLon)
        val hourlyUrl = pointsData.parse("\"forecastHourly\": \"(.*?)\"")
        return hourlyUrl.getNwsHtml()
    }

    fun getHourlyOldData(latLon: LatLon): String {
        return UtilityIO.getHtml(
                "https://forecast.weather.gov/MapClick.php?lat=" +
                        latLon.latForNws + "&lon=" +
                        latLon.lonForNws + "&FcstType=digitalDWML"
        )
    }

    fun get7DayData(latLon: LatLon): String = if (UIPreferences.useNwsApi) {
        val pointsData = getLocationPointData(latLon)
        val forecastUrl = pointsData.parse("\"forecast\": \"(.*?)\"")
        forecastUrl.getNwsHtml()
    } else {
        UtilityUS.getLocationHtml(latLon)
    }

    private fun getLocationPointData(latLon: LatLon): String =
            (GlobalVariables.NWS_API_URL + "/points/" + latLon.latString + "," + latLon.lonString).getNwsHtml()
}
