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
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import joshuatee.wx.settings.UIPreferences

class ObjectPopupMessage(context: Context, view: View, message: String) {

    init {
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        snack.setActionTextColor(Color.YELLOW)
        snack.setAction("DISMISS") { snack.dismiss() }
        val viewSnack = snack.view
        val textView = viewSnack.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        val fgColor = Color.WHITE
//        val bgColor = Color.BLACK
        textView.setTextColor(fgColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)
//        viewSnack.setBackgroundColor(bgColor)
//        viewSnack.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        snack.show()
    }
}
