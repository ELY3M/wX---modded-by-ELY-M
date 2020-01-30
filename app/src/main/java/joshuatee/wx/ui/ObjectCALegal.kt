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
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent

class ObjectCALegal(context: Context, linearLayout: LinearLayout, url: String) {

    private val c1 = ObjectCardText(context)

    init {
        c1.lightText()
        c1.center()
        c1.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                context,
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        })
        c1.text = context.resources.getText(R.string.main_screen_ca_disclaimor).toString()
        linearLayout.addView(c1.card)
    }
}


