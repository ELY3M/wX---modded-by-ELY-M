/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.util.UtilityLog

class TextScreenActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // arg0  URL or text chunk depending on if start with "http"
    // arg1  Title
    // arg2 if "sound" will play TTS on first load

    companion object {
        const val URL: String = ""
    }

    private lateinit var turl: Array<String>
    private var url = ""
    private var html = ""
    private lateinit var c0: ObjectCardText

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.shared_tts)
        val menu = toolbarBottom.menu
        val playlistMi = menu.findItem(R.id.action_playlist)
        playlistMi.isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        try {
            turl = intent.getStringArrayExtra(URL)
        } catch (e: IllegalStateException) {
            UtilityLog.HandleException(e)
        }
        url = turl[0]
        title = turl[1]
        val linearLayout: LinearLayout = findViewById(R.id.ll)
        c0 = ObjectCardText(this)
        linearLayout.addView(c0.card)
        c0.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
        if (!url.startsWith("http")) {
            if (url.contains("<")) {
                c0.setText(Utility.fromHtml(url))
            } else {
                c0.setText(url)
            }
            html = url
        } else {
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    override fun onRestart() {
        if (url.startsWith("http")) {
            GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
        super.onRestart()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            html = url.getHtml()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            c0.setTextAndTranslate(Utility.fromHtml(html))
            if (turl.size > 2) {
                if (turl[2] == "sound") {
                    UtilityTTS.synthesizeTextAndPlay(applicationContext, html, "textscreen")
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, "txt", "txt")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, turl[1], Utility.fromHtml(html))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}