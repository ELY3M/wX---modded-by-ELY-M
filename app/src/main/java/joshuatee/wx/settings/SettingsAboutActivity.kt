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
//modded by ELY M.

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectPopupMessage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityShare

class SettingsAboutActivity : BaseActivity() {

    private var html = ""
    private lateinit var textCard: ObjectCardText
    private val faqUrl = "https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md"
    private val iOSUrl = "https://apps.apple.com/us/app/wxl23/id1171250052"
    //private val releaseNotesUrl = "https://docs.google.com/document/u/1/d/e/2PACX-1vT-YfH9yH_qmxLHe25UGlJvHHj_25qmTHJoeWPBbNWlvS4nm0YBmFeAnEpeel3GTL3OYKnvXkMNbnOX/pub"
    private val releaseNotesUrl = "https://github.com/ELY3M/wX---modded-by-ELY-M/blob/master/README.md"
    private lateinit var box: LinearLayout

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.generic_about, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.generic_about, false)
        box = findViewById(R.id.linearLayout)
        val version = Utility.getVersion(this)
        toolbar.subtitle = "version: $version"

        val faqButton = ObjectCardText(this, box, toolbar, toolbarBottom)
        faqButton.setTextColor(UIPreferences.textHighlightColor)
        faqButton.text = "View FAQ (current app issues listed at top)"
        faqButton.setOnClickListener { ObjectIntent.showWeb(this, faqUrl) }

        val releaseNotesButton = ObjectCardText(this, box, toolbar, toolbarBottom)
        releaseNotesButton.setTextColor(UIPreferences.textHighlightColor)
        releaseNotesButton.text = "View release notes"
        releaseNotesButton.setOnClickListener { ObjectIntent.showWeb(this, releaseNotesUrl) }

        textCard = ObjectCardText(this, box, toolbar, toolbarBottom)
        val cardDeleteFiles = ObjectCardText(this, "Delete old radar files (should not be needed)", UIPreferences.textSizeNormal, UIPreferences.paddingSettings)
        cardDeleteFiles.setOnClickListener {
            ObjectPopupMessage(box, "Deleted old radar files: " + UtilityFileManagement.deleteCacheFiles(this))
        }
        box.addView(cardDeleteFiles.get())
        displayContent()
    }

    private fun displayContent() {
        textCard.text = Utility.showVersion(this)
        html = Utility.showVersion(this)
    }

    override fun onRestart() {
        textCard.text = Utility.showVersion(this)
        html = Utility.showVersion(this)
        super.onRestart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "About wX", html)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        textCard.text = keyCode.toString() + " " + Utility.showVersion(this)
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> true
            KeyEvent.KEYCODE_DPAD_RIGHT -> true
            KeyEvent.KEYCODE_DPAD_UP -> true
            KeyEvent.KEYCODE_DPAD_DOWN -> true
            else -> super.onKeyUp(keyCode, event)
        }
    }
}
