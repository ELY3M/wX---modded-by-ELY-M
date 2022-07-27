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
import joshuatee.wx.Extensions.parseMultiple
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.objects.TextSize

class ObjectAlertDetail(val context: Context, box: VBox) {

    private val objectTextViews = mutableListOf<Text>()
    var title = ""
        private set
    var wfoTitle = ""
        private set

    init {
        repeat(6) {
            objectTextViews.add(Text(context))
            box.addWidget(objectTextViews.last().get())
        }
        objectTextViews[0].setPadding(UIPreferences.padding, 0, UIPreferences.padding, 0)
        objectTextViews[1].setPadding(UIPreferences.padding, 0, UIPreferences.padding, UIPreferences.padding)
        (2..5).forEach {
            objectTextViews[it].setPadding(UIPreferences.padding)
        }
    }

    fun updateContent(capAlert: CapAlert, url: String) {
        val startTime: String
        var endTime = ""
        var wfo = ""
        if (capAlert.text.contains("This alert has expired")) {
            objectTextViews[0].text = capAlert.text
            objectTextViews[0].setTextSize(TextSize.LARGE)
        } else {
            if (!url.contains("urn:oid")) {
                if (capAlert.title.contains("until")) {
                    val items = capAlert.title.parseMultiple("(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
                    title = items[0]
                    startTime = items[1]
                    endTime = items[2]
                    wfo = items[3]
                } else {
                    val items = capAlert.title.parseMultiple("(.*?) issued (.*?) by (.*?)$", 3)
                    title = items[0]
                    startTime = items[1]
                    wfo = items[2]
                }
            } else {
                when {
                    capAlert.title.contains("expiring") -> {
                        val items = capAlert.title.parseMultiple("(.*?) issued (.*?) expiring (.*?) by (.*?)$", 4)
                        title = items[0]
                        startTime = items[1]
                        endTime = items[2]
                        wfo = items[3]
                    }
                    capAlert.title.contains("until") -> {
                        val items = capAlert.title.parseMultiple("(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
                        title = items[0]
                        startTime = items[1]
                        endTime = items[2]
                        wfo = items[3]
                    }
                    else -> {
                        val items = capAlert.title.parseMultiple("(.*?) issued (.*?) by (.*?)$", 3)
                        title = items[0]
                        startTime = items[1]
                        wfo = items[2]
                    }
                }
            }
            objectTextViews[0].text = context.resources.getString(R.string.uswarn_start_time, startTime)
            objectTextViews[1].text = context.resources.getString(R.string.uswarn_end_time, endTime)
            objectTextViews[2].text = capAlert.area
            objectTextViews[2].color = UIPreferences.textHighlightColor
            objectTextViews[3].text = capAlert.summary
            objectTextViews[4].text = capAlert.instructions
            objectTextViews[5].text = capAlert.extended
        }
        wfoTitle = wfo
    }
}
