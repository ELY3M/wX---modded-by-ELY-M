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

package joshuatee.wx.util

import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.Location

class ObjectForecastPackageHazards {

    private var hazardsShort = ""
    var hazards = ""
        private set

    private constructor()

    // US
    internal constructor(locNum: Int) {
        if (Location.isUS(locNum) && MyApplication.homescreenFav.contains("TXT-HAZ")) {
            hazards = getHazardsHtml(Location.getLatLon(locNum))
        }
    }

    internal constructor(location: LatLon) {
        hazards = getHazardsHtml(location)
    }

    fun getHazardsShort() = hazardsShort.replace("^<BR>".toRegex(), "")

    companion object {
        // CA
        internal fun createForCanada(html: String): ObjectForecastPackageHazards {
            val obj = ObjectForecastPackageHazards()
            val hazArr = UtilityCanada.getHazards(html)
            obj.hazardsShort = hazArr[0]
            obj.hazards = hazArr[1]
            return obj
        }

        fun getHazardsHtml(location: LatLon): String {
            // was getNWSStringFromURL
            val html = UtilityDownloadNWS.getHazardData("https://api.weather.gov/alerts?point=" + UtilityMath.latLonFix(location.latString) + "," + UtilityMath.latLonFix(location.lonString) + "&active=1")
            return html
        }
    }
}



