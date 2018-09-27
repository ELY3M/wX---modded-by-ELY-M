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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.MyApplication

import joshuatee.wx.TDWR_RIDS
import joshuatee.wx.util.Utility

object WXGLNexrad {

    private val COD_HASH = mapOf(
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
    // 94    .54   248 256  ( bins 460 , radials 360, scale factor 999 )
    // 99    .13   124 256  ( bins 1200, radials 360, scale factor 999 )
    // 134 DVL .54   248 256   ( bins 460, radials 360, scale factor 1 )
    // 135 EET .54  196  199 ( bins 346, radials 360, scale factor 1 )
    // 159 N0X .13 162 256 ( bins 1200, radials 360, scale factor 999 )
    // 161 N0C .13 162 256 ( bins 1200, radials 360, scale factor 999 )
    // 163 N0K .13 162 256 ( bins 1200, radials 360, scale factor 999 )

    fun getNumberRangeBins(prodId: Int): Short = when (prodId) {
        134 -> 460
        186 -> 1390
        182 -> 720
        135 -> 346
        99, 159, 161, 163, 170, 172 -> 1200
        else -> 460
    }

    private const val binSize54 = 2.0f
    private const val binSize13 = 0.50f
    private const val binSize08 = 0.295011f
    private const val binSize16 = 0.590022f

    fun getBinSize(prodId: Int) = when (prodId) {
        134, 135 -> binSize54
        186 -> binSize16
        159, 161, 163, 165, 99, 170, 172 -> binSize13
        182 -> binSize08
        153, 154 -> binSize13
        else -> binSize54
    }

    fun isRIDTDWR(rid: String) = TDWR_RIDS.any { rid == MyApplication.space.split(it)[0] }

    fun getTDWRFromRID(rid: String) = COD_HASH[rid] ?: ""

    fun savePrefs(context: Context, prefPrefix: String, oglr: WXGLRender) {
        Utility.writePref(context, prefPrefix + "_RID", oglr.rid)
        Utility.writePref(context, prefPrefix + "_PROD", oglr.product)
        Utility.writePref(context, prefPrefix + "_ZOOM", oglr.zoom)
        Utility.writePref(context, prefPrefix + "_X", oglr.x)
        Utility.writePref(context, prefPrefix + "_Y", oglr.y)
        MyApplication.wxoglRid = oglr.rid
        MyApplication.wxoglProd = oglr.product
        MyApplication.wxoglZoom = oglr.zoom
        MyApplication.wxoglX = oglr.x
        MyApplication.wxoglY = oglr.y
    }

    fun savePrefs(context: Context, prefPrefix: String, idx: Int, oglr: WXGLRender) {
        Utility.writePref(context, prefPrefix + "_RID" + idx.toString(), oglr.rid)
        Utility.writePref(context, prefPrefix + "_PROD" + idx.toString(), oglr.product)
        Utility.writePref(context, prefPrefix + "_ZOOM" + idx.toString(), oglr.zoom)
        Utility.writePref(context, prefPrefix + "_X" + idx.toString(), oglr.x)
        Utility.writePref(context, prefPrefix + "_Y" + idx.toString(), oglr.y)
    }
}
