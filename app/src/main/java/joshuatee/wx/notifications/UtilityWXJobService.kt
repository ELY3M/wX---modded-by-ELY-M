/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.notifications


import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

object UtilityWXJobService {

    private var kJobId = 0

    fun startService(c: Context) {
        if (android.os.Build.VERSION.SDK_INT > 23) {
            start(c)
        } else {
            c.startService(Intent(c, AlertService::class.java))
        }
    }

    fun start(context: Context) {
        val alertNotificationIntervalCurrent: Int =
            Utility.readPref(context, "ALERT_NOTIFICATION_INTERVAL", -1)
        UtilityLog.d("wx", "called start for jobService")
        if (android.os.Build.VERSION.SDK_INT > 23) {
            val serviceName = ComponentName(context, WXJobService::class.java)
            val jobInfo = JobInfo.Builder(kJobId++, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setPeriodic(
                    (alertNotificationIntervalCurrent * 1000 * 60).toLong(),
                    60000
                ) // final arg is one minute
                .build()
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val result = scheduler.schedule(jobInfo)
            if (result == JobScheduler.RESULT_SUCCESS) {
                UtilityLog.d("wx", "Job scheduled successfully! jobService")
            }
        }
    }
}
