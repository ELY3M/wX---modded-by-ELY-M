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

import android.app.job.JobParameters
import android.app.job.JobService
import joshuatee.wx.util.Utility

import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityTime
import java.util.concurrent.RejectedExecutionException

class WXJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        // Note: this is preformed on the main thread.
        try {
            BackgroundFetch(this).getContent()
            UtilityLog.d("wx", "job service ran BackgroundFetch")
        } catch (e: RejectedExecutionException) {
            UtilityLog.handleException(e)
        }

        Utility.writePref(
            this,
            "JOBSERVICE_TIME_LAST_RAN",
            UtilityTime.getCurrentLocalTimeAsString()
        )

        // below was commented out till 2018-06-02 and was causing wakelock issues
        if (android.os.Build.VERSION.SDK_INT > 20) {
            jobFinished(params, false)
        }
        return true
    }

    // Stopping jobs if our job requires change.

    override fun onStopJob(params: JobParameters): Boolean = false
} 


