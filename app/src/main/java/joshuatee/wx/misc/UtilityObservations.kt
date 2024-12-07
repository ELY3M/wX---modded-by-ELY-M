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

@Suppress("SpellCheckingInspection")
internal object UtilityObservations {

    val urls = listOf(
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/sfcobs/large_latestsfc.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namswsfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namscsfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namsesfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namcwsfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namccsfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namcesfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namnwsfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namncsfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namnesfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namaksfcwbg.gif",
        "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/sfc/namak2sfcwbg.gif",
            "https://ocean.weather.gov/UA/Conus.gif",
            "https://ocean.weather.gov/UA/West_coast.gif",
            "https://ocean.weather.gov/UA/USA_West.gif",
            "https://ocean.weather.gov/UA/USA_Mid_West.gif",
            "https://ocean.weather.gov/UA/USA_East.gif",
            "https://ocean.weather.gov/UA/East_coast.gif",
            "https://ocean.weather.gov/UA/Hawaii.gif",
            "https://ocean.weather.gov/UA/Alaska.gif",
            "https://ocean.weather.gov/UA/Canada.gif",
            "https://ocean.weather.gov/UA/USA_South.gif",
            "https://ocean.weather.gov/UA/Mexico.gif",
            "https://ocean.weather.gov/UA/OPC_PAC.gif",
            "https://ocean.weather.gov/UA/Pac_Tropics.gif",
            "https://ocean.weather.gov/UA/Pac_Difax.gif",
            "https://ocean.weather.gov/UA/OPC_ATL.gif",
            "https://ocean.weather.gov/UA/Atl_Tropics.gif",
            "https://ocean.weather.gov/UA/Atl_Difax.gif"
    )

    val labels = listOf(
        "CONUS Surface Obs",
        "SW Surface Analysis",
        "SC Surface Analysis",
        "SE Surface Analysis",
        "CW Surface Analysis",
        "C Surface Analysis",
        "CE Surface Analysis",
        "NW Surface Analysis",
        "NC Surface Analysis",
        "NE Surface Analysis",
        "AK Surface Analysis",
        "Gulf of AK Surface Analysis",
        "UA - Continental USA",
        "UA - West Coast",
        "UA - USA West",
        "UA - USA Mid West",
        "UA - USA East",
        "UA - East Coast",
        "UA - Hawaii",
        "UA - Alaska",
        "UA - Canada",
        "UA - USA South",
        "UA - Gulf of Mexico",
        "UA - Pacific Ocean",
        "UA - Pacific Tropical",
        "UA - Pacific Ocean Difax",
        "UA - Atlantic Ocean",
        "UA - Atlantic Tropical",
        "UA - Atlantic Ocean Difax"
    )
}
