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

package joshuatee.wx.wpc

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
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class WpcRainfallForecastActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // show a rainfall outlook for a specific day
    //
    // Arguments
    //
    // 1: day
    //

    companion object { const val NUMBER = "" }

    private var textProduct = ""
    private var imageUrl = ""
    private lateinit var image: Image
    private lateinit var cardText: CardText
    private lateinit var box: VBox
    private var tabletInLandscape = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcmcdshowdetail)
        val arguments = intent.getStringArrayExtra(NUMBER)!!
        val dayIndex = To.int(arguments[0])
        textProduct = UtilityWpcRainfallForecast.productCode[dayIndex]
        imageUrl = UtilityWpcRainfallForecast.urls[dayIndex]
        setTitle("Day " + (dayIndex + 1).toString() + " Excessive Rainfall Discussion", textProduct)
        box = VBox.fromResource(this)
        objectToolbarBottom.hideRadar()
        objectToolbarBottom.connect(this)
        tabletInLandscape = UtilityUI.isTablet() && UtilityUI.isLandScape(this)
        image = if (tabletInLandscape) {
            box.makeHorizontal()
            Image(this, box, UtilityImg.getBlankBitmap(), 2)
        } else {
            Image(this, box)
        }
        cardText = CardText(this, box, toolbar, toolbarBottom)
        getContent()
    }

    private fun getContent() {
        FutureText(this, textProduct, cardText::setText1)
        FutureBytes(this, imageUrl, ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        if (tabletInLandscape) {
            image.set(bitmap, 2)
        } else {
            image.set(bitmap)
        }
        image.connect { Route.image(this, arrayOf(imageUrl, textProduct, "true")) }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, cardText.text, textProduct, textProduct)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.bitmap(this, textProduct, image.bitmap, cardText.text)
            R.id.action_share_text -> UtilityShare.text(this, textProduct, cardText.text)
            R.id.action_share_url -> UtilityShare.text(this, textProduct, textProduct)
            R.id.action_share_image -> UtilityShare.bitmap(this, textProduct, image.bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
