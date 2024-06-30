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

package joshuatee.wx.wpc

import android.util.SparseArray
import joshuatee.wx.safeGet
import joshuatee.wx.util.Group
import joshuatee.wx.ui.MenuTitle

@Suppress("SpellCheckingInspection")
internal object UtilityWpcText {

    fun needsFixedWidthFont(product: String): Boolean {
        val productList = listOf("RWRMX", "UVICAC", "MIATWSEP", "MIATWSAT")
        return product.startsWith("TPT")
                || product.startsWith("SWPC")
                || product.startsWith("MIAPW")
                || product.startsWith("MIATCM")
                || productList.contains(product)
    }

    fun getLabel(token: String): String {
        val list = labelsWithCodes.filter { it.startsWith(token) }
        return if (list.isNotEmpty()) {
            list[0].split(":").safeGet(1)
        } else {
            ""
        }
    }

    private val titles = listOf(
        MenuTitle("General Forecast Discussions", 9),
        MenuTitle("Precipitation Discussions", 2),
        MenuTitle("Hazards", 7),
        MenuTitle("Ocean Weather", 34),
        MenuTitle("Misc North American Weather", 5),
        MenuTitle("Misc Intl Weather", 4),
        MenuTitle("SPC", 8),
        MenuTitle("NHC", 7),
        MenuTitle("Great Lakes", 7),
        MenuTitle("Space Weather", 6),
        MenuTitle("Canada", 4)
    )

