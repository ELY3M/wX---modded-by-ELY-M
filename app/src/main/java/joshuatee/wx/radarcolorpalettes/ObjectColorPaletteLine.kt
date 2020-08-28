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

package joshuatee.wx.radarcolorpalettes

import android.graphics.Color

// represents the items in a single line of a colorpal file
// dbz r g b
class ObjectColorPaletteLine {

    val dbz: Int
    val red: Int
    val green: Int
    val blue: Int

    constructor(items: List<String>) {
        dbz = items[1].toIntOrNull() ?: 0
        red = items[2].toIntOrNull() ?: 0
        green = items[3].toIntOrNull() ?: 0
        blue = items[4].toIntOrNull() ?: 0
    }

    constructor(items: List<String>, fn: (List<String>) -> Int) {
        dbz = fn(items)
        red = items[2].toIntOrNull() ?: 0
        green = items[3].toIntOrNull() ?: 0
        blue = items[4].toIntOrNull() ?: 0
    }

    constructor(dbz: Int, red: String, green: String, blue: String) {
        this.dbz = dbz
        this.red = red.toIntOrNull() ?: 0
        this.green = green.toIntOrNull() ?: 0
        this.blue = blue.toIntOrNull() ?: 0
    }

    val asInt get() = Color.rgb(red, green, blue)
}


