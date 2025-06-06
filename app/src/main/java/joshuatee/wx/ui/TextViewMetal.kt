/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.widget.RelativeLayout
import android.widget.TextView
import joshuatee.wx.R

class TextViewMetal(
    context: Context,
    text: String,
    color: Int,
    textSize: Float,
    singleLine: Boolean = true,
    drawText: Boolean = true
) : Widget {

    private val textView = TextView(context)

    init {
        textView.setTextColor(color)
        textView.setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
        if (singleLine) {
            textView.setSingleLine()
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        textView.layoutParams = layoutParams
        if (drawText) {
            textView.text = text
        }
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        textView.setPadding(left, top, right, bottom)
    }

    override fun getView() = textView
}
