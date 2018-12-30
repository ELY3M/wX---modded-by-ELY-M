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
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.Collections
import java.util.Comparator

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ActionMode
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.Spotter
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMap
import joshuatee.wx.util.UtilityTime
import kotlinx.coroutines.*

class SpottersActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var ca: AdapterSpotter
    private var spotterlist = mutableListOf<Spotter>()
    private var spotterlist2 = mutableListOf<Spotter>()
    private lateinit var recyclerView: ObjectRecyclerViewGeneric
    private var actionMode = ActionMode.RADAR
    private var firstTime = true

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spotters, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = true

            override fun onQueryTextChange(query: String): Boolean {
                val filteredModelList = filter(spotterlist2, query)
                ca.animateTo(filteredModelList)
                recyclerView.scrollToPosition(0)
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
        super.onCreate(savedInstanceState, R.layout.activity_spotters_recyclerview, null, false)
        title = "spotters active"
        toolbar.subtitle = actionMode.getDescription()
        ObjectFab(this, this, R.id.fab_fav, View.OnClickListener { toggleMode(ActionMode.FAV) })
        ObjectFab(this, this, R.id.fab_map, View.OnClickListener { toggleMode(ActionMode.MAP) })
        ObjectFab(this, this, R.id.fab_radar, View.OnClickListener { toggleMode(ActionMode.RADAR) })
        ObjectFab(this, this, R.id.fab_reports, View.OnClickListener { reportFAB() })
        recyclerView = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        getContent()
    }

    private fun toggleMode(am: ActionMode) {
        actionMode = am
        toolbar.subtitle = actionMode.getDescription()
    }

    private fun reportFAB() {
        ObjectIntent(this, SpotterReportsActivity::class.java)
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        spotterlist = withContext(Dispatchers.IO) { UtilitySpotter.spotterData }
        markFavorites()
        ca = AdapterSpotter(spotterlist)
        recyclerView.recyclerView.adapter = ca
        title = spotterlist.size.toString() + " spotters active " + UtilityTime.gmtTime("HH:mm")
        ca.setOnItemClickListener(object : AdapterSpotter.MyClickListener {
            override fun onItemClick(position: Int) {
                itemSelected(position)
            }
        })
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
        val queryLocal = query.toLowerCase()
        val filteredModelList = mutableListOf<Spotter>()
        models.forEach {
            val text = it.lastName.toLowerCase()
            if (text.contains(queryLocal)) {
                filteredModelList.add(it)
            }
        }
        return filteredModelList
    }

    private fun checkFav(posn: Int) {
        if (MyApplication.spotterFav.contains(spotterlist[posn].uniq + ":")) {
            MyApplication.spotterFav =
                    MyApplication.spotterFav.replace(spotterlist[posn].uniq + ":", "")
            spotterlist[posn].lastName = spotterlist[posn].lastName.replace("0FAV ", "")
        } else {
            MyApplication.spotterFav = MyApplication.spotterFav + spotterlist[posn].uniq + ":"
            spotterlist[posn].lastName = "0FAV " + spotterlist[posn].lastName
        }
        sortSpotters()
        ca.notifyDataSetChanged()
        Utility.writePref(this, "SPOTTER_FAV", MyApplication.spotterFav)
    }

    private fun markFavorites() {
        spotterlist
            .filter { MyApplication.spotterFav.contains(it.uniq + ":") && !it.lastName.contains("0FAV ") }
            .forEach { it.lastName = "0FAV " + it.lastName }
        sortSpotters()
    }

    private fun sortSpotters() {
        Collections.sort(spotterlist, Comparator<Spotter> { p1, p2 ->
            val res = p1.lastName.compareTo(p2.lastName, ignoreCase = true)
            if (res != 0)
                return@Comparator res
            p1.firstName.compareTo(p2.firstName, ignoreCase = true)
        })
        if (firstTime) {
            spotterlist2 = mutableListOf()
            spotterlist.indices.forEach { spotterlist2.add(spotterlist[it]) }
            firstTime = false
        }
        Collections.sort(spotterlist2, Comparator<Spotter> { p1, p2 ->
            val res = p1.lastName.compareTo(p2.lastName, ignoreCase = true)
            if (res != 0)
                return@Comparator res
            p1.firstName.compareTo(p2.firstName, ignoreCase = true)
        })
    }

    private fun itemSelected(position: Int) {
        when (actionMode) {
            ActionMode.MAP -> ObjectIntent(
                this,
                WebscreenAB::class.java,
                WebscreenAB.URL,
                arrayOf(
                    UtilityMap.genMapURL(
                        spotterlist[position].lat,
                        spotterlist[position].lon,
                        "9"
                    ), spotterlist[position].lastName + ", " + spotterlist[position].firstName
                )
            )
            ActionMode.FAV -> checkFav(position)
            ActionMode.RADAR -> {
                val rid = UtilityLocation.getNearestOffice(
                    this,
                    "RADAR",
                    LatLon(spotterlist[position].lat, spotterlist[position].lon)
                )
                ObjectIntent(
                    this,
                    WXGLRadarActivity::class.java,
                    WXGLRadarActivity.RID,
                    arrayOf(rid, "", "N0Q", "", spotterlist[position].uniq)
                )
            }
            else -> {
            }
        }
    }
} 
