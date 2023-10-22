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

package joshuatee.wx.spc

import android.graphics.Bitmap
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.parse

internal object UtilitySpcSwo {

    // https://www.spc.noaa.gov/partners/outlooks/state/images/MS_swody1_TORN.png
    fun getSwoStateUrl(state: String, day: String): List<String> = when (day) {
        "1", "2" -> listOf(
                // "${GlobalVariables.nwsSPCwebsitePrefix}/public/state/images/" + state + "_swody" + day + ".png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + day + ".png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + day + "_TORN.png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + day + "_HAIL.png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + day + "_WIND.png",
        )

        "3" -> listOf(
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + day + ".png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + day + "_PROB.png",
        )

        "4-8" -> listOf(
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + "4" + "_PROB.png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + "5" + "_PROB.png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + "6" + "_PROB.png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + "7" + "_PROB.png",
                "${GlobalVariables.nwsSPCwebsitePrefix}/partners/outlooks/state/images/" + state + "_swody" + "8" + "_PROB.png",
        )

        else -> listOf()
    }

    fun getImages(day: String, getAllImages: Boolean): List<Bitmap> {
        val imgUrls = mutableListOf<String>()
        if (day == "4-8" || day == "48" || day == "4") {
            (4..8).forEach {
                imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/exper/day4-8/day" + it.toString() + "prob.gif")
            }
            return imgUrls.map { it.getImage() }
        } else {
            val html = ("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day" + day + "otlk.html").getHtml()
            val time = html.parse("show_tab\\(.otlk_([0-9]{4}).\\)")
            when (day) {
                "1", "2" -> {
                    imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day${day}otlk_$time.gif")
                    imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_torn.gif")
                    imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_hail.gif")
                    imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day${day}probotlk_" + time + "_wind.gif")
                }

                "3" -> {
                    imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day3otlk_$time.gif")
                    imgUrls.add("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day3prob_$time.gif")
                }

                else -> {
                }
            }
            return if (getAllImages) {
                imgUrls.map { it.getImage() }
            } else {
                listOf(imgUrls[0].getImage())
            }
        }
    }

    fun getUrls(day: String): List<String> {
        if (day == "4-8" || day == "48" || day == "4") {
            return (4..8).map { "${GlobalVariables.nwsSPCwebsitePrefix}/products/exper/day4-8/day" + it.toString() + "prob.gif" }
        } else {
            val html = ("${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/day" + day + "otlk.html").getHtml()
            val time = html.parse("show_tab\\(.otlk_([0-9]{4}).\\)")
            return when (day) {
                "1", "2" -> {
                    val day1BaseUrl = GlobalVariables.nwsSPCwebsitePrefix + "/products/outlook/day" + day + "probotlk_"
                    val day1Urls = listOf("_torn.gif", "_hail.gif", "_wind.gif")
                    val urls = mutableListOf(GlobalVariables.nwsSPCwebsitePrefix + "/products/outlook/day" + day + "otlk_" + time + ".gif")
                    urls += day1Urls.map { day1BaseUrl + time + it }
                    urls
                }

                "3" -> {
                    listOf("otlk_", "prob_").map {
                        GlobalVariables.nwsSPCwebsitePrefix + "/products/outlook/day" + day + it + time + ".gif"
                    }
                }

                else -> emptyList()
            }
        }
    }

    fun getImageUrlsDays48(day: String): String =
            GlobalVariables.nwsSPCwebsitePrefix + "/products/exper/day4-8/day" + day + "prob.gif"
}
