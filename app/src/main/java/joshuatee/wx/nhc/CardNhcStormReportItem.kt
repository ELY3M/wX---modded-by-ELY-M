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

package joshuatee.wx.nhc

import android.content.Context
import android.view.Gravity
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.VBox
import joshuatee.wx.ui.Text
import joshuatee.wx.ui.Widget
import joshuatee.wx.util.UtilityMath
import java.util.Locale

class CardNhcStormReportItem(context: Context, stormData: NhcStormDetails) : Widget {

    private val card = Card(context)
    private val vbox = VBox(context, Gravity.CENTER_VERTICAL)
    private val textViewTop = Text(context, UIPreferences.textHighlightColor)
    private val textViewTime = Text(context)
    private val textViewMovement = Text(context)
    private val textViewPressure = Text(context)
    private val textViewWindSpeed = Text(context)
    private val textViewBottom = Text(context, backgroundText = true)

    init {
        vbox.addWidgets(listOf(textViewTop, textViewTime, textViewMovement))
        vbox.addWidgets(listOf(textViewPressure, textViewWindSpeed, textViewBottom))
        card.addLayout(vbox)
        textViewTop.text = stormData.name + " (" + stormData.classification + ") " + stormData.center
        textViewTime.text = stormData.dateTime.replace("T", " ").replace(":00.000Z", "Z")
        textViewMovement.text = "Moving: " + stormData.movement
        textViewPressure.text = "Min pressure: " + stormData.pressure
        textViewWindSpeed.text = "Max sustained: " + UtilityMath.knotsToMph(stormData.intensity) + " mph"
        textViewBottom.text = stormData.status + " " + stormData.binNumber + " " + stormData.stormId.uppercase(Locale.US)
    }

    override fun getView() = card.getView()

    fun connect(fn: View.OnClickListener) {
        card.connect(fn)
    }
}
