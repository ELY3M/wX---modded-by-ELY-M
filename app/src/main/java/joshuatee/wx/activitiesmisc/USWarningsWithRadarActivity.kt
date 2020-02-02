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

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import java.util.Locale
import android.view.ContextMenu
import android.view.MenuItem
import android.view.ContextMenu.ContextMenuInfo
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.GlobalDictionaries

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectAlertSummary
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityImg
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_show_navdrawer_bottom_toolbar.*

// FIXME rename USWarningsWithRadarActivity
class USWarningsWithRadarActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    // US weather alert interface
    //
    // Arguments
    // 1: warning filter ( ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?" )
    // 2: region ( "us" )
    //

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var html = ""
    private var usDownloaded = false
    private var usDataStr = ""
    private val turlLocal = Array(3) { "" }
    private var firstRun = true
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var objectAlertSummary: ObjectAlertSummary

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_show_navdrawer_bottom_toolbar,
                R.menu.uswarn,
                true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        toolbar.setOnClickListener { toolbar.showOverflowMenu() }
        val activityArguments = intent.getStringArrayExtra(URL)
        turlLocal[0] = activityArguments!![0]
        turlLocal[1] = activityArguments[1]
        objectAlertSummary = ObjectAlertSummary(this, this, linearLayout, scrollView)
        objectNavDrawer = ObjectNavDrawer(this, objectAlertSummary.filterArray.toList())
        objectNavDrawer.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            objectNavDrawer.listView.setItemChecked(position, false)
            objectNavDrawer.drawerLayout.closeDrawer(objectNavDrawer.listView)
            if (objectAlertSummary.filterArray[position].length != 2) {
                turlLocal[0] = "^" + objectAlertSummary.filterArray[position]
                turlLocal[1] = "us"
            } else {
                turlLocal[0] = ".*?"
                turlLocal[1] = objectAlertSummary.filterArray[position].toLowerCase(Locale.US)
            }
            getContent()
        }
        toolbarBottom.setOnClickListener { objectNavDrawer.drawerLayout.openDrawer(objectNavDrawer.listView) }
        getContent()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val zone = objectAlertSummary.mapButtonZone[v.id]
        menu.add(0, v.id, 0, "Open radar interface")
        menu.add(0, v.id, 0, "Add new location for this warning ($zone)")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when {
            item.title == "Open radar interface" -> radarInterface(item.itemId)
            (item.title as String).contains("Add new location for this warning") -> locationAdd(item.itemId)
            else -> return false
        }
        return true
    }

    private fun radarInterface(id: Int) {
        val radarSite = GlobalDictionaries.wfoToRadarSite[objectAlertSummary.mapButtonNws[id]] ?: ""
        ObjectIntent(
                this@USWarningsWithRadarActivity,
                WXGLRadarActivity::class.java,
                WXGLRadarActivity.RID,
                arrayOf(radarSite, objectAlertSummary.mapButtonState[id]!!, "N0Q", "")
        )
    }

    private fun locationAdd(id: Int) {
        saveLocFromZone(id)
    }

    private fun saveLocFromZone(id: Int) = GlobalScope.launch(uiDispatcher) {
        var toastStr = ""
        var coord = listOf<String>()
        withContext(Dispatchers.IO) {
            var locNumIntCurrent = Location.numLocations
            locNumIntCurrent += 1
            val locNumToSaveStr = locNumIntCurrent.toString()
            val zone = objectAlertSummary.mapButtonZone[id]
            var state = objectAlertSummary.mapButtonState[id]
            val county = objectAlertSummary.mapButtonCounty[id]
            if (zone!!.length > 3) {
                coord = if (zone.matches("[A-Z][A-Z]C.*?".toRegex())) {
                    UtilityLocation.getXYFromAddressOsm(county + "," + zone.substring(0, 2))
                } else {
                    UtilityDownloadNws.getLatLonForZone(zone)
                }
                state = zone.substring(0, 2)
            }
            val x = coord[0]
            val y = coord[1]
            toastStr = Location.locationSave(this@USWarningsWithRadarActivity, locNumToSaveStr, x, y, state + "_" + county)
        }
        UtilityUI.makeSnackBar(linearLayout, toastStr)
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            bitmap = "https://forecast.weather.gov/wwamap/png/US.png".getImage()
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
        objectAlertSummary.updateContent(bitmap, html, turlLocal[0], firstRun)
        title = objectAlertSummary.getTitle(turlLocal[1])
        if (firstRun) {
            objectNavDrawer.updateLists(this@USWarningsWithRadarActivity, objectAlertSummary.navList.toList())
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawer.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_warnmap -> ObjectIntent(
                    this@USWarningsWithRadarActivity,
                    ImageShowActivity::class.java,
                    ImageShowActivity.URL,
                    arrayOf("https://forecast.weather.gov/wwamap/png/US.png", "CONUS warning map")
            )
            R.id.action_warnmapAK -> ObjectIntent(
                    this@USWarningsWithRadarActivity,
                    ImageShowActivity::class.java,
                    ImageShowActivity.URL,
                    arrayOf("https://forecast.weather.gov/wwamap/png/ak.png", "AK warning map")
            )
            R.id.action_warnmapHI -> ObjectIntent(
                    this@USWarningsWithRadarActivity,
                    ImageShowActivity::class.java,
                    ImageShowActivity.URL,
                    arrayOf("https://forecast.weather.gov/wwamap/png/hi.png", "HI warning map")
            )
            R.id.action_impact_graphics -> ObjectIntent(
                    this@USWarningsWithRadarActivity,
                    USWarningsImpactActivity::class.java
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
} 

