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
import android.os.Bundle

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.util.UtilityTime
import kotlinx.coroutines.*

class USWarningsImpactActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var recyclerView: ObjectRecyclerViewGeneric
    private var warningsList = listOf<ObjectImpactGraphic>()
    private var warningsListSorted = listOf<ObjectImpactGraphic>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_toolbar,
            null,
            false
        )
        recyclerView = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        getContent()
    }

    // TODO onrestart

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        warningsList = withContext(Dispatchers.IO) {
            UtilityWarningsImpact.data
        }
        warningsListSorted = warningsList.sortedWith(compareByDescending { it.title })
        val ca = AdapterUSWarningsImpact(warningsListSorted)
        recyclerView.recyclerView.adapter = ca
        title = warningsListSorted.size.toString() + " NWS active Tor/Tst/Ffw warnings "
        toolbar.subtitle = UtilityTime.gmtTime("HH:mm")
        ca.setOnItemClickListener(object: AdapterUSWarningsImpact.MyClickListener {
            override fun onItemClick(position: Int) {
                ObjectIntent(
                        this@USWarningsImpactActivity,
                        ImageShowActivity::class.java,
                        ImageShowActivity.URL,
                        arrayOf(warningsListSorted[position].imageUrl, warningsListSorted[position].title)
                )
            }
        })
    }
} 
