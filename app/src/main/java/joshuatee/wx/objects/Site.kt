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

package joshuatee.wx.objects

class Site(
    val codeName: String,
    val fullName: String,
    val lat: String,
    var lon: String,
    lonReversed: Boolean
) {

    var distance = 0
    val latLon: LatLon
    val codeAndName: String = "${codeName}: $fullName"

    init {
        if (lonReversed) {
            lon = "-$lon"
        }
        latLon = LatLon(lat, lon)
    }

    companion object {
        fun fromLatLon(codeName: String, latLon: LatLon, distance: Double = 0.0): Site {
            val site = Site(codeName, "", latLon.latString, latLon.lonString, false)
            site.distance = distance.toInt()
            return site
        }
    }
}
