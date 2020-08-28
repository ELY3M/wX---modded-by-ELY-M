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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.Location

import joshuatee.wx.radar.LatLon

class ObjectCurrentConditions {

    var topLine = ""
    var data = ""
        private set
    var iconUrl = ""
        private set
    private var time = ""
    var status = ""
        private set

    constructor()

    constructor(context: Context, locationNumber: Int) {
        if (Location.isUS(locationNumber)) {
            val tmpArr = getConditionsViaMetar(context, Location.getLatLon(locationNumber))
            // 62° / 54°(74%) - 1013 mb - ESE 7 mph - 8 mi - Partly Cloudy
            data = tmpArr[0]
            iconUrl = tmpArr[1]
            status = UtilityUS.getStatusViaMetar(context, time)
        } else {
            val html = UtilityCanada.getLocationHtml(Location.getLatLon(locationNumber))
            data = UtilityCanada.getConditions(html)
            status = UtilityCanada.getStatus(html)
        }
    }

    constructor(context: Context, latLon: LatLon) {
        val items = getConditionsViaMetar(context, latLon)
        data = items[0]
        iconUrl = items[1]
        status = UtilityUS.getStatusViaMetar(context, time)
    }

    private fun getConditionsViaMetar(context: Context, latLon: LatLon): List<String> {
        val objectMetar = ObjectMetar(context, latLon)
        time = objectMetar.conditionsTimeStr
        val temperature = objectMetar.temperature + MyApplication.DEGREE_SYMBOL
        val windChill = objectMetar.windChill + MyApplication.DEGREE_SYMBOL
        val heatIndex = objectMetar.heatIndex + MyApplication.DEGREE_SYMBOL
        val dewPoint = objectMetar.dewPoint + MyApplication.DEGREE_SYMBOL
        val relativeHumidity = objectMetar.relativeHumidity + "%"
        val seaLevelPressure = objectMetar.seaLevelPressure
        val windDirection = objectMetar.windDirection
        val windSpeed = objectMetar.windSpeed
        val windGust = objectMetar.windGust
        val visibility = objectMetar.visibility
        val condition = objectMetar.condition
        var string = temperature
        if (objectMetar.windChill != "NA") {
            string += "($windChill)"
        } else if (objectMetar.heatIndex != "NA") {
            string += "($heatIndex)"
        }
        string += " / $dewPoint($relativeHumidity) - "
        string += "$seaLevelPressure - $windDirection $windSpeed"
        if (windGust != "") string += " G "
        string += "$windGust mph - $visibility mi - $condition"
        return listOf(string, objectMetar.icon)
        // "NA° / 22°(NA%) - 1016 mb - W 13 mph - 10 mi - Mostly Cloudy"
    }

    fun format() {
        val dataList = data.split(" - ")
        val string = if (dataList.size > 4) {
            val items = dataList[0].split("/")
            dataList[4].replace("^ ", "") + " " + items[0] + dataList[2]
        } else {
            ""
        }
        topLine = string
    }
}



