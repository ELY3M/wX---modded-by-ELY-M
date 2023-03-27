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

package joshuatee.wx

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.widgets.WidgetFile
import joshuatee.wx.util.Utility
import joshuatee.wx.widgets.UtilityWidget

class Widget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        Utility.writePref(context, "WIDGETS_ENABLED", "true")
        UtilityWXJobService.startService(context)
        super.onEnabled(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        UtilityWidget.update(context, WidgetFile.CCLegacy)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            val ids = intent.extras!!.getIntArray(WIDGET_IDS_KEY)
            if (intent.hasExtra(WIDGET_DATA_KEY)) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), ids!!)
            } else {
                this.onUpdate(context, AppWidgetManager.getInstance(context), ids!!)
            }
        } else
            super.onReceive(context, intent)
    }

    companion object {
        const val WIDGET_IDS_KEY = "mywidgetproviderwidgetids"
        private const val WIDGET_DATA_KEY = "mywidgetproviderwidgetdata"
    }
}
