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
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.getImage
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CardBlackHeaderText
import joshuatee.wx.ui.CardDashAlertItem
import joshuatee.wx.ui.HBox
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.R
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.util.DownloadImage
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

class SevereDashboardActivity : BaseActivity() {

    //
    // Show a variety of graphical and textual data including
    // US Alert map, Storm reports, any active Watch/MPD/MCD
    // Tornado, Tstorm, or FFW warnings
    // All data items can be tapped on for further exploration
    //

    private var bitmaps = mutableListOf<Bitmap>()
    private var numbers = mutableListOf<String>()
    private var types = mutableListOf<String>()
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
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setupUI()
        getContent()
    }

    private fun setupUI() {
        box = VBox.fromResource(this)
        boxImages = VBox(this)
        boxWarnings = VBox(this)
        box.addLayout(boxImages)
        box.addLayout(boxWarnings)
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded()) {
            bitmaps = MutableList(2) { UtilityImg.getBlankBitmap() }
            listOf(PolygonType.WATCH, PolygonType.MCD, PolygonType.MPD).forEach {
                FutureVoid({ downloadWatch(it) }, ::showItems)
            }
            FutureVoid({ bitmaps[0] = DownloadImage.byProduct(this, "USWARN") }, ::showItems)
            FutureVoid(
                { bitmaps[1] = UtilitySpc.getStormReportsTodayUrl().getImage() },
                ::showItems
            )
            warningsByType.values.forEach {
                FutureVoid({ it.download() }, ::updateWarnings)
            }
        }
    }

    private fun downloadWatch(type: PolygonType) {
        PolygonWatch.byType[type]!!.download(this)
        watchesByType[type]!!.getBitmaps(PolygonWatch.byType[type]!!.storage.value)
    }

    @Synchronized
    private fun updateWarnings() {
        boxWarnings.removeChildrenAndLayout()
        listOf(
            PolygonWarningType.TornadoWarning,
            PolygonWarningType.ThunderstormWarning,
            PolygonWarningType.FlashFloodWarning
        ).forEach {
            val severeWarning = warningsByType[it]!!
            if (severeWarning.getCount() > 0) {
                boxWarnings.addWidget(
                    CardBlackHeaderText(
                        this,
                        "(" + severeWarning.getCount() + ") " + severeWarning.getName()
                    )
                )
                severeWarning.warningList.forEach { w ->
                    if (w.isCurrent) {
                        val cardDashAlertItem = CardDashAlertItem(this, w)
                        boxWarnings.addWidget(cardDashAlertItem)
                        cardDashAlertItem.connect { Route.hazard(this, w.url) }
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
            || warningsByType[PolygonWarningType.FlashFloodWarning]!!.getCount() > 0
        ) {
            subTitle += " (${warningsByType[PolygonWarningType.ThunderstormWarning]!!.getCount()},${warningsByType[PolygonWarningType.TornadoWarning]!!.getCount()},${warningsByType[PolygonWarningType.FlashFloodWarning]!!.getCount()})"
        }
        return subTitle
    }

    @Synchronized
    private fun showItems() {
        boxImages.removeChildrenAndLayout()
        boxRows.clear()
        numbers = mutableListOf("USWARN", "STORM_REPORTS")
        types = mutableListOf("", "")
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
                boxRows.add(HBox(this, boxImages))
            }
            val card = Image(this, bitmaps[it], imagesPerRow)
            boxRows.last().addWidget(card)
            if (it < 2) {
                if (it == 0) {
                    card.connect { Route.alerts(this) }
                } else {
                    card.connect { Route.spcStormReports(this) }
                }
            } else {
                if (it < numbers.size) {
                    val number = numbers[it]
                    val type = types[it]
                    card.connect { Route.mcd(this, number, type) }
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

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_1 -> Route.alerts(this)
            KeyEvent.KEYCODE_2 -> Route.spcStormReports(this)
            KeyEvent.KEYCODE_REFRESH -> getContent()
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}
