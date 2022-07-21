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

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.widget.AdapterView
import java.util.Locale
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectAlertSummary
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityImg

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
    // TODO refactor var naming
    private val turlLocal = Array(3) { "" }
    private var firstRun = true
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var objectAlertSummary: ObjectAlertSummary
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswarn, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_show_navdrawer, R.menu.uswarn, false)
        scrollView = findViewById(R.id.scrollView)
        linearLayout = findViewById(R.id.linearLayout)
        val activityArguments = intent.getStringArrayExtra(URL)!!
        turlLocal[0] = activityArguments[0]
        turlLocal[1] = activityArguments[1]
        objectAlertSummary = ObjectAlertSummary(this, linearLayout, scrollView)
        objectNavDrawer = ObjectNavDrawer(this, objectAlertSummary.filterArray.toList())
        objectNavDrawer.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            objectNavDrawer.listView.setItemChecked(position, false)
            objectNavDrawer.drawerLayout.closeDrawer(objectNavDrawer.listView)
            if (objectAlertSummary.filterArray[position].length != 2) {
                turlLocal[0] = "^" + objectAlertSummary.filterArray[position]
                turlLocal[1] = "us"
            } else {
                turlLocal[0] = ".*?"
                turlLocal[1] = objectAlertSummary.filterArray[position].lowercase(Locale.US)
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
        FutureVoid(this, ::downloadImage, ::updateImage)
    }

    private fun downloadImage() {
        bitmap = "https://forecast.weather.gov/wwamap/png/US.png".getImage()
    }

    private fun updateImage() {
        objectAlertSummary.updateImage(bitmap)
    }

    private fun downloadText() {
        if (turlLocal[1] == "us" && usDownloaded) {
            html = usDataStr
        } else {
            html = UtilityDownloadNws.getCap(turlLocal[1])
            if (turlLocal[1] == "us") {
                usDataStr = html
                usDownloaded = true
            }
        }
    }

    private fun updateText() {
        objectAlertSummary.updateContent(html, turlLocal[0], firstRun)
        title = objectAlertSummary.getTitle(turlLocal[1])
        if (firstRun) {
            objectNavDrawer.updateLists(objectAlertSummary.navList.toList())
            firstRun = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_warnmap -> ObjectIntent.showImage(this, arrayOf("https://forecast.weather.gov/wwamap/png/US.png", "CONUS warning map"))
            R.id.action_warnmapAK -> ObjectIntent.showImage(this, arrayOf("https://forecast.weather.gov/wwamap/png/ak.png", "AK warning map"))
            R.id.action_warnmapHI -> ObjectIntent.showImage(this, arrayOf("https://forecast.weather.gov/wwamap/png/hi.png", "HI warning map"))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
