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

package joshuatee.wx.spc

import android.util.SparseArray
import joshuatee.wx.safeGet
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.MenuTitle
import joshuatee.wx.util.Group

@Suppress("SpellCheckingInspection")
object UtilitySpcMeso {

    private val titles = listOf(
        MenuTitle("Observations", 3),
        MenuTitle("Surface", 15),
        MenuTitle("Upper Air", 25),
        MenuTitle("Thermodynamics", 20),
        MenuTitle("Wind Shear", 20),
        MenuTitle("Composite Indices", 23),
        MenuTitle("Multi-Parameter Fields", 11),
        MenuTitle("Heavy Rain", 8),
        MenuTitle("Winter Weather", 15),
        MenuTitle("Fire Weather", 6),
        MenuTitle("Classic", 3),
        MenuTitle("Beta", 10)
    )

    fun getLabelFromParam(param: String): String {
        val index = params.indexOf(param)
        return if (index == -1) {
            ""
        } else {
            labels[index]
        }
    }

    internal fun moveForward(list: List<String>): Int {
        val param = list.safeGet(0)
        if (list.size > 3) {
            var swipePosition = list.lastIndexOf(param)
            swipePosition += 1
            if (swipePosition == 1) {
                return 3
            }
            return if (list.size > swipePosition) {
                swipePosition
            } else {
                3
            }
        } else {
            return 0
        }
    }

    internal fun moveBack(list: List<String>): Int {
        val param = list.safeGet(0)
        return if (list.size > 3) {
            var swipePosition = list.lastIndexOf(param)
            swipePosition -= 1
            if (swipePosition > 2) {
                swipePosition
            } else {
                list.size - 1
            }
        } else {
            0
        }
    }

    const val DEFAULT_SECTOR = "19"

    val sectorMap = mapOf(
        "19" to "US",
        "20" to "MW",
        "13" to "NC",
        "14" to "C",
        "15" to "SC",
        "16" to "NE",
        "17" to "CE",
        "18" to "SE",
        "12" to "SW",
        "11" to "NW",
        "21" to "GL",
        "22" to "GB",
    )

    internal val imgSf = listOf(
        "mixr",
        "ttd",
        "mcon",
        "thea",
        "mxth",
        "925mb",
        "850mb",
        "850mb2",
        "700mb",
        "500mb",
        "300mb",
        "sbcp",
        "mlcp",
        "mucp",
        "muli",
        "laps",
        "lllr",
        "lclh",
        "lfch",
        "lfrh",
        "effh",
        "stor",
        "stpc",
        "cpsh",
        "comp",
        "lcls",
        "lr3c",
        "tdlr",
        "qlcs1",
        "qlcs2",
        "pwtr",
        "tran",
        "tran_925",
        "tran_925-850",
        "prop",
        "peff",
        "fzlv",
        "les1",
        "tadv_925"
    )

    internal fun setParamFromFav(token: String): String {
        val tmpArr = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
            .dropLastWhile { it.isEmpty() }
        return when (token) {
            "SPCMESO1" -> if (tmpArr.size > 3) tmpArr[3] else "500mb"
            "SPCMESO2" -> if (tmpArr.size > 4) tmpArr[4] else "pmsl"
            "SPCMESO3" -> if (tmpArr.size > 5) tmpArr[5] else "ttd"
            "SPCMESO4" -> if (tmpArr.size > 6) tmpArr[6] else "rgnlrad"
            "SPCMESO5" -> if (tmpArr.size > 7) tmpArr[7] else "lllr"
            "SPCMESO6" -> if (tmpArr.size > 8) tmpArr[8] else "laps"
            else -> ""
        }
    }

    var shortCodes: Array<Array<String>> = Array(12) { Array(25) { "" } }
    var longCodes: Array<Array<String>> = Array(12) { Array(25) { "" } }
    internal val groups = SparseArray<Group>()

    internal fun create() {
        var k = 0
        titles.indices.forEach { index ->
            val group = Group(titles[index].title)
            var m = 0
            for (j in (MenuTitle.getStart(
                titles,
                index
            ) until titles[index].count + MenuTitle.getStart(titles, index))) {
                group.children.add(labels[j])
                shortCodes[index][m] = params[k]
                longCodes[index][m] = labels[k]
                k += 1
                m += 1
            }
            groups.append(index, group)
        }
    }

