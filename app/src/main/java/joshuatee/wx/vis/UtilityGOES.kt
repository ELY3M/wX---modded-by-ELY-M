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

package joshuatee.wx.vis

import android.content.Context
import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility

object UtilityGOES {

    private val COD_HASH = mapOf(
            "WA" to "NW",
            "ID" to "NW",
            "OR" to "NW",
            "CA" to "WC",
            "NV" to "WC",
            "UT" to "WC",
            "AZ" to "SW",
            "NM" to "SW",
            "ND" to "NP",
            "SD" to "NP",
            "MT" to "NP",
            "WY" to "NP",
            "CO" to "CP",
            "NE" to "CP",
            "KS" to "CP",
            "OK" to "CP",
            "TX" to "SC",
            "LA" to "SC",
            "MN" to "GL",
            "WI" to "GL",
            "MI" to "GL",
            "IA" to "MW",
            "IN" to "MW",
            "IL" to "MW",
            "TN" to "MW",
            "MO" to "MW",
            "AR" to "MW",
            "FL" to "SE",
            "MS" to "SE",
            "AL" to "SE",
            "GA" to "SE",
            "SC" to "MA",
            "NC" to "MA",
            "KY" to "MA",
            "OH" to "MA",
            "WV" to "MA",
            "VA" to "MA",
            "PA" to "MA",
            "NJ" to "MA",
            "DE" to "MA",
            "ME" to "NE",
            "MA" to "NE",
            "NH" to "NE",
            "VT" to "NE",
            "CT" to "NE",
            "RI" to "NE",
            "NY" to "NE",
            "AK" to "AK",
            "HI" to "HI"
    )

    val RADS = listOf(
            "TATL:EAST: Atlantic Wide View",
            "TPAC:WEST: East & Central Pacific Wide View",
            "EPAC:WEST: East Pacific ",
            "CPAC:WEST: Central Pacific",
            "HI:WEST: Hawaii",
            "NEPAC:WEST: Northeast Pacific",
            "WEUS:WEST: Western United States"
    )

    val RADS_TOP = listOf(
            "WEUS:WEST: Western United States",
            "HI:WEST: Hawaii",
            "AK:WEST: Alaska",
            "TPAC:WEST: East & Central Pacific Wide View",
            "EPAC:WEST: East Pacific ",
            "CPAC:WEST: Central Pacific",
            "NEPAC:WEST: Northeast Pacific",
            "JMA:NWPAC: Northwest Pacific",
            "JMA:WPAC: West Pacific",
            "JMA:WCPAC: West Central Pacific",
            "JMA:JAPAN: ",
            "JMA:TEASIA: ",
            "JMA:TSWPAC: ",
            "JMA:TWAUS: ",
            "JMA:TWPAC: Tropical West Pacific",
            "JMA:TWPACN: ",
            "EUMET:EATL: East Atlantic",
            "EUMET:NEATL: Northeast Atlantic"
    )

    private fun getGOESSectorFromState(state: String) = COD_HASH[state] ?: ""

    fun getGOESSectorFromNWSOffice(context: Context, office: String) = getGOESSectorFromState(Utility.readPref(context, "NWS_LOCATION_" + office.toUpperCase(Locale.US), "").split(",")[0]).toLowerCase(Locale.US)

    fun getGOESSatSectorFromSector(sector: String): String {
        var sectorSatRet = ""
        for (rad in RADS_TOP) {
            if (rad.startsWith(sector.toUpperCase(Locale.US))) {
                val tmpArr = MyApplication.colon.split(rad)
                sectorSatRet = tmpArr[1].toLowerCase(Locale.US)
                break
            }
        }
        return sectorSatRet
    }

    fun getGOESSatSectorFromNWSOffice(context: Context, office: String): String {
        val satSectorl: String
        val state = Utility.readPref(context, "NWS_LOCATION_" + office.toUpperCase(Locale.US), "").split(",")[0]
        satSectorl = if (state == "AZ" || state == "MT" || state == "ID" || state == "NV" || state == "CA" || state == "OR" || state == "UT" || state == "WA" || state == "AK" || state == "HI")
            "west"
        else
            "east"
        return satSectorl
    }
}