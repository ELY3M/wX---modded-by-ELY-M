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
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.spc.StormReport
import java.util.*

class ObjectCardStormReportItem(context: Context) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTitle = ObjectTextView(context)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)

    init {
        val linearLayoutVertical = ObjectLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER_VERTICAL)
        linearLayoutVertical.addViews(listOf(textViewTop.tv, textViewTitle.tv, textViewBottom.tv))
        objectCard.addView(linearLayoutVertical)
    }

    val card get() = objectCard.card

    fun setId(id: Int) { objectCard.card.id = id }

    fun setListener(fn: View.OnClickListener) = objectCard.card.setOnClickListener(fn)

    fun setTextFields(stormReport: StormReport) {
        textViewTop.text = stormReport.state + ", " + stormReport.city + " " + stormReport.time
        textViewTitle.text = stormReport.address
        textViewBottom.text = stormReport.magnitude + " - " + stormReport.description
    }

    fun setTextHeader(stormReport: StormReport) {
        textViewTop.text = stormReport.title.uppercase(Locale.US)
        textViewTop.setTextSize(TextSize.LARGE)
        textViewTop.setPadding(20)
        textViewTitle.tv.visibility = View.GONE
        textViewBottom.tv.visibility = View.GONE
        textViewTop.tv.setBackgroundColor(Color.BLACK)
        textViewTop.tv.setTextColor(Color.WHITE)
    }
}


