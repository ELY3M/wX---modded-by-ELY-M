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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.objects.ObjectWarning
import joshuatee.wx.objects.PolygonType

class SevereWarning(private val type: PolygonType) {

    //
    // encapsulates VTEC data and count for tst,tor, or ffw
    //

    var warningList = listOf<ObjectWarning>()

    fun getName() = when (type) {
        PolygonType.TOR -> "Tornado Warning"
        PolygonType.TST -> "Severe Thunderstorm Warning"
        PolygonType.FFW -> "Flash Flood Warning"
        else -> ""
    }

    fun generateString() {
        val html = ObjectWarning.getBulkData(type)
        warningList = ObjectWarning.parseJson(html)
    }

    fun getCount(): Int {
        // TODO FIXME
        var i = 0
        for (s in warningList) {
            if (s.isCurrent) {
                i += 1
            }
        }
        return i
    }
}
