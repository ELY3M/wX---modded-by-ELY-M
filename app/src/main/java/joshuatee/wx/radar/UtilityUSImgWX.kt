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

package joshuatee.wx.radar

import java.io.InputStream
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import joshuatee.wx.Extensions.getInputStream
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.R
import joshuatee.wx.settings.RadarPreferences

object UtilityUSImgWX {

    private const val CANVAS_X = 1000
    private const val CANVAS_Y = 1000

    fun layeredImg(context: Context, radarSiteArg: String, product: String, isInteractive: Boolean): Bitmap {
        var radarSite = radarSiteArg
        var tdwr = false
        var ridTdwr = ""
        var scaleType = ProjectionType.WX_RENDER
        val hwLineWidth = 1
        if (WXGLNexrad.isProductTdwr(product)) {
            ridTdwr = WXGLNexrad.getTdwrFromRid(radarSite)
            tdwr = true
            radarSite = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val inputStream: InputStream?
        if (!product.contains("L2")) {
            val url = WXGLDownload.getRadarFileUrl(radarSite, product, tdwr)
            inputStream = url.getInputStream()
            inputStream?.let { UtilityIO.saveInputStream(context, it, "nids") }
        } else {
            val remoteFile = WXGLDownload.getLevel2Url(radarSite)
            inputStream = remoteFile.getInputStream()
            inputStream?.let { UtilityIO.saveInputStream(context, it, "l2") }
            try {
                inputStream?.close()
            } catch (e: Exception) { UtilityLog.handleException(e) }
        }
        val layers = mutableListOf<Drawable>()
        val colorDrawable = ColorDrawable(RadarPreferences.nexradRadarBackgroundColor)
        try {
            var bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            if (!product.startsWith("L2")) {
                if (product.contains("N0R") || product.contains("N0S") || product.contains("N0V") || product.startsWith("TV")) {
                    UtilityNexradRadial4Bit.decodeAndPlot(context, bitmapCanvas, "nids", product)
                } else {
                    UtilityNexradRadial8Bit.decodeAndPlot(context, bitmapCanvas, "nids", product)
                }
            } else {
                UtilityNexradL2.decodeAndPlot(context, bitmapCanvas, product)
            }
            if (tdwr) {
                radarSite = ridTdwr
            }
            val citySize = 18
            UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, radarSite, hwLineWidth, citySize, isInteractive)
            bitmapCanvas = UtilityImg.drawText(context, bitmapCanvas)
            layers.add(colorDrawable)
            layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun layeredImgFromFile(context: Context, radarSiteArg: String, product: String, idxStr: String, isInteractive: Boolean): Bitmap {
        var radarSite = radarSiteArg
        var tdwr = false
        var ridTdwr = ""
        var scaleType = ProjectionType.WX_RENDER
        val hwLineWidth = 1
        if (WXGLNexrad.isProductTdwr(product)) {
            ridTdwr = WXGLNexrad.getTdwrFromRid(radarSite)
            tdwr = true
            radarSite = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val layers = mutableListOf<Drawable>()
        val colorDrawable = ColorDrawable(RadarPreferences.nexradRadarBackgroundColor)
        var bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
        if (!product.startsWith("L2")) {
            if (product.contains("N0R") || product.contains("N0S") || product.contains("N0V") || product.startsWith("TV")) {
                UtilityNexradRadial4Bit.decodeAndPlot(context, bitmapCanvas, "nids$idxStr", product)
            } else {
                UtilityNexradRadial8Bit.decodeAndPlot(context, bitmapCanvas, "nids$idxStr", product)
            }
        } else {
            UtilityNexradL2.decodeAndPlot(context, bitmapCanvas, product)
        }
        if (tdwr) {
            radarSite = ridTdwr
        }
        val citySize = 18
        UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, radarSite, hwLineWidth, citySize, isInteractive)
        bitmapCanvas = UtilityImg.drawText(context, bitmapCanvas)
        layers.add(colorDrawable)
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun animationFromFiles(context: Context, radarSiteOriginal: String, product: String, frameCount: Int, idxStr: String, isInteractive: Boolean): AnimationDrawable {
        var radarSite = radarSiteOriginal
        var scaleType = ProjectionType.WX_RENDER
        val ridTdwr: String
        if (WXGLNexrad.isProductTdwr(product)) {
            ridTdwr = WXGLNexrad.getTdwrFromRid(radarSite)
            radarSite = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val fileList = Array(frameCount) { "" }
        (0 until frameCount).forEach {
            if (idxStr == "") {
                fileList[it] = "nexrad_anim$it"
            } else {
                fileList[it] = idxStr + product + "nexrad_anim" + it.toString()
            }
        }
        val hwLineWidth = 1
        val animDrawable = AnimationDrawable()
        val bitmapCanvas = Bitmap.createBitmap(1000, 1000, Config.ARGB_8888)
        val cd = if (RadarPreferences.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        val bitmaps = Array(frameCount) { UtilityImg.getBlankBitmap() }
        (0 until frameCount).forEach {
            bitmaps[it] = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            if (product.contains("N0R") || product.contains("N0S") || product.contains("N0V") || product.startsWith("TV")) {
                UtilityNexradRadial4Bit.decodeAndPlot(context, bitmaps[it], fileList[it], product)
            } else {
                UtilityNexradRadial8Bit.decodeAndPlot(context, bitmaps[it], fileList[it], product)
            }
        }
        val citySize = 20
        UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, radarSite, hwLineWidth, citySize, isInteractive)
        val delay = UtilityImg.animInterval(context)
        (0 until frameCount).forEach {
            val layers = arrayOf(cd, BitmapDrawable(context.resources, bitmaps[it]), BitmapDrawable(context.resources, bitmapCanvas))
            animDrawable.addFrame(LayerDrawable(layers), delay)
        }
        (0 until frameCount).forEach {
            context.deleteFile(fileList[it])
        }
        return animDrawable
    }

    fun bitmapForColorPalette(context: Context, product: Int): Bitmap {
        val fileName = "nids_dvn_" + product + "_archive"
        UtilityIO.saveRawToInternalStorage(context, WXGLNexrad.productCodeStringToResourceFile[product] ?: R.raw.dvn94, fileName)
        val layers = mutableListOf<Drawable>()
        val colorDrawable = if (RadarPreferences.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        try {
            val bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            UtilityNexradRadial8Bit.decodeAndPlot(context, bitmapCanvas, fileName, WXGLNexrad.productCodeStringToCode[product] ?: "N0Q")
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
