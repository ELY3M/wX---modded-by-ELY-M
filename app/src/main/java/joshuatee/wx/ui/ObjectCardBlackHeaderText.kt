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
import joshuatee.wx.UIPreferences

class ObjectCardBlackHeaderText(context: Context, val linearLayout: LinearLayout, val text: String) {

    private val objCard: ObjectCard
    private val textViewTop: ObjectTextView

    init {
        val linearLayoutVertical = LinearLayout(context)
        textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
        linearLayoutVertical.orientation = LinearLayout.VERTICAL
        linearLayoutVertical.gravity = Gravity.CENTER_VERTICAL
        linearLayoutVertical.addView(textViewTop.tv)
        objCard = ObjectCard(context)
        objCard.addView(linearLayoutVertical)
        linearLayout.addView(objCard.card)
        setTextHeader()
    }

    val card: CardView get() = objCard.card

    fun setListener(fn: View.OnClickListener) {
        objCard.card.setOnClickListener(fn)
    }

    private fun setTextHeader() {
        textViewTop.text = text
        textViewTop.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeLarge)
        textViewTop.setPadding(20,20,20,20)
        textViewTop.setTextColor(UIPreferences.textHighlightColor)
        textViewTop.tv.setBackgroundColor(Color.BLACK)
        textViewTop.tv.setTextColor(Color.WHITE)
    }
}


