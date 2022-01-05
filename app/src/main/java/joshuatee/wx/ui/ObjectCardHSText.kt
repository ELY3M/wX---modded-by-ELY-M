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
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.util.Utility

class ObjectCardHSText(context: Context, val product: String) {

    private val objectCard = ObjectCard(context)
    private val textView: TextView = TextView(context)
    private var textShort = ""
    private var textLong = ""
    private var textShownSmall = true

    init {
        ObjectCardText.textViewSetup(textView)
        textView.setTextIsSelectable(true)
        textView.isFocusable = false
        objectCard.addView(textView)
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

    fun setText(text: String) {
        if (text.contains("<br>") || text.contains("<BR>")) {
            textView.text = Utility.fromHtml(text)
        } else {
            textView.text = text
        }
    }

    fun setup(longTextDownload: String) {
        var longText = longTextDownload
        if (product=="NFDOFFN31" || product=="NFDOFFN32") {
            longText = Utility.fromHtml(longTextDownload)
        }
        setTextLong(longText)
        val shortText = UtilityStringExternal.truncate(longText, UIPreferences.homescreenTextLength)
        setTextShort(shortText)
        setText(shortText)
        if (product == "HOURLY") {
            typefaceMono()
        }
    }

    fun refreshTextSize() = textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)

    private fun setTextLong(text: String) { textLong = text }

    private fun setTextShort(text: String) { textShort = text }

    fun setOnClickListener(fn: View.OnClickListener) = textView.setOnClickListener(fn)

    private fun typefaceMono() { textView.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) }

    val card get() = objectCard.card
}


