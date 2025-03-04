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
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.radar.Spotter
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.settings.BottomSheetFragment
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.FabExtended
import joshuatee.wx.ui.RecyclerViewGeneric
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityMap
import java.util.Locale

class SpottersActivity : BaseActivity() {

    //
    // Show active spotters
    // tap on name to open bottom sheet
    // tap on email or phone to respectively email or call the individual
    //

    private lateinit var adapter: AdapterSpotter
    private var spotterList = mutableListOf<Spotter>()
    private var spotterList2 = mutableListOf<Spotter>()
    private lateinit var recyclerView: RecyclerViewGeneric
    private var firstTime = true
    private val titleString = "Spotters active"

    // TODO onrestart

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spotters, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = true

            override fun onQueryTextChange(query: String): Boolean {
                val filteredModelList = filter(spotterList2, query)
                if (::adapter.isInitialized) {
                    adapter.animateTo(filteredModelList)
                    recyclerView.scrollToPosition(0)
                }
                return true
            }
        })
        if (UIPreferences.themeIsWhite) {
            changeSearchViewTextColor(searchView)
        }
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_toolbar_with_onefab_spotters,
            null,
            false
        )
        setTitle(titleString, "Tap on name for actions.")
        FabExtended(
            this,
            R.id.fab,
            R.drawable.ic_info_outline_24dp_white,
            "Spotter Reports"
        ) { goToSpotterReports() }
        recyclerView = RecyclerViewGeneric(this, R.id.card_list)
        UtilityLog.d("spotter", UIPreferences.spotterFav)
        getContent()
    }

    private fun goToSpotterReports() {
        Route(this, SpotterReportsActivity::class.java)
    }

    private fun getContent() {
        FutureVoid({ spotterList = UtilitySpotter.get().toMutableList() }, ::showText)
    }

    private fun showText() {
        markFavorites()
        adapter = AdapterSpotter(spotterList)
        recyclerView.adapter = adapter
        title = spotterList.size.toString() + " " + titleString
        adapter.setListener(::itemClicked)
    }

    private fun changeSearchViewTextColor(view: View?) {
        if (!UtilityUI.isThemeAllWhite()) {
            if (view != null) {
                if (view is TextView) {
                    view.setTextColor(Color.WHITE)
                } else if (view is ViewGroup) {
                    (0 until view.childCount).forEach {
                        changeSearchViewTextColor(view.getChildAt(it))
                    }
                }
            }
        }
    }

    private fun filter(models: List<Spotter>, query: String): List<Spotter> {
        val queryLocal = query.lowercase(Locale.US)
        val filteredModelList = mutableListOf<Spotter>()
        models.forEach {
            val text = it.lastName.lowercase(Locale.US)
            if (text.contains(queryLocal)) {
                filteredModelList.add(it)
            }
        }
        return filteredModelList
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun checkFavorite(position: Int) {
        if (UIPreferences.spotterFav.contains(spotterList[position].unique + ":")) {
            UIPreferences.spotterFav =
                UIPreferences.spotterFav.replace(spotterList[position].unique + ":", "")
            spotterList[position].lastName = spotterList[position].lastName.replace("0FAV ", "")
        } else {
            UIPreferences.spotterFav = UIPreferences.spotterFav + spotterList[position].unique + ":"
            spotterList[position].lastName = "0FAV " + spotterList[position].lastName
        }
        sortSpotters()
        adapter.notifyDataSetChanged()
        Utility.writePref(this, "SPOTTER_FAV", UIPreferences.spotterFav)
    }

    private fun markFavorites() {
        spotterList.filter {
            UIPreferences.spotterFav.contains(it.unique + ":") && !it.lastName.contains(
                "0FAV "
            )
        }.forEach {
            it.lastName = "0FAV " + it.lastName
        }
        sortSpotters()
    }

    private fun sortSpotters() {
        spotterList.sortWith(compareBy({ it.lastName.lowercase() }, { it.firstName.lowercase() }))
        if (firstTime) {
            spotterList2 = spotterList.toMutableList()
            firstTime = false
        }
        spotterList2.sortWith(compareBy({ it.lastName.lowercase() }, { it.firstName.lowercase() }))
    }

    private fun itemClicked(position: Int) {
        val bottomSheetFragment =
            BottomSheetFragment(this, position, adapter.getItem(position).toString(), false)
        with(bottomSheetFragment) {
            functions = listOf(::showItemOnRadar, ::showItemOnMap, ::toggleFavorite)
            labelList = listOf("Show on radar", "Show on map", "Toggle favorite")
            show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun showItemOnMap(position: Int) {
        Route.webView(
            this,
            UtilityMap.getUrl(spotterList[position].latLon, "9"),
            spotterList[position].lastName + ", " + spotterList[position].firstName
        )
    }

    private fun showItemOnRadar(position: Int) {
        val radarSite = RadarSites.getNearestCode(spotterList[position].latLon)
        Route.radarWithOneSpotter(this, radarSite, spotterList[position].unique)
    }

    private fun toggleFavorite(position: Int) {
        checkFavorite(position)
    }
}
