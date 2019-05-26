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

internal object UtilityCitiesExtended {

    private var initialized = false
    var cities = mutableListOf<CityExt>()

    fun create(context: Context) {
        if (!initialized) {
            cities = mutableListOf()
            initialized = true
            val text: String
            val lines: List<String>
            var tmpArr: Array<String>
            val xmlFileInputStream = context.resources.openRawResource(R.raw.cityall)
            text = UtilityIO.readTextFile(xmlFileInputStream)
            lines = text.split("\n").dropLastWhile { it.isEmpty() }
            lines.forEach {
                tmpArr = MyApplication.comma.split(it)
                if (tmpArr.size > 3) {
                    cities.add(
                            CityExt(
                                    tmpArr[0], tmpArr[1].toDoubleOrNull()
                                    ?: 0.0, (tmpArr[2].replace("-", "")).toDoubleOrNull() ?: 0.0
                            )
                    )
                } else {
                    cities.add(
                            CityExt(
                                    tmpArr[0], tmpArr[1].toDoubleOrNull()
                                    ?: 0.0, (tmpArr[2].replace("-", "")).toDoubleOrNull() ?: 0.0
                            )
                    )
                }
            }
        }
    }
}
