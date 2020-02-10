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
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.ui.UtilityUI

class ObjectNhc(val context: Context, private val linearLayout: LinearLayout) {

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
    private val bitmapsAtlantic = mutableListOf<Bitmap>()
    private val bitmapsPacific = mutableListOf<Bitmap>()
    private val bitmapsCentral = mutableListOf<Bitmap>()
    private var notificationCard: ObjectCardText? = null
    private val cardNotificationHeaderText = "Currently blocked storm notifications, tap this text to clear all blocks "
    var html: String = ""
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val horizontalLinearLayouts = mutableListOf<ObjectLinearLayout>()

    init {
        if (UtilityUI.isLandScape(context)) {
            imagesPerRow = 3
        }
    }

    fun getTextData() {
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

    fun getAtlanticImageData() {
        listOf(
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_0d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_2d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_5d0.png"
        ).forEach { bitmapsAtlantic.add(it.getImage()) }
    }

    fun getPacificImageData() {
        listOf(
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_0d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_2d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_5d0.png"
        ).forEach { bitmapsPacific.add(it.getImage()) }
    }

    fun getCentralImageData() {
        listOf(
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_0d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_2d0.png",
                "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_5d0.png"
        ).forEach { bitmapsCentral.add(it.getImage()) }
    }

    private val imageList = listOf(
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_0d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_2d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_5d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_0d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_2d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_5d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_0d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_2d0.png",
            "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_cpac_5d0.png"
    )

    private val titleList = listOf(
            "Atlantic Tropical Cyclones and Disturbances ",
            "ATL: Two-Day Graphical Tropical Weather Outlook",
            "ATL: Five-Day Graphical Tropical Weather Outlook",
            "EPAC Tropical Cyclones and Disturbances ",
            "EPAC: Two-Day Graphical Tropical Weather Outlook",
            "EPAC: Five-Day Graphical Tropical Weather Outlook",
            "CPAC Tropical Cyclones and Disturbances ",
            "CPAC: Two-Day Graphical Tropical Weather Outlook",
            "CPAC: Five-Day Graphical Tropical Weather Outlook"
    )

    fun showTextData() {
        linearLayout.removeAllViewsInLayout()
        html = ""
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        notificationCard = ObjectCardText(context, cardNotificationHeaderText + muteStr)
        linearLayout.addView(notificationCard?.card)
        notificationCard?.setOnClickListener(View.OnClickListener { clearNhcNotificationBlock() })
        if (muteStr != "") {
            notificationCard?.visibility = View.VISIBLE
        } else {
            notificationCard?.visibility = View.GONE
        }
        if (atlSumList.size < 1) {
            val noAtl = "There are no tropical cyclones in the Atlantic at this time."
            ObjectCardText(context, linearLayout, noAtl)
            html = noAtl
        } else {
            atlSumList.indices.forEach { k ->
                if (atlImg1List[k] != "") {
                    val objStormData = ObjectNhcStormDetails(atlSumList[k])
                    val cAtlData = ObjectCardNhcStormReportItem(context, linearLayout, objStormData)
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
            ObjectCardText(context, linearLayout, noPac)
            html += noPac
        } else {
            pacSumList.indices.forEach { k ->
                if (pacImg1List[k] != "") {
                    val objStormData = ObjectNhcStormDetails(pacSumList[k])
                    val cPacData = ObjectCardNhcStormReportItem(context, linearLayout, objStormData)
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
    }

    fun showAtlanticImageData() {
        bitmapsAtlantic.forEach {
            val objectCardImage: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(context, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                objectCardImage = ObjectCardImage(context, objectLinearLayout.linearLayout, it, imagesPerRow)
            } else {
                objectCardImage = ObjectCardImage(context, horizontalLinearLayouts.last().linearLayout, it, imagesPerRow)
            }
            numberOfImages += 1
            val url = imageList[numberOfImages - 1]
            val title = titleList[numberOfImages - 1]
            objectCardImage.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        context,
                        ImageShowActivity::class.java,
                        ImageShowActivity.URL,
                        arrayOf(url, title)
                )
            })
        }
    }

    fun showPacificImageData() {
        bitmapsPacific.forEach {
            val objectCardImage: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(context, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                objectCardImage = ObjectCardImage(context, objectLinearLayout.linearLayout, it, imagesPerRow)
            } else {
                objectCardImage = ObjectCardImage(context, horizontalLinearLayouts.last().linearLayout, it, imagesPerRow)
            }
            numberOfImages += 1
            val url = imageList[numberOfImages - 1]
            val title = titleList[numberOfImages - 1]
            objectCardImage.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        context,
                        ImageShowActivity::class.java,
                        ImageShowActivity.URL,
                        arrayOf(url, title)
                )
            })
        }
    }

    fun showCentralImageData() {
        bitmapsCentral.forEach {
            val objectCardImage: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(context, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                objectCardImage = ObjectCardImage(context, objectLinearLayout.linearLayout, it, imagesPerRow)
            } else {
                objectCardImage = ObjectCardImage(context, horizontalLinearLayouts.last().linearLayout, it, imagesPerRow)
            }
            numberOfImages += 1
            val url = imageList[numberOfImages - 1]
            val title = titleList[numberOfImages - 1]
            objectCardImage.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        context,
                        ImageShowActivity::class.java,
                        ImageShowActivity.URL,
                        arrayOf(url, title)
                )
            })
        }
    }

    private fun clearNhcNotificationBlock() {
        Utility.writePref(context, "NOTIF_NHC_MUTE", "")
        if (notificationCard != null) {
            notificationCard!!.visibility = View.GONE
        }
    }

    fun handleRestartForNotification() {
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        if (notificationCard != null) {
            if (muteStr != "") {
                notificationCard!!.text = cardNotificationHeaderText + muteStr
                notificationCard!!.visibility = View.VISIBLE
            } else {
                notificationCard!!.visibility = View.GONE
            }
        }
    }
}


