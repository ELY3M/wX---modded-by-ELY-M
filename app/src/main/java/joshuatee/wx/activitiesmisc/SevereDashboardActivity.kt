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
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.*
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.*
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.util.*

class SevereDashboardActivity : BaseActivity() {

    //
    // Show a variety of graphical and textual data including
    // US Alert map, Storm reports, any active Watch/MPD/MCD
    // Tornado, Tstorm, or FFW warnings
    // All data items can be tapped on for further exploration
    //

    private val bitmaps = mutableListOf<Bitmap>()
    private var watchCount = 0
    private var mcdCount = 0
    private var mpdCount = 0
    private var tstCount = 0
    private var ffwCount = 0
    private var torCount = 0
    private var numberOfImages = 0
    private val horizontalLinearLayouts = mutableListOf<ObjectLinearLayout>()
    private var imagesPerRow = 2
    private lateinit var linearLayout: LinearLayout
    private lateinit var linearLayoutWatches: ObjectLinearLayout
    private lateinit var linearLayoutWarnings: ObjectLinearLayout
    // TODO FIXME use enum
    private val snWat = SevereNotice(PolygonType.WATCH)
    private val snMcd = SevereNotice(PolygonType.MCD)
    private val snMpd = SevereNotice(PolygonType.MPD)
    private val wTor = SevereWarning(PolygonType.TOR)
    private val wTst = SevereWarning(PolygonType.TST)
    private val wFfw = SevereWarning(PolygonType.FFW)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        linearLayout = findViewById(R.id.linearLayout)
        linearLayoutWatches = ObjectLinearLayout(this@SevereDashboardActivity, linearLayout)
        linearLayoutWarnings = ObjectLinearLayout(this@SevereDashboardActivity, linearLayout)
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        bitmaps.clear()
        FutureVoid(this, ::downloadWatch, ::updateWatch)
        FutureVoid(this, ::downloadWarnings, ::updateWarnings)
    }

    private fun downloadWatch() {
        // TODO FIXME use enum
        bitmaps.add((UtilityDownload.getImageProduct(this@SevereDashboardActivity, "USWARN")))
        bitmaps.add((UtilitySpc.getStormReportsTodayUrl()).getImage())
        UtilityDownloadWatch.get(this@SevereDashboardActivity)
        snWat.getBitmaps(MyApplication.severeDashboardWat.value)
        UtilityDownloadMcd.get(this@SevereDashboardActivity)
        snMcd.getBitmaps(MyApplication.severeDashboardMcd.value)
        UtilityDownloadMpd.get(this@SevereDashboardActivity)
        snMpd.getBitmaps(MyApplication.severeDashboardMpd.value)
    }

    private fun downloadWarnings() {
        // TODO FIXME use enum
        UtilityDownloadWarnings.getForSevereDashboard(this@SevereDashboardActivity)
        wTor.generateString()
        wTst.generateString()
        wFfw.generateString()
    }

    private fun updateWatch() {
        linearLayoutWatches.removeAllViews()
        horizontalLinearLayouts.clear()
        numberOfImages = 0
        listOf(0, 1).forEach {
            val card: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(this@SevereDashboardActivity, linearLayoutWatches.linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                card = ObjectCardImage(this@SevereDashboardActivity, objectLinearLayout.get(), bitmaps[it], imagesPerRow)
            } else {
                card = ObjectCardImage(this@SevereDashboardActivity, horizontalLinearLayouts.last().get(), bitmaps[it], imagesPerRow)
            }
            if (it == 0) {
                card.setOnClickListener { ObjectIntent.showUsAlerts(this@SevereDashboardActivity) }
            } else {
                card.setOnClickListener { ObjectIntent.showSpcStormReports(this@SevereDashboardActivity) }
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
        watchCount = snWat.getCount()
        mcdCount = snMcd.getCount()
        mpdCount = snMpd.getCount()
        toolbar.subtitle = getSubTitle()
    }

    private fun updateWarnings() {
        linearLayoutWarnings.removeAllViews()
        var numberOfWarnings = 0
        listOf(wTor, wTst, wFfw).forEach { severeWarning ->
            if (severeWarning.getCount() > 0) {
                ObjectCardBlackHeaderText(this@SevereDashboardActivity, linearLayoutWarnings.get(), "(" + severeWarning.getCount() + ") " + severeWarning.getName())
                severeWarning.warningList.forEach { w ->
                    if (w.isCurrent) {
                        val objectCardDashAlertItem = ObjectCardDashAlertItem(this@SevereDashboardActivity, linearLayoutWarnings.get(), w)
                        objectCardDashAlertItem.setListener { showWarningDetails(w.url) }
                        objectCardDashAlertItem.radarButton.setOnClickListener {
                            ObjectIntent.showRadarBySite(this@SevereDashboardActivity, w.getClosestRadar())
                        }
                        objectCardDashAlertItem.detailsButton.setOnClickListener { showWarningDetails(w.url) }
                        objectCardDashAlertItem.setId(numberOfWarnings)
                        numberOfWarnings += 1
                    }
                }
            }
        }
        tstCount = wTst.getCount()
        ffwCount = wFfw.getCount()
        torCount = wTor.getCount()
        toolbar.subtitle = getSubTitle()
    }

    private fun getSubTitle(): String {
        var subTitle = ""
        if (watchCount > 0) {
            subTitle += "W($watchCount) "
        }
        if (mcdCount > 0) {
            subTitle += "M($mcdCount) "
        }
        if (mpdCount > 0) {
            subTitle += "P($mpdCount) "
        }
        if (torCount > 0 || tstCount > 0 || ffwCount > 0) {
            subTitle += " ($tstCount,$torCount,$ffwCount)"
        }
        return subTitle
    }

    private fun showItems(sn: SevereNotice) {
        sn.bitmaps.indices.forEach { j ->
            val card: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(this@SevereDashboardActivity, linearLayoutWatches.get())
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                card = ObjectCardImage(this@SevereDashboardActivity, objectLinearLayout.linearLayout, sn.bitmaps[j], imagesPerRow)
            } else {
                card = ObjectCardImage(this@SevereDashboardActivity, horizontalLinearLayouts.last().get(), sn.bitmaps[j], imagesPerRow)
            }
            val number = sn.numbers[j]
            card.setOnClickListener {
                ObjectIntent.showMcd(this@SevereDashboardActivity, arrayOf(number, "", sn.toString()))
            }
            numberOfImages += 1
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
        ObjectIntent.showHazard(this@SevereDashboardActivity, arrayOf(url, ""))
    }
}
