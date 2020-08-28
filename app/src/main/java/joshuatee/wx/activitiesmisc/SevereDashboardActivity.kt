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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.*
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.*
import kotlinx.android.synthetic.main.activity_linear_layout.*
import kotlinx.coroutines.*
import joshuatee.wx.R
import joshuatee.wx.util.*

class SevereDashboardActivity : BaseActivity() {

    //
    // Show a variety of graphical and textual data including
    // US Alert map, Storm reports, any active Watch/MPD/MCD
    // Tornado, Tstorm, or FFW warnings
    // All data items can be tapped on for further exploration
    //

    private val uiDispatcher = Dispatchers.Main
    private val bitmaps = mutableListOf<Bitmap>()
    private var watchCount = 0
    private var mcdCount = 0
    private var mpdCount = 0
    private var tstCount = 0
    private var ffwCount = 0
    private var torCount = 0
    private var numberOfImages = 0
    private val horizontalLinearLayouts = mutableListOf<ObjectLinearLayout> ()
    private var imagesPerRow = 2
    private val listOfWfoForWarnings = mutableListOf<String>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        if (UtilityUI.isLandScape(this)) imagesPerRow = 3
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps.clear()
        horizontalLinearLayouts.clear()
        val snWat = SevereNotice(PolygonType.WATCH)
        val snMcd = SevereNotice(PolygonType.MCD)
        val snMpd = SevereNotice(PolygonType.MPD)
        withContext(Dispatchers.IO) {
            bitmaps.add((UtilityDownload.getImageProduct(this@SevereDashboardActivity, "USWARN")))
        }
        withContext(Dispatchers.IO) {
            bitmaps.add((UtilitySpc.getStormReportsTodayUrl()).getImage())
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadWatch.get(this@SevereDashboardActivity)
            snWat.getBitmaps(MyApplication.severeDashboardWat.value)
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadMcd.get(this@SevereDashboardActivity)
            snMcd.getBitmaps(MyApplication.severeDashboardMcd.value)
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadMpd.get(this@SevereDashboardActivity)
            snMpd.getBitmaps(MyApplication.severeDashboardMpd.value)
        }
        val wTor = SevereWarning(PolygonType.TOR)
        val wTst = SevereWarning(PolygonType.TST)
        val wFfw = SevereWarning(PolygonType.FFW)
        withContext(Dispatchers.IO) {
            UtilityDownloadWarnings.getForSevereDashboard(this@SevereDashboardActivity)
            wTor.generateString(MyApplication.severeDashboardTor.value)
            wTst.generateString(MyApplication.severeDashboardTst.value)
            wFfw.generateString(MyApplication.severeDashboardFfw.value)
        }
        linearLayout.removeAllViews()
        numberOfImages = 0
        listOf(0,1).forEach {
            val card: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(this@SevereDashboardActivity, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                card = ObjectCardImage(this@SevereDashboardActivity, objectLinearLayout.linearLayout, bitmaps[it], imagesPerRow)
            } else {
                card = ObjectCardImage(this@SevereDashboardActivity, horizontalLinearLayouts.last().linearLayout, bitmaps[it], imagesPerRow)
            }
            if (it == 0) {
                card.setOnClickListener(View.OnClickListener { ObjectIntent.showUsAlerts(this@SevereDashboardActivity) })
            } else {
                card.setOnClickListener(View.OnClickListener { ObjectIntent.showSpcStormReports(this@SevereDashboardActivity) })
            }
            numberOfImages += 1
        }
        showItems(snWat)
        showItems(snMcd)
        showItems(snMpd)
        bitmaps.addAll(snWat.bitmaps)
        bitmaps.addAll(snMcd.bitmaps)
        bitmaps.addAll(snMpd.bitmaps)
        bitmaps.addAll(bitmaps)
        listOfWfoForWarnings.clear()
        var numberOfWarnings = 0
        listOf(wTor, wTst, wFfw).forEach { severeWarning ->
            if (severeWarning.count > 0) {
                ObjectCardBlackHeaderText(this@SevereDashboardActivity, linearLayout, "(" + severeWarning.count + ") " + severeWarning.getName())
                severeWarning.effectiveList.forEachIndexed { index, _ ->
                    val data = severeWarning.warnings[index]
                    if (!data.startsWith("O.EXP")) {
                        val objectCardDashAlertItem = ObjectCardDashAlertItem(this@SevereDashboardActivity, linearLayout, severeWarning, index)
                        objectCardDashAlertItem.setListener(View.OnClickListener { showWarningDetails(severeWarning.idList[index]) })
                        val id = numberOfWarnings
                        objectCardDashAlertItem.radarButton.setOnClickListener(View.OnClickListener {
                            ObjectIntent.showRadarBySite(this@SevereDashboardActivity, listOfWfoForWarnings.safeGet(id))
                        })
                        objectCardDashAlertItem.detailsButton.setOnClickListener(View.OnClickListener { showWarningDetails(severeWarning.idList[index]) })
                        listOfWfoForWarnings.add(severeWarning.listOfWfo[index])
                        objectCardDashAlertItem.setId(numberOfWarnings)
                        numberOfWarnings += 1
                    }
                }
            }
        }
        tstCount = wTst.count
        ffwCount = wFfw.count
        torCount = wTor.count
        watchCount = snWat.bitmaps.size
        mcdCount = snMcd.bitmaps.size
        mpdCount = snMpd.bitmaps.size
        toolbar.subtitle = getSubTitle()
    }

    private fun getSubTitle(): String {
        var subTitle = ""
        if (watchCount > 0) subTitle += "W($watchCount) "
        if (mcdCount > 0) subTitle += "M($mcdCount) "
        if (mpdCount > 0) subTitle += "P($mpdCount) "
        if (torCount > 0 || tstCount > 0 || ffwCount > 0) subTitle += " ($tstCount,$torCount,$ffwCount)"
        return subTitle
    }

    private fun showItems(sn: SevereNotice) {
        listOf(sn)
                .asSequence()
                .filter { it.bitmaps.size > 0 }
                .forEach { severeNotice ->
                    severeNotice.bitmaps.indices.forEach { j ->
                        val card: ObjectCardImage
                        if (numberOfImages % imagesPerRow == 0) {
                            val objectLinearLayout = ObjectLinearLayout(this@SevereDashboardActivity, linearLayout)
                            objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                            horizontalLinearLayouts.add(objectLinearLayout)
                            card = ObjectCardImage(this@SevereDashboardActivity, objectLinearLayout.linearLayout, severeNotice.bitmaps[j], imagesPerRow)
                        } else {
                            card = ObjectCardImage(this@SevereDashboardActivity, horizontalLinearLayouts.last().linearLayout, severeNotice.bitmaps[j], imagesPerRow
                            )
                        }
                        val number = severeNotice.numbers[j]
                        card.setOnClickListener(View.OnClickListener {
                            ObjectIntent.showMcd(this@SevereDashboardActivity, arrayOf(number, "", severeNotice.toString()))
                        })
                        numberOfImages += 1
                    }
                }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, this, "Severe Dashboard", "", bitmaps)
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SevereDashboard)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showWarningDetails(url: String) {
        ObjectIntent.showHazard(this@SevereDashboardActivity, arrayOf("https://api.weather.gov/alerts/$url", ""))
    }
}
