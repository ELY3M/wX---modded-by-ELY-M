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

package joshuatee.wx.fragments

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.Extensions.*
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRender
import joshuatee.wx.settings.Location
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMath
import java.util.*
import java.util.regex.Pattern

object UtilityLocationFragment {

    private val nws7DayTemp1: Pattern = Pattern.compile("with a low around (-?[0-9]{1,3})\\.")
    private val nws7DayTemp2: Pattern = Pattern.compile("with a high near (-?[0-9]{1,3})\\.")
    private val nws7DayTemp3: Pattern = Pattern.compile("teady temperature around (-?[0-9]{1,3})\\.")
    private val nws7DayTemp4: Pattern = Pattern.compile("Low around (-?[0-9]{1,3})\\.")
    private val nws7DayTemp5: Pattern = Pattern.compile("High near (-?[0-9]{1,3})\\.")
    private val nws7DayTemp6: Pattern = Pattern.compile("emperature falling to around (-?[0-9]{1,3}) ")
    private val nws7DayTemp7: Pattern = Pattern.compile("emperature rising to around (-?[0-9]{1,3}) ")
    private val nws7DayTemp8: Pattern = Pattern.compile("emperature falling to near (-?[0-9]{1,3}) ")
    private val nws7DayTemp9: Pattern = Pattern.compile("emperature rising to near (-?[0-9]{1,3}) ")
    private val nws7DayTemp10: Pattern = Pattern.compile("High near (-?[0-9]{1,3}),")
    private val nws7DayTemp11: Pattern = Pattern.compile("Low around (-?[0-9]{1,3}),")
    private val sevenDayWind1: Pattern = Pattern.compile("wind ([0-9]*) to ([0-9]*) mph")
    private val sevenDayWind2: Pattern = Pattern.compile("wind around ([0-9]*) mph")
    private val sevenDayWind3: Pattern = Pattern.compile("with gusts as high as ([0-9]*) mph")
    private val sevenDayWind4: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph after")
    private val sevenDayWind5: Pattern = Pattern.compile(" around ([0-9]*) mph after ")
    private val sevenDayWind6: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph in ")
    private val sevenDayWind7: Pattern = Pattern.compile("around ([0-9]*) mph")
    private val sevenDayWind8: Pattern = Pattern.compile("Winds could gust as high as ([0-9]*) mph\\.")
    private val sevenDayWind9: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph.")
    private val sevenDayWinddir1: Pattern = Pattern.compile("\\. (\\w+\\s?\\w*) wind ")
    private val sevenDayWinddir2: Pattern = Pattern.compile("wind becoming (.*?) [0-9]")
    private val sevenDayWinddir3: Pattern = Pattern.compile("wind becoming (\\w+\\s?\\w*) around")
    private val sevenDayWinddir4: Pattern = Pattern.compile("Breezy, with a[n]? (.*?) wind")
    private val sevenDayWinddir5: Pattern = Pattern.compile("Windy, with a[n]? (.*?) wind")
    private val sevenDayWinddir6: Pattern = Pattern.compile("Blustery, with a[n]? (.*?) wind")
    private val sevenDayWinddir7: Pattern = Pattern.compile("Light (.*?) wind")

