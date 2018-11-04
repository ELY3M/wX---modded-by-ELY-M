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
import joshuatee.wx.R
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasMain
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog

import joshuatee.wx.NEXRAD_PRODUCT_STRING

object UtilityUSImgWX {

    private const val CANVAS_X = 1000
    private const val CANVAS_Y = 1000

    fun layeredImg(context: Context, rid1F: String, prod: String, isInteractive: Boolean): Bitmap {
        var rid1 = rid1F
        var tdwr = false
        var ridTdwr = ""
        var scaleType = ProjectionType.WX_RENDER
        val hwLineWidth = 1
        if (prod == "TR0" || prod == "TV0" || prod == "TZL") {
            ridTdwr = WXGLNexrad.getTDWRFromRID(rid1)
            tdwr = true
            rid1 = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val ridPrefix = UtilityWXOGL.getRidPrefix(rid1, tdwr)
        val inputStream: InputStream?
        if (!prod.contains("L2")) {
            inputStream = UtilityDownload.getInputStreamFromURL(MyApplication.NWS_RADAR_PUB + "SL.us008001/DF.of/DC.radar/" + NEXRAD_PRODUCT_STRING[prod] + "/SI." + ridPrefix + rid1.toLowerCase(Locale.US) + "/sn.last")
            inputStream?.let { UtilityIO.saveInputStream(context, it, "nids") }
        } else {
            val wd = WXGLDownload()
            val remoteFile = wd.iowaMesoL2(rid1)
            inputStream = UtilityDownload.getInputStreamFromURL(remoteFile)
            inputStream?.let { UtilityIO.saveInputStream(context, it, "l2") }
            try {
                inputStream?.close()
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
        val layers = mutableListOf<Drawable>()
        val cd = ColorDrawable(MyApplication.nexradRadarBackgroundColor)
        try {
            var bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            if (!prod.contains("L2")) {
                if (prod.contains("N0R") || prod.contains("N0S") || prod.contains("N0V") || prod.contains("TR")) {
                    UtilityNexradRadial4Bit.decodeAndPlotNexrad(context, bitmapCanvas, "nids", prod)
                } else {
                    UtilityNexradRadial8Bit.decodeAndPlotNexradDigital(context, bitmapCanvas, "nids", prod)
                }
            } else {
                UtilityNexradL2.decodeAndPlotNexradL2(context, bitmapCanvas, prod)
            }
            if (tdwr) {
                rid1 = ridTdwr
            }
            val citySize = 18
            UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, rid1, hwLineWidth, citySize, isInteractive)
            bitmapCanvas = UtilityImg.drawTextToBitmapForNexrad(context, bitmapCanvas)
            layers.add(cd)
            layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun layeredImgFromFile(context: Context, rid1F: String, prod: String, idxStr: String, isInteractive: Boolean): Bitmap {
        var rid = rid1F
        var tdwr = false
        var ridTdwr = ""
        var scaleType = ProjectionType.WX_RENDER
        val hwLineWidth = 1
        if (prod == "TR0" || prod == "TV0" || prod == "TZL") {
            ridTdwr = WXGLNexrad.getTDWRFromRID(rid)
            tdwr = true
            rid = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val layers = mutableListOf<Drawable>()
        val cd = ColorDrawable(MyApplication.nexradRadarBackgroundColor)
        var bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
        if (!prod.contains("L2")) {
            if (prod.contains("N0R") || prod.contains("N0S") || prod.contains("N0V") || prod.contains("TR")) {
                UtilityNexradRadial4Bit.decodeAndPlotNexrad(context, bitmapCanvas, "nids$idxStr", prod)
            } else {
                UtilityNexradRadial8Bit.decodeAndPlotNexradDigital(context, bitmapCanvas, "nids$idxStr", prod)
            }
        } else {
            UtilityNexradL2.decodeAndPlotNexradL2(context, bitmapCanvas, prod)
        }
        if (tdwr) {
            rid = ridTdwr
        }
        val citySize = 18
        UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, rid, hwLineWidth, citySize, isInteractive)
        bitmapCanvas = UtilityImg.drawTextToBitmapForNexrad(context, bitmapCanvas)
        layers.add(cd)
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun animationFromFiles(context: Context, rid1F: String, prod: String, frameCntStr: String, idxStr: String, isInteractive: Boolean): AnimationDrawable {
        var rid1 = rid1F
        val layerCnt = 3
        var scaleType = ProjectionType.WX_RENDER
        val frameCnt = frameCntStr.toIntOrNull() ?: 0
        val ridTdwr: String
        if (prod == "TR0" || prod == "TV0" || prod == "TZL") {
            ridTdwr = WXGLNexrad.getTDWRFromRID(rid1)
            rid1 = ridTdwr
            scaleType = ProjectionType.WX_RENDER_48
        }
        val nidsArr = Array(frameCntStr.toIntOrNull() ?: 0) { _ -> "" }
        (0 until frameCnt).forEach {
            if (idxStr == "") {
                nidsArr[it] = "nexrad_anim" + it.toString()
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
        val bmArr = Array(frameCnt) { _ -> UtilityImg.getBlankBitmap() }
        (0 until frameCnt).forEach {
            bmArr[it] = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            if (prod.contains("N0R") || prod.contains("N0S") || prod.contains("N0V") || prod.contains("TR")) {
                UtilityNexradRadial4Bit.decodeAndPlotNexrad(context, bmArr[it], nidsArr[it], prod)
            } else {
                UtilityNexradRadial8Bit.decodeAndPlotNexradDigital(context, bmArr[it], nidsArr[it], prod)
            }
        }
        val citySize = 20
        UtilityCanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, rid1, hwLineWidth, citySize, isInteractive)
        val delay = UtilityImg.animInterval(context)
        val layers = arrayOfNulls<Drawable>(layerCnt)
        (0 until (frameCntStr.toIntOrNull() ?: 0)).forEach {
            layers[0] = cd
            layers[1] = BitmapDrawable(context.resources, bmArr[it])
            layers[2] = BitmapDrawable(context.resources, bitmapCanvas)
            animDrawable.addFrame(LayerDrawable(layers), delay)
        }
        (0 until frameCnt).forEach { context.deleteFile(nidsArr[it]) }
        return animDrawable
    }

    fun bitmapForColorPalette(context: Context, prod: String): Bitmap {
        val fileName: String
        if (prod == "N0Q") {
            fileName = "nids_dvn_94_archive"
            UtilityIO.saveRawToInternalStorage(context, R.raw.dvn94, fileName)
        } else {
            fileName = "nids_dvn_99_archive"
            UtilityIO.saveRawToInternalStorage(context, R.raw.dvn99, fileName)
        }
        val layers = mutableListOf<Drawable>()
        val cd = if (MyApplication.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        try {
            val bitmapCanvas = Bitmap.createBitmap(CANVAS_X, CANVAS_Y, Config.ARGB_8888)
            UtilityNexradRadial8Bit.decodeAndPlotNexradDigital(context, bitmapCanvas, fileName, prod)
            layers.add(cd)
            layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
        return UtilityImg.scaleBitmap(UtilityImg.layerDrawableToBitmap(layers), 300, 300)
    }
}
