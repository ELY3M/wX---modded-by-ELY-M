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
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.notifications.UtilityNotificationNHC
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.*

class NHCStormActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // Main page for details on individual storms
    //
    // Arguments
    //
    //  1: URL
    //  2: Title
    //  3: IMG URL
    //  4: IMG URL
    //  5: Storm ID
    //

    companion object {
        const val URL: String = ""
    }

    private lateinit var turl: List<String>
    private var url = ""
    private var sigHtmlTmp = ""
    private var prod = ""
    private var stormId = ""
    private var goesId = ""
    private var goesSector = ""
    private var toolbarTitle = ""
    private lateinit var sv: ScrollView
    private val bmAl = mutableListOf<Bitmap>()
    private var baseUrl = ""
    private lateinit var ll: LinearLayout
    private lateinit var cTextProd: ObjectCardText
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.nhc_storm)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        ll = findViewById(R.id.ll)
        sv = findViewById(R.id.sv)
        turl = intent.getStringArrayExtra(URL).toList()
        url = turl[0]
        toolbarTitle = turl[1]
        val titleArr = toolbarTitle.split(" - ")
        if (titleArr.size > 1) {
            title = titleArr[1]
        }
        val imgUrl1 = turl[3]
        val year = UtilityTime.year()
        var yearInString = year.toString()
        yearInString = yearInString.substring(Math.max(yearInString.length - 2, 0))
        baseUrl = imgUrl1.replace(yearInString + "_5day_cone_with_line_and_wind_sm2.png", "")
        baseUrl += yearInString
        stormId = turl[5]
        stormId = stormId.replace("EP0", "EP").replace("AL0", "AL")
        goesSector = UtilityStringExternal.truncate(stormId, 1)
        goesSector = goesSector.replace("A", "L")  // value is either E or L
        stormId = stormId.replace("AL", "AT")
        goesId = stormId.replace("EP", "").replace("AT", "")
        if (goesId.length < 2) {
            goesId = "0$goesId"
        }
        cTextProd = ObjectCardText(this)
        cTextProd.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
        ll.addView(cTextProd.card)
        prod = "MIATCP$stormId"
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            bmAl.clear()
        }

        override fun doInBackground(vararg params: String): String {
            url = UtilityDownload.getTextProduct(contextg, prod)
            listOf("_W5_NL_sm2.png", "_5day_cone_with_line_and_wind_sm2.png", "_W_NL_sm2.gif", "_wind_probs_34_F120_sm2.png", "_wind_probs_50_F120_sm2.png",
                    "_wind_probs_64_F120_sm2.png", "_R_sm2.png", "_S_sm2.png", "_WPCQPF_sm2.png").forEach { bmAl.add((baseUrl + it).getImage()) }
            bmAl.add("http://www.nhc.noaa.gov/tafb_latest/danger_pac_latestBW_sm3.gif".getImage())
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            cTextProd.setText(Utility.fromHtml(url))
            sigHtmlTmp = url
            sv.smoothScrollTo(0, 0)
            bmAl.filter { it.width > 100 }.forEach { ll.addView(ObjectCardImage(contextg, it).card) }
            if (turl.size > 2) {
                if (turl[2] == "sound") UtilityTTS.synthesizeTextAndPlay(applicationContext, sigHtmlTmp, prod)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetText : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            url = UtilityDownload.getTextProduct(contextg, prod)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            if (url.contains("<")) {
                cTextProd.setText(Utility.fromHtml(url))
            } else {
                cTextProd.setText(url)
            }
            sigHtmlTmp = url
            sv.smoothScrollTo(0, 0)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, sigHtmlTmp, prod, prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, turl[1], Utility.fromHtml(url), bmAl)
            R.id.action_MIATCPEP2 -> {
                prod = "MIATCP$stormId"
                GetText().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_MIATCMEP2 -> {
                prod = "MIATCM$stormId"
                GetText().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_MIATCDEP2 -> {
                prod = "MIATCD$stormId"
                GetText().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_MIAPWSEP2 -> {
                prod = "MIAPWS$stormId"
                GetText().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            R.id.action_mute_notification -> UtilityNotificationNHC.muteNotification(this, toolbarTitle)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}






