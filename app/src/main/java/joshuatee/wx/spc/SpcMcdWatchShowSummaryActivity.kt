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

import android.os.Bundle
import android.graphics.Bitmap
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.CardText
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.TextSize
import joshuatee.wx.parseColumn
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To

class SpcMcdWatchShowSummaryActivity : BaseActivity() {

    //
    // show a summary of  MCD or Watch, tap on the image to see more detail (text)
    //
    // Arguments (only available in SPC Tab)
    // - "wat" or "mcd"
    //

    companion object {
        const val NO = ""
    }

    private var number = ""
    private var url = ""
    private var patternStr = ""
    private var nothingPresentStr = ""
    private var activityLabel = ""
    private var titleString = ""
    private var product = ""
    private val bitmaps = mutableListOf<Bitmap>()
    private val mcdNumbers = mutableListOf<String>()
    private var polygonType = PolygonType.WATCH
    private lateinit var box: VBox
    private var mcdList = listOf<String>()
    private var downloadTimer = DownloadTimer("MCD_WATCH_SUMMARY_ACTIVITY")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcmcdsummary, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcmcdsummary, false)
        box = VBox.fromResource(this)
        number = intent.getStringArrayExtra(NO)!![0]
        if (number.contains("wat")) {
            number = number.replace("w", "")
            url = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/"
            patternStr = "[om] Watch #([0-9]*?)</a>"
            nothingPresentStr = "No active watches"
            activityLabel = "Watches"
            product = "SPCWAT$number"
            polygonType = PolygonType.WATCH
        } else {
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

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded()) {
            FutureVoid(::download, ::update)
        }
    }

    private fun download() {
        mcdList = url.getHtml().parseColumn(patternStr)
        mcdList.forEach {
            if (number.contains("at")) {
                val mcdNumber = To.stringPadLeftZeros(To.int(it), 4)
                val imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/ww" + mcdNumber + "_radar.gif"
                mcdNumbers.add(mcdNumber)
                bitmaps.add(imgUrl.getImage())
            } else {
                val imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/mcd$it.png"
                mcdNumbers.add(it)
                bitmaps.add(imgUrl.getImage())
            }
        }
    }

    private fun update() {
        box.removeChildren()
        mcdList.indices.forEach { index ->
            val image = Image(this, bitmaps[index])
            box.addWidget(image)
            image.connect { Route.mcd(this, mcdNumbers[index], polygonType.toString()) }
        }
        titleString = "$activityLabel " + mcdNumbers.toString().replace("[{}]".toRegex(), "").replace("\\[|\\]".toRegex(), "").replace("w", "")
        title = titleString
        if (mcdList.isEmpty()) {
            val nothingCard = CardText(this, nothingPresentStr, TextSize.MEDIUM)
            box.addWidget(nothingCard)
            nothingCard.setPaddingAmount(UIPreferences.padding)
            nothingCard.setTextColor(UIPreferences.textHighlightColor)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "SPC Fire Weather Outlooks", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
