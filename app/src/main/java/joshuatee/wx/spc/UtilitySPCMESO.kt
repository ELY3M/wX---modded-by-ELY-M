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

package joshuatee.wx.spc

import android.util.SparseArray
import java.util.Arrays
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.ObjectMenuTitle
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.util.Group
import joshuatee.wx.util.UtilityLog

object UtilitySPCMESO {

    private val TITLES = Arrays.asList(
            ObjectMenuTitle("Observations", 3),
            ObjectMenuTitle("Surface", 15),
            ObjectMenuTitle("Upper Air", 23),
            ObjectMenuTitle("Thermodynamics", 19),
            ObjectMenuTitle("Wind Shear", 17),
            ObjectMenuTitle("Composite Indices", 21),
            ObjectMenuTitle("Multi-Parameter Fields", 10),
            ObjectMenuTitle("Heavy Rain", 5),
            ObjectMenuTitle("Winter Weather", 14),
            ObjectMenuTitle("Fire Weather", 6),
            ObjectMenuTitle("Classic", 3),
            ObjectMenuTitle("Beta", 9))

    private var swipePosition = 0

    internal fun moveForward(spinnerTime: ObjectSpinner) {
        if (spinnerTime.size() > 3) {
            swipePosition += if (swipePosition == 0) {
                3
            } else {
                1
            }
            UtilityLog.d("wx forward", spinnerTime.size().toString())
            UtilityLog.d("wx forward", swipePosition.toString())
            if (swipePosition >= spinnerTime.size()) {
                swipePosition = 0
            }
            spinnerTime.setSelection(swipePosition)
        }
    }

    internal fun moveBack(spinnerTime: ObjectSpinner) {
        if (spinnerTime.size() > 3) {
            if (swipePosition >= spinnerTime.size()) {
                swipePosition = spinnerTime.size() - 1
            }
            if (swipePosition == 3) {
                swipePosition = 0
            } else {
                swipePosition -= 1
            }
            UtilityLog.d("wx back", spinnerTime.size().toString())
            UtilityLog.d("wx back", swipePosition.toString())
            if (swipePosition == -1) {
                swipePosition = spinnerTime.size() - 1
            }
            spinnerTime.setSelection(swipePosition)
        }
    }

    const val DEFAULT_SECTOR: String = "19"
    internal const val IMG_SF = ":ttd:mcon:thea:mxth:temp_chg:dwpt_chg:mixr_chg:thte_chg:925mb:850mb:700mb:500mb:300mb:sbcp:mlcp:mucp:muli:laps:lllr:lclh:lfch:lfrh:effh:stor:stpc:cpsh:comp:lcls:lr3c:tdlr:qlcs1:qlcs2:pwtr:tran:prop:peff:fzlv:les1:"
    internal fun setParamFromFav(token: String): List<String> {
        var param = ""
        var label = ""
        val tmpArr = MyApplication.spcmesoFav.split(":").dropLastWhile { it.isEmpty() }
        val tmpArrLabel = MyApplication.spcmesoLabelFav.split(":").dropLastWhile { it.isEmpty() }
        when (token) {
            "SPCMESO1" ->
                if (tmpArr.size > 3) {
                    param = tmpArr[3]
                    label = tmpArrLabel[3]
                } else {
                    param = "500mb"
                    label = "500mb Analysis"
                }
            "SPCMESO2" -> if (tmpArr.size > 4) {
                param = tmpArr[4]
                label = tmpArrLabel[4]
            } else {
                param = "pmsl"
                label = "MSL Pressure/Wind"
            }
            "SPCMESO3" -> if (tmpArr.size > 5) {
                param = tmpArr[5]
                label = tmpArrLabel[5]
            } else {
                param = "ttd"
                label = "Temp/Dewpt/Wind"
            }
            "SPCMESO4" -> if (tmpArr.size > 6) {
                param = tmpArr[6]
                label = tmpArrLabel[6]
            } else {
                param = "rgnlrad"
                label = "Radar"
            }
            "SPCMESO5" -> if (tmpArr.size > 7) {
                param = tmpArr[7]
                label = tmpArrLabel[7]
            } else {
                param = "lllr"
                label = "Low-Level Lapse Rates"
            }
            "SPCMESO6" -> if (tmpArr.size > 8) {
                param = tmpArr[8]
                label = tmpArrLabel[8]
            } else {
                param = "laps"
                label = "Mid-Level Lapse Rates"
            }
        }
        return listOf(param, label)
    }

    var SHORT_CODES: Array<Array<String>> = Array(12) { Array(23) { _ -> "" } }
    var LONG_CODES: Array<Array<String>> = Array(12) { Array(23) { _ -> "" } }
    internal val GROUPS = SparseArray<Group>()

