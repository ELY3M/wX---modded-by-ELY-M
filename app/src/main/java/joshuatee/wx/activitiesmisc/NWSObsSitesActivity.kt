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
import android.content.Context

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityIO

import joshuatee.wx.GlobalArrays
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility

// FIXME camelcase file name
class NWSObsSitesActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private val listIds = mutableListOf<String>()
    private val listCity = mutableListOf<String>()
    private val listSort = mutableListOf<String>()
    private var siteDisplay = false
    private var provSelected = ""
    private lateinit var recyclerView: ObjectRecyclerView
    private lateinit var contextg: Context
    private val titleString = "Observation sites"
    val prefToken: String = "NWS_OBSSITE_LAST_USED"
    private lateinit var lastUsedMenuItem: MenuItem

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_bottom_toolbar,
            R.menu.nwsobssites,
            true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        title = titleString
        updateButton()
        siteDisplay = false
        recyclerView = ObjectRecyclerView(
            this,
            this,
            R.id.card_list,
            GlobalArrays.states.toMutableList(),
            ::itemClicked
        )
    }

    private fun updateButton() {
        val menu = toolbarBottom.menu
        lastUsedMenuItem = menu.findItem(R.id.action_lastused)
        lastUsedMenuItem.title = "Last Used: " + Utility.readPref(prefToken, "")
    }

    private fun itemClicked(position: Int) {
        if (!siteDisplay) {
            provSelected = UtilityStringExternal.truncate(GlobalArrays.states[position], 2)
            title = "$titleString ($provSelected)"
            stateSelected()
        } else {
            when (position) {
                0 -> {
                    recyclerView.refreshList(GlobalArrays.states.toMutableList())
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
        ObjectIntent(
            contextg,
            WebscreenAB::class.java,
            WebscreenAB.URL,
            arrayOf(
                "http://www.wrh.noaa.gov/mesowest/timeseries.php?sid=$obsSite",
                obsSite
            )
        )
    }

    private fun stateSelected() {
        getContent()
    }

    private fun getContent() {
        val xmlFileInputStream = resources.openRawResource(R.raw.stations_us4)
        val text = UtilityIO.readTextFile(xmlFileInputStream)
        val lines = text.split("\n")
        listOf(listCity, listIds, listSort).forEach { it.clear() }
        listCity.add("..Back to state list")
        listIds.add("..Back to state list")
        lines.filterTo(listSort) { it.startsWith(provSelected.toUpperCase()) }
        listSort.sort()
        listSort.forEach {
            val tmpArr = it.split(",")
            listCity.add(tmpArr[2] + ": " + tmpArr[1])
            listIds.add(tmpArr[2])
        }
        recyclerView.refreshList(listCity)
        siteDisplay = true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_lastused -> showObsSite(Utility.readPref(prefToken, ""))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
} 
