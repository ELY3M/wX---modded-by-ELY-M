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

package joshuatee.wx.common

import java.util.regex.Pattern

object RegExp {

    val warningVtecPattern: Pattern =
        Pattern.compile("([A-Z0]\\.[A-Z]{3}\\.[A-Z]{4}\\.[A-Z]{2}\\.[A-Z]\\.[0-9]{4}\\.[0-9]{6}T[0-9]{4}Z\\-[0-9]{6}T[0-9]{4}Z)")
    val warningLatLonPattern: Pattern = Pattern.compile("\"coordinates\":\\[\\[(.*?)\\]\\]\\}")
    val watchPattern: Pattern = Pattern.compile("[om] Watch #([0-9]*?)</a>")
    val mcdPatternAlerts: Pattern =
        Pattern.compile("<strong><a href=./products/md/md.....html.>Mesoscale Discussion #(.*?)</a></strong>")
    val mcdPatternUtilSpc: Pattern = Pattern.compile(">Mesoscale Discussion #(.*?)</a>")
    val mpdPattern: Pattern = Pattern.compile(">MPD #(.*?)</a></strong>")
    val prePattern: Pattern = Pattern.compile("<pre.*?>(.*?)</pre>")
    const val PRE: String = "<pre.*?>(.*?)</pre>"
    val pre2Pattern: Pattern = Pattern.compile("<pre>(.*?)</pre>")
    const val PRE2: String = "<pre>(.*?)</pre>"

    val space: Pattern = Pattern.compile(" ")
    val colon: Pattern = Pattern.compile(":")
    val slash: Pattern = Pattern.compile("/")
    val comma: Pattern = Pattern.compile(",")
}
