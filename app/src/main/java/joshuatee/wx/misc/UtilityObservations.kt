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

package joshuatee.wx.misc

import joshuatee.wx.common.GlobalVariables

@Suppress("SpellCheckingInspection")
internal object UtilityObservations {

    val urls = listOf(
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/sfcobs/large_latestsfc.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namswsfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namscsfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namsesfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namcwsfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namccsfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namcesfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namnwsfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namncsfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namnesfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namaksfcwbg.gif",
            "${GlobalVariables.nwsWPCwebsitePrefix}/sfc/namak2sfcwbg.gif"
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