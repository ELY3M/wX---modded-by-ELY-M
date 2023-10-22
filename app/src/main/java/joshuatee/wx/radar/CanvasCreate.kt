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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import joshuatee.wx.getInputStream
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.R
import joshuatee.wx.settings.RadarPreferences

object CanvasCreate {

    private const val imageWidth = 1000
    private const val imageHeight = 1000
    private const val citySize = 18

    fun layeredImage(context: Context, radarSiteArg: String, product: String): Bitmap {
        var radarSite = radarSiteArg
        var scaleType = ProjectionType.WX_RENDER
        if (NexradUtil.isProductTdwr(product)) {
            radarSite = NexradUtil.getTdwrFromRid(radarSite)
            scaleType = ProjectionType.WX_RENDER_48
        }
        if (!product.contains("L2")) {
            val url = NexradDownload.getRadarFileUrl(radarSite, product)
            val inputStream = url.getInputStream()
            inputStream?.let { UtilityIO.saveInputStream(context, it, "nids") }
            inputStream?.close()
        } else {
            val remoteFile = NexradDownload.getLevel2Url(radarSite)
            val inputStream = remoteFile.getInputStream()
            inputStream?.let { UtilityIO.saveInputStream(context, it, "l2") }
            try {
                inputStream?.close()
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        val layers = mutableListOf<Drawable>()
        val colorDrawable = ColorDrawable(RadarPreferences.nexradBackgroundColor)
        try {
            var bitmapCanvas = Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888)
            if (!product.startsWith("L2")) {
                // TODO FIXME method to detect 4bit project?
                if (product.contains("N0R") || product.contains("N0S") || product.contains("N0V") || product.startsWith("TV")) {
                    CanvasRadial4Bit.decodeAndPlot(context, bitmapCanvas, "nids", product)
                } else {
                    CanvasRadial8Bit.decodeAndPlot(context, bitmapCanvas, "nids", product)
                }
            } else {
                CanvasLevel2.decodeAndPlot(context, bitmapCanvas, product)
            }
            CanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, radarSite, citySize)
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

    fun layeredImageFromFile(context: Context, radarSiteArg: String, product: String, idxStr: String): Bitmap {
        var radarSite = radarSiteArg
        var scaleType = ProjectionType.WX_RENDER
        if (NexradUtil.isProductTdwr(product)) {
            radarSite = NexradUtil.getTdwrFromRid(radarSite)
            scaleType = ProjectionType.WX_RENDER_48
        }
        val layers = mutableListOf<Drawable>()
        val colorDrawable = ColorDrawable(RadarPreferences.nexradBackgroundColor)
        var bitmapCanvas = Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888)
        if (!product.startsWith("L2")) {
            // TODO FIXME method to detect 4bit project?
            if (product.contains("N0R") || product.contains("N0S") || product.contains("N0V") || product.startsWith("TV")) {
                CanvasRadial4Bit.decodeAndPlot(context, bitmapCanvas, "nids$idxStr", product)
            } else {
                CanvasRadial8Bit.decodeAndPlot(context, bitmapCanvas, "nids$idxStr", product)
            }
        } else {
            CanvasLevel2.decodeAndPlot(context, bitmapCanvas, product)
        }
        CanvasMain.addCanvasItems(context, bitmapCanvas, scaleType, radarSite, citySize)
        bitmapCanvas = UtilityImg.drawText(context, bitmapCanvas)
        layers.add(colorDrawable)
        layers.add(BitmapDrawable(context.resources, bitmapCanvas))
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun bitmapForColorPalette(context: Context, product: Int): Bitmap {
        val fileName = "nids_dvn_" + product + "_archive"
        UtilityIO.saveRawToInternalStorage(context, NexradUtil.productCodeStringToResourceFile[product]
                ?: R.raw.dvn94, fileName)
        val layers = mutableListOf<Drawable>()
        val colorDrawable = if (RadarPreferences.blackBg) {
            ColorDrawable(Color.BLACK)
        } else {
            ColorDrawable(Color.WHITE)
        }
        try {
            val bitmapCanvas = Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888)
            CanvasRadial8Bit.decodeAndPlot(context, bitmapCanvas, fileName, NexradUtil.productCodeStringToCode[product]
                    ?: "N0Q")
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
