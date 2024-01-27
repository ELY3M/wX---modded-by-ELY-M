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
//modded by ELY M.

package joshuatee.wx.settings

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityMath
import joshuatee.wx.util.UtilityShare

class SettingsAboutActivity : BaseActivity() {

    private lateinit var cardText: CardText
    private val faqUrl = "https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md"
    //private val releaseNotesUrl = "https://gitlab.com/joshua.tee/wx/-/tree/master/doc/ChangeLog_User.md"
    private val releaseNotesUrl = "https://github.com/ELY3M/wX---modded-by-ELY-M/blob/master/README.md"
    private lateinit var box: VBox

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.generic_about, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.generic_about, false)
        box = VBox.fromResource(this)
        setTitle("About wX", "version " + Utility.getVersion(this))
        addCards()
    }

    private fun addCards() {
        val faqButton = CardText(this, "View FAQ") { Route.web(this, faqUrl) }
        val releaseNotesButton = CardText(this, "View Release Notes") { Route.web(this, releaseNotesUrl) }
        val developerSettingsCard = CardText(this, "Developer Settings", SettingsDeveloperActivity::class.java)
        cardText = CardText(this, Utility.showVersion(this))
        val cardDeleteFiles = CardText(this, "Delete old radar files (should not be needed)")
        cardDeleteFiles.connect {
            PopupMessage(box.get(), "Deleted old radar files: " + UtilityFileManagement.deleteCacheFiles(this))
        }
        box.addWidget(faqButton)
        box.addWidget(releaseNotesButton)
        box.addWidget(CardText(this, "Celsius to fahrenheit table"
        ) { Route.text(this, UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table") })
        box.addWidget(developerSettingsCard)
        box.addWidget(cardText)
        box.addWidget(cardDeleteFiles)
    }

    override fun onRestart() {
        cardText.text = Utility.showVersion(this)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "About wX", Utility.showVersion(this))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        cardText.text = keyCode.toString() + " " + Utility.showVersion(this)
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> true
            KeyEvent.KEYCODE_DPAD_RIGHT -> true
            KeyEvent.KEYCODE_DPAD_UP -> true
            KeyEvent.KEYCODE_DPAD_DOWN -> true
            else -> super.onKeyUp(keyCode, event)
        }
    }
}
