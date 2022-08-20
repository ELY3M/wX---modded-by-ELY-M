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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.UtilityMetar
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility

class NwsObsSitesActivity : BaseActivity() {

    //
    // Used to view NWS website for obs data and provide a link to the map
    // User is presented with a list of states that can be drilled down on
    // Last used is displayed in toolbar
    //

    private val listIds = mutableListOf<String>()
    private var siteDisplay = false
    private var stateSelected = ""
    private lateinit var objectRecyclerView: ObjectRecyclerView
    private val titleString = "Obs sites"
    private val prefToken = "NWS_OBSSITE_LAST_USED"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nwsobssites, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_lastused).title = "Last Used: " + Utility.readPref(this, prefToken, UtilityMetar.findClosestObservation(this, Location.latLon).name)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, R.menu.nwsobssites, bottomToolbar = false)
        title = titleString
        updateButton()
        objectRecyclerView = ObjectRecyclerView(this, R.id.card_list, GlobalArrays.states.toMutableList(), ::itemClicked)
    }

    private fun updateButton() {
        invalidateOptionsMenu()
    }

    private fun itemClicked(position: Int) {
        if (!siteDisplay) {
            stateSelected = GlobalArrays.states[position].take(2)
            title = "$titleString ($stateSelected)"
            stateSelected()
        } else {
            when (position) {
                0 -> {
                    objectRecyclerView.refreshList(GlobalArrays.states.toMutableList())
                    siteDisplay = false
                    title = titleString
                }
                else -> showObsSite(listIds[position])
            }
        }
    }

    private fun showObsSite(obsSite: String) {
        Utility.writePref(prefToken, obsSite)
        updateButton()
        Route.webView(this, arrayOf("https://www.weather.gov/wrh/timeseries?site=$obsSite", obsSite))
    }

    private fun stateSelected() {
        getContent()
    }

    private fun getContent() {
        val text = UtilityIO.readTextFileFromRaw(resources, R.raw.stations_us4)
        val lines = text.split("\n")
        val listCity = mutableListOf<String>()
        listOf(listIds).forEach {
            it.clear()
        }
        listCity.add("..Back to state list")
        listIds.add("..Back to state list")
        val listSort = lines.filter { it.startsWith(stateSelected.uppercase(Locale.US)) }.toMutableList()
        listSort.sort()
        listSort.forEach {
            val items = it.split(",")
            listCity.add(items[2] + ": " + items[1])
            listIds.add(items[2])
        }
        objectRecyclerView.refreshList(listCity)
        siteDisplay = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_lastused -> showObsSite(
                    Utility.readPref(this, prefToken, UtilityMetar.findClosestObservation(this, Location.latLon).name))
            R.id.action_map -> Route.webView(this,
                        arrayOf("https://www.wrh.noaa.gov/map/?obs=true&wfo=" + Location.wfo.lowercase(Locale.US),
                                "Observations near " + Location.wfo))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
