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
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.SevereWarning

class ObjectCardDashAlertItem(context: Context, val linearLayout: LinearLayout, private val severeWarning: SevereWarning, val index: Int) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTitle = ObjectTextView(context)
    private val textViewStart = ObjectTextView(context)
    private val textViewEnd = ObjectTextView(context)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)
    val radarButton = ObjectButton(context,"Radar", MyApplication.ICON_RADAR)
    val detailsButton = ObjectButton(context,"Details", MyApplication.ICON_CURRENT)

    init {
        val linearLayoutVertical = ObjectLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER_VERTICAL)
        listOf(textViewTop, textViewTitle, textViewStart, textViewEnd, textViewBottom).forEach {
            linearLayoutVertical.addView(it)
        }
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val linearLayoutHorizontal = LinearLayout(context)
        linearLayoutHorizontal.layoutParams = layoutParams
        linearLayoutHorizontal.addView(radarButton.card)
        linearLayoutHorizontal.addView(detailsButton.card)
        linearLayoutVertical.addView(linearLayoutHorizontal)
        objectCard.addView(linearLayoutVertical)
        setTextFields()
        linearLayout.addView(objectCard.card)
    }

    val card get() = objectCard.card

    fun setListener(fn: View.OnClickListener) = objectCard.card.setOnClickListener(fn)

    private fun setTextFields() {
        textViewTop.text = severeWarning.senderNameList[index]
        textViewTitle.text = severeWarning.eventList[index]
        textViewStart.text = severeWarning.effectiveList[index].replace("T", " ").replace(Regex(":00-0[0-9]:00"), "").replace(Regex(":00-10:00"), "")
        textViewEnd.text = severeWarning.expiresList[index].replace("T", " ").replace(Regex(":00-0[0-9]:00"), "").replace(Regex(":00-10:00"), "")
        textViewBottom.text = severeWarning.areaDescList[index]
    }

    fun setId(id: Int) { objectCard.card.id = id }
}


