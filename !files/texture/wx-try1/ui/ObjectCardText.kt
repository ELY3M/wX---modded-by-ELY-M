/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.audio.UtilityTTSTranslations

class ObjectCardText(private val context: Context) {

    private val objCard = ObjectCard(context)
    val tv: TextView = TextView(context)

    init {
        textViewSetup(tv)
        tv.setTextIsSelectable(true)
        tv.isFocusable = false
        objCard.addView(tv)
    }

    constructor(context: Context, text: String) : this(context) {
        tv.text = text
        tv.isFocusable = false
    }

    constructor(context: Context, text: String, textSize: Float) : this(context, text) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
    }

    fun setTextAndTranslate(text: String) {
        val localText = if (UIPreferences.translateText) {
            UtilityTTSTranslations.tranlasteAbbrevForVisual(text)
        } else {
            text
        }
        tv.text = localText
    }

    fun setText(text: String) {
        tv.text = text
    }

    fun setText(text: CharSequence) {
        tv.text = text.toString()
    }

    fun center() {
        tv.gravity = Gravity.CENTER
    }

    fun setTextColor(color: Int) {
        tv.setTextColor(color)
    }

    fun setTextSize(type: Int, size: Float) {
        tv.setTextSize(type, size)
    }

    fun lightText() {
        tv.setTextAppearance(context, UIPreferences.smallTextTheme)
    }

    val card: CardView get() = objCard.card

    fun setVisibility(visibility: Int) {
        objCard.setVisibility(visibility)
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        tv.setOnClickListener(fn)
    }

    fun setId(id: Int) {
        tv.id = id
    }

    companion object {
        fun textViewSetup(tvTmp: TextView) {
            tvTmp.setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
            tvTmp.gravity = Gravity.START
            tvTmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
            tvTmp.setTextColor(UIPreferences.backgroundColor)
        }
    }
}


