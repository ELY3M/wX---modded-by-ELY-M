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

package joshuatee.wx.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.RemoteViews
import joshuatee.wx.R
import joshuatee.wx.WX
import joshuatee.wx.common.RegExp
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityForecastIcon
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityTimeSunMoon

class ObjectWidgetCC(context: Context) {

    private val remoteViews = RemoteViews(context.packageName, R.layout.widget_cc_layout)

    init {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val widgetLocationNumberAsInteger = To.int(widgetLocationNumber) - 1
        val currentConditionsString = Utility.readPref(context, "CC_WIDGET", "No data")
        val iconUrl = Utility.readPref(context, "CC_WIDGET_ICON_URL", "NULL")
        val updateTime = Utility.readPref(context, "UPDTIME_WIDGET", "No data")
        val sevenDay = Utility.readPref(context, "7DAY_EXT_WIDGET", "No data")
        var bitmap = UtilityImg.getBlankBitmap()
        if (Location.isUS(widgetLocationNumberAsInteger)) {
            bitmap = UtilityForecastIcon.getIcon(context, iconUrl)
        }
        val stringSeparator = " - "
        val currentConditionsList =
            currentConditionsString.split(stringSeparator).dropLastWhile { it.isEmpty() }
        var currentConditionsTime = ""
        if (Location.isUS(widgetLocationNumberAsInteger)) {
            remoteViews.setTextViewText(
                R.id.location,
                Location.getName(widgetLocationNumberAsInteger) + " " + UtilityTimeSunMoon.getSunriseSunset(
                    context, Location.getLatLon(widgetLocationNumberAsInteger), true
                )
            )
            remoteViews.setTextColor(R.id.location, UIPreferences.widgetTextColor)
        }
        if (currentConditionsList.size > 4 && !currentConditionsList[0].contains("NA") && Location.isUS(
                widgetLocationNumberAsInteger
            )
        ) {
            val temperatureList = currentConditionsList[0].split("/").dropLastWhile { it.isEmpty() }
            remoteViews.setTextViewText(R.id.wind, currentConditionsList[2])
            val ccArr = updateTime.split(" ")
            if (ccArr.size > 2) {
                currentConditionsTime = ccArr[0] + " " + ccArr[1]
            }
            remoteViews.setTextViewText(R.id.pressure, currentConditionsList[1])
            remoteViews.setTextViewText(R.id.visibility, currentConditionsList[3])
            remoteViews.setTextColor(R.id.big_dew_point, UIPreferences.widgetTextColor)
            remoteViews.setTextColor(R.id.wind, UIPreferences.widgetTextColor)
            remoteViews.setTextColor(R.id.pressure, UIPreferences.widgetTextColor)
            remoteViews.setTextColor(R.id.visibility, UIPreferences.widgetTextColor)
            remoteViews.setTextViewText(R.id.updatetime, currentConditionsTime)
            remoteViews.setTextColor(R.id.updatetime, UIPreferences.widgetTextColor)
            remoteViews.setTextViewText(R.id.big_temp, temperatureList[0])
            remoteViews.setTextViewText(
                R.id.big_dew_point,
                temperatureList[1].replace("^ ".toRegex(), "")
            )
            remoteViews.setTextColor(R.id.big_temp, UIPreferences.widgetHighlightTextColor)
        }
        if (!UIPreferences.widgetCCShow7Day) {
            remoteViews.setTextViewText(R.id.text4, sevenDay)
            remoteViews.setTextColor(R.id.text4, UIPreferences.widgetTextColor)
        }
        val wbIcon = UtilityImg.vectorDrawableToBitmap(
            context,
            R.drawable.ic_navigation_white_24dp,
            UIPreferences.widgetHighlightTextColor
        )
        var windBardRotate = 0.0f
        if (currentConditionsList.size > 2) {
            val tmpWindArr = RegExp.space.split(currentConditionsList[2])
            var windDirStr = ""
            if (tmpWindArr.isNotEmpty()) {
                windDirStr = tmpWindArr[0]
            }
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
        val scaleFactor = UIPreferences.deviceScale / 3.0f * 1.25f
        val matrix = Matrix()
        matrix.postRotate(windBardRotate, 100f, 100f)
        var rotatedWb = Bitmap.createBitmap(wbIcon, 0, 0, wbIcon.width, wbIcon.height, matrix, true)
        rotatedWb = Bitmap.createScaledBitmap(
            rotatedWb,
            (wbIcon.width * scaleFactor).toInt(),
            (wbIcon.height * scaleFactor).toInt(),
            false
        )
//        remoteViews.setImageViewUri(R.id.wind_barb, Uri.parse(""))
        if (windBardRotate < 500) {
            remoteViews.setImageViewBitmap(R.id.wind_barb, rotatedWb)
        }
        if (!currentConditionsList[0].contains("NA")) {
//            remoteViews.setImageViewUri(R.id.iv, Uri.parse(""))
            remoteViews.setImageViewBitmap(R.id.iv, bitmap)
        }
        if (!UIPreferences.widgetPreventTap) {
            UtilityWidget.setupIntent(context, remoteViews, WX::class.java, R.id.layout, "WX")
        }
    }

    fun get() = remoteViews
}