    internal val params = listOf(
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
        "850mb2",
        "700mb",
        "500mb",
        "300mb",
        "dlcp",
        "tadv_925",
        "tadv",
        "7tad",
        "sfnt",
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
        "skewt",

        "eshr",
        "shr6",
        "shr8",
        "shr3",
        "shr1",
        "brns",
        "effh",
        "srh3",
        "srh1",
        "srh5",
        "llsr",
        "mlsr",
        "ulsr",
        "alsr",
        "mnwd",
        "xover",
        "srh3_chg",
        "shr1_chg",
        "shr6_chg",
        "hodo",

        "scp",
        "lscp",
        "stor",
        "stpc",
        "stpc5",
        "sigt1",
        "sigt2",
        "nstp",
        "vtp3",
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
        "3cape_shr3",
        "3cvr",
        "tdlr",
        "hail",
        "qlcs1",
        "qlcs2",

        "pwtr",
        "pwtr2",
        "tran",
        "tran_925",
        "tran_925-850",
        "prop",
        "peff",
        "mixr",

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
        "skewt-winter",

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
        "tehi",
        "tts",
        "oprh",
        "ptstpe",
        "pstpe",
        "pvstpe",
        "pw3k"
    )

    internal val labels = listOf(
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
        "850mb Analysis (version 2)",
        "700mb Analysis",
        "500mb Analysis",
        "300mb Analysis",
        "Deep Moisture Convergence",
        "925mb Temp Advection",
        "850mb Temp Advection",
        "700mb Temp Advection",
        "Sfc Frontogenesis",
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
        "Skew-T Maps",

        "Bulk Shear - Effective",
        "Bulk Shear - Sfc-6km",
        "Bulk Shear - Sfc-8km",
        "Bulk Shear - Sfc-3km",
        "Bulk Shear - Sfc-1km",
        "BRN Shear",
        "SR Helicity - Effective",
        "SR Helicity - Sfc-3km",
        "SR Helicity - Sfc-1km",
        "SR Helicity - Sfc-500m",
        "SR Wind - Sfc-2km",
        "SR Wind - 4-6km",
        "SR Wind - 9-11km",
        "SR Wind - Anvil Level",
        "850-300mb Mean Wind",
        "850 and 500mb Winds",
        "3hr Sfc-3km SR Helicity Change",
        "3hr Sfc-1km Bulk Shear Change",
        "3hr Sfc-6km Bulk Shear Change",
        "Hodograph Map",

        "Supercell Composite",
        "Supercell Composite (left-moving)",
        "Sgfnt Tornado (fixed layer)",
        "Sgfnt Tornado (effective layer)",
        "Sgfnt Tornado (0-500m SRH)",
        "Cond. Prob. Sigtor (Eqn 1)",
        "Cond. Prob. Sigtor (Eqn 2)",
        "Non-Supercell Tornado",
        "Violent Tornado Parameter (VTP)",
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
        "Bulk Shear - Sfc-3km / Sfc-3km MLCAPE",
        "Sfc Vorticity / Sfc-3km MLCAPE",
        "Sfc Dwpt / 700-500mb Lapse Rates",
        "Hail Parameters",
        "Lowest 3km max. Theta-e diff., MUCAPE, and 0-3km vector shear",
        "Lowest 3km max. Theta-e diff., MLCAPE, and 0-3km vector shear",

        "Precipitable Water",
        "Precipitable Water (w/850mb Moisture Transport Vector)",
        "850mb Moisture Transport",
        "925mb Moisture Transport",
        "925-850mb Moisture Transport",
        "Upwind Propagation Vector",
        "Precipitation Potential Placement",
        "100mb Mean Mixing Ratio",

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
        "Winter Skew-T Maps",

        "Sfc RH / Temp / Wind",
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
        "Tornadic 0-1km EHI",
        "Tornadic Tilting & Stretching",
        "OPRH",
        "Prob EF0+ (conditional on RM supercell)",
        "Prob EF2+ (conditional on RM supercell)",
        "Prob EF4+ (conditional on RM supercell)",
        "PW * 3kmRH"
    )

    private val sectorToLatLon = mapOf(
        "11" to LatLon(44.56, -112.65),
        "12" to LatLon(35.75, -112.48),
        "13" to LatLon(44.65, -96.48),
        "14" to LatLon(37.61, -96.26),
        "15" to LatLon(31.80, -96.66),
        "16" to LatLon(43.71, -77.06),
        "17" to LatLon(36.88, -81.85),
        "18" to LatLon(30.98, -85.63),
        "20" to LatLon(39.01, -91.48),
        "21" to LatLon(44.02, -85.94),
        "22" to LatLon(40.95, -110.63),
    )

//    val sectorMapLong = mapOf(
//        "19" to "CONUS",
//        "20" to "Midwest",
//        "21" to "Great Lakes",
//        "22" to "Intermountain West",
//        "13" to "North Central",
//        "14" to "Central",
//        "15" to "South Central",
//        "16" to "Northeast",
//        "17" to "Central East",
//        "18" to "Southeast",
//        "12" to "Southwest",
//        "11" to "Northwest"
//    )

    fun getNearest(latLon: LatLon) = UtilityLocation.getNearest(latLon, sectorToLatLon)
}
