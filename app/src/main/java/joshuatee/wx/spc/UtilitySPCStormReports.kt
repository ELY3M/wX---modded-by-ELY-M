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

package joshuatee.wx.spc

import joshuatee.wx.UIPreferences

internal object UtilitySpcStormReports {

    fun processData(textArr: List<String>): MutableList<StormReport> {
        val output = StringBuilder()
        val outAl = mutableListOf<StormReport>()
        var lineChunks: List<String>
        var lat: String
        var lon: String
        var state: String
        var time: String
        var address: String
        var damageReport: String
        var magnitude: String
        var city: String
        //var damageHeader: String
        textArr.forEach {
            lat = ""
            lon = ""
            state = ""
            time = ""
            address = ""
            damageReport = ""
            magnitude = ""
            city = ""
            //damageHeader = ""
            output.setLength(0)
            if (it.contains(",F_Scale,")) {
                output.append("Tornado Reports")
            } else if (it.contains(",Speed,")) {
                output.append("Wind Reports")
            } else if (it.contains(",Size,")) {
                output.append("Hail Reports")
            } else {
                lineChunks = it.split(",")
                if (lineChunks.size > 7) {
                    output.append(lineChunks[0])
                    output.append(" ")
                    output.append(lineChunks[1])
                    output.append(" ")
                    output.append(lineChunks[2])
                    output.append("<font color=")
                    output.append(UIPreferences.highlightColorStr)
                    output.append("> ")
                    output.append(lineChunks[3])
                    output.append(" ")
                    output.append(lineChunks[4])
                    output.append("</font>  ")
                    output.append(lineChunks[5])
                    output.append(" ")
                    output.append(lineChunks[6])
                    output.append("<br>")
                    output.append("<i>")
                    output.append(lineChunks[7])
                    output.append("</i>")

                    // 0 - GMT time
                    // 1 - unit
                    // 2 - address
                    // 3 - City
                    // 4 - State
                    // 5 - X
                    // 6 - Y
                    // 7 - description (WFO)

                    //x = lineArr[5]
                    //y = lineArr[6]
                    //time = lineArr[0]
                    //state = lineArr[4]

                    time = lineChunks[0]
                    magnitude = lineChunks[1]
                    address = lineChunks[2]
                    city = lineChunks[3]
                    state = lineChunks[4]
                    lat = lineChunks[5]
                    lon = lineChunks[6]
                    damageReport = lineChunks[7]
                }
            }
            //outAl.add(StormReport(output.toString(), lat, lon, time, state))
            outAl.add(
                StormReport(
                    output.toString(),
                    lat,
                    lon,
                    time,
                    magnitude,
                    address,
                    city,
                    state,
                    damageReport
                    //damageHeader
                )
            )
        }
        return outAl
    }
}
