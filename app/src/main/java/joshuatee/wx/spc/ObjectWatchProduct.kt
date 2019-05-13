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

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityImg

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal class ObjectWatchProduct(type: PolygonType, productNumber: String) {

    private var productNumber = ""
    private var imgUrl = ""
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
    var wfoArr = listOf<String>()
        private set

    init {
        this.productNumber = productNumber
        when (type) {
            PolygonType.WATCH_TORNADO, PolygonType.WATCH -> {
                this.productNumber = productNumber.replace("w".toRegex(), "")
                imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww" + productNumber +
                        "_radar.gif"
                textUrl =
                    "${MyApplication.nwsSPCwebsitePrefix}/products/watch/ww$productNumber.html"
                title = "Watch $productNumber"
                prod = "SPCWAT$productNumber"
            }
            PolygonType.MCD -> {
                imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/mcd$productNumber.gif"
                textUrl = "${MyApplication.nwsSPCwebsitePrefix}/products/md/md$productNumber.html"
                title = "MCD $productNumber"
                prod = "SPCMCD$productNumber"
            }
            PolygonType.MPD -> {
                imgUrl =
                    "${MyApplication.nwsWPCwebsitePrefix}/metwatch/images/mcd$productNumber.gif"
                title = "MPD $productNumber"
                prod = "WPCMPD$productNumber"
            }
            else -> {
            }
        }
    }

    fun getData(context: Context) {
        text = UtilityDownload.getTextProduct(context, prod)
        bitmap = imgUrl.getImage()
        val wfoStr = text.parse("ATTN...WFO...(.*?)...<BR><BR>")
        wfoArr = wfoStr.split("\\.\\.\\.".toRegex()).dropLastWhile { it.isEmpty() }
    }

    val textForSubtitle: String
        get() {
            var stitle = text.parse("Areas affected...(.*?)<BR>")
            if (stitle == "" ) {
                stitle = text.parse("Watch for (.*?)<BR>").condenseSpace()
            }
            return stitle
        }
}


