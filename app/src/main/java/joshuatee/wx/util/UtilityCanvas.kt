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
//modded by ELY M.  

package joshuatee.wx.util

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.graphics.drawable.BitmapDrawable
import android.util.Log

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.objects.GeographyType

internal object UtilityCanvas {

    val TAG = "joshuatee UtilityCanvas"
    fun addWarnings(context: Context, provider: ProjectionType, bitmap: Bitmap, rid: String) {
        val mercato = provider.isMercator
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        val wallpath = Path()
        wallpath.reset()
        val paintList = listOf(
	MyApplication.radarColorTor, 
	MyApplication.radarColorSvr, 
	MyApplication.radarColorEww, 
	MyApplication.radarColorFfw, 
	MyApplication.radarColorSmw, 
	MyApplication.radarColorSvr, 
	MyApplication.radarColorSps
	)
        val warningDataList = listOf(
	MyApplication.severeDashboardTor.valueGet(), 
	MyApplication.severeDashboardSvr.valueGet(), 
	MyApplication.severeDashboardEww.valueGet(), 
	MyApplication.severeDashboardFfw.valueGet(), 
	MyApplication.severeDashboardSmw.valueGet(), 
	MyApplication.severeDashboardSvs.valueGet(), 
	MyApplication.severeDashboardSps.valueGet()
	)
        if (provider.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val pn = ProjectionNumbers(context, rid, provider)
        paint.strokeWidth = pn.polygonWidth.toFloat()
        warningDataList.forEachIndexed { idx, it ->
            paint.color = paintList[idx]
            var warningHTML = it.replace("\n", "")
            warningHTML = warningHTML.replace(" ", "")
            val warningAl =
                UtilityString.parseColumnMutable(warningHTML, RegExp.warningLatLonPattern)
            val vtecAl = warningHTML.parseColumn(RegExp.warningVtecPattern)
            warningAl.forEachIndexed { i, warn ->
                warningAl[i] =
                        warn.replace("[", "").replace("]", "").replace(",", " ").replace("-", "")
            }
            canvasDrawWarningsNewAPI(warningAl, vtecAl, canvas, wallpath, paint, mercato, pn)
        }
    }

    fun drawCitiesUS(
        context: Context,
        provider: ProjectionType,
        bitmap: Bitmap,
        rid: String,
        textSize: Int
    ) {
        val mercator = provider.isMercator
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = GeographyType.CITIES.color
        if (provider.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        if (provider.needsBlackPaint) {
            paint.color = Color.rgb(0, 0, 0)
        }
        paint.textSize = textSize.toFloat()
        val pn = ProjectionNumbers(context, rid, provider)
        var pixXInit: Double
        var pixYInit: Double
        var tmpCoords: DoubleArray
        UtilityCities.CITY_OBJ.indices.forEach {
            tmpCoords = if (mercator) {
                UtilityCanvasProjection.computeMercatorNumbers(
                    UtilityCities.CITY_OBJ[it]!!.x,
                    UtilityCities.CITY_OBJ[it]!!.y,
                    pn
                )
            } else {
                UtilityCanvasProjection.compute4326Numbers(
                    UtilityCities.CITY_OBJ[it]!!.x,
                    UtilityCities.CITY_OBJ[it]!!.y,
                    pn
                )
            }
            pixXInit = tmpCoords[0]
            pixYInit = tmpCoords[1]
            if (textSize > 0) {
                canvas.drawText(
                    MyApplication.comma.split(UtilityCities.CITY_OBJ[it]!!.city)[0],
                    pixXInit.toFloat() + 4,
                    pixYInit.toFloat() - 4,
                    paint
                )
                canvas.drawCircle(pixXInit.toFloat(), pixYInit.toFloat(), 2f, paint)
            } else {
                canvas.drawCircle(pixXInit.toFloat(), pixYInit.toFloat(), 1f, paint)
            }
        }
    }

    fun addLocationDotForCurrentLocation(
        context: Context,
        provider: ProjectionType,
        bitmap: Bitmap,
        rid: String
    ) {
        val mercato = provider.isMercator
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.FILL
        paint.strokeWidth = 1.0f
        paint.color = MyApplication.radarColorLocdot
        if (provider.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val locXCurrent = Location.x
        var locYCurrent = Location.y
        locYCurrent = locYCurrent.replace("-", "")
        val pn = ProjectionNumbers(context, rid, provider)
        val x = locXCurrent.toDoubleOrNull() ?: 0.0
        val y = locYCurrent.toDoubleOrNull() ?: 0.0
        val tmpCoords: DoubleArray
        tmpCoords = if (mercato) {
            UtilityCanvasProjection.computeMercatorNumbers(x, y, pn)
        } else {
            UtilityCanvasProjection.compute4326Numbers(x, y, pn)
        }
        val pixXInit = tmpCoords[0]
        val pixYInit = tmpCoords[1]
        paint.color = MyApplication.radarColorLocdot
	//custom locationdot//
        val locationicon: Bitmap = BitmapFactory.decodeFile(MyApplication.FilesPath+"location.png");
        val locationiconresized: Bitmap = Bitmap.createScaledBitmap(locationicon, 63, 63, false)
        canvas.drawBitmap(locationiconresized, pixXInit.toFloat(), pixYInit.toFloat(), paint)
        ///canvas.drawCircle(pixXInit.toFloat(), pixYInit.toFloat(), 2f, paint)

    }

    fun addMCD(
        context: Context,
        provider: ProjectionType,
        bitmap: Bitmap,
        rid1: String,
        polyType: PolygonType
    ) {
        val mercato = provider.isMercator
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Style.STROKE
        paint.color = Color.rgb(255, 0, 0)
        val wallpath = Path()
        wallpath.reset()
        if (provider.needsCanvasShift) {
            canvas.translate(UtilityCanvasMain.xOffset, UtilityCanvasMain.yOffset)
        }
        val pn = ProjectionNumbers(context, rid1, provider)
        paint.strokeWidth = pn.polygonWidth.toFloat()
        paint.color = polyType.color
        var prefToken = ""
        when (polyType) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatlon.valueGet()
            PolygonType.MPD -> prefToken = MyApplication.mpdLatlon.valueGet()
            PolygonType.WATCH_SVR -> prefToken = MyApplication.watchLatlonSvr.valueGet()
            PolygonType.WATCH_TOR -> prefToken = MyApplication.watchLatlonTor.valueGet()
            else -> {
            }
        }
        val tmpArr = MyApplication.colon.split(prefToken).toList()
        canvasDrawWarnings(tmpArr, canvas, wallpath, paint, mercato, pn)
    }

    // used by MCD/WAT/MPD
    private fun canvasDrawWarnings(
        warningAl: List<String>,
        canvas: Canvas,
        wallpath: Path,
        paint: Paint,
        mercato: Boolean,
        pn: ProjectionNumbers
    ) {
        var pixXInit: Double
        var pixYInit: Double
        var tmpCoords: DoubleArray
        var pixX: Double
        var pixY: Double
        var testArr: Array<String>
        warningAl.forEach { warn ->
            testArr = MyApplication.space.split(warn)
            val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                it.toDoubleOrNull() ?: 0.0
            }
            val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                it.toDoubleOrNull() ?: 0.0
            }
            wallpath.reset()
            if (y.isNotEmpty() && x.isNotEmpty()) {
                tmpCoords = if (mercato) {
                    UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                } else {
                    UtilityCanvasProjection.compute4326Numbers(x[0], y[0], pn)
                }
                pixXInit = tmpCoords[0]
                pixYInit = tmpCoords[1]
                wallpath.moveTo(pixXInit.toFloat(), pixYInit.toFloat())
                if (x.size == y.size) {
                    (1 until x.size).forEach {
                        tmpCoords = if (mercato) {
                            UtilityCanvasProjection.computeMercatorNumbers(x[it], y[it], pn)
                        } else {
                            UtilityCanvasProjection.compute4326Numbers(x[it], y[it], pn)
                        }
                        pixX = tmpCoords[0]
                        pixY = tmpCoords[1]
                        wallpath.lineTo(pixX.toFloat(), pixY.toFloat())
                    }
                    wallpath.lineTo(pixXInit.toFloat(), pixYInit.toFloat())
                    canvas.drawPath(wallpath, paint)
                }
            }
        }
    }

