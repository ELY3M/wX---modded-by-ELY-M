/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.CAPAlert
import joshuatee.wx.objects.TextSize
import joshuatee.wx.util.UtilityString

class ObjectCardAlertSummaryItem(context: Context) {

    private val objCard: ObjectCard
    private val textViewTop: ObjectTextView
    private val textViewTitle: ObjectTextView
    private val textViewStart: ObjectTextView
    private val textViewEnd: ObjectTextView
    private val textViewBottom: ObjectTextView

    init {
        val linearLayoutVertical = LinearLayout(context)
        textViewTop = ObjectTextView(context, Color.BLUE)
        textViewTitle = ObjectTextView(context)
        textViewStart = ObjectTextView(context, TextSize.SMALL)
        textViewEnd = ObjectTextView(context, TextSize.SMALL)
        textViewBottom = ObjectTextView(context)
        textViewBottom.setAsBackgroundText()
        linearLayoutVertical.orientation = LinearLayout.VERTICAL
        linearLayoutVertical.gravity = Gravity.CENTER_VERTICAL
        linearLayoutVertical.addView(textViewTop.tv)
        linearLayoutVertical.addView(textViewTitle.tv)
        linearLayoutVertical.addView(textViewStart.tv)
        linearLayoutVertical.addView(textViewEnd.tv)
        linearLayoutVertical.addView(textViewBottom.tv)
        objCard = ObjectCard(context)
        objCard.addView(linearLayoutVertical)
    }


    val card: CardView get() = objCard.card

    fun setId(id: Int) {
        objCard.card.id = id
    }

    fun setListener(fn: View.OnClickListener) {
        objCard.card.setOnClickListener(fn)
    }

    fun setTextFields(nwsOffice: String, nwsLoc: String, ca: CAPAlert) {
        val title: String
        val startTime: String
        var endTime = ""
        if (ca.title.contains("expiring")) {
            val tmpArr = UtilityString.parseMultipe(
                ca.title,
                "(.*?) issued (.*?) expiring (.*?) by (.*?)$",
                4
            )
            title = tmpArr[0]
            startTime = tmpArr[1]
            endTime = tmpArr[2]
        } else {
            val tmpArr =
                UtilityString.parseMultipe(ca.title, "(.*?) issued (.*?) by (.*?)$", 3)
            title = tmpArr[0]
            startTime = tmpArr[1]
        }
        textViewTop.text = "$nwsOffice ($nwsLoc)"
        textViewBottom.text = ca.area
        textViewTitle.text = title
        textViewStart.text = startTime
        textViewEnd.text = endTime
    }
}


