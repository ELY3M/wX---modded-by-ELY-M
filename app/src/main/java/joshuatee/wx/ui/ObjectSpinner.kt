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
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.Utility

class ObjectSpinner(
    context: Context,
    label: String,
    pref: String,
    prefInit: String,
    strId: Int,
    spinnerArr: List<String>
) : Widget {

    private val card = Card(context)

    init {
        val text = Text(context)
        with(text) {
            setPadding(UIPreferences.paddingSettings)
            wrap()
            text.text = label
            gravity = Gravity.CENTER_VERTICAL
            connect { ObjectDialogue(context, context.resources.getString(strId)) }
        }
        val hbox = HBox(context, Gravity.CENTER_VERTICAL)
        hbox.matchParent()
        hbox.addWidget(text)
        val spinner = AppCompatSpinner(context)
        if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
            setupSpinner(spinner, false)
        }
        val dataAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, spinnerArr)
        dataAdapter.setDropDownViewResource(UIPreferences.spinnerLayout)
        spinner.adapter = dataAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                var newValue = spinner.selectedItem.toString()
                if (pref == "WIDGET_LOCATION") {
                    newValue = newValue.split(":".toRegex()).dropLastWhile { it.isEmpty() }[0]
                }
                Utility.writePref(context, pref, newValue)
                if (pref == "THEME_BLUE") {
                    if (UIPreferences.themeStr != newValue) {
                        Utility.commitPref(context)
                        Utility.restart()
                    }
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
        var value = Utility.readPref(context, pref, prefInit)
        if (pref == "WIDGET_LOCATION") {
            value += ": " + Utility.readPref(context, "LOC" + value + "_LABEL", "").take(20)
        }
        spinner.setSelection(dataAdapter.getPosition(value))
        hbox.addWidget(spinner)
        card.addLayout(hbox)
    }

    override fun getView() = card.getView()

    companion object {

        fun setupSpinner(spinner: Spinner, light: Boolean) {
            val tint = if (light) {
                ColorStateList.valueOf(UIPreferences.colorOffwhiteToolbar)
            } else {
                ColorStateList.valueOf(UIPreferences.colorBlack)
            }
            spinner.backgroundTintList = tint
        }
    }
}
