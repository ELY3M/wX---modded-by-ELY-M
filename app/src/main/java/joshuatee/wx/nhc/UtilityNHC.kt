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

package joshuatee.wx.nhc

import android.content.Context
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

object UtilityNHC {

    fun getHurricaneInfo(rssUrl: String): ObjectNHCStormInfo {
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
        return ObjectNHCStormInfo(title, summary, url, img1, img2, wallet)
    }

    fun getAnim(context: Context, rid: String, prodId: String, frameCntStr: String): AnimationDrawable {
        val baseUrl = "http://www.ssd.noaa.gov/PS/TROP/floaters/$rid/imagery/"
        val urlArr = UtilityImgAnim.getURLArray(baseUrl, "<a href=\"([0-9]{8}_[0-9]{4}Z-$prodId\\.gif)\">", frameCntStr)
        val bmAl = urlArr.mapTo(mutableListOf()) { (baseUrl + it).getImage() }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl, UtilityImg.animInterval(context))
    }

    fun getImage(rid: String, prod: String) = ("http://www.ssd.noaa.gov/PS/TROP/floaters/" + rid + "/imagery/" + prod + "0.gif").getImage()
}




