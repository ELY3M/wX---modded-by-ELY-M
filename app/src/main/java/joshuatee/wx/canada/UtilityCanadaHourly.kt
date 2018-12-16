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

package joshuatee.wx.canada

import java.util.Locale

import joshuatee.wx.MyApplication

import joshuatee.wx.Extensions.*
import joshuatee.wx.settings.Location

internal object UtilityCanadaHourly {

    fun getString(locNumInt: Int): String {
        val htmlUrl = "http://weather.gc.ca/forecast/hourly/" + MyApplication.colon.split(
            Location.getX(locNumInt)
        )[1].toLowerCase(Locale.US) + "-" + MyApplication.colon.split(Location.getY(locNumInt))[0] + "_metric_e.html"
        val html = htmlUrl.getHtml()
        val header = "Time   Temp   Summary   PrecipChance   Wind   Humindex"
        return header + parse(html)
    }

    fun getUrl(locNumInt: Int) =
        "http://weather.gc.ca/forecast/hourly/" + MyApplication.colon.split(Location.getX(locNumInt))[1].toLowerCase(
            Locale.US
        ) + "-" + MyApplication.colon.split(Location.getY(locNumInt))[0] + "_metric_e.html"

    private fun parse(htmlF: String): String {
        val sb = StringBuilder(500)
        val html = htmlF.parse("<tbody>(.*?)</tbody>")
        val timeAl =
            html.parseColumn("<tr>.*?<td.*?>(.*?)</td>.*?<td.*?>.*?</td>.*?<div class=\"media.body\">.*?<p>.*?</p>.*?</div>.*?<td.*?>.*?</td>.*?<abbr title=\".*?\">.*?</abbr>.*?<br />.*?<td.*?>.*?</td>.*?</tr>")
        val tempAl =
            html.parseColumn("<tr>.*?<td.*?>.*?</td>.*?<td.*?>(.*?)</td>.*?<div class=\"media.body\">.*?<p>.*?</p>.*?</div>.*?<td.*?>.*?</td>.*?<abbr title=\".*?\">.*?</abbr>.*?<br />.*?<td.*?>.*?</td>.*?</tr>")
        val currCondAl =
            html.parseColumn("<tr>.*?<td.*?>.*?</td>.*?<td.*?>.*?</td>.*?<div class=\"media.body\">.*?<p>(.*?)</p>.*?</div>.*?<td.*?>.*?</td>.*?<abbr title=\".*?\">.*?</abbr>.*?<br />.*?<td.*?>.*?</td>.*?</tr>")
        val popsAl =
            html.parseColumn("<tr>.*?<td.*?>.*?</td>.*?<td.*?>.*?</td>.*?<div class=\"media.body\">.*?<p>.*?</p>.*?</div>.*?<td.*?>(.*?)</td>.*?<abbr title=\".*?\">.*?</abbr>.*?<br />.*?<td.*?>.*?</td>.*?</tr>")
        val windDirAl =
            html.parseColumn("<tr>.*?<td.*?>.*?</td>.*?<td.*?>.*?</td>.*?<div class=\"media.body\">.*?<p>.*?</p>.*?</div>.*?<td.*?>.*?</td>.*?<abbr title=\".*?\">(.*?)</abbr>.*?<br />.*?<td.*?>.*?</td>.*?</tr>")
        val windSpeedAl =
            html.parseColumn("<tr>.*?<td.*?>.*?</td>.*?<td.*?>.*?</td>.*?<div class=\"media.body\">.*?<p>.*?</p>.*?</div>.*?<td.*?>.*?</td>.*?<abbr title=\".*?\">.*?</abbr>(.*?)<br />.*?<td.*?>.*?</td>.*?</tr>")
        val humindexAl =
            html.parseColumn("<tr>.*?<td.*?>.*?</td>.*?<td.*?>.*?</td>.*?<div class=\"media.body\">.*?<p>.*?</p>.*?</div>.*?<td.*?>.*?</td>.*?<abbr title=\".*?\">.*?</abbr>.*?<br />.*?<td.*?>(.*?)</td>.*?</tr>")
        val space = "   "
        var humindex: String
        timeAl.indices.forEach {
            humindex = humindexAl[it].replace("<abbr.*?>".toRegex(), "")
            humindex = humindex.replace("</abbr>", "")
            sb.append(MyApplication.newline)
            sb.append(timeAl[it])
            sb.append(space)
            sb.append(tempAl[it])
            sb.append(space)
            sb.append(currCondAl[it])
            sb.append(space)
            sb.append(popsAl[it])
            sb.append(space)
            sb.append(windDirAl[it])
            sb.append(windSpeedAl[it])
            sb.append(space)
            sb.append(humindex)
        }
        return sb.toString()
    }
}

