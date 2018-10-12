/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.graphics.Bitmap
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo
import android.widget.LinearLayout
import android.widget.TextView

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent

class SPCMCDWShowSummaryActivity : AudioPlayActivity(), OnMenuItemClickListener {

    companion object {
        const val NO: String = ""
    }

    private var no = ""
    private var imgUrl = ""
    private var textUrl = ""
    private var url = ""
    private var patternStr = ""
    private var nothingPresentStr = ""
    private var activityLabel = ""
    private var text = ""
    private var title = ""
    private var prod = ""
    private var wfoArr = listOf<String>()
    private val bitmapArr = mutableListOf<Bitmap>()
    private val mcdNoArr = mutableListOf<String>()
    private lateinit var miAll: MenuItem
    private lateinit var miText: MenuItem
    private lateinit var miUrl: MenuItem
    private lateinit var miImage: MenuItem
    private lateinit var miTest: MenuItem
    private lateinit var objCard: ObjectCard
    private lateinit var ll: LinearLayout
    private lateinit var polygonType: PolygonType
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcmcdwshow_summary, R.menu.spcmcdshowdetail)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        objCard = ObjectCard(this, R.id.cv1)
        ll = findViewById(R.id.ll)
        val menu = toolbarBottom.menu
        miAll = menu.findItem(R.id.action_share_all)
        miText = menu.findItem(R.id.action_share_text)
        miUrl = menu.findItem(R.id.action_share_url)
        miImage = menu.findItem(R.id.action_share_image)
        miTest = menu.findItem(R.id.action_test)
        miAll.isVisible = false
        miText.isVisible = false
        miUrl.isVisible = false
        miImage.isVisible = false
        val turl = intent.getStringArrayExtra(NO)
        no = turl[0]
        if (no.contains("wat")) {
            no = no.replace("w", "")
            imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" + no + "_radar.gif"
            textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww$no.html"
            url = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/"
            patternStr = "[om] Watch #([0-9]*?)</a>"
            nothingPresentStr = "No active watches"
            activityLabel = "Watches"
            prod = "SPCWAT$no"
            polygonType = PolygonType.WATCH
        } else {
            imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd$no.gif"
            textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md$no.html"
            url = "${MyApplication.nwsSPCwebsitePrefix}/products/md/"
            patternStr = "<strong><a href=./products/md/md.....html.>Mesoscale Discussion #(.*?)</a></strong>"
            nothingPresentStr = "No active MCDs"
            activityLabel = "MCDs"
            prod = "SPCMCD$no"
            polygonType = PolygonType.MCD
        }
        setTitle(activityLabel)
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        internal var sigHtmlTmp = ""
        internal var mcdList = listOf<String>()

        override fun doInBackground(vararg params: String): String {
            try {
                sigHtmlTmp = url.getHtml()
                mcdList = sigHtmlTmp.parseColumn(patternStr)
                mcdList.forEach {
                    if (no.contains("at")) {
                        val mcdNo2 = String.format("%4s", it).replace(' ', '0')
                        imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" + mcdNo2 + "_radar.gif"
                        mcdNoArr.add(mcdNo2)
                    } else {
                        imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd$it.gif"
                        mcdNoArr.add(it)
                    }
                    bitmapArr.add(imgUrl.getImage())
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            if (mcdList.size == 1) {
                if (no.contains("at")) {
                    textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/w" + mcdNoArr[0] + ".html"
                    title = "Watch " + mcdNoArr[0].replace("w", "")
                    prod = "SPCWAT" + mcdNoArr[0].replace("w", "")
                } else {
                    textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md" + mcdNoArr[0] + ".html"
                    title = "MCD " + mcdNoArr[0]
                    prod = "SPCMCD" + mcdNoArr[0]
                }
                text = UtilityDownload.getTextProduct(contextg, prod)
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            mcdList.indices.forEach { mcdIndex ->
                val card = ObjectCardImage(contextg)
                card.setImage(bitmapArr[mcdIndex])
                ll.addView(card.card)
                card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, SPCMCDWShowActivity::class.java, SPCMCDWShowActivity.NO, arrayOf(mcdNoArr[mcdIndex], "", polygonType.toString())) })
                if (mcdList.size == 1) {
                    registerForContextMenu(card.img)
                }
            }
            if (mcdList.size == 1) {
                val wfoStr = text.parse("ATTN...WFO...(.*?)... ")
                wfoArr = wfoStr.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
                val card2 = ObjectCardText(contextg)
                card2.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
                card2.setText(Utility.fromHtml(text))
                ll.addView(card2.card)
                setTitle(title)
                if (!no.contains("at")) {
                    toolbar.subtitle = text.parse("Areas affected...(.*?)<BR>")
                }
                miAll.isVisible = true
                miText.isVisible = true
                miUrl.isVisible = true
                miImage.isVisible = true
            } else {
                setTitle(activityLabel + " " + mcdNoArr.toString().replace("[{}]".toRegex(), "").replace("\\[|\\]".toRegex(), "").replace("w", ""))
                miAll.isVisible = true
            }
            val tv: TextView = findViewById(R.id.tv)
            if (mcdList.isEmpty()) {
                tv.text = nothingPresentStr
                miTest.isVisible = false
            } else {
                tv.visibility = View.GONE
                objCard.setVisibility(View.GONE)
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        (0 until wfoArr.size - 1).forEach {
            menu.add(0, v.id, 0, "Add location: " + wfoArr[it] + " - " + Utility.readPref(this, "NWS_LOCATION_" + wfoArr[it], ""))
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemStr = item.title.toString()
        (0 until wfoArr.size - 1)
                .filter { itemStr.contains(wfoArr[it]) }
                .forEach { saveLocation(wfoArr[it]) }
        return true
    }

    private fun saveLocation(nws_office: String) {
        SaveLoc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nws_office)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SaveLoc : AsyncTask<String, String, String>() {

        internal var toastStr = ""

        override fun doInBackground(vararg params: String): String {
            val locNumIntCurrent = Location.numLocations + 1
            val locNumToSaveStr = locNumIntCurrent.toString()
            val loc = Utility.readPref(contextg, "NWS_LOCATION_" + params[0], "")
            val addrSend = loc.replace(" ", "+")
            val xyStr = UtilityLocation.getXYFromAddressOSM(addrSend)
            toastStr = Location.locationSave(contextg, locNumToSaveStr, xyStr[0], xyStr[1], loc)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityUI.makeSnackBar(ll, toastStr)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, text, no, prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> {
                if (bitmapArr.size > 1)
                    UtilityShare.shareText(this, title, "", bitmapArr)
                else if (bitmapArr.size == 1)
                    UtilityShare.shareText(this, title, Utility.fromHtml(text), bitmapArr[0])
            }
            R.id.action_share_text -> UtilityShare.shareText(this, title, Utility.fromHtml(text))
            R.id.action_share_url -> UtilityShare.shareText(this, title, textUrl)
            R.id.action_share_image -> UtilityShare.shareBitmap(this, title, bitmapArr[0])
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

