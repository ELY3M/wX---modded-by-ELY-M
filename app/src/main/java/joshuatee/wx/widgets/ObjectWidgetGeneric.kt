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
import joshuatee.wx.widgets.WidgetFile.*
import joshuatee.wx.spc.SpcMesoActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.NationalImagesActivity

class ObjectWidgetGeneric(context: Context, val type: WidgetFile) {

    private val remoteViews = RemoteViews(context.packageName, R.layout.widget_generic_layout)

    init {
        UtilityWidget.setImage(context, remoteViews, type.fileName)
        if (!UIPreferences.widgetPreventTap) {
            when (type) {
                WPCIMG -> UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    NationalImagesActivity::class.java,
                    R.id.iv,
                    NationalImagesActivity.URL,
                    arrayOf(""),
                    type.action
                )

                SPCMESO -> UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    SpcMesoActivity::class.java,
                    R.id.iv,
                    SpcMesoActivity.INFO,
                    arrayOf("SPCMESO1", "1", "SPCMESO"),
                    type.action
                )

                CONUSWV -> UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    GoesActivity::class.java,
                    R.id.iv,
                    GoesActivity.RID,
                    arrayOf("CONUS", "09"),
                    type.action
                )

                STRPT -> UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    SpcStormReportsActivity::class.java,
                    R.id.iv,
                    SpcStormReportsActivity.DAY,
                    arrayOf("today"),
                    type.action
                )

                else -> {}
            }
        }
    }

    fun get() = remoteViews
}
