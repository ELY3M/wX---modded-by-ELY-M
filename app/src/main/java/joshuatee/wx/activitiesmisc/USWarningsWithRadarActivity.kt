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

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import java.util.Locale
import android.view.MenuItem
import android.widget.ScrollView
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectAlertSummary
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityDownloadNws

class USWarningsWithRadarActivity : BaseActivity() {

    //
    // US weather alert interface
    //
    // Arguments
    // 1: warning filter ( ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?" )
    // 2: region ( "us" )
    //

    companion object { const val URL = "" }

    private var html = ""
    private var usDownloaded = false
    private var usDataStr = ""
    private var filter = ""
    private var region = ""
    private var firstRun = true
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var objectAlertSummary: ObjectAlertSummary
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswarn, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_show_navdrawer, R.menu.uswarn, false)
        val arguments = intent.getStringArrayExtra(URL)!!
        filter = arguments[0]
        region = arguments[1]
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        objectAlertSummary = ObjectAlertSummary(this, box, scrollView)
        objectNavDrawer = ObjectNavDrawer(this, objectAlertSummary.filterArray.toList())
        objectNavDrawer.connect { _, _, position, _ ->
            objectNavDrawer.setItemChecked(position)
            objectNavDrawer.close()
            if (objectAlertSummary.filterArray[position].length != 2) {
                filter = "^" + objectAlertSummary.filterArray[position]
                region = "us"
            } else {
                filter = ".*?"
                region = objectAlertSummary.filterArray[position].lowercase(Locale.US)
            }
            getContent()
        }
        toolbar.setOnClickListener { objectNavDrawer.open() }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, ::downloadText, ::updateText)
        FutureBytes(this, "https://forecast.weather.gov/wwamap/png/US.png", objectAlertSummary::updateImage)
    }

    private fun downloadText() {
        if (region == "us" && usDownloaded) {
            html = usDataStr
        } else {
            html = UtilityDownloadNws.getCap(region)
            if (region == "us") {
                usDataStr = html
                usDownloaded = true
            }
        }
    }

    private fun updateText() {
        objectAlertSummary.updateContent(html, filter, firstRun)
        title = objectAlertSummary.getTitle(region)
        if (firstRun) {
            objectNavDrawer.updateLists(objectAlertSummary.navList.toList())
            firstRun = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_warnmap -> Route.image(this, "https://forecast.weather.gov/wwamap/png/US.png", "CONUS warning map")
            R.id.action_warnmapAK -> Route.image(this, "https://forecast.weather.gov/wwamap/png/ak.png", "Alaska warning map")
            R.id.action_warnmapHI -> Route.image(this, "https://forecast.weather.gov/wwamap/png/hi.png", "Hawaii warning map")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
