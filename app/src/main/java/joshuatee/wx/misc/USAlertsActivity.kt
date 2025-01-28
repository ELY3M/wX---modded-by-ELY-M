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
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import java.util.Locale
import android.view.MenuItem
import android.widget.ScrollView
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.AlertSummary
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityDownloadNws

class USAlertsActivity : BaseActivity() {

    //
    // US weather alert interface
    //
    // Arguments
    // 1: warning filter ( ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?" )
    // 2: region ( "us" )
    //

    companion object {
        const val URL = ""
    }

    private var usDownloaded = false
    private var usData = ""
    private var filter = ""
    private var region = ""
    private var firstRun = true
    private lateinit var navDrawer: NavDrawer
    private lateinit var alertSummary: AlertSummary
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswarn, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_show_navdrawer,
            R.menu.uswarn,
            false
        )
        val arguments = intent.getStringArrayExtra(URL)!!
        filter = arguments[0]
        region = arguments[1]
        setupUI()
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        alertSummary = AlertSummary(this, box, scrollView)
        setupNavDrawer()
    }

    private fun setupNavDrawer() {
        navDrawer = NavDrawer(this, alertSummary.filterArray)
        navDrawer.connect(::navDrawerSelected)
        objectToolbar.connectClick { navDrawer.open() }
    }

    private fun navDrawerSelected(position: Int) {
        if (alertSummary.filterArray[position].length != 2) {
            filter = "^" + alertSummary.filterArray[position]
            region = "us"
        } else {
            filter = ".*?"
            region = alertSummary.filterArray[position].lowercase(Locale.US)
        }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureText2(::downloadText, ::updateText)
        FutureBytes("https://forecast.weather.gov/wwamap/png/US.png", alertSummary::updateImage)
    }

    private fun downloadText(): String {
        val html: String
        if (region == "us" && usDownloaded) {
            html = usData
        } else {
            html = UtilityDownloadNws.getCap(region)
            if (region == "us") {
                usData = html
                usDownloaded = true
            }
        }
        return html
    }

    private fun updateText(html: String) {
        alertSummary.updateContent(html, filter, firstRun)
        title = alertSummary.getTitle(region)
        if (firstRun) {
            navDrawer.updateLists(alertSummary.navList)
            firstRun = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_warn_map -> Route.image(
                this,
                "https://forecast.weather.gov/wwamap/png/US.png",
                "CONUS warning map"
            )

            R.id.action_warn_map_AK -> Route.image(
                this,
                "https://forecast.weather.gov/wwamap/png/ak.png",
                "Alaska warning map"
            )

            R.id.action_warn_map_HI -> Route.image(
                this,
                "https://forecast.weather.gov/wwamap/png/hi.png",
                "Hawaii warning map"
            )

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
