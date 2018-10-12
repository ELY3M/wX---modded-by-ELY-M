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
import joshuatee.wx.R
import joshuatee.wx.UtilityWidget
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.objects.WidgetFile.*
import joshuatee.wx.spc.SPCMesoActivity
import joshuatee.wx.spc.SPCStormReportsActivity
import joshuatee.wx.vis.GOES16Activity
import joshuatee.wx.wpc.WPCImagesActivity

class ObjectWidgetGeneric(context: Context, val type: WidgetFile) {

    val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.widget_generic_layout)

    init {
        UtilityWidget.setImage(context, remoteViews, type.fileName)
        when (type) {
            WPCIMG -> UtilityWidget.setupIntent(context, remoteViews, WPCImagesActivity::class.java, R.id.iv, type.action)
            SPCMESO -> UtilityWidget.setupIntent(context, remoteViews, SPCMesoActivity::class.java, R.id.iv, SPCMesoActivity.INFO, arrayOf("SPCMESO1", "1", "SPCMESO"), type.action)
            CONUSWV -> UtilityWidget.setupIntent(context, remoteViews, GOES16Activity::class.java, R.id.iv, GOES16Activity.RID, arrayOf("CONUS", "09"), type.action)
            STRPT -> UtilityWidget.setupIntent(context, remoteViews, SPCStormReportsActivity::class.java, R.id.iv, SPCStormReportsActivity.NO, arrayOf("today"), type.action)
            else -> {
            }
        }
    }
}


