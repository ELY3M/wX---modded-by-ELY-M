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

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences

class FabExtended(
    activity: Activity,
    resId: Int,
    iconID: Int,
    text: String,
    fn: View.OnClickListener
) {

    private val fab: ExtendedFloatingActionButton = activity.findViewById(resId)
    private val context: Context = activity

    init {
        setupFab(iconID, text)
        connect(fn)
    }

    fun connect(fn: View.OnClickListener) {
        fab.setOnClickListener(fn)
    }

    var visibility
        get() = fab.visibility
        set(newValue) {
            when (newValue) {
                View.GONE -> fab.hide()
                View.VISIBLE -> fab.show()
            }
        }

    var text: CharSequence
        get() = fab.text
        set(newValue) {
            fab.text = newValue
        }

    private fun setupFab(icon: Int, text: String) {
        if (UIPreferences.themeIsWhite) {
            fab.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent))
            fab.setTextColor(
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.off_white))
            )
            fab.iconTint =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.off_white))
        } else {
            fab.backgroundTintList =
                ColorStateList.valueOf(UtilityTheme.getPrimaryColorFromSelectedTheme(context, 2))
        }
        set(icon)
        fab.text = text
        fab.elevation = UIPreferences.fabElevation
        fab.translationZ = UIPreferences.fabElevationDepressed
    }

    fun set(resourceDrawable: Int) {
        val drawable = ContextCompat.getDrawable(context, resourceDrawable)
        fab.icon = drawable
    }
}
