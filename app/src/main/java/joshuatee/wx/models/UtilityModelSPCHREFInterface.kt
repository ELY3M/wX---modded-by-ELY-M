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

import joshuatee.wx.ui.ObjectMenuTitle
import android.util.SparseArray

import java.util.Arrays

import joshuatee.wx.util.Group

internal object UtilityModelSPCHREFInterface {

    val models = listOf("HREF")

    private val titles = Arrays.asList(
            ObjectMenuTitle("SPC Guidance", 6),
            ObjectMenuTitle("Synoptic", 5),
            ObjectMenuTitle("Severe", 12),
            ObjectMenuTitle("Winter", 18),
            ObjectMenuTitle("Fire", 11),
            ObjectMenuTitle("Precipitation", 16),
            ObjectMenuTitle("Storm Attributes", 24)
    )
    // FIXME should be 4 more params in storm attributes
    var shortCodes = Array(13) { Array(30) { "" } }
    var longCodes = Array(13) { Array(30) { "" } }
    val groups = SparseArray<Group>()

    internal fun createData() {
        var k = 0
        titles.indices.forEach { index ->
            val group = Group(titles[index].title)
            var m = 0
            for (j in (ObjectMenuTitle.getStart(titles, index) until titles[index].count + ObjectMenuTitle.getStart(titles, index))) {
                group.children.add(labels[j])
                shortCodes[index][m] = params[k]
                longCodes[index][m] = labels[k]
                k += 1
                m += 1
            }
            groups.append(index, group)
        }
    }

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

    val params = listOf(
            "guidance_hail_spchazcal_004h",
            "guidance_tor_spchazcal_004h",
            "guidance_wind_spchazcal_004h",
            "guidance_hail_spchazcal_024h",
            "guidance_tor_spchazcal_024h",
            "guidance_wind_spchazcal_024h",

            "500w_mean,500h_mean",
            "700w_mean,700h_mean",
            "850w_mean,850h_mean",
            "sfct_mean,10mwind_mean,mslp_mean",
            "sfctd_mean,10mwind_mean,mslp_mean",

            "sbcape_max,sfc500shear_mean",
            "sbcape_mean,sfc500shear_mean",
            "sbcape_prob0500",
            "sbcape_prob1000",
            "sbcape_prob2000",
            "sbcape_ps",
            "mucape_mean,sfc500shear_mean",
            "srh03_mean",
            "srh01_mean",
            "stpfixed_mean",
            "stpfixed_prob01",
            "stpfixed_prob03",

            "qpf_001h_mean_ptype",
            "snowfall_001h_mean",
            "snowfall_001h_prob01",
            "snowfall_001h_prob02",
            "snowfall_012h_max",
            "snowfall_012h_mean",
            "snowfall_012h_pmm",
            "snowfall_012h_prob04",
            "snowfall_012h_prob08",
            "snowfall_012h_prob12",
            "snowfall_012h_ps",
            "snowfall_024h_max",
            "snowfall_024h_mean",
            "snowfall_024h_pmm",
            "snowfall_024h_prob04",
            "snowfall_024h_prob08",
            "snowfall_024h_prob12",
            "snowfall_024h_ps",

            "fosberg_max",
            "fosberg_mean",
            "fosberg_prob050",
            "fosberg_prob075",
            "10mwindhm_mean_mph_fill,10mwind_mean_mph",
            "sfcrh_mean",
            "rhwnd_prob1030",
            "rhwnd_prob1515",
            "rhwnd_prob1520",
            "rhwnd_prob2015",
            "rhwnd_prob2020",

            "qpf_001h_mean_ptype",
            "qpf_001h_prob01",
            "qpf_003h_nh1",
            "qpf_003h_mean",
            "qpf_003h_pmm",
            "qpf_006h_max",
            "qpf_006h_mean",
            "qpf_006h_pmm",
            "qpf_006h_prob01",
            "qpf_006h_prob02",
            "qpf_006h_prob03",
            "qpf_006h_ps",
            "qpf_012h_max",
            "qpf_012h_mean",
            "qpf_012h_pmm",
            "qpf_012h_ps",

            "cref_members hrwnssl",
            "cref_members hrwarw",
            "cref_members hrwnmmb",
            "cref_members namnest",
            "cref_pb40_members",
            "cref_ps",
            "ref1km_004hmax_nh40",
            "ref1km_004hmax_pb40_members",
            "ref1km_024hmax_nh40",
            "ref1km_024hmax_pb40_members",
            "uh25_004hmax_max",
            "uh25_004hmax_nh075_fill",
            "uh25_004hmax_nh150_fill",
            "uh25_004hmax_pb075_members,uh25_003hmax_nh075",
            "uh25_004hmax_pb150_members,uh25_003hmax_nh150",
            "uh25_024hmax_max,uh25_024hmax_nh075",
            "uh25_024hmax_nh075_fill",
            "uh25_024hmax_nh150_fill",
            "uh25_024hmax_pb075_members,uh25_024hmax_nh075",
            "uh25_024hmax_pb150_members,uh25_024hmax_nh150",
            "wspd_004hmax_max",
            "wspd_004hmax_pb30_members",
            "wspd_024hmax_max",
            "wspd_024hmax_pb30_members"
    )

