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

package joshuatee.wx.canada

import java.util.Locale

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.LatLonStr
import joshuatee.wx.util.Utility

import joshuatee.wx.Extensions.*
import joshuatee.wx.radar.LatLon

object UtilityCanada {

    private val providenceToSector = mapOf(
        "AB" to "PAC",
        "BC" to "PAC",
        "MB" to "PAC",
        "NB" to "ERN",
        "NL" to "ERN",
        "NS" to "ERN",
        "NT" to "CAN",
        "NU" to "CAN",
        "ON" to "ONT",
        "PE" to "ERN",
        "QC" to "ERN",
        "SK" to "PAC",
        "YT" to "CAN"
    )

    fun getIcons7Day(html: String): String {
        var iconList = ""
        val dayAr = html.split((MyApplication.newline + MyApplication.newline))
            .dropLastWhile { it.isEmpty() }
        dayAr.forEach { iconList += translateIconName(it) + "!" }
        return iconList
    }

    fun getIcons7DayAsList(html: String): List<String> {
        val dayAr = html.split((MyApplication.newline + MyApplication.newline))
            .dropLastWhile { it.isEmpty() }
        return dayAr.mapTo(mutableListOf()) { translateIconName(it) }
    }

    private fun translateIconName(s: String): String {
        var newName = ""
        if (s.contains("reezing rain or snow")
            || s.contains("chance of flurries and risk of freezing drizzle")
            || s.contains("chance of flurries before morning with risk of freezing drizzle")
            || s.contains("Periods of freezing drizzle or flurries")
            || s.contains("Flurries or freezing drizzle")
        )
            newName = "fzra_sn"
        else if (s.contains("eriods of freezing drizzle")
            || s.contains("reezing Drizzle")
            || s.contains("reezing drizzle")
            || s.contains("reezing rain")
            || s.contains("isk of freezing drizzle")
        )
            newName = "fzra"
        else if (s.contains("Ice Crystals") || s.contains("ice pellets"))
            newName = "ip"
        else if (s.contains("hundershowers") || s.contains("hunderstorm"))
            newName = "scttsra"
        else if (s.contains("hance of rain showers or flurries")
            || s.contains("lurries or rain showers")
            || s.contains(" few rain showers or flurries")
            || s.contains("ain or snow")
            || s.contains("eriods of rain or snow")
            || s.contains("now or rain")
            || s.contains("ain changing to snow")
            || s.contains("ain showers or flurries")
            || s.contains("ain changing to periods of snow")
            || s.contains("now changing to periods of rain")
            || s.contains("Rain at times heavy changing to snow")
            || s.contains("Snow changing to periods of drizzle")
            || s.contains("Rain at times mixed with wet snow")
        )
            newName = "ra_sn"
        else if (s.contains("hance of showers") || s.contains("howers"))
            newName = "shra"
        else if (s.contains("hance of rain")
            || s.contains("Rain beginning")
            || s.contains("eriods of drizzle changing to rain at times")
            || s.contains("Rain at times heavy")
        )
            newName = "ra"
        else if (s.contains("hance of flurries")
            || s.contains("hance of snow")
            || s.contains("eriods of snow")
            || s.contains(" few flurries")
            || s.contains("eriods of light snow")
            || s.contains("Flurries")
            || s.contains("Snow")
            || s.contains("then snow")
            || s.contains("ight snow")
        )
            newName = "sn"
        else if (s.contains("A few showers")
            || s.contains("eriods of rain")
            || s.contains("drizzle")
        )
            newName = "hi_shwrs"
        else if (s.contains("Increasing cloudiness") || s.contains("Cloudy periods"))
            newName = "bkn"
        else if (s.contains("Mainly sunny") || s.contains("A few clouds"))
            newName = "few"
        else if (s.contains("loudy")
            || s.contains("Mainly cloudy")
            || s.contains("Overcast")
        )
            newName = "ovc"
        else if (s.contains("A mix of sun and cloud")
            || s.contains("Partly cloudy")
            || s.contains("Clearing")
        )
            newName = "sct"
        else if (s.contains("Clear") || s.contains("Sunny"))
            newName = "skc"
        else if (s.contains("Blizzard") || s.contains("Local blowing snow"))
            newName = "blizzard"
        else if (s.contains("Rain"))
            newName = "ra"
        else if (s.contains("Mist")
            || s.contains("Fog")
            || s.contains("Light Drizzle")
        )
            newName = "fg"

        if (s.contains("night")) {
            newName = if (newName.contains("hi_"))
                newName.replace("hi_", "hi_n")
            else
                "n$newName"
        }
        if (s.contains("percent")) {
            val pop = s.parse("([0-9]{2}) percent")
            newName += ",$pop"
        }
        return newName
    }

