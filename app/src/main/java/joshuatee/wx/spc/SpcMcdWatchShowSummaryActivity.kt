/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcMcdWatchShowSummaryActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // show a summary of  MCD or a specific MCD, long press on image to save location
    //
    // Arguments
    // - MCD/Wat number

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
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

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.spcmcdshowdetail
        )
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
        number = intent.getStringArrayExtra(NO)[0]
        if (number.contains("wat")) {
            number = number.replace("w", "")
            imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" + number + "_radar.gif"
            textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww$number.html"
            url = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/"
            patternStr = "[om] Watch #([0-9]*?)</a>"
            nothingPresentStr = "No active watches"
            activityLabel = "Watches"
            product = "SPCWAT$number"
            polygonType = PolygonType.WATCH
        } else {
            imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd$number.gif"
            textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md$number.html"
            url = "${MyApplication.nwsSPCwebsitePrefix}/products/md/"
            patternStr = "<strong><a href=./products/md/md.....html.>Mesoscale Discussion #(.*?)</a></strong>"
            nothingPresentStr = "No active MCDs"
            activityLabel = "MCDs"
            product = "SPCMCD$number"
            polygonType = PolygonType.MCD
        }
        title = activityLabel
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var mcdList = listOf<String>()
        withContext(Dispatchers.IO) {
            mcdList = url.getHtml().parseColumn(patternStr)
            mcdList.forEach {
                if (number.contains("at")) {
                    val mcdNo2 = String.format("%4s", it).replace(' ', '0')
                    imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" + mcdNo2 + "_radar.gif"
                    mcdNumbers.add(mcdNo2)
                } else {
                    imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd$it.gif"
                    mcdNumbers.add(it)
                }
                bitmaps.add(imgUrl.getImage())
            }
            if (mcdList.size == 1) {
                if (number.contains("at")) {
                    textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/w" +
                            mcdNumbers[0] + ".html"
                    titleString = "Watch " + mcdNumbers[0].replace("w", "")
                    product = "SPCWAT" + mcdNumbers[0].replace("w", "")
                } else {
                    textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md" +
                            mcdNumbers[0] + ".html"
                    titleString = "MCD " + mcdNumbers[0]
                    product = "SPCMCD" + mcdNumbers[0]
                }
                text = UtilityDownload.getTextProduct(this@SpcMcdWatchShowSummaryActivity, product)
            }
        }
        mcdList.indices.forEach { mcdIndex ->
            val card = ObjectCardImage(this@SpcMcdWatchShowSummaryActivity, ll, bitmaps[mcdIndex])
            card.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        this@SpcMcdWatchShowSummaryActivity,
                        SpcMcdWatchShowActivity::class.java,
                        SpcMcdWatchShowActivity.NO,
                        arrayOf(mcdNumbers[mcdIndex], "", polygonType.toString())
                )
            })
            if (mcdList.size == 1) {
                registerForContextMenu(card.img)
            }
        }
        if (mcdList.size == 1) {
            val wfoStr = text.parse("ATTN...WFO...(.*?)... ")
            wfos = wfoStr.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
            ObjectCardText(this@SpcMcdWatchShowSummaryActivity, ll, toolbar, toolbarBottom, Utility.fromHtml(text))
            title = titleString
            if (!number.contains("at")) {
                toolbar.subtitle = text.parse("Areas affected...(.*?)<BR>")
            }
            miAll.isVisible = true
            miText.isVisible = true
            miUrl.isVisible = true
            miImage.isVisible = true
        } else {
            titleString =
                    "$activityLabel " + mcdNumbers.toString().replace(
                            "[{}]".toRegex(),
                            ""
                    ).replace("\\[|\\]".toRegex(), "").replace("w", "")
            miAll.isVisible = true
            title = titleString
        }
        if (mcdList.isEmpty()) {
            ObjectCardText(this@SpcMcdWatchShowSummaryActivity, ll, toolbar, toolbarBottom, nothingPresentStr)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        wfos.filter{ !it.contains("<BR>") }.forEach {
            menu.add(0, v.id, 0, "Add location: $it - " + Utility.readPref(
                    this,
                    "NWS_LOCATION_$it",
                    ""
            )
            )
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        wfos.filter { item.title.toString().contains(it) }.forEach {
            UtilityLocation.saveLocationForMcd(it, this@SpcMcdWatchShowSummaryActivity, ll, uiDispatcher)
        }
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, text, number, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> {
                if (bitmaps.size > 1)
                    UtilityShare.shareText(this, titleString, "", bitmaps)
                else if (bitmaps.size == 1)
                    UtilityShare.shareText(this, titleString, Utility.fromHtml(text), bitmaps[0])
            }
            R.id.action_share_text -> UtilityShare.shareText(
                    this,
                    titleString,
                    Utility.fromHtml(text)
            )
            R.id.action_share_url -> UtilityShare.shareText(this, titleString, textUrl)
            R.id.action_share_image -> UtilityShare.shareBitmap(this, titleString, bitmaps[0])
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

