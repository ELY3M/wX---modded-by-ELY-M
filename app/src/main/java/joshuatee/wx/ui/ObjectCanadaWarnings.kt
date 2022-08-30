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

package joshuatee.wx.ui

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import java.util.Locale
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.Route

class ObjectCanadaWarnings(private val activity: Activity, private val box: VBox, private val toolbar: Toolbar) {

    private var listLocUrl = mutableListOf<String>()
    private var listLocName = mutableListOf<String>()
    private var listLocWarning = mutableListOf<String>()
    private var listLocWatch = mutableListOf<String>()
    private var listLocStatement = mutableListOf<String>()
    private var bitmap = UtilityImg.getBlankBitmap()
    var province = "ca"

    fun getData() {
        listLocUrl.clear()
        listLocName.clear()
        listLocWarning.clear()
        listLocWatch.clear()
        listLocStatement.clear()
        bitmap = if (province == "ca") {
            (GlobalVariables.canadaEcSitePrefix + "/data/warningmap/canada_e.png").getImage()
        } else {
            (GlobalVariables.canadaEcSitePrefix + "/data/warningmap/" + province + "_e.png").getImage()
        }
        val html = if (province == "ca") {
            (GlobalVariables.canadaEcSitePrefix + "/warnings/index_e.html").getHtml()
        } else {
            (GlobalVariables.canadaEcSitePrefix + "/warnings/index_e.html?prov=$province").getHtml()
        }
        listLocUrl = html.parseColumnMutable("<tr><td><a href=\"(.*?)\">.*?</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>")
        listLocName = html.parseColumnMutable("<tr><td><a href=\".*?\">(.*?)</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>")
        listLocWarning = html.parseColumnMutable("<tr><td><a href=\".*?\">.*?</a></td>.*?<td>(.*?)</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>")
        listLocWatch = html.parseColumnMutable("<tr><td><a href=\".*?\">.*?</a></td>.*?<td>.*?</td>.*?<td>(.*?)</td>.*?<td>.*?</td>.*?<tr>")
        listLocStatement = html.parseColumnMutable("<tr><td><a href=\".*?\">.*?</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>(.*?)</td>.*?<tr>")
    }

    fun showData() {
        box.removeChildrenAndLayout()
        Image(activity, box, toolbar, bitmap)
        var locWarning: String
        var locWatch: String
        var locStatement: String
        listLocUrl.indices.forEach { index ->
            locWarning = listLocWarning[index]
            locWatch = listLocWatch[index]
            locStatement = listLocStatement[index]
            if (locWarning.contains("href")) {
                locWarning = locWarning.parse("class=.wb-inv.>(.*?)</span>")
                locWarning = locWarning.replace("</.*?>".toRegex(), "")
                locWarning = locWarning.replace("<.*?>".toRegex(), "")
            }
            if (locWatch.contains("href")) {
                locWatch = locWatch.parse("class=.wb-inv.>(.*?)</span>")
                locWatch = locWatch.replace("</.*?>".toRegex(), "")
                locWatch = locWatch.replace("<*?>>".toRegex(), "")
            }
            if (locStatement.contains("href")) {
                locStatement = locStatement.parse("class=.wb-inv.>(.*?)</span>")
                locStatement = locStatement.replace("</.*?>".toRegex(), "")
                locStatement = locStatement.replace("<.*?>".toRegex(), "")
            }
            val province = listLocUrl[index].parse("report_e.html.([a-z]{2}).*?")
            val cardText = CardText(activity, box)
            cardText.text = Utility.fromHtml(province.uppercase(Locale.US) + ": " + locWarning + " " + locWatch + " " + locStatement)
            val url = GlobalVariables.canadaEcSitePrefix + listLocUrl[index]
            val location = listLocName[index]
            cardText.connect { getWarningDetail(url, location) }
        }
        ObjectCALegal(activity, box.get(), GlobalVariables.canadaEcSitePrefix + "/warnings/index_e.html")
    }

    val title get() = provinceToLabel[province] + " (" + listLocUrl.size + ")"

    private fun getWarningDetail(url: String, location: String) {
        FutureText2(activity, { UtilityCanada.getHazardsFromUrl(url) }) { data -> Route.text(activity, data, location) }
    }

    companion object {
        private val provinceToLabel = mapOf(
                "ca" to "Canada",
                "ab" to "Alberta",
                "bc" to "British Columbia",
                "mb" to "Manitoba",
                "nb" to "New Brunswick",
                "nl" to "Newfoundland and Labrador",
                "ns" to "Nova Scotia",
                "nt" to "Northwest Territories",
                "nu" to "Nunavut",
                "son" to "Ontario - South",
                "non" to "Ontario - North",
                "pei" to "Prince Edward Island",
                "sqc" to "Quebec - South",
                "nqc" to "Quebec - North",
                "sk" to "Saskatchewan",
                "yt" to "Yukon"
        )
    }
}
