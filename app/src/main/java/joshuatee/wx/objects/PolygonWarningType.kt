/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
//modded by ELY M. 

package joshuatee.wx.objects

import android.graphics.Color

// The goal of this enum is to ease adoption of new polygon warnings ( ie those that provide LAT LON )
// Previous there would by MyApp vars for enablement/color and data storage
// Need to modify settings -> radar, settings -> colors, wxrender and i'm sure some other stuff
// TODO setup datastructures in myApp that are Maps based for example a Map of PolygonWarningType: boolean ( for enablement )

// NWS default colors: https://www.weather.gov/bro/mapcolors
// NWS default colors: https://www.weather.gov/help-map
enum class PolygonWarningType constructor(
        var productCode: String,
        var urlToken: String,
        var initialColor: Int
) {




    
    ExtremeWindWarning(
            "EWW",
            "Extreme%20Wind%20Warning",
            Color.GRAY
    ),

    SpecialMarineWarning(
            "SMW",
            "Special%20Marine%20Warning",
            Color.CYAN
    ),
    SnowSquallWarning(
            "SQW",
            "Snow%20Squall%20Warning",
            Color.rgb(199, 21, 133)
    ),
    DustStormWarning(
            "DSW",
            "Dust%20Storm%20Warning",
            Color.rgb(255, 228, 196)
    ),
    SevereWeatherStatement(
            "SVS",
            "Severe%20Weather%20Statement",
            Color.rgb(255, 203, 103)
    ),
    SpecialWeatherStatement(
            "SPS",
            "Special%20Weather%20Statement",
            Color.rgb(255, 204, 102)
    );
}


