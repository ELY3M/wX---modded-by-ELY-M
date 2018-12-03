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

package joshuatee.wx.wpc

import android.util.SparseArray

import joshuatee.wx.util.Group
import joshuatee.wx.ui.ObjectMenuTitle
import java.util.*

internal object UtilityWPCText {

    private val titles = Arrays.asList(
            ObjectMenuTitle("General", 3),
            ObjectMenuTitle("General Forecast Discussions", 8),
            ObjectMenuTitle("Precipitation Discussions", 3),
            ObjectMenuTitle("Hazards", 6),
            ObjectMenuTitle("Ocean Weather", 38),
            ObjectMenuTitle("Misc North American Weather", 5),
            ObjectMenuTitle("Misc Intl Weather", 4),
            ObjectMenuTitle("SPC", 8),
            ObjectMenuTitle("NHC", 6),
            ObjectMenuTitle("Great Lakes", 7),
            ObjectMenuTitle("Canada", 8),
            ObjectMenuTitle("Space Weather", 6)
    )

    val labels = listOf(
            "pmdspd: Short Range Forecast Discussion",
            "pmdepd: Extended Forecast Discussion",
            "pmdhmd: Model Diagnostics Discussion",

            "pmdhi: Hawaii Extended Forecast Discussion",
            "pmdak: Alaska Extended Forecast Discussion",
            "pmdsa: South American Synoptic Discussion",
            "pmdca: Tropical Discussion",
            "pmdmrd: Prognostic disc for 6-10 and 8-14 Day Outlooks",
            "pmd30d: Prognostic disc for Monthly Outlook",
            "pmd90d: Prognostic disc for long-lead Seasonal Outlooks",
            "pmdhco: Prognostic disc for long-lead Hawaiian Outlooks",

            "qpfpfd: QPF Discussion",
            "qpferd: Excessive Rainfall Discussion",
            "qpfhsd: Heavy Snow and Icing Discussion",

            "pmdthr: CPC US Hazards Outlook",
            "sccns1: Storm Summary 1",
            "sccns2: Storm Summary 2",
            "sccns3: Storm Summary 3",
            "sccns4: Storm Summary 4",
            "sccns5: Storm Summary 5",

            "miahsfat2: High Seas Forecasts - Atlantic",
            "miahsfep2: High Seas Forecasts - NE Pacific",
            "miahsfep3: High Seas Forecasts - SE Pacific",
            "nfdhsfat1: High Seas Forecasts - N Atlantic",
            "nfdhsfep1: High Seas Forecasts - N Pacific",
            "nfdhsfepi: High Seas Forecasts - E and C N Pacific",
            "mimpac: Marine Weather disc for N PAC Ocean",
            "offn09: Marine fsct for WA and ORE Waters",
            "offn08: Marine fcst for N CA Waters",
            "offn07: Marine fcst for S CA Waters",
            "offpz5: Offshore Waters fsct - PAC 1",
            "offpz6: Offshore Waters fsct - PAC 2",
            "nfdoffn35: (VOBRA) for Offshore Waters - WA/OR",
            "nfdoffn36: (VOBRA) for Offshore Waters - CA",
            "mimatn: Marine disc for N Atlantic Ocean",
            "offn01: Navtex Marine fcst for NE US Waters",
            "offn02: Navtex Marine fcst for Atlantic States Waters",
            "offn03: Navtex Marine fcst for SE US Waters",
            "offn04: Navtex Marine fcst for SE Gulf Of Mexico",
            "offn05: Navtex Marine fcst for San Jaun Atlantic Waters",
            "offn06: Navtex Marine fcst for NW Gulf Of Mexico",
            "offnt1: Offshore Waters fsct - ATL 1",
            "offnt2: Offshore Waters fsct - ATL 2",
            "offnt3: Offshore Waters fsct - Caribbean & SW North Atlantic",
            "offnt4: Offshore Waters fsct - Gulf of Mexico",
            "nfdoffn31: (VOBRA) for Offshore Waters - New England",
            "nfdoffn32: (VOBRA) for Offshore Waters - West Central North Atlantic",
            "offajk: Offshore Waters fsct - Eastern Gulf of Alaska",
            "offaer: Offshore Waters fsct - Western Gulf of Alaska",
            "offalu: Offshore Waters fsct - Bering Sea",
            "offafg: Offshore Waters fsct - US Artic Waters",
            "offhfo: Offshore Waters fsct - Hawaii",
            "offn10: Navtex Marine fcst for Hawaii",
            "offn11: Navtex Marine fcst for Kodiak, AK (SE)",
            "offn12: Navtex Marine fcst for Kodiak, AK (N Gulf)",
            "offn13: Navtex Marine fcst for Kodiak, AK (West)",
            "offn14: Navtex Marine fcst for Kodiak, AK (NW)",
            "offn15: Navtex Marine fcst for Kodiak, AK(Arctic)",

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
            "miatwoep: EPAC Tropical Weather Outlook",
            "miatwdep: EPAC Tropical Weather Discussion",
            "miatwsat: ATL Monthly Tropical Summary",
            "miatwsep: EPAC Monthly Tropical Summary",


            "GLFLM: Lake Michigan - Open Lake Forecast",
            "GLFLS: Lake Superior - Open Lake Forecast",
            "GLFLH: Lake Huron - Open Lake Forecast",
            "GLFSC: Lake St Clair - Open Lake Forecast",
            "GLFLE: Lake Erie - Open Lake Forecast",
            "GLFLO: Lake Ontario - Open Lake Forecast",
            "GLFSL: Saint Lawrence River",

            "focn45: Significant Weather Discussion, PASPC",
            "fxcn01: FXCN01",
            "awcn11: Weather Summary S. Manitoba",
            "awcn12: Weather Summary N. Manitoba",
            "awcn13: Weather Summary S. Saskatchewan",
            "awcn14: Weather Summary N. Saskatchewan",
            "awcn15: Weather Summary S. Alberta",
            "awcn16: Weather Summary N. Alberta",
            "swpc3day: NOAA Geomagnetic Activity Observation and Forecast",
            "swpc3daygeo: NOAA Geomagnetic Activity Probabilities",

            "swpchigh: Weekly Highlights and Forecasts",
            "swpc27day: 27-day Space Weather Outlook Table",
            "swpcdisc: Forecast Discussion",
            "swpcwwa: Advisory Outlook"
    )

    val groups = SparseArray<Group>()
    var shortCodes = Array(12) { Array(39) { "" } }
    var longCodes = Array(12) { Array(39) { "" } }

    internal fun createData() {
        var k = 0
        titles.indices.forEach { index ->
            val group = Group(titles[index].title)
            var m = 0
            for (j in (ObjectMenuTitle.getStart(titles, index) until titles[index].count + ObjectMenuTitle.getStart(titles, index))) {
                group.children.add(labels[j])
                shortCodes[index][m] = labels[k].split(":")[0]
                longCodes[index][m] = labels[k]
                k += 1
                m += 1
            }
            groups.append(index, group)
        }
    }
}

