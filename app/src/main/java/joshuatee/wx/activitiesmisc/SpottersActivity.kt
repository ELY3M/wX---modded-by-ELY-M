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
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.Spotter
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.BottomSheetFragment
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMap
import kotlinx.coroutines.*
import java.util.*

class SpottersActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var ca: AdapterSpotter
    private var spotterList = mutableListOf<Spotter>()
    private var spotterList2 = mutableListOf<Spotter>()
    private lateinit var recyclerView: ObjectRecyclerViewGeneric
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
                if (::ca.isInitialized) {
                    ca.animateTo(filteredModelList)
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
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar_with_onefab, null, false)
        title = titleString
        toolbar.subtitle = "Tap on name for actions."
        ObjectFab(this, this, R.id.fab, R.drawable.ic_info_outline_24dp, View.OnClickListener { reportFAB() })
        recyclerView = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        getContent()
    }

    private fun reportFAB() {
        ObjectIntent(this, SpotterReportsActivity::class.java)
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        spotterList = withContext(Dispatchers.IO) { UtilitySpotter.get(this@SpottersActivity) }.toMutableList()
        markFavorites()
        ca = AdapterSpotter(spotterList)
        recyclerView.recyclerView.adapter = ca
        title = spotterList.size.toString() + " " + titleString
        ca.setListener(::itemClicked)
    }

    private fun changeSearchViewTextColor(view: View?) {
        if (view != null) {
            if (view is TextView) {
                view.setTextColor(Color.WHITE)
            } else if (view is ViewGroup) {
                (0 until view.childCount).forEach { changeSearchViewTextColor(view.getChildAt(it)) }
            }
        }
    }

    private fun filter(models: List<Spotter>, query: String): List<Spotter> {
        val queryLocal = query.toLowerCase(Locale.US)
        val filteredModelList = mutableListOf<Spotter>()
        models.forEach {
            val text = it.lastName.toLowerCase(Locale.US)
            if (text.contains(queryLocal)) {
                filteredModelList.add(it)
            }
        }
        return filteredModelList
    }

    private fun checkFavorite(position: Int) {
        if (MyApplication.spotterFav.contains(spotterList[position].uniq + ":")) {
            MyApplication.spotterFav = MyApplication.spotterFav.replace(spotterList[position].uniq + ":", "")
            spotterList[position].lastName = spotterList[position].lastName.replace("0FAV ", "")
        } else {
            MyApplication.spotterFav = MyApplication.spotterFav + spotterList[position].uniq + ":"
            spotterList[position].lastName = "0FAV " + spotterList[position].lastName
        }
        sortSpotters()
        ca.notifyDataSetChanged()
        Utility.writePref(this, "SPOTTER_FAV", MyApplication.spotterFav)
    }

    private fun markFavorites() {
        spotterList
                .filter { MyApplication.spotterFav.contains(it.uniq + ":") && !it.lastName.contains("0FAV ") }
                .forEach {
                    it.lastName = "0FAV " + it.lastName
                }
        sortSpotters()
    }

    private fun sortSpotters() {
        Collections.sort(spotterList, Comparator<Spotter> { p1, p2 ->
            val res = p1.lastName.compareTo(p2.lastName, ignoreCase = true)
            if (res != 0)
                return@Comparator res
            p1.firstName.compareTo(p2.firstName, ignoreCase = true)
        })
        if (firstTime) {
            spotterList2 = mutableListOf()
            spotterList.indices.forEach { spotterList2.add(spotterList[it]) }
            firstTime = false
        }
        Collections.sort(spotterList2, Comparator<Spotter> { p1, p2 ->
            val res = p1.lastName.compareTo(p2.lastName, ignoreCase = true)
            if (res != 0)
                return@Comparator res
            p1.firstName.compareTo(p2.firstName, ignoreCase = true)
        })
    }

    private fun itemClicked(position: Int) {
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.position = position
        bottomSheetFragment.usedForLocation = false
        bottomSheetFragment.fnList = listOf(::showItemOnRadar, ::showItemOnMap, ::toggleFavorite)
        bottomSheetFragment.labelList = listOf("Show on radar", "Show on map", "Toggle favorite")
        bottomSheetFragment.actContext = this
        bottomSheetFragment.topLabel = ca.getItem(position).toString()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun showItemOnMap(position: Int) {
        ObjectIntent(
                this,
                WebView::class.java,
                WebView.URL,
                arrayOf(
                        UtilityMap.getMapUrl(
                                spotterList[position].lat,
                                spotterList[position].lon,
                                "9"
                        ), spotterList[position].lastName + ", " + spotterList[position].firstName
                )
        )
    }

    private fun showItemOnRadar(position: Int) {
        val radarSite = UtilityLocation.getNearestOffice(
                "RADAR",
                LatLon(spotterList[position].lat, spotterList[position].lon)
        )
        ObjectIntent(
                this,
                WXGLRadarActivity::class.java,
                WXGLRadarActivity.RID,
                arrayOf(radarSite, "", "N0Q", "", spotterList[position].uniq)
        )
    }

    private fun toggleFavorite(position: Int) {
        checkFavorite(position)
    }
} 
