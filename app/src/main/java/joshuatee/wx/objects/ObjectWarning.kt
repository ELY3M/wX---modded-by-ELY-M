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

package joshuatee.wx.objects

import joshuatee.wx.common.RegExp
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityString
import joshuatee.wx.util.UtilityTime

class ObjectWarning() {

    var url = ""
    var title = ""
    var area = ""
    var effective = ""
    var expires = ""
    var event = ""
    var sender = ""
    private var polygon = ""
    private var vtec = ""
    private var geometry = ""
    var isCurrent = true

    constructor(
        url: String,
        title: String,
        area: String,
        effective: String,
        expires: String,
        event: String,
        sender: String,
        polygon: String,
        vtec: String,
        geometry: String
    ): this() {
        this.url = url
        // detailed desc
        this.title = title
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
        this.geometry = geometry
        this.isCurrent = UtilityTime.isVtecCurrent(this.vtec)
        if (vtec.startsWith("O.EXP") || vtec.startsWith("O.CAN")) {
            this.isCurrent = false
        }
    }

    fun getClosestRadar(): String {
        var data = polygon
        data = data.replace("[", "")
        data = data.replace("]", "")
        data = data.replace(",", " ")
        data = data.replace("-", "")
        val points = data.split(" ")
        return getClosestRadarCompute(points)
    }

    fun getPolygonAsLatLons(mult: Int): List<LatLon> {
        var polygonTmp = polygon
        polygonTmp = polygonTmp.replace("[", "")
        polygonTmp = polygonTmp.replace("]", "")
        polygonTmp = polygonTmp.replace(",", " ")
        return LatLon.parseStringToLatLons(polygonTmp, mult.toDouble(), true)
    }

    companion object {

        fun getClosestRadarCompute(points: List<String>): String {
            return if (points.size > 2) {
                val lat = points[1]
                val lon = "-" + points[0]
                val latLon = LatLon(lat, lon)
                val radarSites = UtilityLocation.getNearestRadarSites(latLon, 1, false)
                if (radarSites.isEmpty()) {
                    ""
                } else {
                    radarSites[0].name
                }
            } else {
                ""
            }
        }

        fun getBulkData(type1: PolygonType): String {
            return when (type1) {
                PolygonType.TOR -> {
                    ObjectPolygonWarning.severeDashboardTor.value
                }
                PolygonType.TST -> {
                    ObjectPolygonWarning.severeDashboardTst.value
                }
                PolygonType.FFW -> {
                    ObjectPolygonWarning.severeDashboardFfw.value
                }
                else -> {
                    ""
                }
            }
        }

        fun parseJson(htmlF: String): List<ObjectWarning> {
            var html = htmlF
            html = html.replace("\"geometry\": null,", "\"geometry\": null, \"coordinates\":[[]]}")
            val warnings = mutableListOf<ObjectWarning>()
            val urlList = UtilityString.parseColumn(html, "\"id\": \"(https://api.weather.gov/alerts/urn.*?)\"")
            val titleList = UtilityString.parseColumn(html, "\"description\": \"(.*?)\"")
            val areaDescList = UtilityString.parseColumn(html, "\"areaDesc\": \"(.*?)\"")
            val effectiveList = UtilityString.parseColumn(html, "\"effective\": \"(.*?)\"")
            val expiresList = UtilityString.parseColumn(html, "\"expires\": \"(.*?)\"")
            val eventList = UtilityString.parseColumn(html, "\"event\": \"(.*?)\"")
            val senderNameList = UtilityString.parseColumn(html, "\"senderName\": \"(.*?)\"")
            var data = html
            data = data.replace("\n", "")
            data = data.replace(" ", "")
            val listOfPolygonRaw = UtilityString.parseColumn(data, RegExp.warningLatLonPattern)
            val vtecs = UtilityString.parseColumn(html, RegExp.warningVtecPattern)
            val geometryList = UtilityString.parseColumn(html, "\"geometry\": (.*?),")
            for (index in urlList.indices) {
                warnings.add(ObjectWarning(
                        Utility.safeGet(urlList, index),
                        Utility.safeGet(titleList, index),
                        Utility.safeGet(areaDescList, index),
                        Utility.safeGet(effectiveList, index),
                        Utility.safeGet(expiresList, index),
                        Utility.safeGet(eventList, index),
                        Utility.safeGet(senderNameList, index),
                        Utility.safeGet(listOfPolygonRaw, index),
                        Utility.safeGet(vtecs, index),
                        Utility.safeGet(geometryList, index)
                ))
            }
            return warnings
        }
    }
}
