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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility

class SettingsNavDrawerActivity : BaseActivity() {

    private var tokenList = ""
    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_navdrawer, null, false)
        setTitle("Navigation Drawer", "Turn items off or on for the main screen nav drawer.")
        box = VBox.fromResource(this)
        tokenList = UtilityNavDrawer.getNavDrawerTokenList(this)
        addCards()
    }

    private fun addCards() {
        UtilityNavDrawer.labels.forEach {
            box.addWidget(
                Switch(
                    this,
                    it,
                    UtilityNavDrawer.getPrefVar(UtilityNavDrawer.labelToTokenMap[it] ?: ""),
                    R.string.nav_drawer_main_screen_toggle
                )
            )
        }
    }

    override fun onStop() {
        UtilityNavDrawer.generateNewTokenList(this)
        if (UIPreferences.navDrawerMainScreen) {
            Utility.restart()
        }
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                UtilityNavDrawer.generateNewTokenList(this)
                Utility.restart()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
