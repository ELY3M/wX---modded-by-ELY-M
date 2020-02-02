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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView

import java.util.HashSet
import java.util.Locale
import java.util.TreeMap

import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.activitiesmisc.USAlertsDetailActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

class ObjectAlertSummary(
        private val activity: Activity,
        private val context: Context,
        private val linearLayout: LinearLayout,
        private val sv: ScrollView
) {

    private var totalAlertsCnt = 0
    var navList: List<String> = listOf()
        private set
    var filterArray: List<String> = listOf()
        private set
    @SuppressLint("UseSparseArrays")
    val mapButtonZone: MutableMap<Int, String> = mutableMapOf()
    @SuppressLint("UseSparseArrays")
    val mapButtonNws: MutableMap<Int, String> = mutableMapOf()
    @SuppressLint("UseSparseArrays")
    val mapButtonState: MutableMap<Int, String> = mutableMapOf()
    @SuppressLint("UseSparseArrays")
    val mapButtonCounty: MutableMap<Int, String> = mutableMapOf()

    fun updateContent(bitmap: Bitmap, data: String, filterEventStr: String, firstRun: Boolean) {
        linearLayout.removeAllViews()
        sv.smoothScrollTo(0, 0)
        val cardText = ObjectCardText(context)
        linearLayout.addView(cardText.card)
        val objectCardImageView = ObjectCardImage(context, bitmap)
        objectCardImageView.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    context,
                    ImageShowActivity::class.java,
                    ImageShowActivity.URL,
                    arrayOf("https://forecast.weather.gov/wwamap/png/US.png", "US Alerts", "true")
            )
        })
        linearLayout.addView(objectCardImageView.card)
        totalAlertsCnt = 0
        val mapEvent = TreeMap<String, Int>()
        val mapState = TreeMap<String, Int>()
        mapButtonNws.clear()
        mapButtonState.clear()
        mapButtonCounty.clear()
        mapButtonZone.clear()
        val map = TreeMap<String, Int>()
        var i = 0
        try {
            var html = ""
            var countyArr: List<String>
            var zoneArr: List<String>
            var firstCounty = ""
            var firstZone = ""
            val ca = mutableListOf<CapAlert>()
            val alerts = UtilityString.parseColumnMutable(data, "<entry>(.*?)</entry>")
            alerts.forEach {
                ca.add(CapAlert.initializeFromCap(it))
            }
            ca.forEach { cc ->
                countyArr = cc.area.split(";")
                if (countyArr.isNotEmpty()) firstCounty = countyArr[0]
                zoneArr = cc.zones.split(" ")
                if (zoneArr.isNotEmpty()) firstZone = zoneArr[0]
                totalAlertsCnt += 1
                val tmpStateList = zoneArr.asSequence().filter { it.length > 1 }
                        .mapTo(mutableListOf()) { it.substring(0, 2) }
                val uniqueStates = HashSet(tmpStateList)
                uniqueStates.forEach {
                    val freq3 = mapState[it]
                    mapState[it] = if (freq3 == null) 1 else freq3 + 1
                }
                val freq2: Int? = mapEvent[cc.event]
                mapEvent[cc.event] = if (freq2 == null) 1 else freq2 + 1
                if (cc.event.matches(filterEventStr.toRegex())) {
                    html += "<b>" + cc.title + "</b><br>"
                    html += "<b>Counties: " + cc.area + "</b><br>"
                    html += cc.vtec + "<br><br>"
                    val nwsOffice: String
                    val nwsLoc: String
                    if (cc.vtec.length > 15 && cc.event != "Special Weather Statement") {
                        nwsOffice = cc.vtec.substring(8, 11)
                        nwsLoc = Utility.getWfoSiteName(nwsOffice)
                    } else {
                        nwsOffice = ""
                        nwsLoc = ""
                    }
                    val tmp2StateList = zoneArr.asSequence().filter { it.length > 1 }
                            .mapTo(mutableListOf()) { it.substring(0, 2) }
                    val unique2States = HashSet(tmp2StateList)
                    unique2States.forEach { s ->
                        val freq = map[s]
                        map[s] = if (freq == null) 1 else freq + 1
                        mapButtonState[i] = s
                    }
                    val cText = ObjectCardAlertSummaryItem(context)
                    cText.setId(i)
                    activity.registerForContextMenu(cText.card)
                    mapButtonNws[i] = nwsOffice
                    mapButtonCounty[i] = firstCounty
                    mapButtonZone[i] = firstZone
                    cText.setTextFields(nwsOffice, nwsLoc, cc)
                    val urlStr = cc.url
                    //UtilityLog.d("wx", "URL: " + nwsOffice)
                    cText.setListener(View.OnClickListener {
                        ObjectIntent(
                                context,
                                USAlertsDetailActivity::class.java,
                                USAlertsDetailActivity.URL,
                                arrayOf(urlStr, "")
                        )
                    })
                    linearLayout.addView(cText.card)
                    i += 1
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }

        var mapOut = map.toString()
        mapOut = mapOut.replace("[{}]".toRegex(), "")
        var filter = filterEventStr
        filter = filter.replace("[|*?.]".toRegex(), " ")
        //filter = filter.replace("\\||\\*|\\?|\\.".toRegex(), " ")
        if (mapOut.isNotEmpty())
            cardText.text = (
                    "Filter: " + filter.replace(
                            "\\^".toRegex(),
                            ""
                    ) + " (" + i + ")" + MyApplication.newline + mapOut
            )
        else
            cardText.text = ("Filter: " + filter.replace("\\^".toRegex(), "") + " (" + i + ")")
        if (firstRun) {
            val filterArray1 = mapEvent.keys.toList()
            val filterArray1Label = mutableListOf<String>()
            filterArray1.indices.forEach { filterArray1Label.add(filterArray1[it] + ": " + mapEvent[filterArray1[it]]) }
            val filterArray2 = mapState.keys.toList()
            val filterArray2Label = mutableListOf<String>()
            filterArray2.indices.forEach { filterArray2Label.add(filterArray2[it] + ": " + mapState[filterArray2[it]]) }
            filterArray = filterArray1 + filterArray2
            navList = filterArray1Label + filterArray2Label
        }
    }

    fun getTitle(title: String): String =
            "(" + totalAlertsCnt + ") " + title.toUpperCase(Locale.US) + " Alerts"
}
