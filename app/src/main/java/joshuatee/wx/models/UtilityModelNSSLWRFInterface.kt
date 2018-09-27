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

package joshuatee.wx.models

internal object UtilityModelNSSLWRFInterface {

    val PARAMS = listOf(
            "pptslp",
            "pptsix",
            "rflslp",
            "rflmax",
            "rfcslp",
            "uphlcy",
            "uph3km",
            "hail",
            "grplmx",
            "wndspd",
            "updrft",
            "dndrft",
            "sfcvis",
            "ltg1mx",
            "ltg2mx",
            "ltg3mx",
            "tmfwnd",
            "tdfwnd",
            "capwnd",
            "csbwnd",
            "stpslp",
            "simsvr",
            "ppthvy",
            "wnd5mb",
            "vrt5mb",
            "wnd2mb"
    )

    val LABELS = listOf(
            "1hr QPF",
            "6hr QPF",
            "1km AGL Sim. Reflectivity",
            "Hourly Max Sim. Reflectivity",
            "Max Column Sim. Reflectivity",
            "Hr Max Updraft Helicity",
            "Hr Max Updraft Hel. (0-3km)",
            "Hr Max Hailsize (HAILCAST)",
            "Hr Max Column Int. Graupel",
            "Hr Max 10m Wind Speed",
            "Hr Max Column Updraft",
            "Hr Max Column Downdraft",
            "Surface Visibility",
            "Hr Max Lightning Threat 1",
            "Hr Max Lightning Threat 2",
            "Hr Max Lightning Threat 3",
            "2m Temperature",
            "2m Dew Point",
            "MUCAPE",
            "SBCAPE",
            "SIG TOR (fixed layer)",
            "Experimental Surrogate Severe",
            "Experimental Heavy Rain Guidance",
            "500mb Z and Winds",
            "500mb Z and Vorticity",
            "250mb Z and Winds"
    )

    val SECTORS = listOf("CONUS")
}
