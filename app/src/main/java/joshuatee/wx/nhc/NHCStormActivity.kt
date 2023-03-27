/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.notifications.NotificationNhc
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureText
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.HBox
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

class NhcStormActivity : BaseActivity() {

    //
    // Main page for details on individual storms
    //
    // Arguments
    //
    //  1: object ObjectNhcStormDetails
    //

    companion object { const val URL = "" }

    private lateinit var stormData: NhcStormDetails
    private var product = ""
    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var cardText: CardText
    private lateinit var box: VBox
    private lateinit var boxText: VBox
    private lateinit var boxImage: VBox
    private var imagesPerRow = 2
    private val boxRows = mutableListOf<HBox>()
    private val imageUrls = listOf(
        "_5day_cone_with_line_and_wind_sm2.png",
        "_key_messages.png",
        "WPCQPF_sm2.gif",
        "WPCERO_sm2.gif",
        "_earliest_reasonable_toa_34_sm2.png",
        "_most_likely_toa_34_sm2.png",
        "_wind_probs_34_F120_sm2.png",
        "_wind_probs_50_F120_sm2.png",
        "_wind_probs_64_F120_sm2.png"
    )
    private var textProductUrl = ""
    private var office = "MIA"
    private var downloadTimer = DownloadTimer("NHC_ACTIVITY_STORM")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nhc_storm, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.nhc_storm, false)
        stormData = intent.getSerializableExtra(URL) as NhcStormDetails
        setTitle(stormData.name + " " + stormData.classification, stormData.forTopHeader())
        box = VBox.fromResource(this)
        boxImage = VBox(this)
        boxText = VBox(this)
        box.addLayout(boxImage)
        box.addLayout(boxText)
        product = "MIATCP${stormData.binNumber}"
        textProductUrl = stormData.advisoryNumber
        if (textProductUrl.startsWith("HFO")) {
            office = "HFO"
        }
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded(this)) {
            FutureVoid(this, ::downloadImages, ::showImages)
            FutureText(this, textProductUrl, ::showText)
        }
    }

    private fun downloadImages() {
        bitmaps.clear()
        imageUrls.forEach {
            var url = stormData.baseUrl
            if (it == "WPCQPF_sm2.gif" || it == "WPCERO_sm2.gif") {
                url = url.dropLast(2)
            }
            bitmaps.add((url + it).getImage())
        }
    }

    fun showImages() {
        boxImage.removeChildrenAndLayout()
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 100) {
                if (index % imagesPerRow == 0) {
                    boxRows.add(HBox(this, boxImage.get()))
                }
                val image = Image(this, bitmap, imagesPerRow)
                boxRows.last().addWidget(image)
                image.connect {
                    var url = stormData.baseUrl
                    if (imageUrls[index] == "WPCQPF_sm2.gif" || imageUrls[index] == "WPCERO_sm2.gif") {
                        url = url.dropLast(2)
                    }
                    val fullUrl = url + imageUrls[index]
                    Route.image(this, fullUrl, "")
                }
            }
        }
    }

    fun showText(s: String) {
        boxText.removeChildrenAndLayout()
        cardText = CardText(this, toolbar, toolbarBottom)
        boxText.addWidget(cardText)
        if (s.contains("<")) {
            cardText.text = Utility.fromHtml(s)
        } else {
            cardText.text = s
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_cloud -> Route.visNhc(this, stormData.goesUrl)
            R.id.action_share -> UtilityShare.text(this, stormData.name, "", bitmaps)
            R.id.action_MIATCPEP2 -> Route.wpcText(this, "${office}TCP${stormData.binNumber}")
            R.id.action_MIATCMEP2 -> Route.wpcText(this, "${office}TCM${stormData.binNumber}")
            R.id.action_MIATCDEP2 -> Route.wpcText(this, "${office}TCD${stormData.binNumber}")
            R.id.action_MIAPWSEP2 -> Route.wpcText(this, "${office}PWS${stormData.binNumber}")
            R.id.action_mute_notification -> NotificationNhc.muteNotification(this, stormData.id)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
