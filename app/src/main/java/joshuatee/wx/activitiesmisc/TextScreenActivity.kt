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

package joshuatee.wx.activitiesmisc

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.ui.CardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.VBox

class TextScreenActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // arg0  URL or text chunk depending on if start with "http"
    // arg1  Title
    // arg2 if "sound" will play TTS on first load
    //

    companion object { const val URL = "" }

    private lateinit var arguments: Array<String>
    private var url = ""
    private var html = ""
    private lateinit var cardText: CardText
    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.shared_tts)
        arguments = intent.getStringArrayExtra(URL)!!
        url = arguments[0]
        title = arguments[1]
        box = VBox.fromResource(this)
        toolbarBottom.menu.findItem(R.id.action_playlist).isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        cardText = CardText(this, box, toolbar, toolbarBottom)
        if (!url.startsWith("http")) {
            if (url.contains("<")) cardText.text = Utility.fromHtml(url) else cardText.text = url
            html = url
        } else {
            getContent()
        }
    }

    override fun onRestart() {
        if (url.startsWith("http")) {
            getContent()
        }
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, ::download, ::update)
    }

    fun download() {
        html = url.getHtml()
    }

    fun update() {
        cardText.setTextAndTranslate(Utility.fromHtml(html))
        UtilityTts.conditionalPlay(arguments, 2, applicationContext, html, "textscreen")
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(html)
        if (audioPlayMenu(item.itemId, html, "txt", "txt")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, arguments[1], textToShare)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
