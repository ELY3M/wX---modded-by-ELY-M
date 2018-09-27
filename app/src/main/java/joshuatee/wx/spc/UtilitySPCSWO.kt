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

import android.graphics.Bitmap

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal object UtilitySPCSWO {

    fun getSWOStateURL(state: String, day: String): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/public/state/images/" + state + "_swody" + day + ".png"
    }

    fun getImageURLs(day: String, getAllImages: Boolean): List<Bitmap> {
        val imgURLs = mutableListOf<String>()
        val bitmapArr = mutableListOf<Bitmap>()
        if (day == "4-8" || day == "48" || day == "4") {
            (4..8).forEach { imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/exper/day4-8/day" + it.toString() + "prob.gif") }
            imgURLs.mapTo(bitmapArr) { it.getImage() }
            return bitmapArr
        }
        val html = ("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day" + day + "otlk.html").getHtml()
        val time = html.parse("show_tab\\(.otlk_([0-9]{4}).\\)")
        when (day) {
            "1" -> {
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day1otlk_$time.gif")
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day1probotlk_" + time + "_torn.gif")
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day1probotlk_" + time + "_hail.gif")
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day1probotlk_" + time + "_wind.gif")
            }
            "2" -> {
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day2otlk_$time.gif")
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day2probotlk_" + time + "_any.gif")
            }
            "3" -> {
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day3otlk_$time.gif")
                imgURLs.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day3prob_$time.gif")
            }
            else -> {
            }
        }
        if (getAllImages) {
            imgURLs.mapTo(bitmapArr) { it.getImage() }
        } else {
            bitmapArr.add(imgURLs[0].getImage())
        }
        return bitmapArr
    }
}


