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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.widget.RemoteViews
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UtilityWidget
import joshuatee.wx.WX
import joshuatee.wx.fragments.UtilityNWS
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload

import joshuatee.wx.util.UtilityImg

class ObjectWidgetCC(context: Context) {

    val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.widget_cc_layout)

    init {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val widgetLocNumInt = (widgetLocNum.toIntOrNull() ?: 0) - 1
        val cc = Utility.readPref(context, "CC_WIDGET", "No data")
        val iconUrl = Utility.readPref(context, "CC_WIDGET_ICON_URL", "NULL")
        val updtime = Utility.readPref(context, "UPDTIME_WIDGET", "No data")
        val sevenDay = Utility.readPref(context, "7DAY_EXT_WIDGET", "No data")
        var bmCc = UtilityImg.getBlankBitmap()
        if (Location.isUS(widgetLocNumInt)) {
            bmCc = UtilityNWS.getIconV2(context, iconUrl)
        }
        val sep = " - "
        val tmpArrCc = cc.split(sep).dropLastWhile { it.isEmpty() }
        val tempArr: List<String>
        val ccArr: List<String>
        var ccTimeFormatted = ""
        if (tmpArrCc.size > 4 && Location.isUS(widgetLocNumInt)) {
            tempArr = tmpArrCc[0].split("/").dropLastWhile { it.isEmpty() }
            remoteViews.setTextViewText(R.id.wind, tmpArrCc[2])
            ccArr = updtime.split(" ")
            if (ccArr.size > 2) ccTimeFormatted = ccArr[0] + " " + ccArr[1]
            remoteViews.setTextViewText(R.id.pressure, tmpArrCc[1])
            remoteViews.setTextViewText(R.id.visibility, tmpArrCc[3])
            remoteViews.setTextColor(R.id.bigdewpt, MyApplication.widgetTextColor)
            remoteViews.setTextColor(R.id.wind, MyApplication.widgetTextColor)
            remoteViews.setTextColor(R.id.pressure, MyApplication.widgetTextColor)
            remoteViews.setTextColor(R.id.visibility, MyApplication.widgetTextColor)
            remoteViews.setTextViewText(
                R.id.location,
                Location.getName(widgetLocNumInt) + " " + UtilityDownload.getSunriseSunsetShort(
                    context,
                    (widgetLocNumInt + 1).toString()
                )
            )
            remoteViews.setTextViewText(R.id.updatetime, ccTimeFormatted)
            remoteViews.setTextColor(R.id.location, MyApplication.widgetTextColor)
            remoteViews.setTextColor(R.id.updatetime, MyApplication.widgetTextColor)
            remoteViews.setTextViewText(R.id.bigtemp, tempArr[0])
            remoteViews.setTextViewText(R.id.bigdewpt, tempArr[1].replace("^ ".toRegex(), ""))
            remoteViews.setTextColor(R.id.bigtemp, MyApplication.widgetHighlightTextColor)
        }
        if (!MyApplication.widgetCCShow7Day) {
            remoteViews.setTextViewText(R.id.text4, sevenDay)
            remoteViews.setTextColor(R.id.text4, MyApplication.widgetTextColor)
        }
        if (android.os.Build.VERSION.SDK_INT > 20) {
            val wbIcon = UtilityImg.vectorDrawableToBitmap(
                context,
                R.drawable.ic_navigation_white_24dp,
                MyApplication.widgetHighlightTextColor
            )
            var windBardRotate = 0.0f
            if (tmpArrCc.size > 2) {
                val tmpWindArr = MyApplication.space.split(tmpArrCc[2])
                var windDirStr = ""
                if (tmpWindArr.isNotEmpty()) windDirStr = tmpWindArr[0]
                windBardRotate = when (windDirStr) {
                    "N" -> 180f
                    "NE" -> 225f
                    "E" -> 270f
                    "SE" -> 315f
                    "S" -> 0f
                    "NW" -> 135f
                    "W" -> 90f
                    "SW" -> 45f
                    else -> 1000f
                }
            }
            val scaleFactor = MyApplication.deviceScale / 3.0f * 1.25f
            val matrix = Matrix()
            matrix.postRotate(windBardRotate, 100f, 100f)
            var rotatedWb =
                Bitmap.createBitmap(wbIcon, 0, 0, wbIcon.width, wbIcon.height, matrix, true)
            rotatedWb = Bitmap.createScaledBitmap(
                rotatedWb,
                (wbIcon.width * scaleFactor).toInt(),
                (wbIcon.height * scaleFactor).toInt(),
                false
            )
            remoteViews.setImageViewUri(R.id.wind_barb, Uri.parse(""))
            if (windBardRotate < 500) remoteViews.setImageViewBitmap(R.id.wind_barb, rotatedWb)
        }
        remoteViews.setImageViewUri(R.id.iv, Uri.parse(""))
        remoteViews.setImageViewBitmap(R.id.iv, bmCc)
        if (!MyApplication.widgetPreventTap) {
            UtilityWidget.setupIntent(context, remoteViews, WX::class.java, R.id.layout, "WX")
        }
    }
}


