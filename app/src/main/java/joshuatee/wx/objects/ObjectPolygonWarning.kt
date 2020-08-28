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

import android.content.Context
import joshuatee.wx.DataStorage
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityVtec

class ObjectPolygonWarning(val context: Context, val type: PolygonWarningType) {

    var color = 0
    val storage = DataStorage(prefTokenStorage)
    var isEnabled = false

    init {
        storage.update(context)
        color = Utility.readPref(context, prefTokenColor, type.initialColor)
        isEnabled = Utility.readPref(context, prefTokenEnabled, "false").startsWith("t")
    }

    val name get() = type.urlToken.replace("%20", " ")

    val prefTokenEnabled get() = "RADAR_SHOW_" + type.productCode

    val prefTokenColor get() = "RADAR_COLOR_" + type.productCode

    private val prefTokenStorage get() = "SEVERE_DASHBOARD_" + type.productCode

    companion object {
        private var polygonDataByType = mutableMapOf<PolygonWarningType, ObjectPolygonWarning>()

        private val polygonList = listOf(
                PolygonWarningType.SpecialMarineWarning,
                PolygonWarningType.SnowSquallWarning,
                PolygonWarningType.DustStormWarning,
                PolygonWarningType.SpecialWeatherStatement
        )

        fun areAnyEnabled(): Boolean {
            var anyEnabled = false
            polygonList.forEach { if (polygonDataByType[it]!!.isEnabled) anyEnabled = true }
            return anyEnabled
        }

        fun load(context: Context) {
            polygonList.forEach { polygonDataByType[it] = ObjectPolygonWarning(context, it) }
        }

        fun isCountNonZero(): Boolean {
            val tstCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTst.value)
            val torCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTor.value)
            val ffwCount = UtilityVtec.getStormCount(MyApplication.severeDashboardFfw.value)
            var count = tstCount + torCount + ffwCount
            polygonList.forEach {
                if (polygonDataByType[it]!!.isEnabled) {
                    count += UtilityVtec.getStormCountGeneric(polygonDataByType[it]!!.storage.value)
                }
            }
            return count > 0
        }
    }
}

