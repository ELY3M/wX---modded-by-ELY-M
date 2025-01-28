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

import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.Location
import joshuatee.wx.objects.LatLon
import joshuatee.wx.radar.Metar

class CurrentConditions {

    private var isUS = true
    var topLine = ""
    var data = ""
        private set
    var iconUrl = ""
        private set
    var status = ""
        private set
    private var timeStringUtc = ""
    var latLon = LatLon.empty()

    constructor()

    constructor(locationNumber: Int) {
        if (Location.isUS(locationNumber)) {
            latLon = Location.getLatLon(locationNumber)
            process(Location.getLatLon(locationNumber))
        } else {
            isUS = false
        }
    }

    constructor(latLon: LatLon) {
        this.latLon = latLon
        process(latLon)
    }

    private fun process(latLon: LatLon, index: Int = 0) {
        val objectMetar = ObjectMetar(latLon, index)
        data = objectMetar.temperature + GlobalVariables.DEGREE_SYMBOL
        if (objectMetar.windChill != "NA") {
            data += "(${objectMetar.windChill}${GlobalVariables.DEGREE_SYMBOL})"
        } else if (objectMetar.heatIndex != "NA" && objectMetar.heatIndex != objectMetar.temperature) {
            data += "(${objectMetar.heatIndex}${GlobalVariables.DEGREE_SYMBOL})"
        }
        data += " / ${objectMetar.dewPoint}${GlobalVariables.DEGREE_SYMBOL}(${objectMetar.relativeHumidity}%) - "
        data += "${objectMetar.seaLevelPressure} - ${objectMetar.windDirection} ${objectMetar.windSpeed}"
        if (objectMetar.windGust != "") {
            data += " G "
        }
        data += "${objectMetar.windGust} mph - ${objectMetar.visibility} mi - ${objectMetar.condition}"
        iconUrl = objectMetar.icon
        status =
            objectMetar.conditionsTimeStr + " " + getObsFullName(objectMetar.obsClosest.codeName)
        timeStringUtc = objectMetar.timeStringUtc
    }

    private fun getObsFullName(obsSite: String): String {
        val locationName = Metar.sites.byCode[obsSite]!!.fullName
        return UtilityString.capitalizeString(locationName)
            .trim { it <= ' ' } + " (" + obsSite + ") "
    }

    fun format() {
        val dataList = data.split(" - ")
        topLine = if (dataList.size > 4) {
            val items = dataList[0].split("/")
            dataList[4].replace("^ ", "") + " " + items[0] + dataList[2]
        } else {
            ""
        }
    }

    // compare the timestamp in the metar to the current time
    // if older then a certain amount, download the 2nd closest site and process
    fun timeCheck() {
        if (isUS) {
            val obsTime = ObjectDateTime.fromObs(timeStringUtc)
            val currentTime = ObjectDateTime.getCurrentTimeInUTC()
            val isTimeCurrent = ObjectDateTime.timeDifference(currentTime, obsTime.dateTime, 120)
            if (!isTimeCurrent) {
                process(latLon, 1)
            }
        }
    }
}
