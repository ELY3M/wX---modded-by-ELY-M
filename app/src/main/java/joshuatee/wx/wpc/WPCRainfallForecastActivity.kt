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

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityDownload
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
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var activityArguments: Array<String>
    private lateinit var objectCardImage: ObjectCardImage
    private lateinit var objectCardText: ObjectCardText
    private lateinit var linearLayout: LinearLayout
    private var tabletInLandscape = false
    private var html = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcmcdshowdetail)
        linearLayout = findViewById(R.id.linearLayout)
        toolbarBottom.setOnMenuItemClickListener(this)
        tabletInLandscape = UtilityUI.isTablet() && UtilityUI.isLandScape(this)
        if (tabletInLandscape) {
            linearLayout.orientation = LinearLayout.HORIZONTAL
            objectCardImage = ObjectCardImage(this, linearLayout, UtilityImg.getBlankBitmap(), 2)
        } else {
            objectCardImage = ObjectCardImage(this, linearLayout)
        }
        objectCardText = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        activityArguments = intent.getStringArrayExtra(NUMBER)!!
        textProduct = activityArguments[0]
        imageUrl = activityArguments[1]
        title = "Day " + activityArguments[2] + " Excessive Rainfall Discussion"
        toolbar.subtitle = textProduct
        getContent()
    }

    private fun getContent() {
        FutureVoid(this, { html = UtilityDownload.getTextProduct(this@WpcRainfallForecastActivity, textProduct) }, ::showText)
        FutureVoid(this, { bitmap = imageUrl.getImage()}, ::showImage)
    }

    private fun showText() {
        objectCardText.text = html
        UtilityTts.conditionalPlay(activityArguments, 1, applicationContext, objectCardText.text, textProduct)
    }

    private fun showImage() {
        if (tabletInLandscape) {
            objectCardImage.setImage(bitmap, 2)
        } else {
            objectCardImage.setImage(bitmap)
        }
        objectCardImage.setOnClickListener {
            ObjectIntent.showImage(this@WpcRainfallForecastActivity, arrayOf(imageUrl, textProduct, "true"))
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objectCardText.text, textProduct, textProduct)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.bitmap(this, this, textProduct, bitmap, objectCardText.text)
            R.id.action_share_text -> UtilityShare.text(this, textProduct, objectCardText.text)
            R.id.action_share_url -> UtilityShare.text(this, textProduct, textProduct)
            R.id.action_share_image -> UtilityShare.bitmap(this, this, textProduct, bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
