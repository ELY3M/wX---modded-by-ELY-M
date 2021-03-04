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

import joshuatee.wx.objects.PolygonType

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityTime

class SevereWarning(private val type: PolygonType) {

    //
    // encapsulates VTEC data and count for tst,tor, or ffw
    //

    var count = 0
        private set

    var idList = listOf<String>()
    var areaDescList = listOf<String>()
    var effectiveList = listOf<String>()
    var expiresList = listOf<String>()
    var eventList = listOf<String>()
    var senderNameList = listOf<String>()
    var warnings = listOf<String>()
    var listOfWfo = mutableListOf<String>()
    private var listOfPolygonRaw = listOf<String>()

    fun getName(): String {
        return when (type) {
            PolygonType.TOR -> "Tornado Warning"
            PolygonType.TST -> "Severe Thunderstorm Warning"
            PolygonType.FFW -> "Flash Flood Warning"
            else -> ""
        }
    }

    private fun getClosestRadar(index: Int): String {
        val data = listOfPolygonRaw[index].replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
        val points = data.split(" ")
        // From CapAlert
        return if (points.size > 2) {
            val lat = points[1]
            val lon = "-" + points[0]
            val radarSites = UtilityLocation.getNearestRadarSites(LatLon(lat, lon), 1, includeTdwr = false)
            if (radarSites.isEmpty()) {
                ""
            } else {
                radarSites[0].name
            }
        } else {
            ""
        }
    }

    fun generateString(html: String) {
        idList = html.parseColumn("\"id\": \"(https://api.weather.gov/alerts/urn.*?)\"")
        areaDescList = html.parseColumn("\"areaDesc\": \"(.*?)\"")
        effectiveList = html.parseColumn("\"effective\": \"(.*?)\"")
        expiresList = html.parseColumn("\"expires\": \"(.*?)\"")
        eventList = html.parseColumn("\"event\": \"(.*?)\"")
        senderNameList = html.parseColumn("\"senderName\": \"(.*?)\"")
        val data = html.replace("\n", "").replace(" ", "")
        listOfPolygonRaw = data.parseColumn(RegExp.warningLatLonPattern)
        warnings = html.parseColumn(RegExp.warningVtecPattern)
        warnings.forEachIndexed { index, it ->
            val vtecIsCurrent = UtilityTime.isVtecCurrent(it)
            if (!it.startsWith("O.EXP") && vtecIsCurrent) {
                count += 1
                val radarSite = getClosestRadar(index)
                listOfWfo.add(radarSite)
            } else {
                listOfWfo.add("")
            }
        }
    }
}

