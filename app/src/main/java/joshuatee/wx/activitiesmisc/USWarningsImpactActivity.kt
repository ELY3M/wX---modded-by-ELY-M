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
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.util.UtilityTime
import kotlinx.coroutines.*

class USWarningsImpactActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var recyclerView: ObjectRecyclerViewGeneric
    private var warningsList = listOf<ObjectImpactGraphic>()
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_uswarningsimpact_recyclerview,
            null,
            false
        )
        contextg = this
        recyclerView = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        warningsList = withContext(Dispatchers.IO) { UtilityWarningsImpact.data }
        val ca = AdapterUSWarningsImpact(warningsList)
        recyclerView.recyclerView.adapter = ca
        title = warningsList.size.toString() + " NWS warnings active " +
                UtilityTime.gmtTime("HH:mm")
        ca.setOnItemClickListener(object : AdapterUSWarningsImpact.MyClickListener {
            override fun onItemClick(position: Int) {
                ObjectIntent(
                        contextg,
                        ImageShowActivity::class.java,
                        ImageShowActivity.URL,
                        arrayOf(warningsList[position].imgFile, warningsList[position].title)
                )
            }
        })
    }
} 
