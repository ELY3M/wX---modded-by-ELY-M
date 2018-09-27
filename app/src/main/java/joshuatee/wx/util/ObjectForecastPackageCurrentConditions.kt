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

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.radar.LatLon
import kotlin.math.roundToInt

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
    var data1 = ""
        private set
    var iconUrl = ""
        private set
    private var conditionsTimeStr = ""
    var status = ""
        private set

    private constructor()

    // US
    internal constructor(context: Context, locNum: Int) {
        if (Location.isUS(locNum)) {
            if (MyApplication.currentConditionsViaMetar) {
                val tmpArr = getConditionsViaMetar(context, Location.getLatLon(locNum))
                data1 = tmpArr[0]
                iconUrl = tmpArr[1]
            } else {
                val tmpArr = getConditions(context, Location.getLatLon(locNum))
                data1 = tmpArr[0]
                iconUrl = tmpArr[1]
            }
            status = if (MyApplication.currentConditionsViaMetar) {
                UtilityUSv2.getStatusViaMetar(context, conditionsTimeStr)
            } else {
                UtilityUSv2.getStatus(context, conditionsTimeStr)
            }
        }
    }

    internal constructor(context: Context, location: LatLon) {

        if (MyApplication.currentConditionsViaMetar) {
            val tmpArr = getConditionsViaMetar(context, location)
            data1 = tmpArr[0]
            iconUrl = tmpArr[1]
        } else {
            val tmpArr = getConditions(context, location)
            data1 = tmpArr[0]
            iconUrl = tmpArr[1]
        }
        status = if (MyApplication.currentConditionsViaMetar) {
            UtilityUSv2.getStatusViaMetar(context, conditionsTimeStr)
        } else {
            UtilityUSv2.getStatus(context, conditionsTimeStr)
        }
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

    private fun getConditions(context: Context, location: LatLon): List<String> {
        val sb = StringBuilder(500)
        val obsClosest = UtilityUSv2.getObsFromLatLon(context, location)
        val observationData = UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/stations/$obsClosest/observations/current")
        val icon = observationData.parse("\"icon\": \"(.*?)\", ")
        var condition = observationData.parse("\"textDescription\": \"(.*?)\", ")
        var temperature = observationData.parse("\"temperature\":.*?\"value\": (.*?),")
        var dewpoint = observationData.parse("\"dewpoint\":.*?\"value\": (.*?),")
        var windDirection = observationData.parse("\"windDirection\":.*?\"value\": (.*?),")
        var windSpeed = observationData.parse("\"windSpeed\":.*?\"value\": (.*?),")
        var windGust = observationData.parse("\"windGust\":.*?\"value\": (.*?),")
        var seaLevelPressure = observationData.parse("\"barometricPressure\":.*?\"value\": (.*?),")
        var visibility = observationData.parse("\"visibility\":.*?\"value\": (.*?),")
        var relativeHumidity = observationData.parse("\"relativeHumidity\":.*?\"value\": (.*?),")
        var windChill = observationData.parse("\"windChill\":.*?\"value\": (.*?),")
        var heatIndex = observationData.parse("\"heatIndex\":.*?\"value\": (.*?),")
        conditionsTimeStr = observationData.parse("\"timestamp\": \"(.*?)\"")
        temperature = if (!temperature.contains("NA") && !temperature.contains("null")) {
            val tempD = temperature.toDoubleOrNull() ?: 0.0
            if (MyApplication.unitsF) {
                UtilityMath.cTof(tempD)
            } else {
                UtilityMath.roundToString(tempD)
            }
        } else {
            "NA"
        }
        windChill = if (!windChill.contains("NA") && !windChill.contains("null")) {
            val tempD = windChill.toDoubleOrNull() ?: 0.0
            if (MyApplication.unitsF) {
                UtilityMath.cTof(tempD)
            } else {
                UtilityMath.roundToString(tempD)
            }
        } else {
            "NA"
        }
        heatIndex = if (!heatIndex.contains("NA") && !heatIndex.contains("null")) {
            val tempD = heatIndex.toDoubleOrNull() ?: 0.0
            if (MyApplication.unitsF) {
                UtilityMath.cTof(tempD)
            } else {
                UtilityMath.roundToString(tempD)
            }
        } else {
            "NA"
        }
        dewpoint = if (!dewpoint.contains("NA") && !dewpoint.contains("null")) {
            val tempD = dewpoint.toDoubleOrNull() ?: 0.0
            if (MyApplication.unitsF) {
                UtilityMath.cTof(tempD)
            } else {
                UtilityMath.roundToString(tempD)
            }
        } else {
            "NA"
        }
        windDirection = if (!windDirection.contains("NA") && !windDirection.contains("null")) {
            UtilityMath.convertWindDir(windDirection.toDoubleOrNull() ?: 0.0)
        } else {
            "NA"
        }
        windSpeed = if (!windSpeed.contains("NA") && !windSpeed.contains("null")) {
            val tempD = windSpeed.toDoubleOrNull() ?: 0.0
            UtilityMath.metersPerSecondtoMPH(tempD)
        } else {
            "NA"
        }
        relativeHumidity = if (!relativeHumidity.contains("NA") && !relativeHumidity.contains("null")) {
            val tempD = relativeHumidity.toDoubleOrNull() ?: 0.0
            //UtilityMath.roundToString(tempD)
            tempD.roundToInt().toString()
        } else {
            "NA"
        }
        visibility = if (!visibility.contains("NA") && !visibility.contains("null")) {
            val tempD = visibility.toDoubleOrNull() ?: 0.0
            UtilityMath.metersToMileRounded(tempD)
        } else {
            "NA"
        }
        try {
            val tempD = seaLevelPressure.toDoubleOrNull() ?: 0.0
            seaLevelPressure = if (!MyApplication.unitsM) {
                UtilityMath.pressureMBtoIn(seaLevelPressure)
            } else {
                UtilityMath.pressurePAtoMB(tempD) + " mb"
            }
        } catch (e: Exception) {
            seaLevelPressure = "NA"
            UtilityLog.HandleException(e)
        }
        if (windGust == "null") {
            windGust = ""
        } else {
            val tempD = windGust.toDoubleOrNull() ?: 0.0
            windGust = UtilityMath.metersPerSecondtoMPH(tempD)
            windGust = "G $windGust"
        }
        if (condition == "") {
            condition = " "
        }
        sb.append(temperature)
        sb.append(MyApplication.DEGREE_SYMBOL)
        if (windChill != "NA") {
            sb.append("(")
            sb.append(windChill)
            sb.append(MyApplication.DEGREE_SYMBOL)
            sb.append(")")
        } else if (heatIndex != "NA") {
            sb.append("(")
            sb.append(heatIndex)
            sb.append(MyApplication.DEGREE_SYMBOL)
            sb.append(")")
        }
        sb.append(" / ")
        sb.append(dewpoint)
        sb.append(MyApplication.DEGREE_SYMBOL)
        sb.append("(")
        sb.append(relativeHumidity)
        sb.append("%)")
        sb.append(" - ")
        sb.append(seaLevelPressure)
        sb.append(" - ")
        sb.append(windDirection)
        sb.append(" ")
        sb.append(windSpeed)
        if (windGust != "") {
            sb.append(" ")
        }
        sb.append(windGust)
        sb.append(" mph")
        sb.append(" - ")
        sb.append(visibility)
        sb.append(" mi - ")
        sb.append(condition)
        return listOf(sb.toString(), icon)
    }
}

/*
 
 
 {
 "number": 14,
 "name": "Tuesday Night",
 "startTime": "2016-12-27T18:00:00-05:00",
 "endTime": "2016-12-28T06:00:00-05:00",
 "isDaytime": false,
 "temperature": 50,
 "windSpeed": "7 mph",
 "windDirection": "NW",
 "icon": "https://api-v1.weather.gov/icons/land/night/rain_showers,30?size=medium",
 "shortForecast": "Chance Rain Showers",
 "detailedForecast": "A chance of rain showers. Mostly cloudy, with a low around 50. Chance of precipitation is 30%."
 }
 
 
 */


