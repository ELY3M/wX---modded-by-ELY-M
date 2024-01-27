/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.AnimationDrawable
import android.view.View
import androidx.appcompat.widget.Toolbar
import java.sql.Date
import java.util.Calendar
import java.util.TimeZone
import joshuatee.wx.startAnimation
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityShare

object UtilityModels {

    fun getContent(context: Context, om: ObjectModel, overlayImg: List<String>) {
        om.sectorInt = om.sectors.indexOf(om.sector)
        if (om.truncateTime) {
            om.time = om.time.take(om.timeTruncate)
        }
        writePrefs(context, om)
        FutureVoid(
                {
                    (0 until om.numPanes).forEach {
                        om.displayData.bitmaps[it] = ObjectModelGet.image(om, it, overlayImg)
                    }
                },
                {
                    (0 until om.numPanes).forEach {
                        if (om.numPanes > 1) {
                            UtilityImg.resizeViewAndSetImage(context, om.displayData.bitmaps[it], om.displayData.image[it].get())
                        } else {
                            om.displayData.image[it].set(om.displayData.bitmaps[it])
                        }
                    }
                    om.animRan = false
                    if (!om.firstRun) {
                        (0 until om.numPanes).forEach {
                            om.displayData.image[it].imgRestorePosnZoom(om.modelProvider + om.numPanes.toString() + it.toString())
                        }
                        if (om.numPanes < 2) {
                            om.fab1?.visibility = View.VISIBLE
                            om.fab2?.visibility = View.VISIBLE
                        }
                        om.firstRun = true
                    }
                    updateToolbarLabels(om)
                    om.imageLoaded = true
                }
        )
    }

    private fun updateToolbarLabels(om: ObjectModel) {
        if (om.numPanes > 1) {
            setSubtitleRestoreZoom(
                    om.displayData.image,
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

    fun getAnimate(om: ObjectModel, overlayImg: List<String>) {
        FutureVoid(
                {
                    (0 until om.numPanes).forEach {
                        om.displayData.animDrawable[it] = ObjectModelGet.animate(om, it, overlayImg)
                    }
                },
                {
                    (0 until om.numPanes).forEach {
                        om.displayData.animDrawable[it].startAnimation(om.displayData.image[it])
                    }
                    om.animRan = true
                }
        )
    }

    private fun writePrefs(context: Context, om: ObjectModel) {
        Utility.writePref(context, om.prefSector, om.sector)
        (0 until om.numPanes).forEach {
            Utility.writePref(context, om.prefParam + it.toString(), om.displayData.param[it])
            Utility.writePref(context, om.prefParamLabel + it.toString(), om.displayData.paramLabel[it])
        }
    }

    fun legacyShare(activity: Activity, om: ObjectModel) {
        UtilityShare.bitmap(activity, om.prefModel + " " + om.displayData.paramLabel[0] + " " + om.timeIndex.toString(), om.displayData.bitmaps[0])
    }

    fun convertTimeRunToTimeString(runStrOriginal: String, timeStringOriginal: String, showDate: Boolean): String {
        val runStr = runStrOriginal.takeLast(2)
        var timeStr = timeStringOriginal
        // in response to timeStr coming in as the following on rare occasions we need to truncate
        // 000 Wed 8pm
        // example input GFS 06Z would have repeated calls to this method as follows
        // 06 000
        // 06 003
        // 006 009
        timeStr = timeStr.take(3)
        val runInt = To.int(runStr)
        val timeInt = To.int(timeStr)
        // realTimeGmt - the time in GMT as related to the current model run looking forward in hours
        val realTimeGmt = runInt + timeInt
        val tz = TimeZone.getDefault()
        val now = Date(ObjectDateTime.currentTimeMillis())
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
        // TODO FIXME
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

    fun convertTimeRunToTimeStringNew(runStrOrig: String, timeStrFunc: String, showDate: Boolean): String {
        val runStr = runStrOrig.takeLast(2)
        UtilityLog.d("wxRUN", runStr)
        val timeStr = timeStrFunc.split(" ")[0].take(3)
//        val a = LocalDateTime.now(ZoneOffset.UTC)
//        val time = a.withHour(To.int(runStr.takeLast(2)))
        val runInt = To.int(runStr)
        val timeInt = To.int(timeStr)
        val realTimeGmt = runInt + timeInt
        val offsetFromUtc = ObjectDateTime.offsetFromUtcInSeconds()
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
        val objectDateTime = ObjectDateTime(runInt, 0)
        objectDateTime.addHours(timeInt.toLong() + offsetFromUtc / 60 / 60)
        val month = objectDateTime.getMonth()
        val dayOfMonth = objectDateTime.getDayOfMonth()
        return if (showDate) {
            "${objectDateTime.format("E")}  $hourOfDay$amPm ($month/$dayOfMonth)"
            //"$futureDay  $hourOfDay$amPm ($month/$dayOfMonth)"
        } else {
            "${objectDateTime.format("E")}  $hourOfDay$amPm"
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
            if (To.int(run) > To.int(modelCurrentTime)) {
                run = (To.int(run) - 24).toString()
            }
            listTime.indices.forEach {
                val tmpStr = RegExp.space.split(listTime[it])[0].replace(prefix, "")
                listTime[it] = prefix + tmpStr + " " + convertTimeRunToTimeString(run, tmpStr, showDate)
            }
        }
    }

    fun setSubtitleRestoreZoom(touchImages: List<TouchImage>, toolbar: Toolbar, s: String) {
        val x = FloatArray(touchImages.size)
        val y = FloatArray(touchImages.size)
        val z = FloatArray(touchImages.size)
        val point = mutableListOf<PointF>()
        if (touchImages.isNotEmpty()) {
            touchImages.indices.forEach {
                z[it] = touchImages[it].currentZoom
                if (touchImages[it].scrollPosition != null) {
                    point.add(touchImages[it].scrollPosition!!)
                } else {
                    point.add(PointF(0.5f, 0.5f))
                }
                x[it] = point[it].x
                y[it] = point[it].y
            }
            toolbar.subtitle = s
            touchImages.indices.filter { !x[it].isNaN() && !y[it].isNaN() }.forEach {
                touchImages[it].setZoom(z[it], x[it], y[it])
            }
        }
    }

    fun getAnimation(context: Context, om: ObjectModel, getImage: (Context, ObjectModel, String) -> Bitmap): AnimationDrawable = if (om.spinnerTimeValue == -1) {
        AnimationDrawable()
    } else {
        val bitmaps = (om.timeIndex until om.times.size).map {
            if (it < om.times.size) {
                getImage(context, om, om.times[it].split(" ").getOrNull(0)
                        ?: "")
            } else {
                UtilityImg.getBlankBitmap()
            }
        }
        UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }
}
