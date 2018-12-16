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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.LinearLayout

import java.util.Locale

import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

class ObjectCAWARN(
    private val context: Context,
    private val activity: Activity,
    private val ll: LinearLayout,
    private val toolbar: Toolbar
) {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var listLocUrl = mutableListOf<String>()
    private var listLocName = mutableListOf<String>()
    private var listLocWarning = mutableListOf<String>()
    private var listLocWatch = mutableListOf<String>()
    private var listLocStatement = mutableListOf<String>()
    private var bitmap = UtilityImg.getBlankBitmap()
    var prov: String = "ca"

    fun getData() {
        listLocUrl.clear()
        listLocName.clear()
        listLocWarning.clear()
        listLocWatch.clear()
        listLocStatement.clear()
        try {
            bitmap = if (prov == "ca") {
                "http://weather.gc.ca/data/warningmap/canada_e.png".getImage()
            } else {
                ("http://weather.gc.ca/data/warningmap/" + prov + "_e.png").getImage()
            }
            val dataAsString = if (prov == "ca") {
                "http://weather.gc.ca/warnings/index_e.html".getHtml()
            } else {
                ("http://weather.gc.ca/warnings/index_e.html?prov=$prov").getHtml()
            }
            listLocUrl = UtilityString.parseColumnMutable(
                dataAsString,
                "<tr><td><a href=\"(.*?)\">.*?</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>"
            )
            listLocName = UtilityString.parseColumnMutable(
                dataAsString,
                "<tr><td><a href=\".*?\">(.*?)</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>"
            )
            listLocWarning = UtilityString.parseColumnMutable(
                dataAsString,
                "<tr><td><a href=\".*?\">.*?</a></td>.*?<td>(.*?)</td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<tr>"
            )
            listLocWatch = UtilityString.parseColumnMutable(
                dataAsString,
                "<tr><td><a href=\".*?\">.*?</a></td>.*?<td>.*?</td>.*?<td>(.*?)</td>.*?<td>.*?</td>.*?<tr>"
            )
            listLocStatement = UtilityString.parseColumnMutable(
                dataAsString,
                "<tr><td><a href=\".*?\">.*?</a></td>.*?<td>.*?</td>.*?<td>.*?</td>.*?<td>(.*?)</td>.*?<tr>"
            )
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    fun showData() {
        ll.removeAllViews()
        val c0 = ObjectCardImage(context, bitmap)
        c0.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar) })
        ll.addView(c0.card)
        var locWarning: String
        var locWatch: String
        var locStatement: String
        var provL: String
        listLocUrl.indices.forEach { it ->
            locWarning = listLocWarning[it]
            locWatch = listLocWatch[it]
            locStatement = listLocStatement[it]
            if (locWarning.contains("href")) {
                locWarning += "(Warning)"
                locWarning = locWarning.replace("</.*?>".toRegex(), "")
                locWarning = locWarning.replace("<.*?>".toRegex(), "")
            }
            if (locWatch.contains("href")) {
                locWatch += "(Watch)"
                locWatch = locWatch.replace("</.*?>".toRegex(), "")
                locWatch = locWatch.replace("<*?>>".toRegex(), "")
            }
            if (locStatement.contains("href")) {
                locStatement += "(Statement)"
                locStatement = locStatement.replace("</.*?>".toRegex(), "")
                locStatement = locStatement.replace("<.*?>".toRegex(), "")
            }
            provL = listLocUrl[it].parse("report_e.html.([a-z]{2}).*?")
            val cText = ObjectCardText(context)
            cText.setText(Utility.fromHtml(provL.toUpperCase(Locale.US) + ": " + listLocName[it] + " " + locWarning + " " + locWatch + " " + locStatement))
            val urlStr = "http://weather.gc.ca" + listLocUrl[it]
            val location = listLocName[it]
            cText.setOnClickListener(View.OnClickListener { getWarningDetail(urlStr, location) })
            ll.addView(cText.card)
        }
        val cBanner = ObjectCALegal(activity, "http://weather.gc.ca/warnings/index_e.html")
        ll.addView(cBanner.card)
    }

    val title: String get() = PROV_TO_LABEL[prov] + " (" + listLocUrl.size + ")"

    private fun getWarningDetail(urlStr: String, location: String) =
        GlobalScope.launch(uiDispatcher) {
            var data = ""
            withContext(Dispatchers.IO) {
                data = UtilityCanada.getHazardsFromUrl(urlStr)
            }
            ObjectIntent(
                context,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(data, location)
            )
        }

    companion object {
        private val PROV_TO_LABEL = mapOf(
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


