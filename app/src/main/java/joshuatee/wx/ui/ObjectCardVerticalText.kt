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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

class ObjectCardVerticalText(context: Context, numColumns: Int) {

    private val objCard = ObjectCard(context)
    private var tvArr = mutableListOf<TextView>()

    init {
        val ll = LinearLayout(context)
        ll.gravity = Gravity.CENTER
        ll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        ll.orientation = LinearLayout.HORIZONTAL
        ll.isBaselineAligned = false
        objCard.addView(ll)
        (0 until numColumns).forEach {
            val llv = LinearLayout(context)
            llv.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            ll.addView(llv)
            tvArr.add(TextView(context))
            tvArr[it].gravity = Gravity.CENTER_HORIZONTAL
            tvArr[it].layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llv.addView(tvArr[it])
        }
    }

    constructor(
        context: Context,
        numColumns: Int,
        linearLayout: LinearLayout,
        toolbar: Toolbar
    ) : this(context, numColumns) {
        linearLayout.addView(card)
        setOnClickListener(View.OnClickListener {
            UtilityToolbar.showHide(
                toolbar
            )
        })
    }

    fun setText(textArr: List<String>) {
        if (textArr.size == tvArr.size) {
            (0 until textArr.size).forEach { tvArr[it].text = textArr[it] }
        }
    }

    val card: CardView get() = objCard.card

    fun setOnClickListener(fn: View.OnClickListener) {
        objCard.setOnClickListener(fn)
    }
}


