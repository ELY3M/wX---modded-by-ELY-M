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

package joshuatee.wx.spc

import java.util.Calendar
import java.util.Locale
import android.os.Bundle
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo
import android.widget.ScrollView
import joshuatee.wx.Extensions.getHtmlSep
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.*
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class SpcStormReportsActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // SPC storm reports. Touch image for data selector
    //
    // Arguments
    // 1: string "yesterday" or "today"
    //

    companion object { const val DAY = "" }

    private var dayArgument = ""
    private var imgUrl = ""
    private var textUrl = ""
    private var iowaMesoStr = ""
    private val mapState = mutableMapOf<String, Int>()
    private var date = ""
    private var monthStr = ""
    private var dayStr = ""
    private var yearStr = ""
    private var year = 0
    private var month = 0
    private var day = 0
    private var previousYear = 0
    private var previousMonth = 0
    private var previousDay = 0
    private var stateArray = listOf<String>()
    private var firstRun = true
    private var filter = "All"
    private var text = ""
    private val textForShare = StringBuilder(5000)
    private var stormReports = mutableListOf<StormReport>()
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private lateinit var image: Image
    private lateinit var boxImages: VBox
    private lateinit var boxText: VBox
    private lateinit var objectDatePicker: ObjectDatePicker
    private val helpTitle = " - tap image for date picker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_show_navdrawer_bottom_toolbar, R.menu.spc_stormreports)
        val arguments = intent.getStringArrayExtra(DAY)!!
        dayArgument = arguments[0]
        setTitle("() Storm Reports -", dayArgument + helpTitle)
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        boxImages = VBox(this, box.get())
        boxText = VBox(this, box.get())
        image = Image(this, boxImages, UtilityImg.getBlankBitmap())
        objectToolbarBottom.connect(this)
        objectToolbarBottom.hide(R.id.action_playlist)
        val cal = Calendar.getInstance()
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
        if (dayArgument == "yesterday") {
            day -= 1
        }
        previousYear = year
        previousMonth = month
        previousDay = day
        updateIowaMesoData()
        imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/climo/reports/$dayArgument.gif"
        textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/climo/reports/$dayArgument.csv"
        stateArray = listOf("")
        objectNavDrawer = ObjectNavDrawer(this, stateArray)
        objectNavDrawer.connect { _, _, position, _ ->
            objectNavDrawer.setItemChecked(position)
            objectNavDrawer.close()
            filter = stateArray.getOrNull(position) ?: ""
            getContent()
        }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        scrollView.smoothScrollTo(0, 0)
        FutureVoid(this, ::download, ::displayData)
        FutureBytes2(this, ::downloadImage, ::updateImage)
    }

    private fun downloadImage(): Bitmap {
        if (firstRun) {
            return imgUrl.getImage()
        }
        return image.bitmap
    }

    private fun updateImage(bitmap: Bitmap) {
        image.set2(bitmap)
        image.connect { objectDatePicker = ObjectDatePicker(this, year, month, day, ::updateDisplay) }
    }

    private fun download() {
        if (firstRun) {
            text = textUrl.getHtmlSep()
        }
    }

    private fun displayData() {
        textForShare.setLength(0)
        val linesOfData = text.split("<br>").dropLastWhile { it.isEmpty() }
        mapState.clear()
        boxText.removeChildrenAndLayout()
        image.resetZoom()
        val cardText = CardText(this, boxText)
        cardText.visibility = View.GONE
        cardText.connect {
            filter = "All"
            displayData()
        }
        stormReports = UtilitySpcStormReports.process(linesOfData)
        var stormCnt = -3
        stormReports.forEachIndexed { k, stormReport ->
            val isHeader = stormReport.title == "Tornado Reports" || stormReport.title == "Wind Reports" || stormReport.title == "Hail Reports"
            if (filter == "All" || stormReport.state == filter || isHeader ) {
                stormCnt += 1
                if (stormReport.state != "") {
                    val freq3 = mapState[stormReport.state]
                    mapState[stormReport.state] = if (freq3 == null) 1 else freq3 + 1
                }
                val stormCard = ObjectCardStormReportItem(this)
                stormCard.setId(k)
                boxText.addWidget(stormCard.get())
                stormCard.setTextFields(stormReport)
                if (!isHeader) {
                    registerForContextMenu(stormCard.get())
                }
                val xStr = stormReport.lat
                val yStr = stormReport.lon
                stormCard.connect {
                    Route.webView(this, arrayOf(UtilityMap.getUrl(xStr, yStr, "10"), "$xStr,$yStr"))
                }
                if (!(stormReport.description.contains("(") && stormReport.description.contains(")"))) {
                    stormCard.setTextHeader(stormReport)
                    stormCard.connect { scrollView.smoothScrollTo(0, 0) }
                }
            }
        }
        var mapOut = mapState.toString()
        mapOut = mapOut.replace("[{}]".toRegex(), "")
        cardText.text = mapOut
        textForShare.insert(0, Utility.fromHtml("<br><b>" + mapOut + GlobalVariables.newline + "</b><br>"))
        if (firstRun) {
            stateArray = mapState.keys.sorted().toList()
            val stateArrayLabel = mutableListOf<String>()
            stateArray.indices.forEach {
                stateArrayLabel.add(stateArray[it] + ": " + mapState[stateArray[it]])
            }
            if (stateArrayLabel.size > 0) {
                objectNavDrawer.updateLists(stateArrayLabel)
            }
            firstRun = false
        }
        setTitle("($stormCnt) Storm Reports - $filter", dayArgument + helpTitle)
        if (stormCnt > 0) {
            cardText.visibility = View.VISIBLE
        } else {
            cardText.visibility = View.GONE
        }
    }

    private fun updateDisplay() {
        year = objectDatePicker.year
        month = objectDatePicker.month
        day = objectDatePicker.day
        if (previousMonth != month || previousYear != year || previousDay != day) {
            updateIowaMesoData()
            dayArgument = date + "_rpts"
            imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/climo/reports/$dayArgument.gif"
            textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/climo/reports/$dayArgument.csv"
            firstRun = true
            filter = "All"
            getContent()
            previousYear = year
            previousMonth = month
            previousDay = day
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val index = v.id
        val x = stormReports[index].lat
        val y = stormReports[index].lon
        val radarSite = UtilityLocation.getNearestOffice("RADAR", LatLon(x, y))
        menu.add(0, v.id, 0, "Show L2REF from $radarSite")
        menu.add(0, v.id, 0, "Show L2VEL from $radarSite")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when {
            item.title.contains("Show L2REF") -> radarProdShow(item.itemId, "L2REF")
            item.title.contains("Show L2VEL") -> radarProdShow(item.itemId, "L2VEL")
            else -> return false
        }
        return true
    }

    private fun radarProdShow(id: Int, prod: String) {
        var x = stormReports[id].lat
        var y = stormReports[id].lon
        var time = stormReports[id].time
        var radarSite = UtilityLocation.getNearestOffice("RADAR", LatLon(x, y))
        time = time.take(3)
        if (prod == "TR0" || prod == "TV0") {
            radarSite = WXGLNexrad.getTdwrFromRid(radarSite)
        }
        if ((stormReports[id].time.toIntOrNull() ?: 0) < 1000) {
            monthStr = String.format(Locale.US, "%02d", month + 1)
            dayStr = String.format(Locale.US, "%02d", day + 1)
            yearStr = year.toString()
            yearStr = yearStr.substring(2, 4)
            date = yearStr + monthStr + dayStr
            iowaMesoStr = "20$yearStr$monthStr$dayStr"
        }
        val patternL2 = radarSite + "_" + iowaMesoStr + "_" + time
        if (!PolygonType.LOCDOT.pref) {
            x = "0.0"
            y = "0.0"
        }
        if (prod == "L2REF" || prod == "L2VEL") {
            Route.radar(this, arrayOf(radarSite, "", prod, "", patternL2, x, y))
        }
    }

    private fun updateIowaMesoData() {
        monthStr = String.format(Locale.US, "%02d", month + 1)
        dayStr = String.format(Locale.US, "%02d", day)
        yearStr = year.toString()
        yearStr = yearStr.substring(2, 4)
        date = yearStr + monthStr + dayStr
        iowaMesoStr = "20$yearStr$monthStr$dayStr"
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        if (audioPlayMenu(item.itemId, textForShare.toString(), "spcstreports", "spcstreports")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.bitmap(this, "Storm Reports - $dayArgument", image.bitmap, textForShare.toString())
            R.id.action_share_text -> UtilityShare.text(this, "Storm Reports - $dayArgument", textForShare.toString())
            R.id.action_share_image -> UtilityShare.bitmap(this, "Storm Reports - $dayArgument", image.bitmap)
            R.id.action_lsrbywfo -> Route(this, LsrByWfoActivity::class.java, LsrByWfoActivity.URL, arrayOf(Location.wfo, "LSR"))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
