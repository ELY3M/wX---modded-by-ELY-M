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
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility

import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.Extensions.*
import joshuatee.wx.util.UtilityDownload

class ObjectNhc(val context: Context, private val linearLayout: LinearLayout) {

    private var notificationCard: ObjectCardText? = null
    private val cardNotificationHeaderText = "Currently blocked storm notifications, tap this text to clear all blocks "
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val horizontalLinearLayouts = mutableListOf<ObjectLinearLayout>()
    val regionMap = mutableMapOf<NhcOceanEnum, ObjectNhcRegionSummary>()
    private var stormDataList = mutableListOf<ObjectNhcStormDetails>()
    private var ids = listOf<String>()
    private var binNumbers = listOf<String>()
    private var names = listOf<String>()
    private var classifications = listOf<String>()
    private var intensities = listOf<String>()
    private var pressures = listOf<String>()
    private var latitudes = listOf<String>()
    private var longitudes = listOf<String>()
    private var movementDirs = listOf<String>()
    private var movementSpeeds = listOf<String>()
    private var lastUpdates = listOf<String>()
    private var statusList = mutableListOf<String>()
    val bitmaps = mutableListOf<Bitmap>()

    init {
        if (UtilityUI.isLandScape(context)) imagesPerRow = 3
        NhcOceanEnum.values().forEach { regionMap[it] = ObjectNhcRegionSummary(it) }
    }

    fun getTextData() {
        statusList.clear()
        val url = MyApplication.nwsNhcWebsitePrefix + "/CurrentStorms.json"
        //val url = "https://www.nhc.noaa.gov/productexamples/NHC_JSON_Sample.json"
        val html = url.getHtml()
        ids = html.parseColumn("\"id\": \"(.*?)\"")
        binNumbers = html.parseColumn("\"binNumber\": \"(.*?)\"")
        names = html.parseColumn("\"name\": \"(.*?)\"")
        classifications = html.parseColumn("\"classification\": \"(.*?)\"")
        intensities = html.parseColumn("\"intensity\": \"(.*?)\"")
        pressures = html.parseColumn("\"pressure\": \"(.*?)\"")
        // sample data not quoted for these two
        //intensities = html.parseColumn("\"intensity\": (.*?),");
        //pressures = html.parseColumn("\"pressure\": (.*?),");
        //
        latitudes = html.parseColumn("\"latitude\": \"(.*?)\"")
        longitudes = html.parseColumn("\"longitude\": \"(.*?)\"")
        movementDirs = html.parseColumn("\"movementDir\": (.*?),")
        movementSpeeds = html.parseColumn("\"movementSpeed\": (.*?),")
        lastUpdates = html.parseColumn("\"lastUpdate\": \"(.*?)\"")
        binNumbers.forEach {
            val text = UtilityDownload.getTextProduct(context, "MIATCP$it")
            val status = text.replace("\n", " ").parseFirst("(\\.\\.\\..*?\\.\\.\\.)")
            statusList.add(status)
        }
    }

    fun showTextData() {
        linearLayout.removeAllViewsInLayout()
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        notificationCard = ObjectCardText(context, cardNotificationHeaderText + muteStr)
        linearLayout.addView(notificationCard?.card)
        notificationCard?.setOnClickListener { clearNhcNotificationBlock() }
        if (muteStr != "") {
            notificationCard?.visibility = View.VISIBLE
        } else {
            notificationCard?.visibility = View.GONE
        }

        if (ids.isNotEmpty()) {
            ids.indices.forEach { index ->
                val objectNhcStormDetails = ObjectNhcStormDetails(
                        names[index],
                        movementDirs[index],
                        movementSpeeds[index],
                        pressures[index],
                        binNumbers[index],
                        ids[index],
                        lastUpdates[index],
                        classifications[index],
                        latitudes[index],
                        longitudes[index],
                        intensities[index],
                        statusList[index]
                )
                stormDataList.add(objectNhcStormDetails)
                val card = ObjectCardNhcStormReportItem(context, linearLayout, objectNhcStormDetails)
                card.setListener { ObjectIntent.showNhcStorm(context, objectNhcStormDetails) }
            }
        }
    }

    fun showImageData(region: NhcOceanEnum) {
        bitmaps.clear()
        regionMap[region]!!.bitmaps.forEachIndexed { index, bitmap ->
            val objectCardImage: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(context, linearLayout)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                objectCardImage = ObjectCardImage(context, objectLinearLayout.linearLayout, bitmap, imagesPerRow)
            } else {
                objectCardImage = ObjectCardImage(context, horizontalLinearLayouts.last().linearLayout, bitmap, imagesPerRow)
            }
            numberOfImages += 1
            objectCardImage.setOnClickListener { ObjectIntent.showImage(context, regionMap[region]!!.getTitle(index)) }
            bitmaps.add(bitmap)
        }
    }

    private fun clearNhcNotificationBlock() {
        Utility.writePref(context, "NOTIF_NHC_MUTE", "")
        if (notificationCard != null) notificationCard!!.visibility = View.GONE
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


