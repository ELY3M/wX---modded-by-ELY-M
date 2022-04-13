/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

package joshuatee.wx.objects

import joshuatee.wx.MyApplication
import java.nio.ByteBuffer

enum class GeographyType constructor(var relativeBuffer: ByteBuffer, var count: Int, var color: Int, var pref: Boolean, var lineWidth: Int) {

    STATE_LINES(
        MyApplication.stateRelativeBuffer,
        MyApplication.countState,
        MyApplication.radarColorState,
        true,
        MyApplication.radarStateLineSize
    ),
    COUNTY_LINES(
        MyApplication.countyRelativeBuffer,
        MyApplication.countCounty,
        MyApplication.radarColorCounty,
        MyApplication.radarCounty,
        MyApplication.radarCountyLineSize
    ),
    LAKES(
        MyApplication.lakesRelativeBuffer,
        MyApplication.countLakes,
        MyApplication.radarColorLakes,
        MyApplication.radarLakes,
        MyApplication.radarLakeLineSize
    ),
    HIGHWAYS(
        MyApplication.hwRelativeBuffer,
        MyApplication.countHw,
        MyApplication.radarColorHw,
        MyApplication.radarHw,
        MyApplication.radarHwLineSize
    ),
    HIGHWAYS_EXTENDED(
        MyApplication.hwExtRelativeBuffer,
        MyApplication.countHwExt,
        MyApplication.radarColorHwExt,
        MyApplication.radarHwEnhExt,
        MyApplication.radarHwExtLineSize
    ),
    CITIES(ByteBuffer.allocate(0), 0, MyApplication.radarColorCity, MyApplication.radarCities, 0),
    COUNTY_LABELS(ByteBuffer.allocate(0), 0, MyApplication.radarColorCountyLabels, MyApplication.radarCountyLabels, 0),
    NONE(ByteBuffer.allocateDirect(0), 0, 0, false, 0);

    // FIXME refresh rest of values
    companion object {
        fun refresh() {
            HIGHWAYS_EXTENDED.relativeBuffer = MyApplication.hwExtRelativeBuffer
            STATE_LINES.relativeBuffer = MyApplication.stateRelativeBuffer
            STATE_LINES.count = MyApplication.countState
            COUNTY_LINES.relativeBuffer = MyApplication.countyRelativeBuffer
            COUNTY_LINES.count = MyApplication.countCounty
            HIGHWAYS.relativeBuffer = MyApplication.hwRelativeBuffer
            HIGHWAYS.count = MyApplication.countHw
            HIGHWAYS_EXTENDED.relativeBuffer = MyApplication.hwExtRelativeBuffer
            LAKES.relativeBuffer = MyApplication.lakesRelativeBuffer
            STATE_LINES.color = MyApplication.radarColorState
            COUNTY_LINES.color = MyApplication.radarColorCounty
            LAKES.color = MyApplication.radarColorLakes
            HIGHWAYS_EXTENDED.color = MyApplication.radarColorHwExt
            HIGHWAYS.color = MyApplication.radarColorHw

            STATE_LINES.pref = true
            COUNTY_LINES.pref = MyApplication.radarCounty
            LAKES.pref = MyApplication.radarLakes
            HIGHWAYS.pref = MyApplication.radarHw
            HIGHWAYS_EXTENDED.pref = MyApplication.radarHwEnhExt
            CITIES.pref = MyApplication.radarCities
            COUNTY_LABELS.pref = MyApplication.radarCountyLabels

            STATE_LINES.lineWidth = MyApplication.radarStateLineSize
            COUNTY_LINES.lineWidth = MyApplication.radarCountyLineSize
            LAKES.lineWidth = MyApplication.radarLakeLineSize
            HIGHWAYS.lineWidth = MyApplication.radarHwLineSize
            HIGHWAYS_EXTENDED.lineWidth = MyApplication.radarHwExtLineSize
        }
    }
}
