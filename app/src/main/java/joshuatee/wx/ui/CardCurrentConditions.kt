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
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.settings.Location
import joshuatee.wx.util.CurrentConditions
import joshuatee.wx.util.UtilityForecastIcon

class CardCurrentConditions(val context: Context, version: Int) : Widget {

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
            vbox.addWidgets(listOf(text1, text2, text3))
            hbox.addWidgets(listOf(photo, vbox))
        } else {
            // legacy code
            text1.gravity = Gravity.CENTER
            text3.gravity = Gravity.CENTER
            text2.gravity = Gravity.CENTER
            text1.setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
            text3.setPadding(UIPreferences.padding, 0, UIPreferences.padding, 2)
            text2.setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
            with (hbox) {
                makeVertical()
                addWidget(text1)
                addWidget(text3)
            }
        }
        card.addLayout(hbox)
    }

    override fun getView() = card.getView()

    fun refreshTextSize() {
        text1.refreshTextSize(TextSize.MEDIUM)
        text2.refreshTextSize(TextSize.SMALL)
        text3.refreshTextSize(TextSize.SMALL)
    }

    fun setStatus(s: String) {
        text3.text = s
    }

    fun setTopLine(s: String) {
        text1.text = s
    }

    private fun setMiddleLine(s: String) {
        text2.text = s
    }

    fun connect(objectDialogue: ObjectDialogue?, dialogueItems: MutableList<String>, radarTimestamps: () -> List<String>) {
        photo.connect {
            with (dialogueItems) {
                clear()
                add("Edit Location...")
                add("Force Data Refresh...")
                if (UIPreferences.isNexradOnMainScreen && Location.isUS) {
                    add("Radar type: Reflectivity")
                    add("Radar type: Velocity")
                    add("Reset zoom and center")
                    addAll(radarTimestamps())
                }
            }
            objectDialogue?.show()
        }
    }

    fun update(objCc: CurrentConditions, isUS: Boolean, radarTime: String = "") {
        if (isUS) {
            photo.set(UtilityForecastIcon.getIcon(context, objCc.iconUrl))
        } else {
            photo.set(UtilityForecastIcon.getIcon(context, UtilityCanada.translateIconNameCurrentConditions(objCc.data, objCc.status)))
        }
        val sep = " - "
        val conditionTokens = objCc.data.split(sep).dropLastWhile { it.isEmpty() }
        if (conditionTokens.size > 4 && isUS) {
            val items = conditionTokens[0].split("/").dropLastWhile { it.isEmpty() }
            setTopLine(conditionTokens[4].replace("^ ".toRegex(), "") + " " + items[0] + conditionTokens[2])
            setMiddleLine(items[1].replace("^ ".toRegex(), "") + sep + conditionTokens[1] + sep + conditionTokens[3])
            setStatus(objCc.status + radarTime)
        } else {
            val items = conditionTokens[0].split("/").dropLastWhile { it.isEmpty() }
            setTopLine(conditionTokens[4] + "" + items[0] + conditionTokens[2])
            setMiddleLine(items[1].replace("^ ".toRegex(), "") + sep + conditionTokens[1] + sep + conditionTokens[3])
            setStatus(objCc.status.replace("^ ".toRegex(), "") + radarTime)
        }
    }
}
