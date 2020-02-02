/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.objects.ObjectIntent
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class WpcMpdShowSummaryActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // Show summary of WPC MPD or show detail of only one is active
    // Closely based off SPC MCD equivalent

    companion object {
        private const val NO = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var imageUrl = ""
    private var url = ""
    private var text = ""
    private var wfos = listOf<String>()
    private var product = ""
    private val bitmaps = mutableListOf<Bitmap>()
    private val mpdNumbers = mutableListOf<String>()
    private var titleString = "MPDs"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.shared_tts)
        toolbarBottom.setOnMenuItemClickListener(this)
        // FIXME make number = intent.getStringArrayExtra(NO)[0]
        val no = intent.getStringExtra(NO)
        imageUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd$no.gif"
        url = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php"
        title = titleString
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        var mpdList = listOf<String>()
        withContext(Dispatchers.IO) {
            mpdList = url.getHtml().parseColumn(RegExp.mpdPattern)
            mpdList.forEach {
                imageUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd$it.gif"
                mpdNumbers.add(it)
                bitmaps.add(imageUrl.getImage())
            }
            if (mpdList.size == 1) {
                imageUrl = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd" + mpdNumbers[0] + ".gif"
                titleString = "MPD " + mpdNumbers[0]
                product = "WPCMPD" + mpdNumbers[0]
                text = UtilityDownload.getTextProduct(this@WpcMpdShowSummaryActivity, product)
            }
        }
        mpdList.indices.forEach { mpdIndex ->
            val card = ObjectCardImage(this@WpcMpdShowSummaryActivity, ll, bitmaps[mpdIndex])
            card.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        this@WpcMpdShowSummaryActivity,
                        SpcMcdWatchShowActivity::class.java,
                        SpcMcdWatchShowActivity.NO,
                        arrayOf(mpdNumbers[mpdIndex], "", PolygonType.MPD.toString())
                )
            })
            if (mpdList.size == 1) {
                registerForContextMenu(card.img)
            }
        }
        if (mpdList.size == 1) {
            val wfoStr = text.parse("ATTN...WFO...(.*?)...<br>")
            wfos = wfoStr.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
            ObjectCardText(this@WpcMpdShowSummaryActivity, ll, toolbar, toolbarBottom, Utility.fromHtml(text))
            title = titleString
            toolbar.subtitle = text.parse("AREAS AFFECTED...(.*?)CONCERNING").replace("<BR>", "")
        }
        if (mpdList.isEmpty()) {
            ObjectCardText(this@WpcMpdShowSummaryActivity, ll, toolbar, toolbarBottom, resources.getString(R.string.wpc_mpd_noactive))
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        wfos.forEach {
            menu.add(0, v.id, 0, "Add location: $it" + " - " + Utility.getWfoSiteName(it)
            )
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        wfos.filter { item.title.toString().contains(it) }.forEach {
            UtilityLocation.saveLocationForMcd(it, this@WpcMpdShowSummaryActivity, ll, uiDispatcher)
        }
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, text, product, product))
            return true
        return when (item.itemId) {
            R.id.action_share -> {
                if (bitmaps.size > 1)
                    UtilityShare.shareText(this, this, titleString, "", bitmaps)
                else if (bitmaps.size == 1)
                    UtilityShare.shareBitmap(this, this, titleString, bitmaps[0], Utility.fromHtml(text))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


