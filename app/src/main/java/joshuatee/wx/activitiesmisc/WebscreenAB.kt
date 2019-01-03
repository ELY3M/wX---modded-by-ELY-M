/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity

class WebscreenAB : BaseActivity() {

    // This is a general purpose activity used to view web pages.
    // Toolbar is displayed ( thus AB ie ActionBar (old name) in activity name )
    // arg0 URL
    // arg1 Title

    companion object {
        const val URL: String = ""
    }

    // added to support webview history, this var and block below it
    private lateinit var webview: WebView
    private var url = ""

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
        super.onCreate(savedInstanceState, R.layout.activity_webview_toolbar, null, false)
        val activityArguments = intent.getStringArrayExtra(URL)
        url = activityArguments[0]
        title = activityArguments[1]
        webview = findViewById(R.id.webview)
        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true
        webview.webViewClient = WebViewClient()
        if (url.startsWith("http")) {
            webview.loadUrl(url)
        } else {
            if (url.contains("twitter"))
                webview.loadDataWithBaseURL("fake://not/needed", url, "text/html", "utf-8", null)
            else
                webview.loadData(url, "text/html", null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_browser -> {
            var urlExt = url
            if (url.contains("https://twitter.com/search?q=%23tornado")) {
                urlExt = "https://twitter.com/hashtag/tornado"
            }
            if (url.contains("https://twitter.com/wunderground")) {
                urlExt = "https://twitter.com/wunderground"
            }
            ObjectIntent(this, Intent.ACTION_VIEW, Uri.parse(urlExt))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
