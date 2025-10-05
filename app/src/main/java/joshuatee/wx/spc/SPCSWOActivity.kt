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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.getImage
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureText
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.parse
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.HBox
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityImg

class SpcSwoActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // show SWO for Day X as specified in extra
    // Arguments
    //
    // 1: day
    // 2: "sound" (optional)
    //

    companion object {
        const val NUMBER = ""
    }

    private val bitmaps = MutableList(5) { UtilityImg.getBlankBitmap() }
    private var urls = listOf<String>()
    private lateinit var arguments: Array<String>
    private var day = ""
    private var playlistProd = ""
    private lateinit var cardText: CardText
    private lateinit var box: VBox
    private val images = mutableListOf<Image>()
    private var imagesPerRow = 2
    private lateinit var downloadTimer: DownloadTimer

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.spcswo
        )
        arguments = intent.getStringArrayExtra(NUMBER)!!
        day = arguments[0]
        title = "Day $day Convective Outlook"
        downloadTimer = DownloadTimer("ACTIVITY_SPC_SWO_SUMMARY_$day")
        setupUI()
        setupShareMenu()
        getContent()
    }

    private fun setupUI() {
        box = VBox.fromResource(this)
        if (UtilityUI.isLandScape(this) && UtilityUI.isTablet()) {
            imagesPerRow = 4
        }
        objectToolbarBottom.connect(this)
        val boxRows = mutableListOf<HBox>()
        (0..4).forEach {
            if (it % imagesPerRow == 0) {
                boxRows.add(HBox(this, box))
            }
            images.add(Image(this))
            boxRows.last().addWidget(images.last())
            images[it].visibility = View.GONE
        }
        cardText = CardText(this, toolbar, toolbarBottom)
        box.addWidget(cardText)
    }

    private fun setupShareMenu() {
        val miTornado = objectToolbarBottom.find(R.id.action_share_tornado)
        val miHail = objectToolbarBottom.find(R.id.action_share_hail)
        val miWind = objectToolbarBottom.find(R.id.action_share_wind)
        val miCategorical = objectToolbarBottom.find(R.id.action_share_categorical)
        val miProbabilistic = objectToolbarBottom.find(R.id.action_share_probabilistic)
        val miDay4Img = objectToolbarBottom.find(R.id.action_share_d4)
        val miDay5Img = objectToolbarBottom.find(R.id.action_share_d5)
        val miDay6Img = objectToolbarBottom.find(R.id.action_share_d6)
        val miDay7Img = objectToolbarBottom.find(R.id.action_share_d7)
        val miDay8Img = objectToolbarBottom.find(R.id.action_share_d8)
        if (day == "1" || day == "2") {
            miProbabilistic.isVisible = false
        } else {
            miTornado.isVisible = false
            miHail.isVisible = false
            miWind.isVisible = false
        }
        if (day == "4-8") {
            playlistProd = "swod48"
            miProbabilistic.isVisible = false
            miCategorical.isVisible = false
        } else {
            listOf(miDay4Img, miDay5Img, miDay6Img, miDay7Img, miDay8Img).forEach {
                it.isVisible = false
            }
            playlistProd = "swody$day"
        }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded()) {
            val textUrl = if (day == "4-8") {
                "SWOD48"
            } else {
                "SWODY$day"
            }
            FutureVoid(::downloadImages) {}
            FutureText(this, textUrl, ::showText)
        }
    }

    private fun showText(html: String) {
        cardText.text = html
        toolbar.subtitle = html.parse("(Valid.*?Z - [0-9]{6}Z)")
        UtilityTts.conditionalPlay(arguments, 1, applicationContext, html, "spcswo")
    }

    private fun downloadImages() {
        urls = UtilitySpcSwo.getUrls(day)
        urls.indices.forEach {
            FutureVoid({ bitmaps[it] = urls[it].getImage() }, { showImage(it) })
        }
    }

    private fun showImage(index: Int) {
        images[index].visibility = View.VISIBLE
        images[index].set(bitmaps[index], imagesPerRow)
        images[index].connect { Route.image(this, urls[index], "Day $day Convective Outlook") }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(cardText.text)
        if (audioPlayMenu(item.itemId, cardText.text, playlistProd, playlistProd)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.text(
                this,
                "Day $day Convective Outlook",
                textToShare,
                bitmaps
            )

            R.id.action_share_text -> UtilityShare.text(
                this,
                "Day $day Convective Outlook - Text",
                textToShare
            )

            R.id.action_share_tornado -> if (bitmaps.size > 1) UtilityShare.bitmap(
                this,
                "Day $day Convective Outlook - Tornado",
                bitmaps[1]
            )

            R.id.action_share_hail -> if (bitmaps.size > 2) UtilityShare.bitmap(
                this,
                "Day $day Convective Outlook - Hail",
                bitmaps[2]
            )

            R.id.action_share_wind -> if (bitmaps.size > 3) UtilityShare.bitmap(
                this,
                "Day $day Convective Outlook - Wind",
                bitmaps[3]
            )

            R.id.action_share_categorical -> if (bitmaps.isNotEmpty()) UtilityShare.bitmap(
                this,
                "Day $day Convective Outlook - Categorical",
                bitmaps[0]
            )

            R.id.action_share_probabilistic -> if (bitmaps.size > 1) UtilityShare.bitmap(
                this,
                "Day $day Convective Outlook - Probabilistic",
                bitmaps[1]
            )

            R.id.action_share_d4 -> if (bitmaps.isNotEmpty()) UtilityShare.bitmap(
                this,
                "Day " + "4" + " Convective Outlook - Image",
                bitmaps[0]
            )

            R.id.action_share_d5 -> if (bitmaps.size > 1) UtilityShare.bitmap(
                this,
                "Day " + "5" + " Convective Outlook - Image",
                bitmaps[1]
            )

            R.id.action_share_d6 -> if (bitmaps.size > 2) UtilityShare.bitmap(
                this,
                "Day " + "6" + " Convective Outlook - Image",
                bitmaps[2]
            )

            R.id.action_share_d7 -> if (bitmaps.size > 3) UtilityShare.bitmap(
                this,
                "Day " + "7" + " Convective Outlook - Image",
                bitmaps[3]
            )

            R.id.action_share_d8 -> if (bitmaps.size > 4) UtilityShare.bitmap(
                this,
                "Day " + "8" + " Convective Outlook - Image",
                bitmaps[4]
            )

            R.id.action_state_graphics -> Route(
                this,
                SpcSwoStateGraphicsActivity::class.java,
                SpcSwoStateGraphicsActivity.NO,
                arrayOf(day, "")
            )

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
