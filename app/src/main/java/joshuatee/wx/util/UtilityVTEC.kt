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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.MyApplication
// FIXME see if this external can be removed
import joshuatee.wx.external.ExternalDuplicateRemover

import joshuatee.wx.Extensions.*

// FIXME rename camelcase
object UtilityVTEC {

    fun getStormCount(context: Context, textTor: String): Int {
        var dashboardStrTor = ""
        var nwsLoc = ""
        var nwsOfficeArr: List<String>
        var nwsOffice: String
        val pVtec =
            "([A-Z0]{1}\\.[A-Z]{3}\\.[A-Z]{4}\\.[A-Z]{2}\\.[A-Z]\\.[0-9]{4}\\.[0-9]{6}T[0-9]{4}Z\\-[0-9]{6}T[0-9]{4}Z)"
        val stormList = textTor.parseColumn(pVtec)
        stormList.forEach {
            val vtecIsCurrent = UtilityTime.isVtecCurrent(it)
            if (!it.startsWith("O.EXP") && vtecIsCurrent) {
                dashboardStrTor += it
                nwsOfficeArr = it.split(".")
                if (nwsOfficeArr.size > 1) {
                    nwsOffice = nwsOfficeArr[2]
                    nwsOffice = nwsOffice.replace("^[KP]".toRegex(), "")
                    nwsLoc = Utility.readPref(context, "NWS_LOCATION_$nwsOffice", "")
                }
                dashboardStrTor += "  " + nwsLoc + MyApplication.newline
            }
        }
        dashboardStrTor = ExternalDuplicateRemover().stripDuplicates(dashboardStrTor)
        return dashboardStrTor.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size
    }
}




