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
//modded by ELY M. 

package joshuatee.wx

import java.util.regex.Pattern

object RegExp {

    val nws7DayTemp1: Pattern = Pattern.compile("with a low around (-?[0-9]{1,3})\\.")
    val nws7DayTemp2: Pattern = Pattern.compile("with a high near (-?[0-9]{1,3})\\.")
    val nws7DayTemp3: Pattern = Pattern.compile("teady temperature around (-?[0-9]{1,3})\\.")
    val nws7DayTemp4: Pattern = Pattern.compile("Low around (-?[0-9]{1,3})\\.")
    val nws7DayTemp5: Pattern = Pattern.compile("High near (-?[0-9]{1,3})\\.")
    val nws7DayTemp6: Pattern = Pattern.compile("emperature falling to around (-?[0-9]{1,3}) ")
    val nws7DayTemp7: Pattern = Pattern.compile("emperature rising to around (-?[0-9]{1,3}) ")
    val nws7DayTemp8: Pattern = Pattern.compile("emperature falling to near (-?[0-9]{1,3}) ")
    val nws7DayTemp9: Pattern = Pattern.compile("emperature rising to near (-?[0-9]{1,3}) ")
    val nws7DayTemp10: Pattern = Pattern.compile("High near (-?[0-9]{1,3}),")
    val nws7DayTemp11: Pattern = Pattern.compile("Low around (-?[0-9]{1,3}),")

    val sevenDayWind1: Pattern = Pattern.compile("wind ([0-9]*) to ([0-9]*) mph")
    val sevenDayWind2: Pattern = Pattern.compile("wind around ([0-9]*) mph")
    val sevenDayWind3: Pattern = Pattern.compile("with gusts as high as ([0-9]*) mph")
    val sevenDayWind4: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph after")
    val sevenDayWind5: Pattern = Pattern.compile(" around ([0-9]*) mph after ")
    val sevenDayWind6: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph in ")
    val sevenDayWind7: Pattern = Pattern.compile("around ([0-9]*) mph")
    val sevenDayWind8: Pattern = Pattern.compile("Winds could gust as high as ([0-9]*) mph\\.")
    val sevenDayWind9: Pattern = Pattern.compile(" ([0-9]*) to ([0-9]*) mph.")
    val sevenDayWinddir1: Pattern = Pattern.compile("\\. (\\w+\\s?\\w*) wind ")
    val sevenDayWinddir2: Pattern = Pattern.compile("wind becoming (.*?) [0-9]")
    val sevenDayWinddir3: Pattern = Pattern.compile("wind becoming (\\w+\\s?\\w*) around")
    val sevenDayWinddir4: Pattern = Pattern.compile("Breezy, with a[n]? (.*?) wind")
    val sevenDayWinddir5: Pattern = Pattern.compile("Windy, with a[n]? (.*?) wind")
    val sevenDayWinddir6: Pattern = Pattern.compile("Blustery, with a[n]? (.*?) wind")
    val sevenDayWinddir7: Pattern = Pattern.compile("Light (.*?) wind")
    val patternMetarWxogl1: Pattern = Pattern.compile(".*? (M?../M?..) .*?")
    val patternMetarWxogl2: Pattern = Pattern.compile(".*? A([0-9]{4})")
    val patternMetarWxogl3: Pattern = Pattern.compile("AUTO ([0-9].*?KT) .*?")
    val patternMetarWxogl4: Pattern = Pattern.compile("Z ([0-9].*?KT) .*?")
    val patternMetarWxogl5: Pattern = Pattern.compile("SM (.*?) M?[0-9]{2}/")