    private val ca7DayTemp1: Pattern = Pattern.compile("Temperature falling to (minus [0-9]{1,2}) this")
    private val ca7DayTemp2: Pattern = Pattern.compile("Low (minus [0-9]{1,2})\\.")
    private val ca7DayTemp3: Pattern = Pattern.compile("High (minus [0-9]{1,2})\\.")
    private val ca7DayTemp4: Pattern = Pattern.compile("Low plus ([0-9]{1,2})\\.")
    private val ca7DayTemp5: Pattern = Pattern.compile("High plus ([0-9]{1,2})\\.")
    private val ca7DayTemp6: Pattern = Pattern.compile("steady near (minus [0-9]{1,2})\\.")
    private val ca7DayTemp7: Pattern = Pattern.compile("steady near plus ([0-9]{1,2})\\.")
    private val ca7DayTemp8: Pattern = Pattern.compile("rising to (minus [0-9]{1,2}) ")
    private val ca7DayTemp9: Pattern = Pattern.compile("falling to (minus [0-9]{1,2}) ")
    private val ca7DayTemp10: Pattern = Pattern.compile("Low (minus [0-9]{1,2}) ")
    private val ca7DayTemp11: Pattern = Pattern.compile("Low (zero)\\.")
    private val ca7DayTemp12: Pattern = Pattern.compile("rising to ([0-9]{1,2}) ")
    private val ca7DayTemp13: Pattern = Pattern.compile("High ([0-9]{1,2})[\\. ]")
    private val ca7DayTemp14: Pattern = Pattern.compile("rising to plus ([0-9]{1,2}) ")
    private val ca7DayTemp15: Pattern = Pattern.compile("falling to plus ([0-9]{1,2}) ")
    private val ca7DayTemp16: Pattern = Pattern.compile("High (zero)\\.")
    private val ca7DayTemp17: Pattern = Pattern.compile("rising to (zero) by")
    private val ca7DayTemp18: Pattern = Pattern.compile("Low ([0-9]{1,2})\\.")
    private val ca7DayTemp19: Pattern = Pattern.compile("High ([0-9]{1,2}) with temperature")
    private val ca7DayTemp20: Pattern = Pattern.compile("Temperature falling to (zero) in")
    private val ca7DayTemp21: Pattern = Pattern.compile("steady near ([0-9]{1,2})\\.")
    private val ca7DayTemp22: Pattern = Pattern.compile("steady near (zero)\\.")
    private val ca7DayWinddir1: Pattern = Pattern.compile("Wind ([a-z]*?) [0-9]{2,3} ")
    private val ca7DayWinddir2: Pattern = Pattern.compile("Wind becoming ([a-z]*?) [0-9]{2,3} ")
    private val ca7DayWindspd1: Pattern = Pattern.compile("([0-9]{2,3}) to ([0-9]{2,3}) km/h")
    private val ca7DayWindspd2: Pattern = Pattern.compile("( [0-9]{2,3}) km/h")
    private val ca7DayWindspd3: Pattern = Pattern.compile("gusting to ([0-9]{2,3})")

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
        val wind = chunk.parseMultiple(sevenDayWind1, 2)
        // wind around 9 mph
        val wind2 = chunk.parse(sevenDayWind2)
        // 5 to 10 mph after
        val wind3 = chunk.parseMultiple(sevenDayWind4, 2)
        // around 5 mph after
        val wind4 = chunk.parse(sevenDayWind5)
        // 5 to 7 mph in
        val wind5 = chunk.parseMultiple(sevenDayWind6, 2)
        // around 6 mph.
        val wind7 = chunk.parse(sevenDayWind7)
        // with gusts as high as 21 mph
        var gust = chunk.parse(sevenDayWind3)
        // 5 to 7 mph.
        val wind9 = chunk.parseMultiple(sevenDayWind9, 2)
        // Winds could gusts as high as 21 mph.
        if (gust == "") {
            gust = chunk.parse(sevenDayWind8)
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

    fun setNwsIconSize() = (MyApplication.dm.widthPixels * (UIPreferences.nwsIconSize / 100.0f)).toInt()

    fun extractWindDirection(chunk: String): String {
        val windDir1 = chunk.parseLastMatch(sevenDayWinddir1)
        val windDir2 = chunk.parseLastMatch(sevenDayWinddir2)
        val windDir3 = chunk.parseLastMatch(sevenDayWinddir3)
        val windDir4 = chunk.parseLastMatch(sevenDayWinddir4)
        val windDir5 = chunk.parseLastMatch(sevenDayWinddir5)
        val windDir6 = chunk.parseLastMatch(sevenDayWinddir6)
        val windDir7 = chunk.parseLastMatch(sevenDayWinddir7)
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
            val ret = windDirectionMap[retStr.lowercase(Locale.US)]
            if (ret != null) {
                " $ret"
            } else {
                ""
            }
        }
    }

    fun extractTemperature(blob: String): String {
        val list = listOf(
                nws7DayTemp1,
                nws7DayTemp2,
                nws7DayTemp3,
                nws7DayTemp4,
                nws7DayTemp5,
                nws7DayTemp6,
                nws7DayTemp7,
                nws7DayTemp8,
                nws7DayTemp9,
                nws7DayTemp10,
                nws7DayTemp11
        )
        list.forEach {
            val temp = blob.parse(it)
            if (temp != "") {
                return if (UIPreferences.unitsF) {
                    temp
                } else {
                    UtilityMath.fahrenheitToCelsius(To.double(temp))
                }
            }
        }
        return ""
    }

    fun extractCanadaTemperature(blob: String): String {
        var temp = blob.parse(ca7DayTemp1)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp2)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp3)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp4)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp5)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp6)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp7)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp8)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp9)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp10)
        if (temp != "") return temp.replace("minus ", "-")
        temp = blob.parse(ca7DayTemp11)
        if (temp != "") return "0"
        temp = blob.parse(ca7DayTemp12)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp13)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp14)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp15)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp16)
        if (temp != "") return "0"
        temp = blob.parse(ca7DayTemp17)
        if (temp != "") return "0"
        temp = blob.parse(ca7DayTemp18)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp19)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp20)
        if (temp != "") return "0"
        temp = blob.parse(ca7DayTemp21)
        if (temp != "") return temp
        temp = blob.parse(ca7DayTemp22)
        if (temp != "") return "0"
        return temp
    }

    fun extractCanadaWindDirection(chunk: String): String {
        var windDirection = chunk.parse(ca7DayWinddir1)
        if (windDirection == "") {
            windDirection = chunk.parse(ca7DayWinddir2)
        }
        if (windDirection != "") {
            windDirection = " " + (windDirectionMap[windDirection] ?: "")
        }
        return windDirection
    }

    fun extractCanadaWindSpeed(forecast: String): String {
        val windSpeedRange = forecast.parseMultiple(ca7DayWindspd1, 2)
        val windSpeed = forecast.parse(ca7DayWindspd2)
        var gust = ""
        if (forecast.contains("gusting")) {
            gust = " G " + forecast.parse(ca7DayWindspd3)
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

    fun handleIconTap(stringName: String, wxglRender: WXGLRender?, activityReference: Context, fnRefresh: () -> Unit, fnResetRadarView: () -> Unit, fnGetRadars: () -> Unit) {
        when {
            stringName.contains("Edit Location..") -> ObjectIntent.showLocationEdit(activityReference, arrayOf(Location.currentLocationStr, ""))
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
                val radarSite = stringName.split(":")[0]
                val state = Utility.getRadarSiteName(radarSite).split(",")[0]
                ObjectIntent.showRadar(activityReference, arrayOf(radarSite, state, wxglRender!!.product, ""))
            }
        }
    }
}
