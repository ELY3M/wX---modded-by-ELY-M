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
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import java.util.Locale
import android.view.ContextMenu
import android.view.MenuItem
import android.view.ContextMenu.ContextMenuInfo
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectAlertSummary
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownloadNWS
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.vis.USNWSGOESActivity
import kotlinx.coroutines.*

class USWarningsWithRadarActivity : BaseActivity(), OnMenuItemClickListener {

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
    private lateinit var drw: ObjectNavDrawer
    private lateinit var objAlertSummary: ObjectAlertSummary
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.uswarn, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_uswarnings_with_radar, null, false)
        contextg = this
        toolbar.setOnClickListener { toolbar.showOverflowMenu() }
        val actvityArguments = intent.getStringArrayExtra(URL)
        turlLocal[0] = actvityArguments[0]
        turlLocal[1] = actvityArguments[1]
        linearLayout = findViewById(R.id.ll)
        val scrollView: ScrollView = findViewById(R.id.sv)
        objAlertSummary = ObjectAlertSummary(this, this, linearLayout, scrollView)
        drw = ObjectNavDrawer(this, objAlertSummary.filterArray.toList())
        drw.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drw.listView.setItemChecked(position, false)
            drw.drawerLayout.closeDrawer(drw.listView)
            // FIXME put this in a new method
            if (objAlertSummary.filterArray[position].length != 2) {
                turlLocal[0] = "^" + objAlertSummary.filterArray[position]
                turlLocal[1] = "us"
            } else {
                turlLocal[0] = ".*?"
                turlLocal[1] = objAlertSummary.filterArray[position].toLowerCase(Locale.US)
            }
            getContent()
        }
        getContent()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val zone = objAlertSummary.mapButtonZone[v.id]
        menu.add(0, v.id, 0, "Open radar interface")
        menu.add(0, v.id, 0, "Open radar mosaic")
        menu.add(0, v.id, 0, "Add new location for this warning ($zone)")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when {
            item.title == "Open radar interface" -> radarInterface(item.itemId)
            item.title == "Open radar mosaic" -> radarMosaic(item.itemId)
            (item.title as String).contains("Add new location for this warning") -> locationAdd(item.itemId)
            else -> return false
        }
        return true
    }

    private fun radarInterface(id: Int) {
        val rid = Utility.readPref(contextg, "NWS_RID_" + objAlertSummary.mapButtonNws[id], "")
        ObjectIntent(
            contextg,
            WXGLRadarActivity::class.java,
            WXGLRadarActivity.RID,
            arrayOf(rid, objAlertSummary.mapButtonState[id]!!, "N0Q", "")
        )
    }

    private fun radarMosaic(id: Int) {
        ObjectIntent(
            this,
            USNWSGOESActivity::class.java,
            USNWSGOESActivity.RID,
            arrayOf("nws", objAlertSummary.mapButtonNws[id]!!.toLowerCase(Locale.US), "nws_warn")
        )
    }

    private fun locationAdd(id: Int) {
        saveLocFromZone(id)
    }

    private fun saveLocFromZone(id: Int) = GlobalScope.launch(uiDispatcher) {
        var toastStr = ""
        var coord = listOf<String>()
        // FIXME remove what does not depend on IO
        withContext(Dispatchers.IO) {
            var locNumIntCurrent = Location.numLocations
            locNumIntCurrent += 1
            val locNumToSaveStr = locNumIntCurrent.toString()
            val zone = objAlertSummary.mapButtonZone[id]
            var state = objAlertSummary.mapButtonState[id]
            val county = objAlertSummary.mapButtonCounty[id]
            if (zone!!.length > 3) {
                coord = if (zone.matches("[A-Z][A-Z]C.*?".toRegex())) {
                    UtilityLocation.getXYFromAddressOSM(county + "," + zone.substring(0, 2))
                } else {
                    UtilityDownloadNWS.getLatLonForZone(zone)
                }
                state = zone.substring(0, 2)
            }
            val x = coord[0]
            val y = coord[1]
            toastStr = Location.locationSave(contextg, locNumToSaveStr, x, y, state + "_" + county)
        }

        UtilityUI.makeSnackBar(linearLayout, toastStr)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        // FIXME remove what does not depending on IO
        withContext(Dispatchers.IO) {
            bitmap = "http://forecast.weather.gov/wwamap/png/US.png".getImage()
            try {
                if (turlLocal[1] == "us" && usDownloaded) {
                    html = usDataStr
                } else {
                    html = UtilityDownloadNWS.getCAP(turlLocal[1])
                    if (turlLocal[1] == "us") {
                        usDataStr = html
                        usDownloaded = true
                    }
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
        objAlertSummary.updateContent(bitmap, html, turlLocal[0], firstRun)
        title = objAlertSummary.getTitle(turlLocal[1])
        if (firstRun) {
            drw.updateLists(contextg, objAlertSummary.navList.toList())
            firstRun = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_warnmap -> ObjectIntent(
                contextg,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf("http://forecast.weather.gov/wwamap/png/US.png", "CONUS warning map")
            )
            R.id.action_warnmapAK -> ObjectIntent(
                contextg,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf("http://forecast.weather.gov/wwamap/png/ak.png", "AK warning map")
            )
            R.id.action_warnmapHI -> ObjectIntent(
                contextg,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf("http://forecast.weather.gov/wwamap/png/hi.png", "HI warning map")
            )
            R.id.action_impact_graphics -> ObjectIntent(
                contextg,
                USWarningsImpactActivity::class.java
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
} 

