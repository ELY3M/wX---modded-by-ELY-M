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

package joshuatee.wx.models

import android.graphics.PointF
import androidx.appcompat.widget.Toolbar
import android.widget.ArrayAdapter

import java.sql.Date
import java.util.Calendar
import java.util.TimeZone

import joshuatee.wx.MyApplication
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.TouchImageView2

object UtilityModels {

    fun moveForward(spinnerTime: ObjectSpinner) {
        var timeTmp = spinnerTime.selectedItemPosition
        timeTmp += 1
        if (timeTmp == spinnerTime.size()) {
            timeTmp = 0
        }
        spinnerTime.setSelection(timeTmp)
    }

    fun moveBack(spinnerTime: ObjectSpinner) {
        var timeTmp = spinnerTime.selectedItemPosition
        timeTmp -= 1
        if (timeTmp == -1) {
            timeTmp = spinnerTime.size() - 1
        }
        spinnerTime.setSelection(timeTmp)
    }

    fun parmInArray(arr: List<String>, parm: String): Boolean = arr.contains(parm)

    fun convertTimeRuntoTimeString(runStr: String, timeStrF: String, showDate: Boolean): String {
        var timeStr = timeStrF
        // in response to timeStr coming in as the following on rare occasions we need to truncate
        // 000 Wed 8pm
        // example input GFS 06Z would have repeated calls to this method as follows
        // 06 000
        // 06 003
        // 006 009
        timeStr = UtilityStringExternal.truncate(timeStr, 3)
        val runInt = runStr.toIntOrNull() ?: 0
        val timeInt = timeStr.toIntOrNull() ?: 0
        // realTimeGmt - the time in GMT as related to the current model run looking forward in hours
        val realTimeGmt = runInt + timeInt
        val tz = TimeZone.getDefault()
        val now = Date(System.currentTimeMillis())
        // offsetFromUtc , example for EDT -14400 ( in seconds )
        val offsetFromUtc = tz.getOffset(now.time) / 1000
        val realTime = realTimeGmt + offsetFromUtc / 60 / 60
        var hourOfDay = realTime % 24
        var amPm: String
        if (hourOfDay > 11) {
            amPm = "pm"
            if (hourOfDay > 12) {
                hourOfDay -= 12
            }
        } else {
            amPm = "am"
        }
        var day = realTime / 24
        if (hourOfDay < 0) {
            hourOfDay += 12
            amPm = "pm"
            day -= 1
        }
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hourOfDayLocal = calendar.get(Calendar.HOUR_OF_DAY)
        val calendar2 = Calendar.getInstance()
        calendar2.set(Calendar.HOUR_OF_DAY, runInt)
        calendar2.add(
            Calendar.HOUR_OF_DAY,
            timeInt + offsetFromUtc / 60 / 60
        ) // was 2*offsetFromUtc/60/60
        val dayOfMonth = calendar2.get(Calendar.DAY_OF_MONTH)
        val month = 1 + calendar2.get(Calendar.MONTH)
        if (runInt >= 0 && runInt < -offsetFromUtc / 60 / 60 && hourOfDayLocal - offsetFromUtc / 60 / 60 >= 24) {
            day += 1
        }
        var futureDay = ""
        when ((dayOfWeek + day) % 7) {
            Calendar.SUNDAY -> futureDay = "Sun"
            Calendar.MONDAY -> futureDay = "Mon"
            Calendar.TUESDAY -> futureDay = "Tue"
            Calendar.WEDNESDAY -> futureDay = "Wed"
            Calendar.THURSDAY -> futureDay = "Thu"
            Calendar.FRIDAY -> futureDay = "Fri"
            0 -> futureDay = "Sat"
            else -> {
            }
        }
        return if (showDate) {
            futureDay + "  " + hourOfDay.toString() + amPm + " (" + month.toString() + "/" + dayOfMonth.toString() + ")"
        } else {
            futureDay + "  " + hourOfDay.toString() + amPm
        }
    }

    fun updateTime(
        runF: String,
        modelCurrentTimeF: String,
        listTime: MutableList<String>,
        dataAdapterTime: ArrayAdapter<String>,
        prefix: String,
        showDate: Boolean
    ) {
        var run = runF
        var modelCurrentTime = modelCurrentTimeF
        // run is the current run , ie 12Z
        // modelCurrentTime is the most recent model run
        // listTime is a list of all times for a model ... 000 , 003,006, etc
        // dataAdapterTime is the adapter passed so that we can notify on changes
        // prefix allows us to handle times such as f000 ( SPC SREF )
        // in response to time_str coming in as the following on rare occasions we need to truncate
        // 000 Wed 8pm
        var tmpStr: String
        run = run.replace("Z", "")
        run = run.replace("z", "")
        modelCurrentTime = modelCurrentTime.replace("Z", "")
        modelCurrentTime = modelCurrentTime.replace("z", "")
        if (modelCurrentTime != "") {
            if ((run.toIntOrNull() ?: 0) > (modelCurrentTime.toIntOrNull() ?: 0)) {
                run = ((run.toIntOrNull() ?: 0) - 24).toString()
            }
            (0 until listTime.size).forEach {
                tmpStr = MyApplication.space.split(listTime[it])[0].replace(prefix, "")
                listTime[it] = prefix + tmpStr + " " +
                        UtilityModels.convertTimeRuntoTimeString(run, tmpStr, showDate)
            }
            dataAdapterTime.notifyDataSetChanged()
        }
    }

    fun setSubtitleRestoreIMGXYZOOM(
        img: MutableList<TouchImageView2>,
        toolbar: Toolbar,
        str: String
    ) {
        val x = FloatArray(img.size)
        val y = FloatArray(img.size)
        val z = FloatArray(img.size)
        val poi = mutableListOf<PointF>()
        (0 until img.size).forEach {
            z[it] = img[it].currentZoom
            poi.add(img[it].scrollPosition)
            x[it] = poi[it].x
            y[it] = poi[it].y
        }
        toolbar.subtitle = str
        (0 until img.size)
            .filter { !x[it].isNaN() && !y[it].isNaN() }
            .forEach { img[it].setZoom(z[it], x[it], y[it]) }
    }
}
