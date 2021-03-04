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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.Extensions.parseMultiple
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences

import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.objects.TextSize

class ObjectCardAlertSummaryItem(context: Context) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTitle = ObjectTextView(context)
    private val textViewStart = ObjectTextView(context, TextSize.SMALL)
    private val textViewEnd = ObjectTextView(context, TextSize.SMALL)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)
    val radarButton = ObjectButton(context,"Radar", MyApplication.ICON_RADAR_BLACK)
    val detailsButton = ObjectButton(context,"Details", MyApplication.ICON_CURRENT_BLACK)

    init {
        val objectLinearLayout = ObjectLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER_VERTICAL)
        objectLinearLayout.addViews(listOf(textViewTop.tv, textViewTitle.tv, textViewStart.tv, textViewEnd.tv, textViewBottom.tv))
        val linearLayoutHorizontal = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        linearLayoutHorizontal.layoutParams = layoutParams
        linearLayoutHorizontal.addView(radarButton.card)
        linearLayoutHorizontal.addView(detailsButton.card)
        objectLinearLayout.addView(linearLayoutHorizontal)
        objectCard.addView(objectLinearLayout)
    }

    val card get() = objectCard.card

    fun setId(id: Int) { objectCard.card.id = id }

    fun setListener(fn: View.OnClickListener) = objectCard.card.setOnClickListener(fn)

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
            textViewTop.tv.visibility = View.GONE
        }
        textViewBottom.text = capAlert.area
        textViewTitle.text = title
        textViewStart.text = "Start: $startTime"
        if (endTime != "") {
            textViewEnd.text = "End: $endTime"
        } else {
            textViewEnd.tv.visibility = View.GONE
        }
    }
}


