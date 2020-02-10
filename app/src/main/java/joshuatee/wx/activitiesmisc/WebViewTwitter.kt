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

package joshuatee.wx.activitiesmisc

import java.util.Locale
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectSpinner

import joshuatee.wx.GlobalArrays
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

import kotlinx.android.synthetic.main.activity_webview_toolbar_state.*

class WebViewTwitter : BaseActivity(), OnItemSelectedListener {

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
    private lateinit var sp: ObjectSpinner
    val prefToken = "STATE_CODE"

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.webscreen_ab_state, menu)
        return true
    }

    @SuppressLint("SetJavaScriptEnabled", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_webview_toolbar_state, null, false)
        title = "Twitter"
        sectorList = GlobalArrays.states + canadianSectors
        sector = Utility.readPref(this, prefToken, Location.state)
        sp = ObjectSpinner(this, this, this, R.id.spinner1, sectorList)
        sp.setSelection(findPosition(sector.toLowerCase(Locale.US)))
        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true
        if (UtilityUI.isTablet()) {
            webSettings.textZoom = (120 * (UIPreferences.normalTextSize.toDouble() / UIPreferences.normalTextSizeDefault.toDouble())).toInt()
        } else {
            webSettings.textZoom = (100 * (UIPreferences.normalTextSize.toDouble() / UIPreferences.normalTextSizeDefault.toDouble())).toInt()
        }
        webview.webViewClient = WebViewClient()
    }

    private fun findPosition(key: String) =
        (0 until sp.size()).firstOrNull { sp[it].toLowerCase(Locale.US).startsWith("$key:") }
            ?: 0

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        sector = sectorList[pos].split(":")[0]
        Utility.writePref(this, prefToken, sector)
        var url = "https://mobile.twitter.com/hashtag/" + sector.toLowerCase(Locale.US)
        if (sector.length == 2) {
            url += "wx"
        }
        webview.loadUrl(url)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_browser -> {
                var tail = "wx"
                var stateTmp = sector.toLowerCase(Locale.US)
                canadianSectors.forEach {
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
