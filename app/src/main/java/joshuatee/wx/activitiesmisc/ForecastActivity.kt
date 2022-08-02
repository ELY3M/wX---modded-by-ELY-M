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

//import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.Location
import joshuatee.wx.util.ObjectCurrentConditions
import joshuatee.wx.util.ObjectHazards
import joshuatee.wx.util.ObjectSevenDay
import joshuatee.wx.util.UtilityTimeSunMoon
import joshuatee.wx.util.UtilityTime
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.LatLon
import joshuatee.wx.ui.*

class ForecastActivity : BaseActivity() {

    //
    // long press in radar and select 7 day forecast from arbitrary point
    // arg0  lat
    // arg1  lon
    //

    companion object { const val URL = "" }

    private var latLon = LatLon()
    private var objectCurrentConditions = ObjectCurrentConditions()
    private var objectHazards = ObjectHazards()
    private var objectSevenDay = ObjectSevenDay()
    private lateinit var objectCardCurrentConditions: ObjectCardCurrentConditions
    private lateinit var boxForecast: VBox
    private lateinit var boxHazards: VBox
    private val hazardCards = mutableListOf<CardText>()
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.adhoc_forecast, menu)
        return true
    }

//    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val arguments = intent.getStringArrayExtra(URL)!!
        latLon = LatLon(arguments[0], arguments[1])
        setTitle("Forecast for", latLon.latString + "," + latLon.lonString)
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        objectCardCurrentConditions = ObjectCardCurrentConditions(this, 2)
        box.addWidget(objectCardCurrentConditions.get())
        boxHazards = VBox(this, box.get())
        boxForecast = VBox(this, box.get())
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, ::downloadCc, ::updateCc)
        FutureVoid(this, ::downloadHazards, ::updateHazards)
        FutureVoid(this, ::download7Day, ::update7Day)
    }

    private fun downloadCc() {
        objectCurrentConditions = ObjectCurrentConditions(this, latLon)
        objectCurrentConditions.timeCheck(this)
    }

    private fun updateCc() {
        objectCardCurrentConditions.update(objectCurrentConditions, true)
    }

    private fun downloadHazards() {
        objectHazards = ObjectHazards(latLon)
    }

    private fun updateHazards() {
        if (objectHazards.titles.isEmpty()) {
            boxHazards.removeChildrenAndLayout()
            boxHazards.visibility = View.GONE
        } else {
            boxHazards.visibility = View.VISIBLE
            setupHazardCards()
        }
    }

    private fun download7Day() {
        objectSevenDay = ObjectSevenDay(latLon)
    }

    private fun update7Day() {
        boxForecast.removeChildrenAndLayout()
        objectSevenDay.icons.forEachIndexed { index, iconUrl ->
            val objectCard7Day = SevenDayCard(this, iconUrl, true, index, objectSevenDay.forecastList)
            objectCard7Day.connect { scrollView.smoothScrollTo(0, 0) }
            boxForecast.addWidget(objectCard7Day.get())
        }
        val sunriseCard = CardText(this)
        sunriseCard.center()
        sunriseCard.text = UtilityTimeSunMoon.getSunriseSunset(this, Location.currentLocationStr, false) + GlobalVariables.newline + UtilityTime.gmtTime()
        boxForecast.addWidget(sunriseCard.get())
    }

    private fun setupHazardCards() {
        boxHazards.removeChildrenAndLayout()
        hazardCards.clear()
        objectHazards.titles.indices.forEach { z ->
            hazardCards.add(CardText(this))
            hazardCards[z].setupHazard()
            hazardCards[z].text = objectHazards.titles[z].uppercase(Locale.US)
            hazardCards[z].connect { Route.hazard(this, arrayOf(objectHazards.urls[z])) }
            boxHazards.addWidget(hazardCards[z].get())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> FutureVoid.immediate { saveLocation() }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun saveLocation() {
        val message = Location.save(this, latLon)
        ObjectPopupMessage(box.get(), message)
    }
}
