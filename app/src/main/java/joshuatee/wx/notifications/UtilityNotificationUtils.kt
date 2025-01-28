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

import android.content.Context
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.Utility

object UtilityNotificationUtils {

    fun checkBlackOut(): Boolean {
        val hourCurrent = ObjectDateTime.currentHourIn24
        var inBlackout = false
        if (NotificationPreferences.alertBlackout) {
            if (hourCurrent < NotificationPreferences.alertBlackoutAmCurrent || hourCurrent >= NotificationPreferences.alertBlackoutPmCurrent) {
                inBlackout = true
            }
        }
        return inBlackout
    }

    fun checkToken(context: Context, token: String): Boolean {
        var retStatus = false
        val token1 = Utility.readPref(context, "NOTIF_STR", "")
        val token2 = Utility.readPref(context, "NOTIF_STR_OLD", "")
        var issuedStr: String = Utility.readPref(context, "NOTIF_ISSUED_STR", "")
        if (token1.contains(token) || token2.contains(token) || issuedStr.contains(token)) retStatus =
            true
        if (!issuedStr.contains(token)) {
            val tokenCnt = issuedStr.indices.count { issuedStr[it] == ',' }
            val issuedStrOrig = issuedStr
            val maxCnt = 40
            if (tokenCnt > maxCnt) {
                issuedStr = ""
                val issuedTokens = RegExp.comma.split(issuedStrOrig)
                for (j in maxCnt / 2 until issuedTokens.size) {
                    issuedStr = issuedStr + "," + issuedTokens[j]
                }
            }
            Utility.writePref(context, "NOTIF_ISSUED_STR", "$issuedStr,$token")
        }
        return retStatus
    }
}
