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

package joshuatee.wx.notifications


import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationManagerCompat

class ObjectNotification(
    val context: Context,
    val sound: Boolean,
    val noMain: String,
    val noBody: String,
    val resultPendingIntent: PendingIntent,
    val iconAlert: Int,
    val noSummary: String,
    val priority: Int,
    val color: Int,
    val iconAction: Int,
    val actionPendingIntent: PendingIntent,
    val buttonStr: String
) {

    fun sendNotification(context: Context, cancelString: String, id: Int, notification: Notification) {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifier.notify(cancelString, id, notification)
        }
    }
}


