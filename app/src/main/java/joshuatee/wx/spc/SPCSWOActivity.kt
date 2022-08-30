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

package joshuatee.wx.spc

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.FutureText
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityImg

class SpcSwoActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // show SWO for Day X as specified in extra
    // Arguments
    //
    // 1: day
    //

    companion object { const val NUMBER = "" }

    private val bitmaps = MutableList(5) { UtilityImg.getBlankBitmap() }
    private var urls = listOf<String>()
    private lateinit var arguments: Array<String>
    private var day = ""
    private var playlistProd = ""
    private lateinit var cardText: CardText
    private lateinit var box: VBox
    private val images = mutableListOf<Image>()
    private var imagesPerRow = 2
    private var imageLabel = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcswo)
        arguments = intent.getStringArrayExtra(NUMBER)!!
        day = arguments[0]
        title = "Day $day Convective Outlook"
        box = VBox.fromResource(this)
        if (UtilityUI.isLandScape(this) && UtilityUI.isTablet()) {
            imagesPerRow = 4
        }
        objectToolbarBottom.connect(this)
        val boxRows = mutableListOf<HBox>()
        (0..4).forEach {
            if (it % imagesPerRow == 0) {
                boxRows.add(HBox(this, box.get()))
            }
            images.add(Image(this, boxRows.last()))
            images[it].visibility = View.GONE
        }
        cardText = CardText(this, box, toolbar, toolbarBottom)
        setupShareMenu()
        getContent()
    }

    private fun setupShareMenu() {
        // FIXME TODO refactor
        val menu = toolbarBottom.menu
        val miTornado = menu.findItem(R.id.action_share_tornado)
        val miHail = menu.findItem(R.id.action_share_hail)
        val miWind = menu.findItem(R.id.action_share_wind)
        val miCategorical = menu.findItem(R.id.action_share_categorical)
        val miProbabilistic = menu.findItem(R.id.action_share_probabilistic)
        val miDay4Img = menu.findItem(R.id.action_share_d4)
        val miDay5Img = menu.findItem(R.id.action_share_d5)
        val miDay6Img = menu.findItem(R.id.action_share_d6)
        val miDay7Img = menu.findItem(R.id.action_share_d7)
        val miDay8Img = menu.findItem(R.id.action_share_d8)
        listOf(miDay4Img, miDay5Img, miDay6Img, miDay7Img, miDay8Img).forEach {
            it.isVisible = false
        }
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
            playlistProd = "swody$day"
        }
        if (day == "4-8") {
            val state = menu.findItem(R.id.action_state_graphics)
            state.isVisible = false
            listOf(miDay4Img, miDay5Img, miDay6Img, miDay7Img, miDay8Img).forEach {
                it.isVisible = true
            }
        }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        var textUrl = "SWODY$day"
        imageLabel = "Day $day Convective Outlook"
        if (day == "4-8") {
            textUrl = "SWOD48"
        }
        FutureVoid(this, ::downloadImages) {}
        FutureText(this, textUrl, ::showText)
    }

    private fun showText(html: String) {
        cardText.text = html
        toolbar.subtitle = html.parse("(Valid.*?Z - [0-9]{6}Z)")
        if (arguments.size > 1 && arguments[1] == "sound") {
            UtilityTts.synthesizeTextAndPlay(applicationContext, html, "spcswo")
        }
    }

    private fun downloadImages() {
        urls = UtilitySpcSwo.getUrls(day)
        urls.indices.forEach {
            FutureVoid(this, { bitmaps[it] = urls[it].getImage() }, { showImage(it) })
        }
    }

    private fun showImage(index: Int) {
        images[index].visibility = View.VISIBLE
        images[index].set(bitmaps[index], imagesPerRow)
        images[index].connect { Route.image(this, urls[index], imageLabel) }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(cardText.text)
        if (audioPlayMenu(item.itemId, cardText.text, playlistProd, playlistProd)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.text(this, "Day $day Convective Outlook", textToShare, bitmaps)
            R.id.action_share_text -> UtilityShare.text(this, "Day $day Convective Outlook - Text", textToShare)
            R.id.action_share_tornado -> if (bitmaps.size > 1) UtilityShare.bitmap(this, "Day $day Convective Outlook - Tornado", bitmaps[1])
            R.id.action_share_hail -> if (bitmaps.size > 2) UtilityShare.bitmap(this, "Day $day Convective Outlook - Hail", bitmaps[2])
            R.id.action_share_wind -> if (bitmaps.size > 3) UtilityShare.bitmap(this, "Day $day Convective Outlook - Wind", bitmaps[3])
            R.id.action_share_categorical -> if (bitmaps.isNotEmpty()) UtilityShare.bitmap(this, "Day $day Convective Outlook - Categorical", bitmaps[0])
            R.id.action_share_probabilistic -> if (bitmaps.size > 1) UtilityShare.bitmap(this, "Day $day Convective Outlook - Probabilistic", bitmaps[1])
            R.id.action_share_d4 -> if (bitmaps.isNotEmpty()) UtilityShare.bitmap(this, "Day " + "4" + " Convective Outlook - Image", bitmaps[0])
            R.id.action_share_d5 -> if (bitmaps.size > 1) UtilityShare.bitmap(this, "Day " + "5" + " Convective Outlook - Image", bitmaps[1])
            R.id.action_share_d6 -> if (bitmaps.size > 2) UtilityShare.bitmap(this, "Day " + "6" + " Convective Outlook - Image", bitmaps[2])
            R.id.action_share_d7 -> if (bitmaps.size > 3) UtilityShare.bitmap(this, "Day " + "7" + " Convective Outlook - Image", bitmaps[3])
            R.id.action_share_d8 -> if (bitmaps.size > 4) UtilityShare.bitmap(this, "Day " + "8" + " Convective Outlook - Image", bitmaps[4])
            R.id.action_state_graphics -> Route(this, SpcSwoStateGraphicsActivity::class.java, SpcSwoStateGraphicsActivity.NO, arrayOf(day, ""))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