    private val labels = listOf(
            "4-hr HREF/SREF Calibrated: Hail",
            "4-hr HREF/SREF Calibrated: Tornado",
            "4-hr HREF/SREF Calibrated: Wind",
            "24-hr HREF/SREF Calibrated: Hail",
            "24-hr HREF/SREF Calibrated: Tornado",
            "24-hr HREF/SREF Calibrated: Wind",

            "500 mb Height/Wind",
            "700 mb Height/Wind",
            "850 mb Height/Wind",
            "2m AGL Temperature/MSLP/Wind",
            "2m AGL Dew Point/MSLP/Wind",

            "Surface Based CAPE: max",
            "Surface Based CAPE: mean",
            "Surface Based CAPE: P[>500]",
            "Surface Based CAPE: P[>1000]",
            "Surface Based CAPE: P[>2000]",
            "Surface Based CAPE: stamps",
            "Most Unstable CAPE: mean",
            "0-3 km SRH: mean",
            "0-1 km SRH: mean",
            "Fixed-Layer STP mean",
            "Fixed-Layer STP P[>1]",
            "Fixed-Layer STP P[>3]",

            "1-hr QPF and Precip Type: mean",
            "1-hr Snowfall: mean",
            "1-hr Snowfall: P[>1\"]",
            "1-hr Snowfall: P[>2\"]",
            "12-hr Snowfall: max",
            "12-hr Snowfall: mean",
            "12-hr Snowfall: PMM",
            "12-hr Snowfall: P[>4\"]",
            "12-hr Snowfall: P[>8\"]",
            "12-hr Snowfall: P[>12\"]",
            "12-hr Snowfall: stamps",
            "24-hr Snowfall: max",
            "24-hr Snowfall: mean",
            "24-hr Snowfall: PMM",
            "24-hr Snowfall: P[>4\"]",
            "24-hr Snowfall: P[>8\"]",
            "24-hr Snowfall: P[>12\"]",
            "24-hr Snowfall: stamps",
            "Fosberg Index: max",
            "Fosberg Index: mean",
            "Fosberg Index: P[>50]",
            "Fosberg Index: P[>75]",
            "10 m AGL Wind (Hourly Max): mean",
            "Relative Humidity: mean",
            "Relative Humidity and Wind: CP[RH<10%, V>30mph]",
            "Relative Humidity and Wind: CP[RH<15%, V>15mph]",
            "Relative Humidity and Wind: CP[RH<15%, V>20mph]",
            "Relative Humidity and Wind: CP[RH<20%, V>15mph]",
            "Relative Humidity and Wind: CP[RH<20%, V>20mph]",

            "1-hr QPF and Precip Type: mean",
            "1-hr QPF: P[>1\"]",
            "3-hr QPF: max",
            "3-hr QPF: mean",
            "3-hr QPF: pmm",
            "6-hr QPF: max",
            "6-hr QPF: mean",
            "6-hr QPF: pmm",
            "6-hr QPF: P[>1\"]",
            "6-hr QPF: P[>2\"]",
            "6-hr QPF: P[>3\"]",
            "6-hr QPF: stamps",
            "24-hr QPF: max",
            "24-hr QPF: mean",
            "24-hr QPF: pmm",
            "24-hr QPF: stamps",

            "Reflectivity: hrwnssl",
            "Reflectivity: hrwarw",
            "Reflectivity: hrwnmmb",
            "Reflectivity: namnest",
            "Reflectivity: PB[>40]",
            "Reflectivity: stamps",
            "4-hr max Reflectivity: max",
            "4-hr max Reflectivity: PB[>40]",
            "24-hr max Reflectivity: max",
            "24-hr max Reflectivity: PB[>40]",
            "4-hr max Updraft Helicity (2-5 km): max",
            "4-hr max Updraft Helicity (2-5 km): NP[>75]",
            "4-hr max Updraft Helicity (2-5 km): NP[>150]",
            "4-hr max Updraft Helicity (2-5 km): PB[>75]",
            "4-hr max Updraft Helicity (2-5 km): PB[>150]",
            "24-hr max Updraft Helicity (2-5 km): max",
            "24-hr max Updraft Helicity (2-5 km): NP[>75]",
            "24-hr max Updraft Helicity (2-5 km): NP[>150]",
            "24-hr max Updraft Helicity (2-5 km): PB[>75]",
            "24-hr max Updraft Helicity (2-5 km): PB[>150]",
            "4-hr max Updraft: max",
            "4-hr max Updraft: PB[>20]",
            "24-hr max Updraft: max",
            "24-hr max Updraft: PB[>20]",
            "4-hr max Wind Speed: max",
            "4-hr max Wind Speed: PB[>30]",
            "24-hr max Wind Speed: max",
            "24-hr max Wind Speed: PB[>30]",
            "10m 3-hr max Wind Speed: max PB[>30]",
            "10m 3-hr max Wind Speed: PB[>30]",
            "10m 24-hr max Wind Speed: max",
            "10m 24-hr max Wind Speed: PB[>30]"
    )
}
