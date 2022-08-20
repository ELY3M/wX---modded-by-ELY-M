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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.R
import joshuatee.wx.settings.RadarPreferences

object RadarGeometry {

    val dataByType = mutableMapOf<RadarGeometryTypeEnum, RadarGeomInfo>()

    val orderedTypes = listOf(
            RadarGeometryTypeEnum.CountyLines,
            RadarGeometryTypeEnum.StateLines,
            RadarGeometryTypeEnum.CaLines,
            RadarGeometryTypeEnum.MxLines,
            RadarGeometryTypeEnum.HwLines,
            RadarGeometryTypeEnum.HwExtLines,
            RadarGeometryTypeEnum.LakeLines,
    )

    fun initialize(context: Context) {
        orderedTypes.forEach {
            dataByType[it] = RadarGeomInfo(context, it)
        }
        if (RadarPreferences.stateHires) {
            dataByType[RadarGeometryTypeEnum.StateLines]!!.count = 1166552
            dataByType[RadarGeometryTypeEnum.StateLines]!!.fileId = R.raw.statev3
        }
        if (RadarPreferences.countyHires) {
            dataByType[RadarGeometryTypeEnum.CountyLines]!!.count = 820852
            dataByType[RadarGeometryTypeEnum.CountyLines]!!.fileId = R.raw.countyv2
        }
        orderedTypes.forEach {
            dataByType[it]!!.loadData(context)
        }
    }

    fun updateConfig() {
        dataByType.values.forEach {
            it.update()
        }
    }
}
