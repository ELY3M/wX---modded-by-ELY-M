/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.text.TextUtils
import android.view.View
import joshuatee.wx.R
import joshuatee.wx.misc.TextScreenActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.DownloadText

object NotificationTextProduct {

    const val PREF_TOKEN = "NOTIF_TEXT_PROD"

    fun toggle(context: Context, view: View, prodOriginal: String) {
        val prod = prodOriginal.uppercase(Locale.US)
        if (!NotificationPreferences.notifTextProdStr.contains(prod)) {
            Utility.writePref(context, PREF_TOKEN, NotificationPreferences.notifTextProdStr + ":" + prod)
            NotificationPreferences.notifTextProdStr = NotificationPreferences.notifTextProdStr + ":" + prod
            PopupMessage(view, "$prod saved to notification list")
        } else {
            NotificationPreferences.notifTextProdStr = NotificationPreferences.notifTextProdStr.replace(":$prod", "")
            Utility.writePref(context, PREF_TOKEN, NotificationPreferences.notifTextProdStr)
            PopupMessage(view, "$prod removed from notification list")
            Utility.removePref(context, PREF_TOKEN + "_" + prod)
        }
    }

    fun showAll() = NotificationPreferences.notifTextProdStr

    fun check(prod: String) = NotificationPreferences.notifTextProdStr.contains(prod)

    internal fun notifyOnAll(context: Context) {
        val matchSize = 250
        TextUtils.split(NotificationPreferences.notifTextProdStr, ":").forEach { s ->
            if (s != "") {
                val textProdChunk = DownloadText.byProduct(context, s)
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

    private fun send(context: Context, label: String, firstLine: String, textBody: String) {
        val text = Utility.fromHtml(textBody)
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val objectPendingIntents = ObjectPendingIntents(
                context,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(textBody, label, ""),
                arrayOf(textBody, label, "sound")
        )
        val sound = NotificationPreferences.alertNotificationSoundTextProd && !inBlackout
        val objectNotification = ObjectNotification(
                context,
                sound,
                label,
                text,
                objectPendingIntents,
                GlobalVariables.ICON_CURRENT_WHITE,
                GlobalVariables.ICON_ACTION,
                context.resources.getString(R.string.read_aloud)
        )
        objectNotification.send(firstLine)
    }
}
