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

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityIO

import joshuatee.wx.STATE_ARR
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectRecyclerView

class NWSObsSitesActivity : BaseActivity() {

    private val listIds = mutableListOf<String>()
    private val listCity = mutableListOf<String>()
    private val listSort = mutableListOf<String>()
    private var siteDisplay = false
    private var provSelected = ""
    private lateinit var recyclerView: ObjectRecyclerView
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        contextg = this
        siteDisplay = false
        recyclerView = ObjectRecyclerView(
            this,
            this,
            R.id.card_list,
            STATE_ARR.toMutableList(),
            ::itemClicked
        )
    }

    private fun itemClicked(position: Int) {
        if (!siteDisplay) {
            provSelected = UtilityStringExternal.truncate(STATE_ARR[position], 2)
            title = "Observation sites ($provSelected)"
            provSelected()
        } else {
            when (position) {
                0 -> {
                    recyclerView.refreshList(STATE_ARR.toMutableList())
                    siteDisplay = false
                }
                else -> ObjectIntent(
                    contextg,
                    WebscreenAB::class.java,
                    WebscreenAB.URL,
                    arrayOf(
                        "http://www.wrh.noaa.gov/mesowest/timeseries.php?sid=" + listIds[position],
                        listCity[position]
                    )
                )
            }
        }
    }

    private fun provSelected() {
        getContent()
    }

    private fun getContent() {
        val xmlFileInputStream = resources.openRawResource(R.raw.stations_us4)
        val text = UtilityIO.readTextFile(xmlFileInputStream)
        val lines = text.split("\n")
        listCity.clear()
        listIds.clear()
        listSort.clear()
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
} 
