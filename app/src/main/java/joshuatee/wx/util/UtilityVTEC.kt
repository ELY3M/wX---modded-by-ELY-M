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

package joshuatee.wx.util

import joshuatee.wx.MyApplication
// FIXME see if this external can be removed
import joshuatee.wx.external.ExternalDuplicateRemover

import joshuatee.wx.Extensions.*

object UtilityVtec {

    fun getStormCount(data: String): Int {
        var dashboardString = ""
        val vtecPattern = "([A-Z0]{1}\\.[A-Z]{3}\\.[A-Z]{4}\\.[A-Z]{2}\\.[A-Z]\\.[0-9]{4}\\.[0-9]{6}T[0-9]{4}Z\\-[0-9]{6}T[0-9]{4}Z)"
        data.parseColumn(vtecPattern).forEach { vtec ->
            val vtecIsCurrent = UtilityTime.isVtecCurrent(vtec)
            if (!vtec.startsWith("O.EXP") && vtecIsCurrent) {
                dashboardString += vtec
                val offices = vtec.split(".")
                val officeLabel = if (offices.size > 1) {
                    val wfo = offices[2].replace("^[KP]".toRegex(), "")
                    Utility.getWfoSiteName(wfo)
                } else {
                    ""
                }
                dashboardString += "  " + officeLabel + MyApplication.newline
            }
        }
        dashboardString = ExternalDuplicateRemover().stripDuplicates(dashboardString)
        return dashboardString.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size
    }

    fun getStormCountGeneric(data: String) = data.parseColumn("(\"id\": \"https://api.weather.gov/alerts/urn:oid)").size
}




