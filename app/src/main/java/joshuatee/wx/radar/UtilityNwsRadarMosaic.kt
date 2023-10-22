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

import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.To

object UtilityNwsRadarMosaic {

    private const val baseUrl = "https://radar.weather.gov/ridge/standard/"

    fun getNearest(latLon: LatLon): String = UtilityLocation.getNearest(latLon, cityToLatLon)

    fun get(sector: String): String = if (sector == "CONUS") {
        baseUrl + "CONUS-LARGE_0.gif"
    } else {
        baseUrl + sector + "_0.gif"
    }

    fun getAnimation(sector: String): List<String> {
        val add = if (sector == "CONUS") {
            "-LARGE"
        } else {
            ""
        }
        return (9 downTo 0).map {
            baseUrl + sector + add + "_" + To.string(it) + ".gif"
        }
    }

    val sectors = listOf(
            "CONUS",
            "ALASKA",
            "CARIB",
            "CENTGRLAKES",
            "GUAM",
            "HAWAII",
            "NORTHEAST",
            "NORTHROCKIES",
            "PACNORTHWEST",
            "PACSOUTHWEST",
            "SOUTHEAST",
            "SOUTHMISSVLY",
            "SOUTHPLAINS",
            "SOUTHROCKIES",
            "UPPERMISSVLY",
    )

    val labels = listOf(
            "CONUS",
            "ALASKA",
            "CARIB",
            "CENTGRLAKES",
            "GUAM",
            "HAWAII",
            "NORTHEAST",
            "NORTHROCKIES",
            "PACNORTHWEST",
            "PACSOUTHWEST",
            "SOUTHEAST",
            "SOUTHMISSVLY",
            "SOUTHPLAINS",
            "SOUTHROCKIES",
            "UPPERMISSVLY",
    )

    private val cityToLatLon = mapOf(
            "ALASKA" to LatLon(63.8683, -149.3669),
            "CARIB" to LatLon(18.356, -69.592),
            "CENTGRLAKES" to LatLon(42.4396, -84.7305),
            "GUAM" to LatLon(13.4208, 144.7540),
            "HAWAII" to LatLon(19.5910, -155.4343),
            "NORTHEAST" to LatLon(42.7544, -73.4800),
            "NORTHROCKIES" to LatLon(44.0813, -108.1309),
            "PACNORTHWEST" to LatLon(43.1995, -118.9174),
            "PACSOUTHWEST" to LatLon(35.8313, -119.2245),
            "SOUTHEAST" to LatLon(30.2196, -82.1522),
            "SOUTHMISSVLY" to LatLon(33.2541, -89.8034),
            "SOUTHPLAINS" to LatLon(32.4484, -99.7781),
            "SOUTHROCKIES" to LatLon(33.2210, -110.3162),
            "UPPERMISSVLY" to LatLon(42.9304, -95.7488)
    )
}
