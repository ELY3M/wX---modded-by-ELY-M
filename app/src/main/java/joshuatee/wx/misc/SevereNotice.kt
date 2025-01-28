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

package joshuatee.wx.misc

import android.graphics.Bitmap
import joshuatee.wx.getImage
import joshuatee.wx.common.RegExp
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityString

internal class SevereNotice(val type: PolygonType) {

    //
    // encapsulates a string array representation and bitmap list of current mcd, wat, or mpd
    //

    val bitmaps = mutableListOf<Bitmap>()
    var numbers = mutableListOf<String>()
    var typeAsString = when (type) {
        PolygonType.MCD -> "MCD"
        PolygonType.WATCH -> "WATCH"
        PolygonType.MPD -> "MPD"
        else -> ""
    }

    fun getBitmaps(html: String) {
        bitmaps.clear()
        numbers.clear()
        val zeroString = when (type) {
            PolygonType.MCD -> "<center>No Mesoscale Discussions are currently in effect."
            PolygonType.WATCH -> "<center><strong>No watches are currently valid"
            PolygonType.MPD -> "No MPDs are currently in effect."
            else -> ""
        }
        if (!html.contains(zeroString)) {
            val pattern = when (type) {
                PolygonType.MCD -> RegExp.mcdPatternUtilSpc
                PolygonType.WATCH -> RegExp.watchPattern
                PolygonType.MPD -> RegExp.mpdPattern
                else -> RegExp.mcdPatternUtilSpc
            }
            numbers = UtilityString.parseColumn(html, pattern).toMutableList()
        }
        numbers.indices.forEach { count ->
            if (type == PolygonType.WATCH) {
                numbers[count] =
                    String.format("%4s", numbers[count]).replace(' ', '0') // TODO FIXME
                // numbers[count] = To.stringPadLeftZeros(numbers[count], 4)
            }
            val url = when (type) {
                PolygonType.MCD -> "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/md/mcd" + numbers[count] + ".png"
                PolygonType.WATCH -> "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/watch/ww" + numbers[count] + "_radar.gif"
                PolygonType.MPD -> "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/metwatch/images/mcd" + numbers[count] + ".gif"
                else -> ""
            }
            bitmaps.add(url.getImage())
        }
    }

    fun getCount(): Int = bitmaps.size

    override fun toString(): String = typeAsString
}
