/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ObjectNotification(
    val context: Context,
    val sound: Boolean,
    val title: String,
    val text: String,
    val objectPendingIntents: ObjectPendingIntents,
    val iconAlert: Int,
    val iconAction: Int,
    val buttonStr: String,
    val priority: Int = NotificationCompat.PRIORITY_HIGH
) {

    fun send(cancelString: String) {
        val notification = UtilityNotification.createNotificationBigTextWithAction(this)
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // cancelString / 1 must be unique - since cancelString is always unique we can get away with id being 1
            notifier.notify(cancelString, 1, notification)
        }
    }
}
