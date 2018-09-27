/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx.wpc

import joshuatee.wx.MyApplication
import joshuatee.wx.R

internal object UtilityWPCRainfallForecast {

    val PROD_TEXT_URL = listOf(
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/94epoints.txt",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/98epoints.txt",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/99epoints.txt"
    )

    val PROD_TITLE = listOf(
            "Day 1",
            "Day 2",
            "Day 3"
    )

    val PROD_IMG_URL = listOf(
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/94ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/98ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/99ewbg.gif"
    )

    const val ACTIVITY_TITLE_INT = R.string.title_activity_hpcrainfall_forecast
}

