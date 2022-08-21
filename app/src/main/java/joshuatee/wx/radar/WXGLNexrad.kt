/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.R
import joshuatee.wx.common.RegExp
import joshuatee.wx.util.Utility

object WXGLNexrad {

    val tdwrProductList = listOf(
        "TZ0",
        "TZ1",
        "TZ2",
        "TR0",
        "TR1",
        "TR2",
        "TV0",
        "TV1",
        "TV2",
        "TZL",
        "N1P",
        "NTP"
    )

    fun isProductTdwr(product: String) = product.startsWith("TV") || product == "TZL" || product.startsWith("TZ")

    fun isTdwr(product: String) = product in tdwrProductList

    // next 3 maps are for color palette editor : Map<String, String>
    val productCodeStringToName = mapOf(
        94 to "Reflectivity",
        99 to "Velocity",
        134 to "Digital Vertical Integrated Liquid",
        135 to "Enhanced Echo Tops",
        159 to "Differential Reflectivity",
        161 to "Correlation Coefficient",
        163 to "Specific Differential Phase",
        172 to "Digital Storm Total Precipitation"
    )

    val productCodeStringToCode = mapOf(
        94 to "N0Q",
        99 to "N0U",
        134 to "DVL",
        135 to "EET",
        159 to "N0X",
        161 to "N0C",
        163 to "N0K",
        172 to "DSP"
    )

    val productCodeStringToResourceFile = mapOf(
        94 to R.raw.dvn94,
        99 to R.raw.dvn99,
        134 to R.raw.gsp134,
        135 to R.raw.vax135,
        159 to R.raw.vax159,
        161 to R.raw.vax161,
        163 to R.raw.vax163,
        172 to R.raw.vax172
    )

    val colorPaletteProducts = listOf(
        94,
        99,
        134,
        135,
        159,
        161,
        163,
        165,
        172
    )

    private val closestTdwrToNexrad = mapOf(
        "DTX" to "DTW",
        "LOT" to "ORD",
        "MKX" to "MKE",
        "MPX" to "MSP",
        "FTG" to "DEN",
        "BOX" to "BOS",
        "CLE" to "LVE",
        "EAX" to "MCI",
        "FFC" to "ATL",
        "FWS" to "DFW",
        "GSP" to "CLT",
        "HGX" to "HOU",
        "IND" to "IDS",
        "LIX" to "MSY",
        "LVX" to "SDF",
        "LSX" to "STL",
        "NQA" to "MEM",
        "AMX" to "MIA",
        "OHX" to "BNA",
        "OKX" to "JFK",
        "TLX" to "OKC",
        "PBZ" to "PIT",
        "DIX" to "PHL",
        "IWA" to "PHX",
        "RAX" to "RDU",
        "MTX" to "SLC",
        "TBW" to "TPA",
        "INX" to "TUL",
        "ESX" to "LAS",
        "TBW" to "TPA",
        "JUA" to "SJU",
        "LWX" to "DCA",
        "ILN" to "CMH",
        "MLB" to "MCO",
        "ICT" to "ICT",
        "CMH" to "CMH",
        "CVG" to "CVG",
        "DAL" to "DAL",
        "DAY" to "DAY",
        "EWR" to "EWR",
        "FLL" to "FLL",
        "IAD" to "IAD",
        "IAH" to "IAH",
        "MDW" to "MDW",
        "PBI" to "PBI"
    )

    // 19    .54   124 16

    // DS.p28sw NSP .13x1 32/60 8 0.5° Base spectrum Width
    // DS.p30sw NSW .54x1 124/230 8 0.5° Base spectrum Width
    // 56  srm is same specs as 30 minus 16 bit color
    // 94    .54   248 256  ( bins 460 , radials 360, scale factor 999 )
    // 99    .13   124 256  ( bins 1200, radials 360, scale factor 999 )
    // 134 DVL .54   248 256   ( bins 460, radials 360, scale factor 1 )
    // 135 EET .54  196  199 ( bins 346, radials 360, scale factor 1 )
    // 159 N0X .13 162 256 ( bins 1200, radials 360, scale factor 999 )
    // 161 N0C .13 162 256 ( bins 1200, radials 360, scale factor 999 )
    // 163 N0K .13 162 256 ( bins 1200, radials 360, scale factor 999 )

    // 32 TDWR: DHR - DS.32dhr (wsr-88d also) range 124, colors 256, .54 x 1 nmi x degree
    // 78 TDWR and NEXRAD: N1P DS.78ohp One Hour Precipitation Total range 124, colors 16, 1.1 x 1 nmi x degree
    // 80 TDWR and NEXRAD: NTP DS.80stp Storm Total Precipitation range 124, colors 16, 1.1 x 1 nmi x degree
    // 138 TDWR and NEXRAD: DSP DS.138dp Digital Storm Total Precipitation range 124, colors 256, 1.1 x 1 nmi x degree

    fun getNumberRangeBins(prodId: Int): Short = when (prodId) {
        78, 80 -> 115
        134 -> 460
        186 -> 1390
        180, 181, 182, 2153, 2154 -> 720
        135 -> 346
        99, 159, 161, 163, 170, 172 -> 1200
        else -> 460
    }

    const val radarLocationUpdateDistanceInMeters = 30.0f
    private const val binSize54 = 2.0f
    private const val binSize13 = 0.50f
    private const val binSize08 = 0.295011f
    private const val binSize16 = 0.590022f
    private const val binSize110 = 2.0f * binSize54

    fun getBinSize(prodId: Int) = when (prodId) {
        134, 135 -> binSize54
        186 -> binSize16
        159, 161, 163, 165, 99, 170, 172 -> binSize13
        180, 181, 182 -> binSize08
        78, 80 -> binSize110
        153, 154, 2153, 2154 -> binSize13
        else -> binSize54
    }

    // FIXME use different split
    fun isRidTdwr(rid: String) = GlobalArrays.tdwrRadars.any { rid == RegExp.space.split(it)[0] }

    fun getTdwrFromRid(rid: String) = closestTdwrToNexrad[rid] ?: ""

    fun getRadarInfo(context: Context, pane: String) = Utility.readPref(context, "WX_RADAR_CURRENT_INFO$pane", "")

    // for Location Fragment as there seem to be quite a few
    // at joshuatee.wx.fragments.LocationFragment.getActivityReference (LocationFragment.kt:833)
    // kotlin.KotlinNullPointerException:
    fun getRadarInfo(pane: String) = Utility.readPref("WX_RADAR_CURRENT_INFO$pane", "")

    fun writeRadarInfo(context: Context, pane: String, info: String) {
        Utility.writePref(context, "WX_RADAR_CURRENT_INFO$pane", info)
    }

    fun writeRadarTimeForWidget(context: Context, time: String) {
        Utility.writePref(context, "WX_RADAR_CURRENT_INFO_WIDGET_TIME", time)
    }

    fun readRadarTimeForWidget(context: Context) = Utility.readPref(context, "WX_RADAR_CURRENT_INFO_WIDGET_TIME", "")
}
