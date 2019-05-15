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
import joshuatee.wx.radar.LatLon
import joshuatee.wx.radar.UtilityMetar
import joshuatee.wx.MyApplication
import joshuatee.wx.Extensions.*
import joshuatee.wx.radar.RID

import java.util.Date
import java.util.Calendar

internal class ObjectMetar(context: Context, location: LatLon) {


/*
 ANN ARBOR MUNICIPAL AIRPORT, MI, United States (KARB) 42-13N 083-45W 251M
 Feb 11, 2018 - 06:53 PM EST / 2018.02.11 2353 UTC
 Wind: from the WSW (250 degrees) at 9 MPH (8 KT):0
 Visibility: 10 mile(s):0
 Sky conditions: clear
 Temperature: 24.1 F (-4.4 C)
 Windchill: 14 F (-10 C):1
 Dew Point: 19.9 F (-6.7 C)
 Relative Humidity: 83%
 Pressure (altimeter): 30.02 in. Hg (1016 hPa)
 Pressure tendency: 0.12 inches (4.1 hPa) higher than three hours ago
 ob: KARB 112353Z 25008KT 10SM CLR M04/M07 A3002 RMK AO2 SLP177 60000 T10441067 11033 21044 51041
 cycle: 0

 Oceanside, Oceanside Municipal Airport, CA, United States (KOKH) 33-13-10N 117-20-58W 8M
 Dec 31, 2008 - 10:56 AM EST / 2008.12.31 1556 UTC
 Wind: from the SW (230 degrees) at 16 MPH (14 KT) gusting to 26 MPH (23 KT):0
 Visibility: 10 mile(s):0
 Sky conditions: overcast
 Temperature: 37 F (3 C)
 Dew Point: 32 F (0 C)
 Relative Humidity: 80%
 Pressure (altimeter): 29.95 in. Hg (1014 hPa)
 ob: KOKH 311556Z AUTO 23014G23KT 10SM SCT017 BKN041 OVC065 03/00 A2995 RMK FIRST
 cycle: 16

 https://stackoverflow.com/questions/42803349/swift-3-0-convert-server-utc-time-to-local-time-and-visa-versa/42811162
 */


    //private val decodeIcon = true
    var condition = ""
    var temperature = ""
    var dewpoint = ""
    var windDirection = ""
    var windSpeed = ""
    var windGust = ""
    var seaLevelPressure = ""
    var visibility = ""
    var relativeHumidity = ""
    var windChill = ""
    var heatIndex = ""
    var conditionsTimeStr = ""
    var icon = ""
    private var rawMetar = ""
    private var metarSkyCondition = ""
    private var metarWeatherCondition = ""

    private fun capitalizeString(str: String): String {
        val tokens = str.split(" ")
        var newString = ""
        tokens.forEach { newString += it.capitalize() + " " }
        return newString.trimEnd()
    }

    private fun changeDegreeUnits(value: String): String {
        var newValue = "NA"
        if (value != "") {
            val tempD = value.toDoubleOrNull() ?: 0.0
            newValue = if (MyApplication.unitsF) {
                UtilityMath.roundToString(tempD)
            } else {
                UtilityMath.fToC(tempD)
            }
        }
        return newValue
    }

    private fun changePressureUnits(value: String): String {
        return if (!MyApplication.unitsM) {
            UtilityMath.pressureMBtoIn(value)
        } else {
            "$value mb"
        }
    }

    private fun decodeIconFromMetar(condition: String, obs: RID): String {
        // https://api.weather.gov/icons/land/day/ovc?size=medium
        val sunTimes = UtilityTimeSunMoon.getSunriseSunsetFromObs(obs)
        val sunRiseDate = sunTimes[0].time
        val sunSetDate = sunTimes[1].time
        val currentTime = Date()
        val fallsBetween = currentTime.after(sunRiseDate) && currentTime.before(sunSetDate)
        val currentCal = Calendar.getInstance()
        currentCal.time = Date()
        currentCal.add(Calendar.DATE, 1)
        val currentTimeTomorrow = currentCal.time
        val fallsBetweenTomorrow =
            currentTimeTomorrow.after(sunRiseDate) && currentTimeTomorrow.before(sunSetDate)
        var timeOfDay = "night"
        if (fallsBetween || fallsBetweenTomorrow) {
            timeOfDay = "day"
        }
        val conditionModified = condition.split(";")[0]
        val shortCondition = UtilityMetarConditions.iconFromCondition[conditionModified] ?: ""
        return "https://api.weather.gov/icons/land/$timeOfDay/$shortCondition?size=medium"
    }

    init {
        val obsClosest = UtilityMetar.findClosestObservation(context, location)
        UtilityUS.obsClosestClass = obsClosest.name
        val observationData =
            ("https://api.weather.gov/stations/" + obsClosest.name + "/observations/current").getNwsHtml()
        icon = observationData.parse("\"icon\": \"(.*?)\",")
        condition = observationData.parse("\"textDescription\": \"(.*?)\",")
        val metarData =
            ("${MyApplication.NWS_RADAR_PUB}/data/observations/metar/decoded/" + obsClosest.name + ".TXT").getHtmlSep()
                .replace("<br>", MyApplication.newline)
        temperature = metarData.parse("Temperature: (.*?) F")
        dewpoint = metarData.parse("Dew Point: (.*?) F")
        windDirection = metarData.parse("Wind: from the (.*?) \\(.*? degrees\\) at .*? MPH ")
        windSpeed = metarData.parse("Wind: from the .*? \\(.*? degrees\\) at (.*?) MPH ")
        windGust =
            metarData.parse("Wind: from the .*? \\(.*? degrees\\) at .*? MPH \\(.*? KT\\) gusting to (.*?) MPH")
        seaLevelPressure = metarData.parse("Pressure \\(altimeter\\): .*? in. Hg \\((.*?) hPa\\)")
        visibility = metarData.parse("Visibility: (.*?) mile")
        relativeHumidity = metarData.parse("Relative Humidity: (.*?)%")
        windChill = metarData.parse("Windchill: (.*?) F")
        heatIndex = metarData.parse("Heat index: (.*?) F")
        rawMetar = metarData.parse("ob: (.*?)" + MyApplication.newline)
        metarSkyCondition = metarData.parse("Sky conditions: (.*?)" + MyApplication.newline)
        metarWeatherCondition = metarData.parse("Weather: (.*?)" + MyApplication.newline)
        metarSkyCondition = capitalizeString(metarSkyCondition)
        metarWeatherCondition = capitalizeString(metarWeatherCondition)
        condition = if (metarWeatherCondition == "") {
            metarSkyCondition
        } else {
            metarWeatherCondition
        }
        condition = condition.replace("; Lightning Observed", "")
        if (condition == "Mist") {
            condition = "Fog/Mist"
        }
        icon = decodeIconFromMetar(condition, obsClosest)
        condition = condition.replace(";", " and")
        val metarDataList = metarData.split(MyApplication.newline)
        if (metarDataList.size > 2) {
            val localStatus = metarDataList[1].split("/")
            if (localStatus.size > 1) {
                conditionsTimeStr =
                    UtilityTime.convertFromUTCForMetar(localStatus[1].replace(" UTC", ""))
            }
        }
        seaLevelPressure = changePressureUnits(seaLevelPressure)
        temperature = changeDegreeUnits(temperature)
        dewpoint = changeDegreeUnits(dewpoint)
        windChill = changeDegreeUnits(windChill)
        heatIndex = changeDegreeUnits(heatIndex)
        if (windSpeed == "") {
            windSpeed = "0"
        }
        if (condition == "") {
            condition = "NA"
        }
    }
}