    fun translateIconNameCurrentConditions(s: String, day1: String): String {
        var newName = ""
        if (s.contains("eriods of freezing drizzle")
            || s.contains("Freezing Drizzle")
            || s.contains("Freezing Rain")
        )
            newName = "fzra"
        else if (s.contains("hundershowers") || s.contains("hunderstorm"))
            newName = "scttsra"
        else if (s.contains("Haze"))
            newName = "fg"
        else if (s.contains("hance of rain showers or flurries")
            || s.contains("lurries or rain showers")
            || s.contains(" few rain showers or flurries")
            || s.contains("ain or snow")
            || s.contains("ain and Snow")
            || s.contains("eriods of rain or snow")
            || s.contains("now or rain")
        )
            newName = "ra_sn"
        else if (s.contains("hance of showers") || s.contains("Showers"))
            newName = "shra"
        else if (s.contains("hance of rain"))
            newName = "ra"
        else if (s.contains("A few showers") || s.contains("eriods of rain")
            || s.contains("drizzle")
        )
            newName = "hi_shwrs"
        else if (s.contains("Blizzard")
            || s.contains("Blowing Snow")
            || s.contains("Drifting Snow")
        )
            newName = "blizzard"
        else if (s.contains("hance of flurries")
            || s.contains("hance of snow")
            || s.contains("eriods of snow")
            || s.contains(" few flurries")
            || s.contains("eriods of light snow")
            || s.contains("Flurries")
            || s.contains("Snow")
            || s.contains("ight snow")
        )
            newName = "sn"
        else if (s.contains("Increasing cloudiness") || s.contains("Cloudy periods"))
            newName = "bkn"
        else if (s.contains("Mainly sunny")
            || s.contains("A few clouds")
            || s.contains("Mainly Sunny")
            || s.contains("Mainly Clear")
        )
            newName = "few"
        else if (s.contains("Cloudy")
            || s.contains("Mainly cloudy")
            || s.contains("Overcast")
        )
            newName = "ovc"
        else if (s.contains("A mix of sun and cloud")
            || s.contains("Partly cloudy")
            || s.contains("Clearing")
        )
            newName = "sct"
        else if (s.contains("Clear") || s.contains("Sunny"))
            newName = "skc"
        else if (s.contains("Rain"))
            newName = "ra"
        else if (s.contains("Mist")
            || s.contains("Fog")
            || s.contains("Light Drizzle")
        )
            newName = "fg"
        else if (s.contains("Ice Crystals") || s.contains("Ice Pellets"))
            newName = "ip"
        val time = day1.parse(" ([0-9]{1,2}:[0-9]{2} [AP]M) ")
        val timeArr = MyApplication.colon.split(time)
        var hour: Int
        var daytime = true
        if (timeArr.isNotEmpty()) {
            hour = timeArr[0].toIntOrNull() ?: 0
            if (time.contains("AM"))
                if (hour < 8) daytime = false
            if (time.contains("PM")) {
                if (hour == 12) hour = 0
                if (hour > 6) daytime = false
            }
        }
        if (!daytime) {
            newName = if (newName.contains("hi_"))
                newName.replace("hi_", "hi_n")
            else
                "n$newName"
        }
        if (s.contains("percent")) {
            val pop = s.parse("([0-9]{2}) percent")
            newName += ",$pop"
        }
        return newName
    }

