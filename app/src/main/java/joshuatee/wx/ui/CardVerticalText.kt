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
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.objects.TextSize

class CardVerticalText(context: Context, numberOfColumns: Int) {

    private val card = Card(context)
    private val textViews = mutableListOf<Text>()

    init {
        val box = HBox(context, Gravity.CENTER)
        box.matchParent()
        box.isBaselineAligned = false
        card.addView(box.get())
        repeat(numberOfColumns) {
            val hbox = HBox(context)
            hbox.wrap()
            box.addWidget(hbox.get())
            val textView = Text(context)
            textViews.add(textView)
            textView.gravity = Gravity.START
            textView.wrap()
            hbox.addWidget(textView.get())
        }
    }

    constructor(context: Context, numberOfColumns: Int, box: VBox, toolbar: Toolbar) : this(context, numberOfColumns) {
        box.addWidget(get())
        connect { UtilityToolbar.showHide(toolbar) }
    }

    fun set(list: List<String>) {
        if (list.size == textViews.size) {
            list.indices.forEach {
                textViews[it].text = list[it]
                textViews[it].refreshTextSize(TextSize.SMALL)
            }
        }
    }

    private fun get() = card.get()

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }
}
