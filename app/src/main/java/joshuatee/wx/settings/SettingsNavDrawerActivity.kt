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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityAlertDialog

class SettingsNavDrawerActivity : BaseActivity() {

    private var tokenList = ""
    private lateinit var linearLayout: LinearLayout

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_navdrawer, null, false)
        linearLayout = findViewById(R.id.linearLayout)
        tokenList = UtilityNavDrawer.getNavDrawerTokenList(this)
        title = "Navigation Drawer"
        toolbar.subtitle = "Turn items off or on for the main screen nav drawer."
        UtilityNavDrawer.labels.forEach {
            linearLayout.addView(
                    ObjectSettingsCheckBox(
                            this,
                            it,
                            UtilityNavDrawer.getPrefVar(UtilityNavDrawer.labelToTokenMap[it] ?: ""),
                            R.string.nav_drawer_main_screen_toggle).card
            )
        }
    }

    override fun onBackPressed() {
        // val newTokenList = UtilityNavDrawer.generateNewTokenList(this)
        // if (tokenList != newTokenList && tokenList != "") {
            UtilityAlertDialog.restart()
        // } else {
        //    super.onBackPressed()
        // }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // val newTokenList = UtilityNavDrawer.generateNewTokenList(this)
        when (item.itemId) {
            android.R.id.home -> {
                //if (tokenList != newTokenList && tokenList != "")
                    UtilityAlertDialog.restart()
                //else
                //    NavUtils.navigateUpFromSameTask(this)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
