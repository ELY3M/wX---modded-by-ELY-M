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

package joshuatee.wx.canada

import android.content.Context
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.GeographyType

object UtilityCanadaImg {

    internal fun getGOESAnim(context: Context, url: String): AnimationDrawable {
        val region = url.parse("goes_(.*?)_")
        val imgType = url.parse("goes_.*?_(.*?)_")
        val urlAnim = "https://weather.gc.ca/satellite/satellite_anim_e.html?sat=goes&area=$region&type=$imgType"
        val html = urlAnim.getHtml()
        val times = html.parseColumn(">([0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}h[0-9]{2}m)</option>")
        val frameCnt = 10
        val delay = UtilityImg.animInterval(context)
        val urlAl = mutableListOf<String>()
        if (times.size > frameCnt)
            (times.size - frameCnt until times.size).mapTo(urlAl) { "https://weather.gc.ca/data/satellite/goes_" + region + "_" + imgType + "_m_" + times[it].replace(" ", "_").replace("/", "@") + ".jpg" }
        return UtilityImgAnim.getAnimationDrawableFromURLList(context, urlAl, delay)
    }

    private fun getRadarAnimStringArray(rid: String, duration: String): String {
        val radHtml = ("http://weather.gc.ca/radar/index_e.html?id=$rid").getHtmlSep()
        var durationPatMatch = "<p>Short .1hr.:</p>(.*?)</div>"
        if (duration == "long") {
            durationPatMatch = "<p>Long .3hr.:</p>(.*?)</div>"
        }
        val radarHtml1Hr = radHtml.parse(durationPatMatch)
        val sb = StringBuilder()
        var tmpAl = radarHtml1Hr.parseColumn("display='(.*?)'&amp;")
        tmpAl.forEach {
            sb.append(":")
            sb.append("/data/radar/detailed/temp_image/")
            sb.append(rid)
            sb.append("/")
            sb.append(it)
            sb.append(".GIF")
        }
        tmpAl = radHtml.parseColumn("src=.(/data/radar/.*?GIF)\"")
        tmpAl.forEach {
            sb.append(":")
            sb.append(it)
        }
        return sb.toString()
    }

