/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019 joshua.tee@gmail.com

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
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardVerticalText
import joshuatee.wx.util.UtilityShare

import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import joshuatee.wx.UIPreferences
import kotlinx.coroutines.*

class HourlyActivity : BaseActivity() {

    //
    // This activity is accessible from the action bar and provides hourly forecast for the current location
    //
    // arg0 location number ( "1" being first saved location )
    //

    companion object { const val LOC_NUM = "" }

    private val uiDispatcher = Dispatchers.Main
    private var htmlShare = listOf<String>()
    private lateinit var objectCard: ObjectCard
    private lateinit var objectCardVerticalText: ObjectCardVerticalText
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private lateinit var graphCard: CardView
    private var hourlyData = ObjectHourly()
    private var locationNumber = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hourly_top, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_hourly, R.menu.shared_multigraphics, false)
        scrollView = findViewById(R.id.scrollView)
        linearLayout = findViewById(R.id.linearLayout)
        graphCard = findViewById(R.id.graphCard)
        locationNumber = (intent.getStringExtra(LOC_NUM)!!.toIntOrNull() ?: 0) - 1
        objectCard = ObjectCard(this, R.color.black, R.id.graphCard)
        graphCard.visibility = View.GONE
        objectCardVerticalText = ObjectCardVerticalText(this, 5, linearLayout, toolbar)
        objectCardVerticalText.setOnClickListener { scrollView.scrollTo(0, 0) }
        title = "Hourly Forecast"
        toolbar.subtitle = Location.getName(locationNumber)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        htmlShare = withContext(Dispatchers.IO) {
            UtilityUSHourly.get(locationNumber)
        }
        hourlyData = if (UIPreferences.useNwsApiForHourly) {
            UtilityUSHourly.getStringForActivity(htmlShare[1])
        } else {
            UtilityUSHourly.getStringForActivityFromOldApi(htmlShare[1])
        }
        graphCard.visibility = View.VISIBLE
        objectCardVerticalText.setText(listOf(hourlyData.time, hourlyData.temp, hourlyData.windSpeed, hourlyData.windDir, hourlyData.conditions))
        plotData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> if (htmlShare.size > 1) UtilityShare.text(this, "Hourly", htmlShare[1])
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun plotData() {
        val linesOfData = hourlyData.temp.split(MyApplication.newline).dropLastWhile { it.isEmpty() }
        val dataPoints = mutableListOf<DataPoint>()
        (1 until linesOfData.lastIndex).forEach {
            val temp = linesOfData[it].toIntOrNull() ?: 0
            dataPoints.add(DataPoint(it.toDouble(), temp.toDouble()))
        }
        val series = LineGraphSeries(dataPoints.toTypedArray())
        series.color = Color.BLACK
        val graph: GraphView = findViewById(R.id.graph)
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.backgroundColor = Color.LTGRAY
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(160.0)
        graph.gridLabelRenderer.numHorizontalLabels = 10
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // show normal x values
                    if ((value.toInt() % 10) == 0) super.formatLabel(value, isValueX) else ""
                } else {
                    // show currency for y values
                    super.formatLabel(value, isValueX)
                }
            }
        }
        graph.addSeries(series)
    }
}

