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

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.notifications.UtilityNotificationNhc
import joshuatee.wx.objects.FutureText
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class NhcStormActivity : BaseActivity() {

    // Main page for details on individual storms
    //
    // Arguments
    //
    //  1: object ObjectNhcStormDetails
    //

    companion object { const val URL = "" }

    private lateinit var stormData: ObjectNhcStormDetails
    private var product = ""
    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var objectCardText: ObjectCardText
    private lateinit var box: LinearLayout
    private lateinit var boxText: VBox
    private lateinit var boxImage: VBox
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val horizontalLinearLayouts = mutableListOf<HBox>()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nhc_storm, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.nhc_storm, false)
        box = findViewById(R.id.linearLayout)
        boxImage = VBox(this, box)
        boxText = VBox(this, box)
        stormData = intent.getSerializableExtra(URL) as ObjectNhcStormDetails
        title = stormData.name + " " + stormData.classification
        toolbar.subtitle = stormData.forTopHeader()
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
        FutureVoid( this, ::downloadImages, ::showImages)
        FutureText(this, textProductUrl, ::showText)
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
        boxImage.removeAllViews()
        numberOfImages = 0
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 100) {
                val objectCardImage = if (numberOfImages % imagesPerRow == 0) {
                    val hbox = HBox(this, boxImage.get())
                    horizontalLinearLayouts.add(hbox)
                    ObjectCardImage(this, hbox.get(), bitmap, imagesPerRow)
                } else {
                    ObjectCardImage(this, horizontalLinearLayouts.last().get(), bitmap, imagesPerRow)
                }
                numberOfImages += 1
                objectCardImage.setOnClickListener {
                    var url = stormData.baseUrl
                    if (imageUrls[index] == "WPCQPF_sm2.gif" || imageUrls[index] == "WPCERO_sm2.gif") {
                        url = url.dropLast(2)
                    }
                    val fullUrl = url + imageUrls[index]
                    ObjectIntent.showImage(this, arrayOf(fullUrl, ""))
                }
            }
        }
    }

    fun showText(s: String) {
        boxText.removeAllViews()
        objectCardText = ObjectCardText(this, boxText.get(), toolbar, toolbarBottom)
        if (s.contains("<")) {
            objectCardText.text = Utility.fromHtml(s)
        } else {
            objectCardText.text = s
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_cloud -> ObjectIntent.showVisNhc(this, stormData.goesUrl)
            R.id.action_share -> UtilityShare.text(this, stormData.name, "", bitmaps)
            R.id.action_MIATCPEP2 -> ObjectIntent.showWpcText(this, arrayOf("${office}TCP${stormData.binNumber}"))
            R.id.action_MIATCMEP2 -> ObjectIntent.showWpcText(this, arrayOf("${office}TCM${stormData.binNumber}"))
            R.id.action_MIATCDEP2 -> ObjectIntent.showWpcText(this, arrayOf("${office}TCD${stormData.binNumber}"))
            R.id.action_MIAPWSEP2 -> ObjectIntent.showWpcText(this, arrayOf("${office}PWS${stormData.binNumber}"))
            R.id.action_mute_notification -> UtilityNotificationNhc.muteNotification(this, stormData.id)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
