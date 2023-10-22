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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.Location
import joshuatee.wx.objects.LatLon

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
    var latLon = LatLon(0.0, 0.0)

    constructor()

    constructor(context: Context, locationNumber: Int) {
        if (Location.isUS(locationNumber)) {
            latLon = Location.getLatLon(locationNumber)
            process(context, Location.getLatLon(locationNumber))
        } else {
            isUS = false
        }
    }

    constructor(context: Context, latLon: LatLon) {
        this.latLon = latLon
        process(context, latLon)
    }

    private fun process(context: Context, latLon: LatLon, index: Int = 0) {
        val objectMetar = ObjectMetar(context, latLon, index)
        data = objectMetar.temperature + GlobalVariables.degreeSymbol
        if (objectMetar.windChill != "NA") {
            data += "(${objectMetar.windChill}${GlobalVariables.degreeSymbol})"
        } else if (objectMetar.heatIndex != "NA" && objectMetar.heatIndex != objectMetar.temperature) {
            data += "(${objectMetar.heatIndex}${GlobalVariables.degreeSymbol})"
        }
        data += " / ${objectMetar.dewPoint}${GlobalVariables.degreeSymbol}(${objectMetar.relativeHumidity}%) - "
        data += "${objectMetar.seaLevelPressure} - ${objectMetar.windDirection} ${objectMetar.windSpeed}"
        if (objectMetar.windGust != "") {
            data += " G "
        }
        data += "${objectMetar.windGust} mph - ${objectMetar.visibility} mi - ${objectMetar.condition}"
        iconUrl = objectMetar.icon
        status = UtilityUS.getStatusViaMetar(context, objectMetar.conditionsTimeStr, objectMetar.obsClosest.name)
        timeStringUtc = objectMetar.timeStringUtc
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
    fun timeCheck(context: Context) {
        if (isUS) {
            val obsTime = ObjectDateTime.fromObs(timeStringUtc)
            val currentTime = ObjectDateTime.getCurrentTimeInUTC()
            val isTimeCurrent = ObjectDateTime.timeDifference(currentTime, obsTime.dateTime, 120)
            if (!isTimeCurrent) {
                process(context, latLon, 1)
            }
        }
    }
}
