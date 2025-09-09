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

package joshuatee.wx.objects

import joshuatee.wx.common.RegExp
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityString

class ObjectWarning() {

    var url = ""
    var area = ""
    var effective = ""
    var expires = ""
    var event = ""
    var sender = ""
    private var polygon = ""
    private var vtec = ""
    var isCurrent = true

    constructor(
        url: String,
        area: String,
        effective: String,
        expires: String,
        event: String,
        sender: String,
        polygon: String,
        vtec: String,
    ) : this() {
        this.url = url
        this.area = area

        this.effective = effective
        this.effective = this.effective.replace("T", " ")
        this.effective = UtilityString.replaceAllRegexp(this.effective, ":00-0[0-9]:00", "")

        this.expires = expires
        this.expires = this.expires.replace("T", " ")
        this.expires = UtilityString.replaceAllRegexp(this.expires, ":00-0[0-9]:00", "")

        this.event = event
        this.sender = sender
        this.polygon = polygon
        this.vtec = vtec
        this.isCurrent = ObjectDateTime.isVtecCurrent(this.vtec)
        if (vtec.startsWith("O.EXP") || vtec.startsWith("O.CAN")) {
            this.isCurrent = false
        }
    }

    fun getClosestRadar(): String {
        val data = polygon
            .replace("[", "")
            .replace("]", "")
            .replace(",", " ")
            .replace("-", "")
        val points = data.split(" ")
        return getClosestRadarCompute(points)
    }

    fun getPolygonAsLatLons(multiplier: Int): List<LatLon> {
        val polygonTmp = polygon
            .replace("[", "")
            .replace("]", "")
            .replace(",", " ")
        return LatLon.parseStringToLatLons(polygonTmp, multiplier.toDouble(), true)
    }

    companion object {

        fun getClosestRadarCompute(points: List<String>): String = if (points.size > 2) {
            val lat = points[1]
            val lon = "-" + points[0]
            val latLon = LatLon(lat, lon)
            RadarSites.getNearestCode(latLon)
        } else {
            ""
        }

        fun parseJson(htmlF: String): List<ObjectWarning> {
            val html =
                htmlF.replace("\"geometry\": null,", "\"geometry\": null, \"coordinates\":[[]]}")
            val warnings = mutableListOf<ObjectWarning>()
            val urlList = UtilityString.parseColumn(
                html,
                "\"id\": \"(https://api.weather.gov/alerts/urn.*?)\""
            )
//            val titleList = UtilityString.parseColumn(html, "\"description\": \"(.*?)\"")
            val areaDescList = UtilityString.parseColumn(html, "\"areaDesc\": \"(.*?)\"")
            val effectiveList = UtilityString.parseColumn(html, "\"effective\": \"(.*?)\"")
            val expiresList = UtilityString.parseColumn(html, "\"expires\": \"(.*?)\"")
            val eventList = UtilityString.parseColumn(html, "\"event\": \"(.*?)\"")
            val senderNameList = UtilityString.parseColumn(html, "\"senderName\": \"(.*?)\"")
            val data = html
                .replace("\n", "")
                .replace(" ", "")
            val listOfPolygonRaw = UtilityString.parseColumn(data, RegExp.warningLatLonPattern)
            val vtecs = UtilityString.parseColumn(html, RegExp.warningVtecPattern)
//            val geometryList = UtilityString.parseColumn(html, "\"geometry\": (.*?),")
            urlList.indices.forEach { index ->
                warnings.add(
                    ObjectWarning(
                        Utility.safeGet(urlList, index),
                        Utility.safeGet(areaDescList, index),
                        Utility.safeGet(effectiveList, index),
                        Utility.safeGet(expiresList, index),
                        Utility.safeGet(eventList, index),
                        Utility.safeGet(senderNameList, index),
                        Utility.safeGet(listOfPolygonRaw, index),
                        Utility.safeGet(vtecs, index),
                    )
                )
            }
            return warnings
        }
    }
}
