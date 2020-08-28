/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import joshuatee.wx.util.Utility

import joshuatee.wx.Extensions.*
import joshuatee.wx.GlobalArrays
import joshuatee.wx.radar.LatLon

object UtilityCanada {

    private val provinceToSector = mapOf(
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

    fun getRadarLabel(radarSite: String): String {
        val label = GlobalArrays.canadaRadars.findLast { it.startsWith(radarSite) }
        return if (label == null) {
            radarSite
        } else {
            val items = label.split(":")
            items.safeGet(1)
        }
    }

    fun getIcons7Day(html: String): String {
        var iconList = ""
        val days = html.split((MyApplication.newline + MyApplication.newline)).dropLastWhile { it.isEmpty() }
        days.forEach { iconList += translateIconName(it) + "!" }
        return iconList
    }

    fun getIcons7DayAsList(html: String): List<String> {
        val days = html.split((MyApplication.newline + MyApplication.newline)).dropLastWhile { it.isEmpty() }
        return days.map { translateIconName(it) }
    }

    private fun translateIconName(condition: String): String {
        var newName: String
        if (condition.contains("reezing rain or snow")
            || condition.contains("chance of flurries and risk of freezing drizzle")
            || condition.contains("chance of flurries before morning with risk of freezing drizzle")
            || condition.contains("Periods of freezing drizzle or flurries")
            || condition.contains("Flurries or freezing drizzle")
        )
            newName = "fzra_sn"
        else if (condition.contains("eriods of freezing drizzle") || condition.contains("reezing Drizzle") || condition.contains("reezing drizzle") || condition.contains("reezing rain") || condition.contains("isk of freezing drizzle"))
            newName = "fzra"
        else if (condition.contains("Ice Crystals") || condition.contains("ice pellets"))
            newName = "ip"
        else if (condition.contains("hundershowers") || condition.contains("hunderstorm"))
            newName = "scttsra"
        else if (condition.contains("hance of rain showers or flurries")
            || condition.contains("lurries or rain showers")
            || condition.contains(" few rain showers or flurries")
            || condition.contains("ain or snow")
            || condition.contains("eriods of rain or snow")
            || condition.contains("now or rain")
            || condition.contains("ain changing to snow")
            || condition.contains("ain showers or flurries")
            || condition.contains("ain changing to periods of snow")
            || condition.contains("now changing to periods of rain")
            || condition.contains("Rain at times heavy changing to snow")
            || condition.contains("Snow changing to periods of drizzle")
            || condition.contains("Rain at times mixed with wet snow")
        )
            newName = "ra_sn"
        else if (condition.contains("hance of showers") || condition.contains("howers"))
            newName = "shra"
        else if (condition.contains("hance of rain") || condition.contains("Rain beginning") || condition.contains("eriods of drizzle changing to rain at times") || condition.contains("Rain at times heavy"))
            newName = "ra"
        else if (condition.contains("hance of flurries")
            || condition.contains("hance of snow")
            || condition.contains("eriods of snow")
            || condition.contains(" few flurries")
            || condition.contains("eriods of light snow")
            || condition.contains("Flurries")
            || condition.contains("Snow")
            || condition.contains("then snow")
            || condition.contains("ight snow")
        )
            newName = "sn"
        else if (condition.contains("A few showers") || condition.contains("eriods of rain") || condition.contains("drizzle"))
            newName = "hi_shwrs"
        else if (condition.contains("Increasing cloudiness") || condition.contains("Cloudy periods"))
            newName = "bkn"
        else if (condition.contains("Mainly sunny") || condition.contains("A few clouds"))
            newName = "few"
        else if (condition.contains("loudy") || condition.contains("Mainly cloudy") || condition.contains("Overcast"))
            newName = "ovc"
        else if (condition.contains("A mix of sun and cloud") || condition.contains("Partly cloudy") || condition.contains("Clearing"))
            newName = "sct"
        else if (condition.contains("Clear") || condition.contains("Sunny"))
            newName = "skc"
        else if (condition.contains("Blizzard") || condition.contains("Local blowing snow"))
            newName = "blizzard"
        else if (condition.contains("Rain"))
            newName = "ra"
        else if (condition.contains("Mist") || condition.contains("Fog") || condition.contains("Light Drizzle"))
            newName = "fg"
        else
            newName = ""

        if (condition.contains("night")) {
            newName = if (newName.contains("hi_"))
                newName.replace("hi_", "hi_n")
            else
                "n$newName"
        }
        if (condition.contains("percent")) {
            val pop = condition.parse("([0-9]{2}) percent")
            newName += ",$pop"
        }
        return newName
    }

    fun translateIconNameCurrentConditions(condition: String, day1: String): String {
        var newName = ""
        if (condition.contains("eriods of freezing drizzle")
            || condition.contains("Freezing Drizzle")
            || condition.contains("Freezing Rain")
        )
            newName = "fzra"
        else if (condition.contains("hundershowers") || condition.contains("hunderstorm"))
            newName = "scttsra"
        else if (condition.contains("Haze"))
            newName = "fg"
        else if (condition.contains("hance of rain showers or flurries")
            || condition.contains("lurries or rain showers")
            || condition.contains(" few rain showers or flurries")
            || condition.contains("ain or snow")
            || condition.contains("ain and Snow")
            || condition.contains("eriods of rain or snow")
            || condition.contains("now or rain")
        )
            newName = "ra_sn"
        else if (condition.contains("hance of showers") || condition.contains("Showers"))
            newName = "shra"
        else if (condition.contains("hance of rain"))
            newName = "ra"
        else if (condition.contains("A few showers") || condition.contains("eriods of rain")
            || condition.contains("drizzle")
        )
            newName = "hi_shwrs"
        else if (condition.contains("Blizzard")
            || condition.contains("Blowing Snow")
            || condition.contains("Drifting Snow")
        )
            newName = "blizzard"
        else if (condition.contains("hance of flurries")
            || condition.contains("hance of snow")
            || condition.contains("eriods of snow")
            || condition.contains(" few flurries")
            || condition.contains("eriods of light snow")
            || condition.contains("Flurries")
            || condition.contains("Snow")
            || condition.contains("ight snow")
        )
            newName = "sn"
        else if (condition.contains("Increasing cloudiness") || condition.contains("Cloudy periods"))
            newName = "bkn"
        else if (condition.contains("Mainly sunny")
            || condition.contains("A few clouds")
            || condition.contains("Mainly Sunny")
            || condition.contains("Mainly Clear")
        )
            newName = "few"
        else if (condition.contains("Cloudy")
            || condition.contains("Mainly cloudy")
            || condition.contains("Overcast")
        )
            newName = "ovc"
        else if (condition.contains("A mix of sun and cloud")
            || condition.contains("Partly cloudy")
            || condition.contains("Clearing")
        )
            newName = "sct"
        else if (condition.contains("Clear") || condition.contains("Sunny"))
            newName = "skc"
        else if (condition.contains("Rain"))
            newName = "ra"
        else if (condition.contains("Mist")
            || condition.contains("Fog")
            || condition.contains("Light Drizzle")
        )
            newName = "fg"
        else if (condition.contains("Ice Crystals") || condition.contains("Ice Pellets"))
            newName = "ip"
        val time = day1.parse(" ([0-9]{1,2}:[0-9]{2} [AP]M) ")
        val times = time.split(":")
        var daytime = true
        if (times.isNotEmpty()) {
            var hour = times[0].toIntOrNull() ?: 0
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
        if (condition.contains("percent")) {
            val pop = condition.parse("([0-9]{2}) percent")
            newName += ",$pop"
        }
        return newName
    }

    fun getProvidenceHtml(prov: String) = (MyApplication.canadaEcSitePrefix + "/forecast/canada/index_e.html?id=$prov").getHtmlSep()

    fun getLocationHtml(location: LatLon): String {
        val prov = location.latString.split(":").dropLastWhile { it.isEmpty() }
        val id = location.lonString.split(":").dropLastWhile { it.isEmpty() }
        return if (prov.size > 1 && id.isNotEmpty()) {
            (MyApplication.canadaEcSitePrefix + "/rss/city/" + prov[1].toLowerCase(Locale.US) + "-" + id[0] + "_e.xml").getHtmlSep()
        } else {
            ""
        }
    }

    fun getLocationUrl(x: String, y: String): String {
        val prov = x.split(":").dropLastWhile { it.isEmpty() }
        val id = y.split(":").dropLastWhile { it.isEmpty() }
        return if (prov.count() < 2 || id.count() < 1) {
            ""
        } else {
            MyApplication.canadaEcSitePrefix + "/city/pages/" + prov[1].toLowerCase(Locale.US) + "-" + id[0] + "_metric_e.html"
        }
    }

    fun getStatus(html: String) = html.parse("<b>Observed at:</b>(.*?)<br/>")

    fun getRadarSite(x: String, y: String): String {
        val url = (MyApplication.canadaEcSitePrefix + "/city/pages/"
                + x.split(":").dropLastWhile { it.isEmpty() }[1].toLowerCase(Locale.US) + "-"
                + y.split(":").dropLastWhile { it.isEmpty() }[0] + "_metric_e.html")
        val html = url.getHtmlSep()
        return html.parse("<a href=./radar/index_e.html.id=([a-z]{3})..*?>Weather Radar</a>").toUpperCase(Locale.US)
    }

    fun getConditions(html: String): String {
        val sum = html.parse("<b>Condition:</b> (.*?) <br/>.*?<b>Pressure.*?:</b> .*? kPa.*?<br/>")
        val pressure = html.parse("<b>Condition:</b> .*? <br/>.*?<b>Pressure.*?:</b> (.*?) kPa.*?<br/>")
        val vis = html.parse("<b>Visibility:</b> (.*?)<br/>").replace("<.*?>".toRegex(), "").replace("\\s+".toRegex(), "").replace(" miles", "mi")
        val temp = html.parse("<b>Temperature:</b> (.*?)&deg;C <br/>.*?<b>Humidity:</b> .*? %<br/>.*?<b>Dewpoint:</b> .*?&deg;C <br/>")
        val rh = html.parse("<b>Temperature:</b> .*?&deg;C <br/>.*?<b>Humidity:</b> (.*?) %<br/>.*?<b>Dewpoint:</b> .*?&deg;C <br/>")
        val dew = html.parse("<b>Temperature:</b> .*?&deg;C <br/>.*?<b>Humidity:</b> .*? %<br/>.*?<b>Dewpoint:</b> (.*?)&deg;C <br/>")
        val wind = Utility.fromHtml(html.parse("<b>Wind:</b> (.*?)<br/>")).replace(MyApplication.newline, "")
        return temp + MyApplication.DEGREE_SYMBOL + " / " + dew + MyApplication.DEGREE_SYMBOL + " (" + rh + "%) - " + pressure + "kPa - " + wind + " - " + vis + " - " + sum + " "
    }

    fun get7Day(html: String): String {
        val stringList = html.parseColumn("<category term=\"Weather Forecasts\"/><br> <summary type=\"html\">(.*?\\.) Forecast.*?</summary>")
        var sevenDayForecast = ""
        val resultListDay = html.parseColumn("<title>(.*?)</title>")
        var j = 0
        for (i in 2 until resultListDay.size) {
            if (resultListDay[i].contains("Current Conditions")) continue
            if (!resultListDay[i].contains(":")) continue
            val string = resultListDay[i].split(":")[0] + ": " + stringList[j]
            sevenDayForecast += string + MyApplication.newline + MyApplication.newline
            j += 1
        }
        return sevenDayForecast
    }

    fun getHazards(html: String): List<String> {
        var urls = html.parseColumn("<div id=\"statement\" class=\"floatLeft\">.*?<a href=\"(.*?)\">.*?</a>.*?</div>")
        var titles = html.parseColumn("<div id=\"statement\" class=\"floatLeft\">.*?<a href=\".*?\">(.*?)</a>.*?</div>")
        val statementUrl = urls.joinToString("")
        val statement = titles.joinToString("<BR>")
        var chunk = html.parse("<entry>(.*?)<category term=\"Warnings and Watches\"/>")
        urls = chunk.parseColumn("<title>.*?</title>.*?<link type=\"text/html\" href=\"(.*?)\"/>")
        titles = chunk.parseColumn("<title>(.*?)</title>.*?<link type=\"text/html\" href=\".*?\"/>")
        val warningUrl = urls.joinToString(",")
        val warning = titles.joinToString("<BR>")
        chunk = html.parse("<div id=\"watch\" class=\"floatLeft\">(.*?)</div>")
        urls = chunk.parseColumn("<a href=\"(.*?)\">.*?</a>")
        titles = chunk.parseColumn("<a href=\".*?\">(.*?)</a>")
        val watchUrl = urls.joinToString(",${MyApplication.canadaEcSitePrefix}")
        val watch = titles.joinToString("<BR>")
        val result = mutableListOf(warning + statement + watch, "$warningUrl,$statementUrl,$watchUrl")
        if (!result[0].contains("No watches or warnings in effect")) {
            result[1] = getHazardsFromUrl(warningUrl)
        } else {
            result[1] = result[0]
        }
        result[1] = result[1].replace("ATOM", "")
        return result
    }

    fun getHazardsFromUrl(url: String): String {
        val urls = url.split(",").dropLastWhile { it.isEmpty() }
        var notFound = true
        var warningData = ""
        urls.forEach {
            if (it != "" && notFound) {
                warningData += it.getHtml().parse("<main.*?container.>(.*?)</div>")
                notFound = false
            }
        }
        warningData = warningData.replace("<li><img src=./cacheable/images/img/feed-icon-14x14.png. alt=.ATOM feed.> <a href=./rss/battleboard/.*?.>ATOM</a></li>".toRegex(), "")
        warningData = warningData.replace(" <div class=\"col-xs-12\">", "").replace("<section class=\"followus hidden-print\"><h2>Follow:</h2>", "")
        warningData = warningData.replace("<a href=\"/rss/battleboard/.*?.xml\"><img src=\"/cacheable/images/img/feed-icon-14x14.png\" alt=\"ATOM feed\" class=\"mrgn-rght-sm\">ATOM</a>", "")
        warningData = warningData.replace("<div class=\"row\">", "")
        return warningData
    }

    fun getSectorFromProvince(prov: String) = provinceToSector[prov] ?: ""

    fun isLabelPresent(label: String): Boolean {
        UtilityCitiesCanada.initialize()
        return UtilityCitiesCanada.cities.any { it.contains(label) }
    }

    fun getLatLonFromLabel(label: String): LatLon {
        val latLon = DoubleArray(2)
        UtilityCitiesCanada.initialize()
        for (index in UtilityCitiesCanada.cities.indices) {
            if (UtilityCitiesCanada.cities[index] == label) {
                latLon[0] = UtilityCitiesCanada.lat[index]
                latLon[1] = UtilityCitiesCanada.lon[index]
                break
            }
        }
        return LatLon(latLon[0].toString(), latLon[1].toString())
    }
}


