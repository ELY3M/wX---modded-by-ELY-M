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

import android.graphics.Bitmap

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal object UtilitySpcSwo {

    fun getSwoStateUrl(state: String, day: String) = "${MyApplication.nwsSPCwebsitePrefix}/public/state/images/" + state + "_swody" + day + ".png"

    fun getImages(day: String, getAllImages: Boolean): List<Bitmap> {
        val imgUrls = mutableListOf<String>()
        if (day == "4-8" || day == "48" || day == "4") {
            (4..8).forEach { imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/exper/day4-8/day" + it.toString() + "prob.gif") }
            return imgUrls.map { it.getImage() }
        } else {
            val html = ("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day" + day + "otlk.html").getHtml()
            val time = html.parse("show_tab\\(.otlk_([0-9]{4}).\\)")
            when (day) {
                "1", "2" -> {
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}otlk_$time.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_torn.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_hail.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_wind.gif")
                }
                "3" -> {
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day3otlk_$time.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day3prob_$time.gif")
                }
                else -> {
                }
            }
            return if (getAllImages) imgUrls.map { it.getImage() } else listOf(imgUrls[0].getImage())
        }
    }

    fun getUrls(day: String): List<String> {
        val imgUrls = mutableListOf<String>()
        if (day == "4-8" || day == "48" || day == "4") {
            (4..8).forEach { imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/exper/day4-8/day" + it.toString() + "prob.gif") }
            return imgUrls
        } else {
            val html = ("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day" + day + "otlk.html").getHtml()
            val time = html.parse("show_tab\\(.otlk_([0-9]{4}).\\)")
            when (day) {
                "1", "2" -> {
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}otlk_$time.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_torn.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_hail.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_wind.gif")
                }
                "3" -> {
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day3otlk_$time.gif")
                    imgUrls.add("${MyApplication.nwsSPCwebsitePrefix}/products/outlook/day3prob_$time.gif")
                }
                else -> {
                }
            }
            return imgUrls
        }
    }
}


