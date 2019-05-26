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
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardVerticalText
import joshuatee.wx.util.UtilityShare

import kotlinx.android.synthetic.main.activity_hourly.*

import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.DefaultLabelFormatter
import kotlinx.coroutines.*

class HourlyActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    // This activity is accessible from the action bar and provides hourly forecast for the current location
    // Possible improvements: better text formatting ( possibly color ), proper handling of "nil", graphs
    //

    companion object {
        const val LOC_NUM: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var htmlShare = listOf<String>()
    private lateinit var card: ObjectCard
    private lateinit var textCard: ObjectCardVerticalText
    private var hourlyData = ObjectHourly()
    private var locationNumber = 0

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_hourly,
            R.menu.shared_multigraphics,
            true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        locationNumber = (intent.getStringExtra(LOC_NUM).toIntOrNull() ?: 0) - 1
        card = ObjectCard(this, R.color.black, R.id.cv1)
        cv1.visibility = View.GONE
        textCard = ObjectCardVerticalText(this, 5, linearLayout, toolbar)
        textCard.setOnClickListener(View.OnClickListener { sv.scrollTo(0,0)})
        title = "Hourly Forecast"
        toolbar.subtitle = Location.getName(locationNumber)
        //UtilityLog.d("wx", UtilityTimeSunMoon.getSunTimesForHomescreen())
        //UtilityLog.d("wx", UtilityTimeSunMoon.getMoonTimesForHomescreen())
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        val result1 = async(Dispatchers.IO) { UtilityUSHourly.getString(locationNumber) }
        htmlShare = result1.await()
        val result2 = async(Dispatchers.IO) { UtilityUSHourly.getStringForActivity(htmlShare[1]) }
        hourlyData = result2.await()
        cv1.visibility = View.VISIBLE
        textCard.setText(
            listOf(
                hourlyData.time,
                hourlyData.temp,
                hourlyData.windSpeed,
                hourlyData.windDir,
                hourlyData.conditions
            )
        )
        plotData()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> if (htmlShare.size > 1) {
                UtilityShare.shareText(this, "Hourly", htmlShare[1])
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun plotData() {
        val linesOfData = hourlyData.temp.split(MyApplication.newline).dropLastWhile { it.isEmpty() }
        val dataPoints = mutableListOf<DataPoint>()
        var time = 0
        (1 until linesOfData.size - 1).forEach {
            val temp = linesOfData[it].toIntOrNull() ?: 0
            time += 1
            dataPoints.add(DataPoint(time.toDouble(), temp.toDouble()))
        }
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
                } else {
                    // show currency for y values
                    super.formatLabel(value, isValueX)
                }
            }
        }
        graph.addSeries(series)
    }
}

