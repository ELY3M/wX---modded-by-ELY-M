/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.util.UtilityMath
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class LatLon() {

    private var x = 0.0
    private var y = 0.0
    private var xStr = "0.0"
    private var yStr = "0.0"

    constructor(latlon: DoubleArray) : this() {
        this.x = latlon[0]
        this.y = latlon[1]
        this.xStr = this.x.toString()
        this.yStr = this.y.toString()
    }

    constructor(x: Double, y: Double) : this() {
        this.x = x
        this.y = y
        this.xStr = this.x.toString()
        this.yStr = this.y.toString()
    }

    constructor(xStr: String, yStr: String) : this() {
        this.xStr = xStr
        this.yStr = yStr
        this.x = this.xStr.toDoubleOrNull() ?: 0.0
        this.y = this.yStr.toDoubleOrNull() ?: 0.0
    }

    var lat: Double
        get() {
            return x
        }
        set(newValue) {
            x = newValue
            xStr = x.toString()
        }

    var lon: Double
        get() {
            return y
        }
        set(newValue) {
            y = newValue
            yStr = y.toString()
        }

    var latString: String
        get() {
            return xStr
        }
        set(newValue) {
            xStr = newValue
            x = newValue.toDoubleOrNull() ?: 0.0
        }

    var lonString: String
        get() {
            return yStr
        }
        set(newValue) {
            yStr = newValue
            y = newValue.toDoubleOrNull() ?: 0.0
        }

    companion object {

        // 1.1515 is the number of statute miles in a nautical mile
        // 1.609344 is the number of kilometres in a mile

        fun distance(location1: LatLon, location2: LatLon, unit: DistanceUnit): Double {
            val theta = location1.lon - location2.lon
            var dist = sin(UtilityMath.deg2rad(location1.lat)) * sin(UtilityMath.deg2rad(location2.lat)) + cos(UtilityMath.deg2rad(location1.lat)) * cos(UtilityMath.deg2rad(location2.lat)) * cos(UtilityMath.deg2rad(theta))
            dist = acos(dist)
            dist = UtilityMath.rad2deg(dist)
            dist *= 60.0 * 1.1515
            when (unit) {
                DistanceUnit.KM -> dist *= 1.609344
                DistanceUnit.NAUTICAL_MILE -> dist *= 0.8684
                else -> {
                }
            }
            return dist
        }
    }
}

