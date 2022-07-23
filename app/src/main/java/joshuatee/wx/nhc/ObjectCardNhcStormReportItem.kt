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

package joshuatee.wx.nhc

import android.content.Context
import android.view.Gravity
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.VBox
import joshuatee.wx.ui.ObjectTextView
import java.util.*

class ObjectCardNhcStormReportItem(context: Context, box: VBox, stormData: ObjectNhcStormDetails) {

    private val objectCard = ObjectCard(context)
    private val textViewTop = ObjectTextView(context, UIPreferences.textHighlightColor)
    private val textViewTime = ObjectTextView(context)
    private val textViewMovement = ObjectTextView(context)
    private val textViewPressure = ObjectTextView(context)
    private val textViewWindSpeed = ObjectTextView(context)
    private val textViewBottom = ObjectTextView(context, backgroundText = true)

    init {
        val vbox = VBox(context, Gravity.CENTER_VERTICAL)
        vbox.addViews(listOf(textViewTop.get(), textViewTime.get(), textViewMovement.get()))
        vbox.addViews(listOf(textViewPressure.get(), textViewWindSpeed.get(), textViewBottom.get()))
        objectCard.addView(vbox.get())
        textViewTop.text = stormData.name + " (" + stormData.classification + ") " + stormData.center
        textViewTime.text = stormData.dateTime.replace("T", " ").replace(":00.000Z", "Z")
        textViewMovement.text = "Moving: " + stormData.movement
        textViewPressure.text = "Min pressure: " + stormData.pressure
        textViewWindSpeed.text = "Max sustained: " + stormData.intensity
        textViewBottom.text = stormData.status + " " + stormData.binNumber + " " + stormData.id.uppercase(Locale.US)
        box.addWidget(objectCard.get())
    }

    fun get() = objectCard.get()

    fun setListener(fn: View.OnClickListener) {
        objectCard.setOnClickListener(fn)
    }
}
