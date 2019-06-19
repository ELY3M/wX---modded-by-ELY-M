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

package joshuatee.wx.fragments

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityString
import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.activitiesmisc.SunMoonActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRender
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.util.Utility

object UtilityLocationFragment {

    private val windDirectionMap = mapOf(
            "north" to "N",
            "north northeast" to "NNE",
            "northeast" to "NE",
            "east northeast" to "ENE",
            "east" to "E",
            "east southeast" to "ESE",
            "south southeast" to "SSE",
            "southeast" to "SE",
            "south" to "S",
            "south southwest" to "SSW",
            "southwest" to "SW",
            "west southwest" to "WSW",
            "west" to "W",
            "west northwest" to "WNW",
            "northwest" to "NW",
            "north northwest" to "NNW"
    )

    fun extract7DayMetrics(chunk: String): String {
        val spacing = " "
        // wind 24 to 29 mph
        val wind = UtilityString.parseMultiple(chunk, RegExp.sevenDayWind1, 2)
        // wind around 9 mph
        val wind2 = chunk.parse(RegExp.sevenDayWind2)
        // 5 to 10 mph after
        val wind3 = UtilityString.parseMultiple(chunk, RegExp.sevenDayWind4, 2)
        // around 5 mph after
        val wind4 = chunk.parse(RegExp.sevenDayWind5)
        // 5 to 7 mph in
        val wind5 = UtilityString.parseMultiple(chunk, RegExp.sevenDayWind6, 2)
        // around 6 mph.
        val wind7 = chunk.parse(RegExp.sevenDayWind7)
        // with gusts as high as 21 mph
        var gust = chunk.parse(RegExp.sevenDayWind3)
        // 5 to 7 mph.
        val wind9 = UtilityString.parseMultiple(chunk, RegExp.sevenDayWind9, 2)
        // Winds could gusts as high as 21 mph.
        if (gust == "") {
            gust = chunk.parse(RegExp.sevenDayWind8)
        }
        gust = if (gust != "") {
            " G $gust mph"
        } else {
            " mph"
        }
        if (wind[0] != "" && wind[1] != "") {
            return spacing + wind[0] + "-" + wind[1] + gust
        } else if (wind2 != "") {
            return spacing + wind2 + gust
        } else if (wind3[0] != "" && wind3[1] != "") {
            return spacing + wind3[0] + "-" + wind3[1] + gust
        } else if (wind4 != "") {
            return spacing + wind4 + gust
        } else if (wind5[0] != "" && wind5[1] != "") {
            return spacing + wind5[0] + "-" + wind5[1] + gust
        } else if (wind7 != "") {
            return spacing + wind7 + gust
        } else if (wind9[0] != "" && wind9[1] != "") {
            return spacing + wind9[0] + "-" + wind9[1] + gust
        } else {
            return ""
        }
    }

    fun setNwsIconSize(): Int =
            (MyApplication.dm.widthPixels * (MyApplication.nwsIconSize / 100f)).toInt()

    fun extractWindDirection(chunk: String): String {
        val windDir1 = chunk.parseLastMatch(RegExp.sevenDayWinddir1)
        val windDir2 = chunk.parseLastMatch(RegExp.sevenDayWinddir2)
        val windDir3 = chunk.parseLastMatch(RegExp.sevenDayWinddir3)
        val windDir4 = chunk.parseLastMatch(RegExp.sevenDayWinddir4)
        val windDir5 = chunk.parseLastMatch(RegExp.sevenDayWinddir5)
        val windDir6 = chunk.parseLastMatch(RegExp.sevenDayWinddir6)
        val windDir7 = chunk.parseLastMatch(RegExp.sevenDayWinddir7)
        var retStr = ""
        when {
            windDir4 != "" -> retStr = windDir4
            windDir3 != "" -> retStr = windDir3
            windDir2 != "" -> retStr = windDir2
            windDir1 != "" -> retStr = windDir1
            windDir5 != "" -> retStr = windDir5
            windDir6 != "" -> retStr = windDir6
            windDir7 != "" -> retStr = windDir7
        }
        return if (retStr == "") {
            ""
        } else {
            val ret = windDirectionMap[retStr.toLowerCase()]
            if (ret != null) {
                " $ret"
            } else {
                ""
            }
        }
    }

