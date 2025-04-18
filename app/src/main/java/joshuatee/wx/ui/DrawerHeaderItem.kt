/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout

class DrawerHeaderItem(
    drawerLayout: DrawerLayout,
    headerLayout: View,
    buttonId: Int,
    textId: Int,
    tint: ColorStateList,
    gravityForDrawer: Int,
    fn: () -> Unit
) {

    val button: ImageButton = headerLayout.findViewById(buttonId)
    val text: TextView = headerLayout.findViewById(textId)

    init {
        button.backgroundTintList = tint
        button.setOnClickListener {
            fn()
            drawerLayout.closeDrawer(gravityForDrawer)
        }
        text.setOnClickListener {
            fn()
            drawerLayout.closeDrawer(gravityForDrawer)
        }
    }
}
