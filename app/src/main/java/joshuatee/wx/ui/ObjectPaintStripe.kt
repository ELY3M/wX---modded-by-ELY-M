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

package joshuatee.wx.ui

import android.graphics.Color
import android.graphics.Paint
import joshuatee.wx.settings.UIPreferences

class ObjectPaintStripe {

    val paint = Paint()

    init {
        val red = Color.red(UIPreferences.nwsIconBottomColor)
        val green = Color.green(UIPreferences.nwsIconBottomColor)
        val blue = Color.blue(UIPreferences.nwsIconBottomColor)
        paint.color = Color.argb(200, red, green, blue)
    }
}
