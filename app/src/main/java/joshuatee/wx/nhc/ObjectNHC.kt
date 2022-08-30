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

package joshuatee.wx.nhc

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import joshuatee.wx.objects.Route
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class ObjectNhc(val context: Context, box: VBox) {

    private var notificationCard: CardText? = null
    private val cardNotificationHeaderText = "Currently blocked storm notifications, tap this text to clear all blocks "
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val boxRows = mutableListOf<HBox>()
    private val regionMap = mutableMapOf<NhcOceanEnum, ObjectNhcRegionSummary>()
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
    private var publicAdvisoriesChunk = listOf<String>()
    private var forecastAdvisoriesChunk = listOf<String>()
    private var forecastDiscussionsChunk = listOf<String>()
    private var windSpeedProbabilitiesChunk = listOf<String>()
    private var publicAdvisories = mutableListOf<String>()
    private var forecastAdvisories = mutableListOf<String>()
    private var forecastDiscussions = mutableListOf<String>()
    private var windSpeedProbabilities = mutableListOf<String>()
    val bitmaps = mutableListOf<Bitmap>()
    private val images = mutableListOf<Image>()
    val urls = mutableListOf<String>()
    private val imageTitles = mutableListOf<String>()
    private val boxText = VBox(context, box.get())
    private val boxImages = VBox(context, box.get())

    init {
        if (UtilityUI.isLandScape(context)) {
            imagesPerRow = 3
        }
        NhcOceanEnum.values().forEach {
            regionMap[it] = ObjectNhcRegionSummary(it)
        }
        listOf(
                ObjectNhcRegionSummary(NhcOceanEnum.ATL),
                ObjectNhcRegionSummary(NhcOceanEnum.EPAC),
                ObjectNhcRegionSummary(NhcOceanEnum.CPAC)).forEach {
            urls.addAll(it.urls)
            imageTitles.addAll(it.titles)
        }
        showImageData()
    }

    fun getTextData() {
        statusList.clear()
        val url = GlobalVariables.nwsNhcWebsitePrefix + "/CurrentStorms.json"
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

        // REMOVE Jul 22
//        binNumbers.forEach {
//            val text = UtilityDownload.getTextProduct(context, "MIATCP$it")
//            val status = text.replace("\n", " ").parseFirst("(\\.\\.\\..*?\\.\\.\\.)")
//            statusList.add(status)
//        }

        // ADD Jul 22
        publicAdvisoriesChunk = UtilityString.parseColumn(html, "\"publicAdvisory\": \\{(.*?)\\}")
        forecastAdvisoriesChunk = UtilityString.parseColumn(html, "\"forecastAdvisory\": \\{(.*?)\\}")
        forecastDiscussionsChunk = UtilityString.parseColumn(html, "\"forecastDiscussion\": \\{(.*?)\\}")
        windSpeedProbabilitiesChunk = UtilityString.parseColumn(html, "\"windSpeedProbabilities\": \\{(.*?)\\}")
        for (chunk in publicAdvisoriesChunk) {
            val token = UtilityString.parse(chunk, "\"url\": \"(.*?)\"")
            publicAdvisories.add(token)
        }
        for (chunk in forecastAdvisoriesChunk) {
            val token = UtilityString.parse(chunk, "\"url\": \"(.*?)\"")
            forecastAdvisories.add(token)
        }
        for (chunk in forecastDiscussionsChunk) {
            val token = UtilityString.parse(chunk, "\"url\": \"(.*?)\"")
            forecastDiscussions.add(token)
        }
        for (chunk in windSpeedProbabilitiesChunk) {
            val token = UtilityString.parse(chunk, "\"url\": \"(.*?)\"")
            windSpeedProbabilities.add(token)
        }
        for (adv in publicAdvisories) {
//            UtilityLog.d("wxNHC", adv)
            val productToken = adv.split("/").last().replace(".shtml", "")
            val text = UtilityDownload.getTextProduct(context, productToken)
            val status = text.replace("\n", " ").parseFirst("(\\.\\.\\..*?\\.\\.\\.)")
//            UtilityLog.d("wxNHC", text)
//            val items = UtilityString.parseColumn(text, "^(\\.\\.\\..*?\\.\\.\\.)$")
//            statusList.add(items.joinToString("\n"))
            statusList.add(status)
        }
    }

    fun showTextData() {
        boxText.removeChildren()
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        notificationCard = CardText(context, cardNotificationHeaderText + muteStr)
        boxText.addWidget(notificationCard!!.get())
        notificationCard?.connect { clearNhcNotificationBlock() }
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
                        statusList[index],
                        Utility.safeGet(publicAdvisories, index),
                        Utility.safeGet(forecastAdvisories, index),
                        Utility.safeGet(forecastDiscussions, index),
                        Utility.safeGet(windSpeedProbabilities, index),
                )
                stormDataList.add(objectNhcStormDetails)
                val card = ObjectCardNhcStormReportItem(context, boxText, objectNhcStormDetails)
                card.connect { Route.nhcStorm(context, objectNhcStormDetails) }
            }
        }
    }

    private fun showImageData() {
        bitmaps.clear()
        urls.indices.forEach { index ->
            val bitmap = UtilityImg.getBlankBitmap()
            val image = if (numberOfImages % imagesPerRow == 0) {
                val hbox = HBox(context, boxImages.get())
                boxRows.add(hbox)
                Image(context, hbox, bitmap, imagesPerRow)
            } else {
                Image(context, boxRows.last(), bitmap, imagesPerRow)
            }
            numberOfImages += 1
            image.connect { Route.image(context, urls[index], imageTitles[index]) }
            bitmaps.add(bitmap)
            images.add(image)
        }
    }

    fun updateImageData(index: Int, bitmap: Bitmap) {
        images[index].set2(bitmap, imagesPerRow)
        images[index].connect { Route.image(context, urls[index], imageTitles[index]) }
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
