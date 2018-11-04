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

package joshuatee.wx.audio

import java.util.Locale
import android.content.Context
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityTime

object UtilityPlayList {

    private const val FORMAT_TIME_STR = "MM-dd HH:mm"

    fun add(context: Context, view: View, prodF: String, text: String) {
        val prod = prodF.toUpperCase(Locale.US)
        if (!MyApplication.playlistStr.contains(prod)) {
            Utility.writePref(context, "PLAYLIST", MyApplication.playlistStr + ":" + prod)
            MyApplication.playlistStr = MyApplication.playlistStr + ":" + prod
            UtilityUI.makeSnackBar(view, prod + " saved to playlist: " + text.length)
        } else {
            UtilityUI.makeSnackBar(view, prod + " already in playlist: " + text.length)
        }
        val formattedDate = UtilityTime.getDateAsString(FORMAT_TIME_STR)
        Utility.writePref(context, "PLAYLIST_$prod", text)
        Utility.writePref(context, "PLAYLIST_" + prod + "_TIME", formattedDate)
    }

    fun checkAndSave(context: Context, prodF: String, text: String) {
        val prod = prodF.toUpperCase(Locale.US)
        val formattedDate = UtilityTime.getDateAsString(FORMAT_TIME_STR)
        if (MyApplication.playlistStr.contains(prod)) {
            Utility.writePref(context, "PLAYLIST_$prod", text)
            Utility.writePref(context, "PLAYLIST_" + prod + "_TIME", formattedDate)
        }
    }

    internal fun downloadAll(context: Context): String {
        var resultStr = ""
        val arr = MyApplication.colon.split(MyApplication.playlistStr)
        var text: String
        val formattedDate = UtilityTime.getDateAsString(FORMAT_TIME_STR)
        (1 until arr.size).forEach {
            text = UtilityDownload.getTextProduct(context, arr[it])
            if (arr[it].contains("SWO")) {
                text = text.substring(text.indexOf('>') + 1)
                text = text.replace("^<br>".toRegex(), "")
            }
            if (text != "") {
                Utility.writePref(context, "PLAYLIST_" + arr[it], text)
                Utility.writePref(context, "PLAYLIST_" + arr[it] + "_TIME", formattedDate)
            }
            resultStr = resultStr + arr[it] + " " + text.length.toString() + " "
        }
        val date = UtilityTime.getDateAsString("MM-dd-yy HH:mm:SS Z")
        Utility.writePref(context, "PLAYLIST_STATUS", date + MyApplication.newline + resultStr)
        return resultStr
    }
}
