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

package joshuatee.wx.nhc

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectTextView

class ObjectCardNhcStormReportItem(context: Context, linearLayout: LinearLayout, stormData: ObjectNhcStormDetails) {

    private val objCard: ObjectCard
    private val textViewTop: ObjectTextView
    private val textViewTime: ObjectTextView
    private val textViewTitle: ObjectTextView
    private val textViewBottom: ObjectTextView

    init {
        val linearLayoutVertical = LinearLayout(context)
        textViewTop = ObjectTextView(context, Color.BLUE)
        textViewTime = ObjectTextView(context)
        textViewTitle = ObjectTextView(context)
        textViewBottom = ObjectTextView(context)
        textViewBottom.setAsBackgroundText()
        linearLayoutVertical.orientation = LinearLayout.VERTICAL
        linearLayoutVertical.gravity = Gravity.CENTER_VERTICAL
        linearLayoutVertical.addView(textViewTop.tv)
        linearLayoutVertical.addView(textViewTime.tv)
        linearLayoutVertical.addView(textViewTitle.tv)
        linearLayoutVertical.addView(textViewBottom.tv)
        objCard = ObjectCard(context)
        objCard.addView(linearLayoutVertical)
        textViewTop.text = stormData.name + " (" + stormData.type + ") " + stormData.center
        textViewTime.text = stormData.dateTime
        textViewTitle.text = stormData.movement + ", " + stormData.wind + ", " + stormData.pressure
        textViewBottom.text = stormData.headline + " " + stormData.wallet + " " + stormData.atcf
        linearLayout.addView(objCard.card)
    }

    val card: CardView get() = objCard.card

    fun setListener(fn: View.OnClickListener) {
        objCard.card.setOnClickListener(fn)
    }
}


