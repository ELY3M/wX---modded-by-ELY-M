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

package joshuatee.wx

import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.nhc.UtilityNhc
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.objects.WidgetFile.*
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.*
import joshuatee.wx.wpc.UtilityWpcImages

internal object UtilityWidgetDownload {

    fun download(context: Context, widgetType: WidgetFile) {
        when (widgetType) {
            NEXRAD_RADAR -> nexrad(context)
            SPCMESO -> generic(context, SPCMESO, "SPCMESO1")
            STRPT -> generic(context, STRPT, "STRPT")
            CONUSWV -> generic(context, CONUSWV, "CONUSWV")
            SPCSWO -> downloadSpcSwo(context)
            WPCIMG -> wpcImage(context, widgetType)
            NHC -> nhc(context, widgetType)
            VIS -> vis(context)
            HWO -> hwo(context)
            TEXT_WPC -> textWpc(context)
            AFD -> afd(context)
            MOSAIC_RADAR -> radarMosaic(context)
            else -> {}
        }
    }

    private fun nexrad(context: Context) {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val rid = Location.getRid(context, widgetLocationNumber)
        try {
            val bitmap = if (Location.isUS(widgetLocationNumber)) {
                UtilityImg.getNexradRefBitmap(context, rid, false)
            } else {
                UtilityImg.getBlankBitmap()
            }
            saveImage(context, bitmap, NEXRAD_RADAR.fileName)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun generic(context: Context, type: WidgetFile, product: String) {
        val bitmap = UtilityDownload.getImageProduct(context, product)
        saveImage(context, bitmap, type.fileName)
    }

    private fun downloadSpcSwo(context: Context) {
        listOf("1", "2", "3", "4").forEach {
            val bitmap = UtilityDownload.getImageProduct(context, "SWOD$it")
            saveImage(context, bitmap, SPCSWO.fileName + it)
        }
    }

    private fun vis(context: Context) {
        try {
            val bitmap = UtilityDownload.getImageProduct(context, "GOES16")
            saveImage(context, bitmap, VIS.fileName)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    // save image to 2 different files to workaround android widget update strangeness
    private fun saveImage(context: Context, bitmap: Bitmap, fileName: String) {
        val fileOutputStream = getFileOutputStream(context, fileName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream?.close()
        val fos2 = getFileOutputStream(context, GlobalVariables.WIDGET_FILE_BAK + fileName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos2)
        fos2?.close()
    }

    private fun hwo(context: Context) {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        var wfo = Utility.readPref(context, "NWS$widgetLocationNumber", "").uppercase(Locale.US)
        if (Utility.readPref(context, "WFO_REMEMBER_LOCATION", "") == "true") {
            wfo = Utility.readPref(context, "WFO_LAST_USED", wfo).uppercase(Locale.US)
        }
        var hwoText = UtilityDownload.getTextProduct(context, "HWO$wfo")
        hwoText = hwoText.replaceFirst("<BR>[A-Z][A-Z]Z.*?[0-9]{4}<BR>".toRegex(), "")
        Utility.writePref(context, "HWO_WIDGET", hwoText)
        Utility.commitPref(context)
    }

    private fun textWpc(context: Context) {
        val text = UtilityDownload.getTextProduct(context, UIPreferences.wpcTextFav)
        Utility.writePref(context, "TEXTWPC_WIDGET", text)
        Utility.commitPref(context)
    }

    private fun afd(context: Context) {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        var wfo = Utility.readPref(context, "NWS$widgetLocationNumber", "").uppercase(Locale.US)
        if (Utility.readPref(context, "WFO_REMEMBER_LOCATION", "") == "true") {
            wfo = Utility.readPref(context, "WFO_LAST_USED", wfo).uppercase(Locale.US)
        }
        if (Utility.readPref(context, "WFO_TEXT_FAV", "").startsWith("VFD")) {
            Utility.writePref(context, "AFD_WIDGET", UtilityDownload.getTextProduct(context, "VFD$wfo"))
        } else {
            Utility.writePref(context, "AFD_WIDGET", UtilityDownload.getTextProduct(context, "AFD$wfo"))
        }
    }

    private fun radarMosaic(context: Context) {
        try {
            saveImage(context, UtilityDownload.getRadarMosaic(context), MOSAIC_RADAR.fileName)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun getFileOutputStream(context: Context, fileName: String): FileOutputStream? {
        var fileOutputStream: FileOutputStream? = null
        try {
            val dir = File(context.filesDir.toString() + "/shared")
            if (!dir.mkdirs()) {
                UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
            }
            val file = File(dir, fileName)
            fileOutputStream = FileOutputStream(file)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return fileOutputStream
    }

    private fun wpcImage(context: Context, type: WidgetFile) {
        val url = Utility.readPref(context, "WPG_IMG_FAV_URL", UtilityWpcImages.urls[0])
        val bitmap = url.getImage()
        saveImage(context, bitmap, type.fileName)
    }

    private fun nhc(context: Context, type: WidgetFile) {
        val bitmap1 = UtilityNhc.widgetImageUrlBottom.getImage()
        val bitmap2 = UtilityNhc.widgetImageUrlTop.getImage()
        val combinedBitmap = UtilityImg.mergeImagesVertically(listOf(bitmap1, bitmap2))
        saveImage(context, combinedBitmap, type.fileName + "0")
    }
}
