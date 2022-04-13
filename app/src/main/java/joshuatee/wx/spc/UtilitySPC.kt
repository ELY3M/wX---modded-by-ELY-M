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

package joshuatee.wx.spc

import android.content.Context
import android.graphics.Bitmap

import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityVtec

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

object UtilitySpc {

    fun getStormReportsTodayUrl() = "${MyApplication.nwsSPCwebsitePrefix}/climo/reports/" + "today" + ".gif"

    internal val thunderStormOutlookImages: List<Bitmap>
        get() {
            val url = "${MyApplication.nwsSPCwebsitePrefix}/products/exper/enhtstm/"
            val html = url.getHtml()
            val dates = html.parseColumn("OnClick.\"show_tab\\(.([0-9]{4}).\\)\".*?")
            return dates.map { "${url}imgs/enh_$it.gif".getImage() }
        }

    internal val thunderStormOutlookUrls: List<String>
        get() {
            val url = "${MyApplication.nwsSPCwebsitePrefix}/products/exper/enhtstm/"
            val html = url.getHtml()
            val dates = html.parseColumn("OnClick.\"show_tab\\(.([0-9]{4}).\\)\".*?")
            return dates.map { "${url}imgs/enh_$it.gif" }
        }

    fun checkSpcDayX(context: Context, prod: String): List<String> {
        val highStr = "THERE IS A HIGH RISK OF"
        val moderateStr = "THERE IS A MODERATE RISK OF"
        val slightStr = "THERE IS A SLIGHT RISK"
        val enhStr = "THERE IS AN ENHANCED RISK OF"
        val marginalStr = "THERE IS A MARGINAL RISK OF"
        var returnStr = ""
        var html = UtilityDownload.getTextProduct(context, prod)
        if (html.contains(marginalStr)) returnStr = "marginal"
        if (html.contains(slightStr)) returnStr = "slight"
        if (html.contains(enhStr)) returnStr = "enh"
        if (html.contains(moderateStr)) returnStr = "modt"
        if (html.contains(highStr)) returnStr = "high"
        html = html.replace("ACUS[0-9]{2} KWNS [0-9]{6}".toRegex(), "")
                .replace("SWOD[Y4][1-3]".toRegex(), "")
                .replace("SPC AC [0-9]{6}".toRegex(), "")
                .replace("NWS STORM PREDICTION CENTER NORMAN OK", "")
                .replace("CONVECTIVE OUTLOOK", "")
        return listOf(returnStr, html)
    }

    fun checkSpc(): List<String> {
        val mcdNothingString = "<center>No Mesoscale Discussions are currently in effect."
        val watchNothingString = "<center><strong>No watches are currently valid"
        val mpdNothingString = "No MPDs are currently in effect."
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
            if (!MyApplication.severeDashboardMcd.value.contains(mcdNothingString)) {
                mdPresent = true
                val items = MyApplication.severeDashboardMcd.value.parseColumn(RegExp.mcdPatternUtilSpc)
                mdCount = items.size
                items.forEach { dashboardStrMcd += ":$it" }
            }
            if (!MyApplication.severeDashboardWat.value.contains(watchNothingString)) {
                watchPresent = true
                val items = MyApplication.severeDashboardWat.value.parseColumn(RegExp.watchPattern)
                watchCount = items.size
                items.forEach { dashboardStrWat += ":$it" }
            }
        }
        if (MyApplication.checkwpc) {
            if (!MyApplication.severeDashboardMpd.value.contains(mpdNothingString)) {
                mpdPresent = true
                val items = MyApplication.severeDashboardMpd.value.parseColumn(RegExp.mpdPattern)
                mpdCount = items.size
                items.forEach { dashboardStrMpd += ":$it" }
            }
        }
        var label = MyApplication.tabHeaders[1]
        val tabStrSpc: String
        if (watchPresent || mdPresent || mpdPresent) {
            if (watchPresent) label += " W($watchCount)"
            if (mdPresent) label += " M($mdCount)"
            if (mpdPresent) label += " P($mpdCount)"
            tabStrSpc = label
        } else {
            tabStrSpc = MyApplication.tabHeaders[1]
        }
        // US Warn
        var usWarnPresent = false
        var torCount = 0
        var tStormCount = 0
        var floodCount = 0
        if (MyApplication.checktor) {
            tStormCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTst.value)
            torCount = UtilityVtec.getStormCount(MyApplication.severeDashboardTor.value)
            floodCount = UtilityVtec.getStormCount(MyApplication.severeDashboardFfw.value)
            if (tStormCount > 0 || torCount > 0 || floodCount > 0) usWarnPresent = true
        }
        val tabStr = if (usWarnPresent) {
            MyApplication.tabHeaders[2] + " W(" + tStormCount + "," + torCount + "," + floodCount + ")"
        } else {
            MyApplication.tabHeaders[2]
        }
        return listOf(tabStrSpc, tabStr)
    }
}
