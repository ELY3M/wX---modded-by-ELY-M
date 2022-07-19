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

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SpcMcdWatchShowActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // show a specific MCD, Watch, or MPD
    //
    // Arguments
    //
    // 1: number of MCD, WAT, or MPD such as 0403
    //

    companion object { const val NUMBER = "" }

    private var number = ""
    private lateinit var activityArguments: Array<String>
    private lateinit var objectCardImage: ObjectCardImage
    private lateinit var objectCardText: ObjectCardText
    private lateinit var objectWatchProduct: ObjectWatchProduct
    private lateinit var linearLayout: LinearLayout
    private var tabletInLandscape = false

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
        number = activityArguments[0]
        when (activityArguments[2]) {
            "MCD" -> objectWatchProduct = ObjectWatchProduct(PolygonType.MCD, number)
            "WATCH" -> objectWatchProduct = ObjectWatchProduct(PolygonType.WATCH, number)
            "MPD" -> objectWatchProduct = ObjectWatchProduct(PolygonType.MPD, number)
            else -> {}
        }
        title = objectWatchProduct.title
        getContent()
    }

    private fun getContent() {
        FutureVoid(this, ::download, ::update)
    }

    private fun download() {
        objectWatchProduct.getData(this@SpcMcdWatchShowActivity)
    }

    private fun update() {
        objectCardText.text = Utility.fromHtml(objectWatchProduct.text)
        toolbar.subtitle = objectWatchProduct.textForSubtitle
        if (tabletInLandscape) {
            objectCardImage.setImage(objectWatchProduct.bitmap, 2)
        } else {
            objectCardImage.setImage(objectWatchProduct.bitmap)
        }
        objectCardImage.setOnClickListener {
            ObjectIntent.showImage(this@SpcMcdWatchShowActivity, arrayOf(objectWatchProduct.imgUrl, objectWatchProduct.title, "true"))
        }
        UtilityTts.conditionalPlay(activityArguments, 1, applicationContext, objectWatchProduct.text, objectWatchProduct.prod)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objectWatchProduct.text, number, objectWatchProduct.prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_radar -> ObjectIntent.showRadarBySite(this, objectWatchProduct.getClosestRadar())
            R.id.action_share_all -> UtilityShare.bitmap(this, this, objectWatchProduct.title, objectWatchProduct.bitmap, Utility.fromHtml(objectWatchProduct.text))
            R.id.action_share_text -> UtilityShare.text(this, objectWatchProduct.title, Utility.fromHtml(objectWatchProduct.text))
            R.id.action_share_url -> UtilityShare.text(this, objectWatchProduct.title, objectWatchProduct.textUrl)
            R.id.action_share_image -> UtilityShare.bitmap(this, this, objectWatchProduct.title, objectWatchProduct.bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
