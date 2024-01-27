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

import androidx.appcompat.widget.Toolbar
import android.view.View
import joshuatee.wx.settings.UIPreferences

object UtilityToolbar {

    fun transparentToolbars(toolbar: ObjectToolbar, toolbarBottom: ObjectToolbar) {
        if (UIPreferences.radarToolbarTransparent) {
            toolbar.background.mutate().alpha = 0
            toolbarBottom.background.mutate().alpha = 0
        }
    }

    fun fullScreenMode(toolbar: Toolbar, toolbarBottom: Toolbar) {
        toolbar.elevation = UIPreferences.elevationPref
        toolbarBottom.elevation = UIPreferences.elevationPref
        if (UIPreferences.fullscreenMode) {
            toolbar.visibility = View.GONE
            toolbarBottom.visibility = View.GONE
        }
    }

    fun setElevation(toolbar: Toolbar) {
        toolbar.elevation = UIPreferences.elevationPref
    }

    fun showHide(toolbar: Toolbar, toolbarBottom: Toolbar) {
        if (!UIPreferences.lockToolbars) {
            if (toolbar.isShown) {
                toolbar.visibility = View.GONE
                toolbarBottom.visibility = View.GONE
            } else {
                toolbar.visibility = View.VISIBLE
                toolbarBottom.visibility = View.VISIBLE
            }
        }
    }

    fun showHide(toolbar: Toolbar) {
        if (!UIPreferences.lockToolbars) {
            if (toolbar.isShown) {
                toolbar.visibility = View.GONE
            } else {
                toolbar.visibility = View.VISIBLE
            }
        }
    }
}