    fun getProvidenceHtml(prov: String): String =
        ("http://weather.gc.ca/forecast/canada/index_e.html?id=$prov").getHtmlSep()

    fun getLocationHtml(location: LatLon): String {
        val prov = location.latString.split(":").dropLastWhile { it.isEmpty() }
        val id = location.lonString.split(":").dropLastWhile { it.isEmpty() }
        return ("http://weather.gc.ca/rss/city/" + prov[1].toLowerCase(Locale.US) + "-" + id[0] + "_e.xml").getHtmlSep()
    }

    fun getLocationUrl(x: String, y: String): String {
        val prov = x.split(":").dropLastWhile { it.isEmpty() }
        val id = y.split(":").dropLastWhile { it.isEmpty() }
        if (prov.count() < 2 || id.count() < 1)
            return ""
        return "http://weather.gc.ca/city/pages/" + prov[1].toLowerCase(Locale.US) + "-" + id[0] + "_metric_e.html"
    }

    fun getStatus(html: String): String = html.parse("<b>Observed at:</b>(.*?)<br/>")

    fun getRid(x: String, y: String): String {
        val url = ("http://weather.gc.ca/city/pages/"
                + x.split(":").dropLastWhile { it.isEmpty() }[1].toLowerCase(Locale.US) + "-"
                + y.split(":").dropLastWhile { it.isEmpty() }[0] + "_metric_e.html")
        val html = url.getHtmlSep()
        return html.parse("<a href=./radar/index_e.html.id=([a-z]{3})..*?>Weather Radar</a>")
            .toUpperCase(Locale.US)
    }

    fun getConditions(html: String): String {
        val sum = html.parse("<b>Condition:</b> (.*?) <br/>.*?<b>Pressure.*?:</b> .*? kPa.*?<br/>")
        val pressure =
            html.parse("<b>Condition:</b> .*? <br/>.*?<b>Pressure.*?:</b> (.*?) kPa.*?<br/>")
        var vis = html.parse("<b>Visibility:</b> (.*?)<br/>")
        vis = vis.replace("<.*?>".toRegex(), "")
        vis = vis.replace("\\s+".toRegex(), "")
        val temp =
            html.parse("<b>Temperature:</b> (.*?)&deg;C <br/>.*?<b>Humidity:</b> .*? %<br/>.*?<b>Dewpoint:</b> .*?&deg;C <br/>")
        val rh =
            html.parse("<b>Temperature:</b> .*?&deg;C <br/>.*?<b>Humidity:</b> (.*?) %<br/>.*?<b>Dewpoint:</b> .*?&deg;C <br/>")
        val dew =
            html.parse("<b>Temperature:</b> .*?&deg;C <br/>.*?<b>Humidity:</b> .*? %<br/>.*?<b>Dewpoint:</b> (.*?)&deg;C <br/>")
        var wind = Utility.fromHtml(html.parse("<b>Wind:</b> (.*?)<br/>"))
        wind = wind.replace(MyApplication.newline, "")
        vis = vis.replace(" miles", "mi")
        return temp + MyApplication.DEGREE_SYMBOL + " / " + dew + MyApplication.DEGREE_SYMBOL + " (" + rh + "%) - " + pressure + "kPa - " + wind + " - " + vis + " - " + sum + " "
    }

    fun get7Day(html: String): String {
        val stringList =
            html.parseColumn("<category term=\"Weather Forecasts\"/><br> <summary type=\"html\">(.*?\\.) Forecast.*?</summary>")
        var sevenDayForecast = ""
        val resultListDay = html.parseColumn("<title>(.*?)</title>")
        var j = 0
        for (i in 2 until resultListDay.size) {
            if (resultListDay[i].contains("Current Conditions")) {
                continue
            }
            if (!resultListDay[i].contains(":")) {
                continue
            }
            val tmpStr = resultListDay[i].split(":")[0] + ": " + stringList[j]
            sevenDayForecast += tmpStr + MyApplication.newline + MyApplication.newline
            j += 1
        }
        return sevenDayForecast
    }

