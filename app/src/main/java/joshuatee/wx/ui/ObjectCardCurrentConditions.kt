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
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.LinearLayout

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.TextSize
import joshuatee.wx.settings.Location
import joshuatee.wx.util.ObjectCurrentConditions

class ObjectCardCurrentConditions(context: Context, version: Int) {

    private val objCard = ObjectCard(context)
    private val objectImageView = ObjectImageView(context)
    private val textViewTop = ObjectTextView(context, TextSize.MEDIUM)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)
    private val textViewMiddle = ObjectTextView(context, backgroundText = true)

    init {
        val linearLayoutHorizontal = ObjectLinearLayout(context, LinearLayout.HORIZONTAL)
        val linearLayoutVertical = ObjectLinearLayout(context, LinearLayout.VERTICAL, Gravity.CENTER_VERTICAL)
        if (version == 2) {
            textViewTop.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, 0)
            textViewMiddle.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, 0)
            textViewBottom.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, MyApplication.paddingSmall)
            linearLayoutVertical.addViews(listOf(textViewTop.tv, textViewMiddle.tv, textViewBottom.tv))
            linearLayoutHorizontal.addViews(listOf(objectImageView.imageView, linearLayoutVertical.linearLayout))
        } else {
            // legacy code
            textViewTop.gravity = Gravity.CENTER
            textViewBottom.gravity = Gravity.CENTER
            textViewMiddle.gravity = Gravity.CENTER
            textViewTop.setPadding(MyApplication.padding, 0, MyApplication.padding, 0)
            textViewBottom.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
            textViewMiddle.setPadding(MyApplication.padding, 0, MyApplication.padding, 0)
            linearLayoutHorizontal.orientation = LinearLayout.VERTICAL
            linearLayoutHorizontal.addView(textViewTop)
            linearLayoutHorizontal.addView(textViewBottom)
        }
        objCard.addView(linearLayoutHorizontal)
    }

    val card get() = objCard.card

    fun refreshTextSize() {
        textViewTop.refreshTextSize(TextSize.MEDIUM)
        textViewMiddle.refreshTextSize(TextSize.SMALL)
        textViewBottom.refreshTextSize(TextSize.SMALL)
    }

    fun setStatus(text: String) { textViewBottom.text = text }

    fun setTopLine(text: String) { textViewTop.text = text }

    private fun setMiddleLine(text: String) { textViewMiddle.text = text }

    fun setListener(alertDialogStatus: ObjectDialogue?, alertDialogStatusAl: MutableList<String>, radarTimestamps: () -> List<String>) {
        objectImageView.imageView.setOnClickListener {
            alertDialogStatusAl.clear()
            alertDialogStatusAl.add("Edit Location...")
            alertDialogStatusAl.add("Force Data Refresh...")
            if (MyApplication.locDisplayImg && Location.isUS) {
                alertDialogStatusAl.add("Radar type: Reflectivity")
                alertDialogStatusAl.add("Radar type: Velocity")
                alertDialogStatusAl.add("Reset zoom and center")
                alertDialogStatusAl += radarTimestamps()
            }
            alertDialogStatus?.show()
        }
    }

    fun updateContent(bitmap: Bitmap, objCc: ObjectCurrentConditions, isUS: Boolean, time: String, radarTime: String) {
        objectImageView.setImage(bitmap)
        val sep = " - "
        val conditionTokens = objCc.data.split(sep).dropLastWhile { it.isEmpty() }
        if (conditionTokens.size > 4 && isUS) {
            val items = conditionTokens[0].split("/").dropLastWhile { it.isEmpty() }
            setTopLine(conditionTokens[4].replace("^ ".toRegex(), "") + " " + items[0] + conditionTokens[2])
            setMiddleLine(items[1].replace("^ ".toRegex(), "") + sep + conditionTokens[1] + sep + conditionTokens[3])
            setStatus(time + radarTime)
        } else {
            val items = conditionTokens[0].split("/").dropLastWhile { it.isEmpty() }
            setTopLine(conditionTokens[4] + "" + items[0] + conditionTokens[2])
            setMiddleLine(items[1].replace("^ ".toRegex(), "") + sep + conditionTokens[1] + sep + conditionTokens[3])
            setStatus(time.replace("^ ".toRegex(), "") + radarTime)
        }
    }
}


