/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package joshuatee.wx

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.content.ContentValues
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.widget.RemoteViews
import android.widget.Toast
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility

import java.util.Random

/**
 * Our data observer just notifies an update for all weather widgets when it detects a change.
 */
class WeatherDataProviderObserver(private val mAppWidgetManager: AppWidgetManager, private val mComponentName: ComponentName, h: Handler) : ContentObserver(h) {

    override fun onChange(selfChange: Boolean) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.weather_list)
    }
}

/**
 * The weather widget's AppWidgetProvider.
 */
class WeatherWidgetProvider : AppWidgetProvider() {
    init {
        // Start the worker thread
        sWorkerThread = HandlerThread("WeatherWidgetProvider-worker")
        sWorkerThread!!.start()
        sWorkerQueue = Handler(sWorkerThread!!.looper)
    }

    // XXX: clear the worker queue if we are destroyed?

    override fun onEnabled(context: Context) {
        // Register for external updates to the data to trigger an update of the widget.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
        Utility.writePref(context, "WIDGETS_ENABLED", "true")
        val r = context.contentResolver
        if (sDataObserver == null) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, WeatherWidgetProvider::class.java)
            sDataObserver = WeatherDataProviderObserver(mgr, cn, sWorkerQueue!!)
            r.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, sDataObserver!!)
        }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        val action = intent.action
        if (action == REFRESH_ACTION) {
            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
            // are triggering an update of the data on another thread.  In practice, this update
            // can be triggered from a background service, or perhaps as a result of user actions
            // inside the main application.
            //val context = ctx
            sWorkerQueue!!.removeMessages(0)
            sWorkerQueue!!.post {
                val r = ctx.contentResolver
                val c = r.query(WeatherDataProvider.CONTENT_URI, null, null, null, null)
                val count = c!!.count
                (0 until count).forEach {
                    val uri = ContentUris.withAppendedId(WeatherDataProvider.CONTENT_URI, it.toLong())
                    val values = ContentValues()
                    values.put(WeatherDataProvider.Columns.TEMPERATURE, Random().nextInt(sMaxDegrees))
                    r.update(uri, values, null, null)
                }
                r.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, sDataObserver!!)
                val mgr = AppWidgetManager.getInstance(ctx)
                val cn = ComponentName(ctx, WeatherWidgetProvider::class.java)
                mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.weather_list)
            }
        } else if (action == CLICK_ACTION) {
            val day = intent.getStringExtra(EXTRA_DAY_ID)
            val formatStr = ctx.resources.getString(R.string.toast_format_string)
            Toast.makeText(ctx, String.format(formatStr, day), Toast.LENGTH_SHORT).show()
        }
        super.onReceive(ctx, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update each of the widgets with the remote adapter

        UtilityWidget.updateSevenDay(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        val minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        mIsLargeLayout = minHeight >= 100
        val layout: RemoteViews = buildLayout(context, appWidgetId, mIsLargeLayout)
        appWidgetManager.updateAppWidget(appWidgetId, layout)
    }

    companion object {
        const val CLICK_ACTION: String = "joshuatee.wx.weatherlistwidget.CLICK"
        const val REFRESH_ACTION: String = "joshuatee.wx.weatherlistwidget.REFRESH"
        const val EXTRA_DAY_ID: String = "joshuatee.wx.weatherlistwidget.day"
        var sWorkerThread: HandlerThread? = null
        var sWorkerQueue: Handler? = null
        var sDataObserver: WeatherDataProviderObserver? = null
        private const val sMaxDegrees = 96
        var mIsLargeLayout: Boolean = true
        fun buildLayout(context: Context, appWidgetId: Int, largeLayout: Boolean): RemoteViews {
            val rv: RemoteViews
            val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
            val widgetLocNumInt = (widgetLocNum.toIntOrNull() ?: 0) - 1
            if (largeLayout) {
                // Specify the service to provide data for the collection widget.  Note that we need to
                // embed the appWidgetId via the data otherwise it will be ignored.
                val intent = Intent(context, WeatherWidgetService::class.java)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
                rv = RemoteViews(context.packageName, R.layout.widget_7day_layout)
                rv.setRemoteAdapter(R.id.weather_list, intent)
                val requestID = System.currentTimeMillis().toInt()
                val intentWx = Intent(context, WX::class.java)
                intentWx.action = "WX"
                val pendingIntentWx = PendingIntent.getActivity(context, requestID, intentWx, 0) // was 0
                rv.setPendingIntentTemplate(R.id.weather_list, pendingIntentWx)
                // Restore the minimal header
                rv.setTextViewText(R.id.city_name, Location.getName(widgetLocNumInt))
            } else {
                rv = RemoteViews(context.packageName, R.layout.widget_layout_small)
                // Update the header to reflect the weather for "today"
                val c = context.contentResolver.query(WeatherDataProvider.CONTENT_URI, null, null, null, null)
                if (c!!.moveToPosition(0)) {
                    val tempColIndex = c.getColumnIndex(WeatherDataProvider.Columns.TEMPERATURE)
                    val temp = c.getInt(tempColIndex)
                    val formatStr = context.resources.getString(R.string.header_format_string)
                    val header = String.format(formatStr, temp, Location.getName(widgetLocNumInt))
                    rv.setTextViewText(R.id.city_name, header)
                }
                c.close()
            }
            return rv
        }
    }
}