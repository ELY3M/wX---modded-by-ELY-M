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

import java.util.Locale
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import androidx.core.app.NotificationCompat
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.ui.ObjectPopupMessage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload

object UtilityNotificationTextProduct {

    const val PREF_TOKEN: String = "NOTIF_TEXT_PROD"

    fun toggle(context: Context, view: View, prodOriginal: String) {
        val prod = prodOriginal.uppercase(Locale.US)
        if (!MyApplication.notifTextProdStr.contains(prod)) {
            Utility.writePref(context, PREF_TOKEN, MyApplication.notifTextProdStr + ":" + prod)
            MyApplication.notifTextProdStr = MyApplication.notifTextProdStr + ":" + prod
            ObjectPopupMessage(view, "$prod saved to notification list")
        } else {
            MyApplication.notifTextProdStr = MyApplication.notifTextProdStr.replace(":$prod", "")
            Utility.writePref(context, PREF_TOKEN, MyApplication.notifTextProdStr)
            ObjectPopupMessage(view, "$prod removed from notification list")
            Utility.removePref(context, PREF_TOKEN + "_" + prod)
        }
    }

    fun showAll() = MyApplication.notifTextProdStr

    fun check(prod: String) = MyApplication.notifTextProdStr.contains(prod)

    internal fun notifyOnAll(context: Context) {
        val matchSize = 250
        TextUtils.split(MyApplication.notifTextProdStr, ":").forEach { s ->
            if (s != "") {
                val textProdChunk = UtilityDownload.getTextProduct(context, s)
                val textProdFirstLine = if (textProdChunk.length > matchSize) {
                    textProdChunk.substring(0, matchSize - 2)
                } else {
                    textProdChunk
                }
                // compare the first line to a stored first line, if not execute first block
                if (textProdFirstLine != "") {
                    if (textProdFirstLine != Utility.readPref(context, PREF_TOKEN + "_" + s, "")) {
                        send(context, s, textProdFirstLine, textProdChunk)
                        Utility.writePref(context, PREF_TOKEN + "_" + s, textProdFirstLine)
                    }
                }
            }
        }
    }

    private fun send(context: Context, prod: String, firstLine: String, textBody: String) {
        val noBody = Utility.fromHtml(textBody)
        val noSummary = Utility.fromHtml(textBody)
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val objectPendingIntents = ObjectPendingIntents(
                context,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(textBody, prod, ""),
                arrayOf(textBody, prod, "sound")
        )
        val sound = MyApplication.alertNotificationSoundTextProd && !inBlackout
        val objectNotification = ObjectNotification(
                context,
                sound,
                prod,
                noBody,
                objectPendingIntents.resultPendingIntent,
                MyApplication.ICON_CURRENT_WHITE,
                noSummary,
                NotificationCompat.PRIORITY_HIGH,
                Color.YELLOW,
                MyApplication.ICON_ACTION,
                objectPendingIntents.resultPendingIntent2,
                context.resources.getString(R.string.read_aloud)
        )
        val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
        objectNotification.sendNotification(context, firstLine, 1, notification)
    }
}
