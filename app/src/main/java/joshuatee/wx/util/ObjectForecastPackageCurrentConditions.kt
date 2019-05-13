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

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.Location

import joshuatee.wx.radar.LatLon

class ObjectForecastPackageCurrentConditions {

    companion object {
        // CA
        internal fun createForCanada(html: String): ObjectForecastPackageCurrentConditions {
            val obj = ObjectForecastPackageCurrentConditions()
            obj.data1 = UtilityCanada.getConditions(html)
            obj.status = UtilityCanada.getStatus(html)
            return obj
        }
    }

    var contextg: Context? = null
    var ccLine1: String = ""
    var data1: String = ""
        private set
    var iconUrl: String = ""
        private set
    private var conditionsTimeStr = ""
    var status: String = ""
        private set

    private constructor()

    // US
    internal constructor(context: Context, locNum: Int) {
        if (Location.isUS(locNum)) {
            val tmpArr = getConditionsViaMetar(context, Location.getLatLon(locNum))
            data1 = tmpArr[0]
            iconUrl = tmpArr[1]
            status = UtilityUSv2.getStatusViaMetar(context, conditionsTimeStr)
        }
    }

    internal constructor(context: Context, location: LatLon) {
        val tmpArr = getConditionsViaMetar(context, location)
        data1 = tmpArr[0]
        iconUrl = tmpArr[1]
        status = UtilityUSv2.getStatusViaMetar(context, conditionsTimeStr)
    }

    private fun getConditionsViaMetar(context: Context, location: LatLon): List<String> {
        var sb = ""
        val objMetar = ObjectMetar(context, location)
        conditionsTimeStr = objMetar.conditionsTimeStr
        val temperature = objMetar.temperature + MyApplication.DEGREE_SYMBOL
        val windChill = objMetar.windChill + MyApplication.DEGREE_SYMBOL
        val heatIndex = objMetar.heatIndex + MyApplication.DEGREE_SYMBOL
        val dewpoint = objMetar.dewpoint + MyApplication.DEGREE_SYMBOL
        val relativeHumidity = objMetar.relativeHumidity + "%"
        val seaLevelPressure = objMetar.seaLevelPressure
        val windDirection = objMetar.windDirection
        val windSpeed = objMetar.windSpeed
        val windGust = objMetar.windGust
        val visibility = objMetar.visibility
        val condition = objMetar.condition
        sb += temperature
        if (objMetar.windChill != "NA") {
            sb += "($windChill)"
        } else if (objMetar.heatIndex != "NA") {
            sb += "($heatIndex)"
        }
        sb += " / $dewpoint($relativeHumidity) - "
        sb += "$seaLevelPressure - $windDirection $windSpeed"
        if (windGust != "") {
            sb += " G "
        }
        sb += "$windGust mph - $visibility mi - $condition"
        return listOf(sb, objMetar.icon)
        //sb    String    "NA° / 22°(NA%) - 1016 mb - W 13 mph - 10 mi - Mostly Cloudy"
    }

    // FIXME sync up with flutter/ios port
    fun formatCC() {
        val sep = " - "
        val tmpArrCc = data1.split(sep)
        var retStr = ""
        //var retStr2 = "";
        //var tempArr = listOf<String>()
        if (tmpArrCc.size > 4) {
            val tmpList = tmpArrCc[0].split("/")
            retStr = tmpArrCc[4].replace("^ ", "") + " " + tmpList[0] + tmpArrCc[2]
            /*retStr2 = tempArr[1].replace("^ ", "") +
                    sep +
                    tmpArrCc[1] +
                    sep +
                    tmpArrCc[3];*/
        }
        ccLine1 = retStr
        //ccLine2 = retStr2.trim();
        //ccLine3 = UtilityString.capitalize(locationString);
    }
}



