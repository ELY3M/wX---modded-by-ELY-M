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

import joshuatee.wx.Extensions.*

internal object UtilityWarningsImpact {

    private const val url = "http://www.weather.gov/source/crh/impact/filelist2.json"

    // main site is http://www.weather.gov/crh/impact

    val impactWarningData: List<ObjectImpactGraphic>
        get() {
            val warningsList = mutableListOf<ObjectImpactGraphic>()
            val html = url.getHtmlSep()
            val outerChunk = html.parse("\\[(.*?)\\]")
            val warningListTmp = outerChunk.parseColumn("\\{(.*?)\\}")
            warningListTmp.forEach {
                val title = it.parse("msg.:.(.*?)\"")
                val cities = it.parse("city_list.:.(.*?).,").replace("including ", "")
                val population = it.parse("population.:.(.*?)\"")
                val file = it.parse("file.:.(.*?png)")
                warningsList.add(ObjectImpactGraphic(title, cities, population, file))
            }
            return warningsList
        }
}
