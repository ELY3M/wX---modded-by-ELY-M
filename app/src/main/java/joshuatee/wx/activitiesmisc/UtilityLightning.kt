/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.graphics.Bitmap
import joshuatee.wx.Extensions.*

object UtilityLightning {

    fun getImage(sector: String, period: String): Bitmap {
        val baseUrl = "https://images.lightningmaps.org/blitzortung/america/index.php?map="
        val baseUrlOceania = "https://images.lightningmaps.org/blitzortung/oceania/index.php?map="
        return if (sector.contains("australia") || sector.contains("new_zealand")) {
            "$baseUrlOceania$sector&period=$period".getImage()
        } else {
            "$baseUrl$sector&period=$period".getImage()
        }
    }

    val labels = listOf(
        "USA",
        "FL",
        "TX",
        "OK,KS",
        "North America",
        "South America",
        "Australia",
        "New Zealand"
    )

    val urls = listOf(
        "usa_big",
        "florida_big",
        "texas_big",
        "oklahoma_kansas_big",
        "north_middle_america",
        "south_america",
        "australia_big",
        "new_zealand_big"
    )

    fun getTimePretty(period: String) = when (period) {
        "0.25" -> "15 MIN"
        "2" -> "2 HR"
        "12" -> "12 HR"
        "24" -> "24 HR"
        "48" -> "48 HR"
        else -> ""
    }
}
