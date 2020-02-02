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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.WebView
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SettingsAboutActivity : AudioPlayActivity(), OnMenuItemClickListener {

    private lateinit var activityArguments: Array<String>
    private var html = ""
    private lateinit var textCard: ObjectCardText
    private var keyCodeDiag = ""
    private val faqUrl = "https://docs.google.com/document/d/1OQrviP10XBvQZ7QKh5R4bsd72ZKffK5f0ISRuCaSk5k/edit?usp=sharing"
    private val iOSUrl = "https://apps.apple.com/us/app/wxl23/id1171250052"
    //private val releaseNotesUrl = "https://docs.google.com/document/d/1A7rvP3QrJg0QqoEtKgU4B_VqLkjECijb4CFtXyNQNAM/edit?usp=sharing"
    private val releaseNotesUrl = "https://github.com/ELY3M/wX---modded-by-ELY-M/blob/master/README.md"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.shared_tts
        )
        val menu = toolbarBottom.menu
        val playlistMi = menu.findItem(R.id.action_playlist)
        playlistMi.isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        val version = Utility.getVersion(this)
        toolbar.subtitle = "version: $version"
        val faqButton = ObjectCardText(this, ll, toolbar, toolbarBottom)
        faqButton.setTextColor(UIPreferences.textHighlightColor)
        faqButton.text = "View FAQ (Outage notifications listed at top if any current)"
        faqButton.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    WebView::class.java,
                    WebView.URL,
                    arrayOf(faqUrl, "wX Frequently Asked Questions")
            )
        })
        val releaseNotesButton = ObjectCardText(this, ll, toolbar, toolbarBottom)
        releaseNotesButton.setTextColor(UIPreferences.textHighlightColor)
        releaseNotesButton.text = "View release notes"
        releaseNotesButton.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    WebView::class.java,
                    WebView.URL,
                    arrayOf(releaseNotesUrl, "wX Frequently Asked Questions")
            )
        })
        val emailButton = ObjectCardText(this, ll, toolbar, toolbarBottom)
        emailButton.setTextColor(UIPreferences.textHighlightColor)
        emailButton.text = "Email developer"
        emailButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(MyApplication.emailAsString))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            startActivity(Intent.createChooser(intent, "Send Email"))
        })
        val iOSVersion = ObjectCardText(this, ll, toolbar, toolbarBottom)
        iOSVersion.setTextColor(UIPreferences.textHighlightColor)
        iOSVersion.text = "iOS port of wX is called wXL23"
        iOSVersion.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    WebView::class.java,
                    WebView.URL,
                    arrayOf(iOSUrl, "wXL23 for iOS")
            )
        })
        textCard = ObjectCardText(this, ll, toolbar, toolbarBottom)
        textCard.text = keyCodeDiag + " " + Utility.showVersion(this, this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, "txt", "txt")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                this,
                activityArguments[1],
                Utility.fromHtml(html)
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        textCard.text = keyCode.toString() + " " + Utility.showVersion(this, this)
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}
