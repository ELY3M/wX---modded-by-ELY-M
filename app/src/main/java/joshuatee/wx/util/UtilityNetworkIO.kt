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
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalVariables
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

object UtilityNetworkIO {

    private const val USER_AGENT =
        "Android ${GlobalVariables.PACKAGE_NAME} ${GlobalVariables.EMAIL}"
    private const val ACCEPT_HEADER =
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"

    // String.getImage()
    fun getBitmapFromUrl(url: String): Bitmap = try {
        UtilityLog.download("getBitmapFromUrl: $url")
        val request = Request.Builder().url(url).header("User-Agent", USER_AGENT).build()
        val response = MyApplication.httpClient.newCall(request).execute()
        if (url.contains("hazards_d8_14_contours.png")) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeStream(
                BufferedInputStream(response.body.byteStream()),
                null,
                options
            ) ?: UtilityImg.getBlankBitmap()
        } else {
            BitmapFactory.decodeStream(BufferedInputStream(response.body.byteStream()))
                ?: UtilityImg.getBlankBitmap()
        }
    } catch (e: Exception) {
        UtilityImg.getBlankBitmap()
    } catch (e: OutOfMemoryError) {
        UtilityImg.getBlankBitmap()
    }

    // String.getInputStream()
    // raw downloads - nexrad radar files, etc
    fun getInputStreamFromUrl(url: String): InputStream? = try {
        UtilityLog.download("getInputStreamFromUrl: $url")
        val request = Request.Builder().url(url).header("User-Agent", USER_AGENT).build()
        val response = MyApplication.httpClient.newCall(request).execute()
        response.body.byteStream()
    } catch (e: IOException) {
        UtilityLog.handleException(e)
        null
    }

    // used for CapAlert (XML)
    fun getStringFromUrlXml(url: String): String {
        UtilityLog.download("getStringFromUrlXml: $url")
        val httpUrl = url.toHttpUrlOrNull() ?: return ""
        val request = Request.Builder().url(httpUrl).header("User-Agent", USER_AGENT)
            .addHeader("Accept", "application/atom+xml").build()
        return requestToString(request)
    }

    // String.getHtml()
    // output has newlines removed
    fun getStringFromUrl(url: String): String = getStringFromUrlNew(url, false)

    // String.getHtmlWithNewLine()
    fun getStringFromUrlWithNewLine(url: String): String = getStringFromUrlNew(url, true)

    private fun getStringFromUrlNew(url: String, withNewLine: Boolean): String {
        UtilityLog.download("getStringFromUrlNew $withNewLine: $url")
        val httpUrl = url.toHttpUrlOrNull() ?: return ""
        val request = Request.Builder().url(httpUrl).header("User-Agent", USER_AGENT).build()
        return requestToString(request, withNewLine)
    }

    // PolygonWarning.kt
    // FYI - this is probably not needed and could use getStringFromUrlBaseNoAcceptHeader1 instead
    fun getStringFromUrlBaseNoHeader1(url: String): String {
        UtilityLog.download("getStringFromUrlBaseNoHeader1: $url")
        val httpUrl = url.toHttpUrlOrNull() ?: return ""
        val request = Request.Builder().url(httpUrl).header("User-Agent", USER_AGENT)
            .addHeader("Accept", ACCEPT_HEADER).build()
        return requestToString(request)
    }

    // used by CapAlert.kt
    fun getStringFromUrlSep(url: String): String {
        UtilityLog.download("getStringFromUrlSep: $url")
        val httpUrl = url.toHttpUrlOrNull() ?: return ""
        val request = Request.Builder().url(httpUrl).header("User-Agent", USER_AGENT)
            .addHeader("Accept", "application/vnd.noaa.dwml+xml;version=1").build()
        return requestToString(request)
    }

    private fun requestToString(request: Request, withNewLine: Boolean = false): String {
        val output = StringBuilder(5000)
        try {
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                if (withNewLine) {
                    output.append(line + GlobalVariables.newline)
                } else {
                    output.append(line)
                }
                line = bufferedReader.readLine()
            }
//            output.append(breakString)
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return output.toString()
    }
}
