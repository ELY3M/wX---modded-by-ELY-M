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
import joshuatee.wx.R
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.util.UtilityTime

class SpotterReportsActivity : BaseActivity() {

    //
    // Show active spotter reports
    //

    private lateinit var objectRecyclerViewGeneric: ObjectRecyclerViewGeneric

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        objectRecyclerViewGeneric = ObjectRecyclerViewGeneric(this, R.id.card_list)
        val adapterSpotterReports = AdapterSpotterReports(UtilitySpotter.reports)
        objectRecyclerViewGeneric.adapter = adapterSpotterReports
        updateTitles()
        adapterSpotterReports.setOnItemClickListener(object : AdapterSpotterReports.MyClickListener {
            override fun onItemClick(position: Int) { itemSelected(position) }
        })
    }

    override fun onRestart() {
        objectRecyclerViewGeneric.adapter = AdapterSpotterReports(UtilitySpotter.reports)
        updateTitles()
        super.onRestart()
    }

    private fun updateTitles() {
        setTitle(UtilitySpotter.reports.size.toString() + " Spotter reports", UtilityTime.gmtTime("HH:mm") + " UTC")
    }

    private fun itemSelected(position: Int) {
        val radarSite = UtilityLocation.getNearestOffice("RADAR", LatLon(UtilitySpotter.reports[position].lat, UtilitySpotter.reports[position].lon))
        Route.radar(this, arrayOf(radarSite, "", "N0Q", "", UtilitySpotter.reports[position].uniq))
    }
}
