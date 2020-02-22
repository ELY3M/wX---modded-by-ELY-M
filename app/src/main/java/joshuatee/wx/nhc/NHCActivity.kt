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

package joshuatee.wx.nhc

import android.annotation.SuppressLint
import java.util.Locale

import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.wpc.WpcTextProductsActivity
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class NhcActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // URL is not really used save in the homescreen map since most other activities have a var
    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var objNhc: ObjectNhc

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.nhc
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        objNhc = ObjectNhc(this, ll)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        scrollView.smoothScrollTo(0, 0)
        withContext(Dispatchers.IO) { objNhc.getTextData() }
        objNhc.showTextData()
        NhcOceanEnum.values().forEach {
            withContext(Dispatchers.IO) { objNhc.regionMap[it]!!.getImages() }
            objNhc.showImageData(it)
        }
    }

    private fun showTextProduct(prod: String) {
        ObjectIntent(
                this,
                WpcTextProductsActivity::class.java,
                WpcTextProductsActivity.URL,
                arrayOf(prod.toLowerCase(Locale.US), "")
        )
    }

    private fun showImageProduct(imageUrl: String, title: String, needWhiteBG: String) {
        ObjectIntent(
                this,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf(imageUrl, title, needWhiteBG)
        )
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objNhc.html, "", "")) {
            return true
        }
        when (item.itemId) {
            R.id.action_atl_two -> showTextProduct("MIATWOAT")
            R.id.action_atl_twd -> showTextProduct("MIATWDAT")
            R.id.action_epac_two -> showTextProduct("MIATWOEP")
            R.id.action_epac_twd -> showTextProduct("MIATWDEP")
            R.id.action_atl_tws -> showTextProduct("MIATWSAT")
            R.id.action_epac_tws -> showTextProduct("MIATWSEP")
            R.id.action_cpac_two -> showTextProduct("HFOTWOCP")
            R.id.action_share -> UtilityShare.shareText(this, "", Utility.fromHtml(objNhc.html))
            R.id.action_epac_daily -> showImageProduct(
                    "https://www.ssd.noaa.gov/PS/TROP/DATA/RT/SST/PAC/20.jpg",
                    "EPAC Daily Analysis",
                    "false"
            )
            R.id.action_atl_daily -> showImageProduct(
                    "https://www.ssd.noaa.gov/PS/TROP/DATA/RT/SST/ATL/20.jpg",
                    "ATL Daily Analysis",
                    "false"
            )
            R.id.action_epac_7daily -> showImageProduct(
                    "${MyApplication.nwsNhcWebsitePrefix}/tafb/pac_anal.gif",
                    "EPAC 7-Day Analysis",
                    "true"
            )
            R.id.action_atl_7daily -> showImageProduct(
                    "${MyApplication.nwsNhcWebsitePrefix}/tafb/atl_anal.gif",
                    "ATL 7-Day Analysis",
                    "true"
            )
            R.id.action_epac_sst_anomaly -> showImageProduct(
                    "${MyApplication.nwsNhcWebsitePrefix}/tafb/pac_anom.gif",
                    "EPAC SST Anomaly",
                    "true"
            )
            R.id.action_atl_sst_anomaly -> showImageProduct(
                    "${MyApplication.nwsNhcWebsitePrefix}/tafb/atl_anom.gif",
                    "ATL SST Anomaly",
                    "true"
            )
            R.id.action_glcfs -> ObjectIntent(
                    this,
                    ModelsGenericActivity::class.java,
                    ModelsGenericActivity.INFO,
                    arrayOf("1", "GLCFS", "GLCFS")
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        objNhc.handleRestartForNotification()
        super.onRestart()
    }
}