    internal fun createData() {
        var k = 0
        TITLES.indices.forEach { index ->
            val group = Group(TITLES[index].title)
            var m = 0
            for (j in (ObjectMenuTitle.getStart(TITLES, index) until TITLES[index].count + ObjectMenuTitle.getStart(TITLES, index))) {
                group.children.add(LABELS[j])
                SHORT_CODES[index][m] = PARAMS[k]
                LONG_CODES[index][m] = LABELS[k]
                k += 1
                m += 1
            }
            GROUPS.append(index, group)
        }
    }

    internal val PARAMS = listOf(
            "bigsfc",
            "1kmv",
            "rgnlrad",
            "pmsl",
            "ttd",
            "mcon",
            "thea",
            "mxth",
            "icon",
            "trap",
            "vtm",
            "dvvr",
            "def",
            "pchg",
            "temp_chg",
            "dwpt_chg",
            "mixr_chg",
            "thte_chg",
            "925mb",
            "850mb",
            "700mb",
            "500mb",
            "300mb",
            "dlcp",
            "sfnt",
            "tadv",
            "7tad",
            "9fnt",
            "8fnt",
            "7fnt",
            "925f",
            "98ft",
            "857f",
            "75ft",
            "vadv",
            "padv",
            "ddiv",
            "ageo",
            "500mb_chg",
            "trap_500",
            "trap_250",
            "sbcp",
            "mlcp",
            "mucp",
            "eltm",
            "ncap",
            "dcape",
            "muli",
            "laps",
            "lllr",
            "maxlr",
            "lclh",
            "lfch",
            "lfrh",
            "sbcp_chg",
            "sbcn_chg",
            "mlcp_chg",
            "mucp_chg",
            "lllr_chg",
            "laps_chg",
            "eshr",
            "shr6",
            "shr8",
            "srh1",
            "brns",
            "effh",
            "srh3",
            "srh1",
            "llsr",
            "mlsr",
            "ulsr",
            "alsr",
            "mnwd",
            "xover",
            "srh3_chg",
            "shr1_chg",
            "shr6_chg",
            "scp",
            "lscp",
            "stor",
            "stpc",
            "sigt1",
            "sigt2",
            "nstp",
            "sigh",
            "sars1",
            "sars2",
            "lghl",
            "dcp",
            "cbsig",
            "brn",
            "mcsm",
            "mbcp",
            "desp",
            "ehi1",
            "ehi3",
            "vgp3",
            "crit",

            "mlcp_eshr",
            "cpsh",
            "comp",
            "lcls",
            "lr3c",
            "3cvr",
            "tdlr",
            "hail",
            "qlcs1",
            "qlcs2",

            "pwtr",
            "pwtr2",
            "tran",
            "prop",
            "peff",

            "ptyp",
            "fztp",
            "swbt",
            "fzlv",
            "thck",
            "epvl",
            "epvm",
            "les1",
            "les2",
            "snsq",
            "dend",
            "dendrh",
            "ddrh",
            "mxwb",
            "sfir",
            "fosb",
            "lhan",
            "mhan",
            "hhan",
            "lasi",
            "ttot",
            "kidx",
            "show",
            "sherbe",
            "moshe",
            "cwasp",
            "eehi",
            "oprh",
            "ptstpe",
            "pstpe",
            "pvstpe",
            "vtp"
    )

