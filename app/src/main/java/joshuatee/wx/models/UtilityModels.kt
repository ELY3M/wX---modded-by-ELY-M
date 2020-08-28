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

package joshuatee.wx.models

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.startAnimation

import java.sql.Date
import java.util.Calendar
import java.util.TimeZone

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.*
import kotlinx.coroutines.*

object UtilityModels {

    fun getContentNonSpinner(context: Context, om: ObjectModelNoSpinner, overlayImg: List<String>, uiDispatcher: CoroutineDispatcher): Job =
            GlobalScope.launch(uiDispatcher) {
                om.sectorInt = om.sectors.indexOf(om.sector)
                if (om.truncateTime) {
                    om.time = UtilityStringExternal.truncate(om.time, om.timeTruncate)
                }
                writePrefs(context, om)
                withContext(Dispatchers.IO) { (0 until om.numPanes).forEach { om.displayData.bitmap[it] = om.getImage(it, overlayImg) } }
                (0 until om.numPanes).forEach {
                    if (om.numPanes > 1) {
                        UtilityImg.resizeViewAndSetImage(context, om.displayData.bitmap[it], om.displayData.img[it])
                    } else {
                        om.displayData.img[it].setImageBitmap(om.displayData.bitmap[it])
                    }
                }
                om.animRan = false
                if (!om.firstRun) {
                    (0 until om.numPanes).forEach {
                        UtilityImg.imgRestorePosnZoom(context, om.displayData.img[it], om.modelProvider + om.numPanes.toString() + it.toString())
                    }
                    if (UIPreferences.fabInModels && om.numPanes < 2) {
                        om.fab1?.visibility = View.VISIBLE
                        om.fab2?.visibility = View.VISIBLE
                    }
                    om.firstRun = true
                }
                updateToolbarLabels(om)
                om.imageLoaded = true
            }

    private fun updateToolbarLabels(om: ObjectModelNoSpinner) {
        if (om.numPanes > 1) {
            setSubtitleRestoreIMGXYZOOM(
                    om.displayData.img,
                    om.toolbar,
                    "(" + (om.curImg + 1).toString() + ")" + om.displayData.param[0] + "/" + om.displayData.param[1]
            )
            om.miStatusParam1.title = om.displayData.paramLabel[0]
            om.miStatusParam2.title = om.displayData.paramLabel[1]
        } else {
            om.toolbar.subtitle = om.displayData.paramLabel[0]
            om.miStatusParam1.title = om.displayData.paramLabel[0]
        }
    }


    fun getAnimate(om: ObjectModelNoSpinner, overlayImg: List<String>, uiDispatcher: CoroutineDispatcher): Job =
            GlobalScope.launch(uiDispatcher) {
                withContext(Dispatchers.IO) {
                    (0 until om.numPanes).forEach { om.displayData.animDrawable[it] = om.getAnimate(it, overlayImg) }
                }
                (0 until om.numPanes).forEach { om.displayData.animDrawable[it].startAnimation(om.displayData.img[it]) }
                om.animRan = true
            }

    private fun writePrefs(context: Context, om: ObjectModelNoSpinner) {
        Utility.writePref(context, om.prefSector, om.sector)
        (0 until om.numPanes).forEach {
            Utility.writePref(context, om.prefParam + it.toString(), om.displayData.param[it])
            Utility.writePref(context, om.prefParamLabel + it.toString(), om.displayData.paramLabel[it])
        }
    }

    fun legacyShare(activity: Activity, context: Context, animRan: Boolean, om: ObjectModelNoSpinner) {
        if (animRan)
            UtilityShare.animGif(
                    context,
                    om.prefModel + " " + om.displayData.paramLabel[0] + " " + om.timeIndex.toString(),
                    om.displayData.animDrawable[0]
            )
        else
            UtilityShare.bitmap(
                    activity,
                    context,
                    om.prefModel + " " + om.displayData.paramLabel[0] + " " + om.timeIndex.toString(),
                    om.displayData.bitmap[0]
            )
    }

    // FIXME don't need this - to simple
    fun parameterInList(list: List<String>, parameter: String) = list.contains(parameter)

    fun convertTimeRunToTimeString(runStr: String, timeStringOriginal: String, showDate: Boolean): String {
        var timeStr = timeStringOriginal
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
        val now = Date(UtilityTime.currentTimeMillis())
        // offsetFromUtc , example for EDT -14400 ( in seconds )
        val offsetFromUtc = tz.getOffset(now.time) / 1000
        val realTime = realTimeGmt + offsetFromUtc / 60 / 60
        var hourOfDay = realTime % 24
        var amPm: String
        if (hourOfDay > 11) {
            amPm = "pm"
            if (hourOfDay > 12) hourOfDay -= 12
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
        calendar2.add(Calendar.HOUR_OF_DAY, timeInt + offsetFromUtc / 60 / 60) // was 2*offsetFromUtc/60/60
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
            else -> {}
        }
        return if (showDate) {
            "$futureDay  $hourOfDay$amPm ($month/$dayOfMonth)"
        } else {
            "$futureDay  $hourOfDay$amPm"
        }
    }

    fun updateTime(runOriginal: String, modelCurrentTimeF: String, listTime: MutableList<String>, prefix: String, showDate: Boolean) {
        var run = runOriginal.replace("Z", "").replace("z", "")
        val modelCurrentTime = modelCurrentTimeF.replace("Z", "").replace("z", "")
        // run is the current run , ie 12Z
        // modelCurrentTime is the most recent model run
        // listTime is a list of all times for a model ... 000 , 003,006, etc
        // dataAdapterTime is the adapter passed so that we can notify on changes
        // prefix allows us to handle times such as f000 ( SPC SREF )
        // in response to time_str coming in as the following on rare occasions we need to truncate
        // 000 Wed 8pm
        if (modelCurrentTime != "") {
            if ((run.toIntOrNull() ?: 0) > (modelCurrentTime.toIntOrNull() ?: 0)) {
                run = ((run.toIntOrNull() ?: 0) - 24).toString()
            }
            (0 until listTime.size).forEach {
                val tmpStr = MyApplication.space.split(listTime[it])[0].replace(prefix, "")
                listTime[it] = prefix + tmpStr + " " + convertTimeRunToTimeString(run, tmpStr, showDate)
            }
            //dataAdapterTime.notifyDataSetChanged()
        }
    }

    fun setSubtitleRestoreIMGXYZOOM(img: MutableList<TouchImageView2>, toolbar: Toolbar, string: String) {
        val x = FloatArray(img.size)
        val y = FloatArray(img.size)
        val z = FloatArray(img.size)
        val point = mutableListOf<PointF>()
        if (img.size > 0) {
            (0 until img.size).forEach {
                z[it] = img[it].currentZoom
                if (img[it].scrollPosition != null) {
                    point.add(img[it].scrollPosition)
                } else {
                    point.add(PointF(0.5f, 0.5f))
                }
                x[it] = point[it].x
                y[it] = point[it].y
            }
            toolbar.subtitle = string
            (0 until img.size).filter { !x[it].isNaN() && !y[it].isNaN() }.forEach {
                img[it].setZoom(z[it], x[it], y[it])
            }
        }
    }
}
