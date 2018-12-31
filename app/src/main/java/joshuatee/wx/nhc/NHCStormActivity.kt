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
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.notifications.UtilityNotificationNHC
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.util.*
import kotlinx.coroutines.*

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

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: List<String>
    private var url = ""
    private var html = ""
    private var product = ""
    private var stormId = ""
    private var goesId = ""
    private var goesSector = ""
    private var toolbarTitle = ""
    private lateinit var scrollView: ScrollView
    private val bitmaps = mutableListOf<Bitmap>()
    private var baseUrl = ""
    private lateinit var linearLayout: LinearLayout
    private lateinit var cTextProd: ObjectCardText
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.nhc_storm
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        linearLayout = findViewById(R.id.ll)
        scrollView = findViewById(R.id.sv)
        activityArguments = intent.getStringArrayExtra(URL).toList()
        url = activityArguments[0]
        toolbarTitle = activityArguments[1]
        val titleArr = toolbarTitle.split(" - ")
        if (titleArr.size > 1) {
            title = titleArr[1]
        }
        val imgUrl1 = activityArguments[3]
        val year = UtilityTime.year()
        var yearInString = year.toString()
        yearInString = yearInString.substring(Math.max(yearInString.length - 2, 0))
        baseUrl = imgUrl1.replace(yearInString + "_5day_cone_with_line_and_wind_sm2.png", "")
        baseUrl += yearInString
        stormId = activityArguments[5]
        stormId = stormId.replace("EP0", "EP").replace("AL0", "AL")
        goesSector = UtilityStringExternal.truncate(stormId, 1)
        goesSector = goesSector.replace("A", "L")  // value is either E or L
        stormId = stormId.replace("AL", "AT")
        goesId = stormId.replace("EP", "").replace("AT", "")
        if (goesId.length < 2) {
            goesId = "0$goesId"
        }
        cTextProd = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        product = "MIATCP$stormId"
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps.clear()
        withContext(Dispatchers.IO) {
            url = UtilityDownload.getTextProduct(contextg, product)
            listOf(
                "_W5_NL_sm2.png",
                "_5day_cone_with_line_and_wind_sm2.png",
                "_W_NL_sm2.gif",
                "_wind_probs_34_F120_sm2.png",
                "_wind_probs_50_F120_sm2.png",
                "_wind_probs_64_F120_sm2.png",
                "_R_sm2.png",
                "_S_sm2.png",
                "_WPCQPF_sm2.png"
            ).forEach { bitmaps.add((baseUrl + it).getImage()) }
            bitmaps.add("${MyApplication.nwsNhcWebsitePrefix}/tafb_latest/danger_pac_latestBW_sm3.gif".getImage())
        }
        cTextProd.setText(Utility.fromHtml(url))
        html = url
        scrollView.smoothScrollTo(0, 0)
        bitmaps.filter { it.width > 100 }
            .forEach { ObjectCardImage(contextg, linearLayout, it) }
        if (activityArguments.size > 2) {
            if (activityArguments[2] == "sound") UtilityTTS.synthesizeTextAndPlay(
                applicationContext,
                html,
                product
            )
        }
    }

    private fun getText() = GlobalScope.launch(uiDispatcher) {
        url = withContext(Dispatchers.IO) { UtilityDownload.getTextProduct(contextg, product) }
        if (url.contains("<")) {
            cTextProd.setText(Utility.fromHtml(url))
        } else {
            cTextProd.setText(url)
        }
        html = url
        scrollView.smoothScrollTo(0, 0)
    }

    private fun setProduct(productF: String) {
        product = productF
        getText()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, product, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                this,
                activityArguments[1],
                Utility.fromHtml(url),
                bitmaps
            )
            R.id.action_MIATCPEP2 -> setProduct("MIATCP$stormId")
            R.id.action_MIATCMEP2 -> setProduct("MIATCM$stormId")
            R.id.action_MIATCDEP2 -> setProduct("MIATCD$stormId")
            R.id.action_MIAPWSEP2 -> setProduct("MIAPWS$stormId")
            R.id.action_mute_notification -> UtilityNotificationNHC.muteNotification(
                this,
                toolbarTitle
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}






