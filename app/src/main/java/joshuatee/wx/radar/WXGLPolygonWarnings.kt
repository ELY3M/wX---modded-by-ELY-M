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

import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.ObjectWarning
import joshuatee.wx.objects.PolygonWarningType

internal object WXGLPolygonWarnings {

    fun addGeneric(projectionNumbers: ProjectionNumbers, warn: ObjectPolygonWarning): List<Double> {
        val html = warn.storage.value
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

    fun add(projectionNumbers: ProjectionNumbers, polygonType: PolygonType): List<Double> {
        val html = ObjectWarning.getBulkData(polygonType)
        val warnings = ObjectWarning.parseJson(html)
        val warningList = mutableListOf<Double>()
        for ( w in warnings) {
            if (w.isCurrent) {
                val latLons = w.getPolygonAsLatLons(-1)
                warningList += LatLon.latLonListToListOfDoubles(latLons, projectionNumbers)
            }
        }
        return warningList
    }
}
