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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.MyApplication

internal object UtilityObservations {

    val urls = listOf(
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/sfcobs/large_latestsfc.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namswsfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namscsfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namsesfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namcwsfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namccsfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namcesfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namnwsfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namncsfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namnesfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namaksfcwbg.gif",
        "${MyApplication.nwsWPCwebsitePrefix}/sfc/namak2sfcwbg.gif"
    )

    val labels = listOf(
        "CONUS Surface Obs",
        "SW Surface Analysis",
        "SC Surface Analysis",
        "SE Surface Analysis",
        "CW Surface Analysis",
        "C Surface Analysis",
        "CE Surface Analysis",
        "NW Surface Analysis",
        "NC Surface Analysis",
        "NE Surface Analysis",
        "AK Surface Analysis",
        "Gulf of AK Surface Analysis"
    )
}