    fun extractTemperature(blob: String): String {
        var temp = blob.parse(RegExp.nws7DayTemp1)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp2)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp3)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp4)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp5)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp6)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp7)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp8)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp9)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp10)
        if (temp != "") {
            return temp
        }
        temp = blob.parse(RegExp.nws7DayTemp11)
        if (temp != "") {
            return temp
        }
        return temp
    }

    fun extractCanadaTemperature(blob: String): String {
        var temp = blob.parse(RegExp.ca7DayTemp1)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp2)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp3)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp4)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp5)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp6)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp7)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp8)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp9)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp10)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(RegExp.ca7DayTemp11)
        if (temp != "") return "0"
        temp = blob.parse(RegExp.ca7DayTemp12)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp13)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp14)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp15)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp16)
        if (temp != "") return "0"
        temp = blob.parse(RegExp.ca7DayTemp17)
        if (temp != "") return "0"
        temp = blob.parse(RegExp.ca7DayTemp18)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp19)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp20)
        if (temp != "") return "0"
        temp = blob.parse(RegExp.ca7DayTemp21)
        if (temp != "") return temp
        temp = blob.parse(RegExp.ca7DayTemp22)
        if (temp != "") return "0"
        return temp
    }

    fun extractCanadaWindDirection(forecast: String): String {
        var windDirection = forecast.parse(RegExp.ca7DayWinddir1)
        return if (windDirection == "") {
            windDirection = forecast.parse(RegExp.ca7DayWinddir2)
            " " + windDirectionMap[windDirection]
        } else {
            " " + windDirectionMap[windDirection]
        }
    }

    fun extractCanadaWindSpeed(forecast: String): String {
        val windSpeedRange = UtilityString.parseMultiple(forecast, RegExp.ca7DayWindspd1, 2)
        val windSpeed = forecast.parse(RegExp.ca7DayWindspd2)
        var gust = ""
        if (forecast.contains("gusting")) {
            gust = " G " + forecast.parse(RegExp.ca7DayWindspd3)
        }
        if (windSpeedRange.size > 1 && windSpeedRange[0] != "" && windSpeedRange[1] != "") {
            return " " + windSpeedRange[0] + "-" + windSpeedRange[1] + gust + " km/h"
        }
        return if (windSpeed == "") {
            ""
        } else {
            "$windSpeed$gust km/h"
        }
    }

    fun handleIconTap(
            stringName: String,
            wxglRender: WXGLRender?,
            activityReference: Context,
            fnRefresh: () -> Unit,
            fnResetRadarView: () -> Unit,
            fnGetRadars: () -> Unit
    ) {
        when {
            stringName.contains("Edit Location..") -> ObjectIntent(
                    activityReference,
                    SettingsLocationGenericActivity::class.java,
                    SettingsLocationGenericActivity.LOC_NUM,
                    arrayOf(Location.currentLocationStr, "")
            )
            stringName.contains("Sun/Moon data") -> ObjectIntent(
                    activityReference,
                    SunMoonActivity::class.java
            )
            stringName.contains("Force Data Refresh") -> fnRefresh()
            stringName.contains("Radar type: Reflectivity") -> {
                wxglRender?.product = "N0Q"
                fnGetRadars()
            }
            stringName.contains("Radar type: Velocity") -> {
                wxglRender?.product = "N0U"
                fnGetRadars()
            }
            stringName.contains("Reset zoom and center") -> fnResetRadarView()
            else -> {
                val ridContext = stringName.split(":")[0]
                var stateContext = Utility.readPref("RID_LOC_$ridContext", "")
                stateContext = stateContext.split(",")[0]
                ObjectIntent(
                        activityReference,
                        WXGLRadarActivity::class.java,
                        WXGLRadarActivity.RID,
                        arrayOf(ridContext, stateContext, wxglRender!!.product, "")
                )
            }
        }
    }
}
