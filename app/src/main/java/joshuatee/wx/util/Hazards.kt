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

package joshuatee.wx.util

import joshuatee.wx.parseColumn
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.Location

class Hazards {

    private var hazardsShort = ""
    var urls = listOf<String>()
    var titles = listOf<String>()
    var hazards = ""
        private set

    constructor()

    // US
    constructor(locationNumber: Int) {
        if (Location.isUS(locationNumber) && UIPreferences.homescreenFav.contains("TXT-HAZ")) {
            hazards = getHtml(Location.getLatLon(locationNumber))
            urls = hazards.parseColumn("\"id\": \"(" + GlobalVariables.NWS_API_URL + ".*?)\"")
            titles = hazards.parseColumn("\"event\": \"(.*?)\"")
        }
    }

    // adhoc forecast
    constructor(latLon: LatLon) {
        hazards = getHtml(latLon)
        urls = hazards.parseColumn("\"id\": \"(" + GlobalVariables.NWS_API_URL + ".*?)\"")
        titles = hazards.parseColumn("\"event\": \"(.*?)\"")
    }

    fun getHazardsShort(): String = hazardsShort.replace("^<BR>".toRegex(), "")

    companion object {
        fun getHtml(latLon: LatLon): String =
            ("https://api.weather.gov/alerts?point=" + latLon.latForNws + "," + latLon.lonForNws + "&active=1").getHtml()
    }
}
