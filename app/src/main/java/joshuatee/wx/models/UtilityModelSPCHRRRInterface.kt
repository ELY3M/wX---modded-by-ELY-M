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

package joshuatee.wx.models

@Suppress("SpellCheckingInspection")
internal object UtilityModelSpcHrrrInterface {

    val models = listOf(
            "HRRR"
    )

    val sectors = listOf(
            "National",
            "Northwest US",
            "Southwest US",
            "Northern Plains",
            "Central Plains",
            "Southern Plains",
            "Northeast US",
            "Mid Atlantic",
            "Southeast US",
            "Midwest"
    )

    val sectorCodes = listOf(
            "S19",
            "S11",
            "S12",
            "S13",
            "S14",
            "S15",
            "S16",
            "S17",
            "S18",
            "S20"
    )

    val params = listOf(
            "refc",
            "pmsl",
            "srh3",
            "cape",
            "proxy",
            "wmax",
            "scp",
            "uh",
            "ptype",
            "ttd"
    )

    val labels = listOf(
            "Composite Reflectivity",
            "MSL Pressure & Wind",
            "Shear Parameters",
            "Thermo Parameters",
            "Proxy Indicators",
            "Max Surface Wind",
            "SCP / STP",
            "Updraft Helicity",
            "Winter Parameters",
            "Temp/Dwpt"
    )
}
