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

package joshuatee.wx.activitiesmisc

import android.graphics.Bitmap

import joshuatee.wx.Extensions.*

object UtilityLightning {

    fun getImage(sector: String, period: String): Bitmap {
        val baseUrl = "http://images.lightningmaps.org/blitzortung/america/index.php?map="
        val baseUrlOceania = "http://images.lightningmaps.org/blitzortung/oceania/index.php?map="
        val url = if (sector.contains("australia") || sector.contains("new_zealand"))
            "$baseUrlOceania$sector&period=$period"
        else
            "$baseUrl$sector&period=$period"
        return url.getImage()
    }

    fun getSectorPretty(sector: String): String = when (sector) {
        "usa_big" -> "USA"
        "florida_big" -> "FL"
        "texas_big" -> "TX"
        "oklahoma_kansas_big" -> "OK,KS"
        "north_middle_america" -> "North America"
        "south_america" -> "South America"
        "australia_big" -> "Australia"
        "new_zealand_big" -> "New Zealand"
        "goes16_conus" -> "GOES-16 CONUS (Experimental)"
        "goes16_fulldisk" -> "GOES-16 FULLDISK (Experimental)"
        "goes16_seus" -> "GOES-16 SEUS (Experimental)"
        "goes16_carib" -> "GOES-16 CARIB (Experimental)"
        "goes16_southamer" -> "GOES-16 SOUTHAMER (Experimental)"
        else -> ""
    }

    fun getTimePretty(period: String): String = when (period) {
        "0.25" -> "15 MIN"
        "2" -> "2 HR"
        "12" -> "12 HR"
        "24" -> "24 HR"
        "48" -> "48 HR"
        else -> ""
    }
}
