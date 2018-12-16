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

package joshuatee.wx.vis

import java.util.Locale

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.Extensions.getHtmlSep
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.Extensions.*

object UtilityUSImgNWSGOES {

    fun getGOESMosaic(
        context: Context,
        satSectorF: String,
        sector: String,
        imageTypeNhc: String,
        mesoImgA: List<String>,
        overlayImgA: List<String>,
        wfoChoosen: Boolean,
        isInteractive: Boolean
    ): Bitmap {
        var satSector = satSectorF
        if (wfoChoosen && !satSector.contains("wfo")) {
            satSector += "/wfo"
        }
        val url = "http://www.ssd.noaa.gov/goes/$satSector/$sector/$imageTypeNhc.jpg"
        val urlS = "http://www.ssd.noaa.gov/goes/$satSector/$sector/over/"
        val bitmap: Bitmap
        val bitmapAl = mutableListOf<Bitmap>()
        var bitmapTmp = url.getImage()
        bitmapAl.add(bitmapTmp)
        try {
            overlayImgA.forEach {
                var sLocal = it
                if (it == "FRNT" && (sector == "eaus" || sector == "weus" || sector == "ceus" || sector == "nhem" || sector == "gmex" || sector == "carb" || sector.contains(
                        "atl"
                    ) || sector.contains("pac"))
                ) {
                    sLocal = "FRONTS"
                }
                bitmapTmp = UtilityImg.getBitmapRemoveBG(
                    urlS + sLocal.toUpperCase(Locale.US) + ".GIF",
                    -16777216
                )
                bitmapAl.add(bitmapTmp)
            }
            for (s in mesoImgA) {
                if (s == "radar" && sector.contains("eaus")) {
                    continue
                }
                bitmapTmp = if (!satSector.contains("west/wfo") && !sector.contains("eaus")) {
                    getNWSGOESOverlayImage(satSector, sector, imageTypeNhc, s)
                } else {
                    (urlS + s.toUpperCase(Locale.US) + ".GIF").getImage()
                }
                bitmapTmp = UtilityImg.eraseBG(bitmapTmp, -16777216)
                bitmapAl.add(bitmapTmp)
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        val layersAl =
            bitmapAl.mapTo(mutableListOf<Drawable>()) { BitmapDrawable(context.resources, it) }
        bitmap = UtilityImg.layerDrawableToBitmap(layersAl)
        var citySize = 0
        if (wfoChoosen) {
            citySize = 15
        }
        UtilityCanvasMain.addCanvasItems(
            context,
            bitmap,
            ProjectionType.NWS_GOES,
            sector.toUpperCase(Locale.US),
            0,
            citySize,
            isInteractive
        )
        return bitmap
    }

    private fun getNWSGOESOverlayImage(
        satSectorLocal: String,
        sector: String,
        imageTypeNhc: String,
        imageType: String
    ): Bitmap {
        // http://www.ssd.noaa.gov/goes/east/gl/txtfiles/vis_names.txt
        val urlS = "http://www.ssd.noaa.gov/goes/$satSectorLocal/$sector/over/"
        val imgData =
            ("http://www.ssd.noaa.gov/goes/" + satSectorLocal + "/" + sector + "/txtfiles/" + imageTypeNhc + "_names.txt").getHtml()
        val imgDataAl =
            imgData.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.toMutableList()
        while (imgDataAl.remove("")) {
        }
        return if (imgDataAl.size > 0) {
            val time =
                imgDataAl[imgDataAl.size - 1].parseLastMatch("img/([0-9]{7}_[0-9]{4})$imageTypeNhc.jpg.*?")
            ("$urlS$time$imageType.gif").getImage()
        } else {
            Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
    }

    internal fun getGOESMosaicNONGOES(
        context: Context,
        satSector: String,
        sector: String,
        imageTypeNhc: String,
        imgFormat: String,
        mesoImgA: List<String>,
        overlayImgA: List<String>,
        wfoChoosen: Boolean,
        isInteractive: Boolean
    ): Bitmap {
        val url = "http://www.ssd.noaa.gov/$satSector/$sector/$imageTypeNhc$imgFormat"
        val urlS = "http://www.ssd.noaa.gov/$satSector/$sector/over/"
        var bitmap: Bitmap = UtilityImg.getBlankBitmap()
        var bitmap1: Bitmap = UtilityImg.getBlankBitmap()
        var bitmapTmp2: Bitmap
        val bitmapAl = mutableListOf<Bitmap>()
        if (mesoImgA.isEmpty() && overlayImgA.isEmpty()) {
            val bitmapTmp = url.getImage()
            bitmap = bitmapTmp.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap1 = url.getImage()
            try {
                overlayImgA.forEach {
                    var sLocal = it
                    if (it == "FRNT" && (sector == "eaus" || sector == "weus" || sector == "ceus" || sector == "nhem" || sector == "gmex" || sector == "carb" || sector.contains(
                            "atl"
                        ) || sector.contains("pac"))
                    ) {
                        sLocal = "FRONTS"
                    }
                    bitmapTmp2 = (urlS + sLocal.toUpperCase(Locale.US) + ".GIF").getImage()
                    bitmapAl.add(UtilityImg.eraseBG(bitmapTmp2, -16777216))
                }
                for (s in mesoImgA) {
                    if (s == "radar" && sector.contains("eaus")) // eaus has a radar file but it's outdated
                        continue
                    bitmapTmp2 = if (!satSector.contains("west/wfo") && !sector.contains("eaus")) {
                        val urlArr = UtilityImgAnim.getUrlArray(
                            urlS,
                            "<a href=\"([0-9]{7}_[0-9]{4}$s\\.gif)\">.*?",
                            1
                        )
                        (urlS + urlArr[0]).getImage()
                    } else {
                        (urlS + s.toUpperCase(Locale.US) + ".GIF").getImage()
                    }
                    bitmapAl.add(UtilityImg.eraseBG(bitmapTmp2, -16777216))
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
        if (!(mesoImgA.isEmpty() && overlayImgA.isEmpty())) {
            val layersAl = mutableListOf<Drawable>()
            layersAl.add(BitmapDrawable(context.resources, bitmap1))
            bitmapAl.mapTo(layersAl) { BitmapDrawable(context.resources, it) }
            bitmap = UtilityImg.layerDrawableToBitmap(layersAl)
        }
        var citySize = 0
        if (wfoChoosen) {
            citySize = 15
        }
        UtilityCanvasMain.addCanvasItems(
            context,
            bitmap,
            ProjectionType.NWS_GOES,
            sector.toUpperCase(Locale.US),
            0,
            citySize,
            isInteractive
        )
        return bitmap
    }

    internal fun getNWSGOESAnim(
        context: Context,
        satSector: String,
        sector: String,
        imageTypeAnim: String,
        frameCount: Int
    ): AnimationDrawable {
        //val frameCnt = frameCntStr.toIntOrNull() ?: 0
        val url = "http://www.ssd.noaa.gov/goes/$satSector/$sector/img/"
        val bmAl = mutableListOf<Bitmap>()
        val urlArr: List<String>
        val urlAL = getNWSGOESAnimationURLs(satSector, sector, imageTypeAnim)
        if (urlAL.size >= frameCount) {
            (urlAL.size - frameCount until urlAL.size).mapTo(bmAl) { urlAL[it].getImage() }
        } else {
            try {
                urlArr = UtilityImgAnim.getUrlArray(
                    url,
                    "<a href=\"([0-9]{7}_[0-9]{4}$imageTypeAnim\\.jpg)\">.*?",
                    frameCount
                )
                urlArr.mapTo(bmAl) { (url + it).getImage() }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
        val animDrawable = AnimationDrawable()
        var delay = UtilityImg.animInterval(context)
        (0 until bmAl.size).forEach {
            if (it == frameCount - 1) {
                delay *= 3
            }
            animDrawable.addFrame(BitmapDrawable(context.resources, bmAl[it]), delay)
        }
        return animDrawable
    }

    internal fun getNWSGOESAnimNONGOES(
        context: Context,
        satSector: String,
        sector: String,
        imageTypeAnim: String,
        frameCount: Int
    ): AnimationDrawable {
        val url = "http://www.ssd.noaa.gov/$satSector/$sector/img/"
        var imgFormat = ".gif"
        if (satSector == "eumet") {
            imgFormat = ".jpg"
        }
        val bmAl = mutableListOf<Bitmap>()
        val urlArr: List<String>
        try {
            urlArr = UtilityImgAnim.getUrlArray(
                url,
                "<a href=\"([0-9]{7}_[0-9]{4}$imageTypeAnim\\$imgFormat)\">.*?",
                frameCount
            )
            urlArr.mapTo(bmAl) { (url + it).getImage() }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        val delay = UtilityImg.animInterval(context)
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl, delay)
    }

    private fun getNWSGOESAnimationURLs(
        satSectorLocal: String,
        sector: String,
        imageTypeNhc: String
    ): List<String> {
        // http://www.ssd.noaa.gov/goes/east/gl/txtfiles/vis_names.txt
        val imgData =
            ("http://www.ssd.noaa.gov/goes/" + satSectorLocal + "/" + sector + "/txtfiles/" + imageTypeNhc + "_names.txt").getHtmlSep()
        val urlAlTmp = imgData.split("<br>").dropLastWhile { it.isEmpty() }
        val urlAl = mutableListOf<String>()
        val url = "http://www.ssd.noaa.gov/goes/$satSectorLocal/$sector/"
        var tmpStr: List<String>
        urlAlTmp.forEach {
            tmpStr = it.split(" ")
            if (tmpStr.isNotEmpty()) {
                urlAl.add(url + tmpStr[0])
            }
        }
        return urlAl
    }
}

