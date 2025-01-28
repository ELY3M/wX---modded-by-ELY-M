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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.Route
import joshuatee.wx.parse
import joshuatee.wx.parseLastMatch
import joshuatee.wx.parseMultiple
import joshuatee.wx.radar.NexradRender
import joshuatee.wx.settings.Location
import java.util.Locale
import java.util.regex.Pattern

object UtilityLocationFragment {

    private val nws7DayTemp1: Pattern = Pattern.compile("with a low around (-?[0-9]{1,3})\\.")
    private val nws7DayTemp2: Pattern = Pattern.compile("with a high near (-?[0-9]{1,3})\\.")
    private val nws7DayTemp3: Pattern =
        Pattern.compile("teady temperature around (-?[0-9]{1,3})\\.")
    private val nws7DayTemp4: Pattern = Pattern.compile("Low around (-?[0-9]{1,3})\\.")
    private val nws7DayTemp5: Pattern = Pattern.compile("High near (-?[0-9]{1,3})\\.")
    private val nws7DayTemp6: Pattern =
        Pattern.compile("emperature falling to around (-?[0-9]{1,3}) ")
    private val nws7DayTemp7: Pattern =
        Pattern.compile("emperature rising to around (-?[0-9]{1,3}) ")
    private val nws7DayTemp8: Pattern =
        Pattern.compile("emperature falling to near (-?[0-9]{1,3}) ")
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
    private val sevenDayWind8: Pattern =
        Pattern.compile("Winds could gust as high as ([0-9]*) mph\\.")
    private val sevenDayWind9: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph.")
    private val sevenDayWinddir0: Pattern = Pattern.compile("Light (.*?) wind increasing")
    private val sevenDayWinddir1: Pattern = Pattern.compile("\\. (\\w+\\s?\\w*) wind ")
    private val sevenDayWinddir2: Pattern = Pattern.compile("wind becoming (.*?) [0-9]")
    private val sevenDayWinddir3: Pattern = Pattern.compile("wind becoming (\\w+\\s?\\w*) around")
    private val sevenDayWinddir4: Pattern = Pattern.compile("Breezy, with an? (.*?) wind")
    private val sevenDayWinddir5: Pattern = Pattern.compile("Windy, with an? (.*?) wind")
    private val sevenDayWinddir6: Pattern = Pattern.compile("Blustery, with an? (.*?) wind")
    private val sevenDayWinddir7: Pattern = Pattern.compile("Light (.*?) wind")

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

    fun setNwsIconSize(): Int =
        (MyApplication.dm.widthPixels * (UIPreferences.nwsIconSize / 100.0f)).toInt()

    fun extractWindDirection(chunk: String): String {
        val windDir0 = chunk.parseLastMatch(sevenDayWinddir0)
        val windDir1 = chunk.parseLastMatch(sevenDayWinddir1)
        val windDir2 = chunk.parseLastMatch(sevenDayWinddir2)
        val windDir3 = chunk.parseLastMatch(sevenDayWinddir3)
        val windDir4 = chunk.parseLastMatch(sevenDayWinddir4)
        val windDir5 = chunk.parseLastMatch(sevenDayWinddir5)
        val windDir6 = chunk.parseLastMatch(sevenDayWinddir6)
        val windDir7 = chunk.parseLastMatch(sevenDayWinddir7)
        var retStr = ""
        when {
            windDir0 != "" -> retStr = windDir0
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

    private val tempList = listOf(
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

    fun extractTemperature(blob: String): String {
        tempList.forEach {
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

    fun handleIconTap(
        s: String,
        wxglRender: NexradRender?,
        activityReference: Context,
        fnRefresh: () -> Unit,
        fnResetRadarView: () -> Unit,
        fnGetRadars: () -> Unit
    ) {
        when {
            s.contains("Edit Location..") -> Route.locationEdit(
                activityReference,
                Location.currentLocationStr
            )

            s.contains("Force Data Refresh") -> fnRefresh()
            s.contains("Radar type: Reflectivity") -> {
                wxglRender?.state?.product = "N0Q"
                fnGetRadars()
            }

            s.contains("Radar type: Velocity") -> {
                wxglRender?.state?.product = "N0U"
                fnGetRadars()
            }

            s.contains("Reset zoom and center") -> fnResetRadarView()
            else -> {
                val radarSite = s.split(":")[0]
                Route.radar(
                    activityReference,
                    arrayOf(radarSite, "STATE NOT USED", wxglRender!!.state.product, "")
                )
            }
        }
    }
}
