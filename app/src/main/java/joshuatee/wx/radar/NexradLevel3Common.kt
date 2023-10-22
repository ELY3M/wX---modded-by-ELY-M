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
import joshuatee.wx.objects.LatLon
import joshuatee.wx.util.ProjectionNumbers

internal object NexradLevel3Common {

    fun drawTickMarks(
            startPoint: LatLon,
            projectionNumbers: ProjectionNumbers,
            ecArr: ExternalGlobalCoordinates,
            startBearing: Double,
            distance: Double
    ): List<Double> {
        val start = ExternalGlobalCoordinates(ecArr)
        val externalGlobalCoordinates = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                start,
                startBearing,
                distance)
        return startPoint.asList() + Projection.computeMercatorNumbers(externalGlobalCoordinates, projectionNumbers).toList()
    }

    //storm tracks
    fun drawLine(
            startPoint: DoubleArray,
            projectionNumbers: ProjectionNumbers,
            start: ExternalGlobalCoordinates,
            startBearing: Double,
            distance: Double
    ): List<Double> {
        val externalGlobalCoordinates = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                start,
                startBearing,
                distance)
        return startPoint.toList() + Projection.computeMercatorNumbers(externalGlobalCoordinates, projectionNumbers).toList()
    }

    // wind barbs
    fun drawLine(startEc: ExternalGlobalCoordinates, pn: ProjectionNumbers, startBearing: Double, distance: Double): List<Double> {
        val startPoint = ExternalGlobalCoordinates(startEc)
        val ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(startPoint, startBearing, distance)
        return Projection.computeMercatorNumbers(startEc, pn).toList() + Projection.computeMercatorNumbers(ec, pn).toList()
    }
}
