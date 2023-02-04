/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityColor
import joshuatee.wx.util.Utility

internal class ColorLabel(val context: Context, label: String, private val pref: String) : Widget {

    private val card = Card(context, R.color.black)
    private val text = Text(context)

    init {
        refreshColor()
        with (text) {
            setPadding(UIPreferences.paddingSettings)
            wrap()
            text = label
            setBackgroundColor(ContextCompat.getColor(context, R.color.black))
            gravity = Gravity.CENTER_VERTICAL
            card.addWidget(this)
        }
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black))
        card.connect { Route.colorPicker(context, pref, label) }
    }

    fun refreshColor() {
        val colorInt = Utility.readPrefInt(context, pref, UtilityColor.setColor(pref))
        text.color = if (colorInt != Color.BLACK) colorInt else Color.WHITE
    }

    override fun getView() = card.getView()
}
