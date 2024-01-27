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

import joshuatee.wx.getHtml
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityString

object UtilityRtma {

    val labels = listOf(
            "2-Meter Temperature (F)",
            "10-Meter Wind Speed (Knots) / Direction",
            "2-Meter Dew Point (F)",
    )

    val codes = listOf(
            "2m_temp",
            "10m_wnd",
            "2m_dwpt",
    )

    val sectors = listOf(
            "alaska",
            "ca",
            "co",
            "fl",
            "guam",
            "gulf-coast",
            "mi",
            "mid-atl",
            "mid-west",
            "mt",
            "nc_sc",
            "nd_sd",
            "new-eng",
            "nw-pacific",
            "ohio-valley",
            "sw_us",
            "tx",
            "wi"
    )

    // approx based off inspection
    private val sectorToLatLon = mapOf(
            "alaska" to LatLon(63.25, -156.5),
            "ca" to LatLon(38.0, -118.5),
            "co" to LatLon(39.0, -105.25),
            "fl" to LatLon(27.5, -83.25),
            "guam" to LatLon(13.5, 144.75),
            "gulf-coast" to LatLon(32.75, -90.25),
            "mi" to LatLon(43.75, -84.75),
            "mid-atl" to LatLon(39.75, -75.75),
            "mid-west" to LatLon(39.5, -93.0),
            "mt" to LatLon(45.0, -109.25),
            "nc_sc" to LatLon(34.5, -79.75),
            "nd_sd" to LatLon(45.5, -98.25),
            "new-eng" to LatLon(43.0, -71.25),
            "nw-pacific" to LatLon(45.5, -122.75),
            "ohio-valley" to LatLon(39.0, -84.75),
            "sw_us" to LatLon(34.5, -104.25),
            "tx" to LatLon(32.0, -100.25),
            "wi" to LatLon(44.25, -89.75)
    )

    fun getNearest(latLon: LatLon): String = UtilityLocation.getNearest(latLon, sectorToLatLon)

    fun getTimes(): List<String> {
        val html = "https://mag.ncep.noaa.gov/observation-parameter.php?group=Observations%20and%20Analyses&obstype=RTMA&area=MI&ps=area".getHtml()
        // title="20221116 00 UTC"
        return UtilityString.parseColumn(html, "([0-9]{8} [0-9]{2} UTC)").distinct()
    }

    fun getUrl(code: String, sector: String, runTime: String): String {
        val currentRun = runTime.split(" ").getOrNull(1) ?: ""
        return "https://mag.ncep.noaa.gov/data/rtma/${currentRun}/rtma_${sector}_000_${code}.gif"
    }

    fun getUrlForHomeScreen(product: String): String {
        val sector = getNearest(Location.latLon)
        val runTimes = getTimes()
        val runTime = runTimes.getOrNull(0) ?: ""
        val currentRun = runTime.split(" ").getOrNull(1) ?: ""
        return "https://mag.ncep.noaa.gov/data/rtma/$currentRun/rtma_${sector}_000_${product}.gif"
    }
}
