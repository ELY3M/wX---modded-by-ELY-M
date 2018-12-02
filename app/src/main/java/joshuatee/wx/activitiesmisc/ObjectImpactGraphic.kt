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

internal class ObjectImpactGraphic(var title: String, var cities: String, var population: String, var imgFile: String) {

    companion object {
        private const val baseImageUrl = "https://www.weather.gov/images/crh/impact/"
    }

    init {
        this.title = title.replace("^.*?Sev".toRegex(), "Sev")
        this.cities = "Cities: " + cities.replace("\"", "")
        this.population = "Population: " + population.replace("\"", "")
        this.imgFile = baseImageUrl + imgFile.replace("\"", "").replace("file:", "")
    }
}