    fun getHazards(html: String): List<String> {
        val warning: String
        val statement: String
        val watch: String
        val warningUrl: String
        val statementUrl: String
        val watchUrl: String
        val baseUrl = "http://weather.gc.ca"
        val result = mutableListOf("", "")
        var urls =
            html.parseColumn("<div id=\"statement\" class=\"floatLeft\">.*?<a href=\"(.*?)\">.*?</a>.*?</div>")
        var titles =
            html.parseColumn("<div id=\"statement\" class=\"floatLeft\">.*?<a href=\".*?\">(.*?)</a>.*?</div>")
        statementUrl = urls.joinToString("")
        statement = titles.joinToString("<BR>")
        var chunk = html.parse("<entry>(.*?)<category term=\"Warnings and Watches\"/>")
        urls = chunk.parseColumn("<title>.*?</title>.*?<link type=\"text/html\" href=\"(.*?)\"/>")
        titles =
            chunk.parseColumn("<title>(.*?)</title>.*?<link type=\"text/html\" href=\".*?\"/>")
        warningUrl = urls.joinToString(",")
        warning = titles.joinToString("<BR>")
        chunk = html.parse("<div id=\"watch\" class=\"floatLeft\">(.*?)</div>")
        urls = chunk.parseColumn("<a href=\"(.*?)\">.*?</a>")
        titles = chunk.parseColumn("<a href=\".*?\">(.*?)</a>")
        watchUrl = urls.joinToString(",$baseUrl")
        watch = titles.joinToString("<BR>")
        result[0] = warning + statement + watch
        result[1] = "$warningUrl,$statementUrl,$watchUrl"
        if (!result[0].contains("No watches or warnings in effect")) {
            result[1] = getHazardsFromUrl(warningUrl)
        } else {
            result[1] = result[0]
        }
        return result
    }

    fun getHazardsFromUrl(url: String): String {
        var warningData = ""
        val urlArr = url.split(",").dropLastWhile { it.isEmpty() }
        var notFound = true
        urlArr.forEach {
            if (it != "" && notFound) {
                warningData += it.getHtml().parse("<main.*?container.>(.*?)</div>")
                notFound = false
            }
        }
        warningData = warningData.replace(
            "<li><img src=./cacheable/images/img/feed-icon-14x14.png. alt=.ATOM feed.> <a href=./rss/battleboard/.*?.>ATOM</a></li>".toRegex(),
            ""
        )
        warningData = warningData.replace(" <div class=\"col-xs-12\">", "")
            .replace("<section class=\"followus hidden-print\"><h2>Follow:</h2>", "")
        warningData = warningData.replace(
            "<a href=\"/rss/battleboard/.*?.xml\"><img src=\"/cacheable/images/img/feed-icon-14x14.png\" alt=\"ATOM feed\" class=\"mrgn-rght-sm\">ATOM</a>",
            ""
        )
        warningData = warningData.replace("<div class=\"row\">", "")
        return warningData
    }

    fun getECSectorFromProv(prov: String): String = providenceToSector[prov] ?: ""

    fun isLabelPresent(label: String): Boolean {
        if (!UtilityCitiesCanada.cityInit) {
            UtilityCitiesCanada.loadCitiesArray()
        }
        return UtilityCitiesCanada.CITIES_CA.any { it.contains(label) }
    }

    fun getLatLonFromLabel(label: String): LatLonStr {
        val latLon = DoubleArray(2)
        var i = 0
        if (!UtilityCitiesCanada.cityInit) {
            UtilityCitiesCanada.loadCitiesArray()
        }
        for (l in UtilityCitiesCanada.CITIES_CA) {
            if (l == label) {
                latLon[0] = UtilityCitiesCanada.LAT_CA[i]
                latLon[1] = UtilityCitiesCanada.LON_CA[i]
                break
            }
            i += 1
        }
        return LatLonStr(latLon[0].toString(), latLon[1].toString())
    }
}


