/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.UtilityDownloadMcd
import joshuatee.wx.radar.UtilityDownloadMpd
import joshuatee.wx.radar.UtilityDownloadWatch
import joshuatee.wx.radar.UtilityDownloadWarnings
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SevereDashboardActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val bitmaps = mutableListOf<Bitmap>()
    private var watchCount = 0
    private var mcdCount = 0
    private var mpdCount = 0
    private var tstCount = 0
    private var ffwCount = 0
    private var torCount = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        getContent()
    }

    private fun warningsClicked(filter: String) {
        ObjectIntent(
                this@SevereDashboardActivity,
                USWarningsWithRadarActivity::class.java,
                USWarningsWithRadarActivity.URL,
                arrayOf(filter, "us")
        )
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        // FIXME var naming
        val bitmapArrRep = mutableListOf<Bitmap>()
        val snWat = SevereNotice(PolygonType.WATCH)
        val snMcd = SevereNotice(PolygonType.MCD)
        val snMpd = SevereNotice(PolygonType.MPD)
        ll.removeAllViews()
        val wTor = SevereWarning(PolygonType.TOR)
        val wTst = SevereWarning(PolygonType.TST)
        val wFfw = SevereWarning(PolygonType.FFW)
        withContext(Dispatchers.IO) {
            UtilityDownloadWarnings.get(this@SevereDashboardActivity)
            wTor.generateString(this@SevereDashboardActivity, MyApplication.severeDashboardTor.value)
            wTst.generateString(this@SevereDashboardActivity, MyApplication.severeDashboardTst.value)
            wFfw.generateString(this@SevereDashboardActivity, MyApplication.severeDashboardFfw.value)
        }
        if (wTor.count > 0) {
            val objTor = ObjectCardText(this@SevereDashboardActivity, ll, wTor.text)
            objTor.setOnClickListener(View.OnClickListener { warningsClicked(".*?Tornado Warning.*?") })
        }
        if (wTst.count > 0) {
            val objTst = ObjectCardText(this@SevereDashboardActivity, ll, wTst.text)
            objTst.setOnClickListener(View.OnClickListener { warningsClicked(".*?Severe Thunderstorm Warning.*?") })
        }
        if (wFfw.count > 0) {
            val objFfw = ObjectCardText(this@SevereDashboardActivity, ll, wFfw.text)
            objFfw.setOnClickListener(View.OnClickListener { warningsClicked(".*?Flash Flood Warning.*?") })
        }
        withContext(Dispatchers.IO) {
            UtilityDownloadMcd.get(this@SevereDashboardActivity)
            UtilityDownloadWatch.get(this@SevereDashboardActivity)
            UtilityDownloadMpd.get(this@SevereDashboardActivity)
            snMcd.getBitmaps(MyApplication.severeDashboardMcd.value)
            snWat.getBitmaps(MyApplication.severeDashboardWat.value)
            snMpd.getBitmaps(MyApplication.severeDashboardMpd.value)
            bitmapArrRep.add((UtilitySpc.getStormReportsTodayUrl()).getImage())
        }
        if (bitmapArrRep.size > 0) {
            bitmapArrRep.indices.forEach {
                val card = ObjectCardImage(this@SevereDashboardActivity, ll, bitmapArrRep[it])
                card.setOnClickListener(View.OnClickListener {
                    ObjectIntent(
                            this@SevereDashboardActivity,
                            SpcStormReportsActivity::class.java,
                            SpcStormReportsActivity.NO,
                            arrayOf("today")
                    )
                })
            }
        }
        listOf(snWat, snMcd, snMpd)
                .asSequence()
                .filter { it.bitmaps.size > 0 }
                .forEach { severeNotice ->
                    severeNotice.bitmaps.indices.forEach { j ->
                        val card = ObjectCardImage(this@SevereDashboardActivity, ll, severeNotice.bitmaps[j])
                        var cla: Class<*>? = null
                        var claStr = ""
                        val claArgStr = severeNotice.numbers[j]
                        when (severeNotice.type) {
                            PolygonType.MCD -> {
                                cla = SpcMcdWatchShowActivity::class.java
                                claStr = SpcMcdWatchShowActivity.NO
                            }
                            PolygonType.WATCH -> {
                                cla = SpcMcdWatchShowActivity::class.java
                                claStr = SpcMcdWatchShowActivity.NO
                            }
                            PolygonType.MPD -> {
                                cla = SpcMcdWatchShowActivity::class.java
                                claStr = SpcMcdWatchShowActivity.NO
                            }
                            else -> {
                            }
                        }
                        val cl = cla
                        val clStr = claStr
                        card.setOnClickListener(View.OnClickListener {
                            ObjectIntent(
                                    this@SevereDashboardActivity,
                                    cl!!,
                                    clStr,
                                    arrayOf(claArgStr, "", severeNotice.toString())
                            )
                        })
                    }
                }
        bitmaps.addAll(snWat.bitmaps)
        bitmaps.addAll(snMcd.bitmaps)
        bitmaps.addAll(snMpd.bitmaps)
        bitmaps.addAll(bitmapArrRep)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, "Severe Dashboard", "", bitmaps)
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SevereDashboard)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
