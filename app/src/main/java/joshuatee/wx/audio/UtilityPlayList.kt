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

package joshuatee.wx.audio

import java.util.Locale
import android.content.Context
import android.view.View
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.DownloadText

object UtilityPlayList {

    private const val FORMAT_TIME_STR = "MM-dd HH:mm"

    fun add(context: Context, view: View, product: String, text: String) {
        val productUpperCase = product.uppercase(Locale.US)
        if (!UIPreferences.playlistStr.contains(productUpperCase)) {
            Utility.writePref(context, "PLAYLIST", UIPreferences.playlistStr + ":" + productUpperCase)
            UIPreferences.playlistStr = UIPreferences.playlistStr + ":" + productUpperCase
            PopupMessage(view, productUpperCase + " saved to playlist: " + text.length)
        } else {
            PopupMessage(view, productUpperCase + " already in playlist: " + text.length)
        }
        val formattedDate = ObjectDateTime.getDateAsString(FORMAT_TIME_STR)
        Utility.writePref(context, "PLAYLIST_$productUpperCase", text)
        Utility.writePref(context, "PLAYLIST_" + productUpperCase + "_TIME", formattedDate)
    }

    fun checkAndSave(context: Context, product: String, text: String) {
        val productUpperCase = product.uppercase(Locale.US)
        val formattedDate = ObjectDateTime.getDateAsString(FORMAT_TIME_STR)
        if (UIPreferences.playlistStr.contains(productUpperCase)) {
            Utility.writePref(context, "PLAYLIST_$productUpperCase", text)
            Utility.writePref(context, "PLAYLIST_" + productUpperCase + "_TIME", formattedDate)
        }
    }

    internal fun downloadAll(context: Context): String {
        var s = ""
        val items = RegExp.colon.split(UIPreferences.playlistStr)
        val formattedDate = ObjectDateTime.getDateAsString(FORMAT_TIME_STR)
        (1 until items.size).forEach {
            var text = DownloadText.byProduct(context, items[it])
            if (items[it].contains("SWO")) {
                text = text.substring(text.indexOf('>') + 1)
                text = text.replace("^<br>".toRegex(), "")
            }
            if (text != "") {
                Utility.writePref(context, "PLAYLIST_" + items[it], text)
                Utility.writePref(context, "PLAYLIST_" + items[it] + "_TIME", formattedDate)
            }
            s += items[it] + " " + text.length.toString() + " "
        }
        val date = ObjectDateTime.getDateAsString("MM-dd-yy HH:mm:ss")
        Utility.writePref(context, "PLAYLIST_STATUS", date + GlobalVariables.newline + s)
        return s
    }
}
