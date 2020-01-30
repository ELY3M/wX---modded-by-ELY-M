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

package joshuatee.wx.models

internal object UtilityModelNsslWrfInterface {

    val models = listOf(
        "WRF", "FV3", "HRRRV3", "WRF_3KM"
    )

    val paramsNsslWrf = listOf(
        "mslp",
        "10mwind",
        "sfct",
        "sfctd",
        "visibility",
        "250w",
        "500w",
        "850w",

        "mucape",
        "sbcape",
        "srh01",
        "srh03",
        "stpfixed",

        "qpf_001h",
        "qpf_006h",
        "qpf_total",
        "snowfall_total",

        "cref_uh075",
        "maxref1000m",
        "ref1000m_uh075",
        "uh03_004hmax",
        "uh25_004hmax",
        "dd_004hmax",
        "ud_004hmax",
        "wspd_004hmax",
        "graupelsize_001hmax",
        "hailcast_004hmax",
        "ltgthrt1",
        "ltgthrt2",
        "ltgthrt3"
    )

    val labelsNsslWrf = listOf(
        "MSLP (mb)",
        "10 m Wind (kt)",
        "2-m Temperature",
        "2-m Dew Point",
        "Visibility",
        "250 mb",
        "500 mb",
        "850 mb",

        "MUCAPE",
        "SBCAPE",
        "0-1km SRH",
        "0-3km SRH",
        "STP",

        "1-h QPF",
        "6-h QPF",
        "Total QPF",
        "Total Snowfall",

        "Reflectivity - Composite",
        "Reflectivity - 1h-max (1km AGL)",
        "Reflectivity - 1km AGL",
        "Updraft Helicity - 4-h max (0-3km)",
        "Updraft Helicity - 4-h max (2-5km)",
        "Vertical Velocity - 4-h min Downdraft",
        "Vertical Velocity - 4-h max Updraft",
        "Wind Speed - 4-h max",
        "Hail - 1-h max Graupel",
        "Hail - 4-h max HAILCAST",
        "Lightning - 1-h max Threat1",
        "Lightning - 1-h max Threat2",
        "Lightning - 1-h max Threat3"
    )

    val paramsNsslFv3 = listOf(
        "mslp",
        "10mwind",
        "sfct",
        "sfctd",
        "250w",
        "500w",
        "850w",

        "sbcape",
        "srh01",
        "srh03",
        "stpfixed",

        "qpf_001h",
        "qpf_006h",
        "qpf_total",

        "cref_uh075",
        "uh25_004hmax",
        "dd_004hmax",
        "ud_004hmax"
    )

    val labelsNsslFv3 = listOf(
        "MSLP (mb)",
        "10 m Wind (kt)",
        "2-m Temperature",
        "2-m Dew Point",
        "250 mb",
        "500 mb",
        "850 mb",

        "SBCAPE",
        "0-1km SRH",
        "0-3km SRH",
        "STP",

        "1-h QPF",
        "6-h QPF",
        "Total QPF",

        "Reflectivity - Composite",
        "Updraft Helicity - 4-h max (2-5km)",
        "Vertical Velocity - 4-h min Downdraft",
        "Vertical Velocity - 4-h max Updraft"
    )

    val paramsNsslHrrrv3 = listOf(
        "mslp",
        "10mwind",
        "sfct",
        "sfctd",
        "250w",
        "500w",
        "850w",

        "mucape",
        "sbcape",
        "srh01",
        "srh03",
        "stpfixed",

        "qpf_001h",
        "qpf_006h",
        "qpf_total",

        "cref_uh075",
        "maxref1000m",
        "ref1000m_uh075",
        "uh25_004hmax",
        "ud_004hmax",
        "wspd_004hmax"
    )

    val labelsNsslHrrrv3 = listOf(
        "MSLP (mb)",
        "10 m Wind (kt)",
        "2-m Temperature",
        "2-m Dew Point",
        "250 mb",
        "500 mb",
        "850 mb",

        "MUCAPE",
        "SBCAPE",
        "0-1km SRH",
        "0-3km SRH",
        "STP",

        "1-h QPF",
        "6-h QPF",
        "Total QPF",

        "Reflectivity - Composite",
        "Reflectivity - 1h-max (1km AGL)",
        "Reflectivity - 1km AGL",
        "Updraft Helicity - 4-h max (2-5km)",
        "Vertical Velocity - 4-h max Updraft",
        "Wind Speed - 4-h max"
    )

    val sectorsLong = listOf(
        "CONUS",
        "Central Plains",
        "Mid Atlantic",
        "Midwest",
        "Northeast",
        "Northern Plains",
        "Northwest",
        "Southeast",
        "Southern Plains",
        "Southwest"
    )

    val sectors = listOf(
        "conus",
        "cp",
        "ma",
        "mw",
        "ne",
        "np",
        "nw",
        "se",
        "sp",
        "sw"
    )
}
