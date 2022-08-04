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
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility
import java.util.Random

/**
 * Our data observer just notifies an update for all weather widgets when it detects a change.
 */
class WeatherDataProviderObserver(
        private val mAppWidgetManager: AppWidgetManager,
        private val mComponentName: ComponentName,
        handler: Handler
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
            mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.weather_list
        )
    }
}

/**
 * The weather widget's AppWidgetProvider.
 */
class WeatherWidgetProvider : AppWidgetProvider() {
    init {
        // Start the worker thread
        workerThread = HandlerThread("WeatherWidgetProvider-worker")
        workerThread!!.start()
        workerQueue = Handler(workerThread!!.looper)
    }

    // XXX: clear the worker queue if we are destroyed?

    override fun onEnabled(context: Context) {
        // Register for external updates to the data to trigger an update of the widget.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
        Utility.writePref(context, "WIDGETS_ENABLED", "true")
        val contentResolver = context.contentResolver
        if (weatherDataProviderObserver == null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
            weatherDataProviderObserver = WeatherDataProviderObserver(appWidgetManager, componentName, workerQueue!!)
            contentResolver.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, weatherDataProviderObserver!!)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == REFRESH_ACTION) {
            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
            // are triggering an update of the data on another thread.  In practice, this update
            // can be triggered from a background service, or perhaps as a result of user actions
            // inside the main application.
            //val context = ctx
            workerQueue!!.removeMessages(0)
            workerQueue!!.post {
                val contentResolver = context.contentResolver
                contentResolver.query(WeatherDataProvider.CONTENT_URI, null, null, null, null)?.use { cursor ->
                    val count = cursor.count
                    (0 until count).forEach {
                        val uri = ContentUris.withAppendedId(WeatherDataProvider.CONTENT_URI, it.toLong())
                        val values = ContentValues()
                        values.put(WeatherDataProvider.Columns.TEMPERATURE, Random().nextInt(maxDegrees))
                        contentResolver.update(uri, values, null, null)
                    }
                    contentResolver.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, weatherDataProviderObserver!!)
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.weather_list)
                }

//                val contentResolver = context.contentResolver
//                val cursor = contentResolver.query(WeatherDataProvider.CONTENT_URI, null, null, null, null)
//                val count = cursor!!.count
//                (0 until count).forEach {
//                    val uri = ContentUris.withAppendedId(WeatherDataProvider.CONTENT_URI, it.toLong())
//                    val values = ContentValues()
//                    values.put(WeatherDataProvider.Columns.TEMPERATURE, Random().nextInt(maxDegrees))
//                    contentResolver.update(uri, values, null, null)
//                }
//                contentResolver.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, weatherDataProviderObserver!!)
//                val appWidgetManager = AppWidgetManager.getInstance(context)
//                val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.weather_list)
            }
        } else if (action == CLICK_ACTION) {
            val day = intent.getStringExtra(EXTRA_DAY_ID)
            val formatStr = context.resources.getString(R.string.toast_format_string)
            Toast.makeText(context, String.format(formatStr, day), Toast.LENGTH_SHORT).show()
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update each of the widgets with the remote adapter
        UtilityWidget.updateSevenDay(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        val minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        isLargeLayout = minHeight >= 100
        val layout: RemoteViews = buildLayout(context, appWidgetId, isLargeLayout)
        appWidgetManager.updateAppWidget(appWidgetId, layout)
    }

    companion object {
        const val CLICK_ACTION = "${GlobalVariables.packageNameAsString}.weatherlistwidget.CLICK"
        const val REFRESH_ACTION = "${GlobalVariables.packageNameAsString}.weatherlistwidget.REFRESH"
        const val EXTRA_DAY_ID = "${GlobalVariables.packageNameAsString}.weatherlistwidget.day"
        var workerThread: HandlerThread? = null
        var workerQueue: Handler? = null
        var weatherDataProviderObserver: WeatherDataProviderObserver? = null
        private const val maxDegrees = 96
        var isLargeLayout = true

        fun buildLayout(context: Context, appWidgetId: Int, largeLayout: Boolean): RemoteViews {
            val remoteViews: RemoteViews
            val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
            val widgetLocNumInt = (widgetLocNum.toIntOrNull() ?: 0) - 1
            if (largeLayout) {
                // Specify the service to provide data for the collection widget.  Note that we need to
                // embed the appWidgetId via the data otherwise it will be ignored.
                val intent = Intent(context, WeatherWidgetService::class.java)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
                remoteViews = RemoteViews(context.packageName, R.layout.widget_7day_layout)
                remoteViews.setRemoteAdapter(R.id.weather_list, intent)
                val requestId = ObjectDateTime.currentTimeMillis().toInt()
                val intentWx = Intent(context, WX::class.java)
                intentWx.action = "WX"
                val pendingIntentWx = PendingIntent.getActivity(context, requestId, intentWx, PendingIntent.FLAG_IMMUTABLE) // was 0
                remoteViews.setPendingIntentTemplate(R.id.weather_list, pendingIntentWx)
                remoteViews.setTextViewText(R.id.city_name, Location.getName(widgetLocNumInt))
            } else {
                remoteViews = RemoteViews(context.packageName, R.layout.widget_layout_small)
                // Update the header to reflect the weather for "today"
                val cursor = context.contentResolver.query(WeatherDataProvider.CONTENT_URI, null, null, null, null)
                if (cursor!!.moveToPosition(0)) {
                    val tempColIndex = cursor.getColumnIndex(WeatherDataProvider.Columns.TEMPERATURE)
                    val temp = cursor.getInt(tempColIndex)
                    val formatStr = context.resources.getString(R.string.header_format_string)
                    val header = String.format(formatStr, temp, Location.getName(widgetLocNumInt))
                    remoteViews.setTextViewText(R.id.city_name, header)
                }
                cursor.close()
            }
            return remoteViews
        }
    }
}
