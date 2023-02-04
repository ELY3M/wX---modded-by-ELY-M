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
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.fragments.UtilityLocationFragment
import joshuatee.wx.objects.TextSize
import joshuatee.wx.util.UtilityForecastIcon

class SevenDayCard(context: Context, iconUrl: String, isUS: Boolean, forecast: String) : Widget {

    private val card = Card(context)
    private val photo = Photo(context)
    private val topLineText = Text(context, TextSize.MEDIUM)
    private val bottomLineText = Text(context, backgroundText = true)

    init {
        val hbox = HBox(context)
        val vbox = VBox(context)
        topLineText.setPadding(UIPreferences.padding, 0, UIPreferences.paddingSmall, 0)
        bottomLineText.setPadding(UIPreferences.padding, 0, UIPreferences.paddingSmall, 0)
        vbox.addWidgets(listOf(topLineText, bottomLineText))
        if (!UIPreferences.locfragDontShowIcons) {
            hbox.addWidget(photo)
        }
        hbox.addLayout(vbox)
        card.addLayout(hbox)
        val items = forecast.split(": ")
        if (items.size > 1) {
            if (isUS) {
                setTopLine(
                        items[0] + " (" + UtilityLocationFragment.extractTemperature(
                                items[1]
                        )
                                + GlobalVariables.degreeSymbol
                                + UtilityLocationFragment.extractWindDirection(items[1].substring(1))
                                + UtilityLocationFragment.extract7DayMetrics(items[1].substring(1)) + ")"
                )
            } else {
                setTopLine(
                        items[0] + " ("
                                + UtilityLocationFragment.extractCanadaTemperature(items[1])
                                + GlobalVariables.degreeSymbol
                                + UtilityLocationFragment.extractCanadaWindDirection(items[1])
                                + UtilityLocationFragment.extractCanadaWindSpeed(items[1]) + ")"
                )
            }
            setBottomLine(items[1])
        }
        if (!UIPreferences.locfragDontShowIcons) {
            photo.set(UtilityForecastIcon.getIcon(context, iconUrl))
        }
    }

    private fun setTopLine(text: String) {
        topLineText.text = text
    }

    private fun setBottomLine(text: String) {
        bottomLineText.text = text
    }

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }

    fun refreshTextSize() {
        topLineText.refreshTextSize(TextSize.MEDIUM)
        bottomLineText.refreshTextSize(TextSize.MEDIUM)
    }

    override fun getView() = card.getView()
}
