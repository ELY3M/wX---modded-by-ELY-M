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
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.fragments.UtilityLocationFragment
import joshuatee.wx.fragments.UtilityNWS
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import java.util.*

import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.LatLon
import kotlinx.coroutines.*

class AdhocForecastActivity : BaseActivity() {

    // long press in nexrad radar and select 7 day forecast from arbitrary point
    // arg0  lat
    // arg1  lon

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var latlon = LatLon()
    private var objFcst: ObjectForecastPackage? = null
    private var objHazards: ObjectForecastPackageHazards? = null
    private var objSevenDay: ObjectForecastPackage7Day? = null
    private lateinit var scrollView: ScrollView
    private var ccTime = ""
    private var radarTime = ""
    private var hazardsSum = ""
    private var hazardRaw = ""
    private lateinit var cardCC: ObjectCardCC
    private var bitmapSize = 300
    private lateinit var linearLayoutForecast: LinearLayout
    private lateinit var linearLayoutHazards: LinearLayout
    private lateinit var linearLayout: LinearLayout
    private val hazardsCardAl = mutableListOf<ObjectCardText>()
    private val hazardsExpandedAl = mutableListOf<Boolean>()
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        activityArguments = intent.getStringArrayExtra(URL)
        latlon = LatLon(activityArguments[0], activityArguments[1])
        title = "Forecast for"
        toolbar.subtitle = latlon.latString + "," + latlon.lonString
        scrollView = findViewById(R.id.sv)
        linearLayout = findViewById(R.id.ll)
        cardCC = ObjectCardCC(this, 2)
        linearLayout.addView(cardCC.card)
        linearLayoutHazards = LinearLayout(this)
        linearLayoutHazards.orientation = LinearLayout.VERTICAL
        linearLayout.addView(linearLayoutHazards)
        linearLayoutForecast = LinearLayout(this)
        linearLayoutForecast.orientation = LinearLayout.VERTICAL
        linearLayout.addView(linearLayoutForecast)
        contextg = this
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var bmCc: Bitmap? = null
        val bmArr = mutableListOf<Bitmap>()

        withContext(Dispatchers.IO) {
            //
            // CC
            //
            objFcst = Utility.getCurrentConditionsV2byLatLon(contextg, latlon)
            objHazards = Utility.getCurrentHazards(latlon)
            objSevenDay = Utility.getCurrentSevenDay(latlon)
            hazardRaw = objHazards!!.hazards.getHtmlSep()
            bmCc = UtilityNWS.getIcon(contextg, objFcst!!.objCC.iconUrl)
            //
            // 7day
            //
            objSevenDay!!.iconAl.mapTo(bmArr) { UtilityNWS.getIcon(contextg, it) }
            //
            // hazards
            //
            hazardRaw = objHazards!!.hazards
        }
        //
        // CC
        //
        bitmapSize = UtilityLocationFragment.setNWSIconSize()
        objFcst?.let { _ ->
            cardCC.let {
                ccTime = objFcst!!.objCC.status
                if (bmCc != null) {
                    it.updateContent(bmCc!!, bitmapSize, objFcst!!, true, ccTime, radarTime)
                }
            }
        }
        //
        // 7day
        //
        objFcst?.let {
            linearLayoutForecast.removeAllViewsInLayout()
            val day7Arr = objSevenDay!!.fcstList
            bmArr.forEachIndexed { idx, bm ->
                val c7day = ObjectCard7Day(contextg, bm, true, idx, day7Arr)
                c7day.setOnClickListener(View.OnClickListener {
                    scrollView.smoothScrollTo(0, 0)
                })
                linearLayoutForecast.addView(c7day.card)
            }
            // sunrise card
            val cardSunrise = ObjectCardText(contextg)
            cardSunrise.center()
            cardSunrise.lightText()
            try {
                cardSunrise.setText(
                    UtilityDownload.getSunriseSunset(
                        contextg,
                        Location.currentLocationStr
                    ) + MyApplication.newline + UtilityTime.gmtTime()
                )
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            linearLayoutForecast.addView(cardSunrise.card)
        }

        //
        // hazards
        //
        var hazardSumAsync = ""
        val idAl = hazardRaw.parseColumn("\"@id\": \"(.*?)\"")
        val hazardTitles = hazardRaw.parseColumn("\"event\": \"(.*?)\"")
        hazardTitles.forEach { hazardSumAsync += it + MyApplication.newline }
        if (hazardSumAsync == "") {
            linearLayoutHazards.removeAllViews()
            linearLayoutHazards.visibility = View.GONE
        } else {
            linearLayoutHazards.visibility = View.VISIBLE
            setupHazardCards(hazardSumAsync, idAl)
        }
        hazardsSum = hazardSumAsync
    }

    private fun setupHazardCards(hazStr: String, idAl: List<String>) {
        linearLayoutHazards.removeAllViews()
        hazardsExpandedAl.clear()
        hazardsCardAl.clear()
        val tmpArr = hazStr.split(MyApplication.newline).dropLastWhile { it.isEmpty() }
        tmpArr.indices.forEach { z ->
            hazardsExpandedAl.add(false)
            hazardsCardAl.add(ObjectCardText(contextg))
            hazardsCardAl[z].setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
            hazardsCardAl[z].setTextColor(UIPreferences.textHighlightColor)
            hazardsCardAl[z].setText(tmpArr[z].toUpperCase(Locale.US))
            val url = idAl[z]
            hazardsCardAl[z].setOnClickListener(View.OnClickListener {
                ObjectIntent(
                    contextg,
                    USAlertsDetailActivity::class.java,
                    USAlertsDetailActivity.URL,
                    arrayOf(url)
                )
            })
            linearLayoutHazards.addView(hazardsCardAl[z].card)
        }
    }
}
