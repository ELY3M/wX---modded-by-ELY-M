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

package joshuatee.wx.models

internal object UtilityModelGlcfsInterface {

    // http://www.glerl.noaa.gov/res/glcfs

    val models = listOf("GLCFS")

    val sectors = listOf(
        "Lake Superior",
        "Lake Michigan",
        "Lake Huron",
        "Lake Erie",
        "Lake Ontario",
        "All Lakes"
    )

    val params = listOf(
        "wv",
        "wn",
        "swt",
        "sfcur",
        "wl",
        "wl1d",
        "cl",
        "at"
    )

    val labels = listOf(
        "Wave height",
        "Wind speed",
        "Surface temperature",
        "Surface currents",
        "Water level displacement",
        "Water level displacement 1D",
        "Cloud cover (5 lake view only)",
        "Air temp (5 lake view only)"
    )
}
