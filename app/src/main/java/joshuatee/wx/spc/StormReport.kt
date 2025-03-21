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

package joshuatee.wx.spc

import joshuatee.wx.common.GlobalVariables

class StormReport(
    val lat: String,
    val lon: String,
    val time: String,
    val magnitude: String,
    val address: String,
    val city: String,
    val state: String,
    val damageReport: String,
    val damageHeader: String,
) {

    // var latLon: LatLon = LatLon(lat, lon)
    override fun toString(): String =
        listOf(
            damageHeader,
            lat,
            lon,
            time,
            magnitude,
            address,
            city,
            state,
            damageReport
        ).joinToString(",") + GlobalVariables.newline
}
