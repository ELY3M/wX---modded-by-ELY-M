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

import android.content.Context
import joshuatee.wx.condenseSpace
import joshuatee.wx.getImage
import joshuatee.wx.parse
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.UtilityImg

internal class ObjectWatchProduct(val type: PolygonType, productNumber: String) {

    private var productNumber = ""
    var imgUrl = ""
    var textUrl = ""
        private set
    var title = ""
        private set
    var prod = ""
        private set
    var bitmap = UtilityImg.getBlankBitmap()
        private set
    var text = ""
        private set
    private var wfos = listOf<String>()
    private var stringOfLatLon = ""
    private var latLons = listOf<String>()

    init {
        this.productNumber = productNumber
        when (type) {
            PolygonType.WATCH_TORNADO, PolygonType.WATCH -> {
                this.productNumber = productNumber.replace("w".toRegex(), "")
                imgUrl =
                    "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/watch/ww" + productNumber + "_radar.gif"
                textUrl =
                    "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/watch/ww$productNumber.html"
                title = "Watch $productNumber"
                prod = "SPCWAT$productNumber"
            }

            PolygonType.MCD -> {
                imgUrl =
                    "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/md/mcd$productNumber.png"
                textUrl =
                    "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/products/md/md$productNumber.html"
                title = "MCD $productNumber"
                prod = "SPCMCD$productNumber"
            }

            PolygonType.MPD -> {
                imgUrl =
                    "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/metwatch/images/mcd$productNumber.gif"
                title = "MPD $productNumber"
                prod = "WPCMPD$productNumber"
            }

            else -> {}
        }
    }

    fun getText(context: Context) {
        text = DownloadText.byProduct(context, prod)
        var textWithLatLon = text
        if (type == PolygonType.WATCH || type == PolygonType.WATCH_TORNADO) {
            textWithLatLon = PolygonWatch.getLatLonWatch(productNumber)
        }
        stringOfLatLon = LatLon.storeWatchMcdLatLon(textWithLatLon).replace(":", "")
        latLons = stringOfLatLon.split(" ")
        val wfoString = text.parse("ATTN...WFO...(.*?)...<BR><BR>")
        wfos = wfoString.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
    }

    fun getImage() {
        bitmap = imgUrl.getImage()
    }

    private fun getCenterOfPolygon(latLons: List<LatLon>): LatLon {
        val center = LatLon.empty()
        latLons.forEach {
            center.lat += it.lat
            center.lon += it.lon
        }
        val totalPoints = latLons.size
        center.lat /= totalPoints
        center.lon /= totalPoints
        return center
    }

    fun getClosestRadar(): String {
        return if (latLons.size > 2) {
            val latLonList = LatLon.parseStringToLatLons(stringOfLatLon, -1.0, isWarning = false)
            val center = getCenterOfPolygon(latLonList)
            val radarSites = UtilityLocation.getNearestRadarSites(center, 1, includeTdwr = false)
            if (radarSites.isEmpty()) {
                ""
            } else {
                radarSites[0].name
            }
        } else {
            ""
        }
    }

    val textForSubtitle: String
        get() {
            var subTitle = text.parse("Areas affected...(.*?)\n")
            if (subTitle == "") {
                subTitle = text.parse("Watch for (.*?)<BR>").condenseSpace()
            }
            return subTitle
        }
}