    val labelsWithCodes = listOf(
        "pmdspd: Short Range Forecast Discussion",
        "pmdepd: Extended Forecast Discussion",

        "pmdhi: Hawaii Extended Forecast Discussion",
        "pmdak: Alaska Extended Forecast Discussion",
        "pmdca: Tropical Discussion",
        "pmdmrd: Prognostic disc for 6-10 and 8-14 Day Outlooks",
        "pmd30d: Prognostic disc for Monthly Outlook",
        "pmd90d: Prognostic disc for long-lead Seasonal Outlooks",
        "pmdhco: Prognostic disc for long-lead Hawaiian Outlooks",

        "qpferd: Excessive Rainfall Discussion",
        "qpfhsd: Heavy Snow and Icing Discussion",

        "sccns1: Storm Summary 1",
        "sccns2: Storm Summary 2",
        "sccns3: Storm Summary 3",
        "sccns4: Storm Summary 4",
        "sccns5: Storm Summary 5",
        "pmdthr: CPC US Hazards Outlook Days 8-14",
        "ushzd37: CPC US Hazards Outlook Days 3-7",

        "miahsfat2: High Seas Forecasts - Atlantic",
        "miahsfep2: High Seas Forecasts - NE Pacific",
        "hsfsp: High Seas Forecasts - SE Pacific",
        "nfdhsfat1: High Seas Forecasts - N Atlantic",
        "nfdhsfep1: High Seas Forecasts - N Pacific",
        "nfdhsfepi: High Seas Forecasts - E and C N Pacific",
        "offn09: Marine fsct for WA and ORE Waters",
        "offn08: Marine fcst for N CA Waters",
        "offn07: Marine fcst for S CA Waters",
        "offpz5: Offshore Waters fsct - PAC 1",
        "offpz6: Offshore Waters fsct - PAC 2",
        "nfdoffn35: (VOBRA) for Offshore Waters - WA/OR",
        "nfdoffn36: (VOBRA) for Offshore Waters - CA",
        "offn01: Navtex Marine fcst for NE US Waters",
        "offn02: Navtex Marine fcst for Atlantic States Waters",
        "offn03: Navtex Marine fcst for SE US Waters",
        "offn04: Navtex Marine fcst for SE Gulf Of Mexico",
        "offn05: Navtex Marine fcst for San Jaun Atlantic Waters",
        "offn06: Navtex Marine fcst for NW Gulf Of Mexico",
        "offnt1: Offshore Waters fsct - ATL 1",
        "offnt2: Offshore Waters fsct - ATL 2",
        "offnt3: Offshore Waters fsct - SW and Tropical N Atlantic and Caribbean Sea",
        "offnt4: Offshore Waters fsct - Gulf of Mexico",
        "offnt5: Offshore Waters fcst - SW N Atlantic including the Bahamas",
        "nfdoffn31: (VOBRA) for Offshore Waters - New England",
        "nfdoffn32: (VOBRA) for Offshore Waters - West Central North Atlantic",
        "offajk: Offshore Waters fsct - Eastern Gulf of Alaska",
        "offaer: Offshore Waters fsct - Western Gulf of Alaska",
        "offalu: Offshore Waters fsct - Bering Sea",
        "offafg: Offshore Waters fsct - US Artic Waters",
        "offhfo: Offshore Waters fsct - Hawaii",
        "offn10: Navtex Marine fcst for Hawaii",

        "offn14: Navtex Marine fcst for Kodiak, AK (NW)",
        "offn15: Navtex Marine fcst for Kodiak, AK (Arctic)",

        "uvicac: NOAA/EPA Ultraviolet Index /UVI/ Forecast",
        "tptwrn: Hourly temp/wx for Western US",
        "tptcrn: Hourly temp/wx for Central US",
        "tptern: Hourly temp/wx for Eastern US",
        "tptnam: Hourly temp/wx for NA Cities",

        "rwrmx: Latin America and Caribbean Regional Weather Roundup",
        "tptcan: Canadian temp and precip Table",
        "tptint: Foreign temp and weather table",
        "tptlat: Latin American temp and weather table",

        "swomcd: Most recent MCD",
        "swody1: Day 1 Convective Outlook",
        "swody2: Day 2 Convective Outlook",
        "swody3: Day 3 Convective Outlook",
        "swod48: Day 4-8 Convective Outlook",
        "fwddy1: Day 1 Fire Weather Outlook",
        "fwddy2: Day 2 Fire Weather Outlook",
        "fwddy38: Days 3-8 Fire Weather Outlook",

        "miatwoat: ATL Tropical Weather Outlook",
        "miatwdat: ATL Tropical Weather Discussion",
        "miatwsat: ATL Monthly Tropical Summary",
        "miatwoep: EPAC Tropical Weather Outlook",
        "miatwdep: EPAC Tropical Weather Discussion",
        "miatwsep: EPAC Monthly Tropical Summary",
        "hfotwocp: CPAC Tropical Weather Outlook",

        "GLFLM: Lake Michigan - Open Lake Forecast",
        "GLFLS: Lake Superior - Open Lake Forecast",
        "GLFLH: Lake Huron - Open Lake Forecast",
        "GLFSC: Lake St Clair - Open Lake Forecast",
        "GLFLE: Lake Erie - Open Lake Forecast",
        "GLFLO: Lake Ontario - Open Lake Forecast",
        "GLFSL: Saint Lawrence River",

        "swpc3day: NOAA Geomagnetic Activity Observation and Forecast",
        "swpc3daygeo: NOAA Geomagnetic Activity Probabilities",
        "swpchigh: Weekly Highlights and Forecasts",
        "swpc27day: 27-day Space Weather Outlook Table",
        "swpcdisc: Forecast Discussion",
        "swpcwwa: Advisory Outlook",

        "focn45: Significant Weather Discussion, PASPC",
        "awcn11: Weather Summary Manitoba",
        "awcn13: Weather Summary Saskatchewan",
        "awcn15: Weather Summary Alberta"
    )

    val groups = SparseArray<Group>()
    var shortCodes = Array(13) { Array(39) { "" } }
    var longCodes = Array(13) { Array(39) { "" } }

    internal fun create() {
        var k = 0
        titles.indices.forEach { index ->
            val group = Group(titles[index].title)
            var m = 0
            for (j in (MenuTitle.getStart(
                titles,
                index
            ) until titles[index].count + MenuTitle.getStart(titles, index))) {
                group.children.add(labelsWithCodes[j])
                shortCodes[index][m] = labelsWithCodes[k].split(":")[0]
                longCodes[index][m] = labelsWithCodes[k]
                k += 1
                m += 1
            }
            groups.append(index, group)
        }
    }
}
