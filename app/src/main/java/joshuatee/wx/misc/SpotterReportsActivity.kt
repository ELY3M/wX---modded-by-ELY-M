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

import android.os.Bundle
import joshuatee.wx.R
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.objects.OfficeTypeEnum
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.RecyclerViewGeneric

class SpotterReportsActivity : BaseActivity() {

    //
    // Show active spotter reports
    //

    private lateinit var recyclerViewGeneric: RecyclerViewGeneric

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        recyclerViewGeneric = RecyclerViewGeneric(this, R.id.card_list)
        val adapterSpotterReports = AdapterSpotterReports(UtilitySpotter.reports)
        recyclerViewGeneric.adapter = adapterSpotterReports
        updateTitles()
        adapterSpotterReports.setOnItemClickListener(object : AdapterSpotterReports.MyClickListener {
            override fun onItemClick(position: Int) {
                itemClicked(position)
            }
        })
//        adapterSpotterReports.setListener(::itemClicked)
    }

    override fun onRestart() {
        recyclerViewGeneric.adapter = AdapterSpotterReports(UtilitySpotter.reports)
        updateTitles()
        super.onRestart()
    }

    private fun updateTitles() {
        setTitle(UtilitySpotter.reports.size.toString() + " Spotter reports", ObjectDateTime.gmtTime("HH:mm") + " UTC")
    }

    private fun itemClicked(position: Int) {
        val radarSite = UtilityLocation.getNearestOffice(OfficeTypeEnum.RADAR, UtilitySpotter.reports[position].latLon)
        Route.radarWithOneSpotter(this, radarSite, UtilitySpotter.reports[position].uniq)
    }
}
