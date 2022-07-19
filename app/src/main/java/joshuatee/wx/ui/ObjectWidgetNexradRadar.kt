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
import android.widget.RemoteViews
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.UtilityWidget
import joshuatee.wx.objects.WidgetFile.NEXRAD_RADAR
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility

class ObjectWidgetNexradRadar(context: Context) {

    private val remoteViews = RemoteViews(context.packageName, R.layout.widget_generic_layout)

    init {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val radarSite = Location.getRid(context, widgetLocationNumber)
        UtilityWidget.setImage(context, remoteViews, NEXRAD_RADAR.fileName)
        if (!UIPreferences.widgetPreventTap) {
            UtilityWidget.setupIntent(context, remoteViews, WXGLRadarActivity::class.java, R.id.iv, WXGLRadarActivity.RID, arrayOf(radarSite), NEXRAD_RADAR.action)
        }
    }

    fun get() = remoteViews
}
