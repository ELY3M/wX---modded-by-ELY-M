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

import android.content.Context
import android.graphics.Bitmap
import android.widget.LinearLayout
import android.widget.ScrollView
import joshuatee.wx.GlobalDictionaries
import java.util.Locale
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

class ObjectAlertSummary(private val context: Context, private val linearLayout: LinearLayout, private val scrollView: ScrollView) {

    private var totalAlertsCnt = 0
    var navList = listOf<String>()
        private set
    var filterArray = listOf<String>()
        private set
    private val mapButtonZone = mutableMapOf<Int, String>()
    private val mapButtonNws = mutableMapOf<Int, String>()
    private val mapButtonState = mutableMapOf<Int, String>()
    private val mapButtonCounty = mutableMapOf<Int, String>()
    var bitmap = UtilityImg.getBlankBitmap()
    private var objectCardImageView = ObjectCardImage(context, bitmap)
    private val cardText = ObjectCardText(context)

    init {
        linearLayout.addView(cardText.card)
        linearLayout.addView(objectCardImageView.card)
    }

    fun updateImage(bitmap: Bitmap) {
        this.bitmap = bitmap
        objectCardImageView.setImage(bitmap)
    }

    fun updateContent(data: String, filterOriginal: String, firstRun: Boolean) {
        linearLayout.removeAllViews()
        scrollView.smoothScrollTo(0, 0)
        linearLayout.addView(cardText.card)
        objectCardImageView = ObjectCardImage(context, bitmap)
        objectCardImageView.setOnClickListener { ObjectIntent.showImage(context, arrayOf("https://forecast.weather.gov/wwamap/png/US.png", "US Alerts", "true")) }
        linearLayout.addView(objectCardImageView.card)
        totalAlertsCnt = 0
        val mapEvent = mutableMapOf<String, Int>()
        val mapState = mutableMapOf<String, Int>()
        mapButtonNws.clear()
        mapButtonState.clear()
        mapButtonCounty.clear()
        mapButtonZone.clear()
        val map = mutableMapOf<String, Int>()
        var i = 0
        try {
            val capAlerts = mutableListOf<CapAlert>()
            val alerts = UtilityString.parseColumnMutable(data, "<entry>(.*?)</entry>")
            alerts.forEach { alert -> capAlerts.add(CapAlert.initializeFromCap(alert)) }
            capAlerts.forEach { capAlert ->
                val counties = capAlert.area.split(";")
                val firstCounty = if (counties.isNotEmpty()) counties[0] else ""
                val zones = capAlert.zones.split(" ")
                val firstZone = if (zones.isNotEmpty()) zones[0] else ""
                totalAlertsCnt += 1
                val tmpStateList = zones.asSequence().filter { it.length > 1 }.map { it.substring(0, 2) }
                val uniqueStates = tmpStateList.toSet()
                uniqueStates.forEach {
                    val frequency = mapState[it]
                    mapState[it] = if (frequency == null) 1 else frequency + 1
                }
                val frequency = mapEvent[capAlert.event]
                mapEvent[capAlert.event] = if (frequency == null) 1 else frequency + 1
                if (capAlert.event.matches(filterOriginal.toRegex())) {
                    val nwsOffice: String
                    val nwsLoc: String
                    if (capAlert.vtec.length > 15 && capAlert.event != "Special Weather Statement") {
                        nwsOffice = capAlert.vtec.substring(8, 11)
                        nwsLoc = Utility.getWfoSiteName(nwsOffice)
                    } else {
                        nwsOffice = ""
                        nwsLoc = ""
                    }
                    val tmp2StateList = zones.asSequence().filter { it.length > 1 }.map { it.substring(0, 2) }
                    val unique2States = tmp2StateList.toSet()
                    unique2States.forEach { state ->
                        val frequencyLocal = map[state]
                        map[state] = if (frequencyLocal == null) 1 else frequencyLocal + 1
                        mapButtonState[i] = state
                    }
                    val objectCardAlertSummaryItem = ObjectCardAlertSummaryItem(context)
                    objectCardAlertSummaryItem.setId(i)
                    mapButtonNws[i] = nwsOffice
                    mapButtonCounty[i] = firstCounty
                    mapButtonZone[i] = firstZone
                    objectCardAlertSummaryItem.setTextFields(nwsOffice, nwsLoc, capAlert)
                    val url = capAlert.url
                    objectCardAlertSummaryItem.setListener { showWarningDetails(url) }
                    objectCardAlertSummaryItem.radarButton.setOnClickListener { radarInterface(nwsOffice) }
                    objectCardAlertSummaryItem.detailsButton.setOnClickListener { showWarningDetails(url) }
                    linearLayout.addView(objectCardAlertSummaryItem.card)
                    i += 1
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        var mapOut = map.toString()
        mapOut = mapOut.replace("[{}]".toRegex(), "")
        var filter = filterOriginal
        filter = filter.replace("[|*?.]".toRegex(), " ")
        if (mapOut.isNotEmpty()) {
            cardText.text = ("Filter: " + filter.replace("\\^".toRegex(), "") + " (" + i + ")" + MyApplication.newline + mapOut)
        } else {
            cardText.text = ("Filter: " + filter.replace("\\^".toRegex(), "") + " (" + i + ")")
        }
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

    private fun radarInterface(office: String) {
        ObjectIntent.showRadarBySite(context, GlobalDictionaries.wfoToRadarSite[office] ?: "")
    }

    private fun showWarningDetails(url: String) {
        ObjectIntent.showHazard(context, arrayOf(url, ""))
    }

    fun getTitle(title: String) = "(" + totalAlertsCnt + ") " + title.uppercase(Locale.US) + " Alerts"
}
