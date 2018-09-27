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

    fun getExtendedSunMoonData(currentLoc: Int): String {
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
        val url = "http://api.usno.navy.mil/rstt/oneday?date=today&coords=$x,$y&tz=$tzOffset"
        return url.getHtml()
    }

    fun parseData(contentF: String): Pair<String, String> {
        var content = contentF
        val sundataChunk = content.parse("sundata\":\\[(.*?)\\]")
        val moondataChunk = content.parse("moondata.:\\[(.*?)\\]")
        val moonphaseChunk = content.parse("closestphase.:\\{(.*?)\\}")
        val moonFracillum = content.parse("fracillum\":\"(.*?)%")
        val moonCurrentphase = content.parse("curphase\":\"(.*?)\"")
        val sunTwilight = sundataChunk.parse(" \\{\"phen\":\"BC\", \"time\":\"(.*?)\"\\}")
        val sunRise = sundataChunk.parse(" \\{\"phen\":\"R\", \"time\":\"(.*?)\"\\}")
        val sunUppertransit = sundataChunk.parse(" \\{\"phen\":\"U\", \"time\":\"(.*?)\"\\}")
        val sunSet = sundataChunk.parse(" \\{\"phen\":\"S\", \"time\":\"(.*?)\"\\}")
        val sunEndTwilight = sundataChunk.parse(" \\{\"phen\":\"EC\", \"time\":\"(.*?)\"\\}")
        val moonRise = moondataChunk.parse(" \\{\"phen\":\"R\", \"time\":\"(.*?)\"\\}")
        val moonUppertransit = moondataChunk.parse(" \\{\"phen\":\"U\", \"time\":\"(.*?)\"\\}")
        val moonSet = moondataChunk.parse(" \\{\"phen\":\"S\", \"time\":\"(.*?)\"\\}")
        val header = "Sun/Moon Data"
        content = sunTwilight + " Sun Twilight" + MyApplication.newline + sunRise + " Sunrise" + MyApplication.newline + sunUppertransit + " Sun Upper Transit" + MyApplication.newline + sunSet + " Sunset" + MyApplication.newline + sunEndTwilight + " Sun Twilight End" + MyApplication.newline + MyApplication.newline + moonRise + " Moonrise" + MyApplication.newline + moonUppertransit + " Moon Upper Transit" + MyApplication.newline + moonSet + " Moonset" + MyApplication.newline + MyApplication.newline + moonphaseChunk.replace("\"time\"", "").replace("\"date\"", "").replace("\"", "").replace(":", " ").replace(",", "") + MyApplication.newline + moonFracillum + "% Moon fracillum" + MyApplication.newline + moonCurrentphase + " is the current phase" + MyApplication.newline
        return Pair(header, content)
    }

    fun getFullMoonDates(): String {
        val url = "http://api.usno.navy.mil/moon/phase?date=" + UtilityTime.month().toString() + "/" + UtilityTime.day().toString() + "/" + UtilityTime.year().toString() + "&nump=99"
        val text = url.getHtml()
        var fullText = ""
        val phaseArr = text.parseColumn("\"phase\":\"(.*?)\"")
        val dateArr = text.parseColumn("\"date\":\"(.*?)\"")
        val timeArr = text.parseColumn("\"time\":\"(.*?)\"")
        var idx = 0
        phaseArr.forEach {
            fullText += if (phaseArr[idx].contains("Full Moon")) {
                dateArr[idx] + " " + timeArr[idx] + " " + phaseArr[idx] + "  <-----" + MyApplication.newline
            } else {
                dateArr[idx] + " " + timeArr[idx] + " " + phaseArr[idx] + MyApplication.newline
            }
            idx += 1
        }
        return fullText
    }
}
