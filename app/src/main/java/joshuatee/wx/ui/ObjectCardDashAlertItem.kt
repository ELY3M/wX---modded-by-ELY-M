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
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.ObjectWarning

class ObjectCardDashAlertItem(val context: Context, val box: VBox, private val warning: ObjectWarning) {

    private val card = Card(context)
    private val textTop = Text(context, UIPreferences.textHighlightColor)
    private val textTitle = Text(context)
    private val textStart = Text(context)
    private val textEnd = Text(context)
    private val textBottom = Text(context, backgroundText = true)
    private val radarButton = Button(context,"Radar", GlobalVariables.ICON_RADAR)
    private val detailsButton = Button(context,"Details", GlobalVariables.ICON_CURRENT)

    init {
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        listOf(textTop, textTitle, textStart, textEnd, textBottom).forEach {
            vbox.addWidget(it.get())
        }
        val hbox = HBox(context)
        hbox.wrap()
        hbox.addWidget(radarButton.get())
        hbox.addWidget(detailsButton.get())
        vbox.addLayout(hbox.get())
        card.addLayout(vbox)
        setTextFields()
        box.addWidget(card.get())
        radarButton.connect { Route.radarBySite(context, warning.getClosestRadar()) }
        detailsButton.connect { Route.hazard(context, warning.url) }
    }

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }

    private fun setTextFields() {
        textTop.text = warning.sender
        textTitle.text = warning.event
        textStart.text = warning.effective.replace("T", " ").replace(Regex(":00-0[0-9]:00"), "").replace(Regex(":00-10:00"), "")
        textEnd.text = warning.expires.replace("T", " ").replace(Regex(":00-0[0-9]:00"), "").replace(Regex(":00-10:00"), "")
        textBottom.text = warning.area
    }
}
