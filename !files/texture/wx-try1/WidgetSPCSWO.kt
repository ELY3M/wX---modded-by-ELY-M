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

package joshuatee.wx

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.AsyncTask
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.objects.WidgetFile.*

class WidgetSPCSWO : AppWidgetProvider() {

    private lateinit var contextg: Context

    override fun onDisabled(context: Context) {
        UtilityWidget.disableWidget(context, WidgetFile.SPCSWO)
        super.onDisabled(context)
    }

    override fun onEnabled(context: Context) {
        UtilityWidget.enableWidget(context, WidgetFile.SPCSWO)
        contextg = context
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        super.onEnabled(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        UtilityWidget.update(context, SPCSWO)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            UtilityWidgetDownload.download(contextg, SPCSWO)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityWidget.update(contextg, SPCSWO)
        }
    }
} 