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

package joshuatee.wx.radar

import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.ObjectToolbar

class NexradSubmenu(objectToolbarBottom: ObjectToolbar, private val nexradState: NexradState) {

    var starButton: MenuItem
    private var animateButton: MenuItem
    private var tiltMenu: MenuItem
    private var tiltMenuOption4: MenuItem
    private var l3Menu: MenuItem
    private var l2Menu: MenuItem
    private var tdwrMenu: MenuItem
    private val animateButtonPlayString = "Animate Frames"
    private val animateButtonStopString = "Stop animation"
    private val pauseButtonString = "Pause animation"
    private val starButtonString = "Toggle favorite"
    private val resumeButtonString = "Resume animation"

    init {
        starButton = objectToolbarBottom.getFavIcon()
        animateButton = objectToolbarBottom.find(R.id.action_a)
        tiltMenu = objectToolbarBottom.find(R.id.action_tilt)
        tiltMenuOption4 = objectToolbarBottom.find(R.id.action_tilt4)
        l3Menu = objectToolbarBottom.find(R.id.action_l3)
        l2Menu = objectToolbarBottom.find(R.id.action_l2)
        tdwrMenu = objectToolbarBottom.find(R.id.action_tdwr)
        if (nexradState.numberOfPanes == 2) {
            val rad3 = objectToolbarBottom.find(R.id.action_radar3)
            val rad4 = objectToolbarBottom.find(R.id.action_radar4)
            rad3.isVisible = false
            rad4.isVisible = false
        } else if (nexradState.numberOfPanes == 4) {
            val quadPaneJump = objectToolbarBottom.find(R.id.action_radar_4)
            quadPaneJump.isVisible = false
        }
        if (!UIPreferences.radarImmersiveMode) {
            objectToolbarBottom.hide(R.id.action_blank)
            objectToolbarBottom.hide(R.id.action_level3_blank)
            objectToolbarBottom.hide(R.id.action_level2_blank)
            objectToolbarBottom.hide(R.id.action_animate_blank)
            objectToolbarBottom.hide(R.id.action_tilt_blank)
            objectToolbarBottom.hide(R.id.action_tools_blank)
        }
        // FIXME TODO disable new Level3 super-res until NWS is past deployment phase
        objectToolbarBottom.hide(R.id.action_n0b)
        objectToolbarBottom.hide(R.id.action_n0g)
    }

    fun adjustTiltAndProductMenus() {
        if (nexradState.isTdwr) {
            l3Menu.isVisible = false
            l2Menu.isVisible = false
            tdwrMenu.isVisible = true
        } else {
            l3Menu.isVisible = true
            l2Menu.isVisible = true
            tdwrMenu.isVisible = false
        }
        if (nexradState.isTdwr) {
            tiltMenuOption4.isVisible = false
            tiltMenu.isVisible = nexradState.product.matches(Regex("[A-Z][A-Z][0-2]"))
        } else {
            tiltMenuOption4.isVisible = true
            tiltMenu.isVisible = nexradState.product.matches(Regex("[A-Z][0-3][A-Z]"))
        }
    }

    fun setStarButton() {
        if (UIPreferences.ridFav.contains(":" + nexradState.radarSite + ":")) {
            starButton.setIcon(GlobalVariables.STAR_ICON_WHITE)
        } else {
            starButton.setIcon(GlobalVariables.STAR_OUTLINE_ICON_WHITE)
        }
        starButton.title = starButtonString
    }

    fun setAnimateToPlay() {
        animateButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
        animateButton.title = animateButtonPlayString
    }

    fun setAnimateToStop() {
        animateButton.setIcon(GlobalVariables.ICON_STOP_WHITE)
        animateButton.title = animateButtonStopString
    }

    fun setAnimateToPause() {
        starButton.setIcon(GlobalVariables.ICON_PAUSE_WHITE)
        starButton.title = pauseButtonString
    }

    fun setAnimateToResume() {
        starButton.setIcon(GlobalVariables.ICON_PLAY_WHITE)
        starButton.title = resumeButtonString
    }
}
