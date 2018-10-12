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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener

import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class LightningActivity : VideoRecordActivity(), OnClickListener {

    companion object {
        const val URL: String = ""
    }

    private var bitmap = UtilityImg.getBlankBitmap()
    private var sector = "usa_big"
    private var sectorPretty = "USA"
    private var period = "0.25"
    private var periodPretty = "15 MIN"
    private lateinit var img: TouchImageView2
    private var firstRun = false
    private var imageLoaded = false
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.lightning_activity, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show, null, true, false)
        contextg = this
        toolbar.setOnClickListener { toolbar.showOverflowMenu() }
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        sector = Utility.readPref(this, "LIGHTNING_SECTOR", sector)
        period = Utility.readPref(this, "LIGHTNING_PERIOD", period)
        sectorPretty = UtilityLightning.getSectorPretty(sector)
        periodPretty = UtilityLightning.getTimePretty(period)
        title = "Lightning Strikes"
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            bitmap = UtilityLightning.getImage(sector, period)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.setImageBitmap(bitmap)
            if (!firstRun) {
                img.setZoom("LIGHTNING")
                firstRun = true
            }
            Utility.writePref(contextg, "LIGHTNING_SECTOR", sector)
            Utility.writePref(contextg, "LIGHTNING_PERIOD", period)
            imageLoaded = true
        }

        override fun onPreExecute() {
            title = "Lightning $sectorPretty"
            toolbar.subtitle = periodPretty
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    UtilityShare.shareBitmap(this, "Lightning Strikes $sectorPretty $periodPretty", bitmap)
                }
            }
            R.id.action_us -> setSectorGetContent("usa_big", "USA")
            R.id.action_fl -> setSectorGetContent("florida_big", "FL")
            R.id.action_tx -> setSectorGetContent("texas_big", "TX")
            R.id.action_ok_ks -> setSectorGetContent("oklahoma_kansas_big", "OK,KS")
            R.id.action_na -> setSectorGetContent("north_middle_america", "North America")
            R.id.action_sa -> setSectorGetContent("south_america", "South America")
            R.id.action_au -> setSectorGetContent("australia_big", "Australia")
            R.id.action_nz -> setSectorGetContent("new_zealand_big", "New Zealand")
            R.id.action_15min -> setPeriodGetContent("0.25", "15 MIN")
            R.id.action_2hr -> setPeriodGetContent("2", "2 HR")
            R.id.action_12hr -> setPeriodGetContent("12", "12 HR")
            R.id.action_24hr -> setPeriodGetContent("24", "24 HR")
            R.id.action_48hr -> setPeriodGetContent("48", "48 HR")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setSectorGetContent(sector: String, sectorPretty: String) {
        this.sector = sector
        this.sectorPretty = sectorPretty
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun setPeriodGetContent(period: String, periodPretty: String) {
        this.period = period
        this.periodPretty = periodPretty
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onClick(v: View) {
        when (v.id) {R.id.iv -> UtilityToolbar.showHide(toolbar)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "LIGHTNING")
        }
        super.onStop()
    }
}
