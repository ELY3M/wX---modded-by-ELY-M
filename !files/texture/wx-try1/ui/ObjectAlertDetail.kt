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

package joshuatee.wx.ui

import android.content.Context
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.CAPAlert
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityString

class ObjectAlertDetail(val context: Context, ll: LinearLayout) {

    private val tvArr = mutableListOf<TextView>()
    var title: String = ""
        private set

    init {
        (0 until 7).forEach {
            tvArr.add(TextView(context))
            ll.addView(tvArr[it])
        }
        // setPadding(int left, int top, int right, int bottom)
        tvArr[0].setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
        tvArr[1].setPadding(MyApplication.padding, 0, MyApplication.padding, 0)  // wfo
        tvArr[2].setPadding(MyApplication.padding, 0, MyApplication.padding, 0)  // start
        tvArr[3].setPadding(MyApplication.padding, 0, MyApplication.padding, MyApplication.padding)  // end
        tvArr[4].setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
        tvArr[5].setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
        tvArr[6].setPadding(MyApplication.padding, MyApplication.padding, MyApplication.padding, MyApplication.padding)
    }

    fun updateContent(ca: CAPAlert, url: String) {
        val startTime: String
        var endTime = ""
        val wfo: String
        if (ca.text.contains("This alert has expired")) {
            tvArr[0].text = ca.text
            tvArr[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeLarge)
        } else {
            if (!url.contains("NWS-IDP-PROD")) {
                if (ca.title.contains("until")) {
                    val tmpArr = UtilityString.parseMultipe(ca.title, "(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
                    title = tmpArr[0]
                    startTime = tmpArr[1]
                    endTime = tmpArr[2]
                    wfo = tmpArr[3]
                } else {
                    val tmpArr = UtilityString.parseMultipe(ca.title, "(.*?) issued (.*?) by (.*?)$", 3)
                    title = tmpArr[0]
                    startTime = tmpArr[1]
                    wfo = tmpArr[2]
                }
            } else {
                if (ca.title.contains("expiring")) {
                    val tmpArr = UtilityString.parseMultipe(ca.title, "(.*?) issued (.*?) expiring (.*?) by (.*?)$", 4)
                    title = tmpArr[0]
                    startTime = tmpArr[1]
                    endTime = tmpArr[2]
                    wfo = tmpArr[3]
                } else {
                    val tmpArr = UtilityString.parseMultipe(ca.title, "(.*?) issued (.*?) by (.*?)$", 3)
                    title = tmpArr[0]
                    startTime = tmpArr[1]
                    wfo = tmpArr[2]
                }
            }
            tvArr[0].text = title
            tvArr[0].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeLarge)
            tvArr[1].text = wfo
            tvArr[2].text = context.resources.getString(R.string.uswarn_start_time, startTime)
            tvArr[3].text = context.resources.getString(R.string.uswarn_end_time, endTime)
            tvArr[4].text = ca.area
            tvArr[4].setTextColor(UIPreferences.textHighlightColor)
            tvArr[5].text = Utility.fromHtml(ca.summary)
            tvArr[6].text = Utility.fromHtml(ca.instructions)
        }
    }
}

