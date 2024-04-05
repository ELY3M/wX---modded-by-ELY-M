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

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.UtilityLocationFragment
import joshuatee.wx.util.UtilityForecastIcon

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
class WeatherWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = StackRemoteViewsFactory(this.applicationContext)
}

/**
 * This is the factory that will provide data to the collection widget.
 */

internal class StackRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var cursor: Cursor? = null

    override fun onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    override fun onDestroy() {
        cursor?.close()
    }

    override fun getCount() = if (cursor != null) cursor!!.count else 0

    override fun getViewAt(position: Int): RemoteViews {
        var day = "Unknown Day"
//        var temp = 0
        if (cursor!!.moveToPosition(position)) {
            val dayColIndex = cursor!!.getColumnIndex(WeatherDataProvider.Columns.DAY)
//            val tempColIndex = cursor!!.getColumnIndex(WeatherDataProvider.Columns.TEMPERATURE)
            day = cursor!!.getString(dayColIndex)
//            temp = cursor!!.getInt(tempColIndex)
        }
        var t1 = ""
        var t2 = ""
        val formatString = context.resources.getString(R.string.item_format_string)
        val itemId = R.layout.widget_item
        val remoteViews = RemoteViews(context.packageName, itemId)
        val preferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        if (position != 0) {
            val list = day.split(": ").dropLastWhile { it.isEmpty() }
            if (list.size > 1) {
                t1 = list[0].replace(":", " ") + " (" +
                        UtilityLocationFragment.extractTemperature(list[1]) +
                        GlobalVariables.DEGREE_SYMBOL +
                        UtilityLocationFragment.extractWindDirection(list[1].substring(1)) +
                        UtilityLocationFragment.extract7DayMetrics(list[1].substring(1)) + ")"
                t2 = list[1]
            }
        } else {
            val sep = " - "
            val items = day.split(sep).dropLastWhile { it.isEmpty() }
            if (items.size > 4) {
                val list = items[0].split("/").dropLastWhile { it.isEmpty() }
                t1 = items[4].replace("^ ", "") + " " + list[0] + items[2]
                t2 = list[1].replace("^ ", "") + sep + items[1] + sep + items[3]
            }
            t2 += GlobalVariables.newline + preferences.getString("UPDTIME_WIDGET", "No data")
        }
        remoteViews.setTextViewText(R.id.widget_tv1, String.format(formatString, t1))
        remoteViews.setTextViewText(R.id.widget_tv2, String.format(formatString, t2))
        var iconString = preferences.getString("7DAY_ICONS_WIDGET", "NoData")
        iconString = preferences.getString("CC_WIDGET_ICON_URL", "NULL")!! + "!" + iconString
        val icons = iconString.split("!")
        if (position < icons.size) {
            remoteViews.setImageViewUri(R.id.iv, Uri.parse(""))
            remoteViews.setImageViewBitmap(R.id.iv, UtilityForecastIcon.getIcon(context, icons[position]))
        }
        val fillInIntent = Intent()
        val extras = Bundle()
        extras.putString(WeatherWidgetProvider.EXTRA_DAY_ID, day)
        fillInIntent.putExtras(extras)
        remoteViews.setOnClickFillInIntent(R.id.widget_tv1, fillInIntent)
        remoteViews.setOnClickFillInIntent(R.id.widget_tv2, fillInIntent)
        remoteViews.setOnClickFillInIntent(R.id.iv, fillInIntent)
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount() = 2

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true

    override fun onDataSetChanged() {
        cursor?.close()
        cursor = context.contentResolver.query(WeatherDataProvider.CONTENT_URI, null, null, null, null)
    }
}
