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
import androidx.appcompat.widget.AppCompatTextView
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import joshuatee.wx.Extensions.setPadding

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.TextSize

class ObjectTextView(val context: Context) {

    var tv = AppCompatTextView(context)

    var text
        get() = tv.text.toString()
        set(value) { tv.text = value }

    var gravity
        get() = tv.gravity
        set(value) { tv.gravity = value }

    init {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        tv.setPadding(MyApplication.padding, 0, MyApplication.padding, 0)
        tv.gravity = Gravity.START
    }

    constructor(context: Context, text: String) : this(context) { tv.text = text }

    constructor(context: Context, color: Int) : this(context) { this.color = color }

    constructor(context: Context, backgroundText: Boolean) : this(context) {
        if (backgroundText) {
            setAsBackgroundText()
        }
    }

    constructor(view: View, resourceId: Int) : this(view.context) { tv = view.findViewById(resourceId) }

    constructor(view: View, resourceId: Int, backgroundText: Boolean) : this(view, resourceId) {
        tv = view.findViewById(resourceId)
        if (backgroundText) {
            setAsBackgroundText()
        }
    }

    constructor(view: View, resourceId: Int, color: Int) : this(view, resourceId) { this.color = color }

    constructor(view: View, resourceId: Int, size: TextSize) : this(view.context, size) {
        tv = view.findViewById(resourceId)
        refreshTextSize(size)
    }

    constructor(view: View, resourceId: Int, color: Int, size: TextSize) : this(view, resourceId, size) { this.color = color }

    constructor(context: Context, size: TextSize) : this(context) { refreshTextSize(size) }

    fun refreshTextSize(size: TextSize) {
        when (size) {
            TextSize.SMALL -> tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
            TextSize.MEDIUM -> tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
            TextSize.LARGE -> tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeLarge)
        }
    }

    fun setTextSize(size: TextSize): Unit = refreshTextSize(size)

    var color
        get() = tv.currentTextColor
        set(newValue) { tv.setTextColor(newValue) }

    private fun setAsBackgroundText() {
        setAsSmallText()
        tv.setTextColor(UIPreferences.textSmallThemeColor)
    }

    private fun setAsSmallText() {
        tv.setTextColor(UIPreferences.backgroundColor)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
    }

    fun setOnClickListener(fn: View.OnClickListener): Unit = tv.setOnClickListener(fn)

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int): Unit = tv.setPadding(left, top, right, bottom)

    fun setPadding(padding: Int): Unit = tv.setPadding(padding)
}
