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
import android.widget.RemoteViews
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UtilityWidget
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.objects.WidgetFile.*

class ObjectWidgetNhc(context: Context) {

    val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.widget_nhc_layout)

    init {
        UtilityWidget.setImage(context, remoteViews, R.id.iv1, NHC.fileName + "0")
        UtilityWidget.setImage(context, remoteViews, R.id.iv2, NHC.fileName + "1")
        if (!MyApplication.widgetPreventTap) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    NhcActivity::class.java,
                    R.id.iv1,
                    WidgetFile.NHC.action + "0"
            )
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    NhcActivity::class.java,
                    R.id.iv2,
                    WidgetFile.NHC.action + "1"
            )
        }
    }
}


