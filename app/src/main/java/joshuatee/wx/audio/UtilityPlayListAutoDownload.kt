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

package joshuatee.wx.audio

import java.util.Calendar
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityTime

object UtilityPlayListAutoDownload {

    fun setAllAlarms(context: Context) {
        val tokenSep = "T"
        val prefToken = "PLAYLIST_AUTODOWNLOAD_TIMES"
        var currHr: Int
        var currMin: Int
        val ridFav = Utility.readPref(context, prefToken, "")
        val ridArr = ridFav.split(tokenSep).dropLastWhile { it.isEmpty() }
        if (ridFav.contains(tokenSep)) {
            ridArr.indices.forEach { alarm ->
                val timeArr = ridArr[alarm].split(":").dropLastWhile { it.isEmpty() }
                currHr = timeArr[0].toIntOrNull() ?: 0
                currMin = timeArr[1].toIntOrNull() ?: 0
                setAlarm(context, alarm, currHr, currMin)
            }
        }
    }

    fun setAlarm(context: Context, pos: Int, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        var timeToAlarm = calendar.timeInMillis
        if (calendar.timeInMillis < UtilityTime.currentTimeMillis()) {
            timeToAlarm += (24 * 60 * 60 * 1000).toLong()
        }
        val intent = Intent(context, DownloadPlaylistService::class.java)
        intent.putExtra(DownloadPlaylistService.URL, "true")
        val pendingIntent = PendingIntent.getService(context, pos, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeToAlarm, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    fun cancelAlarm(context: Context, pos: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DownloadPlaylistService::class.java)
        val pendingIntent = PendingIntent.getService(context, pos, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }
}
