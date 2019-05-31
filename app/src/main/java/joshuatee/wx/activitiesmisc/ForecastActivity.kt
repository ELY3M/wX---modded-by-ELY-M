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
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.View
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

import kotlinx.android.synthetic.main.activity_linear_layout.*

class ForecastActivity : BaseActivity() {

    // long press in nexrad radar and select 7 day forecast from arbitrary point
    // arg0  lat
    // arg1  lon

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var latLon = LatLon()
    private var objCc = ObjectForecastPackageCurrentConditions()
    private var objHazards = ObjectForecastPackageHazards()
    private var objSevenDay = ObjectForecastPackage7Day()
    private var ccTime = ""
    private var radarTime = ""
    private lateinit var cardCC: ObjectCardCC
    private lateinit var linearLayoutForecast: ObjectLinearLayout
    private lateinit var linearLayoutHazards: ObjectLinearLayout
    private val hazardCards = mutableListOf<ObjectCardText>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        // FIXME activity_linear_layout need ll to be renamed to linearLayout, need to asses which activities are using it
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        activityArguments = intent.getStringArrayExtra(URL)
        latLon = LatLon(activityArguments[0], activityArguments[1])
        title = "Forecast for"
        toolbar.subtitle = latLon.latString + "," + latLon.lonString
        cardCC = ObjectCardCC(this, 2)
        ll.addView(cardCC.card)
        linearLayoutHazards = ObjectLinearLayout(this, ll)
        linearLayoutForecast = ObjectLinearLayout(this, ll)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var bitmapForCurrentCondition: Bitmap? = null
        val bitmaps = mutableListOf<Bitmap>()

        withContext(Dispatchers.IO) {
            //
            // Current conditions
            //
            objCc = ObjectForecastPackageCurrentConditions(this@ForecastActivity, latLon)
            objHazards = ObjectForecastPackageHazards(latLon)
            objSevenDay = ObjectForecastPackage7Day(latLon)
            bitmapForCurrentCondition = UtilityNws.getIcon(this@ForecastActivity, objCc.iconUrl)
            //
            // 7day
            //
            objSevenDay.icons.mapTo(bitmaps) { UtilityNws.getIcon(this@ForecastActivity, it) }
            //
            // hazards
            //
            //hazardRaw = objHazards!!.hazards
        }
        //
        // CC
        //
        cardCC.let {
            ccTime = objCc.status
            if (bitmapForCurrentCondition != null) {
                it.updateContent(bitmapForCurrentCondition!!, objCc, true, ccTime, radarTime)
            }
        }
        //
        // 7day
        //
        linearLayoutForecast.removeAllViewsInLayout()
        bitmaps.forEachIndexed { index, bitmap ->
            val c7day = ObjectCard7Day(this@ForecastActivity, bitmap, true, index, objSevenDay.forecastList)
            c7day.setOnClickListener(View.OnClickListener {
                sv.smoothScrollTo(0, 0)
            })
            linearLayoutForecast.addView(c7day.card)
        }
        // sunrise card
        val cardSunrise = ObjectCardText(this@ForecastActivity)
        cardSunrise.center()
        try {
            cardSunrise.setText(
                    UtilityTimeSunMoon.getSunriseSunset(
                            this@ForecastActivity,
                            Location.currentLocationStr
                    ) + MyApplication.newline + UtilityTime.gmtTime()
            )
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        linearLayoutForecast.addView(cardSunrise.card)

        //
        // hazards
        //
        if (objHazards.titles.isEmpty()) {
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
        objHazards.titles.indices.forEach { z ->
            hazardCards.add(ObjectCardText(this@ForecastActivity))
            hazardCards[z].setPaddingAmount(MyApplication.paddingSettings)
            hazardCards[z].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
            hazardCards[z].setTextColor(UIPreferences.textHighlightColor)
            hazardCards[z].setText(objHazards.titles[z].toUpperCase(Locale.US))
            hazardCards[z].setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        this@ForecastActivity,
                        USAlertsDetailActivity::class.java,
                        USAlertsDetailActivity.URL,
                        arrayOf(objHazards.urls[z])
                )
            })
            linearLayoutHazards.addView(hazardCards[z].card)
        }
    }
}
