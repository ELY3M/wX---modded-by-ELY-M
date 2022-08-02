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

package joshuatee.wx.settings

import java.util.Locale
import android.os.Bundle
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.ObjectRecyclerView
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
    private lateinit var objectRecyclerView: ObjectRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        title = "Canadian Locations"
        toolbar.subtitle = "Select a location and then use the back arrow to save."
        cityDisplay = false
        objectRecyclerView = ObjectRecyclerView(this, R.id.card_list, provArr.toMutableList(), ::provinceClicked)
    }

    private fun provinceClicked(position: Int) {
        if (!cityDisplay) {
            provSelected = provArr[position].take(2)
            title = "Canadian Locations ($provSelected)"
            getContent()
        } else {
            Utility.writePref(this, "LOCATION_CANADA_PROV", provSelected)
            Utility.writePref(this, "LOCATION_CANADA_CITY", listCity[position])
            Utility.writePref(this, "LOCATION_CANADA_ID", listIds[position])
            finishSave()
        }
    }

    private fun finishSave() {
        val locStr = Utility.readPref(this, "LOCATION_CANADA_PROV", "") + " " +
                Utility.readPref(this, "LOCATION_CANADA_CITY", "") + " " +
                Utility.readPref(this, "LOCATION_CANADA_ID", "")
        toolbar.subtitle = "Selected: $locStr"
        finish()
    }

    private fun getContent() {
        FutureVoid(this, ::download, ::update)
    }

    private fun download() {
        val html = UtilityCanada.getProvidenceHtml(provSelected)
        listIds = html.parseColumn("<li><a href=\"/city/pages/" + provSelected.lowercase(Locale.US) + "-(.*?)_metric_e.html\">.*?</a></li>")
        listCity = html.parseColumn("<li><a href=\"/city/pages/" + provSelected.lowercase(Locale.US) + "-.*?_metric_e.html\">(.*?)</a></li>")
    }

    private fun update() {
        objectRecyclerView.refreshList(listCity.distinct().toMutableList())
        cityDisplay = true
    }
}
