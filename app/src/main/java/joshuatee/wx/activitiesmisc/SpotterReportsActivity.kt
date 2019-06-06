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
import android.os.Bundle

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.radar.LatLon
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.util.UtilityTime

class SpotterReportsActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_toolbar,
            null,
            false
        )
        val objectRecyclerViewGeneric = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        val adapterSpotterReports = AdapterSpotterReports(UtilitySpotter.spotterReports)
        objectRecyclerViewGeneric.recyclerView.adapter = adapterSpotterReports
        title = UtilitySpotter.spotterReports.size.toString() + " Spotter reports " + UtilityTime.gmtTime("HH:mm")
        adapterSpotterReports.setOnItemClickListener(object : AdapterSpotterReports.MyClickListener {
            override fun onItemClick(position: Int) {
                itemSelected(position)
            }
        })
    }

    private fun itemSelected(position: Int) {
        val radarSite = UtilityLocation.getNearestOffice(
            this,
            "RADAR",
            LatLon(
                UtilitySpotter.spotterReports[position].lat,
                UtilitySpotter.spotterReports[position].lon
            )
        )
        ObjectIntent(
            this,
            WXGLRadarActivity::class.java,
            WXGLRadarActivity.RID,
            arrayOf(radarSite, "", "N0Q", "", UtilitySpotter.spotterReports[position].uniq)
        )
    }
} 
