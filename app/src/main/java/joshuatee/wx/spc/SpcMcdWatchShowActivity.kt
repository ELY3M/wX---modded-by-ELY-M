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
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
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
    private lateinit var arguments: Array<String>
    private lateinit var image: Image
    private lateinit var cardText: CardText
    private lateinit var objectWatchProduct: ObjectWatchProduct
    private lateinit var box: VBox
    private var tabletInLandscape = false

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcmcdshowdetail)
        box = VBox.fromResource(this)
        toolbarBottom.setOnMenuItemClickListener(this)
        tabletInLandscape = UtilityUI.isTablet() && UtilityUI.isLandScape(this)
        image = if (tabletInLandscape) {
            box.makeHorizontal()
            Image(this, box, UtilityImg.getBlankBitmap(), 2)
        } else {
            Image(this, box)
        }
        cardText = CardText(this, box, toolbar, toolbarBottom)
        arguments = intent.getStringArrayExtra(NUMBER)!!
        number = arguments[0]
        when (arguments[2]) {
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
        cardText.text = Utility.fromHtml(objectWatchProduct.text)
        toolbar.subtitle = objectWatchProduct.textForSubtitle
        if (tabletInLandscape) {
            image.set(objectWatchProduct.bitmap, 2)
        } else {
            image.set(objectWatchProduct.bitmap)
        }
        image.connect {
            Route.image(this, arrayOf(objectWatchProduct.imgUrl, objectWatchProduct.title, "true"))
        }
        UtilityTts.conditionalPlay(arguments, 1, applicationContext, objectWatchProduct.text, objectWatchProduct.prod)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objectWatchProduct.text, number, objectWatchProduct.prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_radar -> Route.radarBySite(this, objectWatchProduct.getClosestRadar())
            R.id.action_share_all -> UtilityShare.bitmap(this, objectWatchProduct.title, objectWatchProduct.bitmap, Utility.fromHtml(objectWatchProduct.text))
            R.id.action_share_text -> UtilityShare.text(this, objectWatchProduct.title, Utility.fromHtml(objectWatchProduct.text))
            R.id.action_share_url -> UtilityShare.text(this, objectWatchProduct.title, objectWatchProduct.textUrl)
            R.id.action_share_image -> UtilityShare.bitmap(this, objectWatchProduct.title, objectWatchProduct.bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
