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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo
import android.widget.ScrollView
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.getImage
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureBytes2
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.NexradUtil
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.ui.CardStormReportItem
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.DatePicker
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityMap
import joshuatee.wx.util.UtilityShare

class SpcStormReportsActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // SPC storm reports. Touch image for data selector
    //
    // Arguments
    // 1: string "yesterday" or "today"
    //

    companion object {
        const val DAY = ""
    }

    private var dayArgument = ""
    private var imgUrl = ""
    private var textUrl = ""
    private var iowaMesoStr = ""
    private val mapState = mutableMapOf<String, Int>()
    private var year = 0
    private var month = 0
    private var day = 0
    private var stateArray = listOf<String>()
    private var firstRun = true
    private var filter = "All"
    private var text = ""
    private val textForShare = StringBuilder(5000)
    private var stormReports = listOf<StormReport>()
    private lateinit var navDrawer: NavDrawer
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private lateinit var image: Image
    private lateinit var boxImage: VBox
    private lateinit var boxText: VBox
    private lateinit var datePicker: DatePicker
    private val helpTitle = " - tap image for date picker"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_show_navdrawer_bottom_toolbar,
            R.menu.spc_stormreports
        )
        val arguments = intent.getStringArrayExtra(DAY)!!
        dayArgument = arguments[0]
        setTitle("() Storm Reports -", dayArgument + helpTitle)
        setupUI()
        initializeData()
        setupNavDrawer()
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        boxImage = VBox(this)
        boxText = VBox(this)
        box.addLayout(boxImage)
        box.addLayout(boxText)
        image = Image(this, UtilityImg.getBlankBitmap())
        boxImage.addWidget(image)
        objectToolbarBottom.connect(this)
        objectToolbarBottom.hide(R.id.action_playlist)
    }

    private fun initializeData() {
        val cal = ObjectDateTime()
        year = cal.getYear()
        month = cal.getMonth()
        day = cal.getDayOfMonth()
        if (dayArgument == "yesterday") {
            cal.addHours(-24)
            day = cal.getDayOfMonth()
        }
    }

    private fun setupNavDrawer() {
        navDrawer = NavDrawer(this, stateArray)
        navDrawer.connect(::navDrawerSelected)
    }

    private fun navDrawerSelected(position: Int) {
        filter = stateArray.getOrNull(position) ?: ""
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        imgUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/climo/reports/$dayArgument.gif"
        textUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/climo/reports/$dayArgument.csv"
        scrollView.smoothScrollTo(0, 0)
        FutureVoid(::download, ::displayData)
        FutureBytes2(::downloadImage, ::updateImage)
    }

    private fun downloadImage(): Bitmap {
        if (firstRun) {
            return imgUrl.getImage()
        }
        return image.bitmap
    }

    private fun updateImage(bitmap: Bitmap) {
        image.set2(bitmap)
        image.connect { datePicker = DatePicker(this, year, month, day, ::updateDisplay) }
    }

    private fun download() {
        if (firstRun) {
            text = textUrl.getHtmlWithNewLine()
        }
    }

    private fun displayData() {
        textForShare.setLength(0)
        val linesOfData = text.split(GlobalVariables.newline).dropLastWhile { it.isEmpty() }
        mapState.clear()
        boxText.removeChildrenAndLayout()
        image.resetZoom()
        val cardText = CardText(this)
        boxText.addWidget(cardText)
        cardText.visibility = View.GONE
        cardText.connect {
            filter = "All"
            displayData()
        }
        stormReports = UtilitySpcStormReports.process(linesOfData)
        var stormCount = 0
        stormReports.forEachIndexed { k, stormReport ->
            val isHeader =
                stormReport.damageHeader == "Tornado Reports" || stormReport.damageHeader == "Wind Reports" || stormReport.damageHeader == "Hail Reports"
            if (filter == "All" || stormReport.state == filter || isHeader) {
                if (stormReport.state != "") {
                    if (mapState.contains(stormReport.state)) {
                        mapState[stormReport.state] = mapState[stormReport.state]!! + 1
                    } else {
                        mapState[stormReport.state] = 1
                    }
                }
                val cardStormReportItem = CardStormReportItem(this, stormReport, k)
                boxText.addWidget(cardStormReportItem)
                if (!isHeader) {
                    cardStormReportItem.registerForContextMenu(this)
                    stormCount += 1
                }
                cardStormReportItem.connect {
                    Route.webView(
                        this,
                        UtilityMap.getUrl(stormReport.lat, stormReport.lon, "10"),
                        "${stormReport.lat},${stormReport.lon}"
                    )
                }
                if (!(stormReport.damageReport.contains("(") && stormReport.damageReport.contains(")"))) {
                    cardStormReportItem.setTextHeader(stormReport)
                    cardStormReportItem.connect { scrollView.smoothScrollTo(0, 0) }
                }
            }
            textForShare.append(stormReport.toString())
        }
        val mapOut = mapState.toString().replace("[{}]".toRegex(), "")
        cardText.text = mapOut
        textForShare.insert(0, GlobalVariables.newline + mapOut + GlobalVariables.newline)
        if (firstRun) {
            stateArray = mapState.keys.sorted().toList()
            val stateArrayLabel = mutableListOf<String>()
            stateArray.indices.forEach {
                stateArrayLabel.add(stateArray[it] + ": " + mapState[stateArray[it]])
            }
            if (stateArrayLabel.size > 0) {
                navDrawer.updateLists(stateArrayLabel)
            }
            firstRun = false
        }
        setTitle("($stormCount) Storm Reports - $filter", dayArgument + helpTitle)
        if (stormCount > 0) {
            cardText.visibility = View.VISIBLE
        } else {
            cardText.visibility = View.GONE
        }
    }

    private fun updateDisplay() {
        year = datePicker.year
        month = datePicker.month
        day = datePicker.day
        val monthStr = To.stringPadLeftZeros(month + 1, 2)
        val dayStr = To.stringPadLeftZeros(day, 2)
        val yearStr = year.toString().substring(2, 4)
        val date = yearStr + monthStr + dayStr
        iowaMesoStr = "20$yearStr$monthStr$dayStr"
        dayArgument = date + "_rpts"
        firstRun = true
        filter = "All"
        getContent()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val index = v.id
        val x = stormReports[index].lat
        val y = stormReports[index].lon
        val radarSite = RadarSites.getNearestRadarSiteCode(LatLon(x, y))
        menu.add(0, v.id, 0, "Show L2REF from $radarSite")
        menu.add(0, v.id, 0, "Show L2VEL from $radarSite")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when {
            item.title!!.contains("Show L2REF") -> radarProdShow(item.itemId, "L2REF")
            item.title!!.contains("Show L2VEL") -> radarProdShow(item.itemId, "L2VEL")
            else -> return false
        }
        return true
    }

    private fun radarProdShow(id: Int, prod: String) {
        var x = stormReports[id].lat
        var y = stormReports[id].lon
        var time = stormReports[id].time
        var radarSite = RadarSites.getNearestRadarSiteCode(LatLon(x, y))
        time = time.take(3)
        if (prod == "TR0" || prod == "TV0") {
            radarSite = NexradUtil.getTdwrFromRid(radarSite)
        }
        if (To.int(stormReports[id].time) < 1000) {
            val monthStr = To.stringPadLeftZeros(month + 1, 2)
            val dayStr = To.stringPadLeftZeros(day + 1, 2)
            val yearStr = year.toString().substring(2, 4)
            iowaMesoStr = "20$yearStr$monthStr$dayStr"
        }
        val patternL2 = radarSite + "_" + iowaMesoStr + "_" + time
        if (!PolygonType.LOCDOT.pref) {
            x = "0.0"
            y = "0.0"
        }
        if (prod == "L2REF" || prod == "L2VEL") {
            Route.radar(this, arrayOf(radarSite, "STATE NOT USED", prod, "", patternL2, x, y))
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        if (audioPlayMenu(item.itemId, textForShare.toString(), "spcstreports", "spcstreports")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.bitmap(
                this,
                "Storm Reports - $dayArgument",
                image,
                textForShare.toString()
            )

            R.id.action_share_text -> UtilityShare.text(
                this,
                "Storm Reports - $dayArgument",
                textForShare.toString()
            )

            R.id.action_share_image -> UtilityShare.bitmap(
                this,
                "Storm Reports - $dayArgument",
                image
            )

            R.id.action_lsrbywfo -> Route.lsrByWfo(this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (navDrawer.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
