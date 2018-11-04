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

package joshuatee.wx.settings

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility

internal class ObjectSettingsColorLabel(val context: Context, label: String, private val pref: String) {

    private val objCard = ObjectCard(context, R.color.black)
    private val tv = TextView(context)

    init {
        ObjectCardText.textViewSetup(tv)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        refreshColor()
        tv.setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
        tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        tv.text = label
        tv.gravity = Gravity.CENTER_VERTICAL
        val prefInner = pref
        objCard.addView(tv)
        objCard.setOnClickListener(View.OnClickListener { ObjectIntent(context, SettingsColorPickerActivity::class.java, SettingsColorPickerActivity.INFO, arrayOf(prefInner, label)) })
    }

    fun refreshColor() {
        val colorInt = Utility.readPref(context, pref, UtilityColor.setColor(pref))
        if (colorInt != Color.BLACK) {
            tv.setTextColor(colorInt)
        } else {
            tv.setTextColor(Color.WHITE)
        }
    }

    val card get() = objCard.card
}


