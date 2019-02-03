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
import androidx.appcompat.widget.AppCompatTextView
import android.util.TypedValue
import android.view.Gravity
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.TextSize

class ObjectTextView(val context: Context) {

    var tv: AppCompatTextView

    var text: String
        get() = tv.text.toString()
        set(value) {
            tv.text = value
        }

    var gravity: Int
        get() = tv.gravity
        set(value) {
            tv.gravity = value
        }

    var maxLines: Int
        get() = tv.maxLines
        set(value) {
            tv.maxLines = value
        }

    init {
        tv = AppCompatTextView(context)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        tv.setPadding(MyApplication.padding, 0, MyApplication.padding, 0)
        tv.gravity = Gravity.START
    }

    constructor(context: Context, text: String) : this(context) {
        tv.text = text
    }

    constructor(context: Context, color: Int) : this(context) {
        setTextColor(color)
    }

    constructor(view: View, resId: Int) : this(view.context) {
        tv = view.findViewById(resId)
    }

    constructor(view: View, resId: Int, color: Int) : this(view, resId) {
        setTextColor(color)
    }

    constructor(view: View, resId: Int, size: TextSize) : this(view.context, size) {
        tv = view.findViewById(resId)
    }

    constructor(context: Context, size: TextSize) : this(context) {
        when (size) {
            TextSize.SMALL -> tv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                MyApplication.textSizeSmall
            )
            TextSize.MEDIUM -> tv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                MyApplication.textSizeNormal
            )
            TextSize.LARGE -> tv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                MyApplication.textSizeLarge
            )
        }
    }

    fun setTextColor(color: Int) {
        tv.setTextColor(color)
    }

    fun setTextSize(unit: Int, size: Float) {
        tv.setTextSize(unit, size)
    }

    fun setAsBackgroundText() {
        setAsSmallText()
        tv.setTextAppearance(context, UIPreferences.smallTextTheme)
    }

    fun setAsSmallText() {
        tv.setTextColor(UIPreferences.backgroundColor)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        tv.setOnClickListener(fn)
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        tv.setPadding(left, top, right, bottom)
    }
}


