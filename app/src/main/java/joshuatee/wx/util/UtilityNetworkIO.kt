/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
import okhttp3.Request

object UtilityNetworkIO {

    private fun getStringFromUrlNew(url: String, withNewLine: Boolean): String {
        Utility.logDownload("getStringFromUrlNew $withNewLine: $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                if (withNewLine) {
                    out.append(line + GlobalVariables.newline)
                } else {
                    out.append(line)
                }
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return out.toString()
    }

    // String.getHtml()
    // output has newlines removed
    fun getStringFromUrl(url: String): String = getStringFromUrlNew(url, false)

    // String.getHtmlWithNewLine()
    fun getStringFromUrlWithNewLine(url: String): String = getStringFromUrlNew(url, true)

    // String.getHtmlSep()
    // output has newlines removed and a different separator added
    fun getStringFromUrlWithSeparator(url: String): String {
        Utility.logDownload("getStringFromUrlWithSeparator: $url")
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                out.append(line)
                out.append(breakStr)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return out.toString().replace(breakStr, "<br>")
    }

    // String.getImage()
    fun getBitmapFromUrl(url: String): Bitmap = try {
        Utility.logDownload("getBitmapFromUrl: $url")
        val request = Request.Builder().url(url).build()
        val response = MyApplication.httpClient.newCall(request).execute()
        if (url.contains("hazards_d8_14_contours.png")) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeStream(BufferedInputStream(response.body!!.byteStream()), null, options)!!
        } else {
            BitmapFactory.decodeStream(BufferedInputStream(response.body!!.byteStream()))
        }
    } catch (e: Exception) {
        UtilityImg.getBlankBitmap()
    } catch (e: OutOfMemoryError) {
        UtilityImg.getBlankBitmap()
    }

    // String.getInputStream()
    // raw downloads - nexrad radar files, etc
    fun getInputStreamFromUrl(url: String): InputStream? = try {
        Utility.logDownload("getInputStreamFromUrl: $url")
        val request = Request.Builder().url(url).build()
        val response = MyApplication.httpClient.newCall(request).execute()
        response.body!!.byteStream()
    } catch (e: IOException) {
        UtilityLog.handleException(e)
        null
    }
}
