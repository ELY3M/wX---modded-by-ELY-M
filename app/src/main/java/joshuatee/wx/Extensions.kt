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

package joshuatee.wx.Extensions

import android.graphics.drawable.AnimationDrawable
import android.view.View
import java.util.regex.Pattern
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.*

fun Array<String>.safeGet(index: Int) = Utility.safeGet(this, index)

fun List<String>.safeGet(index: Int) = Utility.safeGet(this, index)

fun String.parse(match: String) = UtilityString.parse(this, match)

fun String.strip() = this.trim()

//fun String.ljust(padding: Int) = String.format("%-" + padding.toString() + "s", this)

fun String.condenseSpace()= this.replace("\\s+".toRegex(), " ")

fun String.removeHtml() = this.replace(Regex("\\<[^>]*>"),"")

fun String.removeLineBreaks() = this.replace("\n", "ABC123").replace("ABC123ABC123", "\n")
        .replace("ABC123", " ")
        .replace("  ", " ")

fun String.insert(index: Int, string: String) = StringBuilder(this).insert(index, string).toString()

fun String.parse(pattern: Pattern) = UtilityString.parse(this, pattern)

fun String.parseFirst(pattern: String) = UtilityString.parse(this, pattern)

fun String.parseFirst(pattern: Pattern) = UtilityString.parse(this, pattern)

fun String.parseColumn(match: String) = UtilityString.parseColumn(this, match)

fun String.parseColumn(pattern: Pattern) = UtilityString.parseColumn(this, pattern)

fun String.parseColumnMutable(match: String) = UtilityString.parseColumnMutable(this, match)

fun String.getImage() = UtilityNetworkIO.getBitmapFromUrl(this)

fun String.getHtml() = UtilityNetworkIO.getStringFromUrl(this)

fun String.getHtmlWithNewLine() = UtilityNetworkIO.getStringFromUrlWithNewLine(this)

fun String.getNwsHtml() = UtilityDownloadNws.getStringFromUrl(this)

fun String.getHtmlSep() = UtilityNetworkIO.getStringFromUrlWithSeparator(this)

fun String.parseColumnAll(pattern: Pattern) = UtilityString.parseColumnAll(this, pattern)

fun String.parseLastMatch(pattern: Pattern) = UtilityString.parseLastMatch(this, pattern)

fun String.parseLastMatch(match: String) = UtilityString.parseLastMatch(this, match)

fun String.parseMultiple(match: String, count: Int) = UtilityString.parseMultiple(this, match, count)

fun String.parseMultiple(match: Pattern, count: Int) = UtilityString.parseMultiple(this, match, count)

fun String.getInputStream() = UtilityNetworkIO.getInputStreamFromUrl(this)

fun View.setPadding(padding: Int) {
    this.setPadding(padding, padding, padding, padding)
}

fun Int.isEven() = this and 1 == 0

fun AnimationDrawable.startAnimation(img: TouchImageView2) = UtilityImgAnim.startAnimation(this, img)

fun AnimationDrawable.startAnimation(img: TouchImage) = UtilityImgAnim.startAnimation(this, img)
