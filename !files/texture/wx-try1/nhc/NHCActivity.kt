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

package joshuatee.wx.nhc

import android.annotation.SuppressLint
import java.util.Locale

import android.os.AsyncTask
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.models.ModelsGLCFSActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.wpc.WPCTextProductsActivity

class NHCActivity : AudioPlayActivity(), OnMenuItemClickListener {

    private lateinit var dynamicview: LinearLayout
    private lateinit var objNHC: ObjectNHC

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.nhc)
        toolbarBottom.setOnMenuItemClickListener(this)
        dynamicview = findViewById(R.id.ll)
        objNHC = ObjectNHC(this, dynamicview)
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            val sv: ScrollView = findViewById(R.id.sv)
            sv.smoothScrollTo(0, 0)
        }

        override fun doInBackground(vararg params: String): String {
            objNHC.getData()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            objNHC.showData()
        }
    }

    private fun showTextProduct(prod: String) {
        ObjectIntent(this, WPCTextProductsActivity::class.java, WPCTextProductsActivity.URL, arrayOf(prod.toLowerCase(Locale.US), ""))
    }

    private fun showImageProduct(imageUrl: String, title: String, needWhiteBG: String) {
        ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf(imageUrl, title, needWhiteBG))
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objNHC.html, "", "")) {
            return true
        }
        when (item.itemId) {
            R.id.action_atl_two -> showTextProduct("MIATWOAT")
            R.id.action_atl_twd -> showTextProduct("MIATWDAT")
            R.id.action_epac_two -> showTextProduct("MIATWOEP")
            R.id.action_epac_twd -> showTextProduct("MIATWDEP")
            R.id.action_atl_tws -> showTextProduct("MIATWSAT")
            R.id.action_epac_tws -> showTextProduct("MIATWSEP")
            R.id.action_share -> UtilityShare.shareText(this, "", Utility.fromHtml(objNHC.html))
            R.id.action_epac_daily -> showImageProduct("http://www.ssd.noaa.gov/PS/TROP/DATA/RT/SST/PAC/20.jpg", "EPAC Daily Analysis", "false")
            R.id.action_atl_daily -> showImageProduct("http://www.ssd.noaa.gov/PS/TROP/DATA/RT/SST/ATL/20.jpg", "ATL Daily Analysis", "false")
            R.id.action_epac_7daily -> showImageProduct("http://www.nhc.noaa.gov/tafb/pac_anal.gif", "EPAC 7-Day Analysis", "true")
            R.id.action_atl_7daily -> showImageProduct("http://www.nhc.noaa.gov/tafb/atl_anal.gif", "ATL 7-Day Analysis", "true")
            R.id.action_epac_sst_anomaly -> showImageProduct("http://www.nhc.noaa.gov/tafb/pac_anom.gif", "EPAC SST Anomaly", "true")
            R.id.action_atl_sst_anomaly -> showImageProduct("http://www.nhc.noaa.gov/tafb/atl_anom.gif", "ATL SST Anomaly", "true")
            R.id.action_glcfs -> startActivity(Intent(this, ModelsGLCFSActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRestart() {
        objNHC.handleRestartForNotif()
        super.onRestart()
    }
}


