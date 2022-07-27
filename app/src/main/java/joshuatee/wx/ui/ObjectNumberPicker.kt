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
//modded by ELY M.
//hail text size 

package joshuatee.wx.ui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.Utility

internal class ObjectNumberPicker(
        context: Context,
        val label: String,
        pref: String,
        strId: Int,
        private val defValue: Int,
        private val lowValue: Int,
        highValue: Int
) {

    private val card = Card(context)
    private val initValue = when (pref) {
        "RADAR_TEXT_SIZE" -> (Utility.readPrefFloat(context, pref, defValue.toFloat()) * 10).toInt()
        "RADAR_HI_TEXT_SIZE" -> (Utility.readPrefFloat(context, pref, defValue.toFloat()) * 10).toInt()
        "UI_ANIM_ICON_FRAMES" -> (Utility.readPref(context, pref, RadarPreferences.uiAnimIconFrames)).toIntOrNull() ?: 0
        "CARD_CORNER_RADIUS" -> Utility.readPrefInt(context, pref, 0)
        else -> Utility.readPrefInt(context, pref, defValue)
    }
    private val text = Text(context)
    private val seekBar = SeekBar(context)

    init {
        text.setPadding(UIPreferences.padding)
        text.wrap()
        text.gravity = Gravity.TOP
        text.connect { ObjectDialogue(context, context.resources.getString(strId)) }
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        vbox.matchParent()
        vbox.addWidget(text.get())
        seekBar.max = highValue - lowValue
        seekBar.progress = convert(initValue)
        val padding = 30
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(padding, padding, padding, padding)
        seekBar.layoutParams = layoutParams
        vbox.addWidget(seekBar)
        card.addView(vbox.get())
        updateLabel()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (pref == "TEXTVIEW_FONT_SIZE") {
                    text.get().setTextSize(TypedValue.COMPLEX_UNIT_PX, UtilityUI.spToPx(convertForSave(seekBar.progress), context))
                }
                updateLabel()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val newVal = convertForSave(seekBar.progress)
                when (pref) {
                    "RADAR_TEXT_SIZE" -> Utility.writePrefFloat(context, pref, newVal / 10.0f)
                    "RADAR_HI_TEXT_SIZE" -> Utility.readPrefFloat(context, pref, newVal / 10.0f)
                    "UI_ANIM_ICON_FRAMES" -> Utility.writePref(context, pref, newVal.toString())
                    else -> Utility.writePrefInt(context, pref, newVal)
                }
                Utility.writePref(context, "RESTART_NOTIF", "true")
            }
        })
    }

    private fun convert(value: Int) = value - lowValue

    private fun convertForSave(value: Int) = value + lowValue

    fun updateLabel() {
        text.text = label + " (default is " + defValue.toString() + "): " + convertForSave(seekBar.progress).toString()
    }

    fun get() = card.get()
}
