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

package joshuatee.wx.nhc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ScrollView
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.VBox

class NhcActivity : BaseActivity() {

    private lateinit var nhc: Nhc
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private var downloadTimer = DownloadTimer("NHC_ACTIVITY")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nhc, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.nhc, false)
        setTitle("NHC", "National Hurricane Center")
        setupUI()
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        nhc = Nhc(this, box)
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded()) {
            scrollView.smoothScrollTo(0, 0)
            FutureVoid(nhc::getText, nhc::showText)
            nhc.urls.forEachIndexed { index, url ->
                FutureBytes(url) { s -> nhc.updateImageData(index, s) }
            }
        }
    }

    private fun showTextProduct(prod: String) {
        Route.wpcText(this, prod.lowercase(Locale.US))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_atl_two -> showTextProduct("MIATWOAT")
            R.id.action_atl_twd -> showTextProduct("MIATWDAT")
            R.id.action_epac_two -> showTextProduct("MIATWOEP")
            R.id.action_epac_twd -> showTextProduct("MIATWDEP")
//            R.id.action_atl_tws -> showTextProduct("MIATWSAT")
//            R.id.action_epac_tws -> showTextProduct("MIATWSEP")
            R.id.action_cpac_two -> showTextProduct("HFOTWOCP")
            R.id.action_share -> UtilityShare.text(this, "NHC", "", nhc.bitmaps)
//            R.id.action_epac_daily -> Route.image(
//                this,
//                "https://www.ssd.noaa.gov/PS/TROP/DATA/RT/SST/PAC/20.jpg",
//                "EPAC Daily Analysis"
//            )

//            R.id.action_atl_daily -> Route.image(
//                this,
//                "https://www.ssd.noaa.gov/PS/TROP/DATA/RT/SST/ATL/20.jpg",
//                "ATL Daily Analysis"
//            )

            R.id.action_epac_7daily -> Route.image(
                this,
                "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/tafb/sst_loop/14_pac.png",
                "EPAC 7-Day Analysis"
            )

            R.id.action_atl_7daily -> Route.image(
                this,
                "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/tafb/sst_loop/14_atl.png",
                "ATL 7-Day Analysis"
            )

            R.id.action_epac_sst_anomaly -> Route.image(
                this,
                "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/tafb/sst_loop/14_pac_anom.png",
                "EPAC SST Anomaly"
            )

            R.id.action_atl_sst_anomaly -> Route.image(
                this,
                "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/tafb/sst_loop/14_atl_anom.png",
                "ATL SST Anomaly"
            )

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        nhc.handleRestartForNotification()
        getContent()
        super.onRestart()
    }
}
