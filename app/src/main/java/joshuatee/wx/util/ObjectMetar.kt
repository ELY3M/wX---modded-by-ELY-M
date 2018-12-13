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
import joshuatee.wx.radar.LatLon
import joshuatee.wx.radar.UtilityMetar
import joshuatee.wx.MyApplication
import joshuatee.wx.Extensions.*
import joshuatee.wx.radar.RID

import java.util.Date
import java.util.Calendar

internal class ObjectMetar(context: Context, location: LatLon) {

//if (!decodeIcon) {
//}

//if (decodeIcon) {
//}

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
        val sunTimes = UtilityTime.getSunriseSunsetFromObs(obs)
        val sunRiseDate = sunTimes[0].time
        val sunSetDate = sunTimes[1].time
        val currentTime = Date()
        val fallsBetween = currentTime.after(sunRiseDate) && currentTime.before(sunSetDate)
        val currentCal = Calendar.getInstance()
        currentCal.time = Date()
        currentCal.add(Calendar.DATE, 1)
        val currentTimeTomorrow = currentCal.time
        val fallsBetweenTomorrow = currentTimeTomorrow.after(sunRiseDate) && currentTimeTomorrow.before(sunSetDate)
        var timeOfDay = "night"
        if (fallsBetween || fallsBetweenTomorrow) {
            timeOfDay = "day"
        }
        val conditionModified = condition.split(";")[0]
        val shortCondition = iconFromCondition[conditionModified] ?: ""
        return "https://api.weather.gov/icons/land/$timeOfDay/$shortCondition?size=medium"
    }

    // https://www.weather.gov/forecast-icons
    private val iconFromCondition = mapOf(
            //"" to "skc",
            "Mostly Clear" to "few",

            "Fair" to "skc",
            "Clear" to "skc",
            "Fair with Haze" to "skc",
            "Clear with Haze" to "skc",
            "Fair and Breezy" to "skc",
            "Clear and Breezy" to "skc",

            "A Few Clouds" to "few",
            "A Few Clouds with Haze" to "few",
            "A Few Clouds and Breezy" to "few",

            "Partly Cloudy" to "sct",
            "Partly Cloudy with Haze" to "sct",
            "Partly Cloudy and Breezy" to "sct",

            "Mostly Cloudy" to "bkn",
            "Mostly Cloudy with Haze" to "bkn",
            "Mostly Cloudy and Breezy" to "bkn",
            "Cumulonimbus Clouds Observed" to "bkn",

            "Overcast" to "ovc",
            "Overcast with Haze" to "ovc",
            "Overcast and Breezy" to "ovc",

            "Snow" to "snow",
            "Light Snow" to "snow",
            "Heavy Snow" to "snow",
            "Snow Showers" to "snow",
            "Light Snow Showers" to "snow",
            "Heavy Snow Showers" to "snow",
            "Showers Snow" to "snow",
            "Light Showers Snow" to "snow",
            "Heavy Showers Snow" to "snow",
            "Snow Fog/Mist" to "snow",
            "Light Drizzle, Snow And Mist" to "snow",
            "Light Snow Fog/Mist" to "snow",
            "Heavy Snow Fog/Mist" to "snow",
            "Snow Showers Fog/Mist" to "snow",
            "Light Snow Showers Fog/Mist" to "snow",
            "Heavy Snow Showers Fog/Mist" to "snow",
            "Showers Snow Fog/Mist" to "snow",
            "Light Showers Snow Fog/Mist" to "snow",
            "Heavy Showers Snow Fog/Mist" to "snow",
            "Snow Fog" to "snow",
            "Light Snow Fog" to "snow",
            "Heavy Snow Fog" to "snow",
            "Snow Showers Fog" to "snow",
            "Light Snow Showers Fog" to "snow",
            "Heavy Snow Showers Fog" to "snow",
            "Showers in Vicinity Snow" to "snow",
            "Snow Showers in Vicinity" to "snow",
            "Snow Showers in Vicinity Fog/Mist" to "snow",
            "Snow Showers in Vicinity Fog" to "snow",
            "Low Drifting Snow" to "snow",
            "Blowing Snow" to "snow",
            "Snow Low Drifting Snow" to "snow",
            "Snow Blowing Snow" to "snow",
            "Light Snow Low Drifting Snow" to "snow",
            "Light Snow Blowing Snow" to "snow",
            "Light Snow Blowing Snow Fog/Mist" to "snow",
            "Heavy Snow Low Drifting Snow" to "snow",
            "Heavy Snow Blowing Snow" to "snow",
            "Thunderstorm Snow" to "snow",
            "Light Thunderstorm Snow" to "snow",
            "Heavy Thunderstorm Snow" to "snow",
            "Snow Grains" to "snow",
            "Light Snow Grains" to "snow",
            "Heavy Snow Grains" to "snow",
            "Heavy Blowing Snow" to "snow",
            "Blowing Snow in Vicinity" to "snow",

            "Rain Snow" to "ra_sn",
            "Light Rain Snow" to "ra_sn",
            "Heavy Rain Snow" to "ra_sn",
            "Snow Rain" to "ra_sn",
            "Light Snow Rain" to "ra_sn",
            "Heavy Snow Rain" to "ra_sn",
            "Drizzle Snow" to "ra_sn",
            "Light Drizzle Snow" to "ra_sn",
            "Heavy Drizzle Snow" to "ra_sn",
            "Snow Drizzle" to "ra_sn",
            "Light Snow Drizzle" to "ra_sn",

            "Rain Ice Pellets" to "ra_ip",
            "Light Rain Ice Pellets" to "ra_ip",
            "Heavy Rain Ice Pellets" to "ra_ip",
            "Drizzle Ice Pellets" to "ra_ip",
            "Light Drizzle Ice Pellets" to "ra_ip",
            "Heavy Drizzle Ice Pellets" to "ra_ip",
            "Ice Pellets Rain" to "ra_ip",
            "Light Ice Pellets Rain" to "ra_ip",
            "Heavy Ice Pellets Rain" to "ra_ip",
            "Ice Pellets Drizzle" to "ra_ip",
            "Light Ice Pellets Drizzle" to "ra_ip",
            "Heavy Ice Pellets Drizzle" to "ra_ip",

            "Freezing Rain" to "fzra",
            "Freezing Drizzle" to "fzra",
            "Light Freezing Rain" to "fzra",
            "Light Freezing Drizzle" to "fzra",
            "Heavy Freezing Rain" to "fzra",
            "Heavy Freezing Drizzle" to "fzra",
            "Freezing Rain in Vicinity" to "fzra",
            "Freezing Drizzle in Vicinity" to "fzra",

            "Freezing Rain Rain" to "ra_fzra",
            "Light Freezing Rain Rain" to "ra_fzra",
            "Heavy Freezing Rain Rain" to "ra_fzra",
            "Rain Freezing Rain" to "ra_fzra",
            "Light Rain Freezing Rain" to "ra_fzra",
            "Heavy Rain Freezing Rain" to "ra_fzra",
            "Freezing Drizzle Rain" to "ra_fzra",
            "Light Freezing Drizzle Rain" to "ra_fzra",
            "Heavy Freezing Drizzle Rain" to "ra_fzra",
            "Rain Freezing Drizzle" to "ra_fzra",
            "Light Rain Freezing Drizzle" to "ra_fzra",
            "Heavy Rain Freezing Drizzle" to "ra_fzra",

            "Freezing Rain Snow" to "fzra_sn",
            "Light Freezing Rain Snow" to "fzra_sn",
            "Heavy Freezing Rain Snow" to "fzra_sn",
            "Freezing Drizzle Snow" to "fzra_sn",
            "Light Freezing Drizzle Snow" to "fzra_sn",
            "Heavy Freezing Drizzle Snow" to "fzra_sn",
            "Snow Freezing Rain" to "fzra_sn",
            "Light Snow Freezing Rain" to "fzra_sn",
            "Heavy Snow Freezing Rain" to "fzra_sn",
            "Snow Freezing Drizzle" to "fzra_sn",
            "Light Snow Freezing Drizzle" to "fzra_sn",
            "Heavy Snow Freezing Drizzle" to "fzra_sn",

            "Ice Pellets" to "ip",
            "Light Ice Pellets" to "ip",
            "Heavy Ice Pellets" to "ip",
            "Ice Pellets in Vicinity" to "ip",
            "Showers Ice Pellets" to "ip",
            "Thunderstorm Ice Pellets" to "ip",
            "Ice Crystals" to "ip",
            "Hail" to "ip",
            "Small Hail/Snow Pellets" to "ip",
            "Light Small Hail/Snow Pellets" to "ip",
            "Heavy small Hail/Snow Pellets" to "ip",
            "Showers Hail" to "ip",
            "Hail Showers" to "ip",

            "Snow Ice Pellets" to "snip",

            "Light Rain" to "minus_ra",
            "Drizzle" to "minus_ra",
            "Light Drizzle" to "minus_ra",
            "Heavy Drizzle" to "minus_ra",
            "Light Rain Fog/Mist" to "minus_ra",
            "Drizzle Fog/Mist" to "minus_ra",
            "Light Drizzle Fog/Mist" to "minus_ra",
            "Heavy Drizzle Fog/Mist" to "minus_ra",
            "Light Rain Fog" to "minus_ra",
            "Drizzle Fog" to "minus_ra",
            "Light Drizzle Fog" to "minus_ra",
            "Heavy Drizzle Fog" to "minus_ra",

            "Rain" to "ra",
            "Heavy Rain" to "ra",
            "Rain Fog/Mist" to "ra",
            "Heavy Rain Fog/Mist" to "ra",
            "Rain Fog" to "ra",
            "Heavy Rain Fog" to "ra",
            "Precipitation" to "ra",

            "Rain Showers" to "shra",
            "Light Rain Showers" to "shra",
            "Light Rain and Breezy" to "shra",
            "Heavy Rain Showers" to "shra",
            "Rain Showers in Vicinity" to "shra",
            "Light Showers Rain" to "shra",
            "Heavy Showers Rain" to "shra",
            "Showers Rain" to "shra",
            "Showers Rain in Vicinity" to "shra",
            "Rain Showers Fog/Mist" to "shra",
            "Light Rain Showers Fog/Mist" to "shra",
            "Heavy Rain Showers Fog/Mist" to "shra",
            "Rain Showers in Vicinity Fog/Mist" to "shra",
            "Light Showers Rain Fog/Mist" to "shra",
            "Heavy Showers Rain Fog/Mist" to "shra",
            "Showers Rain Fog/Mist" to "shra",
            "Showers Rain in Vicinity Fog/Mist" to "shra",

            "Showers in Vicinity" to "hi_shwrs",
            "Showers in Vicinity Fog/Mist" to "hi_shwrs",
            "Showers in Vicinity Fog" to "hi_shwrs",
            "Showers in Vicinity Haze" to "hi_shwrs",

            "Thunderstorm" to "tsra",
            "Thunderstorm Rain" to "tsra",
            "Light Thunderstorm Rain" to "tsra",
            "Heavy Thunderstorm Rain" to "tsra",
            "Thunderstorm Rain Fog/Mist" to "tsra",
            "Light Thunderstorm Rain Fog/Mist" to "tsra",
            "Heavy Thunderstorm Rain Fog and Windy" to "tsra",
            "Heavy Thunderstorm Rain Fog/Mist" to "tsra",
            "Thunderstorm Showers in Vicinity" to "tsra",
            "Light Thunderstorm Rain Haze" to "tsra",
            "Heavy Thunderstorm Rain Haze" to "tsra",
            "Thunderstorm Fog" to "tsra",
            "Light Thunderstorm Rain Fog" to "tsra",
            "Heavy Thunderstorm Rain Fog" to "tsra",
            "Thunderstorm Light Rain" to "tsra",
            "Thunderstorm Heavy Rain" to "tsra",
            "Thunderstorm Light Rain Fog/Mist" to "tsra",
            "Thunderstorm Heavy Rain Fog/Mist" to "tsra",
            "Thunderstorm in Vicinity Fog/Mist" to "tsra",
            "Thunderstorm in Vicinity Haze" to "tsra",
            "Thunderstorm Haze in Vicinity" to "tsra",
            "Thunderstorm Light Rain Haze" to "tsra",
            "Thunderstorm Heavy Rain Haze" to "tsra",
            "Thunderstorm Light Rain Fog" to "tsra",
            "Thunderstorm Heavy Rain Fog" to "tsra",
            "Thunderstorm Hail" to "tsra",
            "Light Thunderstorm Rain Hail" to "tsra",
            "Heavy Thunderstorm Rain Hail" to "tsra",
            "Thunderstorm Rain Hail Fog/Mist" to "tsra",
            "Light Thunderstorm Rain Hail Fog/Mist" to "tsra",
            "Heavy Thunderstorm Rain Hail Fog/Hail" to "tsra",
            "Thunderstorm Showers in Vicinity Hail" to "tsra",
            "Light Thunderstorm Rain Hail Haze" to "tsra",
            "Heavy Thunderstorm Rain Hail Haze" to "tsra",
            "Thunderstorm Hail Fog" to "tsra",
            "Light Thunderstorm Rain Hail Fog" to "tsra",
            "Heavy Thunderstorm Rain Hail Fog" to "tsra",
            "Thunderstorm Light Rain Hail" to "tsra",
            "Thunderstorm Heavy Rain Hail" to "tsra",
            "Thunderstorm Light Rain Hail Fog/Mist" to "tsra",
            "Thunderstorm Heavy Rain Hail Fog/Mist" to "tsra",
            "Thunderstorm in Vicinity Hail" to "tsra",
            "Thunderstorm in Vicinity Hail Haze" to "tsra",
            "Thunderstorm Haze in Vicinity Hail" to "tsra",
            "Thunderstorm Light Rain Hail Haze" to "tsra",
            "Thunderstorm Heavy Rain Hail Haze" to "tsra",
            "Thunderstorm Light Rain Hail Fog" to "tsra",
            "Thunderstorm Heavy Rain Hail Fog" to "tsra",
            "Thunderstorm Small Hail/Snow Pellets" to "tsra",
            "Thunderstorm Rain Small Hail/Snow Pellets" to "tsra",
            "Light Thunderstorm Rain Small Hail/Snow Pellets" to "tsra",
            "Heavy Thunderstorm Rain Small Hail/Snow Pellets" to "tsra",
            "Thunder" to "tsra",
            "Rain With Thunder" to "tsra",
            "Thunder In The Vicinity And Mist" to "tsra",

            "Thunder In The Vicinity And Heavy Rain and Mist" to "tsra",


            "Freezing With Thunder Rain And Mist" to "tsra",
            "Heavy Rain With Thunder" to "tsra",
            "Heavy Rain With Thunder In The Vicinity" to "tsra",
            "Light Rain With Thunder" to "tsra",


            //"Thunderstorm in Vicinity" to "scttsra",

            "Thunderstorm in Vicinity" to "hi_tsra",
            "Thunder In The Vicinity" to "hi_tsra",
            "Thunderstorm in Vicinity Fog" to "hi_tsra",
            "Lightning Observed" to "hi_tsra",
            "Cumulonimbus Clouds, Lightning Observed" to "hi_tsra",
            //"Thunderstorm in Vicinity Haze" to "hi_tsra",

            "Funnel Cloud" to "fc",
            "Funnel Cloud in Vicinity" to "fc",
            "Tornado/Water Spout" to "fc",

            "Tornado" to "tor",

            "Hurricane Warning" to "hur_warn",

            "Hurricane Watch" to "hur_watch",

            "Tropical Storm Warning" to "ts_warn",

            "Tropical Storm Watch" to "ts_watch",

            "Tropical Storm Conditions presently exist w/Hurricane Warning in effect" to "ts_nowarn",

            "Windy" to "wind_skc",
            "Breezy" to "wind_skc",
            "Fair and Windy" to "wind_skc",

            "A Few Clouds and Windy" to "wind_few",

            "Partly Cloudy and Windy" to "wind_sct",

            "Mostly Cloudy and Windy" to "wind_bkn",

            "Overcast and Windy" to "wind_ovc",

            "Dust" to "du",
            "Low Drifting Dust" to "du",
            "Blowing Dust" to "du",
            "Blowing Widespread Dust" to "du",
            "Sand" to "du",
            "Blowing Sand" to "du",
            "Low Drifting Sand" to "du",
            "Dust/Sand Whirls" to "du",
            "Dust/Sand Whirls in Vicinity" to "du",
            "Dust Storm" to "du",
            "Heavy Dust Storm" to "du",
            "Dust Storm in Vicinity" to "du",
            "Sand Storm" to "du",
            "Heavy Sand Storm" to "du",
            "Sand Storm in Vicinity" to "du",

            "Smoke" to "fu",

            // no night
            "Haze" to "hz",
            "Hot" to "hot",

            "Cold" to "cold",

            "Blizzard" to "blizzard",

            "Fog" to "fg",
            "Fog/Mist" to "fg",
            "Freezing Fog" to "fg",
            "Shallow Fog" to "fg",
            "Partial Fog" to "fg",
            "Patches of Fog" to "fg",
            "Fog in Vicinity" to "fg",
            "Freezing Fog in Vicinity" to "fg",
            "Shallow Fog in Vicinity" to "fg",
            "Partial Fog in Vicinity" to "fg",
            "Patches of Fog in Vicinity" to "fg",
            //"Showers in Vicinity Fog" to "fg",
            "Light Freezing Fog" to "fg",
            "Heavy Freezing Fog" to "fg",
            "Precipitation and Mist" to "fg"
    )

    init {
        val obsClosest = UtilityMetar.findClosestObservation(context, location)
        UtilityUSv2.obsClosestClass = obsClosest.name
        val observationData = ("https://api.weather.gov/stations/" + obsClosest.name + "/observations/current").getNwsHtml()
        icon = observationData.parse("\"icon\": \"(.*?)\",")
        condition = observationData.parse("\"textDescription\": \"(.*?)\",")
        // FIXME HTTPS
        val metarData = ("${MyApplication.NWS_RADAR_PUB}/data/observations/metar/decoded/" + obsClosest.name + ".TXT").getHtmlSep().replace("<br>", MyApplication.newline)
        //val metarData = ("${MyApplication.NWS_RADAR_PUB}/data/observations/metar/decoded/" + obsClosest.name + ".TXT").getHtmlSepUnsafe().replace("<br>", MyApplication.newline)
        temperature = metarData.parse("Temperature: (.*?) F")
        dewpoint = metarData.parse("Dew Point: (.*?) F")
        windDirection = metarData.parse("Wind: from the (.*?) \\(.*? degrees\\) at .*? MPH ")
        windSpeed = metarData.parse("Wind: from the .*? \\(.*? degrees\\) at (.*?) MPH ")
        windGust = metarData.parse("Wind: from the .*? \\(.*? degrees\\) at .*? MPH \\(.*? KT\\) gusting to (.*?) MPH")
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
                conditionsTimeStr = UtilityTime.convertFromUTCForMetar(localStatus[1].replace(" UTC", ""))
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



