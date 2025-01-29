//*****************************************************************************
// Copyright (c) 2021, 2022, 2023 joshua.tee@gmail.com. All rights reserved.
//
// Refer to the COPYING file of the official project for license.
//*****************************************************************************

package joshuatee.wx.objects

import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityLog
import okhttp3.Request

class URL(val url: String) {

    @Suppress("unused")
    fun getText(): String {
        UtilityLog.download("getHtml $url")
        val out = StringBuilder(5000)
        try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient.newCall(request).execute()
            return response.body.string()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return out.toString()
    }

//    @Suppress("unused")
//    fun getTextXmlAcceptHeader(): String {
//        UtilityLog.download("getStringFromUrlBaseNoHeader: $url")
//        val breakStr = "ABC123_456ZZ"
//        val out = StringBuilder(5000)
//        try {
//            val request = Request.Builder()
//                .url(url)
//                .header("User-Agent", GlobalVariables.HTTP_USER_AGENT)
//                //.addHeader("Accept", "application/vnd.noaa.dwml+xml;version=1")
//                .addHeader("Accept", "application/atom+xml")
//                .build()
//            val response = MyApplication.httpClient.newCall(request).execute()
//            return response.body.string()
//        } catch (e: Exception) {
//            UtilityLog.handleException(e)
//        }
//        return out.toString().replace(breakStr, "<br>")
//    }

    fun getBytes(): ByteArray {
        UtilityLog.download("getByte $url")
        return try {
            val request = Request.Builder().url(url).build()
            val response = MyApplication.httpClient.newCall(request).execute()
            response.body.bytes()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            ByteArray(0)
        }
    }
}
