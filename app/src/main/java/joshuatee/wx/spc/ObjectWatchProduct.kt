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

package joshuatee.wx.spc

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.Extensions.condenseSpace
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.Extensions.parse
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.LatLon
import joshuatee.wx.radar.UtilityDownloadWatch
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityDownload
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
    var bitmap: Bitmap = UtilityImg.getBlankBitmap()
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
                imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/ww" + productNumber + "_radar.gif"
                textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/watch/ww$productNumber.html"
                title = "Watch $productNumber"
                prod = "SPCWAT$productNumber"
            }
            PolygonType.MCD -> {
                imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/mcd$productNumber.gif"
                textUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/products/md/md$productNumber.html"
                title = "MCD $productNumber"
                prod = "SPCMCD$productNumber"
            }
            PolygonType.MPD -> {
                imgUrl = "${GlobalVariables.nwsWPCwebsitePrefix}/metwatch/images/mcd$productNumber.gif"
                title = "MPD $productNumber"
                prod = "WPCMPD$productNumber"
            }
            else -> {}
        }
    }

    fun getData(context: Context) {
        text = UtilityDownload.getTextProduct(context, prod)
        var textWithLatLon = text
        if (type == PolygonType.WATCH || type == PolygonType.WATCH_TORNADO) {
            textWithLatLon = UtilityDownloadWatch.getLatLon(productNumber)
        }
        stringOfLatLon = UtilityNotification.storeWatchMcdLatLon(textWithLatLon).replace(":", "")
        latLons = stringOfLatLon.split(" ")
        bitmap = imgUrl.getImage()
        val wfoString = text.parse("ATTN...WFO...(.*?)...<BR><BR>")
        wfos = wfoString.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
    }

    private fun getCenterOfPolygon(latLons: List<LatLon>): LatLon {
        val center = LatLon(0.0, 0.0)
        latLons.forEach {
            center.lat += it.lat
            center.lon += it.lon
        }
        val totalPoints = latLons.size
        center.lat = center.lat / totalPoints
        center.lon = center.lon / totalPoints
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
            var subTitle = text.parse("Areas affected...(.*?)<BR>")
            if (subTitle == "" ) {
                subTitle = text.parse("Watch for (.*?)<BR>").condenseSpace()
            }
            return subTitle
        }
}
