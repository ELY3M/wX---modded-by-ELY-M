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

import java.util.Locale
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectSpinner

import joshuatee.wx.STATE_ARR
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.util.Utility

class WebscreenABState : BaseActivity(), OnItemSelectedListener {

    // This is a general purpose activity used to view web pages.
    // Toolbar is displayed ( thus AB ie ActionBar (old name) in activity name )
    // URL and title are passed in via extras

    private lateinit var webview: WebView
    private var url = ""
    private val caArr = listOf(
        "bcstorm: British Columbia",
        "abstorm: Alberta",
        "skstorm: Saskatchewan",
        "mbstorm: Manitoba",
        "onstorm: Ontario",
        "meteoqc: Quebec",
        "nbstorm: New Brunswick",
        "pestorm: Prince Edward Island",
        "nsstorm: Nova Scotia",
        "ntstorm: North West Territories",
        "nlwx: Newfoundland"
    )
    private var stateArr = listOf<String>()
    private var stateCodeCurrent = ""
    private var twitterStateId = ""
    private lateinit var sp: ObjectSpinner

    override fun onBackPressed() {
        if (webview.canGoBack())
            webview.goBack()
        else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.webscreen_ab_state, menu)
        return true
    }

    @SuppressLint("SetJavaScriptEnabled", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_webview_toolbar_state, null, false)
        title = "twitter"
        stateArr = STATE_ARR + caArr
        stateCodeCurrent = Utility.readPref(this, "STATE_CODE", "")
        twitterStateId = Utility.readPref(this, "STATE_TW_ID_$stateCodeCurrent", "")
        url = "<a class=\"twitter-timeline\" data-dnt=\"true\" href=\"https://twitter.com/search?q=%23" +
                stateCodeCurrent.toLowerCase(Locale.US) + "wx\" data-widget-id=\"" +
                twitterStateId +
                "\" data-chrome=\"noscrollbar noheader nofooter noborders  \" data-tweet-limit=20>Tweets about \"#" +
                stateCodeCurrent.toLowerCase(Locale.US) +
                "wx\"</a><script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+\"://platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script>"
        sp = ObjectSpinner(this, this, R.id.spinner1, stateArr)
        sp.setOnItemSelectedListener(this)
        sp.setSelection(findPosition(stateCodeCurrent.toLowerCase(Locale.US)))
        webview = findViewById(R.id.webview)
        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true
        webview.webViewClient = WebViewClient()
    }

    private fun findPosition(key: String) =
        (0 until sp.size()).firstOrNull { sp[it].toLowerCase(Locale.US).startsWith("$key:") }
            ?: 0

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        Utility.writePref(this, "STATE_CODE", MyApplication.colon.split(stateArr[pos])[0])
        stateCodeCurrent = Utility.readPref(this, "STATE_CODE", "")
        twitterStateId = Utility.readPref(this, "STATE_TW_ID_$stateCodeCurrent", "")
        url = "<a class=\"twitter-timeline\" data-dnt=\"true\" href=\"https://twitter.com/search?q=%23" +
                stateCodeCurrent.toLowerCase(Locale.US) + "wx\" data-widget-id=\"" +
                twitterStateId +
                "\" data-chrome=\"noscrollbar noheader nofooter noborders  \" data-tweet-limit=20>Tweets about \"#" +
                stateCodeCurrent.toLowerCase(Locale.US) +
                "wx\"</a><script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+\"://platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script>"
        webview.loadDataWithBaseURL("fake://not/needed", url, "text/html", "utf-8", null)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_browser -> {
                var tail = "wx"
                var stateTmp = stateCodeCurrent.toLowerCase(Locale.US)
                caArr.forEach {
                    if (it.contains("$stateTmp:")) {
                        tail = ""
                        stateTmp = stateTmp.replace("wx", "")
                    }
                }
                ObjectIntent(
                    this,
                    Intent.ACTION_VIEW,
                    Uri.parse("http://twitter.com/hashtag/$stateTmp$tail")
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