    private fun canvasDrawWarningsNewAPI(
        warningAl: List<String>,
        vtecAl: List<String>,
        canvas: Canvas,
        wallpath: Path,
        paint: Paint,
        mercato: Boolean,
        pn: ProjectionNumbers
    ) {
        var pixXInit: Double
        var pixYInit: Double
        var tmpCoords: DoubleArray
        var pixX: Double
        var pixY: Double
        var polyCount = -1
        var testArr: Array<String>
        warningAl.forEach { warn ->
            polyCount += 1
            if (vtecAl.isNotEmpty() && vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith(
                    "0.CAN"
                )
            ) {
                testArr = MyApplication.space.split(warn)
                val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                wallpath.reset()
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    tmpCoords = if (mercato) {
                        UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                    } else {
                        UtilityCanvasProjection.compute4326Numbers(x[0], y[0], pn)
                    }
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    wallpath.moveTo(pixXInit.toFloat(), pixYInit.toFloat())
                    if (x.size == y.size) {
                        (1 until x.size).forEach {
                            tmpCoords = if (mercato) {
                                UtilityCanvasProjection.computeMercatorNumbers(x[it], y[it], pn)
                            } else {
                                UtilityCanvasProjection.compute4326Numbers(x[it], y[it], pn)
                            }
                            pixX = tmpCoords[0]
                            pixY = tmpCoords[1]
                            wallpath.lineTo(pixX.toFloat(), pixY.toFloat())
                        }
                        wallpath.lineTo(pixXInit.toFloat(), pixYInit.toFloat())
                        canvas.drawPath(wallpath, paint)
                    }
                }
            }
        }
    }
}
