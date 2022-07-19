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

package joshuatee.wx.objects

import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.settings.RadarPreferences
import java.nio.ByteBuffer

enum class GeographyType constructor(var relativeBuffer: ByteBuffer, var count: Int, var color: Int, var pref: Boolean, var lineWidth: Int) {

    STATE_LINES(
            RadarGeometry.stateRelativeBuffer,
            RadarGeometry.countState,
            RadarPreferences.radarColorState,
            true,
            RadarPreferences.radarStateLineSize
    ),
    COUNTY_LINES(
            RadarGeometry.countyRelativeBuffer,
            RadarGeometry.countCounty,
            RadarPreferences.radarColorCounty,
            RadarPreferences.radarCounty,
            RadarPreferences.radarCountyLineSize
    ),
    LAKES(
            RadarGeometry.lakesRelativeBuffer,
            RadarGeometry.countLakes,
            RadarPreferences.radarColorLakes,
            RadarPreferences.radarLakes,
            RadarPreferences.radarLakeLineSize
    ),
    HIGHWAYS(
            RadarGeometry.hwRelativeBuffer,
            RadarGeometry.countHw,
            RadarPreferences.radarColorHw,
            RadarPreferences.radarHw,
            RadarPreferences.radarHwLineSize
    ),
    HIGHWAYS_EXTENDED(
            RadarGeometry.hwExtRelativeBuffer,
            RadarGeometry.countHwExt,
            RadarPreferences.radarColorHwExt,
            RadarPreferences.radarHwEnhExt,
            RadarPreferences.radarHwExtLineSize
    ),
    CITIES(ByteBuffer.allocate(0), 0, RadarPreferences.radarColorCity, RadarPreferences.radarCities, 0),
    COUNTY_LABELS(ByteBuffer.allocate(0), 0, RadarPreferences.radarColorCountyLabels, RadarPreferences.radarCountyLabels, 0),
    NONE(ByteBuffer.allocateDirect(0), 0, 0, false, 0);

    // FIXME refresh rest of values
    companion object {
        fun refresh() {
            HIGHWAYS_EXTENDED.relativeBuffer = RadarGeometry.hwExtRelativeBuffer
            STATE_LINES.relativeBuffer = RadarGeometry.stateRelativeBuffer
            STATE_LINES.count = RadarGeometry.countState
            COUNTY_LINES.relativeBuffer = RadarGeometry.countyRelativeBuffer
            COUNTY_LINES.count = RadarGeometry.countCounty
            HIGHWAYS.relativeBuffer = RadarGeometry.hwRelativeBuffer
            HIGHWAYS.count = RadarGeometry.countHw
            HIGHWAYS_EXTENDED.relativeBuffer = RadarGeometry.hwExtRelativeBuffer
            LAKES.relativeBuffer = RadarGeometry.lakesRelativeBuffer
            STATE_LINES.color = RadarPreferences.radarColorState
            COUNTY_LINES.color = RadarPreferences.radarColorCounty
            LAKES.color = RadarPreferences.radarColorLakes
            HIGHWAYS_EXTENDED.color = RadarPreferences.radarColorHwExt
            HIGHWAYS.color = RadarPreferences.radarColorHw

            STATE_LINES.pref = true
            COUNTY_LINES.pref = RadarPreferences.radarCounty
            LAKES.pref = RadarPreferences.radarLakes
            HIGHWAYS.pref = RadarPreferences.radarHw
            HIGHWAYS_EXTENDED.pref = RadarPreferences.radarHwEnhExt
            CITIES.pref = RadarPreferences.radarCities
            COUNTY_LABELS.pref = RadarPreferences.radarCountyLabels

            STATE_LINES.lineWidth = RadarPreferences.radarStateLineSize
            COUNTY_LINES.lineWidth = RadarPreferences.radarCountyLineSize
            LAKES.lineWidth = RadarPreferences.radarLakeLineSize
            HIGHWAYS.lineWidth = RadarPreferences.radarHwLineSize
            HIGHWAYS_EXTENDED.lineWidth = RadarPreferences.radarHwExtLineSize
        }
    }
}
