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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import java.util.Locale

import android.os.AsyncTask
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.SingleTextAdapter
import joshuatee.wx.canada.UtilityCanada

import joshuatee.wx.Extensions.*
import joshuatee.wx.util.Utility

class SettingsLocationCanadaActivity : BaseActivity() {

    private var listIds = listOf<String>()
    private var listCity = listOf<String>()
    private var cityDisplay = false
    private var provSelected = ""
    private val provArr = listOf(
            "AB: Alberta",
            "BC: British Columbia",
            "MB: Manitoba",
            "NB: New Brunswick",
            "NL: Newfoundland and Labrador",
            "NS: Nova Scotia",
            "NT: Northwest Territories",
            "NU: Nunavut",
            "ON: Ontario",
            "PE: Prince Edward Island",
            "QC: Quebec",
            "SK: Saskatchewan",
            "YT: Yukon"
    )
    private lateinit var recyclerView: RecyclerView
    private lateinit var ca: SingleTextAdapter

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        title = "Canadian Locations"
        toolbar.subtitle = "Select a location and then use the back arrow to save."
        cityDisplay = false
        recyclerView = findViewById(R.id.card_list)
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        ca = SingleTextAdapter(provArr)
        recyclerView.adapter = ca
        ca.setOnItemClickListener(object : SingleTextAdapter.MyClickListener {
            override fun onItemClick(position: Int) {
                provClicked(position)
            }
        })
    }

    private fun provClicked(position: Int) {
        if (!cityDisplay) {
            provSelected = UtilityStringExternal.truncate(provArr[position], 2)
            title = "Canadian Locations ($provSelected)"
            provSelected()
        } else {
            Utility.writePref(this, "LOCATION_CANADA_PROV", provSelected)
            Utility.writePref(this, "LOCATION_CANADA_CITY", listCity[position])
            Utility.writePref(this, "LOCATION_CANADA_ID", listIds[position])
            finishSave()
        }
    }

    private fun provSelected() {
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun finishSave() {
        val locStr = Utility.readPref(this, "LOCATION_CANADA_PROV", "") + " " +
                Utility.readPref(this, "LOCATION_CANADA_CITY", "") + " " +
                Utility.readPref(this, "LOCATION_CANADA_ID", "")
        toolbar.subtitle = "Selected: $locStr"
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            val html = UtilityCanada.getProvHTML(provSelected)
            listIds = html.parseColumn("<li><a href=\"/city/pages/" + provSelected.toLowerCase(Locale.US) + "-(.*?)_metric_e.html\">.*?</a></li>")
            listCity = html.parseColumn("<li><a href=\"/city/pages/" + provSelected.toLowerCase(Locale.US) + "-.*?_metric_e.html\">(.*?)</a></li>")
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            ca = SingleTextAdapter(listCity.distinct())
            recyclerView.adapter = ca
            cityDisplay = true
        }
    }
} 
