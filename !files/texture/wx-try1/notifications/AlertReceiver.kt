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

package joshuatee.wx.notifications

import android.os.AsyncTask
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityTime

class AlertReceiver : BroadcastReceiver() {

    // This is the main code that handles notifications

    override fun onReceive(context1: Context, intent1: Intent) {
        BackgroundFetch(context1).GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        UtilityLog.d("wx", "legacy jobservice ran BackgroundFetch")
        Utility.writePref(context1, "JOBSERVICE_TIME_LAST_RAN", UtilityTime.getCurrentLocalTimeAsString())
    }
}