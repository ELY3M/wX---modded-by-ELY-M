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

internal object UtilitySpcFireOutlook {

    val textProducts = listOf(
        "fwddy1",
        "fwddy2",
        "fwddy38"
    )

    val urls = listOf(
        "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/fire_wx/day1otlk_fire.gif",
        "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/fire_wx/day2otlk_fire.gif",
        "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/exper/fire_wx/imgs/day38otlk_fire.gif"
    )
}
