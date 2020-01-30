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
import android.util.TypedValue
import android.widget.TextView
import androidx.cardview.widget.CardView
import joshuatee.wx.MyApplication

import joshuatee.wx.util.Utility

class ObjectCardHSText(context: Context, val product: String) {

    private val objCard = ObjectCard(context)
    val tv: TextView = TextView(context)
    private var textShort = ""
    private var textLong = ""
    private var textShownSmall = true

    init {
        ObjectCardText.textViewSetup(tv)
        tv.setTextIsSelectable(true)
        tv.isFocusable = false
        objCard.addView(tv)
    }

    fun toggleText() {
        if (textShownSmall) {
            textShownSmall = false
            setText(textLong)
        } else {
            textShownSmall = true
            setText(textShort)
        }
    }

    fun setText(text: String) {
        if (text.contains("<br>") || text.contains("<BR>")) {
            tv.text = Utility.fromHtml(text)
        } else {
            tv.text = text
        }
    }

    fun refreshTextSize() {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
    }

    fun setTextLong(text: String) {
        textLong = text
    }

    fun setTextShort(text: String) {
        textShort = text
    }

    val card: CardView get() = objCard.card
}


