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

import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.util.*

internal object WXGLNexradLevel3Common {

    fun drawTickMarks(
            startPoint: LatLon,
            externalGeodeticCalculator: ExternalGeodeticCalculator,
            projectionNumbers: ProjectionNumbers,
            ecArr: ExternalGlobalCoordinates,
            startBearing: Double,
            distance: Double
    ): List<Double> {
        val start = ExternalGlobalCoordinates(ecArr)
        val externalGlobalCoordinates = externalGeodeticCalculator.calculateEndingGlobalCoordinates(
            start,
            startBearing,
            distance
        )
        return startPoint.asList() + UtilityCanvasProjection.computeMercatorNumbers(externalGlobalCoordinates, projectionNumbers).toList()
    }

    //storm tracks
    fun drawLine(
            startPoint: DoubleArray,
            externalGeodeticCalculator: ExternalGeodeticCalculator,
            projectionNumbers: ProjectionNumbers,
            start: ExternalGlobalCoordinates,
            startBearing: Double,
            distance: Double
    ): List<Double> {
        val externalGlobalCoordinates = externalGeodeticCalculator.calculateEndingGlobalCoordinates(
            start,
            startBearing,
            distance
        )
        return startPoint.toList() + UtilityCanvasProjection.computeMercatorNumbers(externalGlobalCoordinates, projectionNumbers).toList()
    }

    // wind barbs
    fun drawLine(startEc: ExternalGlobalCoordinates, ecc: ExternalGeodeticCalculator, pn: ProjectionNumbers, startBearing: Double, distance: Double): List<Double>  {
        val startPoint = ExternalGlobalCoordinates(startEc)
        val ec = ecc.calculateEndingGlobalCoordinates(startPoint, startBearing, distance)
        return UtilityCanvasProjection.computeMercatorNumbers(startEc, pn).toList() + UtilityCanvasProjection.computeMercatorNumbers(ec, pn).toList()
    }
}
