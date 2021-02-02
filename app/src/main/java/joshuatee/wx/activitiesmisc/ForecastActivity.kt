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
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.fragments.UtilityNws
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import java.util.*

import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.LatLon
import kotlinx.coroutines.*

class ForecastActivity : BaseActivity() {

    //
    // long press in radar and select 7 day forecast from arbitrary point
    // arg0  lat
    // arg1  lon
    //

    companion object { const val URL = "" }

    private val uiDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var latLon = LatLon()
    private var objectCurrentConditions = ObjectCurrentConditions()
    private var objectHazards = ObjectHazards()
    private var objectSevenDay = ObjectSevenDay()
    private var currentConditionsTime = ""
    private var radarTime = ""
    private lateinit var objectCardCurrentConditions: ObjectCardCurrentConditions
    private lateinit var linearLayoutForecast: ObjectLinearLayout
    private lateinit var linearLayoutHazards: ObjectLinearLayout
    private val hazardCards = mutableListOf<ObjectCardText>()
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.adhoc_forecast, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        scrollView = findViewById(R.id.scrollView)
        linearLayout = findViewById(R.id.linearLayout)
        activityArguments = intent.getStringArrayExtra(URL)!!
        latLon = LatLon(activityArguments[0], activityArguments[1])
        title = "Forecast for"
        toolbar.subtitle = latLon.latString + "," + latLon.lonString
        objectCardCurrentConditions = ObjectCardCurrentConditions(this, 2)
        linearLayout.addView(objectCardCurrentConditions.card)
        linearLayoutHazards = ObjectLinearLayout(this, linearLayout)
        linearLayoutForecast = ObjectLinearLayout(this, linearLayout)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var bitmapForCurrentCondition: Bitmap?
        var bitmaps = listOf<Bitmap>()
        withContext(Dispatchers.IO) {
            //
            // Current conditions
            //
            objectCurrentConditions = ObjectCurrentConditions(this@ForecastActivity, latLon)
            objectHazards = ObjectHazards(latLon)
            objectSevenDay = ObjectSevenDay(latLon)
            bitmapForCurrentCondition = UtilityNws.getIcon(this@ForecastActivity, objectCurrentConditions.iconUrl)
            //
            // 7day
            //
            bitmaps = objectSevenDay.icons.map { UtilityNws.getIcon(this@ForecastActivity, it) }
        }
        //
        // CC
        //
        objectCardCurrentConditions.let {
            currentConditionsTime = objectCurrentConditions.status
            if (bitmapForCurrentCondition != null) {
                it.updateContent(bitmapForCurrentCondition!!, objectCurrentConditions, true, currentConditionsTime, radarTime)
            }
        }
        //
        // 7day
        //
        linearLayoutForecast.removeAllViewsInLayout()
        bitmaps.forEachIndexed { index, bitmap ->
            val objectCard7Day = ObjectCard7Day(this@ForecastActivity, bitmap, true, index, objectSevenDay.forecastList)
            objectCard7Day.setOnClickListener { scrollView.smoothScrollTo(0, 0) }
            linearLayoutForecast.addView(objectCard7Day.card)
        }
        // sunrise card
        val objectCardText = ObjectCardText(this@ForecastActivity)
        objectCardText.center()
        try {
            objectCardText.text = (UtilityTimeSunMoon.getSunriseSunset(this@ForecastActivity, Location.currentLocationStr, false) + MyApplication.newline + UtilityTime.gmtTime())
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        linearLayoutForecast.addView(objectCardText.card)
        //
        // hazards
        //
        if (objectHazards.titles.isEmpty()) {
            linearLayoutHazards.removeAllViews()
            linearLayoutHazards.visibility = View.GONE
        } else {
            linearLayoutHazards.visibility = View.VISIBLE
            setupHazardCards()
        }
    }

    private fun setupHazardCards() {
        linearLayoutHazards.removeAllViews()
        hazardCards.clear()
        objectHazards.titles.indices.forEach { z ->
            hazardCards.add(ObjectCardText(this@ForecastActivity))
            hazardCards[z].setPaddingAmount(MyApplication.paddingSettings)
            hazardCards[z].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
            hazardCards[z].setTextColor(UIPreferences.textHighlightColor)
            hazardCards[z].text = objectHazards.titles[z].toUpperCase(Locale.US)
            hazardCards[z].setOnClickListener { ObjectIntent.showHazard(this@ForecastActivity, arrayOf(objectHazards.urls[z])) }
            linearLayoutHazards.addView(hazardCards[z].card)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> saveLocation()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun saveLocation() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            val message = Location.save(this@ForecastActivity, latLon)
            UtilityUI.makeSnackBar(linearLayout, message)
        }
    }
}
