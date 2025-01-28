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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.widget.ScrollView
import java.util.Locale
import joshuatee.wx.misc.CapAlert
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.Route
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityString

class AlertSummary(
    private val context: Context,
    mainBox: VBox,
    private val scrollView: ScrollView
) {

    //
    // Container used by USAlertsActivity that contains both
    // views and the data that will go into them
    //

    var navList = listOf<String>()
        private set
    var filterArray = listOf<String>()
        private set
    var bitmap = UtilityImg.getBlankBitmap()
    private var image = Image(context, bitmap)
    private val cardText = CardText(context)
    private val textBox = VBox(context)
    private var capAlerts = listOf<CapAlert>()

    init {
        if (UtilityUI.isLandScape(context)) {
            mainBox.makeHorizontal()
        }
        textBox.addWidget(cardText)
        textBox.matchParentWidth()  // ChromeOS optimization
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
        image.connect {
            Route.image(
                context,
                "https://forecast.weather.gov/wwamap/png/US.png",
                "US Alerts"
            )
        }
        val mapEvent = mutableMapOf<String, Int>()
        val mapState = mutableMapOf<String, Int>()
        val mapStateForFilter = mutableMapOf<String, Int>()
        var i = 0
        val alerts = UtilityString.parseColumn(data, "<entry>(.*?)</entry>")
        capAlerts = alerts.map { CapAlert.initializeFromCap(it) }
        capAlerts.forEach { capAlert ->
            val zones = capAlert.zones.split(" ")
            updateStateMap(zones, mapState)
            //
            // build a map to track which events have been seen and how many times
            //
            updateEventMap(capAlert.event, mapEvent)
            if (capAlert.event.matches(filterOriginal.toRegex())) {
                updateStateMap(zones, mapStateForFilter)
                val cardAlertDetail = CardAlertDetail(context, capAlert)
                cardAlertDetail.connect { Route.hazard(context, capAlert.url) }
                textBox.addWidget(cardAlertDetail)
                i += 1
            }
        }
        //
        // Update the navigation drawer entries and the textual filter label
        //
        val mapOut = mapStateForFilter.toString().replace("[{}]".toRegex(), "")
        val filter = filterOriginal.replace("[|*?.]".toRegex(), " ")
        if (mapOut.isNotEmpty()) {
            cardText.text = ("Filter: " + filter.replace(
                "\\^".toRegex(),
                ""
            ) + " (" + i + ")" + GlobalVariables.newline + mapOut)
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

    //
    // Update a map to track which states have been seen and how many times
    //
    private fun updateStateMap(zoneList: List<String>, stateMap: MutableMap<String, Int>) {
        val stateList = zoneList.asSequence().filter { it.length > 1 }.map { it.substring(0, 2) }
        val uniqueStateList = stateList.toSet()
        uniqueStateList.forEach { state ->
            val previousCount = stateMap.getOrDefault(state, 0)
            stateMap[state] = previousCount + 1
        }
    }

    private fun updateEventMap(event: String, map: MutableMap<String, Int>) {
        val prev = map.getOrDefault(event, 0)
        map[event] = prev + 1
    }

    fun getTitle(title: String) =
        "(" + capAlerts.size + ") " + title.uppercase(Locale.US) + " Alerts"
}
