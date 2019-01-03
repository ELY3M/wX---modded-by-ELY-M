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

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.spc.SPCFireOutlookActivity
import joshuatee.wx.util.UtilityString

import android.app.Notification
import android.content.Context
import android.graphics.Color

import joshuatee.wx.Extensions.*

internal object UtilityNotificationSPCFW {

    fun locationNeedsSPCFW(): Boolean {
        return (0 until Location.numLocations).any {
            MyApplication.locations.getOrNull(it)?.notificationSpcfw ?: false
        }
    }

    private fun sendSPCFWNotif(
        context: Context,
        locNum: String,
        day: Int,
        threatLevel: String,
        validTime: String
    ): String {
        var notifUrls = ""
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val noMain: String
        val noBody: String
        val noSummary: String
        val locLabelStr: String
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        locLabelStr = "(" + Location.getName(locNumInt) + ") "
        val dayStr = "SPC FWDY$day"
        noMain = "$locLabelStr$dayStr $threatLevel"
        var detailRaw = threatLevel.replace("<.*?>".toRegex(), " ")
        detailRaw = detailRaw.replace("&nbsp".toRegex(), " ")
        noBody = detailRaw
        noSummary = noBody
        val objPI = ObjectPendingIntents(context, SPCFireOutlookActivity::class.java)
        val cancelStr = "spcfwloc" + day.toString() + locNum + threatLevel + validTime
        if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(
                context,
                cancelStr
            ))
        ) {
            val sound = MyApplication.locations[locNumInt].sound && !inBlackout
            val notifObj = ObjectNotification(
                context,
                sound,
                noMain,
                noBody,
                objPI.resultPendingIntent,
                MyApplication.ICON_ALERT,
                noSummary,
                Notification.PRIORITY_DEFAULT,
                Color.YELLOW,
                MyApplication.ICON_ACTION,
                objPI.resultPendingIntent2,
                context.resources.getString(R.string.read_aloud)
            )
            val noti = UtilityNotification.createNotifBigTextWithAction(notifObj)
            notifObj.sendNotification(context, cancelStr, 1, noti)
        }
        notifUrls += cancelStr + MyApplication.notificationStrSep
        return notifUrls
    }

    fun sendSPCFWD12LocationNotifs(context: Context): String {
        var notifUrls = ""
        var retStr: String
        val threatList = mutableListOf<String>()
        threatList.add("EXTR")
        threatList.add("CRIT")
        threatList.add("ELEV")
        threatList.add("SDRT")
        threatList.add("IDRT")
        var urlBlob: String
        for (day in 1..2) {
            val urlLocal =
                "${MyApplication.nwsSPCwebsitePrefix}/products/fire_wx/fwdy" + day.toString() + ".html"
            urlBlob = UtilityString.getHTMLandParse(
                urlLocal,
                "CLICK FOR <a href=.(.*?txt).>DAY [12] FIREWX AREAL OUTLINE PRODUCT .KWNSPFWFD[12].</a>"
            )
            urlBlob = "${MyApplication.nwsSPCwebsitePrefix}$urlBlob"
            var html = urlBlob.getHtmlSep()
            val validTime = html.parse("VALID TIME ([0-9]{6}Z - [0-9]{6}Z)")
            html = html.replace("<br>", " ")
            val htmlBlob =
                html.parse("FIRE WEATHER OUTLOOK POINTS DAY " + day.toString() + "(.*?&)&") // was (.*?)&&
            for (m in threatList.indices) {
                retStr = ""
                val threatLevelCode = threatList[m]
                val htmlList = htmlBlob.parseColumn(threatLevelCode.substring(1) + "(.*?)[A-Z&]")
                for (h in htmlList.indices) {
                    val coords = htmlList[h].parseColumn("([0-9]{8}).*?")
                    var xStrTmp: String
                    var yStrTmp: String
                    for (temp in coords) {
                        xStrTmp = temp.substring(0, 4)
                        yStrTmp = temp.substring(4, 8)
                        if (yStrTmp.matches("^0".toRegex())) {
                            yStrTmp = yStrTmp.replace("^0".toRegex(), "")
                            yStrTmp += "0"
                        }
                        xStrTmp = UtilityString.addPeriodBeforeLastTwoChars(xStrTmp)
                        yStrTmp = UtilityString.addPeriodBeforeLastTwoChars(yStrTmp)
                        try {
                            var tmpDbl = yStrTmp.toDoubleOrNull() ?: 0.0
                            if (tmpDbl < 40.00) {
                                tmpDbl += 100
                                yStrTmp = tmpDbl.toString()
                            }
                        } catch (e: Exception) {
                            UtilityLog.HandleException(e)
                        }
                        retStr = "$retStr$xStrTmp $yStrTmp "
                    }
                    retStr += ":"
                    retStr = retStr.replace(" :", ":")
                    retStr = retStr.replace(
                        " 99.99 99.99 ",
                        " "
                    ) // need for the way SPC ConvO seperates on 8 's
                } // end looping over polygons of one threat level
                val x = mutableListOf<Double>()
                val y = mutableListOf<Double>()
                var locNum: String
                var locXDbl: Double
                var locYDbl: Double
                var i: Int
                val tmpArr = MyApplication.colon.split(retStr)
                var z = 0
                var test: Array<String>
                while (z < tmpArr.size) {
                    test = MyApplication.space.split(tmpArr[z])
                    x.clear()
                    y.clear()
                    i = 0
                    while (i < test.size) {
                        if (i and 1 == 0) {
                            x.add(test[i].toDoubleOrNull() ?: 0.0)
                        } else {
                            y.add((test[i].toDoubleOrNull() ?: 0.0) * -1)
                        }
                        i += 1
                    }
                    // inject bounding box coords if first doesn't equal last
                    // focus on east coast for now
                    //
                    // 52,-130               52,-62
                    // 21,-130                21,-62
                    //
                    if (y.size >= 3 && x.size >= 3 && x.size == y.size) {
                        val poly2 = ExternalPolygon.Builder()
                        for (j in x.indices) {
                            poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat()))
                        }
                        val polygon2 = poly2.build()
                        for (n in 1..Location.numLocations) {
                            locNum = n.toString()
                            if (MyApplication.locations.getOrNull(n - 1)?.notificationSpcfw == true) {
                                // if location is watching for MCDs pull ib lat/lon and interate over polygons
                                // call secondary method to send notif if required
                                locXDbl = MyApplication.locations[n - 1].x.toDoubleOrNull() ?: 0.0
                                locYDbl = MyApplication.locations[n - 1].y.toDoubleOrNull() ?: 0.0
                                val contains = polygon2.contains(
                                    ExternalPoint(
                                        locXDbl.toFloat(),
                                        locYDbl.toFloat()
                                    )
                                )
                                if (contains) {
                                    if (!notifUrls.contains("spcfwloc" + day.toString() + locNum))
                                        notifUrls += sendSPCFWNotif(
                                            context,
                                            locNum,
                                            day,
                                            threatLevelCode,
                                            validTime
                                        )
                                }
                            }
                        }
                    }
                    z += 1
                } // end loop over polygons for specific day
            } // end loop over treat level
        } // end loop of day 1-3
        return notifUrls
    }
}

