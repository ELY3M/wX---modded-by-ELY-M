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

fun String.parse(matchStr: String): String {
    return UtilityString.parseS(this, matchStr)
}

fun String.condenseSpace(): String {
    return this.replace("\\s+".toRegex(), " ")
}

fun String.truncate(size: Int): String {
    return UtilityStringExternal.truncate(this, size)
}

fun String.parse(p: Pattern): String {
    return UtilityString.parseS(this, p)
}

fun String.parseColumn(matchStr: String): List<String> {
    return UtilityString.parseColumnS(this, matchStr)
}

fun String.parseColumn(p: Pattern): List<String> {
    return UtilityString.parseColumnS(this, p)
}

fun String.getImage(): Bitmap {
    return if (Build.VERSION.SDK_INT > 20) {
        UtilityDownload.getBitmapFromURLS(this)
    } else {
        UtilityDownload.getBitmapFromUrlUnsafe(this)
    }
}

fun String.getHtml(): String {
    return if (Build.VERSION.SDK_INT > 20) {
        UtilityDownload.getStringFromURLS(this)
    } else {
        UtilityDownload.getStringFromUrlUnsafe(this)
    }
}

fun String.getHtmlUnsafe(): String {
    return UtilityDownload.getStringFromUrlUnsafe(this)
}

fun String.getNwsHtml(): String {
    return UtilityDownloadNws.getNWSStringFromURL(this)
}

fun String.getHtmlSep(): String {
    return UtilityDownload.getStringFromURLSepS(this)
}

fun String.parseColumnAll(p: Pattern): List<String> {
    return UtilityString.parseColumnAllS(this, p)
}

fun String.parseLastMatch(p: Pattern): String {
    return UtilityString.parseLastMatchS(this, p)
}

fun String.parseLastMatch(matchStr: String): String {
    return UtilityString.parseLastMatchS(this, matchStr)
}





