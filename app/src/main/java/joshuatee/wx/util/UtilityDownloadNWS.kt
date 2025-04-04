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

import java.util.Locale
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getHtmlWithRetry
import joshuatee.wx.parse

// https://www.weather.gov/documentation/services-web-api
// default format via accept header is GeoJSON: application/geo+json

object UtilityDownloadNws {

    // used for USWarningsWithRadarActivity / AlertSummary / CapAlert.initializeFromCap (XML) - CONUS wide alerts activity
    fun getCap(sector: String): String = if (sector == "us") {
        UtilityNetworkIO.getStringFromUrlXml(GlobalVariables.NWS_API_URL + "/alerts/active?region_type=land")
    } else {
        UtilityNetworkIO.getStringFromUrlXml(
            GlobalVariables.NWS_API_URL + "/alerts/active/area/" + sector.uppercase(
                Locale.US
            )
        )
    }

    fun getHourlyData(latLon: LatLon): String {
        val pointsData = getLocationPointData(latLon)
        val hourlyUrl = pointsData.parse("\"forecastHourly\": \"(.*?)\"")
        return hourlyUrl.getHtmlWithRetry(1000)
    }

    fun getHourlyOldData(latLon: LatLon): String {
        return UtilityIO.getHtml(
            "https://forecast.weather.gov/MapClick.php?lat=" +
                    latLon.latForNws + "&lon=" +
                    latLon.lonForNws + "&FcstType=digitalDWML"
        )
    }

    fun get7DayData(latLon: LatLon): String = if (UIPreferences.useNwsApi) {
        val pointsData = getLocationPointData(latLon)
        val forecastUrl = pointsData.parse("\"forecast\": \"(.*?)\"")
        forecastUrl.getHtmlWithRetry(3000)
    } else {
        UtilityUS.getLocationHtml(latLon)
    }

    fun getLocationPointData(latLon: LatLon): String =
        (GlobalVariables.NWS_API_URL + "/points/" + latLon.latForNws + "," + latLon.lonForNws).getHtml()
}
