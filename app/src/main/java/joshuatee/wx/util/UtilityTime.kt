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

package joshuatee.wx.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

import joshuatee.wx.Extensions.*


object UtilityTime {

    internal fun convertFromUtcForMetar(time: String): String {
        var returnTime = time
        val inputFormat = SimpleDateFormat("yyyy.MM.dd' 'HHmm", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MM-dd h:mm a", Locale.US)
        try {
            val date = inputFormat.parse(time)
            returnTime = outputFormat.format(date!!)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return returnTime
    }

    fun genModelRuns(time: String, hours: Int): List<String> {
        val listRun = mutableListOf<String>()
        val format = SimpleDateFormat("yyyyMMddHH", Locale.US)
        var parsed: Date
        val oneMinuteInMillis: Long = 60000
        var t: Long = 0
        (1 until 4).forEach {
            try {
                parsed = format.parse(time)!!
                t = parsed.time
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            listRun.add(format.format(Date(t - 60 * oneMinuteInMillis * it.toLong() * hours.toLong())))
        }
        return listRun
    }

    fun genModelRuns(time: String, hours: Int, timeStr: String): List<String> {
        val listRun = mutableListOf<String>()
        val format = SimpleDateFormat(timeStr, Locale.US)
        var parsed: Date
        val oneMinuteInMillis: Long = 60000
        var t: Long = 0
        (1 until 4).forEach {
            try {
                parsed = format.parse(time)!!
                t = parsed.time
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            listRun.add(format.format(Date(t - 60 * oneMinuteInMillis * it.toLong() * hours.toLong())))
        }
        return listRun
    }

    fun gmtTime(): String {
        val dateFormatGmt = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US)
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return "GMT: " + dateFormatGmt.format(Date())
    }

    fun gmtTime(format: String): String {
        val dateFormatGmt = SimpleDateFormat(format, Locale.US)
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(Date())
    }

    fun getDateAsString(format: String): String {
        val cal = Calendar.getInstance()
        val df = SimpleDateFormat(format, Locale.US)
        return df.format(cal.time)
    }

    val currentHourIn24: Int
        get() {
            val calendar = GregorianCalendar()
            val time = Date()
            calendar.time = time
            return calendar.get(Calendar.HOUR_OF_DAY)
        }

    fun radarTime(volumeScanDate: Short, volumeScanTime: Int): Date {
        val sec = ((volumeScanDate - 1) * 60 * 60 * 24 + volumeScanTime).toLong()
        val milli = sec * 1000
        val cal = Calendar.getInstance()
        cal.timeInMillis = milli
        return cal.time
    }

    fun radarTimeL2(days: Short, milliSeconds: Int): Date {
        val sec = (days - 1).toLong() * 24 * 3600 * 1000 + milliSeconds
        val cal = Calendar.getInstance()
        cal.timeInMillis = sec
        return cal.time
    }

    fun getCurrentLocalTimeAsString(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    //fun year() = Calendar.getInstance().get(Calendar.YEAR)

    fun getYear() = Calendar.getInstance().get(Calendar.YEAR)

    //fun month(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun day() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val currentHourInUtc: Int
        get() = Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.HOUR_OF_DAY)

    fun isRadarTimeOld(radarTime: String): Boolean {
        val radarTimeComponents = radarTime.split(":")
        if (radarTimeComponents.size < 3) {
            return false
        }
        val radarTimeHours = radarTimeComponents[0].toIntOrNull() ?: 0
        val radarTimeMinutes = radarTimeComponents[1].toIntOrNull() ?: 0
        val radarTimeTotalMinutes = radarTimeHours * 60 + radarTimeMinutes
        val currentTime = getCurrentLocalTimeAsString().split(" ")[1]
        val currentTimeComponents = currentTime.split(":")
        if (currentTimeComponents.size < 3) {
            return false
        }
        val currentTimeHours = currentTimeComponents[0].toIntOrNull() ?: 0
        val currentTimeMinutes = currentTimeComponents[1].toIntOrNull() ?: 0
        val currentTimeTotalMinutes = currentTimeHours * 60 + currentTimeMinutes
        if (currentTimeTotalMinutes < 30) {
            return false
        }
        if (radarTimeTotalMinutes > currentTimeTotalMinutes) {
            return true
        }
        if (radarTimeTotalMinutes < (currentTimeTotalMinutes - 20)) {
            return true
        }
        return false
    }

    fun isVtecCurrent(vtec: String ): Boolean {
        // example 190512T1252Z-190512T1545Z
        val timeRange = (vtec).parse("-([0-9]{6}T[0-9]{4})Z")
        val timeInMinutes = decodeVtecTime(timeRange)
        val currentTimeInMinutes = decodeVtecTime(getGmtTimeForVtec())
        return currentTimeInMinutes.before(timeInMinutes)
    }

    private fun decodeVtecTime(timeRangeOriginal: String): Calendar {
        // Y2K issue
        val timeRange = timeRangeOriginal.replace("T","")
        val year = ("20" + (timeRange).parse("([0-9]{2})[0-9]{4}[0-9]{4}")).toIntOrNull() ?: 0
        val month = ((timeRange).parse("[0-9]{2}([0-9]{2})[0-9]{2}[0-9]{4}")).toIntOrNull() ?: 0
        val day = ((timeRange).parse("[0-9]{4}([0-9]{2})[0-9]{4}")).toIntOrNull() ?: 0
        val hour = ((timeRange).parse("[0-9]{6}([0-9]{2})[0-9]{2}")).toIntOrNull() ?: 0
        val minute = ((timeRange).parse("[0-9]{6}[0-9]{2}([0-9]{2})")).toIntOrNull() ?: 0
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, hour, minute)
        return cal
    }

    private fun getGmtTimeForVtec(): String{
        val dateFormatGmt = SimpleDateFormat("yyMMddHHmm", Locale.US)
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(Date())
    }

    fun currentTimeMillis() = System.currentTimeMillis() // Long
}
