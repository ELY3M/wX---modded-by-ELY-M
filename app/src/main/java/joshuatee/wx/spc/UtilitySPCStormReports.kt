/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

internal object UtilitySPCStormReports {

    fun processData(textArr: List<String>): MutableList<StormReport> {
        val output = StringBuilder()
        val outAl = mutableListOf<StormReport>()
        var lineArr: List<String>
        var x: String
        var y: String
        var state: String
        var time: String
        textArr.forEach {
            x = ""
            y = ""
            state = ""
            time = ""
            output.setLength(0)
            if (it.contains(",F_Scale,")) {
                output.append("Tornado Reports")
            } else if (it.contains(",Speed,")) {
                output.append("Wind Reports")
            } else if (it.contains(",Size,")) {
                output.append("Hail Reports")
            } else {
                lineArr = it.split(",")
                if (lineArr.size > 7) {
                    output.append(lineArr[0])
                    output.append(" ")
                    output.append(lineArr[1])
                    output.append(" ")
                    output.append(lineArr[2])
                    output.append("<font color=")
                    output.append(UIPreferences.highlightColorStr)
                    output.append("> ")
                    output.append(lineArr[3])
                    output.append(" ")
                    output.append(lineArr[4])
                    output.append("</font>  ")
                    output.append(lineArr[5])
                    output.append(" ")
                    output.append(lineArr[6])
                    output.append("<br>")
                    output.append("<i>")
                    output.append(lineArr[7])
                    output.append("</i>")

                    // 0 - GMT time
                    // 1 - unit
                    // 2 - address
                    // 3 - City
                    // 4 - State
                    // 5 - X
                    // 6 - Y
                    // 7 - description (WFO)

                    x = lineArr[5]
                    y = lineArr[6]
                    time = lineArr[0]
                    state = lineArr[4]
                }
            }
            outAl.add(StormReport(output.toString(), x, y, time, state))
        }
        return outAl
    }
}
