/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
//modded by ELY M.

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.spc.SPCMCDWShowActivity
import joshuatee.wx.spc.SPCStormReportsActivity
import joshuatee.wx.spc.UtilitySPC
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

import kotlinx.coroutines.*

class SevereDashboardActivity : BaseActivity() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.severe_dashboard, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        linearLayout = findViewById(R.id.ll)
        contextg = this
        getContent()
    }

    private fun tvWarnClicked(filter: String) {
        ObjectIntent(
            contextg,
            USWarningsWithRadarActivity::class.java,
            USWarningsWithRadarActivity.URL,
            arrayOf(filter, "us")
        )
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {

        val bitmapArrRep = mutableListOf<Bitmap>()
        //val snWatch = SevereNotice(PolygonType.WATCH)
        val snWatchTor = SevereNotice(PolygonType.WATCH_TOR)
        val snWatchSvr = SevereNotice(PolygonType.WATCH_SVR)
        val snMcd = SevereNotice(PolygonType.MCD)
        val snMpd = SevereNotice(PolygonType.MPD)

            linearLayout.removeAllViews()
            val wTor = SevereWarning(PolygonType.TOR)
            val wSvr = SevereWarning(PolygonType.SVR)
            val wEww = SevereWarning(PolygonType.EWW)
            val wFfw = SevereWarning(PolygonType.FFW)
            val wSmw = SevereWarning(PolygonType.SMW)
            val wSvs = SevereWarning(PolygonType.SVS)
            //val wSps = SpecialWeather(PolygonType.SPS)
            wTor.generateString(contextg, MyApplication.severeDashboardTor.valueGet())
            wSvr.generateString(contextg, MyApplication.severeDashboardSvr.valueGet())
            wEww.generateString(contextg, MyApplication.severeDashboardEww.valueGet())
            wFfw.generateString(contextg, MyApplication.severeDashboardFfw.valueGet())
            wSmw.generateString(contextg, MyApplication.severeDashboardSmw.valueGet())
            wSvs.generateString(contextg, MyApplication.severeDashboardSvs.valueGet())
            //wSps.generateString(contextg, MyApplication.severeDashboardSps.valueGet())
            if (wTor.count > 0) {
            val objTor = ObjectCardText(contextg, linearLayout, wTor.text)
                objTor.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Tornado Warning.*?") })
            }
            if (wSvr.count > 0) {
                val objSvr = ObjectCardText(contextg, linearLayout, wSvr.text)
                objSvr.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Severe Thunderstorm Warning.*?") })
            }
            if (wEww.count > 0) {
                val objEww = ObjectCardText(contextg, linearLayout, wEww.text)
                objEww.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Extreme Wind Warning.*?") })
            }
            if (wFfw.count > 0) {
                val objFfw = ObjectCardText(contextg, linearLayout, wFfw.text)
                objFfw.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Flash Flood Warning.*?") })
            }
            if (wSmw.count > 0) {
                val objSmw = ObjectCardText(contextg, linearLayout, wSmw.text)
                objSmw.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Special Marine Warning.*?") })
            }
            if (wSvs.count > 0) {
                val objSvs = ObjectCardText(contextg, linearLayout, wSvs.text)
                objSvs.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Severe Weather Statement.*?") })
            }
            /*
            if (wSps.count > 0) {
                val objSps = ObjectCardText(contextg, linearLayout, wSps.text)
                objSps.setOnClickListener(View.OnClickListener { tvWarnClicked(".*?Special Weather Statement.*?") })
            }
            */
        
	withContext(Dispatchers.IO) {

            snMcd.getBitmaps(MyApplication.severeDashboardMcd.valueGet())
            //snWatch.getBitmaps(MyApplication.severeDashboardWat.valueGet())
            snWatchTor.getBitmaps(MyApplication.severeDashboardWat.valueGet())
            snWatchSvr.getBitmaps(MyApplication.severeDashboardWat.valueGet())
            snMpd.getBitmaps(MyApplication.severeDashboardMpd.valueGet())
            bitmapArrRep.add((UtilitySPC.getStormReportsTodayUrl()).getImage())
        }
        if (bitmapArrRep.size > 0) {
            bitmapArrRep.indices.forEach { it ->
                val card = ObjectCardImage(contextg, linearLayout, bitmapArrRep[it])
                card.setOnClickListener(View.OnClickListener {
                    ObjectIntent(
                        contextg,
                        SPCStormReportsActivity::class.java,
                        SPCStormReportsActivity.NO,
                        arrayOf("today")
                    )
                })
            }
        }
        listOf(snWatchTor, snWatchSvr, snMcd, snMpd)
            .asSequence()
            .filter { it.bitmaps.size > 0 }
            .forEach { severeNotice ->
                severeNotice.bitmaps.indices.forEach { j ->
                    val card = ObjectCardImage(contextg, linearLayout, severeNotice.bitmaps[j])
                    var cla: Class<*>? = null
                    var claStr = ""
                    val claArgStr = severeNotice.strList[j]
                    when (severeNotice.type) {
                        PolygonType.MCD -> {
                            cla = SPCMCDWShowActivity::class.java
                            claStr = SPCMCDWShowActivity.NO
                        }
                        PolygonType.WATCH_TOR, PolygonType.WATCH_SVR -> {
                            cla = SPCMCDWShowActivity::class.java
                            claStr = SPCMCDWShowActivity.NO
                        }
                        PolygonType.MPD -> {
                            cla = SPCMCDWShowActivity::class.java
                            claStr = SPCMCDWShowActivity.NO
                        }
                        else -> {
                        }
                    }
                    val cl = cla
                    val clStr = claStr
                    card.setOnClickListener(View.OnClickListener {
                        ObjectIntent(
                            contextg,
                            cl!!,
                            clStr,
                            arrayOf(claArgStr, "", severeNotice.toString())
                        )
                    })
                }
            }
        //bitmaps.addAll(snWatch.bitmaps)
        bitmaps.addAll(snWatchTor.bitmaps)
        bitmaps.addAll(snWatchSvr.bitmaps)
        bitmaps.addAll(snMcd.bitmaps)
        bitmaps.addAll(snMpd.bitmaps)
        bitmaps.addAll(bitmapArrRep)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, "Severe Dashboard", "", bitmaps)
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.SevereDashboard)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
