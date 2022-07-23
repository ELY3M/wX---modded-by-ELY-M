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
import android.view.Gravity
import android.view.View
import joshuatee.wx.Extensions.parseMultiple
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.TextSize
import joshuatee.wx.util.UtilityLog

class ObjectCardAlertDetail(val context: Context) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTitle = ObjectTextView(context)
    private val textViewStart = ObjectTextView(context, TextSize.SMALL)
    private val textViewEnd = ObjectTextView(context, TextSize.SMALL)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)
    private val radarButton = ObjectButton(context,"Radar", GlobalVariables.ICON_RADAR)
    private val detailsButton = ObjectButton(context,"Details", GlobalVariables.ICON_CURRENT)

    init {
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        vbox.addViews(listOf(textViewTop.get(), textViewTitle.get(), textViewStart.get(), textViewEnd.get(), textViewBottom.get()))
        val hbox = HBox(context)
        hbox.wrap()
        hbox.addWidget(radarButton.get())
        hbox.addWidget(detailsButton.get())
        vbox.addLayout(hbox.get())
        objectCard.addView(vbox)
    }

    fun get() = objectCard.get()

    fun setListener(fn: View.OnClickListener) {
        objectCard.setOnClickListener(fn)
    }

    fun setTextFields(office: String, location: String, capAlert: CapAlert) {
        val title: String
        val startTime: String
        val endTime: String
        if (capAlert.title.contains("until")) {
            val items = capAlert.title.parseMultiple("(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
            title = items[0]
            startTime = items[1]
            endTime = items[2]
        } else {
            val items = capAlert.title.parseMultiple("(.*?) issued (.*?) by (.*?)$", 3)
            title = items[0]
            startTime = items[1]
            endTime = ""
        }
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
            UtilityLog.d("WX", "POINTS HIDE")
            radarButton.visibility = View.GONE
        } else {
            UtilityLog.d("WX", "POINTS NO HIDE " + capAlert.points.size + " " + capAlert.getClosestRadarXml())
            radarButton.setOnClickListener { ObjectIntent.showRadarBySite(context, capAlert.getClosestRadarXml()) }
        }
        detailsButton.setOnClickListener { ObjectIntent.showHazard(context, arrayOf(capAlert.url, "")) }
    }
}
