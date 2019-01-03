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

import java.util.Locale

import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload

object UtilityNotificationTextProduct {

    const val PREF_TOKEN: String = "NOTIF_TEXT_PROD"

    fun toggle(context: Context, view: View, prodF: String) {
        val prod = prodF.toUpperCase(Locale.US)
        if (!MyApplication.notifTextProdStr.contains(prod)) {
            Utility.writePref(context, PREF_TOKEN, MyApplication.notifTextProdStr + ":" + prod)
            MyApplication.notifTextProdStr = MyApplication.notifTextProdStr + ":" + prod
            UtilityUI.makeSnackBar(view, "$prod saved to notification list")
        } else {
            MyApplication.notifTextProdStr = MyApplication.notifTextProdStr.replace(":$prod", "")
            Utility.writePref(context, PREF_TOKEN, MyApplication.notifTextProdStr)
            UtilityUI.makeSnackBar(view, "$prod removed from notification list")
            Utility.removePref(context, PREF_TOKEN + "_" + prod)
        }
    }

    fun showAll(): String = MyApplication.notifTextProdStr

    fun check(prod: String): Boolean = MyApplication.notifTextProdStr.contains(prod)

    internal fun notifyOnAll(context: Context) {
        var textProdChunk: String
        var textProdFirstline = ""
        var arrTmp: List<String>
        TextUtils.split(MyApplication.notifTextProdStr, ":").forEach { s ->
            if (s != "") {
                textProdChunk = UtilityDownload.getTextProduct(context, s)
                arrTmp = textProdChunk.split("<BR>").dropLastWhile { it.isEmpty() }
                if (arrTmp.size > 1)
                    textProdFirstline = arrTmp[0]
                // compare the first line to a stored first line, if not execute first block
                if (textProdFirstline != "")
                    if (textProdFirstline != Utility.readPref(context, PREF_TOKEN + "_" + s, "")) {
                        // send notif
                        sendTextProdNotification(context, s, textProdFirstline, textProdChunk)
                        Utility.writePref(context, PREF_TOKEN + "_" + s, textProdFirstline)
                    }
            }
        }
    }

    private fun sendTextProdNotification(
        context: Context,
        prod: String,
        firstLine: String,
        textBody: String
    ) {
        val noBody = Utility.fromHtml(textBody)
        val noSummary = Utility.fromHtml(textBody)
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val objPI = ObjectPendingIntents(
            context,
            TextScreenActivity::class.java,
            TextScreenActivity.URL,
            arrayOf(textBody, prod, ""),
            arrayOf(textBody, prod, "sound")
        )
        val sound = MyApplication.alertNotificationSoundTextProd && !inBlackout
        val notifObj = ObjectNotification(
            context,
            sound,
            prod,
            noBody,
            objPI.resultPendingIntent,
            MyApplication.ICON_CURRENT,
            noSummary,
            Notification.PRIORITY_DEFAULT,
            Color.YELLOW,
            MyApplication.ICON_ACTION,
            objPI.resultPendingIntent2,
            context.resources.getString(R.string.read_aloud)
        )
        val noti = UtilityNotification.createNotifBigTextWithAction(notifObj)
        notifObj.sendNotification(context, firstLine, 1, noti)
    }
}
