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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.SettingsMainActivity
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardVerticalText
import joshuatee.wx.util.UtilityShare

import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.DefaultLabelFormatter
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

class HourlyActivity : BaseActivity() {

    // This activity is accessible from the action bar and provides hourly forecast for the current location
    // Possible improvements: better text formatting ( possibly color ), proper handling of "nil", graphs
    //

    companion object {
        const val LOC_NUM: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var htmlShare = listOf<String>()
    private lateinit var cv1: ObjectCard
    private lateinit var c0: ObjectCardVerticalText
    private var hourlyData = ObjectHourly()
    private var locatioNumber = 0
    private val menuItemShare: Int = 1
    private val menuItemSettings: Int = 2

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, menuItemShare, Menu.NONE, "Share").setIcon(R.drawable.ic_share_24dp)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, menuItemSettings, Menu.NONE, "Settings")
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_hourly, null, false)
        locatioNumber = (intent.getStringExtra(LOC_NUM).toIntOrNull() ?: 0) - 1
        cv1 = ObjectCard(this, R.color.black, R.id.cv1)
        cv1.setVisibility(View.GONE)
        val linearLayout: LinearLayout = findViewById(R.id.ll)
        c0 = ObjectCardVerticalText(this, 5, linearLayout, toolbar)
        title = "Hourly"
        toolbar.subtitle = Location.getName(locatioNumber)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        val result1 = async(Dispatchers.IO) { UtilityUSHourly.getString(locatioNumber) }
        htmlShare = result1.await()
        val result2 =
            async(Dispatchers.IO) { UtilityUSHourly.getStringForActivity(htmlShare[1]) }
        hourlyData = result2.await()
        cv1.setVisibility(View.VISIBLE)
        c0.setText(
            listOf(
                hourlyData.time,
                hourlyData.temp,
                hourlyData.windSpeed,
                hourlyData.windDir,
                hourlyData.conditions
            )
        )
        plot1()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            menuItemSettings -> ObjectIntent(this, SettingsMainActivity::class.java)
            menuItemShare -> if (htmlShare.size > 1) {
                UtilityShare.shareText(this, "Hourly", htmlShare[1])
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun plot1() {
        val tmpArr2 = hourlyData.temp.split(MyApplication.newline).dropLastWhile { it.isEmpty() }
        val dataPoints = mutableListOf<DataPoint>()
        var time = 0
        (1 until tmpArr2.size - 1).forEach {
            val temp = tmpArr2[it].toIntOrNull() ?: 0
            time += 1
            dataPoints.add(DataPoint(time.toDouble(), temp.toDouble()))
        }
        val graph = findViewById<GraphView>(R.id.graph)
        val series = LineGraphSeries(dataPoints.toTypedArray())
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(160.0)
        graph.gridLabelRenderer.numHorizontalLabels = 10
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // show normal x values
                    if ((value.toInt() % 10) == 0) {
                        super.formatLabel(value, isValueX)
                    } else {
                        ""
                    }
                    //UtilityLog.d("wx", value.toString())
                } else {
                    // show currency for y values
                    super.formatLabel(value, isValueX)
                }
            }
        }
        // enable scaling and scrolling
        //graph.getViewport().setScalable(true)
        //graph.getViewport().setScalableY(true)
        graph.addSeries(series)
    }
}

