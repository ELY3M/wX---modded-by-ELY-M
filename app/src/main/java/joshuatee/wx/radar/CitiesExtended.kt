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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.R
import joshuatee.wx.common.RegExp
import joshuatee.wx.util.UtilityIO

internal object CitiesExtended {

    var labels = arrayOf<String>()
    var lat = DoubleArray(0)
    var lon = DoubleArray(0)

    fun create(context: Context) {
        if (labels.isEmpty()) {
            var tokens: Array<String>
            val lines = UtilityIO.rawFileToStringArrayFromResource(context.resources, R.raw.cityall)
            labels = Array(lines.size) { "" }
            lat = DoubleArray(lines.size)
            lon = DoubleArray(lines.size)
            for (index in lines.indices) {
                tokens = RegExp.comma.split(lines[index])
                labels[index] = tokens[1].trim() + ", " + tokens[0]
                lat[index] = tokens[2].toDoubleOrNull() ?: 0.0
                lon[index] = (tokens[3].replace("-", "")).toDoubleOrNull() ?: 0.0
            }
        }
    }
}
