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

package joshuatee.wx.nhc

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import joshuatee.wx.objects.Route
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parseColumn
import joshuatee.wx.parseFirst
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.HBox
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityString

class Nhc(val context: Context, box: VBox) {

    private var notificationCard: CardText? = null
    private val cardNotificationHeaderText =
        "Currently blocked storm notifications, tap this text to clear all blocks "
    private var numberOfImages = 0
    private var imagesPerRow = 2
    private val boxRows = mutableListOf<HBox>()
    private val regionMap = mutableMapOf<NhcOceanEnum, NhcRegionSummary>()
    val bitmaps = mutableListOf<Bitmap>()
    private val images = mutableListOf<Image>()
    val urls = mutableListOf<String>()
    private val imageTitles = mutableListOf<String>()
    private val boxText = VBox(context)
    private val boxImages = VBox(context)
    private var nhcStormDetails = mutableListOf<NhcStormDetails>()

    init {
        box.addLayout(boxText)
        box.addLayout(boxImages)
        if (UtilityUI.isLandScape(context)) {
            imagesPerRow = 3
        }
        NhcOceanEnum.entries.forEach {
            regionMap[it] = NhcRegionSummary(it)
        }
        listOf(
            NhcRegionSummary(NhcOceanEnum.ATL),
            NhcRegionSummary(NhcOceanEnum.EPAC),
            NhcRegionSummary(NhcOceanEnum.CPAC)
        ).forEach {
            urls.addAll(it.urls)
            imageTitles.addAll(it.titles)
        }
        showImageData()
    }

    fun getText() {
        nhcStormDetails = getTextData(context).toMutableList()
    }

    companion object {

        const val WIDGET_IMAGE_URL_TOP =
            "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_atl_0d0.png"
        const val WIDGET_IMAGE_URL_BOTTOM =
            "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_pac_0d0.png"

        fun getTextData(context: Context): List<NhcStormDetails> {
            val nhcStormDetailsList = mutableListOf<NhcStormDetails>()
            val statusList = mutableListOf<String>()
            val publicAdvisories = mutableListOf<String>()
            val publicAdvisoriesNumbers = mutableListOf<String>()
            val forecastAdvisories = mutableListOf<String>()
            val forecastDiscussions = mutableListOf<String>()
            val windSpeedProbabilities = mutableListOf<String>()
            val url = GlobalVariables.NWS_NHC_WEBSITE_PREFIX + "/CurrentStorms.json"
            //val url = "https://www.nhc.noaa.gov/productexamples/NHC_JSON_Sample.json"
            val html = url.getHtml()
            val ids = html.parseColumn("\"id\": \"(.*?)\"")
            val binNumbers = html.parseColumn("\"binNumber\": \"(.*?)\"")
            val names = html.parseColumn("\"name\": \"(.*?)\"")
            val classifications = html.parseColumn("\"classification\": \"(.*?)\"")
            val intensities = html.parseColumn("\"intensity\": \"(.*?)\"")
            val pressures = html.parseColumn("\"pressure\": \"(.*?)\"")
            // sample data not quoted for these two
            //intensities = html.parseColumn("\"intensity\": (.*?),");
            //pressures = html.parseColumn("\"pressure\": (.*?),");
            //
            val latitudes = html.parseColumn("\"latitude\": \"(.*?)\"")
            val longitudes = html.parseColumn("\"longitude\": \"(.*?)\"")
            val movementDirs = html.parseColumn("\"movementDir\": (.*?),")
            val movementSpeeds = html.parseColumn("\"movementSpeed\": (.*?),")
            val lastUpdates = html.parseColumn("\"lastUpdate\": \"(.*?)\"")
            val publicAdvisoriesChunk =
                UtilityString.parseColumn(html, "\"publicAdvisory\": \\{(.*?)\\}")
            val forecastAdvisoriesChunk =
                UtilityString.parseColumn(html, "\"forecastAdvisory\": \\{(.*?)\\}")
            val forecastDiscussionsChunk =
                UtilityString.parseColumn(html, "\"forecastDiscussion\": \\{(.*?)\\}")
            val windSpeedProbabilitiesChunk =
                UtilityString.parseColumn(html, "\"windSpeedProbabilities\": \\{(.*?)\\}")
            for (chunk in publicAdvisoriesChunk) {
                val token = UtilityString.parse(chunk, "\"url\": \"(.*?)\"")
                publicAdvisories.add(token)
                val tokenNum = UtilityString.parse(chunk, "\"advNum\": \"(.*?)\"")
                publicAdvisoriesNumbers.add(tokenNum)
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
                val productToken = adv.split("/").last().replace(".shtml", "")
                val text = DownloadText.byProduct(context, productToken)
                val status = text.replace("\n", " ")
                    .parseFirst("BULLETIN.*?" + ObjectDateTime.getYearString() + " (.*?)SUMMARY OF ")
                statusList.add(status)
            }

            if (ids.isNotEmpty()) {
                ids.indices.forEach { index ->
                    val objectNhc = NhcStormDetails(
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
                        Utility.safeGet(statusList, index),
                        Utility.safeGet(publicAdvisories, index),
                        Utility.safeGet(publicAdvisoriesNumbers, index)
                    )
                    nhcStormDetailsList.add(objectNhc)
                }
            }
            return nhcStormDetailsList
        }
    }

    fun showText() {
        boxText.removeChildren()
        val muteStr = Utility.readPref(context, "NOTIF_NHC_MUTE", "")
        notificationCard = CardText(context, cardNotificationHeaderText + muteStr)
        boxText.addWidget(notificationCard!!)
        notificationCard?.connect { clearNhcNotificationBlock() }
        if (muteStr != "") {
            notificationCard?.visibility = View.VISIBLE
        } else {
            notificationCard?.visibility = View.GONE
        }
        nhcStormDetails.forEach { objectNhc ->
            val card = CardNhcStormReportItem(context, objectNhc)
            boxText.addWidget(card)
            card.connect { Route.nhcStorm(context, objectNhc) }
        }
    }

    private fun showImageData() {
        bitmaps.clear()
        urls.indices.forEach { index ->
            val bitmap = UtilityImg.getBlankBitmap()
            val image = if (numberOfImages % imagesPerRow == 0) {
                val hbox = HBox(context, boxImages)
                boxRows.add(hbox)
                val image = Image(context, bitmap, imagesPerRow)
                hbox.addWidget(image)
                image
            } else {
                val image = Image(context, bitmap, imagesPerRow)
                boxRows.last().addWidget(image)
                image
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
