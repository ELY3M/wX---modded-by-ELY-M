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
import android.graphics.Typeface
import androidx.appcompat.widget.AppCompatTextView
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.setPadding
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize

class Text(val context: Context) : Widget {

    private var tv = AppCompatTextView(context)

    var text
        get() = tv.text.toString()
        set(value) {
            tv.text = value
        }

    var gravity
        get() = tv.gravity
        set(value) {
            tv.gravity = value
        }

    var visibility
        get() = tv.visibility
        set(value) {
            tv.visibility = value
        }

    init {
        with(tv) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)
            setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
            gravity = Gravity.START
        }
    }

    constructor(context: Context, text: String) : this(context) {
        tv.text = text
    }

    constructor(context: Context, color: Int) : this(context) {
        this.color = color
    }

    constructor(context: Context, backgroundText: Boolean) : this(context) {
        if (backgroundText) {
            setAsBackgroundText()
        }
    }

    constructor(view: View, resourceId: Int) : this(view.context) {
        tv = view.findViewById(resourceId)
    }

    constructor(view: View, resourceId: Int, backgroundText: Boolean) : this(view, resourceId) {
        tv = view.findViewById(resourceId)
        if (backgroundText) {
            setAsBackgroundText()
        }
    }

    constructor(view: View, resourceId: Int, size: TextSize) : this(view.context, size) {
        tv = view.findViewById(resourceId)
        refreshTextSize(size)
    }

    constructor(view: View, resourceId: Int, color: Int, size: TextSize) : this(
        view,
        resourceId,
        size
    ) {
        this.color = color
    }

    constructor(context: Context, size: TextSize) : this(context) {
        refreshTextSize(size)
    }

    fun refreshTextSize(size: TextSize) {
        when (size) {
            TextSize.SMALL -> tv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                UIPreferences.textSizeSmall
            )

            TextSize.MEDIUM -> tv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                UIPreferences.textSizeNormal
            )

            TextSize.LARGE -> tv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                UIPreferences.textSizeLarge
            )
        }
    }

    fun setSize(size: TextSize) {
        refreshTextSize(size)
    }

    fun setTextSize(unit: Int, size: Float) {
        tv.setTextSize(unit, size)
    }

    fun setMonoSpaced() {
        tv.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }

    var color
        get() = tv.currentTextColor
        set(newValue) {
            tv.setTextColor(newValue)
        }

    var typeface: Typeface
        get() = tv.typeface
        set(newValue) {
            tv.typeface = newValue
        }

    private fun setAsBackgroundText() {
        setSmall()
        tv.setTextColor(UIPreferences.textSmallThemeColor)
    }

    private fun setSmall() {
        tv.setTextColor(UIPreferences.backgroundColor)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeSmall)
    }

    fun wrap() {
        tv.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
    }

    fun connect(fn: View.OnClickListener) {
        tv.setOnClickListener(fn)
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        tv.setPadding(left, top, right, bottom)
    }

    fun setPadding(padding: Int) {
        tv.setPadding(padding)
    }

    fun setBackgroundColor(color: Int) {
        tv.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        tv.setTextColor(color)
    }

    override fun getView() = tv
}
