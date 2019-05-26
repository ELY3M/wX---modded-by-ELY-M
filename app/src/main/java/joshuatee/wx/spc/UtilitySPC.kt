/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

import android.content.Context
import android.graphics.Bitmap

import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityVtec

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

object UtilitySpc {

    fun getStormReportsTodayUrl(): String =
            "${MyApplication.nwsSPCwebsitePrefix}/climo/reports/" + "today" + ".gif"

    internal val thunderStormOutlookImages: List<Bitmap>
        get() {
            val url = "${MyApplication.nwsSPCwebsitePrefix}/products/exper/enhtstm"
            val html = url.getHtml()
            val tstArr = html.parseColumn("OnClick.\"show_tab\\(.([0-9]{4}).\\)\".*?")
            return tstArr.map { "$url/imgs/enh_$it.gif".getImage() }
        }

    fun checkSpcDayX(context: Context, prod: String): List<String> {
        val highStr = "THERE IS A HIGH RISK OF"
        val modtStr = "THERE IS A MODERATE RISK OF"
        val slightStr = "THERE IS A SLIGHT RISK"
        val enhStr = "THERE IS AN ENHANCED RISK OF"
        val mrglStr = "THERE IS A MARGINAL RISK OF"
        var returnStr = ""
        var html = UtilityDownload.getTextProduct(context, prod)
        if (html.contains(mrglStr)) {
            returnStr = "marginal"
        }
        if (html.contains(slightStr)) {
            returnStr = "slight"
        }
        if (html.contains(enhStr)) {
            returnStr = "enh"
        }
        if (html.contains(modtStr)) {
            returnStr = "modt"
        }
        if (html.contains(highStr)) {
            returnStr = "high"
        }
        html = html.replace("ACUS[0-9]{2} KWNS [0-9]{6}".toRegex(), "")
                .replace("SWOD[Y4][1-3]".toRegex(), "")
                .replace("SPC AC [0-9]{6}".toRegex(), "")
                .replace("NWS STORM PREDICTION CENTER NORMAN OK", "")
                .replace("CONVECTIVE OUTLOOK", "")
        return listOf(returnStr, html)
    }

    fun checkSpc(context: Context): List<String> {
        var tabStr = ""
        val tabStrSpc: String
        var mdPresent = false
        var watchPresent = false
        var mpdPresent = false
        var mdCount = 0
        var watchCount = 0
        var mpdCount = 0
        var dashboardStrWat = ""
        var dashboardStrMpd = ""
        var dashboardStrMcd = ""
        if (MyApplication.checkspc) {
            if (!MyApplication.severeDashboardMcd.valueGet().contains(MyApplication.MD_COMP)) {
                mdPresent = true
                val al = MyApplication.severeDashboardMcd.valueGet()
                        .parseColumn(RegExp.mcdPatternUtilspc)
                mdCount = al.size
                al.forEach { dashboardStrMcd += ":$it" }
            }
            if (!MyApplication.severeDashboardWat.valueGet().contains(MyApplication.WATCH_COMP)) {
                watchPresent = true
                val al =
                        MyApplication.severeDashboardWat.valueGet().parseColumn(RegExp.watchPattern)
                watchCount = al.size
                al.forEach { dashboardStrWat += ":$it" }
            }
        }
        if (MyApplication.checkwpc) {
            if (!MyApplication.severeDashboardMpd.valueGet().contains(MyApplication.MPD_COMP)) {
                mpdPresent = true
                val al = MyApplication.severeDashboardMpd.valueGet().parseColumn(RegExp.mpdPattern)
                mpdCount = al.size
                al.forEach { dashboardStrMpd += ":$it" }
            }
        }
        var label = MyApplication.tabHeaders[1]
        if (watchPresent || mdPresent || mpdPresent) {
            if (watchPresent)
                label = "$label W($watchCount)"
            if (mdPresent)
                label = "$label M($mdCount)"
            if (mpdPresent)
                label = "$label P($mpdCount)"
            tabStrSpc = label
        } else {
            tabStrSpc = MyApplication.tabHeaders[1]
        }
        // US Warn
        var uswarnPresent = false
        var torCount = 0
        var tstormCount = 0
        var floodCount = 0
        if (MyApplication.checktor) {
            tstormCount = UtilityVtec.getStormCount(context, MyApplication.severeDashboardTst.valueGet())
            torCount = UtilityVtec.getStormCount(context, MyApplication.severeDashboardTor.valueGet())
            floodCount = UtilityVtec.getStormCount(context, MyApplication.severeDashboardFfw.valueGet())
            if (tstormCount > 0 || torCount > 0 || floodCount > 0) {
                uswarnPresent = true
            }
        }
        tabStr = if (uswarnPresent)
            tabStr + "  " + MyApplication.tabHeaders[2] + " W(" + tstormCount + "," + torCount + "," + floodCount + ")"
        else
            MyApplication.tabHeaders[2]
        return listOf(tabStrSpc, tabStr)
    }
}


