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

import joshuatee.wx.fragments.UtilityLocationFragment
import joshuatee.wx.fragments.UtilityNWS

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
class WeatherWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}

/**
 * This is the factory that will provide data to the collection widget.
 */

internal class StackRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {
    private var mCursor: Cursor? = null

    override fun onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    override fun onDestroy() {
        mCursor?.close()
    }

    override fun getCount(): Int {
        return if (mCursor != null) {
            mCursor!!.count
        } else {
            0
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        var day = "Unknown Day"
        var temp = 0
        if (mCursor!!.moveToPosition(position)) {
            val dayColIndex = mCursor!!.getColumnIndex(WeatherDataProvider.Columns.DAY)
            val tempColIndex = mCursor!!.getColumnIndex(
                WeatherDataProvider.Columns.TEMPERATURE
            )
            day = mCursor!!.getString(dayColIndex)
            temp = mCursor!!.getInt(tempColIndex)
        }
        var t1 = ""
        var t2 = ""
        val formatStr = context.resources.getString(R.string.item_format_string)
        val itemId = R.layout.widget_item
        val rv = RemoteViews(context.packageName, itemId)
        val preferences = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )
        if (position != 0) {
            val tempStrArr = MyApplication.colonSpace.split(day)
            if (tempStrArr != null && tempStrArr.size > 1) {
                t1 = tempStrArr[0].replace(":", " ") + " (" +
                        UtilityLocationFragment.extractTemp(tempStrArr[1]) +
                        MyApplication.DEGREE_SYMBOL +
                        UtilityLocationFragment.extractWindDirection(tempStrArr[1].substring(1)) +
                        UtilityLocationFragment.extract7DayMetrics(tempStrArr[1].substring(1)) + ")"
                t2 = tempStrArr[1]
            }
        } else {
            val sep = " - "
            val tmpArrCc = day.split(sep).dropLastWhile { it.isEmpty() }
            val tempArr: List<String>
            if (tmpArrCc.size > 4) {
                tempArr = tmpArrCc[0].split("/").dropLastWhile { it.isEmpty() }
                t1 = tmpArrCc[4].replace("^ ", "") + " " + tempArr[0] + tmpArrCc[2]
                t2 = tempArr[1].replace("^ ", "") + sep + tmpArrCc[1] + sep + tmpArrCc[3]
            }
            t2 += MyApplication.newline + preferences.getString("UPDTIME_WIDGET", "No data")
        }
        rv.setTextViewText(R.id.widget_tv1, String.format(formatStr, temp, t1))
        rv.setTextViewText(R.id.widget_tv2, String.format(formatStr, temp, t2))
        var iconStr = preferences.getString("7DAY_ICONS_WIDGET", "NoData")
        iconStr = preferences.getString("CC_WIDGET_ICON_URL", "NULL") + "!" + iconStr
        val iconArr = iconStr.split("!")
        if (position < iconArr.size) {
            rv.setImageViewUri(R.id.iv, Uri.parse(""))
            rv.setImageViewBitmap(R.id.iv, UtilityNWS.getIcon(context, iconArr[position]))
        }
        val fillInIntent = Intent()
        val extras = Bundle()
        extras.putString(WeatherWidgetProvider.EXTRA_DAY_ID, day)
        fillInIntent.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.widget_tv1, fillInIntent)
        rv.setOnClickFillInIntent(R.id.widget_tv2, fillInIntent)
        rv.setOnClickFillInIntent(R.id.iv, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount() = 2

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true

    override fun onDataSetChanged() {
        mCursor?.close()
        mCursor = context.contentResolver.query(
            WeatherDataProvider.CONTENT_URI,
            null,
            null,
            null,
            null
        )
    }
}