    internal fun getRadarAnimOptionsApplied(context: Context, rid: String, frameCntStr: String): AnimationDrawable {
        val urlStr = UtilityCanadaImg.getRadarAnimStringArray(rid, frameCntStr)
        val urlAl = urlStr.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        val baseUrl = "http://weather.gc.ca"
        val bmAl = mutableListOf<Bitmap>()
        urlAl.reverse()
        urlAl.asSequence().filter { it != "" }.mapTo(bmAl) { UtilityCanadaImg.getRadarBitmapOptionsApplied(context, rid, baseUrl + it.replace("detailed/", "")) }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl, UtilityImg.animInterval(context))
    }

    fun getRadarBitmapOptionsApplied(context: Context, rid: String, url: String): Bitmap {
        val urlImg: String
        if (url == "") {
            val radHtml = ("http://weather.gc.ca/radar/index_e.html?id=$rid").getHtml()
            val matchStr = "(/data/radar/.*?GIF)\""
            var summary = radHtml.parse(matchStr)
            summary = summary.replace("detailed/", "")
            urlImg = "http://weather.gc.ca/$summary"
        } else {
            urlImg = url
        }
        var layerCnt = 1
        if (GeographyType.CITIES.pref) {
            layerCnt = 2
        }
        val bitmapArr = mutableListOf<Bitmap>()
        val layers = mutableListOf<Drawable>()
        bitmapArr.add(urlImg.getImage())
        if (GeographyType.CITIES.pref) {
            val cityUrl = "http://weather.gc.ca/cacheable/images/radar/layers_detailed/default_cities/" + rid.toLowerCase(Locale.US) + "_towns.gif"
            val bmTmp = cityUrl.getImage()
            val bigBitmap = Bitmap.createBitmap(bmTmp.width, bmTmp.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bigBitmap)
            canvas.drawBitmap(bmTmp, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
            bitmapArr.add(bigBitmap)
        }
        (0 until layerCnt).forEach {
            val bmdr = if (it == 1) {
                BitmapDrawable(context.resources, UtilityImg.eraseBG(bitmapArr[it], -1))
            } else {
                BitmapDrawable(context.resources, bitmapArr[it])
            }
            layers.add(bmdr)
        }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun getRadarMosaicBitmapOptionsApplied(context: Context, sector: String): Bitmap {
        var url = "http://weather.gc.ca/radar/index_e.html?id=$sector"
        if (sector == "CAN") url = "http://weather.gc.ca/radar/index_e.html"
        val radHtml = url.getHtmlSep()
        val matchStr = "(/data/radar/.*?GIF)\""
        var summary = radHtml.parse(matchStr)
        summary = summary.replace("detailed/", "")
        var layerCnt = 1
        if (GeographyType.CITIES.pref) {
            layerCnt = 2
        }
        val bitmapArr = mutableListOf<Bitmap>()
        val layers = mutableListOf<Drawable>()
        bitmapArr.add(("http://weather.gc.ca/$summary").getImage())
        var sectorMap = sector.toLowerCase(Locale.US)
        var offset = 100
        if (sector == "CAN") {
            offset = 0
            sectorMap = "nat"
        }
        when (sector) {
            "WRN" -> sectorMap = "pnr"
            "PAC" -> sectorMap = "pyr"
            "ERN" -> sectorMap = "atl"
        }
        if (GeographyType.CITIES.pref) {
            val cityUrl = "http://weather.gc.ca/cacheable/images/radar/layers/composite_cities/" + sectorMap + "_composite.gif"
            val bmTmp = cityUrl.getImage()
            val bigBitmap = Bitmap.createBitmap(bmTmp.width + offset, bmTmp.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bigBitmap)
            canvas.drawBitmap(bmTmp, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
            bitmapArr.add(bigBitmap)
        }
        (0 until layerCnt).forEach { j ->
            val bmdr = if (j == 1) {
                BitmapDrawable(context.resources, UtilityImg.eraseBG(bitmapArr[j], -1)) // was -16777216
            } else {
                BitmapDrawable(context.resources, bitmapArr[j])
            }
            layers.add(bmdr)
        }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    internal fun getRadarMosaicAnimation(context: Context, sectorF: String, duration: String): AnimationDrawable {
        var sector = sectorF
        var url = "http://weather.gc.ca/radar/index_e.html?id=$sector"
        if (sector == "CAN") {
            url = "http://weather.gc.ca/radar/index_e.html"
        }
        val radHtml = url.getHtmlSep()
        if (sector == "CAN") {
            sector = "NAT"
        }
        var durationPatMatch = "<p>Short .1hr.:</p>(.*?)</div>"
        if (duration == "long") {
            durationPatMatch = "<p>Long .3hr.:</p>(.*?)</div>"
        }
        val radarHtml1Hr = radHtml.parse(durationPatMatch)
        val sb = StringBuilder()
        var tmpAl = radarHtml1Hr.parseColumn("display='(.*?)'&amp;")
        tmpAl.forEach {
            sb.append(":")
            sb.append("/data/radar/detailed/temp_image/COMPOSITE_")
            sb.append(sector)
            sb.append("/")
            sb.append(it)
            sb.append(".GIF")
        }
        tmpAl = radHtml.parseColumn("src=.(/data/radar/.*?GIF)\"")
        tmpAl.forEach {
            sb.append(":")
            sb.append(it)
        }
        val urlArr = sb.toString().split(":").dropLastWhile { it.isEmpty() }
        val urlAl = urlArr.mapTo(mutableListOf()) { "http://weather.gc.ca" + it.replace("detailed/", "") }
        urlAl.reverse()
        return UtilityImgAnim.getAnimationDrawableFromURLList(context, urlAl, UtilityImg.animInterval(context))
    }
}