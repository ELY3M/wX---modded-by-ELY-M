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
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.objects.TextSize

class CardVerticalText2(val context: Context) : Widget {

    private val card = Card(context)
    private val box = VBox(context, Gravity.CENTER)

    init {
        box.matchParent()
        card.addLayout(box)
    }

    constructor(context: Context, toolbar: Toolbar) : this(
        context,
    ) {
        connect { UtilityToolbar.showHide(toolbar) }
    }

    fun set(list: List<String>) {
        box.removeChildren()
        list.forEach {
            val text = Text(context, it)
            text.setMonoSpaced()
            text.setSize(TextSize.SMALL)
            box.addWidget(text)
        }
    }

    override fun getView() = card.getView()

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }
}
