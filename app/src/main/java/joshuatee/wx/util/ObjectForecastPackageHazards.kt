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

import joshuatee.wx.Extensions.parseColumn
import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.Location

class ObjectForecastPackageHazards {

    private var hazardsShort = ""
    var urls = listOf<String>()
    var titles = listOf<String>()
    var hazards: String = ""
        private set

    constructor()

    // US
    constructor(locNum: Int) {
        if (Location.isUS(locNum) && MyApplication.homescreenFav.contains("TXT-HAZ")) {
            hazards = getHazardsHtml(Location.getLatLon(locNum))
            urls = hazards.parseColumn("\"id\": \"(" + MyApplication.nwsApiUrl + ".*?)\"")
            titles = hazards.parseColumn("\"event\": \"(.*?)\"")
        }
    }

    // Canada
    constructor(html: String) {
        val hazArr = UtilityCanada.getHazards(html)
        hazardsShort = hazArr[0]
        hazards = hazArr[1]
    }

    // adhoc forecast
    constructor(location: LatLon) {
        hazards = getHazardsHtml(location)
        urls = hazards.parseColumn("\"id\": \"(" + MyApplication.nwsApiUrl + ".*?)\"")
        titles = hazards.parseColumn("\"event\": \"(.*?)\"")
    }

    fun getHazardsShort(): String = hazardsShort.replace("^<BR>".toRegex(), "")

    companion object {
        fun getHazardsHtml(location: LatLon): String {
            val url = "https://api.weather.gov/alerts?point=" + UtilityMath.latLonFix(location.latString) + "," + UtilityMath.latLonFix(location.lonString) + "&active=1"
            return UtilityDownloadNws.getHazardData(url)
        }
    }
}



