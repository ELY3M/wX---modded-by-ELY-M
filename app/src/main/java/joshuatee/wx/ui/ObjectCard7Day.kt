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
import android.view.View
import android.widget.LinearLayout

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.fragments.UtilityLocationFragment
import joshuatee.wx.objects.TextSize

class ObjectCard7Day(context: Context, bitmap: Bitmap, isUS: Boolean, day: Int, forecasts: List<String>) {

    private val objectCard = ObjectCard(context)
    private val objectImageView = ObjectImageView(context)
    private val topLineText = ObjectTextView(context, TextSize.MEDIUM)
    private val bottomLineText = ObjectTextView(context, backgroundText = true)

    init {
        val horizontalContainer = ObjectLinearLayout(context, LinearLayout.HORIZONTAL)
        val verticalContainer = ObjectLinearLayout(context, LinearLayout.VERTICAL)
        topLineText.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, 0)
        bottomLineText.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, 0)
        verticalContainer.addViews(listOf(topLineText.tv, bottomLineText.tv))
        if (!UIPreferences.locfragDontShowIcons) {
            horizontalContainer.addView(objectImageView)
        }
        horizontalContainer.addView(verticalContainer)
        objectCard.addView(horizontalContainer)
        // UtilityLog.d("wx", "A:" + forecasts[day])
        val items = if (forecasts.size > day) {
            forecasts[day].split(": ")
        } else {
            listOf()
        }
        if (items.size > 1) {
            if (isUS) {
                setTopLine(
                        items[0] + " (" + UtilityLocationFragment.extractTemperature(
                                items[1]
                        )
                                + MyApplication.DEGREE_SYMBOL
                                + UtilityLocationFragment.extractWindDirection(items[1].substring(1))
                                + UtilityLocationFragment.extract7DayMetrics(items[1].substring(1)) + ")"
                )
            } else {
                setTopLine(
                        items[0] + " ("
                                + UtilityLocationFragment.extractCanadaTemperature(items[1])
                                + MyApplication.DEGREE_SYMBOL
                                + UtilityLocationFragment.extractCanadaWindDirection(items[1])
                                + UtilityLocationFragment.extractCanadaWindSpeed(items[1]) + ")"
                )
            }
            setBottomLine(items[1])
        }
        if (!UIPreferences.locfragDontShowIcons) {
            objectImageView.setImage(bitmap)
        }
    }

    private fun setTopLine(text: String) {
        topLineText.text = text
    }

    private fun setBottomLine(text: String) {
        bottomLineText.text = text
    }

    fun setOnClickListener(fn: View.OnClickListener) = objectCard.setOnClickListener(fn)

    fun refreshTextSize() {
        topLineText.refreshTextSize(TextSize.MEDIUM)
        bottomLineText.refreshTextSize(TextSize.MEDIUM)
    }

    val card get() = objectCard.card
}


