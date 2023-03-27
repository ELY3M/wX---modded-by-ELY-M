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

import android.content.Context
import android.graphics.Bitmap
import android.widget.ScrollView
import java.util.Locale
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

class AlertSummary(private val context: Context, mainBox: VBox, private val scrollView: ScrollView) {

    var navList = listOf<String>()
        private set
    var filterArray = listOf<String>()
        private set
    var bitmap = UtilityImg.getBlankBitmap()
    private var image = Image(context, bitmap)
    private val cardText = CardText(context)
    private val textBox = VBox(context)
    var capAlerts = listOf<CapAlert>()

    init {
        if (UtilityUI.isLandScape(context)) {
            mainBox.makeHorizontal()
        }
        textBox.addWidget(cardText)
        mainBox.addWidget(image)
        mainBox.addLayout(textBox)
    }

    fun updateImage(bitmap: Bitmap) {
        this.bitmap = bitmap
        if (UtilityUI.isLandScape(context)) {
            image.set2(bitmap, 2)
        } else {
            image.set2(bitmap)
        }
    }

    fun updateContent(data: String, filterOriginal: String, firstRun: Boolean) {
        textBox.removeChildrenAndLayout()
        scrollView.smoothScrollTo(0, 0)
        textBox.addWidget(cardText)
        if (UtilityUI.isLandScape(context)) {
            image.set2(bitmap, 2)
        } else {
            image.set2(bitmap)
        }
        image.connect { Route.image(context, "https://forecast.weather.gov/wwamap/png/US.png", "US Alerts") }
        val mapEvent = mutableMapOf<String, Int>()
        val mapState = mutableMapOf<String, Int>()
        val map = mutableMapOf<String, Int>()
        var i = 0
        try {
            val alerts = UtilityString.parseColumn(data, "<entry>(.*?)</entry>")
            capAlerts = alerts.map { CapAlert.initializeFromCap(it) }
            capAlerts.forEach { capAlert ->
                val zones = capAlert.zones.split(" ")
                val tmpStateList = zones.filter { it.length > 1 }.map { it.substring(0, 2) }
                val uniqueStates = tmpStateList.toSet()
                uniqueStates.forEach {
                    val prev = mapState.getOrDefault(it, 0)
                    mapState[it] = prev + 1
                }
                val prev = mapEvent.getOrDefault(capAlert.event, 0)
                mapEvent[capAlert.event] = prev + 1
                if (capAlert.event.matches(filterOriginal.toRegex())) {
                    val nwsOffice: String
                    val nwsLoc: String
                    if (capAlert.vtec.length > 15 && capAlert.event != "Special Weather Statement") {
                        nwsOffice = capAlert.vtec.substring(8, 11)
                        nwsLoc = UtilityLocation.getWfoSiteName(nwsOffice)
                    } else {
                        nwsOffice = ""
                        nwsLoc = ""
                    }
                    val tmp2StateList = zones.asSequence().filter { it.length > 1 }.map { it.substring(0, 2) }
                    val unique2States = tmp2StateList.toSet()
                    unique2States.forEach { state ->
                        val prev1 = map.getOrDefault(state, 0)
                        map[state] = prev1 + 1
                    }
                    val cardAlertDetail = CardAlertDetail(context)
                    cardAlertDetail.setTextFields(nwsOffice, nwsLoc, capAlert)
                    cardAlertDetail.connect { Route.hazard(context, capAlert.url) }
                    textBox.addWidget(cardAlertDetail)
                    i += 1
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        val mapOut = map.toString().replace("[{}]".toRegex(), "")
        val filter = filterOriginal.replace("[|*?.]".toRegex(), " ")
        if (mapOut.isNotEmpty()) {
            cardText.text = ("Filter: " + filter.replace("\\^".toRegex(), "") + " (" + i + ")" + GlobalVariables.newline + mapOut)
        } else {
            cardText.text = ("Filter: " + filter.replace("\\^".toRegex(), "") + " (" + i + ")")
        }
        if (firstRun) {
            val filtersByEvent = mapEvent.keys.sorted()
            val filtersByEventLabel = filtersByEvent.map { it + ": " + mapEvent[it] }
            val filtersByState = mapState.keys.sorted().toList()
            val filtersByStateLabel = filtersByState.map { it + ": " + mapState[it] }
            filterArray = filtersByEvent + filtersByState
            navList = filtersByEventLabel + filtersByStateLabel
        }
    }

    fun getTitle(title: String): String =
            "(" + capAlerts.size + ") " + title.uppercase(Locale.US) + " Alerts"
}
