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
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.OfficeTypeEnum
import joshuatee.wx.settings.UtilityLocation
import java.util.Locale
import java.util.regex.Pattern

internal object NexradLevel3StormInfo {

    private val pattern1: Pattern = Pattern.compile("AZ/RAN(.*?)V")
    private val pattern2: Pattern = Pattern.compile("MVT(.*?)V")
    private val pattern3: Pattern = Pattern.compile("\\d+")

    fun decode(projectionNumbers: ProjectionNumbers): List<Double> {
        val location = UtilityLocation.getSiteLocation(projectionNumbers.radarSite, OfficeTypeEnum.RADAR)
        val data = NexradLevel3TextProduct.download("STI", projectionNumbers.radarSite.lowercase(Locale.US))
        val posn = data.parseColumn(pattern1)
        val posnString = posn.joinToString("")
                .replace("NEW", "0/ 0")
                .replace("/ ", "/")
                .replace("\\s+".toRegex(), " ")
                .replace("/", " ")
        val motion = data.parseColumn(pattern2)
        val motionString = motion.joinToString("")
                .replace("NEW", "0/ 0")
                .replace("/ ", "/")
                .replace("\\s+".toRegex(), " ")
                .replace("/", " ")
        val posnNumbers = posnString.parseColumnAll(pattern3)
        val motNumbers = motionString.parseColumnAll(pattern3)
        val degreeShift = 180.00
        val arrowLength = 2.0
        val arrowBend = 20.0
        val sti15IncrementLength = 0.40
        val stormList = mutableListOf<Double>()
        if (posnNumbers.size == motNumbers.size && posnNumbers.size > 1) {
            (0 until (posnNumbers.size) step 2).forEach { s ->
                val degree = posnNumbers[s].toDouble()
                val nm = posnNumbers[s + 1].toDouble()
                val degree2 = motNumbers[s].toDouble()
                val nm2 = motNumbers[s + 1].toDouble()
                var start = ExternalGlobalCoordinates(location)
                var ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree, nm * 1852.0)
                stormList += Projection.computeMercatorNumbers(ec, projectionNumbers).toList()
                start = ExternalGlobalCoordinates(ec)
                ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree2 + degreeShift, nm2 * 1852.0)
                // mercator expects lat/lon to both be positive as many products have this
                val coordinates = Projection.computeMercatorNumbers(ec, projectionNumbers)
                stormList += coordinates.toList()
                val ecArr = mutableListOf<ExternalGlobalCoordinates>()
                val latLons = mutableListOf<LatLon>()
                (0..3).forEach { z ->
                    ecArr.add(ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(
                            start,
                            degree2 + degreeShift,
                            nm2 * 1852.0 * z.toDouble() * 0.25
                        )
                    )
                    latLons.add(LatLon(Projection.computeMercatorNumbers(ecArr[z], projectionNumbers)))
                }
                if (nm2 > 0.0) {
                    start = ExternalGlobalCoordinates(ec)
                    listOf(degree2 + arrowBend, degree2 - arrowBend).forEach { startBearing ->
                        stormList += NexradLevel3Common.drawLine(
                                coordinates,
                                projectionNumbers,
                                start,
                                startBearing,
                                arrowLength * 1852.0)
                    }
                    // 0,15,30,45 min ticks
                    val stormTrackTickMarkAngleOff90 = 45.0 // was 30.0
                    latLons.indices.forEach { z ->
                        listOf(
                                degree2 - (90.0 + stormTrackTickMarkAngleOff90),
                                degree2 + (90.0 - stormTrackTickMarkAngleOff90),
                                degree2 - (90.0 - stormTrackTickMarkAngleOff90),
                                degree2 + (90.0 + stormTrackTickMarkAngleOff90)
                        ).forEach { startBearing ->
                            stormList += NexradLevel3Common.drawTickMarks(
                                    latLons[z],
                                    projectionNumbers,
                                    ecArr[z],
                                    startBearing,
                                    arrowLength * 1852.0 * sti15IncrementLength
                            )
                        }
                    }
                }
            }
        }
        return stormList
    }
}


