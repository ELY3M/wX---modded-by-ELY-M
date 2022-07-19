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
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.Utility

object UtilityTheme {

    val primaryColorFromSelectedTheme: Int
        get() = UIPreferences.primaryColor

    fun getPrimaryColorFromSelectedTheme(context: Context, color: Int): Int {
        val attrs = intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent)
        val ta = context.theme.obtainStyledAttributes(attrs)
        val primaryColor = ta.getColor(color, Color.BLACK) //1 index for primaryColorDark
        ta.recycle()
        return primaryColor
    }

    fun setPrimaryColor(context: Context) {
        val attrs = intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent)
        val ta = context.theme.obtainStyledAttributes(attrs)
        if (UIPreferences.themeInt != R.style.MyCustomTheme_mixedBlue_NOAB && !UIPreferences.themeIsWhite) {
            UIPreferences.primaryColor = ta.getColor(0, Color.BLACK) //1 index for primaryColorDark
        } else {
            UIPreferences.primaryColor = ta.getColor(2, Color.BLACK) //1 index for primaryColorDark
        }
        Utility.writePref(context, "MYAPP_PRIMARY_COLOR", UIPreferences.primaryColor)
        ta.recycle()
    }
}
