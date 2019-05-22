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

package joshuatee.wx.nhc

import android.graphics.Bitmap

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp

object UtilityNhc {

    const val widgetImageUrlTop = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_0d0.png"
    const val widgetImageUrlBottom = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_0d0.png"

    fun getHurricaneInfo(rssUrl: String): ObjectNhcStormInfo {
        var title = ""
        var summary = ""
        var url = ""
        var img1 = ""
        var img2 = ""
        var wallet = ""
        val urlList: List<String>
        val html = rssUrl.getHtml()
        if (!html.contains("No current storm in")) {
            title = html.parse(RegExp.utilNhcPattern1)
            summary = html.parse(RegExp.utilNhcPattern2)
            url = html.parse(RegExp.utilNhcPattern3)
            summary = summary.replace("</.*?>".toRegex(), "<br>")
            wallet = html.parse(RegExp.utilNhcPattern4)
            urlList = html.parseColumn(RegExp.utilNhcPattern5)
            if (urlList.size > 1) {
                img1 = urlList[0]
                img2 = urlList[1]
            }
        }
        return ObjectNhcStormInfo(title, summary, url, img1, img2, wallet)
    }

    fun getImage(rid: String, prod: String): Bitmap =
        ("http://www.ssd.noaa.gov/PS/TROP/floaters/" + rid + "/imagery/" + prod + "0.gif").getImage()
}




