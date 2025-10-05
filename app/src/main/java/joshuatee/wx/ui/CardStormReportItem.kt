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

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.spc.StormReport
import java.util.Locale

class CardStormReportItem(context: Context, stormReport: StormReport, k: Int) : Widget {

    private val card = Card(context)
    private val textTop = Text(context, UIPreferences.textHighlightColor)
    private val textTitle = Text(context)
    private val textBottom = Text(context, backgroundText = true)

    init {
        val vbox = VBox.centered(context)
        vbox.addWidgets(textTop, textTitle, textBottom)
        card.addLayout(vbox)
        setId(k)
        setTextFields(stormReport)
    }

    fun registerForContextMenu(activity: Activity) {
        activity.registerForContextMenu(card.getView())
    }

    // This is needed for long press on the card for archived L2 radar (unreliable feature which should be removed)
    private fun setId(id: Int) {
        card.setId(id)
    }

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }

    private fun setTextFields(stormReport: StormReport) {
        textTop.text = stormReport.state + ", " + stormReport.city + " " + stormReport.time
        textTitle.text = stormReport.address
        textBottom.text = stormReport.magnitude + " - " + stormReport.damageReport
    }

    fun setTextHeader(stormReport: StormReport) {
        textTop.text = stormReport.damageHeader.uppercase(Locale.US)
        textTop.setSize(TextSize.LARGE)
        textTop.setPadding(20)
        textTitle.visibility = View.GONE
        textBottom.visibility = View.GONE
        textTop.setBackgroundColor(Color.BLACK)
        textTop.setTextColor(Color.WHITE)
    }

    override fun getView() = card.getView()
}
