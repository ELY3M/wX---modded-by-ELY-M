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
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.misc.CapAlert
import joshuatee.wx.misc.UtilityCapAlert
import joshuatee.wx.objects.TextSize

class AlertDetail(val context: Context, box: VBox) {

    private val textViews = mutableListOf<Text>()
    var title = ""
        private set
    var wfoTitle = ""
        private set

    init {
        repeat(6) {
            textViews.add(Text(context))
            box.addWidget(textViews.last())
        }
        textViews[0].setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
        textViews[1].setPadding(
            UIPreferences.padding,
            0,
            UIPreferences.padding,
            UIPreferences.padding
        )
        (2..5).forEach {
            textViews[it].setPadding(UIPreferences.padding)
        }
    }

    fun updateContent(capAlert: CapAlert) {
        val startTime: String
        val endTime: String
        var wfo = ""
        if (capAlert.text.contains("This alert has expired")) {
            textViews[0].text = capAlert.text
            textViews[0].setSize(TextSize.LARGE)
        } else {
            val items = UtilityCapAlert.times(capAlert)
            startTime = items[0]
            endTime = items[1]
            title = items[2]
            wfo = items[3]
            textViews[0].text = context.resources.getString(R.string.uswarn_start_time, startTime)
            textViews[1].text = context.resources.getString(R.string.uswarn_end_time, endTime)
            textViews[2].text = capAlert.area
            textViews[2].color = UIPreferences.textHighlightColor
            textViews[3].text = capAlert.summary
            textViews[4].text = capAlert.instructions
            textViews[5].text = capAlert.extended
        }
        wfoTitle = wfo
    }
}
