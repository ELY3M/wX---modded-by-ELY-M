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

package joshuatee.wx.activitiesmisc

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.*
import joshuatee.wx.R
import joshuatee.wx.objects.*
import joshuatee.wx.util.*

class SevereDashboardActivity : BaseActivity() {

    //
    // Show a variety of graphical and textual data including
    // US Alert map, Storm reports, any active Watch/MPD/MCD
    // Tornado, Tstorm, or FFW warnings
    // All data items can be tapped on for further exploration
    //

    private var bitmaps = mutableListOf<Bitmap>()
    private val numbers = mutableListOf<String>()
    private val types = mutableListOf<String>()
    private val boxRows = mutableListOf<HBox>()
    private var imagesPerRow = 2
    private lateinit var box: VBox
    private lateinit var boxImages: VBox
    private lateinit var boxWarnings: VBox
    private var downloadTimer = DownloadTimer("SEVERE_DASHBOARD_ACTIVITY")
    private val watchesByType = mapOf(
            PolygonType.WATCH to SevereNotice(PolygonType.WATCH),
            PolygonType.MCD to SevereNotice(PolygonType.MCD),
            PolygonType.MPD to SevereNotice(PolygonType.MPD),
    )
    private val warningsByType = mapOf(
            PolygonWarningType.TornadoWarning to SevereWarning(PolygonWarningType.TornadoWarning),
            PolygonWarningType.ThunderstormWarning to SevereWarning(PolygonWarningType.ThunderstormWarning),
            PolygonWarningType.FlashFloodWarning to SevereWarning(PolygonWarningType.FlashFloodWarning),
    )

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        box = VBox.fromResource(this)
        boxImages = VBox(this, box.get())
        boxWarnings = VBox(this, box.get())
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
        if (downloadTimer.isRefreshNeeded(this)) {
            bitmaps.clear()
            bitmaps.add(UtilityImg.getBlankBitmap())
            bitmaps.add(UtilityImg.getBlankBitmap())
            listOf(PolygonType.WATCH, PolygonType.MCD, PolygonType.MPD).forEach {
                FutureVoid(this, { downloadWatch(it) }, ::showItems)
            }
            FutureVoid(this, { bitmaps[0] = UtilityDownload.getImageProduct(this, "USWARN") }, ::showItems)
            FutureVoid(this, { bitmaps[1] = UtilitySpc.getStormReportsTodayUrl().getImage() }, ::showItems)
            warningsByType.forEach { (_, severeWarning) ->
                FutureVoid(this, { severeWarning.download() }, ::updateWarnings)
            }
        }
    }

    private fun downloadWatch(type: PolygonType) {
        ObjectPolygonWatch.polygonDataByType[type]!!.download(this)
        watchesByType[type]!!.getBitmaps(ObjectPolygonWatch.polygonDataByType[type]!!.storage.value)
    }

    @Synchronized private fun updateWarnings() {
        boxWarnings.removeChildrenAndLayout()
        listOf(PolygonWarningType.TornadoWarning, PolygonWarningType.ThunderstormWarning, PolygonWarningType.FlashFloodWarning).forEach {
            val severeWarning = warningsByType[it]!!
            if (severeWarning.getCount() > 0) {
                ObjectCardBlackHeaderText(this, boxWarnings, "(" + severeWarning.getCount() + ") " + severeWarning.getName())
                severeWarning.warningList.forEach { w ->
                    if (w.isCurrent) {
                        val objectCardDashAlertItem = ObjectCardDashAlertItem(this, boxWarnings, w)
                        objectCardDashAlertItem.connect { Route.hazard(this, arrayOf(w.url, ""))  }
                    }
                }
            }
        }
        toolbar.subtitle = getSubTitle()
    }

    private fun getSubTitle(): String {
        var subTitle = ""
        listOf(PolygonType.WATCH, PolygonType.MCD, PolygonType.MPD).forEach {
            if (watchesByType[it]!!.getCount() > 0) {
                val id = watchesByType[it]!!.typeAsString[0]
                subTitle += if (it == PolygonType.MPD) {
                    "P(${watchesByType[it]!!.getCount()}) "
                } else {
                    "$id(${watchesByType[it]!!.getCount()}) "
                }
            }
        }
        if (warningsByType[PolygonWarningType.TornadoWarning]!!.getCount() > 0
                || warningsByType[PolygonWarningType.ThunderstormWarning]!!.getCount() > 0
                || warningsByType[PolygonWarningType.FlashFloodWarning]!!.getCount() > 0) {
            subTitle += " (${warningsByType[PolygonWarningType.ThunderstormWarning]!!.getCount()},${warningsByType[PolygonWarningType.TornadoWarning]!!.getCount()},${warningsByType[PolygonWarningType.FlashFloodWarning]!!.getCount()})"
        }
        return subTitle
    }

    @Synchronized private fun showItems() {
        boxImages.removeChildrenAndLayout()
        boxRows.clear()
        numbers.clear()
        types.clear()
        numbers.add("USWARN")
        numbers.add("STORM_REPORTS")
        types.add("")
        types.add("")
        bitmaps = bitmaps.subList(0, 2)
        listOf(PolygonType.WATCH, PolygonType.MCD, PolygonType.MPD).forEach { type ->
            bitmaps.addAll(watchesByType[type]!!.bitmaps)
            numbers.addAll(watchesByType[type]!!.numbers)
            repeat(watchesByType[type]!!.bitmaps.size) {
                types.add(watchesByType[type]!!.toString())
            }
        }
        bitmaps.indices.forEach {
            if (it % imagesPerRow == 0) {
                boxRows.add(HBox(this, boxImages.get()))
            }
            val card = Image(this, boxRows.last(), bitmaps[it], imagesPerRow)
            if (it < 2) {
                if (it == 0) {
                    card.connect { Route.usAlerts(this) }
                } else {
                    card.connect { Route.spcStormReports(this) }
                }
            } else {
                if (it < numbers.size) {
                    val number = numbers[it]
                    val type = types[it]
                    card.connect { Route.mcd(this, arrayOf(number, "", type)) }
                }
            }
        }
        toolbar.subtitle = getSubTitle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "Severe Dashboard", "", bitmaps)
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SevereDashboard)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
