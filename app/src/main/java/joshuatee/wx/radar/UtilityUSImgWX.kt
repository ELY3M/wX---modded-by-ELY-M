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

package joshuatee.wx.radar

import java.io.InputStream
import java.util.Locale

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog

import joshuatee.wx.GlobalDictionaries
import joshuatee.wx.R

object UtilityUSImgWX {

    private const val CANVAS_X = 1000
    private const val CANVAS_Y = 1000

    fun layeredImg(
            context: Context,
            radarSiteArg: String,
            product: String,
            isInteractive: Boolean
    ): Bitmap {
        var radarSite = radarSiteArg
        var tdwr = false
        var ridTdwr = ""
        var scaleType = ProjectionType.WX_RENDER
        val hwLineWidth = 1
        if (product == "TR0" || product == "TV0" || product == "TZL") {
            ridTdwr = WXGLNexrad.getTdwrFromRid(radarSite)
            tdwr = true
            radarSite = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val ridPrefix = UtilityWXOGL.getRidPrefix(radarSite, tdwr)
        val inputStream: InputStream?
        if (!product.contains("L2")) {
            inputStream = UtilityDownload.getInputStreamFromUrl(
                    MyApplication.NWS_RADAR_PUB
                            + "SL.us008001/DF.of/DC.radar/"
                            + GlobalDictionaries.NEXRAD_PRODUCT_STRING[product]
                            + "/SI." + ridPrefix + radarSite.toLowerCase(
                            Locale.US
                    ) + "/sn.last"
            )
            inputStream?.let { UtilityIO.saveInputStream(context, it, "nids") }
        } else {
            val wd = WXGLDownload()
            val remoteFile = wd.getLevel2Url(radarSite)
            inputStream = UtilityDownload.getInputStreamFromUrl(remoteFile)
            inputStream?.let { UtilityIO.saveInputStream(context, it, "l2") }
            try {
                inputStream?.close()
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        val layers = mutableListOf<Drawable>()
        val colorDrawable = ColorDrawable(MyApplication.nexradRadarBackgroundColor)
        try {
            var bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            if (!product.contains("L2")) {
                if (product.contains("N0R") || product.contains("N0S") || product.contains("N0V") || product.contains(
                                "TR"
                        )
                ) {
                    UtilityNexradRadial4Bit.decodeAndPlot(
                            context,
                            bitmapCanvas,
                            "nids",
                            product
                    )
                } else {
                    UtilityNexradRadial8Bit.decodeAndPlot(
                            context,
                            bitmapCanvas,
                            "nids",
                            product
                    )
                }
            } else {
                UtilityNexradL2.decodeAndPlot(context, bitmapCanvas, product)
            }
            if (tdwr) {
                radarSite = ridTdwr
            }
            val citySize = 18
            UtilityCanvasMain.addCanvasItems(
                    context,
                    bitmapCanvas,
                    scaleType,
                    radarSite,
                    hwLineWidth,
                    citySize,
                    isInteractive
            )
            bitmapCanvas = UtilityImg.drawTextToBitmapForNexrad(context, bitmapCanvas)
            layers.add(colorDrawable)
            layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun layeredImgFromFile(
            context: Context,
            radarSiteArg: String,
            prod: String,
            idxStr: String,
            isInteractive: Boolean
    ): Bitmap {
        var radarSite = radarSiteArg
        var tdwr = false
        var ridTdwr = ""
        var scaleType = ProjectionType.WX_RENDER
        val hwLineWidth = 1
        if (prod == "TR0" || prod == "TV0" || prod == "TZL") {
            ridTdwr = WXGLNexrad.getTdwrFromRid(radarSite)
            tdwr = true
            radarSite = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val layers = mutableListOf<Drawable>()
        val colorDrawable = ColorDrawable(MyApplication.nexradRadarBackgroundColor)
        var bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
        if (!prod.contains("L2")) {
            if (prod.contains("N0R") || prod.contains("N0S") || prod.contains("N0V") || prod.contains("TR")) {
                UtilityNexradRadial4Bit.decodeAndPlot(
                        context,
                        bitmapCanvas,
                        "nids$idxStr",
                        prod
                )
            } else {
                UtilityNexradRadial8Bit.decodeAndPlot(
                        context,
                        bitmapCanvas,
                        "nids$idxStr",
                        prod
                )
            }
        } else {
            UtilityNexradL2.decodeAndPlot(context, bitmapCanvas, prod)
        }
        if (tdwr) {
            radarSite = ridTdwr
        }
        val citySize = 18
        UtilityCanvasMain.addCanvasItems(
                context,
                bitmapCanvas,
                scaleType,
                radarSite,
                hwLineWidth,
                citySize,
                isInteractive
        )
        bitmapCanvas = UtilityImg.drawTextToBitmapForNexrad(context, bitmapCanvas)
        layers.add(colorDrawable)
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun animationFromFiles(
            context: Context,
            rid1F: String,
            prod: String,
            frameCount: Int,
            idxStr: String,
            isInteractive: Boolean
    ): AnimationDrawable {
        var rid1 = rid1F
        val layerCnt = 3
        var scaleType = ProjectionType.WX_RENDER
        val ridTdwr: String
        if (prod == "TR0" || prod == "TV0" || prod == "TZL") {
            ridTdwr = WXGLNexrad.getTdwrFromRid(rid1)
            rid1 = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val nidsArr = Array(frameCount) { "" }
        (0 until frameCount).forEach {
            if (idxStr == "") {
                nidsArr[it] = "nexrad_anim$it"
            } else {
                nidsArr[it] = idxStr + prod + "nexrad_anim" + it.toString()
            }
        }
        val hwLineWidth = 1
        val animDrawable = AnimationDrawable()
        val bitmapCanvas = Bitmap.createBitmap(1000, 1000, Config.ARGB_8888)
        val cd = if (MyApplication.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        val bmArr = Array(frameCount) { UtilityImg.getBlankBitmap() }
        (0 until frameCount).forEach {
            bmArr[it] = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            if (prod.contains("N0R") || prod.contains("N0S") || prod.contains("N0V") || prod.contains(
                            "TR"
                    )
            ) {
                UtilityNexradRadial4Bit.decodeAndPlot(context, bmArr[it], nidsArr[it], prod)
            } else {
                UtilityNexradRadial8Bit.decodeAndPlot(
                        context,
                        bmArr[it],
                        nidsArr[it],
                        prod
                )
            }
        }
        val citySize = 20
        UtilityCanvasMain.addCanvasItems(
                context,
                bitmapCanvas,
                scaleType,
                rid1,
                hwLineWidth,
                citySize,
                isInteractive
        )
        val delay = UtilityImg.animInterval(context)
        val layers = arrayOfNulls<Drawable>(layerCnt)
        (0 until frameCount).forEach {
            layers[0] = cd
            layers[1] = BitmapDrawable(context.resources, bmArr[it])
            layers[2] = BitmapDrawable(context.resources, bitmapCanvas)
            animDrawable.addFrame(LayerDrawable(layers), delay)
        }
        (0 until frameCount).forEach { context.deleteFile(nidsArr[it]) }
        return animDrawable
    }

    fun bitmapForColorPalette(context: Context, product: String): Bitmap {
        val fileName = "nids_dvn_" + product + "_archive"
        UtilityIO.saveRawToInternalStorage(context, WXGLNexrad.productCodeStringToResourceFile[product] ?: R.raw.dvn94, fileName)
        val layers = mutableListOf<Drawable>()
        val colorDrawable = if (MyApplication.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        try {
            val bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            UtilityNexradRadial8Bit.decodeAndPlot(
                    context,
                    bitmapCanvas,
                    fileName,
                    WXGLNexrad.productCodeStringToCode[product] ?: "N0Q"
            )
            layers.add(colorDrawable)
            layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return UtilityImg.scaleBitmap(UtilityImg.layerDrawableToBitmap(layers), 300, 300)
    }
}
