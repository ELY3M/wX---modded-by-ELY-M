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
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import com.google.android.material.button.MaterialButton
import joshuatee.wx.Extensions.setPadding
import joshuatee.wx.util.Utility

class ObjectButton(context: Context, title: String, icon: Int) {

    companion object {
        private const val padding = 15
    }

    private val button = MaterialButton(context)

    init {
        button.text = title
        button.setIconResource(icon)
        button.setBackgroundColor(Color.TRANSPARENT)
        button.setPadding(padding)
        if (Utility.isThemeAllWhite()) {
            button.iconTintMode = PorterDuff.Mode.DARKEN
        }
        if (Utility.isThemeAllBlack()) {
            button.iconTintMode = PorterDuff.Mode.LIGHTEN
        }
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        button.setOnClickListener(fn)
    }

    val card get() = button
}


