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

import android.app.IntentService
import android.app.Notification
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.Color
//import androidx.localbroadcastmanager.content.LocalBroadcastManager

import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.ObjectNotification
import joshuatee.wx.notifications.ObjectPendingIntents
import joshuatee.wx.notifications.UtilityNotification

class DownloadPlaylistService : IntentService("DownloadPlaylistService") {

    // this service notifies the alarm manager to run AlertReceiver (notifications) according to the
    // configured interval

    companion object {
        const val URL = ""
    }

    override fun onHandleIntent(intent: Intent?) {
        val result = UtilityPlayList.downloadAll(this)
        val arguments = intent!!.getStringExtra(URL)
        val url = "l2"
        if (arguments != "false") {
            val notificationText = "PlayList download complete"
            val resultIntent = Intent(this, SettingsPlaylistActivity::class.java)
            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addParentStack(SettingsPlaylistActivity::class.java)
            stackBuilder.addNextIntent(resultIntent)
            val objectPendingIntents = ObjectPendingIntents(this, SettingsPlaylistActivity::class.java)
            val notificationObj = ObjectNotification(
                this, false, notificationText, result, objectPendingIntents.resultPendingIntent,
                MyApplication.ICON_CURRENT, result, Notification.PRIORITY_DEFAULT, Color.YELLOW,
                MyApplication.ICON_ACTION, objectPendingIntents.resultPendingIntent2, "PLAYLIST"
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(notificationObj)
            notificationObj.sendNotification(this, url, 1, notification)
        }
    }
}
