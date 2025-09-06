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

package joshuatee.wx.misc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.UtilityUI
import androidx.core.net.toUri

class WebView : BaseActivity() {

    //
    // This is a general purpose activity used to view web pages.
    // Toolbar is displayed ( thus AB ie ActionBar (old name) in activity name )
    // arg0 URL
    // arg1 Title
    //

    companion object {
        const val URL = ""
    }

    private var url = ""
    private lateinit var webView: WebView

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.webscreen_ab_state, menu)
        return true
    }

    @SuppressLint("SetJavaScriptEnabled", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_webview_toolbar, null, false)
        val arguments = intent.getStringArrayExtra(URL)!!
        url = arguments[0]
        title = arguments[1]
        webView = findViewById(R.id.webView)
        val webSettings = webView.settings
        with(webSettings) {
            javaScriptEnabled = true
            if (arguments.size > 2) {
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            textZoom = if (UtilityUI.isTablet()) {
                (120 * (UIPreferences.normalTextSize.toDouble() / UIPreferences.normalTextSizeDefault.toDouble())).toInt()
            } else {
                (100 * (UIPreferences.normalTextSize.toDouble() / UIPreferences.normalTextSizeDefault.toDouble())).toInt()
            }
        }
        webView.webViewClient = WebViewClient()
        if (url.startsWith("http")) {
            webView.loadUrl(url)
        } else {
            webView.loadData(url, "text/html", null)
        }

        // https://developer.android.com/guide/navigation/navigation-custom-back
        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_browser -> {
            Route(this, Intent.ACTION_VIEW, url.toUri())
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

//    override fun onBackPressed() {
//        if (webView.canGoBack()) {
//            webView.goBack()
//        } else {
//            super.onBackPressed()
//        }
//    }
}
