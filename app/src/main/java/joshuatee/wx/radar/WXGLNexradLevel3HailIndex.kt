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

package joshuatee.wx.radar

import android.content.Context

import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.external.ExternalEllipsoid
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import java.io.File

object WXGLNexradLevel3HailIndex {

    private const val hiBaseFn = "nids_hi_tab"
    private const val markerSize = 0.015
    var hailList = mutableListOf<Hail>()


    private var initialized = false
    private var lastRefresh = 0.toLong()
    private const val REFRESH_LOC_MIN = 5

/*
    val hailData: MutableList<Hail>
        get() {
            var currentTime = System.currentTimeMillis()
            val currentTimeSec = currentTime / 1000
            val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
            if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
                hailList = mutableListOf()


                hailList.add(
                        Spotter(
                                tmpArr[0],
                                tmpArr[1],
                                tmpArr[2],
                                tmpArr[3],
                                )
                )

            }

            initialized = true
            currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000

            return hailList
        }
*/

    fun decodeAndPlot(context: Context, rid: String, fnSuffix: String): List<Double> {
        val stormList = mutableListOf<Double>()
        val retStr: String
        val location = UtilityLocation.getSiteLocation(context, rid)
        //make sure we clear the list or we get duplicate texts
        hailList.clear()
        //File("/sdcard/wX/hail").copyTo(File("/data/user/0/joshuatee.wx/files/nids_hi_tab0"), true);
        WXGLDownload.getNidsTab(context, "HI", rid, hiBaseFn + fnSuffix)
        val dis: UCARRandomAccessFile
        val posn: List<String>
        val hailPercent: List<String>
        val hailSize: List<String>
        try {
            dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, hiBaseFn + fnSuffix))
            dis.bigEndian = true
            retStr = UtilityLevel3TextProduct.read(dis)
            posn = retStr.parseColumn(RegExp.hiPattern1)
            hailPercent = retStr.parseColumn(RegExp.hiPattern2)
            hailSize = retStr.parseColumn(RegExp.hiPattern3)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
            return listOf()
        }
        var posnStr = ""
        var hailPercentStr = ""
        var hailSizeStr = ""
        posn.forEach { posnStr += it.replace("/", " ") }
        hailPercent.forEach { hailPercentStr += it.replace("/", " ") }
        hailPercentStr = hailPercentStr.replace("UNKNOWN", " 0 0 ")
        hailSize.forEach { hailSizeStr += it.replace("/", " ") }
        hailSizeStr = hailSizeStr.replace("UNKNOWN", " 0.00 ")
        hailSizeStr = hailSizeStr.replace("<0.25", " 0.25 ")
        hailSizeStr = hailSizeStr.replace("<0.50", " 0.50 ")
        val posnNumbers = posnStr.parseColumnAll(RegExp.stiPattern3)
        val hailPercentNumbers = hailPercentStr.parseColumnAll(RegExp.stiPattern3)
        val hailSizeNumbers = hailSizeStr.parseColumnAll(RegExp.hiPattern4)
        if (posnNumbers.size == hailPercentNumbers.size && posnNumbers.size > 1 && hailSizeNumbers.isNotEmpty()) {
            var degree: Double
            var nm: Double
            val bearing = DoubleArray(2)
            var start: ExternalGlobalCoordinates
            var ec: ExternalGlobalCoordinates
            var hailSizeDbl: Double
            var hailSizeText: String = "unknown"
            var k = 0 // k is used to track hail size which is /2 of other 2 arrays
            var s = 0
            while (s < posnNumbers.size) {
                hailSizeDbl = hailSizeNumbers[k].toDoubleOrNull() ?: 0.0
                UtilityLog.d("wx", "hailSizeNumbers: "+hailSizeDbl)
                if (hailSizeDbl > 0.24 && ((hailPercentNumbers[s].toIntOrNull() //was 49
                        ?: 0) > 30 || (hailPercentNumbers[s + 1].toDoubleOrNull() //was 60
                        ?: 0.0) > 30) //was 60
                ) {
                    val ecc = ExternalGeodeticCalculator()
                    degree = posnNumbers[s].toDoubleOrNull() ?: 0.0
                    nm = posnNumbers[s + 1].toDoubleOrNull() ?: 0.0
                    start = ExternalGlobalCoordinates(location)
                    ec = ecc.calculateEndingGlobalCoordinates(
                        ExternalEllipsoid.WGS84,
                        start,
                        degree,
                        nm * 1852.0,
                        bearing
                    )
                    stormList.add(ec.latitude)
                    stormList.add(ec.longitude * -1.0)

                    //FIXME make this pick a icon!

                    /*
                    if (hailSizeDbl > 0.99) {
                        stormList.add(ec.latitude + markerSize)
                        stormList.add(ec.longitude * -1.0)
                    }
                    if (hailSizeDbl > 1.99) {
                        stormList.add(ec.latitude + markerSize * 2.0)
                        stormList.add(ec.longitude * -1.0)
                    }
                    if (hailSizeDbl > 2.99) {
                        stormList.add(ec.latitude + markerSize * 3.0)
                        stormList.add(ec.longitude * -1.0)
                    }
                    if (hailSizeDbl > 3.99) {
                        stormList.add(ec.latitude + markerSize * 4.0)
                        stormList.add(ec.longitude * -1.0)
                    }
                    */


                    if (hailSizeDbl > 0.24) {
                        hailSizeText = "0.25"
                        WXGLRender.hailSizeIcon = "hail05.png"
                    }

                    if (hailSizeDbl > 0.49) {
                        hailSizeText = "0.50"
                        WXGLRender.hailSizeIcon = "hail0.png"
                    }
                    if (hailSizeDbl > 0.74) {
                        hailSizeText = "0.75"
                        WXGLRender.hailSizeIcon = "hail0.png"
                    }
                    if (hailSizeDbl > 0.99) {
                        hailSizeText = "1.00"
                        WXGLRender.hailSizeIcon = "hail1.png"
                    }
                    if (hailSizeDbl > 1.49) {
                        hailSizeText = "1.50"
                        WXGLRender.hailSizeIcon = "hail1.png"
                    }
                    if (hailSizeDbl > 1.99) {
                        hailSizeText = "2.00"
                        WXGLRender.hailSizeIcon = "hail2.png"
                    }
                    if (hailSizeDbl > 2.49) {
                        hailSizeText = "2.50"
                        WXGLRender.hailSizeIcon = "hail2.png"
                    }
                    if (hailSizeDbl > 2.99) {
                        hailSizeText = "3.00"
                        WXGLRender.hailSizeIcon = "hail3.png"
                    }
                    if (hailSizeDbl > 3.49) {
                        hailSizeText = "3.50"
                        WXGLRender.hailSizeIcon = "hail3.png"
                    }
                    if (hailSizeDbl > 3.99) {
                        hailSizeText = "4.00"
                        WXGLRender.hailSizeIcon = "hail4.png"
                    }
                    if (hailSizeDbl > 4.49) {
                        hailSizeText = "4.50"
                        WXGLRender.hailSizeIcon = "hail4.png"
                    }
                    //TODO add big hail --- only use 1 icon for any hail over 5 inch
                    if (hailSizeDbl > 4.99) {
                        hailSizeText = "Big Hail!"
                        WXGLRender.hailSizeIcon = "hailbig.png"
                    }




                    hailList.add(
                            Hail(WXGLRender.hailSizeIcon,
                                    hailSizeText,
                                    ec.latitude,
                                    ec.longitude * -1.0))




                    UtilityLog.d("wx", "hailSizeIcon: "+WXGLRender.hailSizeIcon)
                    UtilityLog.d("wx", "hailSizeText: "+hailSizeText)
                    UtilityLog.d("wx", "hailSizeDbl: "+hailSizeDbl)



                }
                k += 1
                s += 2
            }
        }

        return stormList
    }



}
