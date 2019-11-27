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

package joshuatee.wx.nhc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.notifications.UtilityNotificationNhc
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.util.*
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*
import kotlin.math.max

class NhcStormActivity : AudioPlayActivity(), OnMenuItemClickListener {

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
    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var topBitmap: Bitmap
    private var baseUrl = ""
    private var baseUrlShort = ""
    private lateinit var cTextProd: ObjectCardText

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.nhc_storm
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        activityArguments = intent.getStringArrayExtra(URL).toList()
        url = activityArguments[0]
        toolbarTitle = activityArguments[1]
        val titleArr = toolbarTitle.split(" - ")
        title = "NHC"
        if (titleArr.size > 1) {
            toolbar.subtitle = titleArr[1]
        }
        val imgUrl1 = activityArguments[3]
        val year = UtilityTime.year()
        var yearInString = year.toString()
        val yearInStringFull = year.toString()
        val yearInStringShort = yearInString.substring(2)
        yearInString = yearInString.substring(max(yearInString.length - 2, 0))
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
        product = "MIATCP$stormId"
        baseUrlShort = baseUrl.replace(yearInStringFull, "") + yearInStringShort
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps.clear()
        withContext(Dispatchers.IO) { topBitmap = (baseUrl + "_5day_cone_with_line_and_wind_sm2.png").getImage() }
        ObjectCardImage(this@NhcStormActivity, ll, topBitmap)
        withContext(Dispatchers.IO) {
            url = UtilityDownload.getTextProduct(this@NhcStormActivity, product)
        }
        cTextProd = ObjectCardText(this@NhcStormActivity, ll, toolbar, toolbarBottom)
        cTextProd.setText(Utility.fromHtml(url))
        html = url
        withContext(Dispatchers.IO) {
            listOf(
                    "_key_messages.png",
                    "WPCQPF_sm2.gif",
                    "_earliest_reasonable_toa_34_sm2.png",
                    "_most_likely_toa_34_sm2.png",
                    "_wind_probs_34_F120_sm2.png",
                    "_wind_probs_50_F120_sm2.png",
                    "_wind_probs_64_F120_sm2.png"
            ).forEach {
                var url = baseUrl
                if (it == "WPCQPF_sm2.gif"){
                    url = baseUrlShort
                }
                bitmaps.add((url + it).getImage())
            }
        }
        bitmaps.filter { it.width > 100 }
                .forEach { ObjectCardImage(this@NhcStormActivity, ll, it) }
        if (activityArguments.size > 2) {
            if (activityArguments[2] == "sound") UtilityTts.synthesizeTextAndPlay(
                    applicationContext,
                    html,
                    product
            )
        }
    }

    private fun getText() = GlobalScope.launch(uiDispatcher) {
        url = withContext(Dispatchers.IO) { UtilityDownload.getTextProduct(this@NhcStormActivity, product) }
        if (url.contains("<")) {
            cTextProd.setText(Utility.fromHtml(url))
        } else {
            cTextProd.setText(url)
        }
        html = url
        sv.smoothScrollTo(0, 0)
    }

    private fun setProduct(productF: String) {
        /*ObjectIntent(
                this@NhcStormActivity,
                WpcTextProductsActivity::class.java,
                WpcTextProductsActivity.URL,
                arrayOf(productF)
        )*/
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
                    this,
                    activityArguments[1],
                    Utility.fromHtml(url),
                    bitmaps
            )
            R.id.action_MIATCPEP2 -> setProduct("MIATCP$stormId")
            R.id.action_MIATCMEP2 -> setProduct("MIATCM$stormId")
            R.id.action_MIATCDEP2 -> setProduct("MIATCD$stormId")
            R.id.action_MIAPWSEP2 -> setProduct("MIAPWS$stormId")
            R.id.action_mute_notification -> UtilityNotificationNhc.muteNotification(
                    this,
                    toolbarTitle
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}






