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

package joshuatee.wx.widgets

import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.misc.WfoTextActivity
import joshuatee.wx.util.Utility

class ObjectWidgetAfd(context: Context) {

    private val remoteViews = RemoteViews(context.packageName, R.layout.widget_textview_layout)

    init {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val wfo = Utility.readPref(context, "NWS$widgetLocationNumber", "")
        val afd = Utility.readPref(context, "AFD_WIDGET", "")
        remoteViews.setTextViewText(R.id.text1, afd)
        remoteViews.setTextViewTextSize(
            R.id.text1,
            TypedValue.COMPLEX_UNIT_PX,
            UIPreferences.textSizeSmall
        )
        val product = if (Utility.readPref(context, "WFO_TEXT_FAV", "").startsWith("VFD")) {
            "VFD"
        } else {
            "AFD"
        }
        if (!UIPreferences.widgetPreventTap) {
            UtilityWidget.setupIntent(
                context,
                remoteViews,
                WfoTextActivity::class.java,
                R.id.text1,
                WfoTextActivity.URL,
                arrayOf(wfo, product),
                WidgetFile.AFD.action
            )
        }
    }

    fun get() = remoteViews
}
