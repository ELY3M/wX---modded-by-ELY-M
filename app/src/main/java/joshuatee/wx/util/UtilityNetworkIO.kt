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
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import joshuatee.wx.MyApplication
import okhttp3.Request

object UtilityNetworkIO {

    fun getStringFromUrl(url: String): String {
        UtilityLog.d("wx", "getStringFromUrl: $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(url).build()
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
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return out.toString()
    }

    fun getStringFromUrlWithNewLine(url: String): String {
        UtilityLog.d("wx", "getStringFromUrlWithNewLine: $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val inputStream = BufferedInputStream(response.body!!.byteStream())
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                out.append(line + MyApplication.newline)
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

    fun getStringFromUrlWithSeparator(url: String): String {
        UtilityLog.d("wx", "getStringFromUrlWithSeparator: $url")
        val breakStr = "ABC123_456ZZ"
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            val bufferedReader = BufferedReader(InputStreamReader(BufferedInputStream(response.body!!.byteStream())))
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

    fun getBitmapFromUrl(url: String): Bitmap = try {
            UtilityLog.d("wx", "getBitmapFromUrl: $url")
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            BitmapFactory.decodeStream(BufferedInputStream(response.body!!.byteStream()))
        } catch (e: Exception) {
            UtilityImg.getBlankBitmap()
        } catch (e: OutOfMemoryError) {
            UtilityImg.getBlankBitmap()
        }

   /* fun getBitmapFromUrlUnsafe(url: String): Bitmap = try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClientUnsafe!!.newCall(request).execute()
            BitmapFactory.decodeStream(BufferedInputStream(response.body!!.byteStream()))
    } catch (e: Exception) {
            UtilityImg.getBlankBitmap()
        } catch (e: OutOfMemoryError) {
            UtilityImg.getBlankBitmap()
        }*/

    fun getInputStreamFromUrl(url: String): InputStream? = try {
            UtilityLog.d("wx", "getInputStreamFromUrl: $url")
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient!!.newCall(request).execute()
            response.body!!.byteStream()
    } catch (e: IOException) {
            UtilityLog.handleException(e)
            null
    }
}
