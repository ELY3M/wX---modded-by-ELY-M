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

package joshuatee.wx.settings

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import joshuatee.wx.R
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog

class ObjectSettingsSpinner(context: Context, label: String, pref: String, prefInit: String, strId: Int, spinnerArr: List<String>) {

    private val objectCard = ObjectCard(context)

    init {
        val objectTextView = ObjectTextView(context)
        objectTextView.setPadding(UIPreferences.paddingSettings)
        objectTextView.get().layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        objectTextView.text = label
        objectTextView.gravity = Gravity.CENTER_VERTICAL
        objectTextView.setOnClickListener { ObjectDialogue(context, context.resources.getString(strId)) }
        val objectLinearLayout = ObjectLinearLayout(context, LinearLayout.HORIZONTAL, Gravity.CENTER_VERTICAL)
        objectLinearLayout.matchParent()
        objectLinearLayout.addView(objectTextView.get())
        val spinner = Spinner(context)
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
                        UtilityAlertDialog.restart()
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
        objectLinearLayout.addView(spinner)
        objectCard.addView(objectLinearLayout.linearLayout)
    }

    val card get() = objectCard.get()

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
