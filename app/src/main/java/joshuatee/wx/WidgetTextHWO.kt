/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.WidgetFile.HWO

class WidgetTextHWO : AppWidgetProvider() {

    private val type = HWO

    override fun onDisabled(context: Context) {
        UtilityWidget.disableWidget(context, type)
        super.onDisabled(context)
    }

    override fun onEnabled(context: Context) {
        UtilityWidget.enableWidget(context, type)
        getContent(context)
        super.onEnabled(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        UtilityWidget.update(context, type)
    }

    private fun getContent(context: Context) {
        FutureVoid(
                context,
                { UtilityWidgetDownload.download(context, type) },
                { UtilityWidget.update(context, type) }
        )
    }
}
