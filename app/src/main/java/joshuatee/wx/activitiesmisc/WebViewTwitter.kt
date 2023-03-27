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

import java.util.Locale
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
//import androidx.activity.addCallback
import joshuatee.wx.R
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

class WebViewTwitter : BaseActivity() {

    //
    // WebView for twitter weather tags
    //

    private val canadianSectors = listOf(
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
    private var sectorList = listOf<String>()
    private var sector = ""
    val prefToken = "STATE_CODE"
    private lateinit var webView: WebView

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.webscreen_ab_state, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = sector
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_webview_toolbar_state, null, false)
        webView = findViewById(R.id.webView)
        title = "Twitter"
        sectorList = GlobalArrays.states + canadianSectors
        sector = Utility.readPref(this, prefToken, Location.state)
        val webSettings = webView.settings
        with (webSettings) {
            javaScriptEnabled = true
            textZoom = if (UtilityUI.isTablet()) {
                (120 * (UIPreferences.normalTextSize.toDouble() / UIPreferences.normalTextSizeDefault.toDouble())).toInt()
            } else {
                (100 * (UIPreferences.normalTextSize.toDouble() / UIPreferences.normalTextSizeDefault.toDouble())).toInt()
            }
        }
        webView.webViewClient = WebViewClient()
        getContent()

//        onBackPressedDispatcher.addCallback(this) {
//            if (webView.canGoBack()) {
//                webView.goBack()
//            }
//        }
    }

    fun getContent(index: Int) {
        invalidateOptionsMenu()
        sector = sectorList[index].split(":")[0]
        getContent()
    }

    fun getContent() {
        invalidateOptionsMenu()
        Utility.writePref(this, prefToken, sector)
        var url = "https://mobile.twitter.com/hashtag/" + sector.lowercase(Locale.US)
        if (sector.length == 2) {
            url += "wx"
        }
        webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sector -> ObjectDialogue.generic(this, sectorList, {}) { getContent(it) }
            R.id.action_browser -> {
                var tail = "wx"
                var stateTmp = sector.lowercase(Locale.US)
                canadianSectors.forEach {
                    if (it.contains("$stateTmp:")) {
                        tail = ""
                        stateTmp = stateTmp.replace("wx", "")
                    }
                }
                Route(this, Intent.ACTION_VIEW, Uri.parse("http://twitter.com/hashtag/$stateTmp$tail"))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
