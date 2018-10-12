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

package joshuatee.wx

import java.io.File
import java.io.FileOutputStream
import java.util.Locale

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.radar.UtilityUSImgNWSMosaic
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.canada.UtilityCanadaImg
import joshuatee.wx.objects.WidgetFile
import joshuatee.wx.objects.WidgetFile.*
import joshuatee.wx.settings.Location
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityUSImg
import joshuatee.wx.wpc.UtilityWPCImages

internal object UtilityWidgetDownload {

    fun download(context: Context, widgetType: WidgetFile) {
        when (widgetType) {
            NEXRAD_RADAR -> downloadNexrad(context)
            SPCMESO -> downloadGeneric(context, SPCMESO, "SPCMESO1")
            STRPT -> downloadGeneric(context, STRPT, "STRPT")
            CONUSWV -> downloadGeneric(context, CONUSWV, "CONUSWV")
            SPCSWO -> downloadSPCSWO(context)
            WPCIMG -> downloadWPCIMG(context, widgetType)
            NHC -> downloadNHC(context, widgetType)
            VIS -> downloadVis(context)
            HWO -> downloadHWO(context)
            TEXT_WPC -> downloadTextWPC(context)
            AFD -> downloadAFD(context)
            MOSAIC_RADAR -> downloadRadMosaic(context)
            else -> {
            }
        }
    }

    private fun downloadNexrad(context: Context) {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val rid = Location.getRid(context, widgetLocNum)
        try {
            val bitmap = if (Location.isUS(widgetLocNum)) {
                UtilityUSImg.getPreferredLayeredImg(context, rid, false)
            } else {
                UtilityCanadaImg.getRadarBitmapOptionsApplied(context, rid, "")
            }
            saveImage(context, bitmap, NEXRAD_RADAR.fileName)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    private fun downloadGeneric(context: Context, type: WidgetFile, prod: String) {
        val bitmap = UtilityDownload.getImgProduct(context, prod)
        saveImage(context, bitmap, type.fileName)
    }

    private fun downloadSPCSWO(context: Context) {
        listOf("1", "2", "3", "4").forEach {
            val bitmap = UtilityDownload.getImgProduct(context, "SWOD$it")
            saveImage(context, bitmap, SPCSWO.fileName + it)
        }
    }

    private fun downloadVis(context: Context) {
        try {
            val bitmap = UtilityDownload.getImgProduct(context, "GOES16")
            saveImage(context, bitmap, VIS.fileName)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    // save image to 2 different files to workaround android widget update strangeness
    private fun saveImage(context: Context, bitmap: Bitmap, fileName: String) {
        val fos = getFileOutputStream(context, fileName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos?.close()
        val fos2 = getFileOutputStream(context, MyApplication.WIDGET_FILE_BAK + fileName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos2)
        fos2?.close()
    }

    private fun downloadHWO(context: Context) {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        var nws1Current = Utility.readPref(context, "NWS$widgetLocNum", "").toUpperCase(Locale.US)
        if (Utility.readPref(context, "WFO_REMEMBER_LOCATION", "") == "true") {
            nws1Current = Utility.readPref(context, "WFO_LAST_USED", nws1Current).toUpperCase(Locale.US)
        }
        var hwoText = UtilityDownload.getTextProduct(context, "HWO$nws1Current")
        hwoText = hwoText.replaceFirst("<BR>[A-Z][A-Z]Z.*?[0-9]{4}<BR>".toRegex(), "")
        Utility.writePref(context, "HWO_WIDGET", hwoText)
        Utility.commitPref(context)
    }

    private fun downloadTextWPC(context: Context) {
        val text = UtilityDownload.getTextProduct(context, MyApplication.wpcTextFav)
        Utility.writePref(context, "TEXTWPC_WIDGET", text)
        Utility.commitPref(context)
    }

    private fun downloadAFD(context: Context) {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        var nws1Current = Utility.readPref(context, "NWS$widgetLocNum", "").toUpperCase(Locale.US)
        if (Utility.readPref(context, "WFO_REMEMBER_LOCATION", "") == "true") {
            nws1Current = Utility.readPref(context, "WFO_LAST_USED", nws1Current).toUpperCase(Locale.US)
        }
        if (Utility.readPref(context, "WFO_TEXT_FAV", "").startsWith("VFD")) {
            Utility.writePref(context, "AFD_WIDGET", UtilityDownload.getTextProduct(context, "VFD$nws1Current"))
        } else {
            Utility.writePref(context, "AFD_WIDGET", UtilityDownload.getTextProduct(context, "AFD$nws1Current"))
        }
    }

    private fun downloadRadMosaic(context: Context) {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val rid1 = Location.getRid(context, widgetLocNum)
        try {
            val ridLoc = Utility.readPref(context, "RID_LOC_$rid1", "")
            val nwsLocationArr = ridLoc.split(",").dropLastWhile { it.isEmpty() }
            val state = nwsLocationArr[0]
            var k = Utility.readPref(context, "WIDGET_RADAR_LEVEL", "1km")
            when (k) {
                "regional" -> k = "regional"
                "usa" -> k = "usa"
            }
            val bitmap = if (Location.isUS(widgetLocNum)) {
                if (k == "usa") {
                    UtilityUSImgNWSMosaic.nwsMosaic(context, "latest", false)
                } else {
                    UtilityUSImgNWSMosaic.nwsMosaic(context, UtilityUSImgNWSMosaic.getNWSSectorFromState(state), false)
                }
            } else {
                val prov = Utility.readPref(context, "NWS" + widgetLocNum + "_STATE", "")
                UtilityCanadaImg.getRadarMosaicBitmapOptionsApplied(context, UtilityCanada.getECSectorFromProv(prov))
            }
            saveImage(context, bitmap, MOSAIC_RADAR.fileName)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    private fun getFileOutputStream(context: Context, fileName: String): FileOutputStream? {
        var fos: FileOutputStream? = null
        try {
            val dir = File(context.filesDir.toString() + "/shared")
            if (!dir.mkdirs())
                UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
            val file = File(dir, fileName)
            fos = FileOutputStream(file)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return fos
    }

    private fun downloadWPCIMG(context: Context, type: WidgetFile) {
        val imgUrl = Utility.readPref(context, "WPG_IMG_FAV_URL", UtilityWPCImages.PARAMS[0])
        val bitmap = imgUrl.getImage()
        saveImage(context, bitmap, type.fileName)
    }

    private fun downloadNHC(context: Context, type: WidgetFile) {
        val bm1 = "http://www.nhc.noaa.gov/xgtwo/two_atl_0d0.png".getImage()
        val bm2 = "http://www.nhc.noaa.gov/xgtwo/two_pac_0d0.png".getImage()
        saveImage(context, bm1, type.fileName + "0")
        saveImage(context, bm2, type.fileName + "1")
    }
}
