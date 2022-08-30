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

import joshuatee.wx.objects.*
import joshuatee.wx.util.ProjectionNumbers

internal object WXGLPolygonWarnings {

    fun addGeneric(projectionNumbers: ProjectionNumbers, warn: ObjectPolygonWarning): List<Double> {
        val html = warn.getData()
        val warnings = ObjectWarning.parseJson(html)
        val warningList = mutableListOf<Double>()
        for (w in warnings) {
            if (warn.type == PolygonWarningType.SpecialWeatherStatement || warn.type == PolygonWarningType.SpecialMarineWarning || w.isCurrent) {
                val latLons = w.getPolygonAsLatLons(-1)
                warningList += LatLon.latLonListToListOfDoubles(latLons, projectionNumbers)
            }
        }
        return warningList
    }

    fun getCount(type: PolygonWarningType): Int {
        val html = ObjectPolygonWarning.polygonDataByType[type]!!.getData()
        val warningList = ObjectWarning.parseJson(html)
        var i = 0
        for (s in warningList) {
            if (s.isCurrent) {
                i += 1
            }
        }
        return i
    }

    fun getCountString() = "(" +
                getCount(PolygonWarningType.ThunderstormWarning).toString() + "," +
                getCount(PolygonWarningType.TornadoWarning).toString() + "," +
                getCount(PolygonWarningType.FlashFloodWarning).toString() + ")"

    fun areWarningsPresent(): Boolean {
        val tStormCount = getCount(PolygonWarningType.ThunderstormWarning)
        val torCount = getCount(PolygonWarningType.TornadoWarning)
        val floodCount = getCount(PolygonWarningType.FlashFloodWarning)
        return tStormCount > 0 || torCount > 0 || floodCount > 0
    }
}
