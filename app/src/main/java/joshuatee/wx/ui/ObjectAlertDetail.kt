/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.util.TypedValue
import android.widget.LinearLayout

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.CapAlert
import joshuatee.wx.util.UtilityString

class ObjectAlertDetail(val context: Context, ll: LinearLayout) {

    private val objectTextViews = mutableListOf<ObjectTextView>()
    var title: String = ""
        private set
    var wfoTitle: String = ""
        private set

    init {
        (0 until 5).forEach {
            objectTextViews.add(ObjectTextView(context))
            ll.addView(objectTextViews[it].tv)
        }
        objectTextViews[0].setPadding(
                MyApplication.padding,
                0,
                MyApplication.padding,
                0
        )
        objectTextViews[1].setPadding(
                MyApplication.padding,
                0,
                MyApplication.padding,
                MyApplication.padding
        )
        objectTextViews[2].setPadding(
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding
        )
        objectTextViews[3].setPadding(
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding
        )
        objectTextViews[4].setPadding(
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding,
                MyApplication.padding
        )
    }

    fun updateContent(capAlert: CapAlert, url: String) {
        val startTime: String
        var endTime = ""
        var wfo = ""
        if (capAlert.text.contains("This alert has expired")) {
            objectTextViews[0].text = capAlert.text
            objectTextViews[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeLarge)
        } else {
            if (!url.contains("NWS-IDP-PROD")) {
                if (capAlert.title.contains("until")) {
                    val tmpArr = UtilityString.parseMultiple(
                            capAlert.title,
                            "(.*?) issued (.*?) until (.*?) by (.*?)$",
                            4
                    )
                    title = tmpArr[0]
                    startTime = tmpArr[1]
                    endTime = tmpArr[2]
                    wfo = tmpArr[3]
                } else {
                    val tmpArr =
                            UtilityString.parseMultiple(capAlert.title, "(.*?) issued (.*?) by (.*?)$", 3)
                    title = tmpArr[0]
                    startTime = tmpArr[1]
                    wfo = tmpArr[2]
                }
            } else {
                when {
                    capAlert.title.contains("expiring") -> {
                        val tmpArr = UtilityString.parseMultiple(
                                capAlert.title,
                                "(.*?) issued (.*?) expiring (.*?) by (.*?)$",
                                4
                        )
                        title = tmpArr[0]
                        startTime = tmpArr[1]
                        endTime = tmpArr[2]
                        wfo = tmpArr[3]
                    }
                    capAlert.title.contains("until") -> {
                        val tmpArr = UtilityString.parseMultiple(
                                capAlert.title,
                                "(.*?) issued (.*?) until (.*?) by (.*?)$",
                                4
                        )
                        title = tmpArr[0]
                        startTime = tmpArr[1]
                        endTime = tmpArr[2]
                        wfo = tmpArr[3]
                    }
                    else -> {
                        val tmpArr =
                                UtilityString.parseMultiple(capAlert.title, "(.*?) issued (.*?) by (.*?)$", 3)
                        title = tmpArr[0]
                        startTime = tmpArr[1]
                        wfo = tmpArr[2]
                    }
                }
            }
            objectTextViews[0].text = context.resources.getString(R.string.uswarn_start_time, startTime)
            objectTextViews[1].text = context.resources.getString(R.string.uswarn_end_time, endTime)
            objectTextViews[2].text = capAlert.area
            objectTextViews[2].setTextColor(UIPreferences.textHighlightColor)
            objectTextViews[3].text = capAlert.summary
            objectTextViews[4].text = capAlert.instructions
        }
        wfoTitle = wfo
    }
}


