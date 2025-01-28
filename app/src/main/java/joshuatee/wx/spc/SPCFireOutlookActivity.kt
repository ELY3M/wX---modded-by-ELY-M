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
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.objects.FutureText
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SpcFireOutlookActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // show a fire outlook for a specific day
    //
    // Arguments
    //
    // 1: day
    //

    companion object {
        const val NUMBER = ""
    }

    private var product = ""
    private var imageUrl = ""
    private lateinit var image: Image
    private lateinit var cardText: CardText
    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.spc_fire_weather_forecast
        )
        val arguments = intent.getStringArrayExtra(NUMBER)!!
        val dayIndex = To.int(arguments[0])
        product = UtilitySpcFireOutlook.textProducts[dayIndex]
        imageUrl = UtilitySpcFireOutlook.urls[dayIndex]
        setTitle("Fire Weather Outlook", "SPC $product")
        setupUI()
        getContent()
    }

    private fun setupUI() {
        box = VBox.fromResource(this)
        objectToolbarBottom.connect(this)
        image = if (tabletInLandscape) {
            box.makeHorizontal()
            Image(this, UtilityImg.getBlankBitmap(), 2)
        } else {
            Image(this)
        }
        cardText = CardText(this, toolbar, toolbarBottom)
        box.addWidget(image)
        box.addWidget(cardText)
    }

    private fun getContent() {
        FutureBytes(imageUrl, ::showImage)
        FutureText(this, product, cardText::setText1)
    }

    private fun showImage(bitmap: Bitmap) {
        if (tabletInLandscape) {
            image.set(bitmap, 2)
        } else {
            image.set(bitmap)
        }
        image.connect { Route.image(this, imageUrl, product) }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, cardText.text, product, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.bitmap(this, product, image, cardText.text)
            R.id.action_share_text -> UtilityShare.text(this, product, cardText.text)
            R.id.action_share_url -> UtilityShare.text(this, product, product)
            R.id.action_share_image -> UtilityShare.bitmap(this, product, image)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