    val utilnxanimPattern1: Pattern = Pattern.compile(">(sn.[0-9]{4})</a>")
    val utilnxanimPattern2: Pattern = Pattern.compile(".*?([0-9]{2}-[A-Za-z]{3}-[0-9]{4} [0-9]{2}:[0-9]{2}).*?")
    val uswarnPattern: Pattern = Pattern.compile("<entry>.*?<id>(.+?)</id>.*?<title>(.+?)</title>.*?<summary>(.*?)</summary>.*?<cap:event>(.*?)</cap:event>.*?<cap:areaDesc>(.*?)</cap:areaDesc>.*?<valueName>UGC</valueName>.*?<value>(.*?)</value>.*?<valueName>VTEC</valueName>.*?<value>(.*?)</value>.*?</entry>.*?")
    val stiPattern1: Pattern = Pattern.compile("AZ/RAN(.*?)V")
    val stiPattern2: Pattern = Pattern.compile("MVT(.*?)V")
    val stiPattern3: Pattern = Pattern.compile("\\d+")
    val hiPattern1: Pattern = Pattern.compile("AZ/RAN(.*?)V")
    val hiPattern2: Pattern = Pattern.compile("POSH/POH(.*?)V")
    val hiPattern3: Pattern = Pattern.compile("MAX HAIL SIZE(.*?)V")
    val hiPattern4: Pattern = Pattern.compile("[0-9]*\\.?[0-9]+")
    val tvsPattern1: Pattern = Pattern.compile("P {2}TVS(.{20})")
    val tvsPattern2: Pattern = Pattern.compile(".{9}(.{7})")
    val utilNhcPattern1: Pattern = Pattern.compile("<title>(.*?)</title>")
    val utilNhcPattern2: Pattern = Pattern.compile("<nhc:Cyclone>(.*?)</nhc:Cyclone>")
    val utilNhcPattern3: Pattern = Pattern.compile("<link>.*?(https://www.nhc.noaa.gov/text/refresh/MIATCP[AE][TP][0-9].shtml/.*?shtml).*?</link>")
    val utilNhcPattern4: Pattern = Pattern.compile("<nhc:wallet>(.*?)</nhc:wallet>")
    val utilNhcPattern5: Pattern = Pattern.compile("<img src=.(.*?png).")
    val srefPattern1: Pattern = Pattern.compile("([0-9]{2}z)")
    val srefPattern2: Pattern = Pattern.compile("([0-9]{10}z</a>&nbsp in through <b>f[0-9]{3})")
    val srefPattern3: Pattern = Pattern.compile("<tr><td class=.previous.><a href=sref.php\\?run=[0-9]{10}&id=SREF_H5__>([0-9]{10}z)</a></td></tr>")
    val ncepPattern1: Pattern = Pattern.compile("([0-9]{2}Z)")
    val ncepPattern2: Pattern = Pattern.compile("([0-9]{10}UTC selected_cell)")
    val eslHrrrPattern1: Pattern = Pattern.compile("<option selected>([0-9]{2} \\w{3} [0-9]{4} - [0-9]{2}Z)<.option>")
    val eslHrrrPattern2: Pattern = Pattern.compile("<option>([0-9]{2} \\w{3} [0-9]{4} - [0-9]{2}Z)<.option>")
    val eslHrrrPattern3: Pattern = Pattern.compile("[0-9]{2} \\w{3} ([0-9]{4}) - [0-9]{2}Z")
    val eslHrrrPattern4: Pattern = Pattern.compile("([0-9]{2}) \\w{3} [0-9]{4} - [0-9]{2}Z")
    val eslHrrrPattern5: Pattern = Pattern.compile("[0-9]{2} \\w{3} [0-9]{4} - ([0-9]{2})Z")
    val eslHrrrPattern6: Pattern = Pattern.compile("[0-9]{2} (\\w{3}) [0-9]{4} - [0-9]{2}Z")

