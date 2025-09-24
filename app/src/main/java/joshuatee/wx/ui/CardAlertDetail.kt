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
import android.view.Gravity
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.misc.CapAlert
import joshuatee.wx.misc.UtilityCapAlert
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.TextSize
import joshuatee.wx.util.WfoSites

class CardAlertDetail(val context: Context, val capAlert: CapAlert) : Widget {

    private val card = Card(context)
    private val textViewTop = Text(context, UIPreferences.textHighlightColor)
    private val textViewTitle = Text(context)
    private val textViewStart = Text(context, TextSize.SMALL)
    private val textViewEnd = Text(context, TextSize.SMALL)
    private val textViewBottom = Text(context, backgroundText = true)
    private val radarButton = Button(context, "Radar", GlobalVariables.ICON_RADAR) {
        Route.radarBySite(context, capAlert.getClosestRadarXml())
    }
    private val detailsButton = Button(context, "Details", GlobalVariables.ICON_CURRENT) {
        Route.hazard(context, capAlert.url)
    }

    init {
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        vbox.addWidgets(
            listOf(
                textViewTop,
                textViewTitle,
                textViewStart,
                textViewEnd,
                textViewBottom
            )
        )
        val hbox = HBox(context)
        with(hbox) {
            //wrap() remove for ChromeOS optimization
            addWidget(radarButton)
            addWidget(detailsButton)
            vbox.addLayout(this)
        }
        card.addLayout(vbox)

        val office: String
        val location: String
        if (capAlert.vtec.length > 15 && capAlert.event != "Special Weather Statement") {
            office = capAlert.vtec.substring(8, 11)
            location = WfoSites.getFullName(office)
        } else {
            office = ""
            location = ""
        }

        setTextFields(office, location)
    }

    override fun getView() = card.getView()

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }

    private fun setTextFields(office: String, location: String) {
        val items = UtilityCapAlert.timesForCard(capAlert)
        val startTime = items[0]
        val endTime = items[1]
        val title = items[2]
        textViewTop.text = "$office ($location)"
        if (office == "") {
            textViewTop.visibility = View.GONE
        }
        textViewBottom.text = capAlert.area
        textViewTitle.text = title
        textViewStart.text = "Start: $startTime"
        if (endTime != "") {
            textViewEnd.text = "End: $endTime"
        } else {
            textViewEnd.visibility = View.GONE
        }
        if (capAlert.points.size < 2) {
            radarButton.visibility = View.GONE
        }
    }
}
