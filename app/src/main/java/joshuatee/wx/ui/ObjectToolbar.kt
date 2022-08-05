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

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R

class ObjectToolbar(private val toolbar: Toolbar) {

    fun hide(resId: Int) {
        toolbar.menu.findItem(resId).isVisible = false
    }

    fun hideRadar() {
        toolbar.menu.findItem(R.id.action_radar).isVisible = false
    }

    fun getFavIcon(): MenuItem {
        return toolbar.menu.findItem(R.id.action_fav)
    }

    fun find(resId: Int): MenuItem {
        return toolbar.menu.findItem(resId)
    }

    fun connect(fn: Toolbar.OnMenuItemClickListener) {
        toolbar.setOnMenuItemClickListener(fn)
    }
}
