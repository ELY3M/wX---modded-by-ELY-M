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

package joshuatee.wx.canada

import java.util.Locale
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility

internal object UtilityCanadaHourly {

    fun getString(locNumInt: Int): String {
        val htmlUrl = GlobalVariables.canadaEcSitePrefix + "/forecast/hourly/" + (Location.getX(locNumInt).split(":"))[1].lowercase(Locale.US) + "-" + (Location.getY(locNumInt).split(":"))[0] + "_metric_e.html"
        val html = htmlUrl.getHtml()
        val header = "Time    Temp  Summary                  Precip   Wind"
        return header + parse(html)
    }

    fun getUrl(locNumInt: Int) =
            GlobalVariables.canadaEcSitePrefix + "/forecast/hourly/" + (Location.getX(locNumInt).split(":"))[1].lowercase(
                    Locale.US) + "-" + (Location.getY(locNumInt).split(":"))[0] + "_metric_e.html"

    private fun parse(htmlFullPage: String): String {
        val html = htmlFullPage.parse("<tbody>(.*?)</tbody>")
        val times = html.parseColumn("<td headers=.header1. class=.text-center.>([0-9]{2}:[0-9]{2})</td>")
        val temperatures = html.parseColumn("<td headers=.header2. class=.text-center.>(.*?)</td>")
        val currentConditions = html.parseColumn("</span><div class=.media-body.><p>(.*?)</p></div>")
        val precipChances = html.parseColumn("<td headers=.header4. class=.text-center.>(.*?)</td>")
        val winds = html.parseColumn("<abbr title=(.*?.>.*?<.abbr>..[0-9]{2})<br>").toMutableList()
        //let feelsLikeTemps = html.parseColumn("<td headers=.header7. class=.text-center.>(.*?)</td>")
        winds.indices.forEach {
            val cleanString = removeSpecialCharsFromString(winds[it])
            winds[it] = cleanString.parse(">(.*?)<") + " " + cleanString.parse(".*?([0-9]{1,3})")
        }
        var string = ""
        val space = "   "
        times.indices.forEach {
            string += GlobalVariables.newline + times[it] + space +
                    Utility.safeGet(temperatures, it).padEnd(3, ' ')  + space +
                    Utility.safeGet(currentConditions, it).padEnd(22, ' ') + space +
                    Utility.safeGet(precipChances, it).padEnd(6, ' ')  + space +
                    Utility.safeGet(winds, it)
        }
        return string
    }

    private fun removeSpecialCharsFromString(text: String) = text.filter { it.isLetterOrDigit() || it.isWhitespace() || it == '>' || it == '<' }
}
