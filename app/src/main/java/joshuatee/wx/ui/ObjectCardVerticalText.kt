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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.MyApplication

class ObjectCardVerticalText(context: Context, numberOfColumns: Int) {

    private val objectCard = ObjectCard(context)
    private val textViews = mutableListOf<TextView>()

    init {
        val objectLinearLayout = ObjectLinearLayout(context, LinearLayout.HORIZONTAL, Gravity.CENTER)
        objectLinearLayout.linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        objectLinearLayout.linearLayout.isBaselineAligned = false
        objectCard.addView(objectLinearLayout)
        (0 until numberOfColumns).forEach {_ ->
            val linearLayout = LinearLayout(context)
            linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            objectLinearLayout.linearLayout.addView(linearLayout)
            val textView = TextView(context)
            textViews.add(textView)
            textView.gravity = Gravity.START
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            linearLayout.addView(textView)
        }
    }

    constructor(context: Context, numberOfColumns: Int, linearLayout: LinearLayout, toolbar: Toolbar) : this(context, numberOfColumns) {
        linearLayout.addView(card)
        setOnClickListener { UtilityToolbar.showHide(toolbar) }
    }

    fun setText(list: List<String>) {
        if (list.size == textViews.size) {
            list.indices.forEach {
                textViews[it].text = list[it]
                textViews[it].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
            }
        }
    }

    val card get() = objectCard.card

    fun setOnClickListener(fn: View.OnClickListener) = objectCard.setOnClickListener(fn)
}
