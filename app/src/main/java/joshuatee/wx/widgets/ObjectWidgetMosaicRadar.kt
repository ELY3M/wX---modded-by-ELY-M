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
import android.widget.RemoteViews
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radar.RadarMosaicActivity

class ObjectWidgetMosaicRadar(context: Context) {

    private val remoteViews = RemoteViews(context.packageName, R.layout.widget_generic_layout)

    init {
        UtilityWidget.setImage(context, remoteViews, WidgetFile.MOSAIC_RADAR.fileName)
        if (!UIPreferences.widgetPreventTap) {
            UtilityWidget.setupIntent(
                context,
                remoteViews,
                RadarMosaicActivity::class.java,
                R.id.iv,
                RadarMosaicActivity.URL,
                arrayOf(""),
                WidgetFile.MOSAIC_RADAR.action
            )
        }
    }

    fun get() = remoteViews
}
