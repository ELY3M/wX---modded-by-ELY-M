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

import android.app.IntentService
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import java.util.Calendar

import joshuatee.wx.audio.UtilityPlayListAutoDownload
import joshuatee.wx.util.Utility

// this service notifies the alarm manager to run AlertReceiver ( notifications ) according to the
// configured interval
//

class AlertService : IntentService("AlertService") {

    override fun onHandleIntent(intent: Intent?) {
        val alertNotificationIntervalCurrent =
            Utility.readPref(this, "ALERT_NOTIFICATION_INTERVAL", -1)
        val service = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance()
        val intent = Intent(this, AlertReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        service.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            (alertNotificationIntervalCurrent * 1000 * 60).toLong(),
            pending
        )
        UtilityPlayListAutoDownload.setAllAlarms(this)
    }
} 


