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
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.CardVerticalText2
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.externalGraphView.series.DataPoint
import joshuatee.wx.externalGraphView.series.LineGraphSeries
import joshuatee.wx.externalGraphView.DefaultLabelFormatter
import joshuatee.wx.externalGraphView.GraphView
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To

class HourlyActivity : BaseActivity() {

    //
    // This activity is accessible from the action bar and provides hourly forecast for the current location
    //
    // arg0 location number ( "1" being first saved location )
    //

    companion object {
        const val LOC_NUM = ""
    }

    private var htmlShare = listOf<String>()
    private lateinit var card: Card
    private lateinit var cardVerticalText: CardVerticalText2
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private lateinit var graphCard: Card
    private lateinit var graph: GraphView
    private var hourly = Hourly()
    private var locationNumber = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hourly_top, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_hourly,
            R.menu.shared_multigraphics,
            false
        )
        locationNumber = To.int(intent.getStringExtra(LOC_NUM)!!) - 1
        setTitle("Hourly Forecast", Location.getName(locationNumber))
        setupUI()
        getContent()
    }

    private fun setupUI() {
        card = Card(this, R.color.black, R.id.graphCard)
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        graphCard = Card(this, R.id.graphCard)
        graphCard.setCardBackgroundColor(Color.BLACK)
        graph = findViewById(R.id.graph)
        graphCard.visibility = View.GONE
        cardVerticalText = CardVerticalText2(this, toolbar)
        box.addWidget(cardVerticalText)
        cardVerticalText.connect { scrollView.scrollTo(0, 0) }
        val padding = 100
        graph.layoutParams.width = UtilityUI.screenWidthAbs() - padding
        graph.layoutParams.height = ((UtilityUI.screenWidthAbs() - padding) * 0.4).toInt()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid({ htmlShare = UtilityHourly.get(locationNumber) }, ::update)
    }

    private fun update() {
        hourly = if (UIPreferences.useNwsApiForHourly) {
            UtilityHourly.getStringForActivity(htmlShare[1])
        } else {
            UtilityHourlyOldApi.getStringForActivity(htmlShare[1])
        }
        cardVerticalText.set(hourly.data)
        plotData()
        if (UIPreferences.hourlyShowGraph) {
            graphCard.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> if (htmlShare.size > 1) UtilityShare.text(
                this,
                "Hourly",
                htmlShare[1]
            )

            R.id.action_settings -> Route.settings(this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun plotData() {
        val linesOfData = hourly.temp.split(GlobalVariables.newline).dropLastWhile { it.isEmpty() }
        val dataPoints = mutableListOf<DataPoint>()
        (1 until linesOfData.lastIndex).forEach {
            val temp = To.int(linesOfData[it])
            dataPoints.add(DataPoint(it.toDouble(), temp.toDouble()))
        }
        val series = LineGraphSeries(dataPoints.toTypedArray())
        series.color = Color.BLACK
        with(graph) {
            removeAllSeries()
            viewport.isXAxisBoundsManual = true
            viewport.backgroundColor = Color.LTGRAY
            viewport.setMinX(0.0)
            viewport.setMaxX(160.0)
            gridLabelRenderer.numHorizontalLabels = 10
            gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX) {
                        // show normal x values
                        if ((value.toInt() % 10) == 0) {
                            super.formatLabel(value, true)
                        } else {
                            ""
                        }
                    } else {
                        // show currency for y values
                        super.formatLabel(value, false)
                    }
                }
            }
            addSeries(series)
        }
    }
}
