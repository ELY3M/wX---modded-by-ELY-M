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

package joshuatee.wx.radar

import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.util.UtilityMath
import joshuatee.wx.util.UtilityString
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import joshuatee.wx.Extensions.*
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityCanvasProjection


class LatLon() {

    private var latNum = 0.0
    private var lonNum = 0.0
    private var xStr = "0.0"
    private var yStr = "0.0"

    constructor(latLon: DoubleArray) : this() {
        this.latNum = latLon[0]
        this.lonNum = latLon[1]
        this.xStr = this.latNum.toString()
        this.yStr = this.lonNum.toString()
    }

    constructor(latNum: Double, lonNum: Double) : this() {
        this.latNum = latNum
        this.lonNum = lonNum
        this.xStr = this.latNum.toString()
        this.yStr = this.lonNum.toString()
    }

    constructor(xStr: String, yStr: String) : this() {
        this.xStr = xStr
        this.yStr = yStr
        this.latNum = this.xStr.toDoubleOrNull() ?: 0.0
        this.lonNum = this.yStr.toDoubleOrNull() ?: 0.0
    }

    constructor(temp: String) : this() {
        this.xStr = temp.substring(0, 4)
        this.yStr = temp.substring(4, 8)
        if (this.yStr.matches("^0".toRegex())) {
            this.yStr = this.yStr.replace("^0".toRegex(), "")
            this.yStr += "0"
        }
        this.xStr = UtilityString.addPeriodBeforeLastTwoChars(this.xStr)
        this.yStr = UtilityString.addPeriodBeforeLastTwoChars(this.yStr)
        var tmpDbl = yStr.toDoubleOrNull() ?: 0.0
        if (tmpDbl < 40.00) {
            tmpDbl += 100
            this.yStr = tmpDbl.toString()
        }
        this.latNum = xStr.toDoubleOrNull() ?: 0.0
        this.lonNum = yStr.toDoubleOrNull() ?: 0.0
    }

    var lat: Double
        get() { return latNum }
        set(newValue) {
            latNum = newValue
            xStr = latNum.toString()
        }

    var lon: Double
        get() { return lonNum }
        set(newValue) {
            lonNum = newValue
            yStr = lonNum.toString()
        }

    var latString: String
        get() { return xStr }
        set(newValue) {
            xStr = newValue
            latNum = newValue.toDoubleOrNull() ?: 0.0
        }

    var lonString: String
        get() { return yStr }
        set(newValue) {
            yStr = newValue
            lonNum = newValue.toDoubleOrNull() ?: 0.0
        }

    fun asList() = listOf(lat, lon)

    fun asPoint() = ExternalPoint(this)

    override fun toString() = "$latString:$lonString"

    fun print()= "$latString $lonString "

    companion object {
        // 1.1515 is the number of statute miles in a nautical mile
        // 1.609344 is the number of kilometres in a mile
        fun distance(location1: LatLon, location2: LatLon, unit: DistanceUnit): Double {
            val theta = location1.lon - location2.lon
            var dist = sin(UtilityMath.deg2rad(location1.lat)) * sin(UtilityMath.deg2rad(location2.lat)) + cos(UtilityMath.deg2rad(location1.lat)) *
                    cos(UtilityMath.deg2rad(location2.lat)) * cos(UtilityMath.deg2rad(theta))
            dist = acos(dist)
            dist = UtilityMath.rad2deg(dist)
            dist *= 60.0 * 1.1515
            when (unit) {
                DistanceUnit.KM -> dist *= 1.609344
                DistanceUnit.NAUTICAL_MILE -> dist *= 0.8684
                else -> {}
            }
            return dist
        }

        // take a space separated list of numbers and return a list of LatLon, list is of the format
        // lon0 lat0 lon1 lat1 for watch
        // for UtilityWatch need to multiply Y by -1.0
        fun parseStringToLatLons(stringOfNumbers: String, multiplier: Double = 1.0, isWarning: Boolean = true): List<LatLon> {
            val list = stringOfNumbers.split(" ").dropLastWhile { it.isEmpty() }
            // FIXME move to list of LatLon
            val x = mutableListOf<Double>()
            val y = mutableListOf<Double>()
            list.indices.forEach { i ->
                if (isWarning) {
                    if (i.isEven()) y.add((list[i].toDoubleOrNull() ?: 0.0) * multiplier) else x.add(list[i].toDoubleOrNull() ?: 0.0)
                } else {
                    if (i.isEven()) x.add(list[i].toDoubleOrNull() ?: 0.0) else y.add((list[i].toDoubleOrNull() ?: 0.0) * multiplier)
                }
            }
            val latLons = mutableListOf<LatLon>()
            if (y.size > 3 && x.size > 3 && x.size == y.size) {
                x.forEachIndexed { index, _ -> latLons.add(LatLon(x[index], y[index])) }
            }
            return latLons
        }

        fun latLonListToListOfDoubles(latLons: List<LatLon>, projectionNumbers: ProjectionNumbers): List<Double> {
            val warningList = mutableListOf<Double>()
            if (latLons.isNotEmpty()) {
                val startCoordinates = UtilityCanvasProjection.computeMercatorNumbers(latLons[0], projectionNumbers).toMutableList()
                warningList += startCoordinates
                (1 until latLons.size).forEach { index ->
                    val coordinates = UtilityCanvasProjection.computeMercatorNumbers(latLons[index], projectionNumbers).toMutableList()
                    warningList += coordinates
                    warningList += coordinates
                }
                warningList += startCoordinates
            }
            return warningList
        }
    }
}

