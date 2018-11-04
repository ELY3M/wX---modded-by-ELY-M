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
import android.widget.RemoteViews
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UtilityWidget
import joshuatee.wx.activitiesmisc.AFDActivity
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.util.Utility

class ObjectWidgetAFD(context: Context) {

    val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.widget_textview_layout)

    init {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val nws1Current = Utility.readPref(context, "NWS$widgetLocNum", "")
        val afd = Utility.readPref(context, "AFD_WIDGET", "")
        remoteViews.setTextViewText(R.id.text1, Utility.fromHtml(afd))
        var prodToGoTo = "AFD"
        if (Utility.readPref(context, "WFO_TEXT_FAV", "").startsWith("VFD")) {
            prodToGoTo = "VFD"
        }
        if (!MyApplication.widgetPreventTap) {
            UtilityWidget.setupIntent(context, remoteViews, AFDActivity::class.java, R.id.text1, AFDActivity.URL, arrayOf(nws1Current, prodToGoTo), WidgetFile.AFD.action)
        }
    }
}


