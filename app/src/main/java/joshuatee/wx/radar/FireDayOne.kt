/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

import android.graphics.Color
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.LatLon
import joshuatee.wx.parseAcrossLines
import joshuatee.wx.parseColumnAcrossLines
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityString

internal object FireDayOne {

    val timer = DownloadTimer("FIRE")
    val polygonBy = mutableMapOf<Int, List<Double>>()
    private val threatList = listOf(
        "ELEV",
        "CRIT",
        "EXTM"
    )
    val colors = intArrayOf(
        Color.rgb(255, 128, 0),
        Color.rgb(255, 0, 0),
        Color.rgb(255, 128, 255),
    )

    fun get() {
        if (timer.isRefreshNeeded()) {
            /*
            FIRE WEATHER OUTLOOK POINTS DAY 1

            ... FIRE WEATHER CATEGORICAL ...

            ELEV   33971639 33581665 33361663 32991643 32591629 32541643
                   32551688 32691696 32861696 32951721 33331745 33641795
                   33751807 33781839 34041852 34011880 34111914 34391951
                   34421965 34571973 34661967 34711941 34751854 34431728
                   34361680 34261658 33971639
            CRIT   33141681 33281738 33471759 33621785 33711797 33971784
                   34181807 34211839 34091854 34051886 34201923 34411939
                   34571899 34611855 34511830 34331733 33801685 33241667
                   33141681
            &&

            */

            val mainHtml =
                "https://www.spc.noaa.gov/products/fire_wx/fwdy1.html".getHtmlWithNewLine()
            // CLICK FOR <a href="/products/fire_wx/2025/250111_1200_day1pts.txt">DAY 1 FIREWX AREAL OUTLINE PRODUCT (KWNSPFWFD1)</a>
            val arealOutlineUrl = UtilityString.parse(
                mainHtml,
                "a href=.(/products/fire_wx/[0-9]{4}/[0-9]{6}_[0-9]{4}_day1pts.txt).>DAY 1 FIREWX AREAL OUTLINE PRODUCT"
            )
            // arealOutlineUrl: str = "/products/fire_wx/2025/250108_1200_day1pts.txt"
            val html =
                (GlobalVariables.NWS_SPC_WEBSITE_PREFIX + arealOutlineUrl).getHtmlWithNewLine()
            val htmlChunk = html.parseAcrossLines("... CATEGORICAL ...(.*?&)&")
            threatList.indices.forEach { threatIndex ->
                var data = ""
                val threatLevelCode = threatList[threatIndex]
                val htmlList =
                    htmlChunk.parseColumnAcrossLines(threatLevelCode.substring(1) + "(.*?)[A-Z&]")
                val warningList = mutableListOf<Double>()
                htmlList.forEach { polygon ->
                    val coordinates = polygon.parseColumnAcrossLines("([0-9]{8}).*?")
                    coordinates.forEach { coordinate ->
                        data += LatLon(coordinate).print()
                    }
                    data += ":"
                    data = data.replace(" :", ":")
                }
                val polygons = data.split(":").dropLastWhile { it.isEmpty() }
                if (polygons.isNotEmpty()) {
                    polygons.forEach { polygon ->
                        val numbers = RegExp.space.split(polygon)
                        val x = numbers.filterIndexed { index: Int, _: String -> index and 1 == 0 }
                            .map { To.double(it) }
                        val y = numbers.filterIndexed { index: Int, _: String -> index and 1 != 0 }
                            .map { To.double(it) * -1.0 }
                        if (x.isNotEmpty() && y.isNotEmpty()) {
                            warningList.add(x[0])
                            warningList.add(y[0])
                            (1..x.size - 2).forEach { j ->
                                if (x[j] < 99.0) {
                                    warningList.add(x[j])
                                    warningList.add(y[j])
                                    warningList.add(x[j])
                                    warningList.add(y[j])
                                } else {
                                    warningList.add(x[j - 1])
                                    warningList.add(y[j - 1])
                                    warningList.add(x[j + 1])
                                    warningList.add(y[j + 1])
                                }
                            }
                            warningList.add(x.last())
                            warningList.add(y[x.lastIndex])
                        }
                        polygonBy[threatIndex] = warningList
                    }
                } else {
                    polygonBy[threatIndex] = listOf()
                }
            }
        }
    }
}
