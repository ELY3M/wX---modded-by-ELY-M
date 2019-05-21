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

package joshuatee.wx.Extensions

import android.graphics.Bitmap
import android.os.Build
import java.util.regex.Pattern

import joshuatee.wx.util.UtilityString
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.external.UtilityStringExternal

fun String.parse(match: String): String {
    return UtilityString.parse(this, match)
}

fun String.condenseSpace(): String {
    return this.replace("\\s+".toRegex(), " ")
}

fun String.truncate(size: Int): String {
    return UtilityStringExternal.truncate(this, size)
}

fun String.parse(pattern: Pattern): String {
    return UtilityString.parse(this, pattern)
}

fun String.parseColumn(match: String): List<String> {
    return UtilityString.parseColumn(this, match)
}

fun String.parseColumn(pattern: Pattern): List<String> {
    return UtilityString.parseColumn(this, pattern)
}

fun String.getImage(): Bitmap {
    return if (Build.VERSION.SDK_INT > 20) {
        UtilityDownload.getBitmapFromUrl(this)
    } else {
        UtilityDownload.getBitmapFromUrlUnsafe(this)
    }
}

fun String.getHtml(): String {
    return if (Build.VERSION.SDK_INT > 20) {
        UtilityDownload.getStringFromUrl(this)
    } else {
        UtilityDownload.getStringFromUrlUnsafe(this)
    }
}

fun String.getHtmlUnsafe(): String {
    return UtilityDownload.getStringFromUrlUnsafe(this)
}

fun String.getNwsHtml(): String {
    return UtilityDownloadNws.getStringFromUrl(this)
}

fun String.getHtmlSep(): String {
    return UtilityDownload.getStringFromUrlWithSeparator(this)
}

fun String.parseColumnAll(pattern: Pattern): List<String> {
    return UtilityString.parseColumnAll(this, pattern)
}

fun String.parseLastMatch(pattern: Pattern): String {
    return UtilityString.parseLastMatch(this, pattern)
}

fun String.parseLastMatch(match: String): String {
    return UtilityString.parseLastMatch(this, match)
}