    internal val LABELS = listOf(
            "Surface Observations",
            "Visible Satellite",
            "Radar Base Reflectivity",
            "MSL Pressure/Wind",
            "Temp/Dewpt/Wind",
            "Moisture Convergence",
            "Theta-E Advection",
            "Mixing Ratio / Theta",
            "Instantaneous Contraction Rate (sfc)",
            "Fluid Trapping (sfc)",
            "Velocity Tensor Magnitude (sfc)",
            "Divergence and Vorticity (sfc)",
            "Deformation and Axes of Dilitation (sfc)",
            "2-hour Pressure Change",
            "3-hour Temp Change",
            "3-hour Dwpt Change",
            "3-hour 100 mb Mixing Ratio Change",
            "3-hour Theta-E Change",
            "925mb Analysis",
            "850mb Analysis",
            "700mb Analysis",
            "500mb Analysis",
            "300mb Analysis",
            "Deep Moisture Convergence",
            "Sfc Frontogenesis",
            "850mb Temp Advection",
            "700mb Temp Advection",
            "925mb Frontogenesis",
            "850mb Frontogenesis",
            "700mb Frontogenesis",
            "1000-925mb Frontogenesis",
            "925-850mb Frontogenesis",
            "850-700mb Frontogenesis",
            "700-500mb Frontogenesis",
            "700-400mb Diff. Vorticity Advection",
            "400-250mb Pot. Vorticity Advection",
            "850-250mb Diff. Divergence",
            "300mb Jet Circulation",
            "12-hour 500mb Height Change",
            "Fluid Trapping (500 mb)",
            "Fluid Trapping (250 mb)",
            "CAPE - Surface Based",
            "CAPE - 100mb Mixed-Layer",
            "CAPE - Most-Unstable / LPL Height",
            "EL Temp/MUCAPE/MUCIN",
            "CAPE - Normalized",
            "CAPE - Downdraft",
            "Surface-based Lifted Index",
            "Mid-Level Lapse Rates",
            "Low-Level Lapse Rates",
            "Max 2-6 km AGL Lapse Rate",
            "LCL Height",
            "LFC Height",
            "LCL-LFC Mean RH",
            "3-hour Surface-Based CAPE Change",
            "3-hour Surface-Based CIN Change",
            "3-hour 100mb Mixed-Layer CAPE Change",
            "3-hour Most-Unstable CAPE Change",
            "3-hour Low-Level LR Change",
            "6-hour Mid-Level LR Change",
            "Bulk Shear - Effective",
            "Bulk Shear - Sfc-6km",
            "Bulk Shear - Sfc-8km",
            "Bulk Shear - Sfc-1km",
            "BRN Shear",
            "SR Helicity - Effective",
            "SR Helicity - Sfc-3km",
            "SR Helicity - Sfc-1km",
            "SR Wind - Sfc-2km",
            "SR Wind - 4-6km",
            "SR Wind - 9-11km",
            "SR Wind - Anvil Level",
            "850-300mb Mean Wind",
            "850 and 500mb Winds",
            "3hr Sfc-3km SR Helicity Change",
            "3hr Sfc-1km Bulk Shear Change",
            "3hr Sfc-6km Bulk Shear Change",
            "Supercell Composite",
            "Supercell Composite (left-moving)",
            "Sgfnt Tornado (fixed layer)",
            "Sgfnt Tornado (effective layer)",
            "Cond. Prob. Sigtor (Eqn 1)",
            "Cond. Prob. Sigtor (Eqn 2)",
            "Non-Supercell Tornado",
            "Sgfnt Hail",
            "SARS Hail Size",
            "SARS Sig. Hail Percentage",
            "Large Hail Parameter",
            "Derecho Composite",
            "Craven/Brooks Sgfnt Severe",
            "Bulk Richardson Number",
            "MCS Maintenance",
            "Microburst Composite",
            "Enhanced Stretching Potential",
            "EHI - Sfc-1km",
            "EHI - Sfc-3km",
            "VGP - Sfc-3km",
            "Critical Angle",

            "100mb Mixed-Layer CAPE / Effective Bulk Shear",
            "Most-Unstable CAPE / Effective Bulk Shear",
            "Most-Unstable LI / 850 & 500mb Winds",
            "LCL Height / Sfc-1km SR Helicity",
            "Sfc-3km Lapse Rate / Sfc-3km MLCAPE",
            "Sfc Vorticity / Sfc-3km MLCAPE",
            "Sfc Dwpt / 700-500mb Lapse Rates",
            "Hail Parameters",
            "Lowest 3km max. Theta-e diff., MUCAPE, and 0-3km vector shear",
            "Lowest 3km max. Theta-e diff., MLCAPE, and 0-3km vector shear",

            "Precipitable Water",
            "Precipitable Water (w/850mb Moisture Transport Vector)",
            "850mb Moisture Transport",
            "Upwind Propagation Vector",
            "Precipitation Potential Placement",

            "Precipitation Type",
            "Near-Freezing Surface Temp.",
            "Surface Wet-Bulb Temp",
            "Freezing Level",
            "Critical Thickness",
            "800-750mb EPVg",
            "650-500mb EPVg",
            "Lake Effect Snow 1",
            "Lake Effect Snow 2",
            "Snow Squall Parameter",
            "Dendritic Growth Layer Depth",
            "Dendritic Growth Layer RH",
            "Dendritic Growth Layer Depth & RH",
            "Max Wet Bulb Temperature",

            "Sfc RH, Temp, Wind",
            "Fosberg Index",
            "Low Altitude Haines Index",
            "Mid Altitude Haines Index",
            "High Altitude Haines Index",
            "Lower Atmospheric Severity Index",

            "Total Totals",
            "K-Index",
            "Showalter Index",
            "SHERBE",
            "Modified SHERBE",
            "CWASP",
            "Enhanced EHI",
            "OPRH",
            "Prob EF0+ (conditional on RM supercell)",
            "Prob EF2+ (conditional on RM supercell)",
            "Prob EF4+ (conditional on RM supercell)",
            "Violent Tornado Parameter (VTP)"
    )
}


