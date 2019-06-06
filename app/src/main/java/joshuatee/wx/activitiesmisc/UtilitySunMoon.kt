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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.MyApplication
import joshuatee.wx.settings.Location
import joshuatee.wx.util.*
import joshuatee.wx.Extensions.*

object UtilitySunMoon {

    /*
    http://api.usno.navy.mil/rstt/oneday?date=3/10/2016&coords=41.89N,82.48E&tz=5
    http://aa.usno.navy.mil/data/docs/api.php#rstt
    http://aa.usno.navy.mil/data/docs/RS_OneDay.php
    http://api.usno.navy.mil/rstt/oneday?date=today&coords=42.26,-83.73&tz=-5*/

    fun getExtendedData(currentLoc: Int): String {
        val tzOffset: String
        var x = ""
        var y = ""
        if (Location.isUS(currentLoc)) {
            x = Location.x
            y = Location.y
        } else {
            val tmpX = MyApplication.colon.split(Location.x)
            val tmpY = MyApplication.colon.split(Location.y)
            if (tmpX.size > 2 && tmpY.size > 1) {
                x = tmpX[2]
                y = tmpY[1]
            }
        }
        val timeZone = UtilityTime.getDateAsString("Z")
        tzOffset = timeZone.substring(0, 3) + "." + timeZone.substring(3, 5)
        val url = "${MyApplication.sunMoonDataUrl}/rstt/oneday?date=today&coords=$x,$y&tz=$tzOffset"
        return url.getHtmlUnsafe()
    }

    fun parseData(contentF: String): List<String> {
        var content = contentF
        val sunDataChunk = content.parse("sundata\":\\[(.*?)\\]")
        val moonDataChunk = content.parse("moondata.:\\[(.*?)\\]")
        val moonPhaseChunk = content.parse("closestphase.:\\{(.*?)\\}")
        val moonFracillum = content.parse("fracillum\":\"(.*?)%")
        val moonCurrentPhase = content.parse("curphase\":\"(.*?)\"")
        val sunTwilight = sunDataChunk.parse(" \\{\"phen\":\"BC\", \"time\":\"(.*?)\"\\}")
        val sunRise = sunDataChunk.parse(" \\{\"phen\":\"R\", \"time\":\"(.*?)\"\\}")
        val sunUpperTransit = sunDataChunk.parse(" \\{\"phen\":\"U\", \"time\":\"(.*?)\"\\}")
        val sunSet = sunDataChunk.parse(" \\{\"phen\":\"S\", \"time\":\"(.*?)\"\\}")
        val sunEndTwilight = sunDataChunk.parse(" \\{\"phen\":\"EC\", \"time\":\"(.*?)\"\\}")
        val moonRise = moonDataChunk.parse(" \\{\"phen\":\"R\", \"time\":\"(.*?)\"\\}")
        val moonUpperTransit = moonDataChunk.parse(" \\{\"phen\":\"U\", \"time\":\"(.*?)\"\\}")
        val moonSet = moonDataChunk.parse(" \\{\"phen\":\"S\", \"time\":\"(.*?)\"\\}")
        val header = "Sun/Moon Data"
        content = sunTwilight + " Sun Twilight" + MyApplication.newline + sunRise + " Sunrise" +
                MyApplication.newline + sunUpperTransit + " Sun Upper Transit" +
                MyApplication.newline + sunSet + " Sunset" + MyApplication.newline +
                sunEndTwilight + " Sun Twilight End" + MyApplication.newline +
                MyApplication.newline + moonRise + " Moonrise" + MyApplication.newline +
                moonUpperTransit + " Moon Upper Transit" + MyApplication.newline + moonSet +
                " Moonset" + MyApplication.newline + MyApplication.newline +
                moonPhaseChunk.replace("\"time\"", "").replace("\"date\"", "").replace(
                    "\"",
                    ""
                ).replace(":", " ").replace(",", "") + MyApplication.newline + moonFracillum +
                "% Moon fracillum" + MyApplication.newline + moonCurrentPhase +
                " is the current phase" + MyApplication.newline
        return listOf(header, content)
    }

    fun getFullDates(): String {
        val url = "${MyApplication.sunMoonDataUrl}/moon/phase?date=" + UtilityTime.month().toString() + "/" + UtilityTime.day().toString() + "/" + UtilityTime.year().toString() + "&nump=99"
        val text = url.getHtmlUnsafe()
        var fullText = ""
        val phases = text.parseColumn("\"phase\":\"(.*?)\"")
        val dates = text.parseColumn("\"date\":\"(.*?)\"")
        val times = text.parseColumn("\"time\":\"(.*?)\"")
        phases.forEachIndexed { index, _ ->
            fullText += if (phases[index].contains("Full Moon")) {
                dates[index] + " " + times[index] + " " + phases[index] + "  <-----" + MyApplication.newline
            } else {
                dates[index] + " " + times[index] + " " + phases[index] + MyApplication.newline
            }
        }
        return fullText
    }
}
