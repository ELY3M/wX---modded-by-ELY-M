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

package joshuatee.wx.objects

import android.graphics.Color

// The goal of this enum is to ease adoption of new polygon warnings ( ie those that provide LAT LON )
// Previous there would by MyApp vars for enablement/color and data storage
// Need to modify settings -> radar, settings -> colors, wxrender and i'm sure some other stuff
// TODO setup datastructures in myApp that are Maps based for example a Map of PolygonWarningType: boolean ( for enablement )

// NWS default colors: https://www.weather.gov/bro/mapcolors
// BETTER NWS default colors: https://www.weather.gov/help-map

// TODO DSW Dust%20Storm%20Warning
// TODO SQW Snow%20Squall%20Warning
//

enum class PolygonWarningType constructor(
        var productCode: String,
        var urlToken: String,
        var prefTokenEnabled: String,
        var prefTokenColor: String,
        var prefTokenStorage: String,
        var initialColor: Int
) {

    // FIXME all pref Vars should be hanled in object and use product code

    /*


        private const val torUrl = baseUrl + "Tornado%20Warning"
    private const val svrURl = baseUrl + "Severe%20Thunderstorm%20Warning"
    private const val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    private const val ewwUrl = baseUrl + "Extreme%20Wind%20Warning"
    private const val smwUrl = baseUrl + "Special%20Marine%20Warning"
    private const val svsUrl = baseUrl + "Severe%20Weather%20Statement"
    private const val spsUrl = baseUrl + "Special%20Weather%20Statement"

            val wEww = SevereWarning(PolygonType.EWW)
            val wFfw = SevereWarning(PolygonType.FFW)
            val wSmw = SevereWarning(PolygonType.SMW)
            val wSvs = SevereWarning(PolygonType.SVS)

            radarColorEww = getInitialPreference("RADAR_COLOR_EWW", Color.GRAY)
            radarColorFfw = getInitialPreference("RADAR_COLOR_FFW", Color.GREEN)
            radarColorSmw = getInitialPreference("RADAR_COLOR_SMW", Color.CYAN)
            radarColorSvs = getInitialPreference("RADAR_COLOR_SVS", Color.rgb(255, 203, 103))
            radarColorSps = getInitialPreference("RADAR_COLOR_SPS", Color.rgb(255, 204, 102))

    */


    
    ExtremeWindWarning(
            "EWW",
            "Extreme%20Wind%20Warning",
            "RADAR_SHOW_EWW",
            "RADAR_COLOR_EWW",
            "SEVERE_DASHBOARD_EWW",
            Color.GRAY
    ),

    SpecialMarineWarning(
            "SMW",
            "Special%20Marine%20Warning",
            "RADAR_SHOW_SMW",
            "RADAR_COLOR_SMW",
            "SEVERE_DASHBOARD_SMW",
            Color.CYAN
    ),
    SnowSquallWarning(
            "SQW",
            "Snow%20Squall%20Warning",
            "RADAR_SHOW_SQW",
            "RADAR_COLOR_SQW",
            "SEVERE_DASHBOARD_SQW",
            Color.rgb(199, 21, 133)
    ),
    DustStormWarning(
            "DSW",
            "Dust%20Storm%20Warning",
            "RADAR_SHOW_DSW",
            "RADAR_COLOR_DSW",
            "SEVERE_DASHBOARD_DSW",
            Color.rgb(255, 228, 196)
    ),
    SevereWeatherStatement(
            "SVS",
            //"Flood%20Warning",
            "Severe%20Weather%20Statement",
            "RADAR_SHOW_SVS",
            "RADAR_COLOR_SVS",
            "SEVERE_DASHBOARD_SVS",
            Color.rgb(255, 203, 103)
    ),
    SpecialWeatherStatement(
            "SPS",
            "Special%20Weather%20Statement",
            "RADAR_SHOW_SPS",
            "RADAR_COLOR_SPS",
            "SEVERE_DASHBOARD_SPS",
            Color.rgb(255, 204, 102)
    );
}


