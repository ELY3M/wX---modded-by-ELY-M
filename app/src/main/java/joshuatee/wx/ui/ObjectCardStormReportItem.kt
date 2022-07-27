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
import android.graphics.Color
import android.view.Gravity
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.spc.StormReport
import java.util.*

class ObjectCardStormReportItem(context: Context) {

    private val card = Card(context)
    private val textTop = Text(context, UIPreferences.textHighlightColor)
    private val textTitle = Text(context)
    private val textBottom = Text(context, backgroundText = true)

    init {
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        vbox.addViews(listOf(textTop.get(), textTitle.get(), textBottom.get()))
        card.addView(vbox)
    }

    fun get() = card.get()

    // This is needed for long press on the card for archived L2 radar (unreliable feature which should be removed)
    fun setId(id: Int) {
        card.setId(id)
    }

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }

    fun setTextFields(stormReport: StormReport) {
        textTop.text = stormReport.state + ", " + stormReport.city + " " + stormReport.time
        textTitle.text = stormReport.address
        textBottom.text = stormReport.magnitude + " - " + stormReport.description
    }

    fun setTextHeader(stormReport: StormReport) {
        textTop.text = stormReport.title.uppercase(Locale.US)
        textTop.setTextSize(TextSize.LARGE)
        textTop.setPadding(20)
        textTitle.visibility = View.GONE
        textBottom.visibility = View.GONE
        textTop.setBackgroundColor(Color.BLACK)
        textTop.setTextColor(Color.WHITE)
    }
}
