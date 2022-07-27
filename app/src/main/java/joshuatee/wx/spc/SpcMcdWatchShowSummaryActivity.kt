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
import android.graphics.Bitmap
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.CardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.VBox

class SpcMcdWatchShowSummaryActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // show a summary of  MCD or a specific MCD
    //
    // Arguments
    // - MCD/Wat number
    //

    companion object { const val NO = "" }

    private var number = ""
    private var imgUrl = ""
    private var textUrl = ""
    private var url = ""
    private var patternStr = ""
    private var nothingPresentStr = ""
    private var activityLabel = ""
    private var text = ""
    private var titleString = ""
    private var product = ""
    private var wfos = listOf<String>()
    private val bitmaps = mutableListOf<Bitmap>()
    private val mcdNumbers = mutableListOf<String>()
    private lateinit var miAll: MenuItem
    private lateinit var miText: MenuItem
    private lateinit var miUrl: MenuItem
    private lateinit var miImage: MenuItem
    private lateinit var polygonType: PolygonType
    private lateinit var box: VBox
    private var mcdList = listOf<String>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcmcdshowdetail)
        box = VBox.fromResource(this)
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        miAll = menu.findItem(R.id.action_share_all)
        miText = menu.findItem(R.id.action_share_text)
        miUrl = menu.findItem(R.id.action_share_url)
        miImage = menu.findItem(R.id.action_share_image)
        miAll.isVisible = false
        miText.isVisible = false
        miUrl.isVisible = false
        miImage.isVisible = false
        number = intent.getStringArrayExtra(NO)!![0]
        if (number.contains("wat")) {
            number = number.replace("w", "")
            imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/ww" + number + "_radar.gif"
            textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/ww$number.html"
            url = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/"
            patternStr = "[om] Watch #([0-9]*?)</a>"
            nothingPresentStr = "No active watches"
            activityLabel = "Watches"
            product = "SPCWAT$number"
            polygonType = PolygonType.WATCH
        } else {
            imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/mcd$number.gif"
            textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/md$number.html"
            url = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/"
            patternStr = "<strong><a href=./products/md/md.....html.>Mesoscale Discussion #(.*?)</a></strong>"
            nothingPresentStr = "No active MCDs"
            activityLabel = "MCDs"
            product = "SPCMCD$number"
            polygonType = PolygonType.MCD
        }
        title = activityLabel
        getContent()
    }

    private fun getContent() {
        FutureVoid(this, ::download, ::update)
    }

    private fun download() {
        mcdList = url.getHtml().parseColumn(patternStr)
        mcdList.forEach {
            if (number.contains("at")) {
                val mcdNo2 = String.format("%4s", it).replace(' ', '0')
                imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/ww" + mcdNo2 + "_radar.gif"
                mcdNumbers.add(mcdNo2)
            } else {
                imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/mcd$it.gif"
                mcdNumbers.add(it)
            }
            bitmaps.add(imgUrl.getImage())
        }
        if (mcdList.size == 1) {
            if (number.contains("at")) {
                textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/w" + mcdNumbers[0] + ".html"
                titleString = "Watch " + mcdNumbers[0].replace("w", "")
                product = "SPCWAT" + mcdNumbers[0].replace("w", "")
            } else {
                textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/md" + mcdNumbers[0] + ".html"
                titleString = "MCD " + mcdNumbers[0]
                product = "SPCMCD" + mcdNumbers[0]
            }
            text = UtilityDownload.getTextProduct(this, product)
        }
    }

    private fun update() {
        mcdList.indices.forEach { mcdIndex ->
            val image = Image(this, box, bitmaps[mcdIndex])
            image.connect {
                Route.mcd(this, arrayOf(mcdNumbers[mcdIndex], "", polygonType.toString()))
            }
        }
        if (mcdList.size == 1) {
            val wfoStr = text.parse("ATTN...WFO...(.*?)... ")
            wfos = wfoStr.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
            CardText(this, box, toolbar, toolbarBottom, Utility.fromHtml(text))
            title = titleString
            if (!number.contains("at")) {
                toolbar.subtitle = text.parse("Areas affected...(.*?)<BR>")
            }
            miAll.isVisible = true
            miText.isVisible = true
            miUrl.isVisible = true
            miImage.isVisible = true
        } else {
            titleString = "$activityLabel " + mcdNumbers.toString().replace("[{}]".toRegex(), "").replace("\\[|\\]".toRegex(), "").replace("w", "")
            miAll.isVisible = true
            title = titleString
        }
        if (mcdList.isEmpty()) {
            CardText(this, box, toolbar, toolbarBottom, nothingPresentStr)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, text, number, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> {
                if (bitmaps.size > 1)
                    UtilityShare.text(this, titleString, "", bitmaps)
                else if (bitmaps.size == 1)
                    UtilityShare.bitmap(this, titleString, bitmaps[0], Utility.fromHtml(text))
            }
            R.id.action_share_text -> UtilityShare.text(this, titleString, Utility.fromHtml(text))
            R.id.action_share_url -> UtilityShare.text(this, titleString, textUrl)
            R.id.action_share_image -> UtilityShare.bitmap(this, titleString, bitmaps[0])
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
