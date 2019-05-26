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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.util.UtilityIO

internal object UtilityCountyLabels {

    private var initialized = false
    var countyName = Array(1) { "" }
    var countyLat = DoubleArray(1)
    var countyLon = DoubleArray(1)

    fun create(context: Context) {
        if (!initialized) {
            initialized = true
            val text: String
            val lines: List<String>
            var tmpArr: Array<String>
            val xmlFileInputStream = context.resources.openRawResource(R.raw.gaz_counties_national)
            text = UtilityIO.readTextFile(xmlFileInputStream)
            lines = text.split("\n").dropLastWhile { it.isEmpty() }
            countyName = Array(lines.size) { "" }
            countyLat = DoubleArray(lines.size)
            countyLon = DoubleArray(lines.size)
            lines.indices.forEach {
                tmpArr = MyApplication.comma.split(lines[it])
                countyName[it] = tmpArr[1]
                countyLat[it] = tmpArr[2].toDoubleOrNull() ?: 0.0
                countyLon[it] = -1.0 * (tmpArr[3].toDoubleOrNull() ?: 0.0)
            }
        }
    }
}
