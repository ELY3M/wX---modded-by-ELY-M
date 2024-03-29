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
import android.graphics.Typeface
import android.view.View
import joshuatee.wx.objects.TextSize
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.Utility

class CardHSText(context: Context, val product: String) : Widget {

    private val card = Card(context)
    private val text = Text(context)
    private var textShort = ""
    private var textLong = ""
    private var textShownSmall = true

    init {
        card.addWidget(text)
    }

    fun toggleText() {
        if (textShownSmall) {
            textShownSmall = false
            setText(textLong)
        } else {
            textShownSmall = true
            setText(textShort)
        }
    }

    private fun setText(s: String) {
        if (s.contains("<br>") || s.contains("<BR>")) {
            text.text = Utility.fromHtml(s)
        } else {
            text.text = s
        }
    }

    fun setup(longTextDownload: String) {
        var longText = longTextDownload
        if (product == "NFDOFFN31" || product == "NFDOFFN32") {
            longText = Utility.fromHtml(longTextDownload)
        }
        setTextLong(longText)
        val shortText = longText.take(UIPreferences.homescreenTextLength)
        setTextShort(shortText)
        setText(shortText)
        if (product == "HOURLY") {
            typefaceMono()
        }
    }

    fun refreshTextSize() {
        text.refreshTextSize(TextSize.SMALL)
    }

    private fun setTextLong(text: String) {
        textLong = text
    }

    private fun setTextShort(text: String) {
        textShort = text
    }

    fun connect(fn: View.OnClickListener) {
        text.connect(fn)
    }

    private fun typefaceMono() {
        text.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }

    override fun getView() = card.getView()
}
