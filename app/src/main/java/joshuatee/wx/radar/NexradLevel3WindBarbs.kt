/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.To

internal object NexradLevel3WindBarbs {

    fun decodeAndPlot(radarSite: String, projectionType: ProjectionType, isGust: Boolean, dataSetIndex: Int): List<Double> {
        val stormList = mutableListOf<Double>()
        val projectionNumbers = ProjectionNumbers(radarSite, projectionType)
        val arrWb = if (!isGust) Metar.data[dataSetIndex].obsArrWb else Metar.data[dataSetIndex].obsArrWbGust
        val degreeShift = 180.00
        val arrowLength = 2.5
        val arrowSpacing = 3.0
        val barbLengthScaleFactor = 0.4
        val arrowBend = 60.0
        val nmScaleFactor = -1852.0
        val barbLength = 15.0
        val barbOffset = 0.0
        arrWb.forEach { line ->
            val metarArr = line.split(":").dropLastWhile { it.isEmpty() }
            var angle = 0
            var length = 0
            var locXDbl = 0.0
            var locYDbl = 0.0
            if (metarArr.size > 3) {
                locXDbl = To.double(metarArr[0])
                locYDbl = To.double(metarArr[1])
                angle = To.int(metarArr[2])
                length = To.int(metarArr[3])
            }
            if (length > 4) {
                val degree2 = angle.toDouble()
                val startLength = 0.0
                var start = ExternalGlobalCoordinates(locXDbl, locYDbl)
                var ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, 0.0, startLength)
                stormList += Projection.computeMercatorNumbers(ec, projectionNumbers).toList()
                start = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                        start,
                        degree2 + degreeShift,
                        barbLength * nmScaleFactor * barbLengthScaleFactor)
                val end = ExternalGlobalCoordinates(ec.latitude, ec.longitude)
                stormList += Projection.computeMercatorNumbers(ec, projectionNumbers).toList()
                var barbCount = length / 10
                var halfBarb = false
                var oneHalfBarb = false
                if (length - barbCount * 10 > 4 && length > 10 || length in 5..9) {
                    halfBarb = true
                }
                if (length in 5..9) {
                    oneHalfBarb = true
                }
                val above50: Boolean
                if (length > 49) {
                    above50 = true
                    barbCount -= 4
                } else {
                    above50 = false
                }
                var index = 0
                if (above50) {
                    // initial angled line
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            end,
                            degree2,
                            barbOffset + startLength)
                    stormList += NexradLevel3Common.drawLine(
                            ec,
                            projectionNumbers,
                            degree2 - arrowBend * 2.0,
                            startLength + arrowLength * nmScaleFactor)
                    // perpendicular line from main barb
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            end,
                            degree2,
                            barbOffset + startLength + -1.0 * arrowSpacing * nmScaleFactor * barbLengthScaleFactor)
                    stormList += NexradLevel3Common.drawLine(
                            ec,
                            projectionNumbers,
                            degree2 - 90.0,
                            startLength + 0.80 * arrowLength * nmScaleFactor)
                    // connecting line parallel to main barb
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            end,
                            degree2,
                            barbOffset + startLength)
                    stormList += NexradLevel3Common.drawLine(
                            ec,
                            projectionNumbers,
                            degree2 - 180.0,
                            startLength + 0.5 * arrowLength * nmScaleFactor)
                    index += 1
                }
                (index until barbCount).forEach { _ ->
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            end,
                            degree2,
                            barbOffset + startLength + index.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor)
                    stormList += NexradLevel3Common.drawLine(
                            ec,
                            projectionNumbers,
                            degree2 - arrowBend * 2.0,
                            startLength + arrowLength * nmScaleFactor)
                    index += 1
                }
                val halfBarbOffsetFudge = if (oneHalfBarb) nmScaleFactor * 1.0 else 0.0
                if (halfBarb) {
                    ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            end,
                            degree2,
                            barbOffset + halfBarbOffsetFudge + startLength + index.toDouble() * arrowSpacing * nmScaleFactor * barbLengthScaleFactor)
                    stormList += NexradLevel3Common.drawLine(
                            ec,
                            projectionNumbers,
                            degree2 - arrowBend * 2.0,
                            startLength + arrowLength / 2.0 * nmScaleFactor)
                }
            } // if length greater then 4
        } // loop over wind barbs
        return stormList
    }
}
