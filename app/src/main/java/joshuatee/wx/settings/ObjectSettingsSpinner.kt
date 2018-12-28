/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog

class ObjectSettingsSpinner(
    context: Context,
    private val activity: Activity,
    label: String,
    pref: String,
    prefInit: String,
    strId: Int,
    spinnerArr: List<String>
) {

    private val objCard = ObjectCard(context)

    init {
        val tv = TextView(context)
        ObjectCardText.textViewSetup(tv)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        tv.setTextColor(UIPreferences.backgroundColor)
        tv.setPadding(
            MyApplication.padding,
            MyApplication.padding,
            MyApplication.padding,
            MyApplication.padding
        )
        tv.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
        tv.text = label
        tv.gravity = Gravity.CENTER_VERTICAL
        tv.setOnClickListener { showHelpText(context.resources.getString(strId)) }
        val ll = LinearLayout(context)
        ll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        ll.orientation = LinearLayout.HORIZONTAL
        ll.gravity = Gravity.CENTER_VERTICAL
        ll.addView(tv)
        val spinner = Spinner(context)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            if (UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB) {
                UtilityUI.setupSpinner(spinner, false)
            }
        }
        val dataAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, spinnerArr)
        dataAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
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
        var val1 = Utility.readPref(context, pref, prefInit)
        if (pref == "WIDGET_LOCATION") {
            val1 += ": " + UtilityStringExternal.truncate(
                Utility.readPref(
                    context,
                    "LOC" + val1 + "_LABEL",
                    ""
                ), 20
            )
        }
        spinner.setSelection(dataAdapter.getPosition(val1))
        ll.addView(spinner)
        objCard.addView(ll)
    }

    private fun showHelpText(helpStr: String) {
        UtilityAlertDialog.showHelpText(helpStr, activity)
    }

    val card: CardView get() = objCard.card
}


