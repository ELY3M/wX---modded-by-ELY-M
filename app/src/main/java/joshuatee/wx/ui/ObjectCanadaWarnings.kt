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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.widget.LinearLayout

import java.util.Locale

import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

class ObjectCanadaWarnings(private val context: Context, private val activity: Activity, private val linearLayout: LinearLayout, private val toolbar: Toolbar) {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
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
            (MyApplication.canadaEcSitePrefix + "/data/warningmap/canada_e.png").getImage()
        } else {
            (MyApplication.canadaEcSitePrefix + "/data/warningmap/" + province + "_e.png").getImage()
        }
        val html = if (province == "ca") {
            (MyApplication.canadaEcSitePrefix + "/warnings/index_e.html").getHtml()
        } else {
            (MyApplication.canadaEcSitePrefix + "/warnings/index_e.html?prov=$province").getHtml()
        }
        listLocUrl = html.parseColumnMutable("<tr><td><a href=\"(.*?)\">.*?</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>")
        listLocName = html.parseColumnMutable("<tr><td><a href=\".*?\">(.*?)</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>")
        listLocWarning = html.parseColumnMutable("<tr><td><a href=\".*?\">.*?</a></td>.*?<td>(.*?)</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>")
        listLocWatch = html.parseColumnMutable("<tr><td><a href=\".*?\">.*?</a></td>.*?<td>.*?</td>.*?<td>(.*?)</td>.*?<td>.*?</td>.*?<tr>")
        listLocStatement = html.parseColumnMutable("<tr><td><a href=\".*?\">.*?</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>(.*?)</td>.*?<tr>")
    }

    fun showData() {
        linearLayout.removeAllViews()
        ObjectCardImage(context, linearLayout, toolbar, bitmap)
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
            val objectCardText = ObjectCardText(context, linearLayout)
            objectCardText.text = Utility.fromHtml(province.uppercase(Locale.US) + ": " + locWarning + " " + locWatch + " " + locStatement)
            val url = MyApplication.canadaEcSitePrefix + listLocUrl[index]
            val location = listLocName[index]
            objectCardText.setOnClickListener { getWarningDetail(url, location) }
        }
        ObjectCALegal(activity, linearLayout, MyApplication.canadaEcSitePrefix + "/warnings/index_e.html")
    }

    val title get() = provinceToLabel[province] + " (" + listLocUrl.size + ")"

    private fun getWarningDetail(url: String, location: String) =
            GlobalScope.launch(uiDispatcher) {
                val data = withContext(Dispatchers.IO) { UtilityCanada.getHazardsFromUrl(url) }
                ObjectIntent.showText(context, arrayOf(data, location))
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


