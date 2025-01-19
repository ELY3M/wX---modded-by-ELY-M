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

import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.Site
import joshuatee.wx.radar.Metar
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtmlWithNewLineWithRetry
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parse
import kotlin.math.roundToInt

internal class ObjectMetar(location: LatLon, index: Int = 0) {

    //
    // This is used to show the current conditions on the main screen of the app
    //

    var condition = ""
    var temperature = ""
    var dewPoint = ""
    var windDirection = ""
    var windSpeed = ""
    var windGust = ""
    var seaLevelPressure = ""
    var visibility = ""
    var relativeHumidity = ""
    var windChill = ""
    var heatIndex = ""
    var conditionsTimeStr = ""
    var timeStringUtc = ""
    var icon = ""
    private var rawMetar = ""
    private var metarSkyCondition = ""
    private var metarWeatherCondition = ""
    val obsClosest: Site = Metar.findClosestObservation(location, index)

    init {
        val urlMetar =
            "${GlobalVariables.TGFTP_WEBSITE_PREFIX}/data/observations/metar/decoded/" + obsClosest.codeName + ".TXT"
        val metarData = urlMetar.getHtmlWithNewLineWithRetry(200)
        temperature = metarData.parse("Temperature: (.*?) F")
        dewPoint = metarData.parse("Dew Point: (.*?) F")
        windDirection = metarData.parse("Wind: from the (.*?) \\(.*? degrees\\) at .*? MPH ")
        windSpeed = metarData.parse("Wind: from the .*? \\(.*? degrees\\) at (.*?) MPH ")
        windGust =
            metarData.parse("Wind: from the .*? \\(.*? degrees\\) at .*? MPH \\(.*? KT\\) gusting to (.*?) MPH")
        seaLevelPressure = metarData.parse("Pressure \\(altimeter\\): .*? in. Hg \\((.*?) hPa\\)")
        visibility = metarData.parse("Visibility: (.*?) mile")
        relativeHumidity = metarData.parse("Relative Humidity: (.*?)%")
        windChill = metarData.parse("Windchill: (.*?) F")
        heatIndex = UtilityMath.heatIndex(temperature, relativeHumidity)
        rawMetar = metarData.parse("ob: (.*?)" + GlobalVariables.newline)
        metarSkyCondition = metarData.parse("Sky conditions: (.*?)" + GlobalVariables.newline)
        metarWeatherCondition = metarData.parse("Weather: (.*?)" + GlobalVariables.newline)
        metarSkyCondition = capitalizeString(metarSkyCondition)
        metarWeatherCondition = capitalizeString(metarWeatherCondition)
        condition =
            if (metarWeatherCondition == "" || metarWeatherCondition.contains("Inches Of Snow On Ground")) {
                metarSkyCondition
            } else {
                metarWeatherCondition
            }
        condition = condition.replace("; Lightning Observed", "")
        if (condition == "Mist") condition = "Fog/Mist"
        icon = decodeIconFromMetar(condition, obsClosest)
        condition = condition.replace(";", " and")
        val metarDataList = metarData.split(GlobalVariables.newline)
        if (metarDataList.size > 2) {
            val localStatus = metarDataList[1].split("/")
            if (localStatus.size > 1) {
                conditionsTimeStr =
                    ObjectDateTime.convertFromUtcForMetar(localStatus[1].replace(" UTC", ""))
                timeStringUtc = localStatus[1].trim()
            }
        }
        seaLevelPressure = changePressureUnits(seaLevelPressure)
        temperature = changeDegreeUnits(temperature)
        dewPoint = changeDegreeUnits(dewPoint)
        windChill = changeDegreeUnits(windChill)
        heatIndex = changeDegreeUnits(heatIndex)
        if (windSpeed == "") windSpeed = "0"
        if (condition == "") condition = "NA"
    }

    //
    // Capitalize the first letter of each word in the current condition string
    //
    private fun capitalizeString(s: String): String {
        val tokens = s.split(" ")
        var newString = ""
        tokens.forEach { word ->
            newString += word.replaceFirstChar { it.uppercase() } + " "
        }
        return newString.trimEnd()
    }

    private fun changeDegreeUnits(value: String): String {
        var newValue = "NA"
        if (value != "") {
            val tempD = To.double(value)
            newValue = if (UIPreferences.unitsF) {
                tempD.roundToInt().toString()
            } else {
                UtilityMath.fahrenheitToCelsius(tempD)
            }
        }
        return newValue
    }

    private fun changePressureUnits(value: String): String = if (!UIPreferences.unitsM) {
        UtilityMath.pressureMBtoIn(value)
    } else {
        "$value mb"
    }

    private fun decodeIconFromMetar(condition: String, obs: Site): String {
        // https://api.weather.gov/icons/land/day/ovc?size=medium
        val timeOfDay = if (ObjectDateTime.isDaytime(obs)) {
            "day"
        } else {
            "night"
        }
        val conditionModified = condition.split(";")[0]
        val shortCondition = UtilityMetarConditions.iconFromCondition[conditionModified] ?: ""
        return GlobalVariables.NWS_API_URL + "/icons/land/$timeOfDay/$shortCondition?size=medium"
    }
}
