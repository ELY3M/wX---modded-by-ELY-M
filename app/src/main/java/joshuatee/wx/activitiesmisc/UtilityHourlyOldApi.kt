/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2021 joshua.tee@gmail.com

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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityTime
import joshuatee.wx.util.UtilityString

object UtilityHourlyOldApi {

    fun getHourlyString(locNumber: Int): String {
        val latLon: LatLon = Location.getLatLon(locNumber)
        val html: String = UtilityIO.getHtml("https://forecast.weather.gov/MapClick.php?lat=" +
                latLon.latString + "&lon=" +
                latLon.lonString + "&FcstType=digitalDWML")
        val header: String = "Time".ljust(13) + " " + "Temp".ljust(5) + "Dew".ljust(5) + "Precip%".ljust(7) + "Cloud%".ljust(6) + MyApplication.newline
        return MyApplication.newline + header + parseHourly(html)
    }

    private fun parseHourly(html: String): String {
        val regexpList = arrayOf(
            "<temperature type=.hourly.*?>(.*?)</temperature>",
            "<temperature type=.dew point.*?>(.*?)</temperature>",
            "<time-layout.*?>(.*?)</time-layout>",
            "<probability-of-precipitation.*?>(.*?)</probability-of-precipitation>",
            "<cloud-amount type=.total.*?>(.*?)</cloud-amount>"
        )
        val rawData = UtilityString.parseXmlExt(regexpList, html)
        val temp2List = UtilityString.parseXmlValue(rawData[0])
        val temp3List = UtilityString.parseXmlValue(rawData[1])
        val time2List = UtilityString.parseXml(rawData[2], "start-valid-time")
        val temp4List = UtilityString.parseXmlValue(rawData[3])
        val temp5List = UtilityString.parseXmlValue(rawData[4])
        var sb = ""
        val year = UtilityTime.getYear()
        val temp2Len = temp2List.size
        val temp3Len = temp3List.size
        val temp4Len = temp4List.size
        val temp5Len = temp5List.size
        for (j in 1 until temp2Len) {
            time2List[j] = UtilityString.replaceAllRegexp(time2List[j], "-0[0-9]:00", "")
            time2List[j] = UtilityString.replaceAllRegexp(time2List[j], "^.*?-", "")
            time2List[j] = time2List[j].replace("T", " ")
            time2List[j] = time2List[j].replace("00:00", "00")
            val timeSplit = time2List[j].split(" ")
            val timeSplit2 = timeSplit[0].split("-")
            val month = timeSplit2[0].toIntOrNull() ?: 0
            val day = timeSplit2[1].toIntOrNull() ?: 0
            val dayOfTheWeek: String = UtilityTime.dayOfWeek(year, month, day)
            var temp3Val = "."
            var temp4Val = "."
            var temp5Val = "."
            if (temp2Len == temp3Len) {
                temp3Val = temp3List[j]
            }
            if (temp2Len == temp4Len) {
                temp4Val = temp4List[j]
            }
            if (temp2Len == temp5Len) {
                temp5Val = temp5List[j]
            }
            time2List[j] = time2List[j].replace(":00", "")
            time2List[j] = time2List[j].strip()
            sb += (dayOfTheWeek + " " + time2List[j]).replace("\n", "").ljust(9)
            sb += "   "
            sb += temp2List[j].ljust(5)
            sb += temp3Val.ljust(5)
            sb += temp4Val.ljust(7)
            sb += temp5Val.ljust(6)
            sb += MyApplication.newline
        }
        return sb
    }
}

