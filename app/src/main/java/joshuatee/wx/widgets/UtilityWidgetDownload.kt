/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

package joshuatee.wx.widgets

import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.getImage
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.nhc.Nhc
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.radar.Metar
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.CurrentConditions
import joshuatee.wx.util.DownloadImage
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.Hazards
import joshuatee.wx.util.SevenDay
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.wpc.UtilityWpcImages

internal object UtilityWidgetDownload {

    fun getWidgetData(context: Context) {
        UtilityLog.d("WX", "background widget download")
        (1..Location.numLocations).forEach {
            val locNum = it.toString()
            val locNumInt = To.int(locNum) - 1
            val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
            val widgetsEnabled = Utility.readPref(context, "WIDGETS_ENABLED", "false").startsWith("t")
            val ccUpdateInterval = Utility.readPrefInt(context, "CC_NOTIFICATION_INTERVAL", 30)
            if (Location.locations.size > locNumInt && widgetLocNum == locNum && widgetsEnabled) {
                val currentUpdateTime = ObjectDateTime.currentTimeMillis()
                val lastUpdateTime = Utility.readPrefLong(context, "WIDGET_DOWNLOAD" + locNum + "_LAST_UPDATE", 0.toLong())
                if (currentUpdateTime > lastUpdateTime + 1000 * 60 * ccUpdateInterval) {
                    val currentConditions = CurrentConditions(context, locNumInt)
                    currentConditions.timeCheck(context)
                    val hazards = Hazards(locNumInt)
                    val sevenDay = SevenDay(locNumInt)
                    val updateTime = ObjectDateTime.currentTimeMillis()
                    Utility.writePrefLong(context, "WIDGET_DOWNLOAD" + locNum + "_LAST_UPDATE", updateTime)
                    UtilityWidget.widgetDownloadData(context, currentConditions, sevenDay, hazards)
                }
            }
        }
    }

    fun download(context: Context, widgetType: WidgetFile) {
        when (widgetType) {
            WidgetFile.NEXRAD_RADAR -> nexrad(context)
            WidgetFile.SPCMESO -> generic(context, WidgetFile.SPCMESO, "SPCMESO1")
            WidgetFile.STRPT -> generic(context, WidgetFile.STRPT, "STRPT")
            WidgetFile.CONUSWV -> generic(context, WidgetFile.CONUSWV, "CONUSWV")
            WidgetFile.SPCSWO -> downloadSpcSwo(context)
            WidgetFile.WPCIMG -> wpcImage(context, widgetType)
            WidgetFile.NHC -> nhc(context, widgetType)
            WidgetFile.VIS -> vis(context)
            WidgetFile.HWO -> hwo(context)
            WidgetFile.TEXT_WPC -> textWpc(context)
            WidgetFile.AFD -> afd(context)
            WidgetFile.MOSAIC_RADAR -> radarMosaic(context)
            else -> {}
        }
    }

    private fun nexrad(context: Context) {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val rid = Location.getRid(context, widgetLocationNumber)
        //
        // Warnings
        //
        PolygonWarning.byType.values.forEach {
            if (it.isEnabled) {
                it.download()
            }
        }
        //
        // MPD, MCD / Watch download/update
        //
        listOf(PolygonType.WATCH, PolygonType.MCD, PolygonType.MPD).forEach {
            if (it.pref) {
                PolygonWatch.byType[it]!!.download(context)
            }
        }
        //
        // Wind barbs and observations
        //
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            Metar.get(context, rid, 5)
        }
        try {
            val bitmap = if (Location.isUS(widgetLocationNumber)) {
                UtilityImg.getNexradRefBitmap(context, rid)
            } else {
                UtilityImg.getBlankBitmap()
            }
            saveImage(context, bitmap, WidgetFile.NEXRAD_RADAR.fileName)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun generic(context: Context, type: WidgetFile, product: String) {
        val bitmap = DownloadImage.byProduct(context, product)
        saveImage(context, bitmap, type.fileName)
    }

    private fun downloadSpcSwo(context: Context) {
        listOf("1", "2", "3", "4").forEach {
            val bitmap = DownloadImage.byProduct(context, "SWOD$it")
            saveImage(context, bitmap, WidgetFile.SPCSWO.fileName + it)
        }
    }

    private fun vis(context: Context) {
        try {
            val bitmap = DownloadImage.byProduct(context, "GOES16")
            saveImage(context, bitmap, WidgetFile.VIS.fileName)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    // save image to 2 different files to workaround android widget update strangeness
    private fun saveImage(context: Context, bitmap: Bitmap, fileName: String) {
        UtilityLog.d("WXRADAR", "widget save attempt to $fileName")
        val fileOutputStream = getFileOutputStream(context, fileName)
        if (fileOutputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        }
        fileOutputStream?.close()
        val fos2 = getFileOutputStream(context, GlobalVariables.WIDGET_FILE_BAK + fileName)
        if (fos2 != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos2)
        }
        fos2?.close()
    }

    private fun hwo(context: Context) {
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        var wfo = Utility.readPref(context, "NWS$widgetLocationNumber", "").uppercase(Locale.US)
        if (Utility.readPref(context, "WFO_REMEMBER_LOCATION", "") == "true") {
            wfo = Utility.readPref(context, "WFO_LAST_USED", wfo).uppercase(Locale.US)
        }
        val hwoText = DownloadText.byProduct(context, "HWO$wfo")
                .replaceFirst("<BR>[A-Z][A-Z]Z.*?[0-9]{4}<BR>".toRegex(), "")
        Utility.writePref(context, "HWO_WIDGET", hwoText)
        Utility.commitPref(context)
    }

    private fun textWpc(context: Context) {
        val text = DownloadText.byProduct(context, UIPreferences.wpcTextFav)
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
            Utility.writePref(context, "AFD_WIDGET", DownloadText.byProduct(context, "VFD$wfo"))
        } else {
            Utility.writePref(context, "AFD_WIDGET", DownloadText.byProduct(context, "AFD$wfo"))
        }
    }

    private fun radarMosaic(context: Context) {
        try {
            saveImage(context, DownloadImage.radarMosaic(context), WidgetFile.MOSAIC_RADAR.fileName)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun getFileOutputStream(context: Context, fileName: String): FileOutputStream? {
        var fileOutputStream: FileOutputStream? = null
        try {
            val dir = File(context.filesDir.toString() + "/shared")
            if (dir.exists() && dir.isDirectory) {
                UtilityLog.d("WXRADAR", "getFileOutputStream: shared exists and is dir")
            } else if (!dir.mkdirs()) {
                UtilityLog.d("WXRADAR", "getFileOutputStream: failed to mkdir: " + context.filesDir + "/shared")
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
        val bitmap1 = Nhc.widgetImageUrlBottom.getImage()
        val bitmap2 = Nhc.widgetImageUrlTop.getImage()
        val combinedBitmap = UtilityImg.mergeImagesVertically(listOf(bitmap1, bitmap2))
        saveImage(context, combinedBitmap, type.fileName + "0")
    }
}