    val ca7DayTemp1: Pattern = Pattern.compile("Temperature falling to (minus [0-9]{1,2}) this")
    val ca7DayTemp2: Pattern = Pattern.compile("Low (minus [0-9]{1,2})\\.")
    val ca7DayTemp3: Pattern = Pattern.compile("High (minus [0-9]{1,2})\\.")
    val ca7DayTemp4: Pattern = Pattern.compile("Low plus ([0-9]{1,2})\\.")
    val ca7DayTemp5: Pattern = Pattern.compile("High plus ([0-9]{1,2})\\.")
    val ca7DayTemp6: Pattern = Pattern.compile("steady near (minus [0-9]{1,2})\\.")
    val ca7DayTemp7: Pattern = Pattern.compile("steady near plus ([0-9]{1,2})\\.")
    val ca7DayTemp8: Pattern = Pattern.compile("rising to (minus [0-9]{1,2}) ")
    val ca7DayTemp9: Pattern = Pattern.compile("falling to (minus [0-9]{1,2}) ")
    val ca7DayTemp10: Pattern = Pattern.compile("Low (minus [0-9]{1,2}) ")
    val ca7DayTemp11: Pattern = Pattern.compile("Low (zero)\\.")
    val ca7DayTemp12: Pattern = Pattern.compile("rising to ([0-9]{1,2}) ")
    val ca7DayTemp13: Pattern = Pattern.compile("High ([0-9]{1,2})[\\. ]")
    val ca7DayTemp14: Pattern = Pattern.compile("rising to plus ([0-9]{1,2}) ")
    val ca7DayTemp15: Pattern = Pattern.compile("falling to plus ([0-9]{1,2}) ")
    val ca7DayTemp16: Pattern = Pattern.compile("High (zero)\\.")
    val ca7DayTemp17: Pattern = Pattern.compile("rising to (zero) by")
    val ca7DayTemp18: Pattern = Pattern.compile("Low ([0-9]{1,2})\\.")
    val ca7DayTemp19: Pattern = Pattern.compile("High ([0-9]{1,2}) with temperature")
    val ca7DayTemp20: Pattern = Pattern.compile("Temperature falling to (zero) in")
    val ca7DayTemp21: Pattern = Pattern.compile("steady near ([0-9]{1,2})\\.")
    val ca7DayTemp22: Pattern = Pattern.compile("steady near (zero)\\.")
    val ca7DayWinddir1: Pattern = Pattern.compile("Wind ([a-z]*?) [0-9]{2,3} ")
    val ca7DayWinddir2: Pattern = Pattern.compile("Wind becoming ([a-z]*?) [0-9]{2,3} ")
    val ca7DayWindspd1: Pattern = Pattern.compile("([0-9]{2,3}) to ([0-9]{2,3}) km/h")
    val ca7DayWindspd2: Pattern = Pattern.compile("( [0-9]{2,3}) km/h")
    val ca7DayWindspd3: Pattern = Pattern.compile("gusting to ([0-9]{2,3})")

    val warningVtecPattern: Pattern = Pattern.compile("([A-Z0]\\.[A-Z]{3}\\.[A-Z]{4}\\.[A-Z]{2}\\.[A-Z]\\.[0-9]{4}\\.[0-9]{6}T[0-9]{4}Z\\-[0-9]{6}T[0-9]{4}Z)")
    val warningLatLonPattern: Pattern = Pattern.compile("\"coordinates\":\\[\\[(.*?)\\]\\]\\}")
    val watchPattern: Pattern = Pattern.compile("[om] Watch #([0-9]*?)</a>")
    val mcdPatternAlertr: Pattern = Pattern.compile("<strong><a href=./products/md/md.....html.>Mesoscale Discussion #(.*?)</a></strong>")
    val mcdPatternUtilspc: Pattern = Pattern.compile(">Mesoscale Discussion #(.*?)</a>")
    val mpdPattern: Pattern = Pattern.compile(">MPD #(.*?)</a></strong>")
    val prePattern: Pattern = Pattern.compile("<pre.*?>(.*?)</pre>")
    val pre2Pattern: Pattern = Pattern.compile("<pre>(.*?)</pre>")

    val txtfile: Pattern = Pattern.compile("(.*?).txt$")
    val palfile94: Pattern = Pattern.compile("(.*?)_94.txt(.*?)")
    val palfile99: Pattern = Pattern.compile("(.*?)_99.txt(.*?)")
}

