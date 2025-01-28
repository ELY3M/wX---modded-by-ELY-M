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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.util.CurrentConditions
import joshuatee.wx.util.Hazards
import joshuatee.wx.util.SevenDay
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CardCurrentConditions
import joshuatee.wx.ui.CardHazards
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.ui.SevenDayCollection
import joshuatee.wx.ui.VBox

class ForecastActivity : BaseActivity() {

    //
    // long press in radar and select 7 day forecast from arbitrary point
    // arg0  lat
    // arg1  lon
    //

    companion object {
        const val URL = ""
    }

    private var latLon = LatLon()
    private var currentConditions = CurrentConditions()
    private var hazards = Hazards()
    private var sevenDay = SevenDay()
    private lateinit var cardCurrentConditions: CardCurrentConditions
    private lateinit var boxForecast: VBox
    private lateinit var boxHazards: VBox
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private lateinit var sevenDayCollection: SevenDayCollection
    private var locationName = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.adhoc_forecast, menu)
        return true
    }

    //    @SuppressLint("MissingSuperCall")
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val arguments = intent.getStringArrayExtra(URL)!!
        latLon = LatLon(arguments[0], arguments[1])
        locationName = latLon.prettyPrint() + " - " + UtilityLocation.getNearestCity(this, latLon)
        setTitle("Forecast for", locationName)
        setupUI()
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        cardCurrentConditions = CardCurrentConditions(this, 2)
        boxHazards = VBox(this)
        boxForecast = VBox(this)
        sevenDayCollection = SevenDayCollection(this, boxForecast, scrollView)
        with(box) {
            addWidget(cardCurrentConditions)
            addLayout(boxHazards)
            addLayout(boxForecast)
        }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(::downloadCc, ::updateCc)
        FutureVoid(::downloadHazards, ::updateHazards)
        FutureVoid(::download7Day, ::update7Day)
    }

    private fun downloadCc() {
        currentConditions = CurrentConditions(latLon)
        currentConditions.timeCheck()
    }

    private fun updateCc() {
        cardCurrentConditions.update(currentConditions, true)
    }

    private fun downloadHazards() {
        hazards = Hazards(latLon)
    }

    private fun updateHazards() {
        if (hazards.titles.isEmpty()) {
            boxHazards.removeChildrenAndLayout()
            boxHazards.visibility = View.GONE
        } else {
            boxHazards.visibility = View.VISIBLE
            CardHazards(this, boxHazards, hazards)
        }
    }

    private fun download7Day() {
        sevenDay = SevenDay(latLon)
    }

    private fun update7Day() {
        sevenDayCollection.update(sevenDay, latLon, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> FutureVoid.immediate { saveLocation() }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun saveLocation() {
        val message = Location.save(this, latLon, locationName)
        PopupMessage(box.get(), message)
    }
}
