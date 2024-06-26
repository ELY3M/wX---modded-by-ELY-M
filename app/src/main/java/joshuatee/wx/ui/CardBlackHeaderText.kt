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
import android.graphics.Color
import android.view.Gravity
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize

class CardBlackHeaderText(context: Context, val title: String) : Widget {

    private val card = Card(context)
    private val text = Text(context, UIPreferences.textHighlightColor)

    init {
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        vbox.addWidget(text)
        card.addLayout(vbox)
        setTextHeader()
    }

    private fun setTextHeader() {
        with(text) {
            text = title
            setSize(TextSize.LARGE)
            setPadding(20)
            color = UIPreferences.textHighlightColor
            setBackgroundColor(Color.BLACK)
            setTextColor(Color.WHITE)
        }
    }

    override fun getView() = card.getView()
}
