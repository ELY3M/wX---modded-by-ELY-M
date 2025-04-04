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

internal object UtilitySpcStormReports {

    fun process(linesOfData: List<String>): List<StormReport> {
        val stormReports = mutableListOf<StormReport>()
        linesOfData.forEach { line ->
            val items: List<String>
            var damageHeader = ""
            var lat = ""
            var lon = ""
            var state = ""
            var time = ""
            var address = ""
            var description = ""
            var magnitude = ""
            var city = ""
            if (line.contains(",F_Scale,")) {
                damageHeader = "Tornado Reports"
            } else if (line.contains(",Speed,")) {
                damageHeader = "Wind Reports"
            } else if (line.contains(",Size,")) {
                damageHeader = "Hail Reports"
            } else {
                items = line.split(",")
                if (items.size > 7) {
                    time = items[0]
                    magnitude = items[1]
                    address = items[2]
                    city = items[3]
                    state = items[4]
                    lat = items[5]
                    lon = items[6]
                    description = items[7]
                }
            }
            stormReports.add(
                StormReport(
                    lat,
                    lon,
                    time,
                    magnitude,
                    address,
                    city,
                    state,
                    description,
                    damageHeader
                )
            )
        }
        return stormReports
    }
}
