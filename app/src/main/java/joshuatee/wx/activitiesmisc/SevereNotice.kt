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

package joshuatee.wx.activitiesmisc

import android.graphics.Bitmap
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.MyApplication

import joshuatee.wx.RegExp
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityString
import java.util.regex.Pattern

internal class SevereNotice(val type: PolygonType) {

    // encapsulates a string array representation and bitmap arraylist of current mcd, wat, or mpd

    val bitmaps = mutableListOf<Bitmap>()
    var strList = mutableListOf<String>()
    var pattern: Pattern = Pattern.compile("")
    private var typeAsString = ""

    init {
        when (type) {
            PolygonType.MCD -> typeAsString = "MCD"
            PolygonType.WATCH -> typeAsString = "WATCH"
            PolygonType.MPD -> typeAsString = "MPD"
            else -> {
            }
        }
    }

    fun getBitmaps(dataAsStringMCD: String) {
        var comp = ""
        var url = ""
        when (type) {
            PolygonType.MCD -> comp = "<center>No Mesoscale Discussions are currently in effect."
            PolygonType.WATCH -> comp = "<center><strong>No watches are currently valid"
            PolygonType.MPD -> comp = "No MPDs are currently in effect."
            else -> {
            }
        }
        if (!dataAsStringMCD.contains(comp)) {
            when (type) {
                PolygonType.MCD -> pattern = RegExp.mcdPatternUtilspc
                PolygonType.WATCH -> pattern = RegExp.watchPattern
                PolygonType.MPD -> pattern = RegExp.mpdPattern
                else -> {
                }
            }
            strList = UtilityString.parseColumnAl(dataAsStringMCD, pattern)
        }
        strList.indices.forEach { count ->
            when (type) {
                PolygonType.MCD -> url = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd" +
                        strList[count] + ".gif"
                PolygonType.WATCH -> {
                    strList[count] = String.format("%4s", strList[count]).replace(' ', '0')
                    url = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" +
                            strList[count] + "_radar.gif"
                }
                PolygonType.MPD -> url =
                    "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd" +
                            strList[count] + ".gif"
                else -> {
                }
            }
            bitmaps.add(url.getImage())
        }
    }

    override fun toString() = typeAsString
}

