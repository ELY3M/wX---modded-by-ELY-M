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
import android.graphics.Bitmap
import android.view.Gravity
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.settings.Location
import joshuatee.wx.util.ObjectCurrentConditions

class ObjectCardCurrentConditions(context: Context, version: Int) {

    private val card = Card(context)
    private val photo = Photo(context)
    private val text1 = Text(context, TextSize.MEDIUM)
    private val text2 = Text(context, backgroundText = true)
    private val text3 = Text(context, backgroundText = true)

    init {
        val hbox = HBox(context)
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        if (version == 2) {
            text1.setPadding(UIPreferences.padding, 0, UIPreferences.paddingSmall, 0)
            text2.setPadding(UIPreferences.padding, 0, UIPreferences.paddingSmall, 0)
            text3.setPadding(UIPreferences.padding, 0, UIPreferences.paddingSmall, UIPreferences.paddingSmall)
            vbox.addViews(listOf(text1.get(), text2.get(), text3.get()))
            hbox.addViews(listOf(photo.get(), vbox.get()))
        } else {
            // legacy code
            text1.gravity = Gravity.CENTER
            text3.gravity = Gravity.CENTER
            text2.gravity = Gravity.CENTER
            text1.setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
            text3.setPadding(UIPreferences.padding, 0, UIPreferences.padding, 2)
            text2.setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
            hbox.makeVertical()
            hbox.addWidget(text1.get())
            hbox.addWidget(text3.get())
        }
        card.addView(hbox.get())
    }

    fun get() = card.get()

    fun refreshTextSize() {
        text1.refreshTextSize(TextSize.MEDIUM)
        text2.refreshTextSize(TextSize.SMALL)
        text3.refreshTextSize(TextSize.SMALL)
    }

    fun setStatus(text: String) {
        text3.text = text
    }

    fun setTopLine(text: String) {
        text1.text = text
    }

    private fun setMiddleLine(text: String) {
        text2.text = text
    }

    fun connect(alertDialogStatus: ObjectDialogue?, alertDialogStatusAl: MutableList<String>, radarTimestamps: () -> List<String>) {
        photo.connect {
            alertDialogStatusAl.clear()
            alertDialogStatusAl.add("Edit Location...")
            alertDialogStatusAl.add("Force Data Refresh...")
            if (UIPreferences.locDisplayImg && Location.isUS) {
                alertDialogStatusAl.add("Radar type: Reflectivity")
                alertDialogStatusAl.add("Radar type: Velocity")
                alertDialogStatusAl.add("Reset zoom and center")
                alertDialogStatusAl += radarTimestamps()
            }
            alertDialogStatus?.show()
        }
    }

    fun updateContent(bitmap: Bitmap, objCc: ObjectCurrentConditions, isUS: Boolean, time: String, radarTime: String) {
        photo.setImage(bitmap)
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
