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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Bitmap.Config
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*

object UtilityUSImgNWSMosaic {

    internal val NWS_SECTORS = listOf(
            "alaska",
            "hawaii",
            "pacsouthwest",
            "southrockies",
            "southplains",
            "southmissvly",
            "southeast",
            "pacnorthwest",
            "northrockies",
            "uppermissvly",
            "centgrtlakes",
            "northeast",
            "conus"
    )
    internal val NWS_SECTORS_LABELS = listOf(
            "Alaska",
            "Hawaii",
            "Pacific Southwest",
            "South Rockies",
            "Southern Plains",
            "Southern MS Valley",
            "Southeast",
            "Pacific Northwest",
            "North Rockies",
            "Upper MS Valley",
            "Central Great Lakes",
            "Northeast",
            "CONUS"
    )

    private val NWS_CODE_TO_LABEL = mapOf(
            "alaska" to "Alaska",
            "hawaii" to "Hawaii",
            "pacsouthwest" to "Pacific Southwest",
            "pacnorthwest" to "Pacific Northwest",
            "southrockies" to "South Rockies",
            "northrockies" to "North Rockies",
            "uppermissvly" to "Upper MS Valley",
            "southplains" to "Southern Plains",
            "centgrtlakes" to "Central Great Lakes",
            "southmissvly" to "Southern MS Valley",
            "southeast" to "Southeast",
            "northeast" to "Northeast",
            "conus" to "CONUS"
    )

    private val COD_HASH = mapOf(
            "WA" to "pacnorthwest",
            "ID" to "pacnorthwest",
            "OR" to "pacnorthwest",
            "CA" to "pacsouthwest",
            "NV" to "pacsouthwest",
            "UT" to "northrockies",
            "AZ" to "southrockies",
            "NM" to "southrockies",
            "ND" to "uppermissvly",
            "SD" to "uppermissvly",
            "MT" to "northrockies",
            "WY" to "northrockies",
            "CO" to "northrockies",
            "NE" to "uppermissvly",
            "KS" to "uppermissvly",
            "OK" to "southplains",
            "TX" to "southplains",
            "LA" to "southmissvly",
            "MN" to "uppermissvly",
            "WI" to "centgrtlakes",
            "MI" to "centgrtlakes",
            "IA" to "uppermissvly",
            "IN" to "centgrtlakes",
            "IL" to "centgrtlakes",
            "TN" to "southmissvly",
            "MO" to "uppermissvly",
            "AR" to "southmissvly",
            "FL" to "southeast",
            "MS" to "southmissvly",
            "AL" to "southmissvly",
            "GA" to "southeast",
            "SC" to "southeast",
            "NC" to "southeast",
            "KY" to "centgrtlakes",
            "OH" to "centgrtlakes",
            "WV" to "centgrtlakes",
            "VA" to "northeast",
            "PA" to "northeast",
            "NJ" to "northeast",
            "DE" to "northeast",
            "ME" to "northeast",
            "MA" to "northeast",
            "NH" to "northeast",
            "VT" to "northeast",
            "CT" to "northeast",
            "RI" to "northeast",
            "NY" to "northeast",
            "AK" to "alaska",
            "HI" to "hawaii"
    )

    internal fun getNWSSectorLabelFromCode(code: String) = NWS_CODE_TO_LABEL[code] ?: ""

    fun getNWSSectorFromState(state: String) = COD_HASH[state] ?: ""

    internal fun nwsMosaicAnimation(context: Context, sector: String, frameCntStr: String, isInteractive: Boolean): AnimationDrawable {
        val urlArr: List<String>
        val bmAl = mutableListOf<Bitmap>()
        var scaleType = ProjectionType.NWS_MOSAIC_SECTOR
        val sectorUrl = if (sector == "latest") {
            "Conus"
        } else {
            sector
        }
        val cd = if (MyApplication.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        var bitmapCanvas = UtilityImg.getBlankBitmap()
        val baseUrl = "http://radar.weather.gov/ridge/Conus/RadarImg/"
        if (sector == "latest") {
            scaleType = ProjectionType.NWS_MOSAIC
        }
        var sPattern = "href=.(" + sectorUrl + "_[0-9]{8}_[0-9]{4}_N0Ronly.gif)"
        if (sectorUrl == "alaska") {
            sPattern = "href=.(" + "NATAK" + "_[0-9]{8}_[0-9]{4}.gif)"
        }
        urlArr = UtilityImgAnim.getURLArray("http://radar.weather.gov/ridge/Conus/RadarImg/", sPattern, frameCntStr)
        urlArr.forEach {
            if (MyApplication.blackBg && sector != "alaska") {
                bmAl.add(UtilityImg.getBitmapRemoveBG(baseUrl + it, -1))
            } else {
                bmAl.add((baseUrl + it).getImage())
            }
        }
        try {
            if (bmAl.size > 1 && bmAl[0].height > 10) {
                bitmapCanvas = Bitmap.createBitmap(bmAl[0].width, bmAl[0].height, Config.ARGB_8888)
                UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, sector, 1, 13, isInteractive)
            }
        } catch (e: OutOfMemoryError) {
            bitmapCanvas = UtilityImg.getBlankBitmap()
        }
        val delay = UtilityImg.animInterval(context)
        return UtilityImgAnim.getAnimationDrawableFromBMListWithCanvas(context, bmAl, delay, cd, bitmapCanvas)
    }

    fun nwsMosaic(context: Context, sector: String, isInteractive: Boolean): Bitmap {
        val imgUrl = "http://radar.weather.gov/Conus/RadarImg/" + sector + "_radaronly.gif"
        if (sector == "alaska") {
            return "http://radar.weather.gov/ridge/Conus/RadarImg/alaska.gif".getImage()
        }
        val layers = mutableListOf<Drawable>()
        val cd = if (MyApplication.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        var scaleType = ProjectionType.NWS_MOSAIC_SECTOR
        if (sector == "latest") {
            scaleType = ProjectionType.NWS_MOSAIC
        }
        var bitmap = imgUrl.getImage()
        var bitmapCanvas = UtilityImg.getBlankBitmap()
        if (MyApplication.blackBg) {
            bitmap = UtilityImg.eraseBG(bitmap, -1)
        }
        if (bitmap.height > 10) {
            bitmapCanvas = Bitmap.createBitmap(bitmap.width, bitmap.height, Config.ARGB_8888)
            UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, sector, 1, 13, isInteractive)
        }
        layers.add(cd)
        layers.add(BitmapDrawable(context.resources, bitmap))
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        return UtilityImg.layerDrawableToBitmap(layers)
    }
}
