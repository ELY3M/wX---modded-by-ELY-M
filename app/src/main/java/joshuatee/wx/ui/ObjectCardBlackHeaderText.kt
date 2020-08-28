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

class ObjectCardBlackHeaderText(context: Context, val linearLayout: LinearLayout, val text: String) {

    private val objectCard = ObjectCard(context)
    private val objectTextView = ObjectTextView(context, UIPreferences.textHighlightColor)

    init {
        val objectLinearLayout = ObjectLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER_VERTICAL)
        objectLinearLayout.addView(objectTextView)
        objectCard.addView(objectLinearLayout)
        linearLayout.addView(objectCard.card)
        setTextHeader()
    }

    val card get() = objectCard.card

    fun setListener(fn: View.OnClickListener) = objectCard.card.setOnClickListener(fn)

    private fun setTextHeader() {
        objectTextView.text = text
        objectTextView.setTextSize(TextSize.LARGE)
        objectTextView.setPadding(20)
        objectTextView.color = UIPreferences.textHighlightColor
        objectTextView.tv.setBackgroundColor(Color.BLACK)
        objectTextView.tv.setTextColor(Color.WHITE)
    }
}


