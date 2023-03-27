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

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.To

/**
 * A dummy class that we are going to use internally to store weather data.  Generally, this data
 * will be stored in an external and persistent location (ie. File, Database, SharedPreferences) so
 * that the data can persist if the process is ever killed.  For simplicity, in this sample the
 * data will only be stored in memory.
 */
internal class WeatherDataPoint(var day: String, var degrees: Int)

/**
 * The AppWidgetProvider for our sample weather widget.
 */
class WeatherDataProvider : ContentProvider() {
    object Columns {
        const val ID = "_id"
        const val DAY = "day"
        const val TEMPERATURE = "temperature"
    }

    override fun onCreate(): Boolean {
        val preferences = context!!.getSharedPreferences(context!!.packageName + "_preferences", Context.MODE_PRIVATE)
        val sevenDay = preferences.getString("7DAY_EXT_WIDGET", "No data")!!
        val days = sevenDay.split("\n\n").dropLastWhile { it.isEmpty() }.toMutableList()
        if (days.size > 1) {
            days[0] = preferences.getString("CC_WIDGET", "No data")!!
            weatherDataPoints = (0 until days.lastIndex).map{ WeatherDataPoint(days[it] + "\n", 0) }
        }
        return true
    }

    @Synchronized
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        // assert(uri.pathSegments.isEmpty())
        // In this sample, we only query without any parameters, so we can just return a cursor to
        // all the weather data.
        val matrixCursor = MatrixCursor(arrayOf(Columns.ID, Columns.DAY, Columns.TEMPERATURE))
        weatherDataPoints.indices.forEach {
            val data = weatherDataPoints[it]
            matrixCursor.addRow(arrayOf(it, data.day, data.degrees))
        }
        return matrixCursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.weatherlistwidget.temperature"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0

    @Synchronized
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // assert(uri.pathSegments.size == 1)
        // In this sample, we only update the content provider individually for each row with new
        // temperature values.
        val index = To.int(uri.pathSegments[0])
        //val c = MatrixCursor(arrayOf(Columns.ID, Columns.DAY, Columns.TEMPERATURE))
        //assert(0 <= index && index < sData.size)
        if (weatherDataPoints.size > index) {
            val data = weatherDataPoints[index]
            data.day = values!!.getAsString(Columns.DAY)
        }
        // Notify any listeners that the data backing the content provider has changed, and return
        // the number of rows affected.
        context!!.contentResolver.notifyChange(uri, null)
        return 1
    }

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://${GlobalVariables.packageNameAsString}.weatherlistwidget.provider")
        /**
         * Generally, this data will be stored in an external and persistent location (ie. File,
         * Database, SharedPreferences) so that the data can persist if the process is ever killed.
         * For simplicity, in this sample the data will only be stored in memory.
         */
        private var weatherDataPoints = listOf<WeatherDataPoint>()
    }
}
