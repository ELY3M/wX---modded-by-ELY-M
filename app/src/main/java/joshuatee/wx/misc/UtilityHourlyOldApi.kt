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

package joshuatee.wx.misc

import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.Location
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityString

object UtilityHourlyOldApi {

    fun getHourlyString(locNumber: Int): String {
        val latLon = Location.getLatLon(locNumber)
        val html = UtilityDownloadNws.getHourlyOldData(latLon)
        val header = To.stringPadLeft("Time", 12) + " " +
                To.stringPadLeft("Temp", 8) +
                To.stringPadLeft("Dew", 8) +
                To.stringPadLeft("Precip%", 8) +
                To.stringPadLeft("Cloud%", 8) + GlobalVariables.newline
        return GlobalVariables.newline + header + parseHourly(html)
    }

    private fun parseHourly(html: String): String {
        val rawData = UtilityString.parseXmlExt(regexpList, html)
        val temp2List = UtilityString.parseXmlValue(rawData[0])
        val temp3List = UtilityString.parseXmlValue(rawData[1])
        val time2List = UtilityString.parseXml(rawData[2], "start-valid-time").toTypedArray()
        val temp4List = UtilityString.parseXmlValue(rawData[3])
        val temp5List = UtilityString.parseXmlValue(rawData[4])
        var sb = ""
        val temp2Len = temp2List.size
        val temp3Len = temp3List.size
        val temp4Len = temp4List.size
        val temp5Len = temp5List.size
        for (j in 1 until temp2Len) {
            var temp3Val = "."
            var temp4Val = "."
            var temp5Val = "."
            if (temp2Len <= temp3Len) {
                temp3Val = temp3List[j]
            }
            if (temp2Len <= temp4Len) {
                temp4Val = temp4List[j]
            }
            if (temp2Len <= temp5Len) {
                temp5Val = temp5List[j]
            }
            val time = ObjectDateTime.translateTimeForHourly(time2List[j])
            sb += To.stringPadLeft(time, 10)
            sb += "   "
            sb += To.stringPadLeft(temp2List[j], 8)
            sb += To.stringPadLeft(temp3Val, 8)
            sb += To.stringPadLeft(temp4Val, 8)
            sb += To.stringPadLeft(temp5Val, 8)
            sb += GlobalVariables.newline
        }
        return sb
    }

    private val regexpList = listOf(
            "<temperature type=.hourly.*?>(.*?)</temperature>",
            "<temperature type=.dew point.*?>(.*?)</temperature>",
            "<time-layout.*?>(.*?)</time-layout>",
            "<probability-of-precipitation.*?>(.*?)</probability-of-precipitation>",
            "<cloud-amount type=.total.*?>(.*?)</cloud-amount>"
    )
}
