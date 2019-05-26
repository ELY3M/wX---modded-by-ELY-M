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

package joshuatee.wx.nhc

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityString

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent

class ObjectNhc(val context: Context, private val dynamicview: LinearLayout) {

    private val atlSumList = mutableListOf<String>()
    private val atlLinkList = mutableListOf<String>()
    private val atlImg1List = mutableListOf<String>()
    private val atlImg2List = mutableListOf<String>()
    private val atlWalletList = mutableListOf<String>()
    private val atlTitleList = mutableListOf<String>()
    private val pacSumList = mutableListOf<String>()
    private val pacLinkList = mutableListOf<String>()
    private val pacImg1List = mutableListOf<String>()
    private val pacImg2List = mutableListOf<String>()
    private val pacWalletList = mutableListOf<String>()
    private val pacTitleList = mutableListOf<String>()
    private val bitmaps = mutableListOf<Bitmap>()
    private var cNotif: ObjectCardText? = null
    private val cardNotifHeaderText = "Currently blocked storm notifications, tap this text to clear all blocks "
    var html: String = ""

    fun getData() {
        listOf(
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_0d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_2d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_5d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_0d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_2d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_5d0.png"
        ).forEach { bitmaps.add(it.getImage()) }
        var dataRet: ObjectNhcStormInfo
        (1 until 6).forEach {
            dataRet = UtilityNhc.getHurricaneInfo("${MyApplication.nwsNhcWebsitePrefix}/nhc_at" + it.toString() + ".xml")
            if (dataRet.title != "") {
                atlSumList.add(dataRet.summary)
                atlLinkList.add(UtilityString.getNwsPre(dataRet.url))
                atlImg1List.add(dataRet.img1)
                atlImg2List.add(dataRet.img2)
                atlWalletList.add(dataRet.wallet)
                atlTitleList.add(dataRet.title.replace("NHC Atlantic Wallet", ""))
            }
        }
        (1 until 6).forEach {
            dataRet = UtilityNhc.getHurricaneInfo("${MyApplication.nwsNhcWebsitePrefix}/nhc_ep" + it.toString() + ".xml")
            if (dataRet.title != "") {
                pacSumList.add(dataRet.summary)
                pacLinkList.add(UtilityString.getNwsPre(dataRet.url))
                pacImg1List.add(dataRet.img1)
                pacImg2List.add(dataRet.img2)
                pacWalletList.add(dataRet.wallet)
                pacTitleList.add(dataRet.title.replace("NHC Eastern Pacific Wallet", ""))
            }
        }
    }

    fun showData() {
        dynamicview.removeAllViewsInLayout()
        html = ""
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        cNotif = ObjectCardText(context, cardNotifHeaderText + muteStr)
        dynamicview.addView(cNotif?.card)
        cNotif?.setOnClickListener(View.OnClickListener { clearNHCNotifBlock() })
        if (muteStr != "") {
            cNotif?.setVisibility(View.VISIBLE)
        } else {
            cNotif?.setVisibility(View.GONE)
        }
        if (atlSumList.size < 1) {
            val noAtl = "There are no tropical cyclones in the Atlantic at this time."
            ObjectCardText(context, dynamicview, noAtl)
            html = noAtl
        } else {
            atlSumList.indices.forEach { k ->
                if (atlImg1List[k] != "") {
                    val objStormData = ObjectNhcStormDetails(atlSumList[k])
                    val cAtlData = ObjectCardNhcStormReportItem(context, dynamicview, objStormData)
                    html += atlSumList[k]
                    val url = atlLinkList[k]
                    val imgUrl1 = atlImg1List[k]
                    val imgUrl2 = atlImg2List[k]
                    val title = atlTitleList[k]
                    val wallet = atlWalletList[k]
                    cAtlData.setListener(View.OnClickListener {
                        ObjectIntent(
                                context,
                                NhcStormActivity::class.java,
                                NhcStormActivity.URL,
                                arrayOf(url, title, "nosound", imgUrl1, imgUrl2, wallet)
                        )
                    })
                }
            }
        }
        if (pacSumList.size < 1) {
            val noPac = "There are no tropical cyclones in the Eastern Pacific at this time."
            ObjectCardText(context, dynamicview, noPac)
            html += noPac
        } else {
            pacSumList.indices.forEach { k ->
                if (pacImg1List[k] != "") {
                    val objStormData = ObjectNhcStormDetails(pacSumList[k])
                    val cPacData = ObjectCardNhcStormReportItem(context, dynamicview, objStormData)
                    //val cPac = ObjectCardText(context, dynamicview, Utility.fromHtml(pacSumList[k]))
                    html += pacSumList[k]
                    val url = pacLinkList[k]
                    val imgUrl1 = pacImg1List[k]
                    val imgUrl2 = pacImg2List[k]
                    val title = pacTitleList[k]
                    val wallet = pacWalletList[k]
                    cPacData.setListener(View.OnClickListener {
                        ObjectIntent(
                                context,
                                NhcStormActivity::class.java,
                                NhcStormActivity.URL,
                                arrayOf(url, title, "nosound", imgUrl1, imgUrl2, wallet)
                        )
                    })
                }
            }
        }
        showTwoBitmaps()
    }

    private fun showTwoBitmaps() {
        bitmaps.forEach { ObjectCardImage(context, dynamicview, it) }
    }

    private fun clearNHCNotifBlock() {
        Utility.writePref(context, "NOTIF_NHC_MUTE", "")
        if (cNotif != null)
            cNotif!!.setVisibility(View.GONE)
    }

    fun handleRestartForNotif() {
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        if (cNotif != null) {
            if (muteStr != "") {
                cNotif!!.setText(cardNotifHeaderText + muteStr)
                cNotif!!.setVisibility(View.VISIBLE)
            } else {
                cNotif!!.setVisibility(View.GONE)
            }
        }
    }
}


